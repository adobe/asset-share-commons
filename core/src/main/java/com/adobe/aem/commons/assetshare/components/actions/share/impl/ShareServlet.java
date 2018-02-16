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

package com.adobe.aem.commons.assetshare.components.actions.share.impl;

import com.adobe.aem.commons.assetshare.components.actions.share.Share;
import com.adobe.aem.commons.assetshare.components.actions.share.ShareException;
import com.adobe.aem.commons.assetshare.components.actions.share.ShareService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

@Component(service = Servlet.class,
           property = {
            "sling.servlet.methods=POST",
            "sling.servlet.resourceTypes=asset-share-commons/actions/share",
            "sling.servlet.selectors=share",
            "sling.servlet.extensions=html",
            "sling.servlet.extensions=json"
            }
)
public class ShareServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ShareServlet.class);

    @Reference(target = "(component.name=com.adobe.aem.commons.assetshare.components.actions.share.impl.EmailShareServiceImpl)")
    private ShareService defaultShareService;

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private transient Collection<ShareService> shareServices;

    @Override
    protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        try {
            share(request, response);
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }

    private final void share(SlingHttpServletRequest request, SlingHttpServletResponse response) throws RepositoryException {
        try {
            // Make this map write-able by copying the Request parameters into a new hash map
            final ValueMap shareParameters = new ValueMapDecorator(new HashMap<String, Object>(request.getParameterMap()));

            int shareCount = 0;
            // Call the best ranking Share Service
            for(final ShareService shareService : shareServices) {
                if (shareService.accepts(request)) {
                    shareService.share(request, response, shareParameters);
                    shareCount++;
                }
            }

            if (shareCount == 0) {
                defaultShareService.share(request, response, shareParameters);
            }
        } catch (ShareException ex) {
            log.error("Unable to share assets from Asset Share Commons", ex);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}