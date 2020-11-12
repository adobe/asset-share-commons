package com.adobe.aem.commons.assetshare.content.renditions.download.impl;

import com.adobe.aem.commons.assetshare.content.renditions.download.AssetRenditionsException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssetRenditionsExceptionTest {
    @Test
    public void getMessage() {
        AssetRenditionsException exception = new AssetRenditionsException("Hello Error");
        assertEquals("Hello Error", exception.getMessage());
    }
}