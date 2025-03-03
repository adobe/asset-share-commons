package com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatcher;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import com.adobe.cq.dam.download.api.DownloadTarget;
import com.adobe.cq.wcm.spi.AssetDelivery;
import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetDeliveryRenditionDispatcherImplTest {
    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    MimeTypeService mimeTypeService;

    @Mock
    DownloadTarget downloadTarget;

    @Mock
    ModelFactory modelFactory;

    @Mock
    AssetDelivery assetDelivery;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetDeliveryRenditionDispatcherImplTest.json"), "/content");

        RequireAemMock.setAem(ctx,
                RequireAem.Distribution.CLOUD_READY,
                RequireAem.ServiceType.AUTHOR);

        ctx.addModelsForClasses(AssetModelImpl.class);

        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.png");
        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.oft");

        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);

        ctx.registerService(AssetDelivery.class, assetDelivery);
        ctx.registerService(MimeTypeService.class, mimeTypeService);
        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());

        ctx.registerInjectActivateService(new AssetRenditionsImpl());

        ctx.registerInjectActivateService(new AssetDeliveryRenditionDispatcherImpl(), "rendition.mappings", new String[]{"test=foo"});
    }

    @Test
    public void testAccepts_True() throws Exception {
        ctx.currentResource("/content/dam/test.png");
        AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        AssetRenditionDispatcher dispatcher = ctx.getService(AssetRenditionDispatcher.class);

        assertTrue("PNG should be accepted", dispatcher.accepts(assetModel, "test"));
    }

    @Test
    public void testAccepts_False() throws Exception {
        ctx.currentResource("/content/dam/test.oft");
        AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        AssetRenditionDispatcher dispatcher = ctx.getService(AssetRenditionDispatcher.class);

        assertFalse("OFT should not be accepted", dispatcher.accepts(assetModel, "test"));
    }


    @Test
    public void testAccepts_NoDcFormat_False() {
        ctx.currentResource("/content/dam/test.no-dc-format");
        AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        AssetRenditionDispatcher dispatcher = ctx.getService(AssetRenditionDispatcher.class);

        assertFalse("Missing dc:format should not be accepted", dispatcher.accepts(assetModel, "test"));
    }


    @Test
    public void testAccepts_BlankDcFormat_False() {
        ctx.currentResource("/content/dam/test.blank-dc-format");
        AssetModel assetModel = ctx.request().adaptTo(AssetModel.class);

        AssetRenditionDispatcher dispatcher = ctx.getService(AssetRenditionDispatcher.class);

        assertFalse("Blank dc:format should not be accepted", dispatcher.accepts(assetModel, "test"));
    }

    @Test
    public void testGetDeliveryURL_ValidExpression() {
        AssetDeliveryRenditionDispatcherImpl dispatcher = (AssetDeliveryRenditionDispatcherImpl) ctx.getService(AssetRenditionDispatcher.class);

        Asset asset = ctx.create().asset("/content/dam/test.png", 100, 100, "image/png");
        String expression = "path=/content/dam/test.png&format=webp&seoname=test-asset";
        when(assetDelivery.getDeliveryURL(eq(asset.adaptTo(Resource.class)), any())).thenReturn(asset.getPath());

        String result = dispatcher.getDeliveryURL(expression, asset);

        assertEquals(asset.getPath(), result);
    }

    @Test
    public void testGetDeliveryURL_MissingPath() {
        AssetDeliveryRenditionDispatcherImpl dispatcher = (AssetDeliveryRenditionDispatcherImpl) ctx.getService(AssetRenditionDispatcher.class);

        Asset asset = ctx.create().asset("/content/dam/test.png", 100, 100, "image/png");
        String expression = "format=webp&seoname=test-asset";

        String result = dispatcher.getDeliveryURL(expression, asset);

        assertNull(result);
    }
}