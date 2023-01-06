package com.adobe.aem.commons.assetshare.components.assetkit;


import com.adobe.aem.commons.assetshare.components.Component;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Collection;

@ProviderType
public interface AssetKit extends Component {
    Collection<? extends AssetModel> getAssets();
    boolean isReady();

    @ConsumerType
    interface Filter {
        Collection<? extends AssetModel> filter(Collection<? extends AssetModel> assets);
    }
}
