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

package com.adobe.aem.commons.assetshare.util;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface ServletHelper {
    /**
     * Adds SlingBindings to the SlingHttpServletRequest object so it can be used to create Sling Models.
     * @param request the SlingHttpServletRequest object to add the SlingBindings to
     * @param response the SlingHttpResponse object to create the SlingScriptHelper
     */
    void addSlingBindings(final SlingHttpServletRequest request, final SlingHttpServletResponse response);
}
