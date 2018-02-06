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
import com.day.cq.wcm.commons.WCMUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

public abstract class AbstractPredicate implements Predicate {
    private static final String REQUEST_ATTR_PREDICATE_GROUP_TRACKER = "asset-share-commons__predicate-group";
    private static final String REQUEST_ATTR_LEGACY_PREDICATE_GROUP_TRACKER = "asset-share-commons__legacy_predicate-group";

    private static final String REQUEST_ATTR_FORM_ID_TRACKER = "asset-share-commons__form-id";
    private static final String PN_GENERATE_PREDICATE_GROUP_ID = "generatePredicateGroupId";

    private static final Integer INITIAL_GROUP_ID = 0;
    private static final Integer INITIAL_LEGACY_GROUP_ID = 10000 - 1;

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

    public String getId() {
        if (request.getResource() != null) {
            return getName() + "_" + String.valueOf(request.getResource().getPath().hashCode());
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
        initGroup(request);
    }

    /**
     * Initializes the predicate group number from the tracking request attribute, and increments for the next Sling Model calling this method.
     *
     * @param request the current SlingHttpServletRequest object.
     */
    protected synchronized final void initGroup(final SlingHttpServletRequest request) {
        /* Track Predicate Groups across Request */

        if (!isGroupIdGeneratingComponent(request) || !isReady() || !generateGroupId(request)) {
            generateLegacyGroupId(request);
        }
    }

    /**
     * @param request the Sling Http Request object.
     * @return true if the component is marked as generating a predicate group Id.
     */
    private boolean isGroupIdGeneratingComponent(SlingHttpServletRequest request) {
        final com.day.cq.wcm.api.components.Component component = WCMUtils.getComponent(request.getResource());
        return component != null && component.getProperties().get(PN_GENERATE_PREDICATE_GROUP_ID, false);
    }

    /**
     * Set the groupId and set the request attribute.
     *
     * @param request the Sling Http Request object.
     * @return true if a group id was generated.
     */
    private boolean generateGroupId(SlingHttpServletRequest request) {
        Object groupTracker = request.getAttribute(REQUEST_ATTR_PREDICATE_GROUP_TRACKER);

        if (groupTracker == null) {
            groupTracker = INITIAL_GROUP_ID;
        }

        if (groupTracker instanceof Integer) {
            group = (Integer) groupTracker + 1;
            request.setAttribute(REQUEST_ATTR_PREDICATE_GROUP_TRACKER, group);
            return true;
        }

        return false;
    }

    /**
     * Set the legacy groupId and set the request attribute.
     *
     * @param request the Sling Http Request object.
     */
    private void generateLegacyGroupId(SlingHttpServletRequest request) {
        Object legacyGroupTracker = request.getAttribute(REQUEST_ATTR_LEGACY_PREDICATE_GROUP_TRACKER);

        if (legacyGroupTracker == null) {
            legacyGroupTracker = INITIAL_LEGACY_GROUP_ID;
        }

        if (legacyGroupTracker instanceof Integer) {
            group = (Integer) legacyGroupTracker + 1;
            request.setAttribute(REQUEST_ATTR_LEGACY_PREDICATE_GROUP_TRACKER, group);
        } else {
            group = -1;
        }
    }
}
