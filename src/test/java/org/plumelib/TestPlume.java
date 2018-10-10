package org.plumelib.util;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.Test;

// run like this:
//   java org.plumelib.util.TestPlume

// Files yet to test:
// ArraysPlume.java
// ClassFileVersion.java
// CountingPrintWriter.java
// Digest.java
// FileIOException.java
// FuzzyFloat.java
// GraphPlume.java
// Hasher.java
// Intern.java
// ICalAvailable.java
// LimitedSizeIntSet.java
// MathPlume.java
// OrderedPairIterator.java
// StringBuilderDelimited.java
// UtilPlume.java
// WeakHasherMap.java

/** Test code for the plume package. */
@SuppressWarnings({
  "interning", // interning is due to apparent bugs
  "UseCorrectAssertInTests" // I don't see the problem with using `assert`
})
public final class TestPlume {

  // If true, do 100 instead of 100000 iterations when testing randomElements.
  // This saves only a little time.  However, it is significant when running
  // under instrumentation such as that of Chicory.
  static boolean shortRun = false;

  //   public static void main(String[] args) {
  //     testUtilPlume();
  //     testArraysPlume();
  //     testHasher();
  //     testIntern();
  //     testMathPlume();
  //     testOrderedPairIterator();
  //     testPlume();
  //     testWeakHasherMap();
  //     System.out.println("All plume tests succeeded.");
  //   }

  public static void assertArraysEquals(int @Nullable [] a1, int @Nullable [] a2) {
    boolean result = Arrays.equals(a1, a2);
    if (!result) {
      System.out.println("Arrays differ: " + Arrays.toString(a1) + ", " + Arrays.toString(a2));
    }
    assert result;
    //      assert(Arrays.equals(a1, a2),
    //         "Arrays differ: " + ArraysPlume.toString(a1) + ", " + ArraysPlume.toString(a2));
  }

