package com.adobe.aem.commons.assetshare.util.impl;

import org.apache.sling.api.resource.AbstractResourceVisitor;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;

/**
 * Abstract Resource Visitor that only accepts Resources  that have a sling:resourceType set.
 */
public abstract class AbstractSlingResourceTypeResourceVisitor extends AbstractResourceVisitor {
    @Override
    public final void accept(Resource resource) {
        final ValueMap properties = resource.getValueMap();

        // Only traverse resources that have a sling:resourceType; those without sling:resourceTypes are not components and simply sub-component configurations resources (such as Option lists)
        if (properties.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, String.class) != null) {
            super.accept(resource);
        }
    }

}
