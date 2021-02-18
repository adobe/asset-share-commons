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

jQuery((function(ns, downloads, semanticModal, licenseModal, messages) {
	"use strict";
	AssetShare.SemanticUI.Modals.AsyncDownloadModal = (function() {
		var DOWNLOAD_URL = ns.Data.val("download-url"), 
			DOWNLOAD_MODAL_ID = "download-modal"

		function getId() {
			return DOWNLOAD_MODAL_ID;
		}

		function getUrl() {
			return DOWNLOAD_URL;
		}

		/** REGISTER EVENTS WHEN DOCUMENT IS READY * */
		$((function registerEvents() {
			$("body").on("submit", ns.Elements.selector([ "download-modal" ]),
					function(e) {
						var formEl = $(this);
						e.preventDefault();
						e.stopPropagation();
						if (formEl.form('is valid')) {
							var form = $(formEl);
							downloads.submitDownload(form.serialize(), form.attr("link"));
						}
					});
		}()));

		return {
			id : getId,
			url : getUrl
		};
	}());
}(AssetShare, AssetShare.Downloads, AssetShare.SemanticUI.Modal,
		AssetShare.SemanticUI.Modals.LicenseModal, AssetShare.Messages)));