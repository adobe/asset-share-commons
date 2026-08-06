/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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

package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.configuration.AssetDetailsSelector;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetDetailsResolverImplTest {

    private final AssetDetailsResolverImpl assetDetailsResolver = new AssetDetailsResolverImpl();

    @Mock
    Config config;

    @Mock
    AssetModel assetModel;

    @Mock
    ResourceResolver resourceResolver;

    @Before
    public void setUp() {
        when(config.getResourceResolver()).thenReturn(resourceResolver);
        when(config.getAssetDetailsUrl()).thenReturn("/content/asset-share/details.html");
    }

    private Resource existingResource() {
        final Resource resource = mock(Resource.class);
        when(resource.getResourceType()).thenReturn("some/resource/type");
        return resource;
    }

    private Resource nonExistingResource() {
        final Resource resource = mock(Resource.class);
        when(resource.getResourceType()).thenReturn("sling:nonexisting");
        return resource;
    }

    @Test
    public void getUrl_noSelectorsBound_usesDefault() {
        assertEquals("/content/asset-share/details.html", assetDetailsResolver.getUrl(config, assetModel));
    }

    @Test
    public void getUrl_selectorAccepts_returnsSelectorUrl() {
        final AssetDetailsSelector selector = mock(AssetDetailsSelector.class);
        when(selector.accepts(config, assetModel)).thenReturn(true);
        when(selector.getUrl(config, assetModel)).thenReturn("/content/asset-share/details/image.html");
        final Resource resource = existingResource();
        when(resourceResolver.resolve("/content/asset-share/details/image.html")).thenReturn(resource);

        assetDetailsResolver.bindAssetDetailsSelector(selector, new HashMap<>());

        assertEquals("/content/asset-share/details/image.html", assetDetailsResolver.getUrl(config, assetModel));
    }

    @Test
    public void getUrl_selectorDoesNotAccept_usesDefault() {
        final AssetDetailsSelector selector = mock(AssetDetailsSelector.class);
        when(selector.accepts(config, assetModel)).thenReturn(false);

        assetDetailsResolver.bindAssetDetailsSelector(selector, new HashMap<>());

        assertEquals("/content/asset-share/details.html", assetDetailsResolver.getUrl(config, assetModel));
    }

    @Test
    public void getUrl_selectorAccepts_blankUrl_usesDefault() {
        final AssetDetailsSelector selector = mock(AssetDetailsSelector.class);
        when(selector.accepts(config, assetModel)).thenReturn(true);
        when(selector.getUrl(config, assetModel)).thenReturn("");

        assetDetailsResolver.bindAssetDetailsSelector(selector, new HashMap<>());

        assertEquals("/content/asset-share/details.html", assetDetailsResolver.getUrl(config, assetModel));
    }

    @Test
    public void getUrl_selectorAccepts_nonExistingResource_usesDefault() {
        final AssetDetailsSelector selector = mock(AssetDetailsSelector.class);
        when(selector.accepts(config, assetModel)).thenReturn(true);
        when(selector.getUrl(config, assetModel)).thenReturn("/content/asset-share/details/video.html");
        final Resource resource = nonExistingResource();
        when(resourceResolver.resolve("/content/asset-share/details/video.html")).thenReturn(resource);

        assetDetailsResolver.bindAssetDetailsSelector(selector, new HashMap<>());

        assertEquals("/content/asset-share/details.html", assetDetailsResolver.getUrl(config, assetModel));
    }

    @Test
    public void getUrl_afterUnbind_usesDefault() {
        final AssetDetailsSelector selector = mock(AssetDetailsSelector.class);
        when(selector.accepts(config, assetModel)).thenReturn(true);
        when(selector.getUrl(config, assetModel)).thenReturn("/content/asset-share/details/image.html");
        final Resource resource = existingResource();
        when(resourceResolver.resolve("/content/asset-share/details/image.html")).thenReturn(resource);

        final Map<Object, Object> props = new HashMap<>();
        assetDetailsResolver.bindAssetDetailsSelector(selector, props);
        assertEquals("/content/asset-share/details/image.html", assetDetailsResolver.getUrl(config, assetModel));

        assetDetailsResolver.unbindAssetDetailsSelector(selector, props);
        assertEquals("/content/asset-share/details.html", assetDetailsResolver.getUrl(config, assetModel));
    }

    @Test
    public void getFullUrl_referenceById() {
        when(config.getAssetDetailReferenceById()).thenReturn(true);
        when(assetModel.getAssetId()).thenReturn("abc123");

        assertEquals("/content/asset-share/details.html/abc123.html", assetDetailsResolver.getFullUrl(config, assetModel));
    }

    @Test
    public void getFullUrl_notReferenceById() {
        when(config.getAssetDetailReferenceById()).thenReturn(false);
        when(assetModel.getUrl()).thenReturn("/content/dam/test.png");

        assertEquals("/content/asset-share/details.html/content/dam/test.png", assetDetailsResolver.getFullUrl(config, assetModel));
    }

    @Test
    public void getFullUrl_blankUrl_doesNotAppend() {
        when(config.getAssetDetailsUrl()).thenReturn("");

        assertEquals("", assetDetailsResolver.getFullUrl(config, assetModel));
    }
}
