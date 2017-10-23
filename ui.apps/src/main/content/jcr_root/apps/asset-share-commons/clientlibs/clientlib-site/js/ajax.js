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

/*global jQuery: false, AssetShare: false*/

AssetShare.Ajax = (function ($, ns, messages) {
    "use strict";

    var SUCCESS = "success",
        ERROR = "error";

    function ajax(method, url, data, messageId) {
        var params = {
            method: method || 'GET',
            url: url,
            data: data
        };

        return $.when($.ajax(params).done(function (data, status) {
            messages.show(messageId);
            return data;
        }).fail(function (data, status) {
            messages.show(messageId);
            return data;
        }));
    }

    function get(url, data, messageId) {
        return ajax("GET", url, data, messageId);
    }

    function post(url, data, messageId) {
        return ajax("POST", url, data, messageId);
    }

    return {
        post: post,
        get: get
    };
}(jQuery,
    AssetShare,
    AssetShare.Messages));
