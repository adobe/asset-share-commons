/*
 * Asset Share Commons
 *
 * Copyright (C) 2017 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.util;

import com.day.cq.commons.inherit.InheritanceValueMap;
import org.apache.sling.api.resource.ValueMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This Wrapper maps the `.getInherited(x, y)` method signatures for the InheritanceValueMap to `.get(x, y)`.
 * <p>
 * The purpose of this is to allow the InheritanceValueMap to be directly used in HTL via the `useObject.properties['inheritedPropertyName']` syntax.
 * <p>
 * Note the only way to directly access the resource's properties the InheritanceValueMap is made from is via the .get(key) method.
 */
public final class ForcedInheritanceValueMapWrapper implements ValueMap {
    private InheritanceValueMap inheritanceValueMap;

    public ForcedInheritanceValueMapWrapper(final InheritanceValueMap inheritanceValueMap) {
        this.inheritanceValueMap = inheritanceValueMap;
    }

    public final <T> T get(String name, Class<T> type) {
        return inheritanceValueMap.getInherited(name, type);
    }

    public final <T> T get(String name, T defaultValue) {
        return inheritanceValueMap.getInherited(name, defaultValue);
    }

    @Override
    public int size() {
        return inheritanceValueMap.size();
    }

    @Override
    public boolean isEmpty() {
        return inheritanceValueMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return inheritanceValueMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return inheritanceValueMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return inheritanceValueMap.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return inheritanceValueMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return inheritanceValueMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        inheritanceValueMap.putAll(m);
    }

    @Override
    public void clear() {
        inheritanceValueMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return inheritanceValueMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return inheritanceValueMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return inheritanceValueMap.entrySet();
    }
}
