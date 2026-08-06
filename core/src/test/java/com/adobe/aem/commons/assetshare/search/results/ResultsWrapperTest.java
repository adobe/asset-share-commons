package com.adobe.aem.commons.assetshare.search.results;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResultsWrapperTest {

    @Mock
    private Results wrappedResults;

    private ResultsWrapper resultsWrapper;

    @Before
    public void setUp() {
        resultsWrapper = new ResultsWrapper(wrappedResults);
    }

    @Test
    public void getWrappedResult_ReturnsWrappedInstance() {
        assertSame(wrappedResults, resultsWrapper.getWrappedResult());
    }

    @Test
    public void getResults_DelegatesToWrapped() {
        final List<Result> expected = Collections.emptyList();
        when(wrappedResults.getResults()).thenReturn(expected);

        assertSame(expected, resultsWrapper.getResults());
    }

    @Test
    public void getSize_DelegatesToWrapped() {
        when(wrappedResults.getSize()).thenReturn(5L);

        assertEquals(5, resultsWrapper.getSize());
    }

    @Test
    public void getTotal_DelegatesToWrapped() {
        when(wrappedResults.getTotal()).thenReturn(50L);

        assertEquals(50, resultsWrapper.getTotal());
    }

    @Test
    public void isMore_DelegatesToWrapped() {
        when(wrappedResults.isMore()).thenReturn(true);

        assertTrue(resultsWrapper.isMore());
    }

    @Test
    public void getTimeTaken_DelegatesToWrapped() {
        when(wrappedResults.getTimeTaken()).thenReturn(999L);

        assertEquals(999, resultsWrapper.getTimeTaken());
    }

    @Test
    public void getRunningTotal_DelegatesToWrapped() {
        when(wrappedResults.getRunningTotal()).thenReturn(3L);

        assertEquals(3, resultsWrapper.getRunningTotal());
    }

    @Test
    public void getNextOffset_DelegatesToWrapped() {
        when(wrappedResults.getNextOffset()).thenReturn(20L);

        assertEquals(20, resultsWrapper.getNextOffset());
    }

    @Test
    public void getStatus_DelegatesToWrapped() {
        when(wrappedResults.getStatus()).thenReturn(Results.Status.SUCCESS);

        assertEquals(Results.Status.SUCCESS, resultsWrapper.getStatus());
    }

    @Test
    public void getAdditionalData_DelegatesToWrapped() {
        final ValueMap valueMap = new ValueMapDecorator(new HashMap<>());
        valueMap.put("foo", "bar");
        when(wrappedResults.getAdditionalData()).thenReturn(valueMap);

        assertSame(valueMap, resultsWrapper.getAdditionalData());
        assertEquals("bar", resultsWrapper.getAdditionalData().get("foo"));
    }
}
