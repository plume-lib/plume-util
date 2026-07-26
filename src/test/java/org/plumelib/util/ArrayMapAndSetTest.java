package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

/** Tests of {@link ArrayMap} and {@link ArraySet} behaviors that the adapted JDK suites omit. */
final class ArrayMapAndSetTest {

  ArrayMapAndSetTest() {}

  /**
   * Returns a new ArrayMap with two mappings.
   *
   * @return a new ArrayMap with two mappings
   */
  private static ArrayMap<String, String> twoElementMap() {
    ArrayMap<String, String> result = new ArrayMap<>();
    result.put("a", "1");
    result.put("b", "2");
    return result;
  }

  @Test
  void hashCodeMatchesOtherMapImplementations() {
    ArrayMap<String, String> am = twoElementMap();
    assertEquals(new HashMap<>(am).hashCode(), am.hashCode());
    assertEquals(new LinkedHashMap<>(am).hashCode(), am.hashCode());

    // Per the specification of Map.Entry.hashCode(), an entry's hash code is
    // (key.hashCode() ^ value.hashCode()).
    for (Map.Entry<String, String> entry : am.entrySet()) {
      assertEquals(entry.getKey().hashCode() ^ entry.getValue().hashCode(), entry.hashCode());
    }
  }

  @Test
  void toArrayOnCollectionWithNoRepresentation() {
    // A capacity of 0 means that the internal arrays are not allocated.
    ArrayMap<String, String> am = new ArrayMap<>(0);
    assertArrayEquals(new Object[0], am.keySet().toArray());
    assertArrayEquals(new Object[0], am.values().toArray());
    assertArrayEquals(new Object[0], am.entrySet().toArray());
    assertArrayEquals(new String[0], am.keySet().toArray(new String[0]));
    assertArrayEquals(new String[0], am.values().toArray(new String[0]));

    ArraySet<String> as = new ArraySet<>(0);
    assertArrayEquals(new Object[0], as.toArray());
    assertArrayEquals(new String[0], as.toArray(new String[0]));
  }

  @SuppressWarnings(
      "nullness:toarray.nullable.elements.not.newarray" // passing an existing oversized array,
  // rather than a freshly-constructed one, is the point of this test
  )
  @Test
  void toArrayNullTerminatesOversizedArray() {
    ArrayMap<String, String> am = twoElementMap();

    // Per the specification of Collection.toArray(T[]), if the given array is longer than the
    // collection, the element just past the end is set to null.
    @Nullable String[] keys = new @Nullable String[5];
    Arrays.fill(keys, "junk");
    @Nullable String[] keysResult = am.keySet().toArray(keys);
    assertSame(keys, keysResult);
    assertArrayEquals(new @Nullable String[] {"a", "b", null, "junk", "junk"}, keysResult);

    @Nullable String[] values = new @Nullable String[5];
    Arrays.fill(values, "junk");
    @Nullable String[] valuesResult = am.values().toArray(values);
    assertSame(values, valuesResult);
    assertArrayEquals(new @Nullable String[] {"1", "2", null, "junk", "junk"}, valuesResult);

    // An exactly-sized array is filled but not null-terminated, because there is no room.
    @Nullable String[] exact = new @Nullable String[2];
    assertArrayEquals(new String[] {"a", "b"}, am.keySet().toArray(exact));

    // An undersized array causes a new array to be allocated.
    @Nullable String[] undersized = new @Nullable String[1];
    @Nullable String[] undersizedResult = am.keySet().toArray(undersized);
    assertNotSame(undersized, undersizedResult);
    assertArrayEquals(new String[] {"a", "b"}, undersizedResult);

    // The entry set inherits toArray(T[]) from AbstractCollection, which null-terminates too.
    @Nullable Object[] entries = new @Nullable Object[5];
    Arrays.fill(entries, "junk");
    @Nullable Object[] entriesResult = am.entrySet().toArray(entries);
    assertSame(entries, entriesResult);
    assertArrayEquals(
        new @Nullable Object[] {Map.entry("a", "1"), Map.entry("b", "2"), null, "junk", "junk"},
        entriesResult);
  }

  // These tests compare against the whole of repr(), rather than testing that repr() does not
  // contain the removed element.  A containment test would pass vacuously if repr() were ever
  // changed to print only the first `size` slots rather than the entire arrays.

