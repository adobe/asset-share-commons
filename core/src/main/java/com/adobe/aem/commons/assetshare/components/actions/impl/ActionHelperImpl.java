/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.components.actions.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.wcm.api.WCMMode;
import com.day.text.Text;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_LIST;

@Component
public final class ActionHelperImpl implements ActionHelper {

    @Reference
    private ModelFactory modelFactory;

    @Override
    public final List<AssetModel> getAssetsFromQueryParameter(final SlingHttpServletRequest request, final String parameterName) {
        final RequestParameter[] requestParameters = request.getRequestParameters(parameterName);

        if (requestParameters != null) {
            return Arrays.stream(requestParameters)
                    .filter(Objects::nonNull)
                    .map(RequestParameter::getString)
                    .filter(StringUtils::isNotBlank)
                    .map(path -> Text.unescape(path))
                    .map(path -> request.getResourceResolver().getResource(path))
                    .filter(Objects::nonNull)
                    .map(resource -> modelFactory.getModelFromWrappedRequest(request, resource, AssetModel.class))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return EMPTY_LIST;
    }

    @Override
    public final List<String> getAllowedValuesFromQueryParameter(final SlingHttpServletRequest request, final String parameterName, final String[] allowedValues) {
        if (allowedValues != null) {
            final RequestParameter[] requestParameters = request.getRequestParameters(parameterName);

            if (requestParameters != null) {
                return Arrays.stream(requestParameters).map(RequestParameter::getString)
                        .filter(renditionName -> allowedValues.length == 0 || ArrayUtils.contains(allowedValues, renditionName))
                        .distinct()
                        .collect(Collectors.toList());
            }
        }

        return EMPTY_LIST;
    }

    public final List<AssetModel> getPlaceholderAsset(final SlingHttpServletRequest request) {
        final List<AssetModel> assets = new ArrayList<>();

        if (!WCMMode.DISABLED.equals(WCMMode.fromRequest(request))) {
            final Config config = request.adaptTo(Config.class);
            final AssetModel placeholder = config.getPlaceholderAsset();
            if (placeholder != null) {
                assets.add(placeholder);
            }
        }

        return assets;
    }
}