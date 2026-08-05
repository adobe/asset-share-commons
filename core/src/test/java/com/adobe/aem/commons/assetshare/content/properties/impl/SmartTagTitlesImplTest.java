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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SmartTagTitlesImplTest {

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    @Mock
    Resource resource;

    @Mock
    Resource predictedTagsResource;

    @Mock
    Resource tagLow;

    @Mock
    Resource tagHigh;

    @Mock
    Resource tagBlankName;

    ComputedProperty<List<String>> computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new SmartTagTitlesImpl());
        computedProperty = ctx.getService(ComputedProperty.class);

        when(asset.adaptTo(Resource.class)).thenReturn(resource);
        when(resource.getChild("jcr:content/metadata/predictedTags")).thenReturn(predictedTagsResource);
    }

    @Test
    public void getName() {
        assertEquals(SmartTagTitlesImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(SmartTagTitlesImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{ComputedProperty.Types.METADATA}, computedProperty.getTypes());
    }

    @Test
    public void get_WithNullAsset() {
        final List<String> actual = computedProperty.get(null, ctx.request());

        assertTrue(actual.isEmpty());
    }

    @Test
    public void get_WithNullResource() {
        when(asset.adaptTo(Resource.class)).thenReturn(null);

        final List<String> actual = computedProperty.get(asset, ctx.request());

        assertTrue(actual.isEmpty());
    }

    @Test
    public void get_WithNoPredictedTagsResource() {
        when(resource.getChild("jcr:content/metadata/predictedTags")).thenReturn(null);

        final List<String> actual = computedProperty.get(asset, ctx.request());

        assertTrue(actual.isEmpty());
    }

    @Test
    public void get_SortedByConfidenceAscendingAndFilteringBlanks() {
        when(tagLow.getValueMap()).thenReturn(new org.apache.sling.api.wrappers.ValueMapDecorator(
                mapOf("name", "Low Confidence Tag", "confidence", 0.2d)));
        when(tagHigh.getValueMap()).thenReturn(new org.apache.sling.api.wrappers.ValueMapDecorator(
                mapOf("name", "High Confidence Tag", "confidence", 0.9d)));
        when(tagBlankName.getValueMap()).thenReturn(new org.apache.sling.api.wrappers.ValueMapDecorator(
                mapOf("confidence", 0.5d)));

        final List<Resource> children = Arrays.asList(tagHigh, tagLow, tagBlankName);
        when(predictedTagsResource.listChildren()).thenReturn(children.iterator());

        final List<String> actual = computedProperty.get(asset, ctx.request());

        assertEquals(Arrays.asList("Low Confidence Tag", "High Confidence Tag"), actual);
    }

    private java.util.Map<String, Object> mapOf(Object... kvs) {
        final java.util.Map<String, Object> map = new java.util.HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((String) kvs[i], kvs[i + 1]);
        }
        return map;
    }
}
