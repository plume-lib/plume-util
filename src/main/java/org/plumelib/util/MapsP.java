// If you edit this file, you must also edit its tests.

package org.plumelib.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.dataflow.qual.SideEffectFree;

/** Utility functions for Map. For collections, see {@link CollectionsPlume}. */
public final class MapsP {

  /** This class is a collection of methods; it does not represent anything. */
  private MapsP() {
    throw new Error("do not instantiate");
  }

  /** The system-specific line separator string. */
  private static final String lineSep = System.lineSeparator();

  // In Python, inlining this gave a 10x speed improvement.
  // Will the same be true for Java?
  /**
   * Increments the Integer which is indexed by key in the Map. Sets the value to 1 if not currently
   * mapped.
   *
   * @param <K> type of keys in the map
   * @param m map from K to Integer
   * @param key the key whose value will be incremented
   * @return the old value, before it was incremented; this might be null
   * @throws Error if the key is in the Map but maps to a non-Integer
   */
  public static <K extends @NonNull Object> @Nullable Integer incrementMap(
      Map<K, Integer> m, K key) {
    return incrementMap(m, key, 1);
  }

  /**
   * Increments the Integer which is indexed by key in the Map. Sets the value to {@code count} if
   * not currently mapped.
   *
   * @param <K> type of keys in the map
   * @param m map from K to Integer
   * @param key the key whose value will be incremented
   * @param count how much to increment the value by
   * @return the old value, before it was incremented; this might be null
   * @throws Error if the key is in the Map but maps to a non-Integer
   */
  public static <K extends @NonNull Object> @Nullable Integer incrementMap(
      Map<K, Integer> m, K key, int count) {
    Integer newTotal = m.getOrDefault(key, 0) + count;
    return m.put(key, newTotal);
  }

  /**
   * Returns a sorted version of m.keySet().
   *
   * @param <K> type of the map keys
   * @param <V> type of the map values
   * @param m a map whose keyset will be sorted
   * @return a sorted version of m.keySet()
   */
  public static <K extends Comparable<? super K>, V> Collection<@KeyFor("#1") K> sortedKeySet(
      Map<K, V> m) {
    ArrayList<@KeyFor("#1") K> theKeys = new ArrayList<>(m.keySet());
    Collections.sort(theKeys);
    return theKeys;
  }

  /**
   * Returns a sorted version of m.keySet().
   *
   * @param <K> type of the map keys
   * @param <V> type of the map values
   * @param m a map whose keyset will be sorted
   * @param comparator the Comparator to use for sorting
   * @return a sorted version of m.keySet()
   */
  public static <K, V> Collection<@KeyFor("#1") K> sortedKeySet(
      Map<K, V> m, Comparator<K> comparator) {
    ArrayList<@KeyFor("#1") K> theKeys = new ArrayList<>(m.keySet());
    Collections.sort(theKeys, comparator);
    return theKeys;
  }

  /**
   * Given an expected number of elements, returns the capacity that should be passed to a HashMap
   * or HashSet constructor, so that the set or map will not resize.
   *
   * @param numElements the maximum expected number of elements in the map or set
   * @return the initial capacity to pass to a HashMap or HashSet constructor
   */
  public static int mapCapacity(int numElements) {
    // Equivalent to: (int) (numElements / 0.75) + 1
    // where 0.75 is the default load factor used throughout the JDK.
    return (numElements * 4 / 3) + 1;
  }

  /**
   * Given an array, returns the capacity that should be passed to a HashMap or HashSet constructor,
   * so that the set or map will not resize.
   *
   * @param <T> the type of elements of the array
   * @param a an array whose length is the maximum expected number of elements in the map or set
   * @return the initial capacity to pass to a HashMap or HashSet constructor
   */
  public static <T> int mapCapacity(T[] a) {
    return mapCapacity(a.length);
  }

  /**
   * Given a collection, returns the capacity that should be passed to a HashMap or HashSet
   * constructor, so that the set or map will not resize.
   *
   * @param c a collection whose size is the maximum expected number of elements in the map or set
   * @return the initial capacity to pass to a HashMap or HashSet constructor
   */
  public static int mapCapacity(Collection<?> c) {
    return mapCapacity(c.size());
  }

