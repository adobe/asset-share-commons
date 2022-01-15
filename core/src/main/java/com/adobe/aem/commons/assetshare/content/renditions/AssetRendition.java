/*
 * Asset Share Commons
 *
 * Copyright (C) 2021 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Defines information on how to get an asset rendition using AEM's Async Download Framework
 */
public class AssetRendition {
    private static final Logger log = LoggerFactory.getLogger(AssetRendition.class);
    public static AssetRendition UNAVAILABLE_ASSET_RENDITION = new AssetRendition(URI.create("failed://to.resolve.asset.rendition"), 0L, "unavailable/unavailable");

    private URI binaryUri;
    private Optional<Long> size; // in Bytes
    private String mimeType;

    public AssetRendition(URI uri, Long size, String mimeType) {
        this(uri.toString(), size, mimeType);
    }

    public AssetRendition(String uri, Long size, String mimeType) {
        URI cleanURI = null;

        try {
            cleanURI = cleanURI(uri.toString());
        } catch (URISyntaxException e) {
            log.warn("Unable to clean the URI [ {} ], using it as is.", uri, e);
            cleanURI = URI.create(uri);
        }

        setBinaryUri(cleanURI);
        setSize(size);
        setMimeType(mimeType);
    }

    public URI getBinaryUri() {
        return binaryUri;
    }

    public void setBinaryUri(URI binaryUri) {
        this.binaryUri = binaryUri;
    }

    public Optional<Long> getSize() {
        if (size != null) {
            return size;
        } else {
            return Optional.empty();
        }
    }

    public void setSize(Long size) {
        if (size != null) {
            this.size = Optional.of(size);
        }
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    private URI cleanURI(String uri) throws URISyntaxException {
        uri  = StringUtils.replace(uri, " ", "%20");
        uri  = StringUtils.replace(uri, "/_jcr_content", "/jcr:content");

        return new URI(uri);
    }
}




