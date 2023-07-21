package com.adobe.aem.commons.assetshare.components.predicates.impl;

import com.adobe.cq.wcm.core.components.models.form.OptionItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PropertyPredicateImplTest {

    @Before
    public void setUp() throws Exception {
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