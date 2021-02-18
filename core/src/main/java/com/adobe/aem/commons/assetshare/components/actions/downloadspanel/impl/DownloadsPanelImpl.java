package com.adobe.aem.commons.assetshare.components.actions.downloadspanel.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.downloadspanel.DownloadsPanel;
import com.adobe.aem.commons.assetshare.content.async.download.AsyncDownload;
import com.adobe.cq.dam.download.api.DownloadArtifact;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadProgress;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.acs.commons.util.CookieUtil;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;

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
		if (downloadStatusList == null && downloadIds != null) {
			downloadStatusList = new ArrayList<>();

			try {
				for (String downloadId : downloadIds) {
					DownloadStatus downloadStatus = getDownloadStatusByID(request.getResourceResolver(), downloadId);
					downloadStatusList.add(downloadStatus);
				}
			} catch (Exception e) {
				log.error("Error while processing downloads info");
			}
		}

		return Collections.unmodifiableList(downloadStatusList);
	}

	private DownloadStatus getDownloadStatusByID(ResourceResolver resourceResolver, String downloadId)
			throws DownloadException {

		DownloadProgress progress = asyncDownload.getDownloadStatus(resourceResolver, downloadId);
		DownloadStatus downloadStatus = null;
		DownloadArtifact downloadArtifact = getDownloadArtifact(progress);

		downloadStatus = new DownloadStatus(downloadArtifact.getName(), downloadArtifact.getBinaryURI().toString(),
				progress.getTotalSize(), downloadId, progress.getProgress(), progress.getStatus().toString(),
				progress.getTotalCount(), downloadArtifact.getSuccesses());
		return downloadStatus;
	}

	private DownloadArtifact getDownloadArtifact(DownloadProgress progress) {

		DownloadArtifact returningArtifact = null;
		if (null != progress) {
			// retrieve successfully generated URIs
			for (DownloadArtifact artifact : progress.getArtifacts()) {
				if (artifact.getBinaryURI() != null) {
					returningArtifact = artifact;
					break;
				}
			}
		}
		return returningArtifact;
	}

	public Collection<String> getPaths() {
		return new ArrayList<>(paths);
	}
}