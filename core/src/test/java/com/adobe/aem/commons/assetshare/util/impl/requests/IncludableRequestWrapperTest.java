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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IncludableRequestWrapperTest {

    @Rule
    public final AemContext ctx = new AemContext();

    private IncludableRequestWrapper wrapper;

    @Before
    public void setUp() {
        ctx.create().resource("/content/test");
        ctx.currentResource("/content/test");
        ctx.request().setContentType("text/plain");
        ctx.request().setAttribute("wrapped-attribute", "wrapped-value");

        wrapper = new IncludableRequestWrapper(ctx.request(), "json");
    }

    @Test
    public void getContentType_defaultsToWrappedRequestContentType() {
        assertEquals("text/plain", wrapper.getContentType());
    }

    @Test
    public void setContentType_overridesContentType() {
        wrapper.setContentType("application/json");

        assertEquals("application/json", wrapper.getContentType());
    }

    @Test
    public void setAndGetAttribute_localAttributeTakesPrecedence() {
        wrapper.setAttribute("wrapped-attribute", "local-value");

        assertEquals("local-value", wrapper.getAttribute("wrapped-attribute"));
    }

    @Test
    public void getAttribute_fallsBackToWrappedRequest() {
        assertEquals("wrapped-value", wrapper.getAttribute("wrapped-attribute"));
    }

    @Test
    public void getAttribute_missingAttribute_returnsNull() {
        assertNull(wrapper.getAttribute("does-not-exist"));
    }

    @Test
    public void removeAttribute_removesLocalAttribute() {
        wrapper.setAttribute("local-only", "value");

        wrapper.removeAttribute("local-only");

        assertNull(wrapper.getAttribute("local-only"));
    }

    @Test
    public void removeAttribute_delegatesToWrappedRequestWhenNotLocal() {
        // The attribute exists only on the wrapped request, not the wrapper's local attribute map.
        wrapper.removeAttribute("wrapped-attribute");

        assertNull(wrapper.getAttribute("wrapped-attribute"));
        assertNull(ctx.request().getAttribute("wrapped-attribute"));
    }

    @Test
    public void getAttributeNames_combinesLocalAndWrappedAttributeNames() {
        wrapper.setAttribute("local-attribute", "local-value");

        final Enumeration<String> names = wrapper.getAttributeNames();
        final Set<String> actual = new HashSet<>(Collections.list(names));

        assertTrue(actual.contains("wrapped-attribute"));
        assertTrue(actual.contains("local-attribute"));
    }

    @Test
    public void getAttributeNames_overriddenAttributeNameIsReturnedTwice() {
        // NOTE: IncludableRequestWrapper#getAttributeNames() concatenates the wrapped request's
        // attribute names with the local override keySet() without de-duplicating. When a locally
        // set attribute shadows one already present on the wrapped request (as done here), its name
        // is returned twice from the enumeration. This looks like an oversight in the production code
        // (see IncludableRequestWrapper#getAttributeNames()); documenting the current behavior here
        // rather than silently asserting away the duplication.
        wrapper.setAttribute("wrapped-attribute", "overridden-value");

        final Enumeration<String> names = wrapper.getAttributeNames();
        int count = 0;
        while (names.hasMoreElements()) {
            if ("wrapped-attribute".equals(names.nextElement())) {
                count++;
            }
        }

        assertEquals(2, count);
    }
}
