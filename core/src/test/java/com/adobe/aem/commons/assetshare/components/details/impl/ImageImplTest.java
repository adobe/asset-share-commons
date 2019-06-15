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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Image;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.properties.impl.TitleImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import com.adobe.aem.commons.assetshare.util.impl.MimeTypeHelperImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class ImageImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/ImageImplTest.json", "/content");

        ctx.addModelsForClasses(ImageImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerInjectActivateService(new TitleImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
        ctx.registerService(MimeTypeHelper.class, new MimeTypeHelperImpl());
    }

    @Test
    public void getSrc() {
        final String expected = "/content/dam/test.png.renditions/web/asset.rendition";
        ctx.currentResource("/content/image");
        final Image image = ctx.request().adaptTo(Image.class);

        assertEquals(expected, image.getSrc());
    }

    @Test
    public void getSrc_WithFallback() {
        final String expected = "/content/dam/fallback.png";
        ctx.currentResource("/content/no-rendition-name");
        final Image image = ctx.request().adaptTo(Image.class);

        assertEquals(expected, image.getSrc());
    }


    @Test
    public void getAlt() {
        final String expected = "Test Asset";
        ctx.currentResource("/content/image");
        final Image image = ctx.request().adaptTo(Image.class);

        assertEquals(expected, image.getAlt());
    }

    @Test
    public void getFallback() {
        final String expected = "/content/dam/fallback.png";
        ctx.currentResource("/content/image");
        final Image image = ctx.request().adaptTo(Image.class);

        assertEquals(expected, image.getFallback());
    }

    @Test
    public void isEmpty() {
        ctx.currentResource("/content/empty");
        final Image image = ctx.request().adaptTo(Image.class);
        assertTrue(image.isEmpty());
    }

    @Test
    public void isEmpty_NoFallback() {
        ctx.currentResource("/content/no-fallback");
        final Image image = ctx.request().adaptTo(Image.class);
        assertFalse(image.isEmpty());
    }

    @Test
    public void isEmpty_NoRenditionName() {
        ctx.currentResource("/content/no-rendition-name");
        final Image image = ctx.request().adaptTo(Image.class);
        assertFalse(image.isEmpty());
    }

    @Test
    public void isEmpty_NotEmpty() {
        ctx.currentResource("/content/image");
        final Image image = ctx.request().adaptTo(Image.class);
        assertFalse(image.isEmpty());
    }

    @Test
    public void isReady() {
        ctx.currentResource("/content/image");
        final Image image = ctx.request().adaptTo(Image.class);
        assertTrue(image.isReady());
    }

    @Test
    public void isReady_NoFallback() {
        ctx.currentResource("/content/no-fallback");
        final Image image = ctx.request().adaptTo(Image.class);
        assertTrue(image.isReady());
    }

    @Test
    public void isReady_NoRenditionName() {
        ctx.currentResource("/content/no-rendition-name");
        final Image image = ctx.request().adaptTo(Image.class);
        assertTrue(image.isReady());
    }

    @Test
    public void isReady_NotReady() {
        ctx.currentResource("/content/empty");
        final Image image = ctx.request().adaptTo(Image.class);
        assertFalse(image.isReady());
    }

    @Test
    public void isLegacyMode_NoLegacyConfigOrLegacyMode() {
        ctx.currentResource("/content/image");
        final Image image = ctx.request().adaptTo(Image.class);
        assertFalse(((ImageImpl) image).isLegacyMode());
    }

    @Test
    public void isLegacyMode_WithComputedPropertyAndNoLegacyMode() {
        ctx.currentResource("/content/legacy-computed-property");
        final Image image = ctx.request().adaptTo(Image.class);
        assertTrue(((ImageImpl) image).isLegacyMode());
    }

    @Test
    public void isLegacyMode_WithRenditionRegexAndNoLegacyMode() {
        ctx.currentResource("/content/legacy-rendition-regex");
        final Image image = ctx.request().adaptTo(Image.class);
        assertTrue(((ImageImpl) image).isLegacyMode());
    }

    @Test
    public void isLegacyMode_WithAllConfigsAndNoLegacyMode() {
        ctx.currentResource("/content/all-configs");
        final Image image = ctx.request().adaptTo(Image.class);
        assertFalse(((ImageImpl) image).isLegacyMode());
    }

    @Test
    public void isLegacyMode_On() {
        ctx.currentResource("/content/legacy-on");
        final Image image = ctx.request().adaptTo(Image.class);
        assertTrue(((ImageImpl) image).isLegacyMode());
    }

    @Test
    public void isLegacyMode_Off() {
        ctx.currentResource("/content/legacy-off");
        final Image image = ctx.request().adaptTo(Image.class);
        assertFalse(((ImageImpl) image).isLegacyMode());
    }
}