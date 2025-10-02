package org.plumelib.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringJoiner;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LengthOf;
import org.checkerframework.checker.index.qual.SameLen;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.ArrayLen;

// Implementation note:
// Randoop's main generator ({@link randoop.generation.ForwardGenerator ForwardGenerator})
// creates new sequences by concatenating existing sequences, thenappending a statement at the end.
// When profiling Randoop, we observed that naive concatenation took up a large portion of the
// tool's running time, and the component set (i.e. the set of stored sequences used to create more
// sequences) quickly exhausted the memory available.

/**
 * An immutable list. Different lists may share structure, making the representation space-efficient
 * and making construction time-efficient. Use this only if you will be creating many lists that
 * share structure. Examples are when one list is the concatenation of other lists, or one list is
 * just like another with a single element added.
 *
 * <p>"SIList" stands for "Shared Immutable List".
 *
 * @param <E> the type of elements of the list
 */
public abstract class SIList<E> implements Iterable<E>, Serializable {

  /** Serial version UID. */
  static final long serialVersionUID = 20250617;

  // **************** producers ****************

  /** Creates a SIList. */
  private SIList() {}

  /**
   * Create a SIList from a JDK Collection.
   *
   * @param <E2> the type of list elements
   * @param list the elements of the new list
   * @return the new list
   * @deprecated use {@link #from(Collection)}
   */
  @Deprecated // 2025-08-30
  public static <E2> SIList<E2> fromList(Collection<E2> list) {
    int size = list.size();
    if (size == 0) {
      return empty();
    } else if (size == 1) {
      return singleton(list.iterator().next());
    } else {
      return new SimpleArrayList<>(list);
    }
  }

  /**
   * Create a SIList from a JDK Collection.
   *
   * @param <E2> the type of list elements
   * @param c the elements of the new list
   * @return the new list
   */
  public static <E2> SIList<E2> from(Collection<E2> c) {
    if (c.isEmpty()) {
      return empty();
    } else if (c.size() == 1) {
      return singleton(c.iterator().next());
    } else {
      return new SimpleArrayList<>(c);
    }
  }

  /**
   * Create a SIList from a JDK Iterable.
   *
   * @param <E2> the type of list elements
   * @param ible the elements of the new list
   * @return the new list
   */
  public static <E2> SIList<E2> from(Iterable<E2> ible) {
    return from(ible.iterator());
  }

  /**
   * Create a SIList from a JDK Iterator.
   *
   * @param <E2> the type of list elements
   * @param itor the elements of the new list
   * @return the new list
   */
  public static <E2> SIList<E2> from(Iterator<E2> itor) {
    if (!itor.hasNext()) {
      return empty();
    }
    List<E2> list = new ArrayList<>();
    while (itor.hasNext()) {
      list.add(itor.next());
    }
    return from(list);
  }

  /**
   * Returns an empty list.
   *
   * @param <E2> the type of elements of the list
   * @return an empty list
   */
  @SuppressWarnings("unchecked")
  public static <E2> SIList<E2> empty() {
    return SimpleEmptyList.it;
  }

  /**
   * Returns a new list containing one element.
   *
   * @param <E2> the type of elements of the list
   * @param elt the element
   * @return a new list containing one element
   */
  public static <E2> SIList<E2> singleton(E2 elt) {
    return new SingletonList<>(elt);
  }

  /**
   * Returns a new list containing zero or one element.
   *
   * @param <E2> the type of elements of the list
   * @param elt the element
   * @return a new list containing the element if it is non-null; if the element is null, returns an
   *     empty list
   */
  public static <E2> SIList<E2> singletonOrEmpty(@Nullable E2 elt) {
    if (elt == null) {
      return empty();
    } else {
      return singleton(elt);
    }
  }

  /**
   * Returns a new list that consists of this one plus one more element. Does not modify this
   * object.
   *
   * @param element the additional element
   * @return a new list that consists of this one plus one more element
   */
  public SIList<E> add(E element) {
    return new OneMoreElementList<>(this, element);
  }

  /**
   * Concatenate an array of SILists.
   *
   * @param <E2> the type of list elements
   * @param lists the lists that will compose the newly-created ListOfLists
   * @return the concatenated list
   */
  @SuppressWarnings("unchecked") // heap pollution warning
  public static <E2> SIList<E2> concat(SIList<E2>... lists) {
    List<SIList<E2>> withoutEmpty = new ArrayList<>(lists.length);
    for (SIList<E2> sl : lists) {
      if (!sl.isEmpty()) {
        withoutEmpty.add(sl);
      }
    }
    return concatNonEmpty(withoutEmpty);
  }

