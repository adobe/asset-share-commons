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
import com.adobe.granite.asset.api.AssetException;
import com.day.cq.wcm.api.WCMMode;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.net.ssl.StandardConstants;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

@Component
public final class ActionHelperImpl implements ActionHelper {

    @Reference
    private ModelFactory modelFactory;

    public final Collection<AssetModel> getAssetsFromQueryParameter(final SlingHttpServletRequest request, final String parameterName) {
        final RequestParameter[] requestParameters = request.getRequestParameters(parameterName);
        final Collection<AssetModel> assets = new ArrayList<>();

        if (requestParameters != null) {
            for (final RequestParameter requestParameter : requestParameters) {
                String path = requestParameter.getString();

                try {
                    path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException ex) {
                    throw new AssetException("Could not UTF-8 encode the asset path.", requestParameter.getString());
                }

                final Resource resource = request.getResourceResolver().getResource(path);
                if (resource != null) {
                    final AssetModel asset = modelFactory.getModelFromWrappedRequest(request, resource, AssetModel.class);
                    if (asset != null) {
                        assets.add(asset);
                    }
                }

            }
        }

        return assets;
    }

    public final Collection<AssetModel> getPlaceholderAsset(final SlingHttpServletRequest request) {
        final Collection<AssetModel> assets = new ArrayList<>();

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