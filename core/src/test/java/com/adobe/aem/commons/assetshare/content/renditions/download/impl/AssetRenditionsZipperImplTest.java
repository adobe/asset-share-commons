package com.adobe.aem.commons.assetshare.content.renditions.download.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsDownloadOrchestrator;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsException;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionDispatchersImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.StaticRenditionDispatcherImpl;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.io.IOUtils;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.testing.mock.sling.servlet.MockRequestDispatcherFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionsZipperImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    ModelFactory modelFactory;

    @Mock
    private RequestDispatcher requestDispatcher;

    @Mock
    HttpClientBuilderFactory httpClientBuilderFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("AssetRenditionsZipperImplTest.json"), "/content");

        // 1x1 pixel red png
        ctx.load().binaryFile(getClass().getResourceAsStream("AssetRenditionsZipperImplTest__original.png"),
                "/content/dam/test.png/jcr:content/renditions/original");

        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.png");

        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);

        ctx.registerService(AssetRenditionDispatchers.class, new AssetRenditionDispatchersImpl());

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());

        ctx.registerService(HttpClientBuilderFactory.class, httpClientBuilderFactory);

        ctx.registerInjectActivateService(
                new StaticRenditionDispatcherImpl(),
                ImmutableMap.<String, Object>builder().
                        put(Constants.SERVICE_RANKING, 0).
                        put("label", "Test AssetRenditionDispatcher").
                        put("name", "test").
                        put ("types", new String[]{"image", "video"}).
                        put("rendition.mappings", new String[]{ "test=original" }).
                        build());

        ctx.registerInjectActivateService(new AssetRenditionStreamerImpl());

        ctx.request().setRequestDispatcherFactory(new MockRequestDispatcherFactory() {
            @Override
            public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
                return requestDispatcher;
            }

            @Override
            public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
                return requestDispatcher;
            }
        });
    }

    @Test
    public void accepts() {
        ctx.registerService(AssetRenditionsDownloadOrchestrator.class, new AssetRenditionsZipperImpl());

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        ctx.request().setQueryString(AssetRenditionsDownloadOrchestrator.REQUEST_PARAMETER_NAME + "=" + AssetRenditionsZipperImpl.NAME);

        assertTrue(zipper.accepts(ctx.request(), Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    public void getFileName_FromResource() {
        final String expected = "My Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "file.name", "OSGi value");

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        assertEquals(expected, zipper.getFileName(new ValueMapDecorator(ImmutableMap.of("fileName", "My Assets"))));
    }

    @Test
    public void getFileName_FromOSGiConfig() {
        final String expected = "Test Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "file.name", expected);

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        assertEquals(expected, zipper.getFileName(new ValueMapDecorator(Collections.emptyMap())));
    }

    @Test
    public void getFileName_Default() {
        final String expected = "Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl());

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        assertEquals(expected, zipper.getFileName(new ValueMapDecorator(Collections.emptyMap())));
    }

    @Test
    public void checkForMaxSize_Under() throws AssetRenditionsException {
        final String expected = "Test Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "max.size", 10000);

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        zipper.checkForMaxSize(200);
        assertTrue( "Exception should not be throw", true);
    }

    @Test(expected = AssetRenditionsException.class)
    public void checkForMaxSize_Over() throws AssetRenditionsException {
        final String expected = "Test Assets.zip";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "max.size", 10000);

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        zipper.checkForMaxSize((10000 * 1024) + 100);
        assertTrue( "Exception should not be throw", true);
    }

    @Test
    public void getZipEntryName() {
        final String expected = "test__my-rendition.png";

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "rendition.filename.expression",
                AssetRenditionsZipperImpl.VAR_ASSET_NAME + "__" +  AssetRenditionsZipperImpl.VAR_RENDITION_NAME + "." + AssetRenditionsZipperImpl.VAR_ASSET_EXTENSION );

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        AssetModel asset = modelFactory.getModelFromWrappedRequest(ctx.request(), ctx.resourceResolver().getResource("/content/dam/test.png"), AssetModel.class);

        final String actual = zipper.getZipEntryName("", asset, "my-rendition", "image/png", new HashSet<>());

        assertEquals( expected, actual);
    }

    @Test
    public void getZipEntryName_GenerateUniqueZipFileNameEntry() {
        final String expected = "1-test__my-rendition.png";

        final Set<String> zipEntryFileNames = new HashSet<>();
        zipEntryFileNames.add("test__my-rendition.png");

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl(),
                "rendition.filename.expression",
                AssetRenditionsZipperImpl.VAR_ASSET_NAME + "__" +  AssetRenditionsZipperImpl.VAR_RENDITION_NAME + "." + AssetRenditionsZipperImpl.VAR_ASSET_EXTENSION );

        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        AssetModel asset = modelFactory.getModelFromWrappedRequest(ctx.request(), ctx.resourceResolver().getResource("/content/dam/test.png"), AssetModel.class);

        final String actual = zipper.getZipEntryName("", asset, "my-rendition", "image/png", zipEntryFileNames);

        assertEquals( expected, actual);
    }

    @Test
    public void pack() throws IOException, ServletException {
        final byte[] expectedOutputStream = IOUtils.toByteArray(this.getClass().getResourceAsStream("AssetRenditionsZipperImplTest__original.png"));
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            // Write some data to the response so we know that that requestDispatcher.include(..) was infact invoked.
            ((AssetRenditionDownloadResponse) args[1]).getOutputStream().write(expectedOutputStream);
            return null; // void method, return null
        }).when(requestDispatcher).include(any(SlingHttpServletRequest.class), any(SlingHttpServletResponse.class));

        ctx.registerInjectActivateService(new AssetRenditionsZipperImpl());
        AssetRenditionsZipperImpl zipper = (AssetRenditionsZipperImpl) ctx.getService(AssetRenditionsDownloadOrchestrator.class);

        ctx.currentResource("/content/default");

        AssetModel asset = modelFactory.getModelFromWrappedRequest(ctx.request(), ctx.resourceResolver().getResource("/content/dam/test.png"), AssetModel.class);

        zipper.execute(ctx.request(), ctx.response(), Arrays.asList(asset), Arrays.asList("test"));

        assertEquals("application/zip", ctx.response().getContentType());
        assertEquals(334,  ctx.response().getOutput().length);
    }
}