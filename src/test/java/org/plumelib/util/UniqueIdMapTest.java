package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test the UniqueIdMap class. */
final class UniqueIdMapTest {

  UniqueIdMapTest() {}

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

  @Test
  void idsCountUpFromZero() {
    UniqueIdMap<Object> m = new UniqueIdMap<>();
    Object a = new Object();
    Object b = new Object();
    Object c = new Object();
    assertEquals(0, m.get(a));
    assertEquals(1, m.get(b));
    assertEquals(2, m.get(c));
  }

  @Test
  void idsAreStable() {
    UniqueIdMap<Object> m = new UniqueIdMap<>();
    Object a = new Object();
    Object b = new Object();
    long aId = m.get(a);
    long bId = m.get(b);
    assertEquals(aId, m.get(a));
    assertEquals(bId, m.get(b));
    assertEquals(aId, m.get(a));
    assertNotEquals(aId, bId);
  }

  @Test
  void eachMapHasItsOwnCounter() {
    UniqueIdMap<Object> m1 = new UniqueIdMap<>();
    UniqueIdMap<Object> m2 = new UniqueIdMap<>();
    Object a = new Object();
    Object b = new Object();
    assertEquals(0, m1.get(a));
    assertEquals(1, m1.get(b));
    // m2's counter is independent of m1's.
    assertEquals(0, m2.get(b));
    assertEquals(1, m2.get(a));
  }

  @Test
  void idsAreIdentityBased() {
    UniqueIdMap<List<String>> m = new UniqueIdMap<>();
    List<String> a1 = newList("a");
    List<String> a2 = newList("a");
    assertEquals(a1, a2);
    // Equal but non-identical objects get different IDs.
    assertNotEquals(m.get(a1), m.get(a2));
    assertEquals(0, m.get(a1));
    assertEquals(1, m.get(a2));
  }

  @Test
  void idsAreUnaffectedByMutationOfTheKey() {
    UniqueIdMap<List<String>> m = new UniqueIdMap<>();
    List<String> a = newList("a");
    long id = m.get(a);
    // The map is identity-based, so mutating the key (and thus its hash code) does not lose it.
    a.add("b");
    assertEquals(id, m.get(a));
  }
}
