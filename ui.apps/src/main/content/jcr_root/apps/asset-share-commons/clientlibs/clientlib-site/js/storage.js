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

/*global jQuery: false, AssetShare: false */

AssetShare.Storage = (function (window, ns) {
    "use strict";

    var LOCAL_STORAGE_KEY = "asset-share-commons",
        CURRENT_USER_URI  = "/libs/granite/security/currentuser.json",
        currentUserId,
        localStorageEnabled = false,
        profileLoaded = false;

    /* Return the user profile for the current logged in user from local storage */
    function getUserProfile() {
        var storage;

        if(currentUserId === undefined) {
            return null;
        }

        storage = _getLocalStorage();
        if(storage["users"] && storage["users"][currentUserId]) {
            return storage["users"][currentUserId].profile;
        }
        return null;
    }

    /* gets the persisted return URL (not user specific)*/
    function getReturnUrl() {
        var storage;
        storage = _getLocalStorage();
        return storage["returnUrl"] || null;
    }

    /* persists the return URL */
    function setReturnUrl(url) {
        var storage;

        if(typeof url !== "undefined") {
            storage = _getLocalStorage();
            storage["returnUrl"] = url;
            _updateLocalStorage(storage);
        }
    }

    /* Set the User profile in local storage */
    function _setUserProfile(profile) {
        var storage;

        if(typeof profile === 'undefined' || profile.type !== 'user') {
            return;
        }

        storage = _getLocalStorage();

        //set current user
        currentUserId = profile.authorizableId_xss;
        
        //update existing user
        if(storage["users"] && storage["users"][currentUserId]) {
            storage["users"][currentUserId].profile = profile;
        } else {
            //add new user
            storage["users"] = storage["users"] || {};
            storage["users"][currentUserId] = {profile: profile};
        }

        _updateLocalStorage(storage);
    
    }

    /* adds an asset path to the cart and persists to local storage */
    function addCartAsset(assetPath) {
        var storage = _getLocalStorage(),
            assets;

        if(storage["users"] && storage["users"][currentUserId]) {

            storage["users"][currentUserId]["cart"] = storage["users"][currentUserId]["cart"] || {};
            assets = storage["users"][currentUserId]["cart"]["assets"] || [];

            if(assets.indexOf(assetPath) < 0) {
                assets.push(assetPath);
                storage["users"][currentUserId]["cart"]["assets"] = assets;
                _updateLocalStorage(storage);
                return true;
            }
        }
        return false;
    }

    /* removes an asset path from the cart and persists to local storage */
    function removeCartAsset(assetPath) {
        var storage = _getLocalStorage(),
            assets,
            index;

        if(storage["users"] && storage["users"][currentUserId] && storage["users"][currentUserId].cart) {
            assets = storage["users"][currentUserId]["cart"]["assets"] || [];

            index = assets.indexOf(assetPath);
            if( index >= 0) {
                assets.splice(index,1);
                storage["users"][currentUserId]["cart"]["assets"] = assets;
                _updateLocalStorage(storage);
                return true;
            }
        }
        return false;
    }

     /* clears the cart and sets it to an empty array */
     function clearCartAssets() {
        var storage = _getLocalStorage();

        if(storage["users"] && storage["users"][currentUserId]) {
            storage["users"][currentUserId]["cart"] = storage["users"][currentUserId]["cart"] || {};
            storage["users"][currentUserId]["cart"]["assets"] = [];

            _updateLocalStorage(storage);
            return true;
        }

        return false;
    }

    /* Gets the contents of the cart Array, if not set an empty array is returned */
    function getCartAssets() {
        var storage,
            paths = [];

        storage = _getLocalStorage();
        if(storage["users"] && storage["users"][currentUserId] && storage["users"][currentUserId].cart) {
            paths = storage["users"][currentUserId].cart.assets;
        }

        return paths;
    }

    /* Get status if profile ready and storage can read from local storage */
    function isReady() {
        return localStorageEnabled && profileLoaded;
    }

    /* Check if browser supports local storage capabilities */
    function _localStorageCheck() {
        var storage;

        if(localStorageEnabled) {
            return true;
        }

        try {
            storage = window.localStorage;
            var x = '__storage_test__';
            storage.setItem(x, x);
            storage.removeItem(x);
            localStorageEnabled = true;
            return true;
        }
        catch(e) {
            localStorageEnabled = false;
            return e instanceof DOMException && (
                // everything except Firefox
                e.code === 22 ||
                // Firefox
                e.code === 1014 ||
                // test name field too, because code might not be present
                // everything except Firefox
                e.name === 'QuotaExceededError' ||
                // Firefox
                e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
                // acknowledge QuotaExceededError only if there's something already stored
                (storage && storage.length !== 0);
        }
    }

    /* Return the local storage for Asset Share Commons as a JSON object */
    function _getLocalStorage() {
        if(_localStorageCheck()) {
            return JSON.parse(window.localStorage.getItem(LOCAL_STORAGE_KEY)) || {};
        }
    }

    /* Set the local Storage as a stringified JSON object */
    function _updateLocalStorage(storageUpdate) {
        if(_localStorageCheck()) {
            window.localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(storageUpdate));
        }
    }

    /* make a new request to get current user's profile */
    function _initProfile() {

        return new Promise(function(resolve, reject) {
            var request = new XMLHttpRequest();
            request.open('GET', CURRENT_USER_URI + '?nocache=' + new Date().getTime());
            request.responseType = 'json';
            request.onload = function() {
                if (request.status === 200) {
                    resolve(request.response);
                }
                else {
                    reject(Error('profile did not load' + request.statusText));
                }
            };
            request.onerror = function() {
                // Also deal with the case when the entire request fails to begin with
                // This is probably a network error, so reject the promise with an appropriate message
                    reject(Error('There was a network error.'));
             };
            request.send();
        });
    }

    /* dispatch event that profile was loaded sucessfully */
    function _announceProfileLoaded(profile) {
        var event = new CustomEvent(ns.Events.PROFILE_LOAD, {detail:profile}),
            element = document.getElementsByTagName("body")[0];
            profileLoaded = true;
            element.dispatchEvent(event);
    }

    //initial call to retrieve profile
    _initProfile().then(function(response) {
        if(response.type === 'user') {
           _setUserProfile(response);
           profileLoaded = true;
           _announceProfileLoaded(response);
        }
    });

    return {
        getReturnUrl: getReturnUrl,
        setReturnUrl: setReturnUrl,
        getUserProfile: getUserProfile,
        getCartAssets: getCartAssets,
        addCartAsset: addCartAsset,
        removeCartAsset: removeCartAsset,
        clearCartAssets: clearCartAssets,
        isReady: isReady
    };

}(window,
  AssetShare));