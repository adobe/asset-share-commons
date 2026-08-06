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

package com.adobe.aem.commons.assetshare.search.results.impl.result;

import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.search.results.AssetResult;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AssetResultImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/impl/AssetModelImplTest.json", "/content/dam");

        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetResultImpl.class);
    }

    @Test
    public void getType_ReturnsAsset() {
        ctx.currentResource(ctx.resourceResolver().getResource("/content/dam/test.png"));

        final AssetResult actual = ctx.request().adaptTo(AssetResult.class);

        assertNotNull(actual);
        assertEquals(AssetResult.TYPE, actual.getType());
        assertEquals("asset", actual.getType());
    }

    @Test
    public void getPath_DelegatesToAssetModel() {
        final String expected = "/content/dam/test.png";
        ctx.currentResource(ctx.resourceResolver().getResource(expected));

        final AssetResult actual = ctx.request().adaptTo(AssetResult.class);

        assertNotNull(actual);
        assertEquals(expected, actual.getPath());
    }
}
