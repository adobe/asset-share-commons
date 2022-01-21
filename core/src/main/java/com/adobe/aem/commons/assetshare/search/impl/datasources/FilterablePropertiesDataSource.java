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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.util.*;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/filterable-properties",
                "sling.servlet.methods=GET"
        },
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class FilterablePropertiesDataSource extends SlingSafeMethodsServlet {
    private static final String PN_METADATA_FIELD_TYPES = "metadataFieldTypes";
    private static final String PN_PROPERTY_INDEX = "propertyIndex";
    private static final String PN_FILTER_PROPERTIES = "indexRuleCapabilities";

    @Reference
    private transient DataSourceBuilder dataSourceBuilder;

    @Reference
    private transient FastProperties fastPropertiesService;

    @Reference
    private transient MetadataProperties metadataProperties;

    @Override
    protected final void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) {
        final Map<String, Object> data = new TreeMap<>();
        final ValueMap properties = request.getResource().getValueMap();
        final List<String> metadataFieldTypes = Arrays.asList(properties.get(PN_METADATA_FIELD_TYPES, ArrayUtils.EMPTY_STRING_ARRAY));
        final List<String> indexRuleCapabilityProperties = Arrays.asList(properties.get(PN_FILTER_PROPERTIES, new String[] { PN_PROPERTY_INDEX} ));

        final Map<String, List<String>> collectedMetadata = metadataProperties.getMetadataProperties(request, metadataFieldTypes);
        final List<String> fastProperties = fastPropertiesService.getFastProperties(indexRuleCapabilityProperties);

        for (final Map.Entry<String, List<String>> entry : collectedMetadata.entrySet()) {
            final String label = StringUtils.join(entry.getValue(), " / ") + " (" + StringUtils.removeStart(entry.getKey(), "./") + ")";
            final String value = entry.getKey();

            if (fastProperties.contains(StringUtils.removeStart(entry.getKey(), "./"))) {
                data.put(fastPropertiesService.getFastLabel(label) , value);
            } else {
                data.put(fastPropertiesService.getSlowLabel(label), value);
            }
        }

        if (metadataFieldTypes.isEmpty()) {
            addDeltaFastProperties(data, fastProperties);
        }

        dataSourceBuilder.build(request, data);
    }

    private void addDeltaFastProperties(Map<String, Object> data, List<String> fastProperties) {
        final List<String> deltaFastProperties =
                fastPropertiesService.getDeltaProperties(fastProperties,
                        (Collection<String>) (Collection<?>) data.values());

        for (String deltaFastProperty : deltaFastProperties) {
            data.put(FastProperties.DELTA + " " + deltaFastProperty, deltaFastProperty);
        }
    }
}