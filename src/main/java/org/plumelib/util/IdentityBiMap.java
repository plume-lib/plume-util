package org.plumelib.util;

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.SideEffectsOnly;

/**
 * A bidirectional map (a bijection) that uses object identity ({@code ==}), not {@code equals()},
 * to compare keys and to compare values. Every value is associated with at most one key, so the map
 * can be efficiently inverted: {@link #inverse} returns a view of this map in which the values are
 * keys and the keys are values.
 *
 * <p>Like {@link IdentityHashMap}, this class intentionally violates the general contract of {@link
 * Map}, which mandates the use of {@code equals()} when comparing objects. This class permits null
 * keys and null values.
 *
 * <p>The views returned by {@link #keySet}, {@link #values}, and {@link #entrySet} are backed by
 * the map and support removal. They also use identity for comparisons. Their iterators are
 * fail-fast.
 *
 * <p>This class is not synchronized.
 *
 * @param <K> the type of keys of the map
 * @param <V> the type of values of the map
 */
@SuppressWarnings({
  "interning:not.interned", // this class compares keys and values by identity
  "keyfor", // keys for `this` are also keys for `this.forward`
  "lock", // not yet annotated for the Lock Checker
  "modifiability", // not yet annotated for the Modifiability Checker
  "signedness", // not yet annotated for the Signedness Checker
})
public final class IdentityBiMap<K, V> implements Map<K, V> {

  /** The mapping from keys to values. */
  private final IdentityHashMap<K, V> forward;

  /** The mapping from values to keys. Always the inverse of {@link #forward}. */
  private final IdentityHashMap<V, K> backward;

  /** The inverse view of this map. Its {@code forward} is this map's {@code backward}. */
  private final IdentityBiMap<V, K> inverse;

  /** Creates a new, empty IdentityBiMap. */
  @SuppressWarnings({
    "nullness:argument", // `inverse` stores but does not use `this`, which is under initialization
    "nullness:assignment", // `inverse` is initialized except for its reference to `this`
  })
  public IdentityBiMap() {
    this.forward = new IdentityHashMap<>();
    this.backward = new IdentityHashMap<>();
    this.inverse = new IdentityBiMap<>(/* forward= */ backward, /* backward= */ forward, this);
  }

  /**
   * Creates a new IdentityBiMap containing the mappings in the given map.
   *
   * @param m the mappings to place in the new map
   * @throws IllegalArgumentException if two keys of {@code m} map to the same value
   */
  public IdentityBiMap(Map<? extends K, ? extends V> m) {
    this();
    putAll(m);
  }

  /**
   * Creates a view of the inverse of a map.
   *
   * @param forward the mapping from keys to values
   * @param backward the mapping from values to keys
   * @param inverse the inverse of the new map
   */
  private IdentityBiMap(
      IdentityHashMap<K, V> forward, IdentityHashMap<V, K> backward, IdentityBiMap<V, K> inverse) {
    this.forward = forward;
    this.backward = backward;
    this.inverse = inverse;
  }

  /**
   * Returns the inverse view of this map, which maps each value to its key. The two maps are backed
   * by the same data, so changes to either are reflected in the other.
   *
   * @return the inverse view of this map
   */
  public IdentityBiMap<V, K> inverse() {
    return inverse;
  }

  /**
   * Returns the key that maps to the given value, or null if there is none.
   *
   * @param value a value
   * @return the key that maps to {@code value}, or null if {@code value} is not in this map
   */
  public @Nullable K getKey(@Nullable Object value) {
    return backward.get(value);
  }

  @Override
  public @NonNegative int size() {
    return forward.size();
  }

  @Override
  public boolean isEmpty() {
    return forward.isEmpty();
  }

  @Override
  public boolean containsKey(@Nullable Object key) {
    return forward.containsKey(key);
  }

  @Override
  public boolean containsValue(@Nullable Object value) {
    return backward.containsKey(value);
  }

  @Override
  public @Nullable V get(@Nullable Object key) {
    return forward.get(key);
  }

  /**
   * Associates the given value with the given key. If the key was already in the map, its old value
   * is replaced.
   *
   * @param key a key
   * @param value the value to associate with {@code key}
   * @return the value previously associated with {@code key}, or null if there was none
   * @throws IllegalArgumentException if {@code value} is already associated with a different key;
   *     use {@link #forcePut} to remove that mapping instead
   */
  @Override
  public @Nullable V put(K key, V value) {
    return put(key, value, false);
  }

