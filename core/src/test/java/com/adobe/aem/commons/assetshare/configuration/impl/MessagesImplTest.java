/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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
import com.day.cq.wcm.api.PageManager;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MessagesImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/configuration/impl/MessagesImplTest.json",
                "/content/site");

        ctx.addModelsForClasses(MessagesImpl.class);
    }

    private Messages getMessages(String pagePath) {
        final PageManager pageManager = ctx.resourceResolver().adaptTo(PageManager.class);
        final Page page = pageManager.getPage(pagePath);

        final SlingBindings bindings = new SlingBindings();
        bindings.put("currentPage", page);
        ctx.request().setAttribute(SlingBindings.class.getName(), bindings);

        ctx.currentResource(page.getContentResource());

        return ctx.request().adaptTo(Messages.class);
    }

    @Test
    public void getMessages_leafMessageOverridesParentDueToTrackedPath_andEmptyIsSkipped() {
        final Messages messages = getMessages("/content/site/subpage/subsubpage");

        final Collection<ValueMap> actual = messages.getMessages();

        assertEquals(2, actual.size());

        final Iterator<ValueMap> iterator = actual.iterator();
        final ValueMap first = iterator.next();
        assertEquals("Leaf message", first.get("text", String.class));
        assertEquals("leaf1", first.get("eventId", String.class));

        final ValueMap second = iterator.next();
        assertEquals("Parent message2", second.get("text", String.class));
        assertEquals("parent2", second.get("eventId", String.class));
    }

    @Test
    public void getMessages_noMessagesConfigured_returnsEmpty() {
        final Messages messages = getMessages("/content/site/no-messages-page");

        assertTrue(messages.getMessages().isEmpty());
    }

    @Test
    public void getMessages_onlyEmptyMessage_returnsEmpty() {
        final Messages messages = getMessages("/content/site");

        assertTrue(messages.getMessages().isEmpty());
    }

    @Test
    public void isReady_true() {
        final Messages messages = getMessages("/content/site/subpage");

        assertTrue(messages.isReady());
    }

    @Test
    public void isReady_false() {
        final Messages messages = getMessages("/content/site/no-messages-page");

        assertFalse(messages.isReady());
    }

    @Test
    public void getExportedType() {
        final Messages messages = getMessages("/content/site/subpage");

        assertEquals("asset-share-commons/components/configuration/messages", messages.getExportedType());
    }
}
