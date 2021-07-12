package org.plumelib.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Routines for doing approximate ('fuzzy') floating-point comparisons. Those are comparisons that
 * only require the floating-point numbers to be relatively close to one another to be equal, rather
 * than exactly equal.
 *
 * <p>Floating-point numbers are compared for equality by dividing them by one another and comparing
 * the ratio. By default, they must be within 0.0001 (0.01%) to be considered equal.
 *
 * <p>Zero is never considered equal to a non-zero number, no matter how small its value.
 *
 * <p>Two NaN floats are not considered equal (consistent with the == operator).
 */
public class FuzzyFloat {

  /** Default relative difference between two values such that this class considers them equal. */
  double DEFAULT_RELATIVE_RATIO = .0001;

  /** Minimum ratio between two floats, such that this class considers them equal. */
  double minRatio;
  /** Maximum ratio between two floats, such that this class considers them equal. */
  double maxRatio;

  /**
   * True if this class does approximate (fuzzy) arithmetic comparisons. If false, this class does
   * exact matching
   *
   * <p>ratio test turned off. This occurs exactly if the class is instantiated with the relative
   * difference 0.
   */
  boolean exactComparisons = false;

  /** Creates a FuzzyFloat with the default relativeRatio value of .0001. */
  public FuzzyFloat() {
    setRelativeRatio(DEFAULT_RELATIVE_RATIO);
  }

  /**
   * Creates a FuzzyFloat. Specify the specific relative difference allowed between two floats in
   * order for them to be equal. The default is 0.0001. A relative diff of zero, disables it (i.e.,
   * this class's methods work just like regular Java arithmetic comparisons).
   *
   * @param relativeRatio the relative diff to use; see {@link #setRelativeRatio}
   */
  public FuzzyFloat(double relativeRatio) {
    setRelativeRatio(relativeRatio);
  }

  /**
   * Set all the fields of this class.
   *
   * @param relativeRatio the new relative diff to use; a number near zero (zero is also permitted,
   *     which requires exact matching rather than permitting fuzzy matching)
   * @see #FuzzyFloat
   */
  public void setRelativeRatio(@UnknownInitialization FuzzyFloat this, double relativeRatio) {
    minRatio = 1 - relativeRatio;
    maxRatio = 1 + relativeRatio;
    exactComparisons = (relativeRatio == 0.0);
    // System.out.println ("minRatio = " + minRatio + ", maxRatio = "
    //                    + maxRatio);

  }

  /**
   * Test d1 and d2 for equality using the current ratio. Two NaN floats are not considered equal
   * (consistent with the == operator).
   *
   * <p>Note that if one of the numbers if 0.0, then the other number must be less than the square
   * of the fuzzy ratio. This policy accommodates roundoff errors in floating-point values.
   *
   * @param d1 the first value to compare
   * @param d2 the second value to compare
   * @return true if d1 and d2 are considered equal, false otherwise
   */
  @Pure
  public boolean eq(double d1, double d2) {

    // NaNs are not considered equal.
    if (Double.isNaN(d1) && Double.isNaN(d2)) {
      return (false);
    }

    // if zero was specified for a ratio, don't do the divide.  You might
    // get slightly different answers.  And this should be faster.
    if (exactComparisons) {
      return (d1 == d2);
    }

    // slightly more efficient for matches and catches positive and negative
    // infinity (which match in this test, but not below)
    if (d1 == d2) {
      return (true);
    }

    // When one number is 0, require that the other is less than the square of the fuzzy ratio.
    // This heuristic accommodates roundoff errors in floating-point values.

    if (d1 == 0.0 || d2 == 0.0) {

      double zeroTolerance = Math.pow((maxRatio - 1), 2);

      if (d1 == 0.0) {

        return (Math.abs(d2) < zeroTolerance);

      } else {

        return (Math.abs(d1) < zeroTolerance);
      }
    }

    double ratio = d1 / d2;
    return ((ratio >= minRatio) && (ratio <= maxRatio));
  }

  /**
   * Test d1 and d2 for non-equality using the current ratio.
   *
   * @param d1 the first value to compare
   * @param d2 the second value to compare
   * @return whether d1 and d2 are non-equal
   * @see #eq
   */
  @Pure
  public boolean ne(double d1, double d2) {
    return (!eq(d1, d2));
  }

  /**
   * Test d1 and d2 for {@code d1 < d2}. If d1 is equal to d2 using the current ratio this returns
   * false.
   *
   * @param d1 the first value to compare
   * @param d2 the second value to compare
   * @return whether d1 &lt; d2
   * @see #eq
   */
  @Pure
  public boolean lt(double d1, double d2) {
    return ((d1 < d2) && ne(d1, d2));
  }

  /**
   * Test d1 and d2 for {@code d1 <= d2}. If d1 is equal to d2 using the current ratio, this returns
   * true.
   *
   * @param d1 the first value to compare
   * @param d2 the second value to compare
   * @return whether d1 &le; d2
   * @see #eq
   */
  @Pure
  public boolean lte(double d1, double d2) {
    return ((d1 <= d2) || eq(d1, d2));
  }

  /**
   * test d1 and d2 for {@code d1 > d2}. IF d1 is equal to d2 using the current ratio, this returns
   * false.
   *
   * @param d1 the first value to compare
   * @param d2 the second value to compare
   * @return whether d1 &gt; d2
   * @see #eq
   */
  @Pure
  public boolean gt(double d1, double d2) {
    return ((d1 > d2) && ne(d1, d2));
  }

