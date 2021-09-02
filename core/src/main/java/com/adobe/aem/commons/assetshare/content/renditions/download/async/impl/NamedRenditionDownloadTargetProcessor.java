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

import java.net.URI;
import java.util.*;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadArchiveNamer;
import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadTargetParameters;
import com.adobe.cq.dam.download.api.DownloadApiFactory;
import com.adobe.cq.dam.download.api.DownloadException;
import com.adobe.cq.dam.download.api.DownloadFile;
import com.adobe.cq.dam.download.api.DownloadTarget;
import com.adobe.cq.dam.download.spi.DownloadTargetProcessor;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
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
    public static final String PARAM_DOWNLOAD_COMPONENT_PATH = "downloadComponentPath";

    @Reference
    private transient AssetRenditionDispatchers assetRenditionDispatchers;

    @Reference
    private DownloadApiFactory apiFactory;

    @Reference
    private MimeTypeService mimeTypeService;

    @Reference(policyOption = ReferencePolicyOption.GREEDY, policy = ReferencePolicy.DYNAMIC)
    private volatile DownloadArchiveNamer downloadArchiveNamer;

    @Override
    public Collection<DownloadFile> processTarget(DownloadTarget target, ResourceResolver resourceResolver) throws DownloadException {
        final List<DownloadFile> downloadFiles = new ArrayList<>();

        final String path = target.getParameter(DownloadTargetParameters.ASSET_PATH.toString(), String.class);
        final String renditionName = target.getParameter(DownloadTargetParameters.RENDITION_NAME.toString(), String.class);
        final String archiveName = target.getParameter(DownloadTargetParameters.ARCHIVE_NAME.toString(), String.class);

        final Resource resource = resourceResolver.getResource(path);
        final AssetModel assetModel = resource.adaptTo(AssetModel.class);
        final AssetRenditionParameters assetRenditionParameters = new AssetRenditionParameters(assetModel, renditionName);

        for (final AssetRenditionDispatcher assetRenditionDispatcher : assetRenditionDispatchers.getAssetRenditionDispatchers()) {

            if (assetRenditionDispatcher.accepts(assetModel, renditionName)) {
                log.debug("Setting DownloadTarget for [ {} ] using AssetRenditionDispatcher [ {} ]", assetModel.getPath(), assetRenditionDispatcher.getName());

                final AssetRendition assetRendition = assetRenditionDispatcher.getRendition(assetModel, assetRenditionParameters);

                final Map<String, Object> downloadFileParameters = new HashMap<>();
                downloadFileParameters.put(PARAM_ARCHIVE_NAME, archiveName);

                if (assetRendition != null) {
                    log.debug("Obtained AssetRendition [ {} ] details for [ {} ]", assetRendition.getBinaryUri(), assetModel.getPath());

                    downloadFileParameters.put(PARAM_ARCHIVE_PATH, downloadArchiveNamer.getArchiveFilePath(assetModel, assetRendition, target));
                    downloadFiles.add(apiFactory.createDownloadFile(assetRendition.getSize(),
                            assetRendition.getBinaryUri(),
                            downloadFileParameters));
                } else {
                    downloadFileParameters.put(PARAM_ARCHIVE_PATH, downloadArchiveNamer.getArchiveFilePath(assetModel, AssetRendition.UNAVAILABLE_ASSET_RENDITION, target));

                    downloadFiles.add(apiFactory.createDownloadFile(Optional.of(0L),
                            URI.create("failed://to.resolve.asset.rendition.combination"),
                            downloadFileParameters));

                    log.debug("Unable to obtain AssetRendition details for [ {} ] from AssetDispatcher [ {} ]", assetModel.getPath(), assetRenditionDispatcher.getClass().getName());
                }

                // Stop processing assetRenditionDispatchers once one has accepted
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
        validParameters.put(PARAM_DOWNLOAD_COMPONENT_PATH, true);

        return validParameters;
    }
}