/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import com.day.cq.search.eval.FulltextPredicateEvaluator;
import com.day.cq.search.eval.JcrPropertyPredicateEvaluator;
import com.day.cq.search.eval.PredicateEvaluator;
import com.google.common.collect.ImmutableList;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValuesPredicateEvaluatorTest {

    private PropertyValuesPredicateEvaluator propertyValuesPredicateEvaluator;

    private Predicate predicate;

    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() {
        predicate = new Predicate("test", "propertyvalues");

        ctx.registerInjectActivateService(new PropertyValuesPredicateEvaluator());

        propertyValuesPredicateEvaluator = (PropertyValuesPredicateEvaluator) ctx.getService(PredicateEvaluator.class);
    }


    @Test
    public void buildPredicate_WithAlreadyBuiltPredicate() {
        predicate.set("__asset-share-commons--predicate-built", "true");

        final Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertSame(predicate, actual);
        assertEquals(predicate, actual);
    }

    @Test
    public void buildPredicate_WithNoneDelimiter() {
        final Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put(JcrPropertyPredicateEvaluator.OPERATION, JcrPropertyPredicateEvaluator.OP_EQUALS);
        expectedParams.put(JcrPropertyPredicateEvaluator.PROPERTY, "jcr:content/metadata/property");
        expectedParams.put("0_" + JcrPropertyPredicateEvaluator.VALUE, "foo bar");
        expectedParams.put("1_" + JcrPropertyPredicateEvaluator.VALUE, "zip zap");
        expectedParams.put("__asset-share-commons--predicate-built", "true");
        expectedParams.put("values", null);
        expectedParams.put("1_values", null);
        expectedParams.put("delimiter", null);

        predicate.set("operation", "equals");
        predicate.set("property", "jcr:content/metadata/property");
        predicate.set("values", "foo bar");
        predicate.set("1_values", "zip zap");
        predicate.set("delimiter", "_NONE");

        final Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("propertyvalues", actual.getType());
        assertEquals(expectedParams, actual.getParameters());
        assertEquals("test", actual.getName());
    }

    @Test
    public void buildPredicate_AsPropertyOperation() {
        final Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put(JcrPropertyPredicateEvaluator.OPERATION, JcrPropertyPredicateEvaluator.OP_EQUALS);
        expectedParams.put(JcrPropertyPredicateEvaluator.PROPERTY, "jcr:content/metadata/property");
        expectedParams.put("0_" + JcrPropertyPredicateEvaluator.VALUE, "foo");
        expectedParams.put("1_" + JcrPropertyPredicateEvaluator.VALUE, "bar");
        expectedParams.put("2_" + JcrPropertyPredicateEvaluator.VALUE, "zip");
        expectedParams.put("3_" + JcrPropertyPredicateEvaluator.VALUE, "zap");
        expectedParams.put("__asset-share-commons--predicate-built", "true");
        expectedParams.put("operation", "equals");
        expectedParams.put("values", null);
        expectedParams.put("1_values", null);
        expectedParams.put("99_delimiter", null);

        predicate.set("operation", "equals");
        predicate.set("property", "jcr:content/metadata/property");
        predicate.set("values", "foo bar");
        predicate.set("1_values", "zip zap");
        predicate.set("99_delimiter", "__WS");

        final Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("propertyvalues", actual.getType());
        assertEquals(expectedParams, actual.getParameters());
        assertEquals("test", actual.getName());
    }

    @Test
    public void buildPredicate_AsContainsOperation() {
        final Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put(FulltextPredicateEvaluator.FULLTEXT, "*foo* OR *bar*");
        expectedParams.put(FulltextPredicateEvaluator.REL_PATH, "jcr:content/metadata/@property");
        expectedParams.put("__asset-share-commons--predicate-built", "true");
        expectedParams.put("values", null);
        expectedParams.put("property", null);
        expectedParams.put("operation", null);

        predicate.set("operation", "contains");
        predicate.set("property", "jcr:content/metadata/property");
        predicate.set("values", "foo, bar");

        final Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("propertyvalues", actual.getType());
        assertEquals(expectedParams, actual.getParameters());
        assertEquals("test", actual.getName());
    }

    @Test
    public void buildPredicate_AsStartsWithOperation() {
        final Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put(FulltextPredicateEvaluator.FULLTEXT, "foo* OR bar* OR zip* OR zap*");
        expectedParams.put(FulltextPredicateEvaluator.REL_PATH, "jcr:content/metadata/@property");
        expectedParams.put("__asset-share-commons--predicate-built", "true");
        expectedParams.put("values", null);
        expectedParams.put("property", null);
        expectedParams.put("operation", null);
        expectedParams.put("1_values", null);

        predicate.set("operation", "startsWith");
        predicate.set("property", "jcr:content/metadata/property");
        predicate.set("values", "foo, bar");
        predicate.set("1_values", "zip, zap");

        final Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("propertyvalues", actual.getType());
        assertEquals(expectedParams, actual.getParameters());
        assertEquals("test", actual.getName());
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
        predicate.set("2_delimiter", "__WS");

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
    public void buildPredicate_WhitespaceDelimiters() {
        predicate.set("values", "foo" + System.lineSeparator() + "bar zip   zap");
        predicate.set("delimiter", "__WS");

        Predicate actual = propertyValuesPredicateEvaluator.buildPredicate(predicate);

        assertEquals("foo", actual.get("0_value"));
        assertEquals("bar", actual.get("1_value"));
        assertEquals("zip", actual.get("2_value"));
        assertEquals("zap", actual.get("3_value"));
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
        delimiters.add("\\?");

        List<String> actual = propertyValuesPredicateEvaluator.getValues("one?two,three?four?,five", delimiters);

        assertEquals(expected, actual);
    }

    @Test
    public void getValues_WhitespaceDelimiters() {
        List<String> expected = ImmutableList.of("one", "two", "three", "four", "five");

        List<String> delimiters = new ArrayList<>();
        delimiters.add("\\s");

        List<String> actual = propertyValuesPredicateEvaluator.getValues("one    two  " + System.lineSeparator()  + "  three four      five", delimiters);

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

        assertTrue(actual.contains(Pattern.quote(",")));
        assertTrue(actual.contains(Pattern.quote("?")));
        assertTrue(actual.contains(Pattern.quote("/")));
        assertTrue(actual.contains(Pattern.quote("\\")));

        assertFalse(actual.contains("no"));
    }

    @Test
    public void getDelimiters_Default() {
        List<String> actual = propertyValuesPredicateEvaluator.getDelimiters(predicate);
        assertEquals(1, actual.size());

        assertTrue(actual.contains(Pattern.quote(",")));
    }

    @Test
    public void getPredicateEvaluator_AsPropertyPredicate() {
        predicate.set("operation", "equals");
        PredicateEvaluator actual = propertyValuesPredicateEvaluator.getPredicateEvaluator(predicate);

        assertTrue(actual instanceof JcrPropertyPredicateEvaluator);
    }

    @Test
    public void getPredicateEvaluator_AsFulltextPredicate() {
        predicate.set("operation", "startsWith");
        PredicateEvaluator actual = propertyValuesPredicateEvaluator.getPredicateEvaluator(predicate);

        assertTrue(actual instanceof FulltextPredicateEvaluator);

        predicate.set("operation", "contains");
        actual = propertyValuesPredicateEvaluator.getPredicateEvaluator(predicate);

        assertTrue(actual instanceof FulltextPredicateEvaluator);
    }
}