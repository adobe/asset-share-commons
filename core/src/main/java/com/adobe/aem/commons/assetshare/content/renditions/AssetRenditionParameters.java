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

package com.adobe.aem.commons.assetshare.content.renditions;

import com.adobe.acs.commons.util.PathInfoUtil;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.properties.impl.AssetTypeImpl;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to represent the parameters used by the AssetRenditionServlet for two use cases:
 * <br>
 * - Building URLs that when requested will INVOKE the AssetRenditionsServlet
 * <br>
 * - Parsing parameters from Requests URLs for use IN the AssetRenditionsServlet
 */
public final class AssetRenditionParameters {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionParameters.class);

    // RESERVED SUFFIX TERMS
    public static final String DOWNLOAD = "download";
    public static final String CACHE_FILENAME = "asset.rendition";

    private final Asset asset;
    private final String assetType;
    private final String renditionName;
    private final String fileName;
    private final List<String> otherParameters;

    public AssetRenditionParameters(final SlingHttpServletRequest request) throws IllegalArgumentException {
        final String[] segments = PathInfoUtil.getSuffixSegments(request);

        // Get the asset associated with the request
        this.asset = DamUtil.resolveToAsset(request.getResource());

        // Get renditionName from FIRST suffix segment
        this.renditionName = PathInfoUtil.getFirstSuffixSegment(request);

        if (asset == null) {
            throw new IllegalArgumentException(String.format("Request resource [ %s ] cannot be resolved to an Asset", request.getResource().getPath()));
        } else if (segments.length < 2) {
            throw new IllegalArgumentException(String.format("Request must at least 2 suffix segments, found [ %d ]", segments.length));
        } else if (StringUtils.isBlank(renditionName)) {
            throw new IllegalArgumentException(String.format("Request does not have a rendition name in the first suffix segment"));
        } else if (!CACHE_FILENAME.equals(PathInfoUtil.getLastSuffixSegment(request))) {
            throw new IllegalArgumentException(String.format("Request's last suffix segment must be [ %s ]", CACHE_FILENAME));
        }

        this.assetType = getTypeFromAsset(this.asset, request.getResourceResolver());

        // Build the download filename (for Content-Disposition) from the asset node name and rendition name.
        this.fileName = buildFileName(asset, renditionName);

        // Other parameters are any optional parameters
        this.otherParameters = new ArrayList<>();

        for (int i = 1; i < segments.length - 1; i++) {
            this.otherParameters.add(segments[i]);
        }
    }

    public AssetRenditionParameters(final @Nonnull AssetModel assetModel, final @Nonnull String renditionName) throws IllegalArgumentException {
        this(assetModel, renditionName, false);
    }

    public AssetRenditionParameters(final @Nonnull AssetModel assetModel, final @Nonnull String renditionName, final boolean download) throws IllegalArgumentException {
        this(assetModel, renditionName, download, Collections.EMPTY_LIST);
    }

    public AssetRenditionParameters(final @Nonnull AssetModel assetModel, final @Nonnull String renditionName, final boolean download, final String... otherParameters) throws IllegalArgumentException {
        this(assetModel, renditionName, download, Arrays.asList(otherParameters));
    }

    public AssetRenditionParameters(final @Nonnull AssetModel assetModel, final @Nonnull String renditionName, final boolean download, final List<String> otherParameters) throws IllegalArgumentException {
        if (StringUtils.isBlank(renditionName)) {
            throw new IllegalArgumentException("Am asset is required");
        } else if (StringUtils.isBlank(renditionName)) {
            throw new IllegalArgumentException("A renditionName is required");
        }

        this.asset = DamUtil.resolveToAsset(assetModel.getResource());
        this.assetType = getAssetTypeFromAssetModel(assetModel);

        this.renditionName = renditionName;
        this.fileName = buildFileName(asset, renditionName);
        this.otherParameters = new ArrayList<>(otherParameters);
        if (download) {
            this.otherParameters.add(DOWNLOAD);
        }
    }

    public String getRenditionName() {
        return renditionName;
    }

    public boolean isDownload() {
        return otherParameters.contains(DOWNLOAD);
    }

    public String getFileName() {
        if (fileName == null) {
            log.debug("The fileName can only be derived from parameters sourced from a SlingHttpServletRequest");
        }
        return fileName;
    }

    public List<String> getParameters() {
        return new ArrayList<>(otherParameters);
    }

    protected String buildFileName(final Asset asset, final String renditionName) {
        String fileName;

        final String assetName = StringUtils.substringBeforeLast(asset.getName(), ".");

        if (StringUtils.lastIndexOf(asset.getName(), ".") < 0) {
            fileName = assetName + "." + renditionName;
        } else {
            fileName = assetName + "." + renditionName + "." + StringUtils.substringAfterLast(asset.getName(), ".");
        }

        return fileName;
    }

    private String getTypeFromAsset(Asset asset, ResourceResolver resourceResolver) {
        Resource assetRes = resourceResolver.getResource(asset.getPath());
        if(assetRes!=null) {
            AssetModel assetModel = assetRes.adaptTo(AssetModel.class);
            if(assetModel!=null) {
                return getAssetTypeFromAssetModel(assetModel);
            }
        }

        return StringUtils.EMPTY;
    }

    private String getAssetTypeFromAssetModel(AssetModel assetModel) {
        return StringUtils.lowerCase(assetModel.getProperties().get(AssetTypeImpl.NAME, ""));
    }

    public String getAssetType() {
        return assetType;
    }
}
