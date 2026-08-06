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

import com.adobe.aem.commons.assetshare.components.predicates.HiddenPredicate;
import com.day.cq.search.PredicateGroup;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HiddenPredicateImplTest {

    private static final String RESOURCE_PATH = "/content/hidden";

    @Rule
    public final AemContext ctx = new AemContext();

    private HiddenPredicate adapt(String path) {
        ctx.addModelsForClasses(HiddenPredicateImpl.class);
        ctx.currentResource(path);
        return ctx.request().adaptTo(HiddenPredicate.class);
    }

    @Test
    public void isReady_alwaysFalse() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        assertFalse(predicate.isReady());
    }

    @Test
    public void getPredicateGroup_noPredicatesChild() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        PredicateGroup group = predicate.getPredicateGroup();
        assertEquals(0, group.size());
    }

    @Test
    public void getPredicateGroup_withPredicates() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        ctx.create().resource(RESOURCE_PATH + "/predicates");
        ctx.create().resource(RESOURCE_PATH + "/predicates/item0", "predicate", "type", "value", "dam:Asset");
        ctx.create().resource(RESOURCE_PATH + "/predicates/item1", "predicate", "novalue");
        ctx.create().resource(RESOURCE_PATH + "/predicates/item2");

        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        PredicateGroup group = predicate.getPredicateGroup();

        assertEquals(2, group.size());
        assertEquals("dam:Asset", group.getByName("type").get("type"));
        assertEquals("", group.getByName("novalue").get("novalue"));
        // A predicate resource with a blank "predicate" property is skipped entirely.
        assertNull(group.getByName(""));
    }

    @Test
    public void getGroup_throwsUnsupportedOperationException() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        try {
            predicate.getGroup();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void getName_throwsUnsupportedOperationException() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        try {
            predicate.getName();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void getParams_deprecated_noPredicatesChild() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        Map<String, String> params = predicate.getParams(5);
        assertTrue(params.isEmpty());
    }

    @Test
    public void getParams_deprecated_withPredicates() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        ctx.create().resource(RESOURCE_PATH + "/predicates");
        ctx.create().resource(RESOURCE_PATH + "/predicates/item0", "predicate", "type", "value", "dam:Asset");

        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        Map<String, String> params = predicate.getParams(5);
        assertEquals("dam:Asset", params.get("5_group.type"));
    }

    @Test
    public void getExportedType() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        assertEquals(HiddenPredicateImpl.RESOURCE_TYPE, predicate.getExportedType());
    }

    @Test
    public void abstractPredicateDefaults() {
        ctx.create().resource(RESOURCE_PATH, "sling:resourceType", HiddenPredicateImpl.RESOURCE_TYPE);
        HiddenPredicate predicate = adapt(RESOURCE_PATH);

        // AbstractPredicate defaults, exercised via HiddenPredicateImpl (which doesn't override these).
        assertNull(predicate.getInitialValue());
        assertTrue(predicate.getInitialValues().isEmpty());
        assertFalse(predicate.isAutoSearch());
        assertEquals("", predicate.getComponentUpdateMethod());
    }
}
