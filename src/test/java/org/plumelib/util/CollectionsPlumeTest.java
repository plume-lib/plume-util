package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.junit.jupiter.api.Test;
import org.plumelib.util.CollectionsPlume.Replacement;

public final class CollectionsPlumeTest {

  // If true, do 100 instead of 100000 iterations when testing randomElements.
  // This saves only a little time.  However, it is significant when running
  // under instrumentation such as that of Chicory.
  private static final boolean shortRun = false;

  // //////////////////////////////////////////////////////////////////////
  // Immutable variables
  //

  private static final List<Integer> l123 = Arrays.asList(1, 2, 3);
  private static final List<Integer> l123123 = Arrays.asList(1, 2, 3, 1, 2, 3);
  private static final List<Integer> l12223 = Arrays.asList(1, 2, 2, 2, 3);
  private static final List<Integer> l1123 = Arrays.asList(1, 1, 2, 3);
  private static final List<Integer> l1233 = Arrays.asList(1, 2, 3, 3);

  private static final Object object1 = new Object();
  private static final Object object2 = new Object();
  private static final Object object3 = new Object();

  private static final List<Object> lo123 = Arrays.asList(object1, object2, object3);
  private static final List<Object> lo123123 =
      Arrays.asList(object1, object2, object3, object1, object2, object3);
  private static final List<Object> lo12223 =
      Arrays.asList(object1, object2, object2, object2, object3);
  private static final List<Object> lo1123 = Arrays.asList(object1, object1, object2, object3);
  private static final List<Object> lo1233 = Arrays.asList(object1, object2, object3, object3);

  // //////////////////////////////////////////////////////////////////////
  // Helper functions
  //

  @SuppressWarnings("NonApiType")
  public static <T> ArrayList<T> toArrayList(Iterator<T> itor) {
    ArrayList<T> v = new ArrayList<>();
    while (itor.hasNext()) {
      v.add(itor.next());
    }
    return v;
  }

  @SuppressWarnings({"JdkObsolete", "NonApiType"})
  public static <T> ArrayList<T> toArrayList(Enumeration<T> e) {
    ArrayList<T> v = new ArrayList<>();
    while (e.hasMoreElements()) {
      v.add(e.nextElement());
    }
    return v;
  }

  // These names are taken from APL notation, where iota creates an
  // array of all the numbers up to its argument.
  private static List<Integer> iota0 = new ArrayList<>();
  private static List<Integer> iota10 = new ArrayList<>();
  private static List<Integer> iota10Twice = new ArrayList<>();
  private static List<Integer> iota10Thrice = new ArrayList<>();

  static {
    for (int i = 0; i < 10; i++) {
      iota10.add(i);
    }
    iota10Twice.addAll(iota10);
    iota10Twice.addAll(iota10);
    iota10Thrice.addAll(iota10);
    iota10Thrice.addAll(iota10);
    iota10Thrice.addAll(iota10);
    // Could make these lists immutable to avoid accidental modification in the tests.
  }

  private static class OddPredicate implements Predicate<Integer> {
    public OddPredicate() {}

    @Override
    public boolean test(Integer i) {
      return i.intValue() % 2 != 0;
    }
  }

  // Iterate through numbers from zero up to the argument (non-inclusive)
  private static class IotaIterator implements Iterator<Integer> {
    int i = 0;
    int limit;

    public IotaIterator(int limit) {
      this.limit = limit;
    }

    @Override
    public boolean hasNext(@GuardSatisfied IotaIterator this) {
      return i < limit;
    }

    @Override
    public Integer next(@GuardSatisfied IotaIterator this) {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return i++;
    }

    @Override
    public void remove(@GuardSatisfied IotaIterator this) {
      throw new UnsupportedOperationException();
    }
  }

  // //////////////////////////////////////////////////////////////////////
  // The tests themselves
  //

  // //////////////////////////////////////////////////////////////////////
  // Collections
  //

  // public static <T> boolean addAll(Collection<? super T> c, Iterable<? extends T> elements)

  @Test
  void testAddAllIterable() {
    Iterable<String> itble = CollectionsPlume.listOf("a", "b");
    Collection<String> c = new ArrayList<>();
    CollectionsPlume.addAll(c, itble);
    assertEquals(Arrays.asList("a", "b"), c);
  }

  // public static <T> boolean hasDuplicates(List<T> a)

  @SuppressWarnings("JdkObsolete") // test of List that does not implement RandomAccess
  @Test
  public void test_hasDuplicates() {
    assertFalse(CollectionsPlume.hasDuplicates(l123));
    assertTrue(CollectionsPlume.hasDuplicates(l123123));
    assertTrue(CollectionsPlume.hasDuplicates(l12223));
    assertTrue(CollectionsPlume.hasDuplicates(l1123));
    assertTrue(CollectionsPlume.hasDuplicates(l1233));

    assertFalse(CollectionsPlume.hasDuplicates(new LinkedList<>(l123)));
    assertTrue(CollectionsPlume.hasDuplicates(new LinkedList<>(l123123)));
    assertTrue(CollectionsPlume.hasDuplicates(new LinkedList<>(l12223)));
    assertTrue(CollectionsPlume.hasDuplicates(new LinkedList<>(l1123)));
    assertTrue(CollectionsPlume.hasDuplicates(new LinkedList<>(l1233)));
  }

