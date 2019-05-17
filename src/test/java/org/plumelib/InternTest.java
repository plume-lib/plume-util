package org.plumelib.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.Test;

/** Test code for the plume package. */
@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class InternTest {

  static class InternTestHelper {
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

    InternTestHelper internTestHelper = new InternTestHelper();
    internTestHelper.test(true);
    internTestHelper.test(false);
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
}
