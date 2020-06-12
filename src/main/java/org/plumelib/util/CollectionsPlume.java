// If you edit this file, you must also edit its tests.
// For tests of this and the entire plume package, see class TestPlume.

package org.plumelib.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/** Utility functions for Collections, ArrayList, Iterator, and Map. */
public final class CollectionsPlume {

  /** This class is a collection of methods; it does not represent anything. */
  private CollectionsPlume() {
    throw new Error("do not instantiate");
  }

  /** The system-specific line separator string. */
  private static final String lineSep = System.lineSeparator();

  ///////////////////////////////////////////////////////////////////////////
  /// Collections
  ///

  /**
   * Return the sorted version of the list. Does not alter the list. Simply calls {@code
   * Collections.sort(List<T>, Comparator<? super T>)}.
   *
   * @return a sorted version of the list
   * @param <T> type of elements of the list
   * @param l a list to sort
   * @param c a sorted version of the list
   */
  public static <T> List<T> sortList(List<T> l, Comparator<? super T> c) {
    List<T> result = new ArrayList<>(l);
    Collections.sort(result, c);
    return result;
  }

  // This should perhaps be named withoutDuplicates to emphasize that
  // it does not side-effect its argument.
  /**
   * Return a copy of the list with duplicates removed. Retains the original order.
   *
   * @param <T> type of elements of the list
   * @param l a list to remove duplicates from
   * @return a copy of the list with duplicates removed
   */
  public static <T> List<T> removeDuplicates(List<T> l) {
    HashSet<T> hs = new LinkedHashSet<>(l);
    List<T> result = new ArrayList<>(hs);
    return result;
  }

  /** All calls to deepEquals that are currently underway. */
  private static HashSet<WeakIdentityPair<Object, Object>> deepEqualsUnderway =
      new HashSet<WeakIdentityPair<Object, Object>>();

  /**
   * Determines deep equality for the elements.
   *
   * <ul>
   *   <li>If both are primitive arrays, uses java.util.Arrays.equals.
   *   <li>If both are Object[], uses java.util.Arrays.deepEquals and does not recursively call this
   *       method.
   *   <li>If both are lists, uses deepEquals recursively on each element.
   *   <li>For other types, just uses equals() and does not recursively call this method.
   * </ul>
   *
   * @param o1 first value to compare
   * @param o2 second value to compare
   * @return true iff o1 and o2 are deeply equal
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to static field deepEqualsUnderway
  @Pure
  public static boolean deepEquals(@Nullable Object o1, @Nullable Object o2) {
    @SuppressWarnings("interning")
    boolean sameObject = (o1 == o2);
    if (sameObject) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }

    if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
      return Arrays.equals((boolean[]) o1, (boolean[]) o2);
    }
    if (o1 instanceof byte[] && o2 instanceof byte[]) {
      return Arrays.equals((byte[]) o1, (byte[]) o2);
    }
    if (o1 instanceof char[] && o2 instanceof char[]) {
      return Arrays.equals((char[]) o1, (char[]) o2);
    }
    if (o1 instanceof double[] && o2 instanceof double[]) {
      return Arrays.equals((double[]) o1, (double[]) o2);
    }
    if (o1 instanceof float[] && o2 instanceof float[]) {
      return Arrays.equals((float[]) o1, (float[]) o2);
    }
    if (o1 instanceof int[] && o2 instanceof int[]) {
      return Arrays.equals((int[]) o1, (int[]) o2);
    }
    if (o1 instanceof long[] && o2 instanceof long[]) {
      return Arrays.equals((long[]) o1, (long[]) o2);
    }
    if (o1 instanceof short[] && o2 instanceof short[]) {
      return Arrays.equals((short[]) o1, (short[]) o2);
    }

    @SuppressWarnings({"allcheckers:purity", "lock"}) // creates local state
    WeakIdentityPair<Object, Object> mypair = new WeakIdentityPair<>(o1, o2);
    if (deepEqualsUnderway.contains(mypair)) {
      return true;
    }

    if (o1 instanceof Object[] && o2 instanceof Object[]) {
      return Arrays.deepEquals((Object[]) o1, (Object[]) o2);
    }

    if (o1 instanceof List<?> && o2 instanceof List<?>) {
      List<?> l1 = (List<?>) o1;
      List<?> l2 = (List<?>) o2;
      if (l1.size() != l2.size()) {
        return false;
      }
      try {
        deepEqualsUnderway.add(mypair);
        for (int i = 0; i < l1.size(); i++) {
          Object e1 = l1.get(i);
          Object e2 = l2.get(i);
          if (!deepEquals(e1, e2)) {
            return false;
          }
        }
      } finally {
        deepEqualsUnderway.remove(mypair);
      }

      return true;
    }

    return o1.equals(o2);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// ArrayList
  ///

  /**
   * Returns a vector containing the elements of the enumeration.
   *
   * @param <T> type of the enumeration and vector elements
   * @param e an enumeration to convert to a ArrayList
   * @return a vector containing the elements of the enumeration
   */
  @SuppressWarnings("JdkObsolete")
  public static <T> ArrayList<T> makeArrayList(Enumeration<T> e) {
    ArrayList<T> result = new ArrayList<>();
    while (e.hasMoreElements()) {
      result.add(e.nextElement());
    }
    return result;
  }

