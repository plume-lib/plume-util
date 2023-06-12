package org.plumelib.util;

import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Mutable pair class: type-safely holds two objects of possibly-different types. Defines
 * toString(), equals(), and hashCode() routines. If you don't need these features, you are better
 * off defining your own tiny class with meaningful field names.
 *
 * @param <T1> the type of the first element of the pair
 * @param <T2> the type of the second element of the pair
 * @deprecated use {@link MPair} instead, or {@link IPair} for an immutable pair
 */
@Deprecated // 2023-05-20
public class Pair<T1 extends @Nullable Object, T2 extends @Nullable Object> {
  /** The first element of the pair. */
  public T1 a;

  /** The second element of the pair. */
  public T2 b;

  /**
   * Make a new pair.
   *
   * @param a the first element of the pair
   * @param b the second element of the pair
   */
  public Pair(T1 a, T2 b) {
    this.a = a;
    this.b = b;
  }

  /**
   * Factory method with short name and no need to name type parameters.
   *
   * @param <A> type of first argument
   * @param <B> type of second argument
   * @param a first argument
   * @param b second argument
   * @return a pair of the values (a, b)
   */
  public static <A extends @Nullable Object, B extends @Nullable Object> Pair<A, B> of(A a, B b) {
    return new Pair<>(a, b);
  }

  @Override
  @SideEffectFree
  public String toString(@GuardSatisfied Pair<T1, T2> this) {
    return "<" + String.valueOf(a) + "," + String.valueOf(b) + ">";
  }

  @Override
  @Pure
  public boolean equals(@GuardSatisfied Pair<T1, T2> this, @GuardSatisfied @Nullable Object obj) {
    if (!(obj instanceof Pair<?, ?>)) {
      return false;
    }
    // generics are not checked at run time!
    @SuppressWarnings("unchecked")
    Pair<T1, T2> other = (Pair<T1, T2>) obj;
    return (((this.a == other.a) || (this.a != null && this.a.equals(other.a)))
        && ((this.b == other.b) || (this.b != null && this.b.equals(other.b))));
  }

  // If fields a and b were made final, then the hashcode could be cached.
  // (And if they aren't final, it's a bit odd to be calling hashCode.)
  // But then the class would not be useful for mutable pairs.
  @SuppressWarnings("signedness:override.receiver") // temporary
  @Override
  @Pure
  public int hashCode(@GuardSatisfied Pair<T1, T2> this) {
    return (((a == null) ? 0 : a.hashCode()) + ((b == null) ? 0 : b.hashCode()));
  }
}
