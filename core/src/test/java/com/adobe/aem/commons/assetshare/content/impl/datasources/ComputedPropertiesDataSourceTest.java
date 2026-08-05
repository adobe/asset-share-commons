/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.content.impl.datasources;

import com.adobe.aem.commons.assetshare.content.properties.AbstractComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.Servlet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ComputedPropertiesDataSourceTest {

    @Rule
    public AemContext ctx = new AemContext();

    private final ComputedProperty<String> metadataProp = new MetadataTestComputedProperty();
    private final ComputedProperty<String> urlProp = new UrlTestComputedProperty();
    private final ComputedProperty<String> duplicateMetadataProp = new DuplicateNameTestComputedProperty();

    @Before
    public void setUp() throws Exception {
        ctx.create().resource("/apps/dialog/default",
                singleValueMap("sling:resourceType", "asset-share-commons/data-sources/computed-properties"));

        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());
    }

    private Map<String, Object> singleValueMap(String key, Object value) {
        final Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @Test
    public void doGet_NoTypesFilterReturnsAll() throws Exception {
        ctx.registerService(ComputedProperties.class, (ComputedProperties) () -> Arrays.asList(metadataProp, urlProp));

        ctx.currentResource("/apps/dialog/default");
        final Servlet servlet = ctx.registerInjectActivateService(new ComputedPropertiesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/computed-properties",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap(ctx.request());

        assertArrayEquals(new String[]{"metadataProp", "urlProp"}, actual.values().toArray());
    }

    @Test
    public void doGet_FiltersByComputedPropertyTypes() throws Exception {
        ctx.registerService(ComputedProperties.class, (ComputedProperties) () -> Arrays.asList(metadataProp, urlProp));

        ctx.create().resource("/apps/dialog/filtered", singleValueMap("computedPropertyTypes", new String[]{ComputedProperty.Types.URL}));
        ctx.currentResource("/apps/dialog/filtered");

        final Servlet servlet = ctx.registerInjectActivateService(new ComputedPropertiesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/computed-properties",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap(ctx.request());

        assertArrayEquals(new String[]{"urlProp"}, actual.values().toArray());
    }

    @Test
    public void doGet_WithDuplicateComputedPropertyNames() throws Exception {
        // NOTE (potential production bug): ComputedPropertiesDataSource#doGet(..) intends to skip
        // ComputedProperty entries that share the same name/key (`!data.containsKey(key)` guards the
        // `data.put(...)` call, logging a warning on the "duplicate" branch otherwise). However `data`
        // is a Map<String,Object> keyed by *label* (`data.put(computedProperty.getLabel(), key)`), not
        // by name/key. So `data.containsKey(key)` checks whether the *name* happens to also be a label
        // already in the map, which in practice is never true. As a result, two ComputedProperty
        // instances that return the same getName() are NOT de-duplicated at all -- both appear in the
        // resulting DataSource (one entry per distinct label), contrary to the code's own comment
        // ("Note this follows the execution logic in CombinedProperties"; CombinedProperties actually
        // does de-duplicate by name). This test documents the actual (buggy) behavior.
        ctx.registerService(ComputedProperties.class,
                (ComputedProperties) () -> Arrays.asList(metadataProp, duplicateMetadataProp, urlProp));

        ctx.currentResource("/apps/dialog/default");
        final Servlet servlet = ctx.registerInjectActivateService(new ComputedPropertiesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/computed-properties",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap(ctx.request());

        assertEquals(3, actual.size());
        assertArrayEquals(new String[]{"Duplicate Metadata Prop", "Metadata Prop", "Url Prop"}, actual.keySet().toArray());
    }

    private Map<String, String> toMap(SlingHttpServletRequest request) {
        final Map<String, String> results = new LinkedHashMap<>();
        final DataSource sds = (DataSource) request.getAttribute(DataSource.class.getName());
        final Iterator<Resource> resourcesIterator = sds.iterator();
        while (resourcesIterator.hasNext()) {
            final Resource resource = resourcesIterator.next();
            final ValueMap properties = resource.getValueMap();

            results.put(properties.get(DataSourceBuilder.TEXT, String.class),
                    properties.get(DataSourceBuilder.VALUE, String.class));
        }
        return results;
    }

    private static class MetadataTestComputedProperty extends AbstractComputedProperty<String> {
        @Override
        public String getName() {
            return "metadataProp";
        }

        @Override
        public String getLabel() {
            return "Metadata Prop";
        }

        @Override
        public String[] getTypes() {
            return new String[]{ComputedProperty.Types.METADATA};
        }

        @Override
        public String get(Asset asset) {
            return "unused";
        }
    }

    private static class UrlTestComputedProperty extends AbstractComputedProperty<String> {
        @Override
        public String getName() {
            return "urlProp";
        }

        @Override
        public String getLabel() {
            return "Url Prop";
        }

        @Override
        public String[] getTypes() {
            return new String[]{ComputedProperty.Types.URL};
        }

        @Override
        public String get(Asset asset) {
            return "unused";
        }
    }

    private static class DuplicateNameTestComputedProperty extends AbstractComputedProperty<String> {
        @Override
        public String getName() {
            return "metadataProp";
        }

        @Override
        public String getLabel() {
            return "Duplicate Metadata Prop";
        }

        @Override
        public String[] getTypes() {
            return new String[]{ComputedProperty.Types.METADATA};
        }

        @Override
        public String get(Asset asset) {
            return "unused";
        }
    }
}
