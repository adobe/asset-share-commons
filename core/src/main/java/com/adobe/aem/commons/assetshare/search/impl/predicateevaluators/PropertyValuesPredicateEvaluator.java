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

package com.adobe.aem.commons.assetshare.search.impl.predicateevaluators;

import com.day.cq.search.Predicate;
import com.day.cq.search.eval.EvaluationContext;
import com.day.cq.search.eval.FulltextPredicateEvaluator;
import com.day.cq.search.eval.JcrPropertyPredicateEvaluator;
import com.day.cq.search.eval.PredicateEvaluator;
import com.day.cq.search.facets.FacetExtractor;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.jcr.query.Row;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * The QueryBuilder predicate for this Sample would be structured like so...
 * <p>
 * type=cq:PageContent<br>
 * path=/content<br>
 * <p>
 * propertyvalues.values=val1,val2<br>
 * propertyvalues.delimiter=,<br>
 * propertyvalues.XXX &lt;- all other JcrPropertyPredicateEvaluator configs<br>
 * <p>
 * `values` is the list of values to break out into OOTB property.#_property=value[#]<br>
 * `delimiter` is the delimiter which is used to split the values string
 */
@Component(
        factory = "com.day.cq.search.eval.PredicateEvaluator/" + PropertyValuesPredicateEvaluator.PREDICATE_NAME
)
@Designate(
        ocd = PropertyValuesPredicateEvaluator.Cfg.class
)
public class PropertyValuesPredicateEvaluator implements PredicateEvaluator {
    private PredicateEvaluator propertyEvaluator = new JcrPropertyPredicateEvaluator();
    private PredicateEvaluator fulltextEvaluator = new FulltextPredicateEvaluator();

    private static final String OP_STARTS_WITH = "startsWith";
    private static final String OP_CONTAINS = "contains";

    protected static final String PREDICATE_BUILT_KEY = "__asset-share-commons--predicate-built";
    protected static final String PREDICATE_BUILT_VALUE = "true";
    protected static final String DELIMITER_CODE_NONE = "__NONE";
    protected static final String DELIMITER_CODE_WHITESPACE = "__WS";

    private Map<String, String> delimiterMapping = new HashMap<>();

    protected Cfg cfg;

    public static final String PREDICATE_NAME = "propertyvalues";
    public static final String VALUES = "values";
    private static final String DELIMITER = "delimiter";

    protected Predicate buildPredicate(final Predicate predicate) {
        if (PREDICATE_BUILT_VALUE.equals(predicate.get(PREDICATE_BUILT_KEY))) {
            return predicate;
        }

        final List<String> delimiters = getDelimiters(predicate);
        final List<String> values = new ArrayList<>();

        PredicateEvaluatorUtil.getValues(predicate, VALUES, true)
                .forEach(value -> values.addAll(getValues(value, delimiters)));

        if (isFulltextOperation(predicate)) {
            buildFulltextPredicate(predicate, values, predicate.get(JcrPropertyPredicateEvaluator.PROPERTY));
        } else {
            buildPropertyPredicate(predicate, values);
        }

        predicate.set(PREDICATE_BUILT_KEY, PREDICATE_BUILT_VALUE);

        return predicate;
    }

    private void buildPropertyPredicate(final Predicate propertyPredicate, final List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            propertyPredicate.set(i + "_" + JcrPropertyPredicateEvaluator.VALUE, values.get(i));
        }

