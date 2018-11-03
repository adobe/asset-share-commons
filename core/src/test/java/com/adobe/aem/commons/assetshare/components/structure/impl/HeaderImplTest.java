package com.adobe.aem.commons.assetshare.components.structure.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.testing.mock.aem.junit.AemContext;

public class HeaderImplTest {
    
    private static final String RESOURCEPATH = "/content/asset-share-commons/en/light";
    
    @Rule
    public AemContext context = new AemContext();
    
    @Before
    public void setup() {
        context.load().json(getClass().getResourceAsStream("HeaderImplTest.json"),RESOURCEPATH);
        context.currentPage(RESOURCEPATH);
        context.currentResource(String.format("%s/%s", RESOURCEPATH,"jcr:content/root/main/header"));
    }
    
    @Test
    public void validateSimpleInvocation() {
        HeaderImpl header = context.getService(ModelFactory.class).createModel(context.request(),HeaderImpl.class);
        assertNotNull(header);
        assertEquals("/content/dam/asset-share-commons/en/site/Logo-light.png",header.getLogoPath());
        assertEquals(null,header.getSiteTitle()); // empty
    }

}
