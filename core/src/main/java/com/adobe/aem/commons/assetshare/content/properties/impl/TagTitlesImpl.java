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
import com.day.cq.dam.api.Asset;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.ComponentContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.adobe.aem.commons.assetshare.content.properties.ComputedProperty.DEFAULT_ASC_COMPUTED_PROPERTY_SERVICE_RANKING;

@Component(
        service = ComputedProperty.class,
        property = {
                Constants.SERVICE_RANKING + "=" + DEFAULT_ASC_COMPUTED_PROPERTY_SERVICE_RANKING
        }
)
@Designate(ocd = TagTitlesImpl.Cfg.class)
public class TagTitlesImpl extends AbstractComputedProperty<List<String>> {
    public static final String LABEL = "Tag Titles";
    public static final String NAME = "tagTitles";
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
        final List<String> tagLabels = new ArrayList<>();
        final Resource metadataResource = asset.adaptTo(Resource.class).getChild("jcr:content/metadata");
        final TagManager tagManager = metadataResource.getResourceResolver().adaptTo(TagManager.class);

        if (metadataResource == null || tagManager == null) {
            return tagLabels;
        }

        final Tag[] tags = tagManager.getTags(metadataResource);

        if (tags != null) {
            final ComponentContext componentContext = getComponentContext(request);
            final Page currentPage = componentContext != null ? componentContext.getPage() : null;
            final Locale locale = currentPage == null ? request.getLocale() : currentPage.getLanguage(false);

            for (final Tag tag : tags) {
                tagLabels.add(tag.getTitle(locale));
            }
        }

        Collections.sort(tagLabels);

        return tagLabels;
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }


    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Tag Titles")
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
