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

import com.adobe.aem.commons.assetshare.components.details.Video;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class VideoImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/VideoImplTest.json", "/content");

        ctx.addModelsForClasses(VideoImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.mp4");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
    }

    @Test
    public void getSrc() {
        final String expected = "/content/dam/test.mp4.renditions/mp4/asset.rendition";
        ctx.currentResource("/content/video");
        final Video video = ctx.request().adaptTo(Video.class);
        assertEquals(expected, video.getSrc());
    }

    @Test
    public void isEmpty() {
        ctx.currentResource("/content/empty");
        final Video video = ctx.request().adaptTo(Video.class);
        assertTrue(video.isEmpty());
    }

    @Test
    public void isEmpty_NotEmpty() {
        ctx.currentResource("/content/video");
        final Video video = ctx.request().adaptTo(Video.class);
        assertFalse(video.isEmpty());
    }

    @Test
    public void isReady() {
        ctx.currentResource("/content/video");
        final Video video = ctx.request().adaptTo(Video.class);
        assertTrue(video.isReady());
    }

    @Test
    public void isReady_NotReady() {
        ctx.currentResource("/content/empty");
        final Video video = ctx.request().adaptTo(Video.class);
        assertFalse(video.isReady());
    }

    @Test
    public void isVideoAsset() {
        ctx.currentResource("/content/video");
        final Video video = ctx.request().adaptTo(Video.class);
        assertTrue(video.isVideoAsset());
    }

    @Test
    public void isVideoAsset_VideoAsset() {
        ctx.currentResource("/content/video");
        ctx.requestPathInfo().setSuffix("/content/dam/test.png");
        final Video video = ctx.request().adaptTo(Video.class);
        assertFalse(video.isVideoAsset());
    }

    @Test
    public void isLegacyMode_NoLegacyConfigOrLegacyMode() {
        ctx.currentResource("/content/video");
        final Video video = ctx.request().adaptTo(Video.class);
        assertFalse(((VideoImpl) video).isLegacyMode());
    }

    @Test
    public void isLegacyMode_WithComputedPropertyAndNoLegacyMode() {
        ctx.currentResource("/content/legacy-computed-property");
        final Video video = ctx.request().adaptTo(Video.class);
        assertTrue(((VideoImpl) video).isLegacyMode());
    }

    @Test
    public void isLegacyMode_WithRenditionRegexAndNoLegacyMode() {
        ctx.currentResource("/content/legacy-rendition-regex");
        final Video video = ctx.request().adaptTo(Video.class);
        assertTrue(((VideoImpl) video).isLegacyMode());
    }

    @Test
    public void isLegacyMode_WithAllConfigsAndNoLegacyMode() {
        ctx.currentResource("/content/all-configs");
        final Video video = ctx.request().adaptTo(Video.class);
        assertFalse(((VideoImpl) video).isLegacyMode());
    }

    @Test
    public void isLegacyMode_On() {
        ctx.currentResource("/content/legacy-on");
        final Video video = ctx.request().adaptTo(Video.class);
        assertTrue(((VideoImpl) video).isLegacyMode());
    }

    @Test
    public void isLegacyMode_Off() {
        ctx.currentResource("/content/legacy-off");
        final Video video = ctx.request().adaptTo(Video.class);
        assertFalse(((VideoImpl) video).isLegacyMode());
    }
}