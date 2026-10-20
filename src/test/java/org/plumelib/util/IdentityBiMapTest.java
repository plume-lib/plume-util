package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

/** Test the IdentityBiMap class. */
@SuppressWarnings({
  "interning:not.interned", // tests of an identity map compare by identity
  "lock", // not yet annotated for the Lock Checker
  "nullness:argument", // JUnit's assertSame() is not annotated to accept null
  "PMD.UseEqualsToCompareStrings", // tests of an identity map compare by identity
  "modifiability", // not yet annotated for the Modifiability Checker
  "signedness", // not yet annotated for the Signedness Checker
})
final class IdentityBiMapTest {

  IdentityBiMapTest() {}

  /**
   * Returns a new string that is equal to, but not identical to, the given string.
   *
   * @param s a string
   * @return a new string equal to {@code s}
   */
  @SuppressWarnings({
    "StringCopy", // the point is to create a non-identical copy
    "PMD.StringInstantiation", // the point is to create a non-identical copy
  })
  private static String copy(String s) {
    return new String(s);
  }

  /**
   * Checks that the forward and inverse views of the given map are consistent.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param m the map to check
   */
  private static <K, V> void checkConsistent(IdentityBiMap<K, V> m) {
    IdentityBiMap<V, K> inv = m.inverse();
    assertEquals(m.size(), inv.size());
    for (Map.Entry<K, V> e : m.entrySet()) {
      assertTrue(inv.containsKey(e.getValue()));
      assertSame(e.getKey(), inv.get(e.getValue()));
    }
    for (Map.Entry<V, K> e : inv.entrySet()) {
      assertTrue(m.containsKey(e.getValue()));
      assertSame(e.getKey(), m.get(e.getValue()));
    }
  }

