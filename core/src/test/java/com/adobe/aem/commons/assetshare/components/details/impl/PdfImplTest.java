/*
 * Asset Share Commons
 *
 * Copyright (C) 2024 Adobe
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

package com.adobe.aem.commons.assetshare.components.details.impl;

import com.adobe.aem.commons.assetshare.components.details.Pdf;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.testing.TestStyle;
import com.adobe.aem.commons.assetshare.util.AdobePdfEmbedApi;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PdfImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/PdfImplTest.json", "/content");

        ctx.addModelsForClasses(PdfImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.pdf");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        // Real (lightweight) AssetRenditions implementation
        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());
        ctx.registerInjectActivateService(new AssetRenditionsImpl());

        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);

        // Default (empty) Style, bound to the "currentStyle" SlingBindings entry, so the @ScriptVariable
        // Style currentStyle field resolves to a non-null (but empty) Style rather than null - AEM Mocks does
        // not automatically populate this binding the way a real AEM request would.
        bindStyle(new HashMap<>());
    }

    private void bindStyle(final Map<String, Object> props) {
        TestStyle.bind(ctx, props);
    }

    @Test
    public void getSrc() {
        final String expected = "/content/dam/test.pdf.renditions/original/asset.rendition";

        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        assertEquals(expected, pdf.getSrc());
    }

    @Test
    public void getSrc_NoRenditionName() {
        ctx.currentResource("/content/pdf-no-rendition-name");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        assertEquals(null, pdf.getSrc());
    }

    @Test
    public void getFileName() {
        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        assertEquals("test.pdf", pdf.getFileName());
    }

    @Test
    public void getClientId_Author_AuthorClientId() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);

        final Map<String, Object> props = new HashMap<>();
        props.put("authorClientId", "author-client-id");
        props.put("clientId", "legacy-client-id");
        bindStyle(props);

        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        assertEquals("author-client-id", pdf.getClientId());
    }

    @Test
    public void getClientId_Author_FallbackToLegacyClientId() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);

        final Map<String, Object> props = new HashMap<>();
        props.put("clientId", "legacy-client-id");
        bindStyle(props);

        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        assertEquals("legacy-client-id", pdf.getClientId());
    }

    @Test
    public void getClientId_Publish_PublishClientId() {
        // Override setUp()'s AUTHOR RequireAem registration with a higher service ranking so this one wins.
        ctx.registerService(RequireAem.class, new RequireAem() {
            @Override
            public Distribution getDistribution() {
                return Distribution.CLOUD_READY;
            }

            @Override
            public ServiceType getServiceType() {
                return ServiceType.PUBLISH;
            }
        }, org.osgi.framework.Constants.SERVICE_RANKING, 100);

        final Map<String, Object> props = new HashMap<>();
        props.put("publishClientId", "publish-client-id");
        props.put("clientId", "legacy-client-id");
        bindStyle(props);

        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        assertEquals("publish-client-id", pdf.getClientId());
    }

    @Test
    public void getClientId_FallbackToOsgiService() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        bindStyle(new HashMap<>());

        ctx.registerService(AdobePdfEmbedApi.class, () -> "osgi-service-client-id");

        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        assertEquals("osgi-service-client-id", pdf.getClientId());
    }

    @Test
    public void getClientId_NoConfigAtAll() {
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
        bindStyle(new HashMap<>());

        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        assertTrue(org.apache.commons.lang3.StringUtils.isBlank(pdf.getClientId()));
    }

    @Test
    public void getViewerId_IsPrefixedAndStable() {
        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);

        final String viewerId = pdf.getViewerId();
        assertTrue(viewerId.startsWith("asset_share_commons__adobe_dc_view__"));
        assertFalse(viewerId.contains("-"));
        // Cached - calling again returns the same value
        assertEquals(viewerId, pdf.getViewerId());
    }

    @Test
    public void getEmbedMode_Default() {
        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertEquals("SIZED_CONTAINER", pdf.getEmbedMode());
    }

    @Test
    public void getEmbedMode_Configured() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertEquals("FULL_WINDOW", pdf.getEmbedMode());
    }

    @Test
    public void getDefaultViewMode_Default() {
        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertEquals("FIT_WIDTH", pdf.getDefaultViewMode());
    }

    @Test
    public void getDefaultViewMode_Configured() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertEquals("FIT_PAGE", pdf.getDefaultViewMode());
    }

    @Test
    public void getHeight_Default_Auto() {
        ctx.currentResource("/content/pdf-default-embed-mode");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertEquals("auto", pdf.getHeight());
    }

    @Test
    public void getHeight_Configured() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertEquals("500px", pdf.getHeight());
    }

    @Test
    public void isReadOnly_Default() {
        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.isReadOnly());
    }

    @Test
    public void isReadOnly_Configured() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertFalse(pdf.isReadOnly());
    }

    @Test
    public void showFullScreen_Default() {
        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertFalse(pdf.showFullScreen());
    }

    @Test
    public void showFullScreen_Configured() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.showFullScreen());
    }

    @Test
    public void showDownload() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.showDownload());
    }

    @Test
    public void showPrint() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.showPrint());
    }

    @Test
    public void showZoomControl() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.showZoomControl());
    }

    @Test
    public void showBookmarks() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.showBookmarks());
    }

    @Test
    public void showAnnotationTools() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.showAnnotationTools());
    }

    @Test
    public void isLinearizationEnabled_Default() {
        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertFalse(pdf.isLinearizationEnabled());
    }

    @Test
    public void isLinearizationEnabled_Configured() {
        ctx.currentResource("/content/pdf-embed-modes");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.isLinearizationEnabled());
    }

    @Test
    public void isEmpty_true() {
        ctx.currentResource("/content/pdf-no-rendition-name");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.isEmpty());
    }

    @Test
    public void isEmpty_false() {
        final Map<String, Object> props = new HashMap<>();
        props.put("clientId", "test-client-id");
        bindStyle(props);

        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertFalse(pdf.isEmpty());
    }

    @Test
    public void isReady_true() {
        final Map<String, Object> props = new HashMap<>();
        props.put("clientId", "test-client-id");
        bindStyle(props);

        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertTrue(pdf.isReady());
    }

    @Test
    public void isReady_false() {
        ctx.currentResource("/content/pdf-no-rendition-name");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertFalse(pdf.isReady());
    }

    @Test
    public void getExportedType() {
        ctx.currentResource("/content/pdf");
        final Pdf pdf = ctx.request().adaptTo(Pdf.class);
        assertEquals("asset-share-commons/components/details/pdf", ((PdfImpl) pdf).getExportedType());
    }
}
