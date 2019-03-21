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
 * This interface defines an OSGi Service (with expected multiple implementations) that resolves some rendition of an
 * Asset (could be a static rendition, or ACS Commons Named Image Transform, Dynamic Media, etc.)
 */
@ConsumerType
public interface AssetRenditionResolver {

    /**
     * @return the friendly name of this Rendition Resolver.
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
     * Format should is: Map&lt;OptionName, OptionLabel&gt;
     *
     * @param request       the SlingHttpServletRequest.
     * @param renditionName the "name" of the rendition to serve.
     *
     * @return true if this RenditionResolver should handle this request (ie. dispatch(..) will be called).
     */
    boolean accepts(SlingHttpServletRequest request, String renditionName);

    /**
     * Dispatch the request to the appropriate.
     *
     * @param request  the SlingHttpServletRequest.
     * @param response the SlingHttpServletResponse to include the rendition on.
     *
     * @throws IOException      if the rendition cannot be written.
     * @throws ServletException if the request cannot be dispatched properly.
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
         * <br>
         * &lt;asset-name-without-extension&gt;.&lt;rendition-name&gt;.&lt;asset-extension&gt;
         * <br>
         * examples:<br>
         * <br>
         * cat.png -&gt; cat.web.png<br>
         * dog.pdf -&gt; dog.original.png<br>
         * mouse.mov -&gt; mouse.tiny.mov<br>
         * <br>
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
