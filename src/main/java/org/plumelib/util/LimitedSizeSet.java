package org.plumelib.util;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/*>>>
import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.lock.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;
*/

/**
 * LimitedSizeSet stores up to some maximum number of unique values. If more than that many elements
 * are added, then functionality is degraded: most operations return a conservative estimate
 * (because the internal representation is nulled, in order to save space).
 *
 * @param <T> the type of elements in the set
 */
public class LimitedSizeSet<T> implements Serializable, Cloneable {
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20031021L;

  // public final int max_values;

  /**
   * If null, then at least num_values distinct values have been seen. The size is not separately
   * stored, because that would take extra space.
   */
  protected /*@Nullable*/ T /*@Nullable*/ /*@MinLen(1)*/[] values;
  /** The number of active elements (equivalently, the first unused index). */
  // Not exactly @IndexOrHigh("values"), because the invariant is broken when
  // the values field is set to null. Warnings are suppressed when breaking the invariant.
  /*@IndexOrHigh("values")*/ int num_values;

  /** Whether assertions are enabled. */
  private static boolean assertsEnabled = false;

  static {
    assert assertsEnabled = true; // Intentional side-effect!!!
    // Now assertsEnabled is set to the correct value
  }

  /**
   * Create a new LimitedSizeSet that can hold max_values values.
   *
   * @param max_values the maximum number of values this set will be able to hold; must be positive
   */
  public LimitedSizeSet(/*@Positive*/ int max_values) {
    if (assertsEnabled && !(max_values > 0)) {
      throw new IllegalArgumentException("max_values should be positive, is " + max_values);
    }
    // this.max_values = max_values;
    @SuppressWarnings({
      "unchecked",
      "value" // https://github.com/kelloggm/checker-framework/issues/174
    })
    /*@Nullable*/ T /*@MinLen(1)*/[] new_values_array =
        (/*@Nullable*/ T[]) new /*@Nullable*/ Object[max_values];
    values = new_values_array;
    num_values = 0;
  }

  public void add(T elt) {
    if (repNulled()) {
      return;
    }

    if (contains(elt)) {
      return;
    }
    if (num_values == values.length) {
      nullRep();
      return;
    }
    values[num_values] = elt;
    num_values++;
  }

  public void addAll(LimitedSizeSet<? extends T> s) {
    @SuppressWarnings("interning") // optimization; not a subclass of Collection, though
    boolean sameObject = (this == s);
    if (sameObject) {
      return;
    }
    if (repNulled()) {
      return;
    }
    if (s.repNulled()) {
      // We don't know whether the elements of this and the argument were
      // disjoint.  There might be anywhere from max(size(), s.size()) to
      // (size() + s.size()) elements in the resulting set.
      if (s.size() > values.length) {
        nullRep();
        return;
      } else {
        throw new Error(
            "Arg is rep-nulled, so we don't know its values and can't add them to this.");
      }
    }
    // TODO: s.values isn't modified by the call to add.  Use a local variable until
    // https://tinyurl.com/cfissue/984 is fixed.
    /*@Nullable*/ T /*@SameLen("s.values")*/[] svalues = s.values;
    for (int i = 0; i < s.size(); i++) {
      // This implies that the set cannot hold null.
      assert svalues[i] != null : "@AssumeAssertion(nullness): used portion of array";
      add(svalues[i]);
      if (repNulled()) {
        return; // optimization, not necessary for correctness
      }
    }
  }

  @SuppressWarnings("deterministic") // pure wrt equals() but not ==: throws a new exception
  /*@Pure*/
  public boolean contains(T elt) {
    if (repNulled()) {
      throw new UnsupportedOperationException();
    }
    for (int i = 0; i < num_values; i++) {
      if (Objects.equals(values[i], elt)) {
        return true;
      }
    }
    return false;
  }

  /**
   * A lower bound on the number of elements in the set. Returns either the number of elements that
   * have been inserted in the set, or max_size(), whichever is less.
   *
   * @return a number that is a lower bound on the number of elements added to the set
   */
  /*@Pure*/
  public /*@IndexOrHigh("this.values")*/ int size(/*>>>@GuardSatisfied LimitedSizeSet<T> this*/) {
    return num_values;
  }

  /**
   * An upper bound on how many distinct elements can be individually represented in the set.
   * Returns max_values+1 (where max_values is the argument to the constructor).
   *
   * @return maximum capacity of the set representation
   */
  @SuppressWarnings(
      "lowerbound") // https://tinyurl.com/cfissue/1606: nulling the rep leaves num_values positive
  public /*@Positive*/ int max_size() {
    if (repNulled()) {
      return num_values;
    } else {
      return values.length + 1;
    }
  }

  /**
   * Returns true if more elements have been added than this set can contain (which is the integer
   * that was passed to the constructor when creating this set).
   *
   * @return true if this set has been filled to capacity and its internal representation is nulled
   */
  /*@EnsuresNonNullIf(result=false, expression="values")*/
  /*@Pure*/
  public boolean repNulled(/*>>>@GuardSatisfied LimitedSizeSet<T> this*/) {
    return values == null;
  }

  /**
   * Null the representation, which happens when a client tries to add more elements to this set
   * than it can contain (which is the integer that was passed to the constructor when creating this
   * set).
   */
  @SuppressWarnings("upperbound") // nulling the rep: after which no indexing will occur
  private void nullRep() {
    if (repNulled()) {
      return;
    }
    num_values = values.length + 1;
    values = null;
  }

  @SuppressWarnings("sideeffectfree") // side effect to local state (clone)
  /*@SideEffectFree*/
  @Override
  public LimitedSizeSet<T> clone(/*>>>@GuardSatisfied LimitedSizeSet<T> this*/) {
    LimitedSizeSet<T> result;
    try {
      @SuppressWarnings("unchecked")
      LimitedSizeSet<T> result_as_lss = (LimitedSizeSet<T>) super.clone();
      result = result_as_lss;
    } catch (CloneNotSupportedException e) {
      throw new Error(); // can't happen
    }
    if (values != null) {
      result.values = values.clone();
    }
    return result;
  }

  /**
   * Merges a list of {@code LimitedSizeSet<T>} objects into a single object that represents the
   * values seen by the entire list. Returns the new object, whose max_values is the given integer.
   *
   * @param <T> (super)type of elements of the sets
   * @param max_values the maximum size for the returned LimitedSizeSet
   * @param slist a list of LimitedSizeSet, whose elements will be merged
   * @return a LimitedSizeSet that merges the elements of slist
   */
  public static <T> LimitedSizeSet<T> merge(
      /*@Positive*/ int max_values, List<LimitedSizeSet<? extends T>> slist) {
    LimitedSizeSet<T> result = new LimitedSizeSet<T>(max_values);
    for (LimitedSizeSet<? extends T> s : slist) {
      result.addAll(s);
    }
    return result;
  }

  /*@SideEffectFree*/
  @Override
  public String toString(/*>>>@GuardSatisfied LimitedSizeSet<T> this*/) {
    return ("[size="
        + size()
        + "; "
        + ((repNulled()) ? "null" : ArraysPlume.toString(values))
        + "]");
  }
}
