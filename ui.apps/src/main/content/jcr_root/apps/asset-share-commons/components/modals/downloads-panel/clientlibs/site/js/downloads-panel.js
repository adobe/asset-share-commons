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

jQuery((function($, ns, semanticModal, downloads) {
	"use strict";
	AssetShare.SemanticUI.Modals.DownloadsPanelModal = (function() {
		var DOWNLOAD_PANEL_URL = ns.Data.val("downloads-panel-url"), DOWNLOAD_PANEL_MODAL_ID = "downloadspanel-modal", DOWNLOAD_PANEL_BUTTON_ID = "download-asset";

		function getId() {
			return DOWNLOAD_PANEL_MODAL_ID;
		}

		function getUrl() {
			return DOWNLOAD_PANEL_URL;
		}

		function getDownloadPanel() {
			var data = getDownloadIds();
			var downloadModal = {
					id : DOWNLOAD_PANEL_MODAL_ID,
					url : DOWNLOAD_PANEL_URL,
					data : data,
					options : {}
			};

			return downloadModal;
		}

		function getDownloadIds() {
			var downloadIdData = {};
			downloadIdData.downloadIds = sessionStorage.getItem("downloadIds");

			return downloadIdData;
		}

		function refresh(e) {
			e.preventDefault();
			e.stopPropagation();
			updatePanelModal();
		}

		function clear(e) {
			e.preventDefault();
			e.stopPropagation();
			sessionStorage.removeItem("downloadIds");
			ns.Elements.element("downloads-count").text(0);
			updatePanelModal();
			downloads.clearDownloads();
		}

		function updatePanelModal() {
			var data = getDownloadIds();
			$.post(DOWNLOAD_PANEL_URL, data).then(function(htmlResponse) {
				ns.Elements.update(htmlResponse, 'downloads-update');
			});
		}

		function showDownloadsPanel(e) {
			var downloadPanelModal = getDownloadPanel();
			e.preventDefault();
			e.stopPropagation();
			semanticModal.show([ downloadPanelModal ]);
		}

		/** REGISTER EVENTS WHEN DOCUMENT IS READY * */
		$((function registerEvents() {

			$("body").on("click",
					ns.Elements.selector([ "refresh-downloads" ]), refresh);
			$("body").on("click", ns.Elements.selector([ "clear-downloads" ]),
					clear);
			$("body").on("click",
					ns.Elements.selector([ "show-downloadspanel" ]),
					showDownloadsPanel);

		}()));

		return {
			id : getId,
			url : getUrl,
			modal : getDownloadPanel
		};
	}());
}(jQuery, AssetShare, AssetShare.SemanticUI.Modal, AssetShare.Downloads)));