  /**
   * Associates the given value with the given key. If the key was already in the map, its old value
   * is replaced. If the value was already associated with a different key, that mapping is removed.
   *
   * @param key a key
   * @param value the value to associate with {@code key}
   * @return the value previously associated with {@code key}, or null if there was none
   */
  public @Nullable V forcePut(K key, V value) {
    return put(key, value, true);
  }

  /**
   * Associates the given value with the given key.
   *
   * @param key a key
   * @param value the value to associate with {@code key}
   * @param force if true, remove any other mapping to {@code value}; if false, throw an exception
   *     if there is any other mapping to {@code value}
   * @return the value previously associated with {@code key}, or null if there was none
   * @throws IllegalArgumentException if {@code force} is false and {@code value} is already
   *     associated with a different key
   */
  @SideEffectsOnly("this")
  private @Nullable V put(K key, V value, boolean force) {
    boolean containsKey = forward.containsKey(key);
    if (containsKey && forward.get(key) == value) {
      return value;
    }
    if (backward.containsKey(value)) {
      if (!force) {
        throw new IllegalArgumentException(
            "value already present: " + value + " (mapped from " + backward.get(value) + ")");
      }
      forward.remove(backward.get(value));
    }
    V oldValue = forward.put(key, value);
    if (containsKey) {
      backward.remove(oldValue);
    }
    backward.put(value, key);
    return oldValue;
  }

  @Override
  public @Nullable V remove(@Nullable Object key) {
    if (!forward.containsKey(key)) {
      return null;
    }
    V value = forward.remove(key);
    backward.remove(value);
    return value;
  }

