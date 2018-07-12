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

package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.configuration.AssetDetails;
import com.adobe.aem.commons.assetshare.configuration.AssetDetailsResolver;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.inject.Inject;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {AssetDetails.class}
)
public class AssetDetailsImpl implements AssetDetails {
    @Self
    @Required
    private SlingHttpServletRequest request;

    @Inject
    @Required
    private AssetModel asset;

    @Self
    @Required
    private Config config;

    @OSGiService
    @Required
    private AssetDetailsResolver assetDetailsResolver;

    private String url = null;

    @Override
    public String getUrl() {
        if (url == null) {
            url = assetDetailsResolver.getUrl(config, asset);
        }

        return url;
    }

    @Override
    public String getFullUrl() {
        String fullUrl = getUrl();

        if (config.getAssetDetailReferenceById()) {
            fullUrl += "/" + asset.getAssetId() + ".html";
        } else {
            fullUrl += asset.getPath();
        }

        return fullUrl;
    }
}