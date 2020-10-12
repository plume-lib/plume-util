package org.plumelib.util;

import java.util.concurrent.atomic.AtomicLong;
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
 * definition and drop in one of the two following code snippets.
 *
 * <pre><code>
 * // The following code implements the UniqueId interface, using one global counter shared by all classes.
 * /** The unique ID of this object. *&#47;
 * final transient long uid = UniqueId.nextUid.getAndIncrement();
 * &#064;Override
 * public long getUid() {
 *   return uid;
 * }
 * </code></pre>
 *
 * or:
 *
 * <pre><code>
 * // The following code implements the UniqueId interface, using a counter just for this class.
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
 * <p>If you need a unique identifier for a class that you do not control (you cannot edit it to
 * make it implement this interface, {@code UniqueId}), see {@code UniqueIdMap}.
 */
public interface UniqueId {

  /** The unique ID for the next-created object. */
  static final AtomicLong nextUid = new AtomicLong(0);

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
