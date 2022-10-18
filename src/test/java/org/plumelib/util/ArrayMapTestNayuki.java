// This file was originally CompactHashMapTest.java.  Here is its header:
/*
 * Compact hash map test
 *
 * Copyright (c) 2020 Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/compact-hash-map-java
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nullness") // Should be removed after CF 3.12.1 is released
public final class ArrayMapTestNayuki {

  /* Utilities */

  private static Random rand = new Random();

  /* Test cases */

  @Test
  public void testPut() {
    ArrayMap<String, Integer> map = new ArrayMap<String, Integer>();
    assertEquals(null, map.put("a", 9));
    assertEquals(null, map.put("b", 8));
    assertEquals(null, map.put("c", 7));
    assertEquals(null, map.put("d", 6));
    assertEquals(null, map.put("e", 5));
    assertEquals(null, map.put("f", 4));
    assertEquals(null, map.put("g", 3));
    assertEquals(null, map.put("h", 2));
    assertEquals((Integer) 9, map.put("a", 0));
    assertEquals((Integer) 8, map.put("b", 1));
    assertEquals((Integer) 7, map.put("c", 2));
    assertEquals((Integer) 6, map.put("d", 3));
    assertEquals((Integer) 5, map.put("e", 4));
    assertEquals((Integer) 4, map.put("f", 5));
    assertEquals((Integer) 3, map.put("g", 6));
    assertEquals((Integer) 2, map.put("h", 7));
  }

  @Test
  public void testSize() {
    ArrayMap<String, Integer> map = new ArrayMap<String, Integer>();
    assertEquals(0, map.size());
    map.put("xy", 32);
    assertEquals(1, map.size());
    map.put("xyz", 27);
    assertEquals(2, map.size());
    assertEquals((Integer) 32, map.put("xy", 5));
    assertEquals(2, map.size());
    map.put("a", 0);
    map.put("b", 1);
    map.put("c", -1);
    assertEquals(5, map.size());
  }

  @Test
  public void testMediumSimple() {
    Map<String, Integer> map = new ArrayMap<String, Integer>();
    for (int i = 0; i < 100; i++) {
      assertNull(map.put(Integer.toString(i), i));
      assertEquals(i + 1, map.size());
      int j = rand.nextInt(20000) - 5000;
      assertEquals(j >= 0 && j <= i ? (Integer) j : null, map.get(Integer.toString(j)));
    }
  }

  @Test
  public void testMediumSeesaw() {
    Map<String, Integer> map0 = new HashMap<String, Integer>();
    ArrayMap<String, Integer> map1 = new ArrayMap<String, Integer>();
    for (int i = 0; i < 30; i++) {
      // Generate random data
      int n = rand.nextInt(30);
      String[] keys = new String[n];
      Integer[] values = new Integer[n];
      for (int j = 0; j < n; j++) {
        keys[j] = Integer.toString(rand.nextInt(100000), 36); // Can produce duplicates
        values[j] = rand.nextInt();
      }

      // Do all insertions
      for (int j = 0; j < n; j++) {
        assertEquals(map0.put(keys[j], values[j]), map1.put(keys[j], values[j]));
        String query = Integer.toString(rand.nextInt(100000), 36);
        assertTrue(map0.containsKey(query) == map1.containsKey(query));
        assertEquals(map0.get(query), map1.get(query));
      }
      assertEquals(map0.size(), map1.size()); // May be less than n due to duplicate keys

      // Do all removals
      for (int j = 0; j < n; j++) {
        assertEquals(map0.remove(keys[j]), map1.remove(keys[j]));
        String query = Integer.toString(rand.nextInt(100000), 36);
        assertTrue(map0.containsKey(query) == map1.containsKey(query));
        assertEquals(map0.get(query), map1.get(query));
      }
      assertEquals(0, map0.size());
      assertEquals(0, map1.size());
    }
  }

  @Test
  public void testLargeRandomly() {
    Map<String, Integer> map0 = new HashMap<String, Integer>();
    ArrayMap<String, Integer> map1 = new ArrayMap<String, Integer>();
    for (int i = 0; i < 1000; i++) {
      String key = Integer.toString(rand.nextInt(100000), 36);
      int op = rand.nextInt(10);
      if (op < 5) {
        int val = rand.nextInt();
        assertEquals(map0.put(key, val), map1.put(key, val));
      } else {
        assertEquals(map0.remove(key), map1.remove(key));
      }

      assertEquals(map0.size(), map1.size());
      String query = Integer.toString(rand.nextInt(100000), 36);
      assertTrue(map0.containsKey(query) == map1.containsKey(query));
      assertEquals(map0.get(query), map1.get(query));
    }
  }

  @Test
  public void testIteratorDump() {
    for (int i = 0; i < 100; i++) {
      // Generate random data
      int n = rand.nextInt(30);
      String[] keys = new String[n];
      Integer[] values = new Integer[n];
      for (int j = 0; j < n; j++) {
        keys[j] = Integer.toString(rand.nextInt(100000), 36); // Can produce duplicates
        values[j] = rand.nextInt();
      }

      // Do insertions and removals
      Map<String, Integer> map0 = new HashMap<String, Integer>();
      ArrayMap<String, Integer> map1 = new ArrayMap<String, Integer>();
      for (int j = 0; j < n / 2; j++) {
        map0.put(keys[j], values[j]);
        map1.put(keys[j], values[j]);
      }
      for (int j = n / 2; j < n; j++) {
        map0.remove(keys[j]);
        map1.remove(keys[j]);
      }

      // Test the iterator
      for (Map.Entry<String, Integer> entry : map1.entrySet())
        assertEquals(map0.remove(entry.getKey()), entry.getValue());
      assertEquals(0, map0.size());
    }
  }

  @Test
  public void testIteratorModifyRemove() {
    for (int i = 0; i < 100; i++) {
      // Generate random data
      int n = rand.nextInt(30);
      String[] keys = new String[n];
      Integer[] values = new Integer[n];
      for (int j = 0; j < n; j++) {
        keys[j] = Integer.toString(rand.nextInt(100), 36); // Can produce duplicates
        values[j] = rand.nextInt();
      }

      // Do insertions and removals
      Map<String, Integer> map0 = new HashMap<String, Integer>();
      ArrayMap<String, Integer> map1 = new ArrayMap<String, Integer>();
      for (int j = 0; j < n / 2; j++) {
        map0.put(keys[j], values[j]);
        map1.put(keys[j], values[j]);
      }
      for (int j = n / 2; j < n; j++) {
        map0.remove(keys[j]);
        map1.remove(keys[j]);
      }

      // Do iterator removals and map entry modifications
      double deleteProb = rand.nextDouble();
      for (Iterator<Map.Entry<String, Integer>> iter = map1.entrySet().iterator();
          iter.hasNext(); ) {
        Map.Entry<String, Integer> entry = iter.next();
        if (rand.nextDouble() < deleteProb) {
          // Note order of operations: must use the entry before modifying the iterator.
          map0.remove(entry.getKey());
          iter.remove();
        } else if (rand.nextDouble() < 0.2) {
          int value = rand.nextInt();
          entry.setValue(value);
          map0.put(entry.getKey(), value);
        }
      }
      assertEquals(map0.size(), map1.size());

      // Check remaining contents for sameness
      for (Map.Entry<String, Integer> entry : map1.entrySet()) {
        assertEquals(map0.remove(entry.getKey()), entry.getValue());
      }
      assertEquals(0, map0.size());
    }
  }
}
