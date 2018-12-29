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

/*global
    Granite, Coral
 */
(function (document, $, Coral) {
    "use strict";

    /**
     * Show / Hide the Property field based on the dialog dropdown value.
     *
     */

    function showHidePropertyDropdown(component, propertyTarget, analyzedPropertyTarget) {
        var propertyComponent = propertyTarget.find('.coral-Form-field').adaptTo('foundation-field'),
            analyzedPropertyComponent = analyzedPropertyTarget.find('.coral-Form-field').adaptTo('foundation-field'),
            analyzed = component.value !== 'equals';

        analyzedPropertyComponent.setValue(propertyComponent.getValue());
        propertyTarget.toggleClass('hide', analyzed);
        propertyComponent.setDisabled(analyzed);

        propertyComponent.setValue(analyzedPropertyComponent.getValue());
        analyzedPropertyTarget.toggleClass('hide',!analyzed);
        analyzedPropertyComponent.setDisabled(!analyzed);
    }

    $(document).on("foundation-contentloaded", function (e) {
        $(".cmp-search-freeform-text--editor coral-select.cq-dialog-operation__value", e.target).each(function (i, element) {
            var operationField = $(element),
                propertyFieldSet = operationField
                    .closest('.cmp-search-freeform-text--editor')
                    .find('.cq-dialog-property__field-set')
                    .closest('.coral-Form-fieldwrapper'),
            analyzedPropertyTarget = operationField
                    .closest('.cmp-search-freeform-text--editor')
                    .find('.cq-dialog-analyzed-property__field-set')
                    .closest('.coral-Form-fieldwrapper');

            if (operationField && propertyFieldSet && analyzedPropertyTarget) {
                Coral.commons.ready(element, function (component) {
                    showHidePropertyDropdown(component, propertyFieldSet, analyzedPropertyTarget);

                    component.on("change", function () {
                        showHidePropertyDropdown(component, propertyFieldSet, analyzedPropertyTarget);
                    });
                });
            }
        });
    });


    /**
     * Show / Hide the Delimiter Input field based on the dialog dropdown value.
     *
     * Only the Custom field shows the text input allowing for the author to type in custom values.
     */

    function showHideDelimiter(component, target) {
        target.toggleClass('hide', component.value !== '_CUSTOM');
    }

    $(document).on("foundation-contentloaded", function (e) {
        $(".cmp-search-freeform-text--editor coral-select.cq-dialog-delimiter__value", e.target).each(function (i, element) {
            var delimiterField = $(element),
                fieldSet = delimiterField.closest('.cq-dialog-delimiter__field-set'),
                customDelimiterField = fieldSet.find('.cq-dialog-delimiter__custom-value');

            if (fieldSet && customDelimiterField) {
                Coral.commons.ready(element, function (component) {
                    showHideDelimiter(component, customDelimiterField);
                    component.on("change", function () { showHideDelimiter(component, customDelimiterField); });
                });
            }
        });


        /**
         * Handles show and hide of the Input Validation Field based on the # of Rows.
         *
         * This is because input fields only support native HTML5 pattern validation which is when rows = 1.
         * Textareas do NOT support pattern validation; when rows > 1
         */

        function showHideValidation(component, target) {
            target.toggleClass('hide', component.value > 1);
        }

        $('.cmp-search-freeform-text--editor .cq-dialog-rows__field-set', e.target).each(function (i, element) {
            var rowsInputElement = $(element).find('input[type="number"]'),
                validationPatternFieldSet = rowsInputElement
                    .closest('.cmp-search-freeform-text--editor')
                    .find('.cq-dialog-validation-pattern__field-set')
                    .closest('.coral-Form-fieldwrapper'),
                validationMessageFieldSet = rowsInputElement
                    .closest('.cmp-search-freeform-text--editor')
                    .find('.cq-dialog-validation-message__field-set')
                    .closest('.coral-Form-fieldwrapper');


            if (rowsInputElement && validationPatternFieldSet && validationMessageFieldSet) {
                Coral.commons.ready(element, function (component) {
                    showHideValidation(component, validationPatternFieldSet);
                    showHideValidation(component, validationMessageFieldSet);

                    component.on("change", function () {
                        showHideValidation(component, validationPatternFieldSet);
                        showHideValidation(component, validationMessageFieldSet);
                    });
                });
            }
        });

    });
}(document, Granite.$, Coral));
