package org.plumelib.util;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;

/**
 * An interface for objects that have a unique ID (unique identifier). If you are tempted to print
 * the value of {@code System.identityHashCode()}, consider using this instead.
 *
 * <p>Using <a
 * href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/System.html#identityHashCode(java.lang.Object)">{@code
 * System.identityHashCode}</a> is <em>not</em> a unique identifier, because two values can have the
 * same identityHashCode if they are allocated into the same location in memory. Garbage collection
 * can move or reclaim the first value, permitting the second value to be allocated exactly where
 * the first one was.
 *
 * <p>To use the {@code UniqueId} interface, add {@code implements UniqueId} to your class
 * definition and drop in the following code snippet.
 *
 * <pre><code>
 * /** The unique ID for the next-created object. *&#47;
 * static final AtomicLong nextUid = new AtomicLong(0);
 * /** The unique ID of this object. *&#47;
 * final transient long uid = nextUid.getAndIncrement();
 * &#064;Override
 * public long getUid() {
 *   return uid;
 * }
 * </code></pre>
 *
 * You can also use the above code to implement a unique identifier, without subtyping the {@code
 * UniqueId} interface.
 *
 * <p>If you need a unique identifier for a class that you cannot edit (that is, you cannot make it
 * implement {@code UniqueId}), use {@link UniqueIdMap}.
 */
public interface UniqueId {

  /**
   * Returns the unique ID of this object.
   *
   * @return the unique ID of this object.
   */
  public long getUid(@UnknownInitialization(UniqueId.class) UniqueId this);

  /**
   * Returns the simple name of the class and the unique ID of this object. This method is intended
   * for use in diagnostic output.
   *
   * @return the simple name of the class and the unique ID of this object
   */
  public default String getClassAndUid(@UnknownInitialization(UniqueId.class) UniqueId this) {
    return this.getClass().getSimpleName() + "#" + getUid();
  }
}
