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

	function getPaths() {
		var assetsInCart = [], paths = [];

		if (enabled()) {
			assetsInCart = contextHubStore.get();
		}

		if (!(assetsInCart instanceof Array)) {
			assetsInCart = [ assetsInCart ];
		}

		assetsInCart.forEach(function(cartAssetPath) {
			paths.push(cartAssetPath);
		});

		return paths;
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

	function getCookie(name) {
		var nameEQ = encodeURIComponent(name) + "=";
		var ca = document.cookie.split(';');
		for (var i = 0; i < ca.length; i++) {
			var c = ca[i];
			while (c.charAt(0) === '')
				c = c.substring(1, c.length);
			if (c.indexOf(nameEQ) === 0)
				return decodeURIComponent(c.substring(nameEQ.length, c.length));
		}
		return null;
	}

	function submitDownload(formdata, formurl) {

		$.ajax({
			type : "POST",
			url : formurl,
			data : formdata,
			success : function(data) {
				var cookievalue = getCookie('ADC');
				if (data.downlaodID) {
					add(data);
					if (cookievalue == null || cookievalue == "") {
						document.cookie = "ADC=" + data.downlaodID;
					} else {
						document.cookie = "ADC=" + data.downlaodID + ','
								+ cookievalue;
					}

					cookievalue = getCookie('ADC');
					var count = cookievalue.split(',').length;
					ns.Elements.element("downloads-count").text(count);
					messages.show('download-add');

				}

			},
			error : function() {
				essages.show('error');
			}
		});
	}

	function clear() {
		if (enabled() && contextHubStore.get()
				&& contextHubStore.get().length > 0) {
			contextHubStore.clear();

			$("body").trigger(ns.Events.CART_UPDATE, [ getSize(), getPaths() ]);
			$("body").trigger(ns.Events.CART_CLEAR, [ getSize(), getPaths() ]);
		}
	}

	function getContextHubStore() {
		return contextHubStore;
	}

	return {
		store : getContextHubStore,
		paths : getPaths,
		add : add,
		getCookie : getCookie,
		submitDownload : submitDownload,
		clearDownloads : clearDownloads
	};

}(jQuery, AssetShare, ContextHub.getStore("cart"), AssetShare.Messages));