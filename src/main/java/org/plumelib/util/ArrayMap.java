package org.plumelib.util;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTEqLengthOf;
import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.SameLen;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.EnsuresKeyFor;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.signedness.qual.PolySigned;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A map backed by two arrays. It permits null keys and values, and its iterator has deterministic
 * ordering.
 *
 * <p>Compared to a HashMap or LinkedHashMap: For very small maps, this uses much less space, has
 * comparable performance, and (like a LinkedHashMap) is deterministic, with elements returned in
 * the order their keys were inserted. For large maps, this is significantly less performant than
 * other map implementations.
 *
 * <p>Compared to a TreeMap: This uses somewhat less space, and it does not require defining a
 * comparator. This isn't sorted but does have deteriministic ordering. For large maps, this is
 * significantly less performant than other map implementations.
 *
 * <p>A number of other ArrayMap implementations exist, including
 *
 * <ul>
 *   <li>android.util.ArrayMap
 *   <li>com.google.api.client.util.ArrayMap
 *   <li>it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *   <li>oracle.dss.util.ArrayMap
 *   <li>org.apache.myfaces.trinidad.util.ArrayMap
 * </ul>
 *
 * All of those use the Apache License, version 2.0, whereas this implementation is licensed under
 * the more libral MIT License. In addition, some of those implementations forbid nulls or
 * nondeterministically reorder the contents, and others don't specify their behavior regarding
 * nulls and ordering.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings({
  "index", // TODO
  "keyfor", // https://tinyurl.com/cfissue/4558
  "lock", // not yet annotated for the Lock Checker
  "nullness" // temporary; nullness is tricky because of null-padded arrays
})
public class ArrayMap<K extends @UnknownSignedness Object, V extends @UnknownSignedness Object>
    extends AbstractMap<K, V> {

  // An alternate internal representation would be a list of Map.Entry objects (e.g.,
  // AbstractMap.SimpleEntry) instead of two arrays for lists and values.  That is a bad idea
  // because it both uses more memory and makes some operations more expensive.

  /** The keys. Null if capacity=0. */
  private @Nullable K @SameLen("values") [] keys;
  /** The values. Null if capacity=0. */
  private @Nullable V @SameLen("keys") [] values;
  /** The number of used mappings in the representation of this. */
  private @NonNegative @LessThan("keys.length + 1") @IndexOrHigh({"keys", "values"}) int size = 0;
  // An alternate representation would also store the hash code of each key, for quicker querying.

  /**
   * The number of times this map's size has been modified by adding or removing an element
   * (changing the value associated with a key does not count as a change). This field is used to
   * make view iterators fail-fast.
   */
  transient int sizeModificationCount = 0;

  // Constructors

  /**
   * Constructs an empty {@code ArrayMap} with the specified initial capacity.
   *
   * @param initialCapacity the initial capacity
   * @throws IllegalArgumentException if the initial capacity is negative
   */
  @SuppressWarnings({
    "unchecked", // generic array cast
    "samelen:assignment", // initialization
    "allcheckers:purity.not.sideeffectfree.assign.field" // initializes `this`
  })
  @SideEffectFree
  public ArrayMap(int initialCapacity) {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    if (initialCapacity == 0) {
      this.keys = null;
      this.values = null;
    } else {
      this.keys = (K[]) new Object[initialCapacity];
      this.values = (V[]) new Object[initialCapacity];
    }
  }

  /** Constructs an empty {@code ArrayMap} with the default initial capacity. */
  @SideEffectFree
  public ArrayMap() {
    this(4);
  }

  /**
   * Private constructor. Installs the given objects in this as its representation, without making
   * defensive copies.
   *
   * @param keys the keys
   * @param values the values
   * @param size the number of used items in the arrays; may be less than their lengths
   */
  @SuppressWarnings({
    "samelen:assignment", // initialization
    "allcheckers:purity.not.sideeffectfree.assign.field" // initializes `this`
  })
  @SideEffectFree
  private ArrayMap(
      K @SameLen("values") [] keys,
      V @SameLen("keys") [] values,
      @LTEqLengthOf({"keys", "values"}) int size) {
    this.keys = keys;
    this.values = values;
    this.size = size;
  }

  /**
   * Constructs a new {@code ArrayMap} with the same mappings as the given {@code Map}.
   *
   * @param m the map whose mappings are to be placed in this map
   * @throws NullPointerException if the given map is null
   */
  @SuppressWarnings({
    "allcheckers:purity", // initializes `this`
    "lock:method.guarantee.violated", // initializes `this`
    "nullness:method.invocation", // inference failure;
    // https://github.com/typetools/checker-framework/issues/979 ?
  })
  @SideEffectFree
  public ArrayMap(Map<? extends K, ? extends V> m) {
    this(m.size());
    putAll(m);
  }

  // Factory (constructor) methods

  /**
   * Returns a new ArrayMap or HashMap with the given capacity. Uses an ArrayMap if the capacity is
   * small, and a HashMap otherwise.
   *
   * @param <K> the type of the keys
   * @param <V> the type of the values
   * @param capacity the expected maximum number of elements in the set
   * @return a new ArrayMap or HashMap with the given capacity
   */
  public static <K, V> Map<K, V> newArrayMapOrHashMap(int capacity) {
    if (capacity <= 4) {
      return new ArrayMap<>(capacity);
    } else {
      return new HashMap<>(CollectionsPlume.mapCapacity(capacity));
    }
  }

  /**
   * Returns a new ArrayMap or HashMap with the given elements. Uses an ArrayMap if the capacity is
   * small, and a HashMap otherwise.
   *
   * @param <K> the type of the keys
   * @param <V> the type of the values
   * @param m the elements to put in the returned set
   * @return a new ArrayMap or HashMap with the given elements
   */
  public static <K, V> Map<K, V> newArrayMapOrHashMap(Map<K, V> m) {
    if (m.size() <= 4) {
      return new ArrayMap<>(m);
    } else {
      return new HashMap<>(m);
    }
  }

  /**
   * Returns a new ArrayMap or LinkedHashMap with the given capacity. Uses an ArrayMap if the
   * capacity is small, and a LinkedHashMap otherwise.
   *
   * @param <K> the type of the keys
   * @param <V> the type of the values
   * @param capacity the expected maximum number of elements in the set
   * @return a new ArrayMap or LinkedHashMap with the given capacity
   */
  public static <K, V> Map<K, V> newArrayMapOrLinkedHashMap(int capacity) {
    if (capacity <= 4) {
      return new ArrayMap<>(capacity);
    } else {
      return new LinkedHashMap<>(CollectionsPlume.mapCapacity(capacity));
    }
  }

  /**
   * Returns a new ArrayMap or LinkedHashMap with the given elements. Uses an ArrayMap if the
   * capacity is small, and a LinkedHashMap otherwise.
   *
   * @param <K> the type of the keys
   * @param <V> the type of the values
   * @param m the elements to put in the returned set
   * @return a new ArrayMap or LinkedHashMap with the given elements
   */
  public static <K, V> Map<K, V> newArrayMapOrLinkedHashMap(Map<K, V> m) {
    if (m.size() <= 4) {
      return new ArrayMap<>(m);
    } else {
      return new LinkedHashMap<>(m);
    }
  }

  // Private helper functions

  /**
   * Adds the (key, value) mapping to this.
   *
   * @param index the index of {@code key} in {@code keys}. If -1, add a new mapping. Otherwise,
   *     replace the mapping at {@code index}.
   * @param key the key
   * @param value the value
   */
  @SuppressWarnings({
    "InvalidParam", // Error Prone stupidly warns about field `keys`
    "keyfor:contracts.postcondition" // insertion in keys array suffices
  })
  @EnsuresKeyFor(value = "#2", map = "this")
  private void put(@GTENegativeOne int index, K key, V value) {
    if (index == -1) {
      // Add a new mapping.
      if ((size == 0 && keys == null) || (size == keys.length)) {
        grow();
      }
      keys[size] = key;
      values[size] = value;
      size++;
      sizeModificationCount++;
    } else {
      // Replace an existing mapping.
      values[index] = value;
    }
  }

  /** Increases the capacity of the arrays. */
  @SuppressWarnings({"unchecked"}) // generic array cast
  private void grow() {
    if (keys == null) {
      this.keys = (K[]) new Object[4];
      this.values = (V[]) new Object[4];
    } else {
      int newCapacity = 2 * keys.length;
      keys = Arrays.copyOf(keys, newCapacity);
      values = Arrays.copyOf(values, newCapacity);
    }
  }

  /**
   * Remove the mapping at the given index. Does nothing if index is -1.
   *
   * @param index the index of the mapping to remove
   * @return true if this map was modified
   */
  private boolean removeIndex(@GTENegativeOne int index) {
    if (index == -1) {
      return false;
    }
    System.arraycopy(keys, index + 1, keys, index, size - index - 1);
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
   * Returns the index of the given key, or -1 if it does not appear. Uses {@code Objects.equals}
   * for comparison.
   *
   * @param key a key to find
   * @return the index of the given key, or -1 if it does not appear
   */
  @Pure
  private int indexOfKey(@GuardSatisfied @Nullable @UnknownSignedness Object key) {
    if (keys == null) {
      return -1;
    }
    for (int i = 0; i < size; i++) {
      if (Objects.equals(key, keys[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the given value, or -1 if it does not appear. Uses {@code Objects.equals}
   * for comparison.
   *
   * @param value a value to find
   * @return the index of the given value, or -1 if it does not appear
   */
  @Pure
  private int indexOfValue(@GuardSatisfied @Nullable @UnknownSignedness Object value) {
    if (keys == null) {
      return -1;
    }
    for (int i = 0; i < size; i++) {
      if (Objects.equals(value, values[i])) {
        return i;
      }
    }
    return -1;
  }

  @Pure
  @Override
  @SuppressWarnings("keyfor:contracts.conditional.postcondition") // delegate test to `keys` field
  public boolean containsKey(@GuardSatisfied @Nullable @UnknownSignedness Object key) {
    return indexOfKey(key) != -1;
  }

  @Pure
  @Override
  public boolean containsValue(@GuardSatisfied @Nullable @UnknownSignedness Object value) {
    return indexOfValue(value) != -1;
  }

  /**
   * Returns true if this map contains the given mapping.
   *
   * @param key the key
   * @param value the value
   * @return true if this map contains the given mapping
   */
  @Pure
  private boolean containsEntry(
      @GuardSatisfied @Nullable @UnknownSignedness Object key,
      @GuardSatisfied @Nullable @UnknownSignedness Object value) {
    int index = indexOfKey(key);
    return index != -1 && Objects.equals(value, values[index]);
  }

  @Pure
  @Override
  public @Nullable V get(@GuardSatisfied @Nullable @UnknownSignedness Object key) {
    int index = indexOfKey(key);
    return getOrNull(index);
  }

  /**
   * Returns the value at the given index, or null if the index is -1.
   *
   * @param index the index
   * @return the value at the given index, or null if the index is -1
   */
  @Pure
  private @Nullable V getOrNull(@GTENegativeOne int index) {
    return (index == -1) ? null : values[index];
  }

  // Modification Operations

  @Override
  public @Nullable V put(K key, V value) {
    int index = indexOfKey(key);
    V currentValue = getOrNull(index);
    put(index, key, value);
    return currentValue;
  }

  @Override
  public @Nullable V remove(@GuardSatisfied @Nullable @UnknownSignedness Object key) {
    int index = indexOfKey(key);
    // cannot use removeIndex because it has the wrong return type
    if (index == -1) {
      return null;
    }
    V currentValue = values[index];
    removeIndex(index);
    return currentValue;
  }

  // Bulk Operations

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    if (m.isEmpty()) {
      return;
    }
    for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    if (size != 0) {
      size = 0;
      sizeModificationCount++;
    }
  }

  // Views

  /** A view of the keys. */
  @MonotonicNonNull Set<@KeyFor("this") K> keySet = null;

  // Behavior is undefined if the map is changed while the sets are being iterated through, so these
  // implementations can assume there are no concurrent side effects.
  @Pure
  @SuppressWarnings("allcheckers:purity") // update cache
  @Override
  public Set<@KeyFor("this") K> keySet() {
    if (keySet == null) {
      keySet = new KeySet();
    }
    return keySet;
  }

  /** Represents a view of the keys. */
  final class KeySet extends AbstractSet<@KeyFor("this") K> {

    /** Creates a new KeySet. */
    public KeySet() {}

    @Pure
    @Override
    public final @NonNegative int size() {
      return ArrayMap.this.size();
    }

    @Override
    public final void clear() {
      ArrayMap.this.clear();
    }

    @Override
    public final Iterator<@KeyFor("this") K> iterator() {
      return new KeyIterator();
    }

    @Pure
    @Override
    public final boolean contains(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      return containsKey(o);
    }

    @Override
    public final boolean remove(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      int index = indexOfKey(o);
      return removeIndex(index);
    }

    @SuppressWarnings({"nullness:return"}) // array isn't padded with null, before index `size`
    @SideEffectFree
    @Override
    public @PolySigned Object[] toArray() {
      // toArray must return a new array because clients are permitted to modify it.
      return (@PolySigned Object[]) Arrays.copyOf(keys, size);
    }

    @SuppressWarnings({
      "unchecked", // generic array cast
      "nullness" // Nullness Checker special-cases toArray
    })
    @SideEffectFree
    @Override
    public <T> @Nullable T[] toArray(@PolyNull T[] a) {
      T[] result;
      if (a.length >= size) {
        result = a;
      } else {
        result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
      }
      System.arraycopy(keys, 0, result, 0, size);
      return result;
    }

    @Override
    public final void forEach(Consumer<? super K> action) {
      if (keys == null) {
        return;
      }
      int oldSizeModificationCount = sizeModificationCount;
      for (int i = 0; i < size; i++) {
        K key = keys[i];
        action.accept(key);
      }
      if (oldSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  /** The view of the values. */
  @MonotonicNonNull Collection<V> valuesCollection = null;

  @Pure
  @SuppressWarnings("allcheckers:purity")
  @Override
  public Collection<V> values() {
    if (valuesCollection == null) {
      valuesCollection = new Values();
    }
    return valuesCollection;
  }

  /** Represents a view of the values. */
  final class Values extends AbstractCollection<V> {

    /** Creates a new Values. */
    public Values() {}

    @Pure
    @Override
    public final @NonNegative int size() {
      return ArrayMap.this.size();
    }

    @Override
    public final void clear() {
      ArrayMap.this.clear();
    }

    @Override
    public final Iterator<V> iterator() {
      return new ValueIterator();
    }

    @Pure
    @Override
    public final boolean contains(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      return containsValue(o);
    }

    @SuppressWarnings({"nullness:override.return"}) // polymorphism problem
    @SideEffectFree
    @Override
    public @Nullable @PolySigned Object[] toArray() {
      // toArray must return a new array because clients are permitted to modify it.
      return (@Nullable @PolySigned Object[]) Arrays.copyOf(values, size);
    }

    @SuppressWarnings({
      "unchecked", // generic array cast
      "nullness" // Nullness Checker special-cases toArray
    })
    @SideEffectFree
    @Override
    public <T> @Nullable T[] toArray(@PolyNull T[] a) {
      T[] result;
      if (a.length >= size) {
        result = a;
      } else {
        result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
      }
      System.arraycopy(values, 0, result, 0, size);
      return result;
    }

    @Override
    public final void forEach(Consumer<? super V> action) {
      if (keys == null) {
        return;
      }
      int oldSizeModificationCount = sizeModificationCount;
      for (int i = 0; i < size; i++) {
        action.accept(values[i]);
      }
      if (oldSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  /** The view of the entries. */
  @MonotonicNonNull Set<Map.Entry<@KeyFor("this") K, V>> entrySet = null;

  @SuppressWarnings("allcheckers:purity")
  @Pure
  @Override
  public Set<Map.Entry<@KeyFor("this") K, V>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet();
    }
    return entrySet;
  }

  /** Represents a view of the entries. */
  final class EntrySet extends AbstractSet<Map.Entry<@KeyFor("this") K, V>> {

    /** Creates a new EntrySet. */
    public EntrySet() {}

    @Pure
    @Override
    public final @NonNegative int size() {
      return ArrayMap.this.size();
    }

    @Override
    public final void clear() {
      ArrayMap.this.clear();
    }

    @Override
    public final Iterator<Map.Entry<@KeyFor("ArrayMap.this") K, V>> iterator() {
      return new EntryIterator();
    }

    @Pure
    @Override
    public final boolean contains(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      Object key = e.getKey();
      Object value = e.getValue();
      return containsEntry(key, value);
    }

    @Override
    public final boolean remove(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      if (o instanceof Map.Entry) {
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
        Object key = e.getKey();
        Object value = e.getValue();
        return ArrayMap.this.remove(key, value);
      }
      return false;
    }

    // toArray() and toArray(T[] a) are inherited.

    @SuppressWarnings({
      "interning:argument", // TODO: investigate later
      "signature:argument", // TODO: investigate later
    })
    @Override
    public final void forEach(Consumer<? super Map.Entry<@KeyFor("ArrayMap.this") K, V>> action) {
      int oldSizeModificationCount = sizeModificationCount;
      for (int index = 0; index < size(); index++) {
        action.accept(new Entry(index));
      }
      if (oldSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  // iterators

  /** An iterator over the ArrayMap. */
  abstract class ArrayMapIterator {
    /** The first unread index; the index of the next value to return. */
    @NonNegative int index;
    /** True if remove() has been called since the last call to next(). */
    boolean removed;
    /** The modification count when the iterator is created, for fail-fast. */
    int initialSizeModificationCount;

    /** Creates a new ArrayMapIterator. */
    @SuppressWarnings("allcheckers:purity") // initializes `this`
    @SideEffectFree
    ArrayMapIterator() {
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
    public final boolean hasNext() {
      return index < size();
    }

    /** Removes the previously-returned element. */
    public final void remove() {
      if (removed) {
        throw new IllegalStateException(
            "Called remove() on ArrayMapIterator without calling next() first.");
      }
      if (initialSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
      // Remove the previously returned element, so use index-1.
      @SuppressWarnings("lowerbound:assignment") // removed==false, so index>0.
      @NonNegative int newIndex = index - 1;
      index = newIndex;
      ArrayMap.this.removeIndex(index);
      initialSizeModificationCount = sizeModificationCount;
      removed = true;
    }
  }

  /** An iterator over the keys. */
  final class KeyIterator extends ArrayMapIterator implements Iterator<@KeyFor("this") K> {
    /** Creates a new KeyIterator. */
    @SideEffectFree
    KeyIterator() {}

    @Override
    public final @KeyFor("ArrayMap.this") K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      removed = false;
      return keys[index++];
    }
  }

  /** An iterator over the values. */
  final class ValueIterator extends ArrayMapIterator implements Iterator<V> {
    /** Creates a new ValueIterator. */
    @SideEffectFree
    ValueIterator() {}

    @Override
    public final V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      removed = false;
      return values[index++];
    }
  }

  /** An iterator over the entries. */
  final class EntryIterator extends ArrayMapIterator implements Iterator<Map.Entry<K, V>> {
    /** Creates a new EntryIterator. */
    @SideEffectFree
    EntryIterator() {}

    @Override
    public final Map.Entry<K, V> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      removed = false;
      return new Entry(index++);
    }
  }

  // An alternate representation would be a triple of index, key, and value.
  //  * That would make Entry objects a bit larger (more allocation would be necessary, though the
  //    same *number* of objects), and would take a tiny bit more computation to create.
  //  * That would make calling getKey and getValue slightly cheaper if they are called multiple
  //    times (a local lookup instead of calling an ArrayList method).
  //  * That would provide less surprising results for some illegal client code.  Removing from the
  //    entrySet iterator and then calling any entry method (getKey, getValue, setValue) has
  //    undefined behavior, but clients might try to do it.  This could issue
  //    ConcurrentModificationException in that case, by checking that the values in the array still
  //    match those stored in the entry.

  // Per the specification of Map.Entry, a map entry is  meaningful only during the execution of the
  // iteration over the entry set, and only if the backing map has not been modified except through
  // calling {@code setValue} on the map entry.
  /** An entrySet() entry. Tracks the containing list and the index. */
  final class Entry implements Map.Entry<K, V> {
    /** The index. */
    @NonNegative int index;

    /**
     * Creates a new map entry.
     *
     * @param index the index
     */
    @SuppressWarnings("allcheckers:purity") // initializes `this`
    @Pure
    public Entry(@NonNegative int index) {
      this.index = index;
    }

    @Pure
    @Override
    public K getKey() {
      return keys[index];
    }

    @Pure
    @Override
    public V getValue() {
      return values[index];
    }

    @Override
    public V setValue(V value) {
      // Do not increment sizeModificationCount.
      return values[index] = value;
    }

    /**
     * Returns the ArrayMap associated with this entry.
     *
     * @return the ArrayMap associated with this entry
     */
    @Pure
    private ArrayMap<K, V> theArrayMap() {
      return ArrayMap.this;
    }

    // Per the specification of Map.Entry, this does not compare the underlying list and index.
    @Pure
    @Override
    public boolean equals(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      if (this == o) {
        return true;
      }
      if (o instanceof ArrayMap.Entry) {
        @SuppressWarnings("unchecked")
        Entry otherEntry = (Entry) o;
        @SuppressWarnings({"interning:not.interned", "ReferenceEquality"}) // fast special case test
        boolean result =
            this.index == otherEntry.index && this.theArrayMap() == otherEntry.theArrayMap();
        if (result) {
          return true;
        }
        // else fall through
      }
      if (o instanceof Map.Entry) {
        @SuppressWarnings("unchecked")
        Map.Entry<K, V> otherEntry = (Map.Entry<K, V>) o;
        return Objects.equals(this.getKey(), otherEntry.getKey())
            && Objects.equals(this.getValue(), otherEntry.getValue());
      }
      return false;
    }

    @Pure
    @Override
    public int hashCode() {
      return Objects.hash(getKey(), getValue());
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  // Comparison and hashing:  equals and hashCode are inherited from AbstractSet.

  // Defaultable methods

  @SideEffectFree
  @Override
  public V getOrDefault(@GuardSatisfied @Nullable @UnknownSignedness Object key, V defaultValue) {
    int index = indexOfKey(key);
    if (index != -1) {
      return values[index];
    } else {
      return defaultValue;
    }
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    Objects.requireNonNull(action);
    if (keys == null) {
      return;
    }
    int oldSizeModificationCount = sizeModificationCount;
    for (int index = 0; index < size; index++) {
      K k;
      V v;
      try {
        k = keys[index];
        v = values[index];
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException(e);
      }
      action.accept(k, v);
    }
    if (oldSizeModificationCount != sizeModificationCount) {
      throw new ConcurrentModificationException();
    }
  }

  @Override
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    if (keys == null) {
      return;
    }
    int oldSizeModificationCount = sizeModificationCount;
    int size = size();
    for (int index = 0; index < size; index++) {
      K k;
      V v;
      try {
        k = keys[index];
        v = values[index];
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException(e);
      }
      v = function.apply(k, v);

      try {
        values[index] = v;
        // Do not increment sizeModificationCount.
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException(e);
      }
    }
    if (oldSizeModificationCount != sizeModificationCount) {
      throw new ConcurrentModificationException();
    }
  }

  @Override
  public @Nullable V putIfAbsent(K key, V value) {
    int index = indexOfKey(key);
    if (index == -1 || values[index] == null) {
      put(index, key, value);
      return null;
    } else {
      return values[index];
    }
  }

  @Override
  public boolean remove(
      @GuardSatisfied @Nullable @UnknownSignedness Object key,
      @GuardSatisfied @Nullable @UnknownSignedness Object value) {
    int index = indexOfKey(key);
    if (index == -1) {
      return false;
    }
    Object curValue = values[index];
    if (!Objects.equals(curValue, value)) {
      return false;
    }
    removeIndex(index);
    return true;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    int index = indexOfKey(key);
    if (index == -1) {
      return false;
    }
    Object curValue = values[index];
    if (!Objects.equals(curValue, oldValue)) {
      return false;
    }
    values[index] = newValue;
    // Do not increment sizeModificationCount.
    return true;
  }

  @Override
  public @Nullable V replace(K key, V value) {
    int index = indexOfKey(key);
    if (index == -1) {
      return null;
    }
    V currentValue = values[index];
    values[index] = value;
    // Do not increment sizeModificationCount.
    return currentValue;
  }

  @Override
  public @PolyNull V computeIfAbsent(
      K key, Function<? super K, ? extends @PolyNull V> mappingFunction) {
    Objects.requireNonNull(mappingFunction);
    int index = indexOfKey(key);
    if (index != -1) {
      V currentValue = values[index];
      if (currentValue != null) {
        return currentValue;
      }
    }
    // either index == -1, or values[index]==null.
    int oldSizeModificationCount = sizeModificationCount;
    V newValue = mappingFunction.apply(key);
    if (oldSizeModificationCount != sizeModificationCount) {
      throw new ConcurrentModificationException();
    }
    if (newValue != null) {
      put(index, key, newValue);
    }
    return newValue;
  }

  @Override
  public @PolyNull V computeIfPresent(
      K key, BiFunction<? super K, ? super V, ? extends @PolyNull V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    int index = indexOfKey(key);
    if (index == -1) {
      @SuppressWarnings("nullness:assignment")
      @PolyNull V result = null;
      return result;
    }
    V oldValue = values[index];
    if (oldValue == null) {
      @SuppressWarnings("nullness:assignment")
      @PolyNull V result = null;
      return result;
    }
    // index != -1  and  values[index] != null.
    int oldSizeModificationCount = sizeModificationCount;
    V newValue = remappingFunction.apply(key, oldValue);
    if (oldSizeModificationCount != sizeModificationCount) {
      throw new ConcurrentModificationException();
    }
    if (newValue != null) {
      values[index] = newValue;
      // Do not increment sizeModificationCount.
      return newValue;
    } else {
      removeIndex(index);
      return null;
    }
  }

  @Override
  public @PolyNull V compute(
      K key, BiFunction<? super K, ? super @Nullable V, ? extends @PolyNull V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    int index = indexOfKey(key);
    V oldValue = getOrNull(index);
    int oldSizeModificationCount = sizeModificationCount;
    V newValue = remappingFunction.apply(key, oldValue);
    if (oldSizeModificationCount != sizeModificationCount) {
      throw new ConcurrentModificationException();
    }
    if (newValue == null) {
      removeIndex(index);
      return null;
    } else {
      put(index, key, newValue);
      return newValue;
    }
  }

  @Override
  public @PolyNull V merge(
      K key,
      @NonNull V value,
      BiFunction<? super V, ? super V, ? extends @PolyNull V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    Objects.requireNonNull(value);
    int index = indexOfKey(key);
    V oldValue = getOrNull(index);
    int oldSizeModificationCount = sizeModificationCount;
    @PolyNull V newValue;
    if (oldValue == null) {
      newValue = value;
    } else {
      newValue = remappingFunction.apply(oldValue, value);
    }
    if (oldSizeModificationCount != sizeModificationCount) {
      throw new ConcurrentModificationException();
    }
    if (newValue == null) {
      removeIndex(index);
    } else {
      put(index, key, newValue);
    }
    return newValue;
  }

  /**
   * Returns a copy of this.
   *
   * @return a copy of this
   */
  @SuppressWarnings("unchecked")
  @SideEffectFree
  @Override
  public ArrayMap<K, V> clone() {
    return new ArrayMap<K, V>(Arrays.copyOf(keys, size), Arrays.copyOf(values, size), size);
  }

  /**
   * Returns the internal representation, printed.
   *
   * @return the internal representation, printed
   */
  @SideEffectFree
  /* package-private */ String repr() {
    return String.format(
        "size=%d capacity=%d %s %s",
        size, (keys == null ? 0 : keys.length), Arrays.toString(keys), Arrays.toString(values));
  }
}
