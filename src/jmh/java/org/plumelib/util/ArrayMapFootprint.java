package org.plumelib.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * Measures the memory retained by an {@link ArrayMap}, compared to that retained by a {@link
 * HashMap} and a {@link LinkedHashMap}. The keys and values are shared among all the maps, so the
 * measurement is the overhead of the map itself.
 *
 * <p>Run it with {@code ./gradlew arrayMapFootprint}. The results are approximate, because they are
 * derived from the heap size after garbage collection.
 */
public final class ArrayMapFootprint {

  /** Do not instantiate. */
  private ArrayMapFootprint() {
    throw new Error("do not instantiate");
  }

  /** The number of maps to create, for each map implementation and size. */
  private static final int NUM_MAPS = 200_000;

  /** The map sizes to measure. */
  private static final int[] SIZES = {0, 1, 2, 3, 4, 5, 8, 16, 32};

  /** The largest element of {@link #SIZES}. */
  private static final int MAX_SIZE = 32;

  /**
   * A map implementation to measure.
   *
   * @param name the name of the map implementation, used as a column heading
   * @param factory creates an empty map, given its expected size
   */
  private record Impl(String name, IntFunction<Map<String, Integer>> factory) {}

  /** The map implementations to measure. */
  private static final List<Impl> IMPLS =
      List.of(
          new Impl("ArrayMap", ArrayMap::new),
          new Impl("AM()", size -> new ArrayMap<>()),
          new Impl("HashMap", size -> new HashMap<>(MapsP.mapCapacity(size))),
          new Impl("HM()", size -> new HashMap<>()),
          new Impl("LinkedHM", size -> new LinkedHashMap<>(MapsP.mapCapacity(size))));

  /** Holds the maps, so that they are not garbage-collected while being measured. */
  private static final Object[] holder = new Object[NUM_MAPS];

  /**
   * Returns the number of bytes in use in the heap, after garbage collection.
   *
   * @return the number of bytes in use in the heap
   */
  @SuppressWarnings("PMD.DoNotCallGarbageCollectionExplicitly") // measure only live objects
  private static long usedMemory() {
    Runtime runtime = Runtime.getRuntime();
    for (int i = 0; i < 5; i++) {
      System.gc();
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new Error(e);
      }
    }
    return runtime.totalMemory() - runtime.freeMemory();
  }

  /**
   * Prints a table of the bytes per map.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    String[] keys = new String[MAX_SIZE];
    Integer[] values = new Integer[MAX_SIZE];
    for (int i = 0; i < MAX_SIZE; i++) {
      keys[i] = "field" + i;
      values[i] = 1000 + i;
    }

    System.out.printf("%5s", "size");
    for (Impl impl : IMPLS) {
      System.out.printf(" %9s", impl.name());
    }
    System.out.println();

    for (int size : SIZES) {
      System.out.printf("%5d", size);
      for (Impl impl : IMPLS) {
        Arrays.fill(holder, null);
        long before = usedMemory();
        for (int n = 0; n < NUM_MAPS; n++) {
          Map<String, Integer> m = impl.factory().apply(size);
          for (int i = 0; i < size; i++) {
            m.put(keys[i], values[i]);
          }
          holder[n] = m;
        }
        long after = usedMemory();
        System.out.printf(" %9.1f", (after - before) / (double) NUM_MAPS);
      }
      System.out.println();
    }
  }
}
