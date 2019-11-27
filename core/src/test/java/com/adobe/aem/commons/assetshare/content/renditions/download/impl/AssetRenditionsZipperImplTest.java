package com.adobe.aem.commons.assetshare.content.renditions.download.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionStreamer;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsException;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsPacker;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionsZipperImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetRenditionsZipperImplTest.json"), "/content");

        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.png");

        ctx.registerService(AssetRenditionStreamer.class, new AssetRenditionStreamerImpl());

        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);
    }

    @Test
    public void accepts() {
        ctx.registerService(AssetRenditionsPacker.class, new AssetRenditionsZipperImpl());

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsPacker.class);

        ctx.request().setQueryString(AssetRenditionsPacker.REQUEST_PARAMETER_NAME + "=" + AssetRenditionsZipperImpl.NAME);

        assertTrue(zipper.accepts(ctx.request(), Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    public void getFileName() {
        final String expected = "Test Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "file.name", expected);

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsPacker.class);

        assertEquals(expected, zipper.getFileName());
    }

    @Test
    public void getFileName_Default() {
        final String expected = "Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl());

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsPacker.class);

        assertEquals(expected, zipper.getFileName());
    }

    @Test
    public void checkForMaxSize_Under() throws AssetRenditionsException {
        final String expected = "Test Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "max.size", 10000);

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsPacker.class);

        zipper.checkForMaxSize(200);
        assertTrue( "Exception should not be throw", true);
    }

    @Test(expected = AssetRenditionsException.class)
    public void checkForMaxSize_Over() throws AssetRenditionsException {
        final String expected = "Test Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "max.size", 10000);

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsPacker.class);

        zipper.checkForMaxSize((10000 * 1024) + 100);
        assertTrue( "Exception should not be throw", true);
    }

    @Test
    public void getZipEntryName() {
        final String expected = "test__my-rendition.png";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "rendition.filename.expression",
                AssetRenditionsZipperImpl.VAR_ASSET_NAME + "__" +  AssetRenditionsZipperImpl.VAR_RENDITION_NAME + "." + AssetRenditionsZipperImpl.VAR_ASSET_EXTENSION );

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsPacker.class);

        AssetModel asset = modelFactory.getModelFromWrappedRequest(ctx.request(), ctx.resourceResolver().getResource("/content/dam/test.png"), AssetModel.class);

        final String actual = zipper.getZipEntryName(asset, "my-rendition", "image/png");

        assertEquals( expected, actual);
    }

    @Test
    public void pack() throws IOException {
        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl());
        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsPacker.class);

        AssetModel asset = modelFactory.getModelFromWrappedRequest(ctx.request(), ctx.resourceResolver().getResource("/content/dam/test.png"), AssetModel.class);

        //zipper.pack(ctx.request(), ctx.response(), Arrays.asList(asset), Arrays.asList("original"));
    }

    @Test
    public void activate() {
    }
}