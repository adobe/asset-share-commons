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
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.day.cq.dam.scene7.api.constants.Scene7Constants.*;

@Component
public class AssetRenditionsImpl implements AssetRenditions {
    private static final Logger log = LoggerFactory.getLogger(AssetRenditionsImpl.class);

    @Reference
    private ModelFactory modelFactory;

    @Reference
    private ExpressionEvaluator expressionEvaluator;

    @Override
    public String getUrl(final SlingHttpServletRequest request, final AssetModel asset, final AssetRenditionParameters parameters) {
        String url = request.getResourceResolver().map(asset.getPath()) + "." + AssetRenditionServlet.SERVLET_EXTENSION + "/" + parameters.getRenditionName() + "/";

        if (parameters.isDownload()) {
            url += AssetRenditionParameters.DOWNLOAD + "/";
        }

        url += AssetRenditionParameters.CACHE_FILENAME;

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
        return evaluateExpression(assetModel, new AssetRenditionParameters(request).getRenditionName(), expression);
    }

    @Override
    public String evaluateExpression(final AssetModel assetModel, String renditionName, String expression) {
        expression = expressionEvaluator.evaluateAssetExpression(expression, assetModel);
        expression = expressionEvaluator.evaluateRenditionExpression(expression, renditionName);
        expression = expressionEvaluator.evaluateDynamicMediaExpression(expression, assetModel);

        return expression;
    }
}
