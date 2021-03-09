package com.adobe.aem.commons.assetshare.util;

import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import com.adobe.aem.commons.assetshare.content.AssetModel;

@ProviderType
public interface DownloadHelper {

    /**
     * @param mimeType the mime type to check
     * @return true if the mime type is configured to download.
     */
    public List<AssetModel> getAssets(final SlingHttpServletRequest request);


}
