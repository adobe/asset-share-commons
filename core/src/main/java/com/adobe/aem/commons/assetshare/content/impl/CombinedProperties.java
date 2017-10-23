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

package com.adobe.aem.commons.assetshare.content.impl;

import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.day.cq.dam.api.Asset;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class CombinedProperties implements Map<String, Object> {
    private static final Logger log = LoggerFactory.getLogger(CombinedProperties.class);

    private final Map<String, ComputedProperty> computedProperties;
    private final Map<String, Object> cachedValues = new HashMap<>();
    private final SlingHttpServletRequest request;
    private final Asset asset;

    private ValueMap assetProperties;
    private ValueMap metaProperties;

    public CombinedProperties(final List<ComputedProperty> computedProperties,
                              final SlingHttpServletRequest request,
                              final Asset asset) {

        log.trace("Constructing CombinedProperties for [ {} ]", asset.getPath());

        this.request = request;
        this.asset = asset;
        this.assetProperties = getProperties();
        this.metaProperties = getMetadataProperties();
        this.computedProperties = getComputedPropertiesMap(computedProperties);
    }

    @Override
    public final int size() {
        return computedProperties.size();
    }

    @Override
    public final boolean isEmpty() {
        return computedProperties.isEmpty();
    }

    @Override
    public final boolean containsKey(Object key) {
        final String propertyName = (String) key;
        boolean result = false;

        final ComputedProperty computedProperty = computedProperties.get(propertyName);

        if (computedProperty != null) {
            if (cachedValues.containsKey(propertyName)) {
                result = true;
            } else if (computedProperty.accepts(asset, request, propertyName)) {
                result = true;
            }
        }

        if (!result) {
            result = getProperties().get(propertyName) != null;
        }

        if (!result) {
            result = getMetadataProperties().get(propertyName) != null;
        }

        return result;
    }

    @Override
    public final boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Values are computed, so this cannot be determined.");
    }

    @Override
    public final Object get(Object key) {
        final String propertyName = (String) key;

        log.trace("Getting value for key [ {} ] from CombinedProperties", propertyName);

        if (key == null) {
            return null;
        }

        Object result = null;

        final ComputedProperty computedProperty = computedProperties.get(propertyName);

        if (computedProperty != null) {
            if (computedProperty.isCachable() && cachedValues.containsKey(propertyName)) {
                result = cachedValues.get(propertyName);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Computed value [ %s -> %s ] using [ %s ] served from ComputedPropertyAccessor cache.", propertyName, result, computedProperty.getClass().getName()));
                }
            } else if (computedProperty.accepts(asset, request, propertyName)) {
                try {
                    result = computedProperty.get(asset, request);
                    if (computedProperty.isCachable()) {
                        cachedValues.put(propertyName, result);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Computed value [ %s -> %s ] using [ %s ] ", propertyName, result, computedProperty.getClass().getName()));
                    }
                } catch (Exception ex) {
                    log.error("Exception occurred when requesting computed property [ {} ] for asset [ {} ]. Returning null.", propertyName, asset.getPath());
                    return null;
                }
            }
        }

        if (result == null && metaProperties != null) {
            result = metaProperties.get(propertyName);
        }

        if (result == null && assetProperties != null) {
            result = assetProperties.get(propertyName);
        }

        return result;
    }

    @Override
    public final Object put(String key, Object value) {
        throw new UnsupportedOperationException("Map is not modifiable");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Map is not modifiable");
    }

    @Override
    public final void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException("Map is not modifiable");
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException("Map is not modifiable");
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Values are computed, so this cannot be determined.");
    }

    @Override
    public final Collection<Object> values() {
        throw new UnsupportedOperationException("Values are computed, so this cannot be determined.");
    }

    @Override
    public final Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("Values are computed, so this cannot be determined.");
    }

    @Override
    public final boolean equals(Object o) {
        return computedProperties.equals(o);
    }

    @Override
    public final int hashCode() {
        return computedProperties.hashCode();
    }

    /**
     * private helper methods
     **/

    protected final ValueMap getProperties() {
        return asset.adaptTo(Resource.class).getValueMap();
    }

    protected final ValueMap getMetadataProperties() {
        return asset.adaptTo(Resource.class).getChild("jcr:content/metadata").getValueMap();
    }

    protected final Map<String, ComputedProperty> getComputedPropertiesMap(final List<ComputedProperty> computedProperties) {
        final Map<String, ComputedProperty> result = new HashMap<>();

        for (final ComputedProperty computedProperty : computedProperties) {
            final String key = computedProperty.getName();
            if (!result.containsKey(key)) {
                result.put(key, computedProperty);
                log.trace("Registered ComputedProperty [ {} ] with key [ {} ]", computedProperty.getClass().getName(), key);
            } else {
                log.debug("ComputedProperty [ {} ] with key [ {} ] already provided by a better ranked implementation", computedProperty.getClass().getName(), key);
            }
        }

        return result;
    }
}