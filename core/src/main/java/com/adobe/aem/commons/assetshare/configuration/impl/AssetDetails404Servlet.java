/*
 * Asset Share Commons
 *
 * Copyright [2017]  Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.util.AssetUtil;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.WCMMode;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
            service = Servlet.class,
            property = {
               "sling.servlet.methods=GET",
               "sling.servlet.resourceTypes=asset-share-commons/components/structure/details-page",
               "sling.servlet.extensions=html"
            },
            configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class AssetDetails404Servlet extends SlingSafeMethodsServlet implements OptingServlet {

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        // Control which requests enter this method via accepts(..) below.

        response.setStatus(SlingHttpServletResponse.SC_NOT_FOUND);
        response.getWriter().print("The path to a valid and accessible asset must be provided to the Asset Details page for it to display.");
        response.getWriter().flush();
    }

    @Override public boolean accepts(@Nonnull final SlingHttpServletRequest request) {
        final WCMMode wcmMode = WCMMode.fromRequest(request);

        // ONLY perform this on WCMModes disabled in case someone runs this on AEM Author as disabled.
        if (WCMMode.DISABLED.equals(wcmMode)) {
            final RequestPathInfo requestPathInfo = request.getRequestPathInfo();
            final Resource suffixResource = requestPathInfo.getSuffixResource();
            Asset asset = null;
            //Get an asset by suffixPath or suffix ID
            if (null == suffixResource) {
                asset = AssetUtil.getAssetById(request, requestPathInfo.getSuffix());
            } else {
                asset = suffixResource.adaptTo(Asset.class);
            }

            //if asset is null then return 404
            if (null == asset) {
                return true;
            }
        }

        return false;
    }
}
