package org.plumelib.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/*>>>
import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.lock.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;
*/

/**
 * LimitedSizeIntSet stores up to some maximum number of unique values. If more than that many
 * elements are added, then functionality is degraded: most operations return a conservative
 * estimate (because the internal representation is nulled, in order to save space).
 *
 * <p>The advantage of this class over {@code LimitedSizeSet<Integer>} is that it does not autobox
 * the int values, so it takes less memory.
 *
 * @see LimitedSizeSet
 */
// I have not evaluated the importance of the optimizations in this class.
// Consider adding:
//  * @deprecated Use LimitedSizeSet instead
// @Deprecated
public class LimitedSizeIntSet implements Serializable, Cloneable {
  // We are Serializable, so we specify a version to allow changes to
  // method signatures without breaking serialization.  If you add or
  // remove fields, you should change this number to the current date.
  static final long serialVersionUID = 20031021L;

  // public final int max_values;

  /**
   * If null, then at least num_values distinct values have been seen. The size is not separately
   * stored, because that would take extra space.
   */
  protected int /*@Nullable*/ /*@MinLen(1)*/[] values;
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
   * Create a new LimitedSizeIntSet that can hold max_values values.
   *
   * @param max_values the maximum number of values this set will be able to hold; must be positive
   */
  public LimitedSizeIntSet(/*@Positive*/ int max_values) {
    if (assertsEnabled && !(max_values > 0)) {
      throw new IllegalArgumentException("max_values should be positive, is " + max_values);
    }
    // this.max_values = max_values;
    values = new int[max_values];
    num_values = 0;
  }

  public void add(int elt) {
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

  public void addAll(LimitedSizeIntSet s) {
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
    int[] svalues = s.values;
    for (int i = 0; i < s.size(); i++) {
      @SuppressWarnings(
          "index") // svalues is the internal rep of s, and s.size() <= s.values.length
      /*@IndexFor("svalues")*/ int index = i;
      add(svalues[index]);
      if (repNulled()) {
        return; // optimization, not necessary for correctness
      }
    }
  }

  @SuppressWarnings("deterministic") // pure wrt equals() but not ==: throws a new exception
  /*@Pure*/
  public boolean contains(int elt) {
    if (repNulled()) {
      throw new UnsupportedOperationException();
    }
    for (int i = 0; i < num_values; i++) {
      if (values[i] == elt) {
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
  public int size(/*>>>@GuardSatisfied LimitedSizeIntSet this*/) {
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
  public boolean repNulled(/*>>>@GuardSatisfied LimitedSizeIntSet this*/) {
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
  public LimitedSizeIntSet clone(/*>>>@GuardSatisfied LimitedSizeIntSet this*/) {
    LimitedSizeIntSet result;
    try {
      result = (LimitedSizeIntSet) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error(); // can't happen
    }
    if (values != null) {
      result.values = values.clone();
    }
    return result;
  }

  /**
   * Merges a list of {@code LimitedSizeIntSet} objects into a single object that represents the
   * values seen by the entire list. Returns the new object, whose max_values is the given integer.
   *
   * @param max_values the maximum size for the returned LimitedSizeIntSet
   * @param slist a list of LimitedSizeIntSet, whose elements will be merged
   * @return a LimitedSizeIntSet that merges the elements of slist
   */
  public static LimitedSizeIntSet merge(
      /*@Positive*/ int max_values, List<LimitedSizeIntSet> slist) {
    LimitedSizeIntSet result = new LimitedSizeIntSet(max_values);
    for (LimitedSizeIntSet s : slist) {
      result.addAll(s);
    }
    return result;
  }

  /*@SideEffectFree*/
  @Override
  public String toString(/*>>>@GuardSatisfied LimitedSizeIntSet this*/) {
    return ("[size=" + size() + "; " + Arrays.toString(values) + "]");
  }
}
