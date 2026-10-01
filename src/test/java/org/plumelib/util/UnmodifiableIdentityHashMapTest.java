package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.junit.jupiter.api.Test;

/** Test the UnmodifiableIdentityHashMap class. */
// PMD suggests method references such as `itor::next`, but the Lock Checker rejects a
// method reference whose receiver must be @GuardSatisfied.
@SuppressWarnings("PMD.LambdaCanBeMethodReference")
final class UnmodifiableIdentityHashMapTest {

  UnmodifiableIdentityHashMapTest() {}

  // ///////////////////////////////////////////////////////////////////////////
  // Helper functions
  //

  /**
   * Returns a new one-element list containing the given string. Two calls with the same argument
   * return equal but non-identical lists.
   *
   * @param s the element of the returned list
   * @return a new one-element list containing {@code s}
   */
  private static List<String> newList(String s) {
    List<String> result = new ArrayList<>();
    result.add(s);
    return result;
  }

  /**
   * Returns a new IdentityHashMap containing the two given mappings.
   *
   * @param k1 the first key
   * @param v1 the first value
   * @param k2 the second key
   * @param v2 the second value
   * @return a new IdentityHashMap containing the given mappings
   */
  private static @Modifiable IdentityHashMap<String, String> newMap(
      String k1, String v1, String k2, String v2) {
    IdentityHashMap<String, String> result = new IdentityHashMap<>();
    result.put(k1, v1);
    result.put(k2, v2);
    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // wrap
  //

  @Test
  void wrapDoesNotWrapTwice() {
    IdentityHashMap<String, String> underlying = newMap("a", "1", "b", "2");
    UnmodifiableIdentityHashMap<String, String> wrapped =
        UnmodifiableIdentityHashMap.wrap(underlying);
    assertSame(wrapped, UnmodifiableIdentityHashMap.wrap(wrapped));
  }

  @Test
  void wrapperIsAnIdentityHashMap() {
    // The point of extending IdentityHashMap is assignability to an IdentityHashMap variable.
    IdentityHashMap<String, String> wrapped =
        UnmodifiableIdentityHashMap.wrap(newMap("a", "1", "b", "2"));
    assertEquals(2, wrapped.size());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Read operations
  //

  @Test
  void readOperationsDelegateToTheUnderlyingMap() {
    IdentityHashMap<String, String> underlying = newMap("a", "1", "b", "2");
    UnmodifiableIdentityHashMap<String, String> m = UnmodifiableIdentityHashMap.wrap(underlying);

    assertEquals(2, m.size());
    assertFalse(m.isEmpty());
    assertEquals("1", m.get("a"));
    assertNull(m.get("c"));
    assertTrue(m.containsKey("a"));
    assertFalse(m.containsKey("c"));
    assertTrue(m.containsValue("1"));
    assertFalse(m.containsValue("3"));
    assertEquals("1", m.getOrDefault("a", "default"));
    assertEquals("default", m.getOrDefault("c", "default"));
    assertEquals(underlying.toString(), m.toString());

    assertTrue(UnmodifiableIdentityHashMap.wrap(new IdentityHashMap<>()).isEmpty());
  }

  @SuppressWarnings("IdentityHashMapUsage") // testing that equals()/hashCode() delegate
  @Test
  void equalsAndHashCodeDelegateToTheUnderlyingMap() {
    IdentityHashMap<String, String> underlying = newMap("a", "1", "b", "2");
    UnmodifiableIdentityHashMap<String, String> m = UnmodifiableIdentityHashMap.wrap(underlying);
    assertEquals(underlying.hashCode(), m.hashCode());
    // The wrapper is the "expected" argument, so that its equals() method is the one called.
    assertEquals(m, underlying);
    assertNotEquals(m, newMap("a", "1", "b", "3"));
  }

  @Test
  void forEachVisitsAllMappings() {
    UnmodifiableIdentityHashMap<String, String> m =
        UnmodifiableIdentityHashMap.wrap(newMap("a", "1", "b", "2"));
    Map<String, String> visited = new LinkedHashMap<>();
    m.forEach(visited::put);
    assertEquals(Map.of("a", "1", "b", "2"), visited);
  }

  @Test
  void lookupsUseIdentity() {
    List<String> a1 = newList("a");
    List<String> a2 = newList("a");
    assertEquals(a1, a2);

    IdentityHashMap<List<String>, String> underlying = new IdentityHashMap<>();
    underlying.put(a1, "1");
    UnmodifiableIdentityHashMap<List<String>, String> m =
        UnmodifiableIdentityHashMap.wrap(underlying);

    assertEquals("1", m.get(a1));
    assertNull(m.get(a2));
    assertTrue(m.containsKey(a1));
    assertFalse(m.containsKey(a2));
    assertEquals("default", m.getOrDefault(a2, "default"));
  }

  @Test
  void changesToTheUnderlyingMapAreVisible() {
    IdentityHashMap<String, String> underlying = newMap("a", "1", "b", "2");
    UnmodifiableIdentityHashMap<String, String> m = UnmodifiableIdentityHashMap.wrap(underlying);
    assertEquals(2, m.size());

    underlying.put("c", "3");
    assertEquals(3, m.size());
    assertEquals("3", m.get("c"));

    underlying.remove("a");
    assertEquals(2, m.size());
    assertFalse(m.containsKey("a"));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Mutating operations
  //

  @SuppressWarnings("modifiability:method.invocation") // testing that mutation throws
  @Test
  void mutatingOperationsThrow() {
    UnmodifiableIdentityHashMap<String, String> m =
        UnmodifiableIdentityHashMap.wrap(newMap("a", "1", "b", "2"));

    assertThrows(UnsupportedOperationException.class, () -> m.put("c", "3"));
    assertThrows(UnsupportedOperationException.class, () -> m.putAll(Map.of("c", "3")));
    assertThrows(UnsupportedOperationException.class, () -> m.remove("a"));
    assertThrows(UnsupportedOperationException.class, () -> m.remove("a", "1"));
    assertThrows(UnsupportedOperationException.class, () -> m.clear());
    assertThrows(UnsupportedOperationException.class, () -> m.replaceAll((k, v) -> v + v));
    assertThrows(UnsupportedOperationException.class, () -> m.putIfAbsent("c", "3"));
    assertThrows(UnsupportedOperationException.class, () -> m.replace("a", "9"));
    assertThrows(UnsupportedOperationException.class, () -> m.replace("a", "1", "9"));
    assertThrows(UnsupportedOperationException.class, () -> m.computeIfAbsent("c", k -> "3"));
    assertThrows(
        UnsupportedOperationException.class, () -> m.computeIfPresent("a", (k, v) -> v + v));
    assertThrows(UnsupportedOperationException.class, () -> m.compute("a", (k, v) -> "9"));
    assertThrows(UnsupportedOperationException.class, () -> m.merge("a", "9", (v1, v2) -> v1));

    // A mutating operation that would be a no-op on a modifiable map also throws.
    assertThrows(UnsupportedOperationException.class, () -> m.remove("nonexistent"));
    assertThrows(UnsupportedOperationException.class, () -> m.putAll(Collections.emptyMap()));

    // The map is unchanged.
    assertEquals(2, m.size());
    assertEquals("1", m.get("a"));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Views
  //

  @SuppressWarnings("modifiability:method.invocation") // testing that mutation throws
  @Test
  void keySetIsUnmodifiable() {
    UnmodifiableIdentityHashMap<String, String> m =
        UnmodifiableIdentityHashMap.wrap(newMap("a", "1", "b", "2"));
    Set<String> keys = m.keySet();
    assertEquals(2, keys.size());
    assertTrue(keys.contains("a"));
    assertThrows(UnsupportedOperationException.class, () -> keys.add("c"));
    assertThrows(UnsupportedOperationException.class, () -> keys.remove("a"));
    assertThrows(UnsupportedOperationException.class, () -> keys.clear());
    Iterator<String> itor = keys.iterator();
    itor.next();
    assertThrows(UnsupportedOperationException.class, () -> itor.remove());
  }

  @SuppressWarnings("modifiability:method.invocation") // testing that mutation throws
  @Test
  void valuesIsUnmodifiable() {
    UnmodifiableIdentityHashMap<String, String> m =
        UnmodifiableIdentityHashMap.wrap(newMap("a", "1", "b", "2"));
    Collection<String> values = m.values();
    assertEquals(2, values.size());
    assertTrue(values.contains("1"));
    assertThrows(UnsupportedOperationException.class, () -> values.remove("1"));
    assertThrows(UnsupportedOperationException.class, () -> values.clear());
    Iterator<String> itor = values.iterator();
    itor.next();
    assertThrows(UnsupportedOperationException.class, () -> itor.remove());
  }

  @SuppressWarnings("modifiability:method.invocation") // testing that mutation throws
  @Test
  void entrySetIsUnmodifiable() {
    UnmodifiableIdentityHashMap<String, String> m =
        UnmodifiableIdentityHashMap.wrap(newMap("a", "1", "b", "2"));
    Set<Map.Entry<@KeyFor("m") String, String>> entries = m.entrySet();
    assertEquals(2, entries.size());
    assertThrows(UnsupportedOperationException.class, () -> entries.clear());

    Iterator<Map.Entry<@KeyFor("m") String, String>> itor = entries.iterator();
    Map.Entry<String, String> entry = itor.next();
    // An entry's value cannot be set through the view.
    assertThrows(UnsupportedOperationException.class, () -> entry.setValue("9"));
    assertThrows(UnsupportedOperationException.class, () -> itor.remove());
  }
}
