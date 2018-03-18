package com.adobe.aem.commons.assetshare.components.details.impl;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.aem.commons.assetshare.components.details.Video;
import com.adobe.aem.commons.assetshare.content.AssetModel;

@Model(
        adaptables = { SlingHttpServletRequest.class },
        adapters = { Video.class },
        resourceType = { VideoImpl.RESOURCE_TYPE },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class VideoImpl extends AbstractEmptyTextComponent implements Video {

    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/video";

    @Self
    @Required
    private AssetModel asset;

    @ValueMapValue
    private String computedProperty;

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
            src = combinedProperties.get(computedProperty, String.class);
        }
        return src;
    }

}
