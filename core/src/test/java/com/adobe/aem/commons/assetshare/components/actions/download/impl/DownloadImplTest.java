package com.adobe.aem.commons.assetshare.components.actions.download.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
	SlingHttpServletRequest mockRequest;

	@Mock
	ResourceBundle resourceBundle;

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

		ctx.registerService(ActionHelper.class, actionHelper, Constants.SERVICE_RANKING, Integer.MAX_VALUE);
		ctx.registerService(AssetDownloadHelper.class, assetDownloadHelper, Constants.SERVICE_RANKING,
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
}
