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

package com.adobe.aem.commons.assetshare.content;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

@ProviderType
public interface AssetModel {
    String OVERRIDE_RESOURCE = "overrideAsset";

    /**
     * @return the [dam:Asset] resource this Asset model represents.
     */
    Resource getResource();

    /**
     * @return the absolute JCR path to the Asset
     */
    String getPath();

    /**
     * @return the escaped Asset path for use in a URL
     */
    default String getUrl() { return getPath(); }

    /**
     * @return the underlying CQ Asset object
     */
    default Asset getAsset() { return DamUtil.resolveToAsset(getResource()); }

    /**
     * @return the Assets' Id ([dam:Asset]/jcr:contnet@jcr:uuid)
     */
    String getAssetId();

    /**
     * @return the node name of the Asset. This is typically the file name as well.
     */
    String getName();

    /**
     * @return the title of the Asset. This invokes the "title" Computed Property. Default behavior returns the first entry of the dc:title, and if that is null the asset's node name.
     */
    String getTitle();

    /**
     * @return a list of all Asset Renditions for this asset;
     */
    List<Rendition> getRenditions();

    /**
     * @return a ValueMap composed of a look up based on: 1) ComputedProperties, the [dam:Asset]/jcr:content/metadata ValueMap and finally the [dam:Asset] ValueMap.
     */
    ValueMap getProperties();
}
