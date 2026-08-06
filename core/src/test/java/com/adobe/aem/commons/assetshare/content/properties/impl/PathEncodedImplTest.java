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

package com.adobe.aem.commons.assetshare.content.properties.impl;

import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PathEncodedImplTest {

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    PathEncodedImpl computedProperty;

    @Before
    public void setUp() throws Exception {
        final PathImpl pathImpl = ctx.registerInjectActivateService(new PathImpl());
        computedProperty = ctx.registerInjectActivateService(new PathEncodedImpl());

        // The DS-mock @Reference field injection only injects fields declared directly on the
        // registered class; since pathComputedProperty is declared on the UrlImpl super class,
        // wire it directly here (package-private access; test is in the same package).
        computedProperty.pathComputedProperty = pathImpl;
    }

    @Test
    public void getName() {
        assertEquals(PathEncodedImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(PathEncodedImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{}, computedProperty.getTypes());
    }

    @Test
    public void get() {
        when(asset.getPath()).thenReturn("/content/dam/my file.png");

        final String actual = computedProperty.get(asset, ctx.request());

        assertEquals("/content/dam/my%20file.png", actual);
    }
}
