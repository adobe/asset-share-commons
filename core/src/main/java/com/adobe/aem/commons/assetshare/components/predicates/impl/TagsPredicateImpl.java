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
import com.adobe.aem.commons.assetshare.components.predicates.TagsPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.TagOptionItem;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.adobe.cq.commerce.common.ValueMapDecorator;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.adobe.cq.wcm.core.components.models.form.Options.Type;
import com.day.cq.search.eval.JcrPropertyPredicateEvaluator;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.util.*;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {TagsPredicate.class},
        resourceType = {TagsPredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TagsPredicateImpl extends AbstractPredicate implements TagsPredicate {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/tags";

    private static final String SORT_ALPHABETICAL = "alphabetical";
    private static final String SORT_NATURAL = "natural";

    @Self
    @Required
    SlingHttpServletRequest request;

    @Self
    @Required
    private Options coreOptions;

    @ValueMapValue(name = PropertyPredicateImpl.PN_TYPE)
    private String typeString;

    @ValueMapValue
    private String property;

    @ValueMapValue
    @Named("and")
    @Default(booleanValues = false)
    private boolean and;

    @ValueMapValue
    @Default(values = SORT_NATURAL)
    private String displayOrder;

    @ValueMapValue
    @Default(values = JcrPropertyPredicateEvaluator.OP_EQUALS)
    private String operation;

    private String valueFromRequest;
    private ValueMap valuesFromRequest;

    @PostConstruct
    protected void init() {
        initPredicate(request, coreOptions);
    }

    public Type getType() {
        return coreOptions.getType();
    }

    /* Property Predicate Specific */

    public String getSubType() {
        //support variation of Checkboxes
        return typeString;
    }

    @Override
    public String getProperty() {
        return StringUtils.defaultIfBlank(property, "jcr:content/metadata/cq:tags");
    }

    @Override
    public String getName() {
        return PropertyValuesPredicateEvaluator.PREDICATE_NAME;
    }

    @Override
    public String getValuesKey() {
        return PropertyValuesPredicateEvaluator.VALUES;
    }

    public boolean hasOperation() {
        return StringUtils.isNotBlank(operation);
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

    public List<OptionItem> getItems() {
        final List<OptionItem> items = new ArrayList<OptionItem>();
        final TagManager tagManager = request.getResourceResolver().adaptTo(TagManager.class);
        // This finds the tags applies to the component resource
        final Tag[] tags = tagManager.getTags(request.getResource());

        if (tags != null) {
            final ValueMap initialValues = getInitialValues();
            final Locale locale = request.adaptTo(Config.class).getLocale();

            for (final Tag tag : tags) {
                items.add(new TagOptionItem(tag, locale, PredicateUtil.isOptionInInitialValues(tag.getTagID(), initialValues)));
            }
        }

        if (SORT_ALPHABETICAL.equals(displayOrder)) {
            Collections.sort(items, new AlphabeticalOptionItems());
        }

        return items;
    }

    @Override
    public boolean isReady() {
        final TagManager tagManager = request.getResourceResolver().adaptTo(TagManager.class);
        return tagManager.getTags(request.getResource()).length > 0;
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
}