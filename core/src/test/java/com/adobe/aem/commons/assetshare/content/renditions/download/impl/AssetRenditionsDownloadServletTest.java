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

package com.adobe.aem.commons.assetshare.content.renditions.download.impl;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionDispatchersImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.StaticRenditionDispatcherImpl;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.adobe.aem.commons.assetshare.util.impl.ServletHelperImpl;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.io.IOUtils;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionsDownloadServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    HttpClientBuilderFactory httpClientBuilderFactory;

    @Mock
    ModelFactory modelFactory;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Before
    public void setUp() {
        ctx.load().json(getClass().getResourceAsStream("AssetRenditionsDownloadServletTest.json"), "/content");

        // 1x1 pixel red png
        ctx.load().binaryFile(getClass().getResourceAsStream("AssetRenditionsDownloadServletTest__original.png"),
                "/content/dam/test.png/jcr:content/renditions/original");

        // 1x1 pixel blue png
        ctx.load().binaryFile(getClass().getResourceAsStream("AssetRenditionsDownloadServletTest__cq5dam.web.1280.1280.png"),
                "/content/dam/test.png/jcr:content/renditions/cq5dam.web.1280.1280.png");

        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.png");

        ctx.registerService(HttpClientBuilderFactory.class, httpClientBuilderFactory);

        ctx.registerService(AssetRenditionDispatchers.class, new AssetRenditionDispatchersImpl());

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());

        ctx.registerInjectActivateService(new AssetRenditionStreamerImpl());

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl());

        ctx.registerInjectActivateService(new ServletHelperImpl());

        ctx.registerInjectActivateService(
                new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put(Constants.SERVICE_RANKING, 0).
                        put("label", "Test AssetRenditionDispatcher").
                        put("name", "test").
                        put ("types", new String[]{"image", "video"}).
                        put("rendition.mappings", new String[]{ "test=original" }).
                        build());

        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);

        ctx.request().setRequestDispatcherFactory(new MockRequestDispatcherFactory() {
            @Override
            public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
                return requestDispatcher;
            }

            @Override
            public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
                assertEquals("This method signature should not be called", "This method signature was called.");
                return null;
            }
        });
    }

    @Test
    public void getAssets() {
        ctx.registerInjectActivateService(new AssetRenditionsDownloadServlet());

        AssetRenditionsDownloadServlet servlet = (AssetRenditionsDownloadServlet) ctx.getService(Servlet.class);

        ctx.request().setQueryString("path=/content/dam/test.png&path=/content/dam/test-2.png&path=/content/dam/test-3.png");

        List<com.adobe.aem.commons.assetshare.content.AssetModel> actual = servlet.getAssets(ctx.request());

        assertEquals(1, actual.size());
        assertEquals("/content/dam/test.png", actual.get(0).getPath());
    }

    @Test
    public void getRenditionNames() {
        ctx.registerInjectActivateService(new AssetRenditionsDownloadServlet());
        AssetRenditionsDownloadServlet servlet = (AssetRenditionsDownloadServlet) ctx.getService(Servlet.class);

        ctx.currentResource("/content/allowed-rendition-names");
        ctx.request().setQueryString("renditionName=one&renditionName=four");

        List<String> actual = servlet.getRenditionNames(ctx.request());
        assertEquals(1, actual.size());
        assertEquals("one", actual.get(0));
    }

    @Test
    public void doPost() throws ServletException, IOException {
        ctx.registerInjectActivateService(new AssetRenditionsDownloadServlet());

        AssetRenditionsDownloadServlet servlet = (AssetRenditionsDownloadServlet) ctx.getService(Servlet.class);

        ctx.currentResource("/content/download-servlet");
        ctx.request().setMethod("POST");
        ctx.requestPathInfo().setResourcePath("/content/download-servlet");
        ctx.requestPathInfo().setSelectorString("asset-renditions-download");
        ctx.requestPathInfo().setExtension("zip");
        ctx.request().setQueryString("path=/content/dam/test.png&renditionName=test");

        servlet.service(ctx.request(), ctx.response());

        assertEquals("application/zip", ctx.response().getContentType());
    }

    @Test
    public void doPost_EmptyRenditionName() throws ServletException, IOException {
        ctx.registerInjectActivateService(new AssetRenditionsDownloadServlet());

        AssetRenditionsDownloadServlet servlet = (AssetRenditionsDownloadServlet) ctx.getService(Servlet.class);

        ctx.currentResource("/content/download-servlet");
        ctx.request().setMethod("POST");
        ctx.requestPathInfo().setResourcePath("/content/download-servlet");
        ctx.requestPathInfo().setSelectorString("asset-renditions-download");
        ctx.requestPathInfo().setExtension("zip");
        ctx.request().setQueryString("path=/content/dam/test.png");

        servlet.service(ctx.request(), ctx.response());

        assertEquals("application/zip", ctx.response().getContentType());
        assertEquals(253,  ctx.response().getOutput().length); // Size of zip w/ default no content message
    }

    @Test
    public void doPost_InvalidAssetPath() throws ServletException, IOException {
        ctx.registerInjectActivateService(new AssetRenditionsDownloadServlet());

        AssetRenditionsDownloadServlet servlet = (AssetRenditionsDownloadServlet) ctx.getService(Servlet.class);

        ctx.currentResource("/content/download-servlet");
        ctx.request().setMethod("POST");
        ctx.requestPathInfo().setResourcePath("/content/download-servlet");
        ctx.requestPathInfo().setSelectorString("asset-renditions-download");
        ctx.requestPathInfo().setExtension("zip");
        ctx.request().setQueryString("path=/content/dam/fake.png&renditionName=test");

        servlet.service(ctx.request(), ctx.response());

        assertEquals("application/zip", ctx.response().getContentType());
        assertEquals(253,  ctx.response().getOutput().length); // Size of zip w/ default no content message
    }
}