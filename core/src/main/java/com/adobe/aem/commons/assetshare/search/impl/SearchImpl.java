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

package com.adobe.aem.commons.assetshare.search.impl;

import com.adobe.aem.commons.assetshare.components.search.SearchConfig;
import com.adobe.aem.commons.assetshare.search.Constants;
import com.adobe.aem.commons.assetshare.search.Search;
import com.adobe.aem.commons.assetshare.search.UnsafeSearchException;
import com.adobe.aem.commons.assetshare.search.providers.SearchProvider;
import com.adobe.aem.commons.assetshare.search.results.Results;
import com.adobe.aem.commons.assetshare.search.results.impl.results.EmptyResultsImpl;
import com.adobe.aem.commons.assetshare.util.PredicateUtil;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import java.util.List;

@Model(
        adaptables = {SlingHttpServletRequest.class},
        adapters = {Search.class}
)
public class SearchImpl implements Search {
    private static final Logger log = LoggerFactory.getLogger(SearchImpl.class);

    private static final String PN_MODE = "mode";
    private static final String PN_LAYOUT = "layout";

    private String DEFAULT_MODE = "search";
    private String DEFAULT_LAYOUT = "card";

    @OSGiService
    List<SearchProvider> searchProviders;

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private SearchConfig searchConfig;

    @ScriptVariable
    private Page currentPage;

    private Results results = null;

    // Results component properties
    private ValueMap properties;

    private Resource resource;

    @PostConstruct
    protected void init() {
        resource = searchConfig.getSearchResource();
        properties = request.getResource().getValueMap();
    }

    public String getFormId() {
        return Constants.FORM_ID;
    }

    public Results getResults() {
        if (results == null) {

            for (final SearchProvider searchProvider : searchProviders) {
                if (searchProvider.accepts(request)) {
                    try {
                        results = searchProvider.getResults(request);
                    } catch (UnsafeSearchException e) {
                        log.warn("An unsafe search was initiated. Aborting with prejudice. Returning zero results.", e);
                        results = Results.ERRING_RESULTS;
                    } catch (RepositoryException e) {
                        log.error("An issue occurred while executing the query. Returning zero results.", e);
                        results = Results.ERRING_RESULTS;
                    }
                    break;
                }
            }

            if (results == null) {
                results = new EmptyResultsImpl();
            }
        }

        return results;
    }

    public String getMode() {
        return currentPage.getProperties().get(PN_MODE, DEFAULT_MODE);
    }

    public String getLayout() {
        final String value = PredicateUtil.getParamFromQueryParams(request, "layout");

        return StringUtils.defaultIfBlank(value, properties.get(PN_LAYOUT, DEFAULT_LAYOUT));
    }
}
