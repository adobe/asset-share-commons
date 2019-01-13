package com.adobe.aem.commons.assetshare.components.structure.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.adobe.aem.commons.assetshare.components.structure.Header;

import io.wcm.testing.mock.aem.junit.AemContext;

public class HeaderImplTest {
    
    private static final String RESOURCEPATH = "/content";
    
    @Rule
    public AemContext context = new AemContext();
    
    @Before
    public void setup() {
        context.load().json(getClass().getResourceAsStream("HeaderImplTest.json"),RESOURCEPATH);
    }
    
    @Test
    public void testStandardInvocation() {
        context.currentPage(String.format("%s/%s", RESOURCEPATH,"page_complete"));
        context.currentResource(String.format("%s/%s", RESOURCEPATH,"page_complete/jcr:content/root/main/header"));
        HeaderImpl header = context.getService(ModelFactory.class).createModel(context.request(),HeaderImpl.class);
        assertNotNull(header);
        assertEquals("/content/dam/asset-share-commons/en/site/Logo-light.png",header.getLogoPath());
        assertNull(header.getSiteTitle()); // empty
        
        Collection<Header.NavigationItem> items = header.getItems();
        assertNotNull(items);
        assertEquals(3,items.size());
        
    }

}
