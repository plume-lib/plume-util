// If you edit this file, you must also edit its tests.

package org.plumelib.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

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
  public static boolean getBooleanSystemProperty(String key, boolean defaultValue) {
    return UtilPlume.getBooleanProperty(System.getProperties(), key, defaultValue);
  }

  /**
   * Determines whether a system property has a string value that represents true: "true", "yes", or
   * "1". Errs if the property is set to a value that is not one of "true", "false", "yes", "no",
   * "1", or "0".
   *
   * @param key name of the property to look up
   * @return true iff the property has a string value that represents true
   */
  @Pure
  public static boolean getBooleanSystemProperty(String key) {
    return getBooleanSystemProperty(key, false);
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
   * Returns the number of garbage collections that have occurred.
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
   * Returns the cumulative garbage collection time in milliseconds, across all threads.
   *
   * @return the cumulative garbage collection time in milliseconds
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

  /** A triple of (timestamp, collection time, subsequent timestamp). */
  private static class GcHistoryItem {
    /** When the collection happened. An epoch second. */
    long timestamp;
    /** The cumulative collection time in milliseconds. */
    long collectionTime;
    /**
     * When the subsequent collection happened. It is 0 until after the subsequent collection
     * occurs. The purpose of this field is to avoid the need for a {@code peek2()} method on deque.
     */
    long subsequentTimestamp = 0;

    /**
     * Creates a new GcHistoryItem.
     *
     * @param timestamp when the collection happened; an epoch second
     * @param collectionTime the collection time in milliseconds
     */
    GcHistoryItem(long timestamp, long collectionTime) {
      this.timestamp = timestamp;
      this.collectionTime = collectionTime;
    }
  }

  /** The history of recent garbage collection runs. The queue is never empty. */
  private static Deque<GcHistoryItem> gcHistory;

  static {
    gcHistory = new ArrayDeque<>();
    // Add a dummy element so the queue is never empty.
    gcHistory.add(new GcHistoryItem(Instant.now().getEpochSecond(), getCollectionTime()));
  }

  /**
   * Calls `gcPercentage(60)`.
   *
   * @return the percentage of time spent garbage collecting, in the past minute
   * @see #gcPercentage()
   */
  public static double gcPercentage() {
    return gcPercentage(60);
  }

  /**
   * Returns the fraction of time spent garbage collecting, in the past {@code seconds} seconds.
   * This is generally a value between 0 and 1. This method might return a value greater than 1 if
   * multiple threads are spending all their time collecting. Returns 0 if {@code gcPercentage} was
   * not first called more than {@code seconds} seconds ago.
   *
   * <p>This method also discards all GC history older than {@code seconds} seconds.
   *
   * <p>Instead of calling this method directly, a client program might call {@link
   * #gcUsageMessage}.
   *
   * @param seconds the size of the time window, in seconds
   * @return the percentage of time spent garbage collecting, in the past {@code seconds} seconds
   */
  public static double gcPercentage(int seconds) {
    GcHistoryItem newest = gcHistory.getLast();
    long now = Instant.now().getEpochSecond(); // in seconds
    long collectionTime; // in milliseconds
    if (now == newest.timestamp) {
      // For efficiency, don't add another entry with the same timestamp as the newest one, even
      // though the collectionTime field would differ slightly.
      collectionTime = newest.collectionTime;
    } else {
      newest.subsequentTimestamp = now;
      collectionTime = getCollectionTime();
      newest = new GcHistoryItem(now, collectionTime);
      gcHistory.add(newest);
    }

    GcHistoryItem oldest = gcHistory.getFirst();
    while (oldest.subsequentTimestamp != 0 && now - oldest.subsequentTimestamp > seconds) {
      // The second-oldest history item can be used, so don't use the oldest one.
      gcHistory.removeFirst();
      oldest = gcHistory.getFirst();
    }
    // At this point, the second-oldest history item is too recent to use (or it does not exist).

    long elapsed = now - oldest.timestamp; // in seconds
    if (elapsed < seconds) {
      // The oldest history item is too recent to use.
      return 0;
    }

    double elapsedCollectionTime = (collectionTime - oldest.collectionTime) / 1000.0; // in seconds
    return elapsedCollectionTime / elapsed;
  }

  /**
   * If the fraction of time spent garbage collecting in the past {@code seconds} seconds is less
   * than {@code cpuThreshold}, returns null. Otherwise, returns a string indicating garbage
   * collection CPU usage and memory statistics. The string is multiple lines long, but does not end
   * with a line separator.
   *
   * <p>A typical use is to put the following in an outer loop that takes a significant amount of
   * time (more than a second) to execute:
   *
   * <pre>{@code
   * String message = gcUsageMessage(.25, 60);
   * if (message != null) {
   *   System.err.println(message);
   * }
   * }</pre>
   *
   * @param cpuThreshold the maximum fraction of CPU that should be spent garbage collecting; a
   *     number between 0 and 1
   * @param seconds the time window in which to compute the garbage collection CPU usage
   * @return a GC usage message string, or null
   */
  public static @Nullable String gcUsageMessage(double cpuThreshold, int seconds) {
    double gcPercentage = SystemPlume.gcPercentage(seconds);
    if (gcPercentage < cpuThreshold) {
      return null;
    } else {
      return String.join(
          System.lineSeparator(),
          "Garbage collection consumed "
              + Math.round(gcPercentage * 100)
              + "% of CPU during the past "
              + seconds
              + " seconds.",
          "  max memory = " + Runtime.getRuntime().maxMemory(),
          "total memory = " + Runtime.getRuntime().totalMemory(),
          " free memory = " + Runtime.getRuntime().freeMemory());
    }
  }
}