  /**
   * Create a SIList from a list of SILists.
   *
   * @param <E2> the type of list elements
   * @param lists the lists that will compose the newly-created ListOfLists
   * @return the concatenated list
   */
  public static <E2> SIList<E2> concat(Iterable<SIList<E2>> lists) {
    List<SIList<E2>> withoutEmpty = new ArrayList<>();
    for (SIList<E2> sl : lists) {
      if (!sl.isEmpty()) {
        withoutEmpty.add(sl);
      }
    }
    return concatNonEmpty(withoutEmpty);
  }

  /**
   * Create a SIList from a list of SILists, none of which is empty.
   *
   * @param <E2> the type of list elements
   * @param lists the non-empty lists that will compose the newly-created ListOfLists
   * @return the concatenated list
   */
  private static <E2> SIList<E2> concatNonEmpty(List<SIList<E2>> lists) {
    int numLists = lists.size();
    if (numLists == 0) {
      return empty();
    } else if (numLists == 1) {
      return lists.get(0);
    } else if (numLists == 2) {
      SIList<E2> list2 = lists.get(1);
      if (list2.size() == 1) {
        @SuppressWarnings("index:argument") // SIList.size() is not recognized by the Index Checker
        E2 list2elt = list2.get(0);
        return lists.get(0).add(list2elt);
      }
    }
    return new ListOfLists<>(new ArrayList<>(lists));
  }

  // **************** accessors ****************

  /**
   * Returns the number of elements in this list.
   *
   * @return the number of elements in this list
   */
  public abstract @LengthOf("this") int size();

  /**
   * Returns true if this list is empty.
   *
   * @return true if this list is empty, false otherwise
   */
  public abstract boolean isEmpty();

  /**
   * Returns the element at the given position of this list.
   *
   * @param index a position in the list
   * @return the element at the index
   */
  public abstract E get(@IndexFor("this") int index);

  /**
   * Returns a view of the portion of this list between the specified fromIndex, inclusive, and
   * toIndex, exclusive.
   *
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @return a view of part of this list
   */
  public SIList<E> subList(@IndexFor("this") int fromIndex, @IndexOrHigh("this") int toIndex) {
    checkRange(fromIndex, toIndex);
    if (fromIndex == toIndex) {
      return empty();
    } else if (toIndex == fromIndex + 1) {
      return singleton(get(fromIndex));
    } else {
      // TODO: ListOfLists and OneMoreElementList can sometimes do better than this.
      return new SimpleSubList<>(this, fromIndex, toIndex);
    }
  }

  // The result is always an existing SIList, the smallest one that contains the index.
  /**
   * Returns an arbitrary sublist of this list that contains the index. The result does not
   * necessarily contain the first or last element of this.
   *
   * @param index the index into this list
   * @return a sublist containing this index
   */
  public abstract SIList<E> getSublistContaining(@IndexFor("this") int index);

