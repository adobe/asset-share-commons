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

/*global es6: true,  $: false, AssetShare: false, window: false, document: false*/

AssetShare.FormData = function (formEl) {
    "use strict";

    var form = [];

    if (formEl) {
        form = $(formEl).serializeArray();
    }

    /* IE polyfills for findIndex() and find() */

    function findIndex(key) {
        var i;

        for (i = 0; i < form.length; ++i) {
            if (form[i].name === key) {
                return i;
            }
        }

        return -1;
    }

    function find(key) {
       var index = findIndex(key);

       if (index > -1) {
           return form[index];
       }

       return null;
    }

    function forEach(fn) {
        form.forEach(function (element) {
            fn(element.name, element.value);
        });
    }

    function clear() {
        form = [];
    }

    function getAll() {
        return form;
    }

    function get(key) {
        var element = find(key);

        if (element) {
            return element.value;
        }
    }

    function remove(key) {
        var index = findIndex(key);

        if (index > -1) {
            form.splice(index, 1);
        }
    }

    function add(key, val) {
        form.push({name: key, value: val});
    }


    function set(key, val) {
        remove(key);
        form.push({name: key, value: val});
    }

    function clone() {
        var cloneForm = new AssetShare.FormData();

        form.forEach(function (element) {
            cloneForm.set(element.name, element.value);
        });

        return cloneForm;
    }

    function serialize() {
        return $.param(form);
    }

    return {
        add: add,
        set: set,
        remove: remove,
        get: get,
        getAll: getAll,
        clear: clear,
        forEach: forEach,
        clone: clone,
        serialize: serialize
    };
};