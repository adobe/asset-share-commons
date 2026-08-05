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

package com.adobe.aem.commons.assetshare.search.impl.datasources;

import com.adobe.aem.commons.assetshare.content.MetadataProperties;
import com.adobe.aem.commons.assetshare.search.FastProperties;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import com.adobe.aem.commons.assetshare.util.impl.DataSourceBuilderImpl;
import com.adobe.granite.ui.components.ds.DataSource;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderablePropertiesDataSourceTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    private MetadataProperties metadataProperties;

    @Mock
    private FastProperties fastPropertiesService;

    @Before
    public void setUp() throws Exception {
        ctx.registerService(DataSourceBuilder.class, new DataSourceBuilderImpl());
        ctx.registerService(MetadataProperties.class, metadataProperties);
        ctx.registerService(FastProperties.class, fastPropertiesService);

        ctx.currentResource(ctx.create().resource("/apps/dialog/default"));
    }

    @Test
    public void doGet_IncludesJcrScoreAndMetadataProperties() throws ServletException, IOException {
        final Map<String, List<String>> collected = new LinkedHashMap<>();
        collected.put("./jcr:content/metadata/dc:title", Collections.singletonList("Title"));

        when(metadataProperties.getMetadataProperties(ctx.request())).thenReturn(collected);
        when(fastPropertiesService.getFastProperties("ordered"))
                .thenReturn(Arrays.asList("jcr:content/metadata/dc:title", "jcr:content/metadata/dc:extra"));
        when(fastPropertiesService.getFastLabel("Search Relevancy (jcr:score)")).thenReturn("FAST Score");
        when(fastPropertiesService.getFastLabel("Title (jcr:content/metadata/dc:title )")).thenReturn("FAST Title");
        when(fastPropertiesService.getFastLabel("jcr:content/metadata/dc:extra")).thenReturn("FAST Extra");

        final Servlet servlet = ctx.registerInjectActivateService(new OrderablePropertiesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/orderable-properties",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final DataSource dataSource = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(dataSource);

        assertEquals(3, actual.size());
        assertEquals("@jcr:score", actual.get("FAST Score"));
        assertEquals("@jcr:content/metadata/dc:title", actual.get("FAST Title"));
        assertEquals("@jcr:content/metadata/dc:extra", actual.get("FAST Extra"));
    }

    @Test
    public void doGet_WithNoFastProperties_OnlyJcrScore() throws ServletException, IOException {
        when(metadataProperties.getMetadataProperties(ctx.request())).thenReturn(new LinkedHashMap<>());
        when(fastPropertiesService.getFastProperties("ordered")).thenReturn(Collections.emptyList());
        when(fastPropertiesService.getFastLabel("Search Relevancy (jcr:score)")).thenReturn("FAST Score");

        final Servlet servlet = ctx.registerInjectActivateService(new OrderablePropertiesDataSource(),
                "sling.servlet.resourceTypes", "asset-share-commons/data-sources/orderable-properties",
                "sling.servlet.methods", "GET");

        servlet.service(ctx.request(), ctx.response());

        final DataSource dataSource = (DataSource) ctx.request().getAttribute(DataSource.class.getName());
        final Map<String, String> actual = toMap(dataSource);

        assertEquals(1, actual.size());
        assertEquals("@jcr:score", actual.get("FAST Score"));
    }

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
}
