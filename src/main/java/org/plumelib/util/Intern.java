package org.plumelib.util;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;

/*>>>
import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.interning.qual.*;
import org.checkerframework.checker.lock.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.dataflow.qual.*;
import org.checkerframework.common.value.qual.*;
*/

/**
 * Utilities for interning objects. Interning is also known as canonicalization or hash-consing: it
 * returns a single representative object that {@link Object#equals} the object, and the client
 * discards the argument and uses the result instead. Since only one object exists for every set of
 * equal objects, space usage is reduced. Time may also be reduced, since it is possible to use
 * {@code ==} instead of {@code .equals()} for comparisons.
 *
 * <p>Java builds in interning for Strings, but not for other objects. The methods in this class
 * extend interning to all Java objects.
 */
public final class Intern {

  /** This class is a collection of methods; it does not represent anything. */
  private Intern() {
    throw new Error("do not instantiate");
  }

  /** Whether assertions are enabled. */
  private static boolean assertsEnabled = false;

  static {
    assert assertsEnabled = true; // Intentional side-effect!!!
    // Now assertsEnabled is set to the correct value
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Strings
  ///

  /**
   * Replace each element of the array by its interned version. Side-effects the array, but also
   * returns it.
   *
   * @param a the array whose elements to intern in place
   * @return an interned version of a
   * @see String#intern
   */
  @SuppressWarnings("interning") // side-effects the array in place (dangerous, but convenient)
  public static /*@Interned*/ String /*@PolyValue*/ /*@SameLen("#1")*/[] internStrings(
      String /*@PolyValue*/ [] a) {
    for (int i = 0; i < a.length; i++) {
      if (a[i] != null) {
        a[i] = a[i].intern();
      }
    }
    return a;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Testing interning
  ///

  /**
   * Return true if the argument is interned (is canonical among all objects equal to itself).
   *
   * @param value the value to test for interning
   * @return true iff value is interned
   */
  @SuppressWarnings({"interning", "lock"}) // interning implementation
  /*@Pure*/
  public static boolean isInterned(/*@Nullable*/ Object value) {
    if (value == null) {
      // nothing to do
      return true;
    } else if (value instanceof String) {
      return (value == ((String) value).intern());
    } else if (value instanceof String[]) {
      return (value == intern((String[]) value));
    } else if (value instanceof Integer) {
      return (value == intern((Integer) value));
    } else if (value instanceof Long) {
      return (value == intern((Long) value));
    } else if (value instanceof int[]) {
      return (value == intern((int[]) value));
    } else if (value instanceof long[]) {
      return (value == intern((long[]) value));
    } else if (value instanceof Double) {
      return (value == intern((Double) value));
    } else if (value instanceof double[]) {
      return (value == intern((double[]) value));
    } else if (value instanceof Object[]) {
      return (value == intern((Object[]) value));
    } else {
      // Nothing to do, because we don't intern other types.
      // System.out.println("What type? " + value.getClass().getName());
      return true;
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Interning objects
  ///

  /**
   * Hasher object which hashes and compares Integers. This is the obvious implementation that uses
   * intValue() for the hashCode.
   *
   * @see Hasher
   */
  private static final class IntegerHasher implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      return a1.equals(a2);
    }

    @Override
    public int hashCode(Object o) {
      Integer i = (Integer) o;
      return i.intValue();
    }
  }

  /**
   * Hasher object which hashes and compares Longs. This is the obvious implementation that uses
   * intValue() for the hashCode.
   *
   * @see Hasher
   */
  private static final class LongHasher implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      return a1.equals(a2);
    }

    @Override
    public int hashCode(Object o) {
      Long i = (Long) o;
      return i.intValue();
    }
  }

