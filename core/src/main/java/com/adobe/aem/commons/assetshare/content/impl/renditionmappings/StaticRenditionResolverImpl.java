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

package com.adobe.aem.commons.assetshare.content.impl.renditionmappings;

import com.adobe.aem.commons.assetshare.content.RenditionResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetRenditionServlet;
import com.adobe.aem.commons.assetshare.util.impl.ExtensionOverrideRequestWrapper;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.RenditionPicker;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(
        property = {
                SERVICE_RANKING + "=" + -10000
        }
)
@Designate(ocd = StaticRenditionResolverImpl.Cfg.class)
public class StaticRenditionResolverImpl implements RenditionResolver {
    private static final String OSGI_PROPERTY_VALUE_DELIMITER = "=";

    private static Logger log = LoggerFactory.getLogger(StaticRenditionResolverImpl.class);

    private Cfg cfg;

    private ConcurrentHashMap<String, Pattern> mappings;

    @Override
    public Map<String, String> getOptions() {
        final Map<String, String> options = new LinkedHashMap<>();

        mappings.keySet().stream()
                .sorted()
                .forEach(key -> options.put(key, StringUtils.capitalize(key)));

        return options;
    }

    @Override
    public boolean accepts(SlingHttpServletRequest request, String renditionName) {
        return getOptions().keySet().contains(renditionName);
    }

    @Override
    public void dispatch(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
        final Asset asset = DamUtil.resolveToAsset(request.getResource());
        final String renditionName = getRenditionPatternParam(request.getRequestPathInfo().getSuffix());

        final Rendition rendition = asset.getRendition(new PatternRenditionPicker(mappings.get(renditionName)));

        if (rendition != null) {
            response.setHeader("Content-Type", rendition.getMimeType());

            final RequestDispatcherOptions options = new RequestDispatcherOptions();
            options.setReplaceSuffix("");
            options.setReplaceSelectors("");

            request.getRequestDispatcher(request.getResourceResolver().getResource(rendition.getPath()), options)
                    .include(new ExtensionOverrideRequestWrapper(request, null), response);

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not fine static asset rendition.");
        }
    }

    @Override
    public String getUrl(SlingHttpServletRequest request, String renditionName, Asset asset) {
        return asset.getPath() + "." + AssetRenditionServlet.SERVLET_EXTENSION + "/" + renditionName + ".img";
    }

    private String getRenditionPatternParam(final String suffix) {
        final String[] segments  = StringUtils.split(StringUtils.substringBeforeLast(suffix, "."), "/");

        if (segments.length > 0) {
            return segments[0];
        } else {
            return null;
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

    @ObjectClassDefinition(name = "Asset Share Commons - Rendition Resolver - Static Renditions")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Static rendition mappings",
                description = "In the form: <renditionName>" + OSGI_PROPERTY_VALUE_DELIMITER + "<renditionPickerPattern>]"
        )
        String[] rendition_mappings() default {
                "web=^cq5dam\\.web\\.\\d+\\.\\d+\\.[a-z]+$",
                "thumbnail=^cq5dam\\.thumbnail\\.140\\.100\\.[a-z]+$",
                "original=^original$"
        };
    }

    /**
     * RenditionPicker that pics the first rendition that matches the provided pattern.
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
         * @return the rendition whose name matches the provided pattern, or null if non match.
         */
        @Override
        public Rendition getRendition(Asset asset) {
            if (pattern == null) { return null; }

            return asset.getRenditions().stream()
                    .filter(r -> pattern.matcher(r.getName()).matches())
                    .findFirst()
                    .orElse(null);
        }
    }


     /*
    private void streamRenditionToResponse(final Rendition rendition,
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
    */
}
