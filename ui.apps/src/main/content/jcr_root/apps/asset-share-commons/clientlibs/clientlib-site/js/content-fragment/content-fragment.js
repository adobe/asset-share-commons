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

AssetShare.ContentFragment = (function ($, window, ns) {
    "use strict";

    function setHtml(elem, htmlStr, loader) {
        if (loader) {
            loader.hide();
        }
        if (elem) {
            elem.html(htmlStr);
        }
    }

    function setCFVariation(url, rendererObj) {
        var fallbackHeader = rendererObj.fallbackHeader,
        fallbackMsg = rendererObj.fallbackMsg,
        loader = rendererObj.loader,
        placeholderImgContainer = rendererObj.placeholderImgContainer,
        contentFragmentContainer = rendererObj.contentFragmentContainer;

        $.get(url, function (result) {
            if (result.length === 0) {
                setHtml(fallbackHeader, fallbackMsg, loader);
            } else {
                setHtml(contentFragmentContainer, result, loader);
            }
        }).fail(function () {
            if (loader) {
                loader.hide();
            }
            if (fallbackHeader) {
                fallbackHeader.hide();
            }
            if (placeholderImgContainer) {
                placeholderImgContainer.show();
            }
        });
    }

    function getCFSelectorBasedPath() {
        var originalVarSelPath = "",
            cfVarSelPath = "",
            regex = /\.html/,
            pathList = window.location.pathname.split(regex),
            queryString = window.location.search.replace("?", ''),
            queryParams = queryString.split("&"),
            variationParam = queryParams.filter(function (param) { return param.includes("variation="); });

        if (pathList.length === 3) {
            originalVarSelPath = pathList[2] + ".cfm.content.html";
            if (pathList[2] === "") {
                originalVarSelPath = "";
            }
        } else if (pathList.length === 2) {
            originalVarSelPath = pathList[1] + ".cfm.content.html";
            if (pathList[1] === "") {
                originalVarSelPath = "";
            }
        }


        if (variationParam.length === 0) {
            cfVarSelPath = originalVarSelPath;
        } else if (variationParam.length === 1) {
            cfVarSelPath = originalVarSelPath + "?" + variationParam.toString();
        }
        return cfVarSelPath;
    }

    return {
        getCFSelectorBasedPath: getCFSelectorBasedPath,
        setCFVariation: setCFVariation
    };

}(jQuery, window, AssetShare));
