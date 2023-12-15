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
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

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
    public static final String UNKNOWN_LABEL = "";

    private Cfg cfg;

    @Reference(target = "(component.name=com.adobe.aem.commons.assetshare.content.properties.impl.FileExtensionImpl)")
    ComputedProperty<Long> fileExtension;

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
    public String get(Asset asset) {
        final String mimeType = StringUtils.defaultIfBlank(asset.getMimeType(), "");

        if (StringUtils.isNotBlank(mimeType)) {
            // Has mimetype
            if (StringUtils.startsWith(mimeType, "image/")) {
                // Is image
                if (StringUtils.endsWithAny(mimeType, "/vnd.adobe.photoshop")) {
                    return "PHOTOSHOP";
                } else {
                    return "IMAGE";
                }
            } else if (StringUtils.startsWith(mimeType, "video/")) {
                // Is video
                return "VIDEO";
            } else if (StringUtils.startsWith(mimeType, "audio/")) {
                // Is audio
                return "AUDIO";
            } else if (StringUtils.startsWith(mimeType, "font/")) {
                // Is font
                return "FONT";
            } else if (StringUtils.startsWith(mimeType, "model/")) {
                // Is 3D model
                return "3D";
            } else if (StringUtils.startsWith(mimeType, "application/")) {
                // Is application
                if (StringUtils.endsWithAny(mimeType, "/pdf")) {
                    return "PDF";
                } else if (StringUtils.endsWithAny(mimeType, "/msword", "/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    return "DOCUMENT";
                } else if (StringUtils.endsWithAny(mimeType, "/vnd.ms-excel", "/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                    return "SPREADSHEET";
                } else if (StringUtils.endsWithAny(mimeType, "/vnd.ms-powerpoint", "/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    return "POWERPOINT";
                } else if (StringUtils.endsWithAny(mimeType, "/xml")) {
                    return "XML";
                } else if (StringUtils.endsWithAny(mimeType, "/zip")) {
                    return "ZIP";
                } else if (StringUtils.endsWithAny(mimeType, "/json")) {
                    return "JSON";
                } else if (StringUtils.endsWithAny(mimeType, "/vnd.adobe.illustrator")) {
                    return "ILLUSTRATOR";
                } else if (StringUtils.endsWithAny(mimeType, "/vnd.adobe.indesign", "/vnd.adobe.indesignml", "/vnd.adobe.indesignx", "/vnd.adobe.indesign-idml-package", "/vnd.adobe.indesign-idml-template", "/vnd.adobe.indesign-snippet", "/vnd.adobe.indesign-library", "/vnd.adobe.indesign-xml", "/vnd.adobe.indesign-pkg", "/vnd.adobe.indesign-interchange", "/vnd.adobe.indesign-interchange-package", "/vnd.adobe.indesign-book", "/vnd.adobe.indesign-ccml")) {
                    return "INDESIGN";
                } else if (StringUtils.endsWithAny(mimeType, "/vnd.adobe.aftereffects")) {
                    return "AFTEREFFECTS";
                } else if (StringUtils.endsWithAny(mimeType, "/vnd.adobe.premiere")) {
                    return "PREMIERE";
                } else if (StringUtils.endsWithAny(mimeType, "/vnd.adobe.xd")) {
                    return "XD";
                }
            } else if (StringUtils.startsWith(mimeType, "text/")) {
                // Is text
                if (StringUtils.endsWithAny(mimeType, "/html")) {
                    return "HTML";
                } else if (StringUtils.endsWithAny(mimeType, "/csv")) {
                    return "CSV";
                } else {
                    return "TEXT";
                }
            }
        }

        return UNKNOWN_LABEL;
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
        String unknownLabel() default UNKNOWN_LABEL;
    }
}
