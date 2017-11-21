package com.adobe.aem.commons.assetshare.search.providers;

import com.day.cq.search.facets.Facet;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.Map;

@ConsumerType
public interface FacetPostProcessor {
    Map<String, Facet> process(SlingHttpServletRequest request, SearchResult searchResult, Map<String, Facet> facets);
}
