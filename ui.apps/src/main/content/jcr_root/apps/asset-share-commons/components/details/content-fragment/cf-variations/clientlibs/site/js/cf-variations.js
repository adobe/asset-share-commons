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

jQuery((function ($, window, ns) {
    "use strict";
    (function () {

        var variationButton =$(".cmp-details-cf-variations .cf-variation-label"),
            originalVariationLabel = $(".cmp-details-cf-variations .original").data("variant"),
            contentFragmentContainer = $(".cmp-details-cf-renderer div");

        function setAllVariationButtonStyle(variant) {
            variationButton.each(function () {
                if (variant === $(this).data("variant")) {
                    $(this).css("background-color", "#2fcc9c");
                    $(this).css("color", "white");
                }
                else {
                    $(this).css("background-color", "");
                    $(this).css("color", "");
                }
            });
        }

        function showVariant() {
            var url = ns.getCFSelectorBasedPath(),
            rendererObj = {
                contentFragmentContainer: contentFragmentContainer
            };
            if (url.length > 0) {
                ns.setCFVariation(url,rendererObj);
            }
        }

        function setAddressBar(variant) {
            var paramsString = window.location.search.replace("?", ''),
                searchParams = new window.URLSearchParams(paramsString),
                newUrl = "";
            if (variant === originalVariationLabel) {
                /*jslint es5: true */
                searchParams.delete("variation");
                /*jslint es5: false */
                newUrl = (searchParams.entries()) ? window.location.pathname : window.location.pathname + "?" + searchParams.toString();
                window.history.pushState({}, 'Original', newUrl);
            } else {
                if (paramsString !== "") {
                    if (searchParams.has("variation")) {
                        searchParams.set("variation", variant);
                        newUrl = window.location.pathname + "?" + searchParams.toString();
                    } else {
                        newUrl = window.location.pathname + window.location.search + "&variation=" + variant;
                    }

                } else {
                    newUrl = window.location.pathname + '?variation=' + variant;
                }
                window.history.pushState({}, 'Variation', newUrl);
            }
        }

        if (contentFragmentContainer.length){
            variationButton.on("click", function (target) {
                var buttonVariant = target.currentTarget.dataset.variant;
                setAllVariationButtonStyle(buttonVariant);
                setAddressBar(buttonVariant);
                showVariant();
            });
        }

    }());
}(jQuery, window, AssetShare.ContentFragment)));

