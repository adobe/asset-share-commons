package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.JsonResolver;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonResolverImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("JsonResolverImpl.json"), "/content");


        ctx.load().binaryFile(getClass().getResourceAsStream("JsonResolverImpl__file--damAsset.json"),
                "/content/dam/test-dam-asset.json/jcr:content/renditions/original");

        ctx.load().binaryFile(getClass().getResourceAsStream("JsonResolverImpl__file--ntFile.json"),
                "/content/test-nt-file.json");

        ctx.load().binaryFile(getClass().getResourceAsStream("JsonResolverImpl__file--internalInclude.json"),
                "/content/test-internal-include");

        ctx.registerService(JsonResolver.class, new JsonResolverImpl());
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
}

