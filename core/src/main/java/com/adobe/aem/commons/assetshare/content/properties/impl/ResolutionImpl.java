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
import org.apache.sling.api.SlingHttpServletRequest;
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
@Designate(ocd = ResolutionImpl.Cfg.class)
public class ResolutionImpl extends AbstractComputedProperty<String> {

    public static final String LABEL = "Resolution";
    public static final String NAME = "resolution";

    @Reference(target = "(component.name=com.adobe.aem.commons.assetshare.content.properties.impl.HeightImpl)")
    ComputedProperty<Long> height;

    @Reference(target = "(component.name=com.adobe.aem.commons.assetshare.content.properties.impl.WidthImpl)")
    ComputedProperty<Long> width;

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
    public String get(Asset asset, SlingHttpServletRequest request) {
        String resolution = "";
        if (width != null && height != null) {
            Long widthLong = width.get(asset, request);
            Long heightLong = height.get(asset, request);
            if (widthLong != null && heightLong != null) {
                resolution = UIHelper.getResolutionLabel(widthLong, heightLong, request);
                resolution = resolution.replaceAll(",", "");
            }
        }

        return resolution;
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Resolution")
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
