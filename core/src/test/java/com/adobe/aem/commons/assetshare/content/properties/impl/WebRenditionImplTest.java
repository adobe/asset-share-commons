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

import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebRenditionImplTest {

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    @Mock
    Rendition webRendition;

    @Mock
    Rendition original;

    @Mock
    MimeTypeHelper mimeTypeHelper;

    WebRenditionImpl computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerService(MimeTypeHelper.class, mimeTypeHelper);
        computedProperty = ctx.registerInjectActivateService(new WebRenditionImpl());
    }

    @Test
    public void getName() {
        assertEquals(WebRenditionImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(WebRenditionImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{com.adobe.aem.commons.assetshare.content.properties.ComputedProperty.Types.RENDITION}, computedProperty.getTypes());
    }

    @Test
    public void get_WithBestFitWebRendition() {
        when(webRendition.getName()).thenReturn("cq5dam.web.1280.1280.png");
        when(webRendition.getMimeType()).thenReturn("image/png");
        when(webRendition.getPath()).thenReturn("/content/dam/test.png/jcr:content/renditions/cq5dam.web.1280.1280.png");
        when(asset.getRenditions()).thenReturn(Collections.singletonList(webRendition));
        when(mimeTypeHelper.isBrowserSupportedImage("image/png")).thenReturn(true);

        final String actual = computedProperty.get(asset, ctx.request());

        assertEquals("/content/dam/test.png/_jcr_content/renditions/cq5dam.web.1280.1280.png", actual);
    }

    @Test
    public void get_WithBestFitRenditionNotBrowserSupported_FallsBackToOriginal() {
        when(webRendition.getName()).thenReturn("cq5dam.web.1280.1280.tiff");
        when(webRendition.getMimeType()).thenReturn("image/tiff");
        when(asset.getRenditions()).thenReturn(Collections.singletonList(webRendition));
        when(mimeTypeHelper.isBrowserSupportedImage("image/tiff")).thenReturn(false);

        when(original.getMimeType()).thenReturn("image/jpeg");
        when(original.getPath()).thenReturn("/content/dam/test.png/jcr:content/renditions/original");
        when(asset.getOriginal()).thenReturn(original);
        when(mimeTypeHelper.isBrowserSupportedImage("image/jpeg")).thenReturn(true);

        final String actual = computedProperty.get(asset, ctx.request());

        assertEquals("/content/dam/test.png/_jcr_content/renditions/original", actual);
    }

    @Test
    public void get_WithNoRenditionsAndNoOriginal() {
        when(asset.getRenditions()).thenReturn(Collections.emptyList());
        when(asset.getOriginal()).thenReturn(null);

        final String actual = computedProperty.get(asset, ctx.request());

        assertEquals("", actual);
    }

    @Test
    public void get_WithOriginalNotBrowserSupported() {
        when(asset.getRenditions()).thenReturn(Collections.emptyList());

        when(original.getMimeType()).thenReturn("application/pdf");
        when(asset.getOriginal()).thenReturn(original);
        when(mimeTypeHelper.isBrowserSupportedImage("application/pdf")).thenReturn(false);

        final String actual = computedProperty.get(asset, ctx.request());

        assertEquals("", actual);
    }
}