  // Rather than writing something like ArrayListToStringArray, use
  //   v.toArray(new String[0])

  // Helper method
  /**
   * Compute (n choose k), which is (n! / (k!(n-k)!)).
   *
   * @param n number of elements from which to choose
   * @param k number of elements to choose
   * @return n choose k, or Long.MAX_VALUE if the value would overflow
   */
  private static long choose(int n, int k) {
    // From https://stackoverflow.com/questions/2201113/combinatoric-n-choose-r-in-java-math
    if (n < k) {
      return 0;
    }
    if (k == 0 || k == n) {
      return 1;
    }
    long a = choose(n - 1, k - 1);
    long b = choose(n - 1, k);
    if (a < 0 || a == Long.MAX_VALUE || b < 0 || b == Long.MAX_VALUE || a + b < 0) {
      return Long.MAX_VALUE;
    } else {
      return a + b;
    }
  }

  /**
   * Returns a list of lists of each combination (with repetition, but not permutations) of the
   * specified objects starting at index {@code start} over {@code dims} dimensions, for {@code dims
   * > 0}.
   *
   * <p>For example, createCombinations(1, 0, {a, b, c}) returns a 3-element list of singleton
   * lists:
   *
   * <pre>
   *    {a}, {b}, {c}
   * </pre>
   *
   * And createCombinations(2, 0, {a, b, c}) returns a 6-element list of 2-element lists:
   *
   * <pre>
   *    {a, a}, {a, b}, {a, c}
   *    {b, b}, {b, c},
   *    {c, c}
   * </pre>
   *
   * @param <T> type of the input list elements, and type of the innermost output list elements
   * @param dims number of dimensions: that is, size of each innermost list
   * @param start initial index
   * @param objs list of elements to create combinations of
   * @return list of lists of length dims, each of which combines elements from objs
   */
  public static <T> List<List<T>> createCombinations(
      @Positive int dims, @NonNegative int start, List<T> objs) {

    if (dims < 1) {
      throw new IllegalArgumentException();
    }

    long numResults = choose(objs.size() + dims - 1, dims);
    if (numResults > 100000000) {
      throw new Error("Do you really want to create more than 100 million lists?");
    }

    List<List<T>> results = new ArrayList<List<T>>();

    for (int i = start; i < objs.size(); i++) {
      if (dims == 1) {
        List<T> simple = new ArrayList<>();
        simple.add(objs.get(i));
        results.add(simple);
      } else {
        List<List<T>> combos = createCombinations(dims - 1, i, objs);
        for (List<T> lt : combos) {
          List<T> simple = new ArrayList<>();
          simple.add(objs.get(i));
          simple.addAll(lt);
          results.add(simple);
        }
      }
    }

    return (results);
  }

