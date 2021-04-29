package org.plumelib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.PolyUpperBound;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLen;
import org.checkerframework.common.value.qual.MinLen;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/** Mathematical utilities. */
public final class MathPlume {

  /** This class is a collection of methods; it does not represent anything. */
  private MathPlume() {
    throw new Error("do not instantiate");
  }

  ///
  /// Function versions of Java operators
  ///

  /**
   * Negates its argument.
   *
   * @param a value to negate
   * @return negative of a
   */
  @Pure
  @StaticallyExecutable
  public static int negate(int a) {
    return -a;
  }

  /**
   * Negates its argument.
   *
   * @param a value to negate
   * @return negative of a
   */
  @Pure
  @StaticallyExecutable
  public static long negate(long a) {
    return -a;
  }

  /**
   * Negates its argument.
   *
   * @param a value to negate
   * @return negative of a
   */
  @Pure
  @StaticallyExecutable
  public static double negate(double a) {
    return -a;
  }

  /**
   * Returns ~a, the bitwise complement of its argument.
   *
   * @param a value to bitwise-complement
   * @return ~a, the bitwise complement of a
   */
  @Pure
  @StaticallyExecutable
  public static int bitwiseComplement(int a) {
    return ~a;
  }

  /**
   * Returns ~a, the bitwise complement of its argument.
   *
   * @param a value to bitwise-complement
   * @return ~a, the bitwise complement of a
   */
  @Pure
  @StaticallyExecutable
  public static long bitwiseComplement(long a) {
    return ~a;
  }

  /**
   * Multiplies its arguments.
   *
   * @param x first multiplicand
   * @param y second multiplicand
   * @return x * y
   */
  @Pure
  @StaticallyExecutable
  public static int mul(int x, int y) {
    return x * y;
  }

  /**
   * Multiplies its arguments.
   *
   * @param x first multiplicand
   * @param y second multiplicand
   * @return x * y
   */
  @Pure
  @StaticallyExecutable
  public static long mul(long x, long y) {
    return x * y;
  }

  /**
   * Multiplies its arguments.
   *
   * @param x first multiplicand
   * @param y second multiplicand
   * @return x * y
   */
  @Pure
  @StaticallyExecutable
  public static double mul(double x, double y) {
    return x * y;
  }

  /**
   * Divides its arguments.
   *
   * @param x dividend
   * @param y divisor
   * @return x / y
   */
  @Pure
  @StaticallyExecutable
  public static int div(int x, int y) {
    return x / y;
  }

  /**
   * Divides its arguments.
   *
   * @param x dividend
   * @param y divisor
   * @return x / y
   */
  @Pure
  @StaticallyExecutable
  public static long div(long x, long y) {
    return x / y;
  }

  /**
   * Divides its arguments.
   *
   * @param x dividend
   * @param y divisor
   * @return x / y
   */
  @Pure
  @StaticallyExecutable
  public static double div(double x, double y) {
    return x / y;
  }

  /**
   * Returns x % y, the modulus operation applied to its arguments.
   *
   * @param x valued to be modded
   * @param y modulus
   * @return x % y
   */
  @Pure
  @StaticallyExecutable
  public static int mod(int x, int y) {
    return x % y;
  }

  /**
   * Returns x % y, the modulus operation applied to its arguments.
   *
   * @param x valued to be modded
   * @param y modulus
   * @return x % y
   */
  @Pure
  @StaticallyExecutable
  public static long mod(long x, long y) {
    return x % y;
  }

  /**
   * Returns x &lt;&lt; y, the left-shift operation applied to its arguments.
   *
   * @param x valued to be left-shifted
   * @param y magnitude of the left-shift
   * @return x &lt;&lt; y
   */
  @Pure
  @StaticallyExecutable
  public static int lshift(int x, int y) {
    return x << y;
  }

  /**
   * Returns x &lt;&lt; y, the left-shift operation applied to its arguments.
   *
   * @param x valued to be left-shifted
   * @param y magnitude of the left-shift
   * @return x &lt;&lt; y
   */
  @Pure
  @StaticallyExecutable
  public static long lshift(long x, long y) {
    return x << y;
  }

  /**
   * Returns x &gt;&gt; y, the signed right-shift operation applied to its arguments.
   *
   * @param x valued to be right-shifted
   * @param y magnitude of the right-shift
   * @return x &gt;&gt; y
   */
  @Pure
  @StaticallyExecutable
  public static int rshiftSigned(int x, int y) {
    return x >> y;
  }

  /**
   * Returns x &gt;&gt; y, the signed right-shift operation applied to its arguments.
   *
   * @param x valued to be right-shifted
   * @param y magnitude of the right-shift
   * @return x &gt;&gt; y
   */
  @Pure
  @StaticallyExecutable
  public static long rshiftSigned(long x, long y) {
    return x >> y;
  }

  /**
   * Returns x &gt;&gt;&gt; y, the unsigned right-shift operation applied to its arguments.
   *
   * @param x valued to be right-shifted
   * @param y magnitude of the right-shift
   * @return x &gt;&gt;&gt; y
   */
  @Pure
  @StaticallyExecutable
  public static int rshiftUnsigned(int x, int y) {
    return x >>> y;
  }

  /**
   * Returns x &gt;&gt;&gt; y, the unsigned right-shift operation applied to its arguments.
   *
   * @param x valued to be right-shifted
   * @param y magnitude of the right-shift
   * @return x &gt;&gt;&gt; y
   */
  @Pure
  @StaticallyExecutable
  public static long rshiftUnsigned(long x, long y) {
    return x >>> y;
  }

  /**
   * Returns x &amp; y, the bitwise and of its arguments.
   *
   * @param x first operand
   * @param y second operand
   * @return x &amp; y
   */
  @Pure
  @StaticallyExecutable
  public static int bitwiseAnd(int x, int y) {
    return x & y;
  }

  /**
   * Returns x &amp; y, the bitwise and of its arguments.
   *
   * @param x first operand
   * @param y second operand
   * @return x &amp; y
   */
  @Pure
  @StaticallyExecutable
  public static long bitwiseAnd(long x, long y) {
    return x & y;
  }

  /**
   * Returns the logical and of its arguments. The result is always 0 or 1.
   *
   * @param x first operand
   * @param y second operand
   * @return the logical and of x and y; the result is always 0 or 1
   */
  @Pure
  @StaticallyExecutable
  public static int logicalAnd(int x, int y) {
    return ((x != 0) && (y != 0)) ? 1 : 0;
  }

  /**
   * Returns the logical and of its arguments. The result is always 0 or 1.
   *
   * @param x first operand
   * @param y second operand
   * @return the logical and of x and y; the result is always 0 or 1
   */
  @Pure
  @StaticallyExecutable
  public static long logicalAnd(long x, long y) {
    return ((x != 0) && (y != 0)) ? 1 : 0;
  }

  /**
   * Returns x ^ y, the bitwise xor of its arguments.
   *
   * @param x first operand
   * @param y second operand
   * @return x ^ y
   */
  @Pure
  @StaticallyExecutable
  public static int bitwiseXor(int x, int y) {
    return x ^ y;
  }

