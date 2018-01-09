package com.adobe.aem.commons.assetshare.util;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.AbstractResourceVisitor;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.models.factory.ModelFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility visitor that walks a Page and collects the models for resources matching at least one of the provided resource Types.
 *
 * This visitor only visits resources with a sling:resourceType.
 *
 * @param <T> The Model type to collect.
 */
public final class ComponentModelVisitor<T> extends AbstractResourceVisitor {
    final Collection<T> models = new ArrayList<>();
    final Collection<Resource> resources = new ArrayList<>();

    private final SlingHttpServletRequest request;
    private final ModelFactory modelFactory;
    private final String[] resourceTypes;
    private final Class<T> clazz;

    /**
     * @param request the SlingHttpServletRequest object
     * @param modelFactory the ModelFactory object used to construct the Model
     * @param resourceTypes the resource types that will be attempted to be resolved to the T type.
     * @param clazz the Model class the resources should be made into.
     */
    public ComponentModelVisitor(SlingHttpServletRequest request,
                                 ModelFactory modelFactory,
                                 String[] resourceTypes,
                                 Class<T> clazz) {
        this.request = request;
        this.modelFactory = modelFactory;
        this.resourceTypes = resourceTypes;
        this.clazz = clazz;
    }

    /**
     * Note that getModels() may return a SUBSET of getResources(). If a resource matches the resource type check but cannot be turned into a model, the resources will be in getResources() but not in getModels().
     * @return a list of Models representing the visited resources (assuming they match the resourceTypes and can be made into the clazz model type.
     */
    public final Collection<T> getModels() {
        return models;
    }

    /**
     * Note that getModels() may return a SUBSET of getResources(). If a resource matches the resource type check but cannot be turned into a model, the resources will be in getResources() but not in getModels().
     * @return a list of resource that match at least one resourceTypes.
     */
    public final Collection<Resource> getResources() {
        return resources;
    }

    @Override
    /**
     * {@inheritDoc}
     **/
    public final void accept(Resource resource) {
        final ValueMap properties = resource.getValueMap();

        // Only traverse resources that have a sling:resourceType; those without sling:resourceTypes are not components and simply sub-component configurations resources (such as Option lists)
        if (properties.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, String.class) != null) {
            super.accept(resource);
        }
    }

    @Override
    protected final void visit(Resource resource) {
        for (final String resourceType : resourceTypes) {
            if (handleResourceVisit(resource, resourceType)) {
                handleModelVisit(resource);
                break;
            }
        }
    }

    private boolean handleResourceVisit(Resource resource, String resourceType) {
        if (resource != null && resource.getResourceResolver().isResourceType(resource, resourceType)) {
            resources.add(resource);
            return true;
        }

        return false;
    }

    private void handleModelVisit(Resource resource) {
        if (clazz != null) {
            final T model = modelFactory.getModelFromWrappedRequest(request, resource, clazz);

            if (model != null) {
                models.add(model);
            }
        }
    }
}