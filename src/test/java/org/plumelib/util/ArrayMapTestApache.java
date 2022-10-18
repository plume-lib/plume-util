/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "rawtypes",
  "unchecked",
  "deprecation",
  "removal",
  "nullness", // contains intentional misuses; exceptions are caught in catch statments
  "index", // contains intentional misuses; exceptions are caught in catch statments
  "interning", // ad hoc equality tests
  "BoxedPrimitiveConstructor",
  "BoxedPrimitiveEquality",
  "CatchAndPrintStackTrace",
  "CollectionIncompatibleType",
  "LogicalAssignment",
  "ReferenceEquality",
  "UnnecessaryParentheses"
})
public class ArrayMapTestApache {
  static class MockMap extends AbstractMap {
    @Override
    public Set entrySet(@GuardSatisfied MockMap this) {
      return Collections.EMPTY_SET;
    }

    @Override
    public int size(@GuardSatisfied MockMap this) {
      return 0;
    }
  }

  private static class MockMapNull extends AbstractMap {
    @Override
    public Set entrySet(@GuardSatisfied MockMapNull this) {
      return null;
    }

    @Override
    public int size(@GuardSatisfied MockMapNull this) {
      return 10;
    }
  }

  interface MockInterface {
    public String mockMethod();
  }

  static class MockClass implements MockInterface {
    @Override
    public String mockMethod() {
      return "This is a MockClass";
    }
  }

  static class MockHandler implements InvocationHandler {
    Object obj;

    public MockHandler(Object o) {
      obj = o;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
      Object result = null;
      try {
        result = m.invoke(obj, args);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return result;
    }
  }

