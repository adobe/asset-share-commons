package com.adobe.aem.commons.assetshare.util.impl;


import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

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
}
