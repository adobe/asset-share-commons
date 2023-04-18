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

package com.adobe.aem.commons.assetshare.components.actions.share;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * The Share Service allows for implementations of for various Sharing. Implement this interface to provide your own sharing mechanism.
 * <p>
 * Asset Share Commons comes with an EmailShareImpl implementation that uses the ACS AEM Commons Email service to send share e-mails.
 */
@ConsumerType
public interface ShareService {
    /**
     *
     * @param request         the request that provides context of which Asset Share instance the request is coming to.
     * @return true if the share service should process the request.
     */
    boolean accepts(SlingHttpServletRequest request);

    /**
     * Share method to use in the context of a request; Typically a Servlet will call this method on the appropriate ShareService implementation.
     *
     * @param request         the request that provides context of which Asset Share instance the request is coming to.
     * @param response        the response
     * @param shareParameters a &lt;String, Object&gt; map or parameters; This is initially constructed from the request.getParameterMap() but can be augmented in the ShareService implementation as needed.
     * @throws ShareException is thrown if an error occurs with sharing (required share params are missing) or with the sharing initiation itself.
     */
    void share(SlingHttpServletRequest request, SlingHttpServletResponse response, ValueMap shareParameters) throws ShareException;
}
