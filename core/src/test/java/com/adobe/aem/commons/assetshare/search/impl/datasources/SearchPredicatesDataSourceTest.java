/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.search.impl.datasources;

import com.adobe.aem.commons.assetshare.search.searchpredicates.SearchPredicate;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchPredicatesDataSourceTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    private SearchPredicate excludeExpired;

    @Mock
    private SearchPredicate excludeSubAssets;

    @Before
    public void setUp() throws Exception {
        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());

        ctx.currentResource(ctx.create().resource("/apps/dialog/default"));
    }

    @Test
    public void doGet_WithMultipleSearchPredicates() throws ServletException, IOException {
        when(excludeExpired.getLabel()).thenReturn("Exclude expired assets");
        when(excludeExpired.getName()).thenReturn("exclude-expired-assets");

        when(excludeSubAssets.getLabel()).thenReturn("Exclude sub-assets");
        when(excludeSubAssets.getName()).thenReturn("exclude-sub-assets");

        ctx.registerService(SearchPredicate.class, excludeExpired);
        ctx.registerService(SearchPredicate.class, excludeSubAssets);

        final Servlet servlet = ctx.registerInjectActivateService(new SearchPredicatesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/search-predicates",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final DataSource dataSource = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(dataSource);

        assertEquals(2, actual.size());
        assertEquals("exclude-expired-assets", actual.get("Exclude expired assets"));
        assertEquals("exclude-sub-assets", actual.get("Exclude sub-assets"));
    }

    @Test
    public void doGet_WithNoSearchPredicates() throws ServletException, IOException {
        final Servlet servlet = ctx.registerInjectActivateService(new SearchPredicatesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/search-predicates",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final DataSource dataSource = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(dataSource);

        assertEquals(0, actual.size());
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
