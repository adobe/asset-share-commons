/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

package com.adobe.aem.commons.assetshare.util;

import org.apache.sling.api.resource.AbstractResourceVisitor;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility visitor that walks a Page and collects the resources matching at least one of the provided resourceTypes.
 *
 * This visitor only visits resources with a sling:resourceType.
 */
public class ResourceTypeVisitor extends AbstractResourceVisitor {
    final Collection<Resource> resources = new ArrayList<>();

    protected final String[] resourceTypes;

    /**
     * @param resourceTypes the resource types that will be attempted to be resolved to the T type.
     */
    public ResourceTypeVisitor(String[] resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    /**
     * Note that getModels() may return a SUBSET of getResources(). If a resource matches the resource type check but cannot be turned into a model, the resources will be in getResources() but not in getModels().
     * @return a list of resource that match at least one resourceTypes.
     */
    public Collection<Resource> getResources() {
        return resources;
    }

    @Override
    /**
     * {@inheritDoc}
     **/
    public void accept(Resource resource) {
        final ValueMap properties = resource.getValueMap();

        // Only traverse resources that have a sling:resourceType; those without sling:resourceTypes are not components and simply sub-component configurations resources (such as Option lists)
        if (properties.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, String.class) != null) {
            super.accept(resource);
        }
    }

    @Override
    protected void visit(Resource resource) {
        for (final String resourceType : resourceTypes) {
            if (handleResourceVisit(resource, resourceType)) {
                break;
            }
        }
    }

    protected boolean handleResourceVisit(Resource resource, String resourceType) {
        if (resource != null && resource.getResourceResolver().isResourceType(resource, resourceType)) {
            resources.add(resource);
            return true;
        }

        return false;
    }
}