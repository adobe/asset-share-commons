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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.script.SimpleBindings;

import static org.apache.sling.api.scripting.SlingBindings.*;

public class AssetRenditionDownloadRequest extends SlingHttpServletRequestWrapper {
    private final String suffix;
    private final String[] selectors;
    private final String extension;
    private final String method;
    private Resource resource;
    private final SlingBindings bindings;

    public AssetRenditionDownloadRequest(final SlingHttpServletRequest wrappedRequest,
                                         final String method,
                                         final Resource resource,
                                         final String[] selectors,
                                         final String extension,
                                         final String suffix) {
        super(wrappedRequest);

        this.resource = resource;
        this.method = method;
        this.selectors = selectors;
        this.extension = extension;
        this.suffix = suffix;


        final SlingBindings existingBindings = (SlingBindings) wrappedRequest.getAttribute(SlingBindings.class.getName());

        final SimpleBindings bindings = new SimpleBindings();

        if (existingBindings != null) {
            bindings.put(SLING, existingBindings.getSling());
            bindings.put(RESPONSE, existingBindings.getResponse());
            bindings.put(REQUEST, this);
            bindings.put(READER, existingBindings.getReader());
            bindings.put(OUT, existingBindings.getOut());
            bindings.put(LOG, existingBindings.getLog());
        }

        bindings.put(REQUEST, this);
        bindings.put(RESOURCE, resource);
        bindings.put("resolver", resource.getResourceResolver());

        final SlingBindings slingBindings = new SlingBindings();
        slingBindings.putAll(bindings);

        this.bindings = slingBindings;
    }

    @Override
    public Object getAttribute(String name) {
        if (SlingBindings.class.getName().equals(name)) {
            return bindings;
        } else {
            return super.getAttribute(name);
        }
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public RequestPathInfo getRequestPathInfo() {
        return new RequestPathInfo() {
            @Nonnull
            @Override
            public String getResourcePath() {
                return getResource().getPath();
            }

            @CheckForNull
            @Override
            public String getExtension() {
                return extension;
            }

            @CheckForNull
            @Override
            public String getSelectorString() {
                return StringUtils.join(selectors, ".");
            }

            @Nonnull
            @Override
            public String[] getSelectors() {
                return selectors;
            }

            @CheckForNull
            @Override
            public String getSuffix() {
                return suffix;
            }

            @CheckForNull
            @Override
            public Resource getSuffixResource() {
                return getResource().getResourceResolver().getResource(getSuffix());
            }
        };
    }
}
