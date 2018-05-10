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
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.impl.TitleImpl;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import org.apache.commons.lang3.StringUtils;
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
    private List<ComputedProperty> computedProperties;

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
                properties = new ValueMapDecorator(new CombinedProperties(computedProperties, request, asset));
            } else {
                properties = new ValueMapDecorator(new HashMap<>());
            }
        }

        return properties;
    }

    @Override
    public String getDisplayableExcerpt() {
        String excerpt;
        final ContentFragment contentFragment = resource.adaptTo(ContentFragment.class);
        final Iterator<ContentElement> elements = contentFragment.getElements();
        excerpt = getCFExcerpt(elements, 200);
        if (StringUtils.isNotEmpty(excerpt)) {
            final boolean hasMultipleElements = excerpt.contains("<hr/>");
            final String excerptClass = hasMultipleElements ? "excerpt-elements" : "excerpt";
            excerpt = "<div class=\"" + excerptClass + "\">" + excerpt;
        }
        return excerpt;
    }

    /**
     * Gets excerpt (up to specified number of characters) of a given Content Element.
     *
     * @param elements
     *            Content Element Iterator
     * @param maxChars
     *            max characters
     * @return excerpt
     */
    private String getCFExcerpt(final Iterator<ContentElement> elements, final int maxChars) {
        String excerpt = "";

        while (elements.hasNext() && excerpt.length() < maxChars) {
            final ContentElement ce = elements.next();
            if (ce != null) {
                String ceExcerpt = ce.getContent();
                ceExcerpt = ceExcerpt.replaceAll("\\<[^>]*>", "").replaceAll("(&nbsp;|\t)", " ").replaceAll(" +", " ")
                        .replaceAll("^ +$", "").replaceAll("( *\\n)+", "\n").trim();
                excerpt = excerpt.concat(ceExcerpt);

                if (maxChars < excerpt.length()) {
                    final char charAt = excerpt.charAt(maxChars);

                    excerpt = excerpt.substring(0, maxChars);

                    /* trim remaining letters if a word was cut in the middle */
                    if (charAt != ' ') {
                        final int end = excerpt.lastIndexOf(' ');

                        excerpt = end == -1 ? excerpt : excerpt.substring(0, end);
                    }
                    excerpt = excerpt.concat("...");
                } else if (!StringUtils.isEmpty(ceExcerpt) && elements.hasNext()) {
                    excerpt = excerpt.concat("\n");
                }
            }
        }
        excerpt = excerpt.replaceAll("\\n", "<br/> <hr/>");

        return excerpt;
    }

}
