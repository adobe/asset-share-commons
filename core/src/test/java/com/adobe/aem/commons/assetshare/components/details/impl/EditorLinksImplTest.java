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

import com.adobe.aem.commons.assetshare.components.details.EditorLinks;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.testing.RequireAemMock;
import com.adobe.aem.commons.assetshare.util.RequireAem;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class EditorLinksImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/EditorLinksImplTest.json", "/content");

        ctx.addModelsForClasses(EditorLinksImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());

        ctx.addModelsForClasses(AssetModelImpl.class);
        RequireAemMock.setAem(ctx, RequireAem.Distribution.CLOUD_READY, RequireAem.ServiceType.AUTHOR);
    }

    @Test
    public void getAssetDetailsEditorPath() {
        final String expected = "/assetdetails.html/content/dam/test.png";

        RequireAemMock.setAem(ctx,
                RequireAem.Distribution.CLOUD_READY,
                RequireAem.ServiceType.AUTHOR);

        ctx.currentResource("/content/editor-links");
        final EditorLinks editorLinks = ctx.request().adaptTo(EditorLinks.class);

        assertEquals(expected, editorLinks.getAssetDetailsEditorPath());
    }

    @Test
    public void getAssetFolderEditorPath() {
        final String expected = "/assets.html/content/dam/test.png";

        RequireAemMock.setAem(ctx,
                RequireAem.Distribution.CLOUD_READY,
                RequireAem.ServiceType.AUTHOR);

        ctx.currentResource("/content/editor-links");
        final EditorLinks editorLinks = ctx.request().adaptTo(EditorLinks.class);

        assertEquals(expected, editorLinks.getAssetFolderEditorPath());
    }

    @Test
    public void isEmpty() {
        RequireAemMock.setAem(ctx,
                RequireAem.Distribution.CLOUD_READY,
                RequireAem.ServiceType.AUTHOR);

        ctx.currentResource("/content/empty");
        final EditorLinks editorLinks = ctx.request().adaptTo(EditorLinks.class);
        assertTrue(editorLinks.isEmpty());
    }

    @Test
    public void isEmpty_NotEmpty() {
        RequireAemMock.setAem(ctx,
                RequireAem.Distribution.CLOUD_READY,
                RequireAem.ServiceType.AUTHOR);

        ctx.currentResource("/content/editor-links");
        final EditorLinks editorLinks = ctx.request().adaptTo(EditorLinks.class);
        assertFalse(editorLinks.isEmpty());
    }

    @Test
    public void isReady() {
        RequireAemMock.setAem(ctx,
                RequireAem.Distribution.CLOUD_READY,
                RequireAem.ServiceType.AUTHOR);

        ctx.currentResource("/content/editor-links");
        final EditorLinks editorLinks = ctx.request().adaptTo(EditorLinks.class);
        assertTrue(editorLinks.isReady());
    }

    @Test
    public void isReady_NotReady() {
        RequireAemMock.setAem(ctx,
                RequireAem.Distribution.CLOUD_READY,
                RequireAem.ServiceType.AUTHOR);

        ctx.currentResource("/content/empty");
        final EditorLinks editorLinks = ctx.request().adaptTo(EditorLinks.class);
        assertFalse(editorLinks.isReady());
    }

    @Test
    public void isReady_NotReadyDueToRunmode() {
        RequireAemMock.setAem(ctx,
                RequireAem.Distribution.CLOUD_READY,
                RequireAem.ServiceType.PUBLISH);

        ctx.runMode("publish");
        ctx.currentResource("/content/empty");
        final EditorLinks editorLinks = ctx.request().adaptTo(EditorLinks.class);
        assertFalse(editorLinks.isReady());
    }
}