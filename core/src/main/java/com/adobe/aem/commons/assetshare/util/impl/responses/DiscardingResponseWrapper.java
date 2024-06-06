/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

package com.adobe.aem.commons.assetshare.util.impl.responses;

import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.wrappers.SlingHttpServletResponseWrapper;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class DiscardingResponseWrapper extends SlingHttpServletResponseWrapper {

    public DiscardingResponseWrapper(SlingHttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Discard the data
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // No implementation needed
            }
        };
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new OutputStreamWriter(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Discard the data
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // No implementation needed
            }
        })) {
            @Override
            public void write(int c) {
                // Discard the data
            }

            @Override
            public void write(char[] buf, int off, int len) {
                // Discard the data
            }

            @Override
            public void write(String s, int off, int len) {
                // Discard the data
            }

            @Override
            public void flush() {
                // Do nothing
            }

            @Override
            public void close() {
                // Do nothing
            }
        };
    }
}
