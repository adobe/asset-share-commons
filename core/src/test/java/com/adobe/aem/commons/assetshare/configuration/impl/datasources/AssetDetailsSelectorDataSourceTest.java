/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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

package com.adobe.aem.commons.assetshare.configuration.impl.datasources;

import com.adobe.aem.commons.assetshare.configuration.AssetDetailsSelector;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AssetDetailsSelectorDataSourceTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());
        ctx.currentResource(ctx.create().resource("/apps/dialog/default"));
    }

    private static AssetDetailsSelector selector(final String label, final String id) {
        return new AssetDetailsSelector() {
            @Override
            public String getLabel() {
                return label;
            }

            @Override
            public String getId() {
                return id;
            }

            @Override
            public boolean accepts(Config config, AssetModel asset) {
                return false;
            }

            @Override
            public String getUrl(Config config, AssetModel asset) {
                return null;
            }
        };
    }

    @Test
    public void doGet_noSelectors_returnsEmptyDataSource() throws ServletException, IOException {
        final Servlet servlet = ctx.registerInjectActivateService(new AssetDetailsSelectorDataSource());

        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap();

        assertEquals(0, actual.size());
    }

    @Test
    public void doGet_withSelectors_returnsLabelToIdMap() throws ServletException, IOException {
        ctx.registerService(AssetDetailsSelector.class, selector("Always Use Default", "always-use-default"));
        ctx.registerService(AssetDetailsSelector.class, selector("Asset Type", "asset-type"));

        final Servlet servlet = ctx.registerInjectActivateService(new AssetDetailsSelectorDataSource());

        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap();

        assertEquals("always-use-default", actual.get("Always Use Default"));
        assertEquals("asset-type", actual.get("Asset Type"));
        assertEquals(2, actual.size());
    }

    private Map<String, String> toMap() {
        final DataSource sds = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> results = new LinkedHashMap<>();
        final Iterator<Resource> resourcesIterator = sds.iterator();
        while (resourcesIterator.hasNext()) {
            final Resource resource = resourcesIterator.next();
            final ValueMap properties = resource.getValueMap();
            results.put(properties.get(DataSourceBuilder.TEXT, String.class),
                    properties.get(DataSourceBuilder.VALUE, String.class));
        }
        return results;
    }
}
