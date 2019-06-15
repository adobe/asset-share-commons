/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Image;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import com.adobe.aem.commons.assetshare.util.UrlUtil;
import com.day.cq.dam.api.Rendition;
import com.day.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.net.URLDecoder;
import java.util.regex.Pattern;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Image.class},
        resourceType = {ImageImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class ImageImpl extends AbstractEmptyTextComponent implements Image {
    private static final Logger log = LoggerFactory.getLogger(ImageImpl.class);
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/image";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    @OSGiService
    @Required
    private MimeTypeHelper mimeTypeHelper;

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

    @ValueMapValue
    private String fallbackSrc;

    private ValueMap combinedProperties;

    private String src = null;

    @PostConstruct
    public void init() {
        if (asset != null) {
            combinedProperties = asset.getProperties();
        }
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

            if (StringUtils.isBlank(tmp)) {
                tmp = fallbackSrc;
            }

            src = UrlUtil.escape(tmp);
        }

        return src;
    }


    @Deprecated
    private String getLegacySrc() {
        src = combinedProperties.get(computedProperty, String.class);

        if (StringUtils.isBlank(src) && StringUtils.isNotBlank(renditionRegex)) {
            final Pattern pattern = Pattern.compile(renditionRegex);

            for (final Rendition rendition : asset.getRenditions()) {
                if (pattern.matcher(rendition.getName()).matches() &&
                        mimeTypeHelper.isBrowserSupportedImage(rendition.getMimeType())) {
                    src = rendition.getPath();
                    break;
                }
            }
        }

        return src;
    }

    @Override
    public String getAlt() {
        return asset.getTitle();
    }

    @Override
    public String getFallback() { return UrlUtil.escape(fallbackSrc); }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(getSrc());
    }

    @Override
    public boolean isReady() {
        return !isEmpty();
    }

    boolean isLegacyMode() {
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
}
