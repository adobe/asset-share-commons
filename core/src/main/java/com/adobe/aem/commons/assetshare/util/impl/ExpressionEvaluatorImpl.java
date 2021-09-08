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

package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import static com.day.cq.dam.scene7.api.constants.Scene7Constants.*;

@Component
public class ExpressionEvaluatorImpl implements ExpressionEvaluator {
    public static final String ZIP_EXTENSION = ".zip";

    private static final DateTimeFormatter year = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter month = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter monthName = DateTimeFormatter.ofPattern("MMM");
    private static final DateTimeFormatter day = DateTimeFormatter.ofPattern("dd");
    private static final DateTimeFormatter dayName = DateTimeFormatter.ofPattern("EEE");

    private static final DateTimeFormatter hour24 = DateTimeFormatter.ofPattern("HH");
    private static final DateTimeFormatter hour12 = DateTimeFormatter.ofPattern("hh");
    private static final DateTimeFormatter minute = DateTimeFormatter.ofPattern("mm");
    private static final DateTimeFormatter ampm = DateTimeFormatter.ofPattern("a");

    @Reference
    private MimeTypeService mimeTypeService;

    public String evaluateAssetsRenditionsExpressions(String expression, Collection<AssetModel> assetModels, Collection<String> renditionNames) {
        expression = StringUtils.replace(expression, VAR_ASSET_COUNT, String.valueOf(assetModels.size()));
        expression = StringUtils.replace(expression, VAR_RENDITION_COUNT, String.valueOf(renditionNames.size()));
        expression = StringUtils.replace(expression, VAR_FILE_COUNT, String.valueOf(assetModels.size() * renditionNames.size()));

        return expression;
    }

    public String evaluateDateTimeExpressions(String expression, ZonedDateTime zonedDateTime) {

        expression = StringUtils.replace(expression, VAR_DATE_YEAR, zonedDateTime.format(year));
        expression = StringUtils.replace(expression, VAR_DATE_MONTH, zonedDateTime.format(month));
        expression = StringUtils.replace(expression, VAR_DATE_MONTH_NAME, zonedDateTime.format(monthName));
        expression = StringUtils.replace(expression, VAR_DATE_DAY, zonedDateTime.format(day));
        expression = StringUtils.replace(expression, VAR_DATE_DAY_NAME,zonedDateTime.format(dayName));
        expression = StringUtils.replace(expression, VAR_TIME_HOUR_24, zonedDateTime.format(hour24));
        expression = StringUtils.replace(expression, VAR_TIME_HOUR_12, zonedDateTime.format(hour12));;
        expression = StringUtils.replace(expression, VAR_TIME_MINUTE, zonedDateTime.format(minute));
        expression = StringUtils.replace(expression, VAR_TIME_AM_PM, zonedDateTime.format(ampm));

        return expression;
    }


    public String evaluateDynamicMediaExpression(String expression, AssetModel assetModel) {
        // Dynamic Media properties
        final String dmName = assetModel.getProperties().get(PN_S7_NAME, String.class);
        final String dmId = assetModel.getProperties().get(PN_S7_ASSET_ID, String.class);
        final String dmFile = assetModel.getProperties().get(PN_S7_FILE, String.class);
        final String dmFileAvs = assetModel.getProperties().get(PN_S7_FILE_AVS, String.class);
        final String dmFolder = assetModel.getProperties().get(PN_S7_FOLDER, String.class);
        final String dmDomain = assetModel.getProperties().get(PN_S7_DOMAIN, String.class);
        final String dmApiServer = assetModel.getProperties().get(PN_S7_API_SERVER, String.class);
        final String dmCompanyId = assetModel.getProperties().get(PN_S7_COMPANY_ID, String.class);

        expression = StringUtils.replace(expression, VAR_DM_NAME, dmName);
        expression = StringUtils.replace(expression, VAR_DM_ID, dmId);
        expression = StringUtils.replace(expression, VAR_DM_FILE, dmFile);
        expression = StringUtils.replace(expression, VAR_DM_FILE_AVS, dmFileAvs);
        expression = StringUtils.replace(expression, VAR_DM_FILE_NO_COMPANY,
                StringUtils.substringAfterLast(dmFile, "/"));
        expression = StringUtils.replace(expression, VAR_DM_FOLDER, dmFolder);
        expression = StringUtils.replace(expression, VAR_DM_DOMAIN, dmDomain);
        expression = StringUtils.replace(expression, VAR_DM_API_SERVER, dmApiServer);
        expression = StringUtils.replace(expression, VAR_DM_COMPANY_ID, dmCompanyId);
        expression = StringUtils.replace(expression, VAR_DM_COMPANY_NAME,
                StringUtils.substringBeforeLast(dmFile, "/"));

        return expression;
    }

    public String evaluateAssetExpression(String expression, AssetModel assetModel) {
        final String assetPath = assetModel.getPath();
        final String assetUrl = assetModel.getUrl();
        final String assetName = assetModel.getName();

        expression = StringUtils.replace(expression, VAR_ASSET_PATH, assetPath);
        expression = StringUtils.replace(expression, VAR_ASSET_URL, assetUrl);
        expression = StringUtils.replace(expression, VAR_ASSET_NAME, assetName);
        expression = StringUtils.replace(expression, VAR_ASSET_NAME_NO_EXTENSION,
                StringUtils.substringBeforeLast(assetModel.getName(), "."));

        expression = StringUtils.replace(expression, VAR_ASSET_EXTENSION,
                StringUtils.substringAfterLast(assetName, "."));

        return expression;
    }

    public String evaluateRenditionExpression(String expression, String renditionName) {
        expression = StringUtils.replace(expression, VAR_RENDITION_NAME, renditionName);

        return expression;
    }

    public String evaluateRenditionExpression(String expression, AssetRendition assetRendition) {
        String mimeType = assetRendition.getMimeType();
        String renditionExtension = mimeTypeService.getExtension(mimeType);

        expression = StringUtils.replace(expression, VAR_RENDITION_EXTENSION, renditionExtension);

        return expression;

    }
}
