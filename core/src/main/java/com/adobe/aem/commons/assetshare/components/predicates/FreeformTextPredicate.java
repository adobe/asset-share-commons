package com.adobe.aem.commons.assetshare.components.predicates;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface FreeformTextPredicate extends Predicate {

    /**
     * @return the predicate title.
     */
    String getTitle();

    /**
     * @return a list of delimters used to split the entry.
     */
    String[] getDelimiters();

    /**
     * @return the fields placeholder text
     */
    String getPlaceholder();

    /**
     * @return the number of rows the input text field should be.
     */
    int getRows();
}
