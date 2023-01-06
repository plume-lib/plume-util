package org.plumelib.util;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTEqLengthOf;
import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A set backed by an array. It uses object identity (==) for comparison. It permits null values and
 * its iterator has deterministic ordering.
 *
 * <p>Compared to a set built on IdentityHashMap: For very small sets, this uses much less space,
 * has comparable performance, and (like a LinkedHashSet) is deterministic, with elements returned
 * in the order they were inserted. For large sets, this is significantly less performant than other
 * set implementations.
 *
 * @param <E> the type of the set elements
 */
@SuppressWarnings({
  "index", // TODO
  "keyfor", // https://tinyurl.com/cfissue/4558
  "lock", // not yet annotated for the Lock Checker
  "nullness" // temporary; nullness is tricky because of null-padded arrays
})
public class IdentityArraySet<E extends @UnknownSignedness Object> extends AbstractSet<E> {

  /** The values. Null if capacity=0. */
  private @Nullable E[] values;
  /** The number of used slots in the representation of this. */
  private @NonNegative @LessThan("values.length + 1") @IndexOrHigh({"values"}) int size = 0;
  // An alternate representation would also store the hash code of each key, for quicker querying.

  /**
   * The number of times this set's size has been modified by adding or removing an element. This
   * field is used to make view iterators fail-fast.
   */
  transient int sizeModificationCount = 0;

  // Constructors

  /**
   * Constructs an empty {@code IdentityArraySet} with the specified initial capacity.
   *
   * @param initialCapacity the initial capacity
   * @throws IllegalArgumentException if the initial capacity is negative
   */
  @SuppressWarnings({
    "unchecked", // generic array cast
    "samelen:assignment", // initialization
    "allcheckers:purity.not.sideeffectfree.assign.field", // initializes `this`
    "allcheckers:purity.not.sideeffectfree.call" // calls `super`
  })
  @SideEffectFree
  public IdentityArraySet(int initialCapacity) {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    if (initialCapacity == 0) {
      this.values = null;
    } else {
      this.values = (E[]) new Object[initialCapacity];
    }
  }

  /** Constructs an empty {@code IdentityArraySet} with the default initial capacity. */
  @SideEffectFree
  public IdentityArraySet() {
    this(4);
  }

  /**
   * Private constructor. Installs the given objects in this as its representation, without making
   * defensive copies.
   *
   * @param values the values
   * @param size the number of used items in the array; may be less than its length
   */
  @SuppressWarnings({
    "samelen:assignment", // initialization
    "allcheckers:purity.not.sideeffectfree.assign.field", // initializes `this`
    "allcheckers:purity.not.sideeffectfree.call" // calls `super`
  })
  @SideEffectFree
  private IdentityArraySet(E[] values, @LTEqLengthOf({"values"}) int size) {
    this.values = values;
    this.size = size;
  }

  /**
   * Constructs a new {@code IdentityArraySet} with the same elements as the given collection.
   *
   * @param m the collection whose elements are to be placed in the new set
   * @throws NullPointerException if the given set is null
   */
  @SuppressWarnings({
    "allcheckers:purity", // initializes `this`
    "lock:method.guarantee.violated", // initializes `this`
    "nullness:method.invocation", // inference failure;
    // https://github.com/typetools/checker-framework/issues/979 ?
  })
  @SideEffectFree
  public IdentityArraySet(Collection<? extends E> m) {
    this(m.size());
    addAll(m);
  }

  // Private helper functions

  /**
   * Adds an element to this set.
   *
   * @param index the index of {@code value} in {@code values}. If -1, add a new element. Otherwise,
   *     do nothing.
   * @param value the value
   * @return true if the method modified this set
   */
  @SuppressWarnings({"InvalidParam"}) // Error Prone stupidly warns about field `values`
  private boolean add(@GTENegativeOne int index, E value) {
    if (index == -1) {
      return false;
    }

    // Add a new element.
    if ((size == 0 && values == null) || (size == values.length)) {
      grow();
    }
    values[size] = value;
    size++;
    sizeModificationCount++;
    return true;
  }

  /** Increases the capacity of the array. */
  @SuppressWarnings({"unchecked"}) // generic array cast
  private void grow() {
    if (values == null) {
      this.values = (E[]) new Object[4];
    } else {
      int newCapacity = 2 * values.length;
      values = Arrays.copyOf(values, newCapacity);
    }
  }

  /**
   * Remove the element at the given index. Does nothing if index is -1.
   *
   * @param index the index of the element to remove
   * @return true if this set was modified
   */
  private boolean removeIndex(@GTENegativeOne int index) {
    if (index == -1) {
      return false;
    }
    System.arraycopy(values, index + 1, values, index, size - index - 1);
    size--;
    sizeModificationCount++;
    return true;
  }

