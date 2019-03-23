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

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionResolver;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionsHelper;
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
    private static final String PN_EXCLUDE_ASSETRENDITIONRESOLVERS = "excludeAssetRenditionResolvers";

    @Reference
    private DataSourceBuilder dataSourceBuilder;

    @Reference
    private AssetRenditionsHelper assetRenditionsHelper;

    private Cfg cfg;

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        final Map<String, Object> data = new TreeMap<>();
        final ValueMap properties = request.getResource().getValueMap();

        final Set<String> excludeAssetRenditionResolverNames = new HashSet<>();
        if (cfg.exclude_assetrenditionresolver_names() != null) {
            excludeAssetRenditionResolverNames.addAll(Arrays.asList(cfg.exclude_assetrenditionresolver_names()));
        }
        excludeAssetRenditionResolverNames.addAll(Arrays.asList(properties.get(PN_EXCLUDE_ASSETRENDITIONRESOLVERS, new String[]{})));

        final Set<String> excludeAssetRenditionNames = new HashSet<>();
        if (cfg.exclude_assetrendition_names() != null) {
            excludeAssetRenditionNames.addAll(Arrays.asList(cfg.exclude_assetrendition_names()));
        }
        excludeAssetRenditionNames.addAll(Arrays.asList(properties.get(PN_EXCLUDE_ASSETRENDITIONS, new String[]{})));

        for (final AssetRenditionResolver assetRenditionResolver : assetRenditionsHelper.getAssetRenditionResolvers()) {
            if (excludeAssetRenditionResolverNames.contains(assetRenditionResolver.getName())) {
                log.debug("Skip adding AssetRenditionResolver [ {} ] to Data Source as it has been excluded via configuration", assetRenditionResolver.getName());
                continue;
            }

            assetRenditionResolver.getOptions().entrySet().stream()
                    .filter(entry -> !excludeAssetRenditionNames.contains(entry.getValue()))
                    .forEach(entry -> {
                        String label = entry.getKey();
                        String value = entry.getValue();

                        if (cfg.add_assetrenditionresolver_to_label()) {
                            label += " (" + assetRenditionResolver.getName() + ")";
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
                name = "Exclude AssetRenditionResolvers (by name)",
                description = "Exclude the listed AssetRenditionResolver's from populating this data source."
        )
        String[] exclude_assetrenditionresolver_names() default {};

        @AttributeDefinition(
                name = "Exclude Asset Renditions (by name)",
                description = "Exclude the listed Rendition Names's from populating this data source. (This is agnostic to which AssetRenditionResolver defined them)."
        )
        String[] exclude_assetrendition_names() default {"card", "list"};

        @AttributeDefinition(
                name = "Display AssetRenditionResolver names in labels",
                description = "Select to include the AssetRenditionResolver's name in the DataSource's labels. Adds in format: AssetRendition name (AssetRenditionResolver label)"
        )
        boolean add_assetrenditionresolver_to_label() default false;
    }
}