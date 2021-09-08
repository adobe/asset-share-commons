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
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/orderable-properties",
                "sling.servlet.methods=GET"
        },
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class OrderablePropertiesDataSource extends SlingSafeMethodsServlet {
    private static final String PN_ORDERED = "ordered";

    @Reference
    private transient DataSourceBuilder dataSourceBuilder;

    @Reference
    private transient FastProperties fastPropertiesService;

    @Reference
    private transient MetadataProperties metadataProperties;

    @Override
    protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {

        final Map<String, Object> data = new TreeMap<>();

        final Map<String, List<String>> collectedMetadata = metadataProperties.getMetadataProperties(request);
        final List<String> fastProperties = fastPropertiesService.getFastProperties(PN_ORDERED);

        data.put(fastPropertiesService.getFastLabel("Search Relevancy (jcr:score)"), "@jcr:score");

        for (final String fastProperty : fastProperties) {
            final String value = "@" + fastProperty;

            if (collectedMetadata.keySet().contains("./" + fastProperty)) {
                final List<String> labels = collectedMetadata.get("./" + fastProperty);
                final String label = StringUtils.join(labels, " / ") + " (" + fastProperty + " )";
                data.put(fastPropertiesService.getFastLabel(label), value);
            } else {
                data.put(fastPropertiesService.getFastLabel(fastProperty), value);
            }
        }

        dataSourceBuilder.build(request, data);
    }
}