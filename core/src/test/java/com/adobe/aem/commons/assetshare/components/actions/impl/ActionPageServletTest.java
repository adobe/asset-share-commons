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

import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestDispatcherFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ActionPageServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private RequestDispatcher requestDispatcher;

    private ActionPageServlet servlet;

    @Before
    public void setUp() {
        ctx.create().resource("/content/page",
                "jcr:primaryType", "cq:Page");
        ctx.create().resource("/content/page/jcr:content",
                "jcr:primaryType", "cq:PageContent",
                "sling:resourceType", "asset-share-commons/components/structure/page");

        servlet = (ActionPageServlet) ctx.registerInjectActivateService(new ActionPageServlet());

        ctx.request().setRequestDispatcherFactory(new MockRequestDispatcherFactory() {
            @Override
            public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
                return requestDispatcher;
            }

            @Override
            public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
                return requestDispatcher;
            }
        });
    }

    @Test
    public void accepts_true_whenResourceTypeMatchesConfigured() {
        ctx.currentResource("/content/page");

        assertTrue(servlet.accepts(ctx.request()));
    }

    @Test
    public void accepts_false_whenJcrContentMissing() {
        ctx.create().resource("/content/no-content", "jcr:primaryType", "cq:Page");
        ctx.currentResource("/content/no-content");

        assertFalse(servlet.accepts(ctx.request()));
    }

    @Test
    public void accepts_false_whenResourceTypeDoesNotMatch() {
        ctx.create().resource("/content/other", "jcr:primaryType", "cq:Page");
        ctx.create().resource("/content/other/jcr:content",
                "jcr:primaryType", "cq:PageContent",
                "sling:resourceType", "some/other/resourceType");
        ctx.currentResource("/content/other");

        assertFalse(servlet.accepts(ctx.request()));
    }

    @Test
    public void doPost_forwardsAsGetRequest() throws ServletException, IOException {
        ctx.currentResource("/content/page");
        ctx.request().setMethod("POST");

        servlet.doPost(ctx.request(), ctx.response());

        verify(requestDispatcher).forward(any(), any());
    }

    @Test
    public void getRequest_methodIsAlwaysGet() throws ServletException, IOException {
        ctx.currentResource("/content/page");
        ctx.request().setMethod("POST");

        servlet.doPost(ctx.request(), ctx.response());

        final ArgumentCaptor<ServletRequest> forwardedRequest = ArgumentCaptor.forClass(ServletRequest.class);
        verify(requestDispatcher).forward(forwardedRequest.capture(), any());

        // The GetRequest wrapper always reports GET, regardless of the original request's method.
        assertEquals("GET", ((javax.servlet.http.HttpServletRequest) forwardedRequest.getValue()).getMethod());
        // The original request itself remains a POST.
        assertEquals("POST", ctx.request().getMethod());
    }
}
