package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

  @Test
  void removeAndClearDoNotRetainReferences() {
    // Use distinctive strings, because repr() also contains text such as "size=2 capacity=4".
    ArrayMap<String, String> am = new ArrayMap<>();
    am.put("keyA", "valA");
    am.put("keyB", "valB");
    am.put("keyC", "valC");

    assertTrue(am.repr().contains("keyB"));
    assertTrue(am.repr().contains("valB"));
    am.remove("keyB");
    assertEquals(new HashSet<>(Set.of("keyA", "keyC")), new HashSet<>(am.keySet()));
    // The vacated slot is cleared, so it retains neither the removed key nor the removed value.
    assertFalse(am.repr().contains("keyB"));
    assertFalse(am.repr().contains("valB"));

    am.clear();
    assertTrue(am.isEmpty());
    assertFalse(am.repr().contains("keyA"));
    assertFalse(am.repr().contains("valA"));
    assertFalse(am.repr().contains("keyC"));
    assertFalse(am.repr().contains("valC"));

    ArraySet<String> as = new ArraySet<>();
    as.add("eltX");
    as.add("eltY");
    assertTrue(as.repr().contains("eltY"));
    as.remove("eltY");
    assertFalse(as.repr().contains("eltY"));
    as.clear();
    assertTrue(as.isEmpty());
    assertFalse(as.repr().contains("eltX"));
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
}