  // public static <T> boolean hasNoDuplicates(List<T> a)

  // public static <T> boolean noDuplicates(List<T> a)

  // public static <T> List<T> removeDuplicates(List<T> l)

  // public static <T> List<T> withoutDuplicates(List<T> values)

  @SuppressWarnings("ArrayEquals")
  @Test
  public void test_withoutDuplicates() {

    // public static <T> List<T> withoutDuplicates(List<T> l) {

    assertEquals(l123, CollectionsPlume.withoutDuplicates(l123));
    assertEquals(l123, CollectionsPlume.withoutDuplicates(l123123));
    assertEquals(l123, CollectionsPlume.withoutDuplicates(l12223));
    assertEquals(l123, CollectionsPlume.withoutDuplicates(l1123));
    assertEquals(l123, CollectionsPlume.withoutDuplicates(l1233));

    assertEquals(lo123, CollectionsPlume.withoutDuplicates(lo123));
    assertEquals(lo123, CollectionsPlume.withoutDuplicates(lo123123));
    assertEquals(lo123, CollectionsPlume.withoutDuplicates(lo12223));
    assertEquals(lo123, CollectionsPlume.withoutDuplicates(lo1123));
    assertEquals(lo123, CollectionsPlume.withoutDuplicates(lo1233));
  }

  // public static <T extends Comparable<T>> List<T> withoutDuplicatesSorted(List<T> values)

  // public static <T extends Comparable<T>> List<T> withoutDuplicatesComparable(List<T> values)

