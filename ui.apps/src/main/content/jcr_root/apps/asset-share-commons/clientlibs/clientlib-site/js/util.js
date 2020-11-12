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

/*global jQuery: false, AssetShare: false */
AssetShare.Util = (function() {
    "use strict";

    function isIframe() {
      return window.top.location !== window.location;
    }
  
    function isSameOrigin() {
      if (!isIframe()) {
        return true;
      }
  
      try {
        var __ = window.top.location;
      } catch (e) {
        if (e instanceof window.DOMException) {
          return e.code !== e.SECURITY_ERR;
        }
        return true;
      }
    }
  
    return { isIframe: isIframe, isSameOrigin: isSameOrigin };
})();