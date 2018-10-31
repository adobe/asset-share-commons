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
import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = ComputedProperty.class)
@Designate(ocd = WebRenditionImpl.Cfg.class)
public class WebRenditionImpl extends AbstractComputedProperty<String> {

    @Reference
    private MimeTypeHelper mimeTypeHelper;

    public static final String LABEL = "Web Rendition";
    public static final String NAME = "image";

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
        final Rendition rendition = DamUtil.getBestFitRendition(1280, asset.getRenditions());
        String path = "";

        if (rendition != null &&
                mimeTypeHelper.isBrowserSupportedImage(rendition.getMimeType())) {
            path = rendition.getPath();
        } else if (asset.getOriginal() != null &&
                mimeTypeHelper.isBrowserSupportedImage(asset.getOriginal().getMimeType())) {
            path = asset.getOriginal().getPath();
        }

        return escapeString(path);
    }

    private String escapeString(String str) {
        str = StringUtils.replace(str, " ", "%20");
        return StringUtils.replace(str, "/jcr:content", "/_jcr_content");
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Web Rendition")
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
        String[] types() default {Types.RENDITION};
    }
}