  @Override
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call",
    "lock:method.guarantee.violated"
  }) // side effect to local state
  public String toString(@GuardSatisfied SIList<E> this) {
    StringJoiner sj = new StringJoiner(", ", "SI[", "]");
    for (E elt : this) {
      sj.add(Objects.toString(elt));
    }
    return sj.toString();
  }

  // **************** diagnostics ****************

  /**
   * Throws an exception if the index is not valid for this.
   *
   * @param index an index into this
   */
  // Can't write this @EnsuresQualifier because the @IndexFor needs an argument of "this".
  // @EnsuresQualifier(expression="#1", qualifier=IndexFor.class)
  /*package-private*/ final void checkIndex(int index) {
    if (index < 0 || index >= size()) {
      throw new IllegalArgumentException(
          String.format("Bad index %d for list of length %d: %s", index, size(), this));
    }
  }

  /**
   * Throws an exception if the range is not valid for this.
   *
   * @param fromIndex low endpoint (inclusive) of the range
   * @param toIndex high endpoint (exclusive) of the range
   */
  /*package-private*/ final void checkRange(int fromIndex, int toIndex) {
    if (fromIndex < 0 || fromIndex > toIndex || toIndex > size()) {
      throw new IllegalArgumentException(
          String.format(
              "Bad range (%d,%d) for list of length %d: %s", fromIndex, toIndex, size(), this));
    }
  }

  /**
   * An immutable empty list.
   *
   * @param <E> the type of elements of the list
   */
  private static final @ArrayLen(0) class SimpleEmptyList<E> extends SIList<E>
      implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 20250617;

    /** The unique empty list. */
    @SuppressWarnings("rawtypes")
    public static SIList it = new SimpleEmptyList<>();

    /** Creates a new empty list. */
    @SuppressWarnings("value") // class annotation cannot be verified in constructor
    private SimpleEmptyList() {}

    @Override
    public @LengthOf("this") int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public E get(int index) {
      checkIndex(index);
      throw new Error("This can't happen.");
    }

    @Override
    public SIList<E> subList(int fromIndex, int toIndex) {
      checkRange(fromIndex, toIndex);
      return this;
    }

    @Override
    public SIList<E> getSublistContaining(int index) {
      throw new IndexOutOfBoundsException("index " + index + " for empty list");
    }

    @Override
    public Iterator<E> iterator() {
      return Collections.emptyIterator();
    }
  }

  /**
   * An immutable list containing one element.
   *
   * @param <E> the type of the list elements
   */
  private static final @ArrayLen(1) class SingletonList<E> extends SIList<E> {

    /** serialVersionUID */
    private static final long serialVersionUID = 20250719;

    /** The element of the list. */
    @SuppressWarnings("serial")
    private E element;

    /**
     * Creates a singleton list.
     *
     * @param element the list's element
     */
    @SuppressWarnings("value") // class annotation cannot be verified in constructor
    SingletonList(E element) {
      this.element = element;
    }

    @Override
    public @LengthOf("this") int size() {
      return 1;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public E get(int index) {
      checkIndex(index);
      if (index == 0) {
        return element;
      } else {
        throw new Error("This can't happen");
      }
    }

    @Override
    public SIList<E> getSublistContaining(int index) {
      return this;
    }

    @Override
    public Iterator<E> iterator() {
      return Collections.singleton(element).iterator();
    }
  }

  /**
   * A list that consists of a given list, plus one more element.
   *
   * @param <E> the type of elements of the list
   */
  private static final class OneMoreElementList<E> extends SIList<E> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 1332963552183905833L;

    /** All but the last element in this. */
    private final @SameLen("this") SIList<E> list;

    /** The last element in this. */
    @SuppressWarnings("serial")
    private final E lastElement;

    /** The size of this. */
    private final @LengthOf({"this", "list"}) int size;

    /**
     * Creates a OneMoreElementList.
     *
     * @param list the list to extend; it is not side-effected
     * @param extraElement the additional element
     */
    @SuppressWarnings("index") // constructor creates object of size @SameLen(this) by definition
    public OneMoreElementList(SIList<E> list, E extraElement) {
      this.list = list;
      this.lastElement = extraElement;
      this.size = list.size() + 1;
    }

    @Override
    public @LengthOf("this") int size() {
      return size;
    }

    @Override
    public boolean isEmpty() {
      return size == 0;
    }

    @Override
    public E get(@IndexFor("this") int index) {
      checkIndex(index);
      if (index < size - 1) {
        return list.get(index);
      } else if (index == size - 1) {
        return lastElement;
      } else {
        throw new Error("This can't happen.");
      }
    }

    @Override
    public Iterator<E> iterator() {
      return CollectionsPlume.iteratorPlusOne(list.iterator(), lastElement);
    }

    @Override
    public SIList<E> getSublistContaining(@IndexFor("this") int index) {
      checkIndex(index);
      if (index < size - 1) {
        return list.getSublistContaining(index);
      }
      if (index == size - 1) {
        return this;
      }
      throw new Error("This can't happen.");
    }
  }

  /**
   * A SIList backed by an ArrayList.
   *
   * @param <E> the type of elements of the list
   */
  private static final class SimpleArrayList<E> extends SIList<E> implements Serializable {

    /** serialVersionUID. */
    private static final long serialVersionUID = 20250617;

    // TODO: use an array instead, for efficiency?
    /** The backing storage. */
    private final @SameLen("this") ArrayList<E> delegate;

    /**
     * Creates a new SimpleArrayList containing the given elements.
     *
     * @param c the elements of the list
     */
    @SuppressWarnings("index") // constructor creates object of size @SameLen(this) by definition
    public SimpleArrayList(Collection<? extends E> c) {
      delegate = new ArrayList<>(c);
    }

    @Override
    public boolean isEmpty() {
      return delegate.isEmpty();
    }

    @Override
    public E get(@IndexFor("this") int index) {
      return delegate.get(index);
    }

    @Override
    @SuppressWarnings("index:return") // CF bug? I expected this to type-check.
    public @LengthOf("this") int size() {
      return delegate.size();
    }

    @Override
    public Iterator<E> iterator() {
      return delegate.iterator();
    }

    @Override
    public SIList<E> getSublistContaining(int index) {
      return this;
    }
  }

  /**
   * Given a list of lists, defines methods that can access all the elements as if they were part of
   * a single list, without copying any list contents.
   *
   * @param <E> the type of elements of the list
   */
  private static final class ListOfLists<E> extends SIList<E> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = -3307714585442970263L;

    /** The lists themselves. */
    // TODO: use an array for efficiency, just as `cumulativeSize` is.
    private final SIList<E> @SameLen("cumulativeSize") [] lists;

    /** The i-th value is the number of elements in the sublists up to the i-th one, inclusive. */
    private int @SameLen("lists") [] cumulativeSize;

    /** The size of this collection. */
    private @LengthOf("this") int size;

    /**
     * Create a ListOfLists from a list of SILists.
     *
     * @param lists the lists that will compose the newly-created ListOfLists
     */
    @SuppressWarnings("index") // constructor creates object of size @SameLen(this) by definition
    ListOfLists(List<SIList<E>> lists) {
      // TODO: have a variant that doesn't make a copy?
      @LengthOf({"lists", "this.lists"}) int numLists = lists.size();
      @SuppressWarnings({
        "rawtypes",
        "unchecked",
        "nullness:assignment",
        "nullness:toarray.nullable.elements.not.newarray" // bug in CF: doesn't permit cast
      })
      @NonNull @SameLen({"lists", "this.lists"}) SIList<E>[] tmpLists =
          lists.toArray((@SameLen({"lists", "this.lists"}) SIList<E>[]) new SIList[numLists]);
      this.lists = tmpLists;
      this.cumulativeSize = new int[numLists];
      this.size = 0;
      for (int i = 0; i < numLists; i++) {
        SIList<E> l = this.lists[i];
        size += l.size();
        cumulativeSize[i] = size;
      }
    }

    @Override
    public @LengthOf("this") int size() {
      return size;
    }

    @Override
    public boolean isEmpty() {
      return size == 0;
    }

    @Override
    public E get(int index) {
      checkIndex(index);
      int previousListSize = 0;
      @LengthOf({"cumulativeSize", "lists"}) int numLists = cumulativeSize.length;
      for (int i = 0; i < numLists; i++) {
        if (index < cumulativeSize[i]) {
          @SuppressWarnings("index:argument") // index arithmetic
          E result = lists[i].get(index - previousListSize);
          return result;
        }
        previousListSize = cumulativeSize[i];
      }
      throw new Error("This can't happen.");
    }

    @Override
    public SIList<E> getSublistContaining(int index) {
      checkIndex(index);
      int previousListSize = 0;
      for (int i = 0; i < cumulativeSize.length; i++) {
        if (index < cumulativeSize[i]) {
          @SuppressWarnings("index:argument") // index arithmetic
          SIList<E> result = lists[i].getSublistContaining(index - previousListSize);
          return result;
        }
        previousListSize = cumulativeSize[i];
      }
      throw new Error("This can't happen.");
    }

    @Override
    public Iterator<E> iterator() {
      List<Iterator<E>> itors = CollectionsPlume.mapList(SIList::iterator, lists);
      return CollectionsPlume.mergedIterator(itors.iterator());
    }
  }

  /**
   * A view of part of a SIList.
   *
   * @param <E> the type of elements of the list
   */
  @SuppressWarnings("index") // index arithmetic; assignment `this.delegate = delegate;`
  private static final class SimpleSubList<E> extends SIList<E> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 20250617;

    // TODO: use an array instead, for efficiency?
    /** The backing storage. */
    private final SIList<E> delegate;

    /** The index in `delegate` of the first element in this list. */
    private final @IndexFor("delegate") int fromIndex;

    /** The index in `delegate` of one past the last element in this list. */
    private final @IndexOrHigh("delegate") int toIndex;

    /**
     * Creates a sublist spanning the given range.
     *
     * @param delegate the list whose sublist to return
     * @param fromIndex the index in `delegate` of the first element in this list
     * @param toIndex the index in `delegate` of one past the last element in this list
     */
    SimpleSubList(
        SIList<E> delegate, @IndexFor("#1") int fromIndex, @IndexOrHigh("#1") int toIndex) {
      this.delegate = delegate;
      this.fromIndex = fromIndex;
      this.toIndex = toIndex;
      checkRange(fromIndex, toIndex);
    }

    @Override
    public int size() {
      return toIndex - fromIndex;
    }

    @Override
    public boolean isEmpty() {
      return toIndex == fromIndex;
    }

    @Override
    public E get(int index) {
      return delegate.get(fromIndex + index);
    }

    @Override
    public SIList<E> getSublistContaining(int index) {
      return this;
    }

    @Override
    public Iterator<E> iterator() {
      return new SimpleSubListIterator();
    }

    // Two possible implementation strategies:
    //  1. Create an iterator for `delegate`, discard `fromIndex` elements of it, and fail after
    // getting to `toIndex`.
    //  2. Use `get` to obtain the elements one by one, from `fromIndex` to `toIndex`.
    // I'm not sure which approach is more efficient.
    /** An iterator over a SimpleSubList. */
    private class SimpleSubListIterator implements Iterator<E> {

      /** The index of the next element to return. */
      int index = fromIndex;

      /** Creates a new SimpleSubListIterator. */
      public SimpleSubListIterator() {}

      @Override
      public boolean hasNext(@GuardSatisfied SimpleSubListIterator this) {
        return index < toIndex;
      }

      @Override
      public E next(@GuardSatisfied SimpleSubListIterator this) {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return delegate.get(index++);
      }
    }
  }
}
