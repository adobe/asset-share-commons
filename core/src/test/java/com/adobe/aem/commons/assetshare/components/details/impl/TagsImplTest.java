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

import com.adobe.aem.commons.assetshare.components.details.Tags;
import com.adobe.aem.commons.assetshare.content.AssetResolver;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.impl.AssetResolverImpl;
import com.adobe.aem.commons.assetshare.content.properties.impl.ComputedPropertiesImpl;
import com.adobe.aem.commons.assetshare.content.properties.impl.TagTitlesImpl;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TagsImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json("/com/adobe/aem/commons/assetshare/components/details/impl/TagsImplTest.json", "/content");

        ctx.addModelsForClasses(TagsImpl.class);

        ctx.requestPathInfo().setSuffix("/content/dam/test.png");

        // Dependencies to instantiate AssetModels. ComputedPropertiesImpl and TagTitlesImpl are registered via
        // registerInjectActivateService (real SCR-style activation) - rather than a plain "new" + registerService -
        // so that ComputedPropertiesImpl's dynamic bind/unbind reference to TagTitlesImpl actually fires, letting
        // the "no tagPropertyName configured" fallback (which reads the "tagTitles" Computed Property) resolve
        // through TagTitlesImpl for real, instead of via a hand-set raw property.
        ctx.registerInjectActivateService(new ComputedPropertiesImpl());
        ctx.registerInjectActivateService(new TagTitlesImpl());
        ctx.registerService(AssetResolver.class, new AssetResolverImpl());
        ctx.addModelsForClasses(AssetModelImpl.class);

        // Create real tag content so TagManager#resolve(...) can find them.
        ctx.create().resource("/content/cq:tags/asc/color/red", "jcr:primaryType", "cq:Tag", "jcr:title", "Red");
        ctx.create().resource("/content/cq:tags/asc/color/blue", "jcr:primaryType", "cq:Tag", "jcr:title", "Blue");

        // Add properties whose values are not achievable via JSON content-loading (java.util.List / Set), to
        // exercise all branches of TagsImpl#getTagValuesAsList(...).
        final Resource metadata = ctx.resourceResolver().getResource("/content/dam/test.png/jcr:content/metadata");
        final ModifiableValueMap mvm = metadata.adaptTo(ModifiableValueMap.class);
        mvm.put("listTagIds", Arrays.asList("asc:color/red", "asc:color/blue"));
        mvm.put("setTagIds", new LinkedHashSet<>(Arrays.asList("asc:color/red")));
        mvm.put("unsupportedTagIds", 12345);
    }

    @Test
    public void getTagTitles_NoPropertyName_FallsBackToTagTitlesComputedProperty() {
        ctx.currentResource("/content/tags/default");
        final Tags tags = ctx.request().adaptTo(Tags.class);

        final List<String> titles = tags.getTagTitles();

        // TagTitlesImpl (the "tagTitles" Computed Property) derives titles from the real "cq:tags" association
        // and sorts them alphabetically.
        assertEquals(Arrays.asList("Blue", "Red"), titles);
    }

    @Test
    public void getTagTitles_SingleProperty_ResolvesTagTitlesAndUnresolvableIdsAsIs() {
        ctx.currentResource("/content/tags/single-property");
        final Tags tags = ctx.request().adaptTo(Tags.class);

        final List<String> titles = tags.getTagTitles();

        assertEquals(3, titles.size());
        assertTrue(titles.contains("Red"));
        assertTrue(titles.contains("Blue"));
        // Unresolvable tag ids are used as-is for the title.
        assertTrue(titles.contains("asc:color/unresolvable"));
    }

    @Test
    public void getTagTitles_CombinedProperties_DedupesAndCombinesAcrossProperties() {
        ctx.currentResource("/content/tags/combined-properties");
        final Tags tags = ctx.request().adaptTo(Tags.class);

        final List<String> titles = tags.getTagTitles();

        // myTagIds -> red, blue, unresolvable; singleTagId -> red (deduped)
        assertEquals(3, titles.size());
        assertTrue(titles.contains("Red"));
        assertTrue(titles.contains("Blue"));
        assertTrue(titles.contains("asc:color/unresolvable"));
    }

    @Test
    public void getTagTitles_MissingProperty_ResultsInEmptyList() {
        ctx.currentResource("/content/tags/missing-property");
        final Tags tags = ctx.request().adaptTo(Tags.class);

        assertTrue(tags.getTagTitles().isEmpty());
    }

    @Test
    public void getTagTitles_ListTypedProperty() {
        ctx.currentResource("/content/tags/default");
        ctx.currentResource(ctx.create().resource("/content/tags/list-property",
                "jcr:primaryType", "nt:unstructured",
                "sling:resourceType", "asset-share-commons/components/details/tags",
                "tagPropertyName", new String[]{"listTagIds"}));
        final Tags tags = ctx.request().adaptTo(Tags.class);

        final List<String> titles = tags.getTagTitles();
        assertEquals(2, titles.size());
        assertTrue(titles.contains("Red"));
        assertTrue(titles.contains("Blue"));
    }

    @Test
    public void getTagTitles_SetTypedProperty() {
        ctx.currentResource(ctx.create().resource("/content/tags/set-property",
                "jcr:primaryType", "nt:unstructured",
                "sling:resourceType", "asset-share-commons/components/details/tags",
                "tagPropertyName", new String[]{"setTagIds"}));
        final Tags tags = ctx.request().adaptTo(Tags.class);

        final List<String> titles = tags.getTagTitles();
        assertEquals(1, titles.size());
        assertTrue(titles.contains("Red"));
    }

    @Test
    public void getTagTitles_UnsupportedTypedProperty_IsSkipped() {
        ctx.currentResource(ctx.create().resource("/content/tags/unsupported-property",
                "jcr:primaryType", "nt:unstructured",
                "sling:resourceType", "asset-share-commons/components/details/tags",
                "tagPropertyName", new String[]{"unsupportedTagIds"}));
        final Tags tags = ctx.request().adaptTo(Tags.class);

        assertTrue(tags.getTagTitles().isEmpty());
    }

    @Test
    public void getTagTitles_WithCurrentPage_UsesPageLanguageForLocale() {
        ctx.currentPage("/content/page");
        ctx.currentResource("/content/tags/single-property");
        final Tags tags = ctx.request().adaptTo(Tags.class);

        // Just needs to resolve without error and still find the real tags.
        final List<String> titles = tags.getTagTitles();
        assertTrue(titles.contains("Red"));
    }

    @Test
    public void isEmpty_true() {
        ctx.currentResource("/content/tags/missing-property");
        final Tags tags = ctx.request().adaptTo(Tags.class);
        assertTrue(tags.isEmpty());
    }

    @Test
    public void isEmpty_false() {
        ctx.currentResource("/content/tags/single-property");
        final Tags tags = ctx.request().adaptTo(Tags.class);
        assertFalse(tags.isEmpty());
    }

    @Test
    public void isReady_true() {
        ctx.currentResource("/content/tags/single-property");
        final Tags tags = ctx.request().adaptTo(Tags.class);
        assertTrue(tags.isReady());
    }

    @Test
    public void isReady_false() {
        ctx.currentResource("/content/tags/missing-property");
        final Tags tags = ctx.request().adaptTo(Tags.class);
        assertFalse(tags.isReady());
    }

    @Test
    public void getExportedType() {
        ctx.currentResource("/content/tags/single-property");
        final Tags tags = ctx.request().adaptTo(Tags.class);
        assertEquals("asset-share-commons/components/details/tags", ((TagsImpl) tags).getExportedType());
    }
}
