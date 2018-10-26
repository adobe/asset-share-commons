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
import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = ComputedProperty.class)
@Designate(ocd = ThumbnailImpl.Cfg.class)
public class ThumbnailImpl extends AbstractComputedProperty<String> {

    public static final String LABEL = "Thumbnail Rendition";
    public static final String NAME = "thumbnail";

    private static final String THUMBNAIL_RENDITION_NAME = "cq5dam.thumbnail.319.319.png";

    private Cfg cfg;

    @Reference
    private MimeTypeHelper mimeTypeHelper;

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
    public boolean accepts(final Asset asset, final String propertyName) {
        return true;
    }

    @Override
    public String get(final Asset asset) {
        Rendition rendition = asset.getRendition(THUMBNAIL_RENDITION_NAME);

        if (rendition == null && asset.getImagePreviewRendition() != null) {
            rendition = asset.getImagePreviewRendition();
        }

        // Ensure the rendition is of mime/type image; else the thumbnail will not be able to render
        if (rendition != null && mimeTypeHelper.isBrowserSupportedImage(rendition.getMimeType())) {
            String path = StringUtils.replace(rendition.getPath(), " ", "%20");
            path = StringUtils.replace(path, "/jcr:content", "/_jcr_content");
            return path;
        }

        return "";
    }

    @Activate
    protected void activate(final Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Thumbnail Rendition")
    public @interface Cfg {
        @AttributeDefinition(name = "Label", description = "Human read-able label.")
        String label() default LABEL;

        @AttributeDefinition(
                name = "Types",
                description = "Defines the type of data this exposes. This classification allows for intelligent exposure of Computed Properties in DataSources, etc.")
        String[] types() default { Types.RENDITION, Types.VIDEO_RENDITION };
    }
}
