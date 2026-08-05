package com.adobe.aem.commons.assetshare.util;

import com.adobe.aem.commons.assetshare.components.predicates.Predicate;
import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PredicateUtilTest {

    @Rule
    public final AemContext ctx = new AemContext();

    @Test
    public void findPredicate() {
        final String PATH = "/content/dam/a";

        final Map<String, String> input = new HashMap<>();
        input.put("group.path", PATH);
        input.put("group.path.path", PATH);
        input.put("group.path.1_path", PATH);
        input.put("1_group.path", PATH);
        input.put("1_group.path.path", PATH);
        input.put("1_group.path.1_path", PATH);
        input.put("1_group.1_path", PATH);
        input.put("1_group.1_path.path", PATH);
        input.put("1_group.1_path.1_path", PATH);

        input.put("type", "dam:Asset");

        final Map<String, String> expected = new HashMap<>();
        expected.put("group.path", PATH);
        expected.put("group.path.path", PATH);
        expected.put("group.path.1_path", PATH);
        expected.put("1_group.path", PATH);
        expected.put("1_group.path.path", PATH);
        expected.put("1_group.path.1_path", PATH);
        expected.put("1_group.1_path", PATH);
        expected.put("1_group.1_path.path", PATH);
        expected.put("1_group.1_path.1_path", PATH);

        ValueMap actual = PredicateUtil.findPredicate(input, "path", "path");

        assertTrue(expected.keySet().equals(actual.keySet()));
    }

    @Test
    public void hasPredicate_True() {
        final String PATH = "/content/dam/a";

        Map<String, String> input = new HashMap<>();

        input.put("group.path", PATH);
        assertTrue(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("group.path.path", PATH);
        assertTrue(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("group.path.1_path", PATH);
        assertTrue(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("1_group.path.path", PATH);
        assertTrue(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("1_group.path.1_path", PATH);
        assertTrue(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("1_group.1_path", PATH);
        assertTrue(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("1_group.1_path.path", PATH);
        assertTrue(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("12_group.1_path.1_path", PATH);
        assertTrue(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("type", "dam:Asset");
        assertFalse(PredicateUtil.hasPredicate(input, new String[] { "path" }));
    }

    @Test
    public void hasPredicate_False() {
        final String PATH = "/content/dam/a";

        Map<String, String> input = new HashMap<>();

        input = new HashMap<>();
        input.put("type", "dam:Asset");
        assertFalse(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("pathy", "test");
        assertFalse(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("0_pathy", "test");
        assertFalse(PredicateUtil.hasPredicate(input, new String[] { "path" }));


        input = new HashMap<>();
        input.put("group.pathy", "test");
        assertFalse(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("group.pathy.path", "test");
        assertFalse(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("11_group.pathy", "test");
        assertFalse(PredicateUtil.hasPredicate(input, new String[] { "path" }));

        input = new HashMap<>();
        input.put("12_group.pathy.path", "test");
        assertFalse(PredicateUtil.hasPredicate(input, new String[] { "path" }));
    }

    @Test
    public void isParameterizedSearchRequest_True() {
        ctx.request().setQueryString("5_group.propertyvalues.property=.%2Fjcr%3Acontent%2Fmetadata%2Fdc%3Aformat&5_group.propertyvalues.operation=equals&5_group.propertyvalues.0_values=application%2Fpdf&5_group.propertyvalues.1_values=image%2Fjpeg&orderby=%40jcr%3Acontent%2Fjcr%3AlastModified&orderby.sort=desc&layout=card&p.offset=0&p.limit=24");
        assertTrue(PredicateUtil.isParameterizedSearchRequest(ctx.request()));
    }

    @Test
    public void isParameterizedSearchRequest_False() {
        ctx.request().setQueryString("");
        assertFalse(PredicateUtil.isParameterizedSearchRequest(ctx.request()));

        ctx.request().setQueryString("wcmmode=disabled");
        assertFalse(PredicateUtil.isParameterizedSearchRequest(ctx.request()));

        ctx.request().setQueryString("marketingid=123&script=alert('XSS')");
        assertFalse(PredicateUtil.isParameterizedSearchRequest(ctx.request()));
    }

    @Test
    public void getParamFromQueryParams_found() {
        final Map<String, Object> params = new HashMap<>();
        params.put("myParam", "myValue");
        ctx.request().setParameterMap(params);

        assertEquals("myValue", PredicateUtil.getParamFromQueryParams(ctx.request(), "myParam"));
    }

    @Test
    public void getParamFromQueryParams_notFound_returnsEmptyString() {
        assertEquals("", PredicateUtil.getParamFromQueryParams(ctx.request(), "doesNotExist"));
    }

    @Test
    public void isOptionInInitialValues_optionItem_matchesStringValue() {
        final OptionItem optionItem = mock(OptionItem.class);
        when(optionItem.getValue()).thenReturn("value-1");

        final ValueMap initialValues = new ValueMapDecorator(new HashMap<>());
        initialValues.put("someKey", "value-1");

        assertTrue(PredicateUtil.isOptionInInitialValues(optionItem, initialValues));
    }

    @Test
    public void isOptionInInitialValues_string_matchesStringArrayValue() {
        final ValueMap initialValues = new ValueMapDecorator(new HashMap<>());
        initialValues.put("someKey", new String[]{"value-1", "value-2"});

        assertTrue(PredicateUtil.isOptionInInitialValues("value-2", initialValues));
    }

    @Test
    public void isOptionInInitialValues_string_noMatch() {
        final ValueMap initialValues = new ValueMapDecorator(new HashMap<>());
        initialValues.put("someKey", "value-1");
        initialValues.put("otherKey", new String[]{"value-2", "value-3"});

        assertFalse(PredicateUtil.isOptionInInitialValues("value-not-present", initialValues));
    }

    @Test
    public void isOptionInInitialValues_unsupportedValueType_noMatch() {
        final ValueMap initialValues = new ValueMapDecorator(new HashMap<>());
        initialValues.put("someKey", 12345L);

        assertFalse(PredicateUtil.isOptionInInitialValues("12345", initialValues));
    }

    @Test
    public void getInitialValue_directGroupNameValueNameMatch() {
        final Predicate predicate = mock(Predicate.class);
        when(predicate.getGroup()).thenReturn("group");
        when(predicate.getName()).thenReturn("name");

        final Map<String, Object> params = new HashMap<>();
        params.put("group.name.valueName", "direct-value");
        ctx.request().setParameterMap(params);

        assertEquals("direct-value", PredicateUtil.getInitialValue(ctx.request(), predicate, "valueName"));
    }

    @Test
    public void getInitialValue_fallsBackToGroupNameWhenValueNameBlank() {
        final Predicate predicate = mock(Predicate.class);
        when(predicate.getGroup()).thenReturn("group");
        when(predicate.getName()).thenReturn("name");

        final Map<String, Object> params = new HashMap<>();
        params.put("group.name", "fallback-value");
        ctx.request().setParameterMap(params);

        assertEquals("fallback-value", PredicateUtil.getInitialValue(ctx.request(), predicate, ""));
    }

    @Test
    public void getInitialValue_fallsBackToGroupNameWhenValueNameEqualsName() {
        final Predicate predicate = mock(Predicate.class);
        when(predicate.getGroup()).thenReturn("group");
        when(predicate.getName()).thenReturn("name");

        final Map<String, Object> params = new HashMap<>();
        params.put("group.name", "fallback-value-2");
        ctx.request().setParameterMap(params);

        assertEquals("fallback-value-2", PredicateUtil.getInitialValue(ctx.request(), predicate, "name"));
    }

    @Test
    public void getInitialValue_notFound_returnsEmptyString() {
        final Predicate predicate = mock(Predicate.class);
        when(predicate.getGroup()).thenReturn("group");
        when(predicate.getName()).thenReturn("name");

        assertEquals("", PredicateUtil.getInitialValue(ctx.request(), predicate, "valueName"));
    }

    @Test
    public void getInitialValues_collectsMatchingRequestParameters() {
        final Predicate predicate = mock(Predicate.class);
        when(predicate.getGroup()).thenReturn("group");
        when(predicate.getName()).thenReturn("path");

        final Map<String, Object> params = new HashMap<>();
        params.put("group.path.value", "/content/dam/a");
        params.put("group.1_path.1_value", "/content/dam/b");
        params.put("unrelated", "should-not-be-included");
        ctx.request().setParameterMap(params);

        final ValueMap actual = PredicateUtil.getInitialValues(ctx.request(), predicate, "value");

        assertTrue(actual.containsKey("group.path.value"));
        assertTrue(actual.containsKey("group.1_path.1_value"));
        assertFalse(actual.containsKey("unrelated"));
    }

    @Test
    public void getInitialValues_noMatches_returnsEmptyValueMap() {
        final Predicate predicate = mock(Predicate.class);
        when(predicate.getGroup()).thenReturn("group");
        when(predicate.getName()).thenReturn("path");

        final Map<String, Object> params = new HashMap<>();
        params.put("unrelated", "value");
        ctx.request().setParameterMap(params);

        final ValueMap actual = PredicateUtil.getInitialValues(ctx.request(), predicate, "value");

        assertTrue(actual.isEmpty());
    }
}
