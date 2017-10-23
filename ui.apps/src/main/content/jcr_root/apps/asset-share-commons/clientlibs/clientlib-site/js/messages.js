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

/*global jQuery: false, AssetShare: false */

AssetShare.Messages = (function ($, ns) {
    "use strict";

    var DISPLAY_DURATION = 3000,
        FADE_IN_DURATION = 50,
        QUIET_PERIOD_DURATION = 250,
        activeTimeout,
        activeElement = null;


    function show(id) {
        var selector = "[data-asset-share-message-id=\"" + id + "\"]",
            element = $(selector);

        if (element.length === 1) {

            if (activeElement !== null) {
                clearTimeout(activeTimeout);
                activeElement.hide(0, function() {
                    element.delay(QUIET_PERIOD_DURATION).slideDown(FADE_IN_DURATION);
                });
            } else {
                element.slideDown(FADE_IN_DURATION);
            }

            activeTimeout = setTimeout(function () {
                element.hide();
            }, DISPLAY_DURATION);

            activeElement = element;
        }
    }

    return {
        show: show
    };
}(jQuery,
    AssetShare));
