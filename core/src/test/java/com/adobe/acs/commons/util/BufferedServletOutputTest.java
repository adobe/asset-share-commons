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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BufferedServletOutputTest {

    @Mock
    ServletResponse wrappedResponse;

    @Test
    public void getWriteMethod_nullBeforeAnyCall() {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        assertNull(output.getWriteMethod());
    }

    @Test
    public void getWriter_returnsBufferedWriter_andSetsWriteMethod() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);

        final PrintWriter writer = output.getWriter();
        writer.write("hello");
        writer.flush();

        assertEquals(BufferedServletOutput.ResponseWriteMethod.WRITER, output.getWriteMethod());
        assertEquals("hello", output.getBufferedString());
    }

    @Test
    public void getWriter_calledTwice_returnsSameInstance() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);

        final PrintWriter first = output.getWriter();
        final PrintWriter second = output.getWriter();

        assertSame(first, second);
    }

    @Test
    public void getWriter_afterGetOutputStream_throwsIllegalStateException() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getOutputStream();

        try {
            output.getWriter();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void getOutputStream_returnsBufferedStream_andSetsWriteMethod() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);

        final ServletOutputStream sos = output.getOutputStream();
        sos.write("hello".getBytes());

        assertEquals(BufferedServletOutput.ResponseWriteMethod.OUTPUTSTREAM, output.getWriteMethod());
        assertArrayEquals("hello".getBytes(), output.getBufferedBytes());
    }

    @Test
    public void getOutputStream_afterGetWriter_throwsIllegalStateException() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getWriter();

        try {
            output.getOutputStream();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void getOutputStream_unbuffered_delegatesToWrappedResponse() throws Exception {
        final ServletOutputStream wrappedStream = mock(ServletOutputStream.class);
        when(wrappedResponse.getOutputStream()).thenReturn(wrappedStream);

        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse, null, null);

        assertSame(wrappedStream, output.getOutputStream());
    }

    @Test
    public void getWriter_unbuffered_delegatesToWrappedResponse() throws Exception {
        final PrintWriter wrappedWriter = mock(PrintWriter.class);
        when(wrappedResponse.getWriter()).thenReturn(wrappedWriter);

        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse, null, null);

        assertSame(wrappedWriter, output.getWriter());
    }

    @Test
    public void getBufferedString_beforeGetWriter_throwsIllegalStateException() {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse, null, new ByteArrayOutputStream());

        try {
            output.getBufferedString();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected: writer == null
        }
    }

    @Test
    public void getBufferedString_afterGetOutputStream_throwsIllegalStateException() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getOutputStream();

        try {
            output.getBufferedString();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void getBufferedBytes_beforeGetOutputStream_throwsIllegalStateException() {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse, new StringWriter(), null);

        try {
            output.getBufferedBytes();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected: outputStream == null
        }
    }

    @Test
    public void getBufferedBytes_afterGetWriter_throwsIllegalStateException() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getWriter();

        try {
            output.getBufferedBytes();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void hasPendingData_falseBeforeAnyWrite() {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        assertFalse(output.hasPendingData());
    }

    @Test
    public void hasPendingData_falseWhenWriteMethodIsOutputStream() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getOutputStream();
        assertFalse(output.hasPendingData());
    }

    @Test
    public void hasPendingData_falseWhenWriterNotBuffered() {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse, null, null);
        assertFalse(output.hasPendingData());
    }

    @Test
    public void hasPendingData_trueAfterWrite() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getWriter().write("data");
        assertTrue(output.hasPendingData());
    }

    @Test
    public void resetBuffer_clearsWriterAndOutputStream_andCallsWrapped() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getWriter().write("data");

        output.resetBuffer();

        assertEquals("", output.getBufferedString());
        verify(wrappedResponse, times(1)).resetBuffer();
    }

    @Test
    public void close_flushBufferOnCloseTrue_writerMode_writesToWrappedResponse() throws Exception {
        final PrintWriter wrappedWriter = mock(PrintWriter.class);
        when(wrappedResponse.getWriter()).thenReturn(wrappedWriter);

        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getWriter().write("hello");

        output.close();

        verify(wrappedWriter, times(1)).write("hello");
    }

    @Test
    public void close_flushBufferOnCloseTrue_outputStreamMode_writesToWrappedResponse() throws Exception {
        final ServletOutputStream wrappedStream = mock(ServletOutputStream.class);
        when(wrappedResponse.getOutputStream()).thenReturn(wrappedStream);

        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getOutputStream().write("hello".getBytes());

        output.close();

        verify(wrappedStream, times(1)).write("hello".getBytes());
    }

    @Test
    public void close_flushBufferOnCloseFalse_doesNotWriteToWrappedResponse() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.setFlushBufferOnClose(false);
        output.getWriter().write("hello");

        output.close();

        verify(wrappedResponse, never()).getWriter();
    }

    @Test
    public void close_afterFlushBufferDeferred_callsWrappedFlushBuffer() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);
        output.getWriter();
        output.flushBuffer();

        output.close();

        verify(wrappedResponse, times(1)).flushBuffer();
    }

    @Test
    public void flushBuffer_whenNotBuffered_callsWrappedResponseImmediately() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse, null, null);
        output.getWriter();

        output.flushBuffer();

        verify(wrappedResponse, times(1)).flushBuffer();
    }

    @Test
    public void flushBuffer_beforeAnyWriteCall_isBufferedByDefault() throws Exception {
        final BufferedServletOutput output = new BufferedServletOutput(wrappedResponse);

        output.flushBuffer();

        verify(wrappedResponse, never()).flushBuffer();
    }
}
