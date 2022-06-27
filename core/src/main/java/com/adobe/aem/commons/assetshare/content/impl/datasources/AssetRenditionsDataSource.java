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

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/asset-renditions",
                "sling.servlet.methods=GET"
        },
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = AssetRenditionsDataSource.Cfg.class)
public class AssetRenditionsDataSource extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionsDataSource.class);

    private static final String PN_EXCLUDE_ASSETRENDITIONS = "excludeAssetRenditions";
    private static final String PN_EXCLUDE_ASSETRENDITIONDISPATCHERS = "excludeAssetRenditionDispatchers";
    private static final String PN_ALLOWED_ASSETRENDITIONDISPATCHER_TYPES = "allowedAssetRenditionTypes";
    private static final String PN_ADD_ASSET_RENDITION_DISPATCHER_TO_LABEL = "addAssetRenditionDispatcherToLabel";

    @Reference
    private transient DataSourceBuilder dataSourceBuilder;

    @Reference
    private transient AssetRenditionDispatchers assetRenditionDispatchers;

    private transient Cfg cfg;

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        final Map<String, Object> data = new TreeMap<>();
        final ValueMap properties = request.getResource().getValueMap();
        final List<String> allowedAssetRenditionTypes =
                Arrays.asList(properties.get(PN_ALLOWED_ASSETRENDITIONDISPATCHER_TYPES, new String[]{}));

        final Set<String> excludeAssetRenditionDispatchers = getExcluded(properties,
                cfg.exclude_assetrenditiondispatcher_names(),
                PN_EXCLUDE_ASSETRENDITIONDISPATCHERS);

        final Set<String> excludeAssetRenditions = getExcluded(properties,
                cfg.exclude_assetrendition_names(),
                PN_EXCLUDE_ASSETRENDITIONS);

        final boolean addAssetRenditionDispatcherToLabel =
                properties.get(PN_ADD_ASSET_RENDITION_DISPATCHER_TO_LABEL, cfg.add_assetrenditiondispatcher_to_label());

        for (final AssetRenditionDispatcher assetRenditionDispatcher : assetRenditionDispatchers.getAssetRenditionDispatchers()) {

            if (acceptsAssetRenditionDispatcher(allowedAssetRenditionTypes,
                    excludeAssetRenditionDispatchers,
                    assetRenditionDispatcher)) {

                assetRenditionDispatcher.getOptions().entrySet().stream()
                        .filter(entry -> !excludeAssetRenditions.contains(entry.getValue()))
                        .filter(entry -> !data.containsValue(entry.getValue()))
                        .forEach(entry -> {
                            String label = entry.getKey();
                            String value = entry.getValue();

                            if (addAssetRenditionDispatcherToLabel) {
                                label += " (" + assetRenditionDispatcher.getLabel() + ")";
                            }

                            data.put(label, value);
                        });
            }
        }

        dataSourceBuilder.build(request, data);
    }

    private boolean acceptsAssetRenditionDispatcher(List<String> allowedAssetRenditionTypes, Set<String> excludeAssetRenditionDispatchers, AssetRenditionDispatcher assetRenditionDispatcher) {
        if (!assetRenditionDispatcher.getTypes().isEmpty() &&
                !allowedAssetRenditionTypes.isEmpty() &&
                Collections.disjoint(assetRenditionDispatcher.getTypes(), allowedAssetRenditionTypes)) {
            // If the AssetRenditionDispatcher specifies types, AND allowedRenditionTypes are specified on the DataSource,
            // then check to see if there is at least one type in common between the AssetRenditionDispatcher and the DataSource.
            // If there IS at least one type in common, then continue checking the other criteria, if either of these
            // When assetRenditionDispatcher's types is empty, the is the equivalent of saying that it applies to ALL types, or in other words, it ALWAYS matches.
            if (log.isDebugEnabled()) {
                log.debug("Skip adding AssetRenditionDispatcher factory [ {} ] to Data Source as it does not have any allowed types", assetRenditionDispatcher.getName());
            }
            return false;
        }  else if (assetRenditionDispatcher.isHidden()) {
            if (log.isDebugEnabled()) {
                log.debug("Skip adding AssetRenditionDispatcher factory [ {} ] to Data Source as it has been marked as hidden via configuration", assetRenditionDispatcher.getName());
            }
            return false;
        } else if (excludeAssetRenditionDispatchers.contains(assetRenditionDispatcher.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("Skip adding AssetRenditionDispatcher factory [ {} ] to Data Source as it has been excluded via configuration", assetRenditionDispatcher.getName());
            }
            return false;
        }

        return true;
    }

    private Set<String> getExcluded(final ValueMap properties,
                                    final String[] excludedViaOsgiConfig,
                                    final String excludedPropertyName) {

        final Set<String> excluded = new HashSet<>();

        if (excludedViaOsgiConfig != null) {
            excluded.addAll(Arrays.asList(excludedViaOsgiConfig));
        }

        excluded.addAll(Arrays.asList(properties.get(excludedPropertyName, new String[]{})));

        return excluded;
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Data Sources - Asset Renditions")
    public @interface Cfg {

        @AttributeDefinition(
                name = "Exclude AssetRenditionDispatchers (by name)",
                description = "Exclude the listed AssetRenditionDispatcher's from populating this data source."
        )
        String[] exclude_assetrenditiondispatcher_names() default {};

        @AttributeDefinition(
                name = "Exclude Asset Renditions (by name)",
                description = "Exclude the listed Rendition Names's from populating this data source. (This is agnostic to which AssetRenditionDispatcher defined them)."
        )
        String[] exclude_assetrendition_names() default {"card", "list"};

        @AttributeDefinition(
                name = "Display AssetRenditionDispatcher names in labels",
                description = "Select to include the AssetRenditionDispatcher's name in the DataSource's labels. Adds in formatAssetRendition name (AssetRenditionDispatcher label)"
        )
        boolean add_assetrenditiondispatcher_to_label() default false;
    }
}