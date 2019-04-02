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

package com.adobe.aem.commons.assetshare.content.renditions;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.annotation.versioning.ConsumerType;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * An OSGi Service (with multiple implementations) that dispatches requests to the AssetRenditionsServlet to some representation of the resolved asset rendition.
 * This could be a static rendition (resource in AEM), or ACS Commons Named Image Transform, Dynamic Media, etc. via internal forward/redirects or external redirects (301/302).
 */
@ConsumerType
public interface AssetRenditionDispatcher {

    /**
     * @return the friendly name of this Rendition Resolver displayed to Authors.
     */
    String getLabel();

    /**
     * @return the system name of this Rendition Resolver. This should be unique across all AssetRenditionResolvers instances.
     */
    String getName();

    /**
     * @return the options provided by the RenditionResolver implementation, in the form:
     * - key: Option Title
     * - value: Rendition Name
     */
    Map<String, String> getOptions();

    /**
     * The options supported by this RenditionResolver.
     * <br>
     * Format is: Map&lt;OptionName, OptionLabel&gt;
     *
     * @param request       the SlingHttpServletRequest.
     * @param renditionName the "name" of the rendition to serve.
     *
     * @return true if this RenditionResolver should handle this request (ie. dispatch(..) will be called).
     */
    boolean accepts(SlingHttpServletRequest request, String renditionName);

    /**
     * Dispatch the request to the appropriate mechanism that will provide the desired rendition.
     *
     * @param request  the SlingHttpServletRequest.
     * @param response the SlingHttpServletResponse to include the rendition on.
     *
     * @throws IOException      if the rendition cannot be written.
     * @throws ServletException if the request cannot be dispatched properly.
     */
    void dispatch(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException;
}
