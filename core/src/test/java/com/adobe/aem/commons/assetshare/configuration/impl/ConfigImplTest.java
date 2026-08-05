package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.components.actions.share.ShareService;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.RequireAemImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/configuration/impl/ConfigImplTest.json",
                "/content");

        ctx.registerService(RequireAem.class, new RequireAemImpl());

        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);

        ctx.addModelsForClasses(Config.class);
    }

    @Test
    public void isContextHubEnabled_noContextHub() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.isContextHubEnabled());
    }

    @Test
    public void isContextHubEnabled_invalidContextHub() {
        ctx.currentResource("/content/invalid-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.isContextHubEnabled());
    }

    @Test
    public void isContextHubEnabled_validContextHub() {
        ctx.currentResource("/content/valid-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isContextHubEnabled());
    }

    @Test
    public void getRequest_returnsCurrentRequest() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertSame(ctx.request(), config.getRequest());
    }

    @Test
    public void getResourceResolver_returnsCurrentResourceResolver() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertSame(ctx.resourceResolver(), config.getResourceResolver());
    }

    @Test
    public void getProperties_isNotNull() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertNotNull(config.getProperties());
    }

    @Test
    public void getLocale_returnsPageLanguage() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals(Locale.FRENCH, config.getLocale());
    }

    @Test
    public void isAemClassic_true_withDefaultRequireAemImpl() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        // RequireAemImpl defaults to CLASSIC distribution in the test environment (no RequireAemCanary present)
        assertTrue(config.isAemClassic());
    }

    @Test
    public void getRootPath_findsMatchingAncestorPage() {
        ctx.currentResource("/content/root-tree/sub/leaf");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("/content/root-tree", config.getRootPath());
    }

    @Test
    public void getRootPath_noMatch_returnsSlash() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("/", config.getRootPath());
    }

    @Test
    public void getAssetDetailsSelector_default() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("always-use-default", config.getAssetDetailsSelector());
    }

    @Test
    public void getAssetDetailsSelector_override() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("my-custom-selector", config.getAssetDetailsSelector());
    }

    @Test
    public void getAssetDetailReferenceById_default_false() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.getAssetDetailReferenceById());
    }

    @Test
    public void getAssetDetailReferenceById_override_true() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.getAssetDetailReferenceById());
    }

    @Test
    public void getAssetDetailsPath_defaultsToRootPathPlusDetails() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("/content/action-tests/details", config.getAssetDetailsPath());
    }

    @Test
    public void getAssetDetailsUrl_appendsHtml() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("/content/action-tests/details.html", config.getAssetDetailsUrl());
    }

    @Test
    public void getPlaceholderAsset_notFound_returnsNull() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertNull(config.getPlaceholderAsset());
    }

    @Test
    public void getPlaceholderAsset_found_returnsModelFromModelFactory() {
        ctx.currentResource("/content/action-tests");

        final AssetModel placeholderAssetModel = mock(AssetModel.class);
        when(modelFactory.getModelFromWrappedRequest(eq(ctx.request()), any(), eq(AssetModel.class)))
                .thenReturn(placeholderAssetModel);

        final Config config = ctx.request().adaptTo(Config.class);

        assertSame(placeholderAssetModel, config.getPlaceholderAsset());
    }

    @Test
    public void getDownloadActionUrl_resolves() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("/content/action-tests/actions/download.partial.html", config.getDownloadActionUrl());
    }

    @Test
    public void getDownloadsActionUrl_doesNotResolve_returnsNull() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        // "downloads" resource exists in the fixture, so this should resolve
        assertEquals("/content/action-tests/actions/downloads.partial.html", config.getDownloadsActionUrl());
    }

    @Test
    public void getLicenseActionUrl_resolves() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("/content/action-tests/actions/license.partial.html", config.getLicenseActionUrl());
    }

    @Test
    public void getShareActionUrl_resolves() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("/content/action-tests/actions/share.partial.html", config.getShareActionUrl());
    }

    @Test
    public void getCartActionUrl_resolves() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertEquals("/content/action-tests/actions/cart.partial.html", config.getCartActionUrl());
    }

    @Test
    public void getDownloadActionUrl_doesNotResolve_returnsNull() {
        // No "actions/download" resource exists under this page, so the path won't resolve
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertNull(config.getDownloadActionUrl());
    }

    @Test
    public void isDownloadEnabled_true() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isDownloadEnabled());
    }

    @Test
    public void isDownloadEnabled_false_whenNotConfigured() {
        ctx.currentResource("/content/no-contexthub");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.isDownloadEnabled());
    }

    @Test
    public void isLicenseEnabled_true() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isLicenseEnabled());
    }

    @Test
    public void isCartEnabled_true() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isCartEnabled());
    }

    @Test
    public void isShareEnabled_false_whenNoShareServiceRegistered() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        // Even though share is "enabled" and the url resolves, isShareEnabled() requires a ShareService
        assertFalse(config.isShareEnabled());
    }

    @Test
    public void isShareEnabled_true_withShareServiceRegistered() {
        ctx.registerService(ShareService.class, mock(ShareService.class));
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isShareEnabled());
    }

    @Test
    public void isDownloadEnabledCart_true_whenCartAndDownloadEnabled() {
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isDownloadEnabledCart());
    }

    @Test
    public void isDownloadEnabledCart_false_whenCartDisabled() {
        ctx.currentResource("/content/action-tests-cart-disabled");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.isDownloadEnabledCart());
    }

    @Test
    public void isDownloadEnabledCart_true_whenDownloadEnabledOnlyForCart() {
        ctx.currentResource("/content/action-tests-cart-value");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isDownloadEnabledCart());
        // But it should NOT be enabled for the general (non-cart) case
        assertFalse(config.isDownloadEnabled());
    }

    @Test
    public void isShareEnabledCart_true_withShareServiceRegistered() {
        ctx.registerService(ShareService.class, mock(ShareService.class));
        ctx.currentResource("/content/action-tests");
        final Config config = ctx.request().adaptTo(Config.class);

        assertTrue(config.isShareEnabledCart());
    }

    @Test
    public void isShareEnabledCart_false_whenCartDisabled() {
        ctx.registerService(ShareService.class, mock(ShareService.class));
        ctx.currentResource("/content/action-tests-cart-disabled");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.isShareEnabledCart());
    }

    @Test
    public void isDownloadEnabled_false_whenUnrecognizedEnablementValue() {
        ctx.currentResource("/content/action-tests-unrecognized-value");
        final Config config = ctx.request().adaptTo(Config.class);

        assertFalse(config.isDownloadEnabled());
    }

}