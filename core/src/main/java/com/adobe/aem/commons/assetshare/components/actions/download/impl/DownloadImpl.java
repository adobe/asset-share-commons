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
import com.adobe.aem.commons.assetshare.components.actions.AssetDownloadHelper;
import com.adobe.aem.commons.assetshare.components.actions.download.Download;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.jobs.AssetDownloadService;
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

    @OSGiService
    @Required
    protected ActionHelper actionHelper;

    
    @OSGiService
    @Required
    protected AssetDownloadHelper assetDownloadHelper;

    protected Collection<AssetModel> assets = new ArrayList<>();

    /***
     * Max content size retrieved from com.day.cq.dam.core.impl.servlet.AssetDownloadServlet
     */
    protected Long maxContentSize;

    /***
     * Potential download size of current assets
     */
    protected Long downloadContentSize;

    @PostConstruct
    protected void init() {
        assets = actionHelper.getAssetsFromQueryParameter(request, "path");
        if (assets.isEmpty()) {
            assets = actionHelper.getPlaceholderAsset(request);
        } else {
            this.maxContentSize = assetDownloadHelper.getMaxContentSizeLimit();
            log.info("Max Content Size: " + this.maxContentSize);
            
            this.downloadContentSize = assetDownloadHelper.computeAssetDownloadSize(assets, request.getResource());
            log.info("Download content size: " + this.downloadContentSize);
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
        if(maxContentSize != null && maxContentSize > 0 &&  maxContentSize < downloadContentSize) {
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
}