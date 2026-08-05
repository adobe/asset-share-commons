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

import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchResultsResourceTypeDataSourceTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    QueryBuilder queryBuilder;

    @Mock
    Query query;

    @Mock
    SearchResult searchResult;

    @Before
    public void setUp() throws Exception {
        ctx.registerService(QueryBuilder.class, queryBuilder);
        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());

        when(queryBuilder.createQuery(any(), any())).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
    }

    private Servlet getServlet(final Map<String, Object> resourceProps) {
        ctx.currentResource(ctx.create().resource("/apps/dialog/default", resourceProps));
        return ctx.registerInjectActivateService(new SearchResultsResourceTypeDataSource());
    }

    @Test
    public void doGet_withExtensionTypes() throws Exception {
        when(searchResult.getHits()).thenReturn(Arrays.asList(
                new MockHit("/apps/one", "One"),
                new MockHit("/apps/two", "Two")));

        final Map<String, Object> props = new HashMap<>();
        props.put("extensionTypes", new String[]{"jpg", "png"});

        final Servlet servlet = getServlet(props);
        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap();
        assertEquals("/apps/one", actual.get("One"));
        assertEquals("/apps/two", actual.get("Two"));
        assertEquals(2, actual.size());
    }

    @Test
    public void doGet_withResourceTypes() throws Exception {
        when(searchResult.getHits()).thenReturn(Arrays.asList(new MockHit("/apps/three", "Three")));

        final Map<String, Object> props = new HashMap<>();
        props.put("resourceTypes", new String[]{"asset-share-commons/components/search/results"});

        final Servlet servlet = getServlet(props);
        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap();
        assertEquals("/apps/three", actual.get("Three"));
        assertEquals(1, actual.size());
    }

    @Test
    public void doGet_withResourceSuperTypes() throws Exception {
        when(searchResult.getHits()).thenReturn(Arrays.asList(new MockHit("/apps/four", "Four")));

        final Map<String, Object> props = new HashMap<>();
        props.put("resourceSuperTypes", new String[]{"asset-share-commons/components/search/base"});

        final Servlet servlet = getServlet(props);
        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap();
        assertEquals("/apps/four", actual.get("Four"));
        assertEquals(1, actual.size());
    }

    @Test
    public void doGet_noTypes_noHits() throws Exception {
        when(searchResult.getHits()).thenReturn(Arrays.asList());

        final Servlet servlet = getServlet(new HashMap<>());
        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap();
        assertEquals(0, actual.size());
    }

    @Test
    public void doGet_repositoryExceptionOnHit_isSkipped() throws Exception {
        when(searchResult.getHits()).thenReturn(Arrays.asList(new ThrowingHit()));

        final Servlet servlet = getServlet(new HashMap<>());
        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap();
        assertEquals(0, actual.size());
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

    private static class MockHit implements Hit {
        private final String path;
        private final String title;

        MockHit(String path, String title) {
            this.path = path;
            this.title = title;
        }

        @Override
        public long getIndex() {
            return 0;
        }

        @Override
        public Map<String, String> getExcerpts() {
            return null;
        }

        @Override
        public String getExcerpt() {
            return null;
        }

        @Override
        public Resource getResource() {
            return null;
        }

        @Override
        public Node getNode() {
            return null;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public ValueMap getProperties() {
            return new ValueMapDecorator(new HashMap<>());
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public double getScore() {
            return 0;
        }
    }

    private static class ThrowingHit implements Hit {
        @Override
        public long getIndex() {
            return 0;
        }

        @Override
        public Map<String, String> getExcerpts() {
            return null;
        }

        @Override
        public String getExcerpt() {
            return null;
        }

        @Override
        public Resource getResource() {
            return null;
        }

        @Override
        public Node getNode() {
            return null;
        }

        @Override
        public String getPath() throws RepositoryException {
            return "/apps/throwing";
        }

        @Override
        public ValueMap getProperties() throws RepositoryException {
            throw new RepositoryException("boom");
        }

        @Override
        public String getTitle() {
            return "Throwing";
        }

        @Override
        public double getScore() {
            return 0;
        }
    }
}
