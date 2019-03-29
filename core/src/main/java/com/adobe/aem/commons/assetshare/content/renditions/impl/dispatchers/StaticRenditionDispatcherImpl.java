/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.RenditionPicker;
import com.day.cq.dam.commons.util.DamUtil;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(
        property = {
                SERVICE_RANKING + ":Integer=" + -100000,
                "webconsole.configurationFactory.nameHint={name} [ {label} ]"
        }
)
@Designate(
        ocd = StaticRenditionDispatcherImpl.Cfg.class,
        factory = true
)
public class StaticRenditionDispatcherImpl implements AssetRenditionDispatcher {
    private static final String OSGI_PROPERTY_VALUE_DELIMITER = "=";

    private static Logger log = LoggerFactory.getLogger(StaticRenditionDispatcherImpl.class);

    private Cfg cfg;

    private ConcurrentHashMap<String, Pattern> mappings;

    @Reference
    private AssetRenditions assetRenditions;

    @Override
    public String getLabel() {
        return cfg.label();
    }

    @Override
    public String getName() {
        return cfg.name();
    }

    @Override
    public Map<String, String> getOptions() {
        return assetRenditions.getOptions(mappings);
    }

    @Override
    public boolean accepts(final SlingHttpServletRequest request, final String renditionName) {
        return getOptions().values().contains(renditionName);
    }

    @Override
    public void dispatch(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        final Asset asset = DamUtil.resolveToAsset(request.getResource());
        final String renditionName = assetRenditions.getRenditionName(request);

        Rendition rendition = asset.getRendition(new PatternRenditionPicker(mappings.get(renditionName)));

        if (rendition != null) {

            log.debug("Streaming rendition [ {} ] for resolved rendition name [ {} ]", rendition.getPath(), renditionName);

            response.setHeader("Content-Type", rendition.getMimeType());
            response.setHeader("Content-Length", String.valueOf(rendition.getSize()));

            ByteStreams.copy(rendition.getStream(), response.getOutputStream());

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not serve static asset rendition.");
        }
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;

        this.mappings = new ConcurrentHashMap<>();

        for (final String mapping : this.cfg.rendition_mappings()) {
            final String[] segments = StringUtils.split(mapping, OSGI_PROPERTY_VALUE_DELIMITER);

            if (segments.length == 2) {
                mappings.put(StringUtils.strip(segments[0]),
                        Pattern.compile(StringUtils.strip(segments[1])));
            }
        }
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Rendition Dispatcher - Static Renditions")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Name",
                description = "The system name of this Rendition Dispatcher. This should be unique across all AssetRenditionDispatcher instances."
        )
        String name() default "static";

        @AttributeDefinition(
                name = "Label",
                description = "The human-friendly name of this AssetRenditionDispatcher and may be displayed to authors."
        )
        String label() default "Static Renditions";

        @AttributeDefinition(
                name = "Static rendition mappings",
                description = "In the form: <renditionName>" + OSGI_PROPERTY_VALUE_DELIMITER + "<renditionPickerPattern>"
        )
        String[] rendition_mappings() default {
                "web=^cq5dam\\.web\\.\\d+\\.\\d+\\.[a-z]+$",
                "thumbnail=^cq5dam\\.thumbnail\\.140\\.100\\.[a-z]+$",
                "original=^original$"
        };
    }

    /**
     * RenditionPicker that picks the first rendition that matches the provided pattern.
     * <p>
     * If no matching rendition is found, then null is returned.
     */
    protected class PatternRenditionPicker implements RenditionPicker {
        private final Pattern pattern;

        public PatternRenditionPicker(Pattern pattern) {
            this.pattern = pattern;
        }

        /**
         * @param asset the asset whose renditions should be searched.
         *
         * @return the rendition whose name matches the provided pattern, or null if non match.
         */
        @Override
        public Rendition getRendition(Asset asset) {
            if (pattern == null) {
                return null;
            }

            return asset.getRenditions().stream()
                    .filter(r -> pattern.matcher(r.getName()).matches())
                    .findFirst()
                    .orElse(null);
        }
    }
}
