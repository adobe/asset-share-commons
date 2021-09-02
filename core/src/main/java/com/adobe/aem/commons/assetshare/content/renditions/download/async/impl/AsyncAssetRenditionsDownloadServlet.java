/*
 * Asset Share Commons
 *
 * Copyright (C) 2021 Adobe
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

package com.adobe.aem.commons.assetshare.content.renditions.download.async.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.cq.dam.download.api.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;
import java.util.*;

import static com.adobe.aem.commons.assetshare.content.renditions.download.async.impl.NamedRenditionDownloadTargetProcessor.*;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.methods=POST",
                "sling.servlet.resourceTypes=asset-share-commons/actions/download",
                "sling.servlet.selectors=download-asset-renditions",
                "sling.servlet.extensions=zip",
                "sling.servlet.extensions=json"
        }
)
public class AsyncAssetRenditionsDownloadServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(AsyncAssetRenditionsDownloadServlet.class);

    private static final String DOWNLOAD_ASSETS = "assets";
    private static final String DOWNLOAD_ASSET_COUNT = "assetCount";
    private static final String DOWNLOAD_RENDITION_COUNT = "renditionCount";
    private static final String DOWNLOAD_ID = "id";

    private static final String REQ_KEY_TIME_ZONE = "timezone";
    private static final String REQ_KEY_ASSET_PATHS = "path";
    private static final String REQ_KEY_RENDITION_NAMES = "renditionName";

    private static final String PN_ALLOWED_RENDITION_NAMES = "allowedRenditionNames";
    public static final String PN_BASE_ARCHIVE_NAME_EXPRESSION = "archiveNameExpression";

    public static final String PARAM_ARCHIVE_NAME = "archiveName";
    public static final String PARAM_RENDITION_BY_ASSET_FOLDER = "groupRenditionsByAssetFolder";

    private static final String DOWNLOAD_ARCHIVE_NAME = PARAM_ARCHIVE_NAME;
    private static final String ZIP_EXTENSION = ".zip";

    @Reference(target="(distribution=cloud-ready)")
    private transient RequireAem requireAem;

    @Reference
    private transient ActionHelper actionHelper;

    @Reference
    private transient DownloadService downloadService;

    @Reference
    private transient DownloadApiFactory apiFactory;

    @Reference
    private transient ExpressionEvaluator expressionEvaluator;

    @Override
    protected final void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        final ValueMap componentProperties = request.getResource().getValueMap();

        final Collection<AssetModel> assetModels = actionHelper.getAssetsFromQueryParameter(request, REQ_KEY_ASSET_PATHS);

        final Collection<String> renditionNames = actionHelper.getAllowedValuesFromQueryParameter(request,
                REQ_KEY_RENDITION_NAMES,
                request.getResource().getValueMap().get(PN_ALLOWED_RENDITION_NAMES, new String[]{}));

        final String archiveName = evaluateArchiveName(
                componentProperties.get(PN_BASE_ARCHIVE_NAME_EXPRESSION, "Assets"),
                getZonedNowDateTime(ZonedDateTime.now(ZoneId.of("UTC")), request.getParameter(REQ_KEY_TIME_ZONE)),
                assetModels,
                renditionNames);

        boolean groupRenditionsByAssetFolder = assetModels.size() > 1 && renditionNames.size() > 1;

        DownloadManifest manifest = apiFactory.createDownloadManifest();

        for (final AssetModel assetModel : assetModels) {
            addToDownloadManifest(assetModel, renditionNames, manifest, request.getResource(), archiveName, groupRenditionsByAssetFolder);
        }

        try {
            final String downloadId = downloadService.download(manifest, request.getResourceResolver());

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(getResponseJson(downloadId,
                    assetModels,
                    manifest.getTargetCount(),
                    archiveName).toString());

        } catch (DownloadException e) {
            throw new ServletException("Unable to initiate download", e);
        }
    }

    private void addToDownloadManifest(final AssetModel asset,
                                                   final Collection renditionNames,
                                                   final DownloadManifest manifest,
                                                   final Resource downloadComponentResource,
                                                   final String archiveName,
                                                   final boolean groupRenditionsByAssetFolder) {

        renditionNames.forEach(renditionName -> {
            final Map<String, Object> renditionParameters = new HashMap();

            renditionParameters.put(PARAM_ASSET_PATH, asset.getPath());
            renditionParameters.put(PARAM_RENDITION_NAME, renditionName);
            renditionParameters.put(PARAM_ARCHIVE_NAME, archiveName);
            renditionParameters.put(PARAM_RENDITION_BY_ASSET_FOLDER, groupRenditionsByAssetFolder);
            renditionParameters.put(PARAM_DOWNLOAD_COMPONENT_PATH, downloadComponentResource.getPath());

            final DownloadTarget downloadTarget = apiFactory.createDownloadTarget(
                    NamedRenditionDownloadTargetProcessor.TARGET_TYPE,
                    renditionParameters);

            manifest.addTarget(downloadTarget);
        });
    }

    private JsonObject getResponseJson(final String downloadId, final Collection<AssetModel> assetModels, final int downloadRenditionCount, final String archiveName) {
        final JsonObject json = new JsonObject();

        final JsonArray assetsJsonArray = new JsonArray();

        assetModels.forEach(assetModel -> { assetsJsonArray.add(assetModel.getPath());});

        /** This JSON is considered and API - Do not remove/change key/vales **/

        json.addProperty(DOWNLOAD_ID, downloadId);
        json.add(DOWNLOAD_ASSETS, assetsJsonArray);
        json.addProperty(DOWNLOAD_ASSET_COUNT, assetModels.size());
        json.addProperty(DOWNLOAD_RENDITION_COUNT, downloadRenditionCount);
        json.addProperty(DOWNLOAD_ARCHIVE_NAME, archiveName);

        return json;
    }

    protected ZonedDateTime getZonedNowDateTime(ZonedDateTime utcNow, String timeZoneId) {
        if (StringUtils.isNotBlank(timeZoneId)) {
            try {
                return utcNow.withZoneSameInstant(ZoneId.of(timeZoneId));
            } catch (ZoneRulesException e) {
                log.warn("Time Zone Id [ {} ] invalid. Falling back to UTC [ {} ]", utcNow.getZone().getId());
            }
        }

        return utcNow;
    }

    private String evaluateArchiveName(String expression, ZonedDateTime now, Collection<AssetModel> assetModels, Collection<String> renditionNames) {
        expression = expressionEvaluator.evaluateAssetsRenditionsExpressions(expression, assetModels, renditionNames);
        expression = expressionEvaluator.evaluateDateTimeExpressions(expression, now);

        if (!StringUtils.endsWith(expression, ZIP_EXTENSION)) {
            expression += ZIP_EXTENSION;
        }

        return expression;
    }
}
