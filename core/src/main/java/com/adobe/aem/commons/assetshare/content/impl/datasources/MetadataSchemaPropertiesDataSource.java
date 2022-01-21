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

package com.adobe.aem.commons.assetshare.content.impl.datasources;

import com.adobe.aem.commons.assetshare.content.MetadataProperties;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/metadata-schema-properties",
                "sling.servlet.methods=GET"
        },
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class MetadataSchemaPropertiesDataSource extends SlingSafeMethodsServlet {
    private static final String PN_METADATA_FIELD_RESOURCE_TYPES = "metadataFieldResourceTypes";

    @Reference
    private transient DataSourceBuilder dataSourceBuilder;

    @Reference
    private transient MetadataProperties metadataProperties;

    @Override
    protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        final ValueMap properties = request.getResource().getValueMap();
        final List<String> metadataFieldResourceTypes = Arrays.asList(properties.get(PN_METADATA_FIELD_RESOURCE_TYPES, new String[]{}));

        final Map<String, Object> data = new TreeMap<>();
        final Map<String, List<String>> collectedMetadata = metadataProperties.getMetadataProperties(request, metadataFieldResourceTypes);

        for (final Map.Entry<String, List<String>> entry : collectedMetadata.entrySet()) {
            final String label = StringUtils.join(entry.getValue(), " / ")
                    + " (" + StringUtils.removeStart(entry.getKey(), "./") + ")";
            final String value = entry.getKey();

            data.put(label, value);
        }

        dataSourceBuilder.build(request, data);
    }
}