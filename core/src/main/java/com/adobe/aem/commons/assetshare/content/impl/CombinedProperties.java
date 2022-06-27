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

import com.adobe.acs.commons.util.ParameterUtil;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.day.cq.dam.api.Asset;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class CombinedProperties implements Map<String, Object> {
    private static final Logger log = LoggerFactory.getLogger(CombinedProperties.class);

    private static final String COMPUTED_PROPERTY_NAME_PARAMETER_DELIMITER = "?";
    private static final String PARAMETER_DELIMITER = "&";
    private static final String PARAMETER_KEY_VALUE_DELIMITER = "=";

    private static final String UNSUPPORTED_OPERATION = "This operation is not permitted on the CombinedProperties map.";

    private final Map<String, ComputedProperty> computedProperties;
    private final Map<String, Object> cachedValues = new HashMap<>();
    private final SlingHttpServletRequest request;
    private final Asset asset;

    private ValueMap assetProperties;
    private ValueMap metaProperties;

    public CombinedProperties(final List<ComputedProperty> computedProperties,
                              final SlingHttpServletRequest request,
                              final Asset asset) {

        if (log.isTraceEnabled()) {
            log.trace("Constructing CombinedProperties for [ {} ]", asset.getPath());
        }

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
        final ComputedPropertyParameter computedPropertyParameter = new ComputedPropertyParameter((String) key);

        boolean result = false;

        final ComputedProperty computedProperty = computedProperties.get(computedPropertyParameter.getName());

        if (computedProperty != null &&
                (cachedValues.containsKey(computedPropertyParameter.getCacheId()) ||
                        computedProperty.accepts(asset, request, computedPropertyParameter.getName()))) {
            result = true;
        }

        if (!result) {
            result = assetProperties.get(computedPropertyParameter.getName()) != null;
        }

        if (!result) {
            result = metaProperties.get(computedPropertyParameter.getName()) != null;
        }

        return result;
    }

    @Override
    public final boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Values are computed, so this cannot be determined.");
    }

    @Override
    public final Object get(Object key) {
        if (key == null) {
            return null;
        }

        final ComputedPropertyParameter computedPropertyParameter = new ComputedPropertyParameter((String) key);

        if (log.isTraceEnabled()) {
            log.trace("Getting value for key [ {} ] from CombinedProperties", computedPropertyParameter.getCacheId());
        }

        if (computedPropertyParameter.getName() == null) {
            return null;
        }

        Object result = null;

        final ComputedProperty computedProperty = computedProperties.get(computedPropertyParameter.getName());

        if (computedProperty != null) {
            if (computedProperty.isCachable() && cachedValues.containsKey(computedPropertyParameter.getCacheId())) {
                result = cachedValues.get(computedPropertyParameter.getCacheId());
                if (log.isTraceEnabled()) {
                    log.trace(String.format("Computed value [ %s -> %s ] using [ %s ] served from ComputedPropertyAccessor cache.", computedPropertyParameter.getCacheId(), result, computedProperty.getClass().getName()));
                }
            } else if (computedProperty.accepts(asset, request, computedPropertyParameter.getName())) {
                result = computedProperty.get(asset, request, computedPropertyParameter.getParameters());

                if (computedProperty.isCachable()) {
                    cachedValues.put(computedPropertyParameter.getCacheId(), result);
                }

                if (log.isTraceEnabled()) {
                    log.trace(String.format("Computed value [ %s -> %s ] using [ %s ] ", computedPropertyParameter.getCacheId(), result, computedProperty.getClass().getName()));
                }
            }
        }

        if (result == null && metaProperties != null) {
            result = metaProperties.get(computedPropertyParameter.getName());
        }

        if (result == null && assetProperties != null) {
            result = assetProperties.get(computedPropertyParameter.getName());
        }

        return result;
    }

    @Override
    public final Object put(String key, Object value) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public final void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public final Collection<Object> values() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public final Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
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
     * Internal helper methods and classes
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
                if (log.isTraceEnabled()) {
                    log.trace("Registered ComputedProperty [ {} ] with key [ {} ]", computedProperty.getClass().getName(), key);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("ComputedProperty [ {} ] with key [ {} ] already provided by a better ranked implementation", computedProperty.getClass().getName(), key);
                }
            }
        }

        return result;
    }

    /**
     * Internal class that parses and collects the Computed Property "key" into the ComputedProperty name (used to select the ComputedProperty) and optional Parameters.
     * <br>
     * The Key is inspired by query parameter format:
     * <br>
     * &lt;computed-property-name&gt;?&lt;param1-key&gt;=&lt;param1-value&gt;&&lt;param2-key&gt;=&lt;param2-value&gt;
     */
    protected static class ComputedPropertyParameter {
        private final String name;
        private final ValueMap parameters = new ValueMapDecorator(new TreeMap<>());
        private String cacheId;

        public ComputedPropertyParameter(String rawParam) {
            name = StringUtils.substringBefore(rawParam, COMPUTED_PROPERTY_NAME_PARAMETER_DELIMITER);
            parameters.putAll(
                    ParameterUtil.toMap(StringUtils.split(
                            StringUtils.substringAfter(rawParam, COMPUTED_PROPERTY_NAME_PARAMETER_DELIMITER),
                            PARAMETER_DELIMITER),
                            PARAMETER_KEY_VALUE_DELIMITER, true, ""));
            cacheId = name;

            if (!parameters.isEmpty()) {
                cacheId += COMPUTED_PROPERTY_NAME_PARAMETER_DELIMITER + parameters.keySet().stream()
                        .map(key -> key + PARAMETER_KEY_VALUE_DELIMITER + parameters.get(key, ""))
                        .collect(Collectors.joining(PARAMETER_DELIMITER));
            }
        }

        /**
         * This is in the following format:
         * <br>
         * &lt;computed-property-name&gt;?&lt;param1-key&gt;=&lt;param1-value&gt;&&lt;param2-key&gt;=&lt;param2-value&gt;
         * <br>
         * With the parameters alphabetized by key (a-z).. This is used to allow parameterized calls to ComputedProperties to be cached as long as the parameters are the same.
         *
         * @return the normalized ID for this parameterization. This is used for internal caching.
         */
        public String getCacheId() {
            return cacheId;
        }

        public String getName() {
            return name;
        }

        public ValueMap getParameters() {
            return parameters;
        }
    }
}