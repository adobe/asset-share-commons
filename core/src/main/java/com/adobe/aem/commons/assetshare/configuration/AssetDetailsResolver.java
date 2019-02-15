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
import org.osgi.annotation.versioning.ProviderType;

/**
 * This interface binds OSGi Services that in turn resolve the appropriate AssetDetails page for the provided assets.
 */
@ProviderType
public interface AssetDetailsResolver {
    /**
     * Returns the URL to the asset details page used to render the provided asset.
     * It does NOT include the suffix path which points to the asset to load (path or UUID).
     * If the suffix is desired, use the getFullUrl(..) method.
     *
     * Example return values:
     * - /content/my-asset-share/details/image.html
     * - /content/my-asset-share/details/video.html
     *
     * @param config the asset share Config object
     * @param asset the asset (AssetModel) to resolve
     * @return the url to the asset details page, but do NOT include the asset reference in the suffix.
     */
    String getUrl(final Config config, final AssetModel asset);

    /**
     * Returns the URL to the asset details page used to render the provided asset.
     * It does NOT include the suffix path which points to the asset to load (path or UUID).
     * If the suffix is desired, use the getFullUrl(..) method.
     *
     * Example return values:
     * - /content/my-asset-share/details/image.html/content/dam/pictures/cat.png
     * - /content/my-asset-share/details/video.html/content/dam/videos/puppies.mp4
     *
     * @param config the asset share Config object
     * @param asset the asset (AssetModel) to resolve
     * @return the full asset details link including the asset reference as the suffix.
     */
    default String getFullUrl(final Config config, final AssetModel asset) { return null; }
}
