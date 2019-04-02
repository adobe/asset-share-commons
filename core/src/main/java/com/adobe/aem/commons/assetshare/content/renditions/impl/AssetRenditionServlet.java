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
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.CollectionUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

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
public class AssetRenditionServlet extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionServlet.class);

    public static final String SERVLET_EXTENSION = "renditions";

    @Reference
    private AssetRenditions assetRenditions;

    public final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
        try {
            final AssetRenditionParameters parameters = new AssetRenditionParameters(request);

            for (final AssetRenditionDispatcher assetRenditionDispatcher : assetRenditions.getAssetRenditionDispatchers()) {
                if (accepts(assetRenditionDispatcher, parameters)) {

                    setResponseHeaders(response, parameters);

                    assetRenditionDispatcher.dispatch(request, response);
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            log.debug("Invalid request parameters for AssetRenditionServlet", e);
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                "Unable to locate a AssetRenditionDispatcher which can dispatch an appropriate rendition.");
    }

    protected boolean accepts(final AssetRenditionDispatcher assetRenditionDispatcher, final AssetRenditionParameters parameters) {
        if (assetRenditionDispatcher.getRenditionNames() == null ||
                assetRenditions == null ||
                StringUtils.isBlank(parameters.getRenditionName())) {
            return false;
        } else {
            return assetRenditionDispatcher.getRenditionNames().contains(parameters.getRenditionName());
        }
    }

    protected void setResponseHeaders(final SlingHttpServletResponse response, final AssetRenditionParameters parameters) {
        if (parameters.isDownload()) {
            response.setHeader("Content-Disposition", String.format("attachment; filename=%s", parameters.getFileName()));
        } else {
            response.setHeader("Content-Disposition", String.format("filename=%s", parameters.getFileName()));
        }
    }
}


