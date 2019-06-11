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
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.StaticRenditionDispatcherImpl;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AssetRenditionServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetRenditionServletTest.json"), "/content/dam");
        ctx.currentResource("/content/dam/test.png");

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
    }

    @Test
    public void doGet() throws IOException, ServletException {
        AssetRenditionDispatcher assetRenditionDispatcher = Mockito.spy(new StaticRenditionDispatcherImpl());

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
                                "testing2=value doesnt matter"}).
                        build());

        ctx.registerInjectActivateService(new AssetRenditionServlet());

        ctx.request().setMethod("GET");
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing2/asset.rendition");

        ctx.getService(Servlet.class).service(ctx.request(), ctx.response());

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

        ctx.registerInjectActivateService(new AssetRenditionServlet());

        ctx.request().setMethod("GET");
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing/download/dont-allow-this-parameter/asset.rendition");

        ctx.getService(Servlet.class).service(ctx.request(), ctx.response());

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