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

package com.adobe.aem.commons.assetshare.components.actions.share.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.share.Share;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ShareImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private ActionHelper actionHelper;

    @Mock
    private AssetModel asset1;

    @Before
    public void setUp() {
        ctx.registerService(ActionHelper.class, actionHelper);
        ctx.addModelsForClasses(ShareImpl.class);
    }

    @Test
    public void getAssets_fromQueryParameter() {
        doReturn("/content/dam/asset-1.png").when(asset1).getPath();

        final List<AssetModel> assets = new ArrayList<>();
        assets.add(asset1);
        doReturn(assets).when(actionHelper).getAssetsFromQueryParameter(eq(ctx.request()), eq("path"));

        final Share share = ctx.request().adaptTo(Share.class);

        assertEquals(1, share.getAssets().size());
        assertTrue(share.getPaths().contains("/content/dam/asset-1.png"));
    }

    @Test
    public void getAssets_placeholderWhenNoAssets() {
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(eq(ctx.request()), eq("path"));

        final List<AssetModel> placeholder = new ArrayList<>();
        doReturn("/content/dam/placeholder.png").when(asset1).getPath();
        placeholder.add(asset1);
        doReturn(placeholder).when(actionHelper).getPlaceholderAsset(any());

        final Share share = ctx.request().adaptTo(Share.class);

        assertEquals(1, share.getAssets().size());
        assertTrue(share.getPaths().contains("/content/dam/placeholder.png"));
    }
}
