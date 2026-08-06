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

import com.adobe.aem.commons.assetshare.components.structure.Header;
import com.day.cq.wcm.api.Page;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class NavigationItemImplTest {

    @Test
    public void gettersReturnConstructorValues() {
        final Page page = mock(Page.class);
        final Header.NavigationItem navigationItem =
                new NavigationItemImpl(page, true, false, "home-icon", "/content/site/home", "Home");

        assertEquals(page, navigationItem.getPage());
        assertTrue(navigationItem.isActive());
        assertFalse(navigationItem.isHierarchyActive());
        assertEquals("home-icon", navigationItem.getIcon());
        assertEquals("/content/site/home", navigationItem.getUrl());
        assertEquals("Home", navigationItem.getText());
    }

    @Test
    public void gettersReturnConstructorValues_InverseBooleans() {
        final Header.NavigationItem navigationItem =
                new NavigationItemImpl(null, false, true, null, "https://example.com", null);

        assertNull(navigationItem.getPage());
        assertFalse(navigationItem.isActive());
        assertTrue(navigationItem.isHierarchyActive());
        assertNull(navigationItem.getIcon());
        assertEquals("https://example.com", navigationItem.getUrl());
        assertNull(navigationItem.getText());
    }
}
