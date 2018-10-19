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
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.SortOptionItem;
import com.adobe.aem.commons.assetshare.components.search.SearchConfig;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.search.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {SortPredicate.class},
        resourceType = {SortPredicateImpl.RESOURCE_TYPE}
)
public class SortPredicateImpl extends AbstractPredicate implements SortPredicate {

  protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/sort";

    private static final String NN_ITEMS = "items" ;
    private static final String PN_TEXT = "text";
    private static final String PN_ORDER_BY_CASE = "orderByCase";

  protected ValueMap valuesFromRequest = null;

    private static final String UNKNOWN_SORT_BY = "Unknown";

    private List<SortOptionItem> items = new ArrayList<>();

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
        this.populateOptionItems();
    }

    @Override
    public List<SortOptionItem> getItems() {
      List<SortOptionItem> sortOptionItems = new ArrayList<>();
      final ValueMap initialValues = getInitialValues();

      items.stream().forEach(optionItem -> {
        if (PredicateUtil.isOptionInInitialValues(optionItem.getValue(), initialValues)) {
          optionItem.setSelected(true);
          sortOptionItems.add(optionItem);
        } else {
          optionItem.setSelected(false);
          sortOptionItems.add(optionItem);
        }

      });

      return sortOptionItems;
    }

    @Override
    public String getOrderByLabel() {
        String label = UNKNOWN_SORT_BY;
        for (final SortOptionItem optionItem : getItems()) {
            if (optionItem.isSelected()) {
                label = optionItem.getText();
                break;
            }
        }

        return label;
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
        return !getItems().isEmpty();
    }

    @Override
    public ValueMap getInitialValues() {
        if (valuesFromRequest == null) {
            valuesFromRequest = new ValueMapDecorator(new HashMap<>());

            String orderBy = PredicateUtil.getParamFromQueryParams(request, Predicate.ORDER_BY);
            if (StringUtils.isBlank(orderBy)) {
                orderBy = searchConfig.getOrderBy();
            }
            valuesFromRequest.put(Predicate.ORDER_BY, orderBy);

            String orderBySort = PredicateUtil.getParamFromQueryParams(request, Predicate.ORDER_BY + "." + Predicate.PARAM_SORT);
            if (StringUtils.isBlank(orderBySort)) {
                orderBySort = searchConfig.getOrderBySort();
            }
            valuesFromRequest.put(Predicate.PARAM_SORT, orderBySort);
        }

        return valuesFromRequest;
    }

    private void populateOptionItems() {
       Resource childItem = request.getResource().getChild(NN_ITEMS);
       if (childItem != null) {
         childItem.getChildren().forEach(this::addOption);
       }
    }

    private void addOption(Resource resource) {
      ValueMap properties = resource.getValueMap();
      SortOptionItem sortOptionItem = new SortOptionItem(properties.get(PN_TEXT, String.class),
          properties.get("value", String.class), properties.get(PN_ORDER_BY_CASE, true));
      items.add(sortOptionItem);
    }
}