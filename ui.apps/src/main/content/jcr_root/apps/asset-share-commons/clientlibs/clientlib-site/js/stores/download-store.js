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
 * Utility to interact with download IDs saved to session storage
 * to support asynchronous download
 */
AssetShare.Store.Download = (function (ns, store) {
    'use strict';

    var DOWNLOAD_KEY            = 'downloads';

    /* adds an asset path to the cart and persists to local storage */
    function addDownloadId(downloadId) {
        var downloads = store.getObject(DOWNLOAD_KEY, false) || [];

        if(downloads.indexOf(downloadId) < 0) {
            downloads.push(downloadId);

            // Update local storage with new cart object
            store.setObject(DOWNLOAD_KEY, downloads, false);
            return true;
        }
        // download id already exists
        return false;
    }

    function removeDownloadId(downloadId) {
        var downloads = store.getObject(DOWNLOAD_KEY, false) || [],
            index     = downloads.indexOf(downloadId);

        if( index >= 0) {
            downloads.splice(index, 1);

            // Update local storage with new cart object
            store.setObject(DOWNLOAD_KEY, downloads, false);
            return true;
        }
        // couldn't find a downloadId to remove
        return false;
    }

    /**
     * Returns downloadIds tracked in session storage
     * @returns Array of downloadIds
     */
    function getDownloadIds() {
        return store.getObject(DOWNLOAD_KEY, false) || [];
    }

    return {
        getDownloadIds: getDownloadIds,
        addDownloadId: addDownloadId,
        removeDownloadId: removeDownloadId
    };

}(AssetShare,
  AssetShare.Store));