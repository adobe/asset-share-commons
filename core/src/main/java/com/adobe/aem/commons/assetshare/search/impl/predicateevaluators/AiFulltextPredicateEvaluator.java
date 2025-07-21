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

import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.day.cq.search.Predicate;
import com.day.cq.search.eval.EvaluationContext;
import com.day.cq.search.eval.FulltextPredicateEvaluator;
import com.day.cq.search.eval.PredicateEvaluator;
import com.day.cq.search.facets.FacetExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;

import javax.jcr.query.Row;
import java.util.*;

import static java.util.Collections.emptyList;

/**
 * This is a wrapper about the AEM OOTB FulltextPredicateEvaluator that adds AI Search support
 */
@Component(
        service = { PredicateEvaluator.class },
        factory = "com.day.cq.search.eval.PredicateEvaluator/" + AiFulltextPredicateEvaluator.PREDICATE_NAME
)
public class AiFulltextPredicateEvaluator implements PredicateEvaluator {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AiFulltextPredicateEvaluator.class);

    @Reference(target = "(distribution=cloud-ready)")
    RequireAem requireAem;

    private static final String AI_SEARCH_TOKEN = System.getProperty("com.adobe.granite.omnisearch.inference.query.prefix", "?{}?");

    public static final String PREDICATE_NAME = "ai-" + FulltextPredicateEvaluator.FULLTEXT;

    protected static final String PREDICATE_BUILT_KEY = "__asset-share-commons--predicate-built";
    protected static final String PREDICATE_BUILT_VALUE = "true";

    private PredicateEvaluator fulltextEvaluator = new FulltextPredicateEvaluator();

    protected Predicate buildPredicate(final Predicate predicate) {
        if (PREDICATE_BUILT_VALUE.equals(predicate.get(PREDICATE_BUILT_KEY))) {
            return predicate;
        }

        String value = predicate.get(PREDICATE_NAME, "");

        if (AiSearchRenderCondition.isAiSearchEnabled() && !StringUtils.isBlank(value)) {
            value = AI_SEARCH_TOKEN.concat(value);
        }

        predicate.set(FulltextPredicateEvaluator.FULLTEXT, value);
        predicate.set(PREDICATE_NAME, null);
        predicate.set(PREDICATE_BUILT_KEY, PREDICATE_BUILT_VALUE);

        return predicate;
    }

    protected PredicateEvaluator getPredicateEvaluator(Predicate predicate) {
        return fulltextEvaluator;
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
}




