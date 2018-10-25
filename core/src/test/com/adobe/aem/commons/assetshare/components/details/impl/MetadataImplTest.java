/*
 * Asset Share Commons
 *
 * Copyright (C) 2018 Adobe
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

package com.adobe.aem.commons.assetshare.components.details.impl;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MetadataImplTest {
    String COMBINED_PROPERTY_NAME = "test";

    @Mock
    ValueMap combinedProperties;

    @InjectMocks
    MetadataImpl metadataImpl;

    @Before
    public void setUp() throws Exception {
        metadataImpl  = new MetadataImpl();
        MockitoAnnotations.initMocks(this);
    }

    /** Empty Tests **/

    @Test
    public void isEmpty_EmptyPropertyName() {
        metadataImpl = spy(metadataImpl);

        doReturn(" ").when(metadataImpl).getPropertyName();
        assertTrue(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_NullPropertyName() {
        metadataImpl = spy(metadataImpl);

        doReturn(null).when(metadataImpl).getPropertyName();
        assertTrue(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_NullPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(null);

        assertTrue(metadataImpl.isEmpty());
    }


    @Test
    public void isEmpty_EmptyPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(" ");

        assertTrue(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_EmptyStringArrayPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(new String[]{});

        assertTrue(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_EmptyDateArrayPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(new Date[]{});

        assertTrue(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_StringArrayWithEmptyValuesPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(new String[]{ "", "  ", "      "});

        assertTrue(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_EmptyCollectionPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(new ArrayList(){});

        assertTrue(metadataImpl.isEmpty());
    }

    /** Not Empty Tests **/

    @Test
    public void isEmpty_NonNullStringPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn("Hello world");

        assertFalse(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_NonNullIntPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(100);

        assertFalse(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_NonEmptyStringArrayPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(new String[]{ "Hello", "world"});

        assertFalse(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_StringArrayWithMixedEmptyValuesPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(new String[]{ "", "  ", "      ", "hello world", " "});

        assertFalse(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_NonEmptyObjectArrayPropertyValue() {
        metadataImpl = spy(metadataImpl);

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(new Object[] { "Hello from", Calendar.getInstance(), 100});

        assertFalse(metadataImpl.isEmpty());
    }

    @Test
    public void isEmpty_NonEmptyCollectionPropertyValue() {
        metadataImpl = spy(metadataImpl);

        List<String> list = new ArrayList();
        list.add("Hello");
        list.add("world");

        doReturn(COMBINED_PROPERTY_NAME).when(metadataImpl).getPropertyName();
        when(combinedProperties.get(COMBINED_PROPERTY_NAME)).thenReturn(list);

        assertFalse(metadataImpl.isEmpty());
    }
}