  /**
   * Hasher object which hashes and compares int[] objects according to their contents.
   *
   * @see Hasher
   * @see Arrays#equals(int[], int[])
   */
  private static final class IntArrayHasher implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      return Arrays.equals((int[]) a1, (int[]) a2);
    }

    @Override
    public int hashCode(Object o) {
      return Arrays.hashCode((int[]) o);
    }
  }

  /**
   * Hasher object which hashes and compares long[] objects according to their contents.
   *
   * @see Hasher
   * @see Arrays#equals (long[], long[])
   */
  private static final class LongArrayHasher implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      return Arrays.equals((long[]) a1, (long[]) a2);
    }

    @Override
    public int hashCode(Object o) {
      return Arrays.hashCode((long[]) o);
    }
  }

  private static final int FACTOR = 23;
  // private static final double DOUBLE_FACTOR = 65537;
  private static final double DOUBLE_FACTOR = 263;

  /**
   * Hasher object which hashes and compares Doubles.
   *
   * @see Hasher
   */
  private static final class DoubleHasher implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      return a1.equals(a2);
    }

    @Override
    public int hashCode(Object o) {
      Double d = (Double) o;
      return d.hashCode();
    }
  }

  /**
   * Hasher object which hashes and compares double[] objects according to their contents.
   *
   * @see Hasher
   * @see Arrays#equals(Object[],Object[])
   */
  private static final class DoubleArrayHasher implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      // "Arrays.equals" considers +0.0 != -0.0.
      // Also, it gives inconsistent results (on different JVMs/classpaths?).
      // return Arrays.equals((double[])a1, (double[])a2);
      double[] da1 = (double[]) a1;
      double[] da2 = (double[]) a2;
      if (da1.length != da2.length) {
        return false;
      }
      for (int i = 0; i < da1.length; i++) {
        if (!((da1[i] == da2[i]) || (Double.isNaN(da1[i]) && Double.isNaN(da2[i])))) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode(Object o) {
      double[] a = (double[]) o;
      // Not Arrays.hashCode(a), for consistency with equals method
      // immediately above.
      double running = 0;
      for (int i = 0; i < a.length; i++) {
        double elt = (Double.isNaN(a[i]) ? 0.0 : a[i]);
        running = running * FACTOR + elt * DOUBLE_FACTOR;
      }
      // Could add "... % Integer.MAX_VALUE" here; is that good to do?
      long result = Math.round(running);
      return (int) (result % Integer.MAX_VALUE);
    }
  }

  /**
   * Hasher object which hashes and compares String[] objects according to their contents.
   *
   * @see Hasher
   * @see Arrays.equals
   */
  private static final class StringArrayHasher implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      return Arrays.equals((String[]) a1, (String[]) a2);
    }

    @Override
    public int hashCode(Object o) {
      return Arrays.hashCode((String[]) o);
    }
  }

  /**
   * Hasher object which hashes and compares Object[] objects according to their contents.
   *
   * @see Hasher
   * @see Arrays#equals(Object[], Object[])
   */
  private static final class ObjectArrayHasher implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      return Arrays.equals((/*@Nullable*/ Object[]) a1, (/*@Nullable*/ Object[]) a2);
    }

    @Override
    public int hashCode(Object o) {
      return Arrays.hashCode((Object[]) o);
    }
  }

  // Each of these maps has:
  //   key = an interned object
  //   value = a WeakReference for the object itself.
  // They can be looked up using a non-interned value; equality tests know
  // nothing of the interning types.

  private static WeakHasherMap</*@Interned*/ Integer, WeakReference</*@Interned*/ Integer>>
      internedIntegers;
  private static WeakHasherMap</*@Interned*/ Long, WeakReference</*@Interned*/ Long>> internedLongs;
  private static WeakHasherMap<int /*@Interned*/ [], WeakReference<int /*@Interned*/ []>>
      internedIntArrays;
  private static WeakHasherMap<long /*@Interned*/ [], WeakReference<long /*@Interned*/ []>>
      internedLongArrays;
  private static WeakHasherMap</*@Interned*/ Double, WeakReference</*@Interned*/ Double>>
      internedDoubles;
  private static /*@Interned*/ Double internedDoubleNaN;
  private static /*@Interned*/ Double internedDoubleZero;
  private static WeakHasherMap<double /*@Interned*/ [], WeakReference<double /*@Interned*/ []>>
      internedDoubleArrays;
  private static WeakHasherMap<
          /*@Nullable*/ /*@Interned*/ String /*@Interned*/ [],
          WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []>>
      internedStringArrays;
  private static WeakHasherMap<
          /*@Nullable*/ /*@Interned*/ Object /*@Interned*/ [],
          WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []>>
      internedObjectArrays;
  private static WeakHasherMap<
          SequenceAndIndices<int /*@Interned*/ []>, WeakReference<int /*@Interned*/ []>>
      internedIntSequenceAndIndices;
  private static WeakHasherMap<
          SequenceAndIndices<long /*@Interned*/ []>, WeakReference<long /*@Interned*/ []>>
      internedLongSequenceAndIndices;
  private static WeakHasherMap<
          SequenceAndIndices<double /*@Interned*/ []>, WeakReference<double /*@Interned*/ []>>
      internedDoubleSequenceAndIndices;
  private static WeakHasherMap<
          SequenceAndIndices</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []>,
          WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []>>
      internedObjectSequenceAndIndices;
  private static WeakHasherMap<
          SequenceAndIndices</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []>,
          WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []>>
      internedStringSequenceAndIndices;

  static {
    internedIntegers =
        new WeakHasherMap</*@Interned*/ Integer, WeakReference</*@Interned*/ Integer>>(
            new IntegerHasher());
    internedLongs =
        new WeakHasherMap</*@Interned*/ Long, WeakReference</*@Interned*/ Long>>(new LongHasher());
    internedIntArrays =
        new WeakHasherMap<int /*@Interned*/ [], WeakReference<int /*@Interned*/ []>>(
            new IntArrayHasher());
    internedLongArrays =
        new WeakHasherMap<long /*@Interned*/ [], WeakReference<long /*@Interned*/ []>>(
            new LongArrayHasher());
    internedDoubles =
        new WeakHasherMap</*@Interned*/ Double, WeakReference</*@Interned*/ Double>>(
            new DoubleHasher());
    internedDoubleNaN = Double.NaN;
    internedDoubleZero = 0.0;
    internedDoubleArrays =
        new WeakHasherMap<double /*@Interned*/ [], WeakReference<double /*@Interned*/ []>>(
            new DoubleArrayHasher());
    internedStringArrays =
        new WeakHasherMap<
            /*@Nullable*/ /*@Interned*/ String /*@Interned*/ [],
            WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []>>(
            new StringArrayHasher());
    internedObjectArrays =
        new WeakHasherMap<
            /*@Nullable*/ /*@Interned*/ Object /*@Interned*/ [],
            WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []>>(
            new ObjectArrayHasher());
    internedIntSequenceAndIndices =
        new WeakHasherMap<
            SequenceAndIndices<int /*@Interned*/ []>, WeakReference<int /*@Interned*/ []>>(
            new SequenceAndIndicesHasher<int /*@Interned*/ []>());
    internedLongSequenceAndIndices =
        new WeakHasherMap<
            SequenceAndIndices<long /*@Interned*/ []>, WeakReference<long /*@Interned*/ []>>(
            new SequenceAndIndicesHasher<long /*@Interned*/ []>());
    internedDoubleSequenceAndIndices =
        new WeakHasherMap<
            SequenceAndIndices<double /*@Interned*/ []>, WeakReference<double /*@Interned*/ []>>(
            new SequenceAndIndicesHasher<double /*@Interned*/ []>());
    internedObjectSequenceAndIndices =
        new WeakHasherMap<
            SequenceAndIndices</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []>,
            WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []>>(
            new SequenceAndIndicesHasher</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []>());
    internedStringSequenceAndIndices =
        new WeakHasherMap<
            SequenceAndIndices</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []>,
            WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []>>(
            new SequenceAndIndicesHasher</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []>());
  }

  // For testing only
  public static int numIntegers() {
    return internedIntegers.size();
  }

  public static int numLongs() {
    return internedLongs.size();
  }

  public static int numIntArrays() {
    return internedIntArrays.size();
  }

  public static int numLongArrays() {
    return internedLongArrays.size();
  }

  public static int numDoubles() {
    return internedDoubles.size();
  }

  public static int numDoubleArrays() {
    return internedDoubleArrays.size();
  }

  public static int numStringArrays() {
    return internedStringArrays.size();
  }

  public static int numObjectArrays() {
    return internedObjectArrays.size();
  }

  public static Iterator</*@Interned*/ Integer> integers() {
    return internedIntegers.keySet().iterator();
  }

  public static Iterator</*@Interned*/ Long> longs() {
    return internedLongs.keySet().iterator();
  }

  public static Iterator<int /*@Interned*/ []> intArrays() {
    return internedIntArrays.keySet().iterator();
  }

  public static Iterator<long /*@Interned*/ []> longArrays() {
    return internedLongArrays.keySet().iterator();
  }

  public static Iterator</*@Interned*/ Double> doubles() {
    return internedDoubles.keySet().iterator();
  }

  public static Iterator<double /*@Interned*/ []> doubleArrays() {
    return internedDoubleArrays.keySet().iterator();
  }

  public static Iterator</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []> stringArrays() {
    return internedStringArrays.keySet().iterator();
  }

  public static Iterator</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []> objectArrays() {
    return internedObjectArrays.keySet().iterator();
  }

  /**
   * Interns a String. Delegates to the builtin String.intern() method, but handles {@code null}.
   *
   * @param a the string to intern; may be null
   * @return an interned version of the argument, or null if the argument was null
   */
  /*@Pure*/
  @SuppressWarnings("lock")
  public static /*@Interned*/ /*@PolyNull*/ /*@PolyValue*/ /*@SameLen("#1")*/ String intern(
      /*@PolyNull*/ /*@PolyValue*/ String a) {
    // Checker Framework cannot typecheck:  return (a == null) ? null : a.intern();
    if (a == null) {
      return null;
    }
    return a.intern();
  }

  /**
   * Interns a long. A no-op. Provided for completeness.
   *
   * @param l the long to intern
   * @return an interned version of the argument
   */
  /*@Pure*/
  public static long intern(long l) {
    return l;
  }

  /**
   * Interns a double A no-op. Provided for completeness.
   *
   * @param d the double to intern
   * @return an interned version of the argument
   */
  /*@Pure*/
  public static double intern(double d) {
    return d;
  }

  /**
   * Intern (canonicalize) an Integer. Return a canonical representation for the Integer.
   *
   * @param a an Integer to canonicalize
   * @return a canonical representation for the Integer
   */
  // TODO: JLS 5.1.7 requires that the boxing conversion interns integer
  // values between -128 and 127 (and Intern.valueOf is intended to promise
  // the same).  This does not currently take advantage of that.
  @SuppressWarnings({"interning", "purity", "lock"}) // interning implementation
  /*@Pure*/
  public static /*@Interned*/ Integer intern(Integer a) {
    WeakReference</*@Interned*/ Integer> lookup = internedIntegers.get(a);
    Integer result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      @SuppressWarnings("cast") // cast is redundant (except in JSR 308)
      /*@Interned*/ Integer result = (/*@Interned*/ Integer) a;
      internedIntegers.put(result, new WeakReference</*@Interned*/ Integer>(result));
      return result;
    }
  }

  // Not sure whether this convenience method is really worth it.
  /**
   * Returns an interned Integer with value i.
   *
   * @param i the value to intern
   * @return an interned Integer with value i
   */
  public static /*@Interned*/ Integer internedInteger(int i) {
    return intern(Integer.valueOf(i));
  }

  // Not sure whether this convenience method is really worth it.
  /**
   * Returns an interned Integer with value parsed from the string.
   *
   * @param s the string to parse
   * @return an interned Integer parsed from s
   */
  public static /*@Interned*/ Integer internedInteger(String s) {
    return intern(Integer.decode(s));
  }

  /**
   * Intern (canonicalize) a Long. Return a canonical representation for the Long.
   *
   * @param a the value to intern
   * @return a canonical representation for the Long
   */
  // TODO: JLS 5.1.7 requires that the boxing conversion interns integer
  // values between -128 and 127 (and Long.valueOf is intended to promise
  // the same).  This could take advantage of that.
  @SuppressWarnings({"interning", "purity", "lock"})
  /*@Pure*/
  public static /*@Interned*/ Long intern(Long a) {
    WeakReference</*@Interned*/ Long> lookup = internedLongs.get(a);
    Long result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      @SuppressWarnings("cast") // cast is redundant (except in JSR 308)
      /*@Interned*/ Long result = (/*@Interned*/ Long) a;
      internedLongs.put(result, new WeakReference</*@Interned*/ Long>(result));
      return result;
    }
  }

  // Not sure whether this convenience method is really worth it.
  /**
   * Returns an interned Long with value i.
   *
   * @param i the value to intern
   * @return an interned Integer with value i
   */
  public static /*@Interned*/ Long internedLong(long i) {
    return intern(Long.valueOf(i));
  }

  // Not sure whether this convenience method is really worth it.
  /**
   * Returns an interned Long with value parsed from the string.
   *
   * @param s the string to parse
   * @return an interned Long parsed from s
   */
  public static /*@Interned*/ Long internedLong(String s) {
    return intern(Long.decode(s));
  }

  // I might prefer to have the intern methods first check using a straight
  // eq hashing, which would be more efficient if the array is already
  // interned.  (How frequent do I expect that to be, and how much would
  // that really improve performance even in that case?)

  /**
   * Intern (canonicalize) an int[]. Return a canonical representation for the int[] array. Arrays
   * are compared according to their elements.
   *
   * @param a the array to canonicalize
   * @return a canonical representation for the int[] array
   */
  @SuppressWarnings({"interning", "purity", "lock"})
  /*@Pure*/
  public static int /*@Interned*/ /*@PolyValue*/ /*@SameLen("#1")*/[] intern(
      int /*@PolyValue*/ [] a) {
    // Throwable stack = new Throwable("debug traceback");
    // stack.fillInStackTrace();
    // stack.printStackTrace();

    WeakReference<int /*@Interned*/ []> lookup = internedIntArrays.get(a);
    @SuppressWarnings({
      "index", // for this map, get() can be annotated as @SameLen("#1")
      "value" // for this map, get() can be annotated as @PolyAll (except not interning); also see https://github.com/kelloggm/checker-framework/issues/177
    })
    int /*@PolyValue*/ /*@SameLen("a")*/[] result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      @SuppressWarnings("cast") // cast is redundant (except in JSR 308)
      /*@Interned*/ int[] result = (int /*@Interned*/ /*@PolyValue*/ []) a;
      internedIntArrays.put(result, new WeakReference<int /*@Interned*/ []>(result));
      return result;
    }
  }

  /**
   * Intern (canonicalize) a long[]. Return a canonical representation for the long[] array. Arrays
   * are compared according to their elements.
   *
   * @param a the array to canonicalize
   * @return a canonical representation for the long[] array
   */
  @SuppressWarnings({"interning", "purity", "lock"})
  /*@Pure*/
  public static long /*@Interned*/ /*@PolyValue*/ /*@SameLen("#1")*/[] intern(
      long /*@PolyValue*/ [] a) {
    // System.out.printf("intern %s %s long[] %s%n", a.getClass(),
    //                   a, Arrays.toString (a));
    WeakReference<long /*@Interned*/ []> lookup = internedLongArrays.get(a);
    @SuppressWarnings({
      "index", // for this map, get() can be annotated as @SameLen("#1")
      "value" // for this map, get() can be annotated as @PolyAll (except not interning); also see https://github.com/kelloggm/checker-framework/issues/177
    })
    long /*@PolyValue*/ /*@SameLen("a")*/[] result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      @SuppressWarnings("cast") // cast is redundant (except in JSR 308)
      /*@Interned*/ long[] result = (long /*@Interned*/ /*@PolyValue*/ []) a;
      internedLongArrays.put(result, new WeakReference<long /*@Interned*/ []>(result));
      return result;
    }
  }

  /**
   * Intern (canonicalize) a Double. Return a canonical representation for the Double.
   *
   * @param a the Double to canonicalize
   * @return a canonical representation for the Double
   */
  // TODO: JLS 5.1.7 requires that the boxing conversion interns integer
  // values between -128 and 127 (and Double.valueOf is intended to promise
  // the same).  This could take advantage of that.
  @SuppressWarnings({"interning", "purity", "lock"})
  /*@Pure*/
  public static /*@Interned*/ Double intern(Double a) {
    // Double.NaN == Double.Nan  always evaluates to false.
    if (a.isNaN()) {
      return internedDoubleNaN;
    }
    // Double.+0 == Double.-0,  but they compare true via equals()
    if (a.doubleValue() == 0) { // catches both positive and negative zero
      return internedDoubleZero;
    }
    WeakReference</*@Interned*/ Double> lookup = internedDoubles.get(a);
    Double result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      @SuppressWarnings("cast") // cast is redundant (except in JSR 308)
      /*@Interned*/ Double result = (/*@Interned*/ Double) a;
      internedDoubles.put(result, new WeakReference</*@Interned*/ Double>(result));
      return result;
    }
  }

  // Not sure whether this convenience method is really worth it.
  /**
   * Returns an interned Double with value i.
   *
   * @param d the value to intern
   * @return an interned Double with value d
   */
  public static /*@Interned*/ Double internedDouble(double d) {
    return intern(Double.valueOf(d));
  }

  // Not sure whether this convenience method is really worth it.
  /**
   * Returns an interned Double with value parsed from the string.
   *
   * @param s the string to parse
   * @return an interned Double parsed from s
   */
  public static /*@Interned*/ Double internedDouble(String s) {
    return internedDouble(Double.parseDouble(s));
  }

  // I might prefer to have the intern methods first check using a straight
  // eq hashing, which would be more efficient if the array is already
  // interned.  (How frequent do I expect that to be, and how much would
  // that really improve performance even in that case?)

  /**
   * Intern (canonicalize) a double[]. Return a canonical representation for the double[] array.
   * Arrays are compared according to their elements.
   *
   * @param a the array to canonicalize
   * @return a canonical representation for the double[] array
   */
  @SuppressWarnings({"interning", "purity", "lock"})
  /*@Pure*/
  public static double /*@Interned*/ /*@PolyValue*/ /*@SameLen("#1")*/[] intern(
      double /*@PolyValue*/ [] a) {
    WeakReference<double /*@Interned*/ []> lookup = internedDoubleArrays.get(a);
    @SuppressWarnings({
      "index", // for this map, get() can be annotated as @SameLen("#1")
      "value" // for this map, get() can be annotated as @PolyAll (except not interning); also see https://github.com/kelloggm/checker-framework/issues/177
    })
    double /*@PolyValue*/ /*@SameLen("a")*/[] result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      @SuppressWarnings("cast") // cast is redundant (except in JSR 308)
      /*@Interned*/ double[] result = (double /*@Interned*/ /*@PolyValue*/ []) a;
      internedDoubleArrays.put(result, new WeakReference<double /*@Interned*/ []>(result));
      return result;
    }
  }

  /**
   * Intern (canonicalize) an String[]. Return a canonical representation for the String[] array.
   * Arrays are compared according to their elements' equals() methods.
   *
   * @param a the array to canonicalize. Its elements should already be interned.
   * @return a canonical representation for the String[] array
   */
  @SuppressWarnings({
    "interning", // interns its argument
    "purity",
    "lock",
    "cast"
  }) // cast is redundant (except in JSR 308)
  /*@Pure*/
  public static /*@PolyNull*/ /*@Interned*/ String /*@Interned*/ /*@PolyValue*/ /*@SameLen("#1")*/[]
      intern(/*@PolyNull*/ /*@Interned*/ String /*@PolyValue*/ [] a) {

    // Make sure each element is already interned
    if (assertsEnabled) {
      for (int k = 0; k < a.length; k++) {
        if (!(a[k] == Intern.intern(a[k]))) {
          throw new IllegalArgumentException();
        }
      }
    }

    WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []> lookup =
        internedStringArrays.get(a);
    /*@Nullable*/ /*@Interned*/ String /*@Interned*/ [] result = (lookup != null) ? lookup.get() : null;
    if (result == null) {
      result = (/*@Nullable*/ /*@Interned*/ String /*@Interned*/ []) a;
      internedStringArrays.put(
          result, new WeakReference</*@Nullable*/ /*@Interned*/ String /*@Interned*/ []>(result));
    }
    @SuppressWarnings({
      "nullness", // for this map, get() can be annotated as @PolyAll (except not interning); also see https://github.com/kelloggm/checker-framework/issues/177
      "index", // for this map, get() can be annotated as @SameLen("#1")
      "value" // for this map, get() can be annotated as @PolyAll (except not interning); also see https://github.com/kelloggm/checker-framework/issues/177
    })
    /*@PolyNull*/ /*@Interned*/ String /*@Interned*/ /*@PolyValue*/ /*@SameLen("a")*/[] polyresult = result;
    return polyresult;
  }

  /**
   * Intern (canonicalize) an Object[]. Return a canonical representation for the Object[] array.
   * Arrays are compared according to their elements. The elements should themselves already be
   * interned; they are compared using their equals() methods.
   *
   * @param a the array to canonicalize
   * @return a canonical representation for the Object[] array
   */
  @SuppressWarnings({
    "interning", // interns its argument
    "purity",
    "lock",
    "cast"
  }) // cast is redundant (except in JSR 308)
  /*@Pure*/
  public static /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ /*@PolyValue*/ /*@SameLen("#1")*/[]
      intern(/*@PolyNull*/ /*@Interned*/ /*@PolyValue*/ Object[] a) {
    @SuppressWarnings(
        "nullness") // Polynull:  value = parameter a, so same type & nullness as for parameter a
    WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []> lookup =
        internedObjectArrays.get(a);
    /*@Nullable*/ /*@Interned*/ Object /*@Interned*/ [] result = (lookup != null) ? lookup.get() : null;
    if (result == null) {
      result = (/*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []) a;
      internedObjectArrays.put(
          result, new WeakReference</*@Nullable*/ /*@Interned*/ Object /*@Interned*/ []>(result));
    }
    @SuppressWarnings({
      "nullness", // for this map, get() can be annotated as @PolyAll (except not interning); also see https://github.com/kelloggm/checker-framework/issues/177
      "index", // for this map, get() can be annotated as @SameLen("#1")
      "value" // for this map, get() can be annotated as @PolyAll (except not interning); also see https://github.com/kelloggm/checker-framework/issues/177
    }) // PolyNull/PolyValue:  value = parameter a, so same type & nullness as for parameter a
    /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ /*@PolyValue*/ /*@SameLen("a")*/[] polyresult = result;
    return polyresult;
  }

  /**
   * Convenince method to intern an Object when we don't know its runtime type. Its runtime type
   * must be one of the types for which we have an intern() method, else an exception is thrown. If
   * the argument is an array, its elements should themselves be interned.
   *
   * @param a an Object to canonicalize
   * @return a canonical version of a
   */
  @SuppressWarnings("purity") // defensive coding: throw exception when argument is invalid
  /*@Pure*/
  public static /*@Interned*/ /*@PolyNull*/ Object intern(/*@PolyNull*/ Object a) {
    if (a == null) {
      return null;
    } else if (a instanceof String) {
      return intern((String) a);
    } else if (a instanceof String[]) {
      @SuppressWarnings("interning")
      /*@Interned*/ String[] asArray = (/*@Interned*/ String[]) a;
      return intern(asArray);
    } else if (a instanceof Integer) {
      return intern((Integer) a);
    } else if (a instanceof Long) {
      return intern((Long) a);
    } else if (a instanceof int[]) {
      return intern((int[]) a);
    } else if (a instanceof long[]) {
      return intern((long[]) a);
    } else if (a instanceof Double) {
      return intern((Double) a);
    } else if (a instanceof double[]) {
      return intern((double[]) a);
    } else if (a instanceof Object[]) {
      @SuppressWarnings("interning")
      /*@Interned*/ Object[] asArray = (/*@Interned*/ Object[]) a;
      return intern(asArray);
    } else {
      throw new IllegalArgumentException(
          "Arguments of type " + a.getClass() + " cannot be interned");
    }
  }

  /**
   * Return an interned subsequence of seq from start (inclusive) to end (exclusive). The argument
   * seq should already be interned.
   *
   * <p>The result is the same as computing the subsequence and then interning it, but this method
   * is more efficient: if the subsequence is already interned, it avoids computing the subsequence.
   *
   * <p>For example, since derived variables in Daikon compute the subsequence many times, this
   * shortcut saves quite a bit of computation. It saves even more when there may be many derived
   * variables that are non-canonical, since they are guaranteed to be ==.
   *
   * @param seq the interned sequence whose subsequence should be computed and interned
   * @param start the index of the start of the subsequence to compute and intern
   * @param end the index of the end of the subsequence to compute and intern
   * @return a subsequence of seq from start to end that is interned
   */
  public static int /*@Interned*/ [] internSubsequence(
      int /*@Interned*/ [] seq,
      /*@IndexFor("#1")*/ /*@LessThan("#3")*/ int start,
      /*@NonNegative*/ /*@LTLengthOf(value="#1", offset="#2 - 1")*/ int end) {
    if (assertsEnabled && !Intern.isInterned(seq)) {
      throw new IllegalArgumentException();
    }
    SequenceAndIndices<int /*@Interned*/ []> sai =
        new SequenceAndIndices<int /*@Interned*/ []>(seq, start, end);
    WeakReference<int /*@Interned*/ []> lookup = internedIntSequenceAndIndices.get(sai);
    int[] result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      int[] subseqUninterned = ArraysPlume.subarray(seq, start, end - start);
      int /*@Interned*/ [] subseq = Intern.intern(subseqUninterned);
      internedIntSequenceAndIndices.put(sai, new WeakReference<int /*@Interned*/ []>(subseq));
      return subseq;
    }
  }

  /**
   * @param seq the interned sequence whose subsequence should be computed and interned
   * @param start the index of the start of the subsequence to compute and intern
   * @param end the index of the end of the subsequence to compute and intern
   * @return a subsequence of seq from start to end that is interned
   * @see #internSubsequence(int[], int, int)
   */
  @SuppressWarnings({"purity", "lock"}) // interning logic
  /*@Pure*/
  public static long /*@Interned*/ [] internSubsequence(
      long /*@Interned*/ [] seq,
      /*@IndexFor("#1")*/ /*@LessThan("#3")*/ int start,
      /*@NonNegative*/ /*@LTLengthOf(value = "#1", offset = "#2 - 1")*/ int end) {
    if (assertsEnabled && !Intern.isInterned(seq)) {
      throw new IllegalArgumentException();
    }
    SequenceAndIndices<long /*@Interned*/ []> sai =
        new SequenceAndIndices<long /*@Interned*/ []>(seq, start, end);
    WeakReference<long /*@Interned*/ []> lookup = internedLongSequenceAndIndices.get(sai);
    long[] result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      long[] subseq_uninterned = ArraysPlume.subarray(seq, start, end - start);
      long /*@Interned*/ [] subseq = Intern.intern(subseq_uninterned);
      internedLongSequenceAndIndices.put(sai, new WeakReference<long /*@Interned*/ []>(subseq));
      return subseq;
    }
  }

  /**
   * @param seq the interned sequence whose subsequence should be computed and interned
   * @param start the index of the start of the subsequence to compute and intern
   * @param end the index of the end of the subsequence to compute and intern
   * @return a subsequence of seq from start to end that is interned
   * @see #internSubsequence(int[], int, int)
   */
  @SuppressWarnings({"purity", "lock"}) // interning logic
  /*@Pure*/
  public static double /*@Interned*/ [] internSubsequence(
      double /*@Interned*/ [] seq,
      /*@IndexFor("#1")*/ /*@LessThan("#3")*/ int start,
      /*@NonNegative*/ /*@LTLengthOf(value="#1", offset="#2 - 1")*/ int end) {
    if (assertsEnabled && !Intern.isInterned(seq)) {
      throw new IllegalArgumentException();
    }
    SequenceAndIndices<double /*@Interned*/ []> sai =
        new SequenceAndIndices<double /*@Interned*/ []>(seq, start, end);
    WeakReference<double /*@Interned*/ []> lookup = internedDoubleSequenceAndIndices.get(sai);
    double[] result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      double[] subseq_uninterned = ArraysPlume.subarray(seq, start, end - start);
      double /*@Interned*/ [] subseq = Intern.intern(subseq_uninterned);
      internedDoubleSequenceAndIndices.put(sai, new WeakReference<double /*@Interned*/ []>(subseq));
      return subseq;
    }
  }

  /**
   * @param seq the interned sequence whose subsequence should be computed and interned
   * @param start the index of the start of the subsequence to compute and intern
   * @param end the index of the end of the subsequence to compute and intern
   * @return a subsequence of seq from start to end that is interned
   * @see #internSubsequence(int[], int, int)
   */
  @SuppressWarnings({"purity", "lock"}) // interning logic
  /*@Pure*/
  public static /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ [] internSubsequence(
      /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ [] seq,
      /*@IndexFor("#1")*/ /*@LessThan("#3")*/ int start,
      /*@NonNegative*/ /*@LTLengthOf(value="#1", offset="#2 - 1")*/ int end) {
    if (assertsEnabled && !Intern.isInterned(seq)) {
      throw new IllegalArgumentException();
    }
    SequenceAndIndices</*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ []> sai =
        new SequenceAndIndices</*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ []>(seq, start, end);
    @SuppressWarnings("nullness") // same nullness as key
    WeakReference</*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ []> lookup =
        internedObjectSequenceAndIndices.get(sai);
    /*@PolyNull*/ /*@Interned*/ Object[] result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      /*@PolyNull*/ /*@Interned*/ Object[] subseq_uninterned = ArraysPlume.subarray(seq, start, end - start);
      /*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ [] subseq = Intern.intern(subseq_uninterned);
      @SuppressWarnings("nullness") // safe because map does no side effects
      Object
          ignore = // assignment just so there is a place to hang the @SuppressWarnings annotation
          internedObjectSequenceAndIndices.put(
                  sai,
                  new WeakReference</*@PolyNull*/ /*@Interned*/ Object /*@Interned*/ []>(subseq));
      return subseq;
    }
  }

  /**
   * @param seq the interned sequence whose subsequence should be computed and interned
   * @param start the index of the start of the subsequence to compute and intern
   * @param end the index of the end of the subsequence to compute and intern
   * @return a subsequence of seq from start to end that is interned
   * @see #internSubsequence(int[], int, int)
   */
  /*@Pure*/
  @SuppressWarnings({"purity", "lock"}) // interning logic
  public static /*@PolyNull*/ /*@Interned*/ String /*@Interned*/ [] internSubsequence(
      /*@PolyNull*/ /*@Interned*/ String /*@Interned*/ [] seq,
      /*@IndexFor("#1")*/ /*@LessThan("#3")*/ int start,
      /*@NonNegative*/ /*@LTLengthOf(value="#1", offset="#2 - 1")*/ int end) {
    if (assertsEnabled && !Intern.isInterned(seq)) {
      throw new IllegalArgumentException();
    }
    SequenceAndIndices</*@PolyNull*/ /*@Interned*/ String /*@Interned*/ []> sai =
        new SequenceAndIndices</*@PolyNull*/ /*@Interned*/ String /*@Interned*/ []>(seq, start, end);
    @SuppressWarnings("nullness") // same nullness as key
    WeakReference</*@PolyNull*/ /*@Interned*/ String /*@Interned*/ []> lookup =
        internedStringSequenceAndIndices.get(sai);
    /*@PolyNull*/ /*@Interned*/ String[] result1 = (lookup != null) ? lookup.get() : null;
    if (result1 != null) {
      return result1;
    } else {
      /*@PolyNull*/ /*@Interned*/ String[] subseq_uninterned = ArraysPlume.subarray(seq, start, end - start);
      /*@PolyNull*/ /*@Interned*/ String /*@Interned*/ [] subseq = Intern.intern(subseq_uninterned);
      @SuppressWarnings("nullness") // safe because map does no side effects
      Object
          ignore = // assignment just so there is a place to hang the @SuppressWarnings annotation
          internedStringSequenceAndIndices.put(
                  sai,
                  new WeakReference</*@PolyNull*/ /*@Interned*/ String /*@Interned*/ []>(subseq));
      return subseq;
    }
  }

  /**
   * Data structure for storing triples of a sequence and start and end indices, to represent a
   * subsequence. Requires that the sequence be interned. Used for interning the repeated finding of
   * subsequences on the same sequence.
   */
  private static final class SequenceAndIndices<T extends /*@Interned*/ Object> {
    public T seq;
    public /*@NonNegative*/ int start;
    public int end;

    /** @param seq an interned array */
    public SequenceAndIndices(T seq, /*@NonNegative*/ int start, int end) {
      if (assertsEnabled && !Intern.isInterned(seq)) {
        throw new IllegalArgumentException();
      }
      this.seq = seq;
      this.start = start;
      this.end = end;
    }

    @SuppressWarnings("unchecked")
    /*@Pure*/
    @Override
    public boolean equals(
        /*>>>@GuardSatisfied SequenceAndIndices<T> this,*/
        /*@GuardSatisfied*/ /*@Nullable*/ Object other) {
      if (other instanceof SequenceAndIndices<?>) {
        @SuppressWarnings("unchecked")
        SequenceAndIndices<T> other_sai = (SequenceAndIndices<T>) other;
        return equalsSequenceAndIndices(other_sai);
      } else {
        return false;
      }
    }

    /*@Pure*/
    public boolean equalsSequenceAndIndices(
        /*>>>@GuardSatisfied SequenceAndIndices<T> this,*/
        /*@GuardSatisfied*/ SequenceAndIndices<T> other) {
      return ((this.seq == other.seq) && this.start == other.start && this.end == other.end);
    }

    /*@Pure*/
    @Override
    public int hashCode(/*>>>@GuardSatisfied SequenceAndIndices<T> this*/) {
      return seq.hashCode() + start * 30 - end * 2;
    }

    // For debugging
    /*@SideEffectFree*/
    @Override
    public String toString(/*>>>@GuardSatisfied SequenceAndIndices<T> this*/) {
      return "SAI(" + start + "," + end + ") from: " + ArraysPlume.toString(seq);
    }
  }

  /**
   * Hasher object which hashes and compares String[] objects according to their contents.
   *
   * @see Hasher
   */
  private static final class SequenceAndIndicesHasher<T extends /*@Interned*/ Object>
      implements Hasher {
    @Override
    public boolean equals(Object a1, Object a2) {
      @SuppressWarnings("unchecked")
      SequenceAndIndices<T> sai1 = (SequenceAndIndices<T>) a1;
      @SuppressWarnings("unchecked")
      SequenceAndIndices<T> sai2 = (SequenceAndIndices<T>) a2;
      // The SAI objects are *not* interned, but the arrays inside them are.
      return sai1.equals(sai2);
    }

    @Override
    public int hashCode(Object o) {
      return o.hashCode();
    }
  }
}
