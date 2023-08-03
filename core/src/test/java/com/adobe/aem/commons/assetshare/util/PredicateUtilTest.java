package com.adobe.aem.commons.assetshare.util;

import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PredicateUtilTest {

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
}
