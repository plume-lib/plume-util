package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.jupiter.api.Test;

/** Test the MathP class. */
final class MathPTest {

  MathPTest() {}

  // ///////////////////////////////////////////////////////////////////////////
  // Helper functions
  //

  private static void assertArraysEquals(int @Nullable [] a1, int @Nullable [] a2) {
    boolean result = Arrays.equals(a1, a2);
    if (!result) {
      System.out.println("Arrays differ: " + Arrays.toString(a1) + ", " + Arrays.toString(a2));
    }
    assertTrue(result);
    //      assert(Arrays.equals(a1, a2),
    //         "Arrays differ: " + ArraysP.toString(a1) + ", " + ArraysP.toString(a2));
  }

  private static void assertArraysEquals(long @Nullable [] a1, long @Nullable [] a2) {
    boolean result = Arrays.equals(a1, a2);
    if (!result) {
      System.out.println("Arrays differ: " + Arrays.toString(a1) + ", " + Arrays.toString(a2));
    }
    assertTrue(result);
    //      assert(Arrays.equals(a1, a2),
    //         "Arrays differ: " + ArraysP.toString(a1) + ", " + ArraysP.toString(a2));
  }

  // private static void assertArraysEquals(double[] a1, double[] a2) {
  //   boolean result = Arrays.equals(a1, a2);
  //   if (!result) {
  //     System.out.println(
  //         "Arrays differ: " + ArraysP.toString(a1) + ", " + ArraysP.toString(a2));
  //   }
  //   assertTrue(result);
  // }

  /**
   * Converts an array of longs to an array of ints. Throws an exception if some element is not
   * representable as an int.
   *
   * @param a an array of longs, or null
   * @return the same values as ints, or null if the argument is null
   */
  private static int @Nullable [] narrow(long @Nullable [] a) {
    if (a == null) {
      return null;
    }
    int[] result = new int[a.length];
    for (int i = 0; i < a.length; i++) {
      result[i] = Math.toIntExact(a[i]);
    }
    return result;
  }

  private static Iterator<Integer> intArrayIterator(int[] nums) {
    List<Integer> asList = new ArrayList<>(nums.length);
    for (int num : nums) {
      asList.add(num);
    }
    return asList.iterator();
  }

  private static int[] intIteratorArray(Iterator<Integer> itor) {
    ArrayList<Integer> v = new ArrayList<>();
    while (itor.hasNext()) {
      v.add(itor.next());
    }
    int[] a = new int[v.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = v.get(i);
    }
    return a;
  }

