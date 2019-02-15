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

import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
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
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/computed-properties",
                "sling.servlet.methods=GET"
        },
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class ComputedPropertiesDataSource extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ComputedPropertiesDataSource.class);

    public static final String PN_COMPUTED_PROPERTY_TYPES = "computedPropertyTypes";

    @Reference
    private DataSourceBuilder dataSourceBuilder;

    @Reference
    private ComputedProperties computedProperties;

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        final Map<String, Object> data = new TreeMap<>();

        final ValueMap properties = request.getResource().getValueMap();
        final String[] computedPropertyTypes = properties.get(PN_COMPUTED_PROPERTY_TYPES, new String[]{});

        for (final ComputedProperty computedProperty : computedProperties.getComputedProperties()) {

            if (ArrayUtils.isEmpty(computedPropertyTypes) ||
                    containsAny(computedPropertyTypes, computedProperty.getTypes())) {

                final String key = computedProperty.getName();

                if (StringUtils.isNotBlank(key)) {
                    if (!data.containsKey(key)) {
                        data.put(computedProperty.getLabel(), key);
                    } else {
                        // Note this follows the execution logic in CombinedProperties
                        log.warn("Found duplicate Computed Property key [ {} ]. Only accepting the first instance.", key);
                    }
                }
            }
        }

        dataSourceBuilder.build(request, data);
    }

    private boolean containsAny(String[] arrayOne, String[] arrayTwo) {
        for (final String valueOne : arrayOne) {
            for (final String valueTwo : arrayTwo) {
                if (StringUtils.equals(valueOne, valueTwo)) {
                    return true;
                }
            }
        }

        return false;
    }
}