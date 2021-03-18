/*
 * Asset Share Commons
 *
 * Copyright [2020]  Adobe
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

/**
 * Utility responsible for persisting and updating assets in the shopping cart.
 * Dependent on the profile store since the cart is stored beneath the user profile.
 */
AssetShare.Store.Cart = (function (ns, profileStore) {
    'use strict';

    var CART_KEY            = 'cart',
        CART_ASSETS_KEY     = 'assets';

    function enabled() {
        return profileStore !== null && profileStore.isReady();
    }

    /* Gets the contents of the cart Array, if not set an empty array is returned */
    function getCartAssets() {
        var cart = profileStore.getUserStoreObject(CART_KEY),
            paths = [];
        
        if(cart) {
            paths = cart.assets;
        }
        return paths;
    }

    /* adds an asset path to the cart and persists to local storage */
    function addCartAsset(assetPath) {
        var cart = profileStore.getUserStoreObject(CART_KEY) || {},
            cartAssets = cart[CART_ASSETS_KEY] || [];
        
        if(cartAssets.indexOf(assetPath) < 0) {
            cartAssets.push(assetPath);

            // Update cart object with new asset
            cart[CART_ASSETS_KEY] = cartAssets;

            // Update local storage with new cart object
            profileStore.setUserStoreObject(CART_KEY, cart);
            return true;
        }

        return false;
    }

    /* removes an asset path from the cart and persists to local storage */
    function removeCartAsset(assetPath) {
        var cart = profileStore.getUserStoreObject(CART_KEY) || {},
            cartAssets = cart[CART_ASSETS_KEY] || [],
            index = cartAssets.indexOf(assetPath);
        
        if( index >= 0) {
            cartAssets.splice(index, 1);

            // Update cart object to remove extra asset
            cart[CART_ASSETS_KEY] = cartAssets;

            // Update local storage with new cart object
            profileStore.setUserStoreObject(CART_KEY, cart);
            return true;
        }

        return false;
    
    }

    /* clears the cart and sets it to an empty array */
    function clearCartAssets() {
        var cart = profileStore.getUserStoreObject(CART_KEY);

        if(cart  && cart[CART_ASSETS_KEY]) {
            //set cart assets to empty array
            cart[CART_ASSETS_KEY] = [];
            profileStore.setUserStoreObject(CART_KEY, cart);
            return true;
        }
        return false;
    }

    return {
        enabled: enabled,
        getCartAssets: getCartAssets,
        addCartAsset: addCartAsset,
        removeCartAsset: removeCartAsset,
        clearCartAssets: clearCartAssets
    };

}(AssetShare,
  AssetShare.Store.Profile));