  /**
   * Returns x ^ y, the bitwise xor of its arguments.
   *
   * @param x first operand
   * @param y second operand
   * @return x ^ y
   */
  @Pure
  @StaticallyExecutable
  public static long bitwiseXor(long x, long y) {
    return x ^ y;
  }

  /**
   * Returns the logical xor of its arguments. The result is always 0 or 1.
   *
   * @param x first operand
   * @param y second operand
   * @return the logical xor of x and y; the result is always 0 or 1
   */
  @Pure
  @StaticallyExecutable
  public static int logicalXor(int x, int y) {
    return ((x != 0) ^ (y != 0)) ? 1 : 0;
  }

  /**
   * Returns the logical xor of its arguments. The result is always 0 or 1.
   *
   * @param x first operand
   * @param y second operand
   * @return the logical xor of x and y; the result is always 0 or 1
   */
  @Pure
  @StaticallyExecutable
  public static long logicalXor(long x, long y) {
    return ((x != 0) ^ (y != 0)) ? 1 : 0;
  }

  /**
   * Returns x | y, the bitwise or of its arguments.
   *
   * @param x first operand
   * @param y second operand
   * @return x | y
   */
  @Pure
  @StaticallyExecutable
  public static int bitwiseOr(int x, int y) {
    return x | y;
  }

  /**
   * Returns x | y, the bitwise or of its arguments.
   *
   * @param x first operand
   * @param y second operand
   * @return x | y
   */
  @Pure
  @StaticallyExecutable
  public static long bitwiseOr(long x, long y) {
    return x | y;
  }

  /**
   * Returns the logical or of its arguments. The result is always 0 or 1.
   *
   * @param x first operand
   * @param y second operand
   * @return the logical or of x and y; the result is always 0 or 1
   */
  @Pure
  @StaticallyExecutable
  public static int logicalOr(int x, int y) {
    return ((x != 0) || (y != 0)) ? 1 : 0;
  }

  /**
   * Returns the logical or of its arguments. The result is always 0 or 1.
   *
   * @param x first operand
   * @param y second operand
   * @return the logical or of x and y; the result is always 0 or 1
   */
  @Pure
  @StaticallyExecutable
  public static long logicalOr(long x, long y) {
    return ((x != 0) || (y != 0)) ? 1 : 0;
  }

  ///
  /// sign
  ///

  /**
   * Returns the sign of its argument. The result is always -1, 0, or 1.
   *
   * @param a value to have its sign taken
   * @return the sign of a: -1, 0, or 1
   */
  @Pure
  @StaticallyExecutable
  public static int sign(int a) {
    if (a == 0) {
      return 0;
    } else if (a > 0) {
      return 1;
    } else {
      return -1;
    }
  }

  ///
  /// exponentiation
  ///

  /**
   * Returns of value of the first argument raised to the power of the second argument. The
   * arguments are integers.
   *
   * @param base the base
   * @param expt the exponent
   * @return base to the expt power
   * @see Math#pow(double, double)
   */
  @Pure
  @StaticallyExecutable
  public static int pow(int base, int expt) throws ArithmeticException {
    return powFast(base, expt);
  }

  /**
   * Returns of value of the first argument raised to the power of the second argument.
   *
   * @param base the base
   * @param expt the exponent
   * @return base to the expt power
   * @see Math#pow(double, double)
   */
  @Pure
  @StaticallyExecutable
  public static long pow(long base, long expt) throws ArithmeticException {
    return powFast(base, expt);
  }

  /**
   * Returns of value of the first argument raised to the power of the second argument. Uses a fast
   * algorithm.
   *
   * @param base the base
   * @param expt the exponent
   * @return base to the expt power
   * @see Math#pow(double, double)
   */
  @Pure
  @StaticallyExecutable
  private static int powFast(int base, int expt) throws ArithmeticException {
    if (expt < 0) {
      throw new ArithmeticException("Negative exponent passed to pow");
    }

    int thisSquarePow = base;
    int result = 1;
    while (expt > 0) {
      if ((expt & 1) != 0) {
        result *= thisSquarePow;
      }
      expt >>= 1;
      thisSquarePow *= thisSquarePow;
    }
    return result;
  }

  /**
   * Returns the first argument raised to the power of the second argument. Uses a fast algorithm.
   *
   * @param base the base
   * @param expt the exponent
   * @return base to the expt power
   * @see Math#pow(double, double)
   */
  @Pure
  @StaticallyExecutable
  private static long powFast(long base, long expt) throws ArithmeticException {
    if (expt < 0) {
      throw new ArithmeticException("Negative exponent passed to pow");
    }

    long thisSquarePow = base;
    long result = 1;
    while (expt > 0) {
      if ((expt & 1) != 0) {
        result *= thisSquarePow;
      }
      expt >>= 1;
      thisSquarePow *= thisSquarePow;
    }
    return result;
  }

  // /**
  //  * Returns the first argument raised to the power of the second argument. Uses a slow
  //  * algorithm.
  //  *
  //  * @param base the base
  //  * @param expt the exponent
  //  * @return base to the expt power
  //  * @see Math#pow(double, double)
  //  */
  // @Pure
  // @StaticallyExecutable
  // private static int powSlow(int base, int expt) throws ArithmeticException {
  //   if (expt < 0) {
  //     throw new ArithmeticException("Negative exponent passed to pow");
  //   }
  //
  //   int result = 1;
  //   for (int i = 0; i < expt; i++) {
  //     result *= base;
  //   }
  //   return result;
  // }

  ///
  /// gcd
  ///

  /**
   * Returns the greatest common divisor of the two arguments.
   *
   * @param a first operand
   * @param b second operand
   * @return greatest common divisor of a and b
   */
  @Pure
  @StaticallyExecutable
  public static int gcd(int a, int b) {

    // Euclid's method
    if (b == 0) {
      return Math.abs(a);
    }
    a = Math.abs(a);
    b = Math.abs(b);
    while (b != 0) {
      int tmp = b;
      b = a % b;
      a = tmp;
    }
    return a;
  }

