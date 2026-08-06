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

package com.adobe.aem.commons.assetshare.search.providers.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.adobe.aem.commons.assetshare.components.predicates.PagePredicate;
import com.adobe.aem.commons.assetshare.search.SearchSafety;
import com.adobe.aem.commons.assetshare.search.results.Result;
import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.ResultPage;
import com.day.cq.search.result.SearchResult;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Note on the missing "happy path" test for getResults(..): QuerySearchProviderImpl#getParams(..)
 * unconditionally ends with a call to {@code com.day.cq.search.PredicateConverter#createMap(PredicateGroup)}.
 * That method's real implementation (and even Mockito's static-mocking machinery attempting to intercept it,
 * which was tried and abandoned here) depends on the internal (non-public API) class
 * {@code com.day.cq.search.impl.builder.PredicateWalker}. That class only ships inside a real, running AEM
 * instance - it is not present in the published "uber-jar" or any other test-scope dependency available to
 * this module. As a result, ANY call to getResults()/getParams() throws NoClassDefFoundError in this unit
 * test environment, regardless of how QueryBuilder/ModelFactory/etc. are mocked. This is a pre-existing
 * testability gap (the code works fine deployed in AEM) - see getResults_ThrowsNoClassDefFoundError(..) below,
 * which documents/covers this. The remaining tests exercise the smaller, self-contained private helper
 * methods (which do NOT depend on the missing class) directly via reflection, since they contain the bulk of
 * this class's actual decision logic.
 */
