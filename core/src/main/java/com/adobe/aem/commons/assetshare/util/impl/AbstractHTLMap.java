package com.adobe.aem.commons.assetshare.util.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class AbstractHTLMap implements Map<String, Object> {
    private static final String MAP_NOT_MODIFIABLE = "Map is not modifiable";

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(final Object key) {
        return false;
    }

    @Override
    public final boolean containsValue(Object value) {
        return false;
    }

    @Override
    public final Object put(String key, Object value) {
        throw new UnsupportedOperationException(MAP_NOT_MODIFIABLE);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException(MAP_NOT_MODIFIABLE);
    }

    @Override
    public final void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException(MAP_NOT_MODIFIABLE);
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException(MAP_NOT_MODIFIABLE);
    }

    @Override
    public Set<String> keySet() {
        return Collections.EMPTY_SET;
    }

    @Override
    public final Collection<Object> values() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public final Set<Entry<String, Object>> entrySet() {
        return Collections.EMPTY_SET;
    }
}
