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

package com.adobe.aem.commons.assetshare.components.actions.download.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.AssetDownloadHelper;
import com.adobe.aem.commons.assetshare.components.actions.download.Download;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DownloadImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private ActionHelper actionHelper;

    @Mock
    private AssetDownloadHelper assetDownloadHelper;

    @Mock
    private ModelFactory modelFactory;

    @Mock
    private AssetRenditionDispatchers assetRenditionDispatchers;

    @Mock
    private AssetModel asset1;

    @Before
    public void setUp() {
        ctx.registerService(ActionHelper.class, actionHelper);
        ctx.registerService(AssetDownloadHelper.class, assetDownloadHelper);
        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        ctx.registerService(AssetRenditionDispatchers.class, assetRenditionDispatchers);
        ctx.addModelsForClasses(DownloadImpl.class);

        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getPlaceholderAsset(any());
    }

    private void createDownloadResource(final String zipFileName) {
        ctx.create().resource("/content/download",
                "sling:resourceType", "asset-share-commons/components/modals/download",
                "zipFileName", zipFileName);
        ctx.currentResource("/content/download");
    }

    @Test
    public void init_noAssets_usesPlaceholder() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");

        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));
        final List<AssetModel> placeholder = new ArrayList<>();
        placeholder.add(asset1);
        doReturn(placeholder).when(actionHelper).getPlaceholderAsset(any());

        final Download download = ctx.request().adaptTo(Download.class);

        assertEquals(1, download.getAssets().size());
        verify(assetDownloadHelper, never()).getAssetDownloadSize(any(), any());
    }

    @Test
    public void init_withAssets_classicDistribution_calculatesSizes() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");

        final List<AssetModel> assets = new ArrayList<>();
        assets.add(asset1);
        doReturn(assets).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        doReturn(1000L).when(assetDownloadHelper).getMaxContentSizeLimit();
        doReturn(500L).when(assetDownloadHelper).getAssetDownloadSize(eq(assets), any());

        final Download download = ctx.request().adaptTo(Download.class);

        assertEquals(1000L, download.getMaxContentSize());
        assertEquals(500L, download.getDownloadContentSize());
        verify(assetDownloadHelper).getAssetDownloadSize(eq(assets), any());

        assertNotNull(download.getMaxContentSizeLabel());
        assertNotNull(download.getDownloadContentSizeLabel());
    }

    @Test
    public void init_withAssets_classicDistribution_noSizeLimitSkipsCalculation() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");

        final List<AssetModel> assets = new ArrayList<>();
        assets.add(asset1);
        doReturn(assets).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        doReturn(-1L).when(assetDownloadHelper).getMaxContentSizeLimit();

        final Download download = ctx.request().adaptTo(Download.class);

        assertEquals(-1L, download.getMaxContentSize());
        assertEquals(-1L, download.getDownloadContentSize());
        verify(assetDownloadHelper, never()).getAssetDownloadSize(any(), any());
    }

    @Test
    public void init_withAssets_classicDistribution_npeIsCaught() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");

        final List<AssetModel> assets = new ArrayList<>();
        assets.add(asset1);
        doReturn(assets).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        doThrow(new NullPointerException("Dynamic Media issue")).when(assetDownloadHelper).getMaxContentSizeLimit();

        // Should not throw; the NPE is caught internally.
        final Download download = ctx.request().adaptTo(Download.class);

        assertEquals(1, download.getAssets().size());
    }

    @Test
    public void init_withAssets_cloudReadyDistribution_doesNotCalculateSizes() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");

        final List<AssetModel> assets = new ArrayList<>();
        assets.add(asset1);
        doReturn(assets).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        final Download download = ctx.request().adaptTo(Download.class);

        assertEquals(1, download.getAssets().size());
        verify(assetDownloadHelper, never()).getAssetDownloadSize(any(), any());
        assertTrue(download.isAsynchronous());
    }

    @Test
    public void isAsynchronous_classicIsFalse() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        final Download download = ctx.request().adaptTo(Download.class);

        assertFalse(download.isAsynchronous());
    }

    @Test
    public void getZipFileName_removesZipSuffix() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("MyAssets.ZIP");
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        final Download download = ctx.request().adaptTo(Download.class);

        assertEquals("MyAssets", download.getZipFileName());
    }

    @Test
    public void getExportedType() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        final DownloadImpl download = (DownloadImpl) ctx.request().adaptTo(Download.class);

        assertEquals(DownloadImpl.RESOURCE_TYPE, download.getExportedType());
    }

    @Test
    public void isLegacyMode_explicitTrue() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        ctx.create().resource("/content/download",
                "sling:resourceType", "asset-share-commons/components/modals/download",
                "zipFileName", "Assets",
                "legacyMode", true);
        ctx.currentResource("/content/download");
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        final Download download = ctx.request().adaptTo(Download.class);

        assertTrue(download.isLegacyMode());
    }

    @Test
    public void isLegacyMode_nullWithNoGroupsAndNoExcludeOriginal_isFalse() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        final Download download = ctx.request().adaptTo(Download.class);

        assertFalse(download.isLegacyMode());
    }

    @Test
    public void isLegacyMode_nullWithExcludeOriginalAssetsSet_isTrue() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        ctx.create().resource("/content/download",
                "sling:resourceType", "asset-share-commons/components/modals/download",
                "zipFileName", "Assets",
                "excludeOriginalAssets", true);
        ctx.currentResource("/content/download");
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        final Download download = ctx.request().adaptTo(Download.class);

        assertTrue(download.isLegacyMode());
    }

    @Test
    public void getAssetRenditionsGroups_noGroupsConfigured_returnsEmptyList() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        final Download download = ctx.request().adaptTo(Download.class);

        assertTrue(download.getAssetRenditionsGroups().isEmpty());
    }

    @Test
    public void getAssetRenditionsGroups_filtersInvalidRenditionNamesAndEmptyGroups() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        createDownloadResource("Assets");
        doReturn(new ArrayList<AssetModel>()).when(actionHelper).getAssetsFromQueryParameter(any(), eq("path"));

        ctx.create().resource("/content/download/asset-renditions-groups");
        ctx.create().resource("/content/download/asset-renditions-groups/items");
        ctx.create().resource("/content/download/asset-renditions-groups/items/group1",
                "assetRenditionsGroupTitle", "Web Sizes");
        final Resource group1Renditions = ctx.create().resource("/content/download/asset-renditions-groups/items/group1/asset-renditions");

        ctx.create().resource("/content/download/asset-renditions-groups/items/group2",
                "assetRenditionsGroupTitle", "Empty Group");
        final Resource group2Renditions = ctx.create().resource("/content/download/asset-renditions-groups/items/group2/asset-renditions");

        final OptionItem validItem = mockOptionItem("original", "Original");
        final OptionItem invalidItem = mockOptionItem("invalid", "Invalid");
        final Options group1Options = mockOptions(Arrays.asList(validItem, invalidItem));
        doReturn(group1Options).when(modelFactory).getModelFromWrappedRequest(any(), pathMatches(group1Renditions.getPath()), eq(Options.class));

        final OptionItem onlyInvalidItem = mockOptionItem("not-valid", "Not Valid");
        final Options group2Options = mockOptions(Arrays.asList(onlyInvalidItem));
        doReturn(group2Options).when(modelFactory).getModelFromWrappedRequest(any(), pathMatches(group2Renditions.getPath()), eq(Options.class));

        doReturn(true).when(assetRenditionDispatchers).isValidAssetRenditionName("original");
        doReturn(false).when(assetRenditionDispatchers).isValidAssetRenditionName("invalid");
        doReturn(false).when(assetRenditionDispatchers).isValidAssetRenditionName("not-valid");

        final Download download = ctx.request().adaptTo(Download.class);

        final List<Download.AssetRenditionsGroup> groups = download.getAssetRenditionsGroups();
        assertEquals(1, groups.size());
        assertEquals("Web Sizes", groups.get(0).getTitle());
        assertEquals(1, groups.get(0).getItems().size());
        assertEquals("original", groups.get(0).getItems().get(0).getValue());

        // legacyMode is not set on the resource; with non-empty asset renditions groups present,
        // isLegacyMode() should assume "modern" (false).
        assertFalse(download.isLegacyMode());
    }

    private Resource pathMatches(final String path) {
        return argThat(resource -> resource != null && path.equals(resource.getPath()));
    }

    private OptionItem mockOptionItem(final String value, final String text) {
        final OptionItem item = org.mockito.Mockito.mock(OptionItem.class);
        doReturn(value).when(item).getValue();
        return item;
    }

    private Options mockOptions(final List<OptionItem> items) {
        final Options options = org.mockito.Mockito.mock(Options.class);
        doReturn(items).when(options).getItems();
        return options;
    }
}
