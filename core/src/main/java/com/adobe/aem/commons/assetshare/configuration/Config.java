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

package com.adobe.aem.commons.assetshare.configuration;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.util.Locale;

/**
 * The model interface that represents the configuration for an Asset Share Commons "site".
 *
 * Most of these configurations are derived via the configurations set on the Search Page's Page Properties.
 */
public interface Config {

    /**
     * @return the SlingHttpServletRequest that resolves to this Config object.
     */
    SlingHttpServletRequest getRequest();

    /**
     * @return the ResourceResolver that resolved this Config object.
     */
    ResourceResolver getResourceResolver();

    ValueMap getProperties();

    /**
     * @return the Locale for the Page that resolves to this Config object.
     */
    Locale getLocale();

    /**
     * @return the absolute path Path to the resource resource (cq:Page) that resolves to this Config object.
     */
    String getRootPath();

    /**
     * @return the asset model representing the Placeholder asset for this Config. The Placeholder AssetModel is used for authoring purposes in contexts where there is no natural asset to display.
     */
    AssetModel getPlaceholderAsset();

    /**
     * @return the ID of the AssetDetailsSelector implementation to use.
     */
    String getAssetDetailsSelector();

    /**
     * @return the absolute path to the main asset details page; asset-type specific details pages will exist under this.
     */
    String getAssetDetailsPath();

    /**
     * @return the path segment of the URL to call to render the main asset details page.
     */
    String getAssetDetailsUrl();

    /**
     * @return true if the asset details pages should reference assets by path (else ID).
     */
    default boolean getAssetDetailReferenceById() { return true; }

    /**
     * @return the path segment of the URL to call to render the Cart.
     */
    String getCartActionUrl();

    /**
     * @return the path segment of the URL to call to render the Download action.
     */
    String getDownloadActionUrl();

    /**
     * @return the path segment of the URL to call to render the License Agreement action.
     */
    String getLicenseActionUrl();

    /**
     * @return the path segment of the URL to call to render the Share action.
     */
    String getShareActionUrl();

    /**
     * @return true if the cart is enabled.
     */
    boolean isCartEnabled();

    /**
     * @return true if the download action is enabled.
     */
    boolean isDownloadEnabled();

    /**
     * @return true if the license action is enabled.
     */
    boolean isLicenseEnabled();

    /**
     * @return true if the share action is enabled.
     */
    boolean isShareEnabled();

    /**
     * @return true if AEM is enabled with AEM Assets Dynamic Media.
     */
    default boolean isDynamicMediaEnabled() { return false; }
}