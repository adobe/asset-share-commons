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

package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.FreeformTextPredicate;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FreeformTextPredicatePredicateImplTest {

    private static final String RESOURCE_PATH = "/content/freeform";

    @Rule
    public final AemContext ctx = new AemContext();

    private FreeformTextPredicate createImpl(Map<String, Object> props) {
        Map<String, Object> properties = new HashMap<>(props);
        properties.put("sling:resourceType", "asset-share-commons/components/search/freeform-text");
        ctx.create().resource(RESOURCE_PATH, properties);
        ctx.currentResource(RESOURCE_PATH);

        ctx.addModelsForClasses(FreeformTextPredicatePredicateImpl.class);
        return ctx.request().adaptTo(FreeformTextPredicate.class);
    }

    @Test
    public void getTitle() {
        Map<String, Object> props = new HashMap<>();
        props.put("jcr:title", "My Title");

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals("My Title", predicate.getTitle());
    }

    @Test
    public void getPlaceholder() {
        Map<String, Object> props = new HashMap<>();
        props.put("placeholder", "Enter text...");

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals("Enter text...", predicate.getPlaceholder());
    }

    @Test
    public void getName_isPropertyValuesPredicateName() {
        FreeformTextPredicate predicate = createImpl(new HashMap<>());

        assertEquals(PropertyValuesPredicateEvaluator.PREDICATE_NAME, predicate.getName());
    }

    @Test
    public void getRows_defaultsToOne() {
        FreeformTextPredicate predicate = createImpl(new HashMap<>());

        assertEquals(1, predicate.getRows());
    }

    @Test
    public void getRows_custom() {
        Map<String, Object> props = new HashMap<>();
        props.put("rows", 5);

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals(5, predicate.getRows());
    }

    @Test
    public void getOperation_andHasOperation() {
        Map<String, Object> props = new HashMap<>();
        props.put("operation", "like");

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals("like", predicate.getOperation());
        assertTrue(predicate.hasOperation());
    }

    @Test
    public void getProperty() {
        Map<String, Object> props = new HashMap<>();
        props.put("property", "jcr:content/metadata/dc:description");

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals("jcr:content/metadata/dc:description", predicate.getProperty());
    }

    @Test
    public void getInputValidationMinLength_startsWithOperation_appliesMagicMinimum() {
        Map<String, Object> props = new HashMap<>();
        props.put("operation", "startsWith");

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals(Integer.valueOf(3), predicate.getInputValidationMinLength());
    }

    @Test
    public void getInputValidationMinLength_startsWithOperation_respectsHigherConfiguredValue() {
        Map<String, Object> props = new HashMap<>();
        props.put("operation", "startsWith");
        props.put("inputValidationMinLength", 5);

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals(Integer.valueOf(5), predicate.getInputValidationMinLength());
    }

    @Test
    public void getInputValidationMinLength_otherOperation_usesConfiguredValue() {
        Map<String, Object> props = new HashMap<>();
        props.put("operation", "equals");
        props.put("inputValidationMinLength", 1);

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals(Integer.valueOf(1), predicate.getInputValidationMinLength());
    }

    @Test
    public void getInputValidationMaxLength() {
        Map<String, Object> props = new HashMap<>();
        props.put("inputValidationMaxLength", 100);

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals(Integer.valueOf(100), predicate.getInputValidationMaxLength());
    }

    @Test
    public void getInputValidationPattern_stripsToNull() {
        Map<String, Object> props = new HashMap<>();
        props.put("inputValidationPattern", "   ");

        FreeformTextPredicate predicate = createImpl(props);

        assertNull(predicate.getInputValidationPattern());
    }

    @Test
    public void getInputValidationPattern_returnsValue() {
        Map<String, Object> props = new HashMap<>();
        props.put("inputValidationPattern", "[a-z]+");

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals("[a-z]+", predicate.getInputValidationPattern());
    }

    @Test
    public void getInputValidationMessage() {
        Map<String, Object> props = new HashMap<>();
        props.put("inputValidationMessage", "Invalid input");

        FreeformTextPredicate predicate = createImpl(props);

        assertEquals("Invalid input", predicate.getInputValidationMessage());
    }

    @Test
    public void getDelimiters_noDelimitersResource_isEmpty() {
        FreeformTextPredicate predicate = createImpl(new HashMap<>());

        assertTrue(predicate.getDelimiters().isEmpty());
    }

    @Test
    public void getDelimiters_withConfiguredValues() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", "asset-share-commons/components/search/freeform-text");
        ctx.create().resource(RESOURCE_PATH + "/delimiters");
        ctx.create().resource(RESOURCE_PATH + "/delimiters/comma", "value", ",");
        ctx.create().resource(RESOURCE_PATH + "/delimiters/custom", "value", "__CUSTOM_DELIMITER", "customValue", "|");
        ctx.currentResource(RESOURCE_PATH);
        ctx.addModelsForClasses(FreeformTextPredicatePredicateImpl.class);

        FreeformTextPredicate predicate = ctx.request().adaptTo(FreeformTextPredicate.class);

        List<String> delimiters = predicate.getDelimiters();
        assertEquals(2, delimiters.size());
        assertTrue(delimiters.contains(","));
        assertTrue(delimiters.contains("|"));
    }

    @Test
    public void isReady_alwaysTrue() {
        FreeformTextPredicate predicate = createImpl(new HashMap<>());

        assertTrue(predicate.isReady());
    }

    @Test
    public void getInitialValue_noParam() {
        FreeformTextPredicate predicate = createImpl(new HashMap<>());

        assertEquals("", predicate.getInitialValue());
    }

    @Test
    public void getExportedType() {
        FreeformTextPredicate predicate = createImpl(new HashMap<>());

        assertEquals("asset-share-commons/components/search/freeform-text", predicate.getExportedType());
    }
}
