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

package com.adobe.aem.commons.assetshare.components.actions.impl;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import io.wcm.testing.mock.aem.junit.AemContext;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ActionHelperImplTest {
    private static final String ASSETS_REQUEST_PARAMETER_NAME = "assets";

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    ModelFactory modelFactory;

    @Mock
    AssetModel asset1;

    @Mock
    AssetModel asset2;

    @Mock
    AssetModel asset3;

    @Before
    public void setup() {
        ctx.load().json("/com/adobe/aem/commons/assetshare/search/impl/ActionHelperImplTest.json", "/content/dam");

        doReturn("asset-1.png").when(asset1).getName();
        doReturn(asset1).when(modelFactory).getModelFromWrappedRequest(eq(ctx.request()),
                argThat(new ResourcePath("/content/dam/asset-1.png")),
                eq(AssetModel.class));

        doReturn("asset-2.png").when(asset2).getName();
        doReturn(asset2).when(modelFactory).getModelFromWrappedRequest(eq(ctx.request()),
                argThat(new ResourcePath("/content/dam/asset-2.png")),
                eq(AssetModel.class));

        doReturn("asset 3.png").when(asset3).getName();
        doReturn(asset3).when(modelFactory).getModelFromWrappedRequest(eq(ctx.request()),
                argThat(new ResourcePath("/content/dam/asset 3.png")),
                eq(AssetModel.class));
        ctx.registerService(ModelFactory.class, modelFactory, Constants.SERVICE_RANKING, Integer.MAX_VALUE);

        ctx.registerInjectActivateService(new ActionHelperImpl());
    }

    @Test
    public void getAssetsFromQueryParameter() {
        final ActionHelper actionHelper = ctx.getService(ActionHelper.class);

        final Map<String, Object> requestParameters = new HashMap<>();
        final String[] assets = {
                "/content/dam/asset-1.png",
                "/content/dam/asset-2.png",
                "/content/dam/asset%203.png"
        };

        requestParameters.put(ASSETS_REQUEST_PARAMETER_NAME, assets);

        ctx.request().setParameterMap(requestParameters);

        final Collection<AssetModel> models =
                actionHelper.getAssetsFromQueryParameter(ctx.request(), ASSETS_REQUEST_PARAMETER_NAME);

        assertNotNull(models);
        assertEquals(3, models.size());
        assertEquals("asset-1.png", models.toArray(new AssetModel[2])[0].getName());
        assertEquals("asset-2.png", models.toArray(new AssetModel[2])[1].getName());
        assertEquals("asset 3.png", models.toArray(new AssetModel[2])[2].getName());
    }

    @Test
    public void getAllowedValuesFromQueryParameter() {
        final ActionHelper actionHelper = ctx.getService(ActionHelper.class);

        ctx.request().setQueryString("renditionName=one&renditionName=four");

        List<String> actual = actionHelper.getAllowedValuesFromQueryParameter(ctx.request(), "renditionName", new String[] {"four"});
        assertEquals(1, actual.size());
        assertEquals("four", actual.get(0));
    }

    class ResourcePath implements ArgumentMatcher<Resource> {
        private final String path;

        public ResourcePath(final String path) {
            this.path = path;
        }

        public boolean matches(Resource resource) {
            return resource != null && StringUtils.equals(path, resource.getPath());
        }
        public String toString() {
            //printed in verification errors
            return "[Resource path of " + this.path + "]";
        }
    }
}
