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

/*global ContextHub: false, jQuery: false */

/**
 * This store acts as a Cart for Asset Share Commons and stores asset paths.
 */
(function ($, contextHub) {
    "use strict";

    var CONTEXT_HUB_STORE_NAME = "contexthub.assetsharecommonscart",
        KEY_ASSETS = "assets",

        defaultConfig = {
            assets: []
        };

    function AssetShareCommonsCart(name, config) {
        var self = this;

        self.config = $.extend({}, defaultConfig, config);
        self.init(name, self.config);
    }

    contextHub.Utils.inheritance.inherit(AssetShareCommonsCart, contextHub.Store.PersistedStore);

    $.extend(AssetShareCommonsCart.prototype, {
        /** Get all assets **/
        get: function () {
            return this.getItem(KEY_ASSETS) || [];
        },

        /** Add a specific set of Assets **/
        add: function (paths) {
            var current,
                dirty = false;

            if (!Array.isArray(paths)) {
                paths = [paths];
            }

            current = $.extend(true, [], this.get());

            paths.forEach(function (path) {
                if (current.indexOf(path) === -1) {
                    dirty = true;
                    current.push(path);
                }
            });

            if (dirty) {
                this.setItem(KEY_ASSETS, current);
                return true;
            } else {
                return false;
            }
        },

        /** Remove a specific set of Assets **/
        remove: function (paths) {
            var current = $.extend(true, [], this.get()),
                dirty = false;

            if (!Array.isArray(paths)) {
                paths = [paths];
            }

            paths.forEach(function (path) {
                var index = current.indexOf(path);
                if (index !== -1) {
                    dirty = true;
                    current.splice(index, 1);
                }
            });

            if (dirty) {
                this.setItem(KEY_ASSETS, current);
                return true;
            } else {
                return false;
            }
        },

        /** Remove all Assets **/
        clear: function () {
            this.setItem(KEY_ASSETS, []);
        }
    });

    /** Register the ContextHub Store **/
    contextHub.Utils.storeCandidates.registerStoreCandidate(
        AssetShareCommonsCart,
        CONTEXT_HUB_STORE_NAME,
        0
    );

}(jQuery,
    ContextHub));

