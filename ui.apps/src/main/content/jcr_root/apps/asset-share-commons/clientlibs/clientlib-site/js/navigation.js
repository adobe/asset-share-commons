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

AssetShare.Navigation = (function ($, window, ns) {
    "use strict";

    var LOCAL_STORAGE_KEY = "asset-share-commons";

    function setAddressBar(url) {
        if (window.top.history && window.top.history.pushState) {
            window.top.history.pushState({}, window.top.document.title, url);
        }
    }

    function getOrSetReturnUrl(url) {
        var storage = JSON.parse(window.localStorage.getItem(LOCAL_STORAGE_KEY)) || {};

        if (typeof url === "undefined") {
            return storage.returnUrl;
        } else {
            storage.returnUrl = url;
            window.localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(storage));
        }
    }

    function gotoTop() {
        window.scrollTo(0, 0);
    }

    return {
        addressBar: setAddressBar,
        returnUrl: getOrSetReturnUrl,
        gotoTop: gotoTop
    };
}(jQuery,
    window,
    AssetShare));
