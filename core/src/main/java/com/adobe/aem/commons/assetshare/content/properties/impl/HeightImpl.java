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
import com.day.cq.dam.api.DamConstants;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
@Designate(ocd = HeightImpl.Cfg.class)
public class HeightImpl extends AbstractComputedProperty<Long> {
    public static final String LABEL = "Height";
    public static final String NAME = "height";
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

    @Override
    public Long get(Asset asset) {
        final ValueMap metadata = getMetadataProperties(asset);

        Long height = metadata.get(DamConstants.TIFF_IMAGELENGTH, Long.class);

        if (height == null) {
            height = metadata.get(DamConstants.EXIF_PIXELYDIMENSION, Long.class);
        }

        return height;
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Height")
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
    }
}
