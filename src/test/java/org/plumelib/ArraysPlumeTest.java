package org.plumelib.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.MinLen;
import org.junit.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class ArraysPlumeTest {

  ///////////////////////////////////////////////////////////////////////////
  /// Helper functions
  ///

  private static void assertArraysEquals(int @Nullable [] a1, int @Nullable [] a2) {
    boolean result = Arrays.equals(a1, a2);
    if (!result) {
      System.out.println("Arrays differ: " + Arrays.toString(a1) + ", " + Arrays.toString(a2));
    }
    assert result;
    //      assert(Arrays.equals(a1, a2),
    //         "Arrays differ: " + ArraysPlume.toString(a1) + ", " + ArraysPlume.toString(a2));
  }

  private static void assertArraysEquals(double[] a1, double[] a2) {
    boolean result = Arrays.equals(a1, a2);
    if (!result) {
      System.out.println(
          "Arrays differ: " + ArraysPlume.toString(a1) + ", " + ArraysPlume.toString(a2));
    }
    assert result;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Now the actual testing
  ///

  @Test
  public void testMinAndMax() {

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
  public void testSum() {

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

  @Test
  public void testIndexOf_array() {

    // public static int indexOf(Object[] a, Object elt)
    // public static int indexOfEq(Object[] a, Object elt)
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

  @Test
  public void testIndexOf_list() {
    // public static int indexOf(List<?> a, Object elt)
    // public static int indexOf(List<?> a, Object elt, int minindex, int indexlimit)
    // public static int indexOfEq(List<?> a, Object elt, int minindex, int indexlimit)
    // public static int indexOfEq(List<?> a, Object elt)

    @SuppressWarnings("value") // annotated JDK doesn't have @MinLen on singletonList yet
    @MinLen(1) List<?> listOfStrings = Collections.singletonList("hello");
    assert ArraysPlume.indexOf(listOfStrings, (Object) null) == -1;
    assert ArraysPlume.indexOf(listOfStrings, (Object) null, 0, 0) == -1;
    assert ArraysPlume.indexOf(listOfStrings, (Object) null, 0, 1) == -1;
    assert ArraysPlume.indexOfEq(listOfStrings, (Object) null) == -1;
    assert ArraysPlume.indexOfEq(listOfStrings, (Object) null, 0, 0) == -1;
    assert ArraysPlume.indexOfEq(listOfStrings, (Object) null, 0, 1) == -1;
  }

  @Test
  public void testIndexOf_array_primitive() {

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
  }

  @Test
  public void testIndexOf_array_array() {

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
  public void testSubarray() {

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
  public void testPrinting() {

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
  public void test_sorted() {

    // public static boolean sorted(int[] a)
    assert ArraysPlume.sorted(new int[] {0, 1, 2});
    assert ArraysPlume.sorted(new int[] {0, 1, 2, 2, 3, 3});
    assert ArraysPlume.sorted(new int[] {});
    assert ArraysPlume.sorted(new int[] {0});
    assert ArraysPlume.sorted(new int[] {0, 1});
    assert !ArraysPlume.sorted(new int[] {1, 0});
    assert !ArraysPlume.sorted(new int[] {0, 1, 2, 1, 2, 3});
  }

  @Test
  public void test_noDuplicates() {
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
  }

  @Test
  public void test_fnIsPermutation() {
    // public static boolean fnIsPermutation(int[] a)
    assert ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 3}) == true;
    assert ArraysPlume.fnIsPermutation(new int[] {1, 2, 3, 0}) == true;
    assert ArraysPlume.fnIsPermutation(new int[] {3, 2, 1, 0}) == true;
    assert ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 2}) == false;
    assert ArraysPlume.fnIsPermutation(new int[] {0, -1, 2, 3}) == false;
    assert ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 4}) == false;
    assert ArraysPlume.fnIsPermutation(new int[] {0, 0, 0, 0}) == false;
  }

  @Test
  public void test_fnIsTotal() {
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
  public void testFunctions() {

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
  public void test_isSubset() {

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
  public void test_IntArrayComparator() {

    // public static class IntArrayComparatorLexical implements Comparator
    // public static class IntArrayComparatorLengthFirst implements Comparator

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

  @Test
  public void test_LongArrayComparator() {

    // public static class LongArrayComparatorLexical implements Comparator
    // public static class LongArrayComparatorLengthFirst implements Comparator

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

  @Test
  public void test_DoubleArrayComparatorLexical() {

    // public static class DoubleArrayComparatorLexical implements Comparator

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

  @Test
  public void test_ComparableArrayComparator() {

    // public static class ObjectArrayComparatorLexical implements Comparator
    // public static class ObjectArrayComparatorLengthFirst implements Comparator

    // public static final class ComparableArrayComparatorLexical implements Comparator
    // public static final class ComparableArrayComparatorLengthFirst implements Comparator

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

  @Test
  public void test_anyNull() {

    // public static boolean anyNull(Object[] a)

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

  @Test
  public void test_allNull() {

    // public static boolean allNull(Object[] a)

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
  public void testPartitioning() {

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
  public void testConcat() {
    Date[] da1 = new Date[] {new Date()};
    Date[] da2 = new Date[] {new Date()};
    System.out.println("concat result: " + ArraysPlume.concat(da1, da2));
    Date[] da3 = ArraysPlume.concat(da1, da2);
  }
}
