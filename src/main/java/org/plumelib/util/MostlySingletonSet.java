package org.plumelib.util;

import java.util.LinkedHashSet;

/**
 * A set that is more efficient than HashSet for 0 and 1 elements. Uses {@code Objects.equals} for
 * object comparison and a {@link LinkedHashSet} for backing storage.
 */
public final class MostlySingletonSet<T extends Object> extends AbstractMostlySingletonSet<T> {

  /** Create a MostlySingletonSet. */
  public MostlySingletonSet() {
    super(State.EMPTY);
  }

  /** Create a MostlySingletonSet. */
  public MostlySingletonSet(T value) {
    super(State.SINGLETON, value);
  }

  @Override
  public boolean add(T e) {
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
  public boolean contains(Object o) {
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
