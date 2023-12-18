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

package com.adobe.aem.commons.assetshare.configuration.impl.selectors;

import com.adobe.aem.commons.assetshare.configuration.AssetDetailsSelector;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.properties.impl.ContentTypeImpl;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.RequireAemImpl;
import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentTypeSelectorImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    AssetDetailsSelector assetDetailsSelector;


    @Mock
    Config config;

    @Mock
    AssetModel assetModel;

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/configuration/impl/ContentTypeSelectorImplTest.json",
                "/content/dam");

        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.png");
        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.pdf");
        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.doc");
        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.unknown");

        ctx.registerInjectActivateService(new ContentTypeImpl());
        ctx.registerInjectActivateService(new ContentTypeSelectorImpl());

        assetDetailsSelector = ctx.getService(AssetDetailsSelector.class);
    }

    @Test
    public void getLabel() {
        assertEquals("Content Type", assetDetailsSelector.getLabel());
    }

    @Test
    public void getId() {
        assertEquals("content-type", assetDetailsSelector.getId());
    }

    @Test
    public void accepts_True() {
        when(config.getAssetDetailsSelector()).thenReturn("content-type");
        assertTrue(assetDetailsSelector.accepts(config, null));
    }

    @Test
    public void accepts_False() {
        when(config.getAssetDetailsSelector()).thenReturn("not-content-type");
        assertFalse(assetDetailsSelector.accepts(config, null));
    }

    @Test
    public void getUrl_Image() {
        when(config.getAssetDetailsPath()).thenReturn("/content/asset-details");

        AssetModel assetModelPng = ctx.resourceResolver().getResource("/content/dam/test.png").adaptTo(AssetModel.class);

        assertEquals("/content/asset-details/image.html", assetDetailsSelector.getUrl(config, assetModelPng));
    }

    @Test
    public void getUrl_WordDoc() {
        when(config.getAssetDetailsPath()).thenReturn("/content/asset-details");

        AssetModel assetModelPng = ctx.resourceResolver().getResource("/content/dam/test.doc").adaptTo(AssetModel.class);

        assertEquals("/content/asset-details/word-doc.html", assetDetailsSelector.getUrl(config, assetModelPng));
    }

    @Test
    public void getUrl_Unknown() {
        when(config.getAssetDetailsPath()).thenReturn("/content/asset-details");

        AssetModel assetModelPng = ctx.resourceResolver().getResource("/content/dam/test.unknown").adaptTo(AssetModel.class);

        assertEquals("/content/asset-details.html", assetDetailsSelector.getUrl(config, assetModelPng));
    }
}