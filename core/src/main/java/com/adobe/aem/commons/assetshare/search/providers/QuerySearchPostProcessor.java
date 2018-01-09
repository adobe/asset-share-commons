package com.adobe.aem.commons.assetshare.search.providers;

import com.adobe.aem.commons.assetshare.search.providers.impl.QuerySearchProviderImpl;
import com.adobe.aem.commons.assetshare.search.results.Results;
import com.day.cq.search.Query;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * This interface provides a hook that allows consumer code manipulate the results of the {@link QuerySearchProviderImpl} prior to their return.
 * This is especially useful to populating the `getAdditionalData()` map.
 *
 * To use this interface, create an OSGi Service consumer implementation of this interface.
 */
@ConsumerType
public interface QuerySearchPostProcessor {
    /**
     *  This method provides a hook into processing query parameters immediately prior to querying.
     *  This method must return an object that implements the {@link Results} interface.
     *
     *  A common use of this method is to populate the results.getAdditionalData() map
     *
     *     results.getAdditionalData().put("customKey", createSomeCustomObject(..));
     *     return results;
     *
     *
     * @param request the {@link SlingHttpServletRequest} object that initiates this search.
     * @param query the QueryBuilder query executed to generate the results and searchResult.
     * @param results the results as created by {@link QuerySearchProviderImpl}
     * @param searchResult the "raw" QueryBuilder {@link SearchResult} object.
     * @return the adjusted results object (may be a brand new {@link Results} obj).
     */
    Results process(SlingHttpServletRequest request, Query query, Results results, SearchResult searchResult);
}
