package com.adobe.aem.commons.assetshare.components.actions.downloadspanel.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.downloadspanel.DownloadsPanel;
import com.adobe.cq.dam.download.api.DownloadArtifact;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadProgress;
import com.adobe.cq.dam.download.api.DownloadService;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.wcm.api.WCMMode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Model(
		adaptables = { SlingHttpServletRequest.class },
		adapters = { DownloadsPanel.class },
		resourceType = DownloadsPanelImpl.RESOURCE_TYPE
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class DownloadsPanelImpl implements DownloadsPanel, ComponentExporter {

	private static final Logger log = LoggerFactory.getLogger(DownloadsPanelImpl.class);
	
	static final String RESOURCE_TYPE = "asset-share-commons/components/modals/downloads-panel";

	@Self
	@Required
	private SlingHttpServletRequest request;

	@SlingObject
	@Required
	private ResourceResolver resourceResolver;

	@OSGiService
	private ActionHelper actionHelper;

	@OSGiService
	private DownloadService downloadService;

	private List<DownloadProgress> activeDownloads = null;

	@Override
	public List<DownloadProgress> getDownloadProgresses() throws DownloadException {
		if (activeDownloads == null) {
			activeDownloads = new ArrayList<>();

			if (!WCMMode.EDIT.equals(WCMMode.fromRequest(request))) {
				activeDownloads = StreamSupport.stream(downloadService.getDownloadIds(resourceResolver).spliterator(), false)
						.map(id -> {
							try {
								return downloadService.getProgress(id, resourceResolver);
							} catch (DownloadException e) {
								log.warn("Unable to get async DownloadProgress for downloadId [ {} ] for user [ {} ]", id, resourceResolver.getUserID(), e);
								return null;
							}
						})
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
			} else {
				activeDownloads.add(new PlaceholderDownloadProgress());
				activeDownloads.add(new PlaceholderDownloadProgress());

			}
		}

		return Collections.unmodifiableList(activeDownloads);
	}

	@Nonnull
	@Override
	public String getExportedType() {
		return RESOURCE_TYPE;
	}
}