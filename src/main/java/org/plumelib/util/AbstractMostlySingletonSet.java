package org.plumelib.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.KeyForBottom;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.signedness.qual.PolySigned;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Base class for arbitrary-size sets that is very efficient (more efficient than HashSet) for 0 and
 * 1 elements.
 *
 * <p>Does not support storing {@code null}.
 *
 * <p>This class exists because it has multiple subclasses (currently {@link MostlySingletonSet} and
 * {@link IdentityMostlySingletonSet}).
 *
 * @param <T> the type of elements of the set
 */
public abstract class AbstractMostlySingletonSet<T extends @Signed Object> implements Set<T> {

  /** The possible states of this set. */
  public enum State {
    /** An empty set. */
    EMPTY,
    /** A singleton set. */
    SINGLETON,
    /** A set of arbitrary size. */
    ANY
  }

  /** The current state. */
  protected State state;

  /** The current value, non-null when the state is SINGLETON. */
  protected @Nullable T value;

  /** The wrapped set, non-null when the state is ANY. */
  protected @Nullable Set<T> set;

  /**
   * Create an AbstractMostlySingletonSet.
   *
   * @param s the state
   */
  protected AbstractMostlySingletonSet(State s) {
    this.state = s;
    this.value = null;
  }

  /**
   * Create an AbstractMostlySingletonSet.
   *
   * @param s the state
   * @param v the value
   */
  protected AbstractMostlySingletonSet(State s, T v) {
    this.state = s;
    this.value = v;
  }

  /** Throw an exception if the internal representation is corrupted. */
  protected void checkRep() {
    if ((state == State.EMPTY && (value != null || set != null))
        || (state == State.SINGLETON && (value == null || set != null))
        || (state == State.ANY && (value != null || set == null))) {
      throw new IllegalStateException(
          String.format("Bad set: state=%s, value=%s, set=%s", state, value, set));
    }
  }

  @Override
  public @NonNegative int size(@GuardSatisfied AbstractMostlySingletonSet<T> this) {
    switch (state) {
      case EMPTY:
        return 0;
      case SINGLETON:
        return 1;
      case ANY:
        assert set != null : "@AssumeAssertion(nullness): set initialized before";
        return set.size();
      default:
        throw new IllegalStateException("Unhandled state " + state);
    }
  }

  @Override
  public boolean isEmpty(@GuardSatisfied AbstractMostlySingletonSet<T> this) {
    return size() == 0;
  }

  @Override
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree",
    "lock:override.receiver" // cannot specify the anonymous receiver type
  })
  @SideEffectFree
  public Iterator<T> iterator() {
    switch (state) {
      case EMPTY:
        return Collections.emptyIterator();
      case SINGLETON:
        return new Iterator<T>() {
          /** True if the iterator has a next element. */
          private boolean hasNext = true;

          @Override
          public boolean hasNext(/*@GuardedBy Iterator<T> this*/ ) {
            return hasNext;
          }

          @Override
          public T next(/*@GuardedBy Iterator<T> this*/ ) {
            if (hasNext) {
              hasNext = false;
              assert value != null : "@AssumeAssertion(nullness): previous add is non-null";
              return value;
            }
            throw new NoSuchElementException();
          }

          @Override
          public void remove(/*@GuardedBy Iterator<T> this*/ ) {
            state = State.EMPTY;
            value = null;
          }
        };
      case ANY:
        assert set != null : "@AssumeAssertion(nullness): set initialized before";
        return set.iterator();
      default:
        throw new IllegalStateException("Unhandled state " + state);
    }
  }

  @Override
  public String toString(@GuardSatisfied AbstractMostlySingletonSet<T> this) {
    switch (state) {
      case EMPTY:
        return "[]";
      case SINGLETON:
        return "[" + value + "]";
      case ANY:
        assert set != null : "@AssumeAssertion(nullness): set initialized before";
        return set.toString();
      default:
        throw new IllegalStateException("Unhandled state " + state);
    }
  }

  @Override
  public boolean addAll(
      @GuardSatisfied AbstractMostlySingletonSet<T> this, Collection<? extends T> c) {
    boolean res = false;
    for (T elem : c) {
      res |= add(elem);
    }
    return res;
  }

  @Override
  public @PolySigned Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings({"nullness:unneeded.suppression", "keyfor:override.return"}) // temporary
  @Override
  public <@KeyForBottom S> @Nullable S[] toArray(@PolyNull S[] a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(
      @GuardSatisfied AbstractMostlySingletonSet<T> this, @Nullable @UnknownSignedness Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(
      @GuardSatisfied AbstractMostlySingletonSet<T> this, @GuardSatisfied Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(@GuardSatisfied AbstractMostlySingletonSet<T> this, Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(@GuardSatisfied AbstractMostlySingletonSet<T> this, Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear(@GuardSatisfied AbstractMostlySingletonSet<T> this) {
    throw new UnsupportedOperationException();
  }
}
