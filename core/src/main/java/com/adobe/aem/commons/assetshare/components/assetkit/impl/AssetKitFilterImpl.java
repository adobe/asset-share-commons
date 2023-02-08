/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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
