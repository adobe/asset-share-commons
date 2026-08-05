package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonOptionItemTest {

    @Test
    public void getters_selected() {
        JsonOptionItem jsonOptionItem = new JsonOptionItem("the text", "the value", true);

        assertEquals("the text", jsonOptionItem.getText());
        assertEquals("the value", jsonOptionItem.getValue());
        assertTrue(jsonOptionItem.isSelected());
    }

    @Test
    public void getters_unselected() {
        JsonOptionItem jsonOptionItem = new JsonOptionItem("another text", "another value", false);

        assertEquals("another text", jsonOptionItem.getText());
        assertEquals("another value", jsonOptionItem.getValue());
        assertFalse(jsonOptionItem.isSelected());
    }
}
