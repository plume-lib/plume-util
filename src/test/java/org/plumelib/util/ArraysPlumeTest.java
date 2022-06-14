package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class ArraysPlumeTest {

  @Test
  public void testNCopies() {
    assertArrayEquals(
        new String[] {"hello", "hello", "hello", "hello"}, ArraysPlume.nCopies(4, "hello"));
    assertArrayEquals(new String[] {}, ArraysPlume.nCopies(0, "hello"));
  }

  @Test
  public void testAppend() {
    assertArrayEquals(
        new String[] {"a", "b", "c"}, ArraysPlume.append(new String[] {"a", "b"}, "c"));
    assertArrayEquals(new String[] {"a"}, ArraysPlume.append(new String[] {}, "a"));
  }

  @Test
  public void testMinAndMax() {

    // public static int min(int[] a)
    assertEquals(1, ArraysPlume.min(new int[] {1, 2, 3}));
    assertEquals(1, ArraysPlume.min(new int[] {2, 33, 1}));
    assertEquals(-2, ArraysPlume.min(new int[] {3, -2, 1}));
    assertEquals(3, ArraysPlume.min(new int[] {3}));
    assertEquals(3.1, ArraysPlume.min(new Double[] {3.1, 3.2, 3.3}));
    assertEquals(3.1, ArraysPlume.min(new Double[] {3.3, 3.2, 3.1}));

    // public static int max(int[] a)
    assertEquals(3, ArraysPlume.max(new int[] {1, 2, 3}));
    assertEquals(33, ArraysPlume.max(new int[] {2, 33, 1}));
    assertEquals(3, ArraysPlume.max(new int[] {3, -2, 1}));
    assertEquals(3, ArraysPlume.max(new int[] {3}));
    assertEquals(3.3, ArraysPlume.max(new Double[] {3.1, 3.2, 3.3}));
    assertEquals(3.3, ArraysPlume.max(new Double[] {3.3, 3.2, 3.1}));

    // public static int[] minAndMax(int[] a)
    assertArrayEquals(new int[] {1, 3}, ArraysPlume.minAndMax(new int[] {1, 2, 3}));
    assertArrayEquals(new int[] {1, 33}, ArraysPlume.minAndMax(new int[] {2, 33, 1}));
    assertArrayEquals(new int[] {-2, 3}, ArraysPlume.minAndMax(new int[] {3, -2, 1}));
    assertArrayEquals(new int[] {3, 3}, ArraysPlume.minAndMax(new int[] {3}));
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
    assertEquals(2, ArraysPlume.elementRange(new int[] {1, 2, 3}));
    assertEquals(32, ArraysPlume.elementRange(new int[] {2, 33, 1}));
    assertEquals(5, ArraysPlume.elementRange(new int[] {3, -2, 1}));
    assertEquals(0, ArraysPlume.elementRange(new int[] {3}));
  }

  @Test
  public void testSum() {

    // public static int sum(int[] a)
    assertTrue(0 == ArraysPlume.sum(new int[0]));
    assertTrue(10 == ArraysPlume.sum(new int[] {10}));
    assertTrue(10 == ArraysPlume.sum(new int[] {1, 2, 3, 4}));

    // public static int sum(int[][] a)
    assertTrue(0 == ArraysPlume.sum(new int[0][0]));
    assertTrue(78 == ArraysPlume.sum(new int[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}}));
    assertTrue(68 == ArraysPlume.sum(new int[][] {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 11, 12}}));

    // public static double sum(double[] a)
    assertTrue(0 == ArraysPlume.sum(new double[0]));
    assertTrue(3.14 == ArraysPlume.sum(new double[] {3.14}));
    assertTrue(8.624 == ArraysPlume.sum(new double[] {3.14, 2.718, -1.234, 4}));

    // public static double sum(double[][] a)
    assertTrue(0 == ArraysPlume.sum(new double[0][0]));
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

  @SuppressWarnings({
    "deprecation",
    "removal",
    "BoxedPrimitiveConstructor"
  }) // test performs == comparisons
  @Test
  public void testIndexOf_array() {

    // public static int indexOf(Object[] a, Object elt)
    // public static int indexOfEq(Object[] a, Object elt)
    Integer[] a = new Integer[10];
    for (int i = 0; i < a.length; i++) {
      a[i] = new Integer(i);
    }
    assertTrue(ArraysPlume.indexOf(a, new Integer(-1)) == -1);
    assertTrue(ArraysPlume.indexOf(a, new Integer(0)) == 0);
    assertTrue(ArraysPlume.indexOf(a, new Integer(7)) == 7);
    assertTrue(ArraysPlume.indexOf(a, new Integer(9)) == 9);
    assertTrue(ArraysPlume.indexOf(a, new Integer(10)) == -1);
    assertTrue(ArraysPlume.indexOf(a, new Integer(20)) == -1);
    assertTrue(ArraysPlume.indexOf(a, (Object) null) == -1);
    assertTrue(ArraysPlume.indexOf(a, (Object) null, 1, 5) == -1);

    assertTrue(ArraysPlume.indexOfEq(a, new Integer(-1)) == -1);
    assertTrue(ArraysPlume.indexOfEq(a, new Integer(0)) == -1);
    assertTrue(ArraysPlume.indexOfEq(a, new Integer(7)) == -1);
    assertTrue(ArraysPlume.indexOfEq(a, new Integer(9)) == -1);
    assertTrue(ArraysPlume.indexOfEq(a, new Integer(10)) == -1);
    assertTrue(ArraysPlume.indexOfEq(a, new Integer(20)) == -1);
    assertTrue(ArraysPlume.indexOfEq(a, (Object) null) == -1);
    assertTrue(ArraysPlume.indexOfEq(a, (Object) null, 1, 5) == -1);
    assertTrue(ArraysPlume.indexOfEq(a, a[0]) == 0);
    assertTrue(ArraysPlume.indexOfEq(a, a[7]) == 7);
    assertTrue(ArraysPlume.indexOfEq(a, a[9]) == 9);
  }

  @Test
  public void testIndexOf_list() {
    // public static int indexOf(List<?> a, Object elt)
    // public static int indexOf(List<?> a, Object elt, int minindex, int indexlimit)
    // public static int indexOfEq(List<?> a, Object elt, int minindex, int indexlimit)
    // public static int indexOfEq(List<?> a, Object elt)

    // These calls are no longer legal.  I could create a new version of indexOf that
    // permits nulls, one for LinkedList and one for Vector (both of which permit nulls).
    // @MinLen(1) List<?> listOfStrings = Collections.singletonList("hello");
    // assertTrue(ArraysPlume.indexOf(listOfStrings, (Object) null) == -1);
    // assertTrue(ArraysPlume.indexOf(listOfStrings, (Object) null, 0, 0) == -1);
    // assertTrue(ArraysPlume.indexOf(listOfStrings, (Object) null, 0, 1) == -1);
    // assertTrue(ArraysPlume.indexOfEq(listOfStrings, (Object) null) == -1);
    // assertTrue(ArraysPlume.indexOfEq(listOfStrings, (Object) null, 0, 0) == -1);
    // assertTrue(ArraysPlume.indexOfEq(listOfStrings, (Object) null, 0, 1) == -1);
  }

  @Test
  public void testIndexOf_array_primitive() {

    // public static int indexOf(int[] a, int elt)
    {
      int[] a = new int[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = i;
      }
      assertTrue(ArraysPlume.indexOf(a, -1) == -1);
      assertTrue(ArraysPlume.indexOf(a, 0) == 0);
      assertTrue(ArraysPlume.indexOf(a, 7) == 7);
      assertTrue(ArraysPlume.indexOf(a, 9) == 9);
      assertTrue(ArraysPlume.indexOf(a, 10) == -1);
      assertTrue(ArraysPlume.indexOf(a, 20) == -1);
    }

    // public static int indexOf(boolean[] a, boolean elt)
    {
      boolean[] a = new boolean[10];
      for (int i = 0; i < a.length; i++) {
        a[i] = false;
      }
      assertTrue(ArraysPlume.indexOf(a, true) == -1);
      assertTrue(ArraysPlume.indexOf(a, false) == 0);
      a[9] = true;
      assertTrue(ArraysPlume.indexOf(a, true) == 9);
      assertTrue(ArraysPlume.indexOf(a, false) == 0);
      a[7] = true;
      assertTrue(ArraysPlume.indexOf(a, true) == 7);
      assertTrue(ArraysPlume.indexOf(a, false) == 0);
      a[0] = true;
      assertTrue(ArraysPlume.indexOf(a, true) == 0);
      assertTrue(ArraysPlume.indexOf(a, false) == 1);
      for (int i = 0; i < a.length; i++) {
        a[i] = true;
      }
      assertTrue(ArraysPlume.indexOf(a, true) == 0);
      assertTrue(ArraysPlume.indexOf(a, false) == -1);
    }
  }

  @SuppressWarnings({
    "deprecation",
    "removal",
    "BoxedPrimitiveConstructor"
  }) // test performs == comparisons
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

      assertTrue(ArraysPlume.indexOf(a, b) == 0);
      assertTrue(ArraysPlume.indexOfEq(a, b) == 0);
      assertTrue(ArraysPlume.indexOf(a, c) == 0);
      assertTrue(ArraysPlume.indexOfEq(a, c) == 0);
      assertTrue(ArraysPlume.indexOf(a, c2) == 0);
      assertTrue(ArraysPlume.indexOfEq(a, c2) == -1);
      assertTrue(ArraysPlume.indexOf(a, d) == 1);
      assertTrue(ArraysPlume.indexOfEq(a, d) == 1);
      assertTrue(ArraysPlume.indexOf(a, d2) == 1);
      assertTrue(ArraysPlume.indexOfEq(a, d2) == -1);
      assertTrue(ArraysPlume.indexOf(a, e) == 2);
      assertTrue(ArraysPlume.indexOfEq(a, e) == 2);
      assertTrue(ArraysPlume.indexOf(a, e2) == 2);
      assertTrue(ArraysPlume.indexOfEq(a, e2) == -1);
      assertTrue(ArraysPlume.indexOf(a, f) == 7);
      assertTrue(ArraysPlume.indexOfEq(a, f) == 7);
      assertTrue(ArraysPlume.indexOf(a, f2) == 7);
      assertTrue(ArraysPlume.indexOfEq(a, f2) == -1);
      assertTrue(ArraysPlume.indexOf(a, g) == 7);
      assertTrue(ArraysPlume.indexOfEq(a, g) == -1);
      assertTrue(ArraysPlume.indexOf(a, h) == -1);
      assertTrue(ArraysPlume.indexOfEq(a, h) == -1);
      assertTrue(ArraysPlume.indexOf(i, j) == 1);
      assertTrue(ArraysPlume.indexOfEq(i, j) == 1);
      assertTrue(ArraysPlume.indexOf(a, i) == -1);
      assertTrue(ArraysPlume.indexOfEq(a, i) == -1);
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

      assertTrue(ArraysPlume.indexOf(a, b) == 0);
      assertTrue(ArraysPlume.indexOf(a, c) == 0);
      assertTrue(ArraysPlume.indexOf(a, d) == 1);
      assertTrue(ArraysPlume.indexOf(a, e) == 2);
      assertTrue(ArraysPlume.indexOf(a, f) == 7);
      assertTrue(ArraysPlume.indexOf(a, g) == -1);
      assertTrue(ArraysPlume.indexOf(a, h) == -1);

      // Tests pulled from actual StackAr data
      int[] origTheArray =
          new int[] {
            1267757, 1267757, 1267757, 1267757, 1267757, 1267757, 1267757, 1267757, 1267757,
            1267757, 1267757, 0, 0, 0, 0, 0, 0, 0, 0, 0
          };

      int[] postTheArray = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      assertTrue(ArraysPlume.indexOf(postTheArray, origTheArray) == -1);
      assertTrue(ArraysPlume.indexOf(origTheArray, postTheArray) == -1);
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
      assertTrue(ArraysPlume.toString((Object[]) null).equals("null"));
      assertTrue(ArraysPlume.toStringQuoted((Object[]) null).equals("null"));
      assertTrue(ArraysPlume.toString((List<?>) null).equals("null"));
      assertTrue(ArraysPlume.toStringQuoted((List<?>) null).equals("null"));
      assertEquals(
          "[3.14, null, \"hello\"]",
          ArraysPlume.toStringQuoted(Arrays.asList(new Object[] {3.14, null, "hello"})));
      assertEquals(
          "[\"a\\\"quote\", \"b\", \"c\\\\backslash\", \"d\\nnewline\"]",
          ArraysPlume.toStringQuoted(
              Arrays.asList(new Object[] {"a\"quote", "b", "c\\backslash", "d\nnewline"})));
    }

    // static String toString(int[] a)
    assertTrue(Arrays.toString(new int[] {}).equals("[]"));
    assertTrue(Arrays.toString(new int[] {0}).equals("[0]"));
    assertTrue(Arrays.toString(new int[] {0, 1, 2}).equals("[0, 1, 2]"));
    assertEquals(
        "[\"a\\\"quote\", \"b\", \"c\\\\backslash\", \"d\\nnewline\"]",
        ArraysPlume.toStringQuoted(new Object[] {"a\"quote", "b", "c\\backslash", "d\nnewline"}));
  }

  @Test
  public void test_sorted() {

    // public static boolean sorted(int[] a)
    assertTrue(ArraysPlume.sorted(new int[] {0, 1, 2}));
    assertTrue(ArraysPlume.sorted(new int[] {0, 1, 2, 2, 3, 3}));
    assertTrue(ArraysPlume.sorted(new int[] {}));
    assertTrue(ArraysPlume.sorted(new int[] {0}));
    assertTrue(ArraysPlume.sorted(new int[] {0, 1}));
    assertTrue(!ArraysPlume.sorted(new int[] {1, 0}));
    assertTrue(!ArraysPlume.sorted(new int[] {0, 1, 2, 1, 2, 3}));
  }

  @Test
  public void test_noDuplicates() {
    // public static int noDuplicates(int[] a)
    assertTrue(ArraysPlume.noDuplicates(new int[] {1, 2, 3, 5, 4, 0}) == true);
    assertTrue(ArraysPlume.noDuplicates(new int[] {1, 2, 3, 5, 4, 100}) == true);
    assertTrue(ArraysPlume.noDuplicates(new int[] {2, 2, 3, 5, 4, 0}) == false);
    assertTrue(ArraysPlume.noDuplicates(new int[] {1, 2, 3, 5, 4, 1}) == false);
    assertTrue(ArraysPlume.noDuplicates(new int[] {1, 2, -3, -5, 4, 0}) == true);
    assertTrue(ArraysPlume.noDuplicates(new int[] {1, 2, -2, -2, 4, 100}) == false);
    assertTrue(ArraysPlume.noDuplicates(new int[] {}) == true);
    assertTrue(ArraysPlume.noDuplicates(new int[] {42}) == true);

    // public static int noDuplicates(long[] a)
    assertTrue(ArraysPlume.noDuplicates(new long[] {1, 2, 3, 5, 4, 0}) == true);
    assertTrue(ArraysPlume.noDuplicates(new long[] {1, 2, 3, 5, 4, 100}) == true);
    assertTrue(ArraysPlume.noDuplicates(new long[] {2, 2, 3, 5, 4, 0}) == false);
    assertTrue(ArraysPlume.noDuplicates(new long[] {1, 2, 3, 5, 4, 1}) == false);
    assertTrue(ArraysPlume.noDuplicates(new long[] {1, 2, -3, -5, 4, 0}) == true);
    assertTrue(ArraysPlume.noDuplicates(new long[] {1, 2, -2, -2, 4, 100}) == false);
    assertTrue(ArraysPlume.noDuplicates(new long[] {}) == true);
    assertTrue(ArraysPlume.noDuplicates(new long[] {42}) == true);

    // public static int noDuplicates(double[] a)
    assertTrue(ArraysPlume.noDuplicates(new double[] {1, 2, 3, 5, 4, 0}) == true);
    assertTrue(ArraysPlume.noDuplicates(new double[] {1, 2, 3, 5, 4, 100}) == true);
    assertTrue(ArraysPlume.noDuplicates(new double[] {2, 2, 3, 5, 4, 0}) == false);
    assertTrue(ArraysPlume.noDuplicates(new double[] {1, 2, 3, 5, 4, 1}) == false);
    assertTrue(ArraysPlume.noDuplicates(new double[] {1., 1.001, -3, -5, 4, 0}) == true);
    assertTrue(ArraysPlume.noDuplicates(new double[] {1., 2, -2.00, -2, 4, 100}) == false);
    assertTrue(ArraysPlume.noDuplicates(new double[] {}) == true);
    assertTrue(ArraysPlume.noDuplicates(new double[] {42}) == true);

    // public static int noDuplicates(String[] a)
    assertTrue(ArraysPlume.noDuplicates(new String[] {"1", "2", "3", "5", "4", "0"}) == true);
    assertTrue(ArraysPlume.noDuplicates(new String[] {"A", "a", "foo", "Foo", ""}) == true);
    assertTrue(ArraysPlume.noDuplicates(new String[] {" ", " "}) == false);
    assertTrue(ArraysPlume.noDuplicates(new String[] {"  ", " "}) == true);
  }

  @Test
  public void test_fnIsPermutation() {
    // public static boolean fnIsPermutation(int[] a)
    assertTrue(ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 3}) == true);
    assertTrue(ArraysPlume.fnIsPermutation(new int[] {1, 2, 3, 0}) == true);
    assertTrue(ArraysPlume.fnIsPermutation(new int[] {3, 2, 1, 0}) == true);
    assertTrue(ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 2}) == false);
    assertTrue(ArraysPlume.fnIsPermutation(new int[] {0, -1, 2, 3}) == false);
    assertTrue(ArraysPlume.fnIsPermutation(new int[] {0, 1, 2, 4}) == false);
    assertTrue(ArraysPlume.fnIsPermutation(new int[] {0, 0, 0, 0}) == false);
  }

  @Test
  public void test_fnIsTotal() {
    // public static boolean fnIsTotal(int[] a)
    assertTrue(ArraysPlume.fnIsTotal(new int[] {0, 1, 2, 3}) == true);
    assertTrue(ArraysPlume.fnIsTotal(new int[] {1, 2, 3, 0}) == true);
    assertTrue(ArraysPlume.fnIsTotal(new int[] {3, 2, 1, 0}) == true);
    assertTrue(ArraysPlume.fnIsTotal(new int[] {0, 1, 2, 2}) == true);
    assertTrue(ArraysPlume.fnIsTotal(new int[] {-1, 0, 2, 3}) == false);
    assertTrue(ArraysPlume.fnIsTotal(new int[] {0, -1, 2, 3}) == false);
    assert ArraysPlume.fnIsTotal(new int[] {0, -2, 1, 3}) == true; // weird
    assertTrue(ArraysPlume.fnIsTotal(new int[] {0, 2, 3, -1}) == false);
    assertTrue(ArraysPlume.fnIsTotal(new int[] {0, 1, 2, 4}) == true);
    assertTrue(ArraysPlume.fnIsTotal(new int[] {0, 0, 0, 0}) == true);
  }

  @SuppressWarnings({
    "lowerbound:argument",
    "index:argument"
  }) // https://github.com/kelloggm/checker-framework/issues/147
  @Test
  public void testFunctions() {

    // public static int[] fnIdentity(int length)
    assertArrayEquals(ArraysPlume.fnIdentity(0), new int[] {});
    assertArrayEquals(ArraysPlume.fnIdentity(1), new int[] {0});
    assertArrayEquals(ArraysPlume.fnIdentity(2), new int[] {0, 1});
    assertArrayEquals(ArraysPlume.fnIdentity(3), new int[] {0, 1, 2});

    // public static int[] fnInversePermutation(int[] a)
    assertArrayEquals(
        ArraysPlume.fnInversePermutation(new int[] {0, 1, 2, 3}), new int[] {0, 1, 2, 3});
    assertArrayEquals(
        ArraysPlume.fnInversePermutation(new int[] {1, 2, 3, 0}), new int[] {3, 0, 1, 2});
    assertArrayEquals(
        ArraysPlume.fnInversePermutation(new int[] {3, 2, 1, 0}), new int[] {3, 2, 1, 0});

    // public static int[] fnInverse(int[] a, int arange)
    assertArrayEquals(ArraysPlume.fnInverse(new int[] {0, 1, 2, 3}, 4), new int[] {0, 1, 2, 3});
    assertArrayEquals(ArraysPlume.fnInverse(new int[] {1, 2, 3, 0}, 4), new int[] {3, 0, 1, 2});
    assertArrayEquals(ArraysPlume.fnInverse(new int[] {3, 2, 1, 0}, 4), new int[] {3, 2, 1, 0});
    try {
      ArraysPlume.fnInverse(new int[] {1, 0, 3, 0}, 4);
      throw new Error();
    } catch (UnsupportedOperationException e) {
      assertTrue(
          e.getMessage() != null && e.getMessage().equals("Not invertible; a[1]=0 and a[3]=0"));
    }
    assertArrayEquals(ArraysPlume.fnInverse(new int[] {5}, 6), new int[] {-1, -1, -1, -1, -1, 0});
    assertArrayEquals(
        ArraysPlume.fnInverse(new int[] {1, 2, 3, 5}, 6), new int[] {-1, 0, 1, 2, -1, 3});

    try {
      assertArrayEquals(
          ArraysPlume.fnInverse(new int[] {100, 101, 102, 103}, 4), new int[] {40, 41, 42, 43});
      throw new Error();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage() != null && e.getMessage().equals("Bad range value: a[0]=100"));
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

      assertArrayEquals(ArraysPlume.fnCompose(a1, a1), a1);
      assertArrayEquals(ArraysPlume.fnCompose(a2, a2), new int[] {2, 3, 0, 1});
      assertArrayEquals(ArraysPlume.fnCompose(a3, a3), a1);
      assertArrayEquals(ArraysPlume.fnCompose(a4, a5), new int[] {0, 5, 0, 1});
      assertArrayEquals(ArraysPlume.fnCompose(a7, a8), new int[] {5});
      assertArrayEquals(ArraysPlume.fnCompose(a9, a10), new int[] {2, 3, 5, -1});
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

        assertTrue(ArraysPlume.isSubset(f1, f2));
        assertArrayEquals(f1, f1Copy);
        assertArrayEquals(f2, f2Copy);
      }

      double[] a1 = new double[] {1, 5, 10};
      double[] a2 = new double[] {};
      double[] a3 = new double[] {1};
      double[] a4 = new double[] {10};
      double[] a5 = new double[] {1, 10, 15, 20};
      double[] a6 = new double[] {10, 10, 10, 10, 10, 1};

      assertTrue(ArraysPlume.isSubset(a2, a1));
      assertTrue(!ArraysPlume.isSubset(a1, a2));
      assertTrue(!ArraysPlume.isSubset(a1, a5));
      assertTrue(ArraysPlume.isSubset(a3, a1));
      assertTrue(ArraysPlume.isSubset(a4, a1));
      assertTrue(ArraysPlume.isSubset(a6, a1));
      assertTrue(!ArraysPlume.isSubset(a1, a6));
    }
  }

  @Test
  public void test_sameContents() {
    assertTrue(ArraysPlume.sameContents(new String[] {}, new String[] {}));
    assertTrue(ArraysPlume.sameContents(new String[] {"a"}, new String[] {"a"}));
    assertTrue(ArraysPlume.sameContents(new String[] {"a", "b"}, new String[] {"a", "b"}));
    assertTrue(ArraysPlume.sameContents(new String[] {"a", "b"}, new String[] {"b", "a"}));
    assertTrue(
        ArraysPlume.sameContents(new String[] {"a", "b", "c"}, new String[] {"c", "b", "a"}));
    assertTrue(
        ArraysPlume.sameContents(
            new String[] {"a", "b", "c"}, new String[] {"c", "b", "a", "b", "b"}));
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

    assertTrue(iacl.compare(a0, a1) == 0);
    assertTrue(iaclf.compare(a0, a1) == 0);
    assertTrue(iacl.compare(a1, a0) == 0);
    assertTrue(iaclf.compare(a1, a0) == 0);
    assertTrue(iacl.compare(a1, a2) < 0);
    assertTrue(iaclf.compare(a1, a2) < 0);
    assertTrue(iacl.compare(a2, a1) > 0);
    assertTrue(iaclf.compare(a2, a1) > 0);
    assertTrue(iacl.compare(a2, a3) < 0);
    assertTrue(iaclf.compare(a2, a3) < 0);
    assertTrue(iacl.compare(a3, a2) > 0);
    assertTrue(iaclf.compare(a3, a2) > 0);
    assertTrue(iacl.compare(a3, a4) < 0);
    assertTrue(iaclf.compare(a3, a4) < 0);
    assertTrue(iacl.compare(a4, a3) > 0);
    assertTrue(iaclf.compare(a4, a3) > 0);
    assertTrue(iacl.compare(a4, a5) == 0);
    assertTrue(iaclf.compare(a4, a5) == 0);
    assertTrue(iacl.compare(a5, a4) == 0);
    assertTrue(iaclf.compare(a5, a4) == 0);
    assertTrue(iacl.compare(a5, a6) < 0);
    assertTrue(iaclf.compare(a5, a6) < 0);
    assertTrue(iacl.compare(a6, a5) > 0);
    assertTrue(iaclf.compare(a6, a5) > 0);
    assertTrue(iacl.compare(a6, a7) < 0);
    assertTrue(iaclf.compare(a6, a7) > 0);
    assertTrue(iacl.compare(a7, a6) > 0);
    assertTrue(iaclf.compare(a7, a6) < 0);
    assertTrue(iacl.compare(a1, a4) < 0);
    assertTrue(iaclf.compare(a1, a4) < 0);
    assertTrue(iacl.compare(a4, a1) > 0);
    assertTrue(iaclf.compare(a4, a1) > 0);
    assertTrue(iacl.compare(a2, a4) < 0);
    assertTrue(iaclf.compare(a2, a4) < 0);
    assertTrue(iacl.compare(a4, a2) > 0);
    assertTrue(iaclf.compare(a4, a2) > 0);
    assertTrue(iacl.compare(a6, a4) > 0);
    assertTrue(iaclf.compare(a6, a4) > 0);
    assertTrue(iacl.compare(a4, a6) < 0);
    assertTrue(iaclf.compare(a4, a6) < 0);
    assertTrue(iacl.compare(a7, a4) > 0);
    assertTrue(iaclf.compare(a7, a4) < 0);
    assertTrue(iacl.compare(a4, a7) < 0);
    assertTrue(iaclf.compare(a4, a7) > 0);
    assertTrue(iacl.compare(a8, a9) < 0);
    assertTrue(iaclf.compare(a8, a9) < 0);
    assertTrue(iacl.compare(a10, a7) < 0);
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

    assertTrue(lacl.compare(a0, a1) == 0);
    assertTrue(laclf.compare(a0, a1) == 0);
    assertTrue(lacl.compare(a1, a0) == 0);
    assertTrue(laclf.compare(a1, a0) == 0);
    assertTrue(lacl.compare(a1, a2) < 0);
    assertTrue(laclf.compare(a1, a2) < 0);
    assertTrue(lacl.compare(a2, a1) > 0);
    assertTrue(laclf.compare(a2, a1) > 0);
    assertTrue(lacl.compare(a2, a3) < 0);
    assertTrue(laclf.compare(a2, a3) < 0);
    assertTrue(lacl.compare(a3, a2) > 0);
    assertTrue(laclf.compare(a3, a2) > 0);
    assertTrue(lacl.compare(a3, a4) < 0);
    assertTrue(laclf.compare(a3, a4) < 0);
    assertTrue(lacl.compare(a4, a3) > 0);
    assertTrue(laclf.compare(a4, a3) > 0);
    assertTrue(lacl.compare(a4, a5) == 0);
    assertTrue(laclf.compare(a4, a5) == 0);
    assertTrue(lacl.compare(a5, a4) == 0);
    assertTrue(laclf.compare(a5, a4) == 0);
    assertTrue(lacl.compare(a5, a6) < 0);
    assertTrue(laclf.compare(a5, a6) < 0);
    assertTrue(lacl.compare(a6, a5) > 0);
    assertTrue(laclf.compare(a6, a5) > 0);
    assertTrue(lacl.compare(a6, a7) < 0);
    assertTrue(laclf.compare(a6, a7) > 0);
    assertTrue(lacl.compare(a7, a6) > 0);
    assertTrue(laclf.compare(a7, a6) < 0);
    assertTrue(lacl.compare(a1, a4) < 0);
    assertTrue(laclf.compare(a1, a4) < 0);
    assertTrue(lacl.compare(a4, a1) > 0);
    assertTrue(laclf.compare(a4, a1) > 0);
    assertTrue(lacl.compare(a2, a4) < 0);
    assertTrue(laclf.compare(a2, a4) < 0);
    assertTrue(lacl.compare(a4, a2) > 0);
    assertTrue(laclf.compare(a4, a2) > 0);
    assertTrue(lacl.compare(a6, a4) > 0);
    assertTrue(laclf.compare(a6, a4) > 0);
    assertTrue(lacl.compare(a4, a6) < 0);
    assertTrue(laclf.compare(a4, a6) < 0);
    assertTrue(lacl.compare(a7, a4) > 0);
    assertTrue(laclf.compare(a7, a4) < 0);
    assertTrue(lacl.compare(a4, a7) < 0);
    assertTrue(laclf.compare(a4, a7) > 0);
    assertTrue(lacl.compare(a8, a9) < 0);
    assertTrue(laclf.compare(a8, a9) < 0);
    assertTrue(lacl.compare(a10, a7) < 0);
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

    assertTrue(dacl.compare(a0, a1) == 0);
    assertTrue(dacl.compare(a1, a0) == 0);
    assertTrue(dacl.compare(a1, a2) < 0);
    assertTrue(dacl.compare(a2, a1) > 0);
    assertTrue(dacl.compare(a2, a3) < 0);
    assertTrue(dacl.compare(a3, a2) > 0);
    assertTrue(dacl.compare(a3, a4) < 0);
    assertTrue(dacl.compare(a4, a3) > 0);
    assertTrue(dacl.compare(a4, a5) == 0);
    assertTrue(dacl.compare(a5, a4) == 0);
    assertTrue(dacl.compare(a5, a6) < 0);
    assertTrue(dacl.compare(a6, a5) > 0);
    assertTrue(dacl.compare(a6, a7) < 0);
    assertTrue(dacl.compare(a7, a6) > 0);
    assertTrue(dacl.compare(a1, a4) < 0);
    assertTrue(dacl.compare(a4, a1) > 0);
    assertTrue(dacl.compare(a2, a4) < 0);
    assertTrue(dacl.compare(a4, a2) > 0);
    assertTrue(dacl.compare(a6, a4) > 0);
    assertTrue(dacl.compare(a4, a6) < 0);
    assertTrue(dacl.compare(a7, a4) > 0);
    assertTrue(dacl.compare(a4, a7) < 0);

    // Test the comparisons on small/large numbers
    assertTrue(dacl.compare(a8, a9) > 0);
    assertTrue(dacl.compare(a10, a11) < 0);
    assertTrue(dacl.compare(a11, a12) < 0);
    assertTrue(dacl.compare(a12, a13) > 0);
    assertTrue(dacl.compare(a13, a11) < 0);
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

    assertTrue(cacl.compare(a0, a1) == 0);
    assertTrue(caclf.compare(a0, a1) == 0);
    assertTrue(cacl.compare(a1, a0) == 0);
    assertTrue(caclf.compare(a1, a0) == 0);
    assertTrue(cacl.compare(a1, a2) < 0);
    assertTrue(caclf.compare(a1, a2) < 0);
    assertTrue(cacl.compare(a2, a1) > 0);
    assertTrue(caclf.compare(a2, a1) > 0);
    assertTrue(cacl.compare(a2, a3) < 0);
    assertTrue(caclf.compare(a2, a3) < 0);
    assertTrue(cacl.compare(a3, a2) > 0);
    assertTrue(caclf.compare(a3, a2) > 0);
    assertTrue(cacl.compare(a3, a4) < 0);
    assertTrue(caclf.compare(a3, a4) < 0);
    assertTrue(cacl.compare(a4, a3) > 0);
    assertTrue(caclf.compare(a4, a3) > 0);
    assertTrue(cacl.compare(a4, a5) == 0);
    assertTrue(caclf.compare(a4, a5) == 0);
    assertTrue(cacl.compare(a5, a4) == 0);
    assertTrue(caclf.compare(a5, a4) == 0);
    assertTrue(cacl.compare(a5, a6) < 0);
    assertTrue(caclf.compare(a5, a6) < 0);
    assertTrue(cacl.compare(a6, a5) > 0);
    assertTrue(caclf.compare(a6, a5) > 0);
    assertTrue(cacl.compare(a6, a7) < 0);
    assertTrue(caclf.compare(a6, a7) > 0);
    assertTrue(cacl.compare(a7, a6) > 0);
    assertTrue(caclf.compare(a7, a6) < 0);
    assertTrue(cacl.compare(a1, a4) < 0);
    assertTrue(caclf.compare(a1, a4) < 0);
    assertTrue(cacl.compare(a4, a1) > 0);
    assertTrue(caclf.compare(a4, a1) > 0);
    assertTrue(cacl.compare(a2, a4) < 0);
    assertTrue(caclf.compare(a2, a4) < 0);
    assertTrue(cacl.compare(a4, a2) > 0);
    assertTrue(caclf.compare(a4, a2) > 0);
    assertTrue(cacl.compare(a6, a4) > 0);
    assertTrue(caclf.compare(a6, a4) > 0);
    assertTrue(cacl.compare(a4, a6) < 0);
    assertTrue(caclf.compare(a4, a6) < 0);
    assertTrue(cacl.compare(a7, a4) > 0);
    assertTrue(caclf.compare(a7, a4) < 0);
    assertTrue(cacl.compare(a8, a1) > 0);
    assertTrue(caclf.compare(a8, a1) > 0);
    assertTrue(cacl.compare(a1, a8) < 0);
    assertTrue(caclf.compare(a1, a8) < 0);
    assertTrue(cacl.compare(a8, a2) < 0);
    assertTrue(caclf.compare(a8, a2) > 0);
    assertTrue(cacl.compare(a2, a8) > 0);
    assertTrue(caclf.compare(a2, a8) < 0);
    assertTrue(cacl.compare(a8, a3) < 0);
    assertTrue(caclf.compare(a8, a3) < 0);
    assertTrue(cacl.compare(a3, a8) > 0);
    assertTrue(caclf.compare(a3, a8) > 0);
  }

  @Test
  public void test_anyNull() {

    // public static boolean anyNull(Object[] a)

    Object o = new Object();
    assertTrue(ArraysPlume.anyNull(new Object[] {}) == false);
    assertTrue(ArraysPlume.anyNull(new Object[] {null}) == true);
    assertTrue(ArraysPlume.anyNull(new Object[] {null, null}) == true);
    assertTrue(ArraysPlume.anyNull(new Object[] {o}) == false);
    assertTrue(ArraysPlume.anyNull(new Object[] {o, o}) == false);
    assertTrue(ArraysPlume.anyNull(new Object[] {o, null, null}) == true);
    assertTrue(ArraysPlume.anyNull(new Object[] {null, o, null}) == true);
    assertTrue(ArraysPlume.anyNull(new Object[] {o, null, o}) == true);
    assertTrue(ArraysPlume.anyNull(new Object[] {null, o, o}) == true);
    assertTrue(ArraysPlume.anyNull(new Object[][] {}) == false);
    assertTrue(ArraysPlume.anyNull(new Object[][] {null}) == true);
    // Extraneous @Nullable on the following lines are due to https://tinyurl.com/cfissue/599
    assertTrue(ArraysPlume.anyNull(new @Nullable Object[][] {new Object[] {null}}) == false);
    assertTrue(ArraysPlume.anyNull(new @Nullable Object[][] {new Object[] {null}, null}) == true);
    assert ArraysPlume.anyNull(new @Nullable Object[][] {new Object[] {null}, new Object[] {o}})
        == false;
  }

  @Test
  public void test_allNull() {

    // public static boolean allNull(Object[] a)

    Object o = new Object();
    assertTrue(ArraysPlume.allNull(new Object[] {}) == true);
    assertTrue(ArraysPlume.allNull(new Object[] {null}) == true);
    assertTrue(ArraysPlume.allNull(new Object[] {null, null}) == true);
    assertTrue(ArraysPlume.allNull(new Object[] {o}) == false);
    assertTrue(ArraysPlume.allNull(new Object[] {o, o}) == false);
    assertTrue(ArraysPlume.allNull(new Object[] {o, null, null}) == false);
    assertTrue(ArraysPlume.allNull(new Object[] {null, o, null}) == false);
    assertTrue(ArraysPlume.allNull(new Object[] {o, null, o}) == false);
    assertTrue(ArraysPlume.allNull(new Object[] {null, o, o}) == false);
    assertTrue(ArraysPlume.allNull(new Object[][] {}) == true);
    assertTrue(ArraysPlume.allNull(new Object[][] {null}) == true);
    assertTrue(ArraysPlume.allNull(new Object[][] {null, null}) == true);
    assertTrue(ArraysPlume.allNull(new @Nullable Object[][] {new Object[] {null}}) == false);
    assertTrue(ArraysPlume.allNull(new @Nullable Object[][] {new Object[] {null}, null}) == false);
    assert ArraysPlume.allNull(new @Nullable Object[][] {new Object[] {null}, new Object[] {o}})
        == false;
  }

  /** Returns true if the toString of each element in elts equals the corresponding string. */
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

  List<String> abcdefList = Arrays.asList("a", "b", "c", "d", "e", "f");
  List<String> abcList = Arrays.asList("a", "b", "c");
  List<String> defList = Arrays.asList("d", "e", "f");

  String[] abcdefArray = new String[] {"a", "b", "c", "d", "e", "f"};
  String[] abcArray = new String[] {"a", "b", "c"};
  String[] defArray = new String[] {"d", "e", "f"};
  String[] emptyArray = new String[] {};

  Object[] abcdefArrayObject = new Object[] {"a", "b", "c", "d", "e", "f"};
  Object[] abcArrayObject = new Object[] {"a", "b", "c"};
  Object[] defArrayObject = new Object[] {"d", "e", "f"};
  Object[] emptyArrayObject = new Object[] {};

  @Test
  public void testConcatenate() {
    String[] abcdefArray2 = ArraysPlume.concatenate(abcArray, defArray);
    assertArrayEquals(abcdefArray, abcdefArray2);
    assertNotSame(abcdefArray, abcdefArray2);

    String[] abcArray2 = ArraysPlume.concatenate(abcArray, emptyArray);
    assertArrayEquals(abcArray, abcArray2);
    assertNotSame(abcArray, abcArray2);

    String[] abcArray3 = ArraysPlume.concatenate(emptyArray, abcArray);
    assertArrayEquals(abcArray, abcArray3);
    assertNotSame(abcArray, abcArray3);
  }

  @Test
  public void testConcat() {
    Instant[] da1 = new Instant[] {Instant.now()};
    Instant[] da2 = new Instant[] {Instant.now()};
    @SuppressWarnings("UnusedVariable")
    Instant[] da3 = ArraysPlume.concat(da1, da2);

    assertArrayEquals(abcdefList.toArray(), ArraysPlume.concat(abcList, defList));

    assertArrayEquals(abcdefArray, ArraysPlume.concat(abcArray, defArray));
    assertSame(abcArray, ArraysPlume.concat(abcArray, emptyArray));
    assertSame(abcArray, ArraysPlume.concat(emptyArray, abcArray));

    assertArrayEquals(abcdefArrayObject, ArraysPlume.concat(abcArrayObject, defArrayObject));
    assertSame(abcArrayObject, ArraysPlume.concat(abcArrayObject, emptyArrayObject));
    assertSame(abcArrayObject, ArraysPlume.concat(emptyArrayObject, abcArrayObject));
  }
}
