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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.CFVariations;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.VariationDef;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Sling model implementation of CFVariations interface.
 *
 */
@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {CFVariations.class},
        resourceType = {CFVariationsImpl.RESOURCE_TYPE}
)
public class CFVariationsImpl implements CFVariations {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/content-fragment/cf-variations";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    private Collection<VariationDef> variations = Collections.emptyList();

    @Override
    public Collection<VariationDef> getVariations() {
        if (variations.isEmpty()) {
            Resource cfResource = asset.getResource();
            ContentFragment contentFragment = cfResource.adaptTo(ContentFragment.class);
            if (contentFragment != null) {
                variations = getVariations(contentFragment);
            }
        }
        return variations;
    }

    private List<VariationDef> getVariations(ContentFragment contentFragment) {
        final List<VariationDef> collectedVariations = new ArrayList<>();
        final Iterator<VariationDef> variationDefIterator = contentFragment.listAllVariations();
        while (variationDefIterator.hasNext()) {
            VariationDef variationDef = variationDefIterator.next();
            collectedVariations.add(variationDef);
        }
        return collectedVariations;
    }

    @Override
    public boolean isReady() {
        return !this.getVariations().isEmpty();
    }

}
