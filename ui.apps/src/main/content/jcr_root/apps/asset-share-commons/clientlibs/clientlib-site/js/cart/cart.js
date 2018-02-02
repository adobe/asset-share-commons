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

/*global ContextHub: false, jQuery: false, AssetShare: false */

AssetShare.Cart = (function ($, ns, contextHubStore) {
    "use strict";

    function enabled() {
        return contextHubStore !== null && typeof contextHubStore !== "undefined";
    }

    function getPaths() {
        var assetsInCart = [],
            paths = [];

        if (enabled()) {
            assetsInCart = contextHubStore.get();
        }

        if (!(assetsInCart instanceof Array)) {
            assetsInCart = [assetsInCart];
        }

        assetsInCart.forEach(function (cartAssetPath) {
            paths.push(cartAssetPath);
        });

        return paths;
    }

    function getSize() {
        return getPaths().length;
    }

    function contains(assetPath) {
        var found = false;

        getPaths().forEach(function (cartAssetPath) {
            if (assetPath === cartAssetPath) {
                found = true;
            }
        });

        return found;
    }

    function add(assetPath, licensed) {
        if (enabled()) {
            if(!contains(assetPath)) {
                contextHubStore.add(assetPath);

                $("body").trigger(ns.Events.CART_ADD, [getSize(), assetPath]);
                $("body").trigger(ns.Events.CART_UPDATE, [getSize(), getPaths()]);
                return true;
            } else {
                $("body").trigger(ns.Events.CART_ALREADY_EXISTS, [getSize(), getPaths()]);
            }
        }

        return false;
    }

    function remove(assetPath) {
        if (enabled() && contains(assetPath)) {
            contextHubStore.remove(assetPath);

            $("body").trigger(ns.Events.CART_REMOVE, [getSize(), assetPath]);
            $("body").trigger(ns.Events.CART_UPDATE, [getSize(), getPaths()]);
            return true;
        }

        return false;
    }

    function clear() {
        if (enabled() && contextHubStore.get() && contextHubStore.get().length > 0) {
            contextHubStore.clear();

            $("body").trigger(ns.Events.CART_UPDATE, [getSize(), getPaths()]);
            $("body").trigger(ns.Events.CART_CLEAR, [getSize(), getPaths()]);
        }
    }

    function getContextHubStore() {
        return contextHubStore;
    }

    return {
        store: getContextHubStore,
        add: add,
        clear: clear,
        remove: remove,
        contains: contains,
        paths: getPaths,
        size: getSize
    };

}(jQuery,
    AssetShare,
    ContextHub.getStore("cart")));