  @Nullable ArrayMap hm;
  static final int hmSize = 100;
  Object @Nullable [] objArray;
  Object @Nullable [] objArray2;
  /** java.util.ArrayMap#ArrayMap() */
  @Test
  public void test_Constructor() {
    // Test for method java.util.ArrayMap()
    ArrayMap hm2 = new ArrayMap();
    assertEquals(0, hm2.size());
  }
  /** java.util.ArrayMap#ArrayMap(int) */
  @Test
  public void test_ConstructorI() {
    // Test for method java.util.ArrayMap(int)
    ArrayMap hm2 = new ArrayMap(5);
    assertEquals(0, hm2.size());
    try {
      new ArrayMap(-1);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
      // expected
    }
    ArrayMap empty = new ArrayMap(0);
    assertNull(empty.get("nothing"));
    empty.put("something", "here");
    assertTrue(empty.get("something") == "here"); // interned
  }
  /** java.util.ArrayMap#ArrayMap(int, float) */
  @Test
  public void test_ConstructorIF() {
    // Test for method java.util.ArrayMap(int, float)
    ArrayMap hm2 = new ArrayMap(5);
    assertEquals(0, hm2.size());
    ArrayMap empty = new ArrayMap(0);
    assertNull(empty.get("nothing"));
    empty.put("something", "here");
    assertTrue(empty.get("something") == "here"); // interned
  }
  /** java.util.ArrayMap#ArrayMap(java.util.Map) */
  @Test
  public void test_ConstructorLjava_util_Map() {
    // Test for method java.util.ArrayMap(java.util.Map)
    Map myMap = new TreeMap();
    for (int counter = 0; counter < hmSize; counter++)
      myMap.put(objArray2[counter], objArray[counter]);
    ArrayMap hm2 = new ArrayMap(myMap);
    for (int counter = 0; counter < hmSize; counter++)
      assertTrue(hm.get(objArray2[counter]) == hm2.get(objArray2[counter]));
    Map mockMap = new MockMap();
    hm = new ArrayMap(mockMap);
    assertEquals(hm, mockMap);
  }
  /** java.util.ArrayMap#clear() */
  @Test
  public void test_clear() {
    hm.clear();
    assertEquals(0, hm.size());
    for (int i = 0; i < hmSize; i++) assertNull(hm.get(objArray2[i]));
    // Check clear on a large loaded map of Integer keys
    ArrayMap<Integer, String> map = new ArrayMap<Integer, String>();
    for (int i = -67; i < 68; i++) {
      map.put(i, "foobar");
    }
    map.clear();
    assertEquals(0, hm.size());
    for (int i = -67; i < 68; i++) {
      assertNull(map.get(i));
    }
  }
  /** java.util.ArrayMap#clone() */
  @Test
  public void test_clone() {
    // Test for method java.lang.Object java.util.ArrayMap.clone()
    ArrayMap hm2 = hm.clone();
    assertTrue(hm2 != hm);
    for (int counter = 0; counter < hmSize; counter++)
      assertTrue(hm.get(objArray2[counter]) == hm2.get(objArray2[counter]));
    ArrayMap map = new ArrayMap();
    map.put("key", "value");
    // get the keySet() and values() on the original Map
    Set keys = map.keySet();
    Collection values = map.values();
    assertEquals("value", values.iterator().next());
    assertEquals("key", keys.iterator().next());
    AbstractMap map2 = (AbstractMap) map.clone();
    map2.put("key", "value2");
    Collection values2 = map2.values();
    assertTrue(values2 != values);
    // values() and keySet() on the cloned() map should be different
    assertEquals("value2", values2.iterator().next());
    map2.clear();
    map2.put("key2", "value3");
    Set key2 = map2.keySet();
    assertTrue(key2 != keys);
    assertEquals("key2", key2.iterator().next());
    // regresion test for HARMONY-4603
    ArrayMap hashmap = new ArrayMap();
    MockClonable mock = new MockClonable(1);
    hashmap.put(1, mock);
    assertEquals(1, ((MockClonable) hashmap.get(1)).i);
    ArrayMap hm3 = hashmap.clone();
    assertEquals(1, ((MockClonable) hm3.get(1)).i);
    mock.i = 0;
    assertEquals(0, ((MockClonable) hashmap.get(1)).i);
    assertEquals(0, ((MockClonable) hm3.get(1)).i);
  }
  /** java.util.ArrayMap#containsKey(java.lang.Object) */
  @Test
  public void test_containsKeyLjava_lang_Object() {
    // Test for method boolean
    // java.util.ArrayMap.containsKey(java.lang.Object)
    assertTrue(hm.containsKey(new Integer(87).toString()));
    assertTrue(!hm.containsKey("KKDKDKD"));
    ArrayMap m = new ArrayMap();
    m.put(null, "test");
    assertTrue(m.containsKey(null));
    assertTrue(!m.containsKey(new Integer(0)));
  }
  /** java.util.ArrayMap#containsValue(java.lang.Object) */
  @Test
  public void test_containsValueLjava_lang_Object() {
    // Test for method boolean
    // java.util.ArrayMap.containsValue(java.lang.Object)
    assertTrue(hm.containsValue(new Integer(87)));
    assertTrue(!hm.containsValue(new Integer(-9)));
  }
  /** java.util.ArrayMap#entrySet() */
  @Test
  public void test_entrySet() {
    // Test for method java.util.Set java.util.ArrayMap.entrySet()
    Set s = hm.entrySet();
    Iterator i = s.iterator();
    assertTrue(hm.size() == s.size());
    while (i.hasNext()) {
      Map.Entry m = (Map.Entry) i.next();
      assertTrue(hm.containsKey(m.getKey()) && hm.containsValue(m.getValue()));
    }
    Iterator iter = s.iterator();
    s.remove(iter.next());
    assertEquals(101, s.size());
  }
  /** java.util.ArrayMap#entrySet() */
  @Test
  public void test_entrySetEquals() {
    Set s1 = hm.entrySet();
    Set s2 = new ArrayMap(hm).entrySet();
    assertEquals(s1, s2);
  }
  /** java.util.ArrayMap#entrySet() */
  @Test
  public void test_removeFromViews() {
    hm.put("A", null);
    hm.put("B", null);
    assertTrue(hm.keySet().remove("A"));
    Map<String, String> m2 = new ArrayMap<String, String>();
    m2.put("B", null);
    assertTrue(hm.entrySet().remove(m2.entrySet().iterator().next()));
  }
  /** java.util.ArrayMap#get(java.lang.Object) */
  @Test
  public void test_getLjava_lang_Object() {
    // Test for method java.lang.Object
    // java.util.ArrayMap.get(java.lang.Object)
    assertNull(hm.get("T"));
    hm.put("T", "HELLO");
    assertEquals("HELLO", hm.get("T"));
    ArrayMap m = new ArrayMap();
    m.put(null, "test");
    assertEquals("test", m.get(null));
    assertNull(m.get(new Integer(0)));
  }
  /** java.util.ArrayMap#isEmpty() */
  @Test
  public void test_isEmpty() {
    // Test for method boolean java.util.ArrayMap.isEmpty()
    assertTrue(new ArrayMap().isEmpty());
    assertTrue(!hm.isEmpty());
  }
  /** java.util.ArrayMap#keySet() */
  @Test
  public void test_keySet() {
    // Test for method java.util.Set java.util.ArrayMap.keySet()
    Set s = hm.keySet();
    assertTrue(s.size() == hm.size());
    for (int i = 0; i < objArray.length; i++) assertTrue(s.contains(objArray[i].toString()));
    ArrayMap m = new ArrayMap();
    m.put(null, "test");
    assertTrue(m.keySet().contains(null));
    assertNull(m.keySet().iterator().next());
    Map map = new ArrayMap(101);
    map.put(new Integer(1), "1");
    map.put(new Integer(102), "102");
    map.put(new Integer(203), "203");
    Iterator it = map.keySet().iterator();
    Integer remove1 = (Integer) it.next();
    assertTrue(it.hasNext());
    it.remove();
    Integer remove2 = (Integer) it.next();
    it.remove();
    ArrayList list =
        new ArrayList(
            Arrays.asList(new Integer[] {new Integer(1), new Integer(102), new Integer(203)}));
    list.remove(remove1);
    list.remove(remove2);
    assertEquals(
        1, list.size(), "what list? " + list + " remove1=" + remove1 + " remove2=" + remove2);
    assertEquals(it.next(), list.get(0));
    assertEquals(1, map.size());
    assertTrue(map.keySet().iterator().next().equals(list.get(0)));
    Map map2 = new ArrayMap(101);
    map2.put(new Integer(1), "1");
    map2.put(new Integer(4), "4");
    Iterator it2 = map2.keySet().iterator();
    Integer remove3 = (Integer) it2.next();
    Integer next;
    if (remove3.intValue() == 1) next = new Integer(4);
    else next = new Integer(1);
    assertTrue(it2.hasNext());
    it2.remove();
    assertTrue(it2.next().equals(next));
    assertEquals(1, map2.size());
    assertTrue(map2.keySet().iterator().next().equals(next));
  }
  /** java.util.ArrayMap#put(java.lang.Object, java.lang.Object) */
  @Test
  public void test_putLjava_lang_ObjectLjava_lang_Object() {
    hm.put("KEY", "VALUE");
    assertEquals("VALUE", hm.get("KEY"));
    ArrayMap<Object, Object> m = new ArrayMap<Object, Object>();
    m.put(Short.valueOf((short) 0), "short");
    m.put(null, "test");
    m.put(new Integer(0), "int");
    assertEquals("short", m.get(Short.valueOf((short) 0)));
    assertEquals("int", m.get(new Integer(0)));
    // Check my actual key instance is returned
    ArrayMap<Integer, String> map = new ArrayMap<Integer, String>();
    for (int i = -67; i < 68; i++) {
      map.put(i, "foobar");
    }
    Integer myKey = new Integer(0);
    // Put a new value at the old key position
    map.put(myKey, "myValue");
    assertTrue(map.containsKey(myKey));
    assertEquals("myValue", map.get(myKey));
    boolean found = false;
    for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext(); ) {
      Integer key = itr.next();
      if (found = key == myKey) {
        break;
      }
    }
    assertFalse(found);
    // Add a new key instance and check it is returned
    assertNotNull(map.remove(myKey));
    map.put(myKey, "myValue");
    assertTrue(map.containsKey(myKey));
    assertEquals("myValue", map.get(myKey));
    for (Iterator<Integer> itr = map.keySet().iterator(); itr.hasNext(); ) {
      Integer key = itr.next();
      if (found = key == myKey) {
        break;
      }
    }
    assertTrue(found);
    // Ensure keys with identical hashcode are stored separately
    ArrayMap<Object, Object> objmap = new ArrayMap<Object, Object>();
    for (int i = 0; i < 68; i++) {
      objmap.put(i, "foobar");
    }
    // Put non-equal object with same hashcode
    MyKey aKey = new MyKey();
    assertNull(objmap.put(aKey, "value"));
    assertNull(objmap.remove(new MyKey()));
    assertEquals("foobar", objmap.get(0));
    assertEquals("value", objmap.get(aKey));
  }

