/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
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

import com.adobe.aem.commons.assetshare.components.details.Renditions;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.Rendition;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionDispatchers;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import com.adobe.aem.commons.assetshare.util.impl.ExpressionEvaluatorImpl;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RenditionsImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    private ModelFactory modelFactory;

    @Mock
    private AssetRenditionDispatchers assetRenditionDispatchers;

    @Mock
    private MimeTypeService mimeTypeService;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/RenditionsImplTest.json", "/content");

        ctx.addModelsForClasses(RenditionsImpl.class, LegacyOptionsTestModel.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        // RenditionsImpl's own OSGiService fields
        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        ctx.registerService(AssetRenditionDispatchers.class, assetRenditionDispatchers);
        ctx.registerService(MimeTypeService.class, mimeTypeService, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);

        // Real (lightweight) AssetRenditions implementation, mirrors AssetRenditionsImplTest wiring
        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());
        ctx.registerInjectActivateService(new AssetRenditionsImpl());
    }

    private Options modernOptions(final String... values) {
        final List<OptionItem> items = new ArrayList<>();
        for (final String value : values) {
            items.add(new OptionItem() {
                @Override
                public boolean isSelected() {
                    return false;
                }

                @Override
                public boolean isDisabled() {
                    return false;
                }

                @Override
                public String getValue() {
                    return value;
                }

                @Override
                public String getText() {
                    return "Label-" + value;
                }
            });
        }

        return new Options() {
            @Override
            public List<OptionItem> getItems() {
                return items;
            }

            @Override
            public Type getType() {
                return Type.CHECKBOX;
            }
        };
    }

    private void stubModernOptions(final String... values) {
        doReturn(modernOptions(values)).when(modelFactory)
                .getModelFromWrappedRequest(any(), any(), eq(Options.class));
    }

    @Test
    public void isAllowLinks_true() {
        ctx.currentResource("/content/renditions-modern");
        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertTrue(renditions.isAllowLinks());
    }

    @Test
    public void isAllowLinks_false() {
        ctx.currentResource("/content/renditions-empty");
        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertFalse(renditions.isAllowLinks());
    }

    @Test
    public void isShowMissingRenditions_true() {
        ctx.currentResource("/content/renditions-modern");
        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertTrue(renditions.isShowMissingRenditions());
    }

    @Test
    public void isShowMissingRenditions_false() {
        ctx.currentResource("/content/renditions-modern-hide-missing");
        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertFalse(renditions.isShowMissingRenditions());
    }

    @Test
    public void getRenditions_Modern_ValidRendition() {
        ctx.currentResource("/content/renditions-modern");
        stubModernOptions("thumbnail");
        when(assetRenditionDispatchers.isValidAssetRenditionName("thumbnail")).thenReturn(true);

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        final Collection<Rendition> result = renditions.getRenditions();

        assertEquals(1, result.size());
        final Rendition rendition = result.iterator().next();
        assertTrue(rendition.isExists());
        assertEquals("Label-thumbnail", rendition.getLabel());
        assertEquals("Label-thumbnail", rendition.getName());
        assertEquals("/content/dam/test.png.renditions/thumbnail/download/asset.rendition", rendition.getPath());
        assertEquals("", rendition.getSize());
        assertNull(rendition.getMimeType());
        assertEquals("", rendition.getDownloadFileName());
        assertTrue(rendition.isLicensed());
    }

    @Test
    public void getRenditions_Modern_InvalidRendition_ShowMissing() {
        ctx.currentResource("/content/renditions-modern");
        stubModernOptions("nonexistent");
        when(assetRenditionDispatchers.isValidAssetRenditionName("nonexistent")).thenReturn(false);

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        final Collection<Rendition> result = renditions.getRenditions();

        assertEquals(1, result.size());
        final Rendition rendition = result.iterator().next();
        assertFalse(rendition.isExists());
        assertEquals("Label-nonexistent", rendition.getLabel());
        assertEquals("Label-nonexistent", rendition.getDownloadFileName());
        assertNull(rendition.getPath());
        assertNull(rendition.getName());
        assertNull(rendition.getSize());
        assertNull(rendition.getMimeType());
        assertFalse(rendition.isLicensed());
    }

    @Test
    public void getRenditions_Modern_InvalidRendition_HideMissing() {
        ctx.currentResource("/content/renditions-modern-hide-missing");
        stubModernOptions("nonexistent");
        when(assetRenditionDispatchers.isValidAssetRenditionName("nonexistent")).thenReturn(false);

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        final Collection<Rendition> result = renditions.getRenditions();

        assertTrue(result.isEmpty());
    }

    @Test
    public void getRenditions_Legacy_MatchFound() {
        ctx.currentResource("/content/renditions-legacy");
        when(mimeTypeService.getExtension("image/png")).thenReturn("png");

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        final Collection<Rendition> result = renditions.getRenditions();

        assertEquals(2, result.size());

        final List<Rendition> list = new ArrayList<>(result);
        Rendition matched = null;
        Rendition missing = null;
        for (final Rendition r : list) {
            if (r.isExists()) {
                matched = r;
            } else {
                missing = r;
            }
        }

        assertTrue("Expected a matched legacy rendition", matched != null);
        assertEquals("Web Rendition", matched.getLabel());
        assertEquals("cq5dam.web.1280.1280.png", matched.getName());
        assertEquals("image/png", matched.getMimeType());
        assertEquals("/content/dam/test.png/_jcr_content/renditions/cq5dam.web.1280.1280.png", matched.getPath());
        assertEquals("Web Rendition.png", matched.getDownloadFileName());
        assertTrue(matched.isLicensed());

        assertTrue("Expected a missing legacy rendition", missing != null);
        assertEquals("Nonexistent Rendition", missing.getLabel());
        assertEquals("Nonexistent Rendition", missing.getDownloadFileName());
        assertFalse(missing.isLicensed());
    }

    @Test
    public void getRenditions_Legacy_NoMatch_HideMissing() {
        ctx.currentResource("/content/renditions-legacy-hide-missing");

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        final Collection<Rendition> result = renditions.getRenditions();

        assertTrue(result.isEmpty());
    }

    @Test
    public void isLegacyMode_ExplicitTrue_PrefersLegacy() {
        ctx.currentResource("/content/renditions-legacy-explicit-mode");

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        final Collection<Rendition> result = renditions.getRenditions();

        // Even though modern "asset-renditions" options are available, legacyMode=true forces the legacy path
        assertEquals(1, result.size());
        assertEquals("cq5dam.web.1280.1280.png", result.iterator().next().getName());
    }

    @Test
    public void isLegacyMode_ExplicitFalse_PrefersModern() {
        ctx.currentResource("/content/renditions-modern-explicit-mode");

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        final Collection<Rendition> result = renditions.getRenditions();

        // legacyMode=false forces modern path; since there is no "asset-renditions" node, renditionOptions is null
        // and no renditions are collected (regardless of the legacy-options content present).
        assertTrue(result.isEmpty());
    }

    @Test
    public void isEmpty_true() {
        ctx.currentResource("/content/renditions-empty");
        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertTrue(renditions.isEmpty());
    }

    @Test
    public void isEmpty_false() {
        ctx.currentResource("/content/renditions-modern");
        stubModernOptions("thumbnail");
        when(assetRenditionDispatchers.isValidAssetRenditionName("thumbnail")).thenReturn(true);

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertFalse(renditions.isEmpty());
    }

    @Test
    public void isReady_true() {
        ctx.currentResource("/content/renditions-modern");
        stubModernOptions("thumbnail");
        when(assetRenditionDispatchers.isValidAssetRenditionName("thumbnail")).thenReturn(true);

        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertTrue(renditions.isReady());
    }

    @Test
    public void isReady_false() {
        ctx.currentResource("/content/renditions-empty");
        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertFalse(renditions.isReady());
    }

    @Test
    public void getExportedType() {
        ctx.currentResource("/content/renditions-empty");
        final Renditions renditions = ctx.request().adaptTo(Renditions.class);
        assertEquals("asset-share-commons/components/details/renditions", ((RenditionsImpl) renditions).getExportedType());
    }
}
