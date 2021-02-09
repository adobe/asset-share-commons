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

/*global jQuery: false, AssetShare: false, ContextHub: false*/

jQuery((function($, ns, cart, contextHub) {
    "use strict";

    function updateDownlaodCountBadge() {

        var cookievalue=readCookie('ADC');
        if(cookievalue){
			var count = cookievalue.split(',').length;
        	ns.Elements.element("downloads-count").text(count);
        }else{
			ns.Elements.element("downloads-count").text(0);
        }

    }


    // Page init
    function init() {
        updateDownlaodCountBadge();
    }


    function readCookie(name){
            var nameEQ=encodeURIComponent(name)+"=";
            var ca=document.cookie.split(';');
            for(var i=0;i<ca.length;i++) {
            var c=ca[i];
            while(c.charAt(0)==='')
                 c=c.substring(1,c.length);
                if(c.indexOf(nameEQ)===0)
                return decodeURIComponent(c.substring(nameEQ.length,c.length));
        	}
        	return null;
    }

    init();

    cart.store().eventing.on(contextHub.Constants.EVENT_STORE_READY, init);

}(jQuery,
    AssetShare,
    AssetShare.Cart,
    ContextHub)));