/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2013 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.commons.util;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParameterUtilTest {

    @Test
    public void toMapEntryWithOptionalValue_withValue() {
        final Map.Entry<String, String> entry = ParameterUtil.toMapEntryWithOptionalValue("foo:bar", ":");
        assertEquals("foo", entry.getKey());
        assertEquals("bar", entry.getValue());
    }

    @Test
    public void toMapEntryWithOptionalValue_noSeparator_returnsNullValue() {
        final Map.Entry<String, String> entry = ParameterUtil.toMapEntryWithOptionalValue("foo", ":");
        assertEquals("foo", entry.getKey());
        assertNull(entry.getValue());
    }

    @Test
    public void toMapEntryWithOptionalValue_blank_returnsNull() {
        assertNull(ParameterUtil.toMapEntryWithOptionalValue("", ":"));
    }

    @Test
    public void toMapEntry_withValue() {
        final Map.Entry<String, String> entry = ParameterUtil.toMapEntry("foo:bar", ":");
        assertEquals("foo", entry.getKey());
        assertEquals("bar", entry.getValue());
    }

    @Test
    public void toMapEntry_noSeparator_returnsNull() {
        assertNull(ParameterUtil.toMapEntry("foo", ":"));
    }

    @Test
    public void toMap_simple() {
        final Map<String, String> map = ParameterUtil.toMap(new String[]{"dog:woof", "cat:meow"}, ":");
        assertEquals("woof", map.get("dog"));
        assertEquals("meow", map.get("cat"));
        assertEquals(2, map.size());
    }

    @Test
    public void toMap_nullValues_returnsEmptyMap() {
        final Map<String, String> map = ParameterUtil.toMap(null, ":");
        assertTrue(map.isEmpty());
    }

    @Test
    public void toMap_emptyValues_returnsEmptyMap() {
        final Map<String, String> map = ParameterUtil.toMap(new String[]{}, ":");
        assertTrue(map.isEmpty());
    }

    @Test
    public void toMap_rejectsValuelessKeysByDefault() {
        final Map<String, String> map = ParameterUtil.toMap(new String[]{"dog:woof", "cat"}, ":");
        assertEquals(1, map.size());
        assertEquals("woof", map.get("dog"));
    }

    @Test
    public void toMap_allowValuelessKeys_usesDefaultValue() {
        final Map<String, String> map = ParameterUtil.toMap(new String[]{"dog:woof", "cat"}, ":", true, "meow");
        assertEquals("woof", map.get("dog"));
        assertEquals("meow", map.get("cat"));
        assertEquals(2, map.size());
    }

    @Test
    public void toMap_allowValuelessKeys_skipsKeylessValue() {
        final Map<String, String> map = ParameterUtil.toMap(new String[]{":novalue-for-key"}, ":", true, "default");
        assertTrue(map.isEmpty());
    }

    @Test
    public void toMap_allowMultipleSeparators_onlyFirstConsidered() {
        final Map<String, String> map = ParameterUtil.toMap(new String[]{"dog:woof:extra"}, ":", false, null, true);
        assertEquals("woof:extra", map.get("dog"));
    }

    @Test
    public void toMap_disallowMultipleSeparators_rejectsEntry() {
        final Map<String, String> map = ParameterUtil.toMap(new String[]{"dog:woof:extra"}, ":", false, null, false);
        assertTrue(map.isEmpty());
    }

    @Test
    public void toMap_mapAndListSeparator() {
        final Map<String, String[]> map = ParameterUtil.toMap(new String[]{"dog:woof", "cat:meow,purr"}, ":", ",");
        assertEquals("woof", map.get("dog")[0]);
        assertEquals("meow", map.get("cat")[0]);
        assertEquals("purr", map.get("cat")[1]);
    }

    @Test
    public void toMap_mapAndListSeparator_withValuelessKeysAndDefault() {
        final Map<String, String[]> map = ParameterUtil.toMap(new String[]{"dog", "cat:meow,purr"}, ":", ",", true, "woof");
        assertEquals("woof", map.get("dog")[0]);
        assertEquals("meow", map.get("cat")[0]);
        assertEquals("purr", map.get("cat")[1]);
    }

    @Test
    public void toPatterns_compilesNonBlankValues() {
        final List<Pattern> patterns = ParameterUtil.toPatterns(new String[]{"a.*", "", "  ", "b.*"});
        assertEquals(2, patterns.size());
        assertTrue(patterns.get(0).matcher("abc").matches());
        assertTrue(patterns.get(1).matcher("bcd").matches());
    }

    @Test
    public void toPatterns_nullValues_returnsEmptyList() {
        final List<Pattern> patterns = ParameterUtil.toPatterns(null);
        assertTrue(patterns.isEmpty());
    }
}
