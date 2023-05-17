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

package com.adobe.aem.commons.assetshare.testing;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;


/**
 *     @Rule
 *     public final AemContext ctx = new AemContext();
 *
 *     @Mock
 *     ModelFactory modelFactory;
 *
 *     ...
 *
 *     MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.png");
 *     ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
 */
public class MockAssetModels {

    public static void mockModelFactory(AemContext ctx, ModelFactory mockModelFactory, String assetPath) {
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        Resource previousCurrentResource = ctx.currentResource();
        String previousSuffix = ctx.requestPathInfo().getSuffix();
        String previousResourcePath = ctx.requestPathInfo().getResourcePath();

        ctx.requestPathInfo().setResourcePath(assetPath);
        ctx.requestPathInfo().setSuffix(assetPath);
        ctx.currentResource(assetPath);

        final AssetModel asset = ctx.request().adaptTo(AssetModel.class);

        lenient().doReturn(asset).when(mockModelFactory).getModelFromWrappedRequest(eq(ctx.request()),
                argThat(new ResourcePathMatcher(assetPath)),
                eq(AssetModel.class));

        lenient().doReturn(asset).when(mockModelFactory).createModel(argThat(new ResourcePathMatcher(assetPath)),
                eq(AssetModel.class));

        ctx.requestPathInfo().setResourcePath(previousResourcePath);
        ctx.requestPathInfo().setSuffix(previousSuffix);
        ctx.currentResource(previousCurrentResource);
    }
}
