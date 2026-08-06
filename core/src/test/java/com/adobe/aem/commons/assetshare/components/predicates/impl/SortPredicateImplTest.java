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

import com.adobe.aem.commons.assetshare.components.predicates.AbstractPredicate;
import com.adobe.aem.commons.assetshare.components.search.SearchConfig;
import com.adobe.aem.commons.assetshare.testing.ReflectionTestUtil;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.search.Predicate;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SortPredicateImplTest {

    private static final String RESOURCE_PATH = "/content/sort";

    @Rule
    public final AemContext ctx = new AemContext();

    private SearchConfig searchConfig;
    private Options coreOptions;

    @Before
    public void setUp() {
        searchConfig = mock(SearchConfig.class);
        when(searchConfig.getOrderBy()).thenReturn("@jcr:score");
        when(searchConfig.getOrderBySort()).thenReturn(Predicate.SORT_DESCENDING);
        when(searchConfig.isOrderByCase()).thenReturn(true);

        coreOptions = mock(Options.class);
    }

    private SortPredicateImpl createImpl(Map<String, Object> props) {
        Map<String, Object> properties = new HashMap<>(props);
        properties.put("sling:resourceType", "asset-share-commons/components/search/sort");
        ctx.create().resource(RESOURCE_PATH, properties);

        ctx.create().resource(RESOURCE_PATH + "/items");
        ctx.create().resource(RESOURCE_PATH + "/items/relevance", "text", "Relevance", "value", "@jcr:score");
        ctx.create().resource(RESOURCE_PATH + "/items/title", "text", "Title", "value", "jcr:content/jcr:title", "orderByCase", false);

        ctx.currentResource(RESOURCE_PATH);

        final SortPredicateImpl impl = new SortPredicateImpl();
        ReflectionTestUtil.setField(impl, AbstractPredicate.class, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "coreOptions", coreOptions);
        ReflectionTestUtil.setField(impl, "searchConfig", searchConfig);
        ReflectionTestUtil.setField(impl, "unknownSortBy", props.getOrDefault("unknownSortBy", "Default"));
        ReflectionTestUtil.setField(impl, "ascendingLabel", props.getOrDefault("ascendingLabel", "ASC"));
        ReflectionTestUtil.setField(impl, "descendingLabel", props.getOrDefault("descendingLabel", "DESC"));
        impl.init();

        return impl;
    }

    @Test
    public void getName_isOrderBy() {
        SortPredicateImpl impl = createImpl(new HashMap<>());

        assertEquals("orderby", impl.getName());
    }

    @Test
    public void getItems_populatedFromChildResources() {
        SortPredicateImpl impl = createImpl(new HashMap<>());

        List<OptionItem> items = impl.getItems();
        assertEquals(2, items.size());
    }

    @Test
    public void getItems_selectedMatchesInitialValues() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=jcr%3Acontent%2Fjcr%3Atitle");

        List<OptionItem> items = impl.getItems();
        boolean anySelected = items.stream().anyMatch(OptionItem::isSelected);
        assertTrue(anySelected);
    }

    @Test
    public void getOrderByLabel_defaultsToUnknown() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=something-not-in-the-list");

        assertEquals("Default", impl.getOrderByLabel());
    }

    @Test
    public void getOrderByLabel_matchesSelected() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=jcr%3Acontent%2Fjcr%3Atitle");

        assertEquals("Title", impl.getOrderByLabel());
    }

    @Test
    public void isAscending_true() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=%40jcr%3Ascore&orderby.sort=asc");

        assertTrue(impl.isAscending());
    }

    @Test
    public void isAscending_false() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=%40jcr%3Ascore&orderby.sort=desc");

        assertFalse(impl.isAscending());
    }

    @Test
    public void getOrderBySortLabel_ascending() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=%40jcr%3Ascore&orderby.sort=asc");

        assertEquals("ASC", impl.getOrderBySortLabel());
    }

    @Test
    public void getOrderBySortLabel_descending() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=%40jcr%3Ascore&orderby.sort=desc");

        assertEquals("DESC", impl.getOrderBySortLabel());
    }

    @Test
    public void isReady_trueWhenSearchConfigAndItemsPresent() {
        SortPredicateImpl impl = createImpl(new HashMap<>());

        assertTrue(impl.isReady());
    }

    @Test
    public void isReady_falseWhenSearchConfigNull() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ReflectionTestUtil.setField(impl, "searchConfig", null);

        assertFalse(impl.isReady());
    }

    @Test
    public void getInitialValues_fallsBackToSearchConfig() {
        SortPredicateImpl impl = createImpl(new HashMap<>());

        assertEquals("@jcr:score", impl.getInitialValues().get(Predicate.ORDER_BY, String.class));
        assertEquals(Predicate.SORT_DESCENDING, impl.getInitialValues().get(Predicate.PARAM_SORT, String.class));
    }

    @Test
    public void getInitialValues_usesQueryParamsWhenPresent() {
        SortPredicateImpl impl = createImpl(new HashMap<>());
        ctx.request().setQueryString("orderby=jcr%3Acontent%2Fjcr%3Atitle&orderby.sort=asc");

        assertEquals("jcr:content/jcr:title", impl.getInitialValues().get(Predicate.ORDER_BY, String.class));
        assertEquals("asc", impl.getInitialValues().get(Predicate.PARAM_SORT, String.class));
    }

    @Test
    public void getExportedType() {
        SortPredicateImpl impl = createImpl(new HashMap<>());

        assertEquals("asset-share-commons/components/search/sort", impl.getExportedType());
    }
}
