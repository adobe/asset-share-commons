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

package com.adobe.aem.commons.assetshare.content.properties.impl;

import com.adobe.aem.commons.assetshare.content.properties.AbstractComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.day.cq.dam.api.Asset;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.Map;

import static com.adobe.aem.commons.assetshare.content.properties.ComputedProperty.DEFAULT_ASC_COMPUTED_PROPERTY_SERVICE_RANKING;

@Component(
        service = ComputedProperty.class,
        property = {
                Constants.SERVICE_RANKING + "=" + DEFAULT_ASC_COMPUTED_PROPERTY_SERVICE_RANKING
        }
)
@Designate(ocd = ContentTypeImpl.Cfg.class)
public class ContentTypeImpl extends AbstractComputedProperty<String> {
    public static final String LABEL = "Content Type";
    public static final String NAME = "content-type";
    private Cfg cfg;

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


    // Full mime type to label map
    private static final Map<String, String> mimeTypeToLabelMap = ImmutableMap.<String, String>builder()
            .put("image/vnd.adobe.photoshop",                                                   "Photoshop")
            .put("application/msword",                                                          "Word Doc")
            .put("application//vnd.openxmlformats-officedocument.wordprocessingml.document",    "Word Doc")
            .put("application/vnd.ms-excel",                                                    "Excel")
            .put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",           "Excel")
            .put("application/vnd.ms-powerpoint",                                               "PowerPoint")
            .put("application/vnd.openxmlformats-officedocument.presentationml.presentation",   "PowerPoint")
            .put("application/pdf",                                                             "PDF")
            .put("application/xml",                                                             "XML")
            .put("application/zip",                                                             "Zip")
            .put("application/json",                                                            "JSON")
            .put("application/vnd.adobe.illustrator",                                           "Illustrator")
            .put("application/vnd.adobe.indesign",                                              "InDesign")
            .put("application/vnd.adobe.indesignml",                                            "InDesign")
            .put("application/vnd.adobe.indesignx",                                             "InDesign")
            .put("application/vnd.adobe.aftereffects",                                          "After Effects")
            .put("application/vnd.adobe.premiere",                                              "Premiere")
            .put("application/vnd.adobe.xd",                                                    "XD")
            .put("text/html",                                                                   "HTML")
            .put("text/csv",                                                                    "CSV")
            .build();


    // Fallback mime type prefix to label map
    private static final Map<String, String> mimeTypePrefixToLabelMap = ImmutableMap.<String, String>builder()
            .put("image",   "Image")
            .put("video",   "Video")
            .put("audio",   "Audio")
            .put("font",    "Font")
            .put("model",   "3D")
            .put("text",    "Text")
            .build();

    @Override
    public String get(Asset asset) {
        final String mimeType = StringUtils.defaultIfBlank(asset.getMimeType(), "");

        String value = mimeTypeToLabelMap.get(mimeType);

        if (StringUtils.isBlank(value)) {
            value = mimeTypePrefixToLabelMap.get(StringUtils.substringBefore(mimeType, "/"));
        }

        if (StringUtils.isBlank(value)) {
            value = StringUtils.defaultIfBlank(cfg.unknownLabel(), "");
        }

        return value;
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Content Type")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Label",
                description = "Human read-able label."
        )
        String label() default LABEL;

        @AttributeDefinition(
                name = "Types",
                description = "Defines the type of data this exposes. This classification allows for intelligent exposure of Computed Properties in DataSources, etc."
        )
        String[] types() default {Types.METADATA};

        @AttributeDefinition(
                name = "Unknown Label",
                description = "Defaults to blank so it can be trivially handled via HTL existence checks."
        )
        String unknownLabel() default "";
    }
}
