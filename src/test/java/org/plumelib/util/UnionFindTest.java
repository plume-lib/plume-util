package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/** Tests for the {@link UnionFind} class. */
final class UnionFindTest {

  UnionFindTest() {}

  /**
   * A unary predicate that holds of "big" integers. Used as {@code UnionFindTest::isBig}.
   *
   * @param n an integer
   * @return true if {@code n} is at least 100
   */
  private static boolean isBig(Integer n) {
    return n >= 100;
  }

  /**
   * An ordered binary predicate that holds when the second argument is one greater than the first.
   * Used as {@code UnionFindTest::successor}.
   *
   * @param x an integer
   * @param y an integer
   * @return true if {@code y == x + 1}
   */
  private static boolean successor(Integer x, Integer y) {
    return y == x + 1;
  }

  // ---------------------------------------------------------------------------
  // Basic add / find / union behavior.
  // ---------------------------------------------------------------------------

  @Test
  void testAddAndContains() {
    UnionFind<String> uf = new UnionFind<>();
    assertFalse(uf.contains("a"));
    assertTrue(uf.add("a"));
    assertTrue(uf.contains("a"));
    assertFalse(uf.add("a")); // already present
    assertEquals(1, uf.size());
    assertEquals(1, uf.numberOfSets());
  }

  @Test
  void testAddAll() {
    UnionFind<String> uf = new UnionFind<>();
    // Adding to an empty structure changes it.
    assertTrue(uf.addAll(List.of("a", "b")));
    assertEquals(2, uf.size());
    // Adding only already-present elements does not change it.
    assertFalse(uf.addAll(List.of("a", "b")));
    assertEquals(2, uf.size());
    // A mix of new and already-present elements changes it.
    assertTrue(uf.addAll(List.of("b", "c")));
    assertEquals(3, uf.size());
    // Adding nothing does not change it.
    assertFalse(uf.addAll(List.of()));
  }

  @Test
  void testFindMissingThrows() {
    UnionFind<String> uf = new UnionFind<>();
    assertThrows(NoSuchElementException.class, () -> uf.find("missing"));
  }

  @Test
  void testFindSingleton() {
    UnionFind<String> uf = new UnionFind<>();
    uf.add("a");
    assertEquals("a", uf.find("a"));
  }

  @Test
  void testUnionAutoAdds() {
    UnionFind<String> uf = new UnionFind<>();
    uf.union("a", "b");
    assertTrue(uf.contains("a"));
    assertTrue(uf.contains("b"));
    assertTrue(uf.sameSet("a", "b"));
    assertEquals(1, uf.numberOfSets());
    assertEquals(2, uf.size());
  }

  @Test
  void testUnionReturnsRepresentative() {
    UnionFind<String> uf = new UnionFind<>();
    String rep = uf.union("a", "b");
    assertEquals(rep, uf.find("a"));
    assertEquals(rep, uf.find("b"));
  }

  @Test
  void testUnionIsTransitive() {
    UnionFind<Integer> uf = new UnionFind<>();
    uf.union(1, 2);
    uf.union(3, 4);
    assertFalse(uf.sameSet(1, 3));
    uf.union(2, 3);
    assertTrue(uf.sameSet(1, 4));
    assertEquals(1, uf.numberOfSets());
  }

  @Test
  void testUnionOfSameSetIsNoop() {
    UnionFind<String> uf = new UnionFind<>();
    uf.union("a", "b");
    String rep = uf.find("a");
    assertEquals(rep, uf.union("a", "b"));
    assertEquals(rep, uf.union("b", "a"));
    assertEquals(1, uf.numberOfSets());
  }

  @Test
  void testSameSetMissingThrows() {
    UnionFind<String> uf = new UnionFind<>();
    uf.add("a");
    assertThrows(NoSuchElementException.class, () -> uf.sameSet("a", "b"));
  }

  // ---------------------------------------------------------------------------
  // Listing the elements of a set.
  // ---------------------------------------------------------------------------

  @Test
  void testElementsInSameSetAs() {
    UnionFind<String> uf = new UnionFind<>();
    uf.union("a", "b");
    uf.union("b", "c");
    uf.add("d");
    assertEquals(Set.of("a", "b", "c"), new HashSet<>(uf.elementsInSameSetAs("a")));
    assertEquals(Set.of("a", "b", "c"), new HashSet<>(uf.elementsInSameSetAs("c")));
    assertEquals(List.of("d"), uf.elementsInSameSetAs("d"));
  }

