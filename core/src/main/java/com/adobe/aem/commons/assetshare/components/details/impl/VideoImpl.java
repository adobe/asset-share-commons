package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Video;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.util.UrlUtil;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.regex.Pattern;

/**
 * Sling Model for Video Component
 */
@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Video.class, ComponentExporter.class},
        resourceType = {VideoImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class VideoImpl extends AbstractEmptyTextComponent implements Video {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/video";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    @OSGiService
    @Required
    private AssetRenditions assetRenditions;

    /**
     * @deprecated replaced by renditionName
     */
    @Deprecated
    @ValueMapValue
    private String computedProperty;

    /**
     * @deprecated replaced by renditionName
     */
    @Deprecated
    @ValueMapValue
    private String renditionRegex;

    @ValueMapValue
    private Boolean legacyMode;

    @ValueMapValue
    private String renditionName;

    private ValueMap combinedProperties;

    private String src = null;

    @PostConstruct
    public void init() {
        if (asset != null) {
            combinedProperties = asset.getProperties();
        }
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(getSrc());
    }

    @Override
    public boolean isReady() {
        return !isEmpty();
    }

    @Override
    public String getSrc() {
        if (src == null) {
            String tmp = null;

            if (!isLegacyMode()) {
                if (asset != null && StringUtils.isNotBlank(renditionName)) {
                    final AssetRenditionParameters parameters =
                            new AssetRenditionParameters(asset, renditionName, false);
                    tmp = assetRenditions.getUrl(request, asset, parameters);
                }
            } else {
                tmp = getLegacySrc();
            }

            src = UrlUtil.escape(tmp);
        }

        return src;
    }

    @Deprecated
    private String getLegacySrc() {
        String src = combinedProperties.get(computedProperty, String.class);

        if (StringUtils.isBlank(src) && StringUtils.isNotBlank(renditionRegex)) {
            fetchSrcFromRegex();
        }

        return src;
    }

    /**
     * Method fetches the rendition path from regex
     */
    @Deprecated
    private void fetchSrcFromRegex() {
        final Pattern pattern = Pattern.compile(renditionRegex);

        for (final Rendition rendition : asset.getRenditions()) {
            if (!"video/x-flv".equalsIgnoreCase(rendition.getMimeType())
                    && pattern.matcher(rendition.getName()).matches()) {
                src = rendition.getPath();
                break;
            }
        }
    }

    @Override
    public boolean isVideoAsset() {
        if (null != asset && null != asset.getResource()) {
            return DamUtil.isVideo(asset.getResource().adaptTo(Asset.class));
        }

        return false;
    }

    protected boolean isLegacyMode() {
        if (legacyMode == null) {
            if (StringUtils.isNotBlank(renditionName)) {
                return false;
            } else {
                return StringUtils.isNotBlank(computedProperty) || StringUtils.isNotBlank(renditionRegex);
            }
        } else {
            return legacyMode;
        }
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}
