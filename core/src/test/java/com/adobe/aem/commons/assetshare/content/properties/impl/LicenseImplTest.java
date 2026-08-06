/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.content.properties.impl;

import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LicenseImplTest {

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    @Mock
    Resource resource;

    @Mock
    Resource metadataResource;

    @Mock
    ResourceResolver resourceResolver;

    @Mock
    Resource licenseResource;

    ComputedProperty<String> computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new LicenseImpl());
        computedProperty = ctx.getService(ComputedProperty.class);

        when(asset.adaptTo(Resource.class)).thenReturn(resource);
        when(resource.getChild("jcr:content/metadata")).thenReturn(metadataResource);
        when(resource.getResourceResolver()).thenReturn(resourceResolver);
    }

    @Test
    public void getName() {
        assertEquals(LicenseImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(LicenseImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{ComputedProperty.Types.METADATA}, computedProperty.getTypes());
    }

    @Test
    public void get_WithExternalUrl() {
        final Map<String, Object> map = new HashMap<>();
        map.put("xmpRights:WebStatement", "https://example.com/license");
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(map));

        final String actual = computedProperty.get(asset);

        assertEquals("https://example.com/license", actual);
    }

    @Test
    public void get_WithDoubleSlashUrl() {
        final Map<String, Object> map = new HashMap<>();
        map.put("xmpRights:WebStatement", "//example.com/license");
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(map));

        final String actual = computedProperty.get(asset);

        assertEquals("//example.com/license", actual);
    }

    @Test
    public void get_WithValidResourcePath() {
        final Map<String, Object> map = new HashMap<>();
        map.put("xmpRights:WebStatement", "/content/license");
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(map));
        when(resourceResolver.resolve("/content/license")).thenReturn(licenseResource);

        final String actual = computedProperty.get(asset);

        assertEquals("/content/license", actual);
    }

    @Test
    public void get_WithInvalidResourcePath() {
        final Map<String, Object> map = new HashMap<>();
        map.put("xmpRights:WebStatement", "/content/does-not-exist");
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(map));
        when(resourceResolver.resolve("/content/does-not-exist")).thenReturn(null);

        final String actual = computedProperty.get(asset);

        assertNull(actual);
    }

    @Test
    public void get_WithNoLicense() {
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(Collections.emptyMap()));

        final String actual = computedProperty.get(asset);

        assertNull(actual);
    }
}
