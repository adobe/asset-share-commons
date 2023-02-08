package com.adobe.aem.commons.assetshare.util.assetkit.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.assetkit.AssetKitHelper;
import com.day.cq.search.QueryBuilder;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.resource.collection.ResourceCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import java.util.Arrays;
import java.util.Collection;

import static com.adobe.aem.commons.assetshare.testing.MockAssetModels.mockModelFactory;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetKitHelperImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    QueryBuilder queryBuilder;

    @Mock
    ModelFactory modelFactory;

    AssetKitHelper assetKitHelper;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetKitHelperImplTest.json"), "/content/dam");

        mockModelFactory(ctx, modelFactory, "/content/dam/ntFolder/test-1.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/ntFolder/test-2.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/folder/test-3.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/folder/test-4.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/orderedFolder/test-5.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/orderedFolder/test-6.png");

        ctx.registerService(ModelFactory.class, modelFactory, Constants.SERVICE_RANKING, Integer.MAX_VALUE);

        ctx.registerService(QueryBuilder.class, queryBuilder);

        ctx.registerInjectActivateService(new AssetKitHelperImpl());

        assetKitHelper = ctx.getService(AssetKitHelper.class);
    }

    @Test
    public void getAssets() {
        final Resource resource = ctx.resourceResolver().getResource("/content/dam/ntFolder");

        final Collection<? extends AssetModel> assets = assetKitHelper.getAssets(new Resource[]{resource});

        assertEquals(2, assets.size());
    }

    @Test
    public void getAssetsFromAssetCollection() {
        ResourceCollection collection = mock(ResourceCollection.class);
        when(collection.getResources()).thenReturn(Arrays.stream(new Resource[]{
                ctx.resourceResolver().getResource("/content/dam/ntFolder/test-1.png"),
                ctx.resourceResolver().getResource("/content/dam/folder/test-3.png"),
                ctx.resourceResolver().getResource("/content/dam/orderedFolder/test-5.png"),
        }).iterator());

        Resource collectionResource = spy(ctx.resourceResolver().getResource("/content/dam/collections/my-collection"));
        when(collectionResource.adaptTo(ResourceCollection.class)).thenReturn(collection);

        final Collection<? extends AssetModel> assets = assetKitHelper.getAssets(new Resource[]{collectionResource});

        assertEquals(3, assets.size());
    }

    @Test
    public void getAssetsFromAssetFolder() {
        final Resource resource = ctx.resourceResolver().getResource("/content/dam/folder");

        final Collection<? extends AssetModel> assets = assetKitHelper.getAssets(new Resource[]{resource});

        assertEquals(2, assets.size());
    }

    @Test
    public void getAsset_True() {
        final String expected = "/content/dam/ntFolder/test-2.png";

        AssetModel actual = assetKitHelper.getAsset(ctx.resourceResolver().getResource(expected));
        assertEquals(expected, actual.getPath());

        assertNull(assetKitHelper.getAsset(ctx.resourceResolver().getResource("/content/dam/ntFolder")));
    }

    @Test
    public void isAssetFolder() {
        assertTrue(assetKitHelper.isAssetFolder(ctx.resourceResolver().getResource("/content/dam/ntFolder")));
        assertTrue(assetKitHelper.isAssetFolder(ctx.resourceResolver().getResource("/content/dam/folder")));
        assertTrue(assetKitHelper.isAssetFolder(ctx.resourceResolver().getResource("/content/dam/orderedFolder")));

        assertFalse(assetKitHelper.isAssetFolder(ctx.resourceResolver().getResource("/content/dam/collections")));
        assertFalse(assetKitHelper.isAssetFolder(ctx.resourceResolver().getResource("/content/dam/ntFolder/test-1.png")));
    }

    @Test
    public void isAssetCollection() {
        // AEM Mocks/Sling mocks do not support ResourceCollections
        Resource collectionResource = spy(ctx.resourceResolver().getResource("/content/dam/collections/my-collection"));
        when(collectionResource.adaptTo(ResourceCollection.class)).thenReturn(mock(ResourceCollection.class));
        assertTrue(assetKitHelper.isAssetCollection(collectionResource));

        assertFalse(assetKitHelper.isAssetCollection(ctx.resourceResolver().getResource("/content/dam/ntFolder")));
        assertFalse(assetKitHelper.isAssetCollection(ctx.resourceResolver().getResource("/content/dam/folder")));
        assertFalse(assetKitHelper.isAssetCollection(ctx.resourceResolver().getResource("/content/dam/orderedFolder")));
        assertFalse(assetKitHelper.isAssetCollection(ctx.resourceResolver().getResource("/content/dam/ntFolder/test-1.png")));
    }

    @Test
    public void isAsset() {
        assertTrue(assetKitHelper.isAsset(ctx.resourceResolver().getResource("/content/dam/ntFolder/test-2.png")));

        assertFalse(assetKitHelper.isAsset(ctx.resourceResolver().getResource("/content/dam/ntFolder")));
        assertFalse(assetKitHelper.isAsset(ctx.resourceResolver().getResource("/content/dam/folder")));
        assertFalse(assetKitHelper.isAsset(ctx.resourceResolver().getResource("/content/dam/orderedFolder")));
        assertFalse(assetKitHelper.isAsset(ctx.resourceResolver().getResource("/content/dam/collections")));
    }
}