package com.adobe.aem.commons.assetshare.components.actions;

import java.util.Collection;

import com.adobe.aem.commons.assetshare.content.AssetModel;

import org.apache.sling.api.resource.Resource;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface AssetDownloadHelper {

    public long getMaxContentSizeLimit();

    public long computeAssetDownloadSize(Collection<AssetModel> assets, Resource requestResource);
}