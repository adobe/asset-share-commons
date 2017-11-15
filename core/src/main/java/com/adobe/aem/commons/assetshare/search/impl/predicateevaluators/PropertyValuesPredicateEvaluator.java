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
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.query.Row;
import java.util.*;

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
public class PropertyValuesPredicateEvaluator implements PredicateEvaluator {

    public static final String PREDICATE_NAME = "propertyvalues";
    public static final String VALUES = "values";
    private static final String DELIMITER = "delimiter";
    private static final String DEFAULT_DELIMITER = ",";

    private PredicateEvaluator propertyEvaluator = new com.day.cq.search.eval.JcrPropertyPredicateEvaluator();

    private Predicate buildPredicate(Predicate predicate) {
        final String delimiter = StringUtils.defaultIfEmpty(predicate.get(DELIMITER), DEFAULT_DELIMITER);
        final List<String> properties = new ArrayList<>();

        for (final Map.Entry<String, String> entry : predicate.getParameters().entrySet()) {
            if (entry.getValue() != null &&
                    entry.getKey() != null &&
                    (VALUES.equals(entry.getKey()) || StringUtils.endsWith(entry.getKey(), "_" + VALUES))) {
                properties.addAll(Arrays.asList(StringUtils.split(entry.getValue(), delimiter)));
                predicate.set(entry.getKey(), null);
            }
        }

        predicate.set(DELIMITER, null);

        for (int i = 0; i < properties.size(); i++) {
            predicate.set(i + "_value", properties.get(i));
        }

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
        return getOrderByComparator(buildPredicate(predicate), evaluationContext);
    }

    @Override
    public FacetExtractor getFacetExtractor(Predicate predicate, EvaluationContext evaluationContext) {
        return propertyEvaluator.getFacetExtractor(buildPredicate(predicate), evaluationContext);
    }
}