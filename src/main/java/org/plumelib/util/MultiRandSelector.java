package org.plumelib.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import org.checkerframework.checker.nullness.qual.Nullable;

// TODO: This does not use the Random value that is passed in.

/**
 * Performs uniform random selection over an iterator, where the objects in the iterator may be
 * partitioned so that the random selection chooses the same number from each group.
 *
 * <p>For example, given data about incomes by state, it may be desirable to select 1000 people from
 * each state rather than 50,000 from the nation. As another example, for selecting invocations in a
 * Daikon trace file, it may be desirable to select an equal number of samples per program point.
 *
 * <p>The performance is the same as running a set of RandomSelector Objects, one for each bucket,
 * plus some overhead for determining which bucket to assign to each Object in the iteration.
 *
 * <p>To use this class, call {@link #accept} on every Object in the iteration to be sampled. Then,
 * call {@link #valuesIter} to receive an iteration of all the values selected by the random
 * selection.
 *
 * @param <T> the type of elements to be selected among
 * @see RandomSelector
 */
public class MultiRandSelector<T extends @Nullable Object> {

  /** Whether to toss a coin or select a given number of elements. */
  private boolean coinTossMode;

  /** Number of elements to select. -1 if coinTossMode==true. */
  private int numElts = -1;

  /** Likelihood to select each element. -1.0 if coinTossMode=false. */
  private double keepProbability = -1.0;

  /** The Random instance to use. Is not a seed. Gets side-effected. */
  private Random r;

  /** Partioner that determines how to partition the objects. */
  private Partitioner<T, T> eq;

  /** Maps from partition representatives to the RandomSelector to use on that partition. */
  private HashMap<T, RandomSelector<T>> map = new HashMap<>();

  /**
   * Create a MultiRandSelector that chooses {@code numElts} elements from each bucket.
   *
   * @param numElts the number of elements to select from each bucket
   * @param eq partioner that determines how to partition the objects
   */
  public MultiRandSelector(int numElts, Partitioner<T, T> eq) {
    this(numElts, new Random(), eq);
  }

  /**
   * Create a MultiRandSelector that chooses each element with probability {@code keepProbability}.
   *
   * @param keepProbability the likelihood to select each element
   * @param eq partioner that determines how to partition the objects
   */
  public MultiRandSelector(double keepProbability, Partitioner<T, T> eq) {
    this(keepProbability, new Random(), eq);
  }

  /**
   * Create a MultiRandSelector that chooses {@code numElts} from each partition, using the given
   * Random.
   *
   * @param numElts the number of elements to select from each bucket
   * @param r the Random instance to use for making random choices
   * @param eq partioner that determines how to partition the objects
   */
  public MultiRandSelector(int numElts, Random r, Partitioner<T, T> eq) {
    this(r, eq);
    this.coinTossMode = false;
    this.numElts = numElts;
  }

  /**
   * Create a MultiRandSelector that chooses each element with probability {@code keepProbability}.,
   * using the given Random.
   *
   * @param keepProbability likelihood to select each element
   * @param r the Random instance to use for making random choices
   * @param eq partioner that determines how to partition the objects
   */
  public MultiRandSelector(double keepProbability, Random r, Partitioner<T, T> eq) {
    this(r, eq);
    this.coinTossMode = true;
    this.keepProbability = keepProbability;
  }

  /**
   * Helper constructor to create a not-fully-initialized MultiRandSelector.
   *
   * @param r the Random instance to use for making random choices
   * @param eq partioner that determines how to partition the objects
   */
  private MultiRandSelector(Random r, Partitioner<T, T> eq) {
    this.r = r;
    this.eq = eq;
  }

  /**
   * Use all the iterator's elements in the pool to select from.
   *
   * @param iter contains elements that are added to the pool to select from
   */
  public void acceptIter(Iterator<T> iter) {
    while (iter.hasNext()) {
      accept(iter.next());
    }
  }

  /**
   * Use the given value as one of the objects in the pool to select from.
   *
   * @param next element that is added to the pool to select from
   */
  public void accept(T next) {
    T equivClass = eq.assignToBucket(next);
    if (equivClass == null) {
      return;
    }
    RandomSelector<T> delegation = map.get(equivClass);
    if (delegation == null) {
      delegation =
          (coinTossMode
              ? new RandomSelector<T>(keepProbability, r)
              : new RandomSelector<T>(numElts, r));
      map.put(equivClass, delegation);
    }
    delegation.accept(next);
  }

  // I assume this is only for testing?  Comment it out to see whether that causes a problem for
  // any client.
  // TODO: is there any reason not to simply return a copy?
  // NOT safe from concurrent modification.
  // private Map<T, RandomSelector<T>> values() {
  //   return map;
  // }

  /**
   * Returns an iterator of all objects selected.
   *
   * @return an iterator of all objects selected
   */
  public Iterator<T> valuesIter() {
    ArrayList<T> ret = new ArrayList<>();
    for (RandomSelector<T> rs : map.values()) {
      ret.addAll(rs.getValues());
    }
    return ret.iterator();
  }
}
