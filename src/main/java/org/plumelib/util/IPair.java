package org.plumelib.util;

import java.util.Objects;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

// The type variables are called V1 and V2 so that T1 and T2 can be used for method type variables.
/**
 * Immutable pair class.
 *
 * @param <V1> the type of the first element of the pair
 * @param <V2> the type of the second element of the pair
 */
// TODO: as class is immutable, use @Covariant annotation.
public class IPair<V1, V2> {
  /** The first element of the pair. */
  public final V1 a;

  /** The second element of the pair. */
  public final V2 b;

  /**
   * Creates a new immutable pair. Clients should use {@link #of}.
   *
   * @param a the first element of the pair
   * @param b the second element of the pair
   */
  private IPair(V1 a, V2 b) {
    this.a = a;
    this.b = b;
  }

  /**
   * Creates a new immutable pair.
   *
   * @param <T1> type of first argument
   * @param <T2> type of second argument
   * @param a first argument
   * @param b second argument
   * @return a pair of the values (a, b)
   */
  public static <T1 extends @Nullable Object, T2 extends @Nullable Object> IPair<T1, T2> of(
      T1 a, T2 b) {
    return new IPair<>(a, b);
  }

  // The typical way to make a copy is to first call super.clone() and then modify it.
  // That implementation strategy does not work for IPair because its fields are final, so the clone
  // and deepCopy() methods use of() instead.

  /**
   * Returns a copy of this in which each element is a clone of the corresponding element of this.
   * {@code clone()} may or may not itself make a deep copy of the elements.
   *
   * @param <T1> the type of the first element of the pair
   * @param <T2> the type of the second element of the pair
   * @param orig a pair
   * @return a copy of {@code orig}, with all elements cloned
   */
  @SuppressWarnings({
    "nullness", // generics problem with deepCopy()
    "signedness:return" // generics problem with lower bound
  })
  public static <T1 extends Cloneable, T2 extends Cloneable> IPair<T1, T2> cloneElements(
      IPair<T1, T2> orig) {

    T1 oldA = orig.a;
    T1 newA = oldA == null ? oldA : UtilPlume.clone(oldA);
    T2 oldB = orig.b;
    T2 newB = oldB == null ? oldB : UtilPlume.clone(oldB);
    return of(newA, newB);
  }

  /**
   * Returns a deep copy of this: each element is a deep copy (according to the {@code DeepCopyable}
   * interface) of the corresponding element of this.
   *
   * @param <T1> the type of the first element of the pair
   * @param <T2> the type of the second element of the pair
   * @param orig a pair
   * @return a deep copy of {@code orig}
   */
  @SuppressWarnings("nullness") // generics problem with deepCopy()
  public static <T1 extends DeepCopyable<T1>, T2 extends DeepCopyable<T2>> IPair<T1, T2> deepCopy(
      IPair<T1, T2> orig) {
    return of(DeepCopyable.deepCopyOrNull(orig.a), DeepCopyable.deepCopyOrNull(orig.b));
  }

  /**
   * Returns a copy, where the {@code a} element is deep: the {@code a} element is a deep copy
   * (according to the {@code DeepCopyable} interface), and the {@code b} element is identical to
   * the argument.
   *
   * @param <T1> the type of the first element of the pair
   * @param <T2> the type of the second element of the pair
   * @param orig a pair
   * @return a copy of {@code orig}, where the first element is a deep copy
   */
  @SuppressWarnings("nullness") // generics problem with deepCopy()
  public static <T1 extends DeepCopyable<T1>, T2> IPair<T1, T2> deepCopyFirst(IPair<T1, T2> orig) {
    return of(DeepCopyable.deepCopyOrNull(orig.a), orig.b);
  }

  /**
   * Returns a copy, where the {@code b} element is deep: the {@code a} element is identical to the
   * argument, and the {@code b} element is a deep copy (according to the {@code DeepCopyable}
   * interface).
   *
   * @param <T1> the type of the first element of the pair
   * @param <T2> the type of the second element of the pair
   * @param orig a pair
   * @return a copy of {@code orig}, where the second element is a deep copy
   */
  @SuppressWarnings("nullness") // generics problem with deepCopy()
  public static <T1, T2 extends DeepCopyable<T2>> IPair<T1, T2> deepCopySecond(IPair<T1, T2> orig) {
    return of(orig.a, DeepCopyable.deepCopyOrNull(orig.b));
  }

  @Override
  @Pure
  public boolean equals(@GuardSatisfied IPair<V1, V2> this, @GuardSatisfied @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IPair)) {
      return false;
    }
    // generics are not checked at run time!
    @SuppressWarnings("unchecked")
    IPair<V1, V2> other = (IPair<V1, V2>) obj;
    return Objects.equals(this.a, other.a) && Objects.equals(this.b, other.b);
  }

  /** The cached hash code. -1 means it needs to be computed. */
  private int hashCode = -1;

  @SuppressWarnings({
    "allcheckers:purity.not.deterministic.not.sideeffectfree.assign.field", // caching
    "signedness:override.receiver" // being fixed
  })
  @Override
  @Pure
  public int hashCode(@GuardSatisfied IPair<V1, V2> this) {
    if (hashCode == -1) {
      hashCode = Objects.hash(a, b);
    }
    return hashCode;
  }

  @SuppressWarnings(
      "signedness:argument") // true positive: printing what might be an unsigned value
  @Override
  @SideEffectFree
  public String toString(@GuardSatisfied IPair<V1, V2> this) {
    return "IPair(" + String.valueOf(a) + ", " + String.valueOf(b) + ")";
  }
}
