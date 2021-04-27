package org.plumelib.util;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.EnsuresKeyFor;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A map backed by a list. It permits null keys and values.
 *
 * <p>Compared to a HashMap or LinkedHashMap: For very small maps, this uses much less space, has
 * comparable or better performance, and (like a LinkedHashMap) is deterministic. For large maps,
 * this is significantly less performant.
 *
 * <p>Compared to a TreeMap: This does not require defining a comparator. This isn't sorted.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings({
  "lock", // not yet annotated for the Lock Checker
  "keyfor" // https://tinyurl.com/cfissue/4558
})
public class ArrayMap<K, V> extends AbstractMap<K, V> {

  // An alternate  internal representation should be a list of
  // Map.Entry objects (e.g., AbstractMap.SimpleEntry) instead of two arrays for lists and values.
  // It would make some operations more expensive.

  /** The keys. */
  private final ArrayList<K> keys;
  /** The values. */
  private final ArrayList<V> values;

  // TODO: Maintain and check this.
  /**
   * The number of times this HashMap has been structurally modified (a change to the list lengths
   * due to adding or removing an element). This field is used to make iterators on Collection-views
   * of the HashMap fail-fast.
   */
  transient int modCount = 0;

  // Constructors

  /**
   * Constructs an empty {@code ArrayMap} with the specified initial capacity.
   *
   * @param initialCapacity the initial capacity
   * @throws IllegalArgumentException if the initial capacity is negative
   */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree.assign.field") // initializes `this`
  @SideEffectFree
  public ArrayMap(int initialCapacity) {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    this.keys = new ArrayList<>(initialCapacity);
    this.values = new ArrayList<>(initialCapacity);
  }

  /** Constructs an empty {@code ArrayMap} with the default initial capacity. */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree.assign.field") // initializes `this`
  @SideEffectFree
  public ArrayMap() {
    this.keys = new ArrayList<>();
    this.values = new ArrayList<>();
  }

  /**
   * Private constructor. Installs the given objects in this as its representation.
   *
   * @param keys the keys
   * @param values the values
   */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree.assign.field") // initializes `this`
  @SideEffectFree
  private ArrayMap(ArrayList<K> keys, ArrayList<V> values) {
    this.keys = keys;
    this.values = values;
  }

  /**
   * Constructs a new {@code ArrayMap} with the same mappings as the specified {@code Map}.
   *
   * @param m the map whose mappings are to be placed in this map
   * @throws NullPointerException if the specified map is null
   */
  @SuppressWarnings({
    "allcheckers:purity", // initializes `this`
    "lock:method.guarantee.violated", // initializes `this`
    "nullness:method.invocation.invalid", // inference failure
  })
  @SideEffectFree
  public ArrayMap(Map<? extends K, ? extends V> m) {
    int size = m.size();
    this.keys = new ArrayList<>(size);
    this.values = new ArrayList<>(size);
    putAll(m);
  }

  // Private helper functions

  /**
   * Adds the (key, value) mapping to this.
   *
   * @param index -1 or the index of {@code key} in {@code keys}
   * @param key the key
   * @param value the value
   */
  @SuppressWarnings({
    "InvalidParam", // Error Prone stupidly warns about field `keys`
    "keyfor:contracts.postcondition.not.satisfied" // insertion in keys array suffices
  })
  @EnsuresKeyFor(value = "#2", map = "this")
  private void put(ArrayMap<K, V> this, @GTENegativeOne int index, K key, V value) {
    if (index == -1) {
      keys.add(key);
      values.add(value);
    } else {
      values.set(index, value);
    }
  }

  /**
   * Remove the mapping at the given index. Does nothing if index is -1.
   *
   * @param index the index of the mapping to remove
   * @return true if this map was modified
   */
  private boolean removeIndex(@GTENegativeOne int index) {
    if (index != -1) {
      keys.remove(index);
      values.remove(index);
      return true;
    } else {
      return false;
    }
  }

  // Query Operations

  @Pure
  @Override
  public @NonNegative int size() {
    return keys.size();
  }

  @Pure
  @Override
  public boolean isEmpty() {
    return keys.isEmpty();
  }

  @Pure
  @Override
  @SuppressWarnings(
      "keyfor:contracts.conditional.postcondition.not.satisfied") // delegate test to `keys` field
  public boolean containsKey(@Nullable Object key) {
    return keys.contains(key);
  }

