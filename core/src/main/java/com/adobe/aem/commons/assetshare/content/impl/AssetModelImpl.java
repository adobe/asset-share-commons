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

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.TitleImpl;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {AssetModel.class}
)
public class AssetModelImpl implements AssetModel {

    @Self
    @Required
    private SlingHttpServletRequest request;

    @OSGiService
    @Required
    private ComputedProperties computedProperties;

    @OSGiService
    @Required
    private AssetResolver assetResolver;

    private Resource resource;

    private ValueMap properties;

    // This must be populated in init(); if it cannot be an exception is thrown.
    private Asset asset;

    @PostConstruct
    public void init() {
        if (request != null) {
            asset = assetResolver.resolveAsset(request);
        }

        if (asset != null) {
            resource = asset.adaptTo(Resource.class);
        } else {
            throw new IllegalArgumentException("Unable to to construct an AssetModel from the provided adaptables.");
        }
    }

    public Resource getResource() {
        return resource;
    }

    public String getPath() {
        return asset.getPath();
    }

    public String getAssetId() {
        return asset.getID();
    }

    public String getName() {
        return asset.getName();
    }

    public String getTitle() {
        return getProperties().get(TitleImpl.NAME, String.class);
    }

    public List<Rendition> getRenditions() {
        final List<Rendition> renditions = new ArrayList<Rendition>();
        final Iterator<? extends Rendition> itr = asset.listRenditions();

        while (itr.hasNext()) {
            renditions.add(itr.next());
        }

        return renditions;
    }

    @Override
    public ValueMap getProperties() {
        if (properties == null) {
            if (asset != null) {
                properties = new ValueMapDecorator(new CombinedProperties(computedProperties.getComputedProperties(), request, asset));
            } else {
                properties = new ValueMapDecorator(new HashMap<>());
            }
        }

        return properties;
    }
}
