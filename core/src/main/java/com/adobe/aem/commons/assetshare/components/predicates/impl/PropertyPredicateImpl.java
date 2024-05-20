
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

package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.AbstractPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.DefaultValuesPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.PropertyPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.CoreComponentsOptionItem;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.SelectedOptionItem;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.UnselectedOptionItem;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import com.adobe.aem.commons.assetshare.util.JsonResolver;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.adobe.aem.commons.assetshare.util.impl.responses.DiscardingResponseWrapper;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.eval.JcrPropertyPredicateEvaluator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {PropertyPredicate.class, DefaultValuesPredicate.class, ComponentExporter.class},
        resourceType = {PropertyPredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class PropertyPredicateImpl extends AbstractPredicate implements PropertyPredicate, DefaultValuesPredicate, Options {

    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/property";
    protected static final String PN_TYPE = "type";

    protected String valueFromRequest = null;
    protected ValueMap valuesFromRequest = null;

    @Self
    @Required
    private SlingHttpServletRequest request;

    @SlingObject
    @Required
    private SlingHttpServletResponse response;

    @SlingObject
    private Resource resource;

    @Self
    @Required
    private Options coreOptions;

    @ValueMapValue
    @Nullable
    private String datasourceRT;

    @OSGiService
    private JsonResolver jsonResolver;

    @ValueMapValue
    private String label;

    @ValueMapValue
    private String property;

    @ValueMapValue
    private String operation;

    @ValueMapValue
    private Boolean expanded;

    @ValueMapValue(name = PropertyPredicateImpl.PN_TYPE)
    private String typeString;

    @ValueMapValue
    @Named("and")
    @Default(booleanValues = false)
    private boolean and;

    @ValueMapValue
    private String source;

    @ValueMapValue
    private String jsonSource;

    @PostConstruct
    protected void init() {
        initPredicate(request, coreOptions);
    }

    /* Options - Core Component Delegates */
    private List<OptionItem> items = null;

    public List<OptionItem> getItems() {
        if (items == null) {
            final ValueMap initialValues = getInitialValues();
            final List<OptionItem> processedOptionItems = new ArrayList<>();
            final boolean useDefaultSelected = !isParameterizedSearchRequest();

            List<OptionItem> optionItems = new ArrayList<>();

            if (StringUtils.equalsIgnoreCase("json", source)) {
                JsonElement jsonElement = jsonResolver.resolveJson(request, response, jsonSource);
                if (jsonElement != null) {
                    optionItems = getOptionItemsFromJson(jsonElement);
                }
            } else if (StringUtils.equalsIgnoreCase("datasource", source)) {
                // This is copied down drom Core Components, as it has to be specially handled
                // otherwise extra HTML is inadvertently injected into the page
                optionItems = populateOptionItemsFromDatasource(datasourceRT);

            } else {
                optionItems = coreOptions.getItems();
            }

            optionItems.stream()
                    .forEach(optionItem -> {
                        if (PredicateUtil.isOptionInInitialValues(optionItem, initialValues)) {
                            processedOptionItems.add(new SelectedOptionItem(optionItem));
                        } else if (useDefaultSelected) {
                            processedOptionItems.add(optionItem);
                        } else {
                            processedOptionItems.add(new UnselectedOptionItem(optionItem));
                        }
                    });

            items = processedOptionItems;
        }

        return items;
    }

    public Type getType() {
        return coreOptions.getType();
    }

    /* Property Predicate Specific */

    public String getSubType() {
        //support variation of Checkboxes
        return typeString;
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public String getValuesKey() {
        return PropertyValuesPredicateEvaluator.VALUES;
    }

    @Override
    public boolean hasOperation() {
        return StringUtils.isNotBlank(getOperation());
    }

    @Override
    public String getOperation() {
        return operation;
    }

    public boolean hasAnd() {
        return and;
    }

    public Boolean getAnd() {
        return and;
    }

    @Override
    public String getName() {
        return PropertyValuesPredicateEvaluator.PREDICATE_NAME;
    }

    @Override
    public boolean isReady() {
        return getItems().size() > 0;
    }

    @Override
    public String getInitialValue() {
        if (valueFromRequest == null) {
            valueFromRequest = PredicateUtil.getInitialValue(request, this, PropertyValuesPredicateEvaluator.VALUES);
        }

        return valueFromRequest;
    }

    @Override
    public ValueMap getInitialValues() {
        if (valuesFromRequest == null) {
            valuesFromRequest = PredicateUtil.getInitialValues(request, this, PropertyValuesPredicateEvaluator.VALUES);
        }

        return valuesFromRequest;
    }

    @Override
    public PredicateGroup getPredicateGroup() {
        final Map<String, String> params = new HashMap<>();

        if (resource == null) {
            return new PredicateGroup();
        }

        String prefix = getName() + ".";

        int i = 0;
        for (OptionItem item : getItems()) {
            if (item.isSelected()) {
                params.put(prefix + JcrPropertyPredicateEvaluator.PROPERTY, getProperty());
                params.put(prefix + i + "_" + getValuesKey(), item.getValue());
                params.put(prefix + JcrPropertyPredicateEvaluator.OPERATION,  getOperation());
                if (hasAnd()) {
                    params.put(prefix + JcrPropertyPredicateEvaluator.AND, String.valueOf(getAnd()));
                }
                i++;
            }
        }

        return PredicateConverter.createPredicates(params);
    }


    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }

    protected List<OptionItem> getOptionItemsFromJson(JsonElement json) {
        final String OPTIONS = "options";
        TypeToken textValueTypeToken = new TypeToken<List<TextValueJsonOption>>() {
        };

        List<OptionItem> values = new ArrayList<>();

        JsonArray jsonArray = new JsonArray();

        if (json != null) {
            if (json.isJsonArray()) {
                jsonArray = json.getAsJsonArray();
            } else if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                if (jsonObject.has(OPTIONS) && jsonObject.get(OPTIONS).isJsonArray()) {
                    jsonArray = json.getAsJsonObject().getAsJsonArray(OPTIONS);
                }
            }
        }

        values = new Gson().fromJson(jsonArray, textValueTypeToken.getType());

        if (values.stream().anyMatch(kv -> kv.getText() == null || kv.getValue() == null)) {
            values = new ArrayList<>();
        }

        return values;
    }


    @SuppressWarnings("unchecked")
    /**
     * This is copied from Core Components.
     * The key difference is it passes in the DiscardingResponseWrapper to the include method
     * HTML is not inadvertently injected into the page.
     *
     * See Bug #1129
     */
    private ArrayList<OptionItem> populateOptionItemsFromDatasource(String datasourceRT) {
        ArrayList<OptionItem> optionItems = new ArrayList<>();

        if (StringUtils.isBlank(datasourceRT)) {
            return optionItems;
        }
        // build the options by running the datasource code (the list is set as a request attribute)
        RequestDispatcherOptions opts = new RequestDispatcherOptions();
        opts.setForceResourceType(datasourceRT);
        RequestDispatcher dispatcher = request.getRequestDispatcher(resource, opts);
        try {
            if (dispatcher != null) {
                dispatcher.include(request, new DiscardingResponseWrapper(response));
            } else {
                //log.error("Failed to include the datasource at " + datasourceRT);
            }
        } catch (IOException | ServletException | RuntimeException e) {
            //LOGGER.error("Failed to include the datasource at " + datasourceRT, e);
        }

        // retrieve the datasource from the request and adapt it to form options
        SimpleDataSource dataSource = (SimpleDataSource) request.getAttribute(DataSource.class.getName());
        if (dataSource != null) {
            Iterator<Resource> itemIterator = dataSource.iterator();
            if (itemIterator != null) {
                while (itemIterator.hasNext()) {
                    Resource itemResource = itemIterator.next();
                    OptionItem optionItem = new CoreComponentsOptionItem(request, resource, itemResource);
                    if ((optionItem.isDisabled() || StringUtils.isNotBlank(optionItem.getValue()))) {
                        optionItems.add(optionItem);
                    }
                }
            }
        }

        return optionItems;
    }


    protected class TextValueJsonOption implements OptionItem {

        private final String text;
        private final String value;

        public TextValueJsonOption(String text, String value) {
            this.text = text;
            this.value = value;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
