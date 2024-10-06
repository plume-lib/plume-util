package org.plumelib.util;

import java.util.Objects;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

// The class type variables are called V1 and V2 so that T1 and T2 can be used for method type
// variables.
/**
 * Immutable pair class. If mutation is needed, use {@link MPair}.
 *
 * @param <V1> the type of the first element of the pair
 * @param <V2> the type of the second element of the pair
 */
// TODO: as class is immutable, use @Covariant annotation.
// This class does not implement DeepCopyable because that would require that V1 and V2 implement
// DeepCopyable, but this class should be applicable to any types.  Therefore, deepCopy() in this
// class is a static method that requires that the elements of the argument are DeepCopyable,
public class IPair<V1, V2> {
  /** The first element of the pair. */
  public final V1 first;

  /** The second element of the pair. */
  public final V2 second;

  /**
   * Creates a new immutable pair. Clients should use {@link #of}.
   *
   * @param first the first element of the pair
   * @param second the second element of the pair
   */
  private IPair(V1 first, V2 second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Creates a new immutable pair.
   *
   * @param <T1> type of first argument
   * @param <T2> type of second argument
   * @param first first argument
   * @param second second argument
   * @return a pair of the values (first, second)
   */
  public static <T1, T2> IPair<T1, T2> of(T1 first, T2 second) {
    return new IPair<>(first, second);
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
  // This method is static so that the pair element types can be constrained to be Cloneable.
  @SuppressWarnings("nullness") // generics problem with deepCopy()
  public static <T1 extends Cloneable, T2 extends Cloneable> IPair<T1, T2> cloneElements(
      IPair<T1, T2> orig) {
    T1 oldFirst = orig.first;
    T1 newFirst = oldFirst == null ? oldFirst : UtilPlume.clone(oldFirst);
    T2 oldSecond = orig.second;
    T2 newSecond = oldSecond == null ? oldSecond : UtilPlume.clone(oldSecond);
    return of(newFirst, newSecond);
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
  // This method is static so that the pair element types can be constrained to be DeepCopyable.
  public static <T1 extends DeepCopyable<T1>, T2 extends DeepCopyable<T2>> IPair<T1, T2> deepCopy(
      IPair<T1, T2> orig) {
    return of(DeepCopyable.deepCopyOrNull(orig.first), DeepCopyable.deepCopyOrNull(orig.second));
  }

  /**
   * Returns a copy, where the {@code first} element is deep: the {@code first} element is a deep
   * copy (according to the {@code DeepCopyable} interface), and the {@code second} element is
   * identical to the argument.
   *
   * @param <T1> the type of the first element of the pair
   * @param <T2> the type of the second element of the pair
   * @param orig a pair
   * @return a copy of {@code orig}, where the first element is a deep copy
   */
  @SuppressWarnings("nullness") // generics problem with deepCopy()
  public static <T1 extends DeepCopyable<T1>, T2> IPair<T1, T2> deepCopyFirst(IPair<T1, T2> orig) {
    return of(DeepCopyable.deepCopyOrNull(orig.first), orig.second);
  }

  /**
   * Returns a copy, where the {@code second} element is deep: the {@code first} element is
   * identical to the argument, and the {@code second} element is a deep copy (according to the
   * {@code DeepCopyable} interface).
   *
   * @param <T1> the type of the first element of the pair
   * @param <T2> the type of the second element of the pair
   * @param orig a pair
   * @return a copy of {@code orig}, where the second element is a deep copy
   */
  @SuppressWarnings("nullness") // generics problem with deepCopy()
  public static <T1, T2 extends DeepCopyable<T2>> IPair<T1, T2> deepCopySecond(IPair<T1, T2> orig) {
    return of(orig.first, DeepCopyable.deepCopyOrNull(orig.second));
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
    @SuppressWarnings("unchecked") // generics are not checked at run time
    IPair<V1, V2> other = (IPair<V1, V2>) obj;
    return Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second);
  }

  /** The cached hash code. -1 means it needs to be computed. */
  private int hashCode = -1;

  @SuppressWarnings({
    "allcheckers:purity.not.deterministic.not.sideeffectfree.assign.field", // caching
    "signedness:override.receiver" // being fixed
  })
  @Pure
  @Override
  public int hashCode(@GuardSatisfied IPair<V1, V2> this) {
    if (hashCode == -1) {
      hashCode = Objects.hash(first, second);
    }
    return hashCode;
  }

  @SuppressWarnings(
      "signedness:argument") // true positive: String.valueOf() argument might be an unsigned value
  @SideEffectFree
  @Override
  public String toString(@GuardSatisfied IPair<V1, V2> this) {
    return "IPair(" + String.valueOf(first) + ", " + String.valueOf(second) + ")";
  }
}
