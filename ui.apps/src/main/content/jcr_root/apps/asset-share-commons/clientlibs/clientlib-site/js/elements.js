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

/*global jQuery: false, AssetShare: false, document: false*/

AssetShare.Elements = (function (document, $, ns) {
    "use strict";

    var APPEND = "append",
        REPLACE = "replace",
        ATTRIBUTE = "attribute";

    function getAssetShareIdSelector(ids) {
        if (!ids) {
            return null;
        } else if (ids.constructor === Array) {
            return ids.map(function (id) {
                return "[data-asset-share-id=\"" + id + "\"]";
            }).join(" ");
        } else {
            return "[data-asset-share-id=\"" + ids + "\"]";
        }
    }

    function getElementsByAssetShareId(id, doc) {
        return $(getAssetShareIdSelector(id), doc);
    }

    function removeElementsByAssetShareId(id, doc) {
        var element = $(getAssetShareIdSelector(id), doc);
        if(element) {
            $(element).remove();
        }
    }

    /*
    <div data-asset-share-id="foo"
         data-asset-share-update-method="append|replace|attribute"
         data-asset-share-update-when="load-more|search">
    */
    function getUpdateSelector(when) {
        var selector = "";

        if (when) {
            selector += "[data-asset-share-id][data-asset-share-update-method][data-asset-share-update-when*=\"" + when + "\"]";
            selector += ",";
        }

        // Selects the "all"
        selector += "[data-asset-share-id][data-asset-share-update-method][data-asset-share-update-when=\"\"]";
        selector += ",";
        selector += "[data-asset-share-id][data-asset-share-update-method]:not([data-asset-share-update-when])";

        return selector;
    }

    function update(rawHtml, when) {
        var html = $("<div>" + rawHtml + "</div>");

        html.find(getUpdateSelector(when)).each(function (index, srcEl) {
            var id, method;

            srcEl = $(srcEl);

            id = ns.Data.attr(srcEl, "id");
            method = ns.Data.attr(srcEl, "update-method");
            method = method ? method.toLowerCase() : '';

            if (id) {
                getElementsByAssetShareId(id, document).each(function (index, destEl) {
                    var attributeName;
                    destEl = $(destEl);

                    if (APPEND === method) {
                        destEl.append($.trim(srcEl.html()));
                    } else if (REPLACE === method) {
                        destEl.html($.trim(srcEl.html()));
                    } else if (ATTRIBUTE === method) {
                        attributeName = ns.Data.attr(srcEl, "update-attribute");
                        if (attributeName) {
                            destEl.attr(attributeName, $.time(srcEl.attr(attributeName)));
                        }
                    }
                });
            }
        });
    }

    return {
        selector: getAssetShareIdSelector,
        element: getElementsByAssetShareId,
        update: update,
        remove: removeElementsByAssetShareId
    };
}(document,
    jQuery,
    AssetShare));
