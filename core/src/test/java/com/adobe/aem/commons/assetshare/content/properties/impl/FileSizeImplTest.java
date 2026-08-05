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
import com.day.cq.dam.api.Rendition;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileSizeImplTest {

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    @Mock
    Resource resource;

    @Mock
    Resource metadataResource;

    @Mock
    Rendition original;

    @Mock
    SlingHttpServletRequest request;

    ComputedProperty<String> computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new FileSizeImpl());
        computedProperty = ctx.getService(ComputedProperty.class);

        when(asset.adaptTo(Resource.class)).thenReturn(resource);
        when(resource.getChild("jcr:content/metadata")).thenReturn(metadataResource);
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(Collections.emptyMap()));

        final ResourceBundle bundle = new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return key;
            }

            @Override
            public java.util.Enumeration<String> getKeys() {
                return java.util.Collections.emptyEnumeration();
            }
        };
        when(request.getResourceBundle(any())).thenReturn(bundle);
    }

    @Test
    public void getName() {
        assertEquals(FileSizeImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(FileSizeImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{ComputedProperty.Types.METADATA}, computedProperty.getTypes());
    }

    @Test
    public void get_WithDamSizeAndNoRequest() {
        final Map<String, Object> map = new HashMap<>();
        map.put("dam:size", 2048L);
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(map));

        final String actual = computedProperty.get(asset, (SlingHttpServletRequest) null);

        assertEquals("2.0 KB", actual);
    }

    @Test
    public void get_WithDamSizeAndRequest() {
        final Map<String, Object> map = new HashMap<>();
        map.put("dam:size", 2048L);
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(map));

        final String actual = computedProperty.get(asset, request);

        assertEquals("2.0 KB", actual);
    }

    @Test
    public void get_WithoutDamSizeFallsBackToOriginalRenditionSize() {
        when(asset.getOriginal()).thenReturn(original);
        when(original.getSize()).thenReturn(1024L);

        final String actual = computedProperty.get(asset, (SlingHttpServletRequest) null);

        assertEquals("1.0 KB", actual);
    }

    @Test
    public void get_WithoutDamSizeAndNoOriginal() {
        when(asset.getOriginal()).thenReturn(null);

        final String actual = computedProperty.get(asset, (SlingHttpServletRequest) null);

        assertEquals("0.0 B", actual);
    }
}
