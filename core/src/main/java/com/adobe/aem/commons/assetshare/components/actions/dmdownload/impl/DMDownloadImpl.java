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

package com.adobe.aem.commons.assetshare.components.actions.dmdownload.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.dmdownload.DMDownload;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.dam.entitlement.api.EntitlementConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.featureflags.Features;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {DMDownload.class},
        resourceType = {DMDownloadImpl.RESOURCE_TYPE},
        cache = true
)
public class DMDownloadImpl implements DMDownload {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/modals/dmdownload";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @ValueMapValue
    @Optional
    @Default(values = "Assets")
    private String zipFileName;

    @ValueMapValue
    @Optional
    @Default(values = {})
    private String[] imagePresets;

    @OSGiService
    @Required
    private ActionHelper actionHelper;

    @OSGiService
    @Required
    private Features features;

    @SlingObject
    private ResourceResolver resourceResolver;

    private final String SCENE7_FEATURE_FLAG = "com.adobe.dam.asset.scene7.feature.flag";

    private Collection<AssetModel> assets = new ArrayList<>();

    private boolean isDynamicMediaEnabled;

    @PostConstruct
    protected void init() {
        if(features == null) {
            isDynamicMediaEnabled = false;
            return;
        }

        if(features.isEnabled(EntitlementConstants.ASSETS_DYNAMICMEDIA_FEATURE_FLAG_PID) ||
                features.isEnabled(SCENE7_FEATURE_FLAG)) {
            isDynamicMediaEnabled = true;
        }

        assets = actionHelper.getAssetsFromQueryParameter(request, "path");

        if (assets.isEmpty()) {
            assets = actionHelper.getPlaceholderAsset(request);
        }
    }

    public Collection<AssetModel> getAssets() {
        return assets;
    }

    public String getZipFileName() {
        return StringUtils.removeEndIgnoreCase(zipFileName, ".zip");
    }

    public boolean isDynamicMediaEnabled() { return isDynamicMediaEnabled; }

    public Collection<String> getImagePresets() { return Arrays.asList(imagePresets); }
}