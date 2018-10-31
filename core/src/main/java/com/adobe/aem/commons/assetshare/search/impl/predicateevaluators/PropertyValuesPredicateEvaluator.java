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

package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import com.day.cq.search.eval.EvaluationContext;
import com.day.cq.search.eval.PredicateEvaluator;
import com.day.cq.search.facets.FacetExtractor;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Row;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.EMPTY_LIST;

/**
 * The QueryBuilder predicate for this Sample would be structured like so...
 * <p>
 * type=cq:PageContent
 * path=/content
 * <p>
 * propertyvalues.values=val1,val2
 * propertyvalues.delimiter=,
 * propertyvalues.XXX <- all other JcrPropertyPredicateEvaluator configs
 * <p>
 * `values` is the list of values to break out into OOTB property.#_property=value[#]
 * `delimiter` is the delimiter which is used to split the values string
 */
@Component(factory = "com.day.cq.search.eval.PredicateEvaluator/" + PropertyValuesPredicateEvaluator.PREDICATE_NAME)
@Designate(ocd = PropertyValuesPredicateEvaluator.Cfg.class)
public class PropertyValuesPredicateEvaluator implements PredicateEvaluator {
    private static final Logger log = LoggerFactory.getLogger(PropertyValuesPredicateEvaluator.class);

    private PredicateEvaluator propertyEvaluator = new com.day.cq.search.eval.JcrPropertyPredicateEvaluator();

    private static final String PREDICATE_BUILT_KEY = "__asset-share-commons--predicate-built";
    private static final String PREDICATE_BUILT_VALUE = "true";
    private static final String DELIMITER_CODE_NONE = "_D0";

    private static Map<String, String> delimiterMapping;

    private Cfg cfg;

    public static final String PREDICATE_NAME = "propertyvalues";
    public static final String VALUES = "values";
    public static final String DELIMITER = "delimiter";

    public Predicate buildPredicate(Predicate predicate) {
        if (PREDICATE_BUILT_VALUE.equals(predicate.get(PREDICATE_BUILT_KEY))) {
            return predicate;
        }

        final List<String> delimiters = getDelimiters(predicate);
        final List<String> values = new ArrayList<>();

        PredicateEvaluatorUtil.getValues(predicate, VALUES, true).stream().forEach(value -> {
            values.addAll(getValues(value, delimiters));
        });

        for (int i = 0; i < values.size(); i++) {
            predicate.set(i + "_value", values.get(i));
        }

        predicate.set(PREDICATE_BUILT_KEY, PREDICATE_BUILT_VALUE);

        return predicate;
    }

    @Override
    public String getXPathExpression(Predicate predicate, EvaluationContext evaluationContext) {
        return propertyEvaluator.getXPathExpression(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public boolean includes(final Predicate predicate, final Row row, final EvaluationContext evaluationContext) {
        return propertyEvaluator.includes(buildPredicate(predicate), row, evaluationContext);
    }

    @Override
    public boolean canXpath(final Predicate predicate, final EvaluationContext evaluationContext) {
        return propertyEvaluator.canXpath(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public boolean canFilter(final Predicate predicate, final EvaluationContext evaluationContext) {
        return propertyEvaluator.canFilter(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public boolean isFiltering(final Predicate predicate, final EvaluationContext evaluationContext) {
        return propertyEvaluator.isFiltering(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public String[] getOrderByProperties(Predicate predicate, EvaluationContext evaluationContext) {
        return propertyEvaluator.getOrderByProperties(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public Comparator<Row> getOrderByComparator(Predicate predicate, EvaluationContext evaluationContext) {
        return propertyEvaluator.getOrderByComparator(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public FacetExtractor getFacetExtractor(Predicate predicate, EvaluationContext evaluationContext) {
        return propertyEvaluator.getFacetExtractor(buildPredicate(predicate), evaluationContext);
    }

    protected List<String> getValues(final String data, final List<String> delimiters) {
        final String regex = delimiters.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("|"));

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        if (pattern == null) {
            log.warn("Could not compile pattern for delimited using regex [ {} ]. Returning data as is [ {} ].", regex, data);
            return Arrays.asList(new String[]{data});
        }

        return Arrays.stream(pattern.split(data))
                .map(StringUtils::trimToNull)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    protected List<String> getDelimiters(final Predicate predicate) {
        final List<String> delimiters;
        final List<String> delimiterValues = PredicateEvaluatorUtil.getValues(predicate, DELIMITER, true);

        if (delimiterValues.stream()
                .filter(code -> DELIMITER_CODE_NONE.equals(code))
                .findFirst().isPresent()) {
            // "None" is the in the list so do process ANY of the delimiters
            return EMPTY_LIST;
        }

        delimiters = delimiterValues.stream()
                .map(delimiter -> resolveDelimiter(delimiter))
                .filter(delimiter -> delimiter != null)
                .collect(Collectors.toList());

        if (delimiters.isEmpty()) {
            // If the delimiters is completely empty, then use the default list
            return Arrays.asList(cfg.delimiters_default());
        } else {
            // Else return the passed in delimiters
            return delimiters;
        }
    }

    private String resolveDelimiter(final String delimiter) {
        final String resolvedDelimiter = delimiterMapping.get(delimiter);

        if (resolvedDelimiter != null) {
            return Pattern.quote(resolvedDelimiter);
        } else if (delimiter != null) {
            return Pattern.quote(delimiter);
        } else {
            return null;
        }
    }

    @Activate
    protected void activate(final Cfg cfg) {
        this.cfg = cfg;

        delimiterMapping = new HashMap<>();

        Arrays.stream(cfg.delimiters_mapping()).forEach(mapping -> {
            final String key = StringUtils.substringBefore(mapping, "=");
            final String value = StringUtils.substringAfter(mapping, "=");

            if (StringUtils.isNotBlank(key)) {
                delimiterMapping.put(key, value);
            }
        });
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Properties Values Predicate Evaluator")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Default delimiter",
                description = "The default delimiters to use when none no #_delimiter= is specified. Defaults to ','."
        )
        String[] delimiters_default() default { "," };

        @AttributeDefinition(
                name = "Delimiters mapping",
                description = "Defines the type of data this exposes. This classification allows for intelligent exposure of Computed Properties in DataSources, etc."
        )
        String[] delimiters_mapping() default { "_D0=NONE", "_D1=\\s", "_D2=\\t", "_D3=\\n\\r"};
    }
}