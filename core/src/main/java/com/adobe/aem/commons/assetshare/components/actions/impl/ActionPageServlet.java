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

package com.adobe.aem.commons.assetshare.components.actions.impl;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.methods=POST",
                "sling.servlet.resourceTypes=cq:Page",
                "sling.servlet.selectors=partial",
                "sling.servlet.extensions=html"
        }
)
@Designate(ocd = ActionPageServlet.Cfg.class)
public class ActionPageServlet extends SlingAllMethodsServlet implements OptingServlet {
    private static final String RESOURCE_TYPE = "asset-share-commons/components/structure/page";

    private Cfg cfg;

    public final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(request.getResource()).forward(new GetRequest(request), response);
    }

    @Override
    public boolean accepts(@Nonnull final SlingHttpServletRequest request) {
        final Resource resource = request.getResource().getChild(JcrConstants.JCR_CONTENT);

        if (resource != null) {
            return Arrays.stream(cfg.resourceTypes()).anyMatch(resourceType -> resource.isResourceType(resourceType));
        }

        return false;
    }

    private class GetRequest extends SlingHttpServletRequestWrapper {
        private static final String METHOD_GET = "GET";

        public GetRequest(final SlingHttpServletRequest request) {
            super(request);
        }

        @Override
        public String getMethod() {
            return METHOD_GET;
        }
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Action Page Servlet")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Resource Types",
                description = "The resource types (or super types) that represent Action Pages. Default to [ " + RESOURCE_TYPE + "]"
        )
        String[] resourceTypes() default {RESOURCE_TYPE};

    }
}
