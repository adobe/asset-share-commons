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

package com.adobe.aem.commons.assetshare.components.structure.impl;

import com.adobe.aem.commons.assetshare.components.structure.UserMenu;
import com.adobe.aem.commons.assetshare.testing.TestStyle;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserMenuImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.addModelsForClasses(UserMenuImpl.class);

        ctx.currentResource(ctx.create().resource("/content/user-menu",
                "sling:resourceType", "asset-share-commons/components/structure/user-menu"));
    }

    private void bindStyle(final Map<String, Object> props) {
        TestStyle.bind(ctx, props);
    }

    @Test
    public void isReady_AllPropertiesPresent_true() {
        final Map<String, Object> props = new HashMap<>();
        props.put("logInLabel", "Log In");
        props.put("logInLink", "/content/login");
        props.put("logOutLabel", "Log Out");
        props.put("logOutLink", "/content/logout");
        bindStyle(props);

        final UserMenu userMenu = ctx.request().adaptTo(UserMenu.class);
        assertTrue(userMenu.isReady());
    }

    @Test
    public void isReady_MissingLogInLabel_false() {
        final Map<String, Object> props = new HashMap<>();
        props.put("logInLink", "/content/login");
        props.put("logOutLabel", "Log Out");
        props.put("logOutLink", "/content/logout");
        bindStyle(props);

        final UserMenu userMenu = ctx.request().adaptTo(UserMenu.class);
        assertFalse(userMenu.isReady());
    }

    @Test
    public void isReady_MissingLogInLink_false() {
        final Map<String, Object> props = new HashMap<>();
        props.put("logInLabel", "Log In");
        props.put("logOutLabel", "Log Out");
        props.put("logOutLink", "/content/logout");
        bindStyle(props);

        final UserMenu userMenu = ctx.request().adaptTo(UserMenu.class);
        assertFalse(userMenu.isReady());
    }

    @Test
    public void isReady_MissingLogOutLabel_false() {
        final Map<String, Object> props = new HashMap<>();
        props.put("logInLabel", "Log In");
        props.put("logInLink", "/content/login");
        props.put("logOutLink", "/content/logout");
        bindStyle(props);

        final UserMenu userMenu = ctx.request().adaptTo(UserMenu.class);
        assertFalse(userMenu.isReady());
    }

    @Test
    public void isReady_MissingLogOutLink_false() {
        final Map<String, Object> props = new HashMap<>();
        props.put("logInLabel", "Log In");
        props.put("logInLink", "/content/login");
        props.put("logOutLabel", "Log Out");
        bindStyle(props);

        final UserMenu userMenu = ctx.request().adaptTo(UserMenu.class);
        assertFalse(userMenu.isReady());
    }

    @Test
    public void isReady_NoneConfigured_false() {
        bindStyle(new HashMap<>());

        final UserMenu userMenu = ctx.request().adaptTo(UserMenu.class);
        assertFalse(userMenu.isReady());
    }

    @Test
    public void getExportedType() {
        bindStyle(new HashMap<>());

        final UserMenu userMenu = ctx.request().adaptTo(UserMenu.class);
        assertEquals("asset-share-commons/components/structure/user-menu", ((UserMenuImpl) userMenu).getExportedType());
    }
}
