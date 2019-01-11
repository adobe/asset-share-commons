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

package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.configuration.AssetDetailsResolver;
import com.adobe.aem.commons.assetshare.configuration.AssetDetailsSelector;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(
        reference = {
                @Reference(
                        name = "assetDetailSelector",
                        bind = "bindAssetDetailsSelector",
                        unbind = "unbindAssetDetailsSelector",
                        service = AssetDetailsSelector.class,
                        policy = ReferencePolicy.DYNAMIC,
                        policyOption = ReferencePolicyOption.GREEDY,
                        cardinality = ReferenceCardinality.MULTIPLE
                )
        }
)
public class AssetDetailsResolverImpl implements AssetDetailsResolver {

    private Map<String, AssetDetailsSelector> assetDetailsSelectors = new ConcurrentHashMap<String, AssetDetailsSelector>();

    public String getUrl(final Config config, final AssetModel asset) {
        String url = null;

        for (final AssetDetailsSelector selector : assetDetailsSelectors.values()) {
            if (selector.accepts(config, asset)) {
                url = selector.getUrl(config, asset);
                break;
            }
        }

        if (StringUtils.isBlank(url) ||
                ResourceUtil.isNonExistingResource(config.getResourceResolver().resolve(url))) {
            url = config.getAssetDetailsUrl();
        }

        return url;
    }

    public String getFullUrl(final Config config, final AssetModel asset) {
        String fullUrl = getUrl(config, asset);

        if (StringUtils.isNotBlank(fullUrl)) {
            if (config.getAssetDetailReferenceById()) {
                fullUrl += "/" + asset.getAssetId() + ".html";
            } else {
                fullUrl += asset.getPath();
            }
        }

        return fullUrl;
    }

    protected final void bindAssetDetailsSelector(final AssetDetailsSelector service, final Map<Object, Object> props) {
        final String type = service.getClass().getName();
        if (type != null) {
            this.assetDetailsSelectors.put(type, service);
        }
    }

    protected final void unbindAssetDetailsSelector(final AssetDetailsSelector service, final Map<Object, Object> props) {
        final String type = service.getClass().getName();
        if (type != null) {
            this.assetDetailsSelectors.remove(type);
        }
    }
}