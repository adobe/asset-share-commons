package com.adobe.aem.commons.assetshare.util;

import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PredicateUtilTest {

    @Test
    void findPredicate() {
        final Map<String, String> input = new HashMap<>();
        input.put("group.path", "/content/dam/a/b");
        input.put("group.path.path", "/content/dam/a/b");
        input.put("group.path.1_path", "/content/dam/a/b");
        input.put("1_group.path", "/content/dam/a/b");
        input.put("1_group.path.path", "/content/dam/a/b");
        input.put("1_group.path.1_path", "/content/dam/a/b");
        input.put("1_group.1_path", "/content/dam/a/b");
        input.put("1_group.1_path.path", "/content/dam/a/b");
        input.put("1_group.1_path.1_path", "/content/dam/a/b");

        input.put("type", "dam:Asset");

        final Map<String, String> expected = new HashMap<>();
        expected.put("group.path", "/content/dam/a/b");
        expected.put("group.path.path", "/content/dam/a/b");
        expected.put("group.path.1_path", "/content/dam/a/b");
        expected.put("1_group.path", "/content/dam/a/b");
        expected.put("1_group.path.path", "/content/dam/a/b");
        expected.put("1_group.path.1_path", "/content/dam/a/b");
        expected.put("1_group.1_path", "/content/dam/a/b");
        expected.put("1_group.1_path.path", "/content/dam/a/b");
        expected.put("1_group.1_path.1_path", "/content/dam/a/b");

        ValueMap actual = PredicateUtil.findPredicate(input, "path", "path");

        assertTrue(expected.keySet().equals(actual.keySet()));
    }
}