  /**
   * Returns a list of lists of each combination (with repetition, but not permutations) of integers
   * from start to cnt (inclusive) over arity dimensions.
   *
   * <p>For example, createCombinations(1, 0, 2) returns a 3-element list of singleton lists:
   *
   * <pre>
   *    {0}, {1}, {2}
   * </pre>
   *
   * And createCombinations(2, 10, 2) returns a 6-element list of 2-element lists:
   *
   * <pre>
   *    {10, 10}, {10, 11}, {10, 12}, {11, 11}, {11, 12}, {12, 12}
   * </pre>
   *
   * The length of the list is (cnt multichoose arity), which is ((cnt + arity - 1) choose arity).
   *
   * @param arity size of each innermost list
   * @param start initial value
   * @param cnt maximum element value
   * @return list of lists of length arity, each of which combines integers from start to cnt
   */
  public static ArrayList<ArrayList<Integer>> createCombinations(
      int arity, @NonNegative int start, int cnt) {

    long numResults = choose(cnt + arity - 1, arity);
    if (numResults > 100000000) {
      throw new Error("Do you really want to create more than 100 million lists?");
    }

    ArrayList<ArrayList<Integer>> results = new ArrayList<>();

    // Return a list with one zero length element if arity is zero
    if (arity == 0) {
      results.add(new ArrayList<Integer>());
      return (results);
    }

    for (int i = start; i <= cnt; i++) {
      ArrayList<ArrayList<Integer>> combos = createCombinations(arity - 1, i, cnt);
      for (ArrayList<Integer> li : combos) {
        ArrayList<Integer> simple = new ArrayList<>();
        simple.add(i);
        simple.addAll(li);
        results.add(simple);
      }
    }

    return (results);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Iterator
  ///

  /**
   * Converts an Iterator to an Iterable. The resulting Iterable can be used to produce a single,
   * working Iterator (the one that was passed in). Subsequent calls to its iterator() method will
   * fail, because otherwise they would return the same Iterator instance, which may have been
   * exhausted, or otherwise be in some indeterminate state. Calling iteratorToIterable twice on the
   * same argument can have similar problems, so don't do that.
   *
   * @param source the Iterator to be converted to Iterable
   * @param <T> the element type
   * @return source, converted to Iterable
   */
  public static <T> Iterable<T> iteratorToIterable(final Iterator<T> source) {
    if (source == null) {
      throw new NullPointerException();
    }
    return new Iterable<T>() {
      /** True if this Iterable object has been used. */
      private AtomicBoolean used = new AtomicBoolean();

      @Override
      public Iterator<T> iterator() {
        if (used.getAndSet(true)) {
          throw new Error("Call iterator() just once");
        }
        return source;
      }
    };
  }

  // Making these classes into functions didn't work because I couldn't get
  // their arguments into a scope that Java was happy with.

  /** Converts an Enumeration into an Iterator. */
  public static final class EnumerationIterator<T> implements Iterator<T> {
    /** The enumeration that this object wraps. */
    Enumeration<T> e;

    /**
     * Create an Iterator that yields the elements of the given Enumeration.
     *
     * @param e the Enumeration to make into an Iterator
     */
    public EnumerationIterator(Enumeration<T> e) {
      this.e = e;
    }

    @SuppressWarnings("JdkObsolete")
    @Override
    public boolean hasNext(@GuardSatisfied EnumerationIterator<T> this) {
      return e.hasMoreElements();
    }

    @SuppressWarnings("JdkObsolete")
    @Override
    public T next(@GuardSatisfied EnumerationIterator<T> this) {
      return e.nextElement();
    }

    @Override
    public void remove(@GuardSatisfied EnumerationIterator<T> this) {
      throw new UnsupportedOperationException();
    }
  }

  /** Converts an Iterator into an Enumeration. */
  @SuppressWarnings("JdkObsolete")
  public static final class IteratorEnumeration<T> implements Enumeration<T> {
    /** The iterator that this object wraps. */
    Iterator<T> itor;

    /**
     * Create an Enumeration that contains the elements returned by the given Iterator.
     *
     * @param itor the Iterator to make an Enumeration from
     */
    public IteratorEnumeration(Iterator<T> itor) {
      this.itor = itor;
    }

    @Override
    public boolean hasMoreElements() {
      return itor.hasNext();
    }

    @Override
    public T nextElement() {
      return itor.next();
    }
  }

  // This must already be implemented someplace else.  Right??
  /**
   * An Iterator that returns first the elements returned by its first argument, then the elements
   * returned by its second argument. Like {@link MergedIterator}, but specialized for the case of
   * two arguments.
   */
  public static final class MergedIterator2<T> implements Iterator<T> {
    /** The first of the two iterators that this object merges. */
    Iterator<T> itor1;
    /** The second of the two iterators that this object merges. */
    Iterator<T> itor2;

    /**
     * Create an iterator that returns the elements of {@code itor1} then those of {@code itor2}.
     *
     * @param itor1 an Iterator
     * @param itor2 another Iterator
     */
    public MergedIterator2(Iterator<T> itor1, Iterator<T> itor2) {
      this.itor1 = itor1;
      this.itor2 = itor2;
    }

    @Override
    public boolean hasNext(@GuardSatisfied MergedIterator2<T> this) {
      return (itor1.hasNext() || itor2.hasNext());
    }

    @Override
    public T next(@GuardSatisfied MergedIterator2<T> this) {
      if (itor1.hasNext()) {
        return itor1.next();
      } else if (itor2.hasNext()) {
        return itor2.next();
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove(@GuardSatisfied MergedIterator2<T> this) {
      throw new UnsupportedOperationException();
    }
  }

  // This must already be implemented someplace else.  Right??
  /**
   * An Iterator that returns the elements in each of its argument Iterators, in turn. The argument
   * is an Iterator of Iterators. Like {@link MergedIterator2}, but generalized to arbitrary number
   * of iterators.
   */
  public static final class MergedIterator<T> implements Iterator<T> {
    /** The iterators that this object merges. */
    Iterator<Iterator<T>> itorOfItors;

    /**
     * Create an iterator that returns the elements of the given iterators, in turn.
     *
     * @param itorOfItors an iterator whose elements are iterators; this MergedIterator will merge
     *     them all
     */
    public MergedIterator(Iterator<Iterator<T>> itorOfItors) {
      this.itorOfItors = itorOfItors;
    }

    /** The current iterator (from {@link #itorOfItors}) that is being iterated over. */
    // Initialize to an empty iterator to prime the pump.
    Iterator<T> current = new ArrayList<T>().iterator();

    @SuppressWarnings({"allcheckers:purity", "lock:method.guarantee.violated"})
    @Override
    public boolean hasNext(@GuardSatisfied MergedIterator<T> this) {
      while (!current.hasNext() && itorOfItors.hasNext()) {
        current = itorOfItors.next();
      }
      return current.hasNext();
    }

    @Override
    public T next(@GuardSatisfied MergedIterator<T> this) {
      hasNext(); // for side effect
      return current.next();
    }

    @Override
    public void remove(@GuardSatisfied MergedIterator<T> this) {
      throw new UnsupportedOperationException();
    }
  }

  /** An iterator that only returns elements that match the given Filter. */
  public static final class FilteredIterator<T> implements Iterator<T> {
    /** The iterator that this object is filtering. */
    Iterator<T> itor;
    /** The predicate that determines which elements to retain. */
    Filter<T> filter;

    /**
     * Create an iterator that only returns elements of {@code itor} that match the given Filter.
     *
     * @param itor the Iterator to filter
     * @param filter the predicate that determines which elements to retain
     */
    public FilteredIterator(Iterator<T> itor, Filter<T> filter) {
      this.itor = itor;
      this.filter = filter;
    }

    /** A marker object, distinct from any object that the iterator can return. */
    @SuppressWarnings("unchecked")
    T invalidT = (T) new Object();

    /**
     * The next object that this iterator will yield, or {@link #invalidT} if {@link #currentValid}
     * is false.
     */
    T current = invalidT;
    /** True iff {@link #current} is an object from the wrapped iterator. */
    boolean currentValid = false;

    @SuppressWarnings({"allcheckers:purity", "lock:method.guarantee.violated"}) // benevolent side effects
    @Override
    public boolean hasNext(@GuardSatisfied FilteredIterator<T> this) {
      while (!currentValid && itor.hasNext()) {
        current = itor.next();
        currentValid = filter.accept(current);
      }
      return currentValid;
    }

    @Override
    public T next(@GuardSatisfied FilteredIterator<T> this) {
      if (hasNext()) {
        currentValid = false;
        @SuppressWarnings("interning")
        boolean ok = (current != invalidT);
        assert ok;
        return current;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove(@GuardSatisfied FilteredIterator<T> this) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Returns an iterator just like its argument, except that the first and last elements are
   * removed. They can be accessed via the {@link #getFirst} and {@link #getLast} methods.
   */
  public static final class RemoveFirstAndLastIterator<T> implements Iterator<T> {
    /** The wrapped iterator. */
    Iterator<T> itor;
    /** A marker object, distinct from any object that the iterator can return. */
    @SuppressWarnings("unchecked")
    T nothing = (T) new Object();
    // I don't think this works, because the iterator might itself return null
    // @Nullable T nothing = (@Nullable T) null;

    /** The first object yielded by the wrapped iterator. */
    T first = nothing;
    /** The next object that this iterator will return. */
    T current = nothing;

    /**
     * Create an iterator just like {@code itor}, except without its first and last elements.
     *
     * @param itor an itorator whose first and last elements to discard
     */
    public RemoveFirstAndLastIterator(Iterator<T> itor) {
      this.itor = itor;
      if (itor.hasNext()) {
        first = itor.next();
      }
      if (itor.hasNext()) {
        current = itor.next();
      }
    }

    @Override
    public boolean hasNext(@GuardSatisfied RemoveFirstAndLastIterator<T> this) {
      return itor.hasNext();
    }

    @Override
    public T next(@GuardSatisfied RemoveFirstAndLastIterator<T> this) {
      if (!itor.hasNext()) {
        throw new NoSuchElementException();
      }
      T tmp = current;
      current = itor.next();
      return tmp;
    }

    /**
     * Return the first element of the iterator that was used to construct this. This value is not
     * part of this iterator (unless the original iterator would have returned it multiple times).
     *
     * @return the first element of the iterator that was used to construct this
     */
    public T getFirst() {
      @SuppressWarnings("interning") // check for equality to a special value
      boolean invalid = (first == nothing);
      if (invalid) {
        throw new NoSuchElementException();
      }
      return first;
    }

    /**
     * Return the last element of the iterator that was used to construct this. This value is not
     * part of this iterator (unless the original iterator would have returned it multiple times).
     *
     * <p>Throws an error unless the RemoveFirstAndLastIterator has already been iterated all the
     * way to its end (so the delegate is pointing to the last element).
     *
     * @return the last element of the iterator that was used to construct this.
     */
    // TODO: This is buggy when the delegate is empty.
    public T getLast() {
      if (itor.hasNext()) {
        throw new Error();
      }
      return current;
    }

    @Override
    public void remove(@GuardSatisfied RemoveFirstAndLastIterator<T> this) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Return a List containing numElts randomly chosen elements from the iterator, or all the
   * elements of the iterator if there are fewer. It examines every element of the iterator, but
   * does not keep them all in memory.
   *
   * @param <T> type of the iterator elements
   * @param itor elements to be randomly selected from
   * @param numElts number of elements to select
   * @return list of numElts elements from itor
   */
  public static <T> List<T> randomElements(Iterator<T> itor, int numElts) {
    return randomElements(itor, numElts, r);
  }

  /** The random generator. */
  private static Random r = new Random();

  /**
   * Return a List containing numElts randomly chosen elements from the iterator, or all the
   * elements of the iterator if there are fewer. It examines every element of the iterator, but
   * does not keep them all in memory.
   *
   * @param <T> type of the iterator elements
   * @param itor elements to be randomly selected from
   * @param numElts number of elements to select
   * @param random the Random instance to use to make selections
   * @return list of numElts elements from itor
   */
  public static <T> List<T> randomElements(Iterator<T> itor, int numElts, Random random) {
    // The elements are chosen with the following probabilities,
    // where n == numElts:
    //   n n/2 n/3 n/4 n/5 ...

    RandomSelector<T> rs = new RandomSelector<>(numElts, random);

    while (itor.hasNext()) {
      rs.accept(itor.next());
    }
    return rs.getValues();

    /*
    ArrayList<T> result = new ArrayList<>(numElts);
    int i=1;
    for (int n=0; n<numElts && itor.hasNext(); n++, i++) {
      result.add(itor.next());
    }
    for (; itor.hasNext(); i++) {
      T o = itor.next();
      // test random < numElts/i
      if (random.nextDouble() * i < numElts) {
        // This element will replace one of the existing elements.
        result.set(random.nextInt(numElts), o);
      }
    }
    return result;

    */
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Map
  ///

  // In Python, inlining this gave a 10x speed improvement.
  // Will the same be true for Java?
  /**
   * Increment the Integer which is indexed by key in the Map. Set the value to 1 if not currently
   * mapped.
   *
   * @param <K> type of keys in the map
   * @param m map from K to Integer
   * @param key the key whose value will be incremented
   * @return the old value, before it was incremented; this might be null
   * @throws Error if the key is in the Map but maps to a non-Integer
   */
  public static <K extends @NonNull Object> @Nullable Integer incrementMap(
      Map<K, Integer> m, K key) {
    return incrementMap(m, key, 1);
  }

  /**
   * Increment the Integer which is indexed by key in the Map. Set the value to {@code count} if not
   * currently mapped.
   *
   * @param <K> type of keys in the map
   * @param m map from K to Integer
   * @param key the key whose value will be incremented
   * @param count how much to increment the value by
   * @return the old value, before it was incremented; this might be null
   * @throws Error if the key is in the Map but maps to a non-Integer
   */
  public static <K extends @NonNull Object> @Nullable Integer incrementMap(
      Map<K, Integer> m, K key, int count) {
    Integer old = m.get(key);
    Integer newTotal;
    if (old == null) {
      newTotal = count;
    } else {
      newTotal = old.intValue() + count;
    }
    return m.put(key, newTotal);
  }

  /**
   * Returns a multi-line string representation of a map.
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param m map to be converted to a string
   * @return a multi-line string representation of m
   */
  public static <K, V> String mapToString(Map<K, V> m) {
    StringBuilder sb = new StringBuilder();
    mapToString(sb, m, "");
    return sb.toString();
  }

  /**
   * Write a multi-line representation of the map into the given Appendable (e.g., a StringBuilder).
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param sb an Appendable (such as StringBuilder) to which to write a multi-line string
   *     representation of m
   * @param m map to be converted to a string
   * @param linePrefix prefix to write at the beginning of each line
   */
  public static <K, V> void mapToString(Appendable sb, Map<K, V> m, String linePrefix) {
    try {
      for (Map.Entry<K, V> entry : m.entrySet()) {
        sb.append(linePrefix);
        sb.append(Objects.toString(entry.getKey()));
        sb.append(" => ");
        sb.append(Objects.toString(entry.getValue()));
        sb.append(lineSep);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a sorted version of m.keySet().
   *
   * @param <K> type of the map keys
   * @param <V> type of the map values
   * @param m a map whose keyset will be sorted
   * @return a sorted version of m.keySet()
   */
  public static <K extends Comparable<? super K>, V> Collection<@KeyFor("#1") K> sortedKeySet(
      Map<K, V> m) {
    ArrayList<@KeyFor("#1") K> theKeys = new ArrayList<>(m.keySet());
    Collections.sort(theKeys);
    return theKeys;
  }

  /**
   * Returns a sorted version of m.keySet().
   *
   * @param <K> type of the map keys
   * @param <V> type of the map values
   * @param m a map whose keyset will be sorted
   * @param comparator the Comparator to use for sorting
   * @return a sorted version of m.keySet()
   */
  public static <K, V> Collection<@KeyFor("#1") K> sortedKeySet(
      Map<K, V> m, Comparator<K> comparator) {
    ArrayList<@KeyFor("#1") K> theKeys = new ArrayList<>(m.keySet());
    Collections.sort(theKeys, comparator);
    return theKeys;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Set
  ///

  /**
   * Return the object in this set that is equal to key. The Set abstraction doesn't provide this;
   * it only provides "contains". Returns null if the argument is null, or if it isn't in the set.
   *
   * @param set a set in which to look up the value
   * @param key the value to look up in the set
   * @return the object in this set that is equal to key, or null
   */
  public static @Nullable Object getFromSet(Set<?> set, Object key) {
    if (key == null) {
      return null;
    }
    for (Object elt : set) {
      if (key.equals(elt)) {
        return elt;
      }
    }
    return null;
  }
}
