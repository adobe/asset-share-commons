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

package com.adobe.aem.commons.assetshare.util;

import com.google.gson.JsonElement;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.annotation.versioning.ProviderType;

/**
 * This OSGi service provides a method to resolve a JSON object from a path. The path can be:
 *
 * - The full JCR path to a nt:file/nt:resource/oak:Resource with mime type of application/json
 * - The full JCR path to a dam:Asset
 * - An absolute HTTP/HTTPS URL to a resource that returns a JSON file.
 * - An internal resource path that is resolved via an internal Sling request.
 * - The absolute path to an ACS AEM Commons Generic List page; this will return the list items under a "options" property in the returned JSON object. ("{ options: [ { ... }, { ... } ] }")
 * - Or null if JSON cannot be resolved for any reason.
 */
@ProviderType
public interface JsonResolver {
    /**
     * Resolves a JSON object from a path.
     *
     * @param request  the SlingHttpServletRequest
     * @param response the SlingHttpServletResponse
     * @param path     the path to resolve, this can be a internal JCR Path to a nt:file/nt:resource, dam:Asset, and internal resource that is requested via an internal sling request, or an external url (starting with http:// or https://).
     * @return the JSON object or null if the path could not be resolved.
     */
    JsonElement resolveJson(SlingHttpServletRequest request, SlingHttpServletResponse response, String path);
}
