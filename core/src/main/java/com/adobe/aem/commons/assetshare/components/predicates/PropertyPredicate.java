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

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.List;

@ConsumerType
public interface PropertyPredicate extends Predicate {

    /**
     * @return true of an operation is set.
     */
    default boolean hasOperation() { return false; }

    /**
     * @return the querybuilder predication operation (equals, not equals, exists)
     */
    String getOperation();

    /**
     * @return true if the predicate's "and" operation is set.
     */
    boolean hasAnd();

    /**
     * This is typically preceded by hasAnd() since if the and operation is NOT set, then this will return false.
     *
     * @return the value of the and operation.
     */
    Boolean getAnd();

    /**
     * @return the option items for this predicate.
     */
    List<OptionItem> getItems();

    /**
     * @return the configured input type (checkbox, radio, drop-down, etc.)
     */
    Options.Type getType();

    /**
     * @return the configured sub-type (checkbox, toggle, slider, radio buttons)
     */
    String getSubType();

    /**
     * @return the relative property path used for this predicate.
     */
    String getProperty();

    /**
     * @return the predicate's key that indicates the predicates values.
     */
    String getValuesKey();

}

