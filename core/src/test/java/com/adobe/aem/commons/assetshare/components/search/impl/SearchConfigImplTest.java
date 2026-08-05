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

import com.adobe.aem.commons.assetshare.components.search.SearchConfig;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SearchConfigImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/search/impl/SearchConfigImplTest.json", "/content");
        ctx.addModelsForClasses(SearchConfigImpl.class);
    }

    @Test
    public void init_directlyOnResultsComponent_readsProperties() {
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/results");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("list", config.getLayout());
        assertEquals(25, config.getLimit());
        assertEquals("@jcr:content/title", config.getOrderBy());
        assertEquals("asc", config.getOrderBySort());
        assertFalse(config.isOrderByCase());
        assertEquals("500", config.getGuessTotal());
        assertEquals("browse", config.getSearchProviderId());
        assertEquals("browse", config.getMode());
        assertEquals("myIndex", config.getIndexTag());
        assertEquals("cached", config.getFacetStrategy());

        final List<String> paths = config.getPaths();
        assertEquals(1, paths.size());
        assertEquals("/content/dam/foo", paths.get(0));

        final List<String> predicates = config.getSearchPredicatesNames();
        assertEquals(2, predicates.size());
        assertTrue(predicates.contains("path"));
        assertTrue(predicates.contains("tags"));
    }

    @Test
    public void init_foundViaPageTraversal_fromSiblingComponent() {
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/predicates");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("list", config.getLayout());
    }

    @Test
    public void init_recursesToParentPage_whenNotFoundOnCurrentPage() {
        ctx.currentPage("/content/site/child");
        ctx.currentResource("/content/site/child/jcr:content/predicates");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("list", config.getLayout());
    }

    @Test
    public void init_notFoundAnywhere_adaptsToNull() {
        ctx.currentPage("/content/lonely");
        ctx.currentResource("/content/lonely/jcr:content/predicates");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertNull(config);
    }

    @Test
    public void init_invalidResourcePath_adaptsToNull() {
        ctx.create().resource("/etc/something");
        ctx.currentPage("/content/site");
        ctx.currentResource("/etc/something");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertNull(config);
    }

    @Test
    public void defaults_whenNoPropertiesConfigured() {
        ctx.create().resource("/content/site/jcr:content/results-defaults",
                "sling:resourceType", SearchConfigImpl.RESOURCE_TYPE);
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/results-defaults");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("card", config.getLayout());
        assertEquals(50, config.getLimit());
        assertEquals("@jcr:score", config.getOrderBy());
        assertEquals("desc", config.getOrderBySort());
        assertTrue(config.isOrderByCase());
        assertEquals("250", config.getGuessTotal());
        assertEquals("search", config.getSearchProviderId());
        assertNull(config.getIndexTag());
        assertNull(config.getFacetStrategy());
        assertEquals(1, config.getPaths().size());
        assertEquals("/content/dam", config.getPaths().get(0));
        assertTrue(config.getSearchPredicatesNames().isEmpty());
    }

    @Test
    public void getGuessTotal_trueStringIsPreserved() {
        ctx.create().resource("/content/site/jcr:content/results-guess-true",
                "sling:resourceType", SearchConfigImpl.RESOURCE_TYPE,
                "guessTotal", "true");
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/results-guess-true");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("true", config.getGuessTotal());
    }

    @Test
    public void getGuessTotal_nonNumeric_fallsBackToDefault() {
        ctx.create().resource("/content/site/jcr:content/results-guess-invalid",
                "sling:resourceType", SearchConfigImpl.RESOURCE_TYPE,
                "guessTotal", "not-a-number");
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/results-guess-invalid");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("250", config.getGuessTotal());
    }

    @Test
    public void getGuessTotal_outOfRange_fallsBackToDefault() {
        ctx.create().resource("/content/site/jcr:content/results-guess-toobig",
                "sling:resourceType", SearchConfigImpl.RESOURCE_TYPE,
                "guessTotal", "5000");
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/results-guess-toobig");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("250", config.getGuessTotal());
    }

    @Test
    public void getGuessTotal_negative_fallsBackToDefault() {
        ctx.create().resource("/content/site/jcr:content/results-guess-negative",
                "sling:resourceType", SearchConfigImpl.RESOURCE_TYPE,
                "guessTotal", "-5");
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/results-guess-negative");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("250", config.getGuessTotal());
    }

    @Test
    public void getExportedType() {
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/results");

        final SearchConfigImpl config = (SearchConfigImpl) ctx.request().adaptTo(SearchConfig.class);

        assertEquals(SearchConfigImpl.RESOURCE_TYPE, config.getExportedType());
    }

    @Test
    public void getProperties_returnsResourceValueMap() {
        ctx.currentPage("/content/site");
        ctx.currentResource("/content/site/jcr:content/results");

        final SearchConfig config = ctx.request().adaptTo(SearchConfig.class);

        assertEquals("list", config.getProperties().get("layout", String.class));
    }
}
