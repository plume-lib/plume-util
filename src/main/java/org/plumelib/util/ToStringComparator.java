package org.plumelib.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.mustcall.qual.MustCallUnknown;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.Signed;

/**
 * A comparator that orders values based on the lexicographic ordering of their toString().
 *
 * <p>It handles null values, sorting them according to their printed representation "null".
 */
// Once https://github.com/typetools/checker-framework/issues/1970 is fixed, Comparator's type
// argument should be marked as @Contravariant and this should be declared as "extends
// Comparator<@Nullable Object>".
public class ToStringComparator implements Comparator<Object> {
  /** The unique instance (this class is a singleton). */
  public static ToStringComparator instance = new ToStringComparator();

  /** Creates a ToStringComparator. */
  private ToStringComparator() {}

  @Override
  public int compare(@MustCallUnknown Object o1, @MustCallUnknown Object o2) {
    return Objects.toString(o1).compareTo(Objects.toString(o2));
  }

  /**
   * Returns a copy of the input, sorted according to the elements' {@code toString()}.
   *
   * @param <T> the type of the elements
   * @param in a set of elements
   * @return the elements, sorted according to {@code toString()}
   */
  @SuppressWarnings("nullness:argument") // Comparator should be @Contravariant.
  public static <T extends @Nullable @Signed Object> List<T> sorted(Iterable<T> in) {
    List<T> result = new ArrayList<T>();
    for (T object : in) {
      result.add(object);
    }
    result.sort(instance);
    return result;
  }
}
