package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PredicateEvaluatorUtilTest {

    @Mock
    Predicate predicate;

    @Test
    public void getValues() {
        when(predicate.getParameters()).thenReturn((ImmutableMap.<String, String>builder().
                put("test", "zero").
                put("1_test", "one").
                put("2_test", "two").
                put("_test", "no").
                put("incorrect", "no").
                put("1_incorrect", "no").
                build());

        final List<String> actuals = PredicateEvaluatorUtil.getValues(predicate, "test"));

        assertEquals(actuals.size(), 3);
        assertArrayEquals(new String[] { "zero", "one", "two"}, actuals.toArray(new String[0]));
    }
}