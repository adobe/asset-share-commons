package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CustomOptionItemTest {

    @Test
    public void twoArgConstructor_getValue_returnsCustomValue() {
        CustomOptionItem customOptionItem = new CustomOptionItem("the value", "the custom value");

        assertEquals("the custom value", customOptionItem.getValue());
        assertNull(customOptionItem.getText());
        assertFalse(customOptionItem.isSelected());
        assertFalse(customOptionItem.isDisabled());
    }

    @Test
    public void twoArgConstructor_getValue_withBlankCustomValue_returnsValue() {
        CustomOptionItem customOptionItem = new CustomOptionItem("the value", "");

        assertEquals("the value", customOptionItem.getValue());
    }

    @Test
    public void fourArgConstructor_selected() {
        CustomOptionItem customOptionItem = new CustomOptionItem("the text", "the value", "the custom value", true);

        assertEquals("the text", customOptionItem.getText());
        assertEquals("the custom value", customOptionItem.getValue());
        assertTrue(customOptionItem.isSelected());
        assertFalse(customOptionItem.isDisabled());
    }

    @Test
    public void fourArgConstructor_unselected() {
        CustomOptionItem customOptionItem = new CustomOptionItem("the text", "the value", "the custom value", false);

        assertFalse(customOptionItem.isSelected());
    }
}
