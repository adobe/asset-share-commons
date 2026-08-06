package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SortOptionItemTest {

    private SortOptionItem sortOptionItem;

    @Before
    public void setUp() {
        sortOptionItem = new SortOptionItem("the text", "the value", true);
    }

    @Test
    public void getText() {
        assertEquals("the text", sortOptionItem.getText());
    }

    @Test
    public void getValue() {
        assertEquals("the value", sortOptionItem.getValue());
    }

    @Test
    public void isCaseSensitive_true() {
        assertTrue(sortOptionItem.isCaseSensitive());
    }

    @Test
    public void isCaseSensitive_false() {
        SortOptionItem insensitive = new SortOptionItem("text", "value", false);
        assertFalse(insensitive.isCaseSensitive());
    }

    @Test
    public void isSelected_defaultsFalse() {
        assertFalse(sortOptionItem.isSelected());
    }

    @Test
    public void setSelected_updatesIsSelected() {
        sortOptionItem.setSelected(true);
        assertTrue(sortOptionItem.isSelected());

        sortOptionItem.setSelected(false);
        assertFalse(sortOptionItem.isSelected());
    }
}
