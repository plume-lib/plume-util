package org.plumelib.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import org.checkerframework.checker.interning.qual.FindDistinct;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;

/**
 * An arbitrary-size set that is very efficient (more efficient than HashSet) for 0 and 1 elements.
 * Uses object identity for object comparison.
 *
 * <p>Usually, you should use {@link IdentityArraySet} instead. The advantage of this over {@link
 * IdentityArraySet} is that this is efficient for large sets.
 *
 * @param <T> the type of elements of the set
 */
@SuppressWarnings("ExtendsObject")
public final class IdentityMostlySingletonSet<T extends Object>
    extends AbstractMostlySingletonSet<T> {

  /** Create an IdentityMostlySingletonSet. */
  public IdentityMostlySingletonSet() {
    super(State.EMPTY);
  }

  /**
   * Create an IdentityMostlySingletonSet containing one value.
   *
   * @param value the single element of the set
   */
  public IdentityMostlySingletonSet(T value) {
    super(State.SINGLETON, value);
  }

  @SuppressWarnings("fallthrough")
  @Override
  public boolean add(@GuardSatisfied IdentityMostlySingletonSet<T> this, @FindDistinct T e) {
    switch (state) {
      case EMPTY:
        state = State.SINGLETON;
        value = e;
        return true;
      case SINGLETON:
        if (value == e) {
          return false;
        }
        makeNonSingleton();
      // fall through
      case ANY:
        assert set != null : "@AssumeAssertion(nullness): set initialized before";
        return set.add(e);
      default:
        throw new IllegalStateException("Unhandled state " + state);
    }
  }

  /** Switch the representation of this from SINGLETON to ANY. */
  private void makeNonSingleton(@GuardSatisfied IdentityMostlySingletonSet<T> this) {
    state = State.ANY;
    set = Collections.newSetFromMap(new IdentityHashMap<>(4));
    assert value != null : "@AssumeAssertion(nullness): previous add is non-null";
    set.add(value);
    value = null;
  }

  @SuppressWarnings("interning:not.interned") // this class uses object identity
  @Override
  public boolean contains(
      @GuardSatisfied IdentityMostlySingletonSet<T> this,
      @GuardSatisfied @UnknownSignedness Object o) {
    switch (state) {
      case EMPTY:
        return false;
      case SINGLETON:
        return o == value;
      case ANY:
        assert set != null : "@AssumeAssertion(nullness): set initialized before";
        return set.contains(o);
      default:
        throw new IllegalStateException("Unhandled state " + state);
    }
  }
}
