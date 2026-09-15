package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/** Test the IdentityArraySet class. */
// PMD suggests method references such as `itor::next`, but the Lock Checker rejects a
// method reference whose receiver must be @GuardSatisfied.
@SuppressWarnings("PMD.LambdaCanBeMethodReference")
final class IdentityArraySetTest {

  IdentityArraySetTest() {}

  // ///////////////////////////////////////////////////////////////////////////
  // Helper functions
  //

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

  /**
   * Returns the elements of the given iterable, in iteration order.
   *
   * @param <T> the type of the elements
   * @param iterable an iterable
   * @return the elements of {@code iterable}, in iteration order
   */
  private static <T> List<T> elements(Iterable<T> iterable) {
    List<T> result = new ArrayList<>();
    for (T elt : iterable) {
      result.add(elt);
    }
    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Constructors
  //

  @Test
  void constructorRejectsNegativeCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new IdentityArraySet<>(-1));
  }

  @Test
  void zeroCapacitySetGrowsOnDemand() {
    IdentityArraySet<String> s = new IdentityArraySet<>(0);
    assertTrue(s.isEmpty());
    assertEquals("size=0 capacity=0 null", s.repr());
    assertTrue(s.add("a"));
    assertEquals(1, s.size());
    assertEquals("size=1 capacity=4 [a, null, null, null]", s.repr());
  }

  @Test
  void addToSetWithSpareCapacityDoesNotGrow() {
    IdentityArraySet<String> s = new IdentityArraySet<>(4);
    s.add("x");
    assertEquals("size=1 capacity=4 [x, null, null, null]", s.repr());
  }

  @Test
  void capacityDoublesWhenFull() {
    IdentityArraySet<String> s = new IdentityArraySet<>(1);
    s.add("a");
    assertEquals("size=1 capacity=1 [a]", s.repr());
    s.add("b");
    assertEquals(2, s.size());
    assertEquals("size=2 capacity=2 [a, b]", s.repr());
    s.add("c");
    assertEquals("size=3 capacity=4 [a, b, c, null]", s.repr());
  }

  @Test
  void collectionConstructorUsesIdentity() {
    List<String> a1 = newList("a");
    List<String> a2 = newList("a");
    IdentityArraySet<List<String>> s = new IdentityArraySet<>(Arrays.asList(a1, a2, a1));
    // a1 appears twice in the argument but only once in the set; a2 is a separate element.
    assertEquals(2, s.size());
    assertTrue(s.contains(a1));
    assertTrue(s.contains(a2));
    assertEquals(Arrays.asList(a1, a2), elements(s));
  }

  @Test
  void collectionConstructorOnEmptyCollection() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Collections.emptyList());
    assertTrue(s.isEmpty());
    assertEquals(0, s.size());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Element comparison
  //

  @Test
  void addContainsAndRemoveUseIdentity() {
    List<String> a1 = newList("a");
    List<String> a2 = newList("a");
    assertEquals(a1, a2);

    IdentityArraySet<List<String>> s = new IdentityArraySet<>();
    assertTrue(s.add(a1));
    assertFalse(s.add(a1));
    assertTrue(s.contains(a1));
    assertFalse(s.contains(a2));

    // An equal but non-identical element is a new element.
    assertTrue(s.add(a2));
    assertEquals(2, s.size());

    // Removal is by identity too.
    assertFalse(s.remove(newList("a")));
    assertEquals(2, s.size());
    assertTrue(s.remove(a1));
    assertEquals(1, s.size());
    assertFalse(s.contains(a1));
    assertTrue(s.contains(a2));
    assertFalse(s.remove(a1));
  }

  @Test
  void containsAndRemoveOfAbsentElements() {
    IdentityArraySet<String> s = new IdentityArraySet<>();
    // An empty set has a null representation, which contains() must handle.
    assertFalse(s.contains("a"));
    assertFalse(s.remove("a"));
    assertFalse(s.contains(null));

    s.add("a");
    assertFalse(s.contains("b"));
    assertFalse(s.contains(null));
    assertFalse(s.remove("b"));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Bulk operations
  //

  @Test
  void addAllAndRemoveAll() {
    IdentityArraySet<String> s = new IdentityArraySet<>();
    assertFalse(s.addAll(Collections.emptyList()));
    assertTrue(s.isEmpty());

    assertTrue(s.addAll(Arrays.asList("a", "b", "c")));
    assertEquals(3, s.size());
    assertFalse(s.addAll(Arrays.asList("a", "b")));
    assertEquals(3, s.size());

    assertFalse(s.removeAll(Collections.emptyList()));
    assertEquals(3, s.size());
    assertTrue(s.removeAll(Arrays.asList("a", "c")));
    assertEquals(Collections.singletonList("b"), elements(s));
    assertFalse(s.removeAll(Arrays.asList("a", "c")));
  }

  @Test
  void retainAll() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b", "c"));
    assertFalse(s.retainAll(Arrays.asList("a", "b", "c", "d")));
    assertEquals(3, s.size());
    assertTrue(s.retainAll(Arrays.asList("a", "c")));
    assertEquals(Arrays.asList("a", "c"), elements(s));
  }

  @Test
  void clearEmptiesTheSet() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b"));
    s.clear();
    assertTrue(s.isEmpty());
    assertEquals(0, s.size());
    assertFalse(s.contains("a"));
    assertFalse(s.iterator().hasNext());

    // The set can be used again after being cleared.
    assertTrue(s.add("c"));
    assertEquals(Collections.singletonList("c"), elements(s));
  }

  // These tests compare against the whole of repr(), rather than testing that repr() does not
  // contain the removed element.  A containment test would pass vacuously if repr() were ever
  // changed to print only the first `size` slots rather than the entire array.

  @Test
  void removeAndClearDoNotRetainReferences() {
    IdentityArraySet<String> s = new IdentityArraySet<>(4);
    s.add("a");
    s.add("b");
    s.add("c");
    assertEquals("size=3 capacity=4 [a, b, c, null]", s.repr());

    // Removing an interior element shifts the later elements down and clears the vacated slot.
    assertTrue(s.remove("b"));
    assertEquals("size=2 capacity=4 [a, c, null, null]", s.repr());

    // Removing the last element clears its slot.
    assertTrue(s.remove("c"));
    assertEquals("size=1 capacity=4 [a, null, null, null]", s.repr());

    s.add("d");
    s.add("e");
    assertEquals("size=3 capacity=4 [a, d, e, null]", s.repr());
    s.clear();
    assertEquals("size=0 capacity=4 [null, null, null, null]", s.repr());
  }

  @Test
  void removeThroughIteratorDoesNotRetainReferences() {
    IdentityArraySet<String> s = new IdentityArraySet<>(4);
    s.addAll(Arrays.asList("a", "b", "c"));
    Iterator<String> itor = s.iterator();
    itor.next();
    itor.remove();
    assertEquals("size=2 capacity=4 [b, c, null, null]", s.repr());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Iteration
  //

  @Test
  void iteratorTraversesInInsertionOrder() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b", "c"));
    Iterator<String> itor = s.iterator();
    assertTrue(itor.hasNext());
    assertEquals("a", itor.next());
    assertEquals("b", itor.next());
    assertEquals("c", itor.next());
    assertFalse(itor.hasNext());
    assertThrows(NoSuchElementException.class, () -> itor.next());
  }

  @Test
  void iteratorRemove() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b", "c"));
    Iterator<String> itor = s.iterator();
    assertEquals("a", itor.next());
    itor.remove();
    assertEquals(2, s.size());
    assertFalse(s.contains("a"));
    // Iteration continues with the element after the removed one.
    assertEquals("b", itor.next());
    assertEquals("c", itor.next());
    assertFalse(itor.hasNext());
  }

  @Test
  void iteratorRemoveWithoutNextThrows() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b"));
    Iterator<String> itor = s.iterator();
    assertThrows(IllegalStateException.class, () -> itor.remove());
    itor.next();
    itor.remove();
    // A second remove() without an intervening next() is also illegal.
    assertThrows(IllegalStateException.class, () -> itor.remove());
  }

  @Test
  void iteratorRemoveDetectsConcurrentModification() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b"));
    Iterator<String> itor = s.iterator();
    itor.next();
    s.add("c");
    assertThrows(ConcurrentModificationException.class, () -> itor.remove());
  }

  @Test
  void forEachVisitsAllElements() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b", "c"));
    List<String> visited = new ArrayList<>();
    s.forEach(visited::add);
    assertEquals(Arrays.asList("a", "b", "c"), visited);

    // forEach on an empty set, whose representation is null, visits nothing.
    List<String> visitedEmpty = new ArrayList<>();
    new IdentityArraySet<String>().forEach(visitedEmpty::add);
    assertEquals(Collections.emptyList(), visitedEmpty);
  }

  @Test
  void forEachDetectsConcurrentModification() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b"));
    assertThrows(ConcurrentModificationException.class, () -> s.forEach(elt -> s.remove(elt)));
  }

  @SuppressWarnings("nullness:argument") // testing the behavior on a null argument
  @Test
  void forEachRejectsNullAction() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b"));
    assertThrows(NullPointerException.class, () -> s.forEach(null));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // clone
  //

  @Test
  void cloneIsIndependentOfTheOriginal() {
    IdentityArraySet<String> s = new IdentityArraySet<>(Arrays.asList("a", "b"));
    IdentityArraySet<String> clone = s.clone();
    assertNotSame(s, clone);
    assertEquals(Arrays.asList("a", "b"), elements(clone));

    clone.add("c");
    assertEquals(2, s.size());
    assertEquals(3, clone.size());

    s.remove("a");
    assertEquals(Collections.singletonList("b"), elements(s));
    assertEquals(Arrays.asList("a", "b", "c"), elements(clone));
  }

  @Test
  void addToCloneOfEmptySet() {
    // clone() of an empty-but-allocated IdentityArraySet produces a zero-length representation.
    // Growing it must allocate a nonempty array rather than doubling zero.
    IdentityArraySet<String> s = new IdentityArraySet<>(4);
    IdentityArraySet<String> clone = s.clone();
    assertTrue(clone.add("x"));
    assertEquals(Collections.singletonList("x"), elements(clone));
    assertTrue(s.isEmpty());

    // clone() of a never-allocated IdentityArraySet also produces a usable set.
    IdentityArraySet<String> unallocated = new IdentityArraySet<>(0);
    IdentityArraySet<String> unallocatedClone = unallocated.clone();
    assertTrue(unallocatedClone.add("y"));
    assertEquals(Collections.singletonList("y"), elements(unallocatedClone));
    assertTrue(unallocated.isEmpty());
  }
}
