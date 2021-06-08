package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class CollectionsPlumeTest {

  // If true, do 100 instead of 100000 iterations when testing randomElements.
  // This saves only a little time.  However, it is significant when running
  // under instrumentation such as that of Chicory.
  private static final boolean shortRun = false;

  ///////////////////////////////////////////////////////////////////////////
  /// Helper functions
  ///

  public static <T> ArrayList<T> toArrayList(Iterator<T> itor) {
    ArrayList<T> v = new ArrayList<>();
    while (itor.hasNext()) {
      v.add(itor.next());
    }
    return v;
  }

  @SuppressWarnings("JdkObsolete")
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

  private static class OddFilter implements Filter<Integer> {
    public OddFilter() {}

    @Override
    public boolean accept(Integer i) {
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

  ///////////////////////////////////////////////////////////////////////////
  /// The tests themselves
  ///

  @Test
  public void testListOf() {
    assertEquals(Arrays.asList("a", "b"), CollectionsPlume.listOf("a", "b"));
  }

  @Test
  public void testAppend() {
    assertEquals(
        Arrays.asList("a", "b", "c"), CollectionsPlume.append(Arrays.asList("a", "b"), "c"));
  }

  @Test
  public void testMergedIterator() {

    // public static class EnumerationIterator implements Iterator
    // public static class IteratorEnumeration implements Enumeration

    assertTrue(iota0.equals(toArrayList(iota0.iterator())));
    assert iota0.equals(
        toArrayList(new CollectionsPlume.IteratorEnumeration<Integer>(iota0.iterator())));
    assertTrue(iota10.equals(toArrayList(iota10.iterator())));
    assert iota10.equals(
        toArrayList(new CollectionsPlume.IteratorEnumeration<Integer>(iota10.iterator())));

    // public static class MergedIterator2 implements Iterator {
    assert iota10Twice.equals(
        toArrayList(
            new CollectionsPlume.MergedIterator2<Integer>(iota10.iterator(), iota10.iterator())));
    assert iota10.equals(
        toArrayList(
            new CollectionsPlume.MergedIterator2<Integer>(iota0.iterator(), iota10.iterator())));
    assert iota10.equals(
        toArrayList(
            new CollectionsPlume.MergedIterator2<Integer>(iota10.iterator(), iota0.iterator())));

    // public static class MergedIterator implements Iterator {
    ArrayList<Iterator<Integer>> iota10IteratorThrice = new ArrayList<>();
    iota10IteratorThrice.add(iota10.iterator());
    iota10IteratorThrice.add(iota10.iterator());
    iota10IteratorThrice.add(iota10.iterator());
    assert iota10Thrice.equals(
        toArrayList(new CollectionsPlume.MergedIterator<Integer>(iota10IteratorThrice.iterator())));
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
    assert iota10Twice.equals(
        toArrayList(new CollectionsPlume.MergedIterator<Integer>(iota10IteratorTwice1.iterator())));
    assert iota10Twice.equals(
        toArrayList(new CollectionsPlume.MergedIterator<Integer>(iota10IteratorTwice2.iterator())));
    assert iota10Twice.equals(
        toArrayList(new CollectionsPlume.MergedIterator<Integer>(iota10IteratorTwice3.iterator())));
  }

  @Test
  public void testFilteredIterator() {

    // public static final class FilteredIterator implements Iterator

    ArrayList<Integer> iota10Odd = new ArrayList<>();
    for (int i = 0; i < iota10.size(); i++) {
      if (i % 2 != 0) {
        iota10Odd.add(i);
      }
    }
    assert iota10Odd.equals(
        toArrayList(
            new CollectionsPlume.FilteredIterator<Integer>(iota10.iterator(), new OddFilter())));
  }

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

  List<Integer> l123 = Arrays.asList(1, 2, 3);
  List<Integer> l123123 = Arrays.asList(1, 2, 3, 1, 2, 3);
  List<Integer> l12223 = Arrays.asList(1, 2, 2, 2, 3);
  List<Integer> l1123 = Arrays.asList(1, 1, 2, 3);
  List<Integer> l1233 = Arrays.asList(1, 2, 3, 3);

  Object object1 = new Object();
  Object object2 = new Object();
  Object object3 = new Object();

  List<Object> lo123 = Arrays.asList(object1, object2, object3);
  List<Object> lo123123 = Arrays.asList(object1, object2, object3, object1, object2, object3);
  List<Object> lo12223 = Arrays.asList(object1, object2, object2, object2, object3);
  List<Object> lo1123 = Arrays.asList(object1, object1, object2, object3);
  List<Object> lo1233 = Arrays.asList(object1, object2, object3, object3);

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

  @SuppressWarnings("ArrayEquals")
  @Test
  public void test_withoutDuplicates() {

    // public static List sortList (List l, Comparator c)
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

  @SuppressWarnings("ArrayEquals")
  @Test
  public void test_withoutDuplicatesComparable() {

    // public static List sortList (List l, Comparator c)
    // public static <T> List<T> withoutDuplicates(List<T> l) {

    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l123));
    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l123123));
    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l12223));
    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l1123));
    assertEquals(l123, CollectionsPlume.withoutDuplicatesComparable(l1233));
  }

  @Test
  public void testIsSorted() {
    assertTrue(CollectionsPlume.isSorted(l123));
    assertFalse(CollectionsPlume.isSorted(l123123));
    assertTrue(CollectionsPlume.isSorted(l12223));
    assertTrue(CollectionsPlume.isSorted(l1123));
    assertTrue(CollectionsPlume.isSorted(l1233));
  }

  @Test
  public void testIsSortedNoDuplicates() {
    assertTrue(CollectionsPlume.isSortedNoDuplicates(l123));
    assertFalse(CollectionsPlume.isSortedNoDuplicates(l123123));
    assertFalse(CollectionsPlume.isSortedNoDuplicates(l12223));
    assertFalse(CollectionsPlume.isSortedNoDuplicates(l1123));
    assertFalse(CollectionsPlume.isSortedNoDuplicates(l1233));
  }

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

  @Test
  @SuppressWarnings("lock:methodref.receiver")
  public void testMapList() {
    List<Object> in = Arrays.asList(new Object[] {1, 2, 3});
    List<Object> out = Arrays.asList(new Object[] {"1", "2", "3"});
    assertEquals(out, CollectionsPlume.mapList(Object::toString, in));
  }

  @Test
  @SuppressWarnings("lock:methodref.receiver")
  public void testTransform() {
    List<Object> in = Arrays.asList(new Object[] {1, 2, 3});
    List<Object> out = Arrays.asList(new Object[] {"1", "2", "3"});
    assertEquals(out, CollectionsPlume.transform(in, Object::toString));
  }

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
}
