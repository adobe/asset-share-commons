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

jQuery((function(ns) {
    "use strict";
    AssetShare.SemanticUI.Modals.LicenseModal = (function () {
        var LICENSE_URL = ns.Data.val("license-url"),
            LICENSE_MODAL_ID = "license-modal",
            LICENSE_ACCEPT_ID = "license-accept";

        function getId() {
            return LICENSE_MODAL_ID;
        }

        function getAcceptId() {
            return LICENSE_ACCEPT_ID;
        }

        function getUrl() {
            return LICENSE_URL;
        }

        function getModal(assetPath) {
            if (!assetPath) {
                return null;
            }

            return {
                id: LICENSE_MODAL_ID,
                url: LICENSE_URL + assetPath,
                data: {},
                options: {
                    show: function (modal) {
                        modal.modal('show');
                    }
                }
            };
        }

        return {
            id: getId,
            acceptId: getAcceptId,
            url: getUrl,
            modal: getModal
        };

    }());
}(AssetShare)));