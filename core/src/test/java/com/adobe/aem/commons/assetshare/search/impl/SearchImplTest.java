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

package com.adobe.aem.commons.assetshare.search.impl;

import com.adobe.aem.commons.assetshare.components.search.SearchConfig;
import com.adobe.aem.commons.assetshare.search.Constants;
import com.adobe.aem.commons.assetshare.search.Search;
import com.adobe.aem.commons.assetshare.search.UnsafeSearchException;
import com.adobe.aem.commons.assetshare.search.providers.SearchProvider;
import com.adobe.aem.commons.assetshare.search.results.Results;
import com.adobe.aem.commons.assetshare.search.results.impl.results.EmptyResultsImpl;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jcr.RepositoryException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Note on testing approach: SearchImpl's {@code @Self SearchConfig searchConfig} field cannot be reliably
 * substituted with a Mockito mock via ctx.registerAdapter(..): io.wcm's AemContext auto-discovers ALL
 * {@code @Model}-annotated classes on the module's classpath (via the bnd-generated "Sling-Model-Packages"
 * manifest header), so the real {@code SearchConfigImpl} is always a candidate for {@code SearchConfig}
 * adaptation and, in practice, wins over a custom-registered adapter for an explicit
 * request.adaptTo(SearchConfig.class) call. Rather than fight that (or stand up all of SearchConfigImpl's
 * own real dependencies just to exercise SearchImpl's own logic), this test constructs SearchImpl directly
 * and injects its private fields via reflection - a legitimate white-box, same-package technique - giving
 * full, deterministic control over searchConfig/currentPage/searchProviders/request exactly as SearchImpl
 * declares them.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private SearchConfig searchConfig;

    @Mock
    private Page currentPage;

    @Mock
    private SearchProvider acceptingProvider;

    @Mock
    private SearchProvider nonAcceptingProvider;

    @Mock
    private Results providerResults;

    private SearchImpl searchImpl;

    @Before
    public void setUp() throws Exception {
        searchImpl = new SearchImpl();

        setField("request", ctx.request());
        setField("searchConfig", searchConfig);
        setField("currentPage", currentPage);
        setField("searchProviders", new java.util.ArrayList<SearchProvider>());

        final ValueMap pageProperties = new ValueMapDecorator(new HashMap<>());
        pageProperties.put("mode", "browse");
        when(currentPage.getProperties()).thenReturn(pageProperties);
    }

    @SuppressWarnings("unchecked")
    private void setField(final String name, final Object value) throws Exception {
        final Field field = SearchImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(searchImpl, value);
    }

    @Test
    public void getFormId_ReturnsConstant() {
        assertEquals(Constants.FORM_ID, searchImpl.getFormId());
    }

    @Test
    public void getResults_UsesFirstAcceptingProviderInOrder() throws Exception {
        when(nonAcceptingProvider.accepts(ctx.request())).thenReturn(false);
        when(acceptingProvider.accepts(ctx.request())).thenReturn(true);
        when(acceptingProvider.getResults(ctx.request())).thenReturn(providerResults);

        setField("searchProviders", Arrays.asList(nonAcceptingProvider, acceptingProvider));

        assertSame(providerResults, searchImpl.getResults());
        verify(nonAcceptingProvider, never()).getResults(any());
    }

    @Test
    public void getResults_StopsAtFirstAcceptingProvider_DoesNotConsultLaterProviders() throws Exception {
        final SearchProvider secondAcceptingProvider = org.mockito.Mockito.mock(SearchProvider.class);

        when(acceptingProvider.accepts(ctx.request())).thenReturn(true);
        when(acceptingProvider.getResults(ctx.request())).thenReturn(providerResults);

        setField("searchProviders", Arrays.asList(acceptingProvider, secondAcceptingProvider));

        assertSame(providerResults, searchImpl.getResults());
        verify(secondAcceptingProvider, never()).accepts(any());
        verify(secondAcceptingProvider, never()).getResults(any());
    }

    @Test
    public void getResults_CachesResultAcrossMultipleCalls() throws Exception {
        when(acceptingProvider.accepts(ctx.request())).thenReturn(true);
        when(acceptingProvider.getResults(ctx.request())).thenReturn(providerResults);
        setField("searchProviders", Collections.singletonList(acceptingProvider));

        assertSame(providerResults, searchImpl.getResults());
        assertSame(providerResults, searchImpl.getResults());

        verify(acceptingProvider, times(1)).getResults(ctx.request());
        verify(acceptingProvider, times(1)).accepts(ctx.request());
    }

    @Test
    public void getResults_ProviderThrowsUnsafeSearchException_ReturnsErringResults() throws Exception {
        when(acceptingProvider.accepts(ctx.request())).thenReturn(true);
        when(acceptingProvider.getResults(ctx.request())).thenThrow(new UnsafeSearchException("unsafe"));
        setField("searchProviders", Collections.singletonList(acceptingProvider));

        assertSame(Results.ERRING_RESULTS, searchImpl.getResults());
    }

    @Test
    public void getResults_ProviderThrowsRepositoryException_ReturnsErringResults() throws Exception {
        when(acceptingProvider.accepts(ctx.request())).thenReturn(true);
        when(acceptingProvider.getResults(ctx.request())).thenThrow(new RepositoryException("boom"));
        setField("searchProviders", Collections.singletonList(acceptingProvider));

        assertSame(Results.ERRING_RESULTS, searchImpl.getResults());
    }

    @Test
    public void getResults_NoAcceptingProvider_ReturnsEmptyResults() throws Exception {
        when(acceptingProvider.accepts(ctx.request())).thenReturn(false);
        setField("searchProviders", Collections.singletonList(acceptingProvider));

        assertTrue(searchImpl.getResults() instanceof EmptyResultsImpl);
    }

    @Test
    public void getResults_NoProvidersRegisteredAtAll_ReturnsEmptyResults() {
        assertTrue(searchImpl.getResults() instanceof EmptyResultsImpl);
    }

    @Test
    public void getMode_FromSearchConfigWhenNotBlank() {
        when(searchConfig.getMode()).thenReturn("list");

        assertEquals("list", searchImpl.getMode());
    }

    @Test
    public void getMode_FallsBackToCurrentPageProperty() {
        when(searchConfig.getMode()).thenReturn("");

        assertEquals("browse", searchImpl.getMode());
    }

    @Test
    public void getMode_FallsBackToDefaultWhenNeitherConfiguredNorOnPage() {
        when(searchConfig.getMode()).thenReturn(null);
        when(currentPage.getProperties()).thenReturn(new ValueMapDecorator(new HashMap<>()));

        assertEquals("search", searchImpl.getMode());
    }

    @Test
    public void getLayout_FromQueryParam() {
        final Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("layout", "list");
        ctx.request().setParameterMap(requestParams);

        assertEquals("list", searchImpl.getLayout());
    }

    @Test
    public void getLayout_FallsBackToSearchConfigWhenNoQueryParam() {
        when(searchConfig.getLayout()).thenReturn("card");

        assertEquals("card", searchImpl.getLayout());
    }
}
