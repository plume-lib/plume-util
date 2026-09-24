package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.checkerframework.checker.modifiability.qual.IteratorPolyMod;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.junit.jupiter.api.Test;

/**
 * Test {@link MostlySingletonSet} and {@link IdentityMostlySingletonSet}, including the behavior
 * they inherit from {@link AbstractMostlySingletonSet}.
 */
// PMD suggests method references such as `itor::next`, but the Lock Checker rejects a
// method reference whose receiver must be @GuardSatisfied.
@SuppressWarnings("PMD.LambdaCanBeMethodReference")
final class MostlySingletonSetTest {

  MostlySingletonSetTest() {}

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

  /**
   * Asserts that every operation that {@link AbstractMostlySingletonSet} does not support throws
   * {@code UnsupportedOperationException}.
   *
   * @param s the set to test
   */
  private static void assertUnsupportedOperationsThrow(
      @Modifiable @IteratorPolyMod AbstractMostlySingletonSet<String> s) {
    List<String> arg = Collections.singletonList("a");
    assertThrows(UnsupportedOperationException.class, () -> s.toArray());
    assertThrows(UnsupportedOperationException.class, () -> s.toArray(new String[0]));
    assertThrows(UnsupportedOperationException.class, () -> s.remove("a"));
    assertThrows(UnsupportedOperationException.class, () -> s.containsAll(arg));
    assertThrows(UnsupportedOperationException.class, () -> s.retainAll(arg));
    assertThrows(UnsupportedOperationException.class, () -> s.removeAll(arg));
    assertThrows(UnsupportedOperationException.class, () -> s.clear());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // The EMPTY state
  //

  @Test
  void emptySet() {
    MostlySingletonSet<String> s = new MostlySingletonSet<>();
    s.checkRep();
    assertEquals(0, s.size());
    assertTrue(s.isEmpty());
    assertFalse(s.contains("a"));
    assertFalse(s.iterator().hasNext());
    assertThrows(NoSuchElementException.class, () -> s.iterator().next());
    assertEquals("[]", s.toString());
    assertEquals(Collections.emptyList(), elements(s));
  }

  @Test
  void emptyIdentitySet() {
    IdentityMostlySingletonSet<String> s = new IdentityMostlySingletonSet<>();
    s.checkRep();
    assertEquals(0, s.size());
    assertTrue(s.isEmpty());
    assertFalse(s.contains("a"));
    assertFalse(s.iterator().hasNext());
    assertEquals("[]", s.toString());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // The SINGLETON state
  //

  @Test
  void singletonConstructor() {
    MostlySingletonSet<String> s = new MostlySingletonSet<>("a");
    s.checkRep();
    assertEquals(1, s.size());
    assertFalse(s.isEmpty());
    assertTrue(s.contains("a"));
    assertFalse(s.contains("b"));
    assertEquals("[a]", s.toString());
    assertEquals(Collections.singletonList("a"), elements(s));

    IdentityMostlySingletonSet<String> is = new IdentityMostlySingletonSet<>("a");
    is.checkRep();
    assertEquals(1, is.size());
    assertTrue(is.contains("a"));
    assertEquals("[a]", is.toString());
    assertEquals(Collections.singletonList("a"), elements(is));
  }

  @Test
  void singletonIterator() {
    MostlySingletonSet<String> s = new MostlySingletonSet<>("a");
    Iterator<String> itor = s.iterator();
    assertTrue(itor.hasNext());
    assertEquals("a", itor.next());
    assertFalse(itor.hasNext());
    assertThrows(NoSuchElementException.class, () -> itor.next());
  }

  @Test
  void singletonIteratorRemoveEmptiesTheSet() {
    MostlySingletonSet<String> s = new MostlySingletonSet<>("a");
    @Modifiable Iterator<String> itor = s.iterator();
    assertEquals("a", itor.next());
    itor.remove();
    s.checkRep();
    assertTrue(s.isEmpty());
    assertFalse(s.contains("a"));
    assertEquals("[]", s.toString());

    // The set can be used again after the removal.
    assertTrue(s.add("b"));
    s.checkRep();
    assertEquals(1, s.size());
    assertTrue(s.contains("b"));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Growing through all three states
  //

  @Test
  void addGrowsThroughAllStates() {
    @Modifiable MostlySingletonSet<String> s = new MostlySingletonSet<>();

    // EMPTY -> SINGLETON
    assertTrue(s.add("a"));
    s.checkRep();
    assertEquals(1, s.size());

    // SINGLETON, adding an element that is already present
    assertFalse(s.add("a"));
    s.checkRep();
    assertEquals(1, s.size());

    // SINGLETON -> ANY
    assertTrue(s.add("b"));
    s.checkRep();
    assertEquals(2, s.size());

    // ANY, adding an element that is already present
    assertFalse(s.add("a"));
    assertFalse(s.add("b"));
    assertEquals(2, s.size());

    // ANY -> larger ANY
    assertTrue(s.add("c"));
    s.checkRep();
    assertEquals(3, s.size());
    assertTrue(s.contains("a"));
    assertTrue(s.contains("b"));
    assertTrue(s.contains("c"));
    assertFalse(s.contains("d"));

    // The backing store is a LinkedHashSet, so iteration is in insertion order.
    assertEquals(Arrays.asList("a", "b", "c"), elements(s));
    assertEquals("[a, b, c]", s.toString());
  }

  @Test
  void addAllReturnsWhetherTheSetChanged() {
    @Modifiable @IteratorPolyMod MostlySingletonSet<String> s = new MostlySingletonSet<>();
    assertFalse(s.addAll(Collections.emptyList()));
    assertTrue(s.isEmpty());

    assertTrue(s.addAll(Arrays.asList("a", "b")));
    assertEquals(2, s.size());

    assertFalse(s.addAll(Arrays.asList("a", "b")));
    assertEquals(2, s.size());

    // True because of "c", even though "b" is already present.
    assertTrue(s.addAll(Arrays.asList("b", "c")));
    assertEquals(3, s.size());
  }

  @Test
  void iteratorRemoveInAnyState() {
    @Modifiable @IteratorPolyMod MostlySingletonSet<String> s = new MostlySingletonSet<>();
    s.addAll(Arrays.asList("a", "b", "c"));
    Iterator<String> itor = s.iterator();
    assertEquals("a", itor.next());
    itor.remove();
    assertEquals(2, s.size());
    assertFalse(s.contains("a"));
    s.checkRep();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Element comparison
  //

  @Test
  void mostlySingletonSetUsesEquals() {
    List<String> a1 = newList("a");
    List<String> a2 = newList("a");
    assertEquals(a1, a2);

    @Modifiable MostlySingletonSet<List<String>> s = new MostlySingletonSet<>();
    assertTrue(s.add(a1));
    // In the SINGLETON state, an equal element is a duplicate.
    assertTrue(s.contains(a2));
    assertFalse(s.add(a2));
    assertEquals(1, s.size());

    // In the ANY state, an equal element is still a duplicate.
    assertTrue(s.add(newList("b")));
    assertEquals(2, s.size());
    assertTrue(s.contains(a2));
    assertFalse(s.add(a2));
    assertEquals(2, s.size());
  }

  @Test
  void identityMostlySingletonSetUsesIdentity() {
    List<String> a1 = newList("a");
    List<String> a2 = newList("a");
    assertEquals(a1, a2);

    @Modifiable IdentityMostlySingletonSet<List<String>> s = new IdentityMostlySingletonSet<>();
    assertTrue(s.add(a1));
    // In the SINGLETON state, only the identical element is a duplicate.
    assertTrue(s.contains(a1));
    assertFalse(s.contains(a2));
    assertFalse(s.add(a1));
    assertEquals(1, s.size());

    // An equal but non-identical element is a new element.
    assertTrue(s.add(a2));
    assertEquals(2, s.size());

    // In the ANY state, only the identical element is a duplicate.
    assertTrue(s.contains(a1));
    assertTrue(s.contains(a2));
    assertFalse(s.contains(newList("a")));
    assertFalse(s.add(a1));
    assertFalse(s.add(a2));
    assertEquals(2, s.size());
    assertTrue(s.add(newList("a")));
    assertEquals(3, s.size());
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Unsupported operations
  //

  @Test
  @SuppressWarnings({"iterator:cast.unsafe.constructor.invocation", "modifiability:argument"})
  void unsupportedOperations() {
    assertUnsupportedOperationsThrow(new @IteratorPolyMod MostlySingletonSet<>());
    assertUnsupportedOperationsThrow(new @IteratorPolyMod MostlySingletonSet<>("a"));
    assertUnsupportedOperationsThrow(new @IteratorPolyMod IdentityMostlySingletonSet<>());
    assertUnsupportedOperationsThrow(new @IteratorPolyMod IdentityMostlySingletonSet<>("a"));

    @Modifiable @IteratorPolyMod MostlySingletonSet<String> any = new @IteratorPolyMod MostlySingletonSet<>();
    any.addAll(Arrays.asList("a", "b"));
    assertUnsupportedOperationsThrow(any);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Representation invariant
  //

  @Test
  void checkRepDetectsCorruptedRepresentation() {
    // EMPTY state with a value.
    MostlySingletonSet<String> emptyWithValue = new MostlySingletonSet<>();
    emptyWithValue.value = "a";
    assertThrows(IllegalStateException.class, () -> emptyWithValue.checkRep());

    // SINGLETON state with no value.
    MostlySingletonSet<String> singletonWithoutValue = new MostlySingletonSet<>("a");
    singletonWithoutValue.value = null;
    assertThrows(IllegalStateException.class, () -> singletonWithoutValue.checkRep());

    // ANY state with no set.
    MostlySingletonSet<String> anyWithoutSet = new MostlySingletonSet<>();
    anyWithoutSet.state = AbstractMostlySingletonSet.State.ANY;
    assertThrows(IllegalStateException.class, () -> anyWithoutSet.checkRep());
  }
}
