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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BufferedServletResponseTest {

    @Mock
    ServletResponse wrappedResponse;

    @Test
    public void getBufferedServletOutput_isNotNull() {
        final BufferedServletResponse response = new BufferedServletResponse(wrappedResponse);
        assertNotNull(response.getBufferedServletOutput());
    }

    @Test
    public void getWriter_writesAreBuffered() throws Exception {
        final BufferedServletResponse response = new BufferedServletResponse(wrappedResponse);

        final PrintWriter writer = response.getWriter();
        writer.write("hello");
        writer.flush();

        assertEquals("hello", response.getBufferedServletOutput().getBufferedString());
    }

    @Test
    public void getOutputStream_writesAreBuffered() throws Exception {
        final BufferedServletResponse response = new BufferedServletResponse(wrappedResponse);

        final ServletOutputStream sos = response.getOutputStream();
        sos.write("hello".getBytes());

        assertEquals("hello", new String(response.getBufferedServletOutput().getBufferedBytes()));
    }

    @Test
    public void constructor_withExplicitBuffers() throws Exception {
        final StringWriter writer = new StringWriter();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BufferedServletResponse response = new BufferedServletResponse(wrappedResponse, writer, outputStream);

        response.getWriter().write("hi");
        response.getWriter().flush();

        assertEquals("hi", writer.toString());
    }

    @Test
    public void resetBuffer_delegatesToBufferedOutput() throws Exception {
        final BufferedServletResponse response = new BufferedServletResponse(wrappedResponse);
        response.getWriter().write("hello");

        response.resetBuffer();

        assertEquals("", response.getBufferedServletOutput().getBufferedString());
        verify(wrappedResponse, times(1)).resetBuffer();
    }

    @Test
    public void flushBuffer_deferredUntilClose() throws Exception {
        final BufferedServletResponse response = new BufferedServletResponse(wrappedResponse);
        response.getWriter();

        response.flushBuffer();
        verify(wrappedResponse, times(0)).flushBuffer();

        response.close();
        verify(wrappedResponse, times(1)).flushBuffer();
    }

    @Test
    public void close_writesBufferedContentToWrappedResponse() throws Exception {
        final PrintWriter wrappedWriter = mock(PrintWriter.class);
        when(wrappedResponse.getWriter()).thenReturn(wrappedWriter);

        final BufferedServletResponse response = new BufferedServletResponse(wrappedResponse);
        response.getWriter().write("hello");

        response.close();

        verify(wrappedWriter, times(1)).write("hello");
    }
}