  static class MyKey {
    public MyKey() {
      super();
    }

    @Override
    public int hashCode(@GuardSatisfied MyKey this) {
      return 0;
    }
  }
  /** java.util.ArrayMap#putAll(java.util.Map) */
  @Test
  public void test_putAllLjava_util_Map() {
    // Test for method void java.util.ArrayMap.putAll(java.util.Map)
    ArrayMap hm2 = new ArrayMap();
    hm2.putAll(hm);
    for (int i = 0; i < 10; i++)
      assertTrue(hm2.get(new Integer(i).toString()).equals((new Integer(i))));
    Map mockMap = new MockMap();
    hm2 = new ArrayMap();
    hm2.putAll(mockMap);
    assertEquals(0, hm2.size());
  }
  /** java.util.ArrayMap#putAll(java.util.Map) */
  @Test
  public void test_putAllLjava_util_Map_Null() {
    ArrayMap hashMap = new ArrayMap();
    try {
      hashMap.putAll(new MockMapNull());
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected.
    }
    try {
      hashMap = new ArrayMap(new MockMapNull());
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected.
    }
  }

  @Test
  public void test_putAllLjava_util_Map_Resize() {
    Random rnd = new Random(666);
    Map<Integer, Integer> m1 = new ArrayMap<Integer, Integer>();
    int MID = 10;
    for (int i = 0; i < MID; i++) {
      Integer j = rnd.nextInt();
      m1.put(j, j);
    }
    Map<Integer, Integer> m2 = new ArrayMap<Integer, Integer>();
    int HI = 30;
    for (int i = MID; i < HI; i++) {
      Integer j = rnd.nextInt();
      m2.put(j, j);
    }
    m1.putAll(m2);
    rnd = new Random(666);
    for (int i = 0; i < HI; i++) {
      Integer j = rnd.nextInt();
      assertEquals(j, m1.get(j));
    }
  }
  /** java.util.ArrayMap#remove(java.lang.Object) */
  @Test
  public void test_removeLjava_lang_Object() {
    int size = hm.size();
    Integer y = new Integer(9);
    Integer x = ((Integer) hm.remove(y.toString()));
    assertTrue(x.equals(new Integer(9)));
    assertNull(hm.get(new Integer(9)));
    assertTrue(hm.size() == (size - 1));
    assertNull(hm.remove("LCLCLC"));
    ArrayMap m = new ArrayMap();
    m.put(null, "test");
    assertNull(m.remove(new Integer(0)));
    assertEquals("test", m.remove(null));
    ArrayMap<Integer, Object> map = new ArrayMap<Integer, Object>();
    for (int i = 0; i < 68; i++) {
      map.put(i, "const");
    }
    Object[] values = new Object[68];
    for (int i = 0; i < 68; i++) {
      values[i] = new Object();
      map.put(i, values[i]);
    }
    for (int i = 67; i >= 0; i--) {
      assertEquals(values[i], map.remove(i));
    }
    // Ensure keys with identical hashcode are removed properly
    map = new ArrayMap<Integer, Object>();
    for (int i = -67; i < 68; i++) {
      map.put(i, "foobar");
    }
    // Remove non equal object with same hashcode
    assertNull(map.remove(new MyKey()));
    assertEquals("foobar", map.get(0));
    map.remove(0);
    assertNull(map.get(0));
  }
  /** java.util.ArrayMap#size() */
  @Test
  public void test_size() {
    // Test for method int java.util.ArrayMap.size()
    assertTrue(hm.size() == (objArray.length + 2));
  }
  /** java.util.ArrayMap#values() */
  @Test
  public void test_values() {
    // Test for method java.util.Collection java.util.ArrayMap.values()
    Collection c = hm.values();
    assertTrue(c.size() == hm.size());
    for (int i = 0; i < objArray.length; i++) assertTrue(c.contains(objArray[i]));
    ArrayMap myArrayMap = new ArrayMap();
    for (int i = 0; i < 100; i++) myArrayMap.put(objArray2[i], objArray[i]);
    Collection values = myArrayMap.values();
    values.remove(new Integer(0));
    assertTrue(!myArrayMap.containsValue(new Integer(0)));
  }
  /** java.util.AbstractMap#toString() */
  @Test
  public void test_toString() {
    ArrayMap m = new ArrayMap();
    m.put(m, m);
    String result = m.toString();
    assertTrue(result.indexOf("(this") > -1);
  }

