/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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
package com.adobe.aem.commons.assetshare.util.assetkit.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.resource.collection.ResourceCollection;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;
import java.util.stream.Collectors;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static org.apache.sling.jcr.resource.api.JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY;

@Component
public class AssetKitHelperImpl implements AssetKitHelper {

    @Reference
    private transient QueryBuilder queryBuilder;

    @Reference
    private transient ModelFactory modelFactory;

    @Override
    public Collection<AssetModel> getAssets(ResourceResolver resourceResolver, String[] paths) {
        final Resource[] resources = Arrays.stream(paths).map(path -> resourceResolver.getResource(path)).collect(Collectors.toList()).toArray(new Resource[0]);
        return getAssets(resources);
    }

    @Override
    public Collection<AssetModel> getAssets(Resource[] resources) {
        List<AssetModel> assets = new ArrayList<>();

        for (Resource resource: resources) {
                if (resource != null) {
                    if (isAsset(resource)) {
                        assets.add(getAsset(resource));
                    } else if (isAssetFolder(resource)) {
                        assets.addAll(getAssetsFromAssetFolder(resource));
                    } else if (isAssetCollection(resource)) {
                        assets.addAll(getAssetsFromAssetCollection(resource));
                    }
                }
        }

        return assets;
    }

    @Override
    public Collection<? extends AssetModel> getAssetsFromAssetCollection(Resource resource) {
        final List<AssetModel> assets = new ArrayList<>();

        final ResourceCollection resourceCollection = resource.adaptTo(ResourceCollection.class);
        if (null != resourceCollection) {
            final Iterator<Resource> resourceIterator = resourceCollection.getResources();
            while (resourceIterator.hasNext()) {
                Resource collectionResource = resourceIterator.next();

                if (isAsset(collectionResource)) {
                    assets.add(getAsset(collectionResource));
                }
            }
        }
        return assets;
    }

    @Override
    public Collection<? extends AssetModel> getAssetsFromAssetFolder(Resource resource) {
        final List<AssetModel> assets = new ArrayList<>();
        resource.getChildren().forEach(child -> {
            if (!JCR_CONTENT.equals(child.getName()) && (isAsset(child))) {
                assets.add(getAsset(child));
            }
        });

        return assets;
    }

    @Override
    public AssetModel getAsset(Resource resource) {
        return modelFactory.createModel(resource, AssetModel.class);
    }

    @Override
    public boolean isAssetFolder(Resource resource) {
        return resource != null && StringUtils.startsWith(resource.getPath(), "/content/dam/") &&
                (resource.getResourceType().equals("sling:Folder") || resource.getResourceType().equals("sling:OrderedFolder") || resource.getResourceType().equals("nt:folder"));
    }

    @Override
    public boolean isAssetCollection(Resource resource) {
        return resource != null &&  StringUtils.startsWith(resource.getPath(), "/content/dam/collections/") && resource.adaptTo(ResourceCollection.class) != null;
    }

    @Override
    public boolean isAsset(Resource resource) {
        return resource != null && DamUtil.isAsset(resource);
    }


    @Override
    public void updateComponentOnPage(Page page, String resourceType, String propertyName, String propertyValue) throws PersistenceException, RepositoryException {
        final Resource resource = findResourceByResourceType(page, resourceType);

        if (resource != null) {
            final ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);
            if (properties.keySet().contains(propertyName)) {
                properties.remove(propertyName);
            }
            properties.put(propertyName, propertyValue);
        }
    }

    public Resource findResourceByResourceType(Page page, String resourceType) throws RepositoryException {
        final ResourceResolver resourceResolver = page.getContentResource().getResourceResolver();
        final Map<String, String> map = new HashMap<>();

        map.put("path", page.getContentResource().getPath());
        map.put("path.self", "true");
        map.put("property", SLING_RESOURCE_TYPE_PROPERTY);
        map.put("property.value", resourceType);
        map.put("p.limit", "1");

        final Query query = queryBuilder.createQuery(PredicateGroup.create(map), resourceResolver.adaptTo(Session.class));
        final SearchResult result = query.getResult();

        if (result.getHits().size() > 0) {
            return resourceResolver.getResource(result.getHits().get(0).getPath());
        } else {
            return null;
        }
    }
}
