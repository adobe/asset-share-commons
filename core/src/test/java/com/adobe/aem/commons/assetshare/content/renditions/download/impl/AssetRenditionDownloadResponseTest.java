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

import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssetRenditionDownloadResponseTest {

    @Rule
    public final AemContext ctx = new AemContext();

    StringWriter stringWriter;

    ByteArrayOutputStream baos;

    @Before
    public void setUp() {
        stringWriter = new StringWriter();
        baos = new ByteArrayOutputStream();
    }

    @Test
    public void isAndGetRedirect_WithSendRedirect() throws IOException {
        AssetRenditionDownloadResponse response = new AssetRenditionDownloadResponse(ctx.response(), stringWriter, baos);
        response.sendRedirect("/test.html");

        assertTrue(response.isRedirect());
        assertEquals("/test.html", response.getRedirect());
    }

    @Test
    public void isAndGetRedirect_WithSetStatus301() throws IOException {
        AssetRenditionDownloadResponse response = new AssetRenditionDownloadResponse(ctx.response(), stringWriter, baos);
        response.setStatus(301);
        response.setHeader("Location", "/test.html");

        assertTrue(response.isRedirect());
        assertEquals("/test.html", response.getRedirect());
    }

    @Test
    public void isAndGetRedirect_WithSetStatus302() throws IOException {
        AssetRenditionDownloadResponse response = new AssetRenditionDownloadResponse(ctx.response(), stringWriter, baos);
        response.setStatus(302);
        response.setHeader("Location", "/test.html");

        assertTrue(response.isRedirect());
        assertEquals("/test.html", response.getRedirect());
    }

    @Test
    public void getByteArrayOutputStream() {
        baos.write(100);

        AssetRenditionDownloadResponse response = new AssetRenditionDownloadResponse(ctx.response(), stringWriter, baos);

        assertEquals(baos, response.getByteArrayOutputStream());
    }

    @Test
    public void getStatusCode_WithSetStatus() {
        AssetRenditionDownloadResponse response = new AssetRenditionDownloadResponse(ctx.response(), stringWriter, baos);
        response.setStatus(200);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void getStatusCode_WithSendError() throws IOException {
        AssetRenditionDownloadResponse response = new AssetRenditionDownloadResponse(ctx.response(), stringWriter, baos);
        response.sendError(404);

        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void getStatusCode_WithSendErrorWithMessage() {
        AssetRenditionDownloadResponse response = new AssetRenditionDownloadResponse(ctx.response(), stringWriter, baos);
        response.sendError(404, "test");

        assertEquals(404, response.getStatusCode());
    }

    @Test
    public void getContentType() {
        AssetRenditionDownloadResponse response = new AssetRenditionDownloadResponse(ctx.response(), stringWriter, baos);
        response.setHeader("Content-Type", "application/test");

        assertEquals("application/test", response.getContentType());
    }
}