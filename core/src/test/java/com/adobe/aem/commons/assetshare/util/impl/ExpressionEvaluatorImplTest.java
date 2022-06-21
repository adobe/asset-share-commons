package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionEvaluatorImplTest {
    @Rule
    public AemContext ctx = new AemContext();

    @Mock
    ModelFactory modelFactory;

    @Before
    public void setUp() throws Exception {
        ctx.load().json(getClass().getResourceAsStream("ExpressionEvaluatorImplTest.json"), "/content");

        ctx.addModelsForClasses(AssetModelImpl.class);

        MockAssetModels.mockModelFactory(ctx, modelFactory, "/content/dam/test.png");
        ctx.registerService(ModelFactory.class, modelFactory, org.osgi.framework.Constants.SERVICE_RANKING,
                Integer.MAX_VALUE);

        ctx.registerService(ExpressionEvaluator.class, new ExpressionEvaluatorImpl());

        ctx.currentResource("/content/dam/test.png");
    }

    @Test
    public void evaluateProperties() {
        final String expected = "https://foo.com/serve/metadata-property-value/metadata-property-value/jcr-content-property-value/bad-property/file.ext";
        ExpressionEvaluator expressionEvaluator = ctx.getService(ExpressionEvaluator.class);
        AssetModel assetModel = modelFactory.getModelFromWrappedRequest(ctx.request(), ctx.resourceResolver().getResource("/content/dam/test.png"), AssetModel.class);

        final String actual = expressionEvaluator.evaluateProperties("https://foo.com/serve/${prop@metadataProperty}/${prop@metadataProperty}/${prop@../jcrContentProperty}/bad-property${prop@badProperty}/file.ext", assetModel);

        assertEquals(expected, actual);
    }
}