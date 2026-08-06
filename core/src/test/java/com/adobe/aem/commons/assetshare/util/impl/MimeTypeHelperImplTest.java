package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.util.MimeTypeHelper;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MimeTypeHelperImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Test
    public void isBrowserSupportedImage_defaultConfig() {
        final MimeTypeHelper mimeTypeHelper = ctx.registerInjectActivateService(new MimeTypeHelperImpl());

        assertTrue(mimeTypeHelper.isBrowserSupportedImage("image/png"));
        assertTrue(mimeTypeHelper.isBrowserSupportedImage("image/jpeg"));
        assertFalse(mimeTypeHelper.isBrowserSupportedImage("application/pdf"));
        assertFalse(mimeTypeHelper.isBrowserSupportedImage(null));
    }

    @Test
    public void isBrowserSupportedImage_customConfig() {
        final MimeTypeHelper mimeTypeHelper = ctx.registerInjectActivateService(new MimeTypeHelperImpl(),
                "browserSupportedImageMimeTypes", new String[]{"image/custom"});

        assertTrue(mimeTypeHelper.isBrowserSupportedImage("image/custom"));
        assertFalse(mimeTypeHelper.isBrowserSupportedImage("image/png"));
    }
}
