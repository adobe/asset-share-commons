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
import com.adobe.aem.commons.assetshare.components.predicates.PropertyPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.SelectedOptionItem;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {PropertyPredicate.class},
        resourceType = {PropertyPredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class PropertyPredicateImpl extends AbstractPredicate implements PropertyPredicate, Options {
    private static final Logger log = LoggerFactory.getLogger(PropertyPredicateImpl.class);
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/property";
    protected static final String PN_TYPE = "type";

    protected String valueFromRequest = null;
    protected ValueMap valuesFromRequest = null;
    protected boolean foundValueFromRequest = false;

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private Options coreOptions;

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

    @PostConstruct
    protected void init() {
        initPredicate(request, coreOptions);
    }

    /* Options - Core Component Delegates */

    public List<OptionItem> getItems() {
        final ValueMap initialValues = getInitialValues();
        final List<OptionItem> processedOptionItems = new ArrayList<>();

        for (final OptionItem optionItem : coreOptions.getItems()) {
            if (PredicateUtil.isOptionInInitialValues(getGroup(), optionItem, initialValues)) {
                processedOptionItems.add(new SelectedOptionItem(optionItem));
                foundValueFromRequest = true;
            } else {
                processedOptionItems.add((optionItem));
            }
        }
        return processedOptionItems;
    }

    public Type getType() {
        return coreOptions.getType();
    }

    /* Property Predicate Specific */

    public String getSubType() {
        //support variation of Checkboxes
        return typeString;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public String getValuesKey() {
        return PropertyValuesPredicateEvaluator.VALUES;
    }

    public boolean hasOperation() {
        return StringUtils.isNotBlank(getOperation());
    }

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
            RequestParameter requestParameter = request.getRequestParameter(getGroup() + "." + getName() + "." + PropertyValuesPredicateEvaluator.VALUES);
            if (requestParameter != null) {
                valueFromRequest = requestParameter.getString();
            } else {
                valueFromRequest = "";
            }
        }

        return valueFromRequest;
    }

    @Override
    public ValueMap getInitialValues() {
        if (valuesFromRequest == null) {
            valuesFromRequest = new ValueMapDecorator(new HashMap<>());

            for (final Map.Entry<String, RequestParameter[]> entry : request.getRequestParameterMap().entrySet()) {
                final List<String> values = new ArrayList<>();

                if (entry.getKey().matches("^" + getGroup() + "." + getName() + ".\\d*_?" + PropertyValuesPredicateEvaluator.VALUES + "$")) {
                    for (final RequestParameter tmp : entry.getValue()) {
                        if (StringUtils.isNotBlank(tmp.getString())) {
                            values.add(tmp.getString());
                        }
                    }
                }

                if (!values.isEmpty()) {
                    valuesFromRequest.put(entry.getKey(), values.toArray(new String[values.size()]));
                }
            }
        }

        return valuesFromRequest;
    }
}