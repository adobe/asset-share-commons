package com.adobe.aem.commons.assetshare.components.actions.download.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionDispatchersImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.dispatchers.ExternalRedirectRenditionDispatcherImpl;
import com.adobe.aem.commons.assetshare.testhelpers.TestOptionsImpl;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;
import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.components.actions.AssetDownloadHelper;
import com.adobe.aem.commons.assetshare.components.actions.download.Download;
import com.adobe.aem.commons.assetshare.content.AssetModel;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class DownloadImplTest {

	@Rule
	public final AemContext ctx = new AemContext();

	@Mock
	ActionHelper actionHelper;

	@Mock
	AssetDownloadHelper assetDownloadHelper;

	@Mock
	ModelFactory modelFactory;

	@Mock
	AssetModel asset1;

	@Mock
	AssetModel asset2;

	private Collection<AssetModel> assetModels;

	@Before
	public void setUp() throws Exception {
		ctx.load().json("/com/adobe/aem/commons/assetshare/components/actions/download/impl/DownloadImplTest.json",
				"/content");

		assetModels = new ArrayList<>();
		assetModels.add(asset1);
		assetModels.add(asset2);

		doReturn(assetModels).when(actionHelper).getAssetsFromQueryParameter(ctx.request(), "path");
		doReturn(1024L).when(assetDownloadHelper).getMaxContentSizeLimit();

		RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.PUBLISH);

		ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());
		ctx.registerService(AssetRenditionDispatchers.class, new AssetRenditionDispatchersImpl());

		ctx.registerService(ActionHelper.class, actionHelper, Constants.SERVICE_RANKING, Integer.MAX_VALUE);
		ctx.registerService(AssetDownloadHelper.class, assetDownloadHelper, Constants.SERVICE_RANKING,
				Integer.MAX_VALUE);
        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);
		ctx.addModelsForClasses(Download.class);
    }

	@Test
	public void getAssets() {

		ctx.currentResource("/content/download");
		final Download download = ctx.request().adaptTo(Download.class);

		final Collection<AssetModel> models = download.getAssets();

		assertNotNull(models);
		assertFalse(models.isEmpty());
		assertEquals(2, models.size());
	}

	@Test
	public void getAssets_empty() {
		doReturn(new ArrayList<>()).when(actionHelper).getAssetsFromQueryParameter(ctx.request(), "path");

		ctx.currentResource("/content/download");
		final Download download = ctx.request().adaptTo(Download.class);

		final Collection<AssetModel> models = download.getAssets();

		assertNotNull(models);
		assertTrue(models.isEmpty());
	}

	@Test
	public void getZipFileName() {
		final String expected = "myassets";

		ctx.currentResource("/content/download");
		final Download download = ctx.request().adaptTo(Download.class);

		assertEquals(expected, download.getZipFileName());
	}

	@Test
	public void getZipFileName_Default() {
		final String expected = "Assets";

		ctx.currentResource("/content/download_empty_zip_name");
		final Download download = ctx.request().adaptTo(Download.class);

		assertEquals(expected, download.getZipFileName());
	}

	@Test
	public void getZipFileName_WithExtension() {
		final String expected = "myassets";

		ctx.currentResource("/content/download_with_zip_extension");
		final Download download = ctx.request().adaptTo(Download.class);

		assertEquals(expected, download.getZipFileName());
	}

	@Test
	public void getMaxContentSize() {
		long expected = 1024L;
		ctx.currentResource("/content/download");
		final Download download = ctx.request().adaptTo(Download.class);

		assertEquals(expected, download.getMaxContentSize());
	}

	@Test
	public void getMaxContentSize_EmptyAssets() {
		long expected = -1L;
		doReturn(new ArrayList<>()).when(actionHelper).getAssetsFromQueryParameter(ctx.request(), "path");

		ctx.currentResource("/content/download");
		final Download download = ctx.request().adaptTo(Download.class);

		assertEquals(expected, download.getMaxContentSize());
	}

	@Test
	public void getDownloadContentSize() {
		long expected = 1024L;
		ctx.currentResource("/content/download");
		doReturn(1024L).when(assetDownloadHelper).getAssetDownloadSize(assetModels, ctx.currentResource());
		final Download download = ctx.request().adaptTo(Download.class);

		assertEquals(expected, download.getDownloadContentSize());
	}

	@Test
	public void getDownloadContentSize_EmptyAssets() {
		long expected = -1L;
		doReturn(new ArrayList<>()).when(actionHelper).getAssetsFromQueryParameter(ctx.request(), "path");

		ctx.currentResource("/content/download");
		final Download download = ctx.request().adaptTo(Download.class);

		assertTrue(download.getAssets().isEmpty());
		assertEquals(expected, download.getDownloadContentSize());
	}

	@Test
	public void getDownloadContentSize_NoMaxLimit() {
		long expected = -1L;
		doReturn(-1L).when(assetDownloadHelper).getMaxContentSizeLimit();
		ctx.currentResource("/content/download");
		final Download download = ctx.request().adaptTo(Download.class);

		assertFalse(download.getAssets().isEmpty());
		assertEquals(expected, download.getDownloadContentSize());
	}

	@Test
	public void isLegacyMode_Yes() {
		ctx.currentResource("/content/download_legacy");
		final Download download = ctx.request().adaptTo(Download.class);

		assertTrue("Should be legacy mode", ((DownloadImpl)download).isLegacyMode());
	}

	@Test
	public void isLegacyMode_Yes_WithMode() {
		ctx.currentResource("/content/download_legacy_mode");
		final Download download = ctx.request().adaptTo(Download.class);

		assertTrue("Should be legacy mode", ((DownloadImpl)download).isLegacyMode());
	}

    @Test
	public void isLegacyMode_No() {
		ctx.currentResource("/content/download");
		final Download download = ctx.request().adaptTo(Download.class);

		assertFalse("Should be NOT be legacy mode", ((DownloadImpl)download).isLegacyMode());
	}

	@Test
	public void isLegacyMode_No_WithGroups() {
		ctx.currentResource("/content/download_with_asset_rendition_groups");

		final Options group1Options = new TestOptionsImpl(ctx.currentResource().getChild("asset-renditions-groups/items/item0/asset-renditions/items"));
		final Options group2Options = new TestOptionsImpl(ctx.currentResource().getChild("asset-renditions-groups/items/item1/asset-renditions/items"));

		doReturn(group1Options).when(modelFactory).getModelFromWrappedRequest(eq(ctx.request()),
				argThat(new IsSameResourceByPath(ctx.currentResource().getChild("asset-renditions-groups/items/item0/asset-renditions").getPath())),
				eq(Options.class));

		doReturn(group2Options).when(modelFactory).getModelFromWrappedRequest(eq(ctx.request()),
				argThat(new IsSameResourceByPath(ctx.currentResource().getChild("asset-renditions-groups/items/item1/asset-renditions").getPath())),
				eq(Options.class));

		final Download download = ctx.request().adaptTo(Download.class);

		assertFalse("Should be NOT be legacy mode", ((DownloadImpl)download).isLegacyMode());
	}


	@Test
	public void getAssetRenditionGroups() {
		ctx.currentResource("/content/download_with_asset_rendition_groups");

		ctx.registerInjectActivateService(new ExternalRedirectRenditionDispatcherImpl(),
				"rendition.mappings", new String[] {
						"rendition-1-1=https://adobe.com/test.png",
						"rendition-1-2=https://adobe.com/test.png",
						"rendition-2-1=https://adobe.com/test.png"});

		assertEquals(1, ctx.getService(AssetRenditionDispatchers.class).getAssetRenditionDispatchers().size());

		final Options group1Options = new TestOptionsImpl(ctx.currentResource().getChild("asset-renditions-groups/items/item0/asset-renditions/items"));
		final Options group2Options = new TestOptionsImpl(ctx.currentResource().getChild("asset-renditions-groups/items/item1/asset-renditions/items"));

		doReturn(group1Options).when(modelFactory).getModelFromWrappedRequest(eq(ctx.request()),
				argThat(new IsSameResourceByPath(ctx.currentResource().getChild("asset-renditions-groups/items/item0/asset-renditions").getPath())),
				eq(Options.class));

		doReturn(group2Options).when(modelFactory).getModelFromWrappedRequest(eq(ctx.request()),
				argThat(new IsSameResourceByPath(ctx.currentResource().getChild("asset-renditions-groups/items/item1/asset-renditions").getPath())),
				eq(Options.class));

		final Download download = ctx.request().adaptTo(Download.class);

		final List<Download.AssetRenditionsGroup> actual = download.getAssetRenditionsGroups();

		assertEquals(2, actual.size());
		assertEquals("Group 1", actual.get(0).getTitle());
		assertEquals("Rendition 1.1", actual.get(0).getItems().get(0).getText());
		assertEquals("rendition-1-1", actual.get(0).getItems().get(0).getValue());
		assertEquals("Rendition 1.2", actual.get(0).getItems().get(1).getText());
		assertEquals("rendition-1-2", actual.get(0).getItems().get(1).getValue());

		assertEquals(2, actual.size());
		assertEquals("Group 2", actual.get(1).getTitle());
		assertEquals("Rendition 2.1", actual.get(1).getItems().get(0).getText());
		assertEquals("rendition-2-1", actual.get(1).getItems().get(0).getValue());
		//assertEquals("Rendition 2.2", actual.get(1).getItems().get(1).getText());
		//assertEquals("rendition-2-2", actual.get(1).getItems().get(1).getValue());
	}

    private class IsSameResourceByPath implements ArgumentMatcher<Resource> {
        private final String path;

        public IsSameResourceByPath(String path) {
	        this.path = path;
        }

        public boolean matches(Resource resource) {
            return StringUtils.equals(path, resource.getPath());
        }
    }

}
