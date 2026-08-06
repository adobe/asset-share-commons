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

import com.adobe.aem.commons.assetshare.components.details.Title;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.ComputedProperties;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TitleImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/TitleImplTest.json", "/content");

        ctx.addModelsForClasses(TitleImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        // Dependencies to instantiate AssetModels
        ctx.registerService(ComputedProperties.class, new ComputedPropertiesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        // TitleImpl requires @Required Page currentPage - default to a valid page for every test.
        ctx.currentPage("/content/root-search-page");
    }

    @Test
    public void getTitle_DefaultPropertyName_UsesTitleProperty() {
        // @Default(values = "title") means propertyName defaults to "title" itself, so the raw "title" metadata
        // property is used directly - the "dc:title" fallback only kicks in when the (non-default) configured
        // propertyName resolves to a blank value (see getTitle_BlankConfiguredProperty_FallsBackToRawTitleProperty).
        ctx.currentResource("/content/title");
        final Title title = ctx.request().adaptTo(Title.class);

        assertEquals("Fallback Title Value", title.getTitle());
    }

    @Test
    public void getTitle_CustomPropertyName() {
        ctx.currentResource("/content/title-custom-property");
        final Title title = ctx.request().adaptTo(Title.class);

        assertEquals("Custom Title Value", title.getTitle());
    }

    @Test
    public void getTitle_BlankConfiguredProperty_FallsBackToRawTitleProperty() {
        ctx.currentResource("/content/title-fallback");
        final Title title = ctx.request().adaptTo(Title.class);

        assertEquals("Fallback Title Value", title.getTitle());
    }

    @Test
    public void getTitle_BothBlank_ReturnsEmpty() {
        ctx.requestPathInfo().setSuffix("/content/dam/no-title.png");

        ctx.currentResource("/content/title-both-blank");
        final Title title = ctx.request().adaptTo(Title.class);

        assertEquals("", title.getTitle());
    }

    @Test
    public void getReturnPath_Explicit() {
        ctx.currentResource("/content/title-explicit-return-path");
        final Title title = ctx.request().adaptTo(Title.class);

        assertEquals("/explicit/path", title.getReturnPath());
    }

    @Test
    public void getReturnPath_WalksUpToDefaultSearchPageResourceType() {
        ctx.currentPage("/content/root-search-page/detail-page");

        ctx.currentResource("/content/title-walk-default");
        final Title title = ctx.request().adaptTo(Title.class);

        assertEquals("/content/root-search-page", title.getReturnPath());
    }

    @Test
    public void getReturnPath_WalksUpWithCustomResourceTypes() {
        ctx.currentPage("/content/custom-root/custom-leaf");

        ctx.currentResource("/content/title-walk-custom-types");
        final Title title = ctx.request().adaptTo(Title.class);

        assertEquals("/content/custom-root", title.getReturnPath());
    }

    @Test
    public void getReturnPath_NoMatchingAncestor_ReturnsNull() {
        ctx.currentPage("/content/standalone-page");

        ctx.currentResource("/content/title-walk-no-match");
        final Title title = ctx.request().adaptTo(Title.class);

        assertNull(title.getReturnPath());
    }

    @Test
    public void isEmpty_false() {
        ctx.currentResource("/content/title");
        final Title title = ctx.request().adaptTo(Title.class);
        assertFalse(title.isEmpty());
    }

    @Test
    public void isEmpty_true() {
        ctx.requestPathInfo().setSuffix("/content/dam/no-title.png");

        ctx.currentResource("/content/title-both-blank");
        final Title title = ctx.request().adaptTo(Title.class);
        assertTrue(title.isEmpty());
    }

    @Test
    public void isReady_true() {
        ctx.currentResource("/content/title");
        final Title title = ctx.request().adaptTo(Title.class);
        assertTrue(title.isReady());
    }

    @Test
    public void isReady_false() {
        ctx.requestPathInfo().setSuffix("/content/dam/no-title.png");

        ctx.currentResource("/content/title-both-blank");
        final Title title = ctx.request().adaptTo(Title.class);
        assertFalse(title.isReady());
    }

    @Test
    public void getExportedType() {
        ctx.currentResource("/content/title");
        final Title title = ctx.request().adaptTo(Title.class);
        assertEquals("asset-share-commons/components/details/title", ((TitleImpl) title).getExportedType());
    }
}
