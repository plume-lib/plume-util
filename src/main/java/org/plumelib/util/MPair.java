package org.plumelib.util;

import java.util.Objects;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

// The type variables are called V1 and V2 so that T1 and T2 can be used for method type variables.
/**
 * Mutable pair class: type-safely holds two objects of possibly-different types.
 *
 * @param <V1> the type of the first element of the pair
 * @param <V2> the type of the second element of the pair
 */
public class MPair<V1 extends @Nullable Object, V2 extends @Nullable Object> {
  /** The first element of the pair. */
  public V1 a;

  /** The second element of the pair. */
  public V2 b;

  /**
   * Creates a new mutable pair. Clients should use {@link #of}.
   *
   * @param a the first element of the pair
   * @param b the second element of the pair
   */
  private MPair(V1 a, V2 b) {
    this.a = a;
    this.b = b;
  }

  /**
   * Creates a new mutable pair.
   *
   * @param <T1> type of first argument
   * @param <T2> type of second argument
   * @param a first argument
   * @param b second argument
   * @return a pair of the values (a, b)
   */
  public static <T1 extends @Nullable Object, T2 extends @Nullable Object> MPair<T1, T2> of(
      T1 a, T2 b) {
    return new MPair<>(a, b);
  }

  @Override
  @Pure
  public boolean equals(@GuardSatisfied MPair<V1, V2> this, @GuardSatisfied @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MPair<?, ?>)) {
      return false;
    }
    // generics are not checked at run time!
    @SuppressWarnings("unchecked")
    MPair<V1, V2> other = (MPair<V1, V2>) obj;
    return Objects.equals(this.a, other.a) && Objects.equals(this.b, other.b);
  }

  // If fields a and b were made final, then the hashcode could be cached.
  // (And if they aren't final, it's a bit odd to be calling hashCode.)
  // But then the class would not be useful for mutable pairs.
  @SuppressWarnings("signedness:override.receiver") // temporary
  @Override
  @Pure
  public int hashCode(@GuardSatisfied MPair<V1, V2> this) {
    return Objects.hash(a, b);
  }

  @Override
  @SideEffectFree
  public String toString(@GuardSatisfied MPair<V1, V2> this) {
    return "MPair(" + String.valueOf(a) + ", " + String.valueOf(b) + ")";
  }
}
