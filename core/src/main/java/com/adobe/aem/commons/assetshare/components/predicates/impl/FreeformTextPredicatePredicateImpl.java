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

package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.AbstractPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.FreeformTextPredicate;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.EMPTY_LIST;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {FreeformTextPredicate.class, ComponentExporter.class},
        resourceType = {FreeformTextPredicatePredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
public class FreeformTextPredicatePredicateImpl extends AbstractPredicate implements FreeformTextPredicate {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/freeform-text";
    private static final String CUSTOM_DELIMITER = "__CUSTOM_DELIMITER";

    private static final String PN_DELIMITER_VALUE = "value";
    private static final String PN_DELIMITER_CUSTOM_VALUE = "customValue";
    private static final String NN_DELIMITERS = "delimiters";

    private static final String OP_STARTS_WITH = "startsWith";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @ValueMapValue
    @Named(JcrConstants.JCR_TITLE)
    private String title;

    @ValueMapValue
    private String property;

    @ValueMapValue
    private String operation;

    @ValueMapValue
    private String placeholder;

    @ValueMapValue
    private Integer inputValidationMinLength;

    @ValueMapValue
    private Integer inputValidationMaxLength;

    @ValueMapValue
    private String inputValidationPattern;

    @ValueMapValue
    private String inputValidationMessage;

    @ValueMapValue
    @Default(intValues = 1)
    private int rows;

    @ValueMapValue(name = PropertyPredicateImpl.PN_TYPE)
    private String typeString;
    
    protected List<String> delimiters = null;
    protected String valueFromRequest = null;
    protected ValueMap valuesFromRequest = null;

    @PostConstruct
    protected void init() {
        super.initGroup(request);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getPlaceholder() {
        return this.placeholder;
    }

    @Override
    public String getName() {
        return PropertyValuesPredicateEvaluator.PREDICATE_NAME;
    }

    @Override
    public int getRows() {
        return this.rows;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    @Override
    public boolean hasOperation() {
        return StringUtils.isNotBlank(getOperation());
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public Integer getInputValidationMinLength() {
        if (StringUtils.equals(OP_STARTS_WITH, getOperation()) &&
                (inputValidationMinLength == null || inputValidationMinLength < 3)) {
            // Magic minimum number for like operations
            return 3;
        } else {
            return inputValidationMinLength;
        }
    }

    @Override
    public Integer getInputValidationMaxLength() {
        return inputValidationMaxLength;
    }

    @Override
    public String getInputValidationPattern() {
        return StringUtils.stripToNull(inputValidationPattern);
    }

    @Override
    public String getInputValidationMessage() {
        return StringUtils.stripToNull(inputValidationMessage);
    }

    @Override
    public List<String> getDelimiters() {

        if (delimiters == null) {
            Resource delimitersResource = request.getResource().getChild(NN_DELIMITERS);

            if (delimitersResource == null) {
                return EMPTY_LIST;
            }

            delimiters = StreamSupport.stream(delimitersResource.getChildren().spliterator(), false).map(option -> {
                final ValueMap properties = option.getValueMap();
                final String value = properties.get(PN_DELIMITER_VALUE, String.class);
                final String customValue = properties.get(PN_DELIMITER_CUSTOM_VALUE, String.class);

                if (CUSTOM_DELIMITER.equals(value)) {
                    return customValue;
                } else {
                    return value;
                }

            }).collect(Collectors.toList());
        }

        return delimiters;
    }

    @Override
    public boolean isReady() {
        return true;
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

    @Nonnull
    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }
}
