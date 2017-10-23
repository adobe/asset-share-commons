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

jQuery((function ($, ns, search) {
    "use strict";
    var timeout;

    function triggerLoadMore(element) {
        var elementTop = $(element).offset().top,
            viewportTop = $(window).scrollTop(),
            viewportBottom = (viewportTop + $(window).height()),
            greedy = (elementTop * 0.25);
        return elementTop < (viewportBottom + greedy);
    }

    function greedyLoadMore() {
        clearTimeout(timeout);

        var element = ns.Elements.element("infinite-load-more");

        if (element && element.length === 1) {
            if (triggerLoadMore(element)) {
                search.loadMore();
                timeout = setTimeout(function () {
                    greedyLoadMore();
                }, 250);
            }
        }
    }

    if (ns.Elements.element("infinite-load-more")) {
        if (window === window.top) {
            /** Only infinite scroll when not in an iframe **/
            $(window).on('DOMContentLoaded load resize scroll', function () {
                greedyLoadMore();
            });

            $("body").on("asset-share-commons.search.end", function(event, searchType) {
                if ("search" === searchType) {
                    greedyLoadMore();
                }
            });
        }

        $("body").on("click", ns.Elements.selector("infinite-load-more"), function (e) {
            ns.Search.loadMore(e);
        });
    }

}(jQuery,
    AssetShare,
    AssetShare.Search)));