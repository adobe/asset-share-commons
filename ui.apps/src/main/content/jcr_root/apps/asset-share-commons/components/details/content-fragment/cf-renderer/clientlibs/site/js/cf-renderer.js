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

jQuery((function($, ns) {
    "use strict";
    (function () {

    var fallbackHeader = $(".cmp-details-cf-renderer div h1"),
        contentFragmentContainer = $(".cmp-details-cf-renderer div"),
        placeholderImgContainer = $(".cf-placeholder"),
        fallbackMsg = contentFragmentContainer.data("fallback-msg"),
        loader = $(".loader"),
        url = "",
        rendererObj = {
            contentFragmentContainer: contentFragmentContainer,
            fallbackHeader: fallbackHeader,
            fallbackMsg: fallbackMsg,
            placeholderImgContainer: placeholderImgContainer,
            loader: loader
        };



    if (contentFragmentContainer.length) {
        url = ns.getCFSelectorBasedPath();
        if (url.length > 0) {
            ns.setCFVariation(url, rendererObj);
        } else {
            loader.hide();
            fallbackHeader.hide();
            placeholderImgContainer.show();
        }
    }


    }());
}(jQuery, AssetShare.ContentFragment)));

