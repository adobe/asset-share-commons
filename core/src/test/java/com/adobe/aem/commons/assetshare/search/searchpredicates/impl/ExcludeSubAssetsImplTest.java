package com.adobe.aem.commons.assetshare.search.searchpredicates.impl;

import com.adobe.aem.commons.assetshare.search.searchpredicates.SearchPredicate;
import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExcludeSubAssetsImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Test
    public void getLabel_Default() {
        final ExcludeSubAssetsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeSubAssetsImpl());

        assertEquals(ExcludeSubAssetsImpl.LABEL, searchPredicate.getLabel());
    }

    @Test
    public void getLabel_Custom() {
        final ExcludeSubAssetsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeSubAssetsImpl(),
                "label", "Custom Label");

        assertEquals("Custom Label", searchPredicate.getLabel());
    }

    @Test
    public void getName() {
        final ExcludeSubAssetsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeSubAssetsImpl());

        assertEquals(ExcludeSubAssetsImpl.NAME, searchPredicate.getName());
    }

    @Test
    public void getPredicateGroup() {
        final ExcludeSubAssetsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeSubAssetsImpl());

        final PredicateGroup predicateGroup = searchPredicate.getPredicateGroup(ctx.request());

        assertNotNull(predicateGroup);

        final Predicate mainAsset = predicateGroup.getByName("mainasset");
        assertNotNull(mainAsset);
        assertEquals("true", mainAsset.get("mainasset"));
    }

    @Test
    public void isRegisteredAsSearchPredicateService() {
        assertNotNull(ctx.registerInjectActivateService(new ExcludeSubAssetsImpl()));
        assertTrue(ctx.getService(SearchPredicate.class) instanceof ExcludeSubAssetsImpl);
    }
}
