package org.plumelib.util;

import java.lang.ref.WeakReference;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Immutable pair class: type-safely holds two objects of possibly-different types.
 *
 * <p>Differs from {@code Pair} in the following ways: is immutable, cannot hold null, holds its
 * elements with weak pointers, and its equals() method uses object equality to compare its
 * elements.
 *
 * @param <T1> the type of the pair's first element
 * @param <T2> the type of the pair's second element
 */
public class WeakIdentityPair<T1 extends Object, T2 extends Object> {

  /** The first element of the pair. */
  private final WeakReference<T1> a;
  /** The second element of the pair. */
  private final WeakReference<T2> b;

  /** The hash code of this. */
  // Must cache the hashCode to prevent it from changing.
  private final int hashCode;

  /**
   * Creates a new weakly-held pair of {@code a} and {@code b}.
   *
   * @param a the first element of the pair
   * @param b the second element of the pair
   */
  public WeakIdentityPair(T1 a, T2 b) {
    if (a == null || b == null) {
      throw new IllegalArgumentException(
          String.format("WeakIdentityPair cannot hold null: %s %s", a, b));
    }
    this.a = new WeakReference<>(a);
    this.b = new WeakReference<>(b);
    int hashCodeA;
    int hashCodeB;
    try {
      hashCodeA = a.hashCode();
    } catch (StackOverflowError e) {
      hashCodeA = 0;
    }
    try {
      hashCodeB = b.hashCode();
    } catch (StackOverflowError e) {
      hashCodeB = 0;
    }
    this.hashCode = hashCodeA + hashCodeB;
  }

  /**
   * Factory method with short name and no need to name type parameters.
   *
   * @param <A> type of first argument
   * @param <B> type of second argument
   * @param a first argument
   * @param b second argument
   * @return a WeakIdentityPair of (a, b)
   */
  public static <A extends Object, B extends Object> WeakIdentityPair<A, B> of(A a, B b) {
    return new WeakIdentityPair<A, B>(a, b);
  }

  /**
   * Return the first element of the pair, or null if it has been garbage-collected.
   *
   * @return the first element of the pail, or null if it has been garbage-collected
   */
  @SideEffectFree
  public @Nullable T1 getA(@GuardSatisfied WeakIdentityPair<T1, T2> this) {
    return a.get();
  }

  /**
   * Return the second element of the pair, or null if it has been garbage-collected.
   *
   * @return the second element of the pair, or null if it has been garbage-collected
   */
  @SideEffectFree
  public @Nullable T2 getB(@GuardSatisfied WeakIdentityPair<T1, T2> this) {
    return b.get();
  }

  @Override
  @SideEffectFree
  public String toString(@GuardSatisfied WeakIdentityPair<T1, T2> this) {
    return "<" + String.valueOf(a) + "," + String.valueOf(b) + ">";
  }

  @Override
  @SuppressWarnings({"interning", "allcheckers:purity.not.deterministic.call", "lock"})
  // not @Deterministic: values can change by being garbage-collected
  @SideEffectFree
  public boolean equals(
      @GuardSatisfied WeakIdentityPair<T1, T2> this, @GuardSatisfied @Nullable Object obj) {
    if (!(obj instanceof WeakIdentityPair<?, ?>)) {
      return false;
    }
    // generics are not checked at run time!
    @SuppressWarnings("unchecked")
    WeakIdentityPair<T1, T2> other = (WeakIdentityPair<T1, T2>) obj;

    if (hashCode != other.hashCode) {
      return false;
    }

    @Nullable T1 a = getA();
    @Nullable T2 b = getB();
    @Nullable T1 oa = other.getA();
    @Nullable T2 ob = other.getB();
    if (a == null || b == null || oa == null || ob == null) {
      // false if any of the components has been garbage-collected
      return false;
    }
    return a == oa && b == ob;
  }

  @Override
  @Pure
  public int hashCode(@GuardSatisfied WeakIdentityPair<T1, T2> this) {
    return hashCode;
  }
}
