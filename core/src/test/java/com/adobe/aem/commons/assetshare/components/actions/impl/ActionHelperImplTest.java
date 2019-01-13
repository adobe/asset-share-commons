package com.adobe.aem.commons.assetshare.components.actions.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.adobe.aem.commons.assetshare.components.actions.ActionHelper;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;

@RunWith(MockitoJUnitRunner.class)
public class ActionHelperImplTest {
    
    private static final String REQ_PARAM = "assets";
    
    @Rule
    public SlingContext context = new SlingContext();
    
    ActionHelper actionHelper = new ActionHelperImpl();
    
    @Before
    public void setup() {
        context.registerService(ModelFactory.class);
        context.addModelsForClasses(AssetModelImpl.class);
        context.registerInjectActivateService(actionHelper);
        context.load().json(ActionHelperImplTest.class.getResourceAsStream("ActionHelperImplTest.json"), "/assets");
        
    }
    
    @Test
    @Ignore
    public void testWithData() {
        Map<String,Object> requestParameters = new HashMap<>();
        String[] assets = {"/assets/we-retail-logo.png","/assets/Logo-on-dark.png"};
        requestParameters.put(REQ_PARAM, assets);
        context.request().setParameterMap(requestParameters);
       
        Collection<AssetModel> models = actionHelper.getAssetsFromQueryParameter(context.request(), REQ_PARAM);
        assertNotNull(models);
        assertEquals(2, models.size());
    }

}
