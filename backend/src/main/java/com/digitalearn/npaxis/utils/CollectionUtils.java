package com.digitalearn.npaxis.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for collection operations.
 * Provides safe and common collection manipulations.
 *
 * @author NPAxis Team
 * @version 1.0.0
 */
@Slf4j
public class CollectionUtils {

    /**
     * Checks if a collection is null or empty.
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if a collection is not null and not empty.
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Gets the size of a collection safely.
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Safely gets the first element from a list.
     */
    public static <T> T getFirst(List<T> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * Filters a list by a condition.
     */
    public static <T> List<T> filter(List<T> list, java.util.function.Predicate<T> predicate) {
        if (isEmpty(list)) {
            return List.of();
        }
        return list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of items to another list.
     */
    public static <T, R> List<R> map(List<T> list, java.util.function.Function<T, R> mapper) {
        if (isEmpty(list)) {
            return List.of();
        }
        return list.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Checks if two collections have the same elements.
     */
    public static boolean equals(Collection<?> col1, Collection<?> col2) {
        if (isEmpty(col1) && isEmpty(col2)) {
            return true;
        }
        if (isEmpty(col1) || isEmpty(col2)) {
            return false;
        }
        return col1.size() == col2.size() && col1.containsAll(col2);
    }

    /**
     * Checks if a map is null or empty.
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if a map is not null and not empty.
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Gets a value from a map with a default if key doesn't exist.
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (isEmpty(map) || !map.containsKey(key)) {
            return defaultValue;
        }
        return map.get(key);
    }
}

