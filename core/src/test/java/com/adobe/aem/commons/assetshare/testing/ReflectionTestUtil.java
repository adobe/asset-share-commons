package com.adobe.aem.commons.assetshare.testing;

import java.lang.reflect.Field;

/**
 * Test helper for injecting values directly into (possibly private, possibly shadowed) fields.
 *
 * This is used to exercise Sling Model classes without going through the full Sling Models
 * dependency-injection pipeline, which is necessary for fields typed as Core Components form
 * interfaces (ex. {@code Options}, {@code Text}) since the real Core Components implementations
 * of those interfaces (auto-discovered by the AEM Mocks classpath scanning) cannot be
 * instantiated in this test environment (they have missing/incomplete transitive dependencies).
 */
public final class ReflectionTestUtil {
    private ReflectionTestUtil() {
    }

    /**
     * Sets a (possibly private) field declared on the exact class provided (does not search superclasses),
     * which is important when a field name is shadowed by a subclass (ex. AbstractPredicate#request vs.
     * a concrete predicate impl's own "request" field of the same name).
     *
     * @param target the object instance to set the field on.
     * @param declaringClass the class that actually declares the field.
     * @param fieldName the field name.
     * @param value the value to set.
     */
    public static void setField(Object target, Class<?> declaringClass, String fieldName, Object value) {
        try {
            final Field field = declaringClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to set field [" + fieldName + "] on [" + declaringClass + "]", e);
        }
    }

    /**
     * Sets a (possibly private) field declared directly on the target's own class.
     *
     * @param target the object instance to set the field on.
     * @param fieldName the field name.
     * @param value the value to set.
     */
    public static void setField(Object target, String fieldName, Object value) {
        setField(target, target.getClass(), fieldName, value);
    }
}
