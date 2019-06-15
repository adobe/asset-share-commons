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
    private static final long DEFAULT_SIZE_LIMIT = -1L;

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
            this.maxContentSize = DEFAULT_SIZE_LIMIT;
            this.downloadContentSize = DEFAULT_SIZE_LIMIT;
        } else {
            calculateSizes();
        }    
    }
    
    private void calculateSizes() {
    	this.maxContentSize = assetDownloadHelper.getMaxContentSizeLimit();
        log.debug("Max allowed content size (in bytes) [ {} ]", this.maxContentSize);

        //check if needed to caclulate max content size
        if(this.maxContentSize > 0) {
            log.debug("Max content size set, requires calculation of download  content size.");
            this.downloadContentSize = assetDownloadHelper.getAssetDownloadSize(assets, request.getResource());
            log.debug("Requested download content size (in bytes) [ {} ]", this.downloadContentSize);
        } else {
        	this.downloadContentSize = DEFAULT_SIZE_LIMIT;
        }
    	
    }

    public Collection<AssetModel> getAssets() {
        return assets;
    }

    public String getZipFileName() {
        return StringUtils.removeEndIgnoreCase(zipFileName, ".zip");
    }

    @Override
    public long getMaxContentSize() {
        return this.maxContentSize;
    }

    @Override
    public long getDownloadContentSize() {
        return this.downloadContentSize;
    }

    @Override
    public String getMaxContentSizeLabel() {
        return UIHelper.getSizeLabel(getMaxContentSize(), request);
    }

    @Override
    public String getDownloadContentSizeLabel() {
        return UIHelper.getSizeLabel(getDownloadContentSize(), request);
    }
}