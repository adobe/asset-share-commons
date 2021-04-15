package com.adobe.aem.commons.assetshare.components.predicates;

import org.osgi.annotation.versioning.ConsumerType;

import java.util.List;

@ConsumerType
public interface FreeformTextPredicate extends Predicate {
    /**
     * @return the predicate title.
     */
    String getTitle();

    /**
     * The output of this is coupled with the ASC implementation of PropertyValuesPredicateEvaluator.
     *
     * @return a list of delimiters (or delimiter codes) used to split the entry.
     */
    List<String> getDelimiters();

    /**
     * @return the fields placeholder text
     */
    String getPlaceholder();

    /**
     * @return the number of rows the input text field should be.
     */
    int getRows();

    /**
     * @return the relative property path used for this predicate.
     */
    String getProperty();

    /**
     * @return the querybuilder predication operation (equals, not equals, exists)
     */
    String getOperation();

    /**
     * @return true of an operation is set.
     */
    boolean hasOperation();

    /**
     * @return the min input length allowed for this field, or null if no limit exists.
     */
    Integer getInputValidationMinLength();

    /**
     * @return the max input length allowed for this field, or null if no limit exists.
     */
    Integer getInputValidationMaxLength();

    /**
     * @return the pattern to validate input, or null if no pattern is to be used.
     */
    String getInputValidationPattern();

    /**
     * @return the message to display to the user if the provided input is invalid.
     */
    String getInputValidationMessage();
}
