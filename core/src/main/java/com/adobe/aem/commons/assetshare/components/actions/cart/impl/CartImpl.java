/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.components.actions.cart.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.cart.Cart;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Cart.class},
        resourceType = {CartImpl.RESOURCE_TYPE}
)
public class CartImpl implements Cart {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/modals/cart";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @OSGiService
    @Required
    private ActionHelper actionHelper;

    private Collection<AssetModel> assets = new ArrayList<>();

    private Collection<String> paths = new ArrayList<>();

    @PostConstruct
    protected void init() {
        assets = actionHelper.getAssetsFromQueryParameter(request, "path");

        if (assets.isEmpty()) {
            assets = actionHelper.getPlaceholderAsset(request);
        }

        for (final AssetModel asset : assets) {
            paths.add(asset.getPath());
        }
    }

    public Collection<AssetModel> getAssets() {
        return assets;
    }

    public Collection<String> getPaths() {
        return paths;
    }
}