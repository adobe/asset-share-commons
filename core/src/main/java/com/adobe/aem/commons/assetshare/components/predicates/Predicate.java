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

package com.adobe.aem.commons.assetshare.components.predicates;

import com.adobe.aem.commons.assetshare.components.Component;
import com.adobe.cq.wcm.core.components.models.form.Field;
import org.apache.sling.api.resource.ValueMap;

public interface Predicate extends Component, Field {

    /**
     * Gets the Form id, use to bind inputs to a form via <input form="${predicate.formId}" ..
     *
     * @return
     */
    String getFormId();

    /**
     * @return the auto-incrementing querybuilder group id.
     */
    String getGroup();

    /**
     * @return true is the predicate view should be expanded.
     */
    boolean isExpanded();

    /**
     * Returns the initial value for this predicate. Depending on the predicate this may come from:
     * - The query parameters
     * - The authored component resource
     * <p>
     * An example use of this is the fulltext predicate, which has only 1 predicate param and only 1 value.
     *
     * @return the intial value is present, else null. If there are (or can be) many initial values, then this should return null and getInitialValues() should be used.
     */
    String getInitialValue();

    /**
     * Returns the initial value for this predicate. Depending on the predicate this may come from:
     * - The query parameters
     * - The authored component resource
     * <p>
     * This method is intended for Predicates that can have multiple predicate params and/or multiple values per param.
     *
     * @return
     */
    ValueMap getInitialValues();
}
