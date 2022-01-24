package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.ServletHelper;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.scripting.SlingBindings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.apache.sling.api.scripting.SlingBindings.SLING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServletHelperImplTest {
    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
        ctx.registerInjectActivateService(new ServletHelperImpl());

    }

    @Test
    public void addSlingBindings() {
        ServletHelper servletHelper = ctx.getService(ServletHelper.class);

        servletHelper.addSlingBindings(ctx.request(), ctx.response());

        SlingBindings actual = (SlingBindings) ctx.request().getAttribute(SlingBindings.class.getName());

        assertEquals(ctx.resourceResolver(), actual.getResourceResolver());
        assertNotNull(actual.get(SLING));
        assertEquals(ctx.request(), actual.getRequest());
        assertEquals(ctx.response(), actual.getResponse());
        assertEquals(ctx.currentResource(), actual.getResource());
    }
}