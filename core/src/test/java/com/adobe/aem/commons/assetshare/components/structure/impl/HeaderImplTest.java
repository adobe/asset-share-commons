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

package com.adobe.aem.commons.assetshare.components.structure.impl;

import com.adobe.aem.commons.assetshare.components.structure.Header;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HeaderImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/structure/impl/HeaderImplTest.json", "/content");

        ctx.addModelsForClasses(HeaderImpl.class);
    }

    /**
     * Binds the ScriptVariables HeaderImpl requires: currentPage (via AemContext's own support) and currentStyle
     * (via a content policy mapping for the Header resourceType - AEM Mocks resolves the "currentStyle"
     * ScriptVariable through the current page's content policy, which requires both a bound currentPage and a
     * contentPolicyMapping to be present; pageManager resolves automatically once a currentPage is set).
     */
    private void bindScriptVariables(final String currentPagePath, final Map<String, Object> styleProps) {
        ctx.currentPage(currentPagePath);
        ctx.contentPolicyMapping(HeaderImpl.RESOURCE_TYPE, styleProps);
    }

    @Test
    public void getItems_ReturnsExpectedNavigationItems() {
        bindScriptVariables("/content/root", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);
        final Collection<Header.NavigationItem> items = header.getItems();

        // 5 configured, but the blank-path one is skipped.
        assertEquals(4, items.size());
    }

    @Test
    public void getItems_SkipsEntryWithBlankPath() {
        bindScriptVariables("/content/root", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);

        final boolean anyBlank = header.getItems().stream().anyMatch(item -> "No Path".equals(item.getText()));
        assertFalse(anyBlank);
    }

    @Test
    public void getItems_ExternalUrl_NotActiveNorHierarchyActive_UsesConfiguredText() {
        bindScriptVariables("/content/root", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);
        final Header.NavigationItem external = findByUrl(header, "https://example.com");

        assertEquals("External Site", external.getText());
        assertFalse(external.isActive());
        assertFalse(external.isHierarchyActive());
        assertNull(external.getPage());
    }

    @Test
    public void getItems_RelativePage_BlankText_FallsBackToNavTitle() {
        bindScriptVariables("/content/root", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);
        final Header.NavigationItem sub = findByUrl(header, "/content/root/sub");

        assertEquals("Sub Nav", sub.getText());
    }

    @Test
    public void getItems_RelativePage_BlankText_FallsBackToTitle_WhenNoNavTitle() {
        bindScriptVariables("/content/root", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);
        final Header.NavigationItem leaf = findByUrl(header, "/content/root/leaf");

        assertEquals("Leaf Page", leaf.getText());
    }

    @Test
    public void getItems_IsActive_WhenCurrentPageMatchesExactly() {
        bindScriptVariables("/content/root/sub", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);
        final Header.NavigationItem sub = findByUrl(header, "/content/root/sub");

        assertTrue(sub.isActive());
    }

    @Test
    public void getItems_IsHierarchyActive_WhenCurrentPageIsDescendant() {
        bindScriptVariables("/content/root/sub", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);
        final Header.NavigationItem home = findByUrl(header, "/content/root");

        assertTrue(home.isHierarchyActive());
        assertFalse(home.isActive());
    }

    @Test
    public void setHeaderResource_WalksUpToParentPage_WhenCurrentPageHasEmptyHeader() {
        // /content/root/sub has an empty (childless, no rootPath) header resource of its own, so HeaderImpl
        // must walk up to /content/root's header to find real content.
        bindScriptVariables("/content/root/sub", new HashMap<>());
        ctx.currentResource("/content/root/sub/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);

        assertEquals("/content/root", header.getNavigationRoot());
        assertEquals(4, header.getItems().size());
    }

    @Test
    public void isReady_NoHeaderAnywhereInHierarchy_false() {
        bindScriptVariables("/content/no-header", new HashMap<>());
        ctx.currentResource("/content/no-header/jcr:content/some-other-header");

        final Header header = ctx.request().adaptTo(Header.class);

        assertTrue(header.getItems().isEmpty());
        assertNull(header.getLogoPath());
        assertNull(header.getSiteTitle());
        assertFalse(header.isReady());
    }

    @Test
    public void isReady_true_WhenHeaderHasItems() {
        bindScriptVariables("/content/root", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);
        assertTrue(header.isReady());
    }

    @Test
    public void getNavigationRoot_And_getLogoPath_And_getSiteTitle() {
        bindScriptVariables("/content/root", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);

        assertEquals("/content/root", header.getNavigationRoot());
        assertEquals("/content/dam/logo.png", header.getLogoPath());
        assertEquals("My Site", header.getSiteTitle());
    }

    @Test
    public void getSiteTitle_FallsBackToLegacyJcrTitleProperty() {
        bindScriptVariables("/content/jcr-title-fallback", new HashMap<>());
        ctx.currentResource("/content/jcr-title-fallback/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);

        assertEquals("Legacy Title Property", header.getSiteTitle());
    }

    @Test
    public void getHeaderProperty_FallsBackToStyleDesignValue_WhenNotSetOnHeaderResource() {
        final Map<String, Object> styleProps = new HashMap<>();
        styleProps.put("logoPath", "/content/dam/design-logo.png");
        styleProps.put("title", "Design Fallback Title");
        bindScriptVariables("/content/design-fallback", styleProps);
        ctx.currentResource("/content/design-fallback/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);

        assertEquals("/content/dam/design-logo.png", header.getLogoPath());
        assertEquals("Design Fallback Title", header.getSiteTitle());
    }

    @Test
    public void usesCustomPolicyRelPath_WhenConfigured() {
        final Map<String, Object> styleProps = new HashMap<>();
        styleProps.put("relPath", "custom/header");
        bindScriptVariables("/content/custom-relpath", styleProps);
        ctx.currentResource("/content/custom-relpath/jcr:content/custom/header");

        final Header header = ctx.request().adaptTo(Header.class);

        assertEquals("/content/custom-relpath", header.getNavigationRoot());
        assertEquals("Custom Path Site", header.getSiteTitle());
    }

    @Test
    public void getExportedType() {
        bindScriptVariables("/content/root", new HashMap<>());
        ctx.currentResource("/content/root/jcr:content/root/main/header");

        final Header header = ctx.request().adaptTo(Header.class);
        assertEquals("asset-share-commons/components/structure/header", ((HeaderImpl) header).getExportedType());
    }

    private Header.NavigationItem findByUrl(final Header header, final String url) {
        return header.getItems().stream()
                .filter(item -> url.equals(item.getUrl()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No navigation item found for url [ " + url + " ]"));
    }
}
