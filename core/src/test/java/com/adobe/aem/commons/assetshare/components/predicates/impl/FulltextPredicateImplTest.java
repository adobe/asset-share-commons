/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

import com.adobe.aem.commons.assetshare.components.predicates.AbstractPredicate;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.AiFulltextPredicateEvaluator;
import com.adobe.aem.commons.assetshare.testing.ReflectionTestUtil;
import com.adobe.cq.wcm.core.components.models.form.Text;
import com.day.cq.search.eval.FulltextPredicateEvaluator;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FulltextPredicateImplTest {

    private static final String RESOURCE_PATH = "/content/search/search-bar";

    @Rule
    public final AemContext ctx = new AemContext();

    private Text coreText;

    @Before
    public void setUp() {
        coreText = mock(Text.class);
        when(coreText.isRequired()).thenReturn(true);
        when(coreText.getRequiredMessage()).thenReturn("Required!");
        when(coreText.getPlaceholder()).thenReturn("Search...");
        when(coreText.isReadOnly()).thenReturn(false);
        when(coreText.getConstraintMessage()).thenReturn("Invalid");
        when(coreText.hideTitle()).thenReturn(true);
    }

    private FulltextPredicateImpl createImpl(Map<String, Object> props) {
        Map<String, Object> properties = new HashMap<>(props);
        properties.put("sling:resourceType", "asset-share-commons/components/search/search-bar");
        ctx.create().resource(RESOURCE_PATH, properties);
        ctx.currentResource(RESOURCE_PATH);

        final FulltextPredicateImpl impl = new FulltextPredicateImpl();
        ReflectionTestUtil.setField(impl, AbstractPredicate.class, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "coreText", coreText);
        ReflectionTestUtil.setField(impl, "aiSearch", props.getOrDefault("aiSearch", false));
        impl.init();

        return impl;
    }

    @Test
    public void getName_defaultFulltext() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals(FulltextPredicateEvaluator.FULLTEXT, predicate.getName());
    }

    @Test
    public void getName_aiSearch() {
        Map<String, Object> props = new HashMap<>();
        props.put("aiSearch", true);
        FulltextPredicateImpl predicate = createImpl(props);

        assertEquals(AiFulltextPredicateEvaluator.PREDICATE_NAME, predicate.getName());
    }

    @Test
    public void coreTextDelegation() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());

        assertTrue(predicate.isRequired());
        assertEquals("Required!", predicate.getRequiredMessage());
        assertEquals("Search...", predicate.getPlaceholder());
        assertFalse(predicate.isReadOnly());
        assertEquals("Invalid", predicate.getConstraintMessage());
        assertTrue(predicate.hideTitle());
    }

    @Test
    public void getType_isText() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals("text", predicate.getType());
    }

    @Test
    public void getRows_isZero() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals(0, predicate.getRows());
    }

    @Test
    public void isReady_alwaysTrue() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());

        assertTrue(predicate.isReady());
    }

    @Test
    public void getInitialValue_noParam() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals("", predicate.getInitialValue());
    }

    @Test
    public void getInitialValue_withParam() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("fulltext=hello+world");

        assertEquals("hello world", predicate.getInitialValue());
    }

    @Test
    public void getInitialValues_containsNameAndValue() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());
        ctx.request().setQueryString("fulltext=hello");

        ValueMap initialValues = predicate.getInitialValues();

        assertEquals("hello", initialValues.get(predicate.getName(), String.class));
    }

    @Test
    public void getExportedType() {
        FulltextPredicateImpl predicate = createImpl(new HashMap<>());

        assertEquals("asset-share-commons/components/search/search-bar", predicate.getExportedType());
    }
}
