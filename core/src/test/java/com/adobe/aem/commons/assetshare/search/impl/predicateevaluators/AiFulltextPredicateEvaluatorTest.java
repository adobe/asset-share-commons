package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AiFulltextPredicateEvaluatorTest {
    private static final Object LOCK = new Object();

    @Before
    public void setUp() throws Exception {
    }


    @After
    public void tearDown() throws Exception {
        System.clearProperty("oak.query.InferenceEnabled");
    }

    @Test
    public void buildPredicate_enabled() {
        synchronized (LOCK) {
            System.setProperty("oak.query.InferenceEnabled", "true");

            AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
            Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
            predicate.set(AiFulltextPredicateEvaluator.PREDICATE_NAME, "test");

            Predicate builtPredicate = evaluator.buildPredicate(predicate);

            assertEquals("?{}?test", builtPredicate.get("fulltext"));
            assertNull(builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_NAME));
            assertEquals(AiFulltextPredicateEvaluator.PREDICATE_BUILT_VALUE, builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_BUILT_KEY));
        }
    }

    @Test
    public void buildPredicate_disabledNotSet() {
        synchronized (LOCK) {
            System.clearProperty("oak.query.InferenceEnabled");

            AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
            Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
            predicate.set(AiFulltextPredicateEvaluator.PREDICATE_NAME, "test");

            Predicate builtPredicate = evaluator.buildPredicate(predicate);

            assertEquals("test", builtPredicate.get("fulltext"));
            assertNull(builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_NAME));
            assertEquals(AiFulltextPredicateEvaluator.PREDICATE_BUILT_VALUE, builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_BUILT_KEY));
        }
    }

    @Test
    public void buildPredicate_disabledFalse() {
        synchronized (LOCK) {
            System.setProperty("oak.query.InferenceEnabled", "false");

            AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
            Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
            predicate.set(AiFulltextPredicateEvaluator.PREDICATE_NAME, "test");

            Predicate builtPredicate = evaluator.buildPredicate(predicate);

            assertEquals("test", builtPredicate.get("fulltext"));
            assertNull(builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_NAME));
            assertEquals(AiFulltextPredicateEvaluator.PREDICATE_BUILT_VALUE, builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_BUILT_KEY));
        }
    }
}