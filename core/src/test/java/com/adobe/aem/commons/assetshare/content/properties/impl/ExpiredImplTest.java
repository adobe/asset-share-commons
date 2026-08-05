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

import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpiredImplTest {

    private static final String EXPIRATION_DATE_KEY = "jcr:content/metadata/prism:expirationDate";

    AemContext ctx = new AemContext();

    @Mock
    Asset asset;

    @Mock
    Resource resource;

    @Mock
    ValueMap valueMap;

    ComputedProperty<Boolean> computedProperty;

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new ExpiredImpl());
        computedProperty = ctx.getService(ComputedProperty.class);

        when(asset.adaptTo(Resource.class)).thenReturn(resource);
        when(resource.adaptTo(ValueMap.class)).thenReturn(valueMap);
    }

    @Test
    public void getName() {
        assertEquals(ExpiredImpl.NAME, computedProperty.getName());
    }

    @Test
    public void getLabel() {
        assertEquals(ExpiredImpl.LABEL, computedProperty.getLabel());
    }

    @Test
    public void getTypes() {
        assertArrayEquals(new String[]{ComputedProperty.Types.METADATA}, computedProperty.getTypes());
    }

    @Test
    public void get_WithNullAsset() {
        final Boolean actual = computedProperty.get(null, ctx.request());

        assertFalse(actual);
    }

    @Test
    public void get_WithExpirationDateInThePast() {
        final Calendar past = Calendar.getInstance();
        past.add(Calendar.YEAR, -1);
        when(valueMap.get(EXPIRATION_DATE_KEY, Calendar.class)).thenReturn(past);

        final Boolean actual = computedProperty.get(asset, ctx.request());

        assertTrue(actual);
    }

    @Test
    public void get_WithExpirationDateInTheFuture() {
        final Calendar future = Calendar.getInstance();
        future.add(Calendar.YEAR, 1);
        when(valueMap.get(EXPIRATION_DATE_KEY, Calendar.class)).thenReturn(future);

        final Boolean actual = computedProperty.get(asset, ctx.request());

        assertFalse(actual);
    }

    @Test
    public void get_WithNoExpirationDate() {
        when(valueMap.get(EXPIRATION_DATE_KEY, Calendar.class)).thenReturn(null);

        final Boolean actual = computedProperty.get(asset, ctx.request());

        assertFalse(actual);
    }
}
