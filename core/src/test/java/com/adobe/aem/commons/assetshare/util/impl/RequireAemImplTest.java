package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.RequireAem;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RequireAemImplTest {

    @Rule
    public AemContext ctx = new AemContext();

    private static final String CLOUD_ONLY_VARIABLE_OSGI_PROPERTY = "cloud.only.variable";

    @Test
    public void isRunningInAdobeCloud_False() {
        ctx.registerInjectActivateService(new RequireAemImpl(), CLOUD_ONLY_VARIABLE_OSGI_PROPERTY, RequireAemImpl.CLOUD_ONLY_OSGI_PROPERTY_VALUE);

        RequireAem requireAem = ctx.getService(RequireAem.class);

        assertFalse(requireAem.isRunningInAdobeCloud());
    }

    @Test
    public void isRunningInAdobeCloud_True() {
        ctx.registerInjectActivateService(new RequireAemImpl(), CLOUD_ONLY_VARIABLE_OSGI_PROPERTY, "p1234");

        RequireAem requireAem = ctx.getService(RequireAem.class);

        assertTrue(requireAem.isRunningInAdobeCloud());
    }
}