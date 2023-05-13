package org.plumelib.util;

import java.util.concurrent.atomic.AtomicInteger;
import org.checkerframework.checker.lock.qual.GuardSatisfied;

/**
 * This is a deterministic version of the {@link Object} class. To remove one source of
 * nondeterminism from your program, do not instantiate the Object class, as in {@code new
 * Object()}; instead do {@code new DeterministicObject()}.
 *
 * <p>This class differs from Object in that it overrides {@link #hashCode()}. Any use of {@code
 * Object.hashCode()} is nondeterministic because the return value of {@code Object.hashCode()}
 * depends on when the garbage collector runs. That nondeterminism can affect the iteration order of
 * HashMaps and HashSets, the output of {@code toString()}, and other behavior.
 *
 * <p>To implement similar functionality for other classes, see {@link UniqueId}.
 *
 * @see UniqueId
 */
public class DeterministicObject {

  /** The number of objects created so far. */
  static final AtomicInteger counter = new AtomicInteger(0);

  /** The unique ID for this object. */
  final int uid = counter.getAndIncrement();

  /** Create a DeterministicObject. */
  public DeterministicObject() {}

  /**
   * {@inheritDoc}
   *
   * <p>Returns a unique ID for the object. The first object created has the id (and hash code) 0,
   * the second one has 1, and so forth.
   */
  @Override
  public int hashCode(@GuardSatisfied DeterministicObject this) {
    return uid;
  }
}
