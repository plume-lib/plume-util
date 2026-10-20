package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
