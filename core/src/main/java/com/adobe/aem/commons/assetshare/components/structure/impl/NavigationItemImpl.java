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

package com.adobe.aem.commons.assetshare.components.structure.impl;

import com.adobe.aem.commons.assetshare.components.structure.Header;
import com.day.cq.wcm.api.Page;

public class NavigationItemImpl implements Header.NavigationItem {

    private Page page;
    private boolean active;
    private boolean hierarchyActive;
    private String icon;
    private String url;
    private String text;

    public NavigationItemImpl(Page page, boolean active, boolean hierarchyActive, String icon, String url, String text) {
        this.page = page;
        this.active = active;
        this.hierarchyActive = hierarchyActive;
        this.icon = icon;
        this.url = url;
        this.text = text;
    }

    @Override
    public Page getPage() {
        return page;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isHierarchyActive() {
        return hierarchyActive;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getText() {
        return text;
    }

}
