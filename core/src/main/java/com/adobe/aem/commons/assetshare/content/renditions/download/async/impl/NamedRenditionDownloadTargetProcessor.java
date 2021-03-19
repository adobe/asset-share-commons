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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.cq.dam.download.api.DownloadApiFactory;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadFile;
import com.adobe.cq.dam.download.api.DownloadTarget;
import com.adobe.cq.dam.download.spi.DownloadTargetProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;

@Component
public class NamedRenditionDownloadTargetProcessor implements DownloadTargetProcessor {
    private static final Logger log = LoggerFactory.getLogger(NamedRenditionDownloadTargetProcessor.class);

    public static final String TARGET_TYPE = "asset-share-commons__named-rendition";
    public static final String PARAM_ASSET_PATH = "path";
    public static final String PARAM_RENDITION_NAME = "renditionName";
    public static final String PARAM_ARCHIVE_NAME = "archiveName";
    public static final String PARAM_ARCHIVE_PATH = "archivePath";
    public static final String PARAM_RENDITION_BY_ASSET_FOLDER = "groupRenditionsByAssetFolder";

    @Reference
    private transient AssetRenditionDispatchers assetRenditionDispatchers;

    @Reference
    private DownloadApiFactory apiFactory;

    @Reference
    private MimeTypeService mimeService;

    @Override
    public Collection<DownloadFile> processTarget(DownloadTarget target, ResourceResolver resourceResolver) throws DownloadException {
        final List<DownloadFile> downloadFiles = new ArrayList<>();

        final String path = target.getParameter(PARAM_ASSET_PATH, String.class);
        final String renditionName = target.getParameter(PARAM_RENDITION_NAME, String.class);
        final String archiveName = target.getParameter(PARAM_ARCHIVE_NAME, String.class);

        final Resource resource = resourceResolver.getResource(path);
        final AssetModel assetModel = resource.adaptTo(AssetModel.class);
        final AssetRenditionParameters assetRenditionParameters = new AssetRenditionParameters(assetModel, renditionName);

        final boolean groupRenditionsByAssetFolder = target.getParameter(PARAM_RENDITION_BY_ASSET_FOLDER, true);

        for (final AssetRenditionDispatcher assetRenditionDispatcher : assetRenditionDispatchers.getAssetRenditionDispatchers()) {

            if (assetRenditionDispatcher.accepts(assetModel, renditionName)) {
                log.debug("Setting DownloadTarget for [ {} ] using AssetRenditionDispatcher [ {} ]", assetModel.getPath(), assetRenditionDispatcher.getName());

                final AssetRendition assetRendition = assetRenditionDispatcher.getRendition(assetModel, assetRenditionParameters);

                if (assetRendition != null) {
                    log.debug("Obtained AssetRendition [ {} ] details for [ {} ]", assetRendition.getBinaryUri(), assetModel.getPath());

                    final Map<String, Object> downloadFileParameters = new HashMap<>();

                    downloadFileParameters.put(PARAM_ARCHIVE_NAME, archiveName);
                    downloadFileParameters.put(PARAM_ARCHIVE_PATH, getArchivePath(groupRenditionsByAssetFolder,
                            true,
                            assetModel,
                            renditionName,
                            assetRendition.getMimeType()));
                    downloadFiles.add(apiFactory.createDownloadFile(assetRendition.getSize(),
                            assetRendition.getBinaryUri(),
                            downloadFileParameters));
                } else {
                    log.debug("Unable to obtain AssetRendition details for [ {} ] from AssetDispatcher [ {} ]", assetModel.getPath(), assetRenditionDispatcher.getClass().getName());
                }

                break;
            } else {
                log.debug("assetRenditionDispatcher [ {} ] does not accept AssetModel [ {} ] and renditionName [ {} ]", this.getClass().getName(), assetModel.getPath(), renditionName);
            }
        }

        return downloadFiles;
    }

    @Override
    public String getTargetType() {
        return TARGET_TYPE;
    }

    @Override
    public Map<String, Boolean> getValidParameters() {
        final Map<String, Boolean> validParameters = new HashMap<>();

        validParameters.put(PARAM_ASSET_PATH, true);
        validParameters.put(PARAM_RENDITION_NAME, true);
        validParameters.put(PARAM_ARCHIVE_NAME, true);
        validParameters.put(PARAM_ARCHIVE_PATH, false);

        return validParameters;
    }

    private String getArchivePath(final boolean groupRenditionsByAssetFolder,
                                  final boolean includeRenditionName,
                                  final AssetModel assetModel,
                                  final String renditionName,
                                  final String mimeType) {
        final String assetNameWithoutExtension = StringUtils.substringBeforeLast(assetModel.getName(), ".");

        String folder = "";
        String fileName = "";

        if (groupRenditionsByAssetFolder && StringUtils.isNotBlank(assetNameWithoutExtension)) {
            folder = assetNameWithoutExtension + "/";
        }

        if (includeRenditionName) {
            fileName = StringUtils.substringBeforeLast(assetModel.getName(), ".") + " (" + renditionName + ")." + mimeService.getExtension(mimeType);
        } else {
            fileName = StringUtils.substringBeforeLast(assetModel.getName(), ".") + "." + mimeService.getExtension(mimeType);
        }

        return folder + fileName;
    }
}