  /**
   * test d1 and d2 for {@code d1 >= d2}. If d1 is equal to d2 using the current ratio, this returns
   * true.
   *
   * @param d1 the first value to compare
   * @param d2 the second value to compare
   * @return whether d1 &ge; d2
   * @see #eq
   */
  @Pure
  public boolean gte(double d1, double d2) {
    return ((d1 >= d2) || eq(d1, d2));
  }

  /**
   * Searches for the first occurrence of elt in a. elt is considered equal to a[i] if it passes the
   * {@link #eq} test.
   *
   * @param a the array to search
   * @param elt the element to search for
   * @return the first index containing the specified element, or -1 if the element is not found in
   *     the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public int indexOf(double[] a, double elt) {
    for (int i = 0; i < a.length; i++) {
      if (eq(elt, a[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of {@code a} that matches {@code sub} elementwise. Elements
   * of {@code sub} are considered to match elements of {@code a} if they pass the {@link #eq} test.
   *
   * @param a the sequence to search in
   * @param sub the sequence to search for
   * @return the first index whose subarray is equal to the specified array or -1 if no such
   *     subarray is found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public int indexOf(double[] a, double[] sub) {

    int aIndexMax = a.length - sub.length;

    outer:
    for (int i = 0; i <= aIndexMax; i++) {
      for (int j = 0; j < sub.length; j++) {
        if (ne(a[i + j], sub[j])) {
          continue outer;
        }
      }
      return (i);
    }
    return (-1);
  }

  /**
   * Determines whether or not a1 and a2 are set equivalent (contain only the same elements).
   * Element comparison uses {@link #eq}.
   *
   * <p>Note that this implementation is optimized for cases where the elements are actually the
   * same, since it does a sort of both arrays before starting the comparisons.
   *
   * @param a1 the first set to compare
   * @param a2 the second set to compare
   * @return true if a1 and a2 are set equivalent, false otherwise
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (arrays)
  @Pure
  public boolean isElemMatch(double[] a1, double[] a2) {

    // don't change our parameters
    a1 = a1.clone();
    a2 = a2.clone();

    Arrays.sort(a1);
    Arrays.sort(a2);

    // look for elements of a2 in a1
    int start = 0;
    outer1:
    for (int i = 0; i < a2.length; i++) {
      double val = a2[i];
      for (int j = start; j < a1.length; j++) {
        if (eq(val, a1[j])) {
          start = j;
          continue outer1;
        }
        if (val < a1[j]) {
          // System.out.println ("isElemMatch: " + val + " " + a1[j]);
          return (false);
        }
      }
      // System.out.println ("isElemMatch: " + i);
      return (false);
    }

    // look for elements of a1 in a2
    start = 0;
    outer2:
    for (int i = 0; i < a1.length; i++) {
      double val = a1[i];
      for (int j = start; j < a2.length; j++) {
        if (eq(val, a2[j])) {
          start = j;
          continue outer2;
        }
        if (val < a2[j]) {
          // System.out.println ("isElemMatch: " + val + " " + a2[j]);
          return (false);
        }
      }
      // System.out.println ("isElemMatch: " + i);
      return (false);
    }

    return (true);
  }

  // Slightly more efficient method that will miss some matches
  //     int i = 0;
  //     int j = 0;
  //     while (i < a1.length && j < a2.length) {
  //       if (ne (a1[i], a2[j])) {
  //         System.out.println ("isElemMatch: " + a1[i] + " " + a2[j]);
  //         return (false);
  //       }
  //       double val = a1[i];
  //       i++;
  //       while ((i < a1.length) && (eq (a1[i], val))) {
  //         i++;
  //       }
  //       j++;
  //       while ((j < a2.length) && (eq (a2[j], val))) {
  //         j++;
  //       }
  //     }

  //     // if there are any elements left, then they don't match.
  //     if ((i != a1.length) || (j != a2.length)) {
  //       System.out.println ("isElemMatch: " + i + " " + j);
  //       return (false);
  //     }

  //     return (true);
  //     }

  /** Lexically compares two double arrays. */
  public class DoubleArrayComparatorLexical implements Comparator<double[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Lexically compares o1 and o2 as double arrays.
     *
     * @param a1 the first array to compare
     * @param a2 the second array to compare
     * @return positive if o1 &gt; 02, 0 if o1 == o2, negative if o1 &lt; o2
     */
    @Pure
    @Override
    public int compare(double[] a1, double[] a2) {
      if (a1 == a2) {
        return 0;
      }
      int len = Math.min(a1.length, a2.length);
      for (int i = 0; i < len; i++) {
        if (ne(a1[i], a2[i])) {
          return ((a1[i] > a2[i]) ? 1 : -1);
        }
      }
      return a1.length - a2.length;
    }
  }

  /**
   * Determines whether smaller is a subset of bigger. Element comparison uses {@link #eq}.
   *
   * <p>Note that this implementation is optimized for cases where the elements are actually the
   * same, since it does a sort of both arrays before starting the comparisons.
   *
   * @param smaller the possibly-smaller subset
   * @param bigger the possibly-larger set
   * @return true if smaller is a subset (each element of smaller is also a element of bigger) of
   *     bigger, false otherwise
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (arrays)
  @Pure
  public boolean isSubset(double[] smaller, double[] bigger) {

    // don't change our parameters
    smaller = smaller.clone();
    bigger = bigger.clone();

    Arrays.sort(smaller);
    Arrays.sort(bigger);

    // look for elements of smaller in bigger
    int start = 0;
    outer1:
    for (int i = 0; i < smaller.length; i++) {
      double val = smaller[i];
      for (int j = start; j < bigger.length; j++) {
        if (eq(val, bigger[j])) {
          start = j;
          continue outer1;
        }
        if (val < bigger[j]) {
          return (false);
        }
      }
      return (false);
    }

    return (true);
  }
}
