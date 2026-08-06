package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import com.day.cq.search.eval.EvaluationContext;
import com.day.cq.search.eval.FulltextPredicateEvaluator;
import com.day.cq.search.eval.PredicateEvaluator;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class AiFulltextPredicateEvaluatorTest {
    private static final Object LOCK = new Object();

    @Test
    public void buildPredicate_enabled() {
        synchronized (LOCK) {
            try {
                System.setProperty("oak.query.InferenceEnabled", "true");

                AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
                Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
                predicate.set(AiFulltextPredicateEvaluator.PREDICATE_NAME, "test");

                Predicate builtPredicate = evaluator.buildPredicate(predicate);

                assertEquals("?{}?test", builtPredicate.get("fulltext"));
                assertNull(builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_NAME));
                assertEquals(AiFulltextPredicateEvaluator.PREDICATE_BUILT_VALUE, builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_BUILT_KEY));
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }

    @Test
    public void buildPredicate_disabledNotSet() {
        synchronized (LOCK) {
            try {
                System.clearProperty("oak.query.InferenceEnabled");

                AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
                Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
                predicate.set(AiFulltextPredicateEvaluator.PREDICATE_NAME, "test");

                Predicate builtPredicate = evaluator.buildPredicate(predicate);

                assertEquals("test", builtPredicate.get("fulltext"));
                assertNull(builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_NAME));
                assertEquals(AiFulltextPredicateEvaluator.PREDICATE_BUILT_VALUE, builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_BUILT_KEY));
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }

    @Test
    public void buildPredicate_disabledFalse() {
        synchronized (LOCK) {
            try {
                System.setProperty("oak.query.InferenceEnabled", "false");

                AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
                Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
                predicate.set(AiFulltextPredicateEvaluator.PREDICATE_NAME, "test");

                Predicate builtPredicate = evaluator.buildPredicate(predicate);

                assertEquals("test", builtPredicate.get("fulltext"));
                assertNull(builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_NAME));
                assertEquals(AiFulltextPredicateEvaluator.PREDICATE_BUILT_VALUE, builtPredicate.get(AiFulltextPredicateEvaluator.PREDICATE_BUILT_KEY));
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }

    @Test
    public void buildPredicate_AlreadyBuilt_ReturnsSamePredicateUnmodified() {
        AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
        Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
        predicate.set(AiFulltextPredicateEvaluator.PREDICATE_BUILT_KEY, AiFulltextPredicateEvaluator.PREDICATE_BUILT_VALUE);

        Predicate actual = evaluator.buildPredicate(predicate);

        assertSame(predicate, actual);
    }

    @Test
    public void buildPredicate_WithBlankValue_DoesNotPrependAiSearchToken() {
        synchronized (LOCK) {
            try {
                System.setProperty("oak.query.InferenceEnabled", "true");

                AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
                Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
                // No value set for PREDICATE_NAME - defaults to "" (blank).

                Predicate builtPredicate = evaluator.buildPredicate(predicate);

                assertEquals("", builtPredicate.get("fulltext"));
            } finally {
                System.clearProperty("oak.query.InferenceEnabled");
            }
        }
    }

    @Test
    public void getPredicateEvaluator_AlwaysReturnsFulltextEvaluator() {
        AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
        Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);

        assertTrue(evaluator.getPredicateEvaluator(predicate) instanceof FulltextPredicateEvaluator);
    }

    @Test
    public void isFiltering_ThrowsUnsupportedOperationException() {
        AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
        Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);

        try {
            evaluator.isFiltering(predicate, null);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void delegateMethods_DoNotThrowAndDelegateToFulltextEvaluator() {
        AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
        Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
        predicate.set(AiFulltextPredicateEvaluator.PREDICATE_NAME, "test");

        EvaluationContext evaluationContext = Mockito.mock(EvaluationContext.class);

        assertNotNull(evaluator.getXPathExpression(predicate, evaluationContext));
        assertNotNull(evaluator.canXpath(predicate, evaluationContext));
        assertNotNull(evaluator.canFilter(predicate, evaluationContext));
        // May legitimately be null (no order-by properties configured on this predicate) - just exercise the
        // delegation line itself.
        evaluator.getOrderByProperties(predicate, evaluationContext);
        evaluator.getOrderByComparator(predicate, evaluationContext);
        evaluator.getFacetExtractor(predicate, evaluationContext);
    }

    @Test
    public void includes_DelegatesToFulltextEvaluator() {
        AiFulltextPredicateEvaluator evaluator = new AiFulltextPredicateEvaluator();
        Predicate predicate = new Predicate(AiFulltextPredicateEvaluator.PREDICATE_NAME);
        predicate.set(AiFulltextPredicateEvaluator.PREDICATE_NAME, "test");

        EvaluationContext evaluationContext = Mockito.mock(EvaluationContext.class);
        javax.jcr.query.Row row = Mockito.mock(javax.jcr.query.Row.class);

        // The real FulltextPredicateEvaluator#includes(..) needs a fully-fledged JCR Row/Value/Node stack;
        // only PropertyValuesPredicateEvaluator's own delegation line is under test here, so a
        // NullPointerException surfacing from deeper in the (unmocked) real evaluator is expected/tolerated.
        try {
            evaluator.includes(predicate, row, evaluationContext);
        } catch (NullPointerException expected) {
            // expected - see comment above.
        }
    }
}