  /**
   * Copies all of the mappings from the given map to this map.
   *
   * <p>If an exception is thrown, some of the mappings may already have been added to this map.
   *
   * @param m the mappings to store in this map
   * @throws IllegalArgumentException if a value of {@code m} is already associated with a different
   *     key, or if two keys of {@code m} map to the same value
   */
  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    forward.clear();
    backward.clear();
  }

  /**
   * Returns a view of the keys of this map. The set uses object identity to compare elements.
   *
   * @return a view of the keys of this map
   */
  @Override
  public Set<K> keySet() {
    return new KeySet();
  }

  /**
   * Returns a view of the values of this map. The set uses object identity to compare elements.
   * This is the same as {@code inverse().keySet()}.
   *
   * @return a view of the values of this map
   */
  @Override
  public Set<V> values() {
    return inverse.keySet();
  }

  /**
   * Returns a view of the mappings of this map. The set and its entries use object identity to
   * compare keys and values. {@link Map.Entry#setValue} throws {@link IllegalArgumentException} if
   * the new value is already associated with a different key.
   *
   * @return a view of the mappings of this map
   */
  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  /**
   * Compares the given object with this map for equality. Like {@link IdentityHashMap#equals}, if
   * the given object is an {@code IdentityBiMap} or an {@code IdentityHashMap}, keys and values are
   * compared by identity. Otherwise, the comparison is as specified by {@link Map#equals}.
   *
   * @param o the object to compare with this map
   * @return true if the given object is equal to this map
   */
  @Override
  @SuppressWarnings("IdentityHashMapUsage") // comparing to an arbitrary Map is intended
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof IdentityBiMap<?, ?> other) {
      return forward.equals(other.forward);
    }
    return forward.equals(o);
  }

  @Override
  public int hashCode() {
    return forward.hashCode();
  }

  @Override
  public String toString() {
    return forward.toString();
  }

  /**
   * An iterator over the mappings of this map, whose {@code remove} method updates both {@link
   * #forward} and {@link #backward}.
   *
   * @param <T> the type of the elements returned by the iterator
   */
  private abstract class MappingIterator<T> implements Iterator<T> {

    /** The underlying iterator. */
    private final Iterator<K> itor = forward.keySet().iterator();

    /** True if {@link #lastKey} is valid, that is, if {@link #remove} may be called. */
    private boolean canRemove = false;

    /** The key most recently returned by the underlying iterator. */
    private @Nullable K lastKey = null;

    /** Creates a new MappingIterator. */
    @SideEffectFree
    MappingIterator() {}

    /**
     * Returns the iterator element for the given key.
     *
     * @param key a key in this map
     * @return the iterator element for {@code key}
     */
    @SideEffectFree
    abstract T fromKey(K key);

    @Override
    public boolean hasNext() {
      return itor.hasNext();
    }

    @Override
    public T next() {
      K key = itor.next();
      lastKey = key;
      canRemove = true;
      return fromKey(key);
    }

    @Override
    public void remove() {
      if (!canRemove) {
        throw new IllegalStateException();
      }
      V value = forward.get(lastKey);
      itor.remove();
      backward.remove(value);
      canRemove = false;
      lastKey = null;
    }
  }

  /** An iterator over the keys of this map. */
  private final class KeyIterator extends MappingIterator<K> {

    /** Creates a new KeyIterator. */
    @SideEffectFree
    KeyIterator() {}

    @Override
    @SideEffectFree
    K fromKey(K key) {
      return key;
    }
  }

  /** An iterator over the entries of this map. */
  private final class EntryIterator extends MappingIterator<Map.Entry<K, V>> {

    /** Creates a new EntryIterator. */
    @SideEffectFree
    EntryIterator() {}

    @Override
    @SideEffectFree
    Map.Entry<K, V> fromKey(K key) {
      return new Entry(key);
    }
  }

  /** The keys of an {@link IdentityBiMap}. */
  private final class KeySet extends AbstractSet<K> {

    /** Creates a new KeySet. */
    @SuppressWarnings("allcheckers:purity.call") // calls `super`
    @SideEffectFree
    KeySet() {}

    @Override
    public @NonNegative int size() {
      return forward.size();
    }

    @Override
    public boolean contains(@Nullable Object o) {
      return forward.containsKey(o);
    }

    @Override
    public boolean remove(@Nullable Object o) {
      if (!forward.containsKey(o)) {
        return false;
      }
      IdentityBiMap.this.remove(o);
      return true;
    }

    @Override
    public void clear() {
      IdentityBiMap.this.clear();
    }

    @Override
    public Iterator<K> iterator() {
      return new KeyIterator();
    }

    @Override
    @SuppressWarnings({
      "allcheckers:purity.call", // the hash code does not depend on iteration order
      "PMD.OverrideBothEqualsAndHashcode", // the inherited equals() uses contains(), which uses ==
    })
    public int hashCode() {
      // Use identity hash codes, consistent with `contains`.
      return forward.keySet().hashCode();
    }
  }

  /** The entries of an {@link IdentityBiMap}. */
  private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

    /** Creates a new EntrySet. */
    @SuppressWarnings("allcheckers:purity.call") // calls `super`
    @SideEffectFree
    EntrySet() {}

    @Override
    public @NonNegative int size() {
      return forward.size();
    }

    @Override
    public boolean contains(@Nullable Object o) {
      return o instanceof Map.Entry<?, ?> e
          && forward.containsKey(e.getKey())
          && forward.get(e.getKey()) == e.getValue();
    }

    @Override
    public boolean remove(@Nullable Object o) {
      if (!(o instanceof Map.Entry<?, ?> e) || !contains(e)) {
        return false;
      }
      IdentityBiMap.this.remove(e.getKey());
      return true;
    }

    @Override
    public void clear() {
      IdentityBiMap.this.clear();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }
  }

  /**
   * A mapping in an {@link IdentityBiMap}. It is backed by the map: {@link #getValue} reads the map
   * and {@link #setValue} writes the map. It uses object identity to compare keys and values.
   */
  private final class Entry implements Map.Entry<K, V> {

    /** The key. */
    private final K key;

    /**
     * Creates a new Entry.
     *
     * @param key the key
     */
    @SideEffectFree
    Entry(K key) {
      this.key = key;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    @SuppressWarnings("nullness:return") // the key is in the map, so the result is a value
    public V getValue() {
      return forward.get(key);
    }

    /**
     * Replaces the value of this entry's mapping.
     *
     * @param value the new value
     * @return the old value
     * @throws IllegalArgumentException if {@code value} is already associated with a different key
     */
    @Override
    @SuppressWarnings("nullness:return") // the key is in the map, so the result is a value
    public V setValue(V value) {
      if (!forward.containsKey(key)) {
        throw new IllegalStateException("mapping has been removed: " + key);
      }
      return put(key, value);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      return o instanceof Map.Entry<?, ?> e && key == e.getKey() && getValue() == e.getValue();
    }

    @Override
    public int hashCode() {
      // Consistent with IdentityHashMap's entries.
      return System.identityHashCode(key) ^ System.identityHashCode(getValue());
    }

    @Override
    public String toString() {
      return key + "=" + getValue();
    }
  }
}
