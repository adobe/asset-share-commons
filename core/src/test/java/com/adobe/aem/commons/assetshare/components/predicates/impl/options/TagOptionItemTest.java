package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import com.day.cq.tagging.Tag;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TagOptionItemTest {

    @Test
    public void getValue_withTag() {
        Tag tag = mock(Tag.class);
        when(tag.getTagID()).thenReturn("namespace:tag-id");

        TagOptionItem tagOptionItem = new TagOptionItem(tag, Locale.US, true);

        assertEquals("namespace:tag-id", tagOptionItem.getValue());
    }

    @Test
    public void getText_withTag() {
        Tag tag = mock(Tag.class);
        when(tag.getTitle(Locale.US)).thenReturn("Tag Title");

        TagOptionItem tagOptionItem = new TagOptionItem(tag, Locale.US, false);

        assertEquals("Tag Title", tagOptionItem.getText());
    }

    @Test
    public void getValue_nullTag() {
        TagOptionItem tagOptionItem = new TagOptionItem(null, Locale.US, false);

        assertEquals("", tagOptionItem.getValue());
    }

    @Test
    public void getText_nullTag() {
        TagOptionItem tagOptionItem = new TagOptionItem(null, Locale.US, false);

        assertEquals("", tagOptionItem.getText());
    }

    @Test
    public void isSelected_true() {
        TagOptionItem tagOptionItem = new TagOptionItem(null, Locale.US, true);

        assertTrue(tagOptionItem.isSelected());
    }

    @Test
    public void isSelected_false() {
        TagOptionItem tagOptionItem = new TagOptionItem(null, Locale.US, false);

        assertFalse(tagOptionItem.isSelected());
    }

    @Test
    public void isDisabled_alwaysFalse() {
        TagOptionItem tagOptionItem = new TagOptionItem(null, Locale.US, false);

        assertFalse(tagOptionItem.isDisabled());
    }
}
