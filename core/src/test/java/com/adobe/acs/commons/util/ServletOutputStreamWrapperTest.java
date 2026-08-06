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

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ServletOutputStreamWrapperTest {

    @Test
    public void isReady_alwaysTrue() {
        final ServletOutputStreamWrapper wrapper = new ServletOutputStreamWrapper(new ByteArrayOutputStream());
        assertTrue(wrapper.isReady());
    }

    @Test
    public void setWriteListener_throwsUnsupportedOperationException() {
        final ServletOutputStreamWrapper wrapper = new ServletOutputStreamWrapper(new ByteArrayOutputStream());
        try {
            wrapper.setWriteListener(null);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void write_singleByte_delegatesToWrappedStream() throws Exception {
        final ByteArrayOutputStream wrapped = new ByteArrayOutputStream();
        final ServletOutputStreamWrapper wrapper = new ServletOutputStreamWrapper(wrapped);

        wrapper.write('A');

        assertArrayEquals(new byte[]{'A'}, wrapped.toByteArray());
    }

    @Test
    public void write_byteArrayRange_delegatesToWrappedStream() throws Exception {
        final ByteArrayOutputStream wrapped = new ByteArrayOutputStream();
        final ServletOutputStreamWrapper wrapper = new ServletOutputStreamWrapper(wrapped);

        final byte[] data = "hello world".getBytes();
        wrapper.write(data, 6, 5);

        assertArrayEquals("world".getBytes(), wrapped.toByteArray());
    }
}
