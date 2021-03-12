AssetShare.StorageCart = (function ($, ns, storageProfile) {
    'use strict';

    var CART_KEY            = 'cart',
        CART_ASSETS_KEY     = 'assets';

    function enabled() {
        return storageProfile !== null && storageProfile.isReady();
    }

    /* Gets the contents of the cart Array, if not set an empty array is returned */
    function getCartAssets() {
        var cart = storageProfile.getUserStorageObject(CART_KEY),
            paths = [];
        
        if(cart) {
            paths = cart.assets;
        }
        return paths;
    }

    /* adds an asset path to the cart and persists to local storage */
    function addCartAsset(assetPath) {
        var cart = storageProfile.getUserStorageObject(CART_KEY) || {},
            cartAssets = cart[CART_ASSETS_KEY] || [];
        
        if(cartAssets.indexOf(assetPath) < 0) {
            cartAssets.push(assetPath);

            // Update cart object with new asset
            cart[CART_ASSETS_KEY] = cartAssets;

            // Update local storage with new cart object
            storageProfile.setUserStorageObject(CART_KEY, cart);
            return true;
        }

        return false;
    }

    /* removes an asset path from the cart and persists to local storage */
    function removeCartAsset(assetPath) {
        var cart = storageProfile.getUserStorageObject(CART_KEY) || {},
            cartAssets = cart[CART_ASSETS_KEY] || [],
            index = cartAssets.indexOf(assetPath);
        
        if( index >= 0) {
            cartAssets.splice(index, 1);

            // Update cart object to remove extra asset
            cart[CART_ASSETS_KEY] = cartAssets;

            // Update local storage with new cart object
            storageProfile.setUserStorageObject(CART_KEY, cart);
            return true;
        }

        return false;
    
    }

    /* clears the cart and sets it to an empty array */
    function clearCartAssets() {
        var cart = storageProfile.getUserStorageObject(CART_KEY);

        if(cart  && cart[CART_ASSETS_KEY]) {
            //set cart assets to empty array
            cart[CART_ASSETS_KEY] = [];
            storageProfile.setUserStorageObject(CART_KEY, cart);
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

}(jQuery,
    AssetShare,
    AssetShare.StorageProfile));