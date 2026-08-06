package com.adobe.aem.commons.assetshare.util.impl;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.adobe.aem.commons.assetshare.content.impl.AssetModelImpl;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRendition;
import com.adobe.aem.commons.assetshare.testing.MockAssetModels;
import com.adobe.aem.commons.assetshare.util.ExpressionEvaluator;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    public void evaluateAssetsRenditionsExpressions() {
        final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluatorImpl();

        final Collection<AssetModel> assetModels = Arrays.asList(mock(AssetModel.class), mock(AssetModel.class));
        final Collection<String> renditionNames = Arrays.asList("original", "thumbnail", "web");

        final String actual = expressionEvaluator.evaluateAssetsRenditionsExpressions(
                "assets=${asset.count} renditions=${rendition.count} files=${file.count}", assetModels, renditionNames);

        assertEquals("assets=2 renditions=3 files=6", actual);
    }

    @Test
    public void evaluateDateTimeExpressions() {
        final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluatorImpl();

        final ZonedDateTime zonedDateTime = ZonedDateTime.of(2024, 3, 5, 14, 30, 0, 0, ZoneOffset.UTC);

        final String expected = String.format("%s-%s-%s (%s) %s:%s %s / %s",
                zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy")),
                zonedDateTime.format(DateTimeFormatter.ofPattern("MM")),
                zonedDateTime.format(DateTimeFormatter.ofPattern("dd")),
                zonedDateTime.format(DateTimeFormatter.ofPattern("EEE")),
                zonedDateTime.format(DateTimeFormatter.ofPattern("HH")),
                zonedDateTime.format(DateTimeFormatter.ofPattern("mm")),
                zonedDateTime.format(DateTimeFormatter.ofPattern("a")),
                zonedDateTime.format(DateTimeFormatter.ofPattern("hh")));

        final String actual = expressionEvaluator.evaluateDateTimeExpressions(
                "${year}-${month}-${day} (${day.name}) ${hour.24}:${minute} ${am.pm} / ${hour.12}", zonedDateTime);

        assertEquals(expected, actual);
    }

    @Test
    public void evaluateDynamicMediaExpression() {
        final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluatorImpl();

        final AssetModel assetModel = mock(AssetModel.class);
        final ValueMap properties = new ValueMapDecorator(new HashMap<>());
        properties.put("dam:scene7Name", "s7-name");
        properties.put("dam:scene7ID", "s7-id");
        properties.put("dam:scene7File", "company/folder/file-name");
        properties.put("dam:scene7FileAvs", "s7-file-avs");
        properties.put("dam:scene7Folder", "s7-folder");
        properties.put("dam:scene7Domain", "s7-domain");
        properties.put("dam:scene7APIServer", "s7-api-server");
        properties.put("dam:scene7CompanyID", "s7-company-id");
        when(assetModel.getProperties()).thenReturn(properties);

        final String expression = "${dm.name}|${dm.id}|${dm.file}|${dm.file.avs}|${dm.file.no-company}|${dm.folder}|${dm.domain}|${dm.api-server}|${dm.company.id}|${dm.company.name}";

        final String expected = "s7-name|s7-id|company/folder/file-name|s7-file-avs|file-name|s7-folder|s7-domain|s7-api-server|s7-company-id|company/folder";

        final String actual = expressionEvaluator.evaluateDynamicMediaExpression(expression, assetModel);

        assertEquals(expected, actual);
    }

    @Test
    public void evaluateAssetExpression() {
        final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluatorImpl();

        final AssetModel assetModel = mock(AssetModel.class);
        when(assetModel.getPath()).thenReturn("/content/dam/test.png");
        when(assetModel.getUrl()).thenReturn("/content/dam/test.png.asset.html");
        when(assetModel.getName()).thenReturn("test.png");

        final String expression = "${asset.path}|${asset.url}|${asset.name}|${asset.name.no-extension}|${asset.extension}";
        final String expected = "/content/dam/test.png|/content/dam/test.png.asset.html|test.png|test|png";

        final String actual = expressionEvaluator.evaluateAssetExpression(expression, assetModel);

        assertEquals(expected, actual);
    }

    @Test
    public void evaluateRenditionExpression_byName() {
        final ExpressionEvaluator expressionEvaluator = new ExpressionEvaluatorImpl();

        final String actual = expressionEvaluator.evaluateRenditionExpression("rendition=${rendition.name}", "original");

        assertEquals("rendition=original", actual);
    }

    @Test
    public void evaluateRenditionExpression_byAssetRendition() throws Exception {
        final ExpressionEvaluatorImpl expressionEvaluatorImpl = new ExpressionEvaluatorImpl();

        final MimeTypeService mimeTypeService = mock(MimeTypeService.class);
        when(mimeTypeService.getExtension("image/png")).thenReturn("png");
        setMimeTypeService(expressionEvaluatorImpl, mimeTypeService);

        final AssetRendition assetRendition = new AssetRendition("https://foo.com/test.png", 100L, "image/png");

        final String actual = expressionEvaluatorImpl.evaluateRenditionExpression("extension=${rendition.extension}", assetRendition);

        assertEquals("extension=png", actual);
    }

    private void setMimeTypeService(ExpressionEvaluatorImpl expressionEvaluatorImpl, MimeTypeService mimeTypeService) throws Exception {
        final Field field = ExpressionEvaluatorImpl.class.getDeclaredField("mimeTypeService");
        field.setAccessible(true);
        field.set(expressionEvaluatorImpl, mimeTypeService);
    }
}