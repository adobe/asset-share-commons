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
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private AssetRenditionDispatcher resolver1;

    @Mock
    private AssetRenditionDispatcher resolver2;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/renditions/impl/AssetRenditionServletTest.json", "/content/dam");
        ctx.currentResource("/content/dam/test.png");

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
    }

    @Test
    public void doGet() throws IOException, ServletException {
        ctx.registerService(AssetRenditionDispatcher.class,
                resolver1,
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "testing1=value doesnt matter"}).
                        build());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return "testing1".equals(invocationOnMock.getArguments()[1]);
            }
        }).when(resolver1).accepts(eq(ctx.request()), any(String.class));

        ctx.registerService(AssetRenditionDispatcher.class,
                resolver2,
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "testing2=value doesnt matter"}).
                        build());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return "testing2".equals(invocationOnMock.getArguments()[1]);
            }
        }).when(resolver2).accepts(eq(ctx.request()), any(String.class));

        ctx.registerInjectActivateService(new AssetRenditionServlet());

        ctx.request().setMethod("GET");
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing2/asset.rendition");

        ctx.getService(Servlet.class).service(ctx.request(), ctx.response());

        verify(resolver2, times(1)).dispatch(ctx.request(), ctx.response());
    }

    @Test
    public void setResponseHeaders() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/asset.rendition");

        AssetRenditionDispatcher.Params params = new AssetRenditionServlet.ParamsImpl(ctx.request());

        new AssetRenditionServlet().setResponseHeaders(ctx.response(), params);

        assertEquals("filename=test.testing.png", ctx.response().getHeader("Content-Disposition"));
    }

    @Test
    public void setResponseHeaders_AsAttachment() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        AssetRenditionDispatcher.Params params = new AssetRenditionServlet.ParamsImpl(ctx.request());

        new AssetRenditionServlet().setResponseHeaders(ctx.response(), params);

        assertEquals("attachment; filename=test.testing.png", ctx.response().getHeader("Content-Disposition"));
    }

    /** Static Rendition Params **/


    @Test
    public void paramsImpl_WithRenditionName() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing/asset.rendition");

        AssetRenditionDispatcher.Params actual = new AssetRenditionServlet.ParamsImpl(ctx.request());

        assertTrue(actual.isValid());
        assertEquals("testing", actual.getRenditionName());
        assertFalse(actual.isAttachment());
        assertEquals("test.testing.png", actual.getFileName());
    }

    @Test
    public void paramsImpl_AsAttachment() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        AssetRenditionDispatcher.Params actual = new AssetRenditionServlet.ParamsImpl(ctx.request());

        assertTrue(actual.isValid());
        assertEquals("testing", actual.getRenditionName());
        assertTrue(actual.isAttachment());
        assertEquals("test.testing.png", actual.getFileName());
    }

    @Test
    public void paramsImpl_MissingRenditionName() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("asset.rendition");

        AssetRenditionDispatcher.Params actual = new AssetRenditionServlet.ParamsImpl(ctx.request());

        assertFalse(actual.isValid());
    }

    /*
    @Test
    public void paramsImpl_RenditionNameWithDot() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("test.ing/asset.rendition");

        AssetRenditionDispatcher.Params actual = new AssetRenditionServlet.ParamsImpl(ctx.request());

        assertTrue(actual.isValid());
        assertEquals("test.ing", actual.getRenditionName());
        assertTrue(actual.isAttachment());
        assertEquals("test.test.ing.png", actual.getFileName());
    }
    */
}