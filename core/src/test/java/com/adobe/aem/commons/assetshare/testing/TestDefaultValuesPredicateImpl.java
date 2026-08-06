package com.adobe.aem.commons.assetshare.testing;

import com.adobe.aem.commons.assetshare.components.predicates.DefaultValuesPredicate;
import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal, dependency-free {@link DefaultValuesPredicate} Sling Model used ONLY by tests to exercise
 * {@code PagePredicateImpl#addDefaultValuesAsPredicateGroups(..)} (which walks the current page looking
 * for any component that implements {@code DefaultValuesPredicate}) without needing to wire up the
 * Core Components Options/Field machinery that the "real" predicate components
 * (ex. PropertyPredicateImpl, PathPredicateImpl) depend on.
 */
@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {DefaultValuesPredicate.class},
        resourceType = {TestDefaultValuesPredicateImpl.RESOURCE_TYPE}
)
public class TestDefaultValuesPredicateImpl implements DefaultValuesPredicate {
    public static final String RESOURCE_TYPE = "asset-share-commons/testing/default-values-predicate";

    @Override
    public PredicateGroup getPredicateGroup() {
        final Map<String, String> params = new HashMap<>();
        params.put("testDefaultValue", "true");
        return PredicateConverter.createPredicates(params);
    }
}
