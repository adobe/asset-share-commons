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

/*global es6: true, $: false, AssetShare: false, window: false, document: false*/

AssetShare.Search.Form = function (ns) {
    "use strict";

    var url,
        mode,
        formData;

    function getId() {
       return "asset-share-commons__form-id__1";
    }

    /** Operations **/
    function getUrl() {
        return url;
    }

    function reset() {
        formData = new ns.FormData($('[form="' + getId() + '"]'));
    }

    /** Getter Or Setter Methods **/

    /**
     * Cleaning the form is performed so NOOP query builder parameters are not passed.
     *
     * This works by iterating over all form inputs.
     * - If the the form input has a `data-asset-share-predicate-id` then..
     * -- Search all inputs to see if
     * ---- a 'related' Input exists with with a for="<predicateId>" AND that related Input has SOME value.
     * ---- If it does, add the original input to the form, else dont.
     *
     *
     * @param formData
     * @returns {AssetShare.FormData}
     */
    function clean(formData) {
        var cleanFormData = new ns.FormData();

        // Use formData to auto-collect the super-set of eligible inputs to clean.
        formData.forEach(function (inputName, inputValue) {

            // Only look at input fields that belong to the form via the form attribute.
            var candidateInputs = $("[name=\"" + inputName + "\"][form=\"" + getId() + "\"]");

            // We max have more than 1 input with the same name; for example in radio/toggle/sliders
            candidateInputs.each(function() {

                var candidateInput = $(this),
                    candidatePredicateId = ns.Data.attr(candidateInput, "predicate-id") || null,
                    candidateInputAdded = false;

               if (candidatePredicateId) {
                    $("[for=\"" + candidatePredicateId + "\"]").each(function (index, relatedInput) {
                        // For each 'value-full input' that is associated to this predicateId...
                        if (!candidateInputAdded) {
                            // If this candidate has NOT found a supporting "for" input then keep looking!
                            // Check to see if it exists in formData

                            var relativeInputName = $(relatedInput).attr("name"),
                                relatedInputValue = formData.get(relativeInputName);
                            if (relatedInputValue) {
                                // Add to the clean form
                                if (cleanFormData.get(inputName) !== inputValue) {
                                    // Never add the same exact inputName=inputValue twice
                                    cleanFormData.add(inputName, inputValue);
                                }
                                candidateInputAdded = true;
                            }
                        }
                    });
                } else {
                    // No predicateId, so this is a stand-alone field and always add it unless its already exists
                   if (inputValue !== '' && cleanFormData.get(inputName) !== inputValue) {
                       // Never add the same exact inputName=inputValue twice
                       cleanFormData.add(inputName, inputValue);
                   }
                }
            });
        });

        return cleanFormData;
    }

    function buildFormData(formData, event) {
        var clone = clean(formData.clone());

        // Clear all search data marked as events; they will be re-added as needed below
        $("[data-asset-share-search-actions]").each(function () {
            clone.remove($(this).attr('name'));
        });

        // Add all search data that apply to all events or this specific event
        $("[data-asset-share-search-actions*=\"all\"],[data-asset-share-search-actions*=\"" + event + "\"]").each(function (index, element) {
            if ($.trim($(element).val()) !== '') {
                clone.set($(this).attr('name'), $(this).val());
            }
        });

        return clone;
    }

    function serializeFor(event, resetForm) {
        if (resetForm) {
            reset();
        }
        return buildFormData(formData, event).serialize();
    }

    function init() {
        // On init, the DOM is king as its populated by the server page load
        url = ns.Data.attr(ns.Elements.element("form"), "action");
        mode = ns.Data.val("mode");

        reset();
    }

    init();

    return {
        url: getUrl,
        serializeFor: serializeFor,
        id: getId
    };
};

