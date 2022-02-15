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

package com.adobe.aem.commons.assetshare.components.structure;

import com.adobe.aem.commons.assetshare.components.Component;
import com.day.cq.wcm.api.Page;
import org.osgi.annotation.versioning.ConsumerType;

import java.util.Collection;

@ConsumerType
public interface Header extends Component {

    /**
     * property in which the site's root is located (usually hyperlinked off the site logo/title)
     */
    String PN_ROOT_PATH = "rootPath";

    /**
     * property in which path to Asset is stored to populate logo
     */
    String PN_LOGO_PATH = "logoPath";

    /**
     * property in which a site title is stored.
     */
    String PN_SITE_TITLE = "title";

    /**
     * Name of the node relative to the header component that stores the pages and icon items.
     */
    String PAGES_NODE = "pages";

    /**
     * Name of path property
     */
    String PN_PATH = "path";

    /**
     * Name of the text property. Will be left blank to use Page's title or navigation title
     */
    String PN_TEXT = "text";

    /**
     * Name of policy property that determines the relative location of the header component
     */
    String PN_POLICY_REL_PATH = "relPath";

    /**
     * Creates collection of NavigationItem based on edit dialog.
     *
     * @return {@link Collection} of navigation items
     */
    Collection<NavigationItem> getItems();

    /**
     * Returns the navigation root (from site hierarchy of current page) based on Start Level
     *
     * @return {@link Page} Navigation Root
     */
    String getNavigationRoot();

    /**
     * @return the path to a logo to be used to populate the image in the header
     */
    String getLogoPath();

    /**
     * @return String to display as the Site Title in the header
     */
    String getSiteTitle();

    public interface NavigationItem {

        /**
         * @return The {@link Page} contained in this navigation item.
         */
        Page getPage();

        /**
         * Gets the active information of the current page.
         *
         * @return true if if the navigation item represents the current page (ie the current request's current resolved page).
         */
        boolean isActive();

        /**
         * Gets the hierarchy information  of the current page.
         *
         * @return true if the current page is a descendant of the navigation page.
         */
        boolean isHierarchyActive();

        /**
         * Gets the icon associated with the navigation page.
         *
         * @return the icon associated with the navigation page.
         */
        String getIcon();

        /**
         * @return the URL associated with the navigation. Could be a relative path or an absolute URL.
         */
        String getUrl();

        /**
         * @return the String value to populate the navigation link text.
         */
        String getText();

    }
}
