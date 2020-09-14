package org.plumelib.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Provides a unique ID for classes that you cannot modify. The unique ID is useful because it makes
 * output deterministic. For classes that you can edit, implement the {@link UniqueId} interface.
 *
 * <p>The IDs count up from 0, per instance of UniqueIdMap. When you look up an object in this map,
 * it is given a unique ID if it didn't already have one.
 */
public class UniqueIdMap<E> {

  /** The unique ID for the next-created object. */
  private final AtomicLong nextUid = new AtomicLong(0);

  /** A mapping from objects to their IDs. */
  private WeakIdentityHashMap<E, Long> map = new WeakIdentityHashMap<>();

  /**
   * Get the unique ID for the given object. If the object's ID has not been previously requested, a
   * new one is generated for it.
   *
   * @param object the object to get a unique ID for
   * @return the unique ID for the given object
   */
  public long get(E object) {
    Long id = map.get(object);
    if (id != null) {
      return id;
    }
    long newId = nextUid.getAndIncrement();
    map.put(object, newId);
    return newId;
  }
}
