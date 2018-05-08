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
            final List<VariationDef> collectedVariations = new ArrayList<>();
            Resource cfResource = asset.getResource();
            ContentFragment contentFragment = cfResource.adaptTo(ContentFragment.class);
            if (contentFragment != null) {
                final Iterator<VariationDef> variationDefIterator = contentFragment.listAllVariations();
                while (variationDefIterator.hasNext()) {
                    VariationDef variationDef = variationDefIterator.next();
                    collectedVariations.add(variationDef);
                }
                variations = collectedVariations;
            }
        }
        return variations;
    }

    @Override
    public boolean isReady() {
        return !this.getVariations().isEmpty();
    }

}
