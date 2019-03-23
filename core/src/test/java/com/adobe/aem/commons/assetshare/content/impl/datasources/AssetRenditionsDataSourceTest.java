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

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionsHelper;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsHelperImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.resolvers.StaticRenditionResolverImpl;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

public class AssetRenditionsDataSourceTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/impl/AssetRenditionsDataSourceTest.json", "/apps/dialog");

        ctx.registerService(AssetRenditionsHelper.class, new AssetRenditionsHelperImpl());
        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());

        ctx.registerInjectActivateService(
                new StaticRenditionResolverImpl(),
                ImmutableMap.<String, Object>builder().
                        put("name", "one").
                        put("rendition.mappings", new String[]{
                                "a=value",
                                "b=value",}).
                        build());

        ctx.registerInjectActivateService(
                new StaticRenditionResolverImpl(),
                ImmutableMap.<String, Object>builder().
                        put("name", "two").
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
    public void doGet_ExcludeAssetRenditionResolversViaOsgiConfig() throws ServletException, IOException {
        String[] expected = new String[]{"c", "d"};

        ctx.currentResource("/apps/dialog/default");
        ctx.registerInjectActivateService(new AssetRenditionsDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods", "GET",
                "exclude.assetrenditionresolver.names", "one");

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
    public void doGet_ExcludeAssetRenditionResolversViaProperty() throws ServletException, IOException {
        String[] expected = new String[]{"c", "d"};

        ctx.currentResource("/apps/dialog/exclude-assetrenditionresolvers");
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

    private Map<String, String> toMap(DataSource dataSource) {
        final Map<String, String> results = new HashMap<>();
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