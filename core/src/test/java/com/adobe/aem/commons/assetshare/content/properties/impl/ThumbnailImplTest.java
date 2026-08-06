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

import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThumbnailImplTest {

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    @Mock
    Rendition thumbnailRendition;

    @Mock
    Rendition imagePreviewRendition;

    @Mock
    MimeTypeHelper mimeTypeHelper;

    ThumbnailImpl computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerService(MimeTypeHelper.class, mimeTypeHelper);
        computedProperty = ctx.registerInjectActivateService(new ThumbnailImpl());
    }

    @Test
    public void getName() {
        assertEquals(ThumbnailImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(ThumbnailImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{
                com.adobe.aem.commons.assetshare.content.properties.ComputedProperty.Types.RENDITION,
                com.adobe.aem.commons.assetshare.content.properties.ComputedProperty.Types.VIDEO_RENDITION
        }, computedProperty.getTypes());
    }

    @Test
    public void accepts_AlwaysTrue() {
        assertTrue(computedProperty.accepts(asset, "anything"));
    }

    @Test
    public void get_WithThumbnailRendition() {
        when(asset.getRendition("cq5dam.thumbnail.319.319.png")).thenReturn(thumbnailRendition);
        when(thumbnailRendition.getMimeType()).thenReturn("image/png");
        when(thumbnailRendition.getPath()).thenReturn("/content/dam/test.png/jcr:content/renditions/cq5dam.thumbnail.319.319.png");
        when(mimeTypeHelper.isBrowserSupportedImage("image/png")).thenReturn(true);

        final String actual = computedProperty.get(asset);

        assertEquals("/content/dam/test.png/_jcr_content/renditions/cq5dam.thumbnail.319.319.png", actual);
    }

    @Test
    public void get_WithNoThumbnailFallsBackToImagePreview() {
        when(asset.getRendition("cq5dam.thumbnail.319.319.png")).thenReturn(null);
        when(asset.getImagePreviewRendition()).thenReturn(imagePreviewRendition);
        when(imagePreviewRendition.getMimeType()).thenReturn("image/jpeg");
        when(imagePreviewRendition.getPath()).thenReturn("/content/dam/test.png/jcr:content/renditions/cq5dam.web.1280.1280.jpeg");
        when(mimeTypeHelper.isBrowserSupportedImage("image/jpeg")).thenReturn(true);

        final String actual = computedProperty.get(asset);

        assertEquals("/content/dam/test.png/_jcr_content/renditions/cq5dam.web.1280.1280.jpeg", actual);
    }

    @Test
    public void get_WithNoRenditionAtAll() {
        when(asset.getRendition("cq5dam.thumbnail.319.319.png")).thenReturn(null);
        when(asset.getImagePreviewRendition()).thenReturn(null);

        final String actual = computedProperty.get(asset);

        assertEquals("", actual);
    }

    @Test
    public void get_WithRenditionNotBrowserSupported() {
        when(asset.getRendition("cq5dam.thumbnail.319.319.png")).thenReturn(thumbnailRendition);
        when(thumbnailRendition.getMimeType()).thenReturn("application/pdf");
        when(mimeTypeHelper.isBrowserSupportedImage("application/pdf")).thenReturn(false);

        final String actual = computedProperty.get(asset);

        assertEquals("", actual);
    }
}
