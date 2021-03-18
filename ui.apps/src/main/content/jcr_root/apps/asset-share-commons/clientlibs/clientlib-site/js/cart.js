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

AssetShare.Cart = (function ($, ns, cartStore) {
    "use strict";

    function isReady() {
        if(cartStore.enabled()) {
            return true;
        }
        return cartStore && cartStore.enabled();
    }

    function getPaths() {

        if (isReady()) {
            return cartStore.getCartAssets();
        }

        return [];
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
        if (isReady()) {
            if(!contains(assetPath)) {
                cartStore.addCartAsset(assetPath);

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
        if (isReady() && contains(assetPath)) {
            cartStore.removeCartAsset(assetPath);

            $("body").trigger(ns.Events.CART_REMOVE, [getSize(), assetPath]);
            $("body").trigger(ns.Events.CART_UPDATE, [getSize(), getPaths()]);
            return true;
        }

        return false;
    }

    function clear() {
        if (isReady()) {
            cartStore.clearCartAssets();
            $("body").trigger(ns.Events.CART_UPDATE, [getSize(), getPaths()]);
            $("body").trigger(ns.Events.CART_CLEAR, [getSize(), getPaths()]);
        }
    }

    return {
        add: add,
        clear: clear,
        remove: remove,
        contains: contains,
        paths: getPaths,
        size: getSize,
        isReady: isReady
    };

}(jQuery,
    AssetShare,
    AssetShare.Store.Cart));