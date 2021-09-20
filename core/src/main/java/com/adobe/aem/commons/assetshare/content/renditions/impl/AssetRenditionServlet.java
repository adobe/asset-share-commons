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

package com.adobe.aem.commons.assetshare.content.renditions.impl;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * /content/dam/foo.png.renditions/thumbnail/download/asset.rendition
 * <p>
 * URL is in the format:
 * <p>
 * HTTP GET &lt;absolute asset path&lt;.renditions/&lt;rendition id&gt;/&lt;optional-download&gt;/asset.rendition
 * <p>
 * Examples:
 * <p>
 * HTTP GET /content/dam/asset-share-commons/en/public/pictures/lilly-rum-250927.jpg.renditions/web/asset.rendition
 * HTTP GET /content/dam/asset-share-commons/en/public/pictures/ira.png.renditions/web/download/asset.rendition
 */
@Component(
        service = {Servlet.class},
        property = {
                "sling.servlet.methods=GET",
                "sling.servlet.resourceTypes=dam:Asset",
                "sling.servlet.extensions=" + AssetRenditionServlet.SERVLET_EXTENSION
        }
)
@Designate(ocd = AssetRenditionServlet.Cfg.class)
public class AssetRenditionServlet extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionServlet.class);

    public static final String SERVLET_EXTENSION = "renditions";

    @Reference
    private transient AssetRenditionDispatchers assetRenditionDispatchers;

    private transient Set allowedParameters = new HashSet();

    public final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
        try {
            final AssetRenditionParameters parameters = new AssetRenditionParameters(request);

            if (acceptsAssetRenditionParameters(parameters)) {
                for (final AssetRenditionDispatcher assetRenditionDispatcher : assetRenditionDispatchers.getAssetRenditionDispatchers()) {
                    if (acceptedByAssetRenditionDispatcher(assetRenditionDispatcher, parameters)) {

                        setResponseHeaders(response, parameters);

                        assetRenditionDispatcher.dispatch(request, response);
                        return;
                    }
                }

                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Unable to resolve an AssetRenditionDispatcher that can dispatch to a rendition.");

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported suffix parameters detected.");
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request URL format for AssetRenditionServlet.", e);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR , "Invalid request URL format for AssetRenditionServlet.");
        }
    }

    protected boolean acceptedByAssetRenditionDispatcher(final AssetRenditionDispatcher assetRenditionDispatcher, final AssetRenditionParameters parameters) {
        if (assetRenditionDispatcher.getRenditionNames() == null ||
                assetRenditionDispatchers == null ||
                StringUtils.isBlank(parameters.getRenditionName())) {
            return false;
        } else {
            return assetRenditionDispatcher.getRenditionNames().contains(parameters.getRenditionName()) &&
                    (assetRenditionDispatcher.getTypes().isEmpty() || assetRenditionDispatcher.getTypes().contains(parameters.getAssetType()));
        }
    }

    protected boolean acceptsAssetRenditionParameters(final AssetRenditionParameters assetRenditionParameters) {
        return !assetRenditionParameters.getParameters().stream().
                parallel().
                anyMatch(assetRenditionParameter -> !allowedParameters.contains(assetRenditionParameter));
    }

    protected void setResponseHeaders(final SlingHttpServletResponse response, final AssetRenditionParameters parameters) {
        if (parameters.isDownload()) {
            response.setHeader("Content-Disposition", String.format("attachment; filename=%s", parameters.getFileName()));
        } else {
            response.setHeader("Content-Disposition", String.format("filename=%s", parameters.getFileName()));
        }
    }

    @Activate
    protected void activate(Cfg cfg) {
        if (cfg.allowed_suffix_parameters() != null) {
            this.allowedParameters = new HashSet<>(Arrays.asList(cfg.allowed_suffix_parameters()));
        } else {
            this.allowedParameters = Collections.EMPTY_SET;
        }
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Asset Rendition Servlet")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Allowed suffix parameters",
                description = "Only accept requests to this servlet that contain any sub-set of these parameters. Any request that includes suffix parameters that are NOT in this list will be rejected by the servlet. Leave blank to allow any and all suffix parameters. Suffix parameters are any suffix segments between (exclusive) the first and last suffix segments."
        )
        String[] allowed_suffix_parameters() default {"download"};
    }
}