  @Test
  void emptyMap() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    assertEquals(0, m.size());
    assertTrue(m.isEmpty());
    assertTrue(m.inverse().isEmpty());
    assertNull(m.get("a"));
    assertNull(m.getKey("a"));
    assertEquals("{}", m.toString());
  }

  @Test
  void putAndGet() {
    IdentityBiMap<String, Integer> m = new IdentityBiMap<>();
    Integer one = 1;
    Integer two = 2;
    assertNull(m.put("one", one));
    assertNull(m.put("two", two));
    assertEquals(2, m.size());
    assertSame(one, m.get("one"));
    assertSame(two, m.get("two"));
    assertSame("one", m.getKey(one));
    assertSame("two", m.inverse().get(two));
    assertTrue(m.containsKey("one"));
    assertTrue(m.containsValue(one));
    assertFalse(m.containsValue(3));
    checkConsistent(m);
  }

  @Test
  void identityNotEquals() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    String k1 = "key";
    String k2 = copy(k1);
    String v1 = "value";
    String v2 = copy(v1);
    m.put(k1, v1);
    // k2 and v2 are equal to, but not identical to, k1 and v1.
    assertFalse(m.containsKey(k2));
    assertFalse(m.containsValue(v2));
    assertNull(m.get(k2));
    assertNull(m.getKey(v2));
    m.put(k2, v2);
    assertEquals(2, m.size());
    assertSame(v1, m.get(k1));
    assertSame(v2, m.get(k2));
    assertSame(k1, m.getKey(v1));
    assertSame(k2, m.getKey(v2));
    checkConsistent(m);
  }

  @Test
  void putReplacesValue() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    assertSame("1", m.put("a", "2"));
    assertEquals(1, m.size());
    assertSame("2", m.get("a"));
    assertFalse(m.containsValue("1"));
    assertNull(m.getKey("1"));
    checkConsistent(m);
  }

  @Test
  void putSameMappingIsNoOp() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    assertSame("1", m.put("a", "1"));
    assertEquals(1, m.size());
    checkConsistent(m);
  }

  @Test
  void putDuplicateValueThrows() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    assertThrows(IllegalArgumentException.class, () -> m.put("b", "1"));
    // The map is unchanged.
    assertEquals(1, m.size());
    assertSame("1", m.get("a"));
    assertFalse(m.containsKey("b"));
    checkConsistent(m);
    // An equal but non-identical value is permitted.
    m.put("b", copy("1"));
    assertEquals(2, m.size());
    checkConsistent(m);
  }

  @Test
  void forcePut() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    assertSame("2", m.forcePut("b", "1"));
    assertEquals(1, m.size());
    assertFalse(m.containsKey("a"));
    assertSame("1", m.get("b"));
    assertSame("b", m.getKey("1"));
    assertFalse(m.containsValue("2"));
    checkConsistent(m);

    assertNull(m.forcePut("c", "1"));
    assertEquals(1, m.size());
    assertSame("1", m.get("c"));
    checkConsistent(m);
  }

  @Test
  void remove() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    assertSame("1", m.remove("a"));
    assertNull(m.remove("a"));
    assertEquals(1, m.size());
    assertFalse(m.containsValue("1"));
    assertNull(m.remove(copy("b")));
    assertEquals(1, m.size());
    checkConsistent(m);
  }

  @Test
  void nullKeysAndValues() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put(null, "1");
    m.put("a", null);
    assertEquals(2, m.size());
    assertTrue(m.containsKey(null));
    assertTrue(m.containsValue(null));
    assertSame("1", m.get(null));
    assertNull(m.get("a"));
    assertSame("a", m.getKey(null));
    assertNull(m.getKey("1"));
    assertThrows(IllegalArgumentException.class, () -> m.put("b", null));
    checkConsistent(m);
    assertNull(m.remove("a"));
    assertFalse(m.containsValue(null));
    assertSame("1", m.remove(null));
    assertTrue(m.isEmpty());
    checkConsistent(m);
  }

  @Test
  void inverseView() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    IdentityBiMap<String, String> inv = m.inverse();
    assertSame(m, inv.inverse());
    m.put("a", "1");
    assertSame("a", inv.get("1"));
    inv.put("2", "b");
    assertSame("2", m.get("b"));
    assertThrows(IllegalArgumentException.class, () -> inv.put("3", "a"));
    inv.remove("1");
    assertFalse(m.containsKey("a"));
    assertEquals(1, m.size());
    checkConsistent(m);
    inv.clear();
    assertTrue(m.isEmpty());
  }

  @Test
  void copyConstructorAndPutAll() {
    Map<String, String> source = new HashMap<>();
    source.put("a", "1");
    source.put("b", "2");
    IdentityBiMap<String, String> m = new IdentityBiMap<>(source);
    assertEquals(2, m.size());
    assertSame("1", m.get("a"));
    checkConsistent(m);

    Map<String, String> bad = new HashMap<>();
    bad.put("a", "1");
    bad.put("b", "1");
    assertThrows(IllegalArgumentException.class, () -> new IdentityBiMap<>(bad));
  }

  @Test
  void keySetView() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    Set<String> keys = m.keySet();
    assertEquals(2, keys.size());
    assertTrue(keys.contains("a"));
    assertFalse(keys.contains(copy("a")));
    assertFalse(keys.remove(copy("a")));
    assertTrue(keys.remove("a"));
    assertFalse(m.containsValue("1"));
    assertEquals(1, keys.size());
    checkConsistent(m);
    m.put("c", "3");
    assertTrue(keys.contains("c"));
    keys.clear();
    assertTrue(m.isEmpty());
    assertTrue(m.inverse().isEmpty());
  }

  @Test
  void valuesView() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    Set<String> values = m.values();
    assertEquals(2, values.size());
    assertTrue(values.contains("1"));
    assertFalse(values.contains(copy("1")));
    assertTrue(values.remove("1"));
    assertFalse(m.containsKey("a"));
    assertEquals(1, m.size());
    checkConsistent(m);
  }

  @Test
  void iteratorRemove() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    m.put("c", "3");
    Iterator<String> itor = m.keySet().iterator();
    assertThrows(IllegalStateException.class, itor::remove);
    while (itor.hasNext()) {
      if (itor.next() == "b") {
        itor.remove();
        assertThrows(IllegalStateException.class, itor::remove);
      }
    }
    assertEquals(2, m.size());
    assertFalse(m.containsKey("b"));
    assertFalse(m.containsValue("2"));
    checkConsistent(m);

    Iterator<String> vitor = m.values().iterator();
    while (vitor.hasNext()) {
      if (vitor.next() == "3") {
        vitor.remove();
      }
    }
    assertEquals(1, m.size());
    assertFalse(m.containsKey("c"));
    checkConsistent(m);

    Iterator<Map.Entry<String, String>> eitor = m.entrySet().iterator();
    eitor.next();
    eitor.remove();
    assertTrue(m.isEmpty());
    assertTrue(m.inverse().isEmpty());
  }

  @Test
  void iteratorFailFast() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    Iterator<String> itor = m.keySet().iterator();
    itor.next();
    m.put("c", "3");
    assertThrows(ConcurrentModificationException.class, itor::next);
  }

  @Test
  void entrySetView() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    Set<Map.Entry<String, String>> entries = m.entrySet();
    assertEquals(2, entries.size());
    assertTrue(entries.contains(new AbstractMap.SimpleEntry<>("a", "1")));
    assertFalse(entries.contains(new AbstractMap.SimpleEntry<>("a", "2")));
    assertFalse(entries.contains(new AbstractMap.SimpleEntry<>(copy("a"), "1")));
    assertFalse(entries.contains(new AbstractMap.SimpleEntry<>("a", copy("1"))));
    Set<?> wildcardEntries = entries;
    assertFalse(wildcardEntries.contains("a"));
    assertFalse(entries.remove(new AbstractMap.SimpleEntry<>("a", "2")));
    assertTrue(entries.remove(new AbstractMap.SimpleEntry<>("a", "1")));
    assertEquals(1, m.size());
    assertFalse(m.containsValue("1"));
    checkConsistent(m);
  }

  @Test
  void entrySetValue() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    for (Map.Entry<String, String> e : m.entrySet()) {
      if (e.getKey() == "a") {
        assertSame("1", e.setValue("3"));
        assertSame("3", e.getValue());
        assertThrows(IllegalArgumentException.class, () -> e.setValue("2"));
        assertSame("3", e.getValue());
      }
    }
    assertSame("3", m.get("a"));
    assertSame("a", m.getKey("3"));
    assertFalse(m.containsValue("1"));
    checkConsistent(m);
  }

  @Test
  void entryEqualsAndHashCode() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    Map.Entry<String, String> e = m.entrySet().iterator().next();
    IdentityHashMap<String, String> ihm = new IdentityHashMap<>();
    ihm.put("a", "1");
    Map.Entry<String, String> ihmEntry = ihm.entrySet().iterator().next();
    assertEquals(e, ihmEntry);
    assertEquals(ihmEntry.hashCode(), e.hashCode());
    assertEquals(e, new AbstractMap.SimpleEntry<>("a", "1"));
    assertNotEquals(e, new AbstractMap.SimpleEntry<>(copy("a"), "1"));
    assertEquals("a=1", e.toString());
  }

  @Test
  void equalsAndHashCode() {
    IdentityBiMap<String, String> m1 = new IdentityBiMap<>();
    IdentityBiMap<String, String> m2 = new IdentityBiMap<>();
    assertEquals(m1, m2);
    m1.put("a", "1");
    m1.put("b", "2");
    m2.put("b", "2");
    m2.put("a", "1");
    assertEquals(m1, m2);
    assertEquals(m1.hashCode(), m2.hashCode());

    IdentityHashMap<String, String> ihm = new IdentityHashMap<>();
    ihm.put("a", "1");
    ihm.put("b", "2");
    assertEquals(m1, ihm);
    assertEquals(ihm, m1);
    assertEquals(ihm.hashCode(), m1.hashCode());

    // A HashMap is compared using equals().
    Map<String, String> hm = new HashMap<>();
    hm.put("a", "1");
    hm.put("b", "2");
    assertEquals(m1, hm);

    IdentityBiMap<String, String> m3 = new IdentityBiMap<>();
    m3.put(copy("a"), "1");
    m3.put("b", "2");
    assertNotEquals(m1, m3);

    IdentityBiMap<String, String> m4 = new IdentityBiMap<>();
    m4.put("a", copy("1"));
    m4.put("b", "2");
    assertNotEquals(m1, m4);

    assertNotEquals(m1, "a string");
  }

  @Test
  void viewSetEquality() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    IdentityHashMap<String, String> ihm = new IdentityHashMap<>();
    ihm.put("a", "1");
    ihm.put("b", "2");
    assertEquals(ihm.keySet(), m.keySet());
    assertEquals(ihm.keySet().hashCode(), m.keySet().hashCode());
    assertEquals(ihm.entrySet(), m.entrySet());
    assertEquals(ihm.entrySet().hashCode(), m.entrySet().hashCode());
  }

  @Test
  void removeKeyValue() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    assertFalse(m.remove("a", copy("1")));
    assertFalse(m.remove(copy("a"), "1"));
    assertFalse(m.remove("b", null));
    assertEquals(1, m.size());
    assertTrue(m.remove("a", "1"));
    assertTrue(m.isEmpty());
    assertTrue(m.inverse().isEmpty());

    m.put("b", null);
    assertTrue(m.remove("b", null));
    assertTrue(m.isEmpty());
  }

  @Test
  void replaceKeyOldValueNewValue() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    assertFalse(m.replace("a", copy("1"), "3"));
    assertFalse(m.replace("c", null, "3"));
    assertFalse(m.containsKey("c"));
    assertSame("1", m.get("a"));
    assertTrue(m.replace("a", "1", "3"));
    assertSame("3", m.get("a"));
    assertFalse(m.containsValue("1"));
    assertThrows(IllegalArgumentException.class, () -> m.replace("a", "3", "2"));
    assertSame("3", m.get("a"));
    checkConsistent(m);
  }

  @Test
  void removeAllUsesArgumentContains() {
    // The result does not depend on the relative sizes of the map and the argument.
    String a = copy("a");
    IdentityBiMap<String, String> m1 = new IdentityBiMap<>();
    m1.put(a, "1");
    assertTrue(m1.keySet().removeAll(List.of("a", "b")));
    assertTrue(m1.isEmpty());
    IdentityBiMap<String, String> m2 = new IdentityBiMap<>();
    m2.put(a, "1");
    m2.put("c", "3");
    m2.put("d", "4");
    assertTrue(m2.keySet().removeAll(List.of("a")));
    assertEquals(2, m2.size());
    assertFalse(m2.containsKey(a));
    checkConsistent(m2);

    assertFalse(m2.values().removeAll(List.of("5")));
    assertTrue(m2.values().removeAll(List.of("3", "5", "6")));
    assertEquals(1, m2.size());
    assertFalse(m2.containsKey("c"));
    checkConsistent(m2);

    assertTrue(m2.entrySet().removeAll(List.of(new AbstractMap.SimpleEntry<>("d", "4"))));
    assertTrue(m2.isEmpty());
    assertTrue(m2.inverse().isEmpty());
  }

  @Test
  void replaceAllPermutation() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "x");
    m.put("b", "y");
    m.put("c", "z");
    m.replaceAll((k, v) -> v == "x" ? "y" : v == "y" ? "x" : v);
    assertSame("y", m.get("a"));
    assertSame("x", m.get("b"));
    assertSame("z", m.get("c"));
    checkConsistent(m);
  }

  @Test
  void replaceAllDuplicateLeavesMapUnchanged() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "x");
    m.put("b", "y");
    assertThrows(IllegalArgumentException.class, () -> m.replaceAll((k, v) -> "z"));
    assertEquals(2, m.size());
    assertSame("x", m.get("a"));
    assertSame("y", m.get("b"));
    checkConsistent(m);
  }

  @Test
  @SuppressWarnings("IdentityHashMapUsage") // comparing with an IdentityHashMap is the point
  void equalsIsSymmetricWithIdentityHashMap() {
    String k = "k";
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put(k, "v");
    IdentityHashMap<String, String> ihm = new IdentityHashMap<>();
    ihm.put(k, copy("v"));
    assertEquals(ihm.equals(m), m.equals(ihm));
    assertNotEquals(m, ihm);
    ihm.put(k, "v");
    assertEquals(m, ihm);
    assertEquals(ihm, m);
    ihm.put(copy(k), "v");
    ihm.remove(k);
    assertNotEquals(m, ihm);
    assertNotEquals(ihm, m);
  }

  @Test
  void equalsIsSymmetricWithHashMap() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    Map<String, String> hm = new HashMap<>();
    hm.put(copy("a"), "1");
    assertEquals(hm.equals(m), m.equals(hm));
    assertNotEquals(m, hm);
    hm.clear();
    hm.put("a", copy("1"));
    assertEquals(hm.equals(m), m.equals(hm));
    assertEquals(m, hm);
  }

  @Test
  void equalsWithNullValues() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", null);
    Map<String, String> hm = new HashMap<>();
    hm.put("a", null);
    assertEquals(m, hm);
    assertEquals(hm, m);
    hm.clear();
    hm.put("b", null);
    assertNotEquals(m, hm);
    assertNotEquals(hm, m);
  }

  /** A map whose equals() delegates to its argument's equals(). */
  @SuppressWarnings("serial")
  private static final class DelegatingEqualsMap extends HashMap<String, String> {
    /** Creates a new DelegatingEqualsMap. */
    DelegatingEqualsMap() {}

    @Override
    public boolean equals(@Nullable Object o) {
      return o instanceof Map<?, ?> && o != this && o.equals(this);
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod") // equals() is overridden
    public int hashCode() {
      return super.hashCode();
    }
  }

  @Test
  void equalsDoesNotRecurse() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    DelegatingEqualsMap dm = new DelegatingEqualsMap();
    dm.put("a", "1");
    assertEquals(m, dm);
    assertEquals(dm, m);
  }

  @Test
  void entryAfterRemoval() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    Map.Entry<String, String> e = m.entrySet().iterator().next();
    String key = e.getKey();
    String value = e.getValue();
    Set<Map.Entry<String, String>> set = new HashSet<>();
    set.add(e);
    int hashCode = e.hashCode();
    m.remove(key);
    // The entry still describes the mapping that the iterator returned.
    assertSame(key, e.getKey());
    assertSame(value, e.getValue());
    assertEquals(hashCode, e.hashCode());
    assertTrue(set.contains(e));
    assertFalse(m.entrySet().contains(e));
    assertFalse(m.entrySet().remove(e));
    assertEquals(1, m.size());
  }

  @Test
  @SuppressWarnings("index:array.access.unsafe.high.constant") // the map has one mapping
  void entrySetToArray() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    Object[] array = m.entrySet().toArray();
    assertEquals(1, array.length);
    assertEquals(new AbstractMap.SimpleEntry<>("a", "1"), array[0]);
    // The elements compare by identity.  (assertNotEquals calls its first argument's equals().)
    assertNotEquals(array[0], new AbstractMap.SimpleEntry<>(copy("a"), "1"));
    List<Map.Entry<String, String>> list = new ArrayList<>(m.entrySet());
    assertNotEquals(list.get(0), new AbstractMap.SimpleEntry<>("a", copy("1")));
  }

  @Test
  void valuesIterateInKeyOrder() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    for (int i = 0; i < 50; i++) {
      m.put("key" + i, "value" + i);
    }
    Iterator<String> keys = m.keySet().iterator();
    Iterator<String> values = m.values().iterator();
    for (Map.Entry<String, String> e : m.entrySet()) {
      assertSame(e.getKey(), keys.next());
      assertSame(e.getValue(), values.next());
    }
    assertFalse(keys.hasNext());
    assertFalse(values.hasNext());
  }

  @Test
  void valuesSetHashCodeAndIteratorRemove() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    assertEquals(m.inverse().keySet(), m.values());
    assertEquals(m.inverse().keySet().hashCode(), m.values().hashCode());
    Iterator<String> itor = m.values().iterator();
    String value = itor.next();
    itor.remove();
    assertFalse(m.containsValue(value));
    assertEquals(1, m.size());
    checkConsistent(m);
  }

  @Test
  void iteratorRemoveAfterValueChange() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("k", "x");
    Iterator<Map.Entry<String, String>> eitor = m.entrySet().iterator();
    eitor.next().setValue("y");
    eitor.remove();
    assertTrue(m.isEmpty());
    assertTrue(m.inverse().isEmpty());
    m.put("z", "y");
    checkConsistent(m);

    Iterator<String> kitor = m.keySet().iterator();
    String key = kitor.next();
    m.put(key, "w");
    kitor.remove();
    assertTrue(m.isEmpty());
    assertTrue(m.inverse().isEmpty());
  }

  /** A value whose equals() throws ClassCastException. */
  private static final class ThrowingEquals {
    /** Creates a new ThrowingEquals. */
    ThrowingEquals() {}

    @Override
    public boolean equals(@Nullable Object o) {
      throw new ClassCastException();
    }

    @Override
    public int hashCode() {
      return 0;
    }
  }

  @Test
  void equalsCatchesClassCastException() {
    IdentityBiMap<String, Object> m = new IdentityBiMap<>();
    m.put("a", "1");
    Map<String, Object> hm = new HashMap<>();
    hm.put("a", new ThrowingEquals());
    // assertNotEquals calls its first argument's equals().
    assertNotEquals(m, hm);
  }

  @Test
  void copyConstructorFromIdentityBiMap() {
    IdentityBiMap<String, String> m1 = new IdentityBiMap<>();
    m1.put("a", "1");
    m1.put("b", "2");
    IdentityBiMap<String, String> m2 = new IdentityBiMap<>(m1);
    assertEquals(m1, m2);
    checkConsistent(m2);
    m2.put("c", "3");
    assertFalse(m1.containsKey("c"));
    assertFalse(m1.containsValue("3"));
  }

  @Test
  void setValueAfterRemoval() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    Iterator<Map.Entry<String, String>> itor = m.entrySet().iterator();
    Map.Entry<String, String> e = itor.next();
    itor.remove();
    assertThrows(IllegalStateException.class, () -> e.setValue("3"));
    assertEquals(1, m.size());
    assertFalse(m.containsKey(e.getKey()));
    checkConsistent(m);
  }

  @Test
  void setValueReturnsMapValue() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    Map.Entry<String, String> e = m.entrySet().iterator().next();
    m.put("a", "2");
    assertSame("2", e.setValue("3"));
    assertSame("3", e.getValue());
    assertSame("3", m.get("a"));
    checkConsistent(m);
  }

  @Test
  void serialization() throws IOException, ClassNotFoundException {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(m);
    }
    IdentityBiMap<?, ?> copy;
    try (ObjectInputStream in =
        new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
      copy = (IdentityBiMap<?, ?>) in.readObject();
    }
    assertEquals(2, copy.size());
    assertSame(copy, copy.inverse().inverse());
    // The deserialized keys and values are new objects, so compare using equals().
    assertEquals(new HashMap<Object, Object>(m), new HashMap<Object, Object>(copy));
    checkConsistent(copy);
  }

  @Test
  void entrySetHashCode() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", null);
    int sum = 0;
    for (Map.Entry<String, String> e : m.entrySet()) {
      sum += e.hashCode();
    }
    assertEquals(sum, m.entrySet().hashCode());
    assertEquals(m.hashCode(), m.entrySet().hashCode());
  }

  @Test
  void toStringSelfReference() {
    IdentityBiMap<Object, Object> m = new IdentityBiMap<>();
    m.put("self", m);
    m.put(m.inverse(), "inverse");
    String s = m.toString();
    assertTrue(s.contains("self=(this Map)"), s);
    assertTrue(s.contains("(this Map's inverse)=inverse"), s);
    String inverseString = m.inverse().toString();
    assertTrue(inverseString.contains("(this Map's inverse)=self"), inverseString);
    assertTrue(inverseString.contains("inverse=(this Map)"), inverseString);
  }

  @Test
  void putAllInverse() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "b");
    m.putAll(m.inverse());
    assertEquals(2, m.size());
    assertSame("b", m.get("a"));
    assertSame("a", m.get("b"));
    checkConsistent(m);

    // Putting a map's own mappings is a no-op.
    m.putAll(m);
    assertEquals(2, m.size());
    checkConsistent(m);
  }

  @Test
  void forEach() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    m.put("b", "2");
    IdentityHashMap<String, String> seen = new IdentityHashMap<>();
    m.forEach(seen::put);
    assertEquals(seen, m);
    assertThrows(ConcurrentModificationException.class, () -> m.forEach((k, v) -> m.remove(k)));
  }

  @Test
  void defaultMapMethods() {
    IdentityBiMap<String, String> m = new IdentityBiMap<>();
    m.put("a", "1");
    assertSame("1", m.putIfAbsent("a", "2"));
    assertNull(m.putIfAbsent("b", "2"));
    assertThrows(IllegalArgumentException.class, () -> m.putIfAbsent("c", "1"));
    assertSame("1", m.replace("a", "3"));
    assertFalse(m.containsValue("1"));
    m.replaceAll((k, v) -> v + "!");
    assertEquals("3!", m.get("a"));
    assertEquals("2!", m.get("b"));
    checkConsistent(m);
  }
}