  @Pure
  @Override
  public boolean containsValue(@Nullable Object value) {
    return values.contains(value);
  }

  /**
   * Returns true if this map contains the given mapping.
   *
   * @param key the key
   * @param value the value
   * @return true if this map contains the given mapping.
   */
  @Pure
  private boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    int index = keys.indexOf(key);
    return index != -1 && Objects.equals(value, values.get(index));
  }

  @Pure
  @Override
  public @Nullable V get(@Nullable Object key) {
    int index = keys.indexOf(key);
    if (index == -1) {
      return null;
    } else {
      return values.get(index);
    }
  }

  // Modification Operations

  @Override
  public @Nullable V put(K key, V value) {
    int index = keys.indexOf(key);
    V currentValue = (index == -1) ? null : values.get(index);
    put(index, key, value);
    return currentValue;
  }

  @Override
  public @Nullable V remove(@Nullable Object key) {
    int index = keys.indexOf(key);
    // cannot use removeIndex because it has the wrong return type
    if (index == -1) {
      return null;
    }
    V currentValue = values.get(index);
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
    keys.clear();
    values.clear();
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
    public final boolean contains(Object o) {
      return containsKey(o);
    }

    @Override
    public final boolean remove(Object o) {
      int index = keys.indexOf(o);
      return removeIndex(index);
    }

    @SuppressWarnings("nullness:return.type.incompatible") // polymorphism problem
    @SideEffectFree
    @Override
    public Object[] toArray() {
      return keys.toArray(new Object[keys.size()]);
    }

    @SuppressWarnings("nullness:override.param.invalid") // Nullness Checker special-cases toArray
    @SideEffectFree
    @Override
    public <T> @Nullable T[] toArray(@PolyNull T[] a) {
      return keys.toArray(a);
    }

    @Override
    public final void forEach(Consumer<? super K> action) {
      keys.forEach(action);
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
    public final boolean contains(Object o) {
      return containsValue(o);
    }

    @SuppressWarnings("nullness:override.return.invalid") // polymorphism problem
    @SideEffectFree
    @Override
    public @Nullable Object[] toArray() {
      return values.toArray(new Object[values.size()]);
    }

    @SuppressWarnings("nullness:override.param.invalid") // Nullness Checker special-cases toArray
    @SideEffectFree
    @Override
    public <T> @Nullable T[] toArray(@PolyNull T[] a) {
      return values.toArray(a);
    }

    @Override
    public final void forEach(Consumer<? super V> action) {
      values.forEach(action);
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
    public final boolean contains(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      Object key = e.getKey();
      Object value = e.getValue();
      return containsEntry(key, value);
    }

    @Override
    public final boolean remove(Object o) {
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
      "interning:argument.type.incompatible", // TODO: investigate later
      "signature:argument.type.incompatible", // TODO: investigate later
    })
    @Override
    public final void forEach(Consumer<? super Map.Entry<@KeyFor("ArrayMap.this") K, V>> action) {
      int size = size();
      for (int index = 0; index < size; index++) {
        action.accept(new Entry(index));
      }
      if (size != size()) { // it would be better to maintain a modification count
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
    // This should be a modification count.
    /** The size, for fail-fast. */
    @NonNegative int size;

    /** Creates a new ArrayMapIterator. */
    @SuppressWarnings("allcheckers:purity") // initializes `this`
    @SideEffectFree
    ArrayMapIterator() {
      index = 0;
      size = size();
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

    // TODO: This should only return a single element.  Calling it twice in a row should throw
    // IllegalStateException.
    /** Removes the previously-returned element. */
    public final void remove() {
      if (index == 0) {
        throw new IllegalStateException();
      }
      // WRONG TEST since this is allowed to change the size.
      // if (size != size()) {
      //   throw new ConcurrentModificationException();
      // }
      ArrayMap.this.removeIndex(--index);
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
      return keys.get(index++);
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
      return values.get(index++);
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
      return new Entry(index++);
    }
  }

  // An alternate representation would be a triple of index, key, and value.
  //  * That would make Entry objects a bit larger (more allocation would be necessary, though the
  //    same *number* of objects), and would take a tiny bit more computation to create.
  //  * That would make calling getKey and getValue slightly cheaper (a local lookup instead of
  //    calling an ArrayList method), though in practice the implementation would probably compute
  //    both ways and issue ConcurrentModificationException if the results differed.
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
      return keys.get(index);
    }

    @Pure
    @Override
    public V getValue() {
      return values.get(index);
    }

    @Override
    public V setValue(V value) {
      return values.set(index, value);
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
    public boolean equals(@Nullable Object o) {
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

  // Comparison and hashing

  //   public boolean equals(Object other) equals() is inherited

  @Pure
  @Override
  public int hashCode() {
    return Objects.hash(keys, values);
  }

  // Defaultable methods

  @SideEffectFree
  @Override
  public V getOrDefault(@Nullable Object key, V defaultValue) {
    int index = keys.indexOf(key);
    if (index != -1) {
      return values.get(index);
    } else {
      return defaultValue;
    }
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    Objects.requireNonNull(action);
    int size = size();
    for (int index = 0; index < size; index++) {
      K k;
      V v;
      try {
        k = keys.get(index);
        v = values.get(index);
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException(e);
      }
      action.accept(k, v);
    }
    if (size != size()) { // it would be better to maintain a modification count
      throw new ConcurrentModificationException();
    }
  }

  @Override
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    int size = size();
    for (int index = 0; index < size; index++) {
      K k;
      V v;
      try {
        k = keys.get(index);
        v = values.get(index);
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException(e);
      }
      v = function.apply(k, v);

      try {
        values.set(index, v);
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException(e);
      }
    }
    if (size != size()) { // it would be better to maintain a modification count
      throw new ConcurrentModificationException();
    }
  }

  @Override
  public @Nullable V putIfAbsent(K key, V value) {
    int index = keys.indexOf(key);
    V currentValue = index == -1 ? null : values.get(index);
    put(index, key, value);
    return currentValue;
  }

  @Override
  public boolean remove(@Nullable Object key, @Nullable Object value) {
    int index = keys.indexOf(key);
    if (index == -1) {
      return false;
    }
    Object curValue = values.get(index);
    if (!Objects.equals(curValue, value)) {
      return false;
    }
    removeIndex(index);
    return true;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    int index = keys.indexOf(key);
    if (index == -1) {
      return false;
    }
    Object curValue = values.get(index);
    if (!Objects.equals(curValue, oldValue)) {
      return false;
    }
    values.set(index, newValue);
    return true;
  }

  @Override
  public @Nullable V replace(K key, V value) {
    int index = keys.indexOf(key);
    if (index == -1) {
      return null;
    }
    V currentValue = values.get(index);
    values.set(index, value);
    return currentValue;
  }

  @Override
  public @Nullable V computeIfAbsent(
      K key, Function<? super K, ? extends @Nullable V> mappingFunction) {
    Objects.requireNonNull(mappingFunction);
    int index = keys.indexOf(key);
    V currentValue;
    if (index != -1) {
      currentValue = values.get(index);
      if (currentValue != null) {
        return currentValue;
      }
    }
    V newValue;
    if ((newValue = mappingFunction.apply(key)) != null) {
      put(index, key, newValue);
    }
    return newValue;
  }

  @Override
  public @Nullable V computeIfPresent(
      K key, BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    int index = keys.indexOf(key);
    if (index == -1) {
      return null;
    }
    V oldValue = values.get(index);
    if (oldValue == null) {
      return null;
    }
    V newValue = remappingFunction.apply(key, oldValue);
    if (newValue != null) {
      values.set(index, newValue);
      return newValue;
    } else {
      removeIndex(index);
      return null;
    }
  }

  @Override
  public @Nullable V compute(
      K key, BiFunction<? super K, ? super @Nullable V, ? extends @Nullable V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    int index = keys.indexOf(key);
    V oldValue = (index == -1) ? null : values.get(index);
    V newValue = remappingFunction.apply(key, oldValue);
    if (newValue == null) {
      removeIndex(index);
      return null;
    } else {
      put(index, key, newValue);
      return newValue;
    }
  }

  @Override
  public @Nullable V merge(
      K key, @NonNull V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    Objects.requireNonNull(value);
    int index = keys.indexOf(key);
    V oldValue = (index == -1) ? null : values.get(index);
    V newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);
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
    return new ArrayMap<>(new ArrayList<>(keys), new ArrayList<>(values));
  }
}
