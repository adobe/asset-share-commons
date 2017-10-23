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
    private static final String REQUEST_ATTR_PREDICATE_GROUP_TRACKER = "asset-share-commons__predicate-group";
    private static final String REQUEST_ATTR_FORM_ID_TRACKER = "asset-share-commons__form-id";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean expanded;

    private int group = 1;

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
        return group + "_group";
    }

    public String getInitialValue() {
        return null;
    }

    public ValueMap getInitialValues() {
        return ValueMap.EMPTY;
    }

    /**
     * Core Field Component Delegates
     **/

    public String getId() {
        return coreField.getId();
    }

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
     * Initializers
     **/

    protected final void initPredicate(final SlingHttpServletRequest request, final Field coreField) {
        this.coreField = coreField;
        initGroup(request);
    }

    protected synchronized final void initGroup(final SlingHttpServletRequest request) {
        /* Track Predicate Groups across Request */

        final Object groupTracker = request.getAttribute(REQUEST_ATTR_PREDICATE_GROUP_TRACKER);
        if (groupTracker != null && (groupTracker instanceof Integer)) {
            group = (Integer) groupTracker + 1;
        }

        request.setAttribute(REQUEST_ATTR_PREDICATE_GROUP_TRACKER, group);
    }
}
