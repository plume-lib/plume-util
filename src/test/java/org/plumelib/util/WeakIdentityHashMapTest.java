package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class WeakIdentityHashMapTest {

  /**
   * These tests could be much more thorough. Basically all that is tested is that identity is used
   * rather than a normal hash. The tests will fail however, if WeakHashMap is swapped for
   * WeakIdentityHashMap.
   */
  @Test
  public void testWeakIdentityHashMap() {

    String s1 = "one";
    String s2 = "two";
    String s3 = "three";

    WeakIdentityHashMap<String, Integer> m = new WeakIdentityHashMap<>();
    // WeakHashMap<String,Integer> m = new WeakHashMap<>();

    m.put(s1, 1);
    m.put(s2, 2);
    m.put(s3, 3);

    String s1a = new String(s1);
    String s2a = new String(s2);
    String s3a = new String(s3);

    m.put(s1a, 1);
    m.put(s2a, 2);
    m.put(s3a, 3);

    assertTrue(m.get(s1) == 1);
    assertTrue(m.get(s2) == 2);
    assertTrue(m.get(s3) == 3);
    assertTrue(m.get(s1a) == 1);
    assertTrue(m.get(s2a) == 2);
    assertTrue(m.get(s3a) == 3);

    m.remove(s1);
    m.remove(s2);
    m.remove(s3);
    assertTrue(m.get(s1) == null);
    assertTrue(m.get(s2) == null);
    assertTrue(m.get(s3) == null);
    assertTrue(m.get(s1a) == 1);
    assertTrue(m.get(s2a) == 2);
    assertTrue(m.get(s3a) == 3);
  }
}
