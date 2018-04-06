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

package com.adobe.aem.commons.assetshare.components.actions.dmdownload.impl.datasource;

import com.adobe.aem.commons.assetshare.util.DataSourceBuilder;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.tenant.Tenant;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=asset-share-commons/data-sources/dynamic-media-image-presets",
                "sling.servlet.methods=GET"
        }
)
public class DynamicMediaImagePresetsDataSource extends SlingSafeMethodsServlet {

    private final String IAMGE_PRESET_PATH_DEFAULT = "/macros";
    private final String IMAGE_SERVER_PATH_ROOT_ETC = "/etc/dam/imageserver";
    private final String IMAGE_SERVER_PATH_ROOT_CONF = "/conf/global/settings/dam/dm/presets";

    @Reference
    DataSourceBuilder dataSourceBuilder;

    @Override
    protected void doGet(@Nonnull SlingHttpServletRequest request, @Nonnull SlingHttpServletResponse response) throws
            ServletException, IOException {
        final Map<String, Object> data = new TreeMap<>();
        ResourceResolver resourceResolver = request.getResourceResolver();

        Tenant tenant = resourceResolver.adaptTo(Tenant.class);
        String imagePresetPathEtc = IMAGE_SERVER_PATH_ROOT_ETC;
        String imagePresetPathConf = IMAGE_SERVER_PATH_ROOT_CONF;

        if (tenant != null && tenant.getId() != null) {
            String tenantId = tenant.getId();
            imagePresetPathEtc += "/tenants/" + tenantId + IAMGE_PRESET_PATH_DEFAULT;
            imagePresetPathConf += "/tenants/" + tenantId + IAMGE_PRESET_PATH_DEFAULT;
        } else {
            imagePresetPathEtc += IAMGE_PRESET_PATH_DEFAULT;
            imagePresetPathConf += IAMGE_PRESET_PATH_DEFAULT;
        }

        // for backward compatibility, checking both /etc(6.3 & below) and /conf(6.4)
        Resource imgPresetResourceEtc = resourceResolver.getResource(imagePresetPathEtc);
        Resource imgPresetResourceConf = resourceResolver.getResource(imagePresetPathConf);

        if (imgPresetResourceEtc != null) {
            getPresets(imgPresetResourceEtc, data);
        }

        if (imgPresetResourceConf != null) {
            getPresets(imgPresetResourceConf, data);
        }

        dataSourceBuilder.build(request, data);
    }

    private void getPresets(Resource imgPresetResource, Map<String, Object> data) {
        Iterator<Resource> imgPresetIterator = imgPresetResource.listChildren();
        while (imgPresetIterator.hasNext()) {
            Resource imgPreset = imgPresetIterator.next();
            if (imgPreset != null && !imgPreset.getName().contains(":")){
                data.put(imgPreset.getName(), imgPreset.getName());
            }
        }
    }
}