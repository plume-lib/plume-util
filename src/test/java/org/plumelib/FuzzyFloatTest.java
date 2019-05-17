package org.plumelib.util;

import java.util.Arrays;
import java.util.Comparator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class FuzzyFloatTest {

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
}
