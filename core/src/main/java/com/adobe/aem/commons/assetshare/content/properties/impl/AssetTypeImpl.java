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
import com.day.cq.dam.commons.util.UIHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.commons.mime.MimeTypeService;
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
@Designate(ocd = AssetTypeImpl.Cfg.class)
public class AssetTypeImpl extends AbstractComputedProperty<String> {
    public static final String LABEL = "Asset Type";
    public static final String NAME = "type";

    public static final String IMAGE_LABEL = "IMAGE";
    public static final String DOCUMENT_LABEL = "DOCUMENT";
    public static final String VIDEO_LABEL = "VIDEO";
    public static final String AUDIO_LABEL = "AUDIO";
    public static final String UNKNOWN_LABEL = "";

    private Cfg cfg;

    @Reference
    private MimeTypeService mimeTypeService;

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
        final ResourceResolver resourceResolver = asset.adaptTo(Resource.class).getResourceResolver();
        final String dcFormat = StringUtils.defaultIfBlank(asset.getMimeType(), "");

        final String ext = StringUtils.defaultIfBlank(dcFormat.substring(dcFormat.lastIndexOf('/') + 1, dcFormat.length()), "");
        final Resource lookedupResource = resourceResolver.getResource("/mnt/overlay/dam/gui/content/assets/jcr:content/mimeTypeLookup");

        String displayMimeType = null;

        if (lookedupResource != null && !ResourceUtil.isNonExistingResource(lookedupResource)) {
            displayMimeType = UIHelper.lookupMimeType(ext, lookedupResource, true);
        }

        if (StringUtils.isBlank(displayMimeType)) {
            if (dcFormat.startsWith("image")) {
                displayMimeType = cfg.imageLabel();
            } else if (dcFormat.startsWith("text")) {
                displayMimeType = cfg.documentLabel();
            } else if (dcFormat.startsWith("video")) {
                displayMimeType = cfg.videoLabel();
            } else if (dcFormat.startsWith("audio")) {
                displayMimeType = cfg.audioLabel();
            } else if (dcFormat.startsWith("application")) {
                int indexOne = ext.lastIndexOf('.');
                int indexTwo = ext.lastIndexOf('-');
                int lastWordIdx = (indexOne > indexTwo) ? indexOne : indexTwo;

                displayMimeType = ext.substring(lastWordIdx + 1).toUpperCase();
            }
        }

        return StringUtils.defaultIfBlank(displayMimeType, cfg.unknownLabel());
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Asset Type")
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
                name = "Image Label"
        )
        String imageLabel() default IMAGE_LABEL;

        @AttributeDefinition(
                name = "Document Label"
        )
        String documentLabel() default DOCUMENT_LABEL;

        @AttributeDefinition(
                name = "Video Label"
        )
        String videoLabel() default VIDEO_LABEL;

        @AttributeDefinition(
                name = "Audio Label"
        )
        String audioLabel() default AUDIO_LABEL;

        @AttributeDefinition(
                name = "Unknown Label",
                description = "Defaults to blank so it can be trivially handled via HTL existance checks."
        )
        String unknownLabel() default UNKNOWN_LABEL;
    }
}
