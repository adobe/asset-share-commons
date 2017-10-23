/*
 * Asset Share Commons
 *
 * Copyright [2017]  Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*global jQuery: false, AssetShare: false, window: false */

jQuery((function($, ns, navigation) {
    "use strict";

    /** Update any return urls with the LocalStorage value if present; otherwise use the url in the href */

    (function() {
        $('.cmp-details-title a[data-asset-share-id="return-link"]').each(function(index, element) {
            var hash =  ns.Data.attr(element, "asset"),
                returnUrl = navigation.returnUrl() || '',
                hashIndex = returnUrl.indexOf("#");

            if (returnUrl && returnUrl.length > 0) {
                if (hashIndex !== -1) {
                    // Remove existing hashes
                    returnUrl = returnUrl.substring(0, hashIndex);
                }

                if (hash) {
                    // Add the current asset as the hash
                    returnUrl += "#" + hash;
                }

                // Update the link
                $(element).attr("href", returnUrl);
            }
        });
    }());

}(jQuery,
    AssetShare,
    AssetShare.Navigation)));