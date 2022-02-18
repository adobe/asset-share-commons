/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2017 Adobe
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
package com.adobe.aem.commons.assetshare.util.impl.proxies;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RequestPathInfoWrapper implements InvocationHandler {
    private final RequestPathInfo wrappedRequestPathInfo;
    private final ValueMap requestPathInfoOverrides;
    private final Resource resource;

    private RequestPathInfoWrapper(RequestPathInfo requestPathInfo, ValueMap requestPathInfoOverrides, Resource resource) {
        this.wrappedRequestPathInfo = requestPathInfo;
        this.requestPathInfoOverrides = requestPathInfoOverrides;
        this.resource = resource;
    }

    public static RequestPathInfoWrapper createRequestPathInfoWrapper(final RequestPathInfo requestPathInfo, final ValueMap overrides, final Resource resource) {
        return new RequestPathInfoWrapper(requestPathInfo, overrides, resource);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        switch (methodName) {
            case "getResourcePath":
                return getResourcePath();
            case "getExtension":
                return getExtension();
            case "getSelectorString":
                return getSelectorString();
            case "getSelectors":
                return getSelectors();
            case "getSuffix":
                return getSuffix();
            case "getSuffixResource":
                return getSuffixResource();
            default:
                throw new UnsupportedOperationException("REQUESTPATHINFOWRAPPER >> NO IMPLEMENTATION FOR " + methodName);
        }
    }

    public String getResourcePath() {
        return resource.getPath();
    }

    public String getExtension() {
        if (requestPathInfoOverrides.containsKey("extension")) {
            return requestPathInfoOverrides.get("extension", String.class);
        } else {
            return wrappedRequestPathInfo.getExtension();
        }
    }

    public String getSelectorString() {
        if (requestPathInfoOverrides.containsKey("selectors")) {
            return StringUtils.join(getSelectors(), ".");
        } else {
            return wrappedRequestPathInfo.getSelectorString();
        }

    }

    public String[] getSelectors() {
        if (requestPathInfoOverrides.containsKey("selectors")) {
            return requestPathInfoOverrides.get("selectors", String[].class);
        } else {
            return wrappedRequestPathInfo.getSelectors();
        }
    }

    public String getSuffix() {
        if (requestPathInfoOverrides.containsKey("suffix")) {
            return requestPathInfoOverrides.get("suffix", String.class);
        } else {
            return wrappedRequestPathInfo.getSuffix();
        }
    }

    public Resource getSuffixResource() {
        if (requestPathInfoOverrides.containsKey("suffix")) {
            return resource.getResourceResolver().getResource(getSuffix());
        } else {
            return wrappedRequestPathInfo.getSuffixResource();
        }
    }
}