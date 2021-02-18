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

/*global ContextHub: false, jQuery: false, AssetShare: false */

AssetShare.Downloads = (function($, ns, contextHubStore, messages) {
	"use strict";

	function enabled() {
		return contextHubStore !== null
				&& typeof contextHubStore !== "undefined";
	}

	function add(downloadInfo) {
		if (enabled()) {

			contextHubStore.addDownload(downloadInfo);
			return true;
		}

		return false;
	}

	function clearDownloads() {
		if (enabled()) {

			contextHubStore.clearDownloads();
			return true;
		}

		return false;
	}

	function submitDownload(formdata, formurl) {

		$.ajax({
			type : "POST",
			url : formurl,
			data : formdata,
			success : function(data) {
				var cookievalue = ContextHub.Utils.Cookie.getItem("ADC")
				if (data.downlaodID) {					
					if (cookievalue == null || cookievalue == "") {
						document.cookie = "ADC=" + data.downlaodID;
					} else {
						document.cookie = "ADC=" + data.downlaodID + ','
								+ cookievalue;
					}

					cookievalue = ContextHub.Utils.Cookie.getItem("ADC")
					var count = cookievalue.split(',').length;
					ns.Elements.element("downloads-count").text(count);
					messages.show('download-add');
					add(data);
				}

			},
			error : function() {
				messages.show('error');
			}
		});
	}


	function getContextHubStore() {
		return contextHubStore;
	}

	return {
		store : getContextHubStore,
		submitDownload : submitDownload,
		clearDownloads : clearDownloads
	};

}(jQuery, AssetShare, ContextHub.getStore("cart"), AssetShare.Messages));