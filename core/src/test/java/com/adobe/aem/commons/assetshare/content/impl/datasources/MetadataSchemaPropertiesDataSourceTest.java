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

import com.adobe.aem.commons.assetshare.content.MetadataProperties;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.Servlet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataSchemaPropertiesDataSourceTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    MetadataProperties metadataProperties;

    @Before
    public void setUp() throws Exception {
        ctx.create().resource("/apps/dialog/default",
                singleValueMap("sling:resourceType", "asset-share-commons/data-sources/metadata-schema-properties"));

        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());
        ctx.registerService(MetadataProperties.class, metadataProperties);
    }

    private Map<String, Object> singleValueMap(String key, Object value) {
        final Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @Test
    public void doGet_BuildsLabelsFromFieldLabelsAndPropertyName() throws Exception {
        final Map<String, List<String>> collected = new LinkedHashMap<>();
        collected.put("./jcr:content/metadata/dc:title", Collections.singletonList("Title"));
        collected.put("./jcr:content/metadata/custom", Arrays.asList("Foo", "Bar"));

        when(metadataProperties.getMetadataProperties(any(SlingHttpServletRequest.class), eq(Collections.emptyList())))
                .thenReturn(collected);

        ctx.currentResource("/apps/dialog/default");
        final Servlet servlet = ctx.registerInjectActivateService(new MetadataSchemaPropertiesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/metadata-schema-properties",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap(ctx.request());

        assertArrayEquals(new String[]{
                "./jcr:content/metadata/custom",
                "./jcr:content/metadata/dc:title"
        }, actual.values().toArray());

        assertArrayEquals(new String[]{
                "Foo / Bar (jcr:content/metadata/custom)",
                "Title (jcr:content/metadata/dc:title)"
        }, actual.keySet().toArray());
    }

    @Test
    public void doGet_PassesMetadataFieldResourceTypesFromProperties() throws Exception {
        ctx.create().resource("/apps/dialog/withtypes",
                singleValueMap("metadataFieldResourceTypes", new String[]{"my/custom/type"}));
        ctx.currentResource("/apps/dialog/withtypes");

        when(metadataProperties.getMetadataProperties(any(SlingHttpServletRequest.class), eq(Collections.singletonList("my/custom/type"))))
                .thenReturn(Collections.emptyMap());

        final Servlet servlet = ctx.registerInjectActivateService(new MetadataSchemaPropertiesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/metadata-schema-properties",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final Map<String, String> actual = toMap(ctx.request());

        assertArrayEquals(new String[]{}, actual.values().toArray());
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
}
