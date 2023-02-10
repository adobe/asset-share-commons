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
package com.adobe.aem.commons.assetshare.util.assetkit;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.annotation.versioning.ProviderType;

import javax.jcr.RepositoryException;
import java.util.Collection;

@ProviderType
/**
 * A helper class for working with the Asset Kit. Typically use in custom ComponentUpdater implementations or custom AssetKit component Sling Models.
 */
public interface AssetKitHelper {
    /**
     * Gets a collection of assets that at or exist under the paths provided.
     * @param resourceResolver the resource resolver.
     * @param paths the assets live at or under.
     * @return a collection of assets.
     */
    Collection<? extends AssetModel> getAssets(ResourceResolver resourceResolver, String[] paths);

    /**
     * Gets a collection of assets that at or exist under the paths provided.
     * @param resources an array of resources the assets live at or under.
     * @return a collection of assets.
     */
    Collection<? extends AssetModel> getAssets(Resource[] resources);

    /**
     * Gets a collection of assets that exist at the collection represented by the resource.
     * @param resource represents the collection.
     * @return a collection of assets that belong to the collection.
     */
    Collection<? extends AssetModel> getAssetsFromAssetCollection(Resource resource);

    /**
     * Gets a collection of assets that exist at the folder represented by the resource.
     * @param resource represents the assets folder.
     * @return a collection of (immediate) assets that exist in the folder.
     */
    Collection<? extends AssetModel> getAssetsFromAssetFolder(Resource resource);

    /**
     * Gets the asset represented by the resource.
      * @param resource the resource that represents the asset.
     * @return an asset.
     */
    AssetModel getAsset(Resource resource);

    /**
     * Checks if the resource is an assets folder.
     * @param resource the resource.
     * @return true if the resource is an assets folder.
     */
    boolean isAssetFolder(Resource resource);

    /**
     * Checks if the resource is an assets collection.
     * @param resource the resource.
     * @return true is the resource is an assets collection.
     */
    boolean isAssetCollection(Resource resource);

    /**
     * Checks if the resource is an asset.
     * @param resource the resource
     * @return true if the asset is an asset.
     */
    boolean isAsset(Resource resource);

    /**
     * Updates a component on a page (or the page itself).
     * @param page the page to update.
     * @param resourceType the resource type of the component to update.
     * @param propertyName the component property to update.
     * @param propertyValue the component property value to set.
     * @throws PersistenceException
     * @throws RepositoryException
     */
    void updateComponentOnPage(Page page, String resourceType, String propertyName, String propertyValue) throws PersistenceException, RepositoryException;
}
