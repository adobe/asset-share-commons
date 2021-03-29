package com.adobe.aem.commons.assetshare.content.renditions.download.async.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

public class ArchiveNameEvaluator {

    public static final String ASSET_COUNT = "${asset.count}";
    public static final String RENDITION_COUNT = "${rendition.count}";
    public static final String FILE_COUNT = "${file.count}";
    public static final String DATE_YEAR = "${year}";
    public static final String DATE_MONTH = "${month}";
    public static final String DATE_MONTH_NAME = "${month.name}";
    public static final String DATE_DAY = "${day}";
    public static final String DATE_DAY_NAME = "${day.name}";
    public static final String TIME_HOUR_24 = "${hour.24}";
    public static final String TIME_HOUR_12 = "${hour.12}";
    public static final String TIME_MINUTE = "${minute}";
    public static final String TIME_AM_PM = "${am.pm}";

    private static final SimpleDateFormat year = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat month = new SimpleDateFormat("MM");
    private static final SimpleDateFormat monthName = new SimpleDateFormat("MMM");
    private static final SimpleDateFormat day = new SimpleDateFormat("dd");
    private static final SimpleDateFormat dayName = new SimpleDateFormat("EEE");

    private static final SimpleDateFormat hour24 = new SimpleDateFormat("HH");
    private static final SimpleDateFormat hour12 = new SimpleDateFormat("hh");
    private static final SimpleDateFormat minute = new SimpleDateFormat("mm");
    private static final SimpleDateFormat ampm = new SimpleDateFormat("a");


    public static String evaluateArchiveName(String expression, Collection<AssetModel> assetModels, Collection<String> renditionNames) {

        Date now = GregorianCalendar.getInstance().getTime();

        expression = StringUtils.replace(expression, ASSET_COUNT, String.valueOf(assetModels.size()));
        expression = StringUtils.replace(expression, RENDITION_COUNT, String.valueOf(renditionNames.size()));
        expression = StringUtils.replace(expression, FILE_COUNT, String.valueOf(assetModels.size() * renditionNames.size()));
        expression = StringUtils.replace(expression, DATE_YEAR, year.format(now));
        expression = StringUtils.replace(expression, DATE_MONTH, month.format(now));
        expression = StringUtils.replace(expression, DATE_MONTH_NAME, monthName.format(now));
        expression = StringUtils.replace(expression, DATE_DAY, day.format(now));
        expression = StringUtils.replace(expression, DATE_DAY_NAME, dayName.format(now));
        expression = StringUtils.replace(expression, TIME_HOUR_24, hour24.format(now));
        expression = StringUtils.replace(expression, TIME_HOUR_12, hour12.format(now));
        expression = StringUtils.replace(expression, TIME_MINUTE, minute.format(now));
        expression = StringUtils.replace(expression, TIME_AM_PM, ampm.format(now));

        if (!StringUtils.endsWith(expression, ".zip")) {
            expression += ".zip";
        }

        return expression;
    }

}
