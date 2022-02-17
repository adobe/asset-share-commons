package com.adobe.aem.commons.assetshare.components.actions;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;

@ProviderType
public interface AssetDownloadHelper {

    /**
     * This only applies to AEM 6.x. AEM as a Cloud Service has no upper limit.
     *
     * @return the max size in bytes that the AEM AssetDownloadServlet allows to be downloaded at once.
     **/
    long getMaxContentSizeLimit();

    /**
     * Computes the size of the assets requested to be downloaded.
     * Note that this only computes the size of the ORIGINAL renditions.
     *
     * This only applies to AEM 6.x. AEM as a Cloud Service provides this as part of Async Download Framework response.
     *
     * @param assets the assets to compute the size of.
     * @param configResource the resource
     * @return size of the assets.
     */
    long getAssetDownloadSize(Collection<AssetModel> assets, Resource configResource);
}