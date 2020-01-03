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

import java.io.IOException;
import java.util.List;

@ProviderType
public interface AssetRenditionsDownloadOrchestrator {

    String REQUEST_PARAMETER_NAME = "asset-renditions-download-orchestrator";

    /**
     * @param request the request
     * @param response the response
     * @param assets the assets to pack
     * @param renditionNames the renditions names of the assets to pack
     * @throws IOException when the binary data cannot be streamed out
     * @throws AssetRenditionsException when an error occurs
     */
    void execute(SlingHttpServletRequest request, SlingHttpServletResponse response, List<AssetModel> assets, List<String> renditionNames) throws IOException, AssetRenditionsException;

    /**
     * @param request the request
     * @param assets the assets to pack
     * @param renditionNames the renditions names of the assets to pack
     *
     * @return true if this implementation should pack, otherwise false.
     */
    boolean accepts(SlingHttpServletRequest request, List<AssetModel> assets, List<String> renditionNames);
}
