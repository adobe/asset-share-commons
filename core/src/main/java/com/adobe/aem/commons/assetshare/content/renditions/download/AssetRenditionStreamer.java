/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions.download;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.annotation.versioning.ProviderType;

import java.io.ByteArrayOutputStream;

@ProviderType
public interface AssetRenditionStreamer {
    /**
     * @param request the request
     * @param response the response
     * @param asset the asset
     * @param renditionName the rendition of the asset to stream
     *
     * @return a representation of the asset's rendition
     * @throws AssetRenditionsException if an error streaming the asset's rendition occurs.
     */
    AssetRenditionStream getAssetRendition(final SlingHttpServletRequest request,
                                           final SlingHttpServletResponse response,
                                           final AssetModel asset,
                                           final String renditionName) throws AssetRenditionsException;

    interface AssetRenditionStream {
        /**
         * @return the output stream representing the asset rendition.
         */
        ByteArrayOutputStream getOutputStream();

        /**
         * @return the content type of the output stream, or null if unknown.
         */
        String getContentType();
    }
}
