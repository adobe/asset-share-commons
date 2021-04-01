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

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ProviderType;

import java.util.Map;

/**
 *
 */
@ProviderType
public interface AssetRenditions {
    String VAR_ASSET_PATH = "${asset.path}";
    String VAR_ASSET_URL = "${asset.url}";
    String VAR_ASSET_NAME = "${asset.name}"; // -> BnW.mp4
    String VAR_ASSET_NAME_NO_EXTENSION = "${asset.name.no-extension}"; // -> BnW
    String VAR_ASSET_EXTENSION = "${asset.extension}"; // -> mp4
    String VAR_RENDITION_NAME = "${rendition.name}";

    String VAR_DM_NAME = "${dm.name}"; // metadata/dam:scene7Name () -> BnW-3
    String VAR_DM_ID = "${dm.id}"; // metadata/dam:scene7ID -> a|17904150
    String VAR_DM_FILE = "${dm.file}"; // metadata/dam:scene7File -> DynamicMediaNA/BnW-3
    String VAR_DM_FILE_AVS = "${dm.file.avs}"; // metadata/dam:scene7FileAvs -> DynamicMediaNA/BnW-3-AVS
    String VAR_DM_FILE_NO_COMPANY = "${dm.file.no-company}"; // metadata/dam:scene7File -> DynamicMediaNA/BnW-3 -> after / -> BnW-3
    String VAR_DM_FOLDER= "${dm.folder}"; // metadata/dam:scene7Folder -> DynamicMediaNA/asset-share-commons/en/public/media/
    String VAR_DM_DOMAIN = "${dm.domain}"; // metadata/dam:scene7Domain -> https://s7d2.scene7.com/
    String VAR_DM_API_SERVER = "${dm.api-server}"; // metadata/dam:scene7APIServer -> https://s7sps1apissl.scene7.com
    String VAR_DM_COMPANY_ID = "${dm.company.id}"; // metadata/dam:scene7CompanyID -> c|120365
    String VAR_DM_COMPANY_NAME = "${dm.company.name}"; // metadata/dam:scene7File -> DynamicMediaNA/BnW-3 -> before /

    /**
     * Creates a URL to the AssetRenditionServlet for the
     *
     * @param request the request object.
     * @param asset   the asset to rendition.
     * @param parameters  the params which describe how the AssetRenditionServlet should handle sending the asset rendition to the response.
     *
     * @return the URL that can be used to HTTP GET Request the specified asset rendition (does NOT include scheme/host/port).
     */
    String getUrl(SlingHttpServletRequest request, AssetModel asset, AssetRenditionParameters parameters);

    /**
     * Creates a Map that is used to provide the label/values to drive the AssetRenditionsDatasource dropdown.
     * <p>
     * This is a convenience method, however each AssetRenditionDispatcher implementation can implement their own getOptions() rather than wrapping a call to this method.
     *
     * @param mappings the raw mappings from the
     *
     * @return the Options is the format LABEL:VALUE
     */
    Map<String, String> getOptions(Map<String, ? extends Object> mappings);

    /**
     * Replaces the 'variables' in the expressions with the corresponding bits derived from the request.
     *
     * @param request the request object to the AssetRenditionServlet.
     * @param expression the expression to replace the variables in.
     * @return the expression with the variables replaced with values derived from the request.
     */
    String evaluateExpression(SlingHttpServletRequest request, String expression);

    /**
     * Replaces the 'variables' in the expressions with the corresponding bits derived from the AssetModel.
     *
     * @param assetModel the AssetModel representing the asset whose rendition expression should be evaluated.
     * @param renditionName the requested rendition name
     * @param expression the expression to replace the variables in.
     * @return the expression with the variables replaced with values derived from the request.
     */
    String evaluateExpression(final AssetModel assetModel, String renditionName, String expression);
}
