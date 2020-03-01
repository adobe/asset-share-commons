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
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.RenditionPicker;
import com.day.cq.dam.commons.util.DamUtil;
import com.google.common.io.ByteStreams;
import org.apache.jackrabbit.api.binary.BinaryDownload;
import org.apache.jackrabbit.api.binary.BinaryDownloadOptions;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(
        property = {
                SERVICE_RANKING + ":Integer=" + -20000
        }
)
@Designate(
        ocd = StaticRenditionDispatcherImpl.Cfg.class,
        factory = true
)
public class StaticRenditionDispatcherImpl extends AbstractRenditionDispatcherImpl implements AssetRenditionDispatcher {
    private static Logger log = LoggerFactory.getLogger(StaticRenditionDispatcherImpl.class);

    private static final String OSGI_PROPERTY_VALUE_DELIMITER = "=";

    private Cfg cfg;

    private ConcurrentHashMap<String, Pattern> mappings;

    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY
    )
    private volatile AssetRenditions assetRenditions;

    @Reference
    private transient RequireAem requireAem;

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
    public boolean isHidden() {
        return cfg.hidden();
    }

    @Override
    public Set<String> getRenditionNames() {
        if (mappings == null) {
            return Collections.EMPTY_SET;
        } else {
            return mappings.keySet();
        }
    }

    @Override
    public List<String> getTypes() {
        if (cfg.types() != null) {
            return Arrays.asList(cfg.types());
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public void dispatch(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
        final Asset asset = DamUtil.resolveToAsset(request.getResource());
        final AssetRenditionParameters parameters = new AssetRenditionParameters(request);

        final Rendition rendition = asset.getRendition(new PatternRenditionPicker(mappings.get(parameters.getRenditionName())));

        if (requireAem.isRunningInAdobeCloud()) {
            dispatchFromAemCloud(rendition, parameters, response);
        } else {
            dispatchFromAem(rendition, parameters, response);
        }
    }

    private void dispatchFromAem(Rendition rendition, AssetRenditionParameters parameters, SlingHttpServletResponse response) throws IOException {
        if (rendition != null) {
            log.debug("Streaming rendition [ {} ] for resolved rendition name [ {} ]", rendition.getPath(), parameters.getRenditionName());

            response.setHeader("Content-Type", rendition.getMimeType());
            response.setHeader("Content-Length", String.valueOf(rendition.getSize()));

            ByteStreams.copy(rendition.getStream(), response.getOutputStream());

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not serve static asset rendition.");
        }
    }

    private void dispatchFromAemCloud(Rendition rendition, AssetRenditionParameters parameters, SlingHttpServletResponse response) throws IOException, ServletException {
        if (rendition != null && rendition.getBinary() != null && rendition.getBinary() instanceof BinaryDownload) {
            log.debug("Redirecting rendition [ {} ] for resolved rendition name [ {} ] from the Adobe Cloud", rendition.getPath(), parameters.getRenditionName());

            final BinaryDownload binaryDownload = (BinaryDownload) rendition.getBinary();

            BinaryDownloadOptions downloadOptions;

            if (parameters.isDownload()) {
                // Mark disposition type as Attachment, to invoke download in browser
                downloadOptions = BinaryDownloadOptions.builder()
                        .withMediaType(rendition.getMimeType())
                        .withFileName(parameters.getFileName())
                        .withDispositionTypeAttachment()
                        .build();
            } else {
                // Mark disposition type as Inline, to invoke native browser viewer
                downloadOptions = BinaryDownloadOptions.builder()
                        .withMediaType(rendition.getMimeType())
                        .withFileName(parameters.getFileName())
                        .withDispositionTypeInline()
                        .build();
            }

            final URI uri;
            try {
                uri = binaryDownload.getURI(downloadOptions);

                if (uri != null) {
                    response.sendRedirect(uri.toString());
                } else {
                    log.debug("Unable to get Adobe Cloud redirect for rendition [ {} ]", rendition.getPath());
                }
            } catch (RepositoryException e) {
                throw new ServletException("Unable to dispatch static rendition request to Adobe Cloud", e);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not serve static asset rendition.");
        }
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;

        this.mappings = super.parseMappingsAsPatterns(cfg.rendition_mappings());
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Rendition Dispatcher - Static Renditions")
    public @interface Cfg {
        @AttributeDefinition
        String webconsole_configurationFactory_nameHint() default "{name} [ {label} ] @ {service.ranking}";

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
                name = "Rendition types",
                description = "The types of renditions this configuration will return. Ideally all renditions in this configuration apply types specified here. This is used to drive and scope the Asset Renditions displays in Authoring datasources. OOTB types are: `image` and `video`"
        )
        String[] types() default {};

        @AttributeDefinition(
                name = "Hide renditions",
                description = "Hide if this AssetRenditionDispatcher configuration is not intended to be exposed to AEM authors for selection in dialogs."
        )
        boolean hidden() default false;

        @AttributeDefinition(
                name = "Static rendition mappings",
                description = "In the form: <renditionName>" + OSGI_PROPERTY_VALUE_DELIMITER + "<renditionPickerPattern>"
        )
        String[] rendition_mappings() default {};

        @AttributeDefinition(
                name = "Service ranking",
                description = "The larger the number, the higher the precedence."
        )
        int service_ranking() default 0;
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
