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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.adobe.aem.commons.assetshare.content.properties.AbstractComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.day.cq.dam.api.Asset;

/**
 * This class would be responsible to generate the computed property list for the smart tags.
 *  
 * @author Tuhin Ghosh on 03/07/2018
 *
 */

@Component(service = ComputedProperty.class)
@Designate(ocd = SmartTagsImpl.Cfg.class)
public class SmartTagsImpl extends AbstractComputedProperty<List<String>> {
    public static final String LABEL = "Smart Tags";
    public static final String NAME = "smartTags";
    public static final String SMART_TAG_NAME_PROPERTY = "name";
    public static final String SMART_TAG_NODE_PATH = "jcr:content/metadata/predictedTags";
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
    public List<String> get(Asset asset, SlingHttpServletRequest request) {
        final List<String> smartTagLabels = new ArrayList<>();
        final Resource smartTagResource = (asset != null && asset.adaptTo(Resource.class) != null) ? asset.adaptTo(Resource.class).getChild(SMART_TAG_NODE_PATH) : null;
        Iterator<Resource> smartTagList = (smartTagResource != null && smartTagResource.getChildren() != null) ? smartTagResource.getChildren().iterator() : null;
        if (smartTagList == null) {
            return smartTagLabels;
        }
        while (smartTagList.hasNext()) {
            Resource smartTag = smartTagList.next();
            smartTagLabels.add(smartTag.getValueMap().get(SMART_TAG_NAME_PROPERTY).toString());
        }
        return smartTagLabels;
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Smart Tag Titles")
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