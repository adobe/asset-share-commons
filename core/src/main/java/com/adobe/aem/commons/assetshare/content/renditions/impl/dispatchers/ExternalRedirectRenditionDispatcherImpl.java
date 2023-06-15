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
import com.adobe.aem.commons.assetshare.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(
        property = {
                SERVICE_RANKING + ":Integer=" + -10000,
                "webconsole.configurationFactory.nameHint={name} [ {label} ] @ {service.ranking}"
        }
)
@Designate(
        ocd = ExternalRedirectRenditionDispatcherImpl.Cfg.class,
        factory = true
)
public class ExternalRedirectRenditionDispatcherImpl extends AbstractRenditionDispatcherImpl implements AssetRenditionDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ExternalRedirectRenditionDispatcherImpl.class);

    private static Long PLACEHOLDER_SIZE_IN_BYTES = 104857600L; // 100MB

    private Cfg cfg;

    private ConcurrentHashMap<String, String> mappings;

    @Reference
    private AssetRenditions assetRenditions;

    @Reference
    private MimeTypeService mimeTypeService;

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
        final AssetRenditionParameters parameters = new AssetRenditionParameters(request);

        final String expression = mappings.get(parameters.getRenditionName());
        final String renditionRedirect = assetRenditions.evaluateExpression(request, expression);

        if (StringUtils.isNotBlank(renditionRedirect)) {
            if (log.isDebugEnabled()) {
                log.debug("Serving External redirect rendition [ {} ] for resolved rendition name [ {} ]",
                        renditionRedirect,
                        parameters.getRenditionName());
            }

            if (assetRenditionTracker != null) {
                assetRenditionTracker.track(this, request, parameters, renditionRedirect);
            }

            if (cfg.redirect() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
                response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            } else {
                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            }

            response.setHeader("Location", UrlUtil.escape(renditionRedirect));
        } else {
            log.error("Could not convert [ {} ] into a valid URI", renditionRedirect);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not serve asset rendition.");
        }
    }

    @Override
    public AssetRendition getRendition(AssetModel assetModel, AssetRenditionParameters parameters) {
        final String expression = mappings.get(parameters.getRenditionName());
        String renditionRedirect = assetRenditions.evaluateExpression(assetModel, parameters.getRenditionName(), expression);

        if (StringUtils.isNotBlank(renditionRedirect)) {
            try {
                renditionRedirect = UrlUtil.escape(renditionRedirect, true);

                final String extension = getExtensionFromAscExtQueryParameter(renditionRedirect);

                renditionRedirect = cleanURI(renditionRedirect);

                if (log.isDebugEnabled()) {
                    log.debug("Downloading External redirect rendition [ {} ] for resolved rendition name [ {} ]",
                            renditionRedirect,
                            parameters.getRenditionName());
                }

                if (assetRenditionTracker != null) {
                    assetRenditionTracker.track(this, assetModel, parameters, renditionRedirect);
                }

                return new AssetRendition(renditionRedirect, PLACEHOLDER_SIZE_IN_BYTES, mimeTypeService.getMimeType(extension));
            } catch (URISyntaxException e) {
                log.warn("Unable to create a valid URI for rendition redirect [ {} ]", renditionRedirect, e);
                // Still sending to Async Download Framework so we can get a failure
                return new AssetRendition("failed://to.create.valid.uri.from.rendition.redirect", 0L, "invalid/uri");
            }
        }

        return null;
    }

    @Override
    public boolean accepts(AssetModel assetModel, String renditionName) {
        return getRenditionNames().contains(renditionName);
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;

        this.mappings = super.parseMappingsAsStrings(cfg.rendition_mappings());
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Rendition Dispatcher - External Redirect Renditions")
    public @interface Cfg {
        @AttributeDefinition
        String webconsole_configurationFactory_nameHint() default "{name} [ {label} ] @ {service.ranking}";

        @AttributeDefinition(
                name = "Name",
                description = "The system name of this Rendition Dispatcher. This should be unique across all AssetRenditionDispatcher instances."
        )
        String name() default "external-redirect";

        @AttributeDefinition(
                name = "Label",
                description = "The human-friendly name of this AssetRenditionDispatcher and may be displayed to authors."
        )
        String label() default "External Redirect Renditions";

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
                name = "Redirect",
                description = "Select the type of redirect that should be made: Moved Permanently (301) or Moved Temporarily (302). Defaults to 301.",
                options = {
                        @Option(label = "Moved Permanently (301)", value = "301"),
                        @Option(label = "Moved Temporarily (302)", value = "302"),
                }
        )
        int redirect() default HttpServletResponse.SC_MOVED_PERMANENTLY;

        @AttributeDefinition(
                name = "Rendition mappings",
                description = "In the form: <rendition name>" + OSGI_PROPERTY_VALUE_DELIMITER + "<redirect location>"
        )
        String[] rendition_mappings() default {};

        @AttributeDefinition(
                name = "Service ranking",
                description = "The larger the number, the higher the precedence.",
                type = AttributeType.INTEGER
        )
        int service_ranking() default 0;
    }
}