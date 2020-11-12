package com.adobe.aem.commons.assetshare.content.renditions.download;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface AssetRenditionsDownloadOrchestratorManager {

    /**
     * @param id the AssetRenditionsDownloadOrchestrator's id (aka the full class name)
     * @return the AssetRenditionsDownloadOrchestrator or null if none can be found registered to the provided id (class name)
     */
    AssetRenditionsDownloadOrchestrator getAssetRenditionsDownloadOrchestrator(final String id);
}
