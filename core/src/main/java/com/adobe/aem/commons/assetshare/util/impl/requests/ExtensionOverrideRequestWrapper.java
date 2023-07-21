/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

import com.adobe.aem.commons.assetshare.util.impl.proxies.RequestPathInfoWrapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * SlingHttpServletRequest Wrapper that allows the force setting (or removal) of the original requests extension.
 *
 * This is useful when doing internal request dispatcher (include or forward) as there is no way to reset the extension via RequestDispatcherOptions.
 */
public class ExtensionOverrideRequestWrapper extends SlingHttpServletRequestWrapper {
    private final String extension;

    /**
     * @param wrappedRequest the request to wrap;
     * @param extension the extension to force. Set to null for no extension;
     */
    public ExtensionOverrideRequestWrapper(SlingHttpServletRequest wrappedRequest, String extension) {
        super(wrappedRequest);
        this.extension = extension;
    }

    public RequestPathInfo getRequestPathInfo() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("extension", this.extension);

        final RequestPathInfoWrapper requestPathInfoWrapper = RequestPathInfoWrapper.createRequestPathInfoWrapper(super.getRequestPathInfo(), new ValueMapDecorator(overrides), super.getResource());

        RequestPathInfo wrappedRequestInfo = (RequestPathInfo) Proxy.newProxyInstance(
                ExtensionOverrideRequestWrapper.WrappedRequestPathInfoWrapper.class.getClassLoader(),
                new Class[] { RequestPathInfo.class, ExtensionOverrideRequestWrapper.WrappedRequestPathInfoWrapper.class  },
                requestPathInfoWrapper);

        return wrappedRequestInfo;
    }


    public interface WrappedRequestPathInfoWrapper {
    }

}