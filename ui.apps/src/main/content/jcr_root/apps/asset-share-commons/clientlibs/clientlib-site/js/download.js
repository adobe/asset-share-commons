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

/**
 * Object to facilitate asynchronous download operations
 * common operations include initiating an asynchronous download,
 * polling for status based on downloadId,
 * and performing the actual download once the download package is ready
 */
AssetShare.Download = (function ($, ns, messages, downloadStore) {
    "use strict";

    const DOWNLOAD_ID = "id",
        DWNL_STATUS_PN = "isComplete",
        DWNL_ARTIFACTS_PN = "artifacts",
        BODY_SELECTOR = "body.page",
        LOADING_SELECTOR = "download-loader-text",
        POLL_DATA_ATTRIBUTE = "asset-share-download-automatic",
        POLL_ENDPOINT = "/content/dam.downloadbinaries.json",
        POLL_TIMEOUT = 1000,
        MAX_ATTEMPTS = 3;

    /**
     * Invoked from download Modal
     * @param {*} form
     */
    function initializeDownload(form) {
        form = $(form);
        $.ajax({
            type: "POST",
            url: form.attr("action"),
            data: form.serialize(),
            success: function (data) {

                if (data[DOWNLOAD_ID]) {
                    // initialize polling if polling is configured
                    if (form.data(POLL_DATA_ATTRIBUTE)) {
                        // initiate dimmer on page
                        _showDimmer();
                        _poll(data, 0);
                    } else {
                        // track downloadid in session storage
                        _storeDownloadId(data);
                    }
                }
            },
            error: function (e) {
                console.error(e.responseJSON.error);
            }
        });
    }

    /**
     * Perform polling to determine if a given downloadId is ready
     * @param {*} downloadData the JSON response from the Async Download Servlet
     * returns true if the download is ready
     */
    function _poll(downloadData, attempts) {
        setTimeout(function () {
            $.ajax({
                url: `${POLL_ENDPOINT}?downloadId=${downloadData.id}`,
                success: function (data) {
                    attempts++;
                    if (data[DWNL_STATUS_PN]) {
                        for (let artifact of data[DWNL_ARTIFACTS_PN]) {
                            downloadArtifact(downloadData.id, artifact.uri);
                        }
                        _hideDimmer();
                    } else if (attempts >= MAX_ATTEMPTS) {
                        // max attempts reached, save downloadId to sessionStorage
                        _hideDimmer();
                        console.debug(`Max attempts reached polling of ${downloadData.Id}, saving downloadId to session storage.`);
                        _storeDownloadId(downloadData);
                    } else {
                        _poll(downloadId, attempts);
                    }
                },
                error: function (e) {
                    _hideDimmer();
                    console.error(e.responseJSON.error);
                },
                dataType: "json",
            });
        }, POLL_TIMEOUT);
    }

    /**
     * If polling times out or polling not enabled save the downloadId to session storage
     * User can then access the download archive via the downloads panel
     * @param {*} downloadId
     */
    function _storeDownloadId(downloadData) {
        if (downloadStore.addDownload(downloadData)) {
            messages.show('download-add');
        } else {
            console.debug(`${downloadData.id} was not added to session storage`);
        }
    }

    /**
     * Show a loading dimmer during polling
     */
    function _showDimmer() {
        $(BODY_SELECTOR)
            .dimmer({ closable: false })
            .dimmer("add content", ns.Elements.selector(LOADING_SELECTOR))
            .dimmer("show");
    }

    /**
     * Hide the loading dimmer
     */
    function _hideDimmer() {
        $(BODY_SELECTOR).dimmer("hide", function () {
            // need to remove loader from page dimmer otherwise it will show in subsequent modals
            let loaderMessage = $(ns.Elements.selector(LOADING_SELECTOR)).detach();
            $(BODY_SELECTOR).append(loaderMessage);
        });
    }

    /**
     * Trigger the downloadId in a new window
     * Remove the downloadId from session storage
     * @param {*} downloadId
     * @param {*} downloadUri
     */
    function downloadArtifact(downloadId, downloadUri) {
        //trigger the download in a new window
        window.open(downloadUri, '_blank');

        //remove downloadId from storage
        downloadStore.removeDownloadById(downloadId);
    }

    return {
        initializeDownload: initializeDownload
    };

}(jQuery, AssetShare, AssetShare.Messages, AssetShare.Store.Download));