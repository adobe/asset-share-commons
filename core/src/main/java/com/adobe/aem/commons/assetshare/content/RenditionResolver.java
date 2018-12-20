/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.annotation.versioning.ConsumerType;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * This interface defines an OSGi Service (with expected multiple implementations) that resolves some rendition of an
 * Asset (could be a static rendition, or ACS Commons Named Image Transform, Dynamic Media, etc.)
 */
@ConsumerType
public interface RenditionResolver {

    Map<String, String> getOptions();

    /**
     * The options supported by this RenditionResolver.
     *
     * Format should is: Map&lt;OptionName, OptionLabel&gt;
     *
     * @param request the SlingHttpServletRequest.
     * @param renditionName the "name" of the rendition to serve.
     * @return true if this RenditionResolver should handle this request (ie. dispatch(..) will be called).
     */
    boolean accepts(SlingHttpServletRequest request, String renditionName);

    /**
     * Gets the URL for the specified rendition name.
     *
     * @param request the SlingHttpServletRequest.
     * @param renditionName the "name" of the rendition to serve.
     * @param asset the asset whose rendition should be served.
     * @return the URL that can be used to serve the renditionName of this the asset.
     */
    String getUrl(SlingHttpServletRequest request, String renditionName, Asset asset);

    /**
     * Dispatch the request to the appropriate.
     *
     * @param request the SlingHttpServletRequest.
     * @param request the SlingHttpServletResponse to include the rendition on.
     * @throws IOException
     */
    void dispatch(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException;


    /**
     * Represents the parameters used by the RenditionResolver ot resolve the appropriate rendition.
     */
    interface Params {
        /**
         * @return the rendition name to resolve.
         */
        String getRenditionName();

        /**
         * The file name is computed as:
         *
         * &lt;asset-name-without-extension&gt;.&lt;rendition-name&gt;.&lt;asset-extension&gt;
         *
         * example:
         * - cat.png -> cat.web.png
         * - dog.pdf -> dog.original.png
         * - mouse.mov -> mouse.tiny.mov
         *
         * The is only impacts browser downloads, and not the URL or cached file in AEM dispatcher.
         *
         * @return the filename the rendition should download as.
         */
        String getFileName();

        /**
         * @return true if the rendition should be downloaded as an attachment.
         */
        boolean isAttachment();

        /**
         * @return true if the URL is valid and the requires params can be parsed from it's Suffix.
         */
        boolean isValid();
    }
}
