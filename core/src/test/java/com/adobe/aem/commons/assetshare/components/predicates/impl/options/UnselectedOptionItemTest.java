/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.components.predicates.impl.options;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnselectedOptionItemTest {

    private OptionItem wrapped;
    private UnselectedOptionItem unselectedOptionItem;

    @Before
    public void setUp() {
        wrapped = mock(OptionItem.class);
        when(wrapped.getText()).thenReturn("the text");
        when(wrapped.getValue()).thenReturn("the value");
        when(wrapped.isDisabled()).thenReturn(true);
        when(wrapped.isSelected()).thenReturn(true);

        unselectedOptionItem = new UnselectedOptionItem(wrapped);
    }

    @Test
    public void isSelected_alwaysFalse() {
        assertFalse(unselectedOptionItem.isSelected());
    }

    @Test
    public void isDisabled_delegatesToWrapped() {
        assertTrue(unselectedOptionItem.isDisabled());
    }

    @Test
    public void getValue_delegatesToWrapped() {
        assertEquals("the value", unselectedOptionItem.getValue());
    }

    @Test
    public void getText_delegatesToWrapped() {
        assertEquals("the text", unselectedOptionItem.getText());
    }
}
