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
    function addDownload(downloadData) {
        var downloads = store.getObject(DOWNLOAD_KEY, false) || [];

        if (_getDownloadsIndexById(downloadData.id) < 0) {
            downloads.push(downloadData);

            // Update local storage with new cart object
            store.setObject(DOWNLOAD_KEY, downloads, false);

            _triggerDownloadsAddEvent(downloadData);

            return true;
        }
        // download id already exists
        return false;
    }

    function removeDownloadById(downloadId) {
        var downloads = store.getObject(DOWNLOAD_KEY, false) || [],
            index     = _getDownloadsIndexById(downloadId);

        if (index >= 0) {
            downloads.splice(index, 1);

            // Update local storage with new cart object
            store.setObject(DOWNLOAD_KEY, downloads, false);
            _triggerDownloadsAddEvent(downloadId);

            return true;
        }
        // couldn't find a downloadId to remove
        return false;
    }

    function removeAllDownloads() {
        store.setObject(DOWNLOAD_KEY, [], false);

        _triggerDownloadsClearEvent();
    }

    /**
     * Returns downloads tracked in session storage
     * @returns Array of downloads JSON object
     */
    function getDownloads() {
       return (store.getObject(DOWNLOAD_KEY, false) || []);
    }

    /**
     * Returns downloadIds tracked in storage
     * @returns Array of downloadIds
     */
    function getDownloadIds() {
       return getDownloads().map(download => download.id);
    }

    function _getDownloadsIndexById(downloadId) {
        return getDownloadIds().indexOf(downloadId);
    }

    function _triggerDownloadsUpdateEvent(download, type) {
        $("body").trigger(ns.Events.DOWNLOADS_UPDATE);
    }

    function _triggerDownloadsAddEvent(download) {
        $("body").trigger(ns.Events.DOWNLOADS_ADD, [ download ]);
        _triggerDownloadsUpdateEvent();
    }

    function _triggerDownloadsRemoveEvent(downloadId) {
        $("body").trigger(ns.Events.DOWNLOADS_REMOVE, [ downloadId ]);
        _triggerDownloadsUpdateEvent();
    }

    function _triggerDownloadsClearEvent(download) {
        $("body").trigger(ns.Events.DOWNLOADS_CLEAR);
        _triggerDownloadsUpdateEvent();
    }

    return {
        getDownloads: getDownloads,
        getDownloadIds: getDownloadIds,
        addDownload: addDownload,
        removeDownloadById: removeDownloadById,
        removeAllDownloads: removeAllDownloads
    };

}(AssetShare,
  AssetShare.Store));