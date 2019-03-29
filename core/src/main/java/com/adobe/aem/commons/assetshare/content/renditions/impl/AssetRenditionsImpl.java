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

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionResolver;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.Order;
import org.apache.sling.commons.osgi.RankedServices;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component(
        reference = {
                @Reference(
                        name = "renditionResolver",
                        bind = "bindAssetRenditionResolver",
                        unbind = "unbindAssetRenditionResolver",
                        service = AssetRenditionResolver.class,
                        policy = ReferencePolicy.DYNAMIC,
                        policyOption = ReferencePolicyOption.GREEDY,
                        cardinality = ReferenceCardinality.MULTIPLE
                )
        }
)
public class AssetRenditionsImpl implements AssetRenditions {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionsImpl.class);

    @Reference
    private ModelFactory modelFactory;

    private final RankedServices<AssetRenditionResolver> assetRenditionResolvers = new RankedServices<>(Order.DESCENDING);

    protected void bindAssetRenditionResolver(AssetRenditionResolver service, Map<String, Object> props) {
        log.debug("Binding AssetRenditionResolver [ {} ]", service.getClass().getName());
        assetRenditionResolvers.bind(service, props);
    }

    protected void unbindAssetRenditionResolver(AssetRenditionResolver service, Map<String, Object> props) {
        log.debug("Unbinding AssetRenditionResolver [ {} ]", service.getClass().getName());
        assetRenditionResolvers.unbind(service, props);
    }

    @Override
    public List<AssetRenditionResolver> getAssetRenditionResolvers() {
        return assetRenditionResolvers.getList();
    }

    @Override
    public String getRenditionName(final SlingHttpServletRequest request) {
        final String suffix = request.getRequestPathInfo().getSuffix();
        final String[] segments = StringUtils.split(StringUtils.substringBeforeLast(suffix, "."), "/");

        if (segments != null && segments.length > 0) {
            return segments[0];
        } else {
            return null;
        }
    }

    @Override
    public String getUrl(final SlingHttpServletRequest request, final AssetModel asset, final UrlParams params) {
        String url = request.getResourceResolver().map(asset.getPath()) + "." + AssetRenditionServlet.SERVLET_EXTENSION + "/" + params.getRenditionName() + "/";

        if (params.isDownload()) {
            url += AssetRenditionServlet.DOWNLOAD_AS_ATTACHMENT_SUFFIX_SEGMENT + "/";
        }

        url += AssetRenditionServlet.CACHEABLE_SUFFIX_FILENAME;

        return url;
    }

    @Override
    public Map<String, String> getOptions(final Map<String, ? extends Object> mappings) {
        final Map<String, String> options = new LinkedHashMap<>();

        mappings.keySet().stream()
                .sorted()
                .forEach(key -> {
                    if (!options.containsValue(key)) {
                        options.put(
                                StringUtils.capitalize(StringUtils.replace(key, "_", " ")), key);
                    }
                });

        return options;
    }

    @Override
    public String evaluateExpression(final SlingHttpServletRequest request, String expression) {
        final AssetModel assetModel = request.adaptTo(AssetModel.class);

        // Even though, the name is .path, we use url since this is the URL escaped version of the path
        final String assetPath = assetModel.getUrl();
        final String assetName = assetModel.getName();
        final String assetExtension = StringUtils.substringAfterLast(assetName, ".");
        final String renditionName = getRenditionName(request);

        expression = StringUtils.replace(expression, VAR_ASSET_PATH, assetPath);
        expression = StringUtils.replace(expression, VAR_ASSET_NAME, assetName);
        expression = StringUtils.replace(expression, VAR_ASSET_EXTENSION, assetExtension);
        expression = StringUtils.replace(expression, VAR_RENDITION_NAME, renditionName);

        return expression;
    }
}
