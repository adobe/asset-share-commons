package com.adobe.aem.commons.assetshare.util.impl;


import com.day.cq.commons.Version;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OakIndexResolverTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.load().json(this.getClass().getResourceAsStream("OakIndexResolverTest.json"), "/oak:index");
    }

    @Test
    public void resolveRankingOakIndex() {
        assertEquals("damAssetLucene-2-custom-1", OakIndexResolver.resolveRankingOakIndex(ctx.resourceResolver(),"damAssetLucene"));
    }

    @Test
    public void resolveRankingOakIndex_noMatchingRootName_returnsNull() {
        assertNull(OakIndexResolver.resolveRankingOakIndex(ctx.resourceResolver(), "someIndexRootNameThatDoesNotExist"));
    }

    @Test
    public void getOakIndexVersion_rootNameOnly() {
        assertEquals(Version.create(new String[]{"0", "0"}), OakIndexResolver.getOakIndexVersion("damAssetLucene", "damAssetLucene"));
    }

    @Test
    public void getOakIndexVersion_withProductVersionOnly() {
        assertEquals(Version.create(new String[]{"2", "0"}), OakIndexResolver.getOakIndexVersion("damAssetLucene", "damAssetLucene-2"));
    }

    @Test
    public void getOakIndexVersion_withProductAndCustomVersion() {
        assertEquals(Version.create(new String[]{"2", "5"}), OakIndexResolver.getOakIndexVersion("damAssetLucene", "damAssetLucene-2-custom-5"));
    }

    @Test
    public void getOakIndexVersion_nonMatchingName_returnsNull() {
        assertNull(OakIndexResolver.getOakIndexVersion("damAssetLucene", "someOtherIndexName"));
    }
}
