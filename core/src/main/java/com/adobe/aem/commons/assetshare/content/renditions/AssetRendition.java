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

import java.net.URI;
import java.util.Optional;

/**
 * Defines information on how to get an asset rendition using AEM's Async Download Framework
 */
public class AssetRendition {
    public static AssetRendition UNAVAILABLE_ASSET_RENDITION = new AssetRendition(URI.create("failed://to.resolve.asset.rendition"), 0L, "unavailable/unavailable");

    private URI binaryUri;
    private Optional<Long> size; // in Bytes
    private String mimeType;

    public AssetRendition(URI uri, Long size, String mimeType) {
        setBinaryUri(uri);
        setSize(size);
        setMimeType(mimeType);
    }

    public AssetRendition(String uri, Long size, String mimeType) {
        setBinaryUri(URI.create(uri));
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

}




