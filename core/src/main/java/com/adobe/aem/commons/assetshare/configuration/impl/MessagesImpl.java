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

package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.configuration.Messages;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Model(
        adaptables = SlingHttpServletRequest.class,
        adapters = Messages.class
)
public class MessagesImpl implements Messages {
    private static final String MESSAGES_REL_PATH = ConfigImpl.NODE_NAME + "/messages";
    private static final String PN_MESSAGE_STYLE = "style";
    private static final String PN_MESSAGE_LABEL = "label";
    private static final String PN_MESSAGE_TEXT = "text";
    private static final String PN_MESSAGE_EVENT_ID = "eventId";

    @Self
    private SlingHttpServletRequest request;

    @ScriptVariable
    private Page currentPage;

    private Collection<ValueMap> messages;

    @Override
    public Collection<ValueMap> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();

            final List<String> tracker = new ArrayList<>();

            Page page = currentPage;

            while (page != null && StringUtils.startsWith(page.getPath(), "/content/")) {
                final Resource messageResources = page.getContentResource().getChild(MESSAGES_REL_PATH);

                if (messageResources != null) {
                    final Iterator<Resource> children = messageResources.listChildren();

                    while (children.hasNext()) {
                        addToTracker(tracker, children.next());
                    }
                }

                page = page.getParent();
            }
        }

        return messages;
    }

    private void addToTracker(final List<String> tracker, final Resource child) {
        // Tracked path should be relative to the [cq:Page]/jcr:content
        final String trackedPath = StringUtils.substringAfter(child.getPath(), JcrConstants.JCR_CONTENT);

        if (!tracker.contains(trackedPath) && !isMessageEmpty(child.getValueMap())) {
            messages.add(child.getValueMap());
            tracker.add(trackedPath);
        }
    }

    private boolean isMessageEmpty(ValueMap properties) {
        return StringUtils.isBlank(properties.get(PN_MESSAGE_STYLE, String.class)) &&
                StringUtils.isBlank(properties.get(PN_MESSAGE_LABEL, String.class)) &&
                StringUtils.isBlank(properties.get(PN_MESSAGE_TEXT, String.class)) &&
                StringUtils.isBlank(properties.get(PN_MESSAGE_EVENT_ID, String.class));
    }

    @Override
    public boolean isReady() {
        return !getMessages().isEmpty();
    }
}