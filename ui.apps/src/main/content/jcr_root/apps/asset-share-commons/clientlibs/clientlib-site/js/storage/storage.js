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
    'use strict';

    var STORAGE_KEY = 'asset-share-commons',
        localStorageEnabled = false;

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
            return JSON.parse(window.localStorage.getItem(STORAGE_KEY)) || {};
        }
    }

    /* Set the local Storage as a stringified JSON object */
    function _updateLocalStorage(storageUpdate) {
        if(_localStorageCheck()) {
            window.localStorage.setItem(STORAGE_KEY, JSON.stringify(storageUpdate));
        }
    }

    /**
     * Returns an object stored at the root of the local storage
     * @param {*} key 
     */
    function getObject(key) {
        var storage = _getLocalStorage();
        return storage[key];
    }

    /**
     * Sets a key and object at the root of the local storage
     * @param {*} key - key to store object under
     * @param {*} object - object to persist to local storage
     */
    function setObject(key, object) {
        var updatedStorage = _getLocalStorage();
        updatedStorage[key] = object;
        _updateLocalStorage(updatedStorage);
    }

    return {
        getObject, getObject,
        setObject, setObject
    };

}(window,
  AssetShare));