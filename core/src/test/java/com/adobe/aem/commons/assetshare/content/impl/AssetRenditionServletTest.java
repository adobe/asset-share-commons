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

package com.adobe.aem.commons.assetshare.content.impl;

import com.adobe.aem.commons.assetshare.content.RenditionResolver;
import com.day.cq.dam.api.DamConstants;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class AssetRenditionServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    AssetRenditionServlet servlet = new AssetRenditionServlet();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/impl/AssetRenditionServletTest.json", "/content/dam");
        ctx.currentResource("/content/dam/test.png");
    }

    @Test
    public void doGet() {
    }

    @Test
    public void setResponseHeaders() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/asset.rendition");

        RenditionResolver.Params params = new AssetRenditionServlet.ParamsImpl(ctx.request());

        servlet.setResponseHeaders(ctx.response(), params);

        assertEquals("filename=test.testing.png", ctx.response().getHeader("Content-Disposition"));
    }

    @Test
    public void setResponseHeaders_AsAttachment() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        RenditionResolver.Params params = new AssetRenditionServlet.ParamsImpl(ctx.request());

        servlet.setResponseHeaders(ctx.response(), params);

        assertEquals("attachment; filename=test.testing.png", ctx.response().getHeader("Content-Disposition"));
    }

    /** Static Rendition Params **/


    @Test
    public void paramsImpl_WithRenditionName() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/asset.rendition");

        RenditionResolver.Params actual = new AssetRenditionServlet.ParamsImpl(ctx.request());

        assertTrue(actual.isValid());
        assertEquals("testing", actual.getRenditionName());
        assertFalse(actual.isAttachment());
        assertEquals("test.testing.png", actual.getFileName());
    }

    @Test
    public void paramsImpl_AsAttachment() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        RenditionResolver.Params actual = new AssetRenditionServlet.ParamsImpl(ctx.request());

        assertTrue(actual.isValid());
        assertEquals("testing", actual.getRenditionName());
        assertTrue(actual.isAttachment());
        assertEquals("test.testing.png", actual.getFileName());
    }
}