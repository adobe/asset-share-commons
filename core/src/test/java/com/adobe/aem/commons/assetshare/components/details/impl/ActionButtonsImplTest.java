/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.actions.share.ShareService;
import com.adobe.aem.commons.assetshare.components.details.ActionButtons;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.RequireAemImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ActionButtonsImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/ActionButtonsImplTest.json", "/content");

        ctx.addModelsForClasses(ActionButtonsImpl.class, Config.class);

        ctx.registerService(RequireAem.class, new RequireAemImpl());

        // None of our test pages have a root-page resourceType matching Config's rootResourceTypes, so
        // Config#getRootPath falls back to "/" - meaning the action urls resolve to
        // "/actions/<action>.<viewSelector>.html" (viewSelector defaults to "partial"). Those need to exist as
        // real resources for Config#pathResolves(...) (and thus is*Enabled()) to return true - note the AEM Mocks
        // resourceResolver.resolve(request, path) does NOT strip the extension/selectors the way a real Sling
        // request resolution would, so the resource must be created at the literal, fully-suffixed path.
        ctx.create().resource("/actions/cart.partial.html");
        ctx.create().resource("/actions/download.partial.html");
        ctx.create().resource("/actions/share.partial.html");
    }

    @Test
    public void isReady_CartEnabledWithLabels_true() {
        ctx.currentResource("/content/cart-page/jcr:content/action-buttons");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        assertTrue(actionButtons.isReady());
    }

    @Test
    public void isReady_CartEnabledButLabelsMissing_false() {
        ctx.currentResource("/content/cart-page/jcr:content/action-buttons-no-labels");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        assertFalse(actionButtons.isReady());
    }

    @Test
    public void isReady_DownloadEnabledWithLabel_true() {
        ctx.currentResource("/content/download-page/jcr:content/action-buttons");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        assertTrue(actionButtons.isReady());
    }

    @Test
    public void isReady_ShareEnabledWithLabelAndService_true() {
        ctx.registerService(ShareService.class, Mockito.mock(ShareService.class));

        ctx.currentResource("/content/share-page/jcr:content/action-buttons");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        assertTrue(actionButtons.isReady());
    }

    @Test
    public void isReady_ShareEnabledButNoShareService_false() {
        // No ShareService registered - Config#isShareEnabled() always returns false without one.
        ctx.currentResource("/content/share-page/jcr:content/action-buttons");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        assertFalse(actionButtons.isReady());
    }

    @Test
    public void isReady_AllDisabled_false() {
        ctx.currentResource("/content/all-disabled-page/jcr:content/action-buttons");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        assertFalse(actionButtons.isReady());
    }

    @Test
    public void isEmpty_MirrorsIsReady() {
        ctx.currentResource("/content/cart-page/jcr:content/action-buttons");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        assertFalse(actionButtons.isEmpty());
    }

    @Test
    public void isEmpty_true_WhenNotReady() {
        ctx.currentResource("/content/all-disabled-page/jcr:content/action-buttons");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        assertTrue(actionButtons.isEmpty());
    }

    @Test
    public void getExportedType() {
        ctx.currentResource("/content/cart-page/jcr:content/action-buttons");
        final ActionButtons actionButtons = ctx.request().adaptTo(ActionButtons.class);

        org.junit.Assert.assertEquals("asset-share-commons/components/details/action-buttons",
                ((ActionButtonsImpl) actionButtons).getExportedType());
    }
}
