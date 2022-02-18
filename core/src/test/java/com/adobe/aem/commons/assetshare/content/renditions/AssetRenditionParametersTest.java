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

package com.adobe.aem.commons.assetshare.content.renditions;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.day.cq.dam.commons.util.DamUtil;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionParametersTest {

    @Rule
    public final AemContext ctx = new AemContext();

    private AssetModel testAssetModel;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetRenditionParametersTest.json"), "/content/dam");
        ctx.currentResource("/content/dam/test.png");

        final AssetResolver assetResolver = mock(AssetResolver.class);
        doReturn(DamUtil.resolveToAsset(ctx.resourceResolver().getResource("/content/dam/test.png"))).when(assetResolver).resolveAsset(ctx.request());
        ctx.registerService(AssetResolver.class, assetResolver);

        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        testAssetModel = ctx.request().adaptTo(AssetModel.class);
    }

    @Test
    public void getters_CstorRenditionName() {
        final AssetRenditionParameters actual = new AssetRenditionParameters(testAssetModel, "t.estin.g");

        assertEquals("t.estin.g", actual.getRenditionName());
        assertEquals("test.t.estin.g.png", actual.getFileName());
        assertFalse(actual.isDownload());
        assertTrue(actual.getParameters().isEmpty());
    }

    @Test
    public void getters_CstorRenditionNameAndDownload() {
        final AssetRenditionParameters actual = new AssetRenditionParameters(testAssetModel, "testing", true);

        assertEquals("testing", actual.getRenditionName());
        assertEquals("test.testing.png", actual.getFileName());
        assertTrue(actual.isDownload());
        assertEquals(1, actual.getParameters().size());
        assertEquals("download", actual.getParameters().get(0));
    }

    @Test
    public void getters_CstorRenditionNameAndDownloadAndOtherParameters() {
        final AssetRenditionParameters actual = new AssetRenditionParameters(testAssetModel, "testing", true, "param1", "param2");

        assertEquals("testing", actual.getRenditionName());
        assertEquals("test.testing.png", actual.getFileName());
        assertTrue(actual.isDownload());
        assertEquals(3, actual.getParameters().size());
        assertEquals("param1", actual.getParameters().get(0));
        assertEquals("param2", actual.getParameters().get(1));
        assertEquals("download", actual.getParameters().get(2));
    }

    @Test
    public void getters_FromRequest() {
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing/download/param1/param2/asset.rendition");

        final AssetRenditionParameters actual = new AssetRenditionParameters(ctx.request());

        assertEquals("testing", actual.getRenditionName());
        assertEquals("test.testing.png", actual.getFileName());
        assertTrue(actual.isDownload());
        assertEquals(3, actual.getParameters().size());
        assertEquals("download", actual.getParameters().get(0));
        assertEquals("param1", actual.getParameters().get(1));
        assertEquals("param2", actual.getParameters().get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getters_FromRequestWithInvalidSuffixSegments() throws IllegalArgumentException {
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing");

        final AssetRenditionParameters actual = new AssetRenditionParameters(ctx.request());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getters_FromRequestWithInvalidLastSuffix() throws IllegalArgumentException {
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("testing/foo");

        final AssetRenditionParameters actual = new AssetRenditionParameters(ctx.request());
    }
}