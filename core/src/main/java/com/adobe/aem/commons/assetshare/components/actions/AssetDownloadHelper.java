package com.adobe.aem.commons.assetshare.components.actions;

import java.util.Collection;

import com.adobe.aem.commons.assetshare.content.AssetModel;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface AssetDownloadHelper {

    /**
     * @return the max size in bytes that the AEM AssetDownloadServlet allows to be downloaded at once.
     */
    long getMaxContentSizeLimit();

    /**
     * Computes the size of the assets requested to be downloaded.
     * Note that this only computes the size of the ORIGINAL renditions.
     *
     * @param assets the assets to compute the size of.
     * @param configResource the resource
     * @return size of the assets.
     */
    long getAssetDownloadSize(Collection<AssetModel> assets, Resource configResource);
}