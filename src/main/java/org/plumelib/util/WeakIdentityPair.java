package org.plumelib.util;

import java.lang.ref.WeakReference;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

// The type variables are called V1 and V2 so that T1 and T2 can be used for method type variables.
/**
 * Immutable pair class with weakly-held values.
 *
 * <p>Differs from {@code IPair} in the following ways: cannot hold null, holds its elements with
 * weak pointers, clients must use getter methods rather than accessing the fields, and its {@code
 * equals()} method uses object equality to compare its elements.
 *
 * @param <V1> the type of the pair's first element
 * @param <V2> the type of the pair's second element
 */
// TODO: as class is immutable, use @Covariant annotation.
public class WeakIdentityPair<V1 extends @NonNull Object, V2 extends @NonNull Object> {

  /** The first element of the pair. */
  private final WeakReference<V1> a;

  /** The second element of the pair. */
  private final WeakReference<V2> b;

  /** The hash code of this. */
  // Must cache the hashCode to prevent it from changing.
  private final int hashCode;

  /**
   * Creates a new weakly-held immutable pair. Clients should use {@link #of}.
   *
   * @param a the first element of the pair
   * @param b the second element of the pair
   * @deprecated use {@link #of}
   */
  @Deprecated // 2023-05-20; to be made private
  public WeakIdentityPair(V1 a, V2 b) {
    if (a == null || b == null) {
      throw new IllegalArgumentException(
          String.format("WeakIdentityPair cannot hold null: %s %s", a, b));
    }
    this.a = new WeakReference<>(a);
    this.b = new WeakReference<>(b);
    this.hashCode = System.identityHashCode(a) + System.identityHashCode(b);
  }

  /**
   * Creates a new weakly-held immutable pair.
   *
   * @param <T1> type of first argument
   * @param <T2> type of second argument
   * @param a first argument
   * @param b second argument
   * @return a pair of the values (a, b)
   */
  public static <T1 extends @NonNull Object, T2 extends @NonNull Object>
      WeakIdentityPair<T1, T2> of(T1 a, T2 b) {
    return new WeakIdentityPair<T1, T2>(a, b);
  }

  /**
   * Return the first element of the pair, or null if it has been garbage-collected.
   *
   * @return the first element of the pail, or null if it has been garbage-collected
   */
  @SideEffectFree
  public @Nullable V1 getA(@GuardSatisfied WeakIdentityPair<V1, V2> this) {
    return a.get();
  }

  /**
   * Return the second element of the pair, or null if it has been garbage-collected.
   *
   * @return the second element of the pair, or null if it has been garbage-collected
   */
  @SideEffectFree
  public @Nullable V2 getB(@GuardSatisfied WeakIdentityPair<V1, V2> this) {
    return b.get();
  }

  @SuppressWarnings({"interning", "allcheckers:purity.not.deterministic.call", "lock"})
  @Override
  // not @Deterministic: values can change by being garbage-collected
  @SideEffectFree
  public boolean equals(
      @GuardSatisfied WeakIdentityPair<V1, V2> this, @GuardSatisfied @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof WeakIdentityPair<?, ?>)) {
      return false;
    }
    // generics are not checked at run time!
    @SuppressWarnings("unchecked")
    WeakIdentityPair<V1, V2> other = (WeakIdentityPair<V1, V2>) obj;

    if (hashCode != other.hashCode) {
      return false;
    }

    @Nullable V1 a = getA();
    @Nullable V2 b = getB();
    @Nullable V1 oa = other.getA();
    @Nullable V2 ob = other.getB();
    if (a == null || b == null || oa == null || ob == null) {
      // false if any of the components has been garbage-collected
      return false;
    }
    return a == oa && b == ob;
  }

  @Override
  @Pure
  public int hashCode(@GuardSatisfied WeakIdentityPair<V1, V2> this) {
    return hashCode;
  }

  @Override
  @SideEffectFree
  public String toString(@GuardSatisfied WeakIdentityPair<V1, V2> this) {
    return "WIPair(" + String.valueOf(a) + "," + String.valueOf(b) + ")";
  }
}
