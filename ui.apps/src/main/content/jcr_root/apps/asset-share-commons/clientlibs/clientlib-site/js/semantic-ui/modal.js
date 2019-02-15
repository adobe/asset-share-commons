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

    function isPreviewMode() {
        var topWindow = $(window.top);

        return topWindow.length > 0 &&
            topWindow[0].Granite &&
            topWindow[0].Granite.author &&
            topWindow[0].Granite.author.layerManager &&
            topWindow[0].Granite.author.layerManager.getCurrentLayerName &&
            topWindow[0].Granite.author.layerManager.getCurrentLayerName() === 'Preview';
    }

    function onShowModalInPreviewMode(modal) {
        var topWindow = $(window.top),
            padding = 50,
            scroll,
            modalHeight,
            top,
            offset;

        modal = $(modal);

        scroll = $("#ContentScrollView", $(topWindow)[0].document).scrollTop();
        modalHeight = modal.outerHeight();
        top = scroll + (topWindow.height() - modalHeight)/2;
        offset = ($(window).height()-scroll) - (modalHeight + padding);

        if (offset < 0) {
            top = top + offset;
        }

        modal.css('top', top + 'px').css('margin-top', '');
    }

    function addToOpenModals(id) {
        if(tracker.indexOf(id) === -1) {
            tracker.push(id);
        }
    }

    function removeFromOpenModals(id) {
        var index = tracker.indexOf(id);
        if (index !== -1) {
            tracker.splice(index, 1);
            ns.Elements.remove(id);
        }
    }

    function isOpenModal(id) {
        return tracker.indexOf(id) !== -1;
    }

    function showModal(modals, index) {
        var modal = null;

        if (index < modals.length) {
            // move to the next modal
            modal = modals[index];
        } else {
            // No more modals left to load!
            return;
        }
        
        $.post(modal.url, modal.data, function (htmlResponse) {
            modal.options = modal.options || {};
            modal.options.show = modal.options.show || function (modal) {
                modal.modal("show");
            };

            if (!isOpenModal(modal.id)) {
                modal.options.show($('<div>' + htmlResponse + "</div>").find(ns.Elements.selector(modal.id)).modal({
                    allowMultiple: false,
                    closable: true,
                    onShow: function () {
                        addToOpenModals(modal.id);
                        if (isPreviewMode()) {
                            onShowModalInPreviewMode(this);
                        }
                        $("body").trigger(ns.Events.MODAL_SHOWN);
                    },
                    onHidden: function () {
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

            // Wait for first ajax to finish before making subsequent calls
            showModal(modals, index + 1);
        });
    }

    function show(modals) {
        if (!Array.isArray(modals)) {
            modals = [modals];
        }

        showModal(modals, 0);
    }


    return {
        show: show
    };
}(jQuery,
    AssetShare));
