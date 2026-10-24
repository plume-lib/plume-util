package org.plumelib.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Compares the run time of {@link ArrayMap} to that of {@link HashMap} and {@link LinkedHashMap}.
 *
 * <p>Run it with {@code ./gradlew jmh}. To run a subset, pass JMH arguments, as in {@code ./gradlew
 * jmh --args='getHit -p size=1,4'}.
 *
 * <p>Each benchmark method that operates on all the keys reports the time for all of them, not for
 * one operation.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ArrayMapBenchmark {

  /** Creates a new ArrayMapBenchmark. */
  public ArrayMapBenchmark() {}

  /** The number of mappings in the map. */
  @Param({"1", "2", "4", "8", "16", "32"})
  int size;

  /** The map implementation: "ArrayMap", "HashMap", or "LinkedHashMap". */
  @Param({"ArrayMap", "HashMap", "LinkedHashMap"})
  String impl;

  /** The keys that are stored in the map. */
  String[] keys;

  /**
   * Keys that are equal to, but not the same object as, the elements of {@link #keys}. Looking them
   * up prevents an {@code ==} test from succeeding.
   */
  String[] equalKeys;

  /** Keys that do not appear in the map. */
  String[] absentKeys;

  /** A map that contains {@link #keys}. */
  Map<String, Integer> map;

  /**
   * Creates an empty map whose implementation is {@link #impl}.
   *
   * @param capacity the number of mappings that the map should hold without resizing
   * @return an empty map
   */
  Map<String, Integer> newMap(int capacity) {
    return switch (impl) {
      case "ArrayMap" -> new ArrayMap<>(capacity);
      case "HashMap" -> new HashMap<>(MapsP.mapCapacity(capacity));
      case "LinkedHashMap" -> new LinkedHashMap<>(MapsP.mapCapacity(capacity));
      default -> throw new IllegalArgumentException(impl);
    };
  }

  /**
   * Creates an empty map whose implementation is {@link #impl}, using the no-argument constructor.
   *
   * @return an empty map
   */
  Map<String, Integer> newMapDefaultCapacity() {
    return switch (impl) {
      case "ArrayMap" -> new ArrayMap<>();
      case "HashMap" -> new HashMap<>();
      case "LinkedHashMap" -> new LinkedHashMap<>();
      default -> throw new IllegalArgumentException(impl);
    };
  }

  /** Initializes the keys and the map. */
  @SuppressWarnings("PMD.StringInstantiation") // equalKeys must not be identical to keys
  @Setup
  public void setup() {
    keys = new String[size];
    equalKeys = new String[size];
    absentKeys = new String[size];
    for (int i = 0; i < size; i++) {
      // Keys like those of a parser or of reflection:  a common prefix and varied lengths.
      keys[i] = "field" + i;
      equalKeys[i] = new String(keys[i]);
      absentKeys[i] = "field" + (i + 1000);
      // Cache the hash codes, as is typical for long-lived String keys.
      int unused = keys[i].hashCode() ^ equalKeys[i].hashCode() ^ absentKeys[i].hashCode();
    }
    map = newMap(size);
    for (int i = 0; i < size; i++) {
      map.put(keys[i], i);
    }
  }

  /**
   * Creates a map with the right capacity, and populates it.
   *
   * @return the map
   */
  @Benchmark
  public Map<String, Integer> build() {
    Map<String, Integer> m = newMap(size);
    for (int i = 0; i < size; i++) {
      m.put(keys[i], i);
    }
    return m;
  }

  /**
   * Creates a map with the default capacity, and populates it.
   *
   * @return the map
   */
  @Benchmark
  public Map<String, Integer> buildDefaultCapacity() {
    Map<String, Integer> m = newMapDefaultCapacity();
    for (int i = 0; i < size; i++) {
      m.put(keys[i], i);
    }
    return m;
  }

  /**
   * Looks up every key, using keys that are equal to but not the same object as the stored keys.
   *
   * @param bh consumes the results
   */
  @Benchmark
  public void getHit(Blackhole bh) {
    for (String k : equalKeys) {
      bh.consume(map.get(k));
    }
  }

  /**
   * Looks up every key, using the stored key objects.
   *
   * @param bh consumes the results
   */
  @Benchmark
  public void getHitIdentical(Blackhole bh) {
    for (String k : keys) {
      bh.consume(map.get(k));
    }
  }

  /**
   * Looks up keys that are not in the map.
   *
   * @param bh consumes the results
   */
  @Benchmark
  public void getMiss(Blackhole bh) {
    for (String k : absentKeys) {
      bh.consume(map.get(k));
    }
  }

  /**
   * Iterates over the entries of the map.
   *
   * @return the sum of the values
   */
  @Benchmark
  public int iterate() {
    int sum = 0;
    for (Map.Entry<String, Integer> e : map.entrySet()) {
      sum += e.getValue();
    }
    return sum;
  }

  /**
   * Creates and populates a map, then removes every key in insertion order.
   *
   * @return the (empty) map
   */
  @Benchmark
  public Map<String, Integer> buildAndRemove() {
    Map<String, Integer> m = newMap(size);
    for (int i = 0; i < size; i++) {
      m.put(keys[i], i);
    }
    for (int i = 0; i < size; i++) {
      m.remove(keys[i]);
    }
    return m;
  }
}
