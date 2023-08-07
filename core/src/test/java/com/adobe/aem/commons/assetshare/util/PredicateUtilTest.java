package com.adobe.aem.commons.assetshare.util;

import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

}
