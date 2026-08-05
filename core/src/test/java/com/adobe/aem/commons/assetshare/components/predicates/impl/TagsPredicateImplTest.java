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
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import com.adobe.aem.commons.assetshare.testing.ReflectionTestUtil;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TagsPredicateImplTest {

    private static final String RESOURCE_PATH = "/content/tags";

    @Rule
    public final AemContext ctx = new AemContext();

    private TagManager tagManager;

    @Before
    public void setUp() {
        tagManager = mock(TagManager.class);
        ctx.registerAdapter(ResourceResolver.class, TagManager.class, tagManager);

        Config config = mock(Config.class);
        when(config.getLocale()).thenReturn(Locale.US);
        ctx.registerAdapter(org.apache.sling.api.SlingHttpServletRequest.class, Config.class, config);
    }

    private Tag tag(String id, String title) {
        Tag tag = mock(Tag.class);
        when(tag.getTagID()).thenReturn(id);
        when(tag.getTitle(Locale.US)).thenReturn(title);
        return tag;
    }

    private TagsPredicateImpl createImpl(Map<String, Object> props, Options coreOptions) {
        Map<String, Object> properties = new HashMap<>(props);
        properties.put("sling:resourceType", "asset-share-commons/components/search/tags");
        ctx.create().resource(RESOURCE_PATH, properties);
        ctx.currentResource(RESOURCE_PATH);

        final TagsPredicateImpl impl = new TagsPredicateImpl();
        ReflectionTestUtil.setField(impl, AbstractPredicate.class, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "coreOptions", coreOptions);
        ReflectionTestUtil.setField(impl, "typeString", props.get("type"));
        ReflectionTestUtil.setField(impl, "property", props.get("property"));
        ReflectionTestUtil.setField(impl, "and", props.getOrDefault("and", false));
        ReflectionTestUtil.setField(impl, "displayOrder", props.getOrDefault("displayOrder", "natural"));
        ReflectionTestUtil.setField(impl, "operation", props.getOrDefault("operation", "equals"));
        impl.init();

        return impl;
    }

    @Test
    public void getType_delegatesToCoreOptions() {
        Options coreOptions = mock(Options.class);
        when(coreOptions.getType()).thenReturn(Options.Type.CHECKBOX);
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), coreOptions);

        assertEquals(Options.Type.CHECKBOX, impl.getType());
    }

    @Test
    public void getSubType() {
        Map<String, Object> props = new HashMap<>();
        props.put("type", "checkbox-group");
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(props, mock(Options.class));

        assertEquals("checkbox-group", impl.getSubType());
    }

    @Test
    public void getProperty_defaultsToCqTags() {
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));

        assertEquals("jcr:content/metadata/cq:tags", impl.getProperty());
    }

    @Test
    public void getProperty_customValue() {
        Map<String, Object> props = new HashMap<>();
        props.put("property", "jcr:content/metadata/customTags");
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(props, mock(Options.class));

        assertEquals("jcr:content/metadata/customTags", impl.getProperty());
    }

    @Test
    public void getName_andGetValuesKey() {
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));

        assertEquals(PropertyValuesPredicateEvaluator.PREDICATE_NAME, impl.getName());
        assertEquals(PropertyValuesPredicateEvaluator.VALUES, impl.getValuesKey());
    }

    @Test
    public void hasOperation_andGetOperation() {
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));

        assertTrue(impl.hasOperation());
        assertEquals("equals", impl.getOperation());
    }

    @Test
    public void hasAnd_andGetAnd() {
        Map<String, Object> props = new HashMap<>();
        props.put("and", true);
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(props, mock(Options.class));

        assertTrue(impl.hasAnd());
        assertEquals(Boolean.TRUE, impl.getAnd());
    }

    @Test
    public void isReady_falseWhenNoTags() {
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));

        assertFalse(impl.isReady());
    }

    @Test
    public void isReady_trueWithTags() {
        Tag tagA = tag("ns:a", "A");
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{tagA});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));

        assertTrue(impl.isReady());
    }

    @Test
    public void getItems_naturalOrder() {
        Tag bravo = tag("ns:b", "Bravo");
        Tag alpha = tag("ns:a", "Alpha");
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new Tag[]{bravo, alpha});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));

        assertEquals(2, impl.getItems().size());
        assertEquals("Bravo", impl.getItems().get(0).getText());
        assertEquals("Alpha", impl.getItems().get(1).getText());
    }

    @Test
    public void getItems_alphabeticalOrder() {
        Map<String, Object> props = new HashMap<>();
        props.put("displayOrder", "alphabetical");
        Tag bravo = tag("ns:b", "Bravo");
        Tag alpha = tag("ns:a", "Alpha");
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new Tag[]{bravo, alpha});

        TagsPredicateImpl impl = createImpl(props, mock(Options.class));

        assertEquals("Alpha", impl.getItems().get(0).getText());
        assertEquals("Bravo", impl.getItems().get(1).getText());
    }

    @Test
    public void getItems_selectedWhenInQueryParams() {
        Tag alpha = tag("ns:a", "Alpha");
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{alpha});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));
        ctx.request().setQueryString(impl.getGroup() + ".propertyvalues.0_values=ns%3Aa");

        assertTrue(impl.getItems().get(0).isSelected());
    }

    @Test
    public void getInitialValue_andGetInitialValues() {
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));
        ctx.request().setQueryString(impl.getGroup() + ".propertyvalues.0_values=hello");

        ValueMap initialValues = impl.getInitialValues();
        assertFalse(initialValues.isEmpty());
    }

    @Test
    public void getExportedType() {
        when(tagManager.getTags(org.mockito.ArgumentMatchers.any())).thenReturn(new Tag[]{});

        TagsPredicateImpl impl = createImpl(new HashMap<>(), mock(Options.class));

        assertEquals("asset-share-commons/components/search/tags", impl.getExportedType());
    }
}
