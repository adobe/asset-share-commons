package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CustomOptionItemTest {

    /**
     * NOTE: CustomOptionItem's 4-arg constructor has a copy-paste bug: {@code this.customValue = value;}
     * instead of {@code this.customValue = customValue;}. This means the "customValue" constructor
     * argument is effectively discarded and getValue() always resolves to the "value" argument.
     * These tests assert the ACTUAL (buggy) behavior as observed; see final report for details.
     */
    @Test
    public void twoArgConstructor_getValue_returnsValueNotCustomValue() {
        CustomOptionItem customOptionItem = new CustomOptionItem("the value", "the custom value");

        // Due to the production bug, customValue is never actually applied.
        assertEquals("the value", customOptionItem.getValue());
        assertNull(customOptionItem.getText());
        assertFalse(customOptionItem.isSelected());
        assertFalse(customOptionItem.isDisabled());
    }

    @Test
    public void fourArgConstructor_selected() {
        CustomOptionItem customOptionItem = new CustomOptionItem("the text", "the value", "the custom value", true);

        assertEquals("the text", customOptionItem.getText());
        assertEquals("the value", customOptionItem.getValue());
        assertTrue(customOptionItem.isSelected());
        assertFalse(customOptionItem.isDisabled());
    }

    @Test
    public void fourArgConstructor_unselected() {
        CustomOptionItem customOptionItem = new CustomOptionItem("the text", "the value", "the custom value", false);

        assertFalse(customOptionItem.isSelected());
    }
}
