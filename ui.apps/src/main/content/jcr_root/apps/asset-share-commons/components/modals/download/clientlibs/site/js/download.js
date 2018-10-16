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

jQuery((function(ns, semanticModal, licenseModal) {
    "use strict";
    AssetShare.SemanticUI.Modals.DownloadModal = (function () {
        var DOWNLOAD_URL = ns.Data.val("download-url"),
            DOWNLOAD_MODAL_ID = "download-modal",
            DOWNLOAD_BUTTON_ID = "download-asset";

        function getId() {
            return DOWNLOAD_MODAL_ID;
        }

        function getUrl() {
            return DOWNLOAD_URL;
        }

        function getModal(formDataOrAssetPath, licensed) {
            var formData = formDataOrAssetPath,
                downloadModal;

            if (typeof formDataOrAssetPath === 'string') {
                formData = new ns.FormData();
                formData.add("path", formDataOrAssetPath);
            }

            downloadModal = {
                id: DOWNLOAD_MODAL_ID,
                url: DOWNLOAD_URL,
                data: formData.serialize(),
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

        function download(e) {
            var path = encodeURIComponent(ns.Data.attr(this, "asset")),
                license = ns.Data.attr(this, "license"),
                downloadModal = getModal(path, license);

            e.preventDefault();
            e.stopPropagation();

            if (license && licenseModal.modal(path)) {
                semanticModal.show([licenseModal.modal(path), downloadModal]);
            } else {
                semanticModal.show([downloadModal]);
            }
        }

        /** REGISTER EVENTS WHEN DOCUMENT IS READY **/
        $((function registerEvents() {
            $("body").on("click", ns.Elements.selector([DOWNLOAD_BUTTON_ID]), download);
        }()));

        return {
            id: getId,
            url: getUrl,
            modal: getModal,
            download: download
        };
    }());
}(AssetShare,
    AssetShare.SemanticUI.Modal,
    AssetShare.SemanticUI.Modals.LicenseModal)));