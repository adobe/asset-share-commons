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

package com.adobe.aem.commons.assetshare.content.renditions.impl.resolvers;

import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionResolver;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionsHelper;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsHelperImpl;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class StaticRenditionResolverImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/renditions/impl/resolvers/StaticRenditionResolverImplTest.json", "/content/dam");

        // 1x1 pixel red png
        ctx.load().binaryFile("/com/adobe/aem/commons/assetshare/content/renditions/impl/resolvers/StaticRenditionResolverImplTest__original.png",
                "/content/dam/test.png/jcr:content/renditions/original");

        // 1x1 pixel blue png
        ctx.load().binaryFile("/com/adobe/aem/commons/assetshare/content/renditions/impl/resolvers/StaticRenditionResolverImplTest__cq5dam.web.1280.1280.png",
                "/content/dam/test.png/jcr:content/renditions/cq5dam.web.1280.1280.png");

        ctx.currentResource("/content/dam/test.png");

        ctx.registerService(AssetRenditionsHelper.class, new AssetRenditionsHelperImpl());
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);
    }

    @Test
    public void getLabel() {
        final String expected = "Test Asset Rendition Resolver";

        ctx.registerInjectActivateService(new StaticRenditionResolverImpl(),
                "label", "Test Asset Rendition Resolver");

        final AssetRenditionResolver assetRenditionResolver = ctx.getService(AssetRenditionResolver.class);
        final String actual = assetRenditionResolver.getLabel();

        assertEquals(expected, actual);
    }

    @Test
    public void getName() {
        final String expected = "test";

        ctx.registerInjectActivateService(new StaticRenditionResolverImpl(),
                "name", "test");

        final AssetRenditionResolver assetRenditionResolver = ctx.getService(AssetRenditionResolver.class);
        final String actual = assetRenditionResolver.getName();

        assertEquals(expected, actual);
    }

    @Test
    public void getOptions() {
        final Map<String, String> expected =  ImmutableMap.<String, String>builder().
                put("Foo", "foo").
                put("Foo bar", "foo_bar").
                put("Foo-bar", "foo-bar").
                build();

        ctx.registerInjectActivateService(new StaticRenditionResolverImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "foo=foo value",
                                "foo_bar=foo_bar value",
                                "foo-bar=foo-bar value"}).
                        build());
        final AssetRenditionResolver assetRenditionResolver = ctx.getService(AssetRenditionResolver.class);
        final Map<String, String> actual = assetRenditionResolver.getOptions();

        assertEquals(expected, actual);
    }

    @Test
    public void accepts() {
        ctx.registerInjectActivateService(new StaticRenditionResolverImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "foo=foo value",
                                "test-rendition=test-rendition value"}).
                        build());
        final AssetRenditionResolver assetRenditionResolver = ctx.getService(AssetRenditionResolver.class);
        final boolean actual = assetRenditionResolver.accepts(ctx.request(), "test-rendition");

        assertTrue(actual);
    }

    @Test
    public void accepts_Reject() {
        ctx.registerInjectActivateService(new StaticRenditionResolverImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "foo=foo value",
                                "test-rendition=test-rendition value"}).
                        build());
        final AssetRenditionResolver assetRenditionResolver = ctx.getService(AssetRenditionResolver.class);
        final boolean actual = assetRenditionResolver.accepts(ctx.request(), "unknown-rendition");

        assertFalse(actual);
    }

    @Test
    public void dispatch() throws IOException, ServletException {
        final byte[] expectedOutputStream = IOUtils.toByteArray(this.getClass().getResourceAsStream("/com/adobe/aem/commons/assetshare/content/renditions/impl/resolvers/StaticRenditionResolverImplTest__cq5dam.web.1280.1280.png"));

        ctx.registerInjectActivateService(new StaticRenditionResolverImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "original=original",
                                "testing=^cq5dam\\.web\\..*"}).
                        build());

        final AssetRenditionResolver assetRenditionResolver = ctx.getService(AssetRenditionResolver.class);

        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        assetRenditionResolver.dispatch(ctx.request(), ctx.response());

        assertEquals("image/png", ctx.response().getHeader("Content-Type"));
        assertEquals("70", ctx.response().getHeader("Content-Length"));

        assertArrayEquals(expectedOutputStream, ctx.response().getOutput());
    }
}