        // Clears out propertyvalues specific params (.#_values and .#_delimiter)
        propertyPredicate.getParameters().entrySet().stream()
                .filter(entry -> entry.getKey().matches("^(\\d+_)?((" + VALUES + ")|(" + DELIMITER + "))$"))
                .forEach(entry -> propertyPredicate.set(entry.getKey(), null));
    }

    private void buildFulltextPredicate(final Predicate fulltextPredicate, final List<String> values, String property) {
        final String operation = fulltextPredicate.get(JcrPropertyPredicateEvaluator.OPERATION);

        final String queryParam = values.stream()
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .filter(value -> value.length() >= 3)
                .map(value -> buildFulltextValue(operation, value))
                .collect(Collectors.joining(" OR "));

        fulltextPredicate.set(FulltextPredicateEvaluator.FULLTEXT, queryParam);

        property = StringUtils.removeStart(property, "./");
        final String[] propertySegments = StringUtils.split(property, "/");
        final int lastIndex = propertySegments.length - 1;
        propertySegments[lastIndex] = "@" + propertySegments[lastIndex];
        property = StringUtils.join(propertySegments, "/");

        fulltextPredicate.set(FulltextPredicateEvaluator.REL_PATH, property);

        // Clears out non-fulltext specific params
        fulltextPredicate.getParameters().entrySet().stream()
                .filter(entry -> !entry.getKey().matches("^(\\d+_)?((" + FulltextPredicateEvaluator.FULLTEXT + ")|(" + FulltextPredicateEvaluator.REL_PATH + "))$"))
                .forEach(entry -> fulltextPredicate.set(entry.getKey(), null));
    }


    private String buildFulltextValue(String operation, String value) {
        value = StringUtils.strip(value, "*") + "*";

        if (!OP_STARTS_WITH.equals(operation)) {
            value = "*" + value;
        }

        return value;
    }

    private boolean isFulltextOperation(Predicate predicate) {
        return predicate.get(FulltextPredicateEvaluator.FULLTEXT) != null ||
                ArrayUtils.contains(new String[]{OP_STARTS_WITH, OP_CONTAINS}, predicate.get(JcrPropertyPredicateEvaluator.OPERATION));
    }

    protected PredicateEvaluator getPredicateEvaluator(Predicate predicate) {
        if (isFulltextOperation(predicate)) {
            return fulltextEvaluator;
        } else {
            return propertyEvaluator;
        }
    }

    @Override
    public String getXPathExpression(Predicate predicate, EvaluationContext evaluationContext) {
        return getPredicateEvaluator(predicate).getXPathExpression(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public boolean includes(final Predicate predicate, final Row row, final EvaluationContext evaluationContext) {
        return getPredicateEvaluator(predicate).includes(buildPredicate(predicate), row, evaluationContext);
    }

    @Override
    public boolean canXpath(final Predicate predicate, final EvaluationContext evaluationContext) {
        return getPredicateEvaluator(predicate).canXpath(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public boolean canFilter(final Predicate predicate, final EvaluationContext evaluationContext) {
        return getPredicateEvaluator(predicate).canFilter(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public boolean isFiltering(final Predicate predicate, final EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("isFiltering(..) is deprecated.");
    }

    @Override
    public String[] getOrderByProperties(Predicate predicate, EvaluationContext evaluationContext) {
        return getPredicateEvaluator(predicate).getOrderByProperties(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public Comparator<Row> getOrderByComparator(Predicate predicate, EvaluationContext evaluationContext) {
        return getPredicateEvaluator(predicate).getOrderByComparator(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public FacetExtractor getFacetExtractor(Predicate predicate, EvaluationContext evaluationContext) {
        return getPredicateEvaluator(predicate).getFacetExtractor(buildPredicate(predicate), evaluationContext);
    }

    protected List<String> getValues(final String data, final List<String> delimiters) {
        if (delimiters.size() == 0) {
            return ImmutableList.<String>builder().add(data).build();
        }

        final String regex = delimiters.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("|"));

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        if (data == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(pattern.split(data))
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected List<String> getDelimiters(final Predicate predicate) {
        final List<String> delimiterValues = PredicateEvaluatorUtil.getValues(predicate, DELIMITER, true);

        if (delimiterValues.stream().anyMatch(DELIMITER_CODE_NONE::equals)) {
            // "None" is the in the list so do not process ANY of the delimiters
            return emptyList();
        }

        final List<String> delimiters = delimiterValues.stream()
                .map(this::resolveDelimiter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (delimiters.isEmpty() && cfg.delimiters_default() != null) {
            // If the delimiters is completely empty, then use the default list
            return Stream.of(cfg.delimiters_default()).map(Pattern::quote).collect(Collectors.toList());
        } else {
            // Else return the passed in delimiters
            return delimiters;
        }
    }

    private String resolveDelimiter(String delimiter) {
        final String resolvedDelimiter = delimiterMapping.get(delimiter);

        if (DELIMITER_CODE_NONE.equals(delimiter)) {
            return null;
        } else if (DELIMITER_CODE_WHITESPACE.equals(delimiter)) {
            return "\\s";
        } else if (resolvedDelimiter != null) {
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

        if (cfg.delimiters_mapping() != null) {
            Arrays.stream(cfg.delimiters_mapping()).forEach(mapping -> {
                final String key = StringUtils.substringBefore(mapping, "=");
                final String value = StringUtils.substringAfter(mapping, "=");

                if (key != null) {
                    delimiterMapping.put(key, value);
                }
            });
        }
    }

    @ObjectClassDefinition(name = "Asset Share Commons - Properties Values Predicate Evaluator")
    public @interface Cfg {
        @AttributeDefinition(
                name = "Default delimiter",
                description = "The default delimiters to use when none no #_delimiter= is specified. Defaults to ','."
        )
        String[] delimiters_default() default {","};

        @AttributeDefinition(
                name = "Delimiters mapping",
                description = "Defines custom mappings for delimiter codes to actual delimiters."
        )
        String[] delimiters_mapping() default {};
    }
}