  // Query Operations

  @Pure
  @Override
  public @NonNegative int size() {
    return size;
  }

  @Pure
  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Returns the index of the given value, or -1 if it does not appear. Uses {@code ==} for
   * comparison.
   *
   * @param value a value to find
   * @return the index of the given value, or -1 if it does not appear
   */
  @SuppressWarnings("interning:not.interned") // object identity comparison
  @Pure
  private int indexOf(@GuardSatisfied @Nullable @UnknownSignedness Object value) {
    if (values == null) {
      return -1;
    }
    for (int i = 0; i < size; i++) {
      if (value == values[i]) {
        return i;
      }
    }
    return -1;
  }

  @Pure
  @Override
  public boolean contains(@GuardSatisfied @Nullable @UnknownSignedness Object value) {
    return indexOf(value) != -1;
  }

  // Modification Operations

  @Override
  public boolean add(E value) {
    int index = indexOf(value);
    return add(index, value);
  }

  @Override
  public boolean remove(@GuardSatisfied @Nullable @UnknownSignedness Object value) {
    int index = indexOf(value);
    return removeIndex(index);
  }

  // Bulk Operations

  @Override
  public boolean addAll(Collection<? extends E> c) {
    if (c.isEmpty()) {
      return false;
    }
    boolean changed = false;
    for (E e : c) {
      changed = changed || add(e);
    }
    return changed;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    if (c.isEmpty()) {
      return false;
    }
    boolean changed = false;
    for (Object e : c) {
      // If more than one element is removed, there are more efficient implementations that iterate
      // over the array; but such cleverness is not needed for small sets.
      changed = changed || remove(e);
    }
    return changed;
  }

  // Inherit retainAll() from AbstractCollection.

  @Override
  public void clear() {
    if (size != 0) {
      size = 0;
      sizeModificationCount++;
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  // iterators

  @Override
  public Iterator<E> iterator() {
    return new ArraySetIterator();
  }

  /** An iterator over the IdentityArraySet. */
  private class ArraySetIterator implements Iterator<E> {
    /** The first unread index; the index of the next value to return. */
    @NonNegative int index;
    /** True if remove() has been called since the last call to next(). */
    boolean removed;
    /** The modification count when the iterator is created, for fail-fast. */
    int initialSizeModificationCount;

    /** Creates a new ArraySetIterator. */
    @SuppressWarnings("allcheckers:purity") // initializes `this`
    @SideEffectFree
    ArraySetIterator() {
      index = 0;
      removed = true; // can't remove until next() has been called
      initialSizeModificationCount = sizeModificationCount;
    }

    /**
     * Returns true if this has another element.
     *
     * @return true if this has another element
     */
    @Pure
    @Override
    public final boolean hasNext() {
      return index < size();
    }

    @Override
    public final E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      removed = false;
      return values[index++];
    }

    /** Removes the previously-returned element. */
    @Override
    public final void remove() {
      if (removed) {
        throw new IllegalStateException(
            "Called remove() on ArraySetIterator without calling next() first.");
      }
      if (initialSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
      // Remove the previously returned element, so use index-1.
      @SuppressWarnings("lowerbound:assignment") // removed==false, so index>0.
      @NonNegative int newIndex = index - 1;
      index = newIndex;
      IdentityArraySet.this.removeIndex(index);
      initialSizeModificationCount = sizeModificationCount;
      removed = true;
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  // Comparison and hashing:  equals and hashCode are inherited from AbstractSet.

  // Defaultable methods

  @Override
  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    if (values == null) {
      return;
    }
    int oldSizeModificationCount = sizeModificationCount;
    for (int index = 0; index < size; index++) {
      E e;
      try {
        e = values[index];
      } catch (IndexOutOfBoundsException exc) {
        throw new ConcurrentModificationException(exc);
      }
      action.accept(e);
    }
    if (oldSizeModificationCount != sizeModificationCount) {
      throw new ConcurrentModificationException();
    }
  }

  /**
   * Returns a copy of this.
   *
   * @return a copy of this
   */
  @SuppressWarnings("unchecked")
  @SideEffectFree
  @Override
  public IdentityArraySet<E> clone() {
    return new IdentityArraySet<E>(Arrays.copyOf(values, size), size);
  }

  /**
   * Returns the internal representation, printed.
   *
   * @return the internal representation, printed
   */
  @SideEffectFree
  /* package-private */ String repr() {
    return String.format(
        "size=%d capacity=%s %s",
        size, (values == null ? 0 : values.length), Arrays.toString(values));
  }
}
