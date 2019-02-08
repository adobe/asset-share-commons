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

import com.adobe.aem.commons.assetshare.content.RenditionResolver;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.osgi.Order;
import org.apache.sling.commons.osgi.RankedServices;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 *
 *
 * /content/dam/foo.png.rendition/download/asset.rendition
 *
 *
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.methods=GET",
                "sling.servlet.resourceTypes=dam:Asset",
                "sling.servlet.extensions=" + AssetRenditionServlet.SERVLET_EXTENSION
        },
        reference = {
                @Reference(
                        name = "renditionResolver",
                        bind = "bindRenditionResolver",
                        unbind = "unbindRenditionResolver",
                        service = RenditionResolver.class,
                        policy = ReferencePolicy.DYNAMIC,
                        policyOption = ReferencePolicyOption.GREEDY,
                        cardinality = ReferenceCardinality.MULTIPLE
                )
        }
)
public class AssetRenditionServlet extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionServlet.class);

    public static final String SERVLET_EXTENSION = "rendition";
    public static final String DOWNLOAD_AS_ATTACHMENT_SUFFIX_SEGMENT = "download";
    public static final String[] CACHEABLE_SUFFIX_FILENAMES = {"asset.rendition"};

    private final RankedServices<RenditionResolver> renditionResolvers = new RankedServices<>(Order.DESCENDING);

    protected void bindRenditionResolver(RenditionResolver service, Map<String, Object> props) {
        log.debug("Binding RenditionResolver [ {} ]", service.getClass().getName());
        renditionResolvers.bind(service, props);
    }

    protected void unbindRenditionResolver(RenditionResolver service, Map<String, Object> props) {
        log.debug("Unbinding RenditionResolver [ {} ]", service.getClass().getName());
        renditionResolvers.unbind(service, props);
    }

    public final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException, ServletException {
        final RenditionResolver.Params params = new ParamsImpl(request);

        if (params.isValid()) {
            for (final RenditionResolver renditionResolver : renditionResolvers) {
                if (renditionResolver.accepts(request, params.getRenditionName())) {

                    setResponseHeaders(response, params);

                    renditionResolver.dispatch(request, response);
                    return;
                }
            }
        } else {
            log.debug("Request suffix [ {} ] has invalid 'suffix extension'", request.getRequestPathInfo().getSuffix());
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unable to locate to handle serving rendition.");
    }

    protected void setResponseHeaders(final SlingHttpServletResponse response, final RenditionResolver.Params params) {
        if (params.isAttachment()) {
            response.setHeader("Content-Disposition", String.format("attachment; filename=%s", params.getFileName()));
        } else {
            response.setHeader("Content-Disposition", String.format("filename=%s", params.getFileName()));
        }
    }

    /**
     * Represents the parameters provided in the RequestPathInfo's suffix to determine how the rendition is selected and returned.
     */
    protected static class ParamsImpl implements RenditionResolver.Params {
        private String renditionName = null;
        private String fileName = null;
        private boolean attachment = false;
        private boolean valid = true;

        public ParamsImpl(SlingHttpServletRequest request) {
            final Asset asset = DamUtil.resolveToAsset(request.getResource());
            final String[] segments = StringUtils.split(request.getRequestPathInfo().getSuffix(), "/");

            if (asset == null ||
                    segments.length < 2 ||
                    !ArrayUtils.contains(CACHEABLE_SUFFIX_FILENAMES, segments[segments.length -1])) {
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


