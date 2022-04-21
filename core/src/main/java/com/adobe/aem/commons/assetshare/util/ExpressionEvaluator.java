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

package com.adobe.aem.commons.assetshare.util;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import org.osgi.annotation.versioning.ProviderType;

import java.time.ZonedDateTime;
import java.util.Collection;

@ProviderType
public interface ExpressionEvaluator {
    String VAR_ASSET_COUNT = "${asset.count}";
    String VAR_RENDITION_COUNT = "${rendition.count}";
    String VAR_FILE_COUNT = "${file.count}";
    String VAR_DATE_YEAR = "${year}";
    String VAR_DATE_MONTH = "${month}";
    String VAR_DATE_MONTH_NAME = "${month.name}";
    String VAR_DATE_DAY = "${day}";
    String VAR_DATE_DAY_NAME = "${day.name}";
    String VAR_TIME_HOUR_24 = "${hour.24}";
    String VAR_TIME_HOUR_12 = "${hour.12}";
    String VAR_TIME_MINUTE = "${minute}";
    String VAR_TIME_AM_PM = "${am.pm}";

    String VAR_RENDITION_EXTENSION = "${rendition.extension}";

    String VAR_ASSET_PATH = AssetRenditions.VAR_ASSET_PATH;
    String VAR_ASSET_URL = AssetRenditions.VAR_ASSET_URL;
    String VAR_ASSET_NAME = AssetRenditions.VAR_ASSET_NAME; // -> BnW.mp4
    String VAR_ASSET_NAME_NO_EXTENSION = AssetRenditions.VAR_ASSET_NAME_NO_EXTENSION; // -> BnW
    String VAR_ASSET_EXTENSION =  AssetRenditions.VAR_ASSET_EXTENSION; // -> mp4
    String VAR_RENDITION_NAME = AssetRenditions.VAR_RENDITION_NAME;

    String VAR_DM_NAME = AssetRenditions.VAR_DM_NAME; // metadata/dam:scene7Name () -> BnW-3
    String VAR_DM_ID = AssetRenditions.VAR_DM_ID; // metadata/dam:scene7ID -> a|17904150
    String VAR_DM_FILE = AssetRenditions.VAR_DM_FILE; // metadata/dam:scene7File -> DynamicMediaNA/BnW-3
    String VAR_DM_FILE_AVS = AssetRenditions.VAR_DM_FILE_AVS; // metadata/dam:scene7FileAvs -> DynamicMediaNA/BnW-3-AVS
    String VAR_DM_FILE_NO_COMPANY = AssetRenditions.VAR_DM_FILE_NO_COMPANY; // metadata/dam:scene7File -> DynamicMediaNA/BnW-3 -> after / -> BnW-3
    String VAR_DM_FOLDER= AssetRenditions.VAR_DM_FOLDER; // metadata/dam:scene7Folder -> DynamicMediaNA/asset-share-commons/en/public/media/
    String VAR_DM_DOMAIN = AssetRenditions.VAR_DM_DOMAIN; // metadata/dam:scene7Domain -> https://s7d2.scene7.com/
    String VAR_DM_API_SERVER = AssetRenditions.VAR_DM_API_SERVER; // metadata/dam:scene7APIServer -> https://s7sps1apissl.scene7.com
    String VAR_DM_COMPANY_ID = AssetRenditions.VAR_DM_COMPANY_ID; // metadata/dam:scene7CompanyID -> c|120365
    String VAR_DM_COMPANY_NAME = AssetRenditions.VAR_DM_COMPANY_NAME; // metadata/dam:scene7File -> DynamicMediaNA/BnW-3 -> before /

    String evaluateAssetsRenditionsExpressions(String expression, Collection<AssetModel> assetModels, Collection<String> renditionNames);

    String evaluateDateTimeExpressions(String expression, ZonedDateTime zonedDateTime);

    String evaluateDynamicMediaExpression(String expression, AssetModel assetModel);

    String evaluateAssetExpression(String expression, AssetModel assetModel);

    String evaluateRenditionExpression(String expression, String renditionName);

    String evaluateRenditionExpression(String expression, AssetRendition assetRendition);

    default String evaluateProperties(String expression, AssetModel assetModel) { return expression; }
}
