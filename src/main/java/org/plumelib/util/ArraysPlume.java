// If you edit this file, you must also edit its tests.

package org.plumelib.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.RandomAccess;
import java.util.Set;
import java.util.StringJoiner;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.IndexOrLow;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.SameLen;
import org.checkerframework.checker.interning.qual.PolyInterned;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.common.value.qual.ArrayLen;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.plumelib.reflection.ReflectionPlume;

/** Utilities for manipulating arrays. This complements {@link java.util.Arrays}. */
@SuppressWarnings("interning") // to do later
public final class ArraysPlume {
  /** This class is a collection of methods; it does not represent anything. */
  private ArraysPlume() {
    throw new Error("do not instantiate");
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Creation
  ///

  /**
   * Returns an array consisting of n copies of the specified object.
   *
   * @param <T> the class of the object to copy. The returned array's element type is the
   *     <em>run-time</em> type of {@code o}.
   * @param n the number of elements in the returned array
   * @param o the element to appear repeatedly in the returned array; must not be null
   * @return an array consisting of n copies of the specified object
   */
  public static <T extends Object> T[] nCopies(@NonNegative int n, T o) {
    @SuppressWarnings("unchecked")
    T[] result = (T[]) Array.newInstance(o.getClass(), n);
    Arrays.fill(result, o);
    return result;
  }

  /**
   * Concatenates an array and an element into a new array.
   *
   * @param <T> the type of the array elements
   * @param array the array
   * @param lastElt the new last elemeent
   * @return a new array containing the array elements and the last element, in that order
   */
  @SuppressWarnings({
    "unchecked",
    "index:array.access.unsafe.high" // addition in array length
  })
  public static <T> T[] append(T[] array, T lastElt) {
    @SuppressWarnings({"unchecked", "nullness:assignment"})
    T[] result = Arrays.copyOf(array, array.length + 1);
    result[array.length] = lastElt;
    return result;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// min, max
  ///

  // Could also add linear-time orderStatistics if I liked.

  /**
   * Returns the smallest value in the array.
   *
   * @param a an array
   * @return the smallest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static int min(int[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to min(int[])");
    }
    int result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = Math.min(result, a[i]);
    }
    return result;
  }

  /**
   * Returns the smallest value in the array.
   *
   * @param a an array
   * @return the smallest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static long min(long[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to min(long[])");
    }
    long result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = Math.min(result, a[i]);
    }
    return result;
  }

  /**
   * Returns the smallest value in the array.
   *
   * @param a an array
   * @return the smallest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static double min(double[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to min(double[])");
    }
    double result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = Math.min(result, a[i]);
    }
    return result;
  }

  /**
   * Returns the smallest value in the array.
   *
   * @param a an array
   * @return the smallest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static Integer min(Integer[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to min(Integer[])");
    }
    Integer result = a[0]; // to return a value actually in the array
    int resultInt = result.intValue(); // for faster comparison
    for (int i = 1; i < a.length; i++) {
      if (a[i].intValue() < resultInt) {
        result = a[i];
        resultInt = result.intValue();
      }
    }
    return result;
  }

  /**
   * Returns the smallest value in the array.
   *
   * @param a an array
   * @return the smallest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static Long min(Long[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to min(Long[])");
    }
    Long result = a[0]; // to return a value actually in the array
    long resultLong = result.longValue(); // for faster comparison
    for (int i = 1; i < a.length; i++) {
      if (a[i].longValue() < resultLong) {
        result = a[i];
        resultLong = result.longValue();
      }
    }
    return result;
  }

  /**
   * Returns the smallest value in the array.
   *
   * @param a an array
   * @return the smallest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static Double min(Double[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to min(Double[])");
    }
    Double result = a[0]; // to return a value actually in the array
    double resultDouble = result.doubleValue(); // for faster comparison
    for (int i = 1; i < a.length; i++) {
      if (a[i].doubleValue() < resultDouble) {
        result = a[i];
        resultDouble = result.doubleValue();
      }
    }
    return result;
  }

  /**
   * Returns the largest value in the array.
   *
   * @param a an array
   * @return the largest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static int max(int[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to max(int[])");
    }
    int result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = Math.max(result, a[i]);
    }
    return result;
  }

  /**
   * Returns the largest value in the array.
   *
   * @param a an array
   * @return the largest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static long max(long[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to max(long[])");
    }
    long result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = Math.max(result, a[i]);
    }
    return result;
  }

  /**
   * Returns the largest value in the array.
   *
   * @param a an array
   * @return the largest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static double max(double[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to max(double[])");
    }
    double result = a[0];
    for (int i = 1; i < a.length; i++) {
      result = Math.max(result, a[i]);
    }
    return result;
  }

  /**
   * Returns the largest value in the array.
   *
   * @param a an array
   * @return the largest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static Integer max(Integer[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to max(Integer[])");
    }
    Integer result = a[0]; // to return a value actually in the array
    int resultInt = result.intValue(); // for faster comparison
    for (int i = 1; i < a.length; i++) {
      if (a[i].intValue() > resultInt) {
        result = a[i];
        resultInt = result.intValue();
      }
    }
    return result;
  }

  /**
   * Returns the largest value in the array.
   *
   * @param a an array
   * @return the largest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static Long max(Long[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to max(Long[])");
    }
    Long result = a[0]; // to return a value actually in the array
    long resultLong = result.longValue(); // for faster comparison
    for (int i = 1; i < a.length; i++) {
      if (a[i].longValue() > resultLong) {
        result = a[i];
        resultLong = result.longValue();
      }
    }
    return result;
  }

  /**
   * Returns the largest value in the array.
   *
   * @param a an array
   * @return the largest value in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  public static Double max(Double[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to max(Double[])");
    }
    Double result = a[0]; // to return a value actually in the array
    double resultDouble = result.doubleValue(); // for faster comparison
    for (int i = 1; i < a.length; i++) {
      if (a[i].doubleValue() > resultDouble) {
        result = a[i];
        resultDouble = result.doubleValue();
      }
    }
    return result;
  }

  /**
   * Returns a two-element array containing the smallest and largest values in the array.
   *
   * @param a an array
   * @return a two-element array containing the smallest and largest values in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @SideEffectFree // Deterministic up to .equals(), but not ==
  // @StaticallyExecutable
  public static int @ArrayLen(2) [] minAndMax(int[] a) {
    if (a.length == 0) {
      // return null;
      throw new ArrayIndexOutOfBoundsException("Empty array passed to minAndMax(int[])");
    }
    int resultMin = a[0];
    int resultMax = a[0];
    for (int i = 1; i < a.length; i++) {
      resultMin = Math.min(resultMin, a[i]);
      resultMax = Math.max(resultMax, a[i]);
    }
    return new int[] {resultMin, resultMax};
  }

  /**
   * Returns a two-element array containing the smallest and largest values in the array.
   *
   * @param a an array
   * @return a two-element array containing the smallest and largest values in the array
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @SideEffectFree // Deterministic up to .equals(), but not ==
  // @StaticallyExecutable
  public static long @ArrayLen(2) [] minAndMax(long[] a) {
    if (a.length == 0) {
      // return null;
      throw new ArrayIndexOutOfBoundsException("Empty array passed to minAndMax(long[])");
    }
    long resultMin = a[0];
    long resultMax = a[0];
    for (int i = 1; i < a.length; i++) {
      resultMin = Math.min(resultMin, a[i]);
      resultMax = Math.max(resultMax, a[i]);
    }
    return new long[] {resultMin, resultMax};
  }

  /**
   * Returns the difference between the smallest and largest array elements.
   *
   * @param a an array
   * @return the difference between the smallest and largest array elements
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  @StaticallyExecutable
  public static int elementRange(int[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to elementRange(int[])");
    }
    @SuppressWarnings({
      "allcheckers:purity.not.deterministic.call",
      "allcheckers:method.guarantee.violated"
    }) // pure up to .equals(), which is enough for arithmetic
    int[] minAndMax = minAndMax(a);
    return minAndMax[1] - minAndMax[0];
  }

  /**
   * Returns the difference between the smallest and largest array elements.
   *
   * @param a an array
   * @return the difference between the smallest and largest array elements
   * @throws ArrayIndexOutOfBoundsException if the array has length 0
   */
  @Pure
  @StaticallyExecutable
  public static long elementRange(long[] a) {
    if (a.length == 0) {
      throw new ArrayIndexOutOfBoundsException("Empty array passed to elementRange(long[])");
    }
    @SuppressWarnings({
      "allcheckers:purity.not.deterministic.call",
      "allcheckers:method.guarantee.violated"
    }) // pure up to .equals(), which is enough for arithmetic
    long[] minAndMax = minAndMax(a);
    return minAndMax[1] - minAndMax[0];
  }

  ///////////////////////////////////////////////////////////////////////////
  /// sum
  ///

  /**
   * Returns the sum of an array of integers.
   *
   * @param a an array
   * @return the sum of an array of integers
   */
  @Pure
  @StaticallyExecutable
  public static int sum(int[] a) {
    int sum = 0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i];
    }
    return sum;
  }

