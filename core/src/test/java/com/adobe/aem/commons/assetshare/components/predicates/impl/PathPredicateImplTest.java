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
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.SelectedOptionItem;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.UnselectedOptionItem;
import com.adobe.aem.commons.assetshare.testing.ReflectionTestUtil;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.eval.PathPredicateEvaluator;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PathPredicateImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    private OptionItem optionItem(String text, String value) {
        OptionItem item = mock(OptionItem.class);
        when(item.getText()).thenReturn(text);
        when(item.getValue()).thenReturn(value);
        return item;
    }

    private PathPredicateImpl createImpl(String path, Map<String, Object> props, Options coreOptions) {
        Resource resource = ctx.resourceResolver().getResource(path);
        if (resource == null) {
            resource = ctx.create().resource(path, props);
        }
        ctx.currentResource(path);

        final PathPredicateImpl impl = new PathPredicateImpl();
        ReflectionTestUtil.setField(impl, AbstractPredicate.class, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "resource", resource);
        ReflectionTestUtil.setField(impl, "coreOptions", coreOptions);
        ReflectionTestUtil.setField(impl, "label", props.get("label"));
        ReflectionTestUtil.setField(impl, "operation", props.get("operation"));
        ReflectionTestUtil.setField(impl, "expanded", props.get("expanded"));
        ReflectionTestUtil.setField(impl, "typeString", props.get("type"));
        impl.init();

        return impl;
    }

    @Test
    public void getName_isPath() {
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PathPredicateImpl impl = createImpl("/content/path1", new HashMap<>(), coreOptions);

        assertEquals(PathPredicateEvaluator.PATH, impl.getName());
    }

    @Test
    public void getSubType() {
        Map<String, Object> props = new HashMap<>();
        props.put("type", "checkbox-group");
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PathPredicateImpl impl = createImpl("/content/path2", props, coreOptions);

        assertEquals("checkbox-group", impl.getSubType());
    }

    @Test
    public void isReady_falseWhenNoItems() {
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PathPredicateImpl impl = createImpl("/content/path3", new HashMap<>(), coreOptions);

        assertFalse(impl.isReady());
    }

    @Test
    public void isReady_trueWhenItemsPresent() {
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Item", "/content/dam/whatever"));
        when(coreOptions.getItems()).thenReturn(items);

        PathPredicateImpl impl = createImpl("/content/path4", new HashMap<>(), coreOptions);

        assertTrue(impl.isReady());
    }

    @Test
    public void getItems_filtersOutNonExistentResources() {
        ctx.create().resource("/content/dam/real-folder");

        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Real", "/content/dam/real-folder"));
        items.add(optionItem("Fake", "/content/dam/does-not-exist"));
        when(coreOptions.getItems()).thenReturn(items);

        PathPredicateImpl impl = createImpl("/content/path5", new HashMap<>(), coreOptions);

        List<OptionItem> result = impl.getItems();
        assertEquals(1, result.size());
        assertEquals("/content/dam/real-folder", result.get(0).getValue());
    }

    @Test
    public void getItems_selectedWhenInQueryParams() {
        ctx.create().resource("/content/dam/real-folder");

        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Real", "/content/dam/real-folder"));
        when(coreOptions.getItems()).thenReturn(items);

        PathPredicateImpl impl = createImpl("/content/path6", new HashMap<>(), coreOptions);
        ctx.request().setQueryString(impl.getGroup() + ".path.0_path=%2Fcontent%2Fdam%2Freal-folder");

        List<OptionItem> result = impl.getItems();
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof SelectedOptionItem);
    }

    @Test
    public void getItems_unselectedWhenParameterizedAndNotSelected() {
        ctx.create().resource("/content/dam/real-folder");

        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Real", "/content/dam/real-folder"));
        when(coreOptions.getItems()).thenReturn(items);

        PathPredicateImpl impl = createImpl("/content/path7", new HashMap<>(), coreOptions);
        ctx.request().setQueryString("foo=bar&p.limit=10");

        List<OptionItem> result = impl.getItems();
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof UnselectedOptionItem);
    }

    @Test
    public void getPredicateGroup_noResource() {
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PathPredicateImpl impl = createImpl("/content/path8", new HashMap<>(), coreOptions);
        ReflectionTestUtil.setField(impl, "resource", null);

        PredicateGroup group = impl.getPredicateGroup();
        assertEquals(0, group.size());
    }

    @Test
    public void getPredicateGroup_noSelectedItems_isEmpty() {
        ctx.create().resource("/content/dam/real-folder");
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Real", "/content/dam/real-folder"));
        when(coreOptions.getItems()).thenReturn(items);

        PathPredicateImpl impl = createImpl("/content/path9", new HashMap<>(), coreOptions);

        PredicateGroup group = impl.getPredicateGroup();
        assertEquals(0, group.size());
    }

    @Test
    public void getPredicateGroup_withSelectedItems_includesPOr() {
        ctx.create().resource("/content/dam/real-folder");
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Real", "/content/dam/real-folder"));
        when(coreOptions.getItems()).thenReturn(items);

        PathPredicateImpl impl = createImpl("/content/path10", new HashMap<>(), coreOptions);
        ctx.request().setQueryString(impl.getGroup() + ".path.0_path=%2Fcontent%2Fdam%2Freal-folder");

        PredicateGroup group = impl.getPredicateGroup();
        // "p.or" is a QueryBuilder group-level directive (not a child predicate), so it manifests as
        // the group itself being OR'd (allRequired == false) rather than a literal "p.or" list entry.
        assertEquals(1, group.size());
        assertFalse(group.allRequired());
        assertEquals("/content/dam/real-folder", group.get(0).get("path"));
    }

    @Test
    public void getExportedType() {
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PathPredicateImpl impl = createImpl("/content/path11", new HashMap<>(), coreOptions);

        assertEquals("asset-share-commons/components/search/path", impl.getExportedType());
    }
}
