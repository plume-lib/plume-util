package org.plumelib.util;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A Partitioner accepts Objects and assigns them to an equivalence class.
 *
 * @param <ELEMENT> the type of elements to be classified
 * @param <CLASS> the type of equivalence classes (classification buckets)
 * @see MultiRandSelector
 */
@FunctionalInterface
public interface Partitioner<ELEMENT extends @Nullable Object, CLASS extends @Nullable Object> {

  /**
   * Returns a key representing the bucket containing obj.
   *
   * @param obj the Object to be assigned to a bucket
   * @return a key representing the bucket containing obj
   */
  CLASS assignToBucket(ELEMENT obj);
}
