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

import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import com.adobe.aem.commons.assetshare.util.impl.ExtensionOverrideRequestWrapper;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.mime.MimeTypeService;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InternalRedirectRenditionDispatcherImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    private AssetResolver assetResolver;

    @Mock
    private MimeTypeService mimeTypeService;

    @Before
    public void setUp() throws Exception {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.PUBLISH);

        ctx.load().json(getClass().getResourceAsStream("InternalRedirectRenditionDispatcherImplTest.json"), "/content/dam");
        ctx.currentResource("/content/dam/test.png");

        ctx.registerService(MimeTypeService.class, mimeTypeService);
        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());
        ctx.registerInjectActivateService(new AssetRenditionsImpl());

        ctx.registerService(AssetResolver.class, assetResolver);

        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        ctx.request().setRequestDispatcherFactory(new MockRequestDispatcherFactory() {
            @Override
            public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
                return requestDispatcher;
            }

            @Override
            public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
                assertEquals("This method signature should not be called", "This method signature was called.");
                return null;
            }
        });
    }

    @Test
    public void getLabel() {
        final String expected = "Test Asset Rendition Resolver";

        ctx.registerInjectActivateService(new InternalRedirectRenditionDispatcherImpl(),
                "label", "Test Asset Rendition Resolver");

        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);
        final String actual = assetRenditionDispatcher.getLabel();

        assertEquals(expected, actual);
    }

    @Test
    public void getName() {
        final String expected = "test";

        ctx.registerInjectActivateService(new InternalRedirectRenditionDispatcherImpl(),
                "name", "test");

        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);
        final String actual = assetRenditionDispatcher.getName();

        assertEquals(expected, actual);
    }

    @Test
    public void getOptions() {
        final Map<String, String> expected = ImmutableMap.<String, String>builder().
                put("Foo", "foo").
                put("Foo bar", "foo_bar").
                put("Foo-bar", "foo-bar").
                build();

        ctx.registerInjectActivateService(new InternalRedirectRenditionDispatcherImpl(),
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
        Set<String> expected = new HashSet<>();
        expected.add("foo");
        expected.add("test.ing-rendition");

        ctx.registerInjectActivateService(new InternalRedirectRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "foo=foo value",
                                "test.ing-rendition=test-rendition value"}).
                        build());
        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);
        final Set<String> actual = assetRenditionDispatcher.getRenditionNames();

        assertEquals(expected.size(), actual.size());
        assertTrue(expected.contains("foo"));
        assertTrue(expected.contains("test.ing-rendition"));
    }

    @Test
    public void dispatch() throws IOException, ServletException {
        ctx.registerInjectActivateService(new InternalRedirectRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "testing=${asset.path}.test.500.500.${asset.extension}"}).
                        build());

        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);

        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            // Write some data to the response so we know that that requestDispatcher.include(..) was infact invoked.
            ((MockSlingHttpServletResponse) args[1]).getOutputStream().print("test");
            return null; // void method, return null
        }).when(requestDispatcher).include(any(ExtensionOverrideRequestWrapper.class), eq(ctx.response()));

        assetRenditionDispatcher.dispatch(ctx.request(), ctx.response());

        assertEquals("test", ctx.response().getOutputAsString());
    }

    @Test
    public void dispatch_WithSpacesInPath() throws IOException, ServletException {
        ctx.registerInjectActivateService(new InternalRedirectRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put("rendition.mappings", new String[]{
                                "testing=${asset.path}.test.500.500.${asset.extension}"}).
                        build());

        final AssetRenditionDispatcher assetRenditionDispatcher = ctx.getService(AssetRenditionDispatcher.class);

        ctx.currentResource("/content/dam/test with spaces.png");
        ctx.requestPathInfo().setExtension("rendition");
        ctx.requestPathInfo().setSuffix("testing/download/asset.rendition");

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            // Write some data to the response so we know that that requestDispatcher.include(..) was infact invoked.
            ((MockSlingHttpServletResponse) args[1]).getOutputStream().print("test");
            return null; // void method, return null
        }).when(requestDispatcher).include(any(ExtensionOverrideRequestWrapper.class), eq(ctx.response()));

        ctx.request().setRequestDispatcherFactory(new MockRequestDispatcherFactory() {
            @Override
            public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
                assertEquals("/content/dam/test with spaces.png.test.500.500.png", path);
                return requestDispatcher;
            }

            @Override
            public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
                assertEquals("This method signature should not be called", "This method signature was called.");
                return null;
            }
        });

        assetRenditionDispatcher.dispatch(ctx.request(), ctx.response());

        assertEquals("test", ctx.response().getOutputAsString());
    }

    @Test
    public void cleanPathInfoRequestPath() {
        final InternalRedirectRenditionDispatcherImpl assetRenditionDispatcher = new InternalRedirectRenditionDispatcherImpl();

        assertEquals("/content/dam/test with spaces.png.test.500.500.png",
                assetRenditionDispatcher.cleanPathInfoRequestPath("/content/dam/test with spaces.png.test.500.500.png"));

        assertEquals("/content/dam/test with spaces.png.test.500.500.png",
                assetRenditionDispatcher.cleanPathInfoRequestPath("content/dam/test with spaces.png.test.500.500.png"));

        assertEquals("/content/dam/test with spaces.png.test.500.500.png",
                assetRenditionDispatcher.cleanPathInfoRequestPath("https://test.com/content/dam/test with spaces.png.test.500.500.png"));

        assertEquals("/content/dam/test_without_spaces.png.test.500.500.png",
                assetRenditionDispatcher.cleanPathInfoRequestPath("https://test.com/content/dam/test_without_spaces.png.test.500.500.png"));
    }
}