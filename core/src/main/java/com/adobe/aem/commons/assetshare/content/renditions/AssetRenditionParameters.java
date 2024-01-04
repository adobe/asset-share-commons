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
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.commons.util.DamUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

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

    public static final String PARAMETER_RENDITION_DISPLAY_NAME = "renditionDisplayName";

    private final Asset asset;
    private final String renditionName;
    private final String fileName;
    private final ValueMap otherParameters = new ValueMapDecorator(new HashMap<>());
    private final ValueMap otherProperties = new ValueMapDecorator(new HashMap<>());

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

        // Build the download filename (for Content-Disposition) from the asset node name and rendition name.
        this.fileName = buildFileName(asset, renditionName);

        // Other parameters are any optional parameters in the selector OR in query parameter
        for (int i = 1; i < segments.length - 1; i++) {
            String key = segments[i];
            if (StringUtils.equals(key, DOWNLOAD)) {
                this.otherParameters.put(key, true);
            } else {
                this.otherParameters.put(key, null);
            }
        }

        this.otherParameters.putAll(getParameters(request));
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

    public AssetRenditionParameters(final @Nonnull AssetModel assetModel, final @Nonnull String renditionNameWithParameters, final boolean download, final List<String> otherParameters) throws IllegalArgumentException {
        if (StringUtils.isBlank(renditionNameWithParameters)) {
            throw new IllegalArgumentException("Am asset is required");
        } else if (StringUtils.isBlank(renditionNameWithParameters)) {
            throw new IllegalArgumentException("A renditionName is required");
        }

        this.asset = DamUtil.resolveToAsset(assetModel.getResource());
        this.renditionName = StringUtils.substringBefore(renditionNameWithParameters, "?");
        this.fileName = buildFileName(asset, renditionName);

        for (String otherParameter : otherParameters) {
            this.otherParameters.put(otherParameter, null);
        }

        this.otherParameters.putAll(getParameters(renditionNameWithParameters));

        if (download) {
            this.otherParameters.put(DOWNLOAD, true);
        }
    }

    public String getRenditionName() {
        return renditionName;
    }

    public boolean isDownload() {
        return otherParameters.containsKey(DOWNLOAD);
    }

    public String getFileName() {
        if (fileName == null) {
            log.debug("The fileName can only be derived from parameters sourced from a SlingHttpServletRequest");
        }
        return fileName;
    }

    public Asset getAsset() {
        return asset;
    }


    public ValueMap getParameters() {
        return new ValueMapDecorator(otherParameters);
    }

    /**
     * At this time, only "userId" is set by Asset Share Commons to this ValueMap. Other values can be set by custom implementations as needed.
     *
     * @return other properties associated with this AssetRenditionParameters instance.
     */
    public ValueMap getOtherProperties() {
        return new ValueMapDecorator(new HashMap<>(otherProperties));
    }

    /**
     * Sets a property that will be added to the ValueMap returned by getOtherProperties().
     *
     * @param key the property name
     * @param value the property value
     */
    public void setOtherProperty(String key, Object value) {
        otherProperties.put(key, value);
    }

    public static ValueMap getParameters(SlingHttpServletRequest request) {
        Map<String, Object> params = new TreeMap<>();

        request.getParameterMap().forEach((key, value) -> {
            params.put(key, value[0]);
        });

        return new ValueMapDecorator(params);
    }

    public static ValueMap getParameters(String input) {
        Map<String, Object> params = new TreeMap<>();

        input = StringUtils.substringAfter(input, "?");

        Arrays.stream(StringUtils.split(input, "&")).spliterator().forEachRemaining(param -> {
            final String[] keyValue = StringUtils.split(param, "=");
            if (keyValue.length == 2) {
                String value = keyValue[1];
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.warn("Unable to decode Asset Rendition query parameter value [ {} ]. Using encoded value.", value, e);
                }
                params.putIfAbsent(keyValue[0], value);
            }
        });

        return new ValueMapDecorator(params);
    }

    public static String getRenditionName(String input) {
        if (StringUtils.contains(input, "?")) {
            return StringUtils.substringBefore(input, "?");
        } else {
            return input;
        }
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
}
