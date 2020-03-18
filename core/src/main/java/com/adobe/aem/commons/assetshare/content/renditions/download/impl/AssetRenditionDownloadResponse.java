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

import com.adobe.acs.commons.util.BufferedSlingHttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

public class AssetRenditionDownloadResponse extends BufferedSlingHttpServletResponse {
    private String redirectLocation = null;
    private ByteArrayOutputStream baos;
    private int statusCode = SC_OK;
    private boolean redirect = false;
    private String contentType;

    public AssetRenditionDownloadResponse(SlingHttpServletResponse wrappedResponse, StringWriter writer, ByteArrayOutputStream outputStream) {
        super(wrappedResponse, writer, outputStream);
        this.baos = outputStream;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public String getRedirect() {
        return redirectLocation;
    }

    @Override
    public void sendRedirect(String location) {
        this.redirect = true;
        this.redirectLocation = location;
    }

    @Override
    public void setStatus(int statusCode) {
        this.statusCode = statusCode;

        if (statusCode == HttpServletResponse.SC_MOVED_TEMPORARILY || statusCode == HttpServletResponse.SC_MOVED_PERMANENTLY) {
            redirect = true;
        }
    }

    @Override
    public void setHeader(String key, String value) {
        if ("Location".equals(key)) {
            redirectLocation = value;
        } else if ("Content-Type".equals(key)) {
            contentType = value;
        }
        // Else ignore
    }

    @Override
    public void sendError(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public void sendError(int statusCode, String msg) {
        this.statusCode = statusCode;
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return this.baos;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getContentType() {
        return StringUtils.defaultIfEmpty(contentType, super.getContentType());
    }
}


