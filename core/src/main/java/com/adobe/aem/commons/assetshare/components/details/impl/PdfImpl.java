package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Pdf;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.util.AdobePdfEmbedApi;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.UrlUtil;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.wcm.api.designer.Style;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.UUID;

/**
 * Sling Model for PDF Component
 */
@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Pdf.class, ComponentExporter.class},
        resourceType = {PdfImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class PdfImpl extends AbstractEmptyTextComponent implements Pdf {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/pdf";
    private static final String SIZED_CONTAINER = "SIZED_CONTAINER";
    private static final String IN_LINE = "IN_LINE";
    private static final String FULL_WINDOW = "FULL_WINDOW";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    @OSGiService
    @Required
    private AssetRenditions assetRenditions;

    @OSGiService
    @Required
    private RequireAem requireAem;

    @OSGiService
    @Optional
    private AdobePdfEmbedApi adobePdfEmbedApi;

    @ValueMapValue
    private String renditionName;

    @ScriptVariable
    private Style currentStyle;

    private String src = null;

    private String viewerId = null;

    private String clientId = null;

    private ValueMap properties = new ValueMapDecorator(new HashMap<>());

    @PostConstruct
    public void init() {
        properties = request.getResource().getValueMap();
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(getSrc()) || StringUtils.isBlank(getClientId());
    }

    @Override
    public boolean isReady() {
        return !isEmpty();
    }

    @Override
    public String getSrc() {
        if (src == null) {
            String tmp = null;

            if (asset != null && StringUtils.isNotBlank(renditionName)) {
                final AssetRenditionParameters parameters =
                        new AssetRenditionParameters(asset, renditionName, false);
                tmp = assetRenditions.getUrl(request, asset, parameters);
            }

            src = UrlUtil.escape(tmp);
        }

        return src;
    }

    @Override
    public String getFileName() {
        return asset.getName();
    }

    @Override
    public String getClientId() {

        if (clientId == null) {
            // "clientId" property is the legacy value before we added support for author and publish clientIds.
            if (RequireAem.ServiceType.AUTHOR.equals(requireAem.getServiceType())) {
                clientId = currentStyle.get("authorClientId", currentStyle.get("clientId", String.class));
            } else {
                clientId = currentStyle.get("publishClientId", currentStyle.get("clientId", String.class));
            }

            // Fallback to the AdobePdfEmbedApi OSGi service defined Adobe PDF Embed API Client ID
            if (StringUtils.isBlank(clientId) && adobePdfEmbedApi != null) {
                clientId = adobePdfEmbedApi.getClientId();
            }
        }

        return clientId;
    }

    @Override
    public String getViewerId() {
        if (viewerId == null) {
            viewerId = "asset_share_commons__adobe_dc_view__" + StringUtils.replace(StringUtils.lowerCase(UUID.randomUUID().toString()), "-", "");
        }

        return viewerId;
    }


    @Override
    public String getEmbedMode() {
        return properties.get("embedMode", "SIZED_CONTAINER");
    }

    @Override
    public String getDefaultViewMode() {
        return properties.get("defaultViewMode", "FIT_WIDTH");
    }

    @Override
    public String getHeight() {
        int height = getEmbedModeProperty("height", 100);

        if (height > 100) {
            return height + "px";
        } else {
            return "auto";
        }
    }

    @Override
    public boolean isReadOnly() {
        return getEmbedModeProperty("hasReadOnlyAccess", true);
    }

    @Override
    public boolean showFullScreen() {
        return getEmbedModeProperty("showFullScreen", false);
    }

    @Override
    public boolean showDownload() {
        return getEmbedModeProperty("showDownloadPDF", false);
    }

    @Override
    public boolean showPrint() {
        return getEmbedModeProperty("showPrintPDF", false);
    }

    @Override
    public boolean showZoomControl() {
        return getEmbedModeProperty("showZoomControl", false);
    }

    @Override
    public boolean showBookmarks() {
        return getEmbedModeProperty("showBookmarks", false);
    }

    @Override
    public boolean showAnnotationTools() {
        return getEmbedModeProperty("showAnnotationTools", false);
    }

    @Override
    public boolean isLinearizationEnabled() {
        return properties.get("enableLinearization", false);
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }


    private <T> T getEmbedModeProperty(String propertyName, T defaultValue) {
        String embedMode = StringUtils.replace(getEmbedMode(), "_", "-").toLowerCase();
        T value = properties.get(embedMode + "/" + propertyName, defaultValue);
        return value;
    }
}
