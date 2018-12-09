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

package com.adobe.aem.commons.assetshare.content.impl;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.RenditionPicker;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.methods=GET",
                "sling.servlet.resourceTypes=dam:Asset",
                "sling.servlet.extensions=rendition"
        }
)
@Designate(ocd = AssetRenditionServlet.Cfg.class)
public class AssetRenditionServlet extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionServlet.class);
    private transient Map<String, Pattern> renditionMappings = new ConcurrentHashMap();

    private Cfg cfg;

    public final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        final Resource assetResource = request.getResource();
        final Pattern pattern = getPattern(getRenditionPatternParam(request.getRequestPathInfo().getSuffix()));

        Rendition rendition = null;

        if (pattern != null) {
            final Asset asset = DamUtil.resolveToAsset(assetResource);
            rendition = asset.getRendition(new PatternRenditionPicker(pattern));
        }

        if (rendition != null) {
            log.debug("Streaming rendition [ {} ] using pattern [ {} ]", rendition.getPath(), pattern.pattern());

            streamRenditionToResponse(rendition, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Could not find an appropriate rendition");
        }
    }

    private Pattern getPattern(final String renditionPatternParam) {
        Pattern pattern = renditionMappings.get(renditionPatternParam);

        if (pattern != null) {
            log.debug("Found rendition pattern via suffix lookup [ {} ] -> [ {} ]", renditionPatternParam, pattern.pattern());
        }

        if (pattern == null && cfg.allow_adhoc()) {
            pattern = Pattern.compile(getExactRegex(renditionPatternParam));
            log.debug("Ad-hoc rendition patterns allowed; Using suffix pattern [ {} ]", pattern.pattern());
        }
        return pattern;
    }

    protected String getExactRegex(String regex) {
        if (!StringUtils.startsWith(regex, "^")) {
            regex = "^" + regex;
        }

        if (!StringUtils.endsWith(regex, "$")) {
            regex = regex + "$";
        }

        return regex;
    }

    protected String getRenditionPatternParam(final String suffix) {
        String tmp = StringUtils.substringBeforeLast(suffix, ".");
        tmp = StringUtils.stripToEmpty(tmp);
        return StringUtils.stripStart(tmp, "/");
    }

    protected void streamRenditionToResponse(final Rendition rendition,
                                             final SlingHttpServletResponse response) throws IOException {
        final InputStream input = rendition.getStream();
        final OutputStream output = response.getOutputStream();

        response.setContentType(rendition.getMimeType());
        response.setContentLength(Math.toIntExact(rendition.getSize()));

        try (
                final ReadableByteChannel inputChannel = Channels.newChannel(input);
                final WritableByteChannel outputChannel = Channels.newChannel(output);
        ) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(10240);

            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
        }
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;

        for (String mapping : this.cfg.rendition_mappings()) {
            String[] segments = StringUtils.split(mapping, "=");
            if (segments.length == 2) {
                renditionMappings.put(StringUtils.strip(segments[0]),
                        Pattern.compile(StringUtils.strip(segments[1])));
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        this.renditionMappings = new ConcurrentHashMap<>();
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Action Page Servlet")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Allow ad-hoc rendition selection",
                description = "Allow ad-hoc rendition select via rendition name patterns via URL suffix. If this is false, then only suffixes that map the rendition_mappings() are used.]"
        )
        boolean allow_adhoc() default true;

        @AttributeDefinition(
                name = "Rendition suffix mappings",
                description = "In the form: <renditionParam>=<renditionPickerPattern>]"
        )
        String[] rendition_mappings() default {
                "web=cq5dam\\.web\\.\\d+\\.\\d+\\.[a-z]+",
                "thumbnail=cq5dam\\.thumbnail\\.140\\.100\\.[a-z]+",
                "origin=original"
        };
    }

    /**
     * RenditionPicker that pics the first rendition that matches the provided pattern.
     *
     * If no matching rendition is found, then null is returned.
     */
    protected class PatternRenditionPicker implements RenditionPicker {
        private final Pattern pattern;

        public PatternRenditionPicker(Pattern pattern) {
            this.pattern = pattern;
        }

        /**
         * @param asset the asset whose renditions should be searched.
         * @return the rendition whose name matches the provided pattern, or null if non match.
         */
        @Override
        public Rendition getRendition(Asset asset) {
            return asset.getRenditions().stream()
                    .filter(r -> pattern.matcher(r.getName()).matches())
                    .findFirst()
                    .orElse(null);
        }
    }
}


