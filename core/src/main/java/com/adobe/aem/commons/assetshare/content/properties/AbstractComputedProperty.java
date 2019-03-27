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

package com.adobe.aem.commons.assetshare.content.properties;

import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.HashMap;

public abstract class AbstractComputedProperty<T> implements ComputedProperty<T> {
    private static ValueMap EMPTY_VALUE_MAP = new ValueMapDecorator(new HashMap<String, Object>());

    protected ComponentContext getComponentContext(SlingHttpServletRequest request) {
        return WCMUtils.getComponentContext(request);
    }

    protected ValueMap getAssetProperties(Asset asset) {
        final Resource resource = asset.adaptTo(Resource.class);
        if (resource == null) {
            return EMPTY_VALUE_MAP;
        }

        return resource.getValueMap();
    }

    protected ValueMap getMetadataProperties(Asset asset) {
        final Resource resource = asset.adaptTo(Resource.class);
        if (resource == null) {
            return EMPTY_VALUE_MAP;
        }

        final Resource metadataResource = resource.getChild("jcr:content/metadata");
        if (metadataResource == null) {
            return EMPTY_VALUE_MAP;
        }

        return metadataResource.getValueMap();
    }

    public boolean isCachable() {
        return true;
    }

    public boolean accepts(Asset asset, String propertyName) {
        // Default acceptance condition; as long as the ComputedProperty's name matches the parameter propertyName this is sufficient.
        // This can be overridden at the non-abstract ComputedProperty level.
        return StringUtils.equals(getName(), propertyName);
    }

    public boolean accepts(Asset asset, SlingHttpServletRequest request, String propertyName) {
        return accepts(asset, propertyName);
    }

    public T get(Asset asset, SlingHttpServletRequest request, ValueMap parameters) {
        return get(asset, request);
    }

    public T get(Asset asset, SlingHttpServletRequest request) {
        return get(asset, ValueMap.EMPTY);
    }

    public T get(Asset asset, ValueMap parameters) {
        return get(asset);
    }

    public T get(Asset asset) {
        throw new IllegalArgumentException("This computed property requires a SlingHttpServletRequest object to transform the data");
    }
}