  @Test
  void testElementsInSameSetAsIsSnapshot() {
    UnionFind<String> uf = new UnionFind<>();
    uf.union("a", "b");
    List<String> before = uf.elementsInSameSetAs("a");
    uf.union("a", "c");
    assertEquals(2, before.size()); // not affected by the later union
    assertEquals(3, uf.elementsInSameSetAs("a").size());
  }

  @Test
  void testAllSets() {
    UnionFind<Integer> uf = new UnionFind<>();
    uf.union(1, 2);
    uf.union(3, 4);
    uf.add(5);
    assertEquals(3, uf.allSets().size());
    Set<Set<Integer>> sets = new HashSet<>();
    for (List<Integer> set : uf.allSets()) {
      sets.add(new HashSet<>(set));
    }
    Set<Set<Integer>> expected = new HashSet<>();
    expected.add(new HashSet<>(List.of(1, 2)));
    expected.add(new HashSet<>(List.of(3, 4)));
    expected.add(new HashSet<>(List.of(5)));
    assertEquals(expected, sets);
  }

  // ---------------------------------------------------------------------------
  // Unary predicate.
  // ---------------------------------------------------------------------------

  @Test
  void testUnaryPredicateComputed() {
    UnionFind<Integer> uf = new UnionFind<>(UnionFindTest::isBig, null);
    uf.addAll(List.of(1, 200));
    assertFalse(uf.test(1)); // no big element
    assertTrue(uf.test(200)); // 200 is big
  }

  @Test
  void testUnaryPredicateAppliesToWholeSet() {
    UnionFind<Integer> uf = new UnionFind<>(UnionFindTest::isBig, null);
    uf.union(1, 200);
    // The predicate can be queried through any member of the set.
    assertTrue(uf.test(1));
    assertTrue(uf.test(200));
  }

  /** If p(a) is true, then after z = union(a, e), p(z) is true. */
  @Test
  void testUnaryPredicatePreservedByUnion() {
    UnionFind<Integer> uf = new UnionFind<>(UnionFindTest::isBig, null);
    uf.add(500);
    assertTrue(uf.test(500)); // holds of {500}
    Integer z = uf.union(500, 7);
    assertTrue(uf.test(z));
    assertTrue(uf.test(7)); // through any member
  }

  @Test
  void testUnaryPredicateFalseFlipsTrueWhenBigElementJoins() {
    UnionFind<Integer> uf = new UnionFind<>(UnionFindTest::isBig, null);
    uf.add(1);
    assertFalse(uf.test(1)); // {1}, no big element; caches false
    uf.union(1, 200); // a big element joins the set
    assertTrue(uf.test(1)); // maintained: now true
  }

  @Test
  void testUnaryPredicateFalseStaysFalse() {
    UnionFind<Integer> uf = new UnionFind<>(UnionFindTest::isBig, null);
    uf.add(1);
    assertFalse(uf.test(1)); // {1}, caches false
    uf.union(1, 2); // another small element
    assertFalse(uf.test(1)); // still no big element
  }

  @Test
  void testUnaryPredicateCombinedByOrEitherOrder() {
    // The predicate holds of the merged set if it held of either original set, regardless of which
    // set is the winner.
    UnionFind<Integer> uf1 = new UnionFind<>(UnionFindTest::isBig, null);
    uf1.add(300);
    assertTrue(uf1.test(300)); // holds of {300}
    uf1.union(300, 4); // union with a small singleton
    assertTrue(uf1.test(4));

    UnionFind<Integer> uf2 = new UnionFind<>(UnionFindTest::isBig, null);
    uf2.add(4);
    assertFalse(uf2.test(4)); // holds of neither
    uf2.union(4, 300); // the big element is the second argument
    assertTrue(uf2.test(4));
  }

  @Test
  void testUnaryPredicateLazyAndIncremental() {
    AtomicInteger calls = new AtomicInteger();
    Predicate<Integer> counting =
        n -> {
          calls.incrementAndGet();
          return n >= 100;
        };
    UnionFind<Integer> uf = new UnionFind<>(counting, null);
    uf.union(1, 2); // {1, 2}

    // A query with no cached value tests every element of the set.
    calls.set(0);
    assertFalse(uf.test(1));
    assertEquals(2, calls.get());

    // A repeated query uses the cache and tests nothing.
    calls.set(0);
    assertFalse(uf.test(1));
    assertEquals(0, calls.get());

    // A union maintains the cached false value by testing only the new element.
    calls.set(0);
    uf.union(1, 200);
    assertEquals(1, calls.get());
    assertTrue(uf.test(1));
  }

