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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An OSGi Service (with multiple implementations) that dispatches requests to the AssetRenditionsServlet to some representation of the resolved asset rendition.
 * This could be a static rendition (resource in AEM), or ACS Commons Named Image Transform, Dynamic Media, etc. via internal forward/redirects or external redirects (301/302).
 */
@ConsumerType
public interface AssetRenditionDispatcher {
    final class Types {
        public static final String IMAGE = "image";
        public static final String VIDEO = "video";
    }

    /**
     * @return the friendly name of this Rendition Resolver displayed to Authors.
     */
    String getLabel();

    /**
     * The return value fo this method is used to select the AssetRenditionDispatcher by the AssetRenditionServlet.
     *
     * @return the system name of this Rendition Resolver. This should be unique across all AssetRenditionResolvers instances.
     */
    String getName();

    /**
     * Option map entries in the form:
     * <br>
     * - key: Option Label
     * <br>
     * - value: Rendition Name
     *
     * @return the options provided by the RenditionResolver implementation.
     */
    Map<String, String> getOptions();

    /**
     * @return a list of all the rendition names this AssetRenditionDispatcher can handle.
     */
    Set<String> getRenditionNames();

    /**
     * Returning true will prevent this AssetRenditionDispatcher's options from being listed in the AssetRenditionsDataSource.
     *
     * @return true if this AssetRenditionDispatcher is intended to be hidden from the AEM author's view, and used in the programmatic construction of URLs.
     */
    default boolean isHidden() { return false; }

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

    /**
     * Ideally a rendition dispatcher instances returns renditions that apply to all types returned bu this method.
     *
     * @return a list of Rendition Types this rendition Dispatcher will return;
     */
    default List<String> getTypes() { return Collections.EMPTY_LIST; }
}
