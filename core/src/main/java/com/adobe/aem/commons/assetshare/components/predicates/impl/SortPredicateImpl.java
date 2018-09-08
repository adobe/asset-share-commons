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
import com.adobe.aem.commons.assetshare.components.predicates.SortPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.SelectedOptionItem;
import com.adobe.aem.commons.assetshare.components.search.SearchConfig;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.search.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {SortPredicate.class},
        resourceType = {SortPredicateImpl.RESOURCE_TYPE}
)
public class SortPredicateImpl extends AbstractPredicate implements SortPredicate {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/sort";

    protected ValueMap valuesFromRequest = null;

    private static final String INITIAL_VALUES_ORDER_BY = "orderBy";
    private static final String INITIAL_VALUES_ORDER_BY_SORT = "orderBySort";
    private static final String UNKNOWN_SORT_BY = "Unknown";

    private List<OptionItem> items = null;

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private Options coreOptions;

    @Self
    @Required
    private SearchConfig searchConfig;

    @ValueMapValue
    @Default(values = "ASC")
    private String ascendingLabel;

    @ValueMapValue
    @Default(values = "DESC")
    private String descendingLabel;

    @PostConstruct
    protected void init() {
        initPredicate(request, coreOptions);
    }

    @Override
    public List<OptionItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
            final ValueMap initialValues = getInitialValues();

            coreOptions.getItems().stream().forEach(optionItem -> {
                if (PredicateUtil.isOptionInInitialValues(optionItem, initialValues)) {
                    items.add(new SelectedOptionItem(optionItem));
                } else {
                    items.add(optionItem);
                }
            });
        }

        return items;
    }

    @Override
    public String getOrderBy() {
        return getInitialValues().get(INITIAL_VALUES_ORDER_BY, String.class);
    }

    @Override
    public String getOrderByLabel() {
        String label = UNKNOWN_SORT_BY;
        for (final OptionItem optionItem : getItems()) {
            if (optionItem.isSelected()) {
                label = optionItem.getText();
                break;
            }
        }

        return label;
    }

    @Override
    public String getOrderBySort() {
        return getInitialValues().get(INITIAL_VALUES_ORDER_BY_SORT, String.class);
    }

    @Override
    public String getOrderBySortLabel() {
        if (isAscending()) {
            return ascendingLabel;
        } else {
            return descendingLabel;
        }
    }

    @Override
    public boolean isAscending() {
         final String orderBySort = getInitialValues().get(Predicate.PARAM_SORT, String.class);
         return Predicate.SORT_ASCENDING.equalsIgnoreCase(orderBySort);
    }

    @Override
    public String getName() {
        return "orderby";
    }

    @Override
    public boolean isReady() {
        return getItems().size() > 0;
    }

    @Override
    public ValueMap getInitialValues() {
        if (valuesFromRequest == null) {
            valuesFromRequest = new ValueMapDecorator(new HashMap<>());

            String orderBy = PredicateUtil.getParamFromQueryParams(request, Predicate.ORDER_BY);
            if (StringUtils.isBlank(orderBy)) {
                orderBy = searchConfig.getOrderBy();
            }
            valuesFromRequest.put(INITIAL_VALUES_ORDER_BY, orderBy);

            String orderBySort = PredicateUtil.getParamFromQueryParams(request, Predicate.ORDER_BY + "." + Predicate.PARAM_SORT);
            if (StringUtils.isBlank(orderBySort)) {
                orderBySort = searchConfig.getOrderBySort();
            }
            valuesFromRequest.put(INITIAL_VALUES_ORDER_BY_SORT, orderBySort);
        }

        return valuesFromRequest;
    }
}
