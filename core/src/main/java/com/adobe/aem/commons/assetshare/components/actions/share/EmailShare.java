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

package com.adobe.aem.commons.assetshare.components.actions.share;

import org.apache.sling.api.resource.ValueMap;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface EmailShare extends Share {

    String PN_SIGNATURE = "signature";

    String PN_USE_SHARER_NAME_AS_SIGNATURE = "useSharerSignature";

    String PN_USE_SHARER_EMAIL_AS_REPLY_TO = "replyToSharer";

    /**
     * For example, in the default ASC Email Share implementation, this is the valuemap for the Email Share Modal Component.
     *
     * @return a value map of the share component's resource.
     */
    ValueMap getProperties();

    /**
     * This data is considered "safe" is it is configured at the Component level by the Author, and not passed in by the end-user.
     *
     * @return the data provided via the Share form's component configuration.
     */
    ValueMap getConfiguredData();

    /**
     * @return the data provided by the end user via the Share submissions form.
     */
    ValueMap getUserData();

    /**
     * @return the absolute path to the E0mail Template to use.
     */
    String getEmailTemplatePath();
}