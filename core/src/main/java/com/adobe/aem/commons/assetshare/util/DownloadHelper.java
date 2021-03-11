package com.adobe.aem.commons.assetshare.util;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import com.adobe.aem.commons.assetshare.content.AssetModel;

@ProviderType
public interface DownloadHelper {

    /**
     * @param request the SlingHttpServletRequest object to get assets 
     * @return List of assets 
     */
    public List<AssetModel> getAssets(final SlingHttpServletRequest request);


}
