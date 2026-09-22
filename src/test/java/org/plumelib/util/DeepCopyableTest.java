package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.junit.jupiter.api.Test;

/**
 * Test {@link DeepCopyable} and the deep-copy methods of {@link CollectionsP}, {@link MapsP},
 * {@link IPair}, and {@link MPair}.
 */
final class DeepCopyableTest {

  DeepCopyableTest() {}

  /** A mutable holder of an integer, for testing that copies are independent of the original. */
  private static final class Box implements DeepCopyable<Box> {

    /** The value held by this box. */
    int value;

    /**
     * Creates a Box.
     *
     * @param value the value held by the new box
     */
    Box(int value) {
      this.value = value;
    }

    @Override
    public Box deepCopy() {
      return new Box(value);
    }

    @Override
    @Pure
    @SuppressWarnings("lock:instanceof.pattern.unsafe") // other is @GuardSatisfied
    public boolean equals(@GuardSatisfied Box this, @GuardSatisfied @Nullable Object other) {
      return other instanceof Box otherBox && otherBox.value == this.value;
    }

    @Pure
    @Override
    public int hashCode(@GuardSatisfied Box this) {
      return Integer.hashCode(value);
    }

    @SideEffectFree
    @Override
    public String toString(@GuardSatisfied Box this) {
      return "Box(" + value + ")";
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // DeepCopyable.deepCopyOrNull
  //

  @Test
  void deepCopyOrNullOfNullIsNull() {
    assertNull(DeepCopyable.deepCopyOrNull((@Nullable Box) null));
  }

  @Test
  void deepCopyOrNullMakesAnIndependentCopy() {
    Box orig = new Box(1);
    Box copy = DeepCopyable.deepCopyOrNull(orig);
    assertEquals(orig, copy);
    assertNotSame(orig, copy);
    orig.value = 2;
    assertEquals(new Box(1), copy);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // CollectionsP.deepCopy
  //

  @Test
  void collectionsDeepCopyOfNullIsNull() {
    assertNull(CollectionsP.deepCopy((@Nullable List<Box>) null));
  }

  @Test
  void collectionsDeepCopyCopiesElements() {
    List<Box> orig = new ArrayList<>(List.of(new Box(1), new Box(2)));
    List<Box> copy = CollectionsP.deepCopy(orig);
    assertEquals(orig, copy);
    assertNotSame(orig, copy);
    assertEquals(ArrayList.class, copy.getClass());
    for (int i = 0; i < orig.size(); i++) {
      assertNotSame(orig.get(i), copy.get(i));
    }

    // Mutating an element of the original does not affect the copy.
    orig.get(0).value = 100;
    assertEquals(List.of(new Box(1), new Box(2)), copy);

    // Adding to the original does not affect the copy.
    orig.add(new Box(3));
    assertEquals(2, copy.size());
  }

  @Test
  void collectionsDeepCopyOfEmptyCollection() {
    List<Box> orig = new ArrayList<>();
    List<Box> copy = CollectionsP.deepCopy(orig);
    assertEquals(List.of(), copy);
    assertNotSame(orig, copy);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // MapsP.deepCopy and MapsP.deepCopyValues
  //

  @Test
  void mapsDeepCopyOfNullIsNull() {
    assertNull(MapsP.deepCopy((@Nullable Map<Box, Box>) null));
    assertNull(MapsP.deepCopyValues((@Nullable Map<Box, Box>) null));
  }

  @Test
  void mapsDeepCopyCopiesKeysAndValues() {
    LinkedHashMap<Box, Box> orig = new LinkedHashMap<>();
    Box key = new Box(1);
    Box value = new Box(2);
    orig.put(key, value);

    LinkedHashMap<Box, Box> copy = MapsP.deepCopy(orig);
    assertEquals(orig, copy);
    assertNotSame(orig, copy);

    Map.Entry<Box, Box> copiedEntry = copy.entrySet().iterator().next();
    assertNotSame(key, copiedEntry.getKey());
    assertNotSame(value, copiedEntry.getValue());

    // Mutating the original's value does not affect the copy.
    value.value = 100;
    assertEquals(new Box(2), copy.get(new Box(1)));
  }

  @Test
  void mapsDeepCopyValuesSharesKeys() {
    LinkedHashMap<Box, Box> orig = new LinkedHashMap<>();
    Box key = new Box(1);
    Box value = new Box(2);
    orig.put(key, value);

    LinkedHashMap<Box, Box> copy = MapsP.deepCopyValues(orig);
    assertEquals(orig, copy);
    assertNotSame(orig, copy);

    Map.Entry<Box, Box> copiedEntry = copy.entrySet().iterator().next();
    // The keys are the same objects, but the values are copies.
    assertSame(key, copiedEntry.getKey());
    assertNotSame(value, copiedEntry.getValue());
  }

  @Test
  void mapsDeepCopyOfEmptyMap() {
    LinkedHashMap<Box, Box> orig = new LinkedHashMap<>();
    assertEquals(Map.of(), MapsP.deepCopy(orig));
    assertEquals(Map.of(), MapsP.deepCopyValues(orig));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // IPair and MPair
  //

  @Test
  void ipairDeepCopy() {
    Box first = new Box(1);
    Box second = new Box(2);
    IPair<Box, Box> orig = IPair.of(first, second);

    IPair<Box, Box> copy = IPair.deepCopy(orig);
    assertEquals(orig, copy);
    assertNotSame(first, copy.first);
    assertNotSame(second, copy.second);

    IPair<Box, Box> copyFirst = IPair.deepCopyFirst(orig);
    assertEquals(orig, copyFirst);
    assertNotSame(first, copyFirst.first);
    assertSame(second, copyFirst.second);

    IPair<Box, Box> copySecond = IPair.deepCopySecond(orig);
    assertEquals(orig, copySecond);
    assertSame(first, copySecond.first);
    assertNotSame(second, copySecond.second);
  }

  @Test
  void mpairDeepCopy() {
    Box first = new Box(1);
    Box second = new Box(2);
    MPair<Box, Box> orig = MPair.of(first, second);

    MPair<Box, Box> copy = MPair.deepCopy(orig);
    assertEquals(orig, copy);
    assertNotSame(first, copy.first);
    assertNotSame(second, copy.second);

    MPair<Box, Box> copyFirst = MPair.deepCopyFirst(orig);
    assertEquals(orig, copyFirst);
    assertNotSame(first, copyFirst.first);
    assertSame(second, copyFirst.second);

    MPair<Box, Box> copySecond = MPair.deepCopySecond(orig);
    assertEquals(orig, copySecond);
    assertSame(first, copySecond.first);
    assertNotSame(second, copySecond.second);

    // The copy is independent of the original.
    first.value = 100;
    assertEquals(new Box(1), copy.first);
  }
}
