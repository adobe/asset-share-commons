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

package com.adobe.aem.commons.assetshare.components.search.impl;

import com.adobe.aem.commons.assetshare.components.search.Statistics;
import com.adobe.aem.commons.assetshare.search.Search;
import com.adobe.aem.commons.assetshare.search.results.Results;
import com.adobe.aem.commons.assetshare.util.impl.ModelCacheImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.classloader.DynamicClassLoaderManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private Search search;

    @Mock
    private Results results;

    @Mock
    private DynamicClassLoaderManager dynamicClassLoaderManager;

    @Before
    public void setUp() {
        ctx.create().resource("/content/statistics",
                "sling:resourceType", StatisticsImpl.RESOURCE_TYPE);
        ctx.currentResource("/content/statistics");

        // The real ModelCache implementation is used (it's a thin wrapper over request.adaptTo(..));
        // the Search model it looks up is stubbed via a custom adapter registration.
        ctx.registerService(DynamicClassLoaderManager.class, dynamicClassLoaderManager);
        ctx.addModelsForClasses(ModelCacheImpl.class, StatisticsImpl.class);
    }

    @Test
    public void isReady_true_whenSearchAvailable() {
        ctx.registerAdapter(SlingHttpServletRequest.class, Search.class, search);

        final Statistics statistics = ctx.request().adaptTo(Statistics.class);

        assertTrue(statistics.isReady());
    }

    @Test
    public void isReady_false_whenSearchUnavailable() {
        // No Search adapter registered, so modelCache.get(Search.class) resolves to null.
        final Statistics statistics = ctx.request().adaptTo(Statistics.class);

        assertFalse(statistics.isReady());
    }

    @Test
    public void getters_delegateToSearchResults() {
        ctx.registerAdapter(SlingHttpServletRequest.class, Search.class, search);
        doReturn(results).when(search).getResults();
        doReturn(42L).when(results).getRunningTotal();
        doReturn(100L).when(results).getTotal();
        doReturn(true).when(results).isMoreThanTotal();
        doReturn(250L).when(results).getTimeTaken();

        final Statistics statistics = ctx.request().adaptTo(Statistics.class);

        assertEquals(42L, statistics.getRunningTotal());
        assertEquals(100L, statistics.getTotal());
        assertTrue(statistics.hasMore());
        assertEquals(250L, statistics.getTimeTaken());
    }

    @Test
    public void getId_isStableAndUnique() {
        ctx.registerAdapter(SlingHttpServletRequest.class, Search.class, search);

        final Statistics statistics = ctx.request().adaptTo(Statistics.class);

        final String id = statistics.getId();
        assertNotNull(id);
        assertTrue(id.startsWith("cmp-statistics--"));
        // calling twice should return the same cached id
        assertEquals(id, statistics.getId());
    }

    @Test
    public void getExportedType() {
        ctx.registerAdapter(SlingHttpServletRequest.class, Search.class, search);

        final StatisticsImpl statistics = (StatisticsImpl) ctx.request().adaptTo(Statistics.class);

        assertEquals(StatisticsImpl.RESOURCE_TYPE, statistics.getExportedType());
    }
}
