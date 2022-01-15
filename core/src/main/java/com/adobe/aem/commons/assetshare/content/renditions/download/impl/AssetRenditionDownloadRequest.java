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

import com.adobe.aem.commons.assetshare.util.impl.proxies.RequestPathInfoWrapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import javax.script.SimpleBindings;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.sling.api.scripting.SlingBindings.*;

public class AssetRenditionDownloadRequest extends SlingHttpServletRequestWrapper {
    private final String suffix;
    private final String[] selectors;
    private final String extension;
    private final String method;
    private final SlingHttpServletRequest wrappedRequest;
    private Resource resource;
    private final SlingBindings bindings;

    public AssetRenditionDownloadRequest(final SlingHttpServletRequest wrappedRequest,
                                         final String method,
                                         final Resource resource,
                                         final String[] selectors,
                                         final String extension,
                                         final String suffix) {
        super(wrappedRequest);

        this.wrappedRequest = wrappedRequest;

        this.resource = resource;
        this.method = method;
        if (selectors != null) {
            this.selectors = Arrays.copyOf(selectors, selectors.length);
        } else {
            this.selectors = new String[]{};
        }
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
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("selectors", selectors);
        overrides.put("suffix", suffix);
        overrides.put("extension", this.extension);

        return getWrappedRequestPathInfo(wrappedRequest.getRequestPathInfo(), new ValueMapDecorator(overrides), resource);
    }

    private RequestPathInfo getWrappedRequestPathInfo( RequestPathInfo requestPathInfo, ValueMap requestPathInfoOverrides, Resource resource) {

        final RequestPathInfoWrapper requestPathInfoWrapper = RequestPathInfoWrapper.createRequestPathInfoWrapper(requestPathInfo, requestPathInfoOverrides, resource);

        RequestPathInfo wrappedRequestInfo = (RequestPathInfo) Proxy.newProxyInstance(
                AssetRenditionDownloadRequest.WrappedRequestPathInfoWrapper.class.getClassLoader(),
                new Class[]{RequestPathInfo.class, AssetRenditionDownloadRequest.WrappedRequestPathInfoWrapper.class},
                requestPathInfoWrapper);

        return wrappedRequestInfo;
    }

    public interface WrappedRequestPathInfoWrapper {}

}
