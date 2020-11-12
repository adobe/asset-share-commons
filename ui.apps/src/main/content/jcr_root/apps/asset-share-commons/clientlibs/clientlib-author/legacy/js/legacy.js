/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*global
    Granite, Coral
 */
(function (document, $, Coral) {
    "use strict";
    var CFW = '.coral-Form-fieldwrapper';

    function getFields(elements) {
        // Turn the selected elements into GraniteUI Fields
        return  elements.map(function(index, el) {
            var foundationField  = $(el).adaptTo('foundation-field');
            if (foundationField) {
                return {
                    type: $(el).data('granite-coral-multifield-name') ? 'collection' : 'field',
                    field: foundationField
                };
            }
        });
    }

    function getElements(dialog, fieldNames) {
        // Get create a selector from the parameters
        var selector = fieldNames.map(function(fieldName) {
            return '[name="' + fieldName + '"],[data-granite-coral-multifield-name="' + fieldName + '"]';
        }).join(',');

        // Turn the selected elements into GraniteUI Fields
        return  dialog.find(selector).map(function(index, el) {
            var field  = $(el).adaptTo('foundation-field');
            if (field) { return $(el); }
        });
    }

    function hasContent(fields) {
        var i,
            fieldType,
            field;

        for (i = 0; i < fields.length; i++) {
            fieldType = fields[i].type;
            field = fields[i].field;

            if (field.getValue()) {
                return true;
            } else if (field.getValues()) {
                if (fieldType === 'field' && field.getValues().length === 1 && field.getValues()[0]) {
                    return true;
                } else if (fieldType === 'collection' && field.getValues().length > 0) {
                    // Handle case of multifield lists
                    return true;
                }
            }
        }

        return false;
    }

    function toggleElement(element, visible) {
        element.closest(CFW).toggle(visible);
    }

    function toggleElements(modernElements, legacyElements, showLegacy) {
        modernElements.each(function(index, el) {
            toggleElement(el, !showLegacy);
        });

        legacyElements.each(function(index, el) {
            toggleElement(el, showLegacy);
        });
    }

    function parseFieldNameValues(values) {
        values = values || '';
        return values.split(',') || [];
    }

    $(document).on("foundation-contentloaded", function (e) {
        $(".asset-share-commons__dialog--legacy-support .asset-share-commons__legacy-mode", e.target).each(function (i, element) {

            var dialog = $(element).closest('.asset-share-commons__dialog--legacy-support'),
                legacyMode = $(element),
                legacyModeField = legacyMode.adaptTo('foundation-field'),
                modernFieldNames = parseFieldNameValues(legacyMode.data('modern-field-names')),
                legacyFieldNames = parseFieldNameValues(legacyMode.data('legacy-field-names')),
                modernElements = getElements(dialog, modernFieldNames) || [],
                legacyElements = getElements(dialog, legacyFieldNames) || [];

            Coral.commons.ready(legacyMode, function (legacyModeComponent) {

                var hasModernConfig = hasContent(getFields(modernElements)),
                    hasLegacyConfig = hasContent(getFields(legacyElements)),
                    initialStateIsLegacy = legacyModeField.getValue() === 'true' || (!hasModernConfig && hasLegacyConfig);

                // Add change event listener on legacyMode to toggle
                toggleElements(modernElements, legacyElements, initialStateIsLegacy);

                legacyModeField.setValue(initialStateIsLegacy ? 'true' : '');
                if (hasLegacyConfig) {
                    legacyModeComponent.on('change', function () {
                        toggleElements(modernElements, legacyElements, legacyModeField.getValue() === 'true');
                    }); // change
                } else {
                    toggleElement(legacyMode, false);
                }
            }); // Coral.commons.ready

        });
    });

}(document, Granite.$, Coral));