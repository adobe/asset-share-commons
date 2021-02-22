package com.adobe.aem.commons.assetshare.components.actions.downloadspanel.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.downloadspanel.DownloadsPanel;
import com.adobe.aem.commons.assetshare.content.async.download.AsyncDownload;
import com.adobe.cq.dam.download.api.DownloadArtifact;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadProgress;
import com.day.cq.wcm.api.WCMMode;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Model(adaptables = { SlingHttpServletRequest.class }, adapters = { DownloadsPanel.class })
public class DownloadsPanelImpl implements DownloadsPanel {

	private static final Logger log = LoggerFactory.getLogger(DownloadsPanelImpl.class);
	private static final String ASC_DOWNLOAD_COOKIE = "ADC";

	@Self
	@Required
	private SlingHttpServletRequest request;

	@OSGiService
	private ActionHelper actionHelper;

	@OSGiService
	private AsyncDownload asyncDownload;

	private Collection<String> paths = new ArrayList<>();
	private String[] downloadIds = null;
	protected List<DownloadStatus> downloadStatusList = null;

	@PostConstruct
	protected void init() {
		try {
			String requestDLs = request.getParameter("downloadIds");
			if (requestDLs != null) {
				downloadIds = requestDLs.split(",");
			}
		} catch (Exception e) {
			log.error("Error While processing Async download ", e);
		}
	}

	@Override
	public List<DownloadStatus> getDownloadStatus() {
		downloadStatusList = new ArrayList<>();

		try {
			if (downloadIds != null) {
				for (String downloadId : downloadIds) {
					DownloadStatus downloadStatus = getDownloadStatusByID(request.getResourceResolver(), downloadId);
					downloadStatusList.add(downloadStatus);
				}
			} else {
				if (!WCMMode.DISABLED.equals(WCMMode.fromRequest(request))) {
					DownloadStatus downloadPlaceholderStatus = getPlaceholderDownloadStatus();
					downloadStatusList.add(downloadPlaceholderStatus);
				}

			}
		} catch (Exception e) {
			log.error("Error while processing downloads info", e);
		}

		return Collections.unmodifiableList(downloadStatusList);
	}

	private DownloadStatus getDownloadStatusByID(ResourceResolver resourceResolver, String downloadId)
			throws DownloadException {

		DownloadProgress progress = asyncDownload.getDownloadStatus(resourceResolver, downloadId);
		DownloadStatus downloadStatus = null;
		DownloadArtifact downloadArtifact = getDownloadArtifact(progress);

		if (downloadArtifact != null) {
			downloadStatus = new DownloadStatus(downloadArtifact.getName(), downloadArtifact.getBinaryURI().toString(),
					progress.getTotalSize(), downloadId, progress.getProgress(), progress.getStatus().toString(),
					progress.getTotalCount(), downloadArtifact.getSuccesses());
		} else {
			downloadStatus = new DownloadStatus("Failed", "", 0, downloadId, progress.getProgress(),
					progress.getStatus().toString(), progress.getTotalCount(), null);
		}

		return downloadStatus;
	}
	

	public final DownloadStatus getPlaceholderDownloadStatus() {
		DownloadStatus placeHolderDownloadStatus = null;
		placeHolderDownloadStatus = new DownloadStatus("timestamp.zip", "", 0, "", 0, "Status", 0, null);
		return placeHolderDownloadStatus;
	}

	private DownloadArtifact getDownloadArtifact(DownloadProgress progress) {

		DownloadArtifact returningArtifact = null;
		// retrieve successfully generated URIs
		for (DownloadArtifact artifact : progress.getArtifacts()) {
			if (artifact.getBinaryURI() != null) {
				returningArtifact = artifact;
				break;
			}
		}

		return returningArtifact;
	}

}