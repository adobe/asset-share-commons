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
AssetShare.Store = (function (window, ns) {
    'use strict';

    var STORE_KEY = 'asset-share-commons',
        localStorageEnabled = false;


    /* Return the local storage for Asset Share Commons as a JSON object */
    function _getLocalStorage() {
        return JSON.parse(window.localStorage.getItem(STORE_KEY)) || {};
    }

    /* Set the local Storage as a stringified JSON object */
    function _updateLocalStorage(storageUpdate) {
        window.localStorage.setItem(STORE_KEY, JSON.stringify(storageUpdate));
    }

    /* Return the local storage for Asset Share Commons as a JSON object */
    function _getSessionStorage() {
        return JSON.parse(window.sessionStorage.getItem(STORE_KEY)) || {};
    }

    /* Set the local Storage as a stringified JSON object */
    function _updateSessionStorage(storageUpdate) {
        window.sessionStorage.setItem(STORE_KEY, JSON.stringify(storageUpdate));
    }

    /**
     * Returns an object stored at the root of the local storage
     * @param {*} key
     */
    function getObject(key, useLocalStorage = true) {
        var storage;
        if(useLocalStorage) {
            storage = _getLocalStorage();
        } else {
            storage = _getSessionStorage();
        }

        return storage[key];
    }

    /**
     * Sets a key and object at the root of the local storage
     * @param {*} key - key to store object under
     * @param {*} object - object to persist to local storage
     */
    function setObject(key, object, useLocalStorage = true) {
        var updatedStorage;

        if(useLocalStorage) {
            updatedStorage = _getLocalStorage();
            updatedStorage[key] = object;
            _updateLocalStorage(updatedStorage);
        } else {
            updatedStorage = _getSessionStorage();
            updatedStorage[key] = object;
            _updateSessionStorage(updatedStorage);
        }
    }

    return {
        getObject: getObject,
        setObject: setObject
    };

}(window,
  AssetShare));
