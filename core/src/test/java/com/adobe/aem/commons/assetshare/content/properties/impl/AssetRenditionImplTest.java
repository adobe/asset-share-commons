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

package com.adobe.aem.commons.assetshare.content.properties.impl;

import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionImplTest {

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    ComputedProperty<String> computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new AssetRenditionImpl());
        computedProperty = ctx.getService(ComputedProperty.class);

        when(asset.getPath()).thenReturn("/content/dam/test.png");
    }

    @Test
    public void getName() {
        assertEquals(AssetRenditionImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(AssetRenditionImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{}, computedProperty.getTypes());
    }

    @Test
    public void get_WithoutDownload() {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", "cq5dam.web.1280.1280");
        final ValueMap parameters = new ValueMapDecorator(params);

        final String expected = "/content/dam/test.png.renditions/cq5dam.web.1280.1280/asset.rendition";
        final String actual = computedProperty.get(asset, ctx.request(), parameters);

        assertEquals(expected, actual);
    }

    @Test
    public void get_WithDownload() {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", "cq5dam.web.1280.1280");
        params.put("download", true);
        final ValueMap parameters = new ValueMapDecorator(params);

        final String expected = "/content/dam/test.png.renditions/cq5dam.web.1280.1280/download/asset.rendition";
        final String actual = computedProperty.get(asset, ctx.request(), parameters);

        assertEquals(expected, actual);
    }
}
