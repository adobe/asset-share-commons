/*
 * Asset Share Commons
 *
 * Copyright (C) 2023 Adobe
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

package com.adobe.aem.commons.assetshare.configuration.impl;

import com.adobe.aem.commons.assetshare.configuration.AssetDetails;
import com.adobe.aem.commons.assetshare.configuration.AssetDetailsResolver;
import com.adobe.aem.commons.assetshare.configuration.Config;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.properties.impl.UrlImpl;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import com.adobe.aem.commons.assetshare.util.impl.RequireAemImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/**
 * Note: AssetDetailsImpl's "asset" field is annotated with plain {@code @Inject} (not {@code @Self}), so in
 * order for it to be resolved, an AssetModel must already be bound as either a script-binding or request
 * attribute named "asset" (via the RequestAttributeInjector/BindingsInjector) before this model is adapted.
 * This mirrors how the component is invoked in practice (nested inside a context which has already
 * resolved the current AssetModel).
 */
@RunWith(MockitoJUnitRunner.class)
public class AssetDetailsImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    ModelFactory modelFactory;

    @Mock
    AssetDetailsResolver assetDetailsResolver;

    @Mock
    AssetModel assetModel;

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/configuration/impl/AssetDetailsImplTest.json",
                "/content");

        ctx.registerService(RequireAem.class, new RequireAemImpl());
        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);
        ctx.registerService(AssetDetailsResolver.class, assetDetailsResolver);

        ctx.addModelsForClasses(Config.class, AssetDetailsImpl.class);

        // Simulate the AssetModel already being resolved and bound as a request attribute (script-variable-like)
        // named "asset", which is how AssetDetailsImpl.asset (plain @Inject) actually gets populated.
        ctx.request().setAttribute("asset", assetModel);
    }

    private AssetDetails getAssetDetails(String pagePath) {
        ctx.currentResource(pagePath);
        return ctx.request().adaptTo(AssetDetails.class);
    }

    @Test
    public void getUrl() {
        when(assetDetailsResolver.getUrl(any(Config.class), any(AssetModel.class)))
                .thenReturn("/content/root/details/image.html");

        final AssetDetails assetDetails = getAssetDetails("/content/root");

        assertEquals("/content/root/details/image.html", assetDetails.getUrl());
    }

    @Test
    public void getUrl_isCachedAfterFirstCall() {
        when(assetDetailsResolver.getUrl(any(Config.class), any(AssetModel.class)))
                .thenReturn("/content/root/details/image.html");

        final AssetDetails assetDetails = getAssetDetails("/content/root");

        assertEquals("/content/root/details/image.html", assetDetails.getUrl());
        assertEquals("/content/root/details/image.html", assetDetails.getUrl());

        verify(assetDetailsResolver, times(1)).getUrl(any(Config.class), any(AssetModel.class));
    }

    @Test
    public void getFullUrl_referenceById() {
        when(assetDetailsResolver.getUrl(any(Config.class), any(AssetModel.class)))
                .thenReturn("/content/root/details/image.html");
        when(assetModel.getAssetId()).thenReturn("abc-123");

        final AssetDetails assetDetails = getAssetDetails("/content/root");

        assertEquals("/content/root/details/image.html/abc-123.html", assetDetails.getFullUrl());
    }

    @Test
    public void getFullUrl_notReferenceById_usesUrlProperty() {
        when(assetDetailsResolver.getUrl(any(Config.class), any(AssetModel.class)))
                .thenReturn("/content/root-no-ref-by-id/details/image.html");

        final ValueMap properties = new ValueMapDecorator(new HashMap<>());
        properties.put(UrlImpl.NAME, "/content/dam/test.png");
        when(assetModel.getProperties()).thenReturn(properties);
        when(assetModel.getPath()).thenReturn("/content/dam/test.png");

        final AssetDetails assetDetails = getAssetDetails("/content/root-no-ref-by-id");

        assertEquals("/content/root-no-ref-by-id/details/image.html/content/dam/test.png", assetDetails.getFullUrl());
    }
}
