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

/*global jQuery: false, AssetShare: false*/

jQuery((function(ns, semanticModal, licenseModal,downloads) {
    "use strict";
    AssetShare.SemanticUI.Modals.DownloadsPanelModal = (function () {
    	var DOWNLOAD_PANEL_URL = ns.Data.val("downloads-panel-url"),
            DOWNLOAD_PANEL_MODAL_ID = "downloadspanel-modal",
            DOWNLOAD_PANEL_BUTTON_ID = "download-asset";

        function getId() {
            return DOWNLOAD_PANEL_MODAL_ID;
        }

        function getUrl() {
            return DOWNLOAD_PANEL_URL;
        }

        function getModal(formDataOrAssetPath, licensed) {
            var formData = formDataOrAssetPath,
                downloadModal;

            if (typeof formDataOrAssetPath === 'string') {
                formData = new ns.FormData();
                formData.add("path", formDataOrAssetPath);
            }

            downloadModal = {
                id: DOWNLOAD_PANEL_MODAL_ID,
                url: DOWNLOAD_PANEL_URL,
                data: formData,
                options: {}
            };

            if (licensed) {
                downloadModal.options.show = function (modal) {
                    modal.modal("attach events", ns.Elements.selector([licenseModal.id(), licenseModal.acceptId()]));
                };
            } else {
                downloadModal.options.show = function (modal) {
                    modal.modal('show');
                };
            }

            return downloadModal;
        }

        function refresh(e) {
               	e.preventDefault();
            	e.stopPropagation();
                updatePanelModal();

            return null;
        }

        function clear(e) {
               	e.preventDefault();
            	e.stopPropagation();
            	document.cookie="ADC=;expires = Thu, 01 Jan 1970 00:00:00 GMT";
                updatePanelModal();
            	ns.Elements.element("downloads-count").text(0);
            	downloads.clearDownloads();

            return null;
        }

        function updatePanelModal() {
            $.post(DOWNLOAD_PANEL_URL).then(function (htmlResponse) {
                ns.Elements.update(htmlResponse, 'downloads-update');
            });
        }

        function showDownloadsPanel(e) {

			 var path = ns.Data.attr(this, "asset"),
                license = ns.Data.attr(this, "license"),
                downloadModal = getModal(path, license);

            e.preventDefault();
            e.stopPropagation();
            semanticModal.show([downloadModal]);
        }

        /** REGISTER EVENTS WHEN DOCUMENT IS READY **/
        $((function registerEvents() {

             $("body").on("click", ns.Elements.selector(["refresh-downloads"]), refresh);
             $("body").on("click", ns.Elements.selector(["clear-downloads"]), clear);
             $("body").on("click", ns.Elements.selector(["show-downloadspanel"]), showDownloadsPanel);

        }()));

        return {
            id: getId,
            url: getUrl,
            modal: getModal
        };
    }());
}(AssetShare,
    AssetShare.SemanticUI.Modal,
    AssetShare.SemanticUI.Modals.LicenseModal,
    AssetShare.Downloads)));