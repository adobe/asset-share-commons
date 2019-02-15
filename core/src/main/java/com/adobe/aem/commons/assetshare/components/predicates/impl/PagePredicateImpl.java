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
import com.adobe.aem.commons.assetshare.components.predicates.HiddenPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.PagePredicate;
import com.adobe.aem.commons.assetshare.components.search.SearchConfig;
import com.adobe.aem.commons.assetshare.search.searchpredicates.SearchPredicate;
import com.adobe.aem.commons.assetshare.util.ComponentModelVisitor;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.eval.PathPredicateEvaluator;
import com.day.cq.search.eval.TypePredicateEvaluator;
import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.factory.ModelFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {PagePredicate.class},
        resourceType = {PagePredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class PagePredicateImpl extends AbstractPredicate implements PagePredicate {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/results";

    private static final int MAX_LIMIT = 1000;
    private static final int DEFAULT_LIMIT = 50;

    @Self
    @Required
    SlingHttpServletRequest request;

    @Inject
    @Required
    private Page currentPage;

    @Self
    @Required
    private SearchConfig searchConfig;

    @OSGiService
    private ModelFactory modelFactory;

    @OSGiService
    private List<SearchPredicate> searchPredicates;

    @PostConstruct
    protected void init() {
        initPredicate(request, null);
    }

    @Override
    public String getName() {
        return PredicateConverter.GROUP_PARAMETER_PREFIX;
    }

    @Override
    public boolean isReady() {
        return true;
    }


    public String getOrderBy() {
        final String value = PredicateUtil.getParamFromQueryParams(request, Predicate.ORDER_BY);
        return StringUtils.defaultIfBlank(value, searchConfig.getOrderBy());
    }

    public String getOrderBySort() {
        final String value = PredicateUtil.getParamFromQueryParams(request, Predicate.ORDER_BY + "." + Predicate.PARAM_SORT);
        return StringUtils.defaultIfBlank(value, searchConfig.getOrderBySort());
    }

    private boolean isOrderByCase() {
        if (StringUtils.isBlank(PredicateUtil.getParamFromQueryParams(request, Predicate.ORDER_BY))) {
            // If orderby is NOT passed via HTTP params, then assume the default Results component config should be used.
            return searchConfig.isOrderByCase();
        } else {
            // Else, look for order.case on the request.
            final String value = PredicateUtil.getParamFromQueryParams(request, Predicate.ORDER_BY + "." + Predicate.PARAM_CASE);
            // Remember, this method is orderByCase, so if its case INSENSITIVE (ie. ignore) then we return false.
            return !Predicate.IGNORE_CASE.equals(value);
        }
    }

    public int getLimit() {
        final RequestParameter requestParameter = request.getRequestParameter("p.limit");
        int limit = searchConfig.getLimit();

        if (requestParameter != null) {
            try {
                limit = Integer.parseInt(requestParameter.getString());
            } catch (NumberFormatException e) {
                // Use the configuration-provided default
            }
        }

        if (limit > MAX_LIMIT) {
            return MAX_LIMIT;
        } else if (limit < 1) {
            return DEFAULT_LIMIT;
        } else {
            return limit;
        }
    }

    public int getOffset() {
        final RequestParameter requestParameter = request.getRequestParameter("p.offset");
        int offset = 0;

        if (requestParameter != null) {
            try {
                offset = Integer.parseInt(requestParameter.getString());
            } catch (NumberFormatException e) {
                // Use 0
            }
        }

       return offset;
    }

    public String getGuessTotal() {
        return searchConfig.getGuessTotal();
    }

    public List<String> getPaths() {
        return searchConfig.getPaths();
    }

    @Override
    public PredicateGroup getPredicateGroup() {
        return getPredicateGroup(new ParamTypes[]{});
    }

    @Override
    public PredicateGroup getPredicateGroup(ParamTypes... excludeParamTypes) {
        final PredicateGroup root = new PredicateGroup();
        final PredicateGroup parameterGroup = new PredicateGroup(PredicateConverter.GROUP_PARAMETER_PREFIX);

        // Type Predicate
        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.NODE_TYPE)) {
            addTypeAsPredicate(root);
        }

        // Path Predicate
        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.PATH)) {
            addPathAsPredicateGroup(root);
        }

        // OrderBy Parameters
        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.ORDERBY)) {
            addOrderByAsPredicate(root);
        }

        // Hidden Predicates
        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.HIDDEN_PREDICATES)) {
            addHiddenPredicatesAsPredicateGroups(root);
        }

        // Search Predicates
        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.SEARCH_PREDICATES)) {
            addSearchPredicateAsPredicateGroups(root);
        }

        // QueryBuilder Parameters

        // p.offset
        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.OFFSET)) {
            addOffsetAsParameterPredicate(parameterGroup);
        }

        // p.limit
        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.LIMIT)) {
            addLimitAsParameterPredicate(parameterGroup);
        }

        // p.guessTotal
        if (!ArrayUtils.contains(excludeParamTypes, ParamTypes.GUESS_TOTAL)) {
            addGuessTotalAsParameterPredicate(parameterGroup);
        }

        root.add(parameterGroup);

        return root;
    }

    private void addGuessTotalAsParameterPredicate(final PredicateGroup parameterGroup) {
        parameterGroup.addAll(PredicateConverter.createPredicates(ImmutableMap.<String, String>builder().
                put(Predicate.PARAM_GUESS_TOTAL, getGuessTotal()).
                build()));
    }

    private void addOffsetAsParameterPredicate(final PredicateGroup parameterGroup) {
        parameterGroup.addAll(PredicateConverter.createPredicates(ImmutableMap.<String, String>builder().
                put(Predicate.PARAM_OFFSET,  String.valueOf(getOffset())).
                build()));
    }

    private void addLimitAsParameterPredicate(final PredicateGroup parameterGroup) {
        parameterGroup.addAll(PredicateConverter.createPredicates(ImmutableMap.<String, String>builder().
                put(Predicate.PARAM_LIMIT, String.valueOf(getLimit())).
                build()));
    }

    private void addSearchPredicateAsPredicateGroups(final PredicateGroup root) {
        for (final SearchPredicate searchPredicate : getSearchPredicates()) {
            final PredicateGroup global = new PredicateGroup();

            global.addAll(searchPredicate.getPredicateGroup(request));
            root.add(global);
        }
    }

    private void addHiddenPredicatesAsPredicateGroups(final PredicateGroup root) {
        for (final HiddenPredicate hiddenPredicate : getHiddenPredicates(currentPage)) {
            final PredicateGroup hidden = new PredicateGroup();

            hidden.addAll(hiddenPredicate.getPredicateGroup());
            root.add(hidden);
        }
    }

    private void addPathAsPredicateGroup(final PredicateGroup root) {
        final PredicateGroup paths = new PredicateGroup();
        paths.setAllRequired(false);

        final Map<String, String> params = new HashMap<>();

        int i = 0;
        for (final String path : getPaths()) {
            params.put(i++ + "_" + PathPredicateEvaluator.PATH, path);
        }

        paths.addAll(PredicateConverter.createPredicates(params));
        root.add(paths);
    }

    private void addTypeAsPredicate(final PredicateGroup root) {
        root.addAll(PredicateConverter.createPredicates(ImmutableMap.<String, String>builder().
                put(TypePredicateEvaluator.TYPE, DamConstants.NT_DAM_ASSET).
                build()));
    }


    private void addOrderByAsPredicate(final PredicateGroup root) {
        Builder<String, String> orderPredicateBuilder = ImmutableMap.<String, String>builder().
            put(Predicate.ORDER_BY, searchConfig.getOrderBy()).
            put(Predicate.ORDER_BY + "." + Predicate.PARAM_SORT, searchConfig.getOrderBySort());

        if (!isOrderByCase()) {
            orderPredicateBuilder.put(Predicate.ORDER_BY + "." + Predicate.PARAM_CASE, Predicate.IGNORE_CASE);
        }

        root.addAll(PredicateConverter.createPredicates(orderPredicateBuilder.build()));
    }

    private List<SearchPredicate> getSearchPredicates() {
        final List<String> searchPredicateNames = searchConfig.getSearchPredicatesNames();
        final List<SearchPredicate> matchingSearchPredicates = new ArrayList<>();

        for (String searchPredicateName : searchPredicateNames) {
            searchPredicates.stream()
                    .filter(gp -> StringUtils.equals(searchPredicateName, gp.getName()))
                    .findFirst()
                    .ifPresent(matchingSearchPredicates::add);
        }

        return matchingSearchPredicates;
    }

    private Collection<HiddenPredicate> getHiddenPredicates(final Page page) {
        final ComponentModelVisitor<HiddenPredicate> visitor = new ComponentModelVisitor<HiddenPredicate>(request,
                modelFactory,
                new String[]{HiddenPredicateImpl.RESOURCE_TYPE},
                HiddenPredicate.class);

        visitor.accept(page.getContentResource());
        return visitor.getModels();
    }

    /**
     * Deprecated Methods
     **/

    @Override
    @Deprecated
    public Map<String, String> getParams() {
        return getParams(new ParamTypes[]{});
    }

    @Override
    @Deprecated
    public Map<String, String> getParams(ParamTypes... excludeParamTypes) {
        return PredicateConverter.createMap(getPredicateGroup(excludeParamTypes));
    }
}