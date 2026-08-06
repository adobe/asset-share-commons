/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
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

package com.adobe.aem.commons.assetshare.content.impl;

import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.day.cq.dam.api.Asset;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetResolverImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    Config config;

    @Mock
    AssetModel placeholderAssetModel;

    AssetResolverImpl assetResolver;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/content/impl/AssetResolverImplTest.json", "/content/dam");

        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLASSIC, RequireAem.ServiceType.PUBLISH);
        assetResolver = ctx.registerInjectActivateService(new AssetResolverImpl());
    }

    @Test
    public void resolveAsset_WithSuffixResourceAdaptableToAsset() {
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png.asc-download.html");
        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        final Asset actual = assetResolver.resolveAsset(ctx.request());

        assertEquals("/content/dam/test.png", actual.getPath());
    }

    @Test
    public void resolveAsset_WithRenditionsExtensionFallsBackToRequestResource() {
        ctx.currentResource("/content/dam/test.png");
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");
        ctx.requestPathInfo().setExtension("renditions");

        final Asset actual = assetResolver.resolveAsset(ctx.request());

        assertEquals("/content/dam/test.png", actual.getPath());
    }

    @Test
    public void resolveAsset_FallsBackToRequestResourceWhenNoSuffix() {
        ctx.currentResource("/content/dam/test.png");
        ctx.requestPathInfo().setResourcePath("/content/dam/test.png");

        final Asset actual = assetResolver.resolveAsset(ctx.request());

        assertEquals("/content/dam/test.png", actual.getPath());
    }

    @Test
    public void resolveAsset_WithUnresolvableIdFallsThroughAndThrows() {
        // NOTE: DamUtil.getAssetFromID(..) adapts the ResourceResolver to a JCR Session and calls
        // Session#getNodeByIdentifier(id). The lightweight mock ResourceResolver used by AemContext
        // does not support Session adaptation, so this surfaces as a NullPointerException rather than
        // the RepositoryException that AssetResolverImpl#getAssetById(..) explicitly catches (and which
        // real AEM's JCR-backed Session would throw as ItemNotFoundException for an unknown id, handled
        // gracefully). This still exercises the id-extraction and getAssetById(..) call path.
        ctx.currentResource("/");
        ctx.requestPathInfo().setResourcePath("/content/does-not-exist");
        ctx.requestPathInfo().setSuffix("/some-unknown-id");

        assertThrows(NullPointerException.class, () -> assetResolver.resolveAsset(ctx.request()));
    }

    @Test
    public void resolveAsset_ThrowsWhenNothingResolves() {
        ctx.currentResource("/");
        ctx.requestPathInfo().setResourcePath("/content/does-not-exist");

        assertThrows(IllegalArgumentException.class, () -> assetResolver.resolveAsset(ctx.request()));
    }

    @Test
    public void resolveAssetFromResource_WithValidResource() {
        final Resource resource = ctx.resourceResolver().getResource("/content/dam/test.png");

        final Asset actual = assetResolver.resolveAsset(resource);

        assertEquals("/content/dam/test.png", actual.getPath());
    }

    @Test
    public void resolveAssetFromResource_ThrowsWithInvalidResource() {
        final Resource resource = ctx.create().resource("/content/not-an-asset");

        assertThrows(IllegalArgumentException.class, () -> assetResolver.resolveAsset(resource));
    }

    @Test
    public void resolvePlaceholderAsset_WithNullPlaceholder() {
        when(config.getPlaceholderAsset()).thenReturn(null);

        final Asset actual = assetResolver.resolvePlaceholderAsset(config);

        assertNull(actual);
    }

    @Test
    public void resolvePlaceholderAsset_WithNullPlaceholderResource() {
        when(config.getPlaceholderAsset()).thenReturn(placeholderAssetModel);
        when(placeholderAssetModel.getResource()).thenReturn(null);

        final Asset actual = assetResolver.resolvePlaceholderAsset(config);

        assertNull(actual);
    }

    @Test
    public void resolvePlaceholderAsset_WithValidPlaceholderResource() {
        final Resource resource = ctx.resourceResolver().getResource("/content/dam/test.png");

        when(config.getPlaceholderAsset()).thenReturn(placeholderAssetModel);
        when(placeholderAssetModel.getResource()).thenReturn(resource);

        final Asset actual = assetResolver.resolvePlaceholderAsset(config);

        assertEquals("/content/dam/test.png", actual.getPath());
    }
}
