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
import com.adobe.aem.commons.assetshare.testing.ReflectionTestUtil;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.search.eval.DateRangePredicateEvaluator;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatePredicateImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    private OptionItem optionItem(String text, String value) {
        OptionItem item = mock(OptionItem.class);
        when(item.getText()).thenReturn(text);
        when(item.getValue()).thenReturn(value);
        return item;
    }

    private DatePredicateImpl createImpl(String path, Map<String, Object> props, Options coreOptions) {
        Resource resource = ctx.resourceResolver().getResource(path);
        if (resource == null) {
            resource = ctx.create().resource(path, props);
        }
        ctx.currentResource(path);

        final DatePredicateImpl impl = new DatePredicateImpl();
        ReflectionTestUtil.setField(impl, AbstractPredicate.class, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "property", props.get("property"));
        ReflectionTestUtil.setField(impl, "dateType", props.get("dateType"));
        ReflectionTestUtil.setField(impl, "typeString", props.get("type"));
        // init() internally does "coreOptions = request.adaptTo(Options.class);" which we cannot easily
        // control (the real Core Components Options implementation cannot be constructed in this test
        // environment). So we call init() and then overwrite the field with our own test double.
        impl.init();
        ReflectionTestUtil.setField(impl, "coreOptions", coreOptions);

        return impl;
    }

    @Test
    public void getProperty() {
        Map<String, Object> props = new HashMap<>();
        props.put("property", "jcr:content/metadata/dc:format");

        DatePredicateImpl impl = createImpl("/content/date1", props, mock(Options.class));

        assertEquals("jcr:content/metadata/dc:format", impl.getProperty());
    }

    @Test
    public void getDateType_andGetName() {
        Map<String, Object> props = new HashMap<>();
        props.put("dateType", "relativedaterange");

        DatePredicateImpl impl = createImpl("/content/date2", props, mock(Options.class));

        assertEquals("relativedaterange", impl.getDateType());
        assertEquals("relativedaterange", impl.getName());
    }

    @Test
    public void getSubType() {
        Map<String, Object> props = new HashMap<>();
        props.put("type", "checkbox-group");

        DatePredicateImpl impl = createImpl("/content/date3", props, mock(Options.class));

        assertEquals("checkbox-group", impl.getSubType());
    }

    @Test
    public void getLowerBoundName_andUpperBoundName() {
        Map<String, Object> props = new HashMap<>();
        props.put("dateType", "daterange");

        DatePredicateImpl impl = createImpl("/content/date4", props, mock(Options.class));

        assertEquals("daterange." + DateRangePredicateEvaluator.LOWER_BOUND, impl.getLowerBoundName());
        assertEquals("daterange." + DateRangePredicateEvaluator.UPPER_BOUND, impl.getUpperBoundName());
    }

    @Test
    public void isReady_relativeDateRange_requiresItems() {
        Map<String, Object> props = new HashMap<>();
        props.put("dateType", "relativedaterange");
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        DatePredicateImpl impl = createImpl("/content/date5", props, coreOptions);

        assertFalse(impl.isReady());
    }

    @Test
    public void isReady_relativeDateRange_trueWithItems() {
        Map<String, Object> props = new HashMap<>();
        props.put("dateType", "relativedaterange");
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Last 7 days", "7"));
        when(coreOptions.getItems()).thenReturn(items);

        DatePredicateImpl impl = createImpl("/content/date6", props, coreOptions);

        assertTrue(impl.isReady());
    }

    @Test
    public void isReady_dateRange_alwaysTrue() {
        Map<String, Object> props = new HashMap<>();
        props.put("dateType", "daterange");

        DatePredicateImpl impl = createImpl("/content/date7", props, mock(Options.class));

        assertTrue(impl.isReady());
    }

    @Test
    public void getInitialValue_isNull() {
        Map<String, Object> props = new HashMap<>();

        DatePredicateImpl impl = createImpl("/content/date8", props, mock(Options.class));

        assertNull(impl.getInitialValue());
    }

    @Test
    public void getInitialValues_populatedFromQueryParams() {
        Map<String, Object> props = new HashMap<>();
        props.put("dateType", "daterange");

        DatePredicateImpl impl = createImpl("/content/date9", props, mock(Options.class));
        String lowerParam = impl.getGroup() + "." + impl.getLowerBoundName();
        String upperParam = impl.getGroup() + "." + impl.getUpperBoundName();
        ctx.request().setQueryString(lowerParam + "=2020-01-01&" + upperParam + "=2020-12-31");

        assertEquals("2020-01-01", impl.getInitialValues().get(impl.getLowerBoundName(), String.class));
        assertEquals("2020-12-31", impl.getInitialValues().get(impl.getUpperBoundName(), String.class));
    }

    @Test
    public void getItems_selectedWhenValueInInitialValues() {
        Map<String, Object> props = new HashMap<>();
        props.put("dateType", "relativedaterange");
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Last 7 days", "7"));
        when(coreOptions.getItems()).thenReturn(items);

        DatePredicateImpl impl = createImpl("/content/date10", props, coreOptions);
        String lowerParam = impl.getGroup() + "." + impl.getLowerBoundName();
        ctx.request().setQueryString(lowerParam + "=7");

        List<OptionItem> result = impl.getItems();
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof SelectedOptionItem);
    }

    @Test
    public void getExportedType() {
        DatePredicateImpl impl = createImpl("/content/date11", new HashMap<>(), mock(Options.class));

        assertEquals("asset-share-commons/components/search/date-range", impl.getExportedType());
    }
}
