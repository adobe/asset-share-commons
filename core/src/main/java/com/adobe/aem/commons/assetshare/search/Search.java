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

package com.adobe.aem.commons.assetshare.search;

import com.adobe.aem.commons.assetshare.search.results.Results;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Search {

    /**
     * The ID of the form. This is used to associate results w a particular form.
     * As of Asset Share Commons 1.0.0, only 1 "search" is allowed on the page so this value is not well used.
     *
     * @return the form's id.
     */
    String getFormId();

    /**
     * This is used to expose state around what layout-type was requested for this search. This makes consumers (HTL) of Search results able to easily make decisions about how to render the search results.
     *
     * @return the layout for the search.
     */
    String getLayout();

    /**
     * This is used to expose state around what mode was requested for this search. The mode typically is used to engage a specific {@link com.adobe.aem.commons.assetshare.search.providers.SearchProvider}, however this is not mandatory.
     * In the initial release of Asset Share Commons, the only supported mode is "search".
     *
     * @return the search model.
     */
    String getMode();

    /**
     * @return the {@link Results} object, that represents the results of the search. {@link Results} contains a list of all results as well as heuristics and other metadata about the search.
     */
    Results getResults();
}
