// If you edit this file, you must also edit its tests.

package org.plumelib.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Utility methods relating to the JVM runtime system: sleep and garbage collection. */
public final class SystemPlume {

  /** The Runtime instance for the current execution. */
  private static Runtime runtime = Runtime.getRuntime();

  /** This class is a collection of methods; it does not represent anything. */
  private SystemPlume() {
    throw new Error("do not instantiate");
  }

  ///
  /// Properties
  ///

  /**
   * Determines whether a system property has a string value that represents true: "true", "yes", or
   * "1". Errs if the property is set to a value that is not one of "true", "false", "yes", "no",
   * "1", or "0".
   *
   * @param key name of the property to look up
   * @param defaultValue the value to return if the property is not set
   * @return true iff the property has a string value that represents true
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // does not depend on object identity
  @Pure
  public static boolean getBooleanSystemProperty(Properties p, String key, boolean defaultValue) {
    return getBooleanProperty(System.properties(), key, defaultValue);
  }

  /**
   * Determines whether a system property has a string value that represents true: "true", "yes", or
   * "1". Errs if the property is set to a value that is not one of "true", "false", "yes", "no",
   * "1", or "0".
   *
   * @param key name of the property to look up
   * @return true iff the property has a string value that represents true
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // does not depend on object identity
  @Pure
  public static boolean getBooleanSystemProperty(Properties p, String key) {
    return getBooleanProperty(System.properties(), key);
  }

  ///
  /// Sleep
  ///

  /**
   * Like Thread.sleep, but does not throw any checked exceptions, so it is easier for clients to
   * use. Causes the currently executing thread to sleep (temporarily cease execution) for the
   * specified number of milliseconds.
   *
   * @param millis the length of time to sleep in milliseconds
   */
  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  ///
  /// Garbage collection
  ///

  /**
   * Returns the amount of used memory in the JVM.
   *
   * <p>To force a garbage collection, which gives a more accurate overapproximation of the memory
   * used, but is also slower, use {@link #usedMemory(boolean)}
   *
   * @return the amount of used memory
   */
  public static long usedMemory() {
    return usedMemory(false);
  }

  /**
   * Returns the amount of used memory in the JVM.
   *
   * @param forceGc if true, force a garbage collection, which gives a more accurate
   *     overapproximation of the memory used, but is also slower
   * @return the amount of used memory
   */
  public static long usedMemory(boolean forceGc) {
    if (forceGc) {
      gc();
    }
    // Implementation note:
    // MemoryUsage.getUsed() == Runtime.totalMemory() - Runtime.freeMemory()
    return (runtime.totalMemory() - runtime.freeMemory());
  }

  /**
   * Perform garbage collection. Like System.gc, but waits to return until garbage collection has
   * completed.
   */
  public static void gc() {
    long oldCollectionCount = getCollectionCount();
    System.gc();
    while (getCollectionCount() == oldCollectionCount) {
      try {
        Thread.sleep(1); // 1 millisecond
      } catch (InterruptedException e) {
        // nothing to do
      }
    }
  }

  /**
   * Return the number of garbage collections that have occurred.
   *
   * @return the number of garbage collections that have occurred
   */
  private static long getCollectionCount() {
    long result = 0;
    for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
      long count = b.getCollectionCount();
      if (count != -1) {
        result += count;
      }
    }
    return result;
  }

  /**
   * A list of pairs of (timestamp, accumulated collection time). The timestamp is an epoch second,
   * and the collection time is in milliseconds. New items are added to the front.
   *
   * <p>The list is not currently pruned.
   */
  private static List<Pair<Long, Long>> gcHistory = new ArrayList<>();

  /**
   * Returns the percentage of time spent garbage collecting, in the past minute. Might return a
   * value greater than 1 if multiple threads are spending all their time collecting. Returns 0 if
   * {@code gcPercentage} was not first called more than 1 minute ago.
   *
   * <p>A typical use is to put the following in an outer loop that takes a significant amount of
   * time (more than a second) to execute:
   *
   * <pre>{@code
   * if (GC.gcPercentage() > .25) {
   *   String message = String.format(
   *     "Memory constraints are impeding performance; please increase max heap size (max memory = %d, total memory = %d, free memory = %d)",
   *     Runtime.getRuntime().maxMemory(),
   *     Runtime.getRuntime().totalMemory(),
   *     Runtime.getRuntime().freeMemory());
   *   System.err.println(message);
   * }
   * }</pre>
   *
   * @return the percentage of time spent garbage collecting, in the past minute
   */
  public static float gcPercentage() {
    return gcPercentage(60);
  }

  /**
   * Returns the percentage of time spent garbage collecting, in the past {@code seconds} seconds.
   * Might return a value greater than 1 if multiple threads are spending all their time collecting.
   * Returns 0 if {@code gcPercentage} was not first called more than {@code seconds} seconds ago.
   *
   * <p>A typical use is to put the following in an outer loop that takes a significant amount of
   * time (more than a second) to execute:
   *
   * <pre>{@code
   * if (GC.gcPercentage(10) > .25) {
   *   String message = String.format(
   *     "Memory constraints are impeding performance; please increase max heap size (max memory = %d, total memory = %d, free memory = %d)",
   *     Runtime.getRuntime().maxMemory(),
   *     Runtime.getRuntime().totalMemory(),
   *     Runtime.getRuntime().freeMemory());
   *   System.err.println(message);
   * }
   * }</pre>
   *
   * @param seconds the size of the time window, in seconds
   * @return the percentage of time spent garbage collecting, in the past {@code seconds} seconds
   */
  public static float gcPercentage(int seconds) {
    long now = Instant.now().getEpochSecond();
    long collectionTime = getCollectionTime();
    gcHistory.add(Pair.of(now, collectionTime));

    for (Pair<Long, Long> p : gcHistory) {
      if (now - p.a >= seconds) {
        return (float) ((collectionTime - p.b) / 1000.0 / (now - p.a));
      }
    }
    return 0;
  }

  /**
   * Return the accumulated garbage collection time in milliseconds.
   *
   * @return the accumulated garbage collection time in milliseconds
   */
  private static long getCollectionTime() {
    long result = 0;
    for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
      long time = b.getCollectionTime();
      if (time != -1) {
        result += time;
      }
    }
    return result;
  }
}
