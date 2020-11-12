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

package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.ServletHelper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.scripting.core.ScriptHelper;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.script.SimpleBindings;

import static org.apache.sling.api.scripting.SlingBindings.*;

@Component
public class ServletHelperImpl implements ServletHelper {
    private BundleContext bundleContext;

    public void addSlingBindings(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        final SimpleBindings bindings = new SimpleBindings();

        final ScriptHelper scriptHelper = new ScriptHelper(bundleContext, null, request, response);
        bindings.put(SLING, scriptHelper);
        bindings.put(RESPONSE, response);
        bindings.put(REQUEST, request);
        bindings.put(RESOURCE, request.getResource());
        bindings.put(RESOLVER, request.getResourceResolver());

        final SlingBindings slingBindings = new SlingBindings();
        slingBindings.putAll(bindings);

        request.setAttribute(SlingBindings.class.getName(), slingBindings);
    }

    @Activate
    protected void activate(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
