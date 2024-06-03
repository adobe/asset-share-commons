/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoreComponentsOptionItemTest {

    private SlingHttpServletRequest request;
    private Resource options;
    private Resource option;
    private ValueMap properties;
    private CoreComponentsOptionItem coreComponentsOptionItem;

    @Before
    public void setup() {
        request = mock(SlingHttpServletRequest.class);
        options = mock(Resource.class);
        option = mock(Resource.class);
        properties = mock(ValueMap.class);
        when(option.getValueMap()).thenReturn(properties);
        coreComponentsOptionItem = new CoreComponentsOptionItem(request, options, option);
    }

    @Test
    public void testGetText() {
        String expectedText = "test";
        when(properties.get("text", String.class)).thenReturn(expectedText);
        String actualText = coreComponentsOptionItem.getText();
        assertEquals(expectedText, actualText);
    }

    @Test
    public void testIsDisabled() {
        when(properties.get("disabled", false)).thenReturn(true);
        boolean actualDisabled = coreComponentsOptionItem.isDisabled();
        assertTrue(actualDisabled);
    }

    @Test
    public void testGetValue() {
        String expectedValue = "value";
        when(properties.get("value", String.class)).thenReturn(expectedValue);
        String actualValue = coreComponentsOptionItem.getValue();
        assertEquals(expectedValue, actualValue);
    }
}
