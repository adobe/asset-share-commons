package com.adobe.aem.commons.assetshare.components.assetkit.impl;

import com.adobe.aem.commons.assetshare.components.assetkit.AssetKit;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component(property = {
        "service.ranking:Integer=-10000"
})
public class AssetKitFilterImpl implements AssetKit.Filter {

    @Override
    public Collection<? extends AssetModel> filter(final Collection<? extends AssetModel> assets) {
        return assets.stream().filter(asset -> !StringUtils.equals(StringUtils.lowerCase(asset.getTitle()), "banner")).collect(Collectors.toList());
    }
}