  /**
   * Returns the sum of all the elements of a 2d array of integers.
   *
   * @param a a 2d array
   * @return the sum of all the elements of a 2d array of integers
   */
  @Pure
  @StaticallyExecutable
  public static int sum(int[][] a) {
    int sum = 0;
    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < a[i].length; j++) {
        sum += a[i][j];
      }
    }
    return sum;
  }

  /**
   * Returns the sum of an array of doubles.
   *
   * @param a an array
   * @return the sum of an array of doubles
   */
  @Pure
  @StaticallyExecutable
  public static double sum(double[] a) {
    double sum = 0;
    for (int i = 0; i < a.length; i++) {
      sum += a[i];
    }
    return sum;
  }

  /**
   * Returns the sum of all the elements of a 2d array of doubles.
   *
   * @param a a 2d array
   * @return the sum of all the elements of a 2d array of doubles
   */
  @Pure
  @StaticallyExecutable
  public static double sum(double[][] a) {
    double sum = 0;
    for (int i = 0; i < a.length; i++) {
      for (int j = 0; j < a[i].length; j++) {
        sum += a[i][j];
      }
    }
    return sum;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// indexOf
  ///

  /**
   * Searches for the first occurrence of the given element in the array, testing for equality using
   * the equals method.
   *
   * @param <T> type of the elements of the array
   * @param a an array
   * @param elt the element to search for
   * @return the first index whose element is equal to the specified element, or -1 if no such
   *     element is found in the array
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Pure
  public static <T extends @Nullable Object> int indexOf(T[] a, @Nullable Object elt) {
    if (elt == null) {
      return indexOfEq(a, elt);
    }
    for (int i = 0; i < a.length; i++) {
      if (elt.equals(a[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array, within the given
   * boundaries, testing for equality using the equals method.
   *
   * @param <T> the type of the elements
   * @param a an array
   * @param elt the element to search for
   * @param minindex first index at which to search
   * @param indexlimit first index at which not to search
   * @return the first index i containing the specified element, such that {@code minindex <= i <
   *     indexlimit}, or -1 if the element is not found in that section of the array
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Pure
  public static <T extends @Nullable Object> int indexOf(
      T[] a,
      @Nullable Object elt,
      @IndexFor("#1") int minindex,
      @IndexOrHigh("#1") int indexlimit) {
    if (elt == null) {
      return indexOfEq(a, elt, minindex, indexlimit);
    }
    for (int i = minindex; i < indexlimit; i++) {
      if (elt.equals(a[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the list, testing for equality using
   * the equals method. Identical to List.indexOf, but included for completeness.
   *
   * @param a a list
   * @param elt the element to search for
   * @return the first index whose element is equal to the specified element, or -1 if no such
   *     element is found in the list
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(List<? extends @PolyNull Object> a, Object elt) {
    return a.indexOf(elt);
  }

  /**
   * Searches for the first occurrence of the given element in the list, within the given
   * boundaries, testing for equality using the equals method.
   *
   * @param a a list
   * @param elt the element to search for
   * @param minindex first index at which to search
   * @param indexlimit first index at which not to search
   * @return the first index i containing the specified element, such that {@code minindex <= i <
   *     indexlimit}, or -1 if the element is not found in that section of the list
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(
      List<? extends @PolyNull Object> a,
      @Nullable Object elt,
      @IndexFor("#1") @NonNegative int minindex,
      @IndexOrHigh("#1") int indexlimit) {
    if (elt == null) {
      return indexOfEq(a, elt, minindex, indexlimit);
    }
    for (int i = minindex; i < indexlimit; i++) {
      if (elt.equals(a.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array, testing for equality using
   * == (not the equals method).
   *
   * @param a an array
   * @param elt the element to search for
   * @return the first index containing the specified element, or -1 if the element is not found in
   *     the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOfEq(@PolyNull Object[] a, @Nullable Object elt) {
    for (int i = 0; i < a.length; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array, within the given
   * boundaries, testing for equality using == (not the equals method).
   *
   * @param a an array
   * @param elt the element to search for
   * @param minindex first index at which to search
   * @param indexlimit first index at which not to search
   * @return the first index i containing the specified element, such that {@code minindex <= i <
   *     indexlimit}, or -1 if the element is not found in that section of the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOfEq(
      @PolyNull Object[] a,
      @Nullable Object elt,
      @IndexFor("#1") int minindex,
      @IndexOrHigh("#1") int indexlimit) {
    for (int i = minindex; i < indexlimit; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the list, testing for equality using
   * == (not the equals method).
   *
   * @param a a list
   * @param elt the element to search for
   * @return the first index containing the specified element, or -1 if the element is not found in
   *     the list
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOfEq(List<? extends @PolyNull Object> a, @Nullable Object elt) {
    for (int i = 0; i < a.size(); i++) {
      if (elt == a.get(i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the list, within the given
   * boundaries, testing for equality using == (not the equals method).
   *
   * @param a a list
   * @param elt the element to search for
   * @param minindex first index at which to search
   * @param indexlimit first index at which not to search
   * @return the first index i containing the specified element, such that {@code minindex <= i <
   *     indexlimit}, or -1 if the element is not found in that section of the list
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOfEq(
      List<? extends @PolyNull Object> a,
      @Nullable Object elt,
      @IndexFor("#1") @NonNegative int minindex,
      @IndexOrHigh("#1") int indexlimit) {
    for (int i = minindex; i < indexlimit; i++) {
      if (elt == a.get(i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array.
   *
   * @param a an array
   * @param elt the element to search for
   * @return the first index containing the specified element, or -1 if the element is not found in
   *     the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(int[] a, int elt) {
    for (int i = 0; i < a.length; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array.
   *
   * @param a an array
   * @param elt the element to search for
   * @return the first index containing the specified element, or -1 if the element is not found in
   *     the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(long[] a, long elt) {
    for (int i = 0; i < a.length; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array, within the given
   * boundaries.
   *
   * @param a an array
   * @param elt the element to search for
   * @param minindex first index at which to search
   * @param indexlimit first index at which not to search
   * @return the first index i containing the specified element, such that {@code minindex <=; i <
   *     indexlimit}, or -1 if the element is not found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(
      int[] a, int elt, @IndexFor("#1") int minindex, @IndexOrHigh("#1") int indexlimit) {
    for (int i = minindex; i < indexlimit; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array, within the given
   * boundaries.
   *
   * @param a an array
   * @param elt the element to search for
   * @param minindex first index at which to search
   * @param indexlimit first index at which not to search
   * @return the first index i containing the specified element, such that {@code minindex <=; i <
   *     indexlimit}, or -1 if the element is not found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(
      long[] a, long elt, @IndexFor("#1") int minindex, @IndexOrHigh("#1") int indexlimit) {
    for (int i = minindex; i < indexlimit; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array.
   *
   * @param a an array
   * @param elt the element to search for
   * @return the first index containing the specified element, or -1 if the element is not found in
   *     the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(boolean[] a, boolean elt) {
    for (int i = 0; i < a.length; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array.
   *
   * @param a an array
   * @param elt the element to search for
   * @return the first index containing the specified element, or -1 if the element is not found in
   *     the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(double[] a, double elt) {
    for (int i = 0; i < a.length; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first occurrence of the given element in the array, within the given
   * boundaries.
   *
   * @param a an array
   * @param elt the element to search for
   * @param minindex first index at which to search
   * @param indexlimit first index at which not to search
   * @return the first index i containing the specified element, such that {@code minindex <=; i <
   *     indexlimit}, or -1 if the element is not found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   */
  @Pure
  public static int indexOf(
      boolean[] a, boolean elt, @IndexFor("#1") int minindex, @IndexOrHigh("#1") int indexlimit) {
    for (int i = minindex; i < indexlimit; i++) {
      if (elt == a[i]) {
        return i;
      }
    }
    return -1;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// indexOf, for finding subarrays
  ///

  // This is analogous to Common Lisp's "search" function.

  // This implementation is very inefficient; I could use tricky Boyer-Moore
  // search techniques if I liked, but it's not worth it to me yet.

  /**
   * Searches for the first subsequence of the array that matches the given array elementwise,
   * testing for equality using the equals method.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @return the first index at which the second array starts in the first array, or -1 if no such
   *     element is found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOf(@PolyNull Object[] a, @PolyNull Object[] sub) {
    int aIndexMax = a.length - sub.length;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarray(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the array that matches the given array elementwise,
   * testing for equality using == (not the equals method).
   *
   * @param a an array
   * @param sub subsequence to search for
   * @return the first index at which the second array starts in the first array, or -1 if the
   *     element is not found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  // The signature on this method is unnecessarily strict because it
  // requires that the component types be identical.  The signature should
  // be indexOfEq(@PolyNull(1) Object[], @PolyNull(2) Object[]), but the
  // @PolyNull qualifier does not yet take an argument.
  @Pure
  public static int indexOfEq(@PolyNull Object[] a, @PolyNull Object[] sub) {
    int aIndexMax = a.length - sub.length;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarrayEq(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the list that matches the given array elementwise,
   * testing for equality using the equals method.
   *
   * @param a a list
   * @param sub subsequence to search for
   * @return the first index at which the second array starts in the first list, or -1 if no such
   *     element is found in the list
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOf(List<?> a, @PolyNull Object[] sub) {
    int aIndexMax = a.size() - sub.length;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarray(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the list that matches the given array elementwise,
   * testing for equality using == (not the equals method).
   *
   * @param a a list
   * @param sub subsequence to search for
   * @return the first index at which the second array starts in the first list, or -1 if the
   *     element is not found in the list
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOfEq(List<?> a, @PolyNull Object[] sub) {
    int aIndexMax = a.size() - sub.length;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarrayEq(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the array that matches the given list elementwise,
   * testing for equality using the equals method.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @return the first index at which the second list starts in the first array, or -1 if no such
   *     element is found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOf(@PolyNull Object[] a, List<?> sub) {
    int aIndexMax = a.length - sub.size() + 1;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarray(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the array that matches the given list elementwise,
   * testing for equality using == (not the equals method).
   *
   * @param a an array
   * @param sub subsequence to search for
   * @return the first index at which the second list starts in the first array, or -1 if the
   *     element is not found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOfEq(@PolyNull Object[] a, List<?> sub) {
    int aIndexMax = a.length - sub.size();
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarrayEq(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the list that matches the given list elementwise, testing
   * for equality using the equals method.
   *
   * @param a a list
   * @param sub subsequence to search for
   * @return the first index at which the second list starts in the first list, or -1 if no such
   *     element is found in the list
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOf(List<?> a, List<?> sub) {
    int aIndexMax = a.size() - sub.size();
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarray(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the list that matches the given list elementwise, testing
   * for equality using == (not the equals method).
   *
   * @param a a list
   * @param sub subsequence to search for
   * @return the first index at which the second list starts in the first list, or -1 if the element
   *     is not found in the list
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOfEq(List<?> a, List<?> sub) {
    int aIndexMax = a.size() - sub.size();
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarrayEq(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the array that matches the given array elementwise.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @return the first index at which the second array starts in the first array, or -1 if the
   *     element is not found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOf(int[] a, int[] sub) {
    int aIndexMax = a.length - sub.length;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarray(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the array that matches the given array elementwise.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @return the first index at which the second array starts in the first array, or -1 if the
   *     element is not found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOf(double[] a, double[] sub) {
    int aIndexMax = a.length - sub.length;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarray(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the array that matches the given array elementwise.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @return the first index at which the second array starts in the first array, or -1 if the
   *     element is not found in the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOf(long[] a, long[] sub) {
    int aIndexMax = a.length - sub.length;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarray(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Searches for the first subsequence of the array that matches the given array elementwise.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @return the first index containing the specified element, or -1 if the element is not found in
   *     the array
   * @see java.util.ArrayList#indexOf(java.lang.Object)
   * @see java.lang.String#indexOf(java.lang.String)
   */
  @Pure
  public static int indexOf(boolean[] a, boolean[] sub) {
    int aIndexMax = a.length - sub.length;
    for (int i = 0; i <= aIndexMax; i++) {
      if (isSubarray(a, sub, i)) {
        return i;
      }
    }
    return -1;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// mismatch
  ///

  // This is analogous to Common Lisp's "mismatch" function.

  // Put it off until later; for now, use the simpler subarray function,
  // which is a specialization of mismatch,

  ///////////////////////////////////////////////////////////////////////////
  /// subarray extraction
  ///

  // Note that the second argument is a length, not an end position.
  // That's to avoid confusion over whether it would be the last included
  // index or the first non-included index.

  /**
   * Returns a subarray of the given array.
   *
   * @return a subarray of the given array
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   */
  @SideEffectFree
  public static @PolyNull @PolyInterned Object[] subarray(
      @PolyNull @PolyInterned Object[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    @PolyNull @PolyInterned Object[] result = new @PolyNull @PolyInterned Object[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a sublist of the given list.
   *
   * @param <T> type of the elements
   * @param a the original list
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a sublist of the given list
   */
  @SideEffectFree
  public static <T> List<T> subarray(
      List<T> a, @IndexFor("#1") int startindex, @IndexOrHigh("#1") int length) {
    return a.subList(startindex, startindex + length);
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static @PolyNull @PolyInterned String[] subarray(
      @PolyNull @PolyInterned String[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    @PolyNull @PolyInterned String[] result = new @PolyNull @PolyInterned String[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static byte[] subarray(
      byte[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    byte[] result = new byte[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static boolean[] subarray(
      boolean[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    boolean[] result = new boolean[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static char[] subarray(
      char[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    char[] result = new char[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static double[] subarray(
      double[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    double[] result = new double[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static float[] subarray(
      float[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    float[] result = new float[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static int[] subarray(
      int[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    int[] result = new int[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static long[] subarray(
      long[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    long[] result = new long[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  /**
   * Returns a subarray of the given array.
   *
   * @param a the original array
   * @param startindex the first index to be included
   * @param length the number of elements to include (not an end index, to avoid confusion over
   *     whether it would be the last included index or the first non-included index)
   * @return a subarray of the given array
   */
  @SideEffectFree
  public static short[] subarray(
      short[] a,
      @NonNegative int startindex,
      @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int length) {
    short[] result = new short[length];
    System.arraycopy(a, startindex, result, 0, length);
    return result;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// subarray testing
  ///

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first, testing for equality using the equals method.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarray(
      @PolyNull Object[] a, @PolyNull Object[] sub, @NonNegative int aOffset) {
    if (aOffset + sub.length > a.length) {
      return false;
    }
    for (int i = 0; i < sub.length; i++) {
      if (!Objects.equals(sub[i], a[aOffset + i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first, testing for equality using == (not the equals method).
   *
   * @param a an array
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarrayEq(
      @PolyNull Object[] a, @PolyNull Object[] sub, @NonNegative int aOffset) {
    if (aOffset + sub.length > a.length) {
      return false;
    }
    for (int i = 0; i < sub.length; i++) {
      if (sub[i] != a[aOffset + i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first, testing for equality using the equals method.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarray(@PolyNull Object[] a, List<?> sub, @NonNegative int aOffset) {
    if (aOffset + sub.size() > a.length) {
      return false;
    }
    for (int i = 0; i < sub.size(); i++) {
      if (!Objects.equals(sub.get(i), a[aOffset + i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first, testing for equality using == (not the equals method).
   *
   * @param a an array
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarrayEq(@PolyNull Object[] a, List<?> sub, @NonNegative int aOffset) {
    if (aOffset + sub.size() > a.length) {
      return false;
    }
    for (int i = 0; i < sub.size(); i++) {
      if (sub.get(i) != a[aOffset + i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first, testing for equality using the equals method.
   *
   * @param a a list
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarray(List<?> a, @PolyNull Object[] sub, @NonNegative int aOffset) {
    if (aOffset + sub.length > a.size()) {
      return false;
    }
    for (int i = 0; i < sub.length; i++) {
      if (!Objects.equals(sub[i], a.get(aOffset + i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first, testing for equality using == (not the equals method).
   *
   * @param a a list
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarrayEq(List<?> a, @PolyNull Object[] sub, @NonNegative int aOffset) {
    if (aOffset + sub.length > a.size()) {
      return false;
    }
    for (int i = 0; i < sub.length; i++) {
      if (sub[i] != a.get(aOffset + i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first, testing for equality using the equals method.
   *
   * @param a a list
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarray(List<?> a, List<?> sub, @NonNegative int aOffset) {
    if (aOffset + sub.size() > a.size()) {
      return false;
    }
    for (int i = 0; i < sub.size(); i++) {
      if (!Objects.equals(sub.get(i), a.get(aOffset + i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first, testing for equality using == (not the equals method).
   *
   * @param a a list
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarrayEq(List<?> a, List<?> sub, @NonNegative int aOffset) {
    if (aOffset + sub.size() > a.size()) {
      return false;
    }
    for (int i = 0; i < sub.size(); i++) {
      if (sub.get(i) != a.get(aOffset + i)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarray(int[] a, int[] sub, @NonNegative int aOffset) {
    if (aOffset + sub.length > a.length) {
      return false;
    }
    for (int i = 0; i < sub.length; i++) {
      if (sub[i] != a[aOffset + i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarray(long[] a, long[] sub, @NonNegative int aOffset) {
    if (aOffset + sub.length > a.length) {
      return false;
    }
    for (int i = 0; i < sub.length; i++) {
      if (sub[i] != a[aOffset + i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarray(double[] a, double[] sub, @NonNegative int aOffset) {
    if (aOffset + sub.length > a.length) {
      return false;
    }
    for (int i = 0; i < sub.length; i++) {
      if (sub[i] != a[aOffset + i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines whether the second array is a subarray of the first, starting at the specified index
   * of the first.
   *
   * @param a an array
   * @param sub subsequence to search for
   * @param aOffset first index in {@code a} at which to search. Must be non-negative. The routine
   *     returns false if {@code aOffset} is too large to be a valid index for {@code a}.
   * @return true iff sub is a contiguous subarray of a
   */
  @Pure
  public static boolean isSubarray(boolean[] a, boolean[] sub, @NonNegative int aOffset) {
    if (aOffset + sub.length > a.length) {
      return false;
    }
    for (int i = 0; i < sub.length; i++) {
      if (sub[i] != a[aOffset + i]) {
        return false;
      }
    }
    return true;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Concatenation
  ///

  /**
   * Concatenates two arrays. Can be invoked varargs-style.
   *
   * <p>This differs from {@code concat} in that it always returns a new array, never an existing
   * array.
   *
   * @param <T> the type of the array elements
   * @param array1 the first array
   * @param array2 the second array
   * @return a new array containing the contents of the given arrays, in order
   */
  @SuppressWarnings({
    "unchecked",
    "index:argument" // addition for array length
  })
  public static <T> T[] concatenate(T[] array1, T... array2) {
    @SuppressWarnings("nullness") // elements are not non-null yet, but will be by return stmt
    T[] result = Arrays.copyOf(array1, array1.length + array2.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }

  // Concat used to return null if both arguments are null.  That is
  // convenient for the implementer, but not so good for clients.

  /**
   * Call this method in order to suppress compiler warnings.
   *
   * @param <T> the base type of the result
   * @param lst the list to convert to an array
   * @return the result of lst.toArray, casted to a more precise type than Object[]
   */
  @SideEffectFree
  private static <T> T[] toTArray(List<T> lst) {
    @SuppressWarnings("unchecked")
    T[] asArray = (T[]) lst.toArray();
    return asArray;
  }

  /**
   * A wrapper around a list or an array (or null). Avoids code duplication for arrays and lists, at
   * the cost of object construction and method calls.
   *
   * @param <T> the type of array or list elements
   */
  private static class ListOrArray<T extends @Nullable Object> {
    // At most one field is non-null.  If both are null, this object represents the null value.
    /** The array that this object wraps, or null. */
    T @Nullable [] theArray = null;
    /** The list that this object wraps, or null. */
    @Nullable List<T> theList = null;

    /**
     * Creates a ListOrArray that wraps an array.
     *
     * @param theArray the delegate that will be wrapped
     */
    ListOrArray(T @Nullable [] theArray) {
      this.theArray = theArray;
    }

    /**
     * Creates a ListOrArray that wraps a list.
     *
     * @param theList the delegate that will be wrapped
     */
    ListOrArray(@Nullable List<T> theList) {
      this.theList = theList;
    }

    /**
     * Returns true if this represents a null value.
     *
     * @return true if this represents a null value
     */
    @Pure
    boolean isNull() {
      return theArray == null && theList == null;
    }

    /**
     * Returns the size of the collection this represents.
     *
     * @return the size of the collection this represents
     */
    @Pure
    @NonNegative int size() {
      if (theArray != null) {
        return theArray.length;
      } else if (theList != null) {
        return theList.size();
      } else {
        throw new Error("both fields are null");
      }
    }

    /**
     * Returns true if this represents an empty collection.
     *
     * @return true if this represents an empty collection
     */
    @Pure
    boolean isEmpty() {
      if (theArray != null) {
        return theArray.length == 0;
      } else if (theList != null) {
        return theList.isEmpty();
      } else {
        throw new Error("both fields are null");
      }
    }

    /**
     * Returns an array with the same contents as this.
     *
     * @return an array with the same contents as this
     */
    @SideEffectFree
    T[] toArray() {
      if (theArray != null) {
        return theArray;
      } else if (theList != null) {
        return toTArray(theList);
      } else {
        throw new Error("both fields are null");
      }
    }

    /**
     * Copy the contents of this into the given array, starting at the given index in the array.
     *
     * @param dest the destination array
     * @param destPos the index at which to start overwriting elements of {@code dest}
     */
    @SuppressWarnings({
      "lowerbound:argument", // TODO: annotate for Index Checker
      "index:argument" // TODO: annotate for Index Checker
    })
    void copyInto(T[] dest, int destPos) {
      if (theArray != null) {
        System.arraycopy(theArray, 0, dest, destPos, theArray.length);
      } else if (theList != null) {
        for (int i = 0; i < theList.size(); i++) {
          @SuppressWarnings({
            "lowerbound:assignment",
            "index:assignment"
          }) // index checker has no list support
          @IndexFor("dest") int index = i + destPos;
          dest[index] = theList.get(i);
        }
      } else {
        throw new Error("both fields are null");
      }
    }

    /**
     * Returns the least upper bound of the classes of the elements of this.
     *
     * @return the least upper bound of the classes of the elements of this
     */
    @Nullable Class<? extends @Nullable Object> leastUpperBound() {
      if (theArray != null) {
        return ReflectionPlume.leastUpperBound(theArray);
      } else if (theList != null) {
        return ReflectionPlume.leastUpperBound(theList);
      } else {
        throw new Error("both fields are null");
      }
    }

    @Override
    public String toString(@GuardSatisfied ListOrArray<T> this) {
      if (theArray != null) {
        return Arrays.toString(theArray);
      } else if (theList != null) {
        return theList.toString();
      } else {
        return "null";
      }
    }

    /**
     * Returns a verbose representation of this, for debugging.
     *
     * @return a verbose representation of this, for debugging
     */
    public String toStringDebug() {
      String theArrayString;
      if (theArray == null) {
        theArrayString = "null";
      } else {
        theArrayString = Arrays.toString(theArray) + "[" + System.identityHashCode(theArray) + "]";
      }
      String theListString;
      if (theList == null) {
        theListString = "null";
      } else {
        theListString = theList.toString() + "[" + System.identityHashCode(theList) + "]";
      }

      return "ListOrArray(theArray=" + theArrayString + ", theList=" + theListString + ")";
    }
  }

  /**
   * Returns an array that contains all the elements of both arguments, in order. Returns an
   * existing array if possible (when one argument is null or empty).
   *
   * @param <T> the type of the sequence elements
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  public static <T extends @Nullable Object> T[] concat(T @Nullable [] a, T @Nullable [] b) {
    return concat(new ListOrArray<T>(a), new ListOrArray<T>(b));
  }

  /**
   * Returns an array that contains all the elements of both arguments, in order. Returns the array
   * argument if the list argument is null or empty.
   *
   * @param <T> the type of the sequence elements
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  public static <T extends @Nullable Object> T[] concat(T @Nullable [] a, @Nullable List<T> b) {
    return concat(new ListOrArray<T>(a), new ListOrArray<T>(b));
  }

  /**
   * Returns an array that contains all the elements of both arguments, in order. Returns the array
   * argument if the list argument is null or empty.
   *
   * @param <T> the type of the sequence elements
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  public static <T extends @Nullable Object> T[] concat(@Nullable List<T> a, T @Nullable [] b) {
    return concat(new ListOrArray<T>(a), new ListOrArray<T>(b));
  }

  /**
   * Returns an array that contains all the elements of both arguments, in order.
   *
   * @param <T> the type of the sequence elements
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  public static <T extends @Nullable Object> T[] concat(@Nullable List<T> a, @Nullable List<T> b) {
    return concat(new ListOrArray<T>(a), new ListOrArray<T>(b));
  }

  /**
   * Returns an array that contains all the elements of both arguments, in order.
   *
   * @param <T> the type of the sequence elements
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  private static <T extends @Nullable Object> T[] concat(ListOrArray<T> a, ListOrArray<T> b) {
    if (a.isNull() && b.isNull()) {
      @SuppressWarnings("unchecked")
      T[] result = (T[]) new Object[0];
      return result;
    } else if (a.isNull()) {
      return b.toArray();
    } else if (b.isNull()) {
      return a.toArray();
    }
    // Both a and b are non-null.
    else if (a.isEmpty()) {
      return b.toArray();
    } else if (b.isEmpty()) {
      return a.toArray();
    }
    // Both a and b are non-empty.
    else {
      int size = a.size() + b.size();
      // TODO: Fix.  We want an array of type exactly T.  This computes an estimate to T,
      // from the elements in the arrays.  It might be a subtype of T, though, which is incorrect.
      @SuppressWarnings("unchecked")
      Class<T> resultType =
          ReflectionPlume.leastUpperBound(
              (Class<T>) a.leastUpperBound(), (Class<T>) b.leastUpperBound());

      if (resultType == null) {
        throw new Error("All values are null, don't know how to create result array");
      }

      @SuppressWarnings("unchecked")
      T[] result = (T[]) Array.newInstance(resultType, size);

      a.copyInto(result, 0);
      b.copyInto(result, a.size());
      return result;
    }
  }

  // Note: PolyAll is not quite right.  Need to review.
  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null or empty, in which case it returns the other array.
   *
   * @param a the first array to concatenate
   * @param b the second array to concatenate
   * @return an array that concatenates the arguments
   */
  public static @PolyNull @PolyInterned String[] concat(
      @PolyNull @PolyInterned String @Nullable [] a,
      @PolyNull @PolyInterned String @Nullable [] b) {
    if (a == null || a.length == 0) {
      if (b == null) {
        return new String[0];
      } else {
        return b;
      }
    } else {
      if (b == null || b.length == 0) {
        return a;
      } else {
        @PolyNull @PolyInterned String[] result = new String[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null, in which case it returns the other array.
   *
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  @SideEffectFree
  public static byte[] concat(byte @Nullable [] a, byte @Nullable [] b) {
    if (a == null) {
      if (b == null) {
        return new byte[0];
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        byte[] result = new byte[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null, in which case it returns the other array.
   *
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  @SideEffectFree
  public static boolean[] concat(boolean @Nullable [] a, boolean @Nullable [] b) {
    if (a == null) {
      if (b == null) {
        return new boolean[0];
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        boolean[] result = new boolean[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null, in which case it returns the other array.
   *
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  @SideEffectFree
  public static char[] concat(char @Nullable [] a, char @Nullable [] b) {
    if (a == null) {
      if (b == null) {
        return new char[0];
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        char[] result = new char[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null, in which case it returns the other array.
   *
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  @SideEffectFree
  public static double[] concat(double @Nullable [] a, double @Nullable [] b) {
    if (a == null) {
      if (b == null) {
        return new double[0];
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        double[] result = new double[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null, in which case it returns the other array.
   *
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  @SideEffectFree
  public static float[] concat(float @Nullable [] a, float @Nullable [] b) {
    if (a == null) {
      if (b == null) {
        return new float[0];
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        float[] result = new float[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null, in which case it returns the other array.
   *
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  @SideEffectFree
  public static int[] concat(int @Nullable [] a, int @Nullable [] b) {
    if (a == null) {
      if (b == null) {
        return new int[0];
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        int[] result = new int[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null, in which case it returns the other array.
   *
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  @SideEffectFree
  public static long[] concat(long @Nullable [] a, long @Nullable [] b) {
    if (a == null) {
      if (b == null) {
        return new long[0];
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        long[] result = new long[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  /**
   * Returns an array that contains all the elements of both argument arrays, in order. Returns a
   * new array unless one argument is null, in which case it returns the other array.
   *
   * @param a the first sequence to concatenate
   * @param b the second sequence to concatenate
   * @return an array that concatenates the arguments
   */
  @SideEffectFree
  public static short[] concat(short @Nullable [] a, short @Nullable [] b) {
    if (a == null) {
      if (b == null) {
        return new short[0];
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        short[] result = new short[a.length + b.length];

        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Printing
  ///

  /**
   * Returns a string representation of the contents of the specified array. The argument must be an
   * array or null. This just dispatches one of the 9 overloaded versions of {@code
   * java.util.Arrays.toString()}.
   *
   * @param a an array
   * @return a string representation of the array
   * @throws IllegalArgumentException if a is not an array
   */
  @SideEffectFree
  public static String toString(@Nullable Object a) {
    if (a == null) {
      return "null";
    } else if (a instanceof boolean[]) {
      return Arrays.toString((boolean[]) a);
    } else if (a instanceof byte[]) {
      return Arrays.toString((byte[]) a);
    } else if (a instanceof char[]) {
      return Arrays.toString((char[]) a);
    } else if (a instanceof double[]) {
      return Arrays.toString((double[]) a);
    } else if (a instanceof float[]) {
      return Arrays.toString((float[]) a);
    } else if (a instanceof int[]) {
      return Arrays.toString((int[]) a);
    } else if (a instanceof long[]) {
      return Arrays.toString((long[]) a);
    } else if (a instanceof short[]) {
      return Arrays.toString((short[]) a);
    } else if (a instanceof Object[]) {
      return Arrays.toString((Object[]) a);
    } else if (a instanceof List<?>) {
      // Handles lists, but this is not a documented feature
      return a.toString();
    } else {
      throw new IllegalArgumentException(
          "Argument is not an array, but has class " + a.getClass().getName());
    }
  }

  /**
   * Returns the length of the argument array.
   *
   * @param a an array
   * @return the length of the array
   * @throws IllegalArgumentException if obj is null or is not an array
   */
  @Pure
  public static @NonNegative int length(@Nullable Object a) throws IllegalArgumentException {
    if (a == null) {
      throw new IllegalArgumentException("Argument is null");
    } else if (a instanceof boolean[]) {
      return ((boolean[]) a).length;
    } else if (a instanceof byte[]) {
      return ((byte[]) a).length;
    } else if (a instanceof char[]) {
      return ((char[]) a).length;
    } else if (a instanceof double[]) {
      return ((double[]) a).length;
    } else if (a instanceof float[]) {
      return ((float[]) a).length;
    } else if (a instanceof int[]) {
      return ((int[]) a).length;
    } else if (a instanceof long[]) {
      return ((long[]) a).length;
    } else if (a instanceof short[]) {
      return ((short[]) a).length;
    } else if (a instanceof Object[]) {
      return ((Object[]) a).length;
    } else {
      throw new IllegalArgumentException(
          "Argument is not an array, but has class " + a.getClass().getName());
    }
  }

  /**
   * Returns a string representation of the array. The representation is patterned after that of
   * java.util.ArrayList.
   *
   * @param a an array
   * @return a string representation of the array
   * @see java.util.ArrayList#toString
   */
  @SideEffectFree
  public static String toString(@PolyNull Object @Nullable [] a) {
    return toString(a, false);
  }

  /**
   * Returns a string representation of the array. The representation is patterned after that of
   * java.util.ArrayList. Furthermore, each element is quoted like a Java String.
   *
   * @param a an array
   * @return a string representation of the array, with the elements quoted
   * @see java.util.ArrayList#toString
   */
  @SideEffectFree
  public static String toStringQuoted(@PolyNull Object @Nullable [] a) {
    return toString(a, true);
  }

  /**
   * Returns a string representation of the array. The representation is patterned after that of
   * java.util.ArrayList. Furthermore, if quoted is true, then each element is quoted like a Java
   * String.
   *
   * @param a an array
   * @param quoted whether to quote the array elements
   * @return a string representation of the array
   * @see java.util.ArrayList#toString
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (string creation)
  @SideEffectFree
  public static String toString(@PolyNull Object @Nullable [] a, boolean quoted) {
    if (a == null) {
      return "null";
    }
    StringJoiner sj = new StringJoiner(", ", "[", "]");
    for (int i = 0; i < a.length; i++) {
      Object elt = a[i];
      if (quoted && elt instanceof String) {
        sj.add("\"" + StringsPlume.escapeJava((String) elt) + "\"");
      } else {
        sj.add(Objects.toString(elt));
      }
    }
    return sj.toString();
  }

  /**
   * Returns a string representation of the collection. The representation is patterned after that
   * of java.util.ArrayList.
   *
   * @param a a collection
   * @return a string representation of the collection
   * @see java.util.ArrayList#toString
   */
  @SideEffectFree
  public static String toString(@Nullable Collection<?> a) {
    return toString(a, false);
  }

  /**
   * Returns a string representation of the collection. The representation is patterned after that
   * of java.util.ArrayList.
   *
   * @param a a collection
   * @return a string representation of the collection, with the elements quoted
   * @see java.util.ArrayList#toString
   */
  @SideEffectFree
  public static String toStringQuoted(@Nullable Collection<?> a) {
    return toString(a, true);
  }

  /**
   * Returns a string representation of the collection. The representation is patterned after that
   * of java.util.ArrayList. The representation permits quoting (or not) of strings.
   *
   * @param a a collection
   * @param quoted whether to quote the collection elements that are Java strings
   * @return a string representation of the list
   * @see java.util.ArrayList#toString
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (string creation)
  @SideEffectFree
  public static String toString(@Nullable Collection<?> a, boolean quoted) {
    if (a == null) {
      return "null";
    }
    StringJoiner sj = new StringJoiner(", ", "[", "]");
    for (Object elt : a) {
      if (quoted && elt instanceof String) {
        sj.add("\"" + StringsPlume.escapeJava((String) elt) + "\"");
      } else {
        sj.add(Objects.toString(elt));
      }
    }
    return sj.toString();
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Sortedness
  ///

  /**
   * Returns whether the array is sorted.
   *
   * @param a an array
   * @return true iff the array is sorted
   */
  @Pure
  public static boolean sorted(int[] a) {
    for (int i = 0; i < a.length - 1; i++) {
      if (a[i + 1] < a[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether the array is sorted.
   *
   * @param a an array
   * @return true iff the array is sorted
   */
  @Pure
  public static boolean sorted(long[] a) {
    for (int i = 0; i < a.length - 1; i++) {
      if (a[i + 1] < a[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether the array is sorted in descending order.
   *
   * @param a an array
   * @return true iff the array is sorted in descending order
   */
  @Pure
  public static boolean isSortedDescending(int[] a) {
    for (int i = 0; i < a.length - 1; i++) {
      if (a[i + 1] > a[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns whether the array is sorted in descending order.
   *
   * @param a an array
   * @return true iff the array is sorted in descending order
   */
  @Pure
  public static boolean isSortedDescending(long[] a) {
    for (int i = 0; i < a.length - 1; i++) {
      if (a[i + 1] > a[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(boolean[] a) {
    Set<Boolean> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(boolean[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(byte[] a) {
    Set<Byte> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(byte[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(char[] a) {
    Set<Character> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(char[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(float[] a) {
    Set<Float> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(float[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(short[] a) {
    Set<Short> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(short[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(int[] a) {
    Set<Integer> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(int[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements. Equality checking uses {@link Double#equals}.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(double[] a) {
    Set<Double> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }
  /**
   * Returns true iff a does not contain duplicate elements. Equality checking uses {@link
   * Double#equals}.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(double[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(long[] a) {
    Set<Long> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(long[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(String[] a) {
    HashSet<String> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(String[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff a contains duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a contains duplicate elements
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean hasDuplicates(Object[] a) {
    HashSet<Object> hs = new HashSet<>();
    for (int i = 0; i < a.length; i++) {
      if (!hs.add(a[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true iff a does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param a an array
   * @return true iff a does not contain duplicate elements
   */
  @Pure
  public static boolean noDuplicates(Object[] a) {
    return !hasDuplicates(a);
  }

  /**
   * Returns true iff the list does not contain duplicate elements.
   *
   * <p>The implementation uses O(n) time and O(n) space.
   *
   * @param <T> the type of the elements
   * @param a a list
   * @return true iff a does not contain duplicate elements
   * @deprecated use {@link CollectionsPlume#noDuplicates}
   */
  @Deprecated // 2021-04-09
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static <T> boolean noDuplicates(List<T> a) {
    if (a instanceof RandomAccess) {
      HashSet<T> hs = new HashSet<>();
      for (int i = 0; i < a.size(); i++) {
        T elt = a.get(i);
        if (!hs.add(elt)) {
          return false;
        }
      }
      return true;
    } else {
      HashSet<T> hs = new HashSet<>();
      for (T elt : a) {
        if (!hs.add(elt)) {
          return false;
        }
      }
      return true;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Arrays as partial functions of int->int
  ///

  /**
   * Returns true if the array is a permutation of [0..a.length).
   *
   * @param a an array, representing a function
   * @return true iff all elements of a are in [0..a.length) and a contains no duplicates.
   */
  @SuppressWarnings("allcheckers:purity") // side effect to local state (array)
  @Pure
  public static boolean fnIsPermutation(int[] a) {
    // In the common case we expect to succeed, so use as few loops as possible
    boolean[] see = new boolean[a.length];
    for (int i = 0; i < a.length; i++) {
      int n = a[i];
      if (n < 0 || n >= a.length || see[n]) {
        return false;
      }
      see[n] = true;
    }
    return true;
  }

  /**
   * Returns true iff the array does not contain -1.
   *
   * @param a an array, representing a function
   * @return true iff no element of a maps to -1
   */
  @Pure
  public static boolean fnIsTotal(int[] a) {
    return indexOf(a, -1) == -1; // not found
  }

  /**
   * Returns an array [0..length).
   *
   * @param length the length of the result
   * @return fresh array that is the identity function of the given length
   */
  public static int[] fnIdentity(@NonNegative int length) {
    int[] result = new int[length];
    for (int i = 0; i < length; i++) {
      result[i] = i;
    }
    return result;
  }

  /**
   * Requires that fnIsPermutation(a) holds.
   *
   * @param a the input permutation
   * @return fresh array which is the inverse of the given permutation
   * @see #fnIsPermutation(int[])
   */
  @SideEffectFree
  public static int[] fnInversePermutation(int[] a) {
    return fnInverse(a, a.length);
  }

  /**
   * Returns the inverse of the given function, which is represented as an array.
   *
   * @param a an array representing a function from [0..a.length) to [0..arange); each element of a
   *     is between 0 (inclusive) and arange (exclusive)
   * @param arange length of the argument's range and the result's domain
   * @return function from [0..arange) to [0..a.length) that is the inverse of a
   * @throws IllegalArgumentException if a value of a is outside of arange
   * @exception UnsupportedOperationException when the function is not invertible
   */
  @SuppressWarnings({
    "allcheckers:purity",
    "lock:method.guarantee.violated"
  }) // side effect to local state
  @SideEffectFree
  public static int[] fnInverse(int[] a, @NonNegative int arange) {
    int[] result = new int[arange];
    Arrays.fill(result, -1);
    for (int i = 0; i < a.length; i++) {
      int ai = a[i];
      if (ai < -1 || ai >= arange) {
        throw new IllegalArgumentException(String.format("Bad range value: a[%d]=%d", i, ai));
      }
      // ai is either -1 or a valid index
      if (ai >= 0) {
        if (result[ai] != -1) {
          throw new UnsupportedOperationException(
              String.format("Not invertible; a[%d]=%d and a[%d]=%d", result[ai], ai, i, ai));
        }
        result[ai] = i;
      }
    }
    return result;
  }

  /**
   * Returns the composition of the given two functions, all of which are represented as arrays.
   *
   * @param a function from [0..a.length) to [0..b.length)
   * @param b function from [0..b.length) to range R
   * @return function from [0..a.length) to range R that is the composition of a and b
   */
  @SuppressWarnings("allcheckers:purity") // side effect to local state
  @SideEffectFree
  public static int @SameLen("#1") [] fnCompose(@IndexFor("#2") int[] a, int[] b) {
    int[] result = new int[a.length];
    for (int i = 0; i < a.length; i++) {
      result[i] = b[a[i]];
    }
    return result;
  }

  /**
   * Returns the composition of the given two (possibly partial) functions, all of which are
   * represented as arrays.
   *
   * @param a function from [0..a.length) to [-1..b.length)
   * @param b function from [0..b.length) to range R
   * @return function from [0..a.length) to {range R} union {-1}, that is the composition of a and
   *     b.
   */
  @SuppressWarnings("allcheckers:purity") // side effect to local state
  @SideEffectFree
  public static int @SameLen("#1") [] partialFnCompose(@IndexOrLow("#2") int[] a, int[] b) {
    int[] result = new int[a.length];
    for (int i = 0; i < a.length; i++) {
      int inner = a[i];
      if (inner == -1) {
        result[i] = -1;
      } else {
        result[i] = b[inner];
      }
    }
    return result;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Set operations, such as subset, unions, and intersections
  ///

  // This implementation is O(n^2) when the smaller really is a subset, but
  // might be quicker when it is not.  Sorting both sets has (minimum
  // and maximum) running time of Theta(n log n).
  /**
   * Returns whether smaller is a subset of bigger.
   *
   * <p>The implementation is to use collections because we want to take advantage of HashSet's
   * constant time membership tests.
   *
   * @param smaller first set to test
   * @param bigger second set to test
   * @return true iff smaller is a subset of bigger
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean isSubset(long[] smaller, long[] bigger) {
    Set<Long> setBigger = new HashSet<>();

    for (int i = 0; i < bigger.length; i++) {
      setBigger.add(bigger[i]);
    }

    for (int i = 0; i < smaller.length; i++) {
      if (!setBigger.contains(smaller[i])) {
        return false;
      }
    }

    return true;
  }

  // This implementation is O(n^2) when the smaller really is a subset, but
  // might be quicker when it is not.  Sorting both sets has (minimum
  // and maximum) running time of Theta(n log n).
  /**
   * Returns whether smaller is a subset of bigger.
   *
   * <p>The implementation is to use collections because we want to take advantage of HashSet's
   * constant time membership tests.
   *
   * @param smaller first set to test
   * @param bigger second set to test
   * @return true iff smaller is a subset of bigger
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean isSubset(double[] smaller, double[] bigger) {
    Set<Double> setBigger = new HashSet<>();

    for (int i = 0; i < bigger.length; i++) {
      setBigger.add(bigger[i]);
    }

    for (int i = 0; i < smaller.length; i++) {
      if (!setBigger.contains(smaller[i])) {
        return false;
      }
    }

    return true;
  }

  // This implementation is O(n^2) when the smaller really is a subset, but
  // might be quicker when it is not.  Sorting both sets has (minimum
  // and maximum) running time of Theta(n log n).
  /**
   * Returns whether smaller is a subset of bigger.
   *
   * <p>The implementation is to use collections because we want to take advantage of HashSet's
   * constant time membership tests.
   *
   * @param smaller first set to test
   * @param bigger second set to test
   * @return true iff smaller is a subset of bigger
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (HashSet)
  @Pure
  public static boolean isSubset(String[] smaller, String[] bigger) {
    Set<String> setBigger = new HashSet<>();

    for (int i = 0; i < bigger.length; i++) {
      setBigger.add(bigger[i]);
    }

    for (int i = 0; i < smaller.length; i++) {
      if (!setBigger.contains(smaller[i])) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns true if the arrays contain the same contents, treated as a set (order and duplicates do
   * not matter).
   *
   * @param <T> the type of the array contents
   * @param arr1 an array
   * @param arr2 an array
   * @return true if the arrays contain the same contents
   */
  public static <T> boolean sameContents(T[] arr1, T[] arr2) {
    List<T> list1 = Arrays.asList(arr1);
    List<T> list2 = Arrays.asList(arr2);
    return list1.containsAll(list2) && list2.containsAll(list1);

    // // Alterate implementation, which is more efficient if the arrays are large:
    // Note: this sorts the arrays as a side effect.
    // Arrays.sort(arr1);
    // Arrays.sort(arr2);
    // return Arrays.equals(arr1, arr2);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Array comparators
  ///

  /**
   * Compare two arrays lexically (element-by-element). If all shared elements are the same, but the
   * lengths differ, then the shorter array is considered less.
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical numbers but are not equal according to
   * {@code equals()} (which tests reference equality).
   */
  public static final class IntArrayComparatorLexical implements Comparator<int[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    @Override
    public int compare(int[] a1, int[] a2) {
      if (a1 == a2) {
        return 0;
      }
      int len = Math.min(a1.length, a2.length);
      for (int i = 0; i < len; i++) {
        if (a1[i] != a2[i]) {
          return ((a1[i] > a2[i]) ? 1 : -1);
        }
      }
      return a1.length - a2.length;
    }
  }

  /**
   * Compare two arrays lexically (element-by-element). If all shared elements are the same, but the
   * lengths differ, then the shorter array is considered less.
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical numbers but are not equal according to
   * {@code equals()} (which tests reference equality).
   */
  public static final class LongArrayComparatorLexical implements Comparator<long[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    @Override
    public int compare(long[] a1, long[] a2) {
      if (a1 == a2) {
        return 0;
      }
      int len = Math.min(a1.length, a2.length);
      for (int i = 0; i < len; i++) {
        if (a1[i] != a2[i]) {
          return ((a1[i] > a2[i]) ? 1 : -1);
        }
      }
      return a1.length - a2.length;
    }
  }

  /**
   * Compare two arrays lexically (element-by-element). If all shared elements are the same, but the
   * lengths differ, then the shorter array is considered less.
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical numbers but are not equal according to
   * {@code equals()} (which tests reference equality).
   */
  public static final class DoubleArrayComparatorLexical
      implements Comparator<double[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    @Override
    public int compare(double[] a1, double[] a2) {
      if (a1 == a2) {
        return 0;
      }
      int len = Math.min(a1.length, a2.length);
      for (int i = 0; i < len; i++) {
        int result = Double.compare(a1[i], a2[i]);
        if (result != 0) {
          return (result);
        }
      }
      return a1.length - a2.length;
    }
  }

  /**
   * Compare two arrays lexically (element-by-element). If all shared elements are the same, but the
   * lengths differ, then the shorter array is considered less.
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical numbers but are not equal according to
   * {@code equals()}.
   */
  public static final class StringArrayComparatorLexical
      implements Comparator<String[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    // The signature on this method is unnecessarily strict because it
    // requires that the component types be identical.  The signature should
    // be compare(@PolyNull(1) String[], @PolyNull(2) String[]), but the
    // @PolyNull qualifier does not yet take an argument.
    @Override
    public int compare(@PolyNull String[] a1, @PolyNull String[] a2) {
      if (a1 == a2) {
        return 0;
      }
      int len = Math.min(a1.length, a2.length);
      for (int i = 0; i < len; i++) {
        int tmp = 0;
        if ((a1[i] == null) && (a2[i] == null)) {
          tmp = 0;
        } else if (a1[i] == null) {
          tmp = -1;
        } else if (a2[i] == null) {
          tmp = 1;
        } else {
          tmp = a1[i].compareTo(a2[i]);
        }
        if (tmp != 0) {
          return (tmp);
        }
      }
      return a1.length - a2.length;
    }
  }

  /**
   * Compare two arrays lexically (element-by-element). If all shared elements are the same, but the
   * lengths differ, then the shorter array is considered less.
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical elements but are not equal according
   * to {@code equals()} (which tests reference equality).
   */
  public static final class ComparableArrayComparatorLexical<T extends Comparable<T>>
      implements Comparator<T[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    // The signature on this method is unnecessarily strict because it
    // requires that the component types be identical.  The signature should
    // be compare(@PolyNull(1) T[], @PolyNull(2) T[]), but the
    // @PolyNull qualifier does not yet take an argument.
    @Override
    public int compare(@PolyNull T[] a1, @PolyNull T[] a2) {
      if (a1 == a2) {
        return 0;
      }
      int len = Math.min(a1.length, a2.length);
      for (int i = 0; i < len; i++) {
        T elt1 = a1[i];
        T elt2 = a2[i];
        // Make null compare smaller than anything else
        if ((elt1 == null) && (elt2 == null)) {
          continue;
        }
        if (elt1 == null) {
          return -1;
        }
        if (elt2 == null) {
          return 1;
        }
        int tmp = elt1.compareTo(elt2);
        if (tmp != 0) {
          return tmp;
        }
        // Check the assumption that the two elements are equal.
        assert elt1.equals(elt2);
      }
      return a1.length - a2.length;
    }
  }

  /**
   * Compare two arrays lexically (element-by-element). If all shared elements are the same, but the
   * lengths differ, then the shorter array is considered less.
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain equal objects but are not equal according to
   * {@code equals()}.
   *
   * <p>Note: if toString returns a nondeterministic value, such as one that depends on the result
   * of {@code hashCode()}, then this comparator may yield different orderings from run to run of a
   * program.
   */
  public static final class ObjectArrayComparatorLexical
      implements Comparator<Object[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    // The signature on this method is unnecessarily strict because it
    // requires that the component types be identical.  The signature should
    // be compare(@PolyNull(1) Object[], @PolyNull(2) Object[]), but the
    // @PolyNull qualifier does not yet take an argument.
    @Override
    public int compare(@PolyNull Object[] a1, @PolyNull Object[] a2) {
      if (a1 == a2) {
        return 0;
      }
      int len = Math.min(a1.length, a2.length);
      for (int i = 0; i < len; i++) {
        int tmp = objectComparator.compare(a1[i], a2[i]);
        if (tmp != 0) {
          return tmp;
        }
      }
      return a1.length - a2.length;
    }
  }

  /**
   * Compare two arrays first by length (a shorter array is considered less), and if of equal length
   * compare lexically (element-by-element).
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical numbers but are not equal according to
   * {@code equals()}.
   */
  public static final class IntArrayComparatorLengthFirst
      implements Comparator<int[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays by length, then lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    @Override
    public int compare(int[] a1, int[] a2) {
      if (a1 == a2) {
        return 0;
      }
      if (a1.length != a2.length) {
        return a1.length - a2.length;
      }
      for (int i = 0; i < a1.length; i++) {
        if (a1[i] != a2[i]) {
          return ((a1[i] > a2[i]) ? 1 : -1);
        }
      }
      return 0;
    }
  }

  /**
   * Compare two arrays first by length (a shorter array is considered less), and if of equal length
   * compare lexically (element-by-element).
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical numbers but are not equal according to
   * {@code equals()}.
   */
  public static final class LongArrayComparatorLengthFirst
      implements Comparator<long[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays by length, then lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    @Override
    public int compare(long[] a1, long[] a2) {
      if (a1 == a2) {
        return 0;
      }
      if (a1.length != a2.length) {
        return a1.length - a2.length;
      }
      for (int i = 0; i < a1.length; i++) {
        if (a1[i] != a2[i]) {
          return ((a1[i] > a2[i]) ? 1 : -1);
        }
      }
      return 0;
    }
  }

  /**
   * Compare two arrays first by length (a shorter array is considered less), and if of equal length
   * compare lexically (element-by-element).
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical objects but are not equal according to
   * {@code equals()}.
   */
  public static final class ComparableArrayComparatorLengthFirst<T extends Comparable<T>>
      implements Comparator<T[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays by length, then lexically (element-by-element).
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    // The signature on this method is unnecessarily strict because it
    // requires that the component types be identical.  The signature should
    // be compare(@PolyNull(1) T[], @PolyNull(2) T[]), but the
    // @PolyNull qualifier does not yet take an argument.
    @Override
    public int compare(@PolyNull T[] a1, @PolyNull T[] a2) {
      if (a1 == a2) {
        return 0;
      }
      if (a1.length != a2.length) {
        return a1.length - a2.length;
      }
      for (int i = 0; i < a1.length; i++) {
        T elt1 = a1[i];
        T elt2 = a2[i];
        // Make null compare smaller than anything else
        if ((elt1 == null) && (elt2 == null)) {
          continue;
        }
        if (elt1 == null) {
          return -1;
        }
        if (elt2 == null) {
          return 1;
        }
        int tmp = elt1.compareTo(elt2);
        if (tmp != 0) {
          return tmp;
        }
        // Check the assumption that the two elements are equal.
        assert elt1.equals(elt2);
      }
      return 0;
    }
  }

  /** Sorts arbitrary objects; used to determine equal. */
  private static final StringsPlume.ObjectComparator objectComparator =
      new StringsPlume.ObjectComparator();

  /**
   * Compare two arrays first by length (a shorter array is considered less), and if of equal length
   * compare lexically (element-by-element).
   *
   * <p>Note: this comparator imposes orderings that are inconsistent with {@link Object#equals}.
   * That is, it may return 0 if the arrays contain identical objects but are not equal according to
   * {@code equals()}.
   *
   * <p>Note: if toString returns a nondeterministic value, such as one that depends on the result
   * of {@code hashCode()}, then this comparator may yield different orderings from run to run of a
   * program.
   */
  public static final class ObjectArrayComparatorLengthFirst
      implements Comparator<Object[]>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /**
     * Compare two arrays by length, then lexically (element-by-element). Null elements are
     * considered smaller than non-null elements.
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return a negative integer, zero, or a positive integer, depending on whether the first
     *     argument is less than, equal to, or greater than the second argument
     */
    @Pure
    // The signature on this method is unnecessarily strict because it
    // requires that the component types be identical.  The signature should
    // be compare(@PolyNull(1) Object[], @PolyNull(2) Object[]), but the
    // @PolyNull qualifier does not yet take an argument.
    @Override
    public int compare(@PolyNull Object[] a1, @PolyNull Object[] a2) {
      if (a1 == a2) {
        return 0;
      }
      if (a1.length != a2.length) {
        return a1.length - a2.length;
      }
      for (int i = 0; i < a1.length; i++) {
        int tmp = objectComparator.compare(a1[i], a2[i]);
        if (tmp != 0) {
          return tmp;
        }
      }
      return 0;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  /// nullness
  ///

  /**
   * Returns true if a contains null.
   *
   * @param a an array
   * @return true iff some element of a is null (false if a is zero-sized)
   */
  @Pure
  public static boolean anyNull(@PolyNull Object[] a) {
    if (a.length == 0) {
      return false;
    }
    // The cast ensures that the right version of IndexOfEq gets called.
    return indexOfEq(a, (@Nullable Object) null) >= 0;
  }

  /**
   * Returns true if all elements of a are null.
   *
   * @param a an array
   * @return true iff all elements of a are null (unspecified result if a is zero-sized)
   */
  @Pure
  public static boolean allNull(@PolyNull Object[] a) {
    for (int i = 0; i < a.length; i++) {
      if (!(a[i] == null)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if a contains null.
   *
   * @param a an array
   * @return true iff some element of a is null (false if a is zero-sized)
   */
  @Pure
  public static boolean anyNull(List<? extends @Nullable Object> a) {
    if (a.size() == 0) {
      return false;
    }
    // The cast ensures that the right version of IndexOfEq gets called.
    return indexOfEq(a, (@Nullable Object) null) >= 0;
  }

  /**
   * Returns true if all elements of a are null.
   *
   * @param a an array
   * @return true iff all elements of a are null (unspecified result if a is zero-sized)
   */
  @Pure
  public static boolean allNull(List<?> a) {
    for (int i = 0; i < a.size(); i++) {
      if (!(a.get(i) == null)) {
        return false;
      }
    }
    return true;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Partitioning
  ///

  /**
   * Partition a set of non-null elements into exactly k subsets. A partitioning is of type {@code
   * List<List<T>>}, where the union of the inner lists is {@code elts}. This method returns a list
   * of such partitionings.
   *
   * @param <T> type of items to be partitioned
   * @param elts items to be partitioned
   * @param k number of subsets into which to partition {@code elts}
   * @return a list of partitionings, where each contains exactly k subsets
   */
  public static <T extends @NonNull Object> List<Partitioning<T>> partitionInto(
      Collection<T> elts, @NonNegative int k) {
    return partitionInto(new ArrayDeque<T>(elts), k);
  }

  /**
   * Partition a set of elements into exactly k subsets. A partitioning is of type {@code
   * List<List<T>>}, where the union of the inner lists is {@code elts}. This method returns a list
   * of such partitionings.
   *
   * @param <T> type of items to be partitioned
   * @param elts items to be partitioned
   * @param k number of subsets into which to partition {@code elts}
   * @return a list of partitionings, where each contains exactly k subsets
   */
  public static <T extends @NonNull Object> List<Partitioning<T>> partitionInto(
      Queue<T> elts, @NonNegative int k) {
    if (elts.size() < k) {
      throw new IllegalArgumentException();
    }
    return partitionIntoHelper(elts, Arrays.asList(new Partitioning<T>()), k, 0);
  }

  /**
   * Returns a set of partitionings, each of size numEmptyParts + numNonemptyParts. A helper method
   * for {@link #partitionInto}.
   *
   * @param <T> type of items to be partitioned
   * @param elts the elements that remain to be added to the partitionings
   * @param resultSoFar a list of partitionings, each of which has numNonemptyParts parts
   * @param numEmptyParts the number of partitions in the partitioning that are empty so far
   * @param numNonemptyParts the number of partitions in the partitioning that have at least one
   *     member so far
   * @return a list of partitionings, where each contains exactly k subsets
   */
  // "p.addToPart(i, ...)" is OK: i is < numNonemptyParts
  //  and p.size() = numNonemptyParts + numEmptyParts, both of which are non-negative.
  public static <T extends @NonNull Object> List<Partitioning<T>> partitionIntoHelper(
      Queue<T> elts,
      List</*@ LengthIs("#3")*/ Partitioning<T>> resultSoFar,
      @NonNegative int numEmptyParts,
      @NonNegative int numNonemptyParts) {

    if (numEmptyParts > elts.size()) {
      throw new IllegalArgumentException(numEmptyParts + " > " + elts.size());
    }

    if (elts.isEmpty()) {
      return resultSoFar;
    }

    Queue<T> eltsRemaining = new ArrayDeque<T>(elts);
    T elt = eltsRemaining.remove();

    List<Partitioning<T>> result = new ArrayList<Partitioning<T>>();

    // Put elt in an existing part in the partitioning.
    if (elts.size() > numEmptyParts) {
      List<Partitioning<T>> resultSoFar_augmented = new ArrayList<Partitioning<T>>();
      for (int i = 0; i < numNonemptyParts; i++) {
        for (Partitioning<T> p : resultSoFar) {
          resultSoFar_augmented.add(p.addToPart(i, elt));
        }
      }
      result.addAll(
          partitionIntoHelper(
              eltsRemaining, resultSoFar_augmented, numEmptyParts, numNonemptyParts));
    }

    // Put elt in a newly-created part in the partitioning.
    if (numEmptyParts > 0) {
      List<Partitioning<T>> resultSoFar_augmented = new ArrayList<Partitioning<T>>();
      for (Partitioning<T> p : resultSoFar) {
        resultSoFar_augmented.add(p.addToPart(numNonemptyParts, elt));
      }
      result.addAll(
          partitionIntoHelper(
              eltsRemaining, resultSoFar_augmented, numEmptyParts - 1, numNonemptyParts + 1));
    }

    return result;
  }

  /** A partitioning is a set of sets. It adds a few methods to {@code ArrayList<ArrayList<T>>}. */
  static class Partitioning<T extends @NonNull Object> extends ArrayList<ArrayList<T>> {

    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20170418;

    /** Empty constructor. */
    Partitioning() {}

    /**
     * Copy constructor.
     *
     * @param other the Partitioning to make a copy of
     */
    Partitioning(Partitioning<T> other) {
      super(other);
    }

    /**
     * The set that has been partitioned. That is, all the elements that have been added to this.
     * Equivalently, the union of all the partitions.
     *
     * @return all the elements in any part of the Partitioning
     */
    ArrayList<T> partitionedSet() {
      ArrayList<T> result = new ArrayList<>();
      for (List<T> part : this) {
        result.addAll(part);
      }
      return result;
    }

    /**
     * True if this is a partitioning for {@code elts}.
     *
     * @param elts the elements that might be partitioned by this
     * @return true if this is a partitioning for {@code elts}
     */
    boolean isPartitioningFor(List<T> elts) {
      // Inefficient O(n^2) implementation.  We can do O(n log n) if desired.
      ArrayList<T> ps = partitionedSet();
      return ps.size() == elts.size() && ps.containsAll(elts);
    }

    /**
     * Returns a new partitioning just like this one, but with elt added to the ith part.
     *
     * @param i the index of an existing part, or the current size (to create a new part)
     * @param elt the element to add
     * @return a new partitioning just like this one, but with elt added to the ith part
     */
    Partitioning<T> addToPart(@NonNegative int i, T elt) {
      Partitioning<T> result = new Partitioning<>(this);
      if (size() == i) {
        ArrayList<T> newPart = newArrayList(elt);
        result.add(newPart);
      } else {
        ArrayList<T> newPart = new ArrayList<>(result.get(i));
        newPart.add(elt);
        result.set(i, newPart);
      }
      return result;
    }
  }

  /**
   * Returns a singleton ArrayList containing the given element.
   *
   * @param <T> the element type of the list
   * @param elt the element to put in the ArrayList
   * @return a singleton ArrayList containing {@code elt}
   */
  private static <T> ArrayList<T> newArrayList(T elt) {
    ArrayList<T> result = new ArrayList<>(1);
    result.add(elt);
    return result;
  }

  // /**
  //  * Returns a singleton ArrayDeque containing the given element.
  //  *
  //  * @param <T> the element type of the list
  //  * @param elt the element to put in the ArrayDeque
  //  * @return a singleton ArrayDeque containing {@code elt}
  //  */
  // private static <T extends @NonNull Object> ArrayDeque<T> newArrayDeque(T elt) {
  //   ArrayDeque<T> result = new ArrayDeque<>();
  //   result.add(elt);
  //   return result;
  // }
}
