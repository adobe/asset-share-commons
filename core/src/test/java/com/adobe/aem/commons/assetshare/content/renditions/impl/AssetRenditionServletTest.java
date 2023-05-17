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

package com.adobe.aem.commons.assetshare.content.renditions.impl;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.StaticRenditionDispatcherImpl;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.adobe.aem.commons.assetshare.util.ServletHelper;
import com.adobe.aem.commons.assetshare.util.impl.ServletHelperImpl;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.testing.mock.sling.servlet.MockRequestDispatcherFactory;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetRenditionServletTest.json"), "/content/dam");

        // 1x1 pixel red png
        ctx.load().binaryFile(getClass().getResourceAsStream("AssetRenditionServletTest__original.png"),
                "/content/dam/test.png/jcr:content/renditions/original");

        // 1x1 pixel blue png
        ctx.load().binaryFile(getClass().getResourceAsStream("AssetRenditionServletTest__cq5dam.web.1280.1280.png"),
                "/content/dam/test.png/jcr:content/renditions/cq5dam.web.1280.1280.png");

        ctx.currentResource("/content/dam/test.png");

        ctx.registerInjectActivateService(new ServletHelperImpl());
        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
        ctx.registerService(AssetRenditionDispatchers.class, new AssetRenditionDispatchersImpl());

        MockAssetModels.mockModelFactory(ctx, modelFactory,"/content/dam/test.png");

        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);

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
    public void doGet() throws IOException, ServletException {
        final AssetRenditionDispatcher assetRenditionDispatcher = Mockito.spy(new StaticRenditionDispatcherImpl());

        final byte[] expectedOutputStream = IOUtils.toByteArray(this.getClass().getResourceAsStream("AssetRenditionServletTest__cq5dam.web.1280.1280.png"));
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            // Write some data to the response so we know that requestDispatcher.include(..) was infact invoked.
            ((MockSlingHttpServletResponse) args[1]).getOutputStream().write(expectedOutputStream);
            return null; // void method, return null
        }).when(requestDispatcher).include(any(SlingHttpServletRequest.class), any(SlingHttpServletResponse.class));

        ctx.registerInjectActivateService(
                new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "testing1=value doesnt matter"}).
                        build());

        ctx.registerInjectActivateService(
                assetRenditionDispatcher,
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "original=original",
                                "testing2=^cq5dam\\.web\\..*"}).
                        build());

        Servlet servlet = ctx.registerInjectActivateService(new AssetRenditionServlet());

        ctx.request().setMethod("GET");
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing2/asset.rendition");

        servlet.service(ctx.request(), ctx.response());

        Mockito.verify(assetRenditionDispatcher, Mockito.times(1)).dispatch(ctx.request(), ctx.response());
    }

    @Test
    public void doGet_InvalidParameters() throws IOException, ServletException {
        final AssetRenditionDispatcher assetRenditionDispatcher = Mockito.spy(new StaticRenditionDispatcherImpl());

        ctx.registerInjectActivateService(
                assetRenditionDispatcher,
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "testing=value doesnt matter"}).
                        build());

        Servlet servlet = ctx.registerInjectActivateService(new AssetRenditionServlet());

        ctx.request().setMethod("GET");
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing/download/dont-allow-this-parameter/asset.rendition");

        servlet.service(ctx.request(), ctx.response());

        assertEquals(400, ctx.response().getStatus());
        Mockito.verify(assetRenditionDispatcher, Mockito.times(0)).dispatch(ctx.request(), ctx.response());
    }

    @Test
    public void setResponseHeaders() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/asset.rendition");

        final AssetRenditionParameters params = new AssetRenditionParameters(ctx.request());

        new AssetRenditionServlet().setResponseHeaders(ctx.response(), params);

        assertEquals("filename=test.testing.png", ctx.response().getHeader("Content-Disposition"));
    }

    @Test
    public void setResponseHeaders_AsAttachment() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        final AssetRenditionParameters params = new AssetRenditionParameters(ctx.request());

        new AssetRenditionServlet().setResponseHeaders(ctx.response(), params);

        assertEquals("attachment; filename=test.testing.png", ctx.response().getHeader("Content-Disposition"));
    }
}