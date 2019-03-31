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
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.InternalRedirectRenditionDispatcherImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.StaticRenditionDispatcherImpl;
import com.day.cq.dam.commons.util.DamUtil;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionsImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/renditions/impl/AssetRenditionsImplTest.json", "/content/dam");
        ctx.currentResource("/content/dam/test.png");

        final AssetResolver assetResolver = mock(AssetResolver.class);
        doReturn(DamUtil.resolveToAsset(ctx.resourceResolver().getResource("/content/dam/test.png"))).when(assetResolver).resolveAsset(ctx.request());
        ctx.registerService(AssetResolver.class, assetResolver);

        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
    }

    @Test
    public void getAssetRenditionResolvers() {
        AssetRenditionDispatcher one = new StaticRenditionDispatcherImpl();
        AssetRenditionDispatcher two = new InternalRedirectRenditionDispatcherImpl();
        AssetRenditionDispatcher three = new InternalRedirectRenditionDispatcherImpl();

        ctx.registerInjectActivateService(two, Constants.SERVICE_RANKING, 90);
        ctx.registerInjectActivateService(one, Constants.SERVICE_RANKING, 100);
        ctx.registerInjectActivateService(three, Constants.SERVICE_RANKING, 80);

        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);
        final List<AssetRenditionDispatcher> actual = assetRenditions.getAssetRenditionDispatchers();

        assertEquals(3, actual.size());
        assertSame(one, actual.get(0));
        assertSame(two, actual.get(1));
        assertSame(three, actual.get(2));
    }

    @Test
    public void getRenditionName() {
        final String expected = "test-rendition";
        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);

        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");
        ctx.requestPathInfo().setSuffix("/test-rendition/asset.rendition");

        String actual = assetRenditions.getRenditionName(ctx.request());
        assertEquals(expected, actual);
    }

    @Test
    public void getUrl() {
        final String expected = "/content/dam/test.png.renditions/test-rendition/asset.rendition";
        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);

        final AssetRenditions.UrlParams urlParams = new AssetRenditions.UrlParams("test-rendition", false);
        final AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        String actual = assetRenditions.getUrl(ctx.request(), assetModel, urlParams);

        assertEquals(expected, actual);
    }

    @Test
    public void getUrl_AsDownload() {
        final String expected = "/content/dam/test.png.renditions/test-rendition/download/asset.rendition";
        final AssetRenditions assetRenditions = ctx.getService(AssetRenditions.class);

        final AssetRenditions.UrlParams urlParams = new AssetRenditions.UrlParams("test-rendition", true);
        final AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        String actual = assetRenditions.getUrl(ctx.request(), assetModel, urlParams);

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
}