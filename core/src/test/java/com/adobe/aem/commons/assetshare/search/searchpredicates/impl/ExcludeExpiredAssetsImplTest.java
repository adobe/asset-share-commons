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

public class ExcludeExpiredAssetsImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Test
    public void getLabel_Default() {
        final ExcludeExpiredAssetsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeExpiredAssetsImpl());

        assertEquals(ExcludeExpiredAssetsImpl.LABEL, searchPredicate.getLabel());
    }

    @Test
    public void getLabel_Custom() {
        final ExcludeExpiredAssetsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeExpiredAssetsImpl(),
                "label", "Custom Label");

        assertEquals("Custom Label", searchPredicate.getLabel());
    }

    @Test
    public void getName() {
        final ExcludeExpiredAssetsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeExpiredAssetsImpl());

        assertEquals(ExcludeExpiredAssetsImpl.NAME, searchPredicate.getName());
    }

    @Test
    public void getPredicateGroup() {
        final ExcludeExpiredAssetsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeExpiredAssetsImpl());

        final PredicateGroup predicateGroup = searchPredicate.getPredicateGroup(ctx.request());

        assertNotNull(predicateGroup);

        final PredicateGroup group = (PredicateGroup) predicateGroup.getByName("group");
        assertNotNull(group);
        assertEquals("true", group.get("or"));

        final Predicate property = group.getByName("property");
        assertEquals("jcr:content/metadata/prism:expirationDate", property.get("property"));
        assertEquals("not", property.get("operation"));

        final Predicate relativeDateRange = group.getByName("relativedaterange");
        assertEquals("jcr:content/metadata/prism:expirationDate", relativeDateRange.get("property"));
        assertEquals("0d", relativeDateRange.get("lowerBound"));
    }

    @Test
    public void isRegisteredAsSearchPredicateService() {
        assertNotNull(ctx.registerInjectActivateService(new ExcludeExpiredAssetsImpl()));
        assertTrue(ctx.getService(SearchPredicate.class) instanceof ExcludeExpiredAssetsImpl);
    }
}
