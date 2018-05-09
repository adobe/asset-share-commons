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

package com.adobe.aem.commons.assetshare.content.properties.impl;

import com.adobe.aem.commons.assetshare.content.properties.AbstractComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = ComputedProperty.class)
@Designate(ocd = TitleImpl.Cfg.class)
public class TitleImpl extends AbstractComputedProperty<String> {
    public static final String LABEL = "Title";
    public static final String NAME = "title";
    private Cfg cfg;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return cfg.label();
    }

    @Override
    public String[] getTypes() {
        return cfg.types();
    }

    @Override
    public String get(final Asset asset) {
        final Resource assetResource = asset.adaptTo(Resource.class);
        if (null != assetResource.adaptTo(ContentFragment.class)) {
            final ValueMap jcrValueMap = getJcrProperties(asset);
            if(StringUtils.isEmpty(jcrValueMap.get(JcrConstants.JCR_TITLE, String.class))){
                return asset.getName();
            }
            return jcrValueMap.get(JcrConstants.JCR_TITLE, String.class);
        }

        final ValueMap metadata = getMetadataProperties(asset);
        final String[] dcTitles = metadata.get("dc:title", String[].class);

        if (dcTitles != null && dcTitles.length > 0) {
            return dcTitles[0];
        } else {
            return asset.getName();
        }
    }

    @Activate
    protected void activate(final Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Title")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Label",
                description = "Human read-able label."
        )
        String label() default LABEL;

        @AttributeDefinition(
                name = "Types",
                description = "Defines the type of data this exposes. This classification allows for intelligent exposure of Computed Properties in DataSources, etc."
        )
        String[] types() default {Types.METADATA};
    }
}
