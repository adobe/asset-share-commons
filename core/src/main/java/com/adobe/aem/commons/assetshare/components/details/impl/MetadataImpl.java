/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Metadata;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.JsonResolver;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.sightly.SightlyWCMMode;
import com.day.cq.wcm.api.Page;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.*;

@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = {Metadata.class, ComponentExporter.class},
        resourceType = {MetadataImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class MetadataImpl extends AbstractEmptyTextComponent implements Metadata {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/metadata";
    protected static final String PN_TYPE = "type";
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MetadataImpl.class);
    @Self
    @Required
    private SlingHttpServletRequest request;

    @SlingObject
    @Required
    private SlingHttpServletResponse response;

    @Self
    @Required
    private AssetModel asset;

    @OSGiService
    private JsonResolver jsonResolver;

    @ScriptVariable
    private SightlyWCMMode wcmmode;

    @ScriptVariable
    private Page currentPage;

    @ValueMapValue
    private String propertyName;

    @ValueMapValue
    private String computedProperty;

    @ValueMapValue
    private String formatDate;

    @ValueMapValue
    private String formatNumber;

    @ValueMapValue
    private Double unitConverter;

    @ValueMapValue(name = MetadataImpl.PN_TYPE)
    private String typeString;

    @ValueMapValue
    private String jsonSource;

    private DataType type;

    private String locale;

    /***
     * ValueMap of the properties of the Asset currently being viewed
     */
    private ValueMap combinedProperties;

    private List<String> values = null;

    @PostConstruct
    public void init() {
        combinedProperties = getProperties();
    }

    @Override
    public DataType getType() {
        type = Metadata.DataType.fromString(typeString);
        return type;
    }

    @Override
    public String getLocale() {
        if (locale == null) {
            locale = currentPage == null ? Locale.getDefault().getLanguage() : currentPage.getLanguage(false).getLanguage();
        }
        return locale;
    }

    @Override
    public String getFormat() {

        if (type == null) {
            getType();
        }
        switch (type) {
            case DATE:
                return formatDate;
            case NUMBER:
                return formatNumber;
            default:
                return null;
        }
    }

    @Override
    public ValueMap getProperties() {
        if (combinedProperties == null) {
            combinedProperties = asset.getProperties();
        }
        return combinedProperties;
    }

    @Override
    public AssetModel getAsset() {
        return asset;
    }

    @Override
    public String getPropertyName() {
        if (Metadata.DataType.COMPUTED.equals(getType())) {
            return computedProperty;
        } else {
            return propertyName;
        }
    }

    @Override
    public boolean isEmpty() {
        if (StringUtils.isBlank(getPropertyName())) {
            return true;
        } else if (DataType.JSON.equals(getType())) {
            return CollectionUtils.isEmpty(getValues());
        } else {
            Object val = combinedProperties.get(getPropertyName());
            if (val == null) {
                return true;
            } else if (val instanceof String) {
                return StringUtils.isBlank((String) val);
            } else if (val instanceof String[]) {
                return ArrayUtils.isEmpty((String[]) val) ||
                        !Arrays.stream((String[]) val).filter(StringUtils::isNotBlank).findFirst().isPresent();
            } else if (val instanceof Object[]) {
                return ArrayUtils.isEmpty((Object[]) val);
            } else if (val instanceof Collection) {
                // This is never null due to the first check
                return ((Collection) val).isEmpty();
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean isReady() {
        return !isEmpty() || hasEmptyText();
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }


    @Override
    public List<String> getValues() {
        if (values == null) {
            values = new ArrayList<>();

            Object val = combinedProperties.get(getPropertyName());

            if (null == val) {
                return values;
            } else if (val instanceof String) {
                values.add((String) val);
            } else if (val instanceof String[]) {
                values.addAll(Arrays.asList((String[]) val));
            }

            if (DataType.JSON.equals(getType()) && StringUtils.isNotBlank(jsonSource)) {
                JsonElement jsonElement = jsonResolver.resolveJson(request, response, jsonSource);

                if (jsonElement != null) {
                    values = getValuesFromJson(values, jsonElement);
                } else {
                    log.error("Unable to read JSON from [ {} ]. Defaulting to no values.", jsonSource);
                }
            }
        }

        return values;
    }

    /**
     * @param propertyValues the property values from the asset that will be used as the keys to look up values in the JSON
     * @param json           the JsonElement that contains the propertyValue to display value mappings
     * @return values the values to display
     */
    private List<String> getValuesFromJson(List<String> propertyValues, JsonElement json) {
        final String OPTIONS = "options";

        List<String> values = new ArrayList<>();

        JsonArray jsonArray = new JsonArray();
        if (json.isJsonArray()) {
            jsonArray = json.getAsJsonArray();
        } else if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has(OPTIONS) && jsonObject.get(OPTIONS).isJsonArray()) {
                jsonArray = jsonObject.getAsJsonArray(OPTIONS);
            }
        } else {
            return propertyValues;
        }

        List<JsonOption> options = new Gson().fromJson(jsonArray, new TypeToken<List<JsonOption>>() {
        }.getType());

        for (String propertyValue : propertyValues) {
            JsonOption value = options.stream()
                    .filter(option -> propertyValue.equals(option.value))
                    .findFirst()
                    .orElse(null);

            if (null != value) {
                values.add(value.text);
            } else {
                values.add(propertyValue);
            }
        }

        return values;
    }

    class JsonOption {
        String text;
        String value;
    }
}
