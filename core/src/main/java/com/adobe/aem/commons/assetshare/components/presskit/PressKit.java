package com.adobe.aem.commons.assetshare.components.presskit;


import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface PressKit {

    List<AssetModel> getAssets();
}
