package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class MathPlumeTest {

  ///////////////////////////////////////////////////////////////////////////
  /// Utility functions
  ///

  private static void assertArraysEquals(int @Nullable [] a1, int @Nullable [] a2) {
    boolean result = Arrays.equals(a1, a2);
    if (!result) {
      System.out.println("Arrays differ: " + Arrays.toString(a1) + ", " + Arrays.toString(a2));
    }
    assertTrue(result);
    //      assert(Arrays.equals(a1, a2),
    //         "Arrays differ: " + ArraysPlume.toString(a1) + ", " + ArraysPlume.toString(a2));
  }

  // private static void assertArraysEquals(double[] a1, double[] a2) {
  //   boolean result = Arrays.equals(a1, a2);
  //   if (!result) {
  //     System.out.println(
  //         "Arrays differ: " + ArraysPlume.toString(a1) + ", " + ArraysPlume.toString(a2));
  //   }
  //   assertTrue(result);
  // }

  private static Iterator<Integer> intArrayIterator(int[] nums) {
    List<Integer> asList = new ArrayList<>(nums.length);
    for (int i = 0; i < nums.length; i++) {
      asList.add(nums[i]);
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
      a[i] = v.get(i).intValue();
    }
    return a;
  }

  // Test the utility functions
  @Test
  public void testTestUtilPlume() {
    int[] a = new int[] {3, 4, 5};
    assertArraysEquals(intIteratorArray(intArrayIterator(a)), a);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// The tests themselves
  ///

  @Test
  public void test_negate() {

    // int negate(int a)
    assertTrue(MathPlume.negate(3) == -3);
    assertTrue(MathPlume.negate(-22) == 22);
    assertTrue(MathPlume.negate(0) == 0);
  }

  @Test
  public void test_bitwiseComplement() {

    // int bitwiseComplement(int a)
    assertTrue(MathPlume.bitwiseComplement(3) == -4);
    assertTrue(MathPlume.bitwiseComplement(-22) == 21);
    assertTrue(MathPlume.bitwiseComplement(0) == -1);
  }

  @Test
  public void test_sign() {

    // int sign(int a)
    assertTrue(MathPlume.sign(3) == 1);
    assertTrue(MathPlume.sign(-22) == -1);
    assertTrue(MathPlume.sign(0) == 0);
  }

  @Test
  public void test_pow() {

    // int pow(int base, int expt)
    try {
      assertTrue(MathPlume.pow(3, 3) == 27);
      assertTrue(MathPlume.pow(-5, 5) == -3125);
      assertTrue(MathPlume.pow(22, 0) == 1);
      assertTrue(MathPlume.pow(4, 6) == 4096);
      assertTrue(MathPlume.pow(1, 222222) == 1);
      assertTrue(MathPlume.pow(-2, 25) == -33554432);
      // This is beyond the precision.  Maybe return a long instead of an int?
      // assertTrue(MathPlume.pow(-3, 25) == ...);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error(e);
    }
    try {
      MathPlume.pow(3, -3);
      throw new Error("Didn't throw ArithmeticException");
    } catch (ArithmeticException e) {
    }
  }

  @Test
  public void test_gcd() {

    // int gcd(int a, int b)
    assertTrue(MathPlume.gcd(2, 50) == 2);
    assertTrue(MathPlume.gcd(50, 2) == 2);
    assertTrue(MathPlume.gcd(12, 144) == 12);
    assertTrue(MathPlume.gcd(144, 12) == 12);
    assertTrue(MathPlume.gcd(96, 144) == 48);
    assertTrue(MathPlume.gcd(144, 96) == 48);
    assertTrue(MathPlume.gcd(10, 25) == 5);
    assertTrue(MathPlume.gcd(25, 10) == 5);
    assertTrue(MathPlume.gcd(17, 25) == 1);
    assertTrue(MathPlume.gcd(25, 17) == 1);
    assertTrue(MathPlume.gcd(0, 10) == 10);
    assertTrue(MathPlume.gcd(10, 0) == 10);
    assertTrue(MathPlume.gcd(25, -10) == 5);
    assertTrue(MathPlume.gcd(-25, -10) == 5);
    assertTrue(MathPlume.gcd(-25, 10) == 5);
    assertTrue(MathPlume.gcd(1, 10) == 1);
    assertTrue(MathPlume.gcd(10, 1) == 1);
    assertTrue(MathPlume.gcd(1, 0) == 1);
    assertTrue(MathPlume.gcd(0, 1) == 1);

    // int gcd(int[] a)
    assertTrue(MathPlume.gcd(new int[] {2, 50, 17}) == 1);
    assertTrue(MathPlume.gcd(new int[] {2, 50, 17, 234, 7}) == 1);
    assertTrue(MathPlume.gcd(new int[] {2, 50}) == 2);
    assertTrue(MathPlume.gcd(new int[] {12, 144}) == 12);
    assertTrue(MathPlume.gcd(new int[] {96, 144}) == 48);
    assertTrue(MathPlume.gcd(new int[] {10, 25}) == 5);
    assertTrue(MathPlume.gcd(new int[] {100, 10, 25}) == 5);
    assertTrue(MathPlume.gcd(new int[] {768, 324}) == 12);
    assertTrue(MathPlume.gcd(new int[] {2400, 48, 36}) == 12);
    assertTrue(MathPlume.gcd(new int[] {2400, 72, 36}) == 12);

    // int gcdDifferences(int[] a)
    // Weak set of tests, derived directly from those of "int gcd(int[] a)".
    assertTrue(MathPlume.gcdDifferences(new int[] {0, 2, 52}) == 2);
    assertTrue(MathPlume.gcdDifferences(new int[] {0, 12, 156}) == 12);
    assertTrue(MathPlume.gcdDifferences(new int[] {0, 96, 240}) == 48);
    assertTrue(MathPlume.gcdDifferences(new int[] {0, 10, 35}) == 5);
    assertTrue(MathPlume.gcdDifferences(new int[] {0, 100, 110, 135}) == 5);
    assertTrue(MathPlume.gcdDifferences(new int[] {0, 768, 1092}) == 12);
    assertTrue(MathPlume.gcdDifferences(new int[] {0, 2400, 2448, 2484}) == 12);
    assertTrue(MathPlume.gcdDifferences(new int[] {0, 2400, 2472, 2508}) == 12);
    assertTrue(MathPlume.gcdDifferences(new int[] {5, 5, 5, 5}) == 0);
  }

  @Test
  public void test_mod() {

    // int modNonnegative(int x, int y)
    assertTrue(MathPlume.modNonnegative(33, 5) == 3);
    assertTrue(MathPlume.modNonnegative(-33, 5) == 2);
    assertTrue(MathPlume.modNonnegative(33, -5) == 3);
    assertTrue(MathPlume.modNonnegative(-33, -5) == 2);
  }

  static class TestMissingNumbersIteratorInt {
    void test(int[] orig, boolean addEnds, int[] goalMissing) {
      Iterator<Integer> orig_iterator = intArrayIterator(orig);
      Iterator<Integer> missing_iterator =
          new MathPlume.MissingNumbersIteratorInt(orig_iterator, addEnds);
      int[] missing = intIteratorArray(missing_iterator);
      assertArraysEquals(missing, goalMissing);
    }
  }

  @Test
  public void test_missingNumbers() {

    // int[] missingNumbers(int[] nums)
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 5, 6, 7, 8}), new int[] {});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 6, 7, 8}), new int[] {5});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 8}), new int[] {5, 6, 7});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 5, 6, 8}), new int[] {4, 7});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 6, 8}), new int[] {4, 5, 7});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 5, 5, 6, 7, 8}), new int[] {});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 4, 4, 6, 6, 7, 8}), new int[] {5});
    assertArraysEquals(MathPlume.missingNumbers(new int[] {3, 3, 3}), new int[] {});

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

    void check(Iterator<Integer> itor, int @Nullable [] goalRm) {
      // There would be no point to this:  it's testing
      // intIteratorArray, not the iterator version!
      // return check(intIteratorArray(itor), goalRm);
      assertArraysEquals(MathPlume.modulusInt(itor), goalRm);
    }

    void checkIterator(int[] nums, int @Nullable [] goalRm) {
      check(intArrayIterator(nums), goalRm);
    }
  }

  static class TestNonModulus {
    void checkStrict(int[] nums, int @Nullable [] goalRm) {
      check(nums, goalRm, true);
      Iterator<Integer> itor = intArrayIterator(nums);
      assertArraysEquals(MathPlume.nonmodulusStrictInt(itor), goalRm);
    }

    void checkNonstrict(int[] nums, int @Nullable [] goalRm) {
      check(nums, goalRm, false);
    }

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

  @Test
  public void test_modulus() {

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

    // int[] nonmodulusStrict(int[] nums)
    // int[] nonmodulusNonstrict(int[] nums)
    // int[] nonmodulusStrict(Iterator nums)

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
}
