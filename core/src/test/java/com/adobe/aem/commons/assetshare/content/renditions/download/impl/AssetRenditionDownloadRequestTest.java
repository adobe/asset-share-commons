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

import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class AssetRenditionDownloadRequestTest {

    @Rule
    public AemContext ctx = new AemContext();

    Resource resource;

    @Before
    public void setUp() {
        resource = ctx.create().resource("/content/test");
    }

    @Test
    public void getAttribute() {
        final SlingHttpServletRequest request = new AssetRenditionDownloadRequest(
                ctx.request(),
                "GET",
                resource,
                new String[]{},
                "html",
                "/my/suffix");

        SlingBindings actual = (SlingBindings) request.getAttribute(SlingBindings.class.getName());

        assertEquals(resource, actual.get(SlingBindings.RESOURCE));
        assertEquals(ctx.resourceResolver(), actual.get(SlingBindings.RESOLVER));

        assertNull(actual.get("not-sling-bindings"));
    }

    @Test
    public void getResource() {
        final Resource expected = resource;

        final SlingHttpServletRequest request = new AssetRenditionDownloadRequest(
                ctx.request(),
                "GET",
                expected,
                new String[]{},
                "html",
                "/my/suffix");

        final Resource actual = request.getResource();

        assertEquals(expected, actual);
    }

    @Test
    public void getMethod() {
        final String expected = "HEAD";

        final SlingHttpServletRequest request = new AssetRenditionDownloadRequest(
                ctx.request(),
                expected,
                resource,
                new String[]{},
                "html",
                "/my/suffix");

        final String actual = request.getMethod();

        assertEquals(expected, actual);
    }

    @Test
    public void getRequestPathInfo() {
        final String expectedResourcePath = "/content/test";
        final String expectedSuffix = "/content/my/suffix";
        final String expectedExtension = "html";
        final String[] expectedSelectors = new String[]{"foo", "bar"};
        final String expectedSelectorString = StringUtils.join(expectedSelectors, ".");
        final Resource expectedSuffixResource = ctx.create().resource(expectedSuffix);

        final SlingHttpServletRequest request = new AssetRenditionDownloadRequest(
                ctx.request(),
                "GET",
                resource,
                expectedSelectors,
                expectedExtension,
                expectedSuffix);

        RequestPathInfo actual = request.getRequestPathInfo();

        assertEquals(expectedResourcePath, actual.getResourcePath());
        assertArrayEquals(expectedSelectors, actual.getSelectors());
        assertEquals(expectedSelectorString, actual.getSelectorString());
        assertEquals(expectedExtension, actual.getExtension());
        assertEquals(expectedSuffix, actual.getSuffix());
        assertEquals(expectedSuffixResource.getPath(), actual.getSuffixResource().getPath());
    }
}