  /**
   * Given a map, returns the capacity that should be passed to a HashMap or HashSet constructor, so
   * that the set or map will not resize.
   *
   * @param m a map whose size is the maximum expected number of elements in the map or set
   * @return the initial capacity to pass to a HashMap or HashSet constructor
   */
  public static int mapCapacity(Map<?, ?> m) {
    return mapCapacity(m.size());
  }

  // The following two methods cannot share an implementation because their generic bounds differ.

  /**
   * Returns a copy of {@code orig}, where each key and value in the result is a deep copy
   * (according to the {@code DeepCopyable} interface) of the corresponding element of {@code orig}.
   *
   * @param <K> the type of keys of the map
   * @param <V> the type of values of the map
   * @param <M> the type of the map
   * @param orig a map
   * @return a copy of {@code orig}, as described above
   */
  @SuppressWarnings({"nullness", "signedness"}) // generics problem with clone
  public static <
          K extends @Nullable DeepCopyable<K>,
          V extends @Nullable DeepCopyable<V>,
          M extends @Nullable Map<K, V>>
      @PolyNull M deepCopy(@PolyNull M orig) {
    if (orig == null) {
      return null;
    }
    M result = UtilPlume.clone(orig);
    result.clear();
    for (Map.Entry<K, V> mapEntry : orig.entrySet()) {
      K oldKey = mapEntry.getKey();
      V oldValue = mapEntry.getValue();
      result.put(DeepCopyable.deepCopyOrNull(oldKey), DeepCopyable.deepCopyOrNull(oldValue));
    }
    return result;
  }

  /**
   * Returns a copy of {@code orig}, where each value of the result is a deep copy (according to the
   * {@code DeepCopyable} interface) of the corresponding value of {@code orig}, but the keys are
   * the same objects.
   *
   * @param <K> the type of keys of the map
   * @param <V> the type of values of the map
   * @param <M> the type of the map
   * @param orig a map
   * @return a copy of {@code orig}, as described above
   */
  @SuppressWarnings({"nullness", "signedness"}) // generics problem with clone
  public static <K, V extends @Nullable DeepCopyable<V>, M extends @Nullable Map<K, V>>
      @PolyNull M deepCopyValues(@PolyNull M orig) {
    if (orig == null) {
      return null;
    }
    M result = UtilPlume.clone(orig);
    result.clear();
    for (Map.Entry<K, V> mapEntry : orig.entrySet()) {
      K oldKey = mapEntry.getKey();
      V oldValue = mapEntry.getValue();
      result.put(oldKey, DeepCopyable.deepCopyOrNull(oldValue));
    }
    return result;
  }

