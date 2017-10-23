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

AssetShare.Data = ( function ($, ns) {
    "use strict";

    function deriveData(el) {
        var val;

        el = $(el);
        if (el.is(":text, :hidden")) {
            val = el.val();
        } else if (el.is(":checkbox, :checked")) {
            val = el.val();
        }

        return val;
    }

    function getOrSetValue(id, value) {
        var els = ns.Elements.element(id) || [],
            values = [];

        if (value === null || typeof value === 'undefined') {
            if (els.length > 0) {
                els.each(function (index, el) {
                    var val = deriveData(el);
                    if (val) {
                        values.push(val);
                    }
                });
            }

            if (values.length === 0) {
                return null;
            } else if (values.length === 1) {
                return values[0];
            } else {
                return values;
            }
        } else {
            els.val(value);
        }
    }

    function getAttributeValue(el, id) {
        el = $(el);
        return el.data("asset-share-" + id);
    }

    function setData(id, value, attributeName) {
        var targetEl = ns.Elements.element(id);

        targetEl.each(function (index, el) {
            $(el).attr((attributeName || "value"), value);
        });

        return value;
    }

    return {
        val: getOrSetValue,
        attr: getAttributeValue
    };
}(jQuery,
    AssetShare));
