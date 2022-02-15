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

package com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers;

import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestDispatcherFactory;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class StaticRenditionDispatcherImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    private RequestDispatcher requestDispatcher;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("StaticRenditionDispatcherImplTest.json"), "/content/dam");

        // 1x1 pixel red png
        ctx.load().binaryFile(getClass().getResourceAsStream("StaticRenditionDispatcherImplTest__original.png"),
                "/content/dam/test.png/jcr:content/renditions/original");

        // 1x1 pixel blue png
        ctx.load().binaryFile(getClass().getResourceAsStream("StaticRenditionDispatcherImplTest__cq5dam.web.1280.1280.png"),
                "/content/dam/test.png/jcr:content/renditions/cq5dam.web.1280.1280.png");

        ctx.currentResource("/content/dam/test.png");

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        ctx.request().setRequestDispatcherFactory(new MockRequestDispatcherFactory() {
            @Override
            public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
                return requestDispatcher;
            }

            @Override
            public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
                return requestDispatcher;
            }
        });
    }

    @Test
    public void getLabel() {
        final String expected = "Test Asset Rendition Resolver";

        ctx.registerInjectActivateService(new StaticRenditionDispatcherImpl(),
                "label", "Test Asset Rendition Resolver");

        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);
        final String actual = assetRenditionDispatcher.getLabel();

        assertEquals(expected, actual);
    }

    @Test
    public void getName() {
        final String expected = "test";

        ctx.registerInjectActivateService(new StaticRenditionDispatcherImpl(),
                "name", "test");

        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);
        final String actual = assetRenditionDispatcher.getName();

        assertEquals(expected, actual);
    }

    @Test
    public void getOptions() {
        final Map<String, String> expected =  ImmutableMap.<String, String>builder().
                put("Foo", "foo").
                put("Foo bar", "foo_bar").
                put("Foo-bar", "foo-bar").
                build();

        ctx.registerInjectActivateService(new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "foo=foo value",
                                "foo_bar=foo_bar value",
                                "foo-bar=foo-bar value"}).
                        build());
        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);
        final Map<String, String> actual = assetRenditionDispatcher.getOptions();

        assertEquals(expected, actual);
    }

    @Test
    public void getRenditionNames() {
        Set<String> expected = new LinkedHashSet<>();
        expected.add("foo");
        expected.add("test.ing");

        ctx.registerInjectActivateService(new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "foo=foo value",
                                "test.ing-rendition=test-rendition value"}).
                        build());
        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);
        final Set<String> actual = assetRenditionDispatcher.getRenditionNames();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.contains("foo"));
        assertTrue(expected.contains("test.ing"));    }


    @Test
    public void dispatch() throws IOException, ServletException {
        final byte[] expectedOutputStream = IOUtils.toByteArray(this.getClass().getResourceAsStream("StaticRenditionDispatcherImplTest__cq5dam.web.1280.1280.png"));

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            // Write some data to the response so we know that that requestDispatcher.include(..) was infact invoked.
            ((MockSlingHttpServletResponse) args[1]).getOutputStream().write(expectedOutputStream);
            return null; // void method, return null
        }).when(requestDispatcher).include(any(SlingHttpServletRequest.class), any(SlingHttpServletResponse.class));

        ctx.registerInjectActivateService(new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "original=original",
                                "testing=^cq5dam\\.web\\..*"}).
                        build());

        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);

        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        assetRenditionDispatcher.dispatch(ctx.request(), ctx.response());

        assertArrayEquals(expectedOutputStream, ctx.response().getOutput());
    }
}