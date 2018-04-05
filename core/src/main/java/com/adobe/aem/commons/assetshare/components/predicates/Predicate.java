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
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface Predicate extends Component, Field {

    /**
     * In version 1.x.x of this project, this will always be "asset-share-commons__form-id__1".
     *
     * @return the Form id, use to bind inputs to a form via &lt;input form="${predicate.formId}"... &gt;.
     */
    String getFormId();

    /**
     * This value is initiated in internal initialized state. Multiple calls on the same Sling Model will return the same value.
     *
     * @return the auto-incrementing QueryBuilder group id.
     */
    String getGroup();

    /**
     * @return true is the predicate view should be expanded.
     */
    boolean isExpanded();

    /**
     * The support and implementation of autoSearch is component-implementation dependent.
     *
     * @return true if auto searching should be enabled for this predicate.
     */
    default boolean isAutoSearch() {
        return false;
    }

    /**
     * Intended to drive the the data-asset-share-update-method attribute value in a component.
     * It is up to each component to respect this value.
     *
     * @return the update method for this component.
     */
    default String getComponentUpdateMethod() {
        return "never";
    }

    /**
     * GUIDANCE NOTICE
     * It is (almost) always preferred to use getInitialValues() even if only one initial value is present as it allows for easier and better future-proofing and extension.
     *
     * Returns the initial value for this predicate. Depending on the predicate this may come from:
     * - The query parameters
     * - The authored component resource
     * <p>
     * An example use of this is the fulltext predicate, which has only 1 predicate param and only 1 value.
     *
     * @return the initial value present, else null. If there are (or can be) many initial values, then this should return null and getInitialValues() should be used.
     */
    String getInitialValue();

    /**
     * Returns the initial value for this predicate. Depending on the predicate this may come from:
     * - The query parameters
     * - The authored component resource
     * <p>
     * This method is intended for Predicates that can have multiple predicate params and/or multiple values per param.
     *
     * @return he initial values for this predicate.
     */
    ValueMap getInitialValues();
}
