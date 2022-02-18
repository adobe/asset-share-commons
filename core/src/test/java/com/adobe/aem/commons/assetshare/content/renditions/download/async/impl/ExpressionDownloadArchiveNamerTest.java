package com.adobe.aem.commons.assetshare.content.renditions.download.async.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.content.renditions.download.DownloadExtensionResolver;
import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadArchiveNamer;
import com.adobe.aem.commons.assetshare.content.renditions.download.async.DownloadTargetParameters;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import com.adobe.cq.dam.download.api.DownloadTarget;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionDownloadArchiveNamerTest {
    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    MimeTypeService mimeTypeService;

    @Mock
    DownloadTarget downloadTarget;

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("ExpressionDownloadArchiveNamerTest.json"), "/content");

        ctx.addModelsForClasses(AssetModelImpl.class);

        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.png");
        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);


        ctx.registerService(MimeTypeService.class, mimeTypeService);
        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());

        ctx.registerInjectActivateService(new AssetRenditionsImpl());

        when(downloadTarget.getParameter(DownloadTargetParameters.DOWNLOAD_COMPONENT_PATH.toString(), String.class)).thenReturn("/content/download-component");
        when(downloadTarget.getParameter(DownloadTargetParameters.RENDITION_NAME.toString(), String.class)).thenReturn("Test Web");

        ctx.currentResource("/content/dam/test.png");
    }

    @Test
    public void getArchiveFilePath() {
        ctx.registerInjectActivateService(new ExpressionDownloadArchiveNamer());

        DownloadArchiveNamer namer = ctx.getService(DownloadArchiveNamer.class);

        AssetModel assetModel = modelFactory.getModelFromWrappedRequest(ctx.request(), ctx.resourceResolver().getResource("/content/dam/test.png"), AssetModel.class);
        AssetRendition assetRendition = new AssetRendition("/content/dam/test.png/jcr:renditions/cq5dam.web.1280.1280.png", 10l, "image/png");

        assertEquals("test/test (Test Web).png", namer.getArchiveFilePath(assetModel, assetRendition, downloadTarget));
    }

    @Test
    public void getArchiveFilePath_withDownloadExtensionResolver() {
        ctx.registerService(DownloadExtensionResolver.class, new TestDownloadExtensionResolver());
        ctx.registerInjectActivateService(new ExpressionDownloadArchiveNamer());

        DownloadArchiveNamer namer = ctx.getService(DownloadArchiveNamer.class);

        AssetModel assetModel = modelFactory.getModelFromWrappedRequest(ctx.request(), ctx.resourceResolver().getResource("/content/dam/test.png"), AssetModel.class);
        AssetRendition assetRendition = new AssetRendition("/content/dam/test.png/jcr:renditions/cq5dam.web.1280.1280.png", 10l, "image/png");

        assertEquals("test/test (Test Web).custom", namer.getArchiveFilePath(assetModel, assetRendition, downloadTarget));
    }


    class TestDownloadExtensionResolver implements DownloadExtensionResolver {

        @Override
        public String resolve(AssetModel assetModel, AssetRendition assetRendition) {

            if ("image/png".equals(assetRendition.getMimeType())) {
                return "custom";
             }

            return null;
        }
    }
}