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

import com.day.cq.dam.api.Asset;
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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResolutionImplTest {

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    @Mock
    Resource resource;

    @Mock
    Resource metadataResource;

    @Mock
    SlingHttpServletRequest request;

    ResolutionImpl computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new HeightImpl());
        ctx.registerInjectActivateService(new WidthImpl());
        computedProperty = ctx.registerInjectActivateService(new ResolutionImpl());

        when(asset.adaptTo(Resource.class)).thenReturn(resource);
        when(resource.getChild("jcr:content/metadata")).thenReturn(metadataResource);

        // Provide a pass-through ResourceBundle (no locale, identity translations) so that
        // com.day.cq.i18n.I18n (used internally by UIHelper.getResolutionLabel) does not blow
        // up with a MissingResourceException when the "{0} x {1}" key isn't translated.
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
        assertEquals(ResolutionImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(ResolutionImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{com.adobe.aem.commons.assetshare.content.properties.ComputedProperty.Types.METADATA}, computedProperty.getTypes());
    }

    @Test
    public void get_WithWidthAndHeight() {
        final Map<String, Object> map = new HashMap<>();
        map.put("tiff:ImageWidth", 1280L);
        map.put("tiff:ImageLength", 720L);
        final ValueMap valueMap = new ValueMapDecorator(map);
        when(metadataResource.getValueMap()).thenReturn(valueMap);

        final String actual = computedProperty.get(asset, request);

        assertEquals("1280 x 720", actual);
    }

    @Test
    public void get_WithoutWidthOrHeight() {
        when(metadataResource.getValueMap()).thenReturn(new ValueMapDecorator(new HashMap<>()));

        final String actual = computedProperty.get(asset, request);

        assertEquals("", actual);
    }
}
