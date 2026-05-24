package com.skyblockexp.teamsapi.extension.towny;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection helpers for Towny API integration.
 */
final class TownyReflection {

    /** Hidden constructor. */
    private TownyReflection() {
    }

    /**
     * Invokes a static no-arg method.
     *
     * @param type target class
     * @param methodName method name
     * @return invocation result or null
     */
    static Object invokeStatic(final Class<?> type, final String methodName) {
        return invokeStatic(type, methodName, new Class<?>[0], new Object[0]);
    }

    /**
     * Invokes a static method by name.
     *
     * @param type target class
     * @param methodName method name
     * @param types argument types
     * @param args arguments
     * @return invocation result or null
     */
    static Object invokeStatic(final Class<?> type, final String methodName,
            final Class<?>[] types, final Object[] args) {
        if (type == null) {
            return null;
        }
        try {
            final Method method = type.getMethod(methodName, types);
            return method.invoke(null, args);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    /**
     * Invokes a no-arg method.
     *
     * @param target target object
     * @param methodName method name
     * @return invocation result or {@code null}
     */
    static Object invoke(final Object target, final String methodName) {
        return invoke(target, methodName, new Class<?>[0], new Object[0]);
    }

    /**
     * Invokes a method by name and signature.
     *
     * @param target target object
     * @param methodName method name
     * @param types argument types
     * @param args argument values
     * @return invocation result or {@code null}
     */
    static Object invoke(final Object target, final String methodName,
            final Class<?>[] types, final Object[] args) {
        if (target == null) {
            return null;
        }
        try {
            final Method method = target.getClass().getMethod(methodName, types);
            return method.invoke(target, args);
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    /**
     * Loads a class by name.
     *
     * @param className class name
     * @return class or null
     */
    static Class<?> loadClass(final String className) {
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    /**
     * Returns whether a type/object has a public method with the given signature.
     *
     * @param target target object or class
     * @param methodName method name
     * @param types parameter types
     * @return true when method exists
     */
    static boolean hasMethod(final Object target, final String methodName, final Class<?>... types) {
        if (target == null) {
            return false;
        }
        final Class<?> type = target instanceof Class<?> ? (Class<?>) target : target.getClass();
        try {
            type.getMethod(methodName, types);
            return true;
        }
        catch (NoSuchMethodException ignored) {
            return false;
        }
    }
}
