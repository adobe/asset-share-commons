package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PredicateEvaluatorUtilTest {

    @Test
    public void getValues() {
        Predicate predicate = new Predicate("testing");

        predicate.set("test", "zero");
        predicate.set("1_test", "one");
        predicate.set("2_test", "two");
        predicate.set("_test", "no");
        predicate.set("incorrect", "no");
        predicate.set("1_incorrect", "no");

        final List<String> actuals = PredicateEvaluatorUtil.getValues(predicate, "test");

        assertEquals(actuals.size(), 3);

        assertTrue(actuals.contains("zero"));
        assertTrue(actuals.contains("one"));
        assertTrue(actuals.contains("two"));
    }


    @Test
    public void getValues_nullify() {
        Predicate predicate = new Predicate("testing");

        predicate.set("test", "zero");
        predicate.set("1_test", "one");
        predicate.set("2_test", "two");
        predicate.set("_test", "no");
        predicate.set("incorrect", "no");
        predicate.set("1_incorrect", "no");

        final List<String> actuals = PredicateEvaluatorUtil.getValues(predicate, "test", true);

        assertEquals(actuals.size(), 3);

        assertTrue(actuals.contains("zero"));
        assertTrue(actuals.contains("one"));
        assertTrue(actuals.contains("two"));

        assertNull(predicate.get("test"));
        assertNull(predicate.get("1_test"));
        assertNull(predicate.get("2_test"));

        assertNotNull(predicate.get("_test"));
        assertNotNull(predicate.get("incorrect"));
        assertNotNull(predicate.get("1_incorrect"));

    }
}