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

package com.adobe.aem.commons.assetshare.components.actions.download.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.download.Download;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.commons.util.UIHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Download.class},
        resourceType = {DownloadImpl.RESOURCE_TYPE}
)
public class DownloadImpl implements Download {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/modals/download";
    private static final Logger log = LoggerFactory.getLogger(DownloadImpl.class);

    @Self
    @Required
    protected SlingHttpServletRequest request;

    @ValueMapValue
    @Optional
    @Default(values = "Assets")
    protected String zipFileName;

    @ValueMapValue
    @Optional
    protected Long maxContentSize;

    @OSGiService
    @Required
    protected ActionHelper actionHelper;

    protected Collection<AssetModel> assets = new ArrayList<>();

    private Long downloadContentSize;

    @PostConstruct
    protected void init() {
        assets = actionHelper.getAssetsFromQueryParameter(request, "path");

        if (assets.isEmpty()) {
            assets = actionHelper.getPlaceholderAsset(request);
            downloadContentSize = -1L;
        } else {
            downloadContentSize = calculateDownloadContentSize(assets);
        }
    }

    public Collection<AssetModel> getAssets() {
        return assets;
    }

    public String getZipFileName() {
        return StringUtils.removeEndIgnoreCase(zipFileName, ".zip");
    }

    @Override
    public boolean isMaxContentSize() {
        if(maxContentSize != null && maxContentSize < downloadContentSize) {
            return true;
        }
        return false;
    }

    @Override
    public String getMaxContentSizeLimit() {
        return UIHelper.getSizeLabel(maxContentSize, request);
    }

    @Override
    public String getDownloadContentSize() {
        return UIHelper.getSizeLabel(downloadContentSize, request);
    }

    private long calculateDownloadContentSize(Collection<AssetModel> assets) {
        Long contentSize = 0L;
        for(AssetModel asset : assets) {
            contentSize += asset.getProperties().get(DamConstants.DAM_SIZE, 0L);
        }
        return contentSize;
    }
}