package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.AbstractPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.FreeformTextPredicate;
import com.adobe.aem.commons.assetshare.search.impl.predicateevaluators.PropertyValuesPredicateEvaluator;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import javax.inject.Named;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {FreeformTextPredicate.class},
        resourceType = {FreeformTextPredicatePredicateImpl.RESOURCE_TYPE},
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class FreeformTextPredicatePredicateImpl extends AbstractPredicate implements FreeformTextPredicate {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/search/freeform-text";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @ValueMapValue
    @Named("jcr:title")
    private String title;

    @ValueMapValue
    private String property;

    @ValueMapValue
    private String placeholder;

    @ValueMapValue
    private String[] delimiters;

    @ValueMapValue
    @Default(intValues = 1)
    private int rows;

    @ValueMapValue(name = PropertyPredicateImpl.PN_TYPE)
    private String typeString;

    protected String valueFromRequest = null;
    protected ValueMap valuesFromRequest = null;

    @PostConstruct
    protected void init() {
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
    public String[] getDelimiters() {
        return this.delimiters;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public int getRows() {
        return this.rows;
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
