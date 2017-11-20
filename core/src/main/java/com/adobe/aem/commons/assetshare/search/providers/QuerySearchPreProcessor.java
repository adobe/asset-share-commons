package com.adobe.aem.commons.assetshare.search.providers;

import com.day.cq.search.PredicateGroup;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.Map;

/**
 * This interface provides a hook that allows consumer code to construct the PredicateGroup that QueryBuilder will execute.
 *
 * To use this interface, create an OSGi Service consumer implementation of this interface.
 */
@ConsumerType
public interface QuerySearchPreProcessor {
    /**
     * This method provides a hook into processing query parameters immediately prior to querying. This method must return a PredicateGroup that will in be used to create the QueryBuilder query.
     *
     * The most basic form of this method would be:
     *    `return PredicateGroup.create(queryBuilderParams)`
     *
     * @param request the {@link SlingHttpServletRequest} object that initiates this search.
     * @param queryBuilderParams the prepared queryBuilder parameter map to execute.
     * @return the predicateGroup to execute via this search.
     */
    PredicateGroup process(SlingHttpServletRequest request, Map<String, String> queryBuilderParams);
}
