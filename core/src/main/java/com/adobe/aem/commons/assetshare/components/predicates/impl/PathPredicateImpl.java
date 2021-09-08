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
import com.adobe.aem.commons.assetshare.components.predicates.PathPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.PropertyPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.SelectedOptionItem;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.UnselectedOptionItem;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.search.eval.PathPredicateEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {PathPredicate.class, ComponentExporter.class},
        resourceType = {PathPredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class PathPredicateImpl extends AbstractPredicate implements PathPredicate, Options {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/path";
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
    private String operation;

    @ValueMapValue
    private Boolean expanded;

    @ValueMapValue(name = PathPredicateImpl.PN_TYPE)
    private String typeString;

    @PostConstruct
    protected void init() {
        initPredicate(request, coreOptions);
    }

    /* Options - Core Component Delegates */

    public List<OptionItem> getItems() {
        final ValueMap initialValues = getInitialValues();
        final List<OptionItem> processedOptionItems = new ArrayList<>();
        final boolean useDefaultSelected = !isParameterizedSearchRequest();

        coreOptions.getItems().stream()
                .filter(optionItem -> request.getResourceResolver().getResource(optionItem.getValue()) != null)
                .forEach(optionItem -> {
                    if (PredicateUtil.isOptionInInitialValues(optionItem, initialValues)) {
                        processedOptionItems.add(new SelectedOptionItem(optionItem));
                    } else if (useDefaultSelected) {
                        processedOptionItems.add(optionItem);
                    } else {
                        processedOptionItems.add(new UnselectedOptionItem(optionItem));
                    }
                });

        return processedOptionItems;
    }

    public Type getType() {
        return coreOptions.getType();
    }

    /* Property Predicate Specific */

    @Override
    public String getSubType() {
        //support variation of Checkboxes
        return typeString;
    }

    @Override
    public String getName() {
        return PathPredicateEvaluator.PATH;
    }

    @Override
    public boolean isReady() {
        return coreOptions.getItems().size() > 0;
    }

    @Override
    public String getInitialValue() {
        if (valueFromRequest == null) {
            valueFromRequest = PredicateUtil.getInitialValue(request, this, getName());
        }

        return valueFromRequest;
    }

    @Override
    public ValueMap getInitialValues() {
        if (valuesFromRequest == null) {
            valuesFromRequest = PredicateUtil.getInitialValues(request, this, getName());
        }

        return valuesFromRequest;
    }

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}