@RunWith(MockitoJUnitRunner.class)
public class QuerySearchProviderImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private SearchSafety searchSafety;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private ModelFactory modelFactory;

    @Mock
    private PagePredicate pagePredicate;

    @Mock
    private SearchResult searchResult;

    @Mock
    private ResultPage nextPage;

    private QuerySearchProviderImpl provider;

    private Object invokePrivate(final String methodName, final Class<?>[] paramTypes, final Object... args) throws Exception {
        final Method method = QuerySearchProviderImpl.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(provider, args);
    }

    @org.junit.Before
    public void setUp() {
        ctx.registerService(SearchSafety.class, searchSafety);
        ctx.registerService(QueryBuilder.class, queryBuilder);
        ctx.registerService(ModelFactory.class, modelFactory);

        provider = ctx.registerInjectActivateService(new QuerySearchProviderImpl());

        ctx.registerAdapter(SlingHttpServletRequest.class, PagePredicate.class, pagePredicate);
        when(pagePredicate.getPaths()).thenReturn(Collections.emptyList());
        // Stub the varargs getPredicateGroup(..) call as invoked with an explicit empty exclusion array (the
        // shape used when the request contains no user-provided/allowed path predicate - see
        // getResults_ThrowsNoClassDefFoundError(..) below, the only test that reaches this call).
        when(pagePredicate.getPredicateGroup(new PagePredicate.ParamTypes[]{})).thenReturn(new PredicateGroup());
    }

    @Test
    public void accepts_AlwaysReturnsTrue() {
        assertTrue(provider.accepts(ctx.request()));
    }

    @Test
    public void getResults_ThrowsNoClassDefFoundError_DueToMissingAemInternalPredicateConverterClass() {
        try {
            provider.getResults(ctx.request());
            fail("Expected a NoClassDefFoundError - see class-level javadoc for why.");
        } catch (final NoClassDefFoundError expected) {
            assertTrue(expected.getMessage().contains("PredicateWalker"));
        } catch (final Exception e) {
            fail("Expected a NoClassDefFoundError but got: " + e);
        }
    }

    @Test
    public void cleanParams_RemovesJunkParamsButKeepsOthers() throws Exception {
        final Map<String, String> params = new HashMap<>();
        params.put("p.guessTotal", "true");
        params.put("mode", "list");
        params.put("layout", "card");
        params.put("wcmmode", "edit");
        params.put("forceeditcontext", "true");
        params.put("fulltext", "logo");
        params.put("path", "/content/dam");

        invokePrivate("cleanParams", new Class<?>[]{Map.class}, params);

        assertFalse(params.containsKey("p.guessTotal"));
        assertFalse(params.containsKey("mode"));
        assertFalse(params.containsKey("layout"));
        assertFalse(params.containsKey("wcmmode"));
        assertFalse(params.containsKey("forceeditcontext"));
        assertEquals("logo", params.get("fulltext"));
        assertEquals("/content/dam", params.get("path"));
    }

    @Test
    public void cleanParams_WithNoJunkParams_LeavesMapUnchanged() throws Exception {
        final Map<String, String> params = new HashMap<>();
        params.put("fulltext", "logo");

        invokePrivate("cleanParams", new Class<?>[]{Map.class}, params);

        assertEquals(1, params.size());
        assertEquals("logo", params.get("fulltext"));
    }

    @Test
    public void isPathsProvidedByRequestParams_WithNoPathParams_ReturnsFalse() throws Exception {
        final Map<String, String> params = new HashMap<>();
        params.put("fulltext", "logo");

        final boolean actual = (boolean) invokePrivate("isPathsProvidedByRequestParams",
                new Class<?>[]{PagePredicate.class, Map.class}, pagePredicate, params);

        assertFalse(actual);
        assertEquals(1, params.size());
    }

    @Test
    public void isPathsProvidedByRequestParams_WithAllowedPath_ReturnsTrueAndKeepsPath() throws Exception {
        when(pagePredicate.getPaths()).thenReturn(Collections.singletonList("/content/dam/allowed"));

        final Map<String, String> params = new HashMap<>();
        params.put("path", "/content/dam/allowed/sub-folder");

        final boolean actual = (boolean) invokePrivate("isPathsProvidedByRequestParams",
                new Class<?>[]{PagePredicate.class, Map.class}, pagePredicate, params);

        assertTrue(actual);
        assertEquals("/content/dam/allowed/sub-folder", params.get("path"));
    }

    @Test
    public void isPathsProvidedByRequestParams_WithExactAllowedPath_ReturnsTrueAndKeepsPath() throws Exception {
        when(pagePredicate.getPaths()).thenReturn(Collections.singletonList("/content/dam/allowed"));

        final Map<String, String> params = new HashMap<>();
        params.put("path", "/content/dam/allowed");

        final boolean actual = (boolean) invokePrivate("isPathsProvidedByRequestParams",
                new Class<?>[]{PagePredicate.class, Map.class}, pagePredicate, params);

        assertTrue(actual);
        assertEquals("/content/dam/allowed", params.get("path"));
    }

    @Test
    public void isPathsProvidedByRequestParams_WithDisallowedPath_ReturnsFalseAndRemovesPath() throws Exception {
        when(pagePredicate.getPaths()).thenReturn(Collections.singletonList("/content/dam/allowed"));

        final Map<String, String> params = new HashMap<>();
        params.put("path", "/content/dam/not-allowed");

        final boolean actual = (boolean) invokePrivate("isPathsProvidedByRequestParams",
                new Class<?>[]{PagePredicate.class, Map.class}, pagePredicate, params);

        assertFalse(actual);
        assertFalse("The disallowed path predicate must have been removed from the params map",
                params.containsKey("path"));
    }

    @Test
    public void isPathsProvidedByRequestParams_WithMixOfAllowedAndDisallowedPaths_KeepsOnlyAllowed() throws Exception {
        when(pagePredicate.getPaths()).thenReturn(Collections.singletonList("/content/dam/allowed"));

        final Map<String, String> params = new HashMap<>();
        params.put("path", "/content/dam/allowed/one");
        params.put("1_path", "/content/dam/not-allowed/two");

        final boolean actual = (boolean) invokePrivate("isPathsProvidedByRequestParams",
                new Class<?>[]{PagePredicate.class, Map.class}, pagePredicate, params);

        assertTrue(actual);
        assertTrue(params.containsKey("path"));
        assertFalse(params.containsKey("1_path"));
    }

    @Test
    public void safeMerge_CombinesGroupsAndResetsGroupPredicateNames() throws Exception {
        final Map<String, String> srcParams = new HashMap<>();
        srcParams.put("group.fulltext", "logo");
        final PredicateGroup src = com.day.cq.search.PredicateConverter.createPredicates(srcParams);

        final PredicateGroup dest = new PredicateGroup();
        dest.add(new Predicate("path", "path").set("path", "/content/dam"));

        final PredicateGroup merged = (PredicateGroup) invokePrivate("safeMerge",
                new Class<?>[]{PredicateGroup.class, PredicateGroup.class}, src, dest);

        // The 'dest' path predicate must still be present under its original name.
        assertEquals("/content/dam", merged.getByName("path").get("path"));

        // The 'src' predicate group must have been merged in (with its group-name reset so it does not
        // collide with dest's predicate names) - verify by total predicate count.
        assertEquals(2, merged.size());
    }

    @Test
    public void safeMerge_WithParameterGroup_PreservesPName() throws Exception {
        // Manually build a "p" (GROUP_PARAMETER_PREFIX) named PredicateGroup child, mirroring exactly how
        // PagePredicateImpl.getPredicateGroup(..) builds its own parameterGroup, to unambiguously exercise
        // the "is this the reserved 'p' group" branch in safeMerge(..).
        final PredicateGroup src = new PredicateGroup();
        final PredicateGroup pGroupChild = new PredicateGroup(com.day.cq.search.PredicateConverter.GROUP_PARAMETER_PREFIX);
        pGroupChild.add(new Predicate("limit").set("limit", "10"));
        src.add(pGroupChild);

        final PredicateGroup dest = new PredicateGroup();

        final PredicateGroup merged = (PredicateGroup) invokePrivate("safeMerge",
                new Class<?>[]{PredicateGroup.class, PredicateGroup.class}, src, dest);

        // Locate the merged "p" group by index rather than PredicateGroup#getByName(..): the "p" child is the
        // only element merged in from src, and clone(false) (the "keep name alone" branch for the reserved
        // GROUP_PARAMETER_PREFIX) preserves its position and name.
        assertEquals(1, merged.size());
        final Predicate pGroup = merged.get(0);
        assertEquals(com.day.cq.search.PredicateConverter.GROUP_PARAMETER_PREFIX, pGroup.getName());
        assertEquals(PredicateGroup.TYPE, pGroup.getType());
        assertEquals("10", ((PredicateGroup) pGroup).get(0).get("limit"));
    }

    @Test
    public void debugPreQuery_WithDebugDisabled_DoesNotThrow() throws Exception {
        final Logger logger = (Logger) LoggerFactory.getLogger(QuerySearchProviderImpl.class);
        final Level original = logger.getLevel();
        logger.setLevel(Level.WARN);
        try {
            invokePrivate("debugPreQuery", new Class<?>[]{PredicateGroup.class}, new PredicateGroup());
        } finally {
            logger.setLevel(original);
        }
    }

    @Test
    public void debugPostQuery_WithDebugEnabled_LogsSearchResultStats() throws Exception {
        when(searchResult.getQueryStatement()).thenReturn("SELECT * FROM [dam:Asset]");
        when(searchResult.getHits()).thenReturn(Collections.emptyList());
        when(searchResult.getResultPages()).thenReturn(Collections.emptyList());
        when(searchResult.getStartIndex()).thenReturn(0L);
        when(searchResult.hasMore()).thenReturn(false);
        when(searchResult.getTotalMatches()).thenReturn(0L);
        when(searchResult.getExecutionTimeMillis()).thenReturn(1L);

        final Logger logger = (Logger) LoggerFactory.getLogger(QuerySearchProviderImpl.class);
        final Level original = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            invokePrivate("debugPostQuery", new Class<?>[]{SearchResult.class}, searchResult);
        } finally {
            logger.setLevel(original);
        }
    }

    @Test
    public void debugPostAdaptation_WithDebugEnabled_LogsResultsSize() throws Exception {
        final List<Result> results = new ArrayList<>();

        final Logger logger = (Logger) LoggerFactory.getLogger(QuerySearchProviderImpl.class);
        final Level original = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        try {
            invokePrivate("debugPostAdaptation", new Class<?>[]{List.class}, results);
        } finally {
            logger.setLevel(original);
        }
    }
}
