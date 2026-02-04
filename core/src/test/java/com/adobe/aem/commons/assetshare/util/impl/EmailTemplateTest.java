package com.adobe.aem.commons.assetshare.util.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EmailTemplateTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testReplace_singleVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("name", "World");
        String result = EmailTemplate.SimpleSubstitutor.replace("Hello, ${name}!", vars);
        assertEquals("Hello, World!", result);
    }

    @Test
    public void testReplace_multipleVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.put("greeting", "Hi");
        vars.put("name", "Alice");
        String result = EmailTemplate.SimpleSubstitutor.replace("${greeting}, ${name}!", vars);
        assertEquals("Hi, Alice!", result);
    }

    @Test
    public void testReplace_missingVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("foo", "bar");
        String result = EmailTemplate.SimpleSubstitutor.replace("Value: ${missing}", vars);
        assertEquals("Value: ${missing}", result);
    }

    @Test
    public void testReplace_noVariables() {
        Map<String, String> vars = Collections.emptyMap();
        String result = EmailTemplate.SimpleSubstitutor.replace("No variables here.", vars);
        assertEquals("No variables here.", result);
    }

    @Test
    public void testReplace_nullTemplate() {
        Map<String, String> vars = new HashMap<>();
        String result = EmailTemplate.SimpleSubstitutor.replace(null, vars);
        assertEquals(null, result);
    }

    @Test
    public void testReplace_emptyTemplate() {
        Map<String, String> vars = new HashMap<>();
        String result = EmailTemplate.SimpleSubstitutor.replace("", vars);
        assertEquals("", result);
    }

    @Test
    public void testReplace_nullVars() {
        String result = EmailTemplate.SimpleSubstitutor.replace("Hello, ${name}!", null);
        assertEquals("Hello, ${name}!", result);
    }

    @Test
    public void testReplace_unclosedVariable() {
        Map<String, String> vars = new HashMap<>();
        vars.put("foo", "bar");
        String result = EmailTemplate.SimpleSubstitutor.replace("Test ${foo", vars);
        assertEquals("Test ${foo", result);
    }

    @Test
    public void testReplace_adjacentVariables() {
        Map<String, String> vars = new HashMap<>();
        vars.put("a", "1");
        vars.put("b", "2");
        String result = EmailTemplate.SimpleSubstitutor.replace("${a}${b}", vars);
        assertEquals("12", result);
    }
}