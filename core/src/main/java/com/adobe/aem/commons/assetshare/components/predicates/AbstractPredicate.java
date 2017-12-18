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

import com.adobe.cq.wcm.core.components.models.form.Field;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

public abstract class AbstractPredicate implements Predicate {
    private static final String REQUEST_ATTR_FORM_ID_TRACKER = "asset-share-commons__form-id";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean expanded;

    private Field coreField;

    /**
     * Asset Share Predicate Methods
     **/

    public boolean isExpanded() {
        if (!expanded) {
            // Handling incoming request query params
            return StringUtils.isNotBlank(getInitialValue()) || !getInitialValues().isEmpty();
        }

        return expanded;
    }

    public String getGroup() {
        return getResourceId() + "_group";
    }

    public String getInitialValue() {
        return null;
    }

    public ValueMap getInitialValues() {
        return ValueMap.EMPTY;
    }

    public String getId() {
        if (request.getResource() != null) {
            return getName() + "_" + getResourceId();
        } else {
            return coreField.getId();
        }
    }

    /**
     * Core Field Component Delegates
     **/

    public String getTitle() {
        return coreField.getTitle();
    }

    public String getValue() {
        return coreField.getValue();
    }

    public String getHelpMessage() {
        return coreField.getHelpMessage();
    }

    public String getFormId() {
        if (request.getAttribute(REQUEST_ATTR_FORM_ID_TRACKER) == null) {
            request.setAttribute(REQUEST_ATTR_FORM_ID_TRACKER, 1);
        }

        return REQUEST_ATTR_FORM_ID_TRACKER + "__" + String.valueOf(request.getAttribute(REQUEST_ATTR_FORM_ID_TRACKER));
    }

    /**
     * Initializer Methods.
     **/

    /**
     * Initializes the abstract predicate; This is used to:
     * - Initialize the predicate group number for the Model.
     * - Initialize the Core Components Field Sling Model which the Asset Share Commons Predicates Components delegate to.
     *
     * @param request the current SlingHttpServletRequest object
     * @param coreField the Core Components Field component (if the request can be adapted to one by the concrete implementing class).
     */
    protected final void initPredicate(final SlingHttpServletRequest request, final Field coreField) {
        this.coreField = coreField;
    }

    private String getResourceId() {
        return String.valueOf(Math.abs(request.getResource().getPath().hashCode() - 1));
    }
}
