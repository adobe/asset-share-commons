/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions.impl;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.google.common.collect.ImmutableList;
import org.apache.sling.commons.osgi.Order;
import org.apache.sling.commons.osgi.RankedServices;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(
        reference = {
                @Reference(
                        name = "assetRenditionDispatcher",
                        bind = "bindAssetRenditionDispatcher",
                        unbind = "unbindAssetRenditionDispatcher",
                        service = AssetRenditionDispatcher.class,
                        policy = ReferencePolicy.DYNAMIC,
                        policyOption = ReferencePolicyOption.GREEDY,
                        cardinality = ReferenceCardinality.MULTIPLE
                )
        }
)
public class AssetRenditionDispatchersImpl implements AssetRenditionDispatchers {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionDispatchersImpl.class);

    private final RankedServices<AssetRenditionDispatcher> assetRenditionDispatchers = new RankedServices<>(Order.DESCENDING);

    protected void bindAssetRenditionDispatcher(AssetRenditionDispatcher service, Map<String, Object> props) {
        if (log.isDebugEnabled()) {
            log.debug("Binding AssetRenditionDispatcher [ {} ]", service.getClass().getName());
        }
        assetRenditionDispatchers.bind(service, props);
    }

    protected void unbindAssetRenditionDispatcher(AssetRenditionDispatcher service, Map<String, Object> props) {
        if (log.isDebugEnabled()) {
            log.debug("Unbinding AssetRenditionDispatcher [ {} ]", service.getClass().getName());
        }
        assetRenditionDispatchers.unbind(service, props);
    }

    @Override
    public List<AssetRenditionDispatcher> getAssetRenditionDispatchers() {
        if (assetRenditionDispatchers == null || assetRenditionDispatchers.getList() == null) {
            return Collections.EMPTY_LIST;
        } else {
            return ImmutableList.copyOf(assetRenditionDispatchers.getList());
        }
    }

    @Override
    public boolean isValidAssetRenditionName(final String name) {
        final Optional<AssetRenditionDispatcher> found = getAssetRenditionDispatchers().stream()
                .filter(dispatcher -> dispatcher.getRenditionNames().contains(name))
                .findAny();

        return found.isPresent();
    }
}
