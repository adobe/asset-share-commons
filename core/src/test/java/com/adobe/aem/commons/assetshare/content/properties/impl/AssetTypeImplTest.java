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
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetTypeImplTest {

    private static final String MIMETYPE_LOOKUP_RESOURCE_PATH = "/mnt/overlay/dam/gui/content/assets/jcr:content/mimeTypeLookup";

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    @Mock
    ResourceResolverFactory resourceResolverFactory;

    @Mock
    ResourceResolver serviceResourceResolver;

    @Mock
    Resource mimeTypeLookupResource;

    @Mock
    Resource configuredEntry;

    ComputedProperty<String> computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerService(ResourceResolverFactory.class, resourceResolverFactory);
        ctx.registerInjectActivateService(new AssetTypeImpl());
        computedProperty = ctx.getService(ComputedProperty.class);
    }

    @Test
    public void getName() {
        assertEquals(AssetTypeImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(AssetTypeImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{ComputedProperty.Types.METADATA}, computedProperty.getTypes());
    }

    @Test
    public void get_MatchedByMimetypeLookupConfiguration() throws Exception {
        when(asset.getMimeType()).thenReturn("image/jpeg");
        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenReturn(serviceResourceResolver);
        when(serviceResourceResolver.getResource(MIMETYPE_LOOKUP_RESOURCE_PATH)).thenReturn(mimeTypeLookupResource);

        when(configuredEntry.adaptTo(org.apache.sling.api.resource.ValueMap.class)).thenReturn(
                new ValueMapDecorator(mapOf("mimetypes", "JPEG,PNG", "jcr:description", "Photo")));
        final Iterator<Resource> children = Collections.singletonList((Resource) configuredEntry).iterator();
        when(mimeTypeLookupResource.listChildren()).thenReturn(children);

        final String actual = computedProperty.get(asset);

        assertEquals("Photo", actual);
    }

    @Test
    public void get_FallsBackToImagePrefix() throws Exception {
        when(asset.getMimeType()).thenReturn("image/jpeg");
        givenNoMimetypeLookupMatch();

        final String actual = computedProperty.get(asset);

        assertEquals(AssetTypeImpl.IMAGE_LABEL, actual);
    }

    @Test
    public void get_FallsBackToTextPrefix() throws Exception {
        when(asset.getMimeType()).thenReturn("text/html");
        givenNoMimetypeLookupMatch();

        final String actual = computedProperty.get(asset);

        assertEquals(AssetTypeImpl.DOCUMENT_LABEL, actual);
    }

    @Test
    public void get_FallsBackToVideoPrefix() throws Exception {
        when(asset.getMimeType()).thenReturn("video/mp4");
        givenNoMimetypeLookupMatch();

        final String actual = computedProperty.get(asset);

        assertEquals(AssetTypeImpl.VIDEO_LABEL, actual);
    }

    @Test
    public void get_FallsBackToAudioPrefix() throws Exception {
        when(asset.getMimeType()).thenReturn("audio/mp3");
        givenNoMimetypeLookupMatch();

        final String actual = computedProperty.get(asset);

        assertEquals(AssetTypeImpl.AUDIO_LABEL, actual);
    }

    @Test
    public void get_FallsBackToApplicationPrefixDerivesFromLastSegment() throws Exception {
        when(asset.getMimeType()).thenReturn("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        givenNoMimetypeLookupMatch();

        final String actual = computedProperty.get(asset);

        assertEquals("SHEET", actual);
    }

    @Test
    public void get_UnknownMimeTypeReturnsUnknownLabel() throws Exception {
        when(asset.getMimeType()).thenReturn("foo/bar");
        givenNoMimetypeLookupMatch();

        final String actual = computedProperty.get(asset);

        assertEquals(AssetTypeImpl.UNKNOWN_LABEL, actual);
    }

    @Test
    public void get_BlankMimeType() throws Exception {
        when(asset.getMimeType()).thenReturn(null);
        givenNoMimetypeLookupMatch();

        final String actual = computedProperty.get(asset);

        assertEquals(AssetTypeImpl.UNKNOWN_LABEL, actual);
    }

    @Test
    public void get_WithLoginExceptionThrowsIllegalStateException() throws Exception {
        when(asset.getMimeType()).thenReturn("image/jpeg");
        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenThrow(new LoginException("no access"));

        assertThrows(IllegalStateException.class, () -> computedProperty.get(asset));
    }

    @Test
    public void get_WithMissingLookupResourceThrowsIllegalStateException() throws Exception {
        when(asset.getMimeType()).thenReturn("image/jpeg");
        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenReturn(serviceResourceResolver);
        when(serviceResourceResolver.getResource(MIMETYPE_LOOKUP_RESOURCE_PATH)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> computedProperty.get(asset));
    }

    private void givenNoMimetypeLookupMatch() throws Exception {
        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenReturn(serviceResourceResolver);
        when(serviceResourceResolver.getResource(MIMETYPE_LOOKUP_RESOURCE_PATH)).thenReturn(mimeTypeLookupResource);
        when(mimeTypeLookupResource.listChildren()).thenReturn(Collections.<Resource>emptyList().iterator());
    }

    private Map<String, Object> mapOf(Object... kvs) {
        final Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            map.put((String) kvs[i], kvs[i + 1]);
        }
        return map;
    }
}
