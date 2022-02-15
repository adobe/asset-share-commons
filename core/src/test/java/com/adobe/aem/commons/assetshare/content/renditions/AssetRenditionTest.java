package com.adobe.aem.commons.assetshare.content.renditions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssetRenditionTest {

    @Test
    public void getBinaryUri() {

        AssetRendition assetRendition = new AssetRendition("/content/dam/test.jpg", 123L, "image/jpeg");
        assertEquals("/content/dam/test.jpg", assetRendition.getBinaryUri().toString());

        assetRendition = new AssetRendition("/content/dam/test asset.jpg", 123L, "image/jpeg");
        assertEquals("/content/dam/test%20asset.jpg", assetRendition.getBinaryUri().toString());

        assetRendition = new AssetRendition("/content/dam/test asset.jpg/jcr:content/renditions.cq5dam.web.1.2.png", 123L, "image/jpeg");
        assertEquals("/content/dam/test%20asset.jpg/jcr:content/renditions.cq5dam.web.1.2.png", assetRendition.getBinaryUri().toString());

        assetRendition = new AssetRendition("/content/dam/test+with+plus.jpg", 123L, "image/jpeg");
        assertEquals("/content/dam/test+with+plus.jpg", assetRendition.getBinaryUri().toString());

        assetRendition = new AssetRendition("https://test.com/content/dam/test asset.jpg", 123L, "image/jpeg");
        assertEquals("https://test.com/content/dam/test%20asset.jpg", assetRendition.getBinaryUri().toString());

        assetRendition = new AssetRendition("https://smartimaging.scene7.com/is/image/DynamicMediaNA/test (test):Medium", 123L, "image/jpeg");
        assertEquals("https://smartimaging.scene7.com/is/image/DynamicMediaNA/test%20(test):Medium", assetRendition.getBinaryUri().toString());

        assetRendition = new AssetRendition("https://smartimaging.scene7.com/is/image/DynamicMediaNA/test (test)?$grayscale$", 123L, "image/jpeg");
        assertEquals("https://smartimaging.scene7.com/is/image/DynamicMediaNA/test%20(test)?$grayscale$", assetRendition.getBinaryUri().toString());
    }

    @Test
    public void getSize() {
        AssetRendition assetRendition = new AssetRendition("/content/dam/test.jpg", 123L, "image/jpeg");
        assertEquals(Long.valueOf(123), assetRendition.getSize().get());
    }

    @Test
    public void getMimeType() {
        AssetRendition assetRendition = new AssetRendition("/content/dam/test.jpg", 123L, "image/jpeg");
        assertEquals("image/jpeg", assetRendition.getMimeType());
    }
}