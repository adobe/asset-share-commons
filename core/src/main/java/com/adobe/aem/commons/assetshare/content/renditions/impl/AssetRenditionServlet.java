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
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
    public static final String DOWNLOAD_AS_ATTACHMENT_SUFFIX_SEGMENT = "download";
    public static final String CACHEABLE_SUFFIX_FILENAME = "asset.rendition";

    @Reference
    private AssetRenditions assetRenditions;

    public final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
        final AssetRenditionDispatcher.Params params = new ParamsImpl(request);

        if (params.isValid()) {
            for (final AssetRenditionDispatcher assetRenditionDispatcher : assetRenditions.getAssetRenditionDispatchers()) {
                if (assetRenditionDispatcher.accepts(request, params.getRenditionName())) {

                    setResponseHeaders(response, params);

                    assetRenditionDispatcher.dispatch(request, response);
                    return;
                }
            }
        } else {
            log.debug("Request suffix [ {} ] has invalid 'suffix extension'", request.getRequestPathInfo().getSuffix());
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND,
                "Unable to locate a AssetRenditionDispatcher to dispatch a rendition for [" + params.getRenditionName() + "].");
    }

    protected void setResponseHeaders(final SlingHttpServletResponse response, final AssetRenditionDispatcher.Params params) {
        if (params.isAttachment()) {
            response.setHeader("Content-Disposition", String.format("attachment; filename=%s", params.getFileName()));
        } else {
            response.setHeader("Content-Disposition", String.format("filename=%s", params.getFileName()));
        }
    }

    /**
     * Represents the parameters provided in the RequestPathInfo's suffix to determine how the rendition is selected and returned.
     */
    protected static class ParamsImpl implements AssetRenditionDispatcher.Params {
        private String renditionName = null;
        private String fileName = null;
        private boolean attachment = false;
        private boolean valid = true;

        public ParamsImpl(SlingHttpServletRequest request) {
            final Asset asset = DamUtil.resolveToAsset(request.getResource());
            final String[] segments = StringUtils.split(request.getRequestPathInfo().getSuffix(), "/");

            if (asset == null ||
                    segments.length < 2 ||
                    (!CACHEABLE_SUFFIX_FILENAME.equals(segments[segments.length - 1]) &&
                            !StringUtils.startsWith(segments[segments.length - 1], CACHEABLE_SUFFIX_FILENAME + "."))) {
                valid = false;
            } else {
                if (segments.length > 0) {
                    renditionName = StringUtils.stripToNull(StringUtils.substringBefore(segments[0], "."));
                }

                attachment = ArrayUtils.indexOf(segments, DOWNLOAD_AS_ATTACHMENT_SUFFIX_SEGMENT) > 0;

                final int dotIndex = StringUtils.lastIndexOf(asset.getName(), ".");

                if (dotIndex < 0) {
                    fileName = asset.getName() + "." + renditionName;
                } else {
                    fileName = asset.getName().substring(0, dotIndex) + "." + renditionName + asset.getName().substring(dotIndex);
                }
            }
        }

        @Override
        public String getRenditionName() {
            return renditionName;
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public boolean isAttachment() {
            return attachment;
        }

        @Override
        public boolean isValid() {
            return valid;
        }
    }
}


