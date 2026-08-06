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
import com.adobe.aem.commons.assetshare.content.properties.impl.AssetTypeImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetTypeSelectorImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    AssetDetailsSelector assetDetailsSelector;

    @Mock
    Config config;

    @Mock
    AssetModel assetModel;

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new AssetTypeSelectorImpl());

        assetDetailsSelector = ctx.getService(AssetDetailsSelector.class);
    }

    @Test
    public void getLabel() {
        assertEquals(AssetTypeSelectorImpl.LABEL, assetDetailsSelector.getLabel());
    }

    @Test
    public void getId() {
        assertEquals(AssetTypeSelectorImpl.ID, assetDetailsSelector.getId());
    }

    @Test
    public void accepts_True() {
        when(config.getAssetDetailsSelector()).thenReturn("asset-type");
        assertTrue(assetDetailsSelector.accepts(config, assetModel));
    }

    @Test
    public void accepts_CaseInsensitive() {
        when(config.getAssetDetailsSelector()).thenReturn("ASSET-TYPE");
        assertTrue(assetDetailsSelector.accepts(config, assetModel));
    }

    @Test
    public void accepts_False() {
        when(config.getAssetDetailsSelector()).thenReturn("not-asset-type");
        assertFalse(assetDetailsSelector.accepts(config, assetModel));
    }

    @Test
    public void getUrl() {
        when(config.getAssetDetailsPath()).thenReturn("/content/asset-details");

        final ValueMap properties = new ValueMapDecorator(new HashMap<>());
        properties.put(AssetTypeImpl.NAME, "Image");
        when(assetModel.getProperties()).thenReturn(properties);

        assertEquals("/content/asset-details/image.html", assetDetailsSelector.getUrl(config, assetModel));
    }

    @Test
    public void getUrl_noAssetType_returnsNull() {
        final ValueMap properties = new ValueMapDecorator(new HashMap<>());
        when(assetModel.getProperties()).thenReturn(properties);

        assertEquals(null, assetDetailsSelector.getUrl(config, assetModel));
    }
}
