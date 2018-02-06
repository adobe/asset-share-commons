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

public interface UserMenu extends Component {

    /**
     * Deprecated: User Name should be obtained client-side via the AEM ContextHub to support caching.
     *
     * @return String to display for the currently logged in user. If anonymous will return null
     */
    @Deprecated
    String getUserName();

    /**
     * Deprecated: User Profile Image should be obtained client-side via the AEM ContextHub to support caching.
     *
     * @return String to display a user profile picture. Defaults to anonymous photo if not found.
     */
    @Deprecated
    String getUserProfileImg();

    /**
     * Deprecated: Authenticated State should be obtained client-side via the AEM ContextHub to support caching.
     *
     * @return true if user is logged in (not anonymous)
     */
    @Deprecated
    Boolean isLoggedIn();

}
