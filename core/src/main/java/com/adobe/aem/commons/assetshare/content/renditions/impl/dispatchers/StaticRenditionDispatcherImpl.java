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

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.*;
import com.adobe.aem.commons.assetshare.content.renditions.download.impl.AssetRenditionDownloadRequest;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.RenditionPicker;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final Logger log = LoggerFactory.getLogger(StaticRenditionDispatcherImpl.class);

    private static final String OSGI_PROPERTY_VALUE_DELIMITER = "=";

    private Cfg cfg;

    private ConcurrentHashMap<String, Pattern> mappings;

    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY
    )
    private volatile AssetRenditions assetRenditions;

    @Reference(
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY
    )
    private volatile AssetRenditionTracker assetRenditionTracker;

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

        final Rendition rendition = findRendition(asset, parameters);

        if (rendition != null) {
            if (log.isDebugEnabled()) {
                log.debug("Serving internal static rendition [ {} ] with resolved rendition name [ {} ] through internal Sling Forward",
                        rendition.getPath(),
                        parameters.getRenditionName());
            }

            if (assetRenditionTracker != null) {
                assetRenditionTracker.track(this, request, parameters, rendition.getPath());
            }

            response.setHeader("Content-Type", rendition.getMimeType());

            request.getRequestDispatcher(rendition.adaptTo(Resource.class)).include(
                   new AssetRenditionDownloadRequest(request,
                           "GET",
                           rendition.adaptTo(Resource.class),
                           new String[]{},
                           null,
                           ""), response);

        } else {
            throw new ServletException(String.format("Cloud not locate rendition [ %s ] for assets [ %s ]", parameters.getRenditionName(), asset.getPath()));
        }
    }

    @Override
    public AssetRendition getRendition(final AssetModel assetModel, final AssetRenditionParameters parameters) {
        final Rendition rendition = findRendition(assetModel.getAsset(), parameters);

        if (rendition != null) {
            if (log.isDebugEnabled()) {
                log.debug("Downloading asset rendition [ {} ] for resolved rendition name [ {} ]",
                        rendition.getPath(),
                        parameters.getRenditionName());
            }

            if (assetRenditionTracker != null) {
                assetRenditionTracker.track(this, assetModel, parameters, rendition.getPath());
            }

            return new AssetRendition(rendition.getPath(), rendition.getSize(), rendition.getMimeType());
        }

        return null;
    }

    @Override
    public boolean accepts(AssetModel assetModel, String renditionName) {
        return getRenditionNames().contains(renditionName);
    }

    private Rendition findRendition(final Asset asset, final AssetRenditionParameters parameters) {
        return asset.getRendition(new PatternRenditionPicker(mappings.get(parameters.getRenditionName())));
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
                description = "Hide if this AssetRenditionDispatcher configuration is not intended to be exposed to AEM authors for selection in dialogs.",
                type = AttributeType.BOOLEAN
        )
        boolean hidden() default false;

        @AttributeDefinition(
                name = "Static rendition mappings",
                description = "In the form: <renditionName>" + OSGI_PROPERTY_VALUE_DELIMITER + "<renditionPickerPattern>"
        )
        String[] rendition_mappings() default {};

        @AttributeDefinition(
                name = "Service ranking",
                description = "The larger the number, the higher the precedence.",
                type = AttributeType.INTEGER
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
