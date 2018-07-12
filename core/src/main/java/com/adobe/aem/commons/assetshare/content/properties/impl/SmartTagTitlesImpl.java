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

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
 * This class generates the computed property list of smart tag titles.
 */
@Component(service = ComputedProperty.class)
@Designate(ocd = SmartTagTitlesImpl.Cfg.class)
public class SmartTagTitlesImpl extends AbstractComputedProperty<List<String>> {
    public static final String LABEL = "Smart Tags";
    public static final String NAME = "smartTagTitles";
    public static final String PN_SMART_TAG_NAME = "name";
    public static final String PN_SMART_TAG_CONFIDENCE = "confidence";
    public static final String REL_PATH_SMART_TAGS_RESOURCE = "jcr:content/metadata/predictedTags";

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
        List<String> smartTagTitles = new ArrayList<>();

        final Resource smartTagsResource = getSmartTagsResource(asset);

        if (smartTagsResource != null) {
            smartTagTitles = getSmartTagsByConfidence(smartTagsResource).stream()
                    .map(r -> r.getValueMap().get(PN_SMART_TAG_NAME, String.class))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        }

        return smartTagTitles;
    }

    private Resource getSmartTagsResource(final Asset asset) {
        if (asset == null) {
            return null;
        }

        final Resource resource = asset.adaptTo(Resource.class);

        if (resource == null) {
            return null;
        }

        return resource.getChild(REL_PATH_SMART_TAGS_RESOURCE);
    }

    private Collection<Resource> getSmartTagsByConfidence(final Resource smartTagsResource) {
        final List<Resource> smartTagResources = new ArrayList<>();

        smartTagsResource.listChildren().forEachRemaining(r -> { smartTagResources.add(r); });

        // Sort the smart tag resources by confidence score
        Collections.sort(smartTagResources, new SmartTagsConfidenceComparator());

        return smartTagResources;
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

    private class SmartTagsConfidenceComparator implements Comparator<Resource> {
        @Override
        public int compare(final Resource o1, final Resource o2) {
            final Double confidence1 =  o1.getValueMap().get(PN_SMART_TAG_CONFIDENCE, 0d);
            final Double confidence2 =  o2.getValueMap().get(PN_SMART_TAG_CONFIDENCE, 0d);

            return confidence1.compareTo(confidence2);
        }
    }
}