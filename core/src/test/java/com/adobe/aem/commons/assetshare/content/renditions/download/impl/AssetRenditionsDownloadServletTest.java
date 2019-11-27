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

package com.adobe.aem.commons.assetshare.content.renditions.download.impl;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsPacker;
import com.adobe.aem.commons.assetshare.content.renditions.impl.AssetRenditionsImpl;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.adobe.aem.commons.assetshare.util.ServletHelper;
import com.adobe.aem.commons.assetshare.util.impl.ServletHelperImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.Servlet;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AssetRenditionsDownloadServletTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() {
        ctx.load().json(getClass().getResourceAsStream("AssetRenditionsDownloadServletTest.json"), "/content");

        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test-1.png");

        ctx.registerService(AssetRenditionsPacker.class, new AssetRenditionsZipperImpl());

        ctx.registerService(AssetRenditions.class, new AssetRenditionsImpl());

        ctx.registerService(ServletHelper.class, new ServletHelperImpl());

        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);
    }

    @Test
    public void getAssets() {
        ctx.registerInjectActivateService(new AssetRenditionsDownloadServlet());

        AssetRenditionsDownloadServlet servlet = (AssetRenditionsDownloadServlet) ctx.getService(Servlet.class);

        ctx.request().setQueryString("path=/content/dam/test-1.png&path=/content/dam/test-2.png&path=/content/dam/test-3.png");

        List<com.adobe.aem.commons.assetshare.content.AssetModel> actual = servlet.getAssets(ctx.request());

        assertEquals(1, actual.size());
        assertEquals("/content/dam/test-1.png", actual.get(0).getPath());
    }

    @Test
    public void getRenditionNames() {
        ctx.registerInjectActivateService(new AssetRenditionsDownloadServlet());
        AssetRenditionsDownloadServlet servlet = (AssetRenditionsDownloadServlet) ctx.getService(Servlet.class);

        ctx.currentResource("/content/allowed-rendition-names");
        ctx.request().setQueryString("renditionName=one&renditionName=four");

        List<String> actual = servlet.getRenditionNames(ctx.request());
        assertEquals(1, actual.size());
        assertEquals("one", actual.get(0));
    }
}