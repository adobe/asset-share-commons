package com.adobe.aem.commons.assetshare.search.searchpredicates;

import com.day.cq.search.PredicateGroup;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface SearchPredicate {

    /**
     * @return The label to display in Authoring UIs to allow the selection of this Search Predicate implementation.
     */
    String getLabel();

    /**
     * @return A unique ID across Search Predicates, for this Search Predicate.
     */
    String getName();

    /**
     * @param request the Sling Http Request object that represents this search request.
     * @return the PredicateGroup that represents this SearchPredicate.
     */
    PredicateGroup getPredicateGroup(SlingHttpServletRequest request);
}
