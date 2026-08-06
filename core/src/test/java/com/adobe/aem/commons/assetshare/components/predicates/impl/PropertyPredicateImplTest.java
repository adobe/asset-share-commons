package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.aem.commons.assetshare.components.predicates.AbstractPredicate;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.SelectedOptionItem;
import com.adobe.aem.commons.assetshare.components.predicates.impl.options.UnselectedOptionItem;
import com.adobe.aem.commons.assetshare.testing.ComponentManagerMock;
import com.adobe.aem.commons.assetshare.testing.ReflectionTestUtil;
import com.adobe.aem.commons.assetshare.util.JsonResolver;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.adobe.cq.wcm.core.components.models.form.Options;
import com.day.cq.search.PredicateGroup;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertyPredicateImplTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Before
    public void setUp() throws Exception {
    }

    private PropertyPredicateImpl createImpl(String path, Map<String, Object> props, Options coreOptions, JsonResolver jsonResolver) {
        Resource resource = ctx.resourceResolver().getResource(path);
        if (resource == null) {
            resource = ctx.create().resource(path, props);
        }
        ctx.currentResource(path);

        final PropertyPredicateImpl impl = new PropertyPredicateImpl();
        ReflectionTestUtil.setField(impl, AbstractPredicate.class, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "request", ctx.request());
        ReflectionTestUtil.setField(impl, "response", ctx.response());
        ReflectionTestUtil.setField(impl, "resource", resource);
        ReflectionTestUtil.setField(impl, "coreOptions", coreOptions);
        ReflectionTestUtil.setField(impl, "jsonResolver", jsonResolver);
        ReflectionTestUtil.setField(impl, "label", props.get("label"));
        ReflectionTestUtil.setField(impl, "property", props.get("property"));
        ReflectionTestUtil.setField(impl, "operation", props.get("operation"));
        ReflectionTestUtil.setField(impl, "expanded", props.get("expanded"));
        ReflectionTestUtil.setField(impl, "typeString", props.get("type"));
        ReflectionTestUtil.setField(impl, "and", props.getOrDefault("and", false));
        ReflectionTestUtil.setField(impl, "source", props.get("source"));
        ReflectionTestUtil.setField(impl, "jsonSource", props.get("jsonSource"));
        ReflectionTestUtil.setField(impl, "datasourceRT", props.get("datasourceRT"));
        impl.init();

        return impl;
    }

    private OptionItem optionItem(String text, String value) {
        OptionItem item = mock(OptionItem.class);
        when(item.getText()).thenReturn(text);
        when(item.getValue()).thenReturn(value);
        return item;
    }

    @Test
    public void getProperty() {
        Map<String, Object> props = new HashMap<>();
        props.put("property", "jcr:content/metadata/dc:format");
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property1", props, coreOptions, mock(JsonResolver.class));

        assertEquals("jcr:content/metadata/dc:format", impl.getProperty());
    }

    @Test
    public void getName_isPropertyValuesPredicateName() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property2", props, coreOptions, mock(JsonResolver.class));

        assertEquals("propertyvalues", impl.getName());
    }

    @Test
    public void hasOperation_andGetOperation() {
        Map<String, Object> props = new HashMap<>();
        props.put("operation", "equals");
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property3", props, coreOptions, mock(JsonResolver.class));

        assertTrue(impl.hasOperation());
        assertEquals("equals", impl.getOperation());
    }

    @Test
    public void hasOperation_falseWhenBlank() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property4", props, coreOptions, mock(JsonResolver.class));

        assertFalse(impl.hasOperation());
    }

    @Test
    public void hasAnd_andGetAnd() {
        Map<String, Object> props = new HashMap<>();
        props.put("and", true);
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property5", props, coreOptions, mock(JsonResolver.class));

        assertTrue(impl.hasAnd());
        assertEquals(Boolean.TRUE, impl.getAnd());
    }

    @Test
    public void isReady_falseWhenNoItems() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property6", props, coreOptions, mock(JsonResolver.class));

        assertFalse(impl.isReady());
    }

    @Test
    public void isReady_trueWhenItemsPresent() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Text", "value1"));
        when(coreOptions.getItems()).thenReturn(items);

        PropertyPredicateImpl impl = createImpl("/content/property7", props, coreOptions, mock(JsonResolver.class));

        assertTrue(impl.isReady());
    }

    @Test
    public void getItems_delegatesToCoreOptions_defaultSelected() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Text1", "value1"));
        items.add(optionItem("Text2", "value2"));
        when(coreOptions.getItems()).thenReturn(items);

        PropertyPredicateImpl impl = createImpl("/content/property8", props, coreOptions, mock(JsonResolver.class));

        // Not a parameterized search request, so items default to whatever coreOptions provides (not wrapped as Unselected).
        List<OptionItem> result = impl.getItems();
        assertEquals(2, result.size());
        assertFalse(result.get(0) instanceof SelectedOptionItem);
        assertFalse(result.get(0) instanceof UnselectedOptionItem);
    }

    @Test
    public void getItems_selectedWhenInQueryParams() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Text1", "value1"));
        when(coreOptions.getItems()).thenReturn(items);

        PropertyPredicateImpl impl = createImpl("/content/property9", props, coreOptions, mock(JsonResolver.class));
        ctx.request().setQueryString(impl.getGroup() + ".propertyvalues.0_values=value1&p.limit=10");

        List<OptionItem> result = impl.getItems();
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof SelectedOptionItem);
    }

    @Test
    public void getItems_unselectedWhenParameterizedAndNotSelected() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("Text1", "value1"));
        when(coreOptions.getItems()).thenReturn(items);

        PropertyPredicateImpl impl = createImpl("/content/property10", props, coreOptions, mock(JsonResolver.class));
        // Parameterized search request (contains the "&p." marker), but value1 is not selected.
        ctx.request().setQueryString("foo=bar&p.limit=10");

        List<OptionItem> result = impl.getItems();
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof UnselectedOptionItem);
    }

    @Test
    public void getItems_fromJsonSource() {
        Map<String, Object> props = new HashMap<>();
        props.put("source", "json");
        props.put("jsonSource", "/content/dam/options.json");
        Options coreOptions = mock(Options.class);

        JsonResolver jsonResolver = mock(JsonResolver.class);
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(
                "{ \"options\": [ { \"text\": \"A\", \"value\": \"a\" } ] }", JsonObject.class);
        when(jsonResolver.resolveJson(any(), any(), Mockito.eq("/content/dam/options.json"))).thenReturn(jsonElement);

        PropertyPredicateImpl impl = createImpl("/content/property11", props, coreOptions, jsonResolver);

        List<OptionItem> result = impl.getItems();
        assertEquals(1, result.size());
        assertEquals("a", result.get(0).getValue());
    }

    @Test
    public void getItems_fromJsonSource_nullJson() {
        Map<String, Object> props = new HashMap<>();
        props.put("source", "json");
        props.put("jsonSource", "/content/dam/missing.json");
        Options coreOptions = mock(Options.class);

        JsonResolver jsonResolver = mock(JsonResolver.class);
        when(jsonResolver.resolveJson(any(), any(), any())).thenReturn(null);

        PropertyPredicateImpl impl = createImpl("/content/property12", props, coreOptions, jsonResolver);

        List<OptionItem> result = impl.getItems();
        assertTrue(result.isEmpty());
    }

    @Test
    public void getType_delegatesToCoreOptions() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getType()).thenReturn(Options.Type.CHECKBOX);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property13", props, coreOptions, mock(JsonResolver.class));

        assertEquals(Options.Type.CHECKBOX, impl.getType());
    }

    @Test
    public void getSubType() {
        Map<String, Object> props = new HashMap<>();
        props.put("type", "checkbox-group");
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property14", props, coreOptions, mock(JsonResolver.class));

        assertEquals("checkbox-group", impl.getSubType());
    }

    @Test
    public void getPredicateGroup_noResource() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property15", props, coreOptions, mock(JsonResolver.class));
        ReflectionTestUtil.setField(impl, "resource", null);

        PredicateGroup group = impl.getPredicateGroup();
        assertEquals(0, group.size());
    }

    @Test
    public void getPredicateGroup_withSelectedItemsAndAnd() {
        Map<String, Object> props = new HashMap<>();
        props.put("property", "jcr:content/metadata/dc:format");
        props.put("operation", "equals");
        props.put("and", true);

        Options coreOptions = mock(Options.class);
        List<OptionItem> items = new ArrayList<>();
        items.add(optionItem("PDF", "application/pdf"));
        when(coreOptions.getItems()).thenReturn(items);

        PropertyPredicateImpl impl = createImpl("/content/property16", props, coreOptions, mock(JsonResolver.class));
        ctx.request().setQueryString(impl.getGroup() + ".propertyvalues.0_values=application%2Fpdf");

        PredicateGroup group = impl.getPredicateGroup();
        assertEquals(1, group.size());
        com.day.cq.search.Predicate predicate = group.get(0);
        assertEquals("application/pdf", predicate.get("0_values"));
        assertEquals("jcr:content/metadata/dc:format", predicate.get("property"));
        assertEquals("equals", predicate.get("operation"));
        assertEquals("true", predicate.get("and"));
    }

    @Test
    public void getInitialValue_andGetInitialValues() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property17", props, coreOptions, mock(JsonResolver.class));
        ctx.request().setQueryString(impl.getGroup() + ".propertyvalues.0_values=hello");

        ValueMap initialValues = impl.getInitialValues();
        assertFalse(initialValues.isEmpty());
    }

    @Test
    public void getExportedType() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property18", props, coreOptions, mock(JsonResolver.class));

        assertEquals("asset-share-commons/components/search/property", impl.getExportedType());
    }

    @Test
    public void abstractPredicate_coreFieldDelegation() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());
        when(coreOptions.getTitle()).thenReturn("The Title");
        when(coreOptions.getValue()).thenReturn("The Value");
        when(coreOptions.getHelpMessage()).thenReturn("The Help Message");

        PropertyPredicateImpl impl = createImpl("/content/property20", props, coreOptions, mock(JsonResolver.class));

        // AbstractPredicate#getTitle()/getValue()/getHelpMessage() delegate to the Core Field (coreOptions here).
        assertEquals("The Title", impl.getTitle());
        assertEquals("The Value", impl.getValue());
        assertEquals("The Help Message", impl.getHelpMessage());
    }

    @Test
    public void abstractPredicate_getId_existingResource() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property21", props, coreOptions, mock(JsonResolver.class));

        assertEquals("cmp-" + impl.getName() + "_" + String.valueOf("/content/property21".hashCode()), impl.getId());
    }

    @Test
    public void abstractPredicate_getFormId_incrementsPerRequest() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property22", props, coreOptions, mock(JsonResolver.class));

        assertEquals("asset-share-commons__form-id__1", impl.getFormId());
        // Calling it again on the same request returns the same tracked value (not incremented again).
        assertEquals("asset-share-commons__form-id__1", impl.getFormId());
    }

    @Test
    public void abstractPredicate_isExpanded_trueWhenAuthoredExpanded() {
        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl = createImpl("/content/property23", props, coreOptions, mock(JsonResolver.class));
        // AbstractPredicate#isExpanded() reads its OWN "expanded" field (distinct from
        // PropertyPredicateImpl's own shadowed "expanded" field, which is unrelated to this method).
        ReflectionTestUtil.setField(impl, AbstractPredicate.class, "expanded", true);

        assertTrue(impl.isExpanded());
    }

    @Test
    public void abstractPredicate_groupIdGeneration_reusesTrackedGroupForSamePath() {
        // Exercises AbstractPredicate#initGroup / #generateGroupId including the branch where a
        // group id has ALREADY been tracked for the current resource path on the request.
        ComponentManagerMock.setComponentProperties(ctx, new HashMap<String, Object>() {{
            put("generatePredicateGroupId", true);
        }});

        Map<String, Object> props = new HashMap<>();
        Options coreOptions = mock(Options.class);
        when(coreOptions.getItems()).thenReturn(new ArrayList<>());

        PropertyPredicateImpl impl1 = createImpl("/content/property19", props, coreOptions, mock(JsonResolver.class));
        PropertyPredicateImpl impl2 = createImpl("/content/property19", props, coreOptions, mock(JsonResolver.class));

        // Both predicates resolve to the same tracked group for the shared resource path.
        assertEquals(impl1.getGroup(), impl2.getGroup());
    }

    @Test
    public void getKeyValuePairsFromJson_textValue() {
        PropertyPredicateImpl propertyPredicate = new PropertyPredicateImpl();

        String json = "{ \"options\": [ { \"text\": \"the text 1\", \"value\": \"the value 1\" }, { \"text\": \"the text 2\", \"value\": \"the value 2\" } ] }";

        Gson gson = new Gson();

        JsonElement jsonElement = gson.fromJson(json, JsonObject.class);

        List<OptionItem> result = propertyPredicate.getOptionItemsFromJson(jsonElement);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("the text 1", result.get(0).getText());
        assertEquals("the value 1", result.get(0).getValue());
        assertEquals("the text 2", result.get(1).getText());
        assertEquals("the value 2", result.get(1).getValue());
    }

    @Test
    public void getKeyValuePairsFromJson_jcrTitleValue() {
        PropertyPredicateImpl propertyPredicate = new PropertyPredicateImpl();

        String json = "[ { \"text\": \"the text 1\", \"value\": \"the value 1\" }, { \"text\": \"the text 2\", \"value\": \"the value 2\" } ]";

        Gson gson = new Gson();

        JsonElement jsonElement = gson.fromJson(json, JsonArray.class);

        List<OptionItem> result = propertyPredicate.getOptionItemsFromJson(jsonElement);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("the text 1", result.get(0).getText());
        assertEquals("the value 1", result.get(0).getValue());
        assertEquals("the text 2", result.get(1).getText());
        assertEquals("the value 2", result.get(1).getValue());
    }
}