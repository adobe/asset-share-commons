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
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
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
import java.util.*;

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

    @Reference
    private DataSourceBuilder dataSourceBuilder;

    @Reference
    private AssetRenditions assetRenditions;

    private Cfg cfg;

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        final Map<String, Object> data = new TreeMap<>();
        final ValueMap properties = request.getResource().getValueMap();

        final Set<String> excludeAssetRenditionResolverNames = new HashSet<>();
        if (cfg.exclude_assetrenditiondispatcher_names() != null) {
            excludeAssetRenditionResolverNames.addAll(Arrays.asList(cfg.exclude_assetrenditiondispatcher_names()));
        }
        excludeAssetRenditionResolverNames.addAll(Arrays.asList(properties.get(PN_EXCLUDE_ASSETRENDITIONDISPATCHERS, new String[]{})));

        final Set<String> excludeAssetRenditionNames = new HashSet<>();
        if (cfg.exclude_assetrendition_names() != null) {
            excludeAssetRenditionNames.addAll(Arrays.asList(cfg.exclude_assetrendition_names()));
        }
        excludeAssetRenditionNames.addAll(Arrays.asList(properties.get(PN_EXCLUDE_ASSETRENDITIONS, new String[]{})));

        for (final AssetRenditionDispatcher assetRenditionDispatcher : assetRenditions.getAssetRenditionDispatchers()) {
            if (excludeAssetRenditionResolverNames.contains(assetRenditionDispatcher.getName())) {
                log.debug("Skip adding AssetRenditionDispatcher [ {} ] to Data Source as it has been excluded via configuration", assetRenditionDispatcher.getName());
                continue;
            }

            assetRenditionDispatcher.getOptions().entrySet().stream()
                    .filter(entry -> !excludeAssetRenditionNames.contains(entry.getValue()))
                    .forEach(entry -> {
                        String label = entry.getKey();
                        String value = entry.getValue();

                        if (cfg.add_assetrenditiondispatcher_to_label()) {
                            label += " (" + assetRenditionDispatcher.getName() + ")";
                        }

                        data.put(label, value);
                    });
        }

        dataSourceBuilder.build(request, data);
    }

    @Activate
    protected void activate(AssetRenditionsDataSource.Cfg cfg) {
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