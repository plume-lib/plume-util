package org.plumelib.util;

import java.util.LinkedHashSet;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;

/**
 * A set that is more efficient than HashSet for 0 and 1 elements. Uses {@code Objects.equals} for
 * object comparison and a {@link LinkedHashSet} for backing storage.
 *
 * <p>Usually, you should use {@link ArraySet} instead. The advantage of this over {@link ArraySet}
 * is that this is efficient for large sets.
 *
 * @param <T> the type of elements of the set
 */
public final class MostlySingletonSet<T extends Object> extends AbstractMostlySingletonSet<T> {

  /** Create a MostlySingletonSet. */
  public MostlySingletonSet() {
    super(State.EMPTY);
  }

  /**
   * Create a MostlySingletonSet containing one value.
   *
   * @param value the single element of the set
   */
  public MostlySingletonSet(T value) {
    super(State.SINGLETON, value);
  }

  @Override
  @SuppressWarnings({
    "fallthrough",
    "lock:method.invocation" // #979?
  })
  public boolean add(@GuardSatisfied MostlySingletonSet<T> this, T e) {
    switch (state) {
      case EMPTY:
        state = State.SINGLETON;
        value = e;
        return true;
      case SINGLETON:
        assert value != null : "@AssumeAssertion(nullness): SINGLETON => value != null";
        if (value.equals(e)) {
          return false;
        }
        makeNonSingleton();
        // fall through
      case ANY:
        assert set != null : "@AssumeAssertion(nullness): ANY => value != null";
        return set.add(e);
      default:
        throw new IllegalStateException("Unhandled state " + state);
    }
  }

  /** Switch the representation of this from SINGLETON to ANY. */
  private void makeNonSingleton() {
    state = State.ANY;
    set = new LinkedHashSet<>();
    assert value != null : "@AssumeAssertion(nullness): SINGLETON => value != null";
    set.add(value);
    value = null;
  }

  @Override
  public boolean contains(
      @GuardSatisfied MostlySingletonSet<T> this, @GuardSatisfied @UnknownSignedness Object o) {
    switch (state) {
      case EMPTY:
        return false;
      case SINGLETON:
        assert value != null : "@AssumeAssertion(nullness): SINGLETON => value != null";
        return value.equals(o);
      case ANY:
        assert set != null : "@AssumeAssertion(nullness): set initialized before";
        return set.contains(o);
      default:
        throw new IllegalStateException("Unhandled state " + state);
    }
  }
}
