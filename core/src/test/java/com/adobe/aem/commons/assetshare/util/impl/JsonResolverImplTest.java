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

package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.JsonResolver;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JsonResolverImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("JsonResolverImpl.json"), "/content");
        ctx.load().json(getClass().getResourceAsStream("JsonResolverImpl--generic-list.json"), "/etc/acs-commons/lists");

        ctx.load().binaryFile(getClass().getResourceAsStream("JsonResolverImpl__file--damAsset.json"),
                "/content/dam/test-dam-asset.json/jcr:content/renditions/original");

        ctx.load().binaryFile(getClass().getResourceAsStream("JsonResolverImpl__file--ntFile.json"),
                "/content/test-nt-file.json");

        ctx.load().binaryFile(getClass().getResourceAsStream("JsonResolverImpl__file--internalInclude.json"),
                "/content/test-internal-include");

        ctx.registerService(JsonResolver.class, new JsonResolverImpl());

        // Additional fixtures (purely additive) used by the extra coverage tests below.

        // A DAM asset whose metadata mime type is NOT application/json, to exercise
        // JsonResolverImpl#getJsonStringFromDamAsset(..)'s "wrong mimetype" -> null branch.
        ctx.create().resource("/content/dam/test-dam-asset-wrong-mimetype.txt", "jcr:primaryType", "dam:Asset");
        ctx.create().resource("/content/dam/test-dam-asset-wrong-mimetype.txt/jcr:content", "jcr:primaryType", "nt:unstructured");
        ctx.create().resource("/content/dam/test-dam-asset-wrong-mimetype.txt/jcr:content/metadata",
                "jcr:primaryType", "nt:unstructured",
                "dc:format", "text/plain");

        // An nt:file whose jcr:content mime type is NOT application/json, to exercise
        // JsonResolverImpl#getJsonFromNtFile(..)'s "wrong mimetype" -> null branch.
        ctx.load().binaryFile(new ByteArrayInputStream("plain text content".getBytes(StandardCharsets.UTF_8)),
                "/content/test-nt-file-wrong-mimetype.txt");

        // A resource under /etc/acs-commons/lists/ that is not contained within any cq:Page, to
        // exercise the "page == null" branch of the acs-commons list handling in resolveJson(..).
        ctx.create().resource("/etc/acs-commons/lists/orphan", "jcr:primaryType", "nt:unstructured");
    }

    @Test
    public void resolveDamAsset() {
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(), "/content/dam/test-dam-asset.json");

        assertNotNull(actual);
        assertEquals("dam asset", actual.getAsJsonObject().get("test").getAsString());
    }

    @Test
    public void resolveExternalInclude() {
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(), "https://opensource.adobe.com/asset-share-commons/tests/example.json");

        assertNotNull(actual);
        assertEquals("remote test json file.", actual.getAsJsonObject().get("test").getAsString());
    }


    //@Test
    public void resolveInternalInclude() {
        // This test requires too much mocking to be worth it.
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(), "/content/test-internal-include.json");

        assertNotNull(actual);
        assertEquals("internal include", actual.getAsJsonObject().get("test").getAsString());
    }

    @Test
    public void resolveGenericList() {
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(), "/etc/acs-commons/lists/test");

        assertNotNull(actual);
        assertEquals(2, actual.getAsJsonObject().getAsJsonArray("options").size());
        assertEquals("Item 1", actual.getAsJsonObject().getAsJsonArray("options").get(0).getAsJsonObject().get("text").getAsString());
        assertEquals("1", actual.getAsJsonObject().getAsJsonArray("options").get(0).getAsJsonObject().get("value").getAsString());
        assertEquals("Item 2", actual.getAsJsonObject().getAsJsonArray("options").get(1).getAsJsonObject().get("text").getAsString());
        assertEquals("2", actual.getAsJsonObject().getAsJsonArray("options").get(1).getAsJsonObject().get("value").getAsString());
    }

    @Test
    public void resolveNtFile() {
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(), "/content/test-nt-file.json");

        assertNotNull(actual);
        assertEquals("nt file", actual.getAsJsonObject().get("test").getAsString());
    }

    @Test
    public void resolveNtFile_wrongMimeType_returnsNull() {
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(), "/content/test-nt-file-wrong-mimetype.txt");

        assertNull(actual);
    }

    @Test
    public void resolveDamAsset_wrongMimeType_returnsNull() {
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(), "/content/dam/test-dam-asset-wrong-mimetype.txt");

        assertNull(actual);
    }

    @Test
    public void resolveGenericList_pageNotFound_returnsNull() {
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(), "/etc/acs-commons/lists/orphan");

        assertNull(actual);
    }

    @Test
    public void resolveExternalInclude_notFound_returnsNull() {
        JsonResolver jsonResolver = ctx.getService(JsonResolver.class);

        JsonElement actual = jsonResolver.resolveJson(ctx.request(), ctx.response(),
                "https://opensource.adobe.com/asset-share-commons/tests/this-file-does-not-exist-404.json");

        assertNull(actual);
    }

}

