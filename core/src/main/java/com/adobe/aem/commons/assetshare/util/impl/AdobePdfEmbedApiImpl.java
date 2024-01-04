/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.AdobePdfEmbedApi;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;

/**
 * Allows for Git-based managed Adobe PDF Embed API Client ID used for the Asset Share Commons PDF details.
 *
 * https://acrobatservices.adobe.com/dc-integration-creation-app-cdn/main.html?api=pdf-embed-api
 */
@Component(
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = AdobePdfEmbedApiImpl.Config.class)
public class AdobePdfEmbedApiImpl implements AdobePdfEmbedApi {
    private Config cfg;

    @Override
    public String getClientId() {
        return cfg.client_id();
    }

    @ObjectClassDefinition(
            name = "Asset Share Commons - Adobe PDF Embed API Configuration",
            description = "Configuration for the Adobe PDF Embed API. Each domain requires a unique Client ID."
    )
    @interface Config {
        @AttributeDefinition(
                name = "Adobe PDF Embed API Client ID",
                description = "Get free Adobe PDF Embed API Client ID from: https://acrobatservices.adobe.com/dc-integration-creation-app-cdn/main.html?api=pdf-embed-api"
        )
        String client_id();
    }

    @Activate
    @Modified
    protected void activate(Config cfg) {
        this.cfg = cfg;
    }


}
