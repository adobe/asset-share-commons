package com.adobe.aem.commons.assetshare.search.results.impl.results;

import com.adobe.aem.commons.assetshare.search.results.Result;
import com.adobe.aem.commons.assetshare.search.results.Results;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.ResultPage;
import com.day.cq.search.result.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryBuilderResultsImplTest {

    @Mock
    private SearchResult searchResult;

    @Mock
    private ResultPage nextPage;

    @Mock
    private Result result1;

    @Mock
    private Result result2;

    @Mock
    private Hit hit1;

    @Mock
    private Hit hit2;

    @Test
    public void constructor_WithNextPageAndMoreResults() {
        final List<Result> results = new ArrayList<>();
        results.add(result1);
        results.add(result2);

        final List<Hit> hits = new ArrayList<>();
        hits.add(hit1);
        hits.add(hit2);

        when(searchResult.getTotalMatches()).thenReturn(100L);
        when(searchResult.getExecutionTimeMillis()).thenReturn(123L);
        when(searchResult.getNextPage()).thenReturn(nextPage);
        when(nextPage.getStart()).thenReturn(2L);
        when(searchResult.getStartIndex()).thenReturn(0L);
        when(searchResult.getHits()).thenReturn(hits);
        when(searchResult.hasMore()).thenReturn(true);

        final QueryBuilderResultsImpl queryBuilderResults = new QueryBuilderResultsImpl(results, searchResult);

        assertEquals(results, queryBuilderResults.getResults());
        assertEquals(2, queryBuilderResults.getSize());
        assertEquals(100, queryBuilderResults.getTotal());
        assertEquals(123, queryBuilderResults.getTimeTaken());
        assertEquals(2, queryBuilderResults.getNextOffset());
        assertEquals(2, queryBuilderResults.getRunningTotal());
        assertTrue(queryBuilderResults.isMoreThanTotal());
        assertTrue(queryBuilderResults.isMore());
        assertEquals(Results.Status.SUCCESS, queryBuilderResults.getStatus());
        assertSame(searchResult, queryBuilderResults.getSearchResult());
    }

    @Test
    public void constructor_ResultsListIsUnmodifiable() {
        final List<Result> results = new ArrayList<>();
        results.add(result1);

        when(searchResult.getHits()).thenReturn(Collections.emptyList());
        when(searchResult.getNextPage()).thenReturn(null);

        final QueryBuilderResultsImpl queryBuilderResults = new QueryBuilderResultsImpl(results, searchResult);

        try {
            queryBuilderResults.getResults().add(result2);
            org.junit.Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void constructor_WithNoNextPage_NextOffsetIsNegativeOne() {
        final List<Result> results = new ArrayList<>();

        when(searchResult.getNextPage()).thenReturn(null);
        when(searchResult.getHits()).thenReturn(Collections.emptyList());
        when(searchResult.hasMore()).thenReturn(false);
        when(searchResult.getStartIndex()).thenReturn(0L);
        when(searchResult.getTotalMatches()).thenReturn(0L);

        final QueryBuilderResultsImpl queryBuilderResults = new QueryBuilderResultsImpl(results, searchResult);

        assertEquals(-1, queryBuilderResults.getNextOffset());
        assertFalse(queryBuilderResults.isMoreThanTotal());
    }

    @Test
    public void constructor_WithRunningTotalLessThanTotalMatches_IsMoreTrue() {
        final List<Result> results = new ArrayList<>();
        final List<Hit> hits = new ArrayList<>();
        hits.add(hit1);

        when(searchResult.getNextPage()).thenReturn(null);
        when(searchResult.getHits()).thenReturn(hits);
        when(searchResult.hasMore()).thenReturn(false);
        when(searchResult.getStartIndex()).thenReturn(0L);
        when(searchResult.getTotalMatches()).thenReturn(10L);

        final QueryBuilderResultsImpl queryBuilderResults = new QueryBuilderResultsImpl(results, searchResult);

        assertTrue(queryBuilderResults.isMore());
    }

    @Test
    public void getAdditionalData_NotNull() {
        when(searchResult.getNextPage()).thenReturn(null);
        when(searchResult.getHits()).thenReturn(Collections.emptyList());

        final QueryBuilderResultsImpl queryBuilderResults = new QueryBuilderResultsImpl(new ArrayList<>(), searchResult);

        assertNotNull(queryBuilderResults.getAdditionalData());
        assertTrue(queryBuilderResults.getAdditionalData().isEmpty());
    }
}
