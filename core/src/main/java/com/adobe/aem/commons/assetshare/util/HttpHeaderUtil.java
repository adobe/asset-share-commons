/*
 * Asset Share Commons
 *
 * Copyright (C) 2026 Adobe
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

package com.adobe.aem.commons.assetshare.util;

public class HttpHeaderUtil {

    /**
     * Strips CR and LF characters from a value that will be placed into an HTTP response header,
     * preventing header/response-splitting injection when the value is sourced from editable
     * content (ex. DAM asset metadata such as dc:title or dam:MimeType).
     *
     * @param value the candidate header value to sanitize.
     * @return the value with all CR and LF characters removed, or null if value is null.
     */
    public static String sanitize(final String value) {
        if (value == null) {
            return null;
        }

        return value.replaceAll("[\\r\\n]", "");
    }
}
