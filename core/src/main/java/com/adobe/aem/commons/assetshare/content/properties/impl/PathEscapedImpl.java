/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.adobe.aem.commons.assetshare.content.properties.ComputedProperty.DEFAULT_ASC_COMPUTED_PROPERTY_SERVICE_RANKING;

/**
 * This Computed Property returns a escaped path to the asset.
 *
 * This replaced the path/encoded Computed Property.
 *
 * The asset path is collected via the PathImpl Computed Property.
 */
@Component(
        service = ComputedProperty.class,
        property = {
                Constants.SERVICE_RANKING + "=" + DEFAULT_ASC_COMPUTED_PROPERTY_SERVICE_RANKING
        }
)
@Designate(ocd = PathEscapedImpl.Cfg.class)
public class PathEscapedImpl extends AbstractComputedProperty<String> {
    private static final String PATH_SEGMENT_DELIMITER = "/";

    public static final String LABEL = "Asset Path (for URLs)";
    public static final String NAME = "path/escaped";
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
        final String path = pathComputedProperty.get(asset, request);
        final String[] pathSegments = StringUtils.split(path, PATH_SEGMENT_DELIMITER);

        return PATH_SEGMENT_DELIMITER + Arrays.stream(pathSegments)
                .map(pathSegment -> UrlEscapers.urlPathSegmentEscaper().escape(pathSegment))
                .collect( Collectors.joining(PATH_SEGMENT_DELIMITER));
    }

    @Activate
    protected void activate(final Cfg cfg) {
        this.cfg = cfg;
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Computed Property - Asset Path (Escaped)")
    public @interface Cfg {
        @AttributeDefinition(name = "Label", description = "Human readable label.")
        String label() default LABEL;

        @AttributeDefinition(
                name = "Types",
                description = "Defines the type of data this exposes. This classification allows for intelligent exposure of Computed Properties in DataSources, etc.")
        String[] types() default { Types.URL, Types.RENDITION, Types.VIDEO_RENDITION };
    }
}
