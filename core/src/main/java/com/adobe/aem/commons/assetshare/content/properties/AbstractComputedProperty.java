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

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.commons.WCMUtils;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.HashMap;

public abstract class AbstractComputedProperty<T> implements ComputedProperty<T> {
    private static ValueMap EMPTY_VALUE_MAP = new ValueMapDecorator(new HashMap<String, Object>());

    protected ComponentContext getComponentContext(final SlingHttpServletRequest request) {
        return WCMUtils.getComponentContext(request);
    }

    protected ValueMap getAssetProperties(final Asset asset) {
        final Resource resource = asset.adaptTo(Resource.class);
        if (resource == null) {
            return EMPTY_VALUE_MAP;
        }

        return resource.getValueMap();
    }

    protected ValueMap getMetadataProperties(final Asset asset) {
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

    protected ValueMap getJcrProperties(final Asset asset) {
        final Resource resource = asset.adaptTo(Resource.class);
        if (null == resource) {
            return EMPTY_VALUE_MAP;
        }
        final Resource jcrResource = resource.getChild(JcrConstants.JCR_CONTENT);
        if (null == jcrResource) {
            return EMPTY_VALUE_MAP;
        }
       return jcrResource.getValueMap();
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public boolean accepts(final Asset asset, final String propertyName) {
        return true;
    }

    @Override
    public boolean accepts(final Asset asset, final SlingHttpServletRequest request, final String propertyName) {
        return accepts(asset, propertyName);
    }

    @Override
    public T get(final Asset asset) {
        throw new IllegalArgumentException("This computed property requires a SlingHttpServletRequest object to transform the data");
    }

    @Override
    public T get(final Asset asset, final SlingHttpServletRequest request) {
        return get(asset);
    }
}
