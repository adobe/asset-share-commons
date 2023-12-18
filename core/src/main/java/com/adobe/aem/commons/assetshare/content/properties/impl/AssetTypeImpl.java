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

import static com.adobe.aem.commons.assetshare.content.properties.ComputedProperty.DEFAULT_ASC_COMPUTED_PROPERTY_SERVICE_RANKING;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.adobe.aem.commons.assetshare.content.properties.AbstractComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.UIHelper;

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

    private static final String SERVICE_NAME = "mimetype-service";
    private static final String MIMETYPE_LOOKUP_RESOURCE_PATH = "/mnt/overlay/dam/gui/content/assets/jcr:content/mimeTypeLookup";
    
    private Cfg cfg;

    @Reference
    private transient ResourceResolverFactory resourceResolverFactory;

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

    /**
     * Tries to derive the high-level asset type from the low-level mime type of the asset.
     * It leverages the configuration below {@code /apps/dam/gui/content/assets/jcr:content/mimeTypeLookup} or {@code /libs/dam/gui/content/assets/jcr:content/mimeTypeLookup}
     * for the classification.
     * @return the computed type (one of {@link #IMAGE_LABEL}, {@link #DOCUMENT_LABEL}, {@link #VIDEO_LABEL}, {@link #AUDIO_LABEL} or {@link #UNKNOWN_LABEL})
     */
    @Override
    public String get(Asset asset) {
        final String mimeType = StringUtils.defaultIfBlank(asset.getMimeType(), "");
        String assetType = getAssetTypeFromMimetypeConfiguration(mimeType);

        if (StringUtils.isBlank(assetType)) {
            if (mimeType.startsWith("image")) {
                assetType = cfg.imageLabel();
            } else if (mimeType.startsWith("text")) {
                assetType = cfg.documentLabel();
            } else if (mimeType.startsWith("video")) {
                assetType = cfg.videoLabel();
            } else if (mimeType.startsWith("audio")) {
                assetType = cfg.audioLabel();
            } else if (mimeType.startsWith("application")) {
                final String ext = StringUtils.defaultIfBlank(mimeType.substring(mimeType.lastIndexOf('/') + 1, mimeType.length()), "");
                int indexOne = ext.lastIndexOf('.');
                int indexTwo = ext.lastIndexOf('-');
                int lastWordIdx = (indexOne > indexTwo) ? indexOne : indexTwo;

                assetType = ext.substring(lastWordIdx + 1).toUpperCase();
            }
        }
        return StringUtils.defaultIfBlank(assetType, cfg.unknownLabel());
    }

    private String getAssetTypeFromMimetypeConfiguration(String mimeType) {
        final String ext = StringUtils.defaultIfBlank(mimeType.substring(mimeType.lastIndexOf('/') + 1, mimeType.length()), "");
        Map<String, Object> authInfo = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, (Object) SERVICE_NAME);
        try (ResourceResolver serviceResourceResolver = resourceResolverFactory.getServiceResourceResolver(authInfo)) {
            final Resource lookedupResource = serviceResourceResolver.getResource(MIMETYPE_LOOKUP_RESOURCE_PATH);
            if (lookedupResource == null) {
                throw new IllegalStateException("Service resource resolver " + serviceResourceResolver + " does not have access to system resource " + MIMETYPE_LOOKUP_RESOURCE_PATH);
            }
            return UIHelper.lookupMimeType(ext, lookedupResource, true);
        } catch (LoginException e) {
            throw new IllegalStateException("Service resource resolver with subservice name " + SERVICE_NAME + " does not allow login", e);
        }
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
                description = "Defaults to blank so it can be trivially handled via HTL existence checks."
        )
        String unknownLabel() default UNKNOWN_LABEL;
    }
}
