package org.plumelib.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

/**
 * Given two sorted iterators, this class returns a new iterator that pairs equal elements of the
 * inputs, according to the sort order or the given comparator. If an element has no equal element
 * in the other iterator, then the element is paired with null.
 *
 * <p>For example, suppose that the inputs are
 *
 * <pre>
 *   [1, 2, 3, 5] and
 *   [1, 3, 5, 7, 9].
 * </pre>
 *
 * <p>Then the output is
 *
 * <pre>
 *   [(1,1), (2,null), (3,3), (5,5), (null,7), (null, 9)].
 * </pre>
 *
 * <p>(This operation is similar to, but not the same as, the operation called "zipping".)
 *
 * <p>In some cases this is just the right abstraction. But in some cases it's appropriate to use
 * set intersection/difference instead.
 *
 * @param <T> the element type of each component iterator; this OrderedPairIterator has elements of
 *     type {@code MPair<T,T>}
 */
// T need not extend Comparable<T>, because a comparator can be passed in.
// TODO: Make this use IPair instead?
public class OrderedPairIterator<T extends @Nullable Object>
    implements java.util.Iterator<MPair<@Nullable T, @Nullable T>> {

  /** The iterator for first elements of pairs. */
  Iterator<T> itor1;

  /** The iterator for second elements of pairs. */
  Iterator<T> itor2;

  /** The next element to be read by itor1. */
  @Nullable T next1;

  /** The next element to be read by itor2. */
  @Nullable T next2;

  /**
   * The comparator to be used to compare elements from the two iterators, to determine whether they
   * match. Null to use the natural comparison.
   */
  @Nullable Comparator<? super T> comparator;

  /**
   * Create an iterator that returns pairs, where each pair contains has an element from each
   * iterator and the two elements are equal.
   *
   * @param itor1 iterator for first elements of pairs
   * @param itor2 iterator for second elements of pairs
   */
  // For this constructor, the arg type is actually Iterator<T extends
  // Comparable<T>>, but T is already bound above and can't be changed.
  public OrderedPairIterator(Iterator<T> itor1, Iterator<T> itor2) {
    this.itor1 = itor1;
    this.itor2 = itor2;
    setnext1();
    setnext2();
  }

  /**
   * Create an iterator that returns pairs, where each pair contains has an element from each
   * iterator and the two elements are equal according to the comparator.
   *
   * @param itor1 iterator for first elements of pairs
   * @param itor2 iterator for second elements of pairs
   * @param comparator determines whether two elements are equal and should be paired together
   */
  public OrderedPairIterator(Iterator<T> itor1, Iterator<T> itor2, Comparator<T> comparator) {
    this(itor1, itor2);
    this.comparator = comparator;
  }

  /** Set the next1 variable. */
  @RequiresNonNull("itor1")
  private void setnext1(@GuardSatisfied @UnknownInitialization OrderedPairIterator<T> this) {
    next1 = itor1.hasNext() ? itor1.next() : null;
  }

  /** Set the next2 variable. */
  @RequiresNonNull("itor2")
  private void setnext2(@GuardSatisfied @UnknownInitialization OrderedPairIterator<T> this) {
    next2 = itor2.hasNext() ? itor2.next() : null;
  }

  // Have the caller do this directly, probably.
  // public OrderedPairIterator(Set s1, Set s2) {
  //   this((new TreeSet(s1)).iterator(), (new TreeSet(s2)).iterator());
  // }
  @Override
  public boolean hasNext(@GuardSatisfied OrderedPairIterator<T> this) {
    return ((next1 != null) || (next2 != null));
  }

  /**
   * Returns an element of the first iterator, paired with null.
   *
   * @return an element of the first iterator, paired with null
   */
  private MPair<@Nullable T, @Nullable T> return1(@GuardSatisfied OrderedPairIterator<T> this) {
    MPair<@Nullable T, @Nullable T> result =
        MPair.<@Nullable T, @Nullable T>of(next1, (@Nullable T) null);
    setnext1();
    return result;
  }

  /**
   * Returns a pair of null and an element of the second iterator.
   *
   * @return a pair of null and an element of the second iterator
   */
  private MPair<@Nullable T, @Nullable T> return2(@GuardSatisfied OrderedPairIterator<T> this) {
    MPair<@Nullable T, @Nullable T> result =
        MPair.<@Nullable T, @Nullable T>of((@Nullable T) null, next2);
    setnext2();
    return result;
  }

  /**
   * Returns a pair containing an element from each iterator.
   *
   * @return a pair containing an element from each iterator
   */
  private MPair<@Nullable T, @Nullable T> returnboth(@GuardSatisfied OrderedPairIterator<T> this) {
    MPair<@Nullable T, @Nullable T> result = MPair.<@Nullable T, @Nullable T>of(next1, next2);
    setnext1();
    setnext2();
    return result;
  }

  @Override
  public MPair<@Nullable T, @Nullable T> next(@GuardSatisfied OrderedPairIterator<T> this) {
    if (next1 == null) {
      if (next2 == null) {
        throw new NoSuchElementException();
      } else {
        return return2();
      }
    } else {
      if (next2 == null) {
        return return1();
      } else {
        int comparison;
        // Either T extends Comparable<T>, or else a comparator was passed in.
        try {
          if (comparator == null) {
            // This code creates bytecodes that ASM rejects; thus, the classfiles cannot be
            // processed by Gradle, shadowJar, or other tools.  I'm not sure where the fault lies;
            // for now, remove the annotations.
            // @SuppressWarnings("unchecked")
            // Comparable<@NonNull T> cble1 = (Comparable<@NonNull T>) next1;
            @SuppressWarnings({"unchecked", "nullness"})
            Comparable<T> cble1 = (Comparable<T>) next1;
            comparison = cble1.compareTo(next2);
          } else {
            comparison = comparator.compare(next1, next2);
          }
        } catch (NullPointerException npe) {
          // Either one of the arguments is null, or the comparator is buggy
          if (next1 == null && next2 == null) {
            comparison = 0;
          } else if (next1 == null && next2 != null) {
            comparison = -1;
          } else if (next1 != null && next2 == null) {
            comparison = 1;
          } else {
            throw new RuntimeException("this can't happen " + next1 + " " + next2);
          }
        }
        if (comparison < 0) {
          return return1();
        } else if (comparison > 0) {
          return return2();
        } else {
          return returnboth();
        }
      }
    }
  }

  @Override
  public void remove(@GuardSatisfied OrderedPairIterator<T> this) {
    throw new UnsupportedOperationException();
  }
}
