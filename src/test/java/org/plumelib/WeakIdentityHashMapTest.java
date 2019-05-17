package org.plumelib.util;

import org.junit.Test;

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

    assert m.get(s1) == 1;
    assert m.get(s2) == 2;
    assert m.get(s3) == 3;
    assert m.get(s1a) == 1;
    assert m.get(s2a) == 2;
    assert m.get(s3a) == 3;

    m.remove(s1);
    m.remove(s2);
    m.remove(s3);
    assert m.get(s1) == null;
    assert m.get(s2) == null;
    assert m.get(s3) == null;
    assert m.get(s1a) == 1;
    assert m.get(s2a) == 2;
    assert m.get(s3a) == 3;
  }
}
