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

package com.adobe.aem.commons.assetshare.content.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AssetModelImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/impl/AssetModelImplTest.json", "/content/dam");

        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);
    }

    @Test
    public void getUrl_WithSimplePath() {
        String expected = "/content/dam/test.png";
        ctx.currentResource(ctx.resourceResolver().getResource(expected));

        AssetModel actual = ctx.request().adaptTo(AssetModel.class);

        assertNotNull(actual);
        assertEquals(expected, actual.getUrl());
    }

    @Test
    public void getUrl_WithChineseChars() {
        final String expected = "/content/dam/%e4%bc%8a%e6%8b%89%e5%a5%87%e8%b9%9f%e7%8b%97.pdf";

        ctx.currentResource(ctx.resourceResolver().getResource("/content/dam/伊拉奇蹟狗.pdf"));

        final AssetModel actual = ctx.request().adaptTo(AssetModel.class);

        assertEquals(expected, actual.getUrl());
    }

    @Test
    public void getUrl_WithComplexPath() {
        final String expected = "/content/dam/ir%c4%81%2bpu%20p%c3%b6%20%26p%c3%aep%e2%98%83.jpeg";

        ctx.currentResource(ctx.resourceResolver().getResource("/content/dam/irā+pu pö &pîp☃.jpeg"));

        final AssetModel actual = ctx.request().adaptTo(AssetModel.class);

        assertEquals(expected, actual.getUrl());
    }

    @Test
    public void getUrl_WithSimplePathResource() {
        String expected = "/content/dam/test.png";
        AssetModel actual = ctx.resourceResolver().getResource(expected).adaptTo(AssetModel.class);

        assertNotNull(actual);
        assertEquals(expected, actual.getUrl());
    }
}