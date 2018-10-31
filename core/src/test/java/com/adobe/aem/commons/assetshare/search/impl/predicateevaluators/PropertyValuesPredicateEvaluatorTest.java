package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PropertyValuesPredicateEvaluatorTest {
    final PropertyValuesPredicateEvaluator propertyValuesPredicateEvaluator = new PropertyValuesPredicateEvaluator();

    private Predicate predicate;

    @Before
    public void setUp() throws Exception {
        predicate = new Predicate("testing");
    }

    @Test
    public void buildPredicate_DefaultDelimiter() {
        predicate.set("values", "foo, bar");
        predicate.set("1_values", "zip,zap");

        Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("foo", actual.get("0_value"));
        assertEquals("bar", actual.get("1_value"));
        assertEquals("zip", actual.get("2_value"));
        assertEquals("zap", actual.get("3_value"));
    }


    @Test
    public void buildPredicate_CustomDelimiter() {
        predicate.set("values", "foo?bar");
        predicate.set("delimiter", "?");

        Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("foo", actual.get("0_value"));
        assertEquals("bar", actual.get("1_value"));
    }


    @Test
    public void buildPredicate_MultiDelimiters() {
        predicate.set("values", "foo?bar, zip, zap");
        predicate.set("1_values", "cat,dog,bird?turtle horse,cow      chicken");
        predicate.set("delimiter", "?");
        predicate.set("1_delimiter", ",");
        predicate.set("2_delimiter", "\\s");

        Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("foo", actual.get("0_value"));
        assertEquals("bar", actual.get("1_value"));
        assertEquals("zip", actual.get("2_value"));
        assertEquals("zap", actual.get("3_value"));
        assertEquals("cat", actual.get("4_value"));
        assertEquals("dog", actual.get("5_value"));
        assertEquals("bird", actual.get("6_value"));
        assertEquals("turtle", actual.get("7_value"));
        assertEquals("horse", actual.get("8_value"));
        assertEquals("cow", actual.get("9_value"));
        assertEquals("chicken", actual.get("10_value"));
    }

    @Test
    public void getValues() {
        List<String> expected = new ArrayList<>();
        expected.add("one");
        expected.add("two");
        expected.add("three");

        List<String> delimiters = new ArrayList<>();
        delimiters.add(",");

        List<String> actual = propertyValuesPredicateEvaluator.getValues("one,two,three", delimiters);

        assertEquals(expected, actual);
    }

    @Test
    public void getValues_MultipleDelimiters() {
        List<String> expected = new ArrayList<>();
        expected.add("one");
        expected.add("two");
        expected.add("three");
        expected.add("four");
        expected.add("five");

        List<String> delimiters = new ArrayList<>();
        delimiters.add(",");
        delimiters.add("?");

        List<String> actual = propertyValuesPredicateEvaluator.getValues("one?two,three?four?,five", delimiters);

        assertEquals(expected, actual);
    }

    @Test
    public void getValues_WhitespaceDelimiters() {
        List<String> expected = new ArrayList<>();
        expected.add("one");
        expected.add("two");
        expected.add("three");
        expected.add("four");
        expected.add("five");

        List<String> delimiters = new ArrayList<>();
        delimiters.add("\\s");

        List<String> actual = propertyValuesPredicateEvaluator.getValues("one    two    three four      five", delimiters);

        assertEquals(expected, actual);
    }

    @Test
    public void getDelimiters() {
        predicate.set("delimiter", ",");
        predicate.set("0_delimiter", "?");
        predicate.set("1_delimiter", "/");
        predicate.set("2_delimiter", "\\");
        predicate.set("_delimiter", "no");
        predicate.set("incorrect", "no");
        predicate.set("1_incorrect", "no");


        List<String> actual = propertyValuesPredicateEvaluator.getDelimiters(predicate);
        assertEquals(4, actual.size());

        assertTrue(actual.contains(","));
        assertTrue(actual.contains("?"));
        assertTrue(actual.contains("/"));
        assertTrue(actual.contains("\\"));

        assertFalse(actual.contains("no"));
    }

    @Test
    public void getDelimiters_Default() {
        List<String> actual = propertyValuesPredicateEvaluator.getDelimiters(predicate);
        assertEquals(1, actual.size());

        assertTrue(actual.contains(","));
    }
}