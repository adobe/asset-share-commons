/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2019 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.commons.util;

import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BufferedSlingHttpServletResponseTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Test
    public void getBufferedServletOutput_isNotNull() {
        final BufferedSlingHttpServletResponse response = new BufferedSlingHttpServletResponse(ctx.response());
        assertNotNull(response.getBufferedServletOutput());
    }

    @Test
    public void getWriter_writesAreBuffered() throws Exception {
        final BufferedSlingHttpServletResponse response = new BufferedSlingHttpServletResponse(ctx.response());

        final PrintWriter writer = response.getWriter();
        writer.write("hello");
        writer.flush();

        assertEquals("hello", response.getBufferedServletOutput().getBufferedString());
    }

    @Test
    public void getOutputStream_writesAreBuffered() throws Exception {
        final BufferedSlingHttpServletResponse response = new BufferedSlingHttpServletResponse(ctx.response());

        final ServletOutputStream sos = response.getOutputStream();
        sos.write("hello".getBytes());

        assertEquals("hello", new String(response.getBufferedServletOutput().getBufferedBytes()));
    }

    @Test
    public void constructor_withExplicitBuffers() throws Exception {
        final StringWriter writer = new StringWriter();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BufferedSlingHttpServletResponse response = new BufferedSlingHttpServletResponse(ctx.response(), writer, outputStream);

        response.getWriter().write("hi");
        response.getWriter().flush();

        assertEquals("hi", writer.toString());
    }

    @Test
    public void resetBuffer_clearsBufferedOutput() throws Exception {
        final BufferedSlingHttpServletResponse response = new BufferedSlingHttpServletResponse(ctx.response());
        response.getWriter().write("hello");

        response.resetBuffer();

        assertEquals("", response.getBufferedServletOutput().getBufferedString());
    }

    @Test
    public void close_writesBufferedContentToWrappedResponse() throws Exception {
        final BufferedSlingHttpServletResponse response = new BufferedSlingHttpServletResponse(ctx.response());
        response.getWriter().write("hello");

        response.close();

        // The AemContext's real MockSlingHttpServletResponse should have received the buffered content
        assertEquals("hello", ctx.response().getOutputAsString());
    }

    @Test
    public void isSlingHttpServletResponseWrapper() {
        final BufferedSlingHttpServletResponse response = new BufferedSlingHttpServletResponse(ctx.response());
        assertNotNull((SlingHttpServletResponse) response);
    }
}