  @Test
  void testUnaryPredicateMissingThrows() {
    UnionFind<Integer> uf = new UnionFind<>(); // no predicates supplied
    assertThrows(IllegalStateException.class, () -> uf.test(1));
  }

  @Test
  void testUnaryPredicateAbsentElementThrows() {
    UnionFind<Integer> uf = new UnionFind<>(UnionFindTest::isBig, null);
    assertThrows(NoSuchElementException.class, () -> uf.test(1)); // 1 was never added
  }

  // ---------------------------------------------------------------------------
  // Binary predicate.
  // ---------------------------------------------------------------------------

  @Test
  void testBinaryPredicateComputed() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(3, 4, 5));
    assertTrue(uf.test(3, 4)); // 4 == 3 + 1
    assertFalse(uf.test(3, 5)); // 5 != 3 + 1
  }

  @Test
  void testBinaryPredicateIsOrdered() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(3, 4));
    assertTrue(uf.test(3, 4)); // 4 == 3 + 1
    assertFalse(uf.test(4, 3)); // 3 != 4 + 1
  }

  @Test
  void testBinaryPredicateWithinOneSet() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.union(3, 4);
    // The pair (3, 4) is drawn from the single set {3, 4}.
    assertTrue(uf.test(3, 3));
  }

  @Test
  void testBinaryPredicateAppliesToWholeSets() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.union(3, 30); // one set is {3, 30}
    uf.union(4, 40); // the other is {4, 40}
    // The witnessing pair is (3, 4); it is queryable through any members of the two sets.
    assertTrue(uf.test(30, 40));
    assertTrue(uf.test(3, 40));
    assertTrue(uf.test(30, 4));
  }

  /** If q(a, b) is true, then after x = union(a, c), q(x, b) is true. */
  @Test
  void testBinaryPredicatePreservedByUnionOfFirstArg() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(3, 4));
    assertTrue(uf.test(3, 4)); // caches true for ({3}, {4})
    Integer x = uf.union(3, 10);
    assertTrue(uf.test(x, 4));
    assertTrue(uf.test(10, 4)); // through any member of x's set
  }

  /** If q(a, b) is true, then after y = union(b, d), q(a, y) is true. */
  @Test
  void testBinaryPredicatePreservedByUnionOfSecondArg() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(3, 4));
    assertTrue(uf.test(3, 4)); // caches true for ({3}, {4})
    Integer y = uf.union(4, 10);
    assertTrue(uf.test(3, y));
    assertTrue(uf.test(3, 10)); // through any member of y's set
  }

  @Test
  void testBinaryPredicateFalseFlipsTrueOnNewCrossPair() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(1, 5));
    assertFalse(uf.test(1, 5)); // ({1}, {5}); 5 != 1 + 1; caches false
    uf.union(5, 2); // the second set becomes {5, 2}, creating the new pair (1, 2)
    assertTrue(uf.test(1, 5)); // maintained: 2 == 1 + 1
  }

  @Test
  void testBinaryPredicateFalseStaysFalse() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(1, 5));
    assertFalse(uf.test(1, 5)); // caches false
    uf.union(5, 9); // second set becomes {5, 9}; new pair (1, 9) is not a success
    assertFalse(uf.test(1, 5));
  }

  @Test
  void testBinaryPredicateSelfPairMaintainedOnUnion() {
    // A false self-pair becomes true when a union introduces a witnessing cross pair.
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.add(3);
    assertFalse(uf.test(3, 3)); // {3}; (3, 3) is not a success; caches false
    uf.union(3, 4); // set becomes {3, 4}, adding the pair (3, 4)
    assertTrue(uf.test(3, 3)); // maintained: 4 == 3 + 1
  }

  @Test
  void testBinaryPredicateBetweenTwoSetsThatGetUnioned() {
    // When the two related sets are themselves merged, the value becomes a self-pair value.
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(3, 4));
    assertTrue(uf.test(3, 4)); // caches true for ({3}, {4})
    Integer rep = uf.union(3, 4);
    assertTrue(uf.test(rep, rep));
    assertTrue(uf.test(3, 4));
  }

  @Test
  void testBinaryPredicateWinnerFalseEntryMaintainedByUnion() {
    // A cached false pair that mentions only the winner (not the loser) must still be re-evaluated
    // against the loser's new members when the winner's set grows.
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(1, 2, 3, 4));
    uf.union(1, 4); // {1, 4}, the higher-rank set, so it wins the next union
    assertFalse(uf.test(1, 3)); // ({1,4}, {3}): (1,3),(4,3) fail; caches (rep, 3) = false
    uf.union(1, 2); // the winner's set grows to {1, 4, 2}
    assertTrue(uf.test(1, 3)); // the new pair (2, 3) satisfies successor
  }

  @Test
  void testBinaryPredicateSelfPairAbsentBlockMaintainedByUnion() {
    // A false self-pair whose winner-side block was never evaluated must still become true when a
    // union merges in a set that forms a witnessing pair inside the winner's own old members.
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.addAll(List.of(1, 2, 5));
    uf.union(1, 2); // {1, 2}; test(1,1) is never called, so the self-pair value stays uncomputed
    assertFalse(uf.test(1, 5)); // ({1,2}, {5}): (1,5),(2,5) fail; caches false
    uf.union(1, 5); // merged set is {1, 2, 5}
    assertTrue(
        uf.test(1, 1)); // the pair (1, 2), inside the old winner members, satisfies successor
  }

  @Test
  void testBinaryPredicateLazyAndIncremental() {
    AtomicInteger calls = new AtomicInteger();
    BiPredicate<Integer, Integer> counting =
        (x, y) -> {
          calls.incrementAndGet();
          return y == x + 1;
        };
    UnionFind<Integer> uf = new UnionFind<>(null, counting);
    uf.union(1, 2); // first set {1, 2}
    uf.union(5, 9); // second set {5, 9}

    // A query with no cached value tests every ordered pair across the two sets.
    calls.set(0);
    assertFalse(uf.test(1, 5)); // 4 pairs: (1,5) (1,9) (2,5) (2,9)
    assertEquals(4, calls.get());

    // A repeated query uses the cache and tests nothing.
    calls.set(0);
    assertFalse(uf.test(1, 5));
    assertEquals(0, calls.get());

    // Enlarging the second set with {3} maintains the cached false value by testing only the new
    // cross pairs {1, 2} x {3}, not the pairs already checked.
    calls.set(0);
    uf.union(5, 3);
    assertEquals(2, calls.get()); // (1, 3) and (2, 3)
    assertTrue(uf.test(1, 5)); // 3 == 2 + 1
  }

  @Test
  void testBinaryPredicateMissingThrows() {
    UnionFind<Integer> uf = new UnionFind<>(); // no predicates supplied
    assertThrows(IllegalStateException.class, () -> uf.test(1, 2));
  }

  @Test
  void testBinaryPredicateAbsentElementThrows() {
    UnionFind<Integer> uf = new UnionFind<>(null, UnionFindTest::successor);
    uf.add(1);
    assertThrows(NoSuchElementException.class, () -> uf.test(1, 2)); // 2 was never added
  }

  @Test
  void testBothPredicatesSupplied() {
    UnionFind<Integer> uf = new UnionFind<>(UnionFindTest::isBig, UnionFindTest::successor);
    uf.addAll(List.of(3, 4, 100));
    assertTrue(uf.test(100));
    assertTrue(uf.test(3, 4));
    assertFalse(uf.test(3));
    assertFalse(uf.test(4, 3));
  }

  // ---------------------------------------------------------------------------
  // A larger, randomized-style scenario to exercise path compression and rank.
  // ---------------------------------------------------------------------------

  @Test
  void testManyElements() {
    UnionFind<Integer> uf = new UnionFind<>();
    for (int i = 0; i < 100; i++) {
      uf.add(i);
    }
    // Union consecutive pairs: {0,1}, {2,3}, ...
    for (int i = 0; i < 100; i += 2) {
      uf.union(i, i + 1);
    }
    assertEquals(50, uf.numberOfSets());
    // Chain all even representatives together.
    for (int i = 0; i < 98; i += 2) {
      uf.union(i, i + 2);
    }
    assertEquals(1, uf.numberOfSets());
    assertEquals(100, uf.elementsInSameSetAs(42).size());
    for (int i = 0; i < 100; i++) {
      assertTrue(uf.sameSet(0, i));
    }
  }
}