  static class ReusableKey {
    private int key = 0;

    public void setKey(int key) {
      this.key = key;
    }

    @Override
    public int hashCode(@GuardSatisfied ReusableKey this) {
      return key;
    }

    @Override
    public boolean equals(@GuardSatisfied ReusableKey this, @GuardSatisfied Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof ReusableKey)) {
        return false;
      }
      return key == ((ReusableKey) o).key;
    }
  }

  class MockClonable implements Cloneable {
    public int i;

    public MockClonable(int i) {
      this.i = i;
    }

    @SuppressWarnings("allcheckers:purity.not.sideeffectfree.call")
    @Override
    protected Object clone(@GuardSatisfied MockClonable this) throws CloneNotSupportedException {
      return new MockClonable(i);
    }
  }
  /*
   * Regression test for HY-4750
   */
  @Test
  public void test_EntrySet() {
    ArrayMap map = new ArrayMap();
    map.put(new Integer(1), "ONE");
    Set entrySet = map.entrySet();
    Iterator e = entrySet.iterator();
    Object real = e.next();
    Map.Entry copyEntry = new MockEntry();
    assertEquals(real, copyEntry);
    assertTrue(entrySet.contains(copyEntry));
    entrySet.remove(copyEntry);
    assertFalse(entrySet.contains(copyEntry));
  }

  private static class MockEntry implements Map.Entry {
    @SuppressWarnings("allcheckers:purity.not.deterministic.object.creation")
    @Override
    public Object getKey(@GuardSatisfied MockEntry this) {
      return new Integer(1);
    }

    @Override
    public Object getValue(@GuardSatisfied MockEntry this) {
      return "ONE";
    }

    @Override
    public Object setValue(@GuardSatisfied MockEntry this, Object object) {
      return null;
    }
  }
  /**
   * Sets up the fixture, for example, open a network connection. This method is called before a
   * test is executed.
   */
  @BeforeEach
  protected void setUp() {
    objArray = new Object[hmSize];
    objArray2 = new Object[hmSize];
    for (int i = 0; i < objArray.length; i++) {
      objArray[i] = new Integer(i);
      objArray2[i] = objArray[i].toString();
    }
    hm = new ArrayMap();
    for (int i = 0; i < objArray.length; i++) {
      hm.put(objArray2[i], objArray[i]);
    }
    hm.put("test", null);
    hm.put(null, "test");
  }

  @AfterEach
  protected void tearDown() {
    hm = null;
    objArray = null;
    objArray2 = null;
  }

  static class SubMap<K extends @GuardedBy Object, V extends @GuardedBy Object>
      extends ArrayMap<K, V> {
    public SubMap(Map<? extends K, ? extends V> m) {
      super(m);
    }

    @SuppressWarnings({
      "lock", // ArrayMap is not yet annotated for the Lock Checker
    })
    @Override
    public V put(@GuardSatisfied SubMap<K, V> this, K key, V value) {
      throw new UnsupportedOperationException();
    }
  }
}
