package org.plumelib.util;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Interface for things that make boolean decisions. This is inspired by {@code
 * java.io.FilenameFilter}.
 *
 * @param <T> the type of arguments to {@link #accept}
 * @deprecated use {@code java.util.function.Predicate}
 */
@Deprecated // 2024-04-19
public interface Filter<T extends @Nullable Object> {
  /**
   * Tests whether a specified Object satisfies the filter.
   *
   * @param o the object to test
   * @return whether the object satisfies the filter
   */
  boolean accept(T o);
}
