/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.content.properties.impl;

import com.adobe.aem.commons.assetshare.content.properties.AbstractComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.adobe.granite.asset.api.AssetException;
import com.day.cq.dam.api.Asset;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * This Computed Property returns a UTF-8 encoded path to the asset.
 *
 * The asset path is collected via the PathImpl Computed Property.
 */
@Component(service = ComputedProperty.class)
@Designate(ocd = PathEncodedImpl.Cfg.class)
public class PathEncodedImpl extends AbstractComputedProperty<String> {
    public static final String LABEL = "Asset Path (UTF-8 Encoded)";
    public static final String NAME = "path/encoded";
    private Cfg cfg;

    @Reference(target = "(component.name=com.adobe.aem.commons.assetshare.content.properties.impl.PathImpl)")
    ComputedProperty<String> pathComputedProperty;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return cfg.label();
    }

    @Override
    public String[] getTypes() {
        return cfg.types();
    }

    @Override
    public String get(final Asset asset, final SlingHttpServletRequest request) {
        try {
            return URLEncoder.encode(pathComputedProperty.get(asset, request),
                    StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssetException("Could not UTF-8 encode the asset path.", asset.getPath());
        }
    }

    @Activate
    protected void activate(final Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Asset Path (Encoded)")
    public @interface Cfg {
        @AttributeDefinition(name = "Label", description = "Human readable label.")
        String label() default LABEL;

        @AttributeDefinition(
                name = "Types",
                description = "Defines the type of data this exposes. This classification allows for intelligent exposure of Computed Properties in DataSources, etc.")
        String[] types() default { Types.URL, Types.RENDITION, Types.VIDEO_RENDITION };
    }
}
