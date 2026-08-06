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

package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.PagePredicate;
import com.adobe.aem.commons.assetshare.components.predicates.PagePredicate.ParamTypes;
import com.adobe.aem.commons.assetshare.components.search.impl.SearchConfigImpl;
import com.adobe.aem.commons.assetshare.search.searchpredicates.impl.ExcludeSubAssetsImpl;
import com.adobe.aem.commons.assetshare.testing.TestDefaultValuesPredicateImpl;
import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.scripting.SlingBindings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PagePredicateImplTest {

    private static final String PAGE_PATH = "/content/mysite/en";
    private static final String RESULTS_PATH = PAGE_PATH + "/jcr:content/results";

    @Rule
    public final AemContext ctx = new AemContext();

    private Page page;

    @Before
    public void setUp() {
        page = ctx.create().page(PAGE_PATH);
        // ResourceTypeVisitor (used to walk the page looking for HiddenPredicate/DefaultValuesPredicate
        // components) only descends into resources that have a sling:resourceType, so the page's
        // jcr:content itself needs one for the traversal to even begin.
        page.getContentResource().adaptTo(org.apache.sling.api.resource.ModifiableValueMap.class)
                .put("sling:resourceType", "asset-share-commons/components/page");
    }

    private PagePredicateImpl createImpl(Map<String, Object> resultsProps) {
        Map<String, Object> properties = new HashMap<>(resultsProps);
        properties.put("sling:resourceType", PagePredicateImpl.RESOURCE_TYPE);
        if (ctx.resourceResolver().getResource(RESULTS_PATH) == null) {
            ctx.create().resource(RESULTS_PATH, properties);
        }
        ctx.currentResource(RESULTS_PATH);

        // PagePredicateImpl injects Page currentPage via a plain @Inject, which Sling Models resolves
        // via the request's script bindings (ex. how AEM/HTL exposes "currentPage").
        final SlingBindings bindings = new SlingBindings();
        bindings.put("currentPage", page);
        ctx.request().setAttribute(SlingBindings.class.getName(), bindings);

        ctx.addModelsForClasses(PagePredicateImpl.class, SearchConfigImpl.class, HiddenPredicateImpl.class,
                TestDefaultValuesPredicateImpl.class);

        return (PagePredicateImpl) ctx.request().adaptTo(PagePredicate.class);
    }

    @Test
    public void getOrderBy_fromSearchConfigDefault() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals("@jcr:score", predicate.getOrderBy());
    }

    @Test
    public void getOrderBy_fromQueryParam() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=jcr%3Acontent%2Fjcr%3Atitle");

        assertEquals("jcr:content/jcr:title", predicate.getOrderBy());
    }

    @Test
    public void getOrderBySort_fromSearchConfigDefault() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals(Predicate.SORT_DESCENDING, predicate.getOrderBySort());
    }

    @Test
    public void getOrderBySort_fromQueryParam() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby.sort=asc");

        assertEquals("asc", predicate.getOrderBySort());
    }

    @Test
    public void getLimit_defaultsFromSearchConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put("limit", 30);

        PagePredicateImpl predicate = createImpl(props);

        assertEquals(30, predicate.getLimit());
    }

    @Test
    public void getLimit_fromQueryParam() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("p.limit=15");

        assertEquals(15, predicate.getLimit());
    }

    @Test
    public void getLimit_clampedToMax() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("p.limit=5000");

        assertEquals(1000, predicate.getLimit());
    }

    @Test
    public void getLimit_negativeFallsBackToDefault() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("p.limit=-5");

        assertEquals(50, predicate.getLimit());
    }

    @Test
    public void getLimit_invalidNumberFallsBackToSearchConfig() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("p.limit=not-a-number");

        // searchConfig default limit is 50 (SearchConfigImpl.DEFAULT_LIMIT), which is not > MAX_LIMIT
        // and not < 1, so it is returned as-is.
        assertEquals(50, predicate.getLimit());
    }

    @Test
    public void getOffset_defaultsToZero() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals(0, predicate.getOffset());
    }

    @Test
    public void getOffset_fromQueryParam() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("p.offset=20");

        assertEquals(20, predicate.getOffset());
    }

    @Test
    public void getOffset_invalidNumberFallsBackToZero() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("p.offset=abc");

        assertEquals(0, predicate.getOffset());
    }

    @Test
    public void getGuessTotal() {
        Map<String, Object> props = new HashMap<>();
        props.put("guessTotal", "500");

        PagePredicateImpl predicate = createImpl(props);

        assertEquals("500", predicate.getGuessTotal());
    }

    @Test
    public void getIndexTag() {
        Map<String, Object> props = new HashMap<>();
        props.put("indexTag", "myIndex");

        PagePredicateImpl predicate = createImpl(props);

        assertEquals("myIndex", predicate.getIndexTag());
    }

    @Test
    public void getFacetStrategy() {
        Map<String, Object> props = new HashMap<>();
        props.put("facetStrategy", "myStrategy");

        PagePredicateImpl predicate = createImpl(props);

        assertEquals("myStrategy", predicate.getFacetStrategy());
    }

    @Test
    public void getPaths() {
        Map<String, Object> props = new HashMap<>();
        props.put("paths", new String[]{"/content/dam/foo"});

        PagePredicateImpl predicate = createImpl(props);

        assertTrue(predicate.getPaths().contains("/content/dam/foo"));
    }

    @Test
    public void getPredicateGroup_includesTypePredicate() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        PredicateGroup group = predicate.getPredicateGroup();
        boolean hasType = false;
        for (Predicate p : group) {
            if ("dam:Asset".equals(p.get("type"))) {
                hasType = true;
            }
        }
        assertTrue(hasType);
    }

    @Test
    public void getPredicateGroup_excludesTypePredicateWhenRequested() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        PredicateGroup group = predicate.getPredicateGroup(ParamTypes.NODE_TYPE);
        boolean hasType = false;
        for (Predicate p : group) {
            if ("dam:Asset".equals(p.get("type"))) {
                hasType = true;
            }
        }
        assertFalse(hasType);
    }

    @Test
    public void getPredicateGroup_includesHiddenPredicates() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.create().resource(RESULTS_PATH + "/hidden", "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        ctx.create().resource(RESULTS_PATH + "/hidden/predicates");
        ctx.create().resource(RESULTS_PATH + "/hidden/predicates/item0", "predicate", "damAssetState", "value", "processed");

        PredicateGroup group = predicate.getPredicateGroup();
        assertTrue(group.toString().contains("processed"));
    }

    @Test
    public void getPredicateGroup_excludesHiddenPredicatesWhenRequested() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.create().resource(RESULTS_PATH + "/hidden", "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        ctx.create().resource(RESULTS_PATH + "/hidden/predicates");
        ctx.create().resource(RESULTS_PATH + "/hidden/predicates/item0", "predicate", "damAssetState", "value", "processed");

        PredicateGroup group = predicate.getPredicateGroup(ParamTypes.HIDDEN_PREDICATES);
        assertEquals(group.toString().contains("processed"), false);
    }

    @Test
    public void getPredicateGroup_includesDefaultValuesWhenNotParameterized() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.create().resource(RESULTS_PATH + "/defaults", "sling:resourceType", TestDefaultValuesPredicateImpl.RESOURCE_TYPE);

        PredicateGroup group = predicate.getPredicateGroup();
        assertTrue(group.toString().contains("testDefaultValue"));
    }

    @Test
    public void getPredicateGroup_excludesDefaultValuesWhenParameterized() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.create().resource(RESULTS_PATH + "/defaults", "sling:resourceType", TestDefaultValuesPredicateImpl.RESOURCE_TYPE);
        ctx.request().setQueryString("foo=bar&p.limit=10");

        PredicateGroup group = predicate.getPredicateGroup();
        assertFalse(group.toString().contains("testDefaultValue"));
    }

    @Test
    public void getPredicateGroup_includesSearchPredicates() {
        ctx.registerInjectActivateService(new ExcludeSubAssetsImpl());
        Map<String, Object> props = new HashMap<>();
        props.put("searchPredicates", new String[]{ExcludeSubAssetsImpl.NAME});

        PagePredicateImpl predicate = createImpl(props);

        PredicateGroup group = predicate.getPredicateGroup();
        assertTrue(group.toString().contains("mainasset"));
    }

    @Test
    public void getPredicateGroup_excludesSearchPredicatesWhenRequested() {
        ctx.registerInjectActivateService(new ExcludeSubAssetsImpl());
        Map<String, Object> props = new HashMap<>();
        props.put("searchPredicates", new String[]{ExcludeSubAssetsImpl.NAME});

        PagePredicateImpl predicate = createImpl(props);

        PredicateGroup group = predicate.getPredicateGroup(ParamTypes.SEARCH_PREDICATES);
        assertFalse(group.toString().contains("mainasset"));
    }

    @Test
    public void getPredicateGroup_includesPathPredicateGroup() {
        Map<String, Object> props = new HashMap<>();
        props.put("paths", new String[]{"/content/dam/foo"});

        PagePredicateImpl predicate = createImpl(props);

        PredicateGroup group = predicate.getPredicateGroup();
        assertTrue(group.toString().contains("/content/dam/foo"));
    }

    @Test
    public void getPredicateGroup_excludesPathWhenRequested() {
        Map<String, Object> props = new HashMap<>();
        props.put("paths", new String[]{"/content/dam/foo"});

        PagePredicateImpl predicate = createImpl(props);

        PredicateGroup group = predicate.getPredicateGroup(ParamTypes.PATH);
        assertFalse(group.toString().contains("/content/dam/foo"));
    }

    @Test
    public void getPredicateGroup_includesOrderByParameters() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        PredicateGroup group = predicate.getPredicateGroup();
        assertTrue(group.toString().contains("orderby"));
    }

    @Test
    public void getPredicateGroup_excludesOrderByWhenRequested() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        PredicateGroup group = predicate.getPredicateGroup(ParamTypes.ORDERBY);
        assertFalse(group.toString().contains("orderby"));
    }

    @Test
    public void getPredicateGroup_orderByCase_ignoreCaseAddedWhenNotCaseSensitive() {
        Map<String, Object> props = new HashMap<>();
        props.put("orderByCase", false);

        PagePredicateImpl predicate = createImpl(props);

        PredicateGroup group = predicate.getPredicateGroup();
        assertTrue(group.toString().contains(Predicate.IGNORE_CASE));
    }

    @Test
    public void getPredicateGroup_excludesOffsetWhenRequested() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("p.offset=5");

        PredicateGroup withOffset = predicate.getPredicateGroup();
        assertTrue(withOffset.toString().contains(Predicate.PARAM_OFFSET));

        PredicateGroup withoutOffset = predicate.getPredicateGroup(ParamTypes.OFFSET);
        assertFalse(withoutOffset.toString().contains(Predicate.PARAM_OFFSET));
    }

    @Test
    public void getPredicateGroup_excludesLimitWhenRequested() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        PredicateGroup group = predicate.getPredicateGroup(ParamTypes.LIMIT);
        assertFalse(group.toString().contains(Predicate.PARAM_LIMIT + "="));
    }

    @Test
    public void getPredicateGroup_excludesGuessTotalWhenRequested() {
        Map<String, Object> props = new HashMap<>();
        props.put("guessTotal", "500");

        PagePredicateImpl predicate = createImpl(props);

        PredicateGroup group = predicate.getPredicateGroup(ParamTypes.GUESS_TOTAL);
        assertFalse(group.toString().contains("500"));
    }

    @Test
    public void getPredicateGroup_excludesIndexTagWhenRequested() {
        Map<String, Object> props = new HashMap<>();
        props.put("indexTag", "myIndex");

        PagePredicateImpl predicate = createImpl(props);

        PredicateGroup groupWith = predicate.getPredicateGroup();
        assertTrue(groupWith.toString().contains("myIndex"));

        PredicateGroup groupWithout = predicate.getPredicateGroup(ParamTypes.INDEX_TAG);
        assertFalse(groupWithout.toString().contains("myIndex"));
    }

    @Test
    public void getPredicateGroup_blankIndexTagNotAdded() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        PredicateGroup group = predicate.getPredicateGroup();
        assertFalse(group.toString().contains("indextag"));
    }

    @Test
    public void getPredicateGroup_excludesFacetStrategyWhenRequested() {
        Map<String, Object> props = new HashMap<>();
        props.put("facetStrategy", "myStrategy");

        PagePredicateImpl predicate = createImpl(props);

        PredicateGroup groupWith = predicate.getPredicateGroup();
        assertTrue(groupWith.toString().contains("myStrategy"));

        PredicateGroup groupWithout = predicate.getPredicateGroup(ParamTypes.FACET_STRATEGY);
        assertFalse(groupWithout.toString().contains("myStrategy"));
    }

    @Test
    public void getPredicateGroup_blankFacetStrategyNotAdded() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        PredicateGroup group = predicate.getPredicateGroup();
        assertFalse(group.toString().contains("facetstrategy"));
    }

    // NOTE: PagePredicateImpl#getParams()/#getParams(ParamTypes...) (the deprecated Map-returning
    // methods) delegate to com.day.cq.search.PredicateConverter#createMap(PredicateGroup), which in
    // the real AEM runtime relies on com.day.cq.search.impl.builder.PredicateWalker. That class is part
    // of the QueryBuilder *implementation* (not the API surface shipped in the uber-jar test
    // dependency used by this module), so it is not present on the unit test classpath. Calling these
    // deprecated methods here reliably throws java.lang.NoClassDefFoundError, so they cannot be
    // exercised in this test environment. This is a pre-existing test-environment limitation, not
    // something introduced by these tests.

    @Test
    public void isReady_alwaysTrue() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        assertTrue(predicate.isReady());
    }

    @Test
    public void getName_isGroupParameterPrefix() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals(com.day.cq.search.PredicateConverter.GROUP_PARAMETER_PREFIX, predicate.getName());
    }

    @Test
    public void getExportedType() {
        PagePredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals(PagePredicateImpl.RESOURCE_TYPE, predicate.getExportedType());
    }
}
