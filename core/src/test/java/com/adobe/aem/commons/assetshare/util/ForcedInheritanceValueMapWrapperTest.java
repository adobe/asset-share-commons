/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

package com.adobe.aem.commons.assetshare.util;

import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ForcedInheritanceValueMapWrapperTest {

    @Rule
    public final AemContext ctx = new AemContext();

    private ForcedInheritanceValueMapWrapper wrapper;

    @Before
    public void setUp() {
        ctx.create().resource("/content/parent", "jcr:primaryType", "cq:Page");
        ctx.create().resource("/content/parent/jcr:content",
                "jcr:primaryType", "cq:PageContent",
                "title", "Parent Title",
                "inheritedProp", "parent-value");

        ctx.create().resource("/content/parent/child", "jcr:primaryType", "cq:Page");
        ctx.create().resource("/content/parent/child/jcr:content",
                "jcr:primaryType", "cq:PageContent",
                "title", "Child Title");

        final Resource childContentResource = ctx.resourceResolver().getResource("/content/parent/child/jcr:content");
        final InheritanceValueMap inheritanceValueMap = new HierarchyNodeInheritanceValueMap(childContentResource);
        wrapper = new ForcedInheritanceValueMapWrapper(inheritanceValueMap);
    }

    @Test
    public void get_withClass_isInherited() {
        assertEquals("parent-value", wrapper.get("inheritedProp", String.class));
    }

    @Test
    public void get_withDefaultValue_isInherited() {
        assertEquals("default-value", wrapper.get("does-not-exist-anywhere", "default-value"));
    }

    @Test
    public void get_withDefaultValue_foundOnCurrentResource() {
        assertEquals("Child Title", wrapper.get("title", "some-default"));
    }

    @Test
    public void plainMapGet_isNotInherited() {
        // Unlike get(name, Class)/get(name, default), the plain Map#get(Object) only ever exposes
        // the LOCAL (non-inherited) value of the resource the map was built from.
        assertEquals("Child Title", wrapper.get((Object) "title"));
        assertNull(wrapper.get((Object) "inheritedProp"));
    }

    @Test
    public void containsKey_onlyLocalProperties() {
        assertTrue(wrapper.containsKey("title"));
        assertFalse(wrapper.containsKey("inheritedProp"));
    }

    @Test
    public void containsValue_onlyLocalProperties() {
        assertTrue(wrapper.containsValue("Child Title"));
        assertFalse(wrapper.containsValue("parent-value"));
    }

    @Test
    public void size_isEmpty_keySet_values_entrySet_delegateToLocalValueMap() {
        assertFalse(wrapper.isEmpty());
        assertTrue(wrapper.size() > 0);
        assertTrue(wrapper.keySet().contains("title"));
        assertTrue(wrapper.values().contains("Child Title"));
        assertTrue(wrapper.entrySet().stream().anyMatch(e -> "title".equals(e.getKey()) && "Child Title".equals(e.getValue())));
    }

    @Test
    public void put_delegatesToInheritanceValueMap() {
        // The underlying InheritanceValueMap for a JCR-backed resource is a read-only view, so
        // mutating operations are expected to throw.
        try {
            wrapper.put("newKey", "newValue");
        } catch (UnsupportedOperationException expected) {
            return;
        }
        // If the underlying implementation ever allows mutation, at least confirm delegation worked.
        assertEquals("newValue", wrapper.get((Object) "newKey"));
    }

    @Test
    public void remove_delegatesToInheritanceValueMap() {
        try {
            wrapper.remove("title");
        } catch (UnsupportedOperationException expected) {
            return;
        }
        assertNull(wrapper.get((Object) "title"));
    }

    @Test
    public void putAll_delegatesToInheritanceValueMap() {
        final Map<String, Object> additions = new HashMap<>();
        additions.put("anotherKey", "anotherValue");

        try {
            wrapper.putAll(additions);
        } catch (UnsupportedOperationException expected) {
            return;
        }
        assertEquals("anotherValue", wrapper.get((Object) "anotherKey"));
    }

    @Test
    public void clear_delegatesToInheritanceValueMap() {
        try {
            wrapper.clear();
        } catch (UnsupportedOperationException expected) {
            return;
        }
        assertTrue(wrapper.isEmpty());
    }
}
