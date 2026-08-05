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

public class ExcludeContentFragmentsImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    @Test
    public void getLabel_Default() {
        final ExcludeContentFragmentsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeContentFragmentsImpl());

        assertEquals(ExcludeContentFragmentsImpl.LABEL, searchPredicate.getLabel());
    }

    @Test
    public void getLabel_Custom() {
        final ExcludeContentFragmentsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeContentFragmentsImpl(),
                "label", "Custom Label");

        assertEquals("Custom Label", searchPredicate.getLabel());
    }

    @Test
    public void getName() {
        final ExcludeContentFragmentsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeContentFragmentsImpl());

        assertEquals(ExcludeContentFragmentsImpl.NAME, searchPredicate.getName());
    }

    @Test
    public void getPredicateGroup() {
        final ExcludeContentFragmentsImpl searchPredicate = ctx.registerInjectActivateService(new ExcludeContentFragmentsImpl());

        final PredicateGroup predicateGroup = searchPredicate.getPredicateGroup(ctx.request());

        assertNotNull(predicateGroup);

        final Predicate property = predicateGroup.getByName("property");
        assertNotNull(property);
        assertEquals("not", property.get("operation"));
        assertEquals("jcr:content/contentFragment", property.get("property"));
    }

    @Test
    public void isRegisteredAsSearchPredicateService() {
        assertNotNull(ctx.registerInjectActivateService(new ExcludeContentFragmentsImpl()));
        assertTrue(ctx.getService(SearchPredicate.class) instanceof ExcludeContentFragmentsImpl);
    }
}
