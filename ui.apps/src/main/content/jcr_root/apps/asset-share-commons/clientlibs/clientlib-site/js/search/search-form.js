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

    function _htmlForm() {
        return $('form[id="' + getId() + '"]');
    }

    /** Operations **/
    function getUrl() {
        return url;
    }

    function reset() {
        formData = new ns.FormData(_htmlForm());
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
                                if (cleanFormData.getAll(inputName).indexOf(inputValue) === -1) {
                                    // Never add the same exact inputName=inputValue twice
                                    cleanFormData.add(inputName, inputValue);
                                }
                                candidateInputAdded = true;
                            }
                        }
                    });
                } else {
                    // No predicateId, so this is a stand-alone field and always add it unless its already exists
                   if (inputValue !== '' && cleanFormData.getAll(inputName).indexOf(inputValue) === -1) {
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
        clone = _adjustFormData(clone);

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

    function deserialize(query) {
        var deserializedFormData = new ns.FormData();

        $.each((query || "").replace(/^\?/, "").split("&"), function(index, pair) {
            var separator,
                name,
                value;

            if (!pair) {
                return;
            }

            separator = pair.indexOf("=");
            name = separator > -1 ? pair.substring(0, separator) : pair;
            value = separator > -1 ? pair.substring(separator + 1) : "";

            try {
                name = decodeURIComponent(name.replace(/\+/g, " "));
                value = decodeURIComponent(value.replace(/\+/g, " "));
            } catch (e) {
                return;
            }

            if (name) {
                deserializedFormData.add(name, value);
            }
        });

        return deserializedFormData;
    }

    function removeAll(formDataToUpdate, name) {
        while (typeof formDataToUpdate.get(name) !== "undefined") {
            formDataToUpdate.remove(name);
        }
    }

    function serializeQueryFor(query, event) {
        var queryFormData = deserialize(query);

        $("[data-asset-share-search-actions]").each(function() {
            removeAll(queryFormData, $(this).attr("name"));
        });

        $("[data-asset-share-search-actions*=\"all\"],[data-asset-share-search-actions*=\"" + event + "\"]").each(function() {
            if ($.trim($(this).val()) !== "") {
                queryFormData.set($(this).attr("name"), $(this).val());
            }
        });

        return queryFormData.serialize();
    }

    function serializeJsonFor(event, resetForm, removeKeys) {
        var json = {};

        if (resetForm) {
            reset();
        }

        removeKeys = removeKeys || [];

        buildFormData(formData, event).getAll().forEach(function(field) {
            if (removeKeys.indexOf(field.name) > -1) {
                return;
            }

            if (json[field.name]) {
                if (!Array.isArray(json[field.name])) {
                    json[field.name] = [json[field.name]];
                }
                json[field.name].push(field.value);
            } else {
                json[field.name] = field.value;
            }
        });

        return JSON.stringify(json);
    }

    function fieldsToJson(fields, removeKeys) {
        var json = {};

        removeKeys = removeKeys || [];

        fields.forEach(function(field) {
            if (removeKeys.indexOf(field.name) > -1) {
                return;
            }

            if (json[field.name]) {
                if (!Array.isArray(json[field.name])) {
                    json[field.name] = [json[field.name]];
                }
                json[field.name].push(field.value);
            } else {
                json[field.name] = field.value;
            }
        });

        return json;
    }

    function getInputLabel(input) {
        var inputElement = $(input),
            label;

        if (inputElement.attr("id")) {
            label = $("label[for=\"" + inputElement.attr("id") + "\"]").first().text();
        }

        if (!label) {
            label = inputElement.closest(".checkbox").find("label").first().text();
        }

        return $.trim(label || "");
    }

    function getPredicateTitle(predicateId, relatedInputs) {
        var title = relatedInputs.closest(".content").prev(".title").first().text();

        if (!title) {
            title = relatedInputs.closest(".accordion").find(".title").first().text();
        }

        return $.trim(title || predicateId).replace(/\s+/g, " ");
    }

    function getPredicateOptions(relatedInputs) {
        var options = [];

        relatedInputs.each(function(index, input) {
            var inputElement = $(input),
                type = inputElement.attr("type") || input.tagName.toLowerCase();

            if (inputElement.is("select")) {
                inputElement.find("option").each(function(optionIndex, option) {
                    var optionElement = $(option);

                    if ($.trim(optionElement.val()) !== "") {
                        options.push({
                            name: inputElement.attr("name"),
                            value: optionElement.val(),
                            label: $.trim(optionElement.text()).replace(/\s+/g, " "),
                            selected: optionElement.is(":selected"),
                            disabled: optionElement.is(":disabled")
                        });
                    }
                });
            } else if (type === "checkbox" || type === "radio") {
                options.push({
                    name: inputElement.attr("name"),
                    value: inputElement.val(),
                    label: getInputLabel(input),
                    selected: inputElement.is(":checked"),
                    disabled: inputElement.is(":disabled")
                });
            }
        });

        return options;
    }

    function serializeDiscoveryContextFor(event, resetForm, removeKeys) {
        var context,
            predicates = {};

        if (resetForm) {
            reset();
        }

        removeKeys = removeKeys || [];

        context = fieldsToJson(buildFormData(formData, event).getAll(), removeKeys);
        context.selectedQuery = $.extend({}, context);
        context.predicates = [];

        $("[data-asset-share-predicate-id][form=\"" + getId() + "\"]").each(function(index, element) {
            var field = $(element),
                predicateId = ns.Data.attr(field, "predicate-id"),
                predicate;

            if (!predicateId) {
                return;
            }

            if (!predicates[predicateId]) {
                predicates[predicateId] = {
                    id: predicateId,
                    title: "",
                    fields: [],
                    inputs: [],
                    options: []
                };
                context.predicates.push(predicates[predicateId]);
            }

            predicate = predicates[predicateId];
            predicate.fields.push({
                name: field.attr("name"),
                value: field.val()
            });
        });

        context.predicates.forEach(function(predicate) {
            var relatedInputs = $(":input[for=\"" + predicate.id + "\"][form=\"" + getId() + "\"]");

            predicate.title = getPredicateTitle(predicate.id, relatedInputs);
            predicate.inputs = relatedInputs.map(function(index, input) {
                var inputElement = $(input);

                return {
                    name: inputElement.attr("name"),
                    type: inputElement.attr("type") || input.tagName.toLowerCase(),
                    value: inputElement.val(),
                    selected: inputElement.is(":checked") ||
                        inputElement.is("select") && inputElement.val() !== ""
                };
            }).get();
            predicate.options = getPredicateOptions(relatedInputs);
        });

        return JSON.stringify(context);
    }

    function _adjustFormData(formData) {
        formData.getAll().forEach(function(field) {
            // Handle date range fields upperBounds to make it the last millisecond of the selected day
            // Make sure not to double-process field values that have already been adjusted
            if (field.name.endsWith('daterange.upperBound') && !field.value.endsWith('T23:59:59.999Z')) {
                formData.set(field.name, field.value + 'T23:59:59.999Z');
            }
        });

        return formData;
    }

    function _valid(formToValidate) {
        var valid = true,
            visible = true;

        formData.getAll().forEach(function(formEntry) {
           var inputElement = $('[name="' + formEntry.name + '"][form="' + getId() + '"]'),
               inputElementValid,
               inputElementValidationMessage;

           if (inputElement && inputElement[0]) {
               inputElementValid = inputElement[0].checkValidity();

               if (!inputElementValid) {
                   valid = false;
                   visible = visible && inputElement.is(':visible');

                   inputElementValidationMessage = inputElement.data('asset-share-input-validation-message');
                   if (inputElementValidationMessage) {
                       inputElement[0].setCustomValidity(inputElementValidationMessage);
                   }
               }
           }
        });

        if (!valid && visible) {
            // Only trigger if all erroring fields are visible (if not a JS error is thrown)
            $('<input type="submit">').hide().appendTo(_htmlForm()).click().remove();
        }

        return valid;
    }

    function submit(serializationType, resetForm, success, failure) {
        var formToSubmit = serializeFor(serializationType, resetForm);

        if (_valid(formToSubmit)) {
            $.when($.get(getUrl(), formToSubmit)).then(success).fail(failure);
            return true;
        } else {
            return false;
        }
    }

    function submitQuery(query, success) {
        return $.when($.get(getUrl(), query)).then(success);
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
        serializeQueryFor: serializeQueryFor,
        serializeJsonFor: serializeJsonFor,
        serializeDiscoveryContextFor: serializeDiscoveryContextFor,
        id: getId,
        submit: submit,
        submitQuery: submitQuery
    };
};
