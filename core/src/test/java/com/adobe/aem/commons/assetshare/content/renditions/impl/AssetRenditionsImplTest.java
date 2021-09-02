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

package com.adobe.aem.commons.assetshare.content.renditions.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import com.day.cq.dam.commons.util.DamUtil;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.commons.mime.MimeTypeService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionsImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    private MimeTypeService mimeTypeService;

    private AssetModel testAssetModel;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetRenditionsImplTest.json"), "/content/dam");
        ctx.currentResource("/content/dam/test.png");

        final AssetResolver assetResolver = mock(AssetResolver.class);
        doReturn(DamUtil.resolveToAsset(ctx.resourceResolver().getResource("/content/dam/test.png"))).when(assetResolver).resolveAsset(ctx.request());
        ctx.registerService(AssetResolver.class, assetResolver);

        ctx.registerService(mimeTypeService);
        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());

        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        ctx.registerInjectActivateService(new AssetRenditionsImpl());

        testAssetModel = ctx.request().adaptTo(AssetModel.class);
    }

    @Test
    public void getUrl() {
        final String expected = "/content/dam/test.png.renditions/test-rendition/asset.rendition";
        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);

        final AssetRenditionParameters params = new AssetRenditionParameters(testAssetModel, "test-rendition", false);
        final AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        String actual = assetRenditions.getUrl(ctx.request(), assetModel, params);

        assertEquals(expected, actual);
    }

    @Test
    public void getUrl_AsDownload() {
        final String expected = "/content/dam/test.png.renditions/test-rendition/download/asset.rendition";
        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);

        final AssetRenditionParameters params = new AssetRenditionParameters(testAssetModel, "test-rendition", true);
        final AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        String actual = assetRenditions.getUrl(ctx.request(), assetModel, params);

        assertEquals(expected, actual);
    }

    @Test
    public void getOptions() {
        final Map<String, String> params = new HashMap<>();
        params.put("foo", "foo value");
        params.put("foo_bar", "foo_bar value");
        params.put("foo-bar", "foo-bar value");

        final Map<String, String> expected = new HashMap<>();
        expected.put("Foo", "foo");
        expected.put("Foo bar", "foo_bar");
        expected.put("Foo-bar", "foo-bar");

        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);

        final Map<String, String> actual = assetRenditions.getOptions(params);
        assertEquals(expected, actual);
    }

    @Test
    public void evaluateExpression() {
        final String expression = "${asset.path}.test-selector.${asset.extension}?filename=${asset.name}&rendition=${rendition.name}";
        final String expected = "/content/dam/test.png.test-selector.png?filename=test.png&rendition=test-rendition";

        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);

        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("/test-rendition/asset.rendition");

        String actual = assetRenditions.evaluateExpression(ctx.request(), expression);
        assertEquals(expected, actual);

    }

    @Test
    public void evaluateExpression_ForDynamicMediaVariables() {
        final String expression = "${dm.domain}is/image/${dm.file}?folder=${dm.folder}&name=${dm.name}&id=${dm.id}&api=${dm.api-server}";
        final String expected = "http://test.scene7.com/is/image/testing/test_1?folder=testing&name=test_1&id=x|1234&api=https://test.api.scene7.com";

        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);

        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("/test-rendition/asset.rendition");

        String actual = assetRenditions.evaluateExpression(ctx.request(), expression);
        assertEquals(expected, actual);
    }
}