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
import java.util.Map;
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

    @Reference
    private DataSourceBuilder dataSourceBuilder;

    @Reference
    private AssetRenditionsHelper assetRenditionsHelper;

    private Cfg cfg;

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        final Map<String, Object> data = new TreeMap<>();

        for (final AssetRenditionResolver assetRenditionResolver : assetRenditionsHelper.getAssetRenditionResolvers()) {
            assetRenditionResolver.getOptions().entrySet().stream().forEach(entry -> {
                    String title = entry.getKey();
                    if (cfg.add_assetrenditionresolver_to_label()) {
                       title += " (" + assetRenditionResolver.getName() + ")";
                    }
                    data.put(title, entry.getValue());
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
                name = "Display AssetRenditionResolver names in labels",
                description = "Select to include the AssetRenditionResolver's name in the DataSource's labels."
        )
        boolean add_assetrenditionresolver_to_label() default false;
    }
}