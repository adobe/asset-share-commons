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

package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Rule;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DataSourceBuilderImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    private Map<String, String> toMap(DataSource dataSource) {
        final Map<String, String> results = new LinkedHashMap<>();
        final Iterator<Resource> resourcesIterator = dataSource.iterator();
        while (resourcesIterator.hasNext()) {
            final Resource resource = resourcesIterator.next();
            final ValueMap properties = resource.getValueMap();

            results.put(properties.get(DataSourceBuilder.TEXT, String.class),
                    properties.get(DataSourceBuilder.VALUE, String.class));
        }
        return results;
    }

    @Test
    public void build_withoutNoneOption() {
        ctx.create().resource("/content/dialog/no-none-option");
        ctx.currentResource("/content/dialog/no-none-option");

        final Map<String, Object> data = new LinkedHashMap<>();
        data.put("Label A", "value-a");
        data.put("Label B", "value-b");

        new DataSourceBuilderImpl().build(ctx.request(), data);

        final DataSource dataSource = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(dataSource);

        assertEquals(2, actual.size());
        assertEquals("value-a", actual.get("Label A"));
        assertEquals("value-b", actual.get("Label B"));
        assertFalse("None option should not be present", actual.containsKey("None"));
    }

    @Test
    public void build_withNoneOption_andExplicitNoneValue() {
        ctx.create().resource("/content/dialog/with-none-option",
                DataSourceBuilder.PN_NONE_TEXT, "None",
                DataSourceBuilder.PN_NONE_VALUE, "none-value");
        ctx.currentResource("/content/dialog/with-none-option");

        final Map<String, Object> data = new LinkedHashMap<>();
        data.put("Label A", "value-a");

        new DataSourceBuilderImpl().build(ctx.request(), data);

        final DataSource dataSource = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(dataSource);

        // "None" must be the FIRST entry.
        assertEquals("None", actual.keySet().iterator().next());
        assertEquals("none-value", actual.get("None"));
        assertEquals("value-a", actual.get("Label A"));
        assertEquals(2, actual.size());
    }

    @Test
    public void build_withNoneOption_andDefaultNoneValue() {
        ctx.create().resource("/content/dialog/with-none-option-default-value",
                DataSourceBuilder.PN_NONE_TEXT, "None");
        ctx.currentResource("/content/dialog/with-none-option-default-value");

        final Map<String, Object> data = new LinkedHashMap<>();

        new DataSourceBuilderImpl().build(ctx.request(), data);

        final DataSource dataSource = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(dataSource);

        assertEquals(1, actual.size());
        assertEquals("", actual.get("None"));
    }
}
