package com.adobe.aem.commons.assetshare.components.actions.downloads.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.downloads.Downloads;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadProgress;
import com.adobe.cq.dam.download.api.DownloadService;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.wcm.api.WCMMode;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
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
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Model(
        adaptables = { SlingHttpServletRequest.class },
        adapters = { Downloads.class },
        resourceType = DownloadsImpl.RESOURCE_TYPE
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class DownloadsImpl implements Downloads, ComponentExporter {

    private static final Logger log = LoggerFactory.getLogger(DownloadsImpl.class);

    static final String RESOURCE_TYPE = "asset-share-commons/components/modals/downloads";
    private static final String REQ_PARAM_DOWNLOAD_IDS = "downloadId";

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
                final List allowedDownloadIds = getAllowedDownloadIds(request);
                final Calendar now = Calendar.getInstance();
                activeDownloads = StreamSupport.stream(downloadService.getDownloadIds(resourceResolver).spliterator(), false)
                        .filter(id -> allowedDownloadIds.contains(id))
                        .map(id -> {
                            try {
                                return downloadService.getProgress(id, resourceResolver);
                            } catch (DownloadException e) {
                                log.warn("Unable to get async DownloadProgress for downloadId [ {} ] for user [ {} ]", id, resourceResolver.getUserID(), e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter((dp) -> {
                            // Filter out any Download Progress that is older than 1 hour
                                if (dp.getFinished() == null) {
                                return true;
                            } else {
                                return Duration.between(dp.getFinished().toInstant(), now.toInstant()).abs().toHours() <= 1;
                            }
                        })
                        .collect(Collectors.toList());
            } else {
                activeDownloads.add(new PlaceholderDownloadProgress());
            }
        }

        return Collections.unmodifiableList(activeDownloads);
    }

    /**
     * Collect the downloadIds the user provides to let AEM know which DownloadProgresses should be returned.
     * @param request the request
     * @return the list of downloadIds the user should be able to request access to.
     */
    private List<String> getAllowedDownloadIds(final SlingHttpServletRequest request) {
        RequestParameter[] requestParameters = request.getRequestParameters(REQ_PARAM_DOWNLOAD_IDS);

        if (requestParameters != null) {
            return Arrays.stream(requestParameters).map(rp -> rp.getString()).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}