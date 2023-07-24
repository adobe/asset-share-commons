/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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

package com.adobe.aem.commons.assetshare.util.impl.requests;

import org.apache.sling.api.SlingHttpServletRequest;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

public class IncludableRequestWrapper extends ExtensionOverrideRequestWrapper{
    private HashMap<String, Object> attributes = new HashMap<>();
    private String contentType;

    /**
     * @param wrappedRequest the request to wrap;
     * @param extension      the extension to force. Set to null for no extension;
     */
    public IncludableRequestWrapper(SlingHttpServletRequest wrappedRequest, String extension) {
        super(wrappedRequest, extension);
        contentType = wrappedRequest.getContentType();
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    @Override
    public Object getAttribute(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        return super.getAttribute(name);
    }

    @Override
    public void removeAttribute(String name) {
        if (attributes.containsKey(name)) {
            attributes.remove(name);
            return;
        }
        super.removeAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        List<String> attributeNames = Collections.list(super.getAttributeNames());
        attributeNames.addAll(attributes.keySet());

        return Collections.enumeration(attributeNames);
    }
}
