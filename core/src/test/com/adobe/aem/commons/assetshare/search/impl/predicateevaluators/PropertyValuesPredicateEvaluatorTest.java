package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertyValuesPredicateEvaluatorTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    Predicate predicate;

    PropertyValuesPredicateEvaluator propertyValuesPredicateEvaluator = new PropertyValuesPredicateEvaluator();


    @Before
    public void setUp() throws Exception {
        // Base predicate set up
        when(predicate.get("delimiter")).thenReturn(null);
        when(predicate.get("delimiters")).thenReturn(null);

        when(predicate.get("propertyvalues")).thenReturn("prop1");
        when(predicate.get("propertyvalues.property")).thenReturn("prop1");

        when(predicate.get("values")).thenReturn("value1,value2");
    }


    @Test
    public void buildPredicate_noDelimiter_legacy() {

        when(predicate.get("delimiter")).thenReturn(null);
        when(predicate.get("delimiters")).thenReturn(null);

        when(predicate.get("propertyvalues")).thenReturn("prop1");
        when(predicate.get("value")).thenReturn("test value");

        Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("prop1", actual.get("test value"));
    }

    @Test
    public void buildPredicate_legacyDelimiter() {
        Predicate predicate = mock(Predicate.class);
        when(predicate.get("delimiter")).thenReturn(null);
        when(predicate.get("propertyvalues")).thenReturn("prop1,prop2");
        when(predicate.get("value")).thenReturn("test value");

        Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("prop1,prop2", actual.get("test value"));
    }

    @Test
    public void getXPathExpression() {
    }

    @Test
    public void includes() {
    }

    @Test
    public void canXpath() {
    }

    @Test
    public void canFilter() {
    }

    @Test
    public void isFiltering() {
    }

    @Test
    public void getOrderByProperties() {
    }

    @Test
    public void getOrderByComparator() {
    }

    @Test
    public void getFacetExtractor() {
    }
}