package com.adobe.aem.commons.assetshare.components.details.impl;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.aem.commons.assetshare.components.details.Audio;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.util.UrlUtil;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.util.AbstractComponentImpl;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.commons.util.DamUtil;
import com.drew.lang.annotations.NotNull;

@Model(adaptables = { SlingHttpServletRequest.class }, adapters = { Audio.class, ComponentExporter.class }, resourceType = {
    AudioImpl.RESOURCE_TYPE }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class AudioImpl extends AbstractComponentImpl implements Audio {

    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/audio";

    @Self
    private SlingHttpServletRequest request;

    @Self
    private AssetModel asset;

    @OSGiService
    private AssetRenditions assetRenditions;

    @ValueMapValue
    private String renditionName;

    @Override
    public String getSrc() {
        return Optional.ofNullable(asset)
                .map(this::getRenditionUrl)
                .map(UrlUtil::escape)
                .orElse(StringUtils.EMPTY);
    }

    @Override
    public boolean isAudioAsset() {
        return Optional.ofNullable(asset)
                .map(AssetModel::getResource)
                .map(DamUtil::resolveToAsset)
                .filter(DamUtil::isAudio)
                .isPresent();
    }

    @Override
    public boolean hasEmptyText() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(getSrc());
    }

    @Override
    public boolean isReady() {
        return !isEmpty();
    }

    public String getRenditionName() {
        return Optional.ofNullable(renditionName)
                .orElse(DamConstants.ORIGINAL_FILE);
    }

    private String getRenditionUrl(AssetModel asset) {
        AssetRenditionParameters parameters = new AssetRenditionParameters(asset, getRenditionName(), false);
        return Optional.ofNullable(assetRenditions)
                .map(rendition -> rendition.getUrl(request, asset, parameters))
                .orElse(StringUtils.EMPTY);
    }


}
