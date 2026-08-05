/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

import com.adobe.aem.commons.assetshare.components.details.Audio;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import com.day.cq.dam.api.DamConstants;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AudioImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/AudioImplTest.json", "/content");

        ctx.addModelsForClasses(AudioImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.mp3");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        // Real (lightweight) AssetRenditions implementation
        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());
        ctx.registerInjectActivateService(new AssetRenditionsImpl());
    }

    @Test
    public void getSrc() {
        final String expected = "/content/dam/test.mp3.renditions/mp3/asset.rendition";

        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);

        assertEquals(expected, audio.getSrc());
    }

    @Test
    public void getSrc_DefaultRenditionName_UsesOriginal() {
        final String expected = "/content/dam/test.mp3.renditions/" + DamConstants.ORIGINAL_FILE + "/asset.rendition";

        ctx.currentResource("/content/audio-default-rendition");
        final Audio audio = ctx.request().adaptTo(Audio.class);

        assertEquals(expected, audio.getSrc());
    }

    @Test
    public void getSrc_NoAsset_ReturnsEmpty() {
        // No suffix resolves to a valid asset, so @Self AssetModel injection fails and stays null (OPTIONAL).
        ctx.requestPathInfo().setSuffix("/content/dam/does-not-exist.mp3");

        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);

        assertEquals("", audio.getSrc());
    }

    @Test
    public void isAudioAsset_true() {
        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertTrue(audio.isAudioAsset());
    }

    @Test
    public void isAudioAsset_false_NonAudioAsset() {
        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertFalse(audio.isAudioAsset());
    }

    @Test
    public void isAudioAsset_false_NoAsset() {
        ctx.requestPathInfo().setSuffix("/content/dam/does-not-exist.mp3");

        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertFalse(audio.isAudioAsset());
    }

    @Test
    public void hasEmptyText_AlwaysFalse() {
        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertFalse(audio.hasEmptyText());
    }

    @Test
    public void isEmpty_false() {
        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertFalse(audio.isEmpty());
    }

    @Test
    public void isEmpty_true_NoAsset() {
        ctx.requestPathInfo().setSuffix("/content/dam/does-not-exist.mp3");

        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertTrue(audio.isEmpty());
    }

    @Test
    public void isReady_true() {
        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertTrue(audio.isReady());
    }

    @Test
    public void isReady_false_NoAsset() {
        ctx.requestPathInfo().setSuffix("/content/dam/does-not-exist.mp3");

        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertFalse(audio.isReady());
    }

    @Test
    public void getType() {
        ctx.currentResource("/content/audio");
        final Audio audio = ctx.request().adaptTo(Audio.class);
        assertEquals("audio/mpeg", audio.getType());
    }

    @Test
    public void getRenditionName_Explicit() {
        ctx.currentResource("/content/audio");
        final AudioImpl audio = (AudioImpl) ctx.request().adaptTo(Audio.class);
        assertEquals("mp3", audio.getRenditionName());
    }

    @Test
    public void getRenditionName_DefaultsToOriginal() {
        ctx.currentResource("/content/audio-default-rendition");
        final AudioImpl audio = (AudioImpl) ctx.request().adaptTo(Audio.class);
        assertEquals(DamConstants.ORIGINAL_FILE, audio.getRenditionName());
    }
}