  /**
   * Returns the greatest common divisor of the elements of int array a.
   *
   * @param a array of operands
   * @return greatest common divisor of the elements of a
   */
  @Pure
  @StaticallyExecutable
  public static int gcd(int[] a) {
    if (a.length == 0) {
      return 0;
    }
    int result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = gcd(a[i], result);
      if ((result == 1) || (result == 0)) {
        return result;
      }
    }
    return result;
  }

  /**
   * Returns the gcd (greatest common divisor) of the differences between the elements of int array
   * a.
   *
   * @param a array of operands
   * @return greatest common divisor of the differences between the elements of a
   */
  @Pure
  @StaticallyExecutable
  public static int gcdDifferences(int[] a) {
    if (a.length < 2) {
      return 0;
    }
    int result = a[1] - a[0];
    for (int i = 2; i < a.length; i++) {
      result = gcd(a[i] - a[i - 1], result);
      if ((result == 1) || (result == 0)) {
        return result;
      }
    }
    return result;
  }

  /// gcd -- version for manipulating long (rather than int) values

  /**
   * Returns the greatest common divisor of the two arguments.
   *
   * @param a first operand
   * @param b second operand
   * @return greatest common divisor of a and b
   */
  @Pure
  @StaticallyExecutable
  public static long gcd(long a, long b) {

    // Euclid's method
    if (b == 0) {
      return Math.abs(a);
    }
    a = Math.abs(a);
    b = Math.abs(b);
    while (b != 0) {
      long tmp = b;
      b = a % b;
      a = tmp;
    }
    return a;
  }

  /**
   * Returns the greatest common divisor of the elements of long array a.
   *
   * @param a array of operands
   * @return greatest common divisor of the elements of a
   */
  @Pure
  @StaticallyExecutable
  public static long gcd(long[] a) {
    if (a.length == 0) {
      return 0;
    }
    long result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = gcd(a[i], result);
      if ((result == 1) || (result == 0)) {
        return result;
      }
    }
    return result;
  }

  /**
   * Returns the gcd (greatest common divisor) of the differences between the elements of long array
   * a.
   *
   * @param a array of operands
   * @return greatest common divisor of the differences between the elements of a
   */
  @Pure
  @StaticallyExecutable
  public static long gcdDifferences(long[] a) {
    if (a.length < 2) {
      return 0;
    }
    long result = a[1] - a[0];
    for (int i = 2; i < a.length; i++) {
      result = gcd(a[i] - a[i - 1], result);
      if ((result == 1) || (result == 0)) {
        return result;
      }
    }
    return result;
  }

  /**
   * Returns the greatest common divisor of the two arguments.
   *
   * @param a first operand
   * @param b second operand
   * @return greatest common divisor of a and b
   */
  @Pure
  @StaticallyExecutable
  public static double gcd(double a, double b) {

    if (a == Double.POSITIVE_INFINITY
        || a == Double.NEGATIVE_INFINITY
        || Double.isNaN(a)
        || b == Double.POSITIVE_INFINITY
        || b == Double.NEGATIVE_INFINITY
        || Double.isNaN(b)) {
      return Double.NaN;
    }

    // Euclid's method
    if (b == 0) {
      return Math.abs(a);
    }
    a = Math.abs(a);
    b = Math.abs(b);
    while (b != 0) {
      double tmp = b;
      b = a % b;
      a = tmp;
    }
    return a;
  }

  /**
   * Returns the greatest common divisor of the elements of double array a.
   *
   * @param a array of operands
   * @return greatest common divisor of the elements of a
   */
  @Pure
  @StaticallyExecutable
  public static double gcd(double[] a) {
    if (a.length == 0) {
      return 0;
    }
    double result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = gcd(a[i], result);
      if ((result == 1) || (result == 0)) {
        return result;
      }
    }
    return result;
  }

  /**
   * Returns the gcd (greatest common divisor) of the differences between the elements of double
   * array a.
   *
   * @param a array of operands
   * @return greatest common divisor of the differences between the elements of a
   */
  @Pure
  @StaticallyExecutable
  public static double gcdDifferences(double[] a) {
    if (a.length < 2) {
      return 0;
    }
    double result = a[1] - a[0];
    for (int i = 2; i < a.length; i++) {
      result = gcd(a[i] - a[i - 1], result);
      if ((result == 1) || (result == 0)) {
        return result;
      }
    }
    return result;
  }

  ///
  /// Modulus
  ///

  /**
   * Returns z such that {@code (z == x mod y) && (0 <= z < abs(y))}. This should really be named
   * {@code modNonnegative} rather than {@code modPositive}.
   *
   * @param x value to be modded
   * @param y modulus
   * @return x % y, where the result is constrained to be non-negative
   * @deprecated use {@link #modNonnegative(int, int)}
   */
  @Deprecated // use modNonnegative(); deprecated 2020-02-20
  @Pure
  @StaticallyExecutable
  public static @NonNegative @LessThan("#2") @PolyUpperBound int modPositive(
      int x, @PolyUpperBound int y) {
    return modNonnegative(x, y);
  }

  /**
   * Returns z such that {@code (z == x mod y) && (0 <= z < abs(y))}.
   *
   * @param x value to be modded
   * @param y modulus
   * @return x % y, where the result is constrained to be non-negative
   */
  @SuppressWarnings({
    "lessthan:return",
    "lowerbound:return",
    "index:return"
  }) // result is non-negative because either y is positive (-> x % y is non-negative)
  // or |y| is added to x % y, which is also non-negative
  @Pure
  @StaticallyExecutable
  public static @NonNegative @LessThan("#2") @PolyUpperBound int modNonnegative(
      int x, @PolyUpperBound int y) {
    int result = x % y;
    if (result < 0) {
      result += Math.abs(y);
    }
    return result;
  }

  /**
   * Returns an array of two integers (r,m) such that each number in NUMS is equal to r (mod m). The
   * largest possible modulus is used, and the trivial constraint that all integers are equal to 0
   * mod 1 is not returned (null is returned instead). Also, return null if the array is less than 3
   * elements long.
   *
   * @param nums array of operands
   * @return an array of two integers (r,m) such that each number in NUMS is equal to r (mod m), or
   *     null if no such exists or the iterator contains fewer than 3 elements
   */
  @SuppressWarnings("value:statically.executable.not.pure") // results are .equals() but not ==
  @SideEffectFree
  @StaticallyExecutable
  public static int @Nullable @ArrayLen(2) [] modulus(int[] nums) {
    if (nums.length < 3) {
      return null;
    }

    int modulus = Math.abs(gcdDifferences(nums));
    if ((modulus == 0) || (modulus == 1)) {
      return null;
    }

    int remainder = nums[0] % modulus;
    if (remainder < 0) {
      remainder += modulus;
    }

    return new int[] {remainder, modulus};
  }

  /**
   * The iterator produces Integer values. This can be more efficient than modulus(int[]) if the
   * int[] doesn't already exist, because this does not necessarily examine every value produced by
   * its iterator.
   *
   * @param itor iterator of operands; modified by this method
   * @return an array of two integers (r,m) such that each number in itor is equal to r (mod m), or
   *     null if no such exists or the iterator contains fewer than 3 elements
   * @see #modulus(int[])
   */
  public static int @Nullable @ArrayLen(2) [] modulusInt(Iterator<Integer> itor) {
    if (!itor.hasNext()) {
      return null;
    }
    int avalue = itor.next().intValue();
    if (!itor.hasNext()) {
      return null;
    }
    int modulus = Math.abs(avalue - itor.next().intValue());
    if (modulus == 1) {
      return null;
    }
    int count = 2;
    while (itor.hasNext()) {
      int i = itor.next().intValue();
      if (i == avalue) {
        continue;
      }
      modulus = MathPlume.gcd(modulus, Math.abs(avalue - i));
      count++;
      if (modulus == 1) {
        return null;
      }
    }
    if (count < 3) {
      return null;
    }
    return new int[] {MathPlume.modPositive(avalue, modulus), modulus};
  }

  /**
   * Returns an array of two integers (r,m) such that each number in NUMS is equal to r (mod m). The
   * largest possible modulus is used, and the trivial constraint that all integers are equal to 0
   * mod 1 is not returned (null is returned instead).
   *
   * <p>This "Strict" version requires its input to be sorted, and no element may be missing.
   *
   * <p>This "Strict" version differs from the regular modulus by requiring that the argument be
   * dense: that is, every pair of numbers in the argument array is separated by exactly the
   * modulus.
   *
   * <p>The endpoints can be treated in two different ways: Either exactly like other numbers in the
   * input, or they can merely be checked for the condition without the strict density requirement.
   *
   * @param nums array of operands
   * @param nonstrictEnds whether endpoints are NOT subject to the strict density requirement
   * @return an array of two integers (r,m) such that each number in NUMS is equal to r (mod m), or
   *     null if no such exists or the array contains fewer than 3 elements
   */
  @SuppressWarnings("value:statically.executable.not.pure") // results are .equals() but not ==
  @SideEffectFree
  @StaticallyExecutable
  public static int @Nullable @ArrayLen(2) [] modulusStrict(int[] nums, boolean nonstrictEnds) {
    if (nums.length < 3) {
      return null;
    }

    int firstIndex = 0;
    int lastIndex = nums.length - 1;
    int firstNonstrict = 0; // arbitrary initial value
    int lastNonstrict = 0; // arbitrary initial value
    if (nonstrictEnds) {
      firstNonstrict = nums[firstIndex];
      firstIndex++;
      lastNonstrict = nums[lastIndex];
      lastIndex--;
    }
    if (lastIndex - firstIndex < 2) {
      return null;
    }

    int modulus = nums[firstIndex + 1] - nums[firstIndex];
    if (modulus == 1) {
      return null;
    }
    for (int i = firstIndex + 2; i <= lastIndex; i++) {
      if (nums[i] - nums[i - 1] != modulus) {
        return null;
      }
    }

    int r = modPositive(nums[firstIndex], modulus);
    if (nonstrictEnds) {
      if ((r != modPositive(firstNonstrict, modulus))
          || (r != modPositive(lastNonstrict, modulus))) {
        return null;
      }
    }

    return new int[] {r, modulus};
  }

  /**
   * The iterator produces Integer values. This can be more efficient than modulus(int[]) if the
   * int[] doesn't already exist, because this does not necessarily examine every value produced by
   * its iterator.
   *
   * <p>For documentation, see {@link #modulusStrict(int[], boolean)}.
   *
   * @param itor iterator of operands; modified by this method
   * @param nonstrictEnds whether endpoints are NOT subject to the strict density requirement
   * @return an array of two integers (r,m) such that each number in NUMS is equal to r (mod m), or
   *     null if no such exists or the iterator contains fewer than 3 elements
   * @see #modulusStrict(int[], boolean)
   */
  public static int @Nullable @ArrayLen(2) [] modulusStrictInt(
      Iterator<Integer> itor, boolean nonstrictEnds) {
    if (!itor.hasNext()) {
      return null;
    }

    int firstNonstrict = 0; // arbitrary initial value
    int lastNonstrict = 0; // arbitrary initial value
    if (nonstrictEnds) {
      firstNonstrict = itor.next().intValue();
    }

    int prev = itor.next().intValue();
    if (!itor.hasNext()) {
      return null;
    }
    int next = itor.next().intValue();
    int modulus = next - prev;
    if (modulus == 1) {
      return null;
    }
    int count = 2;
    while (itor.hasNext()) {
      prev = next;
      next = itor.next().intValue();
      if (nonstrictEnds && !itor.hasNext()) {
        lastNonstrict = next;
        break;
      }

      if (next - prev != modulus) {
        return null;
      }
      count++;
    }
    if (count < 3) {
      return null;
    }

    int r = MathPlume.modPositive(next, modulus);
    if (nonstrictEnds) {
      if ((r != modPositive(firstNonstrict, modulus))
          || (r != modPositive(lastNonstrict, modulus))) {
        return null;
      }
    }

    return new int[] {r, modulus};
  }

  /// modulus for long (as opposed to int) values

  /**
   * Returns z such that {@code (z == x mod y) && (0 <= z < abs(y))}. This should really be named
   * {@code modNonnegative} rather than {@code modPositive}.
   *
   * @param x value to be modded
   * @param y modulus
   * @return x % y, where the result is constrained to be non-negative
   * @deprecated use {@link #modNonnegative(long, long)}
   */
  @Deprecated // use modNonnegative(); deprecated 2020-02-20
  @Pure
  @StaticallyExecutable
  public static @NonNegative @LessThan("#2") @PolyUpperBound long modPositive(
      long x, @PolyUpperBound long y) {
    return modNonnegative(x, y);
  }

  /**
   * Returns z such that {@code (z == x mod y) && (0 <= z < abs(y))}.
   *
   * @param x value to be modded
   * @param y modulus
   * @return x % y, where the result is constrained to be non-negative
   */
  @SuppressWarnings({
    "lessthan:return",
    "lowerbound:return",
    "index:return"
  }) // result is non-negative because either y is positive (-> x % y is non-negative) or
  // |y| is added to x % y, which is also non-negative
  @Pure
  @StaticallyExecutable
  public static @NonNegative @LessThan("#2") @PolyUpperBound long modNonnegative(
      long x, @PolyUpperBound long y) {
    long result = x % y;
    if (result < 0) {
      result += Math.abs(y);
    }
    return result;
  }

  /**
   * Returns an array of two integers (r,m) such that each number in NUMS is equal to r (mod m). The
   * largest possible modulus is used, and the trivial constraint that all integers are equal to 0
   * mod 1 is not returned (null is returned instead). Also, return null if the array is less than 3
   * elements long.
   *
   * @param nums array of operands
   * @return an array of two integers (r,m) such that each number in NUMS is equal to r (mod m), or
   *     null if no such exists or the iterator contains fewer than 3 elements
   */
  @SuppressWarnings("value:statically.executable.not.pure") // results are .equals() but not ==
  @SideEffectFree
  @StaticallyExecutable
  public static long @Nullable @ArrayLen(2) [] modulus(long[] nums) {
    if (nums.length < 3) {
      return null;
    }

    long modulus = Math.abs(gcdDifferences(nums));
    if ((modulus == 0) || (modulus == 1)) {
      return null;
    }

    long remainder = nums[0] % modulus;
    if (remainder < 0) {
      remainder += modulus;
    }

    return new long[] {remainder, modulus};
  }

  /**
   * The iterator produces Long values. This can be more efficient than modulus(long[]) if the
   * long[] doesn't already exist, because this does not necessarily examine every value produced by
   * its iterator.
   *
   * @param itor iterator of operands; modified by this method
   * @return an array of two integers (r,m) such that each number in itor is equal to r (mod m), or
   *     null if no such exists or the iterator contains fewer than 3 elements
   * @see #modulus(long[])
   */
  public static long @Nullable @ArrayLen(2) [] modulusLong(Iterator<Long> itor) {
    if (!itor.hasNext()) {
      return null;
    }
    long avalue = itor.next().longValue();
    if (!itor.hasNext()) {
      return null;
    }
    long modulus = Math.abs(avalue - itor.next().longValue());
    if (modulus == 1) {
      return null;
    }
    int count = 2;
    while (itor.hasNext()) {
      long i = itor.next().longValue();
      if (i == avalue) {
        continue;
      }
      modulus = MathPlume.gcd(modulus, Math.abs(avalue - i));
      count++;
      if (modulus == 1) {
        return null;
      }
    }
    if (count < 3) {
      return null;
    }
    return new long[] {MathPlume.modPositive(avalue, modulus), modulus};
  }

  /**
   * Returns an array of two integers (r,m) such that each number in NUMS is equal to r (mod m). The
   * largest possible modulus is used, and the trivial constraint that all integers are equal to 0
   * mod 1 is not returned (null is returned instead).
   *
   * <p>This "Strict" version requires its input to be sorted, and no element may be missing.
   *
   * <p>This "Strict" version differs from the regular modulus by requiring that the argument be
   * dense: that is, every pair of numbers in the argument array is separated by exactly the
   * modulus.
   *
   * <p>The endpoints can be treated in two different ways: Either exactly like other numbers in the
   * input, or they can merely be checked for the condition without the strict density requirement.
   *
   * @param nums array of operands
   * @param nonstrictEnds whether endpoints are NOT subject to the strict density requirement
   * @return an array of two integers (r,m) such that each number in NUMS is equal to r (mod m), or
   *     null if no such exists or the array contains fewer than 3 elements
   */
  @SuppressWarnings("value:statically.executable.not.pure") // results are .equals() but not ==
  @SideEffectFree
  @StaticallyExecutable
  public static long @Nullable @ArrayLen(2) [] modulusStrict(long[] nums, boolean nonstrictEnds) {
    if (nums.length < 3) {
      return null;
    }

    int firstIndex = 0;
    int lastIndex = nums.length - 1;
    long firstNonstrict = 0; // arbitrary initial value
    long lastNonstrict = 0; // arbitrary initial value
    if (nonstrictEnds) {
      firstNonstrict = nums[firstIndex];
      firstIndex++;
      lastNonstrict = nums[lastIndex];
      lastIndex--;
    }
    if (lastIndex - firstIndex < 2) {
      return null;
    }

    long modulus = nums[firstIndex + 1] - nums[firstIndex];
    if (modulus == 1) {
      return null;
    }
    for (int i = firstIndex + 2; i <= lastIndex; i++) {
      if (nums[i] - nums[i - 1] != modulus) {
        return null;
      }
    }

    long r = modPositive(nums[firstIndex], modulus);
    if (nonstrictEnds) {
      if ((r != modPositive(firstNonstrict, modulus))
          || (r != modPositive(lastNonstrict, modulus))) {
        return null;
      }
    }

    return new long[] {r, modulus};
  }

  /**
   * The iterator produces Long values. This can be more efficient than modulus(long[]) if the
   * long[] doesn't already exist, because this does not necessarily examine every value produced by
   * its iterator.
   *
   * <p>For documentation, see {@link #modulusStrict(long[], boolean)}.
   *
   * @param itor iterator of operands; modified by this method
   * @param nonstrictEnds whether endpoints are NOT subject to the strict density requirement
   * @return an array of two integers (r,m) such that each number in NUMS is equal to r (mod m), or
   *     null if no such exists or the iterator contains fewer than 3 elements
   * @see #modulusStrict(int[], boolean)
   */
  public static long @Nullable @ArrayLen(2) [] modulusStrictLong(
      Iterator<Long> itor, boolean nonstrictEnds) {
    if (!itor.hasNext()) {
      return null;
    }

    long firstNonstrict = 0; // arbitrary initial value
    long lastNonstrict = 0; // arbitrary initial value
    if (nonstrictEnds) {
      firstNonstrict = itor.next().longValue();
    }

    long prev = itor.next().longValue();
    if (!itor.hasNext()) {
      return null;
    }
    long next = itor.next().longValue();
    long modulus = next - prev;
    if (modulus == 1) {
      return null;
    }
    int count = 2;
    while (itor.hasNext()) {
      prev = next;
      next = itor.next().longValue();
      if (nonstrictEnds && !itor.hasNext()) {
        lastNonstrict = next;
        break;
      }

      if (next - prev != modulus) {
        return null;
      }
      count++;
    }
    if (count < 3) {
      return null;
    }

    long r = MathPlume.modPositive(next, modulus);
    if (nonstrictEnds) {
      if ((r != modPositive(firstNonstrict, modulus))
          || (r != modPositive(lastNonstrict, modulus))) {
        return null;
      }
    }

    return new long[] {r, modulus};
  }

  ///
  /// Non-Modulus
  ///

  /**
   * Returns an array containing all the numbers <b>not</b> in its argument array (which must be
   * non-empty) but in the argument's range; that is, bigger than its argument's minimum value and
   * smaller than its argument's maximum value. The result contains no duplicates and is in order.
   *
   * @param nums numbers to be excluded; length &gt; 0; may contain duplicates
   * @return the set: [min(nums)..max(nums)] - nums
   */
  @SuppressWarnings({"allcheckers:purity", "lock"})
  @Pure
  @StaticallyExecutable
  public static int[] missingNumbers(int @MinLen(1) [] nums) {
    // avoid modifying parameter
    nums = nums.clone();
    Arrays.sort(nums);
    int min = nums[0];
    int max = nums[nums.length - 1];
    int sizeEstimate = max - min + 1 - nums.length;
    List<Integer> resultList = new ArrayList<>(sizeEstimate < 1 ? 1 : sizeEstimate);
    int val = min;
    for (int i = 0; i < nums.length; i++) {
      while (val < nums[i]) {
        resultList.add(val);
        val++;
      }
      if (val == nums[i]) {
        val++;
      }
    }
    int[] resultArray = new int[resultList.size()];
    for (int i = 0; i < resultArray.length; i++) {
      resultArray[i] = resultList.get(i).intValue();
    }
    return resultArray;
  }

  /**
   * This iterator returns all the numbers *not* in its argument array (which must be non-empty) but
   * in the argument's range; that is, bigger than its argument's minimum value and smaller than its
   * argument's maximum value. The result contains no duplicates and is in order. If boolean addEnds
   * is set, then the bracketing endpoints are also returned; otherwise, all returned values are
   * between the minimum and maximum of the original values.
   */
  static final class MissingNumbersIteratorInt implements Iterator<Integer> {
    // Exactly one of nums and numsItor is non-null.
    /** The numbers not to include in the iterator. */
    int @MonotonicNonNull @MinLen(1) [] nums;
    /** The numbers not to include in the iterator. */
    @MonotonicNonNull Iterator<Integer> numsItor;
    /** The current element of the numbers not to include in the iterator. */
    int currentNonmissing;
    /** The next element to be returned by the iterator. */
    int currentMissing;
    /** Used only if nums != null, in which case it is an index into nums. */
    @IndexFor("nums") int currentIndex;
    /**
     * If true, include the value just before the minimum excluded element and the value just after
     * the maximum excluded element.
     */
    boolean addEnds;

    /**
     * An iterator over all the numbers <b>not</b> in the argument array, but within its range.
     *
     * @param nums a non-empty array
     * @param addEnds if true, include the bracketing endpoints
     */
    MissingNumbersIteratorInt(int @MinLen(1) [] nums, boolean addEnds) {
      this.addEnds = addEnds;
      { // avoid modifying parameter
        int[] numsCopy = new int[nums.length];
        System.arraycopy(nums, 0, numsCopy, 0, nums.length);
        nums = numsCopy;
      }
      Arrays.sort(nums);
      this.nums = nums;
      currentIndex = 0;
      currentNonmissing = nums[currentIndex];
      if (addEnds) {
        currentMissing = currentNonmissing - 1;
      } else {
        currentMissing = currentNonmissing;
      }
    }

    /**
     * An iterator over all the numbers <b>not</b> in the argument iterator, but within its range.
     *
     * @param numsItor a non-empty iterator; it must return integers in sorted order
     * @param addEnds if true, include the bracketing endpoints
     */
    MissingNumbersIteratorInt(Iterator<Integer> numsItor, boolean addEnds) {
      this.addEnds = addEnds;
      if (!numsItor.hasNext()) {
        throw new Error("No elements in numsItor");
      }
      currentNonmissing = numsItor.next().intValue();
      if (addEnds) {
        currentMissing = currentNonmissing - 1;
      } else {
        currentMissing = currentNonmissing;
      }
      this.numsItor = numsItor;
      @SuppressWarnings("lowerbound:assignment") // unused variable, so value doesn't matter
      @IndexFor("nums") int unused = Integer.MIN_VALUE;
      currentIndex = unused;
    }

    @SuppressWarnings({
      "allcheckers:purity", // benevolent side effects
      "lock:method.guarantee.violated"
    })
    @Override
    public boolean hasNext(@GuardSatisfied MissingNumbersIteratorInt this) {
      if (currentMissing < currentNonmissing) {
        return true;
      }
      // This loop ("while" instead of "if") permits duplicates in nums.
      while (currentMissing == currentNonmissing) {
        if (nums != null) {
          @SuppressWarnings(
              "index:assignment" // This breaks the invariant, but it's checked right below and the
          // function exits.
          )
          @IndexFor("nums") int currentIndex_temp = currentIndex + 1;
          currentIndex = currentIndex_temp;
          if (currentIndex >= nums.length) {
            if (addEnds) {
              currentMissing++;
              return true;
            } else {
              return false;
            }
          }
          currentNonmissing = nums[currentIndex];
        } else if (numsItor != null) {
          if (!numsItor.hasNext()) {
            if (addEnds) {
              currentMissing++;
              return true;
            } else {
              return false;
            }
          }
          // prevNonmissing is for testing only
          int prevNonmissing = currentNonmissing;
          currentNonmissing = numsItor.next().intValue();
          if (!(prevNonmissing < currentNonmissing)) {
            throw new Error(
                "Non-sorted Iterator supplied to MissingNumbersIteratorInt: prevNonmissing = "
                    + prevNonmissing
                    + ", currentNonmissing = "
                    + currentNonmissing);
          }
        } else {
          throw new Error("Can't happen");
        }
        currentMissing++;
        return hasNext();
      }
      if (addEnds) {
        return (currentMissing == currentNonmissing + 1);
      } else {
        throw new Error("Can't happen: " + currentMissing + " " + currentNonmissing);
      }
    }

    @Override
    public Integer next(@GuardSatisfied MissingNumbersIteratorInt this) {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Integer result = currentMissing;
      currentMissing++;
      return result;
    }

    @Override
    public void remove(@GuardSatisfied MissingNumbersIteratorInt this) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Returns a tuple of (r,m) where no number in NUMS is equal to r (mod m) but all missing numbers
   * in their range are. Returns null if the input array has 0 length.
   *
   * @param nums the list of operands
   * @return a (remainder, modulus) pair that fails to match elements of nums
   */
  @SuppressWarnings({"allcheckers:purity", "lock"})
  @Pure
  @StaticallyExecutable
  public static int @Nullable @ArrayLen(2) [] nonmodulusStrict(int[] nums) {
    // This implementation is particularly inefficient; find a better way to
    // compute this.  Perhaps obtain the new modulus numbers incrementally
    // instead of all at once.
    if (nums.length == 0) {
      return null;
    }
    int range = ArraysPlume.elementRange(nums);
    if (range > 65536) {
      return null;
    }
    return nonmodulusStrictIntInternal(new MissingNumbersIteratorInt(nums, true));
  }

  /**
   * Helper for {@link #nonmodulusStrict(int[])}.
   *
   * @param missing the missing integers; modified by this method
   * @return value to be returned by {@link #nonmodulusStrict(int[])}: a tuple of (r,m) where all
   *     numbers in {@code missing} are equal to r (ood m)
   */
  private static int @Nullable @ArrayLen(2) [] nonmodulusStrictIntInternal(
      Iterator<Integer> missing) {
    // Must not use regular modulus:  that can produce errors, eg
    // nonmodulusStrict({1,2,3,5,6,7,9,11}) => {0,2}.  Thus, use
    // modulusStrict.
    CollectionsPlume.RemoveFirstAndLastIterator<Integer> missingNums =
        new CollectionsPlume.RemoveFirstAndLastIterator<Integer>(missing);
    int[] result = modulusStrictInt(missingNums, false);
    if (result == null) {
      return result;
    }
    if (!checkFirstAndLastNonmodulus(result, missingNums)) {
      return null;
    }

    return result;
  }

  /**
   * @param rm a tuple of (r,m)
   * @param rfali a sequence of numbers, plus a first and last element outside their range. This
   *     iterator has already been iterated all the way to its end.
   * @return true if the first and last elements are not equal to r (mod m)
   */
  private static boolean checkFirstAndLastNonmodulus(
      int @ArrayLen(2) [] rm, CollectionsPlume.RemoveFirstAndLastIterator<Integer> rfali) {
    int r = rm[0];
    int m = rm[1];
    int first = rfali.getFirst().intValue();
    int last = rfali.getLast().intValue();
    return ((r != modPositive(first, m)) && (r != modPositive(last, m)));
  }

  /**
   * Returns a tuple of (r,m) where no number in NUMS is equal to r (mod m) but all missing numbers
   * in their range are.
   *
   * @param nums the list of operands
   * @return a (remainder, modulus) pair that fails to match elements of nums
   */
  public static int @Nullable @ArrayLen(2) [] nonmodulusStrictInt(Iterator<Integer> nums) {
    return nonmodulusStrictIntInternal(new MissingNumbersIteratorInt(nums, true));
  }

  // Old, slightly less efficient implementation that uses the version of
  // missingNumbers that returns an array instead of an Iterator.
  // /**
  //  * Returns a tuple of (r,m) where no number in NUMS is equal to r (mod
  //  * m) but all missing numbers in their range are.
  //  */
  // public static int @Nullable @ArrayLen(2) [] nonmodulusStrict(int[] nums) {
  //   // This implementation is particularly inefficient; find a better way to
  //   // compute this.  Perhaps obtain the new modulus numbers incrementally
  //   // instead of all at once.
  //   if (nums.length == 0) {
  //     return null;
  //   }
  //   int range = ArraysPlume.elementRange(nums);
  //   if (range > 65536) {
  //     return null;
  //   }
  //   return modulus(missingNumbers(nums));
  // }

  /**
   * Returns a tuple of (r,m) where no number in NUMS is equal to r (mod m) but for every number in
   * NUMS, at least one is equal to every non-r remainder. The modulus is chosen as small as
   * possible, but no greater than half the range of the input numbers (else null is returned).
   *
   * @param nums the list of operands
   * @return a (remainder, modulus) pair that fails to match elements of nums
   */
  // This seems to give too many false positives (or maybe my probability
  // model was wrong); use nonmodulusStrict instead.
  @SuppressWarnings("allcheckers:purity")
  @Pure
  @StaticallyExecutable
  public static int @Nullable @ArrayLen(2) [] nonmodulusNonstrict(int[] nums) {
    if (nums.length < 4) {
      return null;
    }
    int maxModulus = Math.min(nums.length / 2, ArraysPlume.elementRange(nums) / 2);

    // System.out.println("nums.length=" + nums.length + ", range=" +
    // ArraysPlume.elementRange(nums) + ", maxModulus=" + maxModulus);

    // no real sense checking 2, as commonModulus would have found it, but
    // include it to make this function stand on its own
    for (int m = 2; m <= maxModulus; m++) {
      // System.out.println("Trying m=" + m);
      boolean[] hasModulus = new boolean[m]; // initialized to false?
      int numNonmodulus = m;
      for (int i = 0; i < nums.length; i++) {
        @IndexFor("hasModulus") int rem = modPositive(nums[i], m);
        if (!hasModulus[rem]) {
          hasModulus[rem] = true;
          numNonmodulus--;
          // System.out.println("rem=" + rem + " for " + nums[i] + "; numNonmodulus=" +
          // numNonmodulus);
          if (numNonmodulus == 0) {
            // Quit as soon as we see every remainder instead of processing
            // each element of the input list.
            break;
          }
        }
      }
      // System.out.println("For m=" + m + ", numNonmodulus=" + numNonmodulus);
      if (numNonmodulus == 1) {
        return new int[] {ArraysPlume.indexOf(hasModulus, false), m};
      }
    }
    return null;
  }

  /// non-modulus for long (as opposed to int) values

  /**
   * Returns an array containing all the numbers <b>not</b> in its argument array (which must be
   * non-empty) but in the argument's range; that is, bigger than its argument's minimum value and
   * smaller than its argument's maximum value. The result contains no duplicates and is in order.
   *
   * @param nums numbers to be excluded; length &gt; 0; may contain duplicates
   * @return the set: [min(nums)..max(nums)] - nums
   */
  @SuppressWarnings({"allcheckers:purity", "lock"})
  @Pure
  @StaticallyExecutable
  public static long[] missingNumbers(long @MinLen(1) [] nums) {
    // avoid modifying parameter
    nums = nums.clone();
    Arrays.sort(nums);
    long min = nums[0];
    long max = nums[nums.length - 1];
    int sizeEstimate = ((int) (max - min + 1 - nums.length));
    List<Long> resultList = new ArrayList<>(sizeEstimate < 1 ? 1 : sizeEstimate);
    long val = min;
    for (int i = 0; i < nums.length; i++) {
      while (val < nums[i]) {
        resultList.add(val);
        val++;
      }
      if (val == nums[i]) {
        val++;
      }
    }

    long[] resultArray = new long[resultList.size()];
    for (int i = 0; i < resultArray.length; i++) {
      resultArray[i] = resultList.get(i).longValue();
    }
    return resultArray;
  }

  /**
   * This iterator returns all the numbers *not* in its argument array (which must be non-empty) but
   * in the argument's range; that is, bigger than its argument's minimum value and smaller than its
   * argument's maximum value. The result contains no duplicates and is in order. If boolean addEnds
   * is set, then the bracketing endpoints are also returned; otherwise, all returned values are
   * between the minimum and maximum of the original values.
   */
  static final class MissingNumbersIteratorLong implements Iterator<Long> {
    // Exactly one of nums and numsItor is non-null.
    /** The numbers not to include in the iterator. */
    long @MonotonicNonNull @MinLen(1) [] nums;
    /** The numbers not to include in the iterator. */
    @MonotonicNonNull Iterator<Long> numsItor;
    /** The current element of the numbers not to include in the iterator. */
    long currentNonmissing;
    /** The next element to be returned by the iterator. */
    long currentMissing;
    /** Used only if nums != null, in which case it is an index into nums. */
    @IndexFor("nums") int currentIndex;
    /**
     * If true, include the value just before the minimum excluded element and the value just after
     * the maximum excluded element.
     */
    boolean addEnds;

    /**
     * An iterator over all the numbers <b>not</b> in its original argument array, but within its
     * range.
     *
     * @param nums a non-empty array
     * @param addEnds if true, include the bracketing endpoints
     */
    MissingNumbersIteratorLong(long @MinLen(1) [] nums, boolean addEnds) {
      this.addEnds = addEnds;
      { // avoid modifying parameter
        long[] numsCopy = new long[nums.length];
        System.arraycopy(nums, 0, numsCopy, 0, nums.length);
        nums = numsCopy;
      }
      Arrays.sort(nums);
      this.nums = nums;
      currentIndex = 0;
      currentNonmissing = nums[currentIndex];
      if (addEnds) {
        currentMissing = currentNonmissing - 1;
      } else {
        currentMissing = currentNonmissing;
      }
    }

    /**
     * An iterator over all the numbers <b>not</b> in its argument iterator, but within its range.
     *
     * @param numsItor a non-empty array; must return longs in sorted order
     * @param addEnds if true, include the bracketing endpoints
     */
    MissingNumbersIteratorLong(Iterator<Long> numsItor, boolean addEnds) {
      this.addEnds = addEnds;
      if (!numsItor.hasNext()) {
        throw new Error("No elements in numsItor");
      }
      currentNonmissing = numsItor.next().longValue();
      if (addEnds) {
        currentMissing = currentNonmissing - 1;
      } else {
        currentMissing = currentNonmissing;
      }
      this.numsItor = numsItor;
      @SuppressWarnings("lowerbound:assignment") // unused variable, so value doesn't matter
      @IndexFor("nums") int unused = Integer.MIN_VALUE;
      currentIndex = unused;
    }

    @SuppressWarnings({
      "allcheckers:purity", // benevolent side effects
      "lock:method.guarantee.violated"
    })
    @Override
    public boolean hasNext(@GuardSatisfied MissingNumbersIteratorLong this) {
      if (currentMissing < currentNonmissing) {
        return true;
      }
      // This loop ("while" instead of "if") permits duplicates in nums.
      while (currentMissing == currentNonmissing) {
        if (nums != null) {
          @SuppressWarnings(
              "index:assignment" // This breaks the invariant, but it's checked right below and the
          // function exits.
          )
          @IndexFor("nums") int currentIndex_temp = currentIndex + 1;
          currentIndex = currentIndex_temp;
          if (currentIndex >= nums.length) {
            if (addEnds) {
              currentMissing++;
              return true;
            } else {
              return false;
            }
          }
          currentNonmissing = nums[currentIndex];
        } else if (numsItor != null) {
          if (!numsItor.hasNext()) {
            if (addEnds) {
              currentMissing++;
              return true;
            } else {
              return false;
            }
          }
          // prevNonmissing is for testing only
          long prevNonmissing = currentNonmissing;
          currentNonmissing = numsItor.next().longValue();
          if (!(prevNonmissing < currentNonmissing)) {
            throw new Error(
                "Non-sorted Iterator supplied to MissingNumbersIteratorLong: prevNonmissing = "
                    + prevNonmissing
                    + ", currentNonmissing = "
                    + currentNonmissing);
          }
        } else {
          throw new Error("Can't happen");
        }
        currentMissing++;
        return hasNext();
      }
      if (addEnds) {
        return (currentMissing == currentNonmissing + 1);
      } else {
        throw new Error("Can't happen: " + currentMissing + " " + currentNonmissing);
      }
    }

    @Override
    public Long next(@GuardSatisfied MissingNumbersIteratorLong this) {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Long result = currentMissing;
      currentMissing++;
      return result;
    }

    @Override
    public void remove(@GuardSatisfied MissingNumbersIteratorLong this) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Returns a tuple of (r,m) where no number in NUMS is equal to r (mod m) but all missing numbers
   * in their range are. Returns null if the input array has 0 length.
   *
   * @param nums the list of operands
   * @return a (remainder, modulus) pair that fails to match elements of nums
   */
  @SuppressWarnings({"allcheckers:purity", "lock"})
  @Pure
  @StaticallyExecutable
  public static long @Nullable @ArrayLen(2) [] nonmodulusStrict(long[] nums) {
    // This implementation is particularly inefficient; find a better way to
    // compute this.  Perhaps obtain the new modulus numbers incrementally
    // instead of all at once.
    if (nums.length == 0) {
      return null;
    }
    long range = ArraysPlume.elementRange(nums);
    if (range > 65536) {
      return null;
    }
    return nonmodulusStrictLongInternal(new MissingNumbersIteratorLong(nums, true));
  }

  /**
   * Helper for {@link #nonmodulusStrict(long[])}.
   *
   * @param missing the missing integers; modified by this method
   * @return value to be returned by {@link #nonmodulusStrict(long[])}
   */
  private static long @Nullable @ArrayLen(2) [] nonmodulusStrictLongInternal(
      Iterator<Long> missing) {
    // Must not use regular modulus:  that can produce errors, eg
    // nonmodulusStrict({1,2,3,5,6,7,9,11}) => {0,2}.  Thus, use
    // modulusStrict.
    CollectionsPlume.RemoveFirstAndLastIterator<Long> missingNums =
        new CollectionsPlume.RemoveFirstAndLastIterator<Long>(missing);
    long[] result = modulusStrictLong(missingNums, false);
    if (result == null) {
      return result;
    }
    if (!checkFirstAndLastNonmodulus(result, missingNums)) {
      return null;
    }

    return result;
  }

  /**
   * @param rm an an array containing two elements
   * @param rfali a sequence of numbers, plus a first and last element outside their range. This
   *     iterator has already been iterated all the way to its end.
   * @return true if the first and last elements are equal to r (mod m)
   */
  @Pure
  private static boolean checkFirstAndLastNonmodulus(
      long @ArrayLen(2) [] rm, CollectionsPlume.RemoveFirstAndLastIterator<Long> rfali) {
    long r = rm[0];
    long m = rm[1];
    long first = rfali.getFirst().longValue();
    long last = rfali.getLast().longValue();
    return ((r != modPositive(first, m)) && (r != modPositive(last, m)));
  }

  /**
   * Returns a tuple of (r,m) where no number in NUMS is equal to r (mod m) but all missing numbers
   * in their range are.
   *
   * @param nums the list of operands
   * @return a (remainder, modulus) pair that fails to match elements of nums
   */
  public static long @Nullable @ArrayLen(2) [] nonmodulusStrictLong(Iterator<Long> nums) {
    return nonmodulusStrictLongInternal(new MissingNumbersIteratorLong(nums, true));
  }

  // Old, slightly less efficient implementation that uses the version of
  // missingNumbers that returns an array instead of an Iterator.
  // /**
  //  * Returns a tuple of (r,m) where no number in NUMS is equal to r (mod
  //  * m) but all missing numbers in their range are.
  //  */
  // public static long @Nullable @ArrayLen(2) [] nonmodulusStrict(long[] nums) {
  //   // This implementation is particularly inefficient; find a better way to
  //   // compute this.  Perhaps obtain the new modulus numbers incrementally
  //   // instead of all at once.
  //   if (nums.length == 0) {
  //     return null;
  //   }
  //   long range = ArraysPlume.elementRange(nums);
  //   if (range > 65536) {
  //     return null;
  //   }
  //   return modulus(missingNumbers(nums));
  // }

  /**
   * Returns a tuple of (r,m) where no number in NUMS is equal to r (mod m) but for every number in
   * NUMS, at least one is equal to every non-r remainder. The modulus is chosen as small as
   * possible, but no greater than half the range of the input numbers (else null is returned).
   *
   * @param nums the list of operands
   * @return a (remainder, modulus) pair that fails to match elements of nums
   */
  // This seems to give too many false positives (or maybe my probability
  // model was wrong); use nonmodulusStrict instead.
  @SuppressWarnings("allcheckers:purity")
  @Pure
  @StaticallyExecutable
  public static long @Nullable @ArrayLen(2) [] nonmodulusNonstrict(long[] nums) {
    if (nums.length < 4) {
      return null;
    }
    int maxModulus = (int) Math.min(nums.length / 2, ArraysPlume.elementRange(nums) / 2);

    // System.out.println("nums.length=" + nums.length + ", range=" +
    // ArraysPlume.elementRange(nums) + ", maxModulus=" + maxModulus);

    // no real sense checking 2, as commonModulus would have found it, but
    // include it to make this function stand on its own
    for (int m = 2; m <= maxModulus; m++) {
      // System.out.println("Trying m=" + m);
      boolean[] hasModulus = new boolean[m]; // initialized to false?
      int numNonmodulus = m;
      for (int i = 0; i < nums.length; i++) {
        @IndexFor("hasModulus") int rem = (int) modPositive(nums[i], m);
        if (!hasModulus[rem]) {
          hasModulus[rem] = true;
          numNonmodulus--;
          // System.out.println("rem=" + rem + " for " + nums[i] + "; numNonmodulus=" +
          // numNonmodulus);
          if (numNonmodulus == 0) {
            // Quit as soon as we see every remainder instead of processing
            // each element of the input list.
            break;
          }
        }
      }
      // System.out.println("For m=" + m + ", numNonmodulus=" + numNonmodulus);
      if (numNonmodulus == 1) {
        return new long[] {ArraysPlume.indexOf(hasModulus, false), m};
      }
    }
    return null;
  }
}
