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
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TagTitlesImplTest {

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
    TagManager tagManager;

    @Mock
    Tag tagB;

    @Mock
    Tag tagA;

    ComputedProperty<List<String>> computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new TagTitlesImpl());
        computedProperty = ctx.getService(ComputedProperty.class);

        when(asset.adaptTo(Resource.class)).thenReturn(resource);
        when(resource.getChild("jcr:content/metadata")).thenReturn(metadataResource);
        when(metadataResource.getResourceResolver()).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(TagManager.class)).thenReturn(tagManager);
    }

    @Test
    public void getName() {
        assertEquals(TagTitlesImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(TagTitlesImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{ComputedProperty.Types.METADATA}, computedProperty.getTypes());
    }

    @Test
    public void get_WithTagsSortedAlphabetically() {
        when(tagB.getTitle(any(Locale.class))).thenReturn("Bravo");
        when(tagA.getTitle(any(Locale.class))).thenReturn("Alpha");
        when(tagManager.getTags(metadataResource)).thenReturn(new Tag[]{tagB, tagA});

        final SlingHttpServletRequest request = ctx.request();

        final List<String> actual = computedProperty.get(asset, request);

        assertEquals(java.util.Arrays.asList("Alpha", "Bravo"), actual);
    }

    @Test
    public void get_WithNoTags() {
        when(tagManager.getTags(metadataResource)).thenReturn(null);

        final List<String> actual = computedProperty.get(asset, ctx.request());

        assertTrue(actual.isEmpty());
    }

    @Test
    public void get_WithNoTagManager() {
        when(resourceResolver.adaptTo(TagManager.class)).thenReturn(null);

        final List<String> actual = computedProperty.get(asset, ctx.request());

        assertTrue(actual.isEmpty());
    }
}
