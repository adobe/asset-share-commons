/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2013 Adobe
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
package com.adobe.acs.commons.util;

import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathInfoUtilTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Test
    public void getQueryParam_returnsValue() {
        ctx.request().setParameterMap(java.util.Collections.singletonMap("foo", "bar"));
        assertEquals("bar", PathInfoUtil.getQueryParam(ctx.request(), "foo"));
    }

    @Test
    public void getQueryParam_missing_returnsNull() {
        assertNull(PathInfoUtil.getQueryParam(ctx.request(), "missing"));
    }

    @Test
    public void getQueryParam_withDefault_returnsValueWhenPresent() {
        ctx.request().setParameterMap(java.util.Collections.singletonMap("foo", "bar"));
        assertEquals("bar", PathInfoUtil.getQueryParam(ctx.request(), "foo", "default"));
    }

    @Test
    public void getQueryParam_withDefault_returnsDefaultWhenBlank() {
        assertEquals("default", PathInfoUtil.getQueryParam(ctx.request(), "missing", "default"));
    }

    @Test
    public void getSelector_returnsSelectorAtIndex() {
        ctx.requestPathInfo().setSelectorString("selA.selB");
        assertEquals("selA", PathInfoUtil.getSelector(ctx.request(), 0));
        assertEquals("selB", PathInfoUtil.getSelector(ctx.request(), 1));
    }

    @Test
    public void getSelector_outOfBounds_returnsNull() {
        ctx.requestPathInfo().setSelectorString("selA");
        assertNull(PathInfoUtil.getSelector(ctx.request(), 5));
    }

    @Test
    public void getSelector_withDefault_returnsDefaultWhenOutOfBounds() {
        ctx.requestPathInfo().setSelectorString("selA");
        assertEquals("default2", PathInfoUtil.getSelector(ctx.request(), 1, "default2"));
    }

    @Test
    public void getSuffixSegments_splitsOnSlash() {
        ctx.requestPathInfo().setSuffix("/segment-0/segment-1/segment-2");
        assertArrayEquals(new String[]{"segment-0", "segment-1", "segment-2"},
                PathInfoUtil.getSuffixSegments(ctx.request()));
    }

    @Test
    public void getSuffixSegments_noSuffix_returnsEmptyArray() {
        assertArrayEquals(new String[]{}, PathInfoUtil.getSuffixSegments(ctx.request()));
    }

    @Test
    public void getSuffixSegment_returnsSegmentAtIndex() {
        ctx.requestPathInfo().setSuffix("/suffixA/suffixB");
        assertEquals("suffixA", PathInfoUtil.getSuffixSegment(ctx.request(), 0));
        assertEquals("suffixB", PathInfoUtil.getSuffixSegment(ctx.request(), 1));
    }

    @Test
    public void getSuffixSegment_outOfBounds_returnsNull() {
        ctx.requestPathInfo().setSuffix("/suffixA");
        assertNull(PathInfoUtil.getSuffixSegment(ctx.request(), 5));
    }

    @Test
    public void getSuffix_returnsFullSuffix() {
        ctx.requestPathInfo().setSuffix("/suffixA/suffixB");
        assertEquals("/suffixA/suffixB", PathInfoUtil.getSuffix(ctx.request()));
    }

    @Test
    public void getSuffix_noSuffix_returnsNull() {
        assertNull(PathInfoUtil.getSuffix(ctx.request()));
    }

    @Test
    public void getFirstSuffixSegment_returnsFirst() {
        ctx.requestPathInfo().setSuffix("/suffixA/suffixB");
        assertEquals("suffixA", PathInfoUtil.getFirstSuffixSegment(ctx.request()));
    }

    @Test
    public void getLastSuffixSegment_returnsLast() {
        ctx.requestPathInfo().setSuffix("/suffixA/suffixB");
        assertEquals("suffixB", PathInfoUtil.getLastSuffixSegment(ctx.request()));
    }

    @Test
    public void getLastSuffixSegment_noSuffix_returnsNull() {
        assertNull(PathInfoUtil.getLastSuffixSegment(ctx.request()));
    }
}
