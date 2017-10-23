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

AssetShare.SemanticUI.Modal = (function ($, ns) {
    "use strict";

    var tracker = [];

    function addToOpenModals(id) {
        if(tracker.indexOf(id) === -1) {
            tracker.push(id);
        }
    }

    function removeFromOpenModals(id) {
        var index = tracker.indexOf(id);
        if (index !== -1) {
            tracker.splice(index, 1);
        }
    }

    function isOpenModal(id) {
        return tracker.indexOf(id) !== -1;
    }

    function show(modals) {
        if (!Array.isArray(modals)) {
            modals = [modals];
        }

        modals.forEach(function (modal) {
            $.get(modal.url, modal.data, function (htmlResponse) {

                modal.options = modal.options || {};
                modal.options.show = modal.options.show || function (modal) {
                    modal.modal("show");
                };

                if (!isOpenModal(modal.id)) {
                    modal.options.show($('<div>' + htmlResponse + "</div>").find(ns.Elements.selector(modal.id)).modal({
                        allowMultiple: false,
                        closable: true,
                        onShow: function() {
                            addToOpenModals(modal.id);
                        },
                        onHidden: function() {
                            removeFromOpenModals(modal.id);
                        },
                        onDeny: function () {
                            return modal.options.onDeny ? modal.options.onDeny() : true;
                        },
                        onApprove: function () {
                            return modal.options.onApprove ? modal.options.onApprove() : true;
                        }
                    }));
                }
            });
        });
    }

    return {
        show: show
    };
}(jQuery,
    AssetShare));
