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
import com.adobe.aem.commons.assetshare.util.impl.ExtensionOverrideRequestWrapper;
import com.day.cq.commons.PathInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.osgi.framework.Constants.SERVICE_RANKING;

@Component(
        property = {
                SERVICE_RANKING + ":Integer=" + -100000,
                "webconsole.configurationFactory.nameHint={name} [ {label} ]"
        }
)
@Designate(
        ocd = InternalRedirectRenditionDispatcherImpl.Cfg.class,
        factory = true
)
public class InternalRedirectRenditionDispatcherImpl implements AssetRenditionDispatcher {
    private static Logger log = LoggerFactory.getLogger(InternalRedirectRenditionDispatcherImpl.class);

    private static final String OSGI_PROPERTY_VALUE_DELIMITER = "=";

    private Cfg cfg;

    private ConcurrentHashMap<String, String> mappings;

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
    public void dispatch(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
        final String renditionName = assetRenditions.getRenditionName(request);

        final String expression = mappings.get(renditionName);

        if (StringUtils.isNotBlank(expression)) {
            final String evaluatedExpression = assetRenditions.evaluateExpression(request, expression);
            final PathInfo pathInfo = new PathInfo(request.getResourceResolver(), evaluatedExpression);

            log.debug("Serving internal redirect rendition [ {} ] for resolved rendition name [ {} ]",
                    evaluatedExpression,
                    renditionName);

            final RequestDispatcherOptions options = new RequestDispatcherOptions();
            options.setReplaceSelectors(StringUtils.removeStart(pathInfo.getSelectorString(), "."));
            options.setReplaceSuffix(pathInfo.getSuffix());

            request.getRequestDispatcher(request.getResourceResolver().getResource(pathInfo.getResourcePath()), options)
                    .include(new ExtensionOverrideRequestWrapper(request, pathInfo.getExtension()), response);

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Could not serve asset rendition.");
        }
    }

    @Activate
    protected void activate(Cfg cfg) {
        this.cfg = cfg;

        this.mappings = new ConcurrentHashMap<>();

        if (this.cfg.rendition_mappings() != null) {
            for (final String mapping : this.cfg.rendition_mappings()) {
                final String[] segments = StringUtils.split(mapping, OSGI_PROPERTY_VALUE_DELIMITER);

                if (segments.length == 2) {
                    mappings.put(StringUtils.strip(segments[0]), StringUtils.strip(segments[1]));
                }
            }
        }
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Rendition Dispatcher - Internal Redirect Renditions")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Name",
                description = "The system name of this Rendition Dispatcher. This should be unique across all AssetRenditionDispatcher instances."
        )
        String name() default "internal-redirect";

        @AttributeDefinition(
                name = "Label",
                description = "The human-friendly name of this AssetRenditionDispatcher and may be displayed to authors."
        )
        String label() default "Internal Redirect Renditions";

        @AttributeDefinition(
                name = "Rendition mappings",
                description = "In the form: <renditionName>" + OSGI_PROPERTY_VALUE_DELIMITER + "<postFix>"
        )
        String[] rendition_mappings() default {};
    }
}