  public static void assertArraysEquals(double[] a1, double[] a2) {
    boolean result = Arrays.equals(a1, a2);
    if (!result) {
      System.out.println(
          "Arrays differ: " + ArraysPlume.toString(a1) + ", " + ArraysPlume.toString(a2));
    }
    assert result;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Utility functions
  ///

  public static Iterator<Integer> intArrayIterator(int[] nums) {
    List<Integer> asList = new ArrayList<>(nums.length);
    for (int i = 0; i < nums.length; i++) {
      asList.add(nums[i]);
    }
    return asList.iterator();
  }

  public static int[] intIteratorArray(Iterator<Integer> itor) {
    ArrayList<Integer> v = new ArrayList<>();
    while (itor.hasNext()) {
      v.add(itor.next());
    }
    int[] a = new int[v.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = v.get(i).intValue();
    }
    return a;
  }

  public static <T> ArrayList<T> toArrayList(Iterator<T> itor) {
    ArrayList<T> v = new ArrayList<>();
    while (itor.hasNext()) {
      v.add(itor.next());
    }
    return v;
  }

  public static <T> ArrayList<T> toArrayList(Enumeration<T> e) {
    ArrayList<T> v = new ArrayList<>();
    while (e.hasMoreElements()) {
      v.add(e.nextElement());
    }
    return v;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Now the actual testing
  ///

  ///////////////////////////////////////////////////////////////////////////
  /// ArraysPlume
  ///

  @Test
  public void testArraysPlume_minAndMax() {

    // public static int min(int[] a)
    assert ArraysPlume.min(new int[] {1, 2, 3}) == 1;
    assert ArraysPlume.min(new int[] {2, 33, 1}) == 1;
    assert ArraysPlume.min(new int[] {3, -2, 1}) == -2;
    assert ArraysPlume.min(new int[] {3}) == 3;

    // public static int max(int[] a)
    assert ArraysPlume.max(new int[] {1, 2, 3}) == 3;
    assert ArraysPlume.max(new int[] {2, 33, 1}) == 33;
    assert ArraysPlume.max(new int[] {3, -2, 1}) == 3;
    assert ArraysPlume.max(new int[] {3}) == 3;

    // public static int[] minAndMax(int[] a)
    assertArraysEquals(ArraysPlume.minAndMax(new int[] {1, 2, 3}), new int[] {1, 3});
    assertArraysEquals(ArraysPlume.minAndMax(new int[] {2, 33, 1}), new int[] {1, 33});
    assertArraysEquals(ArraysPlume.minAndMax(new int[] {3, -2, 1}), new int[] {-2, 3});
    assertArraysEquals(ArraysPlume.minAndMax(new int[] {3}), new int[] {3, 3});
    try {
      ArraysPlume.minAndMax(new int[] {});
      throw new Error("Didn't throw ArrayIndexOutOfBoundsException");
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    try {
      ArraysPlume.minAndMax(new long[] {});
      throw new Error("Didn't throw ArrayIndexOutOfBoundsException");
    } catch (ArrayIndexOutOfBoundsException e) {
    }

    // public static int elementRange(int[] a)
    assert ArraysPlume.elementRange(new int[] {1, 2, 3}) == 2;
    assert ArraysPlume.elementRange(new int[] {2, 33, 1}) == 32;
    assert ArraysPlume.elementRange(new int[] {3, -2, 1}) == 5;
    assert ArraysPlume.elementRange(new int[] {3}) == 0;
  }

  @Test
  public void testArraysPlume_sum() {

    // public static int sum(int[] a)
    assert 0 == ArraysPlume.sum(new int[0]);
    assert 10 == ArraysPlume.sum(new int[] {10});
    assert 10 == ArraysPlume.sum(new int[] {1, 2, 3, 4});

    // public static int sum(int[][] a)
    assert 0 == ArraysPlume.sum(new int[0][0]);
    assert 78 == ArraysPlume.sum(new int[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}});
    assert 68 == ArraysPlume.sum(new int[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 11, 12}});

    // public static double sum(double[] a)
    assert 0 == ArraysPlume.sum(new double[0]);
    assert 3.14 == ArraysPlume.sum(new double[] {3.14});
    assert 8.624 == ArraysPlume.sum(new double[] {3.14, 2.718, -1.234, 4});

    // public static double sum(double[][] a)
    assert 0 == ArraysPlume.sum(new double[0][0]);
    assert 79.5
        == ArraysPlume.sum(new double[][] {{1.1, 2.2, 3.3, 4.4}, {5.5, 6, 7, 8}, {9, 10, 11, 12}});
  }

  /**
   * Like Integer in that it has a constructor that takes an int and creates a non-interned object,
   * so == and equals() differ.
   *
   * <p>The Integer(int) constructor is discouraged because it does not do interning, and later
   * versions of the JDK even deprecate it. One might imagine using this class instead, but the
   * interning methods have hard-coded knowledge of the real Integer class. So, I cannot use this
   * class, and instead I suppress deprecation warnings.
   */
  static class MyInteger {
    int value;

    public MyInteger(int value) {
      this.value = value;
    }

    @Override
    public boolean equals(@GuardSatisfied MyInteger this, @GuardSatisfied @Nullable Object other) {
      if (!(other instanceof MyInteger)) {
        return false;
      }
      MyInteger that = (MyInteger) other;
      return this.value == that.value;
    }

    @Override
    public int hashCode(@GuardSatisfied MyInteger this) {
      return value;
    }
  }

  @SuppressWarnings({"deprecation", "BoxedPrimitiveConstructor"}) // interning tests
  @Test
  public void testArraysPlumeIndexOf() {

    // public static int indexOf(Object[] a, Object elt)
    // public static int indexOfEq(Object[] a, Object elt)
    {
      Integer[] a = new Integer[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = new Integer(i);
      }
      assert ArraysPlume.indexOf(a, new Integer(-1)) == -1;
      assert ArraysPlume.indexOf(a, new Integer(0)) == 0;
      assert ArraysPlume.indexOf(a, new Integer(7)) == 7;
      assert ArraysPlume.indexOf(a, new Integer(9)) == 9;
      assert ArraysPlume.indexOf(a, new Integer(10)) == -1;
      assert ArraysPlume.indexOf(a, new Integer(20)) == -1;
      assert ArraysPlume.indexOf(a, (Object) null) == -1;
      assert ArraysPlume.indexOf(a, (Object) null, 1, 5) == -1;

      assert ArraysPlume.indexOfEq(a, new Integer(-1)) == -1;
      assert ArraysPlume.indexOfEq(a, new Integer(0)) == -1;
      assert ArraysPlume.indexOfEq(a, new Integer(7)) == -1;
      assert ArraysPlume.indexOfEq(a, new Integer(9)) == -1;
      assert ArraysPlume.indexOfEq(a, new Integer(10)) == -1;
      assert ArraysPlume.indexOfEq(a, new Integer(20)) == -1;
      assert ArraysPlume.indexOfEq(a, (Object) null) == -1;
      assert ArraysPlume.indexOfEq(a, (Object) null, 1, 5) == -1;
      assert ArraysPlume.indexOfEq(a, a[0]) == 0;
      assert ArraysPlume.indexOfEq(a, a[7]) == 7;
      assert ArraysPlume.indexOfEq(a, a[9]) == 9;
    }

    // public static int indexOf(List<?> a, Object elt)
    // public static int indexOf(List<?> a, Object elt, int minindex, int indexlimit)
    // public static int indexOfEq(List<?> a, Object elt, int minindex, int indexlimit)
    // public static int indexOfEq(List<?> a, Object elt)
    {
      assert ArraysPlume.indexOf((List<?>) new ArrayList<Object>(), (Object) null) == -1;
      assert ArraysPlume.indexOf((List<?>) new ArrayList<Object>(), (Object) null, 0, -1) == -1;
      assert ArraysPlume.indexOfEq((List<?>) new ArrayList<Object>(), (Object) null) == -1;
      assert ArraysPlume.indexOfEq((List<?>) new ArrayList<Object>(), (Object) null, 0, -1) == -1;
    }

    // public static int indexOf(int[] a, int elt)
    {
      int[] a = new int[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = i;
      }
      assert ArraysPlume.indexOf(a, -1) == -1;
      assert ArraysPlume.indexOf(a, 0) == 0;
      assert ArraysPlume.indexOf(a, 7) == 7;
      assert ArraysPlume.indexOf(a, 9) == 9;
      assert ArraysPlume.indexOf(a, 10) == -1;
      assert ArraysPlume.indexOf(a, 20) == -1;
    }

    // public static int indexOf(boolean[] a, boolean elt)
    {
      boolean[] a = new boolean[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = false;
      }
      assert ArraysPlume.indexOf(a, true) == -1;
      assert ArraysPlume.indexOf(a, false) == 0;
      a[9] = true;
      assert ArraysPlume.indexOf(a, true) == 9;
      assert ArraysPlume.indexOf(a, false) == 0;
      a[7] = true;
      assert ArraysPlume.indexOf(a, true) == 7;
      assert ArraysPlume.indexOf(a, false) == 0;
      a[0] = true;
      assert ArraysPlume.indexOf(a, true) == 0;
      assert ArraysPlume.indexOf(a, false) == 1;
      for (int i = 0; i < a.length; i++) {
        a[i] = true;
      }
      assert ArraysPlume.indexOf(a, true) == 0;
      assert ArraysPlume.indexOf(a, false) == -1;
    }

    // public static int indexOf(Object[] a, Object[] sub)
    // public static int indexOfEq(Object[] a, Object[] sub)
    {
      Integer[] a = new Integer[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = i;
      }
      Integer[] b = new Integer[] {};
      Integer[] c = new Integer[] {a[0], a[1], a[2]};
      Integer[] d = new Integer[] {a[1], a[2]};
      Integer[] e = new Integer[] {a[2], a[3], a[4], a[5]};
      Integer[] f = new Integer[] {a[7], a[8], a[9]};
      Integer[] g = new Integer[] {a[7], new Integer(8), a[9]};
      Integer[] h = new Integer[] {a[7], a[8], a[9], new Integer(10)};
      @SuppressWarnings("nullness") // accommodates poor annotation on indexOf(Object[], Object[])
      Integer[] i = new Integer[] {a[7], a[8], null, a[9], new Integer(10)};
      @SuppressWarnings("nullness") // accommodates poor annotation on indexOf(Object[], Object[])
      Integer[] j = new Integer[] {a[8], null, a[9]};
      Integer[] c2 = new Integer[] {new Integer(0), new Integer(1), new Integer(2)};
      Integer[] d2 = new Integer[] {new Integer(1), new Integer(2)};
      Integer[] e2 = new Integer[] {new Integer(2), new Integer(3), new Integer(4), new Integer(5)};
      Integer[] f2 = new Integer[] {new Integer(7), new Integer(8), new Integer(9)};

      assert ArraysPlume.indexOf(a, b) == 0;
      assert ArraysPlume.indexOfEq(a, b) == 0;
      assert ArraysPlume.indexOf(a, c) == 0;
      assert ArraysPlume.indexOfEq(a, c) == 0;
      assert ArraysPlume.indexOf(a, c2) == 0;
      assert ArraysPlume.indexOfEq(a, c2) == -1;
      assert ArraysPlume.indexOf(a, d) == 1;
      assert ArraysPlume.indexOfEq(a, d) == 1;
      assert ArraysPlume.indexOf(a, d2) == 1;
      assert ArraysPlume.indexOfEq(a, d2) == -1;
      assert ArraysPlume.indexOf(a, e) == 2;
      assert ArraysPlume.indexOfEq(a, e) == 2;
      assert ArraysPlume.indexOf(a, e2) == 2;
      assert ArraysPlume.indexOfEq(a, e2) == -1;
      assert ArraysPlume.indexOf(a, f) == 7;
      assert ArraysPlume.indexOfEq(a, f) == 7;
      assert ArraysPlume.indexOf(a, f2) == 7;
      assert ArraysPlume.indexOfEq(a, f2) == -1;
      assert ArraysPlume.indexOf(a, g) == 7;
      assert ArraysPlume.indexOfEq(a, g) == -1;
      assert ArraysPlume.indexOf(a, h) == -1;
      assert ArraysPlume.indexOfEq(a, h) == -1;
      assert ArraysPlume.indexOf(i, j) == 1;
      assert ArraysPlume.indexOfEq(i, j) == 1;
      assert ArraysPlume.indexOf(a, i) == -1;
      assert ArraysPlume.indexOfEq(a, i) == -1;
    }

    // public static int indexOf(int[] a, int[] sub)
    {
      int[] a = new int[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = i;
      }
      int[] b = new int[] {};
      int[] c = new int[] {a[0], a[1], a[2]};
      int[] d = new int[] {a[1], a[2]};
      int[] e = new int[] {a[2], a[3], a[4], a[5]};
      int[] f = new int[] {a[7], a[8], a[9]};
      int[] g = new int[] {a[7], 22, a[9]};
      int[] h = new int[] {a[7], a[8], a[9], 10};

      assert ArraysPlume.indexOf(a, b) == 0;
      assert ArraysPlume.indexOf(a, c) == 0;
      assert ArraysPlume.indexOf(a, d) == 1;
      assert ArraysPlume.indexOf(a, e) == 2;
      assert ArraysPlume.indexOf(a, f) == 7;
      assert ArraysPlume.indexOf(a, g) == -1;
      assert ArraysPlume.indexOf(a, h) == -1;

      // Tests pulled from actual StackAr data
      int[] origTheArray =
          new int[] {
            1267757, 1267757, 1267757, 1267757, 1267757, 1267757, 1267757, 1267757, 1267757,
            1267757, 1267757, 0, 0, 0, 0, 0, 0, 0, 0, 0
          };

      int[] postTheArray = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      assert ArraysPlume.indexOf(postTheArray, origTheArray) == -1;
      assert ArraysPlume.indexOf(origTheArray, postTheArray) == -1;
    }
  }

  @Test
  public void testArraysPlume_subarray() {

    // public static int indexOf(boolean[] a, boolean[] sub)
    // [I'm punting on this for now; deal with it later...]

    // public static Object[] subarray(Object[] a, int startindex, int length)
    // public static byte[] subarray(byte[] a, int startindex, int length)
    // public static boolean[] subarray(boolean[] a, int startindex, int length)
    // public static char[] subarray(char[] a, int startindex, int length)
    // public static double[] subarray(double[] a, int startindex, int length)
    // public static float[] subarray(float[] a, int startindex, int length)
    // public static int[] subarray(int[] a, int startindex, int length)
    // public static long[] subarray(long[] a, int startindex, int length)
    // public static short[] subarray(short[] a, int startindex, int length)

    // public static boolean isSubarray(Object[] a, Object[] sub, int aOffset)
    // public static boolean isSubarrayEq(Object[] a, Object[] sub, int aOffset)
    // public static boolean isSubarray(int[] a, int[] sub, int aOffset)
    // public static boolean isSubarray(boolean[] a, boolean[] sub, int aOffset)
    // (The subarray tests are missing; I hope that the indexOf(..., array)
    // operations above test them sufficiently.)
  }

  @Test
  public void testArraysPlume_printing() {

    // public static String toString(Object @Nullable [] a)
    // public static String toStringQuoted(Object @Nullable [] a)
    // public static String toString(Object @Nullable [] a, boolean quoted)
    // public static String toString(List<?> a)
    // public static String toStringQuoted(List<?> a)
    // public static String toString(List<?> a, boolean quoted)
    {
      assert ArraysPlume.toString((Object[]) null).equals("null");
      assert ArraysPlume.toStringQuoted((Object[]) null).equals("null");
      assert ArraysPlume.toString((List<?>) null).equals("null");
      assert ArraysPlume.toStringQuoted((List<?>) null).equals("null");
      assert ArraysPlume.toStringQuoted(Arrays.asList(new Object[] {3.14, null, "hello"}))
          .equals("[3.14, null, \"hello\"]");
    }

    // static String toString(int[] a)
    assert Arrays.toString(new int[] {}).equals("[]");
    assert Arrays.toString(new int[] {0}).equals("[0]");
    assert Arrays.toString(new int[] {0, 1, 2}).equals("[0, 1, 2]");
  }

  @Test
  public void testArraysPlume_sorting() {

    // public static boolean sorted(int[] a)
    assert ArraysPlume.sorted(new int[] {0, 1, 2});
    assert ArraysPlume.sorted(new int[] {0, 1, 2, 2, 3, 3});
    assert ArraysPlume.sorted(new int[] {});
    assert ArraysPlume.sorted(new int[] {0});
    assert ArraysPlume.sorted(new int[] {0, 1});
    assert !ArraysPlume.sorted(new int[] {1, 0});
    assert !ArraysPlume.sorted(new int[] {0, 1, 2, 1, 2, 3});

    // public static int noDuplicates(int[] a)
    assert ArraysPlume.noDuplicates(new int[] {1, 2, 3, 5, 4, 0}) == true;
    assert ArraysPlume.noDuplicates(new int[] {1, 2, 3, 5, 4, 100}) == true;
    assert ArraysPlume.noDuplicates(new int[] {2, 2, 3, 5, 4, 0}) == false;
    assert ArraysPlume.noDuplicates(new int[] {1, 2, 3, 5, 4, 1}) == false;
    assert ArraysPlume.noDuplicates(new int[] {1, 2, -3, -5, 4, 0}) == true;
    assert ArraysPlume.noDuplicates(new int[] {1, 2, -2, -2, 4, 100}) == false;
    assert ArraysPlume.noDuplicates(new int[] {}) == true;
    assert ArraysPlume.noDuplicates(new int[] {42}) == true;

    // public static int noDuplicates(long[] a)
    assert ArraysPlume.noDuplicates(new long[] {1, 2, 3, 5, 4, 0}) == true;
    assert ArraysPlume.noDuplicates(new long[] {1, 2, 3, 5, 4, 100}) == true;
    assert ArraysPlume.noDuplicates(new long[] {2, 2, 3, 5, 4, 0}) == false;
    assert ArraysPlume.noDuplicates(new long[] {1, 2, 3, 5, 4, 1}) == false;
    assert ArraysPlume.noDuplicates(new long[] {1, 2, -3, -5, 4, 0}) == true;
    assert ArraysPlume.noDuplicates(new long[] {1, 2, -2, -2, 4, 100}) == false;
    assert ArraysPlume.noDuplicates(new long[] {}) == true;
    assert ArraysPlume.noDuplicates(new long[] {42}) == true;

    // public static int noDuplicates(double[] a)
    assert ArraysPlume.noDuplicates(new double[] {1, 2, 3, 5, 4, 0}) == true;
    assert ArraysPlume.noDuplicates(new double[] {1, 2, 3, 5, 4, 100}) == true;
    assert ArraysPlume.noDuplicates(new double[] {2, 2, 3, 5, 4, 0}) == false;
    assert ArraysPlume.noDuplicates(new double[] {1, 2, 3, 5, 4, 1}) == false;
    assert ArraysPlume.noDuplicates(new double[] {1., 1.001, -3, -5, 4, 0}) == true;
    assert ArraysPlume.noDuplicates(new double[] {1., 2, -2.00, -2, 4, 100}) == false;
    assert ArraysPlume.noDuplicates(new double[] {}) == true;
    assert ArraysPlume.noDuplicates(new double[] {42}) == true;

    // public static int noDuplicates(String[] a)
    assert ArraysPlume.noDuplicates(new String[] {"1", "2", "3", "5", "4", "0"}) == true;
    assert ArraysPlume.noDuplicates(new String[] {"A", "a", "foo", "Foo", ""}) == true;
    assert ArraysPlume.noDuplicates(new String[] {" ", " "}) == false;
    assert ArraysPlume.noDuplicates(new String[] {"  ", " "}) == true;

    // public static boolean fnIsPermutation(int[] a)
    assert ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 3}) == true;
    assert ArraysPlume.fnIsPermutation(new int[] {1, 2, 3, 0}) == true;
    assert ArraysPlume.fnIsPermutation(new int[] {3, 2, 1, 0}) == true;
    assert ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 2}) == false;
    assert ArraysPlume.fnIsPermutation(new int[] {0, -1, 2, 3}) == false;
    assert ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 4}) == false;
    assert ArraysPlume.fnIsPermutation(new int[] {0, 0, 0, 0}) == false;

    // public static boolean fnIsTotal(int[] a)
    assert ArraysPlume.fnIsTotal(new int[] {0, 1, 2, 3}) == true;
    assert ArraysPlume.fnIsTotal(new int[] {1, 2, 3, 0}) == true;
    assert ArraysPlume.fnIsTotal(new int[] {3, 2, 1, 0}) == true;
    assert ArraysPlume.fnIsTotal(new int[] {0, 1, 2, 2}) == true;
    assert ArraysPlume.fnIsTotal(new int[] {-1, 0, 2, 3}) == false;
    assert ArraysPlume.fnIsTotal(new int[] {0, -1, 2, 3}) == false;
    assert ArraysPlume.fnIsTotal(new int[] {0, -2, 1, 3}) == true; // weird
    assert ArraysPlume.fnIsTotal(new int[] {0, 2, 3, -1}) == false;
    assert ArraysPlume.fnIsTotal(new int[] {0, 1, 2, 4}) == true;
    assert ArraysPlume.fnIsTotal(new int[] {0, 0, 0, 0}) == true;
  }

  @SuppressWarnings("index") // https://github.com/kelloggm/checker-framework/issues/147
  @Test
  public void testArraysPlumeFunctions() {

    // public static int[] fnIdentity(int length)
    assertArraysEquals(ArraysPlume.fnIdentity(0), new int[] {});
    assertArraysEquals(ArraysPlume.fnIdentity(1), new int[] {0});
    assertArraysEquals(ArraysPlume.fnIdentity(2), new int[] {0, 1});
    assertArraysEquals(ArraysPlume.fnIdentity(3), new int[] {0, 1, 2});

    // public static int[] fnInversePermutation(int[] a)
    assertArraysEquals(
        ArraysPlume.fnInversePermutation(new int[] {0, 1, 2, 3}), new int[] {0, 1, 2, 3});
    assertArraysEquals(
        ArraysPlume.fnInversePermutation(new int[] {1, 2, 3, 0}), new int[] {3, 0, 1, 2});
    assertArraysEquals(
        ArraysPlume.fnInversePermutation(new int[] {3, 2, 1, 0}), new int[] {3, 2, 1, 0});

    // public static int[] fnInverse(int[] a, int arange)
    assertArraysEquals(ArraysPlume.fnInverse(new int[] {0, 1, 2, 3}, 4), new int[] {0, 1, 2, 3});
    assertArraysEquals(ArraysPlume.fnInverse(new int[] {1, 2, 3, 0}, 4), new int[] {3, 0, 1, 2});
    assertArraysEquals(ArraysPlume.fnInverse(new int[] {3, 2, 1, 0}, 4), new int[] {3, 2, 1, 0});
    try {
      ArraysPlume.fnInverse(new int[] {1, 0, 3, 0}, 4);
      throw new Error();
    } catch (UnsupportedOperationException e) {
      assert e.getMessage() != null && e.getMessage().equals("Not invertible; a[1]=0 and a[3]=0");
    }
    assertArraysEquals(ArraysPlume.fnInverse(new int[] {5}, 6), new int[] {-1, -1, -1, -1, -1, 0});
    assertArraysEquals(
        ArraysPlume.fnInverse(new int[] {1, 2, 3, 5}, 6), new int[] {-1, 0, 1, 2, -1, 3});

    try {
      assertArraysEquals(
          ArraysPlume.fnInverse(new int[] {100, 101, 102, 103}, 4), new int[] {40, 41, 42, 43});
      throw new Error();
    } catch (IllegalArgumentException e) {
      assert e.getMessage() != null && e.getMessage().equals("Bad range value: a[0]=100");
    }

    // public static int[] fnCompose(int[] a, int[] b)
    {
      int[] a1 = new int[] {0, 1, 2, 3};
      int[] a2 = new int[] {1, 2, 3, 0};
      int[] a3 = new int[] {3, 2, 1, 0};
      int[] a4 = new int[] {0, 1, 0, 3};
      int[] a5 = new int[] {0, 5, 2, 1};
      int[] a7 = new int[] {0};
      int[] a8 = new int[] {5};
      int[] a9 = new int[] {1, 2, 3, 5};
      int[] a10 = new int[] {1, 2, 3, 5, -1, -1};

      assertArraysEquals(ArraysPlume.fnCompose(a1, a1), a1);
      assertArraysEquals(ArraysPlume.fnCompose(a2, a2), new int[] {2, 3, 0, 1});
      assertArraysEquals(ArraysPlume.fnCompose(a3, a3), a1);
      assertArraysEquals(ArraysPlume.fnCompose(a4, a5), new int[] {0, 5, 0, 1});
      assertArraysEquals(ArraysPlume.fnCompose(a7, a8), new int[] {5});
      assertArraysEquals(ArraysPlume.fnCompose(a9, a10), new int[] {2, 3, 5, -1});
    }
  }

  @Test
  public void testArraysPlume_set_operations() {

    // public static boolean isSubset(long[] smaller, long[] bigger)
    // public static boolean isSubset(double[] smaller, double[] bigger)
    // public static boolean isSubset(String[] smaller, String[] bigger)

    {
      double[] f1 = new double[10];
      double[] f2 = new double[20];

      for (int j = 0; j < f2.length; j++) {
        f2[j] = j;
      }
      for (int i = 0; i < f2.length - f1.length; i++) {

        // fill up f1 with elements of f2
        for (int j = 0; j < f1.length; j++) {
          f1[j] = f2[i + j];
        }

        f1[5] = f2[i];

        double[] f1Copy = f1.clone();
        double[] f2Copy = f2.clone();

        assert ArraysPlume.isSubset(f1, f2);
        assertArraysEquals(f1, f1Copy);
        assertArraysEquals(f2, f2Copy);
      }

      double[] a1 = new double[] {1, 5, 10};
      double[] a2 = new double[] {};
      double[] a3 = new double[] {1};
      double[] a4 = new double[] {10};
      double[] a5 = new double[] {1, 10, 15, 20};
      double[] a6 = new double[] {10, 10, 10, 10, 10, 1};

      assert ArraysPlume.isSubset(a2, a1);
      assert !ArraysPlume.isSubset(a1, a2);
      assert !ArraysPlume.isSubset(a1, a5);
      assert ArraysPlume.isSubset(a3, a1);
      assert ArraysPlume.isSubset(a4, a1);
      assert ArraysPlume.isSubset(a6, a1);
      assert !ArraysPlume.isSubset(a1, a6);
    }
  }

  @Test
  public void testArraysPlume_comparators() {

    // public static class IntArrayComparatorLexical implements Comparator
    // public static class IntArrayComparatorLengthFirst implements Comparator
    {
      Comparator<int[]> iacl = new ArraysPlume.IntArrayComparatorLexical();
      Comparator<int[]> iaclf = new ArraysPlume.IntArrayComparatorLengthFirst();

      int[] a0 = new int[] {};
      int[] a1 = new int[] {};
      int[] a2 = new int[] {0, 1, 2, 3};
      int[] a3 = new int[] {0, 1, 2, 3, 0};
      int[] a4 = new int[] {0, 1, 2, 3, 4};
      int[] a5 = new int[] {0, 1, 2, 3, 4};
      int[] a6 = new int[] {0, 1, 5, 3, 4};
      int[] a7 = new int[] {1, 2, 3, 4};
      int[] a8 = new int[] {-5};
      int[] a9 = new int[] {Integer.MAX_VALUE};
      int[] a10 = new int[] {Integer.MIN_VALUE};

      assert iacl.compare(a0, a1) == 0;
      assert iaclf.compare(a0, a1) == 0;
      assert iacl.compare(a1, a0) == 0;
      assert iaclf.compare(a1, a0) == 0;
      assert iacl.compare(a1, a2) < 0;
      assert iaclf.compare(a1, a2) < 0;
      assert iacl.compare(a2, a1) > 0;
      assert iaclf.compare(a2, a1) > 0;
      assert iacl.compare(a2, a3) < 0;
      assert iaclf.compare(a2, a3) < 0;
      assert iacl.compare(a3, a2) > 0;
      assert iaclf.compare(a3, a2) > 0;
      assert iacl.compare(a3, a4) < 0;
      assert iaclf.compare(a3, a4) < 0;
      assert iacl.compare(a4, a3) > 0;
      assert iaclf.compare(a4, a3) > 0;
      assert iacl.compare(a4, a5) == 0;
      assert iaclf.compare(a4, a5) == 0;
      assert iacl.compare(a5, a4) == 0;
      assert iaclf.compare(a5, a4) == 0;
      assert iacl.compare(a5, a6) < 0;
      assert iaclf.compare(a5, a6) < 0;
      assert iacl.compare(a6, a5) > 0;
      assert iaclf.compare(a6, a5) > 0;
      assert iacl.compare(a6, a7) < 0;
      assert iaclf.compare(a6, a7) > 0;
      assert iacl.compare(a7, a6) > 0;
      assert iaclf.compare(a7, a6) < 0;
      assert iacl.compare(a1, a4) < 0;
      assert iaclf.compare(a1, a4) < 0;
      assert iacl.compare(a4, a1) > 0;
      assert iaclf.compare(a4, a1) > 0;
      assert iacl.compare(a2, a4) < 0;
      assert iaclf.compare(a2, a4) < 0;
      assert iacl.compare(a4, a2) > 0;
      assert iaclf.compare(a4, a2) > 0;
      assert iacl.compare(a6, a4) > 0;
      assert iaclf.compare(a6, a4) > 0;
      assert iacl.compare(a4, a6) < 0;
      assert iaclf.compare(a4, a6) < 0;
      assert iacl.compare(a7, a4) > 0;
      assert iaclf.compare(a7, a4) < 0;
      assert iacl.compare(a4, a7) < 0;
      assert iaclf.compare(a4, a7) > 0;
      assert iacl.compare(a8, a9) < 0;
      assert iaclf.compare(a8, a9) < 0;
      assert iacl.compare(a10, a7) < 0;
    }

    // public static class LongArrayComparatorLexical implements Comparator
    // public static class LongArrayComparatorLengthFirst implements Comparator
    {
      Comparator<long[]> lacl = new ArraysPlume.LongArrayComparatorLexical();
      Comparator<long[]> laclf = new ArraysPlume.LongArrayComparatorLengthFirst();
      long[] a0 = new long[] {};
      long[] a1 = new long[] {};
      long[] a2 = new long[] {0, 1, 2, 3};
      long[] a3 = new long[] {0, 1, 2, 3, 0};
      long[] a4 = new long[] {0, 1, 2, 3, 4};
      long[] a5 = new long[] {0, 1, 2, 3, 4};
      long[] a6 = new long[] {0, 1, 5, 3, 4};
      long[] a7 = new long[] {1, 2, 3, 4};
      long[] a8 = new long[] {-5};
      long[] a9 = new long[] {Long.MAX_VALUE};
      long[] a10 = new long[] {Long.MIN_VALUE};

      assert lacl.compare(a0, a1) == 0;
      assert laclf.compare(a0, a1) == 0;
      assert lacl.compare(a1, a0) == 0;
      assert laclf.compare(a1, a0) == 0;
      assert lacl.compare(a1, a2) < 0;
      assert laclf.compare(a1, a2) < 0;
      assert lacl.compare(a2, a1) > 0;
      assert laclf.compare(a2, a1) > 0;
      assert lacl.compare(a2, a3) < 0;
      assert laclf.compare(a2, a3) < 0;
      assert lacl.compare(a3, a2) > 0;
      assert laclf.compare(a3, a2) > 0;
      assert lacl.compare(a3, a4) < 0;
      assert laclf.compare(a3, a4) < 0;
      assert lacl.compare(a4, a3) > 0;
      assert laclf.compare(a4, a3) > 0;
      assert lacl.compare(a4, a5) == 0;
      assert laclf.compare(a4, a5) == 0;
      assert lacl.compare(a5, a4) == 0;
      assert laclf.compare(a5, a4) == 0;
      assert lacl.compare(a5, a6) < 0;
      assert laclf.compare(a5, a6) < 0;
      assert lacl.compare(a6, a5) > 0;
      assert laclf.compare(a6, a5) > 0;
      assert lacl.compare(a6, a7) < 0;
      assert laclf.compare(a6, a7) > 0;
      assert lacl.compare(a7, a6) > 0;
      assert laclf.compare(a7, a6) < 0;
      assert lacl.compare(a1, a4) < 0;
      assert laclf.compare(a1, a4) < 0;
      assert lacl.compare(a4, a1) > 0;
      assert laclf.compare(a4, a1) > 0;
      assert lacl.compare(a2, a4) < 0;
      assert laclf.compare(a2, a4) < 0;
      assert lacl.compare(a4, a2) > 0;
      assert laclf.compare(a4, a2) > 0;
      assert lacl.compare(a6, a4) > 0;
      assert laclf.compare(a6, a4) > 0;
      assert lacl.compare(a4, a6) < 0;
      assert laclf.compare(a4, a6) < 0;
      assert lacl.compare(a7, a4) > 0;
      assert laclf.compare(a7, a4) < 0;
      assert lacl.compare(a4, a7) < 0;
      assert laclf.compare(a4, a7) > 0;
      assert lacl.compare(a8, a9) < 0;
      assert laclf.compare(a8, a9) < 0;
      assert lacl.compare(a10, a7) < 0;
    }

    // public static class DoubleArrayComparatorLexical implements Comparator
    {
      Comparator<double[]> dacl = new ArraysPlume.DoubleArrayComparatorLexical();
      double[] a0 = new double[] {};
      double[] a1 = new double[] {};
      double[] a2 = new double[] {0, 1, 2, 3};
      double[] a3 = new double[] {0, 1, 2, 3, 0};
      double[] a4 = new double[] {0, 1, 2, 3, 4};
      double[] a5 = new double[] {0, 1, 2, 3, 4};
      double[] a6 = new double[] {0, 1, 5, 3, 4};
      double[] a7 = new double[] {1, 2, 3, 4};
      double[] a8 = new double[] {0.005};
      double[] a9 = new double[] {0.004};
      double[] a10 = new double[] {-0.005};
      double[] a11 = new double[] {-0.004};
      double[] a12 = new double[] {10.0 * Integer.MAX_VALUE};
      double[] a13 = new double[] {10.0 * Integer.MIN_VALUE};

      assert dacl.compare(a0, a1) == 0;
      assert dacl.compare(a1, a0) == 0;
      assert dacl.compare(a1, a2) < 0;
      assert dacl.compare(a2, a1) > 0;
      assert dacl.compare(a2, a3) < 0;
      assert dacl.compare(a3, a2) > 0;
      assert dacl.compare(a3, a4) < 0;
      assert dacl.compare(a4, a3) > 0;
      assert dacl.compare(a4, a5) == 0;
      assert dacl.compare(a5, a4) == 0;
      assert dacl.compare(a5, a6) < 0;
      assert dacl.compare(a6, a5) > 0;
      assert dacl.compare(a6, a7) < 0;
      assert dacl.compare(a7, a6) > 0;
      assert dacl.compare(a1, a4) < 0;
      assert dacl.compare(a4, a1) > 0;
      assert dacl.compare(a2, a4) < 0;
      assert dacl.compare(a4, a2) > 0;
      assert dacl.compare(a6, a4) > 0;
      assert dacl.compare(a4, a6) < 0;
      assert dacl.compare(a7, a4) > 0;
      assert dacl.compare(a4, a7) < 0;

      // Test the comparisons on small/large numbers
      assert dacl.compare(a8, a9) > 0;
      assert dacl.compare(a10, a11) < 0;
      assert dacl.compare(a11, a12) < 0;
      assert dacl.compare(a12, a13) > 0;
      assert dacl.compare(a13, a11) < 0;
    }

    // public static class ObjectArrayComparatorLexical implements Comparator
    // public static class ObjectArrayComparatorLengthFirst implements Comparator

    // public static final class ComparableArrayComparatorLexical implements Comparator
    // public static final class ComparableArrayComparatorLengthFirst implements Comparator
    {
      Comparator<String[]> cacl = new ArraysPlume.ComparableArrayComparatorLexical<String>();
      Comparator<String[]> caclf = new ArraysPlume.ComparableArrayComparatorLengthFirst<String>();
      String[] a0 = new String[] {};
      String[] a1 = new String[] {};
      String[] a2 = new String[] {"0", "1", "2", "3"};
      String[] a3 = new String[] {"0", "1", "2", "3", "0"};
      String[] a4 = new String[] {"0", "1", "2", "3", "4"};
      String[] a5 = new String[] {"0", "1", "2", "3", "4"};
      String[] a6 = new String[] {"0", "1", "5", "3", "4"};
      String[] a7 = new String[] {"1", "2", "3", "4"};
      @SuppressWarnings(
          "nullness") // accommodates poor annotation on ComparableArrayComparatorLexical.compare()
      // and ComparableArrayComparatorLengthFirst.compare()
      String[] a8 = new String[] {"0", "1", null, "3", "4"};

      assert cacl.compare(a0, a1) == 0;
      assert caclf.compare(a0, a1) == 0;
      assert cacl.compare(a1, a0) == 0;
      assert caclf.compare(a1, a0) == 0;
      assert cacl.compare(a1, a2) < 0;
      assert caclf.compare(a1, a2) < 0;
      assert cacl.compare(a2, a1) > 0;
      assert caclf.compare(a2, a1) > 0;
      assert cacl.compare(a2, a3) < 0;
      assert caclf.compare(a2, a3) < 0;
      assert cacl.compare(a3, a2) > 0;
      assert caclf.compare(a3, a2) > 0;
      assert cacl.compare(a3, a4) < 0;
      assert caclf.compare(a3, a4) < 0;
      assert cacl.compare(a4, a3) > 0;
      assert caclf.compare(a4, a3) > 0;
      assert cacl.compare(a4, a5) == 0;
      assert caclf.compare(a4, a5) == 0;
      assert cacl.compare(a5, a4) == 0;
      assert caclf.compare(a5, a4) == 0;
      assert cacl.compare(a5, a6) < 0;
      assert caclf.compare(a5, a6) < 0;
      assert cacl.compare(a6, a5) > 0;
      assert caclf.compare(a6, a5) > 0;
      assert cacl.compare(a6, a7) < 0;
      assert caclf.compare(a6, a7) > 0;
      assert cacl.compare(a7, a6) > 0;
      assert caclf.compare(a7, a6) < 0;
      assert cacl.compare(a1, a4) < 0;
      assert caclf.compare(a1, a4) < 0;
      assert cacl.compare(a4, a1) > 0;
      assert caclf.compare(a4, a1) > 0;
      assert cacl.compare(a2, a4) < 0;
      assert caclf.compare(a2, a4) < 0;
      assert cacl.compare(a4, a2) > 0;
      assert caclf.compare(a4, a2) > 0;
      assert cacl.compare(a6, a4) > 0;
      assert caclf.compare(a6, a4) > 0;
      assert cacl.compare(a4, a6) < 0;
      assert caclf.compare(a4, a6) < 0;
      assert cacl.compare(a7, a4) > 0;
      assert caclf.compare(a7, a4) < 0;
      assert cacl.compare(a8, a1) > 0;
      assert caclf.compare(a8, a1) > 0;
      assert cacl.compare(a1, a8) < 0;
      assert caclf.compare(a1, a8) < 0;
      assert cacl.compare(a8, a2) < 0;
      assert caclf.compare(a8, a2) > 0;
      assert cacl.compare(a2, a8) > 0;
      assert caclf.compare(a2, a8) < 0;
      assert cacl.compare(a8, a3) < 0;
      assert caclf.compare(a8, a3) < 0;
      assert cacl.compare(a3, a8) > 0;
      assert caclf.compare(a3, a8) > 0;
    }
  }

  @Test
  public void testArraysPlume_nullness() {

    // public static boolean anyNull(Object[] a)
    {
      Object o = new Object();
      assert ArraysPlume.anyNull(new Object[] {}) == false;
      assert ArraysPlume.anyNull(new Object[] {null}) == true;
      assert ArraysPlume.anyNull(new Object[] {null, null}) == true;
      assert ArraysPlume.anyNull(new Object[] {o}) == false;
      assert ArraysPlume.anyNull(new Object[] {o, o}) == false;
      assert ArraysPlume.anyNull(new Object[] {o, null, null}) == true;
      assert ArraysPlume.anyNull(new Object[] {null, o, null}) == true;
      assert ArraysPlume.anyNull(new Object[] {o, null, o}) == true;
      assert ArraysPlume.anyNull(new Object[] {null, o, o}) == true;
      assert ArraysPlume.anyNull(new Object[][] {}) == false;
      assert ArraysPlume.anyNull(new Object[][] {null}) == true;
      // Extraneous @Nullable on the following lines are due to https://tinyurl.com/cfissue/599
      assert ArraysPlume.anyNull(new @Nullable Object[][] {new Object[] {null}}) == false;
      assert ArraysPlume.anyNull(new @Nullable Object[][] {new Object[] {null}, null}) == true;
      assert ArraysPlume.anyNull(new @Nullable Object[][] {new Object[] {null}, new Object[] {o}})
          == false;
    }

    // public static boolean allNull(Object[] a)
    {
      Object o = new Object();
      assert ArraysPlume.allNull(new Object[] {}) == true;
      assert ArraysPlume.allNull(new Object[] {null}) == true;
      assert ArraysPlume.allNull(new Object[] {null, null}) == true;
      assert ArraysPlume.allNull(new Object[] {o}) == false;
      assert ArraysPlume.allNull(new Object[] {o, o}) == false;
      assert ArraysPlume.allNull(new Object[] {o, null, null}) == false;
      assert ArraysPlume.allNull(new Object[] {null, o, null}) == false;
      assert ArraysPlume.allNull(new Object[] {o, null, o}) == false;
      assert ArraysPlume.allNull(new Object[] {null, o, o}) == false;
      assert ArraysPlume.allNull(new Object[][] {}) == true;
      assert ArraysPlume.allNull(new Object[][] {null}) == true;
      assert ArraysPlume.allNull(new Object[][] {null, null}) == true;
      assert ArraysPlume.allNull(new @Nullable Object[][] {new Object[] {null}}) == false;
      assert ArraysPlume.allNull(new @Nullable Object[][] {new Object[] {null}, null}) == false;
      assert ArraysPlume.allNull(new @Nullable Object[][] {new Object[] {null}, new Object[] {o}})
          == false;
    }
  }

  /** Return true if the toString of each element in elts equals the corresponding string. */
  private static boolean equalElementStrings(List<?> elts, List<String> strings) {
    if (elts.size() != strings.size()) {
      return false;
    }
    for (int i = 0; i < elts.size(); i++) {
      if (!String.valueOf(elts.get(i)).equals(strings.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Test
  public void testArraysPlume_partitioning() {

    assert equalElementStrings(
        ArraysPlume.partitionInto(Arrays.asList("a"), 1), Arrays.asList("[[a]]"));
    assert equalElementStrings(
        ArraysPlume.partitionInto(Arrays.asList("a", "b"), 1), Arrays.asList("[[a, b]]"));
    assert equalElementStrings(
        ArraysPlume.partitionInto(Arrays.asList("a", "b"), 2), Arrays.asList("[[a], [b]]"));
    assert equalElementStrings(
        ArraysPlume.partitionInto(Arrays.asList("a", "b", "c"), 1), Arrays.asList("[[a, b, c]]"));
    assert equalElementStrings(
        ArraysPlume.partitionInto(Arrays.asList("a", "b", "c"), 2),
        Arrays.asList("[[a, b], [c]]", "[[a, c], [b]]", "[[a], [b, c]]"));
    assert equalElementStrings(
        ArraysPlume.partitionInto(Arrays.asList("a", "b", "c", "d", "e"), 2),
        Arrays.asList(
            "[[a, b, c, d], [e]]",
            "[[a, b, c, e], [d]]",
            "[[a, b, c], [d, e]]",
            "[[a, b, d, e], [c]]",
            "[[a, b, e], [c, d]]",
            "[[a, b, d], [c, e]]",
            "[[a, b], [c, d, e]]",
            "[[a, c, d, e], [b]]",
            "[[a, d, e], [b, c]]",
            "[[a, c, e], [b, d]]",
            "[[a, e], [b, c, d]]",
            "[[a, c, d], [b, e]]",
            "[[a, d], [b, c, e]]",
            "[[a, c], [b, d, e]]",
            "[[a], [b, c, d, e]]"));
  }

  @Test
  public void testArraysPlume_concat() {
    Date[] da1 = new Date[] {new Date()};
    Date[] da2 = new Date[] {new Date()};
    System.out.println("concat result: " + ArraysPlume.concat(da1, da2));
    Date[] da3 = ArraysPlume.concat(da1, da2);
  }

  // This cannot be static because it instantiates an inner class.
  @Test
  public void testHasher() {

    /// To check (maybe some of these are done already).
    /// All of these methods are in Intern; should the tests appear in
    /// testIntern() or here?
    // public static void internStrings(String[] a)
    // public static boolean isInterned(Object value)
    // public static int numIntegers()
    // public static int numIntArrays()
    // public static int numDoubles()
    // public static int numDoubleArrays()
    // public static int numObjectArrays()
    // public static Iterator integers()
    // public static Iterator intArrays()
    // public static Iterator doubles()
    // public static Iterator doubleArrays()
    // public static Iterator objectArrays()
    // public static Integer intern(Integer a)
    // public static Integer internedInteger(int i)
    // public static Integer internedInteger(String s)
    // public static int[] intern(int[] a)
    // public static Double intern(Double a)
    // public static Double internedDouble(int i)
    // public static Double internedDouble(String s)
    // public static double[] intern(double[] a)
    // public static Object[] intern(Object[] a)

    // private static class IntArrayHasher implements Hasher
    // private static class ObjectArrayHasher implements Hasher
    // public static int[] intern(int[] a)
    // public static Object[] intern(Object[] a)

    class InternTest {
      // javadoc won't let this be static.
      void test(boolean random) {
        int size1 = (random ? 100 : 1);
        int size2 = (random ? 10 : 1);

        Random randomGen = new Random();

        int @ArrayLen(100) [] @ArrayLen(10) [] arrays = new int[100] @ArrayLen(10) [];
        for (int i = 0; i < arrays.length; i++) {
          int[] a = new int[10];
          for (int j = 0; j < a.length; j++) {
            if (random) {
              a[j] = randomGen.nextInt(1000);
            } else {
              a[j] = j;
            }
          }
          arrays[i] = a;
          // System.out.println(ArraysPlume.toString(a));
          // Sadly, this is required to get the last array to be
          // garbage-collected with Jikes 1.03 and JDK 1.2.2.
          a = null;
        }
        System.gc();
        if (Intern.numIntArrays() != 0) {
          throw new Error(" expected 0 int arrays at start, found " + Intern.numIntArrays());
        }
        for (int i = 0; i < arrays.length; i++) {
          Intern.intern(arrays[i]);
        }
        if (Intern.numIntArrays() != size1) {
          throw new Error("Expected " + size1 + ", got " + Intern.numIntArrays() + " int arrays");
        }
        System.gc();
        if (Intern.numIntArrays() != size1) {
          throw new Error();
        }
        for (int i = 10; i < arrays.length; i++) {
          @SuppressWarnings("nullness") // test code: permit garbage collection to test interning
          int @NonNull [] reset_value = null;
          arrays[i] = reset_value;
        }
        System.gc();
        if (Intern.numIntArrays() != size2) {
          if (Intern.numIntArrays() < size2 + 10) {
            System.out.println(
                "Is JIT disabled?  Size should have been "
                    + size2
                    + ", actually was "
                    + Intern.numIntArrays());
          } else {
            System.out.println("================");
            for (int i = 0; i < arrays.length; i++) {
              System.out.println(Arrays.toString(arrays[i]));
            }
            System.out.println("================");
            for (Iterator<int[]> itor = Intern.intArrays(); itor.hasNext(); ) {
              System.out.println(Arrays.toString(itor.next()));
            }
            String message =
                ("Size should have been " + size2 + ", actually was " + Intern.numIntArrays());
            System.out.println(message);
            throw new Error(message);
          }
        }
      }
    }

    InternTest intern = new InternTest();
    intern.test(true);
    intern.test(false);
  }

  @Test
  public void testIntern() {
    Integer i = Intern.internedInteger("1234");
    assert Intern.isInterned(i);
    assert i.intValue() == 1234;
    i = Intern.internedInteger("0x12ab");
    assert Intern.isInterned(i);
    assert i.intValue() == 0x12ab;

    Long l = Intern.internedLong("12345678");
    assert Intern.isInterned(l);
    assert l.intValue() == 12345678;
    l = Intern.internedLong("0x1234abcd");
    assert Intern.isInterned(l);
    assert l.intValue() == 0x1234abcd;
  }

  // Tests the method "Object intern(Object)" in Intern.java
  @SuppressWarnings({"deprecation", "BoxedPrimitiveConstructor"}) // interning test
  @Test
  public void testInternObject() {
    Object nIntern = Intern.intern((@Nullable Object) null);
    assert nIntern == null;

    String sOrig = new String("foo");
    String sIntern = Intern.intern(sOrig);
    Object sObjIntern = Intern.intern((Object) sOrig);
    assert sIntern == sObjIntern;
    Object sOtherIntern = Intern.intern(new String("foo"));
    assert sIntern == sOtherIntern;

    String[] saOrig = new String[] {"foo", "bar"};
    String[] saIntern = Intern.intern(saOrig);
    Object saObjIntern = Intern.intern((Object) saOrig);
    assert saIntern == saObjIntern;
    Object saOtherIntern = Intern.intern(new String[] {"foo", "bar"});
    assert saIntern == saOtherIntern;

    Integer iOrig = new Integer(1);
    Integer iIntern = Intern.intern(iOrig);
    Object iObjIntern = Intern.intern((Object) iOrig);
    assert iIntern == iObjIntern;
    Object iOtherIntern = Intern.intern((Object) new Integer(1));
    assert iIntern == iOtherIntern;

    Long lOrig = new Long(12345678901234L);
    Long lIntern = Intern.intern(lOrig);
    Object lObjIntern = Intern.intern((Object) lOrig);
    assert lIntern == lObjIntern;
    Object lOtherIntern = Intern.intern((Object) new Long(12345678901234L));
    assert lIntern == lOtherIntern;

    int[] iaOrig = new int[] {1, 2, 3};
    int[] iaIntern = Intern.intern(iaOrig);
    Object iaObjIntern = Intern.intern((Object) iaOrig);
    assert iaIntern == iaObjIntern;
    Object iaOtherIntern = Intern.intern((Object) new int[] {1, 2, 3});
    assert iaIntern == iaOtherIntern;

    long[] laOrig = new long[] {12345678901234L, 98765432109876L};
    long[] laIntern = Intern.intern(laOrig);
    Object laObjIntern = Intern.intern((Object) laOrig);
    assert laIntern == laObjIntern;
    Object laOtherIntern = Intern.intern((Object) new long[] {12345678901234L, 98765432109876L});
    assert laIntern == laOtherIntern;

    // Need to test positive and negative zeros, infinities.

    Double dOrig = new Double(3.14);
    Double dIntern = Intern.intern(dOrig);
    Object dObjIntern = Intern.intern((Object) dOrig);
    assert dIntern == dObjIntern;
    Object dOtherIntern = Intern.intern((Object) dOrig);
    assert dIntern == dOtherIntern;

    Double dnOrig = new Double(Double.NaN);
    Double dnIntern = Intern.intern(dnOrig);
    Object dnObjIntern = Intern.intern((Object) dnOrig);
    assert dnIntern == dnObjIntern;
    Object dnOtherIntern =
        Intern.intern((Object) new Double(Double.POSITIVE_INFINITY / Double.POSITIVE_INFINITY));
    assert dnIntern == dnOtherIntern;

    Double diOrig = new Double(Double.POSITIVE_INFINITY);
    Double diIntern = Intern.intern(diOrig);
    Object diObjIntern = Intern.intern((Object) diOrig);
    assert diIntern == diObjIntern;
    Object diOtherIntern = Intern.intern((Object) new Double(2 * Double.MAX_VALUE));
    assert diIntern == diOtherIntern;

    double positiveZero = +0.0;
    double negativeZero = -0.0;
    assert positiveZero == negativeZero;
    assert 1 / positiveZero == Double.POSITIVE_INFINITY;
    assert 1 / negativeZero == Double.NEGATIVE_INFINITY;

    Double dzOrig = new Double(positiveZero);
    Double dzIntern = Intern.intern(dzOrig);
    Object dzObjIntern = Intern.intern((Object) dzOrig);
    assert dzIntern == dzObjIntern;
    Object dzOtherIntern = Intern.intern((Object) new Double(negativeZero));
    assert dzIntern == dzOtherIntern;

    double[] daOrig = new double[] {3.14, 2.71};
    double[] daIntern = Intern.intern(daOrig);
    Object daObjIntern = Intern.intern((Object) daOrig);
    assert daIntern == daObjIntern;
    Object daOtherIntern = Intern.intern((Object) new double[] {3.14, 2.71});
    assert daIntern == daOtherIntern;

    double[] da2Orig = new double[] {+0.0, Double.NaN};
    double[] da2Intern = Intern.intern(da2Orig);
    Object da2ObjIntern = Intern.intern((Object) da2Orig);
    assert da2Intern == da2ObjIntern;
    Object da2OtherIntern =
        Intern.intern(
            (Object) new double[] {-0.0, Double.POSITIVE_INFINITY / Double.POSITIVE_INFINITY});
    assert da2Intern == da2OtherIntern;

    Object[] oaOrig = new Object[] {new String("foo"), new Integer(1)};
    Object[] oaIntern = Intern.intern(oaOrig);
    Object oaObjIntern = Intern.intern((Object) oaOrig);
    assert oaIntern == oaObjIntern;
    Object oaOtherIntern = Intern.intern((Object) new Object[] {new String("foo"), new Integer(1)});
    assert oaIntern == oaOtherIntern;

    java.awt.Point pOrig = new java.awt.Point(1, 2);
    try {
      Intern.intern((Object) pOrig); // performed for side effect
      throw new Error("Didn't throw IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  // Add 100 elements randomly selected from the range 0..limit-1 to the set.
  private static void lsisAddElts(@Positive int limit, LimitedSizeSet<Integer> s) {
    Random r = new Random(20140613);
    for (int i = 0; i < 100; i++) {
      s.add(r.nextInt(limit));
    }
  }

  // Create a LimitedSizeSet of the given size, and add elements to it.
  private static void lsis_test(@Positive int maxSize) {
    LimitedSizeSet<Integer> s = new LimitedSizeSet<>(maxSize);
    for (int i = 1; i < 2 * maxSize; i++) {
      lsisAddElts(i, s);
      int size = s.size();
      assert ((i <= maxSize) ? (size == i) : (size == maxSize + 1))
          : String.format(
              "(%d<=%d) ? (%d==%d) : (%d==%d+1)   size=%d, i=%d, maxSize=%d, s=%s",
              i, maxSize, size, i, size, maxSize, size, i, maxSize, s);
    }
  }

  private static void lss_withNull_test() {
    LimitedSizeSet<@Nullable Integer> s = new LimitedSizeSet<>(10);
    s.add(1);
    s.add(2);
    s.add(null);
    assert s.size() == 3;
    assert s.contains(1);
    assert s.contains(null);
    s.add(3);
    assert s.size() == 4;
    assert s.contains(1);
    assert s.contains(null);
    assert s.contains(3);
  }

  @Test
  public void testLimitedSizeSet() {
    for (int i = 1; i < 10; i++) {
      lsis_test(i);
    }
    lss_withNull_test();
  }

  // This cannot be static because it instantiates an inner class.
  @Test
  public void testMathPlume() {

    // int negate(int a)
    assert MathPlume.negate(3) == -3;
    assert MathPlume.negate(-22) == 22;
    assert MathPlume.negate(0) == 0;

    // int bitwiseComplement(int a)
    assert MathPlume.bitwiseComplement(3) == -4;
    assert MathPlume.bitwiseComplement(-22) == 21;
    assert MathPlume.bitwiseComplement(0) == -1;

    // int sign(int a)
    assert MathPlume.sign(3) == 1;
    assert MathPlume.sign(-22) == -1;
    assert MathPlume.sign(0) == 0;

    // int pow(int base, int expt)
    try {
      assert MathPlume.pow(3, 3) == 27;
      assert MathPlume.pow(-5, 5) == -3125;
      assert MathPlume.pow(22, 0) == 1;
      assert MathPlume.pow(4, 6) == 4096;
      assert MathPlume.pow(1, 222222) == 1;
      assert MathPlume.pow(-2, 25) == -33554432;
      // This is beyond the precision.  Maybe return a long instead of an int?
      // assert MathPlume.pow(-3, 25) == ...;
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error(e);
    }
    try {
      MathPlume.pow(3, -3);
      throw new Error("Didn't throw ArithmeticException");
    } catch (ArithmeticException e) {
    }

    // int gcd(int a, int b)
    assert MathPlume.gcd(2, 50) == 2;
    assert MathPlume.gcd(50, 2) == 2;
    assert MathPlume.gcd(12, 144) == 12;
    assert MathPlume.gcd(144, 12) == 12;
    assert MathPlume.gcd(96, 144) == 48;
    assert MathPlume.gcd(144, 96) == 48;
    assert MathPlume.gcd(10, 25) == 5;
    assert MathPlume.gcd(25, 10) == 5;
    assert MathPlume.gcd(17, 25) == 1;
    assert MathPlume.gcd(25, 17) == 1;
    assert MathPlume.gcd(0, 10) == 10;
    assert MathPlume.gcd(10, 0) == 10;
    assert MathPlume.gcd(25, -10) == 5;
    assert MathPlume.gcd(-25, -10) == 5;
    assert MathPlume.gcd(-25, 10) == 5;

    // int gcd(int[] a)
    assert MathPlume.gcd(new int[] {2, 50}) == 2;
    assert MathPlume.gcd(new int[] {12, 144}) == 12;
    assert MathPlume.gcd(new int[] {96, 144}) == 48;
    assert MathPlume.gcd(new int[] {10, 25}) == 5;
    assert MathPlume.gcd(new int[] {100, 10, 25}) == 5;
    assert MathPlume.gcd(new int[] {768, 324}) == 12;
    assert MathPlume.gcd(new int[] {2400, 48, 36}) == 12;
    assert MathPlume.gcd(new int[] {2400, 72, 36}) == 12;

    // int gcdDifferences(int[] a)
    // Weak set of tests, derived directly from those of "int gcd(int[] a)".
    assert MathPlume.gcdDifferences(new int[] {0, 2, 52}) == 2;
    assert MathPlume.gcdDifferences(new int[] {0, 12, 156}) == 12;
    assert MathPlume.gcdDifferences(new int[] {0, 96, 240}) == 48;
    assert MathPlume.gcdDifferences(new int[] {0, 10, 35}) == 5;
    assert MathPlume.gcdDifferences(new int[] {0, 100, 110, 135}) == 5;
    assert MathPlume.gcdDifferences(new int[] {0, 768, 1092}) == 12;
    assert MathPlume.gcdDifferences(new int[] {0, 2400, 2448, 2484}) == 12;
    assert MathPlume.gcdDifferences(new int[] {0, 2400, 2472, 2508}) == 12;
    assert MathPlume.gcdDifferences(new int[] {5, 5, 5, 5}) == 0;

    // int modPositive(int x, int y)
    assert MathPlume.modPositive(33, 5) == 3;
    assert MathPlume.modPositive(-33, 5) == 2;
    assert MathPlume.modPositive(33, -5) == 3;
    assert MathPlume.modPositive(-33, -5) == 2;

    // int[] missingNumbers(int[] nums)
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 5, 6, 7, 8}), new int[] {});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 6, 7, 8}), new int[] {5});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 8}), new int[] {5, 6, 7});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 5, 6, 8}), new int[] {4, 7});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 6, 8}), new int[] {4, 5, 7});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 5, 5, 6, 7, 8}), new int[] {});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 4, 6, 6, 7, 8}), new int[] {5});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 3, 3}), new int[] {});

    // class MissingNumbersIteratorInt
    class TestMissingNumbersIteratorInt {
      // javadoc won't let this be static
      void test(int[] orig, boolean addEnds, int[] goalMissing) {
        Iterator<Integer> orig_iterator = intArrayIterator(orig);
        Iterator<Integer> missing_iterator =
            new MathPlume.MissingNumbersIteratorInt(orig_iterator, addEnds);
        int[] missing = TestPlume.intIteratorArray(missing_iterator);
        assertArraysEquals(missing, goalMissing);
      }
    }

    TestMissingNumbersIteratorInt tmni = new TestMissingNumbersIteratorInt();
    tmni.test(new int[] {3, 4, 5, 6, 7, 8}, false, new int[] {});
    tmni.test(new int[] {3, 4, 6, 7, 8}, false, new int[] {5});
    tmni.test(new int[] {3, 4, 8}, false, new int[] {5, 6, 7});
    tmni.test(new int[] {3, 5, 6, 8}, false, new int[] {4, 7});
    tmni.test(new int[] {3, 6, 8}, false, new int[] {4, 5, 7});
    tmni.test(new int[] {3}, false, new int[] {});
    tmni.test(new int[] {3, 4, 5}, false, new int[] {});
    tmni.test(new int[] {3, 4, 5, 6, 7, 8}, true, new int[] {2, 9});
    tmni.test(new int[] {3, 4, 6, 7, 8}, true, new int[] {2, 5, 9});
    tmni.test(new int[] {3, 4, 8}, true, new int[] {2, 5, 6, 7, 9});
    tmni.test(new int[] {3, 5, 6, 8}, true, new int[] {2, 4, 7, 9});
    tmni.test(new int[] {3, 6, 8}, true, new int[] {2, 4, 5, 7, 9});
    tmni.test(new int[] {3, 4, 5}, true, new int[] {2, 6});

    tmni.test(new int[] {-1, 1, 2, 3, 5, 6, 7, 9}, true, new int[] {-2, 0, 4, 8, 10});

    // int[] modulus(int[] nums)
    // int[] modulus(Iterator itor)

    class TestModulus {
      // javadoc won't let this be static
      void check(int[] nums, int @Nullable [] goalRm) {
        int[] rm = MathPlume.modulus(nums);
        if (!Arrays.equals(rm, goalRm)) {
          throw new Error(
              "Expected (r,m)=" + Arrays.toString(goalRm) + ", saw (r,m)=" + Arrays.toString(rm));
        }
        if (rm == null) {
          return;
        }
        int goalR = rm[0];
        int m = rm[1];
        for (int i = 0; i < nums.length; i++) {
          int r = nums[i] % m;
          if (r < 0) {
            r += m;
          }
          if (r != goalR) {
            throw new Error("Expected " + nums[i] + " % " + m + " = " + goalR + ", got " + r);
          }
        }
      }

      // javadoc won't let this be static
      void check(Iterator<Integer> itor, int @Nullable [] goalRm) {
        // There would be no point to this:  it's testing
        // intIteratorArray, not the iterator version!
        // return check(intIteratorArray(itor), goalRm);
        assertArraysEquals(MathPlume.modulusInt(itor), goalRm);
      }

      // javadoc won't let this be static
      void checkIterator(int[] nums, int @Nullable [] goalRm) {
        check(intArrayIterator(nums), goalRm);
      }
    }

    TestModulus testModulus = new TestModulus();

    testModulus.check(new int[] {3, 7, 47, 51}, new int[] {3, 4});
    testModulus.check(new int[] {3, 11, 43, 51}, new int[] {3, 8});
    testModulus.check(new int[] {3, 11, 47, 55}, new int[] {3, 4});
    testModulus.check(new int[] {2383, 4015, -81, 463, -689}, new int[] {15, 32});
    testModulus.check(new int[] {}, null);
    testModulus.check(new int[] {1}, null);
    testModulus.check(new int[] {3, 7}, null);
    testModulus.check(new int[] {2, 3, 5, 7}, null);
    testModulus.check(new int[] {2, 19, 101}, null);
    testModulus.check(new int[] {5, 5, 5, 5, 5}, null);

    testModulus.checkIterator(new int[] {}, null);
    testModulus.checkIterator(new int[] {1}, null);
    testModulus.checkIterator(new int[] {3, 7, 47, 51}, new int[] {3, 4});
    testModulus.checkIterator(new int[] {3, 11, 43, 51}, new int[] {3, 8});
    testModulus.checkIterator(new int[] {3, 11, 47, 55}, new int[] {3, 4});
    testModulus.checkIterator(new int[] {2383, 4015, -81, 463, -689}, new int[] {15, 32});
    testModulus.checkIterator(new int[] {5, 5, 5, 5, 5}, null);

    // int[] nonmodulusStrict(int[] nums)
    // int[] nonmodulusNonstrict(int[] nums)
    // int[] nonmodulusStrict(Iterator nums)

    class TestNonModulus {
      // javadoc won't let this be static
      void checkStrict(int[] nums, int @Nullable [] goalRm) {
        check(nums, goalRm, true);
        Iterator<Integer> itor = intArrayIterator(nums);
        assertArraysEquals(MathPlume.nonmodulusStrictInt(itor), goalRm);
      }

      // javadoc won't let this be static
      void checkNonstrict(int[] nums, int @Nullable [] goalRm) {
        check(nums, goalRm, false);
      }

      // javadoc won't let this be static
      void check(int[] nums, int @Nullable [] goalRm, boolean strict) {
        int[] rm;
        if (strict) {
          rm = MathPlume.nonmodulusStrict(nums);
        } else {
          rm = MathPlume.nonmodulusNonstrict(nums);
        }
        if (!Arrays.equals(rm, goalRm)) {
          throw new Error(
              "Expected (r,m)=" + Arrays.toString(goalRm) + ", saw (r,m)=" + Arrays.toString(rm));
        }
        if (rm == null) {
          return;
        }
        int goalR = rm[0];
        int m = rm[1];
        for (int i = 0; i < nums.length; i++) {
          int r = nums[i] % m;
          if (r < 0) {
            r += m;
          }
          if (r == goalR) {
            throw new Error("Expected inequality, saw " + nums[i] + " % " + m + " = " + r);
          }
        }
      }
    }

    TestNonModulus testNonModulus = new TestNonModulus();

    testNonModulus.checkStrict(new int[] {1, 2, 3, 5, 6, 7, 9}, null);
    testNonModulus.checkStrict(new int[] {-1, 1, 2, 3, 5, 6, 7, 9}, new int[] {0, 4});
    testNonModulus.checkStrict(new int[] {1, 2, 3, 5, 6, 7, 9, 11}, null);
    testNonModulus.checkStrict(new int[] {1, 2, 3, 5, 6, 7, 11}, null);
    testNonModulus.checkStrict(new int[] {1, 2, 4, 6, 8, 10}, null);

    // null because only 7 elements, so don't try modulus = 4
    testNonModulus.checkNonstrict(new int[] {1, 2, 3, 5, 6, 7, 9}, null);
    testNonModulus.checkNonstrict(new int[] {1, 2, 3, 5, 6, 7, 9, 10}, new int[] {0, 4});
    testNonModulus.checkNonstrict(new int[] {1, 2, 3, 5, 6, 7, 9, 11}, new int[] {0, 4});
    testNonModulus.checkNonstrict(new int[] {1, 2, 3, 5, 6, 7, 9, 11, 12, 13}, null);
    testNonModulus.checkNonstrict(
        new int[] {1, 2, 3, 5, 6, 7, 9, 11, 12, 13, 14, 15}, new int[] {4, 6});
    testNonModulus.checkNonstrict(new int[] {1, 2, 3, 5, 6, 7, 9, 11, 12, 13, 14, 15, 22}, null);
  }

  @Test
  public void testOrderedPairIterator() {
    final int NULL = -2222;

    ArrayList<Integer> ones = new ArrayList<>();
    for (int i = 1; i <= 30; i++) {
      ones.add(i);
    }
    ArrayList<Integer> twos = new ArrayList<>();
    for (int i = 2; i <= 30; i += 2) {
      twos.add(i);
    }
    ArrayList<Integer> threes = new ArrayList<>();
    for (int i = 3; i <= 30; i += 3) {
      threes.add(i);
    }

    // I've replaced the nulls by 0 in order to permit the array elements
    // to be ints instead of Integers.

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(ones.iterator(), ones.iterator()),
        new int[][] {
          {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7}, {8, 8}, {9, 9}, {10, 10},
          {11, 11}, {12, 12}, {13, 13}, {14, 14}, {15, 15}, {16, 16}, {17, 17}, {18, 18}, {19, 19},
          {20, 20}, {21, 21}, {22, 22}, {23, 23}, {24, 24}, {25, 25}, {26, 26}, {27, 27}, {28, 28},
          {29, 29}, {30, 30},
        });

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(ones.iterator(), twos.iterator()),
        new int[][] {
          {1, NULL},
          {2, 2},
          {3, NULL},
          {4, 4},
          {5, NULL},
          {6, 6},
          {7, NULL},
          {8, 8},
          {9, NULL},
          {10, 10},
          {11, NULL},
          {12, 12},
          {13, NULL},
          {14, 14},
          {15, NULL},
          {16, 16},
          {17, NULL},
          {18, 18},
          {19, NULL},
          {20, 20},
          {21, NULL},
          {22, 22},
          {23, NULL},
          {24, 24},
          {25, NULL},
          {26, 26},
          {27, NULL},
          {28, 28},
          {29, NULL},
          {30, 30},
        });

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(twos.iterator(), ones.iterator()),
        new int[][] {
          {NULL, 1},
          {2, 2},
          {NULL, 3},
          {4, 4},
          {NULL, 5},
          {6, 6},
          {NULL, 7},
          {8, 8},
          {NULL, 9},
          {10, 10},
          {NULL, 11},
          {12, 12},
          {NULL, 13},
          {14, 14},
          {NULL, 15},
          {16, 16},
          {NULL, 17},
          {18, 18},
          {NULL, 19},
          {20, 20},
          {NULL, 21},
          {22, 22},
          {NULL, 23},
          {24, 24},
          {NULL, 25},
          {26, 26},
          {NULL, 27},
          {28, 28},
          {NULL, 29},
          {30, 30},
        });

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(ones.iterator(), threes.iterator()),
        new int[][] {
          {1, NULL},
          {2, NULL},
          {3, 3},
          {4, NULL},
          {5, NULL},
          {6, 6},
          {7, NULL},
          {8, NULL},
          {9, 9},
          {10, NULL},
          {11, NULL},
          {12, 12},
          {13, NULL},
          {14, NULL},
          {15, 15},
          {16, NULL},
          {17, NULL},
          {18, 18},
          {19, NULL},
          {20, NULL},
          {21, 21},
          {22, NULL},
          {23, NULL},
          {24, 24},
          {25, NULL},
          {26, NULL},
          {27, 27},
          {28, NULL},
          {29, NULL},
          {30, 30},
        });

    compareOrderedPairIterator(
        new OrderedPairIterator<Integer>(twos.iterator(), threes.iterator()),
        new int[][] {
          {2, NULL},
          {NULL, 3},
          {4, NULL},
          {6, 6},
          {8, NULL},
          {NULL, 9},
          {10, NULL},
          {12, 12},
          {14, NULL},
          {NULL, 15},
          {16, NULL},
          {18, 18},
          {20, NULL},
          {NULL, 21},
          {22, NULL},
          {24, 24},
          {26, NULL},
          {NULL, 27},
          {28, NULL},
          {30, 30},
        });
  }

  /**
   * Throws an assertion unless the paired iterator contains the same values as the argument array.
   * Requires that size of opi = ints.length.
   *
   * @param opi an iterator over pairs of integers
   * @param ints an array of two-element arrays of integers
   * @throws AssertionError iff the iterator returns the same values as the argument array contains
   */
  @SuppressWarnings("index") // same length iterator and array, and while loop with ++ on index
  public static void compareOrderedPairIterator(
      OrderedPairIterator<Integer> opi, int[] @ArrayLen(2) [] ints) {
    int pairno = 0;
    while (opi.hasNext()) {
      Pair<@Nullable Integer, @Nullable Integer> pair = opi.next();
      // System.out.println("Iterator: <" + pair.a + "," + pair.b + ">, array: <" + ints[pairno][0]
      //     + "," + ints[pairno][1] + ">");
      assert (pair.a == null) || (pair.a.intValue() == ints[pairno][0]);
      assert (pair.b == null) || (pair.b.intValue() == ints[pairno][1]);
      pairno++;
    }
    assert pairno == ints.length;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// UtilPlume
  ///

  private static BitSet randomBitSet(@NonNegative int length, Random r) {
    BitSet result = new BitSet(length);
    for (int i = 0; i < length; i++) {
      result.set(i, r.nextBoolean());
    }
    return result;
  }

  @Test
  public void testStringBuilderDelimited() {
    compareJoinAndSBD(new String[] {"foo", "bar", "baz"});
    compareJoinAndSBD(new String[] {"foo"});
    compareJoinAndSBD(new String[] {});
  }

  public void compareJoinAndSBD(String[] strings) {
    StringBuilderDelimited sbd = new StringBuilderDelimited(",");
    for (String str : strings) {
      sbd.add(str);
    }
    assert sbd.toString().equals(UtilPlume.join(strings, ","));
  }

  // This cannot be static because it instantiates an inner class.
  @SuppressWarnings("ArrayEquals")
  @Test
  public void testUtilPlume() {

    // public static intersectionCardinalityAtLeast(BitSet a, BitSet b, int i)
    {
      Random r = new Random(20031008);
      for (int i = 0; i < 100; i++) {
        BitSet b1 = randomBitSet(r.nextInt(100), r);
        BitSet b2 = randomBitSet(r.nextInt(100), r);
        BitSet b3 = randomBitSet(r.nextInt(100), r);
        BitSet intersection = (BitSet) b1.clone();
        intersection.and(b2);
        int card = intersection.cardinality();
        for (int j = 0; j < 100; j++) {
          assert UtilPlume.intersectionCardinalityAtLeast(b1, b2, j) == (card >= j);
        }
        intersection.and(b3);
        card = intersection.cardinality();
        for (int j = 0; j < 100; j++) {
          assert UtilPlume.intersectionCardinalityAtLeast(b1, b2, b3, j) == (card >= j);
        }
      }
    }

    // public static BufferedReader bufferedFileReader(String filename)
    // public static LineNumberReader lineNumberFileReader(String filename)
    // public static BufferedWriter bufferedFileWriter(String filename) throws IOException
    // public static Class classForName(String className)

    // public static void addToClasspath(String dir)
    // public static final class WildcardFilter implements FilenameFilter
    //   public WildcardFilter(String filename)
    //   public boolean accept(File dir, String name)
    // public static boolean canCreateAndWrite(File file)
    // public static void writeObject(Object o, File file) throws IOException
    // public static Object readObject(File file)
    // public static File createTempDir(String prefix, String suffix)

    // public Object incrementHashMap(HashMap hm, Object key, int count)

    try {
      assert UtilPlume.canCreateAndWrite(new File("TestPlume.java"));

      // This test fails if run by the superuser (who can overwrite
      // any file).
      if (!System.getProperty("user.name").equals("root")) {
        File readOnly = new File("temp");
        readOnly.createNewFile();
        readOnly.setReadOnly();
        assert !UtilPlume.canCreateAndWrite(readOnly);
        readOnly.delete();
      }

      assert UtilPlume.canCreateAndWrite(new File("temp"));
      assert !UtilPlume.canCreateAndWrite(new File("temp/temp"));
    } catch (IOException e) {
      e.printStackTrace();
      org.junit.Assert.fail("failure while testing UtilPlume.canCreateAndWrite(): " + e.toString());
    }

    {
      // These names are taken from APL notation, where iota creates an
      // array of all the numbers up to its argument.
      ArrayList<Integer> iota0 = new ArrayList<>();
      ArrayList<Integer> iota10 = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        iota10.add(i);
      }
      ArrayList<Integer> iota10Twice = new ArrayList<>();
      iota10Twice.addAll(iota10);
      iota10Twice.addAll(iota10);
      ArrayList<Integer> iota10Thrice = new ArrayList<>();
      iota10Thrice.addAll(iota10);
      iota10Thrice.addAll(iota10);
      iota10Thrice.addAll(iota10);

      // public static class EnumerationIterator implements Iterator
      // public static class IteratorEnumeration implements Enumeration

      assert iota0.equals(toArrayList(iota0.iterator()));
      assert iota0.equals(
          toArrayList(new CollectionsPlume.IteratorEnumeration<Integer>(iota0.iterator())));
      assert iota10.equals(toArrayList(iota10.iterator()));
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
          toArrayList(
              new CollectionsPlume.MergedIterator<Integer>(iota10IteratorThrice.iterator())));
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
          toArrayList(
              new CollectionsPlume.MergedIterator<Integer>(iota10IteratorTwice1.iterator())));
      assert iota10Twice.equals(
          toArrayList(
              new CollectionsPlume.MergedIterator<Integer>(iota10IteratorTwice2.iterator())));
      assert iota10Twice.equals(
          toArrayList(
              new CollectionsPlume.MergedIterator<Integer>(iota10IteratorTwice3.iterator())));

      class OddFilter implements Filter<Integer> {
        public OddFilter() {}

        @Override
        public boolean accept(Integer i) {
          return i.intValue() % 2 != 0;
        }
      }

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

    // public static final class RemoveFirstAndLastIterator implements Iterator
    {
      ArrayList<Integer> iota5 = new ArrayList<>();
      for (int i = 0; i < 5; i++) {
        iota5.add(i);
      }
      ArrayList<Integer> iota5middle = new ArrayList<>();
      for (int i = 1; i < 4; i++) {
        iota5middle.add(i);
      }
      CollectionsPlume.RemoveFirstAndLastIterator<Integer> rfali =
          new CollectionsPlume.RemoveFirstAndLastIterator<Integer>(iota5.iterator());
      ArrayList<Integer> rfali_vector = toArrayList(rfali);
      assert iota5middle.equals(rfali_vector);
      assert rfali.getFirst().equals(0);
      assert rfali.getLast().equals(4);
    }

    // public static ArrayList randomElements(Iterator itor, int numElts)
    // public static ArrayList randomElements(Iterator itor, int numElts, Random random)

    // Iterate through numbers from zero up to the argument (non-inclusive)
    class IotaIterator implements Iterator<Integer> {
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
    {
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
            "index", "value"
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
        assert ArraysPlume.sum(totals) == grandTotal : "Totals = " + ArraysPlume.sum(totals);
        // System.out.print("chosen:\t");
        for (int k = 0; k < numEltsLimit; k++) {
          int thisTotal = totals[k];
          int expected = tries * iTruncated / itorSize;
          double ratio = (double) thisTotal / (double) expected;
          // System.out.print(((k<10) ? " " : "") + k + " " + thisTotal + "\t");
          // System.out.print("\nExp=" + expected + "\tratio=" + ratio + "\t");
          assert k >= itorSize || (ratio > ratioLimit && ratio < 1 / ratioLimit);
        }
        // System.out.println();
      }
    }

    // public static <T> @Nullable Integer incrementMap(Map<T,Integer> m, T key, int count) {
    // public static <K,V> String mapToString(Map<K,V> m) {
    // public static <K,V> void mapToString(Appendable sb, Map<K,V> m, String linePrefix) {
    // public static <K extends Comparable<? super K>,V> Collection<@KeyFor("#1") K>
    //     sortedKeySet(Map<K,V> m) {
    // public static <K,V> Collection<@KeyFor("#1") K>
    //     sortedKeySet(Map<K,V> m, Comparator<K> comparator) {

    // public static boolean propertyIsTrue(Properties p, String key)
    // public static String appendProperty(Properties p, String key, String value)
    // public static String setDefault(Properties p, String key, String value)
    // public static void streamCopy(java.io.InputStream from, java.io.OutputStream to)

    // public static String replaceString(String target, String oldStr, String newStr)

    assert UtilPlume.replaceString("hello dolly well hello dolly", " ", "  ")
        .equals("hello  dolly  well  hello  dolly");
    assert UtilPlume.replaceString("  hello  dolly well hello dolly  ", " ", "  ")
        .equals("    hello    dolly  well  hello  dolly    ");
    assert UtilPlume.replaceString("hello dolly well hello dolly", "ll", "y")
        .equals("heyo doyy wey heyo doyy");
    assert UtilPlume.replaceString("hello dolly well hello dolly", "q", "yyy")
        .equals("hello dolly well hello dolly");

    // public static String[] split(String s, char delim)
    // public static String[] split(String s, String delim)

    assert Arrays.equals(UtilPlume.split("foo,bar,baz", ','), new String[] {"foo", "bar", "baz"});
    assert Arrays.equals(UtilPlume.split("foo", ','), new String[] {"foo"});
    assert Arrays.equals(UtilPlume.split("", ','), new String[] {""});
    assert Arrays.equals(UtilPlume.split(",foo,", ','), new String[] {"", "foo", ""});
    assert Arrays.equals(UtilPlume.split("foo,bar,baz", ","), new String[] {"foo", "bar", "baz"});
    assert Arrays.equals(UtilPlume.split("foo", ","), new String[] {"foo"});
    assert Arrays.equals(UtilPlume.split("", ","), new String[] {""});
    assert Arrays.equals(UtilPlume.split(",foo,", ","), new String[] {"", "foo", ""});
    assert Arrays.equals(
        UtilPlume.split("foo, bar, baz", ", "), new String[] {"foo", "bar", "baz"});
    assert Arrays.equals(UtilPlume.split("foo", ", "), new String[] {"foo"});
    assert Arrays.equals(UtilPlume.split("", ", "), new String[] {""});
    assert Arrays.equals(UtilPlume.split(", foo, ", ", "), new String[] {"", "foo", ""});

    // public static String join(Object[] a, String delim)
    // public static String join(ArrayList v, String delim)

    assert UtilPlume.join(new String[] {"foo", "bar", "baz"}, ", ").equals("foo, bar, baz");
    assert UtilPlume.join(new String[] {"foo"}, ", ").equals("foo");
    assert UtilPlume.join(new String[] {}, ", ").equals("");
    assert UtilPlume.join(new Integer[] {0, 1, 2, 3, 4}, "").equals("01234");
    ArrayList<Object> potpourri = new ArrayList<>();
    potpourri.add("day");
    potpourri.add(2);
    potpourri.add("day");
    assert UtilPlume.join(potpourri, " ").equals("day 2 day");

    // public static String escapeNonJava(String orig)
    // public static String escapeNonJava(Character ch)

    assert UtilPlume.escapeNonJava("foobar").equals("foobar");
    assert UtilPlume.escapeNonJava("").equals("");
    assert UtilPlume.escapeNonJava("\\").equals("\\\\");
    assert UtilPlume.escapeNonJava("\\\n\r\"").equals("\\\\\\n\\r\\\"");
    assert UtilPlume.escapeNonJava("split\nlines").equals("split\\nlines");
    assert UtilPlume.escapeNonJava("\\relax").equals("\\\\relax");
    assert UtilPlume.escapeNonJava("\"hello\"").equals("\\\"hello\\\"");
    assert UtilPlume.escapeNonJava("\"hello\" \"world\"").equals("\\\"hello\\\" \\\"world\\\"");

    // public static String escapeNonASCII(String orig)

    assert UtilPlume.escapeNonASCII("foobar").equals("foobar");
    assert UtilPlume.escapeNonASCII("").equals("");
    assert UtilPlume.escapeNonASCII("\\").equals("\\\\");
    assert UtilPlume.escapeNonASCII("\\\n\r\"").equals("\\\\\\n\\r\\\"");
    assert UtilPlume.escapeNonASCII("split\nlines").equals("split\\nlines");
    assert UtilPlume.escapeNonASCII("\\relax").equals("\\\\relax");
    assert UtilPlume.escapeNonASCII("\"hello\"").equals("\\\"hello\\\"");
    assert UtilPlume.escapeNonASCII("\"hello\" \"world\"").equals("\\\"hello\\\" \\\"world\\\"");
    assert UtilPlume.escapeNonASCII("\0\1\2\7\12\70\100\111\222")
        .equals("\\000\\001\\002\\007\\n8@I\\222");
    assert UtilPlume.escapeNonASCII("\u0100\u1000\ucafe\uffff")
        .equals("\\u0100\\u1000\\ucafe\\uffff");

    // private static String escapeNonASCII(char c)

    // public static String unescapeNonJava(String orig)

    assert UtilPlume.unescapeNonJava("foobar").equals("foobar");
    assert UtilPlume.unescapeNonJava("").equals("");
    assert UtilPlume.unescapeNonJava("\\\\").equals("\\");
    assert UtilPlume.unescapeNonJava("\\\"").equals("\"");
    assert UtilPlume.unescapeNonJava("\\n").equals("\n"); // not lineSep
    assert UtilPlume.unescapeNonJava("\\r").equals("\r");
    assert UtilPlume.unescapeNonJava("split\\nlines").equals("split\nlines");
    assert UtilPlume.unescapeNonJava("\\\\\\n").equals("\\\n"); // not lineSep
    assert UtilPlume.unescapeNonJava("\\n\\r").equals("\n\r"); // not lineSep
    assert UtilPlume.unescapeNonJava("\\\\\\n\\r\\\"").equals("\\\n\r\"");
    assert UtilPlume.unescapeNonJava("\\\\relax").equals("\\relax");
    assert UtilPlume.unescapeNonJava("\\\"hello\\\"").equals("\"hello\"");
    assert UtilPlume.unescapeNonJava("\\\"hello\\\" \\\"world\\\"").equals("\"hello\" \"world\"");
    assert UtilPlume.unescapeNonJava("\\").equals("\\");
    assert UtilPlume.unescapeNonJava("foo\\").equals("foo\\");
    assert UtilPlume.unescapeNonJava("\\*abc").equals("*abc");
    // Should add more tests here.

    // Unfortunately, there isn't yet a unescapeNonASCII function.
    // If implemented, it should have the following behavior:
    // assert UtilPlume.unescapeNonASCII("\\115").equals("M");
    // assert UtilPlume.unescapeNonASCII("\\115\\111\\124").equals("MIT");

    // public static String removeWhitespaceAround(String arg, String delimiter)
    // public static String removeWhitespaceAfter(String arg, String delimiter)
    // public static String removeWhitespaceBefore(String arg, String delimiter)

    assert UtilPlume.removeWhitespaceBefore("a,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceBefore("a, b", ",").equals("a, b");
    assert UtilPlume.removeWhitespaceBefore("a ,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceBefore("a , b", ",").equals("a, b");
    assert UtilPlume.removeWhitespaceBefore("ab=>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceBefore("ab=> cd", "=>").equals("ab=> cd");
    assert UtilPlume.removeWhitespaceBefore("ab =>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceBefore("ab => cd", "=>").equals("ab=> cd");
    assert UtilPlume.removeWhitespaceBefore("123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceBefore(" 123 cd", "123").equals("123 cd");
    assert UtilPlume.removeWhitespaceBefore(" 123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceBefore("123 cd", "123").equals("123 cd");
    assert UtilPlume.removeWhitespaceBefore("cd123", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceBefore("cd 123 ", "123").equals("cd123 ");
    assert UtilPlume.removeWhitespaceBefore("cd123 ", "123").equals("cd123 ");
    assert UtilPlume.removeWhitespaceBefore("cd 123", "123").equals("cd123");

    assert UtilPlume.removeWhitespaceAfter("a,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAfter("a, b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAfter("a ,b", ",").equals("a ,b");
    assert UtilPlume.removeWhitespaceAfter("a , b", ",").equals("a ,b");
    assert UtilPlume.removeWhitespaceAfter("ab=>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAfter("ab=> cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAfter("ab =>cd", "=>").equals("ab =>cd");
    assert UtilPlume.removeWhitespaceAfter("ab => cd", "=>").equals("ab =>cd");
    assert UtilPlume.removeWhitespaceAfter("123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAfter(" 123 cd", "123").equals(" 123cd");
    assert UtilPlume.removeWhitespaceAfter(" 123cd", "123").equals(" 123cd");
    assert UtilPlume.removeWhitespaceAfter("123 cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAfter("cd123", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAfter("cd 123 ", "123").equals("cd 123");
    assert UtilPlume.removeWhitespaceAfter("cd123 ", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAfter("cd 123", "123").equals("cd 123");

    assert UtilPlume.removeWhitespaceAround("a,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAround("a, b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAround("a ,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAround("a , b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAround("ab=>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAround("ab=> cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAround("ab =>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAround("ab => cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAround("123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAround(" 123 cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAround(" 123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAround("123 cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAround("cd123", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAround("cd 123 ", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAround("cd123 ", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAround("cd 123", "123").equals("cd123");

    // public static String nplural(int n, String noun)

    assert UtilPlume.nplural(0, "fuss").equals("0 fusses");
    assert UtilPlume.nplural(1, "fuss").equals("1 fuss");
    assert UtilPlume.nplural(2, "fuss").equals("2 fusses");
    assert UtilPlume.nplural(0, "fox").equals("0 foxes");
    assert UtilPlume.nplural(1, "fox").equals("1 fox");
    assert UtilPlume.nplural(2, "fox").equals("2 foxes");
    assert UtilPlume.nplural(0, "fish").equals("0 fishes");
    assert UtilPlume.nplural(1, "fish").equals("1 fish");
    assert UtilPlume.nplural(2, "fish").equals("2 fishes");
    assert UtilPlume.nplural(0, "fletch").equals("0 fletches");
    assert UtilPlume.nplural(1, "fletch").equals("1 fletch");
    assert UtilPlume.nplural(2, "fletch").equals("2 fletches");
    assert UtilPlume.nplural(0, "fund").equals("0 funds");
    assert UtilPlume.nplural(1, "fund").equals("1 fund");
    assert UtilPlume.nplural(2, "fund").equals("2 funds");
    assert UtilPlume.nplural(0, "f-stop").equals("0 f-stops");
    assert UtilPlume.nplural(1, "f-stop").equals("1 f-stop");
    assert UtilPlume.nplural(2, "f-stop").equals("2 f-stops");

    // public static String rpad(String s, int length)
    // public static String rpad(int num, int length)
    // public static String rpad(double num, int length)

    assert UtilPlume.rpad("", 5).equals("     ");
    assert UtilPlume.rpad("abcd", 5).equals("abcd ");
    assert UtilPlume.rpad("abcde", 5).equals("abcde");
    assert UtilPlume.rpad("abcdef", 5).equals("abcde");
    assert UtilPlume.rpad("abcde ghij", 5).equals("abcde");
    assert UtilPlume.rpad(10, 5).equals("10   ");
    assert UtilPlume.rpad(3.14, 5).equals("3.14 ");

    // public static class NullableStringComparator
    //   public int compare(Object o1, Object o2)

    // public static int count(String s, int ch)
    // public static int count(String s, String sub)

    assert UtilPlume.count("abcde", 'a') == 1;
    assert UtilPlume.count("abcde", 'c') == 1;
    assert UtilPlume.count("abcde", 'e') == 1;
    assert UtilPlume.count("abcde", 'z') == 0;
    assert UtilPlume.count("abacadaea", 'a') == 5;
    assert UtilPlume.count("aaa aea", 'a') == 5;
    assert UtilPlume.count("daeaaa", 'a') == 4;

    // This will be easy to write tests for, when I get around to it.
    // public static ArrayList tokens(String str, String delim, boolean returnTokens)
    // public static ArrayList tokens(String str, String delim)
    // public static ArrayList tokens(String str)

    // public static List sortList (List l, Comparator c)
    // public static <T> List<T> removeDuplicates(List<T> l) {

    List<Integer> l123 = new ArrayList<>();
    l123.add(1);
    l123.add(2);
    l123.add(3);
    List<Integer> l123123 = new ArrayList<>();
    l123123.add(1);
    l123123.add(2);
    l123123.add(3);
    l123123.add(1);
    l123123.add(2);
    l123123.add(3);
    List<Integer> l12223 = new ArrayList<>();
    l12223.add(1);
    l12223.add(2);
    l12223.add(2);
    l12223.add(2);
    l12223.add(3);
    List<Integer> l1123 = new ArrayList<>();
    l1123.add(1);
    l1123.add(1);
    l1123.add(2);
    l1123.add(3);
    List<Integer> l1233 = new ArrayList<>();
    l1233.add(1);
    l1233.add(1);
    l1233.add(2);
    l1233.add(3);

    assert CollectionsPlume.removeDuplicates(l123).equals(l123);
    assert CollectionsPlume.removeDuplicates(l123123).equals(l123);
    assert CollectionsPlume.removeDuplicates(l12223).equals(l123);
    assert CollectionsPlume.removeDuplicates(l1123).equals(l123);
    assert CollectionsPlume.removeDuplicates(l1233).equals(l123);

    // public boolean deepEquals(Object o1, Object o2)

    boolean[] zatft1 = new boolean[] {true, false, true};
    boolean[] zatft2 = new boolean[] {true, false, true};
    boolean[] zatff = new boolean[] {true, false, false};
    assert !zatft1.equals(zatft2);
    assert CollectionsPlume.deepEquals(zatft1, zatft2);
    assert !zatft1.equals(zatff);
    assert !CollectionsPlume.deepEquals(zatft1, zatff);

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
    // assert ! l1.equals(l2);
    // assert ! l1.equals(l3);
    // assert ! l2.equals(l3);
    assert CollectionsPlume.deepEquals(l1, l2);
    assert !CollectionsPlume.deepEquals(l1, l3);
    assert !CollectionsPlume.deepEquals(l2, l3);

    // This is tested by the tokens methods.
    // public static ArrayList makeArrayList(Enumeration e)

    Locale.setDefault(Locale.US);
    assert UtilPlume.abbreviateNumber(5).equals("5.00");
    assert UtilPlume.abbreviateNumber(5000).equals("5.00K");
    assert UtilPlume.abbreviateNumber(5000000).equals("5.00M");
    assert UtilPlume.abbreviateNumber(1000000000).equals("1.00G");
    assert UtilPlume.abbreviateNumber(1).equals("1.00");
    assert UtilPlume.abbreviateNumber(12).equals("12.0");
    assert UtilPlume.abbreviateNumber(123).equals("123");
    assert UtilPlume.abbreviateNumber(1234).equals("1.23K");
    assert UtilPlume.abbreviateNumber(12345).equals("12.3K");
    assert UtilPlume.abbreviateNumber(123456).equals("123K");
    assert UtilPlume.abbreviateNumber(1234567).equals("1.23M");
    assert UtilPlume.abbreviateNumber(12345678).equals("12.3M");
    assert UtilPlume.abbreviateNumber(123456789).equals("123M");
    assert UtilPlume.abbreviateNumber(1234567890).equals("1.23G");
    assert UtilPlume.abbreviateNumber(9).equals("9.00");
    assert UtilPlume.abbreviateNumber(98).equals("98.0");
    assert UtilPlume.abbreviateNumber(987).equals("987");
    assert UtilPlume.abbreviateNumber(9876).equals("9.88K");
    assert UtilPlume.abbreviateNumber(98765).equals("98.8K");
    assert UtilPlume.abbreviateNumber(987654).equals("988K");
    assert UtilPlume.abbreviateNumber(9876543).equals("9.88M");
    assert UtilPlume.abbreviateNumber(98765432).equals("98.8M");
    assert UtilPlume.abbreviateNumber(987654321).equals("988M");
    assert UtilPlume.abbreviateNumber(9876543210L).equals("9.88G");
  }

  @Test
  public void testTestUtilPlume() {
    int[] a = new int[] {3, 4, 5};
    assertArraysEquals(intIteratorArray(intArrayIterator(a)), a);
  }

  @Test
  public void testWeakHasherMap() {}

  /**
   * These tests could be much more thorough. Basically all that is tested is that identity is used
   * rather than a normal hash. The tests will fail however, if WeakHashMap is swapped for
   * WeakIdentityHashMap.
   */
  @Test
  public void testWeakIdentityHashMap() {

    String s1 = "one";
    String s2 = "two";
    String s3 = "three";

    WeakIdentityHashMap<String, Integer> m = new WeakIdentityHashMap<>();
    // WeakHashMap<String,Integer> m = new WeakHashMap<>();

    m.put(s1, 1);
    m.put(s2, 2);
    m.put(s3, 3);

    String s1a = new String(s1);
    String s2a = new String(s2);
    String s3a = new String(s3);

    m.put(s1a, 1);
    m.put(s2a, 2);
    m.put(s3a, 3);

    assert m.get(s1) == 1;
    assert m.get(s2) == 2;
    assert m.get(s3) == 3;
    assert m.get(s1a) == 1;
    assert m.get(s2a) == 2;
    assert m.get(s3a) == 3;

    m.remove(s1);
    m.remove(s2);
    m.remove(s3);
    assert m.get(s1) == null;
    assert m.get(s2) == null;
    assert m.get(s3) == null;
    assert m.get(s1a) == 1;
    assert m.get(s2a) == 2;
    assert m.get(s3a) == 3;
  }

  /**
   * Test the intering of subsequences as triples of the original sequence, the start and the end
   * indices.
   */
  @SuppressWarnings("index") // test code that relies on assumptions about what is being tested
  @Test
  public void testSequenceAndIndices() {
    int[] a1 = Intern.intern(new int[] {1, 2, 3, 4, 5, 6, 7});
    int[] a2 = Intern.intern(new int[] {1, 2, 3, 4, 5, 6, 7});
    int[] a3 = Intern.intern(new int[] {2, 3, 4, 5, 6, 7});
    int i = 2;
    int j = 4;
    int k = 5;

    int[] s1 = Intern.internSubsequence(a1, i, j);
    int[] s2 = Intern.internSubsequence(a2, i, j);
    int[] s3 = Intern.internSubsequence(a1, j, k);
    int[] s4 = Intern.internSubsequence(a1, j, k);
    int[] s5 = Intern.internSubsequence(a3, j - 1, k - 1);

    assert a1 == a2;
    assert s1 == s2;
    assert s3 == s4;
    assert s3 == s5;
    assert ArraysPlume.isSubarray(s1, ArraysPlume.subarray(a1, i, j - i), 0);
    assert ArraysPlume.isSubarray(ArraysPlume.subarray(a1, i, j - i), s1, 0);

    long[] l1 = Intern.intern(new long[] {1, 2, 3, 4, 5, 6});
    assert l1 == Intern.internSubsequence(l1, 0, l1.length);
  }

  // To do
  // @Test
  // public static void testFileIOException() {
  // }

  /** Test the comparison, indexof, and set equivalence calls in fuzzy float. */
  @Test
  public void testFuzzyFloat() {

    FuzzyFloat ff = new FuzzyFloat(0.0001);
    double offset = 0.00007;
    double offhigh = 1 + offset;
    double offlow = 1 - offset;
    double offhigh2 = 1 + 2 * offset;
    double offlow2 = 1 - 2 * offset;

    // test equality for a variety of postive and negative numbers
    for (double d = -20000; d < 20000; d += 1000.36) {
      assert ff.eq(d, d * offhigh);
      assert ff.eq(d, d * offlow);
      assert !ff.eq(d, d * offhigh2);
      assert !ff.eq(d, d * offlow2);
      assert !ff.ne(d, d * offhigh);
      assert !ff.ne(d, d * offlow);
      assert ff.ne(d, d * offhigh2);
      assert ff.ne(d, d * offlow2);
    }

    // make sure nothing is equal to zero
    assert ff.eq(0, Double.MIN_VALUE);
    assert ff.eq(0, -Double.MIN_VALUE);
    assert !ff.ne(0, Double.MIN_VALUE);
    assert !ff.ne(0, -Double.MIN_VALUE);

    // make sure that 0 equals 0
    assert ff.eq(0, 0);
    assert !ff.ne(0, 0);

    // make sure that NaNs are not equal
    assert !ff.eq(Double.NaN, Double.NaN);

    // make sure that various unusual values are equal
    assert ff.eq(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    assert ff.eq(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    // rudimentary checks on the comparison operators (since they all just
    // use eq and ne anyway)
    {
      double d = 2563.789;
      assert !ff.gt(d, d * offlow);
      assert !ff.lt(d, d * offhigh);
      assert ff.gt(d, d * offlow2);
      assert ff.lt(d, d * offhigh2);
      assert ff.gte(d, d * offhigh);
      assert ff.lte(d, d * offlow);
      assert !ff.gte(d, d * offhigh2);
      assert !ff.lte(d, d * offlow2);
    }

    // public int indexOf (double[] a, double elt)
    {
      double[] a = new double[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = i;
      }
      double[] aCopy = a.clone();
      assert ff.indexOf(a, -1) == -1;
      assert ff.indexOf(a, 0) == 0;
      assert ff.indexOf(a, 7) == 7;
      assert ff.indexOf(a, 9) == 9;
      assert ff.indexOf(a, 10) == -1;
      assert ff.indexOf(a, 20) == -1;
      assert ff.indexOf(a, Double.MIN_VALUE) == 0;
      assert ff.indexOf(a, 7 * offhigh) == 7;
      assert ff.indexOf(a, 9 * offlow) == 9;
      assert ff.indexOf(a, 7 * offhigh2) == -1;
      assert ff.indexOf(a, 9 * offlow2) == -1;
      assertArraysEquals(a, aCopy);
    }

    // public int indexOf (double[] a, double[] sub)
    {
      double[] a = new double[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = i;
      }
      double[] b = new double[] {};
      double[] c = new double[] {a[0], a[1], a[2]};
      double[] d = new double[] {a[1], a[2]};
      double[] e = new double[] {a[2], a[3], a[4], a[5]};
      double[] f = new double[] {a[7], a[8], a[9]};
      double[] g = new double[] {a[7], 22, a[9]};
      double[] h = new double[] {a[7], a[8], a[9], 10};

      assert ff.indexOf(a, b) == 0;
      assert ff.indexOf(a, c) == 0;
      assert ff.indexOf(a, d) == 1;
      assert ff.indexOf(a, e) == 2;
      assert ff.indexOf(a, f) == 7;
      assert ff.indexOf(a, g) == -1;
      assert ff.indexOf(a, h) == -1;
    }
    {
      double[] a = new double[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = i;
      }
      double[] b = new double[] {};
      double[] c = new double[] {a[0] * offlow, a[1] * offhigh, a[2] * offlow};
      double[] d = new double[] {a[1] * offhigh, a[2] * offlow};
      double[] e = new double[] {a[2], a[3], a[4] * offlow, a[5] * offhigh};
      double[] f = new double[] {a[7], a[8] * offlow, a[9] * offhigh};
      double[] g = new double[] {a[7], 22, a[9]};
      double[] h = new double[] {a[7], a[8], a[9], 10};
      double[] aCopy = a.clone();
      double[] bCopy = b.clone();
      double[] cCopy = c.clone();
      double[] dCopy = d.clone();
      double[] eCopy = e.clone();
      double[] fCopy = f.clone();
      double[] gCopy = g.clone();
      double[] hCopy = h.clone();

      assert ff.indexOf(a, b) == 0;
      assert ff.indexOf(a, c) == 0;
      assert ff.indexOf(a, d) == 1;
      assert ff.indexOf(a, e) == 2;
      assert ff.indexOf(a, f) == 7;
      assert ff.indexOf(a, g) == -1;
      assert ff.indexOf(a, h) == -1;

      assertArraysEquals(a, aCopy);
      assertArraysEquals(b, bCopy);
      assertArraysEquals(c, cCopy);
      assertArraysEquals(d, dCopy);
      assertArraysEquals(e, eCopy);
      assertArraysEquals(f, fCopy);
      assertArraysEquals(g, gCopy);
      assertArraysEquals(h, hCopy);
    }

    // public boolean isElemMatch (double[] a1, double[] a2)
    {
      double[] f1 = new double[10];
      double[] f2 = new double[20];

      for (int j = 0; j < 10; j++) {

        initializeF1AndF2(j, f1, f2);

        // make two elements off just a little
        f2[7] = f2[7] * (1 + offset);
        f2[8] = f2[8] * (1 - offset);

        // test with each array the bigger one
        if ((j % 2) == 0) {
          assert ff.isElemMatch(f1, f2);
        } else {
          assert ff.isElemMatch(f2, f1);
        }
      }
      for (int j = 0; j < 200; j++) {

        initializeF1AndF2(j, f1, f2);

        // make two elements off just a little
        f2[7] = f2[7] * (1 + 2 * offset);
        f2[8] = f2[8] * (1 - 2 * offset);

        // test with each array the bigger one
        double[] f1Copy = f1.clone();
        double[] f2Copy = f2.clone();
        if ((j % 2) == 0) {
          assert !ff.isElemMatch(f1, f2);
        } else {
          assert !ff.isElemMatch(f2, f1);
        }
        assertArraysEquals(f1, f1Copy);
        assertArraysEquals(f2, f2Copy);
      }
    }
    {
      double[] a = new double[] {2, 1, 0};
      double[] b = new double[] {};
      double[] c = new double[] {1, 1, 1, 1};
      double[] d = new double[] {1};
      assert !ff.isElemMatch(a, b);
      assert !ff.isElemMatch(b, a);
      assert ff.isElemMatch(c, d);
      assert ff.isElemMatch(d, c);
      assert ff.isElemMatch(b, b);
    }

    // public class DoubleArrayComparatorLexical implements Comparator
    // public int compare(Object o1, Object o2)
    {
      Comparator<double[]> comparator = ff.new DoubleArrayComparatorLexical();
      double[] a0 = new double[] {};
      double[] a1 = new double[] {};
      double[] a2 = new double[] {0, 1, 2, 3};
      double[] a3 = new double[] {0, 1, 2, 3, 0};
      double[] a4 = new double[] {0, 1, 2, 3, 4};
      double[] a5 = new double[] {0, 1, 2, 3, 4};
      double[] a6 = new double[] {0, 1, 5, 3, 4};
      double[] a7 = new double[] {1, 2, 3, 4};
      double[] a0Copy = a0.clone();
      double[] a1Copy = a1.clone();
      double[] a2Copy = a2.clone();
      double[] a3Copy = a3.clone();
      double[] a4Copy = a4.clone();
      double[] a5Copy = a5.clone();
      double[] a6Copy = a6.clone();
      double[] a7Copy = a7.clone();

      assert comparator.compare(a0, a1) == 0;
      assert comparator.compare(a1, a0) == 0;
      assert comparator.compare(a1, a2) < 0;
      assert comparator.compare(a2, a1) > 0;
      assert comparator.compare(a2, a3) < 0;
      assert comparator.compare(a3, a2) > 0;
      assert comparator.compare(a3, a4) < 0;
      assert comparator.compare(a4, a3) > 0;
      assert comparator.compare(a4, a5) == 0;
      assert comparator.compare(a5, a4) == 0;
      assert comparator.compare(a5, a6) < 0;
      assert comparator.compare(a6, a5) > 0;
      assert comparator.compare(a6, a7) < 0;
      assert comparator.compare(a7, a6) > 0;
      assert comparator.compare(a1, a4) < 0;
      assert comparator.compare(a4, a1) > 0;
      assert comparator.compare(a2, a4) < 0;
      assert comparator.compare(a4, a2) > 0;
      assert comparator.compare(a6, a4) > 0;
      assert comparator.compare(a4, a6) < 0;
      assert comparator.compare(a7, a4) > 0;
      assert comparator.compare(a4, a7) < 0;

      assertArraysEquals(a0, a0Copy);
      assertArraysEquals(a1, a1Copy);
      assertArraysEquals(a2, a2Copy);
      assertArraysEquals(a3, a3Copy);
      assertArraysEquals(a4, a4Copy);
      assertArraysEquals(a5, a5Copy);
      assertArraysEquals(a6, a6Copy);
      assertArraysEquals(a7, a7Copy);
    }

    // public boolean FuzzyFloat.isSubset (double[] a1, double[] a2)
    {
      double[] f1 = new double[10];
      double[] f2 = new double[20];

      for (int j = 0; j < f2.length; j++) {
        f2[j] = j;
      }
      for (int i = 0; i < f2.length - f1.length; i++) {

        // fill up f1 with elements of f2
        for (int j = 0; j < f1.length; j++) {
          f1[j] = f2[i + j];
        }

        f1[5] = f2[i] * offhigh;

        double[] f1Copy = f1.clone();
        double[] f2Copy = f2.clone();

        assert ff.isSubset(f1, f2);
        assertArraysEquals(f1, f1Copy);
        assertArraysEquals(f2, f2Copy);
      }

      double[] a1 = new double[] {1, 5, 10};
      double[] a2 = new double[] {};
      double[] a3 = new double[] {1};
      double[] a4 = new double[] {10};
      double[] a5 = new double[] {1, 10, 15, 20};
      double[] a6 = new double[] {10, 10, 10, 10, 10, 1};

      assert ff.isSubset(a2, a1);
      assert !ff.isSubset(a1, a2);
      assert !ff.isSubset(a1, a5);
      assert ff.isSubset(a3, a1);
      assert ff.isSubset(a4, a1);
      assert ff.isSubset(a6, a1);
      assert !ff.isSubset(a1, a6);
    }
  }

  /** Initialize f2 to be the same as two copies of f1. */
  void initializeF1AndF2(int j, double @ArrayLen(10) [] f1, double @ArrayLen(20) [] f2) {

    // start two arrays out exactly equal
    for (int i = 0; i < f1.length; i++) {
      f1[i] = j + i * 10;
      f2[i] = j + i * 10;
    }

    // fill out the second half of f2 with dup of f1
    for (int i = 10; i < f2.length; i++) {
      f2[i] = j + (i - 10) * 10;
    }
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
    assert combo1.size() == 3;
    assert combo1.contains(aList);
    assert combo1.contains(bList);
    assert combo1.contains(cList);

    List<List<Object>> combo2 = CollectionsPlume.createCombinations(2, 0, abc);
    assert combo2.size() == 6;
    assert combo2.contains(aa);
    assert combo2.contains(ab);
    assert combo2.contains(ac);
    assert combo2.contains(bb);
    assert combo2.contains(bc);
    assert combo2.contains(cc);

    // public static List createCombinations (int arity, int start, int cnt)
    Integer i0 = 0;
    Integer i1 = 1;
    Integer i2 = 2;
    Integer i10 = 10;
    Integer i11 = 11;
    Integer i12 = 12;

    List<ArrayList<Integer>> combo3 = CollectionsPlume.createCombinations(1, 0, 2);
    assert combo3.size() == 3;
    assert combo3.contains(Arrays.asList(new Integer[] {i0}));
    assert combo3.contains(Arrays.asList(new Integer[] {i1}));
    assert combo3.contains(Arrays.asList(new Integer[] {i2}));

    List<ArrayList<Integer>> combo4 = CollectionsPlume.createCombinations(2, 0, 2);
    assert combo4.size() == 6;
    assert combo4.contains(Arrays.asList(new Integer[] {i0, i0}));
    assert combo4.contains(Arrays.asList(new Integer[] {i0, i1}));
    assert combo4.contains(Arrays.asList(new Integer[] {i0, i2}));
    assert combo4.contains(Arrays.asList(new Integer[] {i1, i1}));
    assert combo4.contains(Arrays.asList(new Integer[] {i1, i2}));
    assert combo4.contains(Arrays.asList(new Integer[] {i2, i2}));

    List<ArrayList<Integer>> combo5 = CollectionsPlume.createCombinations(2, 10, 12);
    assert combo5.size() == 6;
    assert combo5.contains(Arrays.asList(new Integer[] {i10, i10}));
    assert combo5.contains(Arrays.asList(new Integer[] {i10, i11}));
    assert combo5.contains(Arrays.asList(new Integer[] {i10, i12}));
    assert combo5.contains(Arrays.asList(new Integer[] {i11, i11}));
    assert combo5.contains(Arrays.asList(new Integer[] {i11, i12}));
    assert combo5.contains(Arrays.asList(new Integer[] {i12, i12}));
  }

  @Test
  public void testSplitLines() {

    String str = "one\ntwo\n\rthree\r\nfour\rfive\n\n\nsix\r\n\r\n\r\n";
    @SuppressWarnings("value") // method that returns an array is not StaticallyExecutable
    String @ArrayLen(11) [] sa = UtilPlume.splitLines(str);
    // for (String s : sa)
    //   System.out.printf ("'%s'%n", s);
    assert sa.length == 11;
    assert sa[0].equals("one");
    assert sa[1].equals("two");
    assert sa[2].equals("three");
    assert sa[3].equals("four");
    assert sa[4].equals("five");
    assert sa[5].equals("");
    assert sa[6].equals("");
    assert sa[7].equals("six");
    assert sa[8].equals("");
    assert sa[9].equals("");
    assert sa[10].equals("");
  }

  // Figure 1 from
  // http://www.boost.org/libs/graph/doc/lengauer_tarjan_dominator.htm#fig:dominator-tree-example
  private static @Nullable Map<Integer, List<@KeyFor("preds1") Integer>> preds1;
  private static @Nullable Map<Integer, List<@KeyFor("succs1") Integer>> succs1;

  @SuppressWarnings({"keyfor", "nullness"}) // test code
  @EnsuresNonNull({"preds1", "succs1"})
  private static void initializePreds1AndSucc1() {
    if (preds1 != null) {
      return;
    }

    preds1 = new LinkedHashMap<>();
    succs1 = new LinkedHashMap<>();
    for (int i = 0; i <= 7; i++) {
      preds1.put(i, new ArrayList<Integer>());
      succs1.put(i, new ArrayList<Integer>());
    }
    succs1.get(0).add(1);
    preds1.get(1).add(0);
    succs1.get(1).add(2);
    preds1.get(2).add(1);
    succs1.get(1).add(3);
    preds1.get(3).add(1);
    succs1.get(2).add(7);
    preds1.get(7).add(2);
    succs1.get(3).add(4);
    preds1.get(4).add(3);
    succs1.get(4).add(5);
    preds1.get(5).add(4);
    succs1.get(4).add(6);
    preds1.get(6).add(4);
    succs1.get(5).add(7);
    preds1.get(7).add(5);
    succs1.get(6).add(4);
    preds1.get(4).add(6);
  }

  @SuppressWarnings("nullness") // test code
  @Test
  public void testGraphPlume() {

    initializePreds1AndSucc1();

    Map<Integer, List<Integer>> dom1post = GraphPlume.dominators(succs1);
    assert dom1post.get(0).toString().equals("[7, 1, 0]");
    assert dom1post.get(1).toString().equals("[7, 1]");
    assert dom1post.get(2).toString().equals("[7, 2]");
    assert dom1post.get(3).toString().equals("[7, 5, 4, 3]");
    assert dom1post.get(4).toString().equals("[7, 5, 4]");
    assert dom1post.get(5).toString().equals("[7, 5]");
    assert dom1post.get(6).toString().equals("[7, 5, 4, 6]");
    assert dom1post.get(7).toString().equals("[7]");

    Map<Integer, List<Integer>> dom1pre = GraphPlume.dominators(preds1);
    assert dom1pre.get(0).toString().equals("[0]");
    assert dom1pre.get(1).toString().equals("[0, 1]");
    assert dom1pre.get(2).toString().equals("[0, 1, 2]");
    assert dom1pre.get(3).toString().equals("[0, 1, 3]");
    assert dom1pre.get(4).toString().equals("[0, 1, 3, 4]");
    assert dom1pre.get(5).toString().equals("[0, 1, 3, 4, 5]");
    assert dom1pre.get(6).toString().equals("[0, 1, 3, 4, 6]");
    assert dom1pre.get(7).toString().equals("[0, 1, 7]");

    // I should add some more tests.

  }
}