  @Test
  void removeAndClearDoNotRetainReferences() {
    ArrayMap<String, String> am = new ArrayMap<>();
    am.put("keyA", "valA");
    am.put("keyB", "valB");
    am.put("keyC", "valC");
    assertEquals("size=3 capacity=4 [keyA, keyB, keyC, null] [valA, valB, valC, null]", am.repr());

    am.remove("keyB");
    assertEquals(new HashSet<>(Set.of("keyA", "keyC")), new HashSet<>(am.keySet()));
    // The vacated slot is cleared, so it retains neither the removed key nor the removed value.
    assertEquals("size=2 capacity=4 [keyA, keyC, null, null] [valA, valC, null, null]", am.repr());

    am.clear();
    assertTrue(am.isEmpty());
    assertEquals("size=0 capacity=4 [null, null, null, null] [null, null, null, null]", am.repr());

    ArraySet<String> as = new ArraySet<>();
    as.add("eltX");
    as.add("eltY");
    assertEquals("size=2 capacity=4 [eltX, eltY, null, null]", as.repr());

    as.remove("eltY");
    assertEquals("size=1 capacity=4 [eltX, null, null, null]", as.repr());

    as.clear();
    assertTrue(as.isEmpty());
    assertEquals("size=0 capacity=4 [null, null, null, null]", as.repr());
  }

  @Test
  void removeThroughViewsDoesNotRetainReferences() {
    ArrayMap<String, String> am = new ArrayMap<>();
    am.put("keyA", "valA");
    am.put("keyB", "valB");
    am.put("keyC", "valC");

    // Removal through the key set clears the vacated slot.
    assertTrue(am.keySet().remove("keyA"));
    assertEquals("size=2 capacity=4 [keyB, keyC, null, null] [valB, valC, null, null]", am.repr());

    // Removal through the entry set clears the vacated slot.
    assertTrue(am.entrySet().remove(Map.entry("keyB", "valB")));
    assertEquals("size=1 capacity=4 [keyC, null, null, null] [valC, null, null, null]", am.repr());

    // Removal through a view's iterator clears the vacated slot.
    Iterator<String> valueIterator = am.values().iterator();
    valueIterator.next();
    valueIterator.remove();
    assertEquals("size=0 capacity=4 [null, null, null, null] [null, null, null, null]", am.repr());

    ArraySet<String> as = new ArraySet<>();
    as.add("eltX");
    as.add("eltY");
    Iterator<String> setIterator = as.iterator();
    setIterator.next();
    setIterator.remove();
    assertEquals("size=1 capacity=4 [eltY, null, null, null]", as.repr());
  }

  @Test
  void addToCollectionWithSpareCapacityDoesNotGrow() {
    // Adding the first element to a collection that was allocated with spare capacity must not
    // reallocate the representation.
    ArraySet<String> as = new ArraySet<>(4);
    as.add("x");
    assertEquals("size=1 capacity=4 [x, null, null, null]", as.repr());

    ArrayMap<String, String> am = new ArrayMap<>(4);
    am.put("k", "v");
    assertEquals("size=1 capacity=4 [k, null, null, null] [v, null, null, null]", am.repr());
  }

  @Test
  void addToCloneOfEmptySet() {
    // clone() of an empty-but-allocated ArraySet produces a zero-length representation.  Growing
    // it must allocate a nonempty array rather than doubling zero.
    ArraySet<String> as = new ArraySet<>(4);
    ArraySet<String> clone = as.clone();
    assertTrue(clone.add("x"));
    assertEquals(new HashSet<>(Set.of("x")), new HashSet<>(clone));
    assertTrue(as.isEmpty());

    // Adding to a clone of a nonempty set does not disturb the original.
    ArraySet<String> nonEmpty = new ArraySet<>();
    nonEmpty.add("a");
    ArraySet<String> nonEmptyClone = nonEmpty.clone();
    nonEmptyClone.add("b");
    assertEquals(new HashSet<>(Set.of("a")), new HashSet<>(nonEmpty));
    assertEquals(new HashSet<>(Set.of("a", "b")), new HashSet<>(nonEmptyClone));
  }

  @Test
  void putToCloneOfEmptyMap() {
    // As for ArraySet, clone() of an empty-but-allocated ArrayMap produces a zero-length
    // representation.  ArrayMap.grow() tests the capacity against zero, whereas ArraySet.grow()
    // tests the array length; both must handle a zero-length array rather than doubling zero.
    ArrayMap<String, String> am = new ArrayMap<>(4);
    ArrayMap<String, String> clone = am.clone();
    assertNull(clone.put("x", "1"));
    assertEquals(Map.of("x", "1"), clone);
    assertTrue(am.isEmpty());

    // Adding to a clone of a nonempty map does not disturb the original.
    ArrayMap<String, String> nonEmpty = new ArrayMap<>();
    nonEmpty.put("a", "1");
    ArrayMap<String, String> nonEmptyClone = nonEmpty.clone();
    nonEmptyClone.put("b", "2");
    assertEquals(Map.of("a", "1"), nonEmpty);
    assertEquals(Map.of("a", "1", "b", "2"), nonEmptyClone);
  }
}
