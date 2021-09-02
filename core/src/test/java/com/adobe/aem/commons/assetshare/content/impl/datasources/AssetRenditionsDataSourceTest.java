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

package com.adobe.aem.commons.assetshare.content.impl.datasources;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionDispatchersImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.StaticRenditionDispatcherImpl;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import com.adobe.aem.commons.assetshare.util.impl.RequireAemImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.mime.MimeTypeService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionsDataSourceTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    private MimeTypeService mimeTypeService;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/impl/AssetRenditionsDataSourceTest.json", "/apps/dialog");

        ctx.registerService(RequireAem.class, new RequireAemImpl());
        ctx.registerService(MimeTypeService.class, mimeTypeService);
        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());
        ctx.registerInjectActivateService(new AssetRenditionsImpl());
        ctx.registerService(AssetRenditionDispatchers.class, new AssetRenditionDispatchersImpl());
        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());

        ctx.registerInjectActivateService(
                new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put(Constants.SERVICE_RANKING, 0).
                        put("label", "One AssetRenditionDispatcher").
                        put("name", "one").
                        put ("types", new String[]{"image", "video"}).
                        put("rendition.mappings", new String[]{
                                "a=value",
                                "b=value",}).
                        build());

        ctx.registerInjectActivateService(
                new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put(Constants.SERVICE_RANKING, 0).
                        put("label", "Two AssetRenditionDispatcher").
                        put("name", "two").
                        put ("types", new String[]{"video"}).
                        put("rendition.mappings", new String[]{
                                "c=value",
                                "d=value"}).
                        build());
    }

    @Test
    public void doGet_NoExcludes() throws ServletException, IOException {
        String[] expected = new String[]{"a", "b", "c", "d"};

        ctx.currentResource("/apps/dialog/default");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods", "GET");

        final Servlet servlet = ctx.getService(Servlet.class);

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expected, actual.values().toArray());
    }

    @Test
    public void doGet_ExcludeAssetRenditionDispatchersViaOsgiConfig() throws ServletException, IOException {
        String[] expected = new String[]{"c", "d"};

        ctx.currentResource("/apps/dialog/default");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods", "GET",
                "exclude.assetrenditiondispatcher.names", "one");

        final Servlet servlet = ctx.getService(Servlet.class);

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expected, actual.values().toArray());
    }

    @Test
    public void doGet_ExcludeAssetRenditionsViaOsgiConfig() throws ServletException, IOException {
        String[] expected = new String[]{"a", "c", "d"};

        ctx.currentResource("/apps/dialog/default");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods", "GET",
                "exclude.assetrendition.names", "b");

        final Servlet servlet = ctx.getService(Servlet.class);

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expected, actual.values().toArray());
    }

    @Test
    public void doGet_ExcludeAssetRenditionDispatchersViaProperty() throws ServletException, IOException {
        String[] expected = new String[]{"c", "d"};

        ctx.currentResource("/apps/dialog/exclude-assetrenditiondispatchers");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods", "GET");

        final Servlet servlet = ctx.getService(Servlet.class);

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expected, actual.values().toArray());
    }

    @Test
    public void doGet_ExcludeAssetRenditionsViaProperty() throws ServletException, IOException {
        String[] expected = new String[]{"a", "c", "d"};

        ctx.currentResource("/apps/dialog/exclude-assetrenditions");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods", "GET");

        final Servlet servlet = ctx.getService(Servlet.class);

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expected, actual.values().toArray());
    }


    @Test
    public void doGet_ServiceRanking() throws ServletException, IOException {
        final String[] expectedKeys = new String[]{
                "A (Three AssetRenditionDispatcher)",
                "B (One AssetRenditionDispatcher)",
                "C (Three AssetRenditionDispatcher)",
                "D (Two AssetRenditionDispatcher)"
        };

        final String[] expectedValues = new String[]{"a", "b", "c", "d"};

        ctx.currentResource("/apps/dialog/default");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "add.assetrenditiondispatcher.to.label", true,
                "sling.servlet.methods", "GET");


        ctx.registerInjectActivateService(
                new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put(Constants.SERVICE_RANKING, 1000).
                        put("label", "Three AssetRenditionDispatcher").
                        put("name", "three").
                        put("rendition.mappings", new String[]{
                                "a=preferred value for a",
                                "c=preferred value for c"}).
                        build());


        final Servlet servlet = ctx.getService(Servlet.class);

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expectedKeys, actual.keySet().toArray());
        assertArrayEquals(expectedValues, actual.values().toArray());
    }

    @Test
    public void doGet_WithHiddenAssetRenditionDispatcher() throws ServletException, IOException {
        final String[] expectedValues = new String[]{"a", "b", "c", "d"};

        ctx.currentResource("/apps/dialog/default");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods", "GET");

        ctx.registerInjectActivateService(
                new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put(Constants.SERVICE_RANKING, 1000).
                        put("label", "Three AssetRenditionDispatcher").
                        put("name", "three").
                        put ("hidden", true).
                        put("rendition.mappings", new String[]{
                                "e=value",
                                "f=value"}).
                        build());


        final Servlet servlet = ctx.getService(Servlet.class);

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expectedValues, actual.values().toArray());
    }

    @Test
    public void doGet_WithAllowedAssetRenditionDispatcherTypes() throws ServletException, IOException {
        final String[] expectedValues = new String[]{"a", "b", "e", "f"};

        ctx.currentResource("/apps/dialog/allowed-assetrendition-types");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods", "GET");

        ctx.registerInjectActivateService(
                new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put(Constants.SERVICE_RANKING, 1000).
                        put("label", "Three AssetRenditionDispatcher").
                        put("name", "three").
                        put ("types", new String[]{"image"}).
                        put("rendition.mappings", new String[]{
                                "e=value",
                                "f=value"}).
                        build());


        final Servlet servlet = ctx.getService(Servlet.class);

        servlet.service(ctx.request(), ctx.response());

        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(sds);

        assertArrayEquals(expectedValues, actual.values().toArray());
    }

    private Map<String, String> toMap(DataSource dataSource) {
        final Map<String, String> results = new LinkedHashMap<>();
        final Iterator<Resource> resourcesIterator = dataSource.iterator();
        while (resourcesIterator.hasNext()) {
            final Resource resource = resourcesIterator.next();
            final ValueMap properties = resource.getValueMap();

            results.put(properties.get(DataSourceBuilder.TEXT, String.class),
                    properties.get(DataSourceBuilder.VALUE, String.class));
        }
        return results;
    }
}