  /**
   * Creates a LRU cache.
   *
   * <p>You might want to consider using a {@code WeakHashMap} or {@code WeakIdentityHashMap}
   * instead
   *
   * @param <K> the type of keys
   * @param <V> the type of values
   * @param size size of the cache
   * @return a new cache with the provided size
   */
  public static <K, V> Map<K, V> createLruCache(@Positive int size) {
    return new LinkedHashMap<K, V>(size, .75F, true) {

      private static final long serialVersionUID = 5261489276168775084L;

      @SuppressWarnings(
          "lock:override.receiver") // cannot write receiver parameter within an anonymous class
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > size;
      }
    };
  }

  /**
   * Returns a copy of {@code orig}, where each key and value in the result is a clone of the
   * corresponding element of {@code orig}.
   *
   * @param <K> the type of keys of the map
   * @param <V> the type of values of the map
   * @param <M> the type of the map
   * @param orig a map
   * @return a copy of {@code orig}, as described above
   */
  @SuppressWarnings({"nullness", "signedness"}) // generics problem with clone
  public static <K, V, M extends @Nullable Map<K, V>> @PolyNull M cloneElements(@PolyNull M orig) {
    return cloneElements(orig, true);
  }

  /**
   * Returns a copy of {@code orig}, where each value of the result is a clone of the corresponding
   * value of {@code orig}, but the keys are the same objects.
   *
   * @param <K> the type of keys of the map
   * @param <V> the type of values of the map
   * @param <M> the type of the map
   * @param orig a map
   * @return a copy of {@code orig}, as described above
   */
  @SuppressWarnings({"nullness", "signedness"}) // generics problem with clone
  public static <K, V, M extends @Nullable Map<K, V>> @PolyNull M cloneValues(@PolyNull M orig) {
    return cloneElements(orig, false);
  }

  /**
   * Returns a copy of {@code orig}, where each key and value in the result is a clone of the
   * corresponding element of {@code orig}.
   *
   * @param <K> the type of keys of the map
   * @param <V> the type of values of the map
   * @param <M> the type of the map
   * @param orig a map
   * @param cloneKeys if true, clone keys; otherwise, re-use them
   * @return a copy of {@code orig}, as described above
   */
  @SuppressWarnings({"nullness", "signedness"}) // generics problem with clone
  private static <K, V, M extends @Nullable Map<K, V>> @PolyNull M cloneElements(
      @PolyNull M orig, boolean cloneKeys) {
    if (orig == null) {
      return null;
    }
    M result = UtilPlume.clone(orig);
    result.clear();
    for (Map.Entry<K, V> mapEntry : orig.entrySet()) {
      K oldKey = mapEntry.getKey();
      K newKey = cloneKeys ? UtilPlume.clone(oldKey) : oldKey;
      result.put(newKey, UtilPlume.clone(mapEntry.getValue()));
    }
    return result;
  }

  //
  // Map to string
  //

  // First, versions that append to an Appendable.

  /**
   * Write a multi-line representation of the map into the given Appendable (e.g., a StringBuilder),
   * including a final line separator (unless the map is empty).
   *
   * <p>Each line has the form "{linePrefix}{key} =&gt; {value}"
   *
   * <p>This is less expensive than {@code sb.append(mapToStringMultiLine(m))}.
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param sb an Appendable (such as StringBuilder) to which to write a multi-line string
   *     representation of m
   * @param m map to be converted to a string
   * @param linePrefix a prefix to put at the beginning of each line
   * @deprecated use {@link #mapToStringMultiLine(Appendable, Map, String)}
   */
  @Deprecated // 2026-06-21
  public static <K extends @Signed @Nullable Object, V extends @Signed @Nullable Object>
      void mapToString(Appendable sb, Map<K, V> m, String linePrefix) {
    mapToStringMultiLine(sb, m, linePrefix);
  }

  /**
   * Write a multi-line representation of the map into the given Appendable (e.g., a StringBuilder),
   * including a final line separator (unless the map is empty).
   *
   * <p>Each line has the form "{linePrefix}{key} =&gt; {value}".
   *
   * <p>This is less expensive than {@code sb.append(mapToStringMultiLine(m))}.
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param sb an Appendable (such as StringBuilder) to which to write a multi-line string
   *     representation of m
   * @param m map to be converted to a string
   * @param linePrefix a prefix to put at the beginning of each line
   */
  public static <K extends @Signed @Nullable Object, V extends @Signed @Nullable Object>
      void mapToStringMultiLine(Appendable sb, Map<K, V> m, String linePrefix) {
    try {
      for (Map.Entry<K, V> entry : m.entrySet()) {
        sb.append(linePrefix);
        sb.append(Objects.toString(entry.getKey()));
        sb.append(" => ");
        sb.append(Objects.toString(entry.getValue()));
        sb.append(lineSep);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Write a multi-line representation of the map of maps into the given Appendable (e.g., a
   * StringBuilder), including a final line separator (unless the map is empty).
   *
   * <p>The form of the output is
   *
   * <pre>
   * {outerkey1}
   * {innermap1}
   * {outerkey2}
   * {innermap2}
   * ...
   * </pre>
   *
   * where each inner map is formmatted by {@link mapToStringMultiLine(Appendable, Map, String)}.
   *
   * @param <K1> the type of the outer map keys
   * @param <K2> the type of the inner map keys
   * @param <V2> the type of the inner map values
   * @param sb the destination for the string representation
   * @param linePrefix a prefix to put at the beginning of each line
   * @param innerHeader what to print before each key of the outer map (equivalently, before each
   *     each inner map). If non-empty, it usually ends with a space to avoid abutting the outer map
   *     key.
   * @param mapMap what to print
   */
  static <K1 extends @Signed Object, K2 extends @Signed Object, V2 extends @Signed Object>
      void mapMapToStringMultiLine(
          Appendable sb, String innerHeader, Map<K1, Map<K2, V2>> mapMap, String linePrefix) {
    try {
      for (Map.Entry<K1, Map<K2, V2>> entry : mapMap.entrySet()) {
        sb.append(linePrefix);
        sb.append(innerHeader);
        sb.append(Objects.toString(entry.getKey()));
        sb.append(lineSep);
        mapToStringMultiLine(sb, entry.getValue(), linePrefix + "  ");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // Second, versions that return a String.

  /**
   * Returns a multi-line string representation of a map. Each key-value pair appears on its own
   * line, with no indentation. The last line does not end with a line separator.
   *
   * <p>Each line has the form "{linePrefix}{key} =&gt; {value}".
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param m map to be converted to a string
   * @return a multi-line string representation of m
   * @deprecated use {@link #mapToStringMultiLine(Map)}
   */
  @Deprecated // 2025-06-21
  @SideEffectFree
  public static <K extends @Signed @Nullable Object, V extends @Signed @Nullable Object>
      String mapToString(Map<K, V> m) {
    return mapToStringMultiLine(m);
  }

  /**
   * Returns a multi-line string representation of a map. Each key-value pair appears on its own
   * line, with no indentation. The last line does not end with a line separator.
   *
   * <p>Each line has the form "{key} =&gt; {value}".
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param m map to be converted to a string
   * @return a multi-line string representation of the map
   */
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @SideEffectFree
  public static <K extends @Signed @Nullable Object, V extends @Signed @Nullable Object>
      String mapToStringMultiLine(Map<K, V> m) {
    StringJoiner result = new StringJoiner(lineSep);
    for (Map.Entry<K, V> e : m.entrySet()) {
      result.add(e.getKey() + " => " + e.getValue());
    }
    return result.toString();
  }

  /**
   * Returns a multi-line string representation of a map. Each key-value pair appears on its own
   * line, with no indentation. The last line does not end with a line separator.
   *
   * <p>Each line has the form "{linePrefix}{key} =&gt; {value}".
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param m map to be converted to a string
   * @param linePrefix a prefix to put at the beginning of each line
   * @return a multi-line string representation of the map
   */
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @SideEffectFree
  public static <K extends @Signed @Nullable Object, V extends @Signed @Nullable Object>
      String mapToStringMultiLine(Map<K, V> m, String linePrefix) {
    StringJoiner result = new StringJoiner(lineSep);
    for (Map.Entry<K, V> e : m.entrySet()) {
      result.add(linePrefix + e.getKey() + " => " + e.getValue());
    }
    return result.toString();
  }

  /**
   * Convert a map to a multi-line string representation, which includes the runtime class of keys
   * and values. The last line does not end with a line separator.
   *
   * <p>Each line has the form "{key} [{key.getClass()}] =&gt; {value} [{value.getClass()}]", where
   * the "{}" characters indicate interpolation and the "[]" characters are literally present.
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param m a map
   * @return a string representation of the map
   */
  @SideEffectFree
  public static <K extends @Signed @Nullable Object, V extends @Signed @Nullable Object>
      String mapToStringAndClassMultiLine(Map<K, V> m) {
    return mapToStringAndClassMultiLine(m, "");
  }

  /**
   * Convert a map to a multi-line string representation, which includes the runtime class of keys
   * and values. The last line does not end with a line separator.
   *
   * <p>Each line has the form "{linePrefix}{key} [{key.getClass()}] =&gt; {value}
   * [{value.getClass()}]", where the "{}" characters indicate interpolation and the "[]" characters
   * are literally present.
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param m a map
   * @param linePrefix a prefix to put at the beginning of each line
   * @return a string representation of the map
   */
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @SideEffectFree
  public static <K extends @Signed @Nullable Object, V extends @Signed @Nullable Object>
      String mapToStringAndClassMultiLine(Map<K, V> m, String linePrefix) {
    StringJoiner result = new StringJoiner(lineSep);
    for (Map.Entry<K, V> e : m.entrySet()) {
      result.add(
          linePrefix
              + StringsPlume.toStringAndClass(e.getKey())
              + " => "
              + StringsPlume.toStringAndClass(e.getValue()));
    }
    return result.toString();
  }
}
