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
import org.osgi.annotation.versioning.ConsumerType;

import java.util.List;

@ConsumerType
public interface SortPredicate extends Predicate {
    /**
     * @return the option items for this predicate.
     */
    List<OptionItem> getItems();

    /**
     * @return true if the active sort order is ascending (vs. descending).
     */
    default boolean isAscending() { return false; }

    /**
     * @return the active label for the Order By (Sort By) field.
     */
    default String getOrderByLabel() { return "Sort By"; }

    /**
     * @return the active label for the Order By Sort (Sort Direction) field.
     */
    default String getOrderBySortLabel() { return "DESC"; }

    interface SortOptionItem extends OptionItem {
        default boolean isCaseSensitive() {
            return false;
        }

        void setSelected(boolean selected);
    }
}

