/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionsHelper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;
import javax.inject.Named;

@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = AssetRendition.class
)
public class AssetRenditionImpl implements AssetRendition {
    private SlingHttpServletRequest request;
    private AssetModel asset;
    private String renditionName;
    private boolean download;

    @OSGiService
    private AssetRenditionsHelper assetRenditionsHelper;

    @Inject
    public AssetRenditionImpl(@Self SlingHttpServletRequest request,
                              @RequestAttribute @Named(value = "asset") AssetModel asset,
                              @RequestAttribute @Named(value = "renditionName") String renditionName,
                              @RequestAttribute @Named(value = "renditionDownload") @Default(booleanValues = false) boolean download) {
        this.request = request;
        this.asset = asset;
        this.renditionName = renditionName;
        this.download = download;
    }

    @Override
    public String getUrl() {
        return assetRenditionsHelper.getUrl(request, asset, new AssetRendition.UrlParams(renditionName, download));
    }
}