  @SuppressWarnings("ArrayEquals")
  @Test
  public void test_withoutDuplicatesComparable() {

    // public static <T> List<T> withoutDuplicates(List<T> l) {

    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l123));
    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l123123));
    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l12223));
    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l1123));
    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l1233));
  }

  // public static <T> List<T> sortList(List<T> l, Comparator<@MustCallUnknown ? super T> c)

  // public static <T extends Comparable<T>> boolean isSorted(List<T> values)

  @Test
  public void testIsSorted() {
    assertTrue(CollectionsPlume.isSorted(l123));
    assertFalse(CollectionsPlume.isSorted(l123123));
    assertTrue(CollectionsPlume.isSorted(l12223));
    assertTrue(CollectionsPlume.isSorted(l1123));
    assertTrue(CollectionsPlume.isSorted(l1233));
  }

  // public static <T extends Comparable<T>> boolean isSortedNoDuplicates(List<T> values)

  @Test
  public void testIsSortedNoDuplicates() {
    assertTrue(CollectionsPlume.isSortedNoDuplicates(l123));
    assertFalse(CollectionsPlume.isSortedNoDuplicates(l123123));
    assertFalse(CollectionsPlume.isSortedNoDuplicates(l12223));
    assertFalse(CollectionsPlume.isSortedNoDuplicates(l1123));
    assertFalse(CollectionsPlume.isSortedNoDuplicates(l1233));
  }

  // public static <T> Collection<T> duplicates(Collection<T> c)

  @Test
  void testDuplicates() {
    assertEquals(Collections.emptyList(), new ArrayList<>(CollectionsPlume.duplicates(l123)));
    assertEquals(l123, new ArrayList<>(CollectionsPlume.duplicates(l123123)));
    assertEquals(Arrays.asList(2), new ArrayList<>(CollectionsPlume.duplicates(l12223)));
    assertEquals(Arrays.asList(1), new ArrayList<>(CollectionsPlume.duplicates(l1123)));
    assertEquals(Arrays.asList(3), new ArrayList<>(CollectionsPlume.duplicates(l1233)));
  }

  // public static boolean deepEquals(@Nullable Object o1, @Nullable Object o2)

  @Test
  @SuppressWarnings("ArrayEquals") // demonstrates the effect of regular equals
  public void testDeepEquals() {

    // public boolean deepEquals(Object o1, Object o2)

    boolean[] zatft1 = new boolean[] {true, false, true};
    boolean[] zatft2 = new boolean[] {true, false, true};
    boolean[] zatff = new boolean[] {true, false, false};
    assertTrue(!zatft1.equals(zatft2)); // regular equals returns false
    assertTrue(CollectionsPlume.deepEquals(zatft1, zatft2));
    assertTrue(!zatft1.equals(zatff)); // regular equals returns false
    assertTrue(!CollectionsPlume.deepEquals(zatft1, zatff));

    List<Object> l1 = new ArrayList<>();
    List<Object> l2 = new ArrayList<>();
    List<Object> l3 = new ArrayList<>();
    l1.add(l1);
    l2.add(l2);
    l3.add(l3);
    l1.add(zatft1);
    l2.add(zatft2);
    l3.add(zatff);
    // Don't test .equals because it suffers infinite recursion.
    // assertTrue(! l1.equals(l2));
    // assertTrue(! l1.equals(l3));
    // assertTrue(! l2.equals(l3));
    assertTrue(CollectionsPlume.deepEquals(l1, l2));
    assertTrue(!CollectionsPlume.deepEquals(l1, l3));
    assertTrue(!CollectionsPlume.deepEquals(l2, l3));
  }

  // public static <List<TO> mapList(Function<? super FROM, ? extends TO> f, Iterable<FROM>
  // iterable)

  // public static <List<TO> mapList(Function<@MustCallUnknown ? super FROM, ? extends TO> f, FROM[]
  // a)

  @Test
  @SuppressWarnings("lock:type.arguments.not.inferred")
  public void testMapList() {
    List<Object> in = Arrays.asList(new Object[] {1, 2, 3});
    List<Object> out = Arrays.asList(new Object[] {"1", "2", "3"});
    assertEquals(out, CollectionsPlume.mapList(Object::toString, in));
  }

  // public static <List<TO> transform(Iterable<FROM> iterable, Function f)

  @Test
  @SuppressWarnings("lock:type.arguments.not.inferred")
  public void testTransform() {
    List<Object> in = Arrays.asList(new Object[] {1, 2, 3});
    List<Object> out = Arrays.asList(new Object[] {"1", "2", "3"});
    assertEquals(out, CollectionsPlume.transform(in, Object::toString));
  }

  // public static <T extends @Nullable Object, C extends @Nullable Collection<T>>

  // public static <T extends @Nullable DeepCopyable<T>, C extends @Nullable Collection<T>>

  // public static <T> List<T> listFilter(Iterable<T> coll, Predicate<? super T> filter)

  @Test
  public void testListFilter() {
    List<Integer> in = Arrays.asList(new Integer[] {1, 2, 3, 4, 5});
    List<Integer> odd = Arrays.asList(new Integer[] {1, 3, 5});
    List<Integer> even = Arrays.asList(new Integer[] {2, 4});
    assertEquals(odd, CollectionsPlume.filter(in, i -> i % 2 == 1));
    assertEquals(even, CollectionsPlume.filter(in, i -> i % 2 == 0));
  }

  // public static <T> List<T> filter(Iterable<T> coll, Predicate<? super T> filter)

  // public static <T> boolean anyMatch(Iterable<T> coll, Predicate<? super T> predicate)

  @Test
  public void testAnyMatch() {
    List<Integer> iota = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    assertTrue(CollectionsPlume.anyMatch(iota, i -> i > -5));
    assertTrue(CollectionsPlume.anyMatch(iota, i -> i > 5));
    assertFalse(CollectionsPlume.anyMatch(iota, i -> i > 15));
  }

  // public static <T> boolean allMatch(Iterable<T> coll, Predicate<? super T> predicate)

  @Test
  public void testAllMatch() {
    List<Integer> iota = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    assertTrue(CollectionsPlume.allMatch(iota, i -> i > -5));
    assertFalse(CollectionsPlume.allMatch(iota, i -> i > 5));
    assertFalse(CollectionsPlume.allMatch(iota, i -> i > 15));
  }

  // public static <T> boolean noneMatch(Iterable<T> coll, Predicate<? super T> predicate)

  @Test
  public void testNoneMatch() {
    List<Integer> iota = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    assertFalse(CollectionsPlume.noneMatch(iota, i -> i > -5));
    assertFalse(CollectionsPlume.noneMatch(iota, i -> i > 5));
    assertTrue(CollectionsPlume.noneMatch(iota, i -> i > 15));
  }

  // public static <T> @Nullable T firstMatch(Iterable<T> coll, Predicate<? super T> predicate)

  @Test
  @SuppressWarnings("nullness:unboxing.of.nullable")
  public void testfirstMatch() {
    List<Integer> iota = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    assertEquals(0, (int) CollectionsPlume.firstMatch(iota, i -> i > -5));
    assertEquals(6, (int) CollectionsPlume.firstMatch(iota, i -> i > 5));
    assertNull(CollectionsPlume.firstMatch(iota, i -> i > 15));
  }

  // public static int indexOf(List<?> list, Object value, int start)

  @Test
  public void testIndexOf() {
    List<Integer> nums = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 0, 1, 2});
    assertEquals(3, CollectionsPlume.indexOf(nums, 3, 0));
    assertEquals(-1, CollectionsPlume.indexOf(nums, 3, 5));
    assertEquals(7, CollectionsPlume.indexOf(nums, 1, 3));
    assertEquals(-1, CollectionsPlume.indexOf(nums, 100, 0));
  }

  // public static class Replacement<T>

  // public static <T> List<T> replace(Iterable<T> c, Iterable<Replacement<T>> replacements)

  // public static <T> List<T> replace(T[] c, Collection<Replacement<T>> replacements)

  @Test
  public void testReplace() {
    List<Integer> iota = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    List<Integer> empty = Arrays.asList(new Integer[] {});
    List<Integer> l_101_103 = Arrays.asList(new Integer[] {101, 102, 103});
    List<Integer> l_201_205 = Arrays.asList(new Integer[] {201, 202, 203, 204, 205});

    {
      List<Replacement<Integer>> replacements =
          Arrays.asList(Replacement.of(1, 3, l_101_103), Replacement.of(5, 6, l_201_205));
      List<Integer> expected =
          Arrays.asList(new Integer[] {0, 101, 102, 103, 4, 201, 202, 203, 204, 205, 7, 8, 9});
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements =
          Arrays.asList(
              Replacement.of(1, 1, Arrays.asList(new Integer[] {101, 102, 103})),
              Replacement.of(5, 5, Arrays.asList(new Integer[] {201, 202, 203, 204, 205})));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(
              new Integer[] {0, 101, 102, 103, 2, 3, 4, 201, 202, 203, 204, 205, 6, 7, 8, 9});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements =
          Arrays.asList(
              Replacement.of(1, 0, Arrays.asList(new Integer[] {101, 102, 103})),
              Replacement.of(5, 4, Arrays.asList(new Integer[] {201, 202, 203, 204, 205})));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(
              new Integer[] {0, 101, 102, 103, 1, 2, 3, 4, 201, 202, 203, 204, 205, 5, 6, 7, 8, 9});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements =
          Arrays.asList(
              Replacement.of(0, 4, Arrays.asList(new Integer[] {101, 102, 103})),
              Replacement.of(5, 5, Arrays.asList(new Integer[] {201, 202, 203, 204, 205})));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(new Integer[] {101, 102, 103, 201, 202, 203, 204, 205, 6, 7, 8, 9});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements = Arrays.asList(Replacement.of(0, 9, empty));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected = empty;
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements = Arrays.asList(Replacement.of(1, 8, empty));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected = Arrays.asList(new Integer[] {0, 9});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements =
          Arrays.asList(Replacement.of(0, 5, l_101_103), Replacement.of(5, 8, l_201_205));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(new Integer[] {101, 102, 103, 201, 202, 203, 204, 205, 9});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements = Arrays.asList(Replacement.of(9, 9, l_101_103));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 101, 102, 103});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements = Arrays.asList(Replacement.of(9, 8, l_101_103));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 101, 102, 103, 9});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements = Arrays.asList(Replacement.of(0, 0, l_101_103));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(new Integer[] {101, 102, 103, 1, 2, 3, 4, 5, 6, 7, 8, 9});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements = Arrays.asList(Replacement.of(0, -1, l_101_103));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(new Integer[] {101, 102, 103, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements =
          Arrays.asList(
              Replacement.of(0, 4, l_101_103),
              Replacement.of(5, 4, l_201_205),
              Replacement.of(5, 4, l_201_205));
      List<Integer> replaced = CollectionsPlume.replace(iota, replacements);
      List<Integer> expected =
          Arrays.asList(
              new Integer[] {
                101, 102, 103, 201, 202, 203, 204, 205, 201, 202, 203, 204, 205, 5, 6, 7, 8, 9
              });
      assertEquals(expected, replaced);
    }

    {
      List<Replacement<Integer>> replacements = Arrays.asList(Replacement.of(0, -1, l_101_103));
      List<Integer> replaced = CollectionsPlume.replace(empty, replacements);
      List<Integer> expected = l_101_103;
      assertEquals(expected, replaced);
    }
  }

  // public static <T> boolean isSubsequenceMaybeNonContiguous(Iterable<T> longer, Iterable<T>
  // shorter)

  @Test
  public void testIsSubsequenceMaybeNonContiguous() {
    List<Integer> iota = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    List<Integer> iota11 = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
    List<Integer> empty = Arrays.asList(new Integer[] {});

    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(empty, empty));
    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, empty));
    assertFalse(CollectionsPlume.isSubsequenceMaybeNonContiguous(empty, iota));

    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, Arrays.asList(0)));
    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, Arrays.asList(0, 1, 2)));
    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, Arrays.asList(9)));
    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, Arrays.asList(7, 8, 9)));
    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, Arrays.asList(5)));
    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, Arrays.asList(4, 5, 6)));
    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, Arrays.asList(2, 4, 6, 8)));
    assertFalse(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota, iota11));
    assertTrue(CollectionsPlume.isSubsequenceMaybeNonContiguous(iota11, iota));
  }

  // //////////////////////////////////////////////////////////////////////
  // SortedSet
  //

  // public static <T> boolean sortedSetEquals(SortedSet<T> set1, SortedSet<T> set2)

  @Test
  public void testSortedSetEquals() {
    TreeSet<Integer> s2 = new TreeSet<>(Arrays.asList(1, 2));
    TreeSet<Integer> s3 = new TreeSet<>(Arrays.asList(1, 2, 3));
    TreeSet<Integer> s3a = new TreeSet<>(Arrays.asList(3, 2, 1));
    TreeSet<Integer> s4 = new TreeSet<>(Arrays.asList(1, 2, 3, 4));

    assertTrue(CollectionsPlume.sortedSetEquals(s3, s3));
    assertTrue(CollectionsPlume.sortedSetEquals(s3, s3a));
    assertFalse(CollectionsPlume.sortedSetEquals(s3, s2));
    assertFalse(CollectionsPlume.sortedSetEquals(s3, s4));
  }

  // public static <T> boolean sortedSetContainsAll(SortedSet<T> set1, SortedSet<T> set2)

  @Test
  public void testSortedSetContainsAll() {
    TreeSet<Integer> s2 = new TreeSet<>(Arrays.asList(1, 2));
    TreeSet<Integer> s3 = new TreeSet<>(Arrays.asList(1, 2, 3));
    TreeSet<Integer> s3a = new TreeSet<>(Arrays.asList(3, 2, 1));
    TreeSet<Integer> s4 = new TreeSet<>(Arrays.asList(1, 2, 3, 4));

    assertTrue(CollectionsPlume.sortedSetContainsAll(s2, s2));
    assertFalse(CollectionsPlume.sortedSetContainsAll(s2, s3));
    assertFalse(CollectionsPlume.sortedSetContainsAll(s2, s3a));
    assertFalse(CollectionsPlume.sortedSetContainsAll(s2, s4));

    assertTrue(CollectionsPlume.sortedSetContainsAll(s3, s2));
    assertTrue(CollectionsPlume.sortedSetContainsAll(s3, s3));
    assertTrue(CollectionsPlume.sortedSetContainsAll(s3, s3a));
    assertFalse(CollectionsPlume.sortedSetContainsAll(s3, s4));

    assertTrue(CollectionsPlume.sortedSetContainsAll(s3a, s2));
    assertTrue(CollectionsPlume.sortedSetContainsAll(s3a, s3));
    assertTrue(CollectionsPlume.sortedSetContainsAll(s3a, s3a));
    assertFalse(CollectionsPlume.sortedSetContainsAll(s3a, s4));

    assertTrue(CollectionsPlume.sortedSetContainsAll(s4, s2));
    assertTrue(CollectionsPlume.sortedSetContainsAll(s4, s3));
    assertTrue(CollectionsPlume.sortedSetContainsAll(s4, s3a));
    assertTrue(CollectionsPlume.sortedSetContainsAll(s4, s4));
  }

  // Median of 5 runs with size=4: ratio = .90, meaning 10% speedup.
  // @Test
  @SuppressWarnings("ReturnValueIgnored")
  void testSortedSetTime() {
    int size = 4;
    int iterations = 100000;
    long sortedTime = 0;
    long unsortedTime = 0;
    Random random = new Random(0);
    for (int i = 0; i < iterations; i++) {
      SortedSet<Integer> s1 = new TreeSet<Integer>();
      SortedSet<Integer> s2 = new TreeSet<Integer>();
      SortedSet<Integer> s3 = new TreeSet<Integer>();
      for (int j = 0; j < size; j++) {
        int elt1 = random.nextInt(10);
        int elt2 = random.nextInt(10);
        s1.add(elt1);
        s2.add(elt1);
        s3.add(elt2);
      }
      s3.add(random.nextInt(10));
      long sortedStart = System.nanoTime();
      for (int k = 0; k < 10; k++) {
        CollectionsPlume.sortedSetEquals(s1, s2);
        CollectionsPlume.sortedSetEquals(s2, s1);
        CollectionsPlume.sortedSetEquals(s2, s3);
        CollectionsPlume.sortedSetEquals(s3, s2);
        CollectionsPlume.sortedSetContainsAll(s1, s2);
        CollectionsPlume.sortedSetContainsAll(s2, s1);
        CollectionsPlume.sortedSetContainsAll(s2, s3);
        CollectionsPlume.sortedSetContainsAll(s3, s2);
      }
      sortedTime += (System.nanoTime() - sortedStart);
      long unsortedStart = System.nanoTime();
      for (int k = 0; k < 10; k++) {
        s1.equals(s2);
        s2.equals(s1);
        s2.equals(s3);
        s3.equals(s2);
        s1.containsAll(s2);
        s2.containsAll(s1);
        s2.containsAll(s3);
        s3.containsAll(s2);
      }
      unsortedTime += (System.nanoTime() - unsortedStart);
    }
    System.out.printf("testSortedSetTime: size = %s, iterations = %s%n", size, iterations);
    System.out.printf("  CollectionsPlume: time = %s%n", sortedTime);
    System.out.printf("  JDK             : time = %s%n", unsortedTime);
    System.out.printf("  ratio = %s%n", 1.0 * sortedTime / unsortedTime);
  }

  // //////////////////////////////////////////////////////////////////////
  // ArrayList
  //

  // public static <T> ArrayList<T> makeArrayList(Enumeration<T> e)

  // public static <E> List<E> listOf(E e1, E e2)

  @Test
  public void testListOf() {
    assertEquals(Arrays.asList("a", "b"), CollectionsPlume.listOf("a", "b"));
  }

  // public static <T> List<T> append(Collection<T> list, T lastElt)

  @Test
  public void testAppend() {
    assertEquals(
        Arrays.asList("a", "b", "c"), CollectionsPlume.append(Arrays.asList("a", "b"), "c"));
  }

  // public static <T> List<T> concatenate(Collection<T> list1, Collection<T> list2)

  // public static <T> List<List<T>> createCombinations(

  // public static ArrayList<ArrayList<Integer>> createCombinations(int arity, @NonNegative int
  // start, int cnt)

  /** Tests UtilPlume createCombinations routines. */
  @Test
  public void test_createCombinations() {

    // public static List createCombinations (int dims, int start, List objs)
    Object a = new Object();
    Object b = new Object();
    Object c = new Object();
    List<Object> aList = Arrays.<Object>asList(new Object[] {a});
    List<Object> bList = Arrays.<Object>asList(new Object[] {b});
    List<Object> cList = Arrays.<Object>asList(new Object[] {c});
    List<Object> aa = Arrays.<Object>asList(new Object[] {a, a});
    List<Object> bb = Arrays.<Object>asList(new Object[] {b, b});
    List<Object> cc = Arrays.<Object>asList(new Object[] {c, c});
    List<Object> ab = Arrays.<Object>asList(new Object[] {a, b});
    List<Object> ac = Arrays.<Object>asList(new Object[] {a, c});
    List<Object> bc = Arrays.<Object>asList(new Object[] {b, c});

    List<Object> abc = Arrays.asList(a, b, c);
    List<List<Object>> combo1 = CollectionsPlume.createCombinations(1, 0, abc);
    assertTrue(combo1.size() == 3);
    assertTrue(combo1.contains(aList));
    assertTrue(combo1.contains(bList));
    assertTrue(combo1.contains(cList));

    List<List<Object>> combo2 = CollectionsPlume.createCombinations(2, 0, abc);
    assertTrue(combo2.size() == 6);
    assertTrue(combo2.contains(aa));
    assertTrue(combo2.contains(ab));
    assertTrue(combo2.contains(ac));
    assertTrue(combo2.contains(bb));
    assertTrue(combo2.contains(bc));
    assertTrue(combo2.contains(cc));

    // public static List createCombinations (int arity, int start, int cnt)
    Integer i0 = 0;
    Integer i1 = 1;
    Integer i2 = 2;
    Integer i10 = 10;
    Integer i11 = 11;
    Integer i12 = 12;

    List<ArrayList<Integer>> combo3 = CollectionsPlume.createCombinations(1, 0, 2);
    assertTrue(combo3.size() == 3);
    assertTrue(combo3.contains(Arrays.asList(new Integer[] {i0})));
    assertTrue(combo3.contains(Arrays.asList(new Integer[] {i1})));
    assertTrue(combo3.contains(Arrays.asList(new Integer[] {i2})));

    List<ArrayList<Integer>> combo4 = CollectionsPlume.createCombinations(2, 0, 2);
    assertTrue(combo4.size() == 6);
    assertTrue(combo4.contains(Arrays.asList(new Integer[] {i0, i0})));
    assertTrue(combo4.contains(Arrays.asList(new Integer[] {i0, i1})));
    assertTrue(combo4.contains(Arrays.asList(new Integer[] {i0, i2})));
    assertTrue(combo4.contains(Arrays.asList(new Integer[] {i1, i1})));
    assertTrue(combo4.contains(Arrays.asList(new Integer[] {i1, i2})));
    assertTrue(combo4.contains(Arrays.asList(new Integer[] {i2, i2})));

    List<ArrayList<Integer>> combo5 = CollectionsPlume.createCombinations(2, 10, 12);
    assertTrue(combo5.size() == 6);
    assertTrue(combo5.contains(Arrays.asList(new Integer[] {i10, i10})));
    assertTrue(combo5.contains(Arrays.asList(new Integer[] {i10, i11})));
    assertTrue(combo5.contains(Arrays.asList(new Integer[] {i10, i12})));
    assertTrue(combo5.contains(Arrays.asList(new Integer[] {i11, i11})));
    assertTrue(combo5.contains(Arrays.asList(new Integer[] {i11, i12})));
    assertTrue(combo5.contains(Arrays.asList(new Integer[] {i12, i12})));
  }

  // //////////////////////////////////////////////////////////////////////
  // Iterator
  //

  // public static <T> Iterable<T> iteratorToIterable(final Iterator<T> source)

  // public static final class EnumerationIterator<T> implements Iterator<T>

  // public static final class IteratorEnumeration<T> implements Enumeration<T>

  // public static <T> Iterator<T> iteratorPlusOne(Iterator<T> itor, T lastElement)

  // public static <T> Iterator<T> mergedIterator2(Iterator<T> itor1, Iterator<T> itor2)

  // public static final class MergedIterator2<T> implements Iterator<T>

  // public static <T> Iterator<T> mergedIterator(Iterable<Iterator<T>> itbleOfItors)

  // public static <T> Iterator<T> mergedIterator(Iterator<Iterator<T>> itorOfItors)

  // public static final class MergedIterator<T> implements Iterator<T>

  @Test
  public void testMergedIterator() {

    // public static class EnumerationIterator implements Iterator
    // public static class IteratorEnumeration implements Enumeration

    assertTrue(iota0.equals(toArrayList(iota0.iterator())));
    assertEquals(
        iota0, toArrayList(new CollectionsPlume.IteratorEnumeration<Integer>(iota0.iterator())));
    assertTrue(iota10.equals(toArrayList(iota10.iterator())));
    assertEquals(
        iota10, toArrayList(new CollectionsPlume.IteratorEnumeration<Integer>(iota10.iterator())));

    // public static class MergedIterator2 implements Iterator {
    assertEquals(
        iota10Twice,
        toArrayList(CollectionsPlume.mergedIterator2(iota10.iterator(), iota10.iterator())));
    assertEquals(
        iota10, toArrayList(CollectionsPlume.mergedIterator2(iota0.iterator(), iota10.iterator())));
    assertEquals(
        iota10, toArrayList(CollectionsPlume.mergedIterator2(iota10.iterator(), iota0.iterator())));

    // public static class MergedIterator implements Iterator {
    ArrayList<Iterator<Integer>> iota10IteratorThrice = new ArrayList<>();
    iota10IteratorThrice.add(iota10.iterator());
    iota10IteratorThrice.add(iota10.iterator());
    iota10IteratorThrice.add(iota10.iterator());
    assertEquals(
        iota10Thrice,
        toArrayList(CollectionsPlume.mergedIterator(iota10IteratorThrice.iterator())));
    ArrayList<Iterator<Integer>> iota10IteratorTwice1 = new ArrayList<>();
    iota10IteratorTwice1.add(iota0.iterator());
    iota10IteratorTwice1.add(iota10.iterator());
    iota10IteratorTwice1.add(iota10.iterator());
    ArrayList<Iterator<Integer>> iota10IteratorTwice2 = new ArrayList<>();
    iota10IteratorTwice2.add(iota10.iterator());
    iota10IteratorTwice2.add(iota0.iterator());
    iota10IteratorTwice2.add(iota10.iterator());
    ArrayList<Iterator<Integer>> iota10IteratorTwice3 = new ArrayList<>();
    iota10IteratorTwice3.add(iota10.iterator());
    iota10IteratorTwice3.add(iota10.iterator());
    iota10IteratorTwice3.add(iota0.iterator());
    assertEquals(
        iota10Twice, toArrayList(CollectionsPlume.mergedIterator(iota10IteratorTwice1.iterator())));
    assertEquals(
        iota10Twice, toArrayList(CollectionsPlume.mergedIterator(iota10IteratorTwice2.iterator())));
    assertEquals(
        iota10Twice, toArrayList(CollectionsPlume.mergedIterator(iota10IteratorTwice3.iterator())));
  }

  // public static <T> Iterator<T> filteredIterator(Iterator<T> itor, Predicate<T> predicate)

  // public static final class FilteredIterator<T> implements Iterator<T>

  @Test
  public void testFilteredIterator() {

    // public static final class FilteredIterator implements Iterator

    ArrayList<Integer> iota10Odd = new ArrayList<>();
    for (int i = 0; i < iota10.size(); i++) {
      if (i % 2 != 0) {
        iota10Odd.add(i);
      }
    }
    assertEquals(
        iota10Odd,
        toArrayList(CollectionsPlume.filteredIterator(iota10.iterator(), new OddPredicate())));
  }

  // public static <T extends @Nullable Object> Iterator<T> removeFirstAndLastIterator(Iterator<T>
  // itor)

  // public static final class RemoveFirstAndLastIterator<T> implements Iterator<T>

  @SuppressWarnings("deprecation") // to be made package-private
  @Test
  public void testRemoveFirstAndLastIterator() {

    List<Integer> iota5 = Arrays.asList(0, 1, 2, 3, 4);
    List<Integer> iota5middle = Arrays.asList(1, 2, 3);
    CollectionsPlume.RemoveFirstAndLastIterator<Integer> rfali =
        new CollectionsPlume.RemoveFirstAndLastIterator<Integer>(iota5.iterator());
    ArrayList<Integer> rfali_vector = toArrayList(rfali);
    assertTrue(iota5middle.equals(rfali_vector));
    assertTrue(rfali.getFirst().equals(0));
    assertTrue(rfali.getLast().equals(4));
  }

  // public static <T extends @Nullable Object> List<T> randomElements(Iterator<T> itor, int
  // numElts)

  // public static <T> List<T> randomElements(Iterator<T> itor, int numElts, Random random)

  @Test
  public void testRandomElements() {

    // Tests CollectionsPlume.randomElements(...)

    // Typically, no progress reports are printed, because the loop
    // finishes in well under 1 minute.  Users will see progress reports
    // when this class is slowed down by instrumentation.
    Calendar nextNotification = Calendar.getInstance();
    nextNotification.add(Calendar.MINUTE, 1);
    DateFormat df = new SimpleDateFormat();

    int itorSize = 10;
    int numEltsLimit = 12;
    int tries = shortRun ? 100 : 100000;
    double ratioLimit = .02;
    Random r = new Random(20020311);
    // "i++" instead of "i+=3" here works, but is slow
    for (int i = 1; i < numEltsLimit; i += 3) {
      int[] totals = new int[numEltsLimit];
      for (int j = 0; j < tries; j++) {
        if (j % 100 == 0) {
          Calendar now = Calendar.getInstance();
          if (now.after(nextNotification)) {
            System.out.printf(
                "%s: iteration (%d,%d) out of (%d,%d)%n",
                df.format(nextNotification.getTime()), i, j, numEltsLimit, tries);
            nextNotification.add(Calendar.MINUTE, 1);
          }
        }
        @SuppressWarnings({
          "index:type.arguments.not.inferred",
          "lowerbound:assignment",
          "index:assignment",
          "value"
        }) // The IotaIterator only contains indexes for totals.length, and since chosen's
        // elements are selected randomly from the IotaIterator, all of its elements are
        // @IndexFor
        List<@IndexFor("totals") Integer> chosen =
            CollectionsPlume.randomElements(new IotaIterator(itorSize), i, r);
        for (int m = 0; m < chosen.size(); m++) {
          for (int n = m + 1; n < chosen.size(); n++) {
            if (chosen.get(m).intValue() == chosen.get(n).intValue()) {
              throw new Error("Duplicate at " + m + "," + n);
            }
          }
        }
        for (int k = 0; k < chosen.size(); k++) {
          totals[chosen.get(k).intValue()]++;
        }
      }
      int iTruncated = Math.min(itorSize, i);
      int grandTotal = tries * iTruncated;
      assertTrue(ArraysPlume.sum(totals) == grandTotal); // "Totals = " + ArraysPlume.sum(totals))
      // System.out.print("chosen:\t");
      for (int k = 0; k < numEltsLimit; k++) {
        int thisTotal = totals[k];
        int expected = tries * iTruncated / itorSize;
        double ratio = (double) thisTotal / (double) expected;
        // System.out.print(((k<10) ? " " : "") + k + " " + thisTotal + "\t");
        // System.out.print("\nExp=" + expected + "\tratio=" + ratio + "\t");
        assertTrue(k >= itorSize || (ratio > ratioLimit && ratio < 1 / ratioLimit));
      }
      // System.out.println();
    }
  }

  // //////////////////////////////////////////////////////////////////////
  // Map
  //

  // public static @Nullable Integer incrementMap(Map<K, Integer> m, K key)

  // public static @Nullable Integer incrementMap(Map<K, Integer> m, K key, int count)

  // public static sortedKeySet(Map<K, V> m)

  // public static <K, V> Collection<@KeyFor("#1") K> sortedKeySet(Map<K, V> m, Comparator<K>
  // comparator)

  // public static int mapCapacity(int numElements)

  // public static <T> int mapCapacity(T[] a)

  // public static int mapCapacity(Collection<?> c)

  // public static int mapCapacity(Map<?, ?> m)

  // public static <T> int mapCapacity(T[] a)

  // public static int mapCapacity(Collection<?> c)

  // public static <K, V> Map<K, V> createLruCache(@Positive int size)

  // public static <K, V, M extends @Nullable Map<K, V>> @PolyNull M cloneElements(@PolyNull M orig)

  // public static <K, V, M extends @Nullable Map<K, V>> @PolyNull M cloneValues(@PolyNull M orig)

  //
  // Map to string
  //

  // public static void mapToString(Appendable sb, Map<K, V> m, String linePrefix)

  // public static void mapToStringMultiLine(Appendable sb, Map<K, V> m, String linePrefix)

  // public static void mapMapToStringMultiLine(Appendable sb, String innerHeader, Map<K1, Map<K2,
  // V2>> mapMap, String linePrefix)

  // public static String mapToString(Map<K, V> m)

  // public static String mapToStringMultiLine(Map<K, V> m)

  // public static String mapToStringMultiLine(Map<K, V> m, String linePrefix)

  // public static String mapToStringAndClassMultiLine(Map<K, V> m)

  // public static String mapToStringAndClassMultiLine(Map<K, V> m, String linePrefix)

  // //////////////////////////////////////////////////////////////////////
  // Set
  //

  // public static @Nullable Object getFromSet(Set<? extends @Nullable Object> set, Object key)

  @Test
  public void testGetFromSet() {
    Integer i2 = 2;
    Integer i10 = 10;
    Set<Integer> iota5 = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4));
    assertEquals(i2, CollectionsPlume.getFromSet(iota5, i2));
    assertEquals(null, CollectionsPlume.getFromSet(iota5, i10));
  }

  // public static <T> boolean adjoin(Collection<T> c, T e)

  @Test
  public void testAdjoin() {
    Integer i2 = 2;
    Integer i5 = 5;
    List<Integer> iota5 = Arrays.asList(0, 1, 2, 3, 4);
    List<Integer> iota6 = Arrays.asList(0, 1, 2, 3, 4, 5);
    List<Integer> myList = new ArrayList<>(iota5);
    assertFalse(CollectionsPlume.adjoin(myList, i2));
    assertEquals(iota5, myList);
    assertTrue(CollectionsPlume.adjoin(myList, i5));
    assertEquals(iota6, myList);
  }

  // public static <T> boolean adjoinAll(Collection<T> c, Collection<? extends T> toAdd)

  @Test
  public void testAdjoinAll() {
    List<Integer> iota5 = Arrays.asList(0, 1, 2, 3, 4);
    List<Integer> countdown = Arrays.asList(8, 7, 6, 5, 4, 3);
    List<Integer> result = Arrays.asList(0, 1, 2, 3, 4, 8, 7, 6, 5);
    List<Integer> myList = new ArrayList<>(iota5);
    assertTrue(CollectionsPlume.adjoinAll(myList, countdown));
    assertEquals(result, myList);
  }

  // public static <T> List<T> listUnion(Collection<T> c1, Collection<T> c2)

  @Test
  public void testListUnion() {
    List<Integer> iota5 = Arrays.asList(0, 1, 2, 3, 4);
    List<Integer> countdown = Arrays.asList(8, 7, 6, 5, 4, 3);
    List<Integer> result = Arrays.asList(0, 1, 2, 3, 4, 8, 7, 6, 5);
    List<Integer> myList = CollectionsPlume.listUnion(iota5, countdown);
    assertEquals(result, myList);
  }

  // public static <T> List<T> listIntersection(Collection<T> c1, Collection<T> c2)

  @Test
  public void testListIntersection() {
    List<Integer> iota5 = Arrays.asList(0, 1, 2, 3, 4);
    List<Integer> countdown = Arrays.asList(8, 7, 6, 5, 4, 3);
    List<Integer> result = Arrays.asList(3, 4);
    List<Integer> myList = CollectionsPlume.listIntersection(iota5, countdown);
    assertEquals(result, myList);
  }

  // //////////////////////////////////////////////////////////////////////
  // BitSet
  //

  // public static boolean intersectionCardinalityAtLeast(BitSet a, BitSet b, @NonNegative int i)

  // public static boolean intersectionCardinalityAtLeast(BitSet a, BitSet b, BitSet c, @NonNegative
  // int i)

  // public static int intersectionCardinality(BitSet a, BitSet b)

  // public static int intersectionCardinality(BitSet a, BitSet b, BitSet c)

}
