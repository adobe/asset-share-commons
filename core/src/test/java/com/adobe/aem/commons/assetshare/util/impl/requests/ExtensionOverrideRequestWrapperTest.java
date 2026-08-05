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

package com.adobe.aem.commons.assetshare.util.impl.requests;

import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.request.RequestPathInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExtensionOverrideRequestWrapperTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() {
        ctx.create().resource("/content/test");
        ctx.currentResource("/content/test");
        ctx.requestPathInfo().setSelectorString("foo.bar");
        ctx.requestPathInfo().setSuffix("/content/suffix");
        ctx.requestPathInfo().setExtension("html");

        ctx.create().resource("/content/suffix");
    }

    @Test
    public void getRequestPathInfo_overridesExtension() {
        final ExtensionOverrideRequestWrapper wrapper = new ExtensionOverrideRequestWrapper(ctx.request(), "json");

        final RequestPathInfo requestPathInfo = wrapper.getRequestPathInfo();

        assertEquals("json", requestPathInfo.getExtension());
    }

    @Test
    public void getRequestPathInfo_nullExtension_returnsNull() {
        final ExtensionOverrideRequestWrapper wrapper = new ExtensionOverrideRequestWrapper(ctx.request(), null);

        final RequestPathInfo requestPathInfo = wrapper.getRequestPathInfo();

        assertNull(requestPathInfo.getExtension());
    }

    @Test
    public void getRequestPathInfo_delegatesResourcePath() {
        final ExtensionOverrideRequestWrapper wrapper = new ExtensionOverrideRequestWrapper(ctx.request(), "json");

        final RequestPathInfo requestPathInfo = wrapper.getRequestPathInfo();

        assertEquals("/content/test", requestPathInfo.getResourcePath());
    }

    @Test
    public void getRequestPathInfo_delegatesSelectors() {
        final ExtensionOverrideRequestWrapper wrapper = new ExtensionOverrideRequestWrapper(ctx.request(), "json");

        final RequestPathInfo requestPathInfo = wrapper.getRequestPathInfo();

        assertEquals("foo.bar", requestPathInfo.getSelectorString());
        assertArrayEquals(new String[]{"foo", "bar"}, requestPathInfo.getSelectors());
    }

    @Test
    public void getRequestPathInfo_delegatesSuffix() {
        final ExtensionOverrideRequestWrapper wrapper = new ExtensionOverrideRequestWrapper(ctx.request(), "json");

        final RequestPathInfo requestPathInfo = wrapper.getRequestPathInfo();

        assertEquals("/content/suffix", requestPathInfo.getSuffix());
        assertEquals("/content/suffix", requestPathInfo.getSuffixResource().getPath());
    }
}