  // Test the utility functions
  @Test
  void testTestUtilP() {
    int[] a = {3, 4, 5};
    assertArraysEquals(intIteratorArray(intArrayIterator(a)), a);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // The tests themselves
  //

  //
  // Function versions of Java operators
  //

  /** Test negate(). */
  @Test
  void test_negate() {

    // int negate(int a)
    assertEquals(-3, MathP.negate(3));
    assertEquals(22, MathP.negate(-22));
    assertEquals(0, MathP.negate(0));
  }

  /** Test bitwiseComplement(). */
  @Test
  void test_bitwiseComplement() {

    // int bitwiseComplement(int a)
    assertEquals(-4, MathP.bitwiseComplement(3));
    assertEquals(21, MathP.bitwiseComplement(-22));
    assertEquals(-1, MathP.bitwiseComplement(0));
  }

  //
  // sign
  //

  /** Test sign(). */
  @Test
  void test_sign() {

    // int sign(int a)
    assertEquals(1, MathP.sign(3));
    assertEquals(-1, MathP.sign(-22));
    assertEquals(0, MathP.sign(0));
  }

  @Test
  void test_pow() {

    // int pow(int base, int expt)
    try {
      assertEquals(27, MathP.pow(3, 3));
      assertEquals(-3125, MathP.pow(-5, 5));
      assertEquals(1, MathP.pow(22, 0));
      assertEquals(4096, MathP.pow(4, 6));
      assertEquals(1, MathP.pow(1, 222_222));
      assertEquals(-33_554_432, MathP.pow(-2, 25));
      // This is beyond the precision.  Maybe return a long instead of an int?
      // assertTrue(MathP.pow(-3, 25) == ...);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error(e);
    }
  }

  @SuppressWarnings("PMD.JUnitUseExpected") // wrong version of JUnit?
  @Test
  void test_pow_exception() {
    try {
      MathP.pow(3, -3);
      throw new Error("Didn't throw ArithmeticException");
    } catch (ArithmeticException e) {
      // This is the expected behavior, so do nothing.
    }
  }

  @Test
  void test_gcd() {

    // int gcd(int a, int b)
    assertEquals(2, MathP.gcd(2, 50));
    assertEquals(2, MathP.gcd(50, 2));
    assertEquals(12, MathP.gcd(12, 144));
    assertEquals(12, MathP.gcd(144, 12));
    assertEquals(48, MathP.gcd(96, 144));
    assertEquals(48, MathP.gcd(144, 96));
    assertEquals(5, MathP.gcd(10, 25));
    assertEquals(5, MathP.gcd(25, 10));
    assertEquals(1, MathP.gcd(17, 25));
    assertEquals(1, MathP.gcd(25, 17));
    assertEquals(10, MathP.gcd(0, 10));
    assertEquals(10, MathP.gcd(10, 0));
    assertEquals(5, MathP.gcd(25, -10));
    assertEquals(5, MathP.gcd(-25, -10));
    assertEquals(5, MathP.gcd(-25, 10));
    assertEquals(1, MathP.gcd(1, 10));
    assertEquals(1, MathP.gcd(10, 1));
    assertEquals(1, MathP.gcd(1, 0));
    assertEquals(1, MathP.gcd(0, 1));

    // int gcd(int[] a)
    assertEquals(1, MathP.gcd(new int[] {2, 50, 17}));
    assertEquals(1, MathP.gcd(new int[] {2, 50, 17, 234, 7}));
    assertEquals(2, MathP.gcd(new int[] {2, 50}));
    assertEquals(12, MathP.gcd(new int[] {12, 144}));
    assertEquals(48, MathP.gcd(new int[] {96, 144}));
    assertEquals(5, MathP.gcd(new int[] {10, 25}));
    assertEquals(5, MathP.gcd(new int[] {100, 10, 25}));
    assertEquals(12, MathP.gcd(new int[] {768, 324}));
    assertEquals(12, MathP.gcd(new int[] {2400, 48, 36}));
    assertEquals(12, MathP.gcd(new int[] {2400, 72, 36}));

    // int gcdDifferences(int[] a)
    // Weak set of tests, derived directly from those of "int gcd(int[] a)".
    assertEquals(2, MathP.gcdDifferences(new int[] {0, 2, 52}));
    assertEquals(12, MathP.gcdDifferences(new int[] {0, 12, 156}));
    assertEquals(48, MathP.gcdDifferences(new int[] {0, 96, 240}));
    assertEquals(5, MathP.gcdDifferences(new int[] {0, 10, 35}));
    assertEquals(5, MathP.gcdDifferences(new int[] {0, 100, 110, 135}));
    assertEquals(12, MathP.gcdDifferences(new int[] {0, 768, 1092}));
    assertEquals(12, MathP.gcdDifferences(new int[] {0, 2400, 2448, 2484}));
    assertEquals(12, MathP.gcdDifferences(new int[] {0, 2400, 2472, 2508}));
    assertEquals(0, MathP.gcdDifferences(new int[] {5, 5, 5, 5}));
  }

  /** Test mul(). */
  @Test
  void test_mul() {
    // Tests go here.
  }

  /** Test mod(). */
  @Test
  void test_mod() {
    // int modNonnegative(int x, int y)
    assertEquals(3, MathP.modNonnegative(33, 5));
    assertEquals(2, MathP.modNonnegative(-33, 5));
    assertEquals(3, MathP.modNonnegative(33, -5));
    assertEquals(2, MathP.modNonnegative(-33, -5));
  }

  //
  // Non-Modulus
  //

  static class TestMissingNumbersIteratorInt {
    void test(int[] orig, boolean addEnds, int[] goalMissing) {
      Iterator<Integer> orig_iterator = intArrayIterator(orig);
      Iterator<Integer> missing_iterator =
          new MathP.MissingNumbersIteratorInt(orig_iterator, addEnds);
      int[] missing = intIteratorArray(missing_iterator);
      assertArraysEquals(missing, goalMissing);
    }
  }

  @Test
  void test_missingNumbers() {

    // int[] missingNumbers(int[] nums)
    assertArraysEquals(MathP.missingNumbers(new int[] {3, 4, 5, 6, 7, 8}), new int[] {});
    assertArraysEquals(MathP.missingNumbers(new int[] {3, 4, 6, 7, 8}), new int[] {5});
    assertArraysEquals(MathP.missingNumbers(new int[] {3, 4, 8}), new int[] {5, 6, 7});
    assertArraysEquals(MathP.missingNumbers(new int[] {3, 5, 6, 8}), new int[] {4, 7});
    assertArraysEquals(MathP.missingNumbers(new int[] {3, 6, 8}), new int[] {4, 5, 7});
    assertArraysEquals(MathP.missingNumbers(new int[] {3, 4, 5, 5, 6, 7, 8}), new int[] {});
    assertArraysEquals(MathP.missingNumbers(new int[] {3, 4, 4, 6, 6, 7, 8}), new int[] {5});
    assertArraysEquals(MathP.missingNumbers(new int[] {3, 3, 3}), new int[] {});

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
  }

  static class TestModulus {
    void check(int[] nums, int @Nullable [] goalRm) {
      int[] rm = MathP.modulus(nums);
      if (!Arrays.equals(rm, goalRm)) {
        throw new Error(
            "Expected (r,m)=" + Arrays.toString(goalRm) + ", saw (r,m)=" + Arrays.toString(rm));
      }
      if (rm == null) {
        return;
      }
      int goalR = rm[0];
      int m = rm[1];
      for (int num : nums) {
        int r = num % m;
        if (r < 0) {
          r += m;
        }
        if (r != goalR) {
          throw new Error("Expected " + num + " % " + m + " = " + goalR + ", got " + r);
        }
      }
    }

    void check(Iterator<Integer> itor, int @Nullable [] goalRm) {
      // There would be no point to this:  it's testing
      // intIteratorArray, not the iterator version!
      // return check(intIteratorArray(itor), goalRm);
      assertArraysEquals(MathP.modulusInt(itor), goalRm);
    }

    void checkIterator(int[] nums, int @Nullable [] goalRm) {
      check(intArrayIterator(nums), goalRm);
    }
  }

  static class TestModulusLong {
    void check(long[] nums, long @Nullable [] goalRm) {
      long[] rm = MathP.modulusLong(Arrays.stream(nums).iterator());
      if (!Arrays.equals(rm, goalRm)) {
        throw new Error(
            "Expected (r,m)=" + Arrays.toString(goalRm) + ", saw (r,m)=" + Arrays.toString(rm));
      }
      if (rm == null) {
        return;
      }
      long goalR = rm[0];
      long m = rm[1];
      for (long num : nums) {
        long r = num % m;
        if (r < 0) {
          r += m;
        }
        if (r != goalR) {
          throw new Error("Expected " + num + " % " + m + " = " + goalR + ", got " + r);
        }
      }
    }

    void check(Iterator<Long> itor, long @Nullable [] goalRm) {
      // There would be no point to this:  it's testing
      // longIteratorArray, not the iterator version!
      // return check(longIteratorArray(itor), goalRm);
      assertArraysEquals(MathP.modulusLong(itor), goalRm);
    }

    void checkIterator(long[] nums, long @Nullable [] goalRm) {
      check(Arrays.stream(nums).iterator(), goalRm);
    }

    /**
     * Checks that the {@code int} overloads agree with the {@code long} overloads, both for arrays
     * and for iterators. Does nothing if some element is not representable as an {@code int}.
     *
     * @param nums the operands
     * @param nonstrictEnds true if endpoints are NOT subject to the strict density requirement
     */
    void checkStrictIntOverloads(long[] nums, boolean nonstrictEnds) {
      int[] intNums = new int[nums.length];
      for (int i = 0; i < nums.length; i++) {
        if (nums[i] != (int) nums[i]) {
          return;
        }
        intNums[i] = (int) nums[i];
      }
      assertArraysEquals(
          narrow(MathP.modulusStrict(nums.clone(), nonstrictEnds)),
          MathP.modulusStrict(intNums.clone(), nonstrictEnds));
      assertArraysEquals(
          narrow(MathP.modulusStrictLong(Arrays.stream(nums).iterator(), nonstrictEnds)),
          MathP.modulusStrictInt(intArrayIterator(intNums), nonstrictEnds));
    }

    void checkStrict(long[] nums, long @Nullable @ArrayLen(2) [] goalRm) {
      long[] rm = MathP.modulusStrictLong(Arrays.stream(nums).iterator(), false);
      // The array-based overload must agree with the iterator-based overload.  (In particular, a
      // constant array such as {5,5,5,5,5} must return null rather than dividing by a zero
      // modulus.)
      assertArraysEquals(rm, MathP.modulusStrict(nums.clone(), false));
      checkStrictIntOverloads(nums, false);
      // A returned modulus is always positive.
      if (rm != null) {
        assertTrue(rm[1] > 0, "modulus should be positive: " + Arrays.toString(rm));
      }
      if (goalRm == null) {
        assertNull(rm);
      } else {
        assertArraysEquals(goalRm, rm);
        if (nums.length == 0) {
          throw new Error("this can't happen, because goalRm is not null");
        }
        long modulus = goalRm[1];
        long first = nums[0];
        // The elements may be in decreasing order, in which case the signed step is the negation
        // of the (always positive) modulus.
        long step = nums.length > 1 ? nums[1] - nums[0] : modulus;
        assertEquals(modulus, Math.abs(step));
        for (int i = 0; i < nums.length; i++) {
          assertEquals(nums[i], first + i * step);
        }
      }
    }

    void checkStrictNonStrictEnds(long[] nums, long @Nullable @ArrayLen(2) [] goalRm) {
      long[] rm = MathP.modulusStrictLong(Arrays.stream(nums).iterator(), true);
      // The array-based overload must agree with the iterator-based overload.  (In particular,
      // when fewer than 3 elements are subject to the strict density requirement, both must take
      // the same fallback rather than one of them returning null.)
      assertArraysEquals(rm, MathP.modulusStrict(nums.clone(), true));
      checkStrictIntOverloads(nums, true);
      // A returned modulus is always positive.
      if (rm != null) {
        assertTrue(rm[1] > 0, "modulus should be positive: " + Arrays.toString(rm));
      }
      if (goalRm == null) {
        assertNull(rm);
      } else {
        assertArraysEquals(goalRm, rm);
        if (nums.length < 3) {
          throw new Error("this can't happen, because goalRm is not null");
        }
        long remainder = goalRm[0];
        long modulus = goalRm[1];
        assertEquals(remainder, nums[0] % modulus);
        assertEquals(remainder, nums[nums.length - 1] % modulus);
        long first = nums[1];
        // The elements may be in decreasing order, in which case the signed step is the negation
        // of the (always positive) modulus.  With only one strict element, any step is consistent.
        long step = nums.length >= 4 ? nums[2] - nums[1] : modulus;
        assertEquals(modulus, Math.abs(step));
        for (int i = 1; i < nums.length - 1; i++) {
          assertEquals(nums[i], first + (i - 1) * step);
        }
      }
    }
  }

  static class TestNonModulus {
    void checkStrict(int[] nums, int @Nullable [] goalRm) {
      check(nums, goalRm, true);
      Iterator<Integer> itor = intArrayIterator(nums);
      assertArraysEquals(MathP.nonmodulusStrictInt(itor), goalRm);
    }

    void checkNonstrict(int[] nums, int @Nullable [] goalRm) {
      check(nums, goalRm, false);
    }

    void check(int[] nums, int @Nullable [] goalRm, boolean strict) {
      int[] rm;
      if (strict) {
        rm = MathP.nonmodulusStrict(nums);
      } else {
        rm = MathP.nonmodulusNonstrict(nums);
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
      for (int num : nums) {
        int r = num % m;
        if (r < 0) {
          r += m;
        }
        if (r == goalR) {
          throw new Error("Expected inequality, saw " + num + " % " + m + " = " + r);
        }
      }
    }
  }

  //
  // Modulus
  //

  /** Test modNonnegative(). */
  @Test
  void test_modNonnegative() {
    // Tests go here.
  }

  /** Test modulus(). */
  @Test
  void test_modulus() {

    // int[] modulus(int[] nums)
    // int[] modulus(Iterator itor)

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

    TestModulusLong testModulusLong = new TestModulusLong();

    testModulusLong.check(new long[] {3, 7, 47, 51}, new long[] {3, 4});
    testModulusLong.check(new long[] {3, 11, 43, 51}, new long[] {3, 8});
    testModulusLong.check(new long[] {3, 11, 47, 55}, new long[] {3, 4});
    testModulusLong.check(new long[] {2383, 4015, -81, 463, -689}, new long[] {15, 32});
    testModulusLong.check(new long[] {}, null);
    testModulusLong.check(new long[] {1}, null);
    testModulusLong.check(new long[] {3, 7}, null);
    testModulusLong.check(new long[] {2, 3, 5, 7}, null);
    testModulusLong.check(new long[] {2, 19, 101}, null);
    testModulusLong.check(new long[] {5, 5, 5, 5, 5}, null);

    testModulusLong.checkStrict(new long[] {3, 7, 11, 15}, new long[] {3, 4});
    testModulusLong.checkStrict(new long[] {3, 11, 19, 27}, new long[] {3, 8});
    testModulusLong.checkStrict(new long[] {27, 3, 11, 19, 27, -5}, null);
    testModulusLong.checkStrict(new long[] {3, 11}, null);
    testModulusLong.checkStrict(new long[] {3}, null);
    testModulusLong.checkStrict(new long[] {2383, 4015, -81, 463, -689}, null);
    testModulusLong.checkStrict(new long[] {}, null);
    testModulusLong.checkStrict(new long[] {1}, null);
    testModulusLong.checkStrict(new long[] {3, 7}, null);
    testModulusLong.checkStrict(new long[] {2, 3, 5, 7}, null);
    testModulusLong.checkStrict(new long[] {2, 19, 101}, null);
    testModulusLong.checkStrict(new long[] {5, 5, 5, 5, 5}, null);

    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 7, 11, 15, 19}, new long[] {3, 4});
    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 11, 19, 27, 35}, new long[] {3, 8});
    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 7, 11, 15}, new long[] {3, 4});
    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 11, 19, 27}, new long[] {3, 8});
    testModulusLong.checkStrictNonStrictEnds(
        new long[] {27, 3, 11, 19, 27, 203}, new long[] {3, 8});
    // TODO testModulusLong.checkStrictNonStrictEnds(new long[] {27, 3, 11, 19, 27, -5}, new long[]
    // {3, 8});
    // Regression tests: the remainder must be computed from an element that is subject to the
    // strict density requirement, not from the (nonstrict) last endpoint.  The strict elements
    // 7, 11, 15 are all 3 (mod 4), so an endpoint that is 1 (mod 4) must be rejected, even though
    // both endpoints agree with each other.
    testModulusLong.checkStrictNonStrictEnds(new long[] {1, 7, 11, 15, 5}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {1, 7, 11, 15, 19, 5}, null);
    // The same values, with endpoints that do match the strict elements.
    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 7, 11, 15, 19, 23}, new long[] {3, 4});
    // The same regression tests, for the int iterator overload.
    assertArraysEquals(
        null, MathP.modulusStrictInt(intArrayIterator(new int[] {1, 7, 11, 15, 5}), true));
    assertArraysEquals(
        null, MathP.modulusStrictInt(intArrayIterator(new int[] {1, 7, 11, 15, 19, 5}), true));
    assertArraysEquals(
        new int[] {3, 4},
        MathP.modulusStrictInt(intArrayIterator(new int[] {3, 7, 11, 15, 19, 23}), true));
    // All four overloads agree when fewer than 3 elements are subject to the strict density
    // requirement: each falls back to the non-strict computation rather than returning null.
    // (Regression test: the array overloads used to return null here.)
    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 7, 11, 15}, new long[] {3, 4});
    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 7, 11}, new long[] {3, 4});
    assertArraysEquals(
        new int[] {3, 4}, MathP.modulusStrictInt(intArrayIterator(new int[] {3, 7, 11, 15}), true));
    assertArraysEquals(
        new int[] {3, 4}, MathP.modulusStrictInt(intArrayIterator(new int[] {3, 7, 11}), true));
    assertArraysEquals(new int[] {3, 4}, MathP.modulusStrict(new int[] {3, 7, 11, 15}, true));
    assertArraysEquals(new int[] {3, 4}, MathP.modulusStrict(new int[] {3, 7, 11}, true));

    // The fallback applies even when the step inferred from the two strict elements is 0, as in
    // {3, 7, 7, 11}.  The strict density requirement is vacuous, so the non-strict computation
    // over all four elements decides, exactly as `modulus` does.  (Regression test: the inferred
    // step of 0 used to veto the fallback, so all four overloads returned null.)  This case uses
    // explicit assertions rather than `checkStrictNonStrictEnds`, because that helper also
    // asserts that consecutive strict elements are separated by the modulus, which is not true
    // of this input.
    assertArraysEquals(new int[] {3, 4}, MathP.modulus(new int[] {3, 7, 7, 11}));
    assertArraysEquals(
        new long[] {3, 4},
        MathP.modulusStrictLong(Arrays.stream(new long[] {3, 7, 7, 11}).iterator(), true));
    assertArraysEquals(new long[] {3, 4}, MathP.modulusStrict(new long[] {3, 7, 7, 11}, true));
    assertArraysEquals(
        new int[] {3, 4}, MathP.modulusStrictInt(intArrayIterator(new int[] {3, 7, 7, 11}), true));
    assertArraysEquals(new int[] {3, 4}, MathP.modulusStrict(new int[] {3, 7, 7, 11}, true));
    // With strict ends, every element is subject to the density requirement, which 7, 7 violates.
    assertArraysEquals(
        null, MathP.modulusStrictLong(Arrays.stream(new long[] {3, 7, 7, 11}).iterator(), false));
    assertArraysEquals(null, MathP.modulusStrict(new long[] {3, 7, 7, 11}, false));
    assertArraysEquals(
        null, MathP.modulusStrictInt(intArrayIterator(new int[] {3, 7, 7, 11}), false));
    assertArraysEquals(null, MathP.modulusStrict(new int[] {3, 7, 7, 11}, false));

    // Regression test for the guard against a zero modulus: a constant input yields a modulus of
    // 0, which must be rejected rather than used as a divisor.
    assertArraysEquals(
        null, MathP.modulusStrictInt(intArrayIterator(new int[] {5, 5, 5, 5, 5}), false));
    assertArraysEquals(
        null, MathP.modulusStrictInt(intArrayIterator(new int[] {5, 5, 5, 5, 5}), true));
    assertArraysEquals(null, MathP.modulusStrict(new int[] {5, 5, 5, 5, 5}, false));
    assertArraysEquals(null, MathP.modulusStrict(new int[] {5, 5, 5, 5, 5}, true));

    testModulusLong.checkStrictNonStrictEnds(new long[] {11, 7, 3}, new long[] {3, 4});
    testModulusLong.checkStrictNonStrictEnds(new long[] {15, 7, 3}, new long[] {3, 4});
    // A decreasing sequence yields a positive modulus, just as `modulus()` does.
    testModulusLong.checkStrict(new long[] {15, 11, 7, 3}, new long[] {3, 4});
    testModulusLong.checkStrictNonStrictEnds(new long[] {19, 15, 11, 7, 3}, new long[] {3, 4});
    // A sequence whose difference is 1 or -1 is the trivial constraint "x = 0 (mod 1)", which is
    // not reported.
    testModulusLong.checkStrict(new long[] {3, 4, 5, 6}, null);
    testModulusLong.checkStrict(new long[] {6, 5, 4, 3}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {2, 3, 4, 5, 6}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {7, 6, 5, 4, 3}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 11}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {3}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {2383, 4015, -81, 463, -689}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {1}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {3, 7}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {2, 3, 5, 7}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {2, 19, 101}, null);
    testModulusLong.checkStrictNonStrictEnds(new long[] {5, 5, 5, 5, 5}, null);

    testModulusLong.checkIterator(new long[] {}, null);
    testModulusLong.checkIterator(new long[] {1}, null);
    testModulusLong.checkIterator(new long[] {3, 7, 47, 51}, new long[] {3, 4});
    testModulusLong.checkIterator(new long[] {3, 11, 43, 51}, new long[] {3, 8});
    testModulusLong.checkIterator(new long[] {3, 11, 47, 55}, new long[] {3, 4});
    testModulusLong.checkIterator(new long[] {2383, 4015, -81, 463, -689}, new long[] {15, 32});
    testModulusLong.checkIterator(new long[] {5, 5, 5, 5, 5}, null);

    // int[] nonmodulusStrict(int[] nums)
    // int[] nonmodulusNonstrict(int[] nums)
    // int[] nonmodulusStrict(Iterator nums)

    TestNonModulus testNonModulus = new TestNonModulus();

    testNonModulus.checkStrict(new int[] {1}, null);
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
}
