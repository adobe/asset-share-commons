/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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

package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.WCMMode;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetDetails404ServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    AssetResolver assetResolver;

    private AssetDetails404Servlet servlet;

    @Before
    public void setUp() throws Exception {
        ctx.registerService(AssetResolver.class, assetResolver);
        servlet = ctx.registerInjectActivateService(new AssetDetails404Servlet());
    }

    @Test
    public void doGet_sends404() throws Exception {
        servlet.doGet(ctx.request(), ctx.response());

        assertEquals(404, ctx.response().getStatus());
        assertEquals("text/html;charset=UTF-8", ctx.response().getContentType());
    }

    @Test
    public void accepts_disabledModeAndNoAssetResolved_returnsTrue() {
        WCMMode.DISABLED.toRequest(ctx.request());
        when(assetResolver.resolveAsset(ctx.request())).thenReturn(null);

        assertTrue(servlet.accepts(ctx.request()));
    }

    @Test
    public void accepts_disabledModeAndAssetResolved_returnsFalse() {
        WCMMode.DISABLED.toRequest(ctx.request());
        when(assetResolver.resolveAsset(ctx.request())).thenReturn(mockAsset());

        assertFalse(servlet.accepts(ctx.request()));
    }

    @Test
    public void accepts_disabledModeAndIllegalArgumentException_returnsTrue() {
        WCMMode.DISABLED.toRequest(ctx.request());
        when(assetResolver.resolveAsset(ctx.request())).thenThrow(new IllegalArgumentException("bad path"));

        assertTrue(servlet.accepts(ctx.request()));
    }

    @Test
    public void accepts_editMode_returnsFalse() {
        WCMMode.EDIT.toRequest(ctx.request());

        assertFalse(servlet.accepts(ctx.request()));
    }

    private Asset mockAsset() {
        return org.mockito.Mockito.mock(Asset.class);
    }
}
