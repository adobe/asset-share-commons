package com.adobe.aem.commons.assetshare.components.assetkit.impl;

import com.adobe.aem.commons.assetshare.components.assetkit.AssetKit;
import com.adobe.aem.commons.assetshare.components.details.EditorLinks;
import com.adobe.aem.commons.assetshare.components.details.impl.EditorLinksImpl;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperty;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.properties.impl.TitleImpl;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.assetkit.impl.AssetKitHelperImpl;
import com.adobe.aem.commons.assetshare.util.assetkit.impl.componentupdaters.AssetKitComponentUpdaterImpl;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import static com.adobe.aem.commons.assetshare.testing.MockAssetModels.mockModelFactory;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetKitImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    QueryBuilder queryBuilder;

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetKitImplTest.json"), "/content");

        mockModelFactory(ctx, modelFactory, "/content/dam/folder/banner.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/folder/test-1.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/folder/test-2.png");
        mockModelFactory(ctx, modelFactory, "/content/dam/folder/test-3.png");

        ctx.registerService(ModelFactory.class, modelFactory, Constants.SERVICE_RANKING, Integer.MAX_VALUE);

        ctx.registerService(QueryBuilder.class, queryBuilder);

        ctx.registerInjectActivateService(new TitleImpl());
        ctx.registerInjectActivateService(new AssetKitHelperImpl());
        ctx.registerInjectActivateService(new AssetKitComponentUpdaterImpl());

        ctx.addModelsForClasses(AssetKitImpl.class);
    }


    @Test
    public void getAssets() {
        ctx.currentResource("/content/page/jcr:content/root/responsivegrid/asset-kit");
        final AssetKit assetKit = ctx.request().adaptTo(AssetKit.class);

        assertEquals("AssetKit should have 4 assets", 4, assetKit.getAssets().size());
    }

    @Test
    public void getAssets_withFilter() {
        ctx.registerService(AssetKit.Filter.class, new AssetKitFilterImpl());

        ctx.currentResource("/content/page/jcr:content/root/responsivegrid/asset-kit");
        final AssetKit assetKit = ctx.request().adaptTo(AssetKit.class);

        assertEquals("AssetKit should have 3 assets", 3, assetKit.getAssets().size());
    }

    @Test
    public void isReady() {
        ctx.currentResource("/content/page/jcr:content/root/responsivegrid/asset-kit");
        final AssetKit assetKit = ctx.request().adaptTo(AssetKit.class);
        assertTrue("AssetKit should be ready", assetKit.isReady());
    }

    @Test
    public void isReady_NotReady() {
        ctx.currentResource("/content/page/jcr:content/root/responsivegrid/asset-kit-not-ready");
        final AssetKit assetKit = ctx.request().adaptTo(AssetKit.class);
        assertFalse("AssetKit should not be ready", assetKit.isReady());
    }

    @Test
    public void getExportedType() {
        assertEquals("asset-share-commons/components/asset-kit", new AssetKitImpl().getExportedType());
    }
}