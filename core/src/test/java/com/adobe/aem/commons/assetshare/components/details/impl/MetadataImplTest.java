/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

import com.adobe.aem.commons.assetshare.components.details.Metadata;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class MetadataImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/MetadataImplTest.json", "/content");

        ctx.addModelsForClasses(MetadataImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);
    }

    @Test
    public void getType() {
        final Metadata.DataType expected = Metadata.DataType.TEXT;

        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getType());
    }

    @Test
    public void getLocale_Default() {
        final String expected = Locale.getDefault().getLanguage();
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getLocale());
    }

    @Test
    public void getFormat() {
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertNull(metadata.getFormat());
    }

    @Test
    public void getFormat_Date() {
        final String expected = "yyyy-MM-dd";

        ctx.currentResource("/content/date");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getFormat());
    }

    @Test
    public void getFormat_Number() {
        final String expected = "#.###";

        ctx.currentResource("/content/number");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getFormat());
    }

    @Test
    public void getProperties() {
    }

    @Test
    public void getAsset() {
        final String expected = "/content/dam/test.png";

        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getAsset().getPath());
    }

    @Test
    public void getPropertyName() {
        final String expected = "./dc:title";

        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getPropertyName());
    }

    @Test
    public void getPropertyName_ComputedProperty() {
        final String expected = "title";

        ctx.currentResource("/content/computed");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);

        assertEquals(expected, metadata.getPropertyName());
    }

    @Test
    public void isEmpty_NullValue() {
        ctx.currentResource("/content/empty");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void isEmpty_EmptyText() {
        ctx.currentResource("/content/empty-text");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void isEmpty_NotEmpty() {
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertFalse(metadata.isEmpty());
    }

    @Test
    public void isReady() {
        ctx.currentResource("/content/metadata");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertTrue(metadata.isReady());
    }

    @Test
    public void isReady_NotReady() {
        ctx.currentResource("/content/empty");
        final Metadata metadata = ctx.request().adaptTo(Metadata.class);
        assertFalse(metadata.isReady());
    }
}