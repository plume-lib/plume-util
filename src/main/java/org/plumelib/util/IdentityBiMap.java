package org.plumelib.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
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
 * fail-fast, and they iterate over the mappings in the same order.
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
  public IdentityBiMap() {
    this(new IdentityHashMap<>(), new IdentityHashMap<>());
  }

  /**
   * Creates a new IdentityBiMap containing the mappings in the given map.
   *
   * @param m the mappings to place in the new map
   * @throws IllegalArgumentException if two keys of {@code m} map to the same value
   */
  public IdentityBiMap(Map<? extends K, ? extends V> m) {
    this(new IdentityHashMap<>(m.size()), new IdentityHashMap<>(m.size()));
    if (m instanceof IdentityBiMap<? extends K, ? extends V> other) {
      // `other` is already a bijection, so there is no need to check each mapping.
      forward.putAll(other.forward);
      backward.putAll(other.backward);
    } else {
      putAll(m);
    }
  }

  /**
   * Creates a new IdentityBiMap that uses the given maps, which must be empty.
   *
   * @param forward the mapping from keys to values
   * @param backward the mapping from values to keys
   */
  @SuppressWarnings({
    "nullness:argument", // `inverse` stores but does not use `this`, which is under initialization
    "nullness:assignment", // `inverse` is initialized except for its reference to `this`
  })
  private IdentityBiMap(IdentityHashMap<K, V> forward, IdentityHashMap<V, K> backward) {
    this.forward = forward;
    this.backward = backward;
    this.inverse = new IdentityBiMap<>(/* forward= */ backward, /* backward= */ forward, this);
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
   * Returns the key that maps to the given value, or null if there is none. This is equivalent to,
   * and more concise than, {@code inverse().get(value)}.
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
    V currentValue = forward.get(key);
    boolean containsKey = currentValue != null || forward.containsKey(key);
    if (containsKey && currentValue == value) {
      return value;
    }
    K otherKey = backward.get(value);
    if (otherKey != null || backward.containsKey(value)) {
      if (!force) {
        throw new IllegalArgumentException(
            "value already present: " + value + " (mapped from " + otherKey + ")");
      }
      forward.remove(otherKey);
    }
    V oldValue = forward.put(key, value);
    if (containsKey) {
      backward.remove(oldValue);
    }
    backward.put(value, key);
    return oldValue;
  }

  /**
   * Returns true if this map maps the given key to the given value. Keys and values are compared by
   * identity.
   *
   * @param key a key
   * @param value a value
   * @return true if this map maps {@code key} to {@code value}
   */
  @Pure
  private boolean containsMapping(@Nullable Object key, @Nullable Object value) {
    V actual = forward.get(key);
    return actual == value && (actual != null || forward.containsKey(key));
  }

  @Override
  public @Nullable V remove(@Nullable Object key) {
    if (!forward.containsKey(key)) {
      return null;
    }
    return removePresentKey(key);
  }

  /**
   * Removes the mapping for the given key, which must be in this map.
   *
   * @param key a key in this map
   * @return the value that was associated with {@code key}
   */
  @SideEffectsOnly("this")
  private @Nullable V removePresentKey(@Nullable Object key) {
    V value = forward.remove(key);
    backward.remove(value);
    return value;
  }

  /**
   * Removes the mapping for the given key, if it is mapped to the given value. Unlike {@link
   * Map#remove(Object, Object)}, values are compared by identity.
   *
   * @param key a key
   * @param value the value expected to be associated with {@code key}
   * @return true if the mapping was removed
   */
  @Override
  public boolean remove(@Nullable Object key, @Nullable Object value) {
    if (!containsMapping(key, value)) {
      return false;
    }
    removePresentKey(key);
    return true;
  }

  /**
   * Replaces the value for the given key, if it is mapped to the given value. Unlike {@link
   * Map#replace(Object, Object, Object)}, values are compared by identity.
   *
   * @param key a key
   * @param oldValue the value expected to be associated with {@code key}
   * @param newValue the value to associate with {@code key}
   * @return true if the value was replaced
   * @throws IllegalArgumentException if {@code newValue} is already associated with a different key
   */
  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    if (!containsMapping(key, oldValue)) {
      return false;
    }
    put(key, newValue);
    return true;
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
  @SuppressWarnings({
    "allcheckers:purity.unknown.sideeffectsonly", // allocates a copy of the inverse
    "IdentityHashMapUsage", // the copy of the inverse compares by identity
  })
  public void putAll(Map<? extends K, ? extends V> m) {
    if (m == inverse) {
      // Iterating over the inverse while modifying this map would throw
      // ConcurrentModificationException.
      m = new IdentityHashMap<>(m);
    }
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  /**
   * Replaces each value with the result of applying the given function to its mapping. The new
   * values must be distinct, but they may permute the old values.
   *
   * <p>If an exception is thrown, this map is unchanged, unless {@code function} modified this map.
   *
   * @param function the function that computes each new value
   * @throws IllegalArgumentException if the function produces the same value for two keys
   */
  @Override
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    // Compute all the new values before changing either map, so that a failure leaves this map
    // unchanged and so that the new values may permute the old values.
    IdentityHashMap<V, K> newBackward = new IdentityHashMap<>(forward.size());
    for (Map.Entry<K, V> e : forward.entrySet()) {
      K key = e.getKey();
      V newValue = function.apply(key, e.getValue());
      if (newBackward.containsKey(newValue)) {
        throw new IllegalArgumentException(
            "duplicate value: "
                + newValue
                + " (mapped from "
                + newBackward.get(newValue)
                + " and "
                + key
                + ")");
      }
      newBackward.put(newValue, key);
    }
    // Every key is already in `forward`, so these calls are not structural modifications.
    for (Map.Entry<V, K> e : newBackward.entrySet()) {
      forward.put(e.getValue(), e.getKey());
    }
    backward.clear();
    backward.putAll(newBackward);
  }

  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    forward.forEach(action);
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
   * Returns a view of the values of this map. The set uses object identity to compare elements. It
   * has the same elements as {@code inverse().keySet()}, but it iterates in the same order as
   * {@link #keySet} and {@link #entrySet}.
   *
   * @return a view of the values of this map
   */
  @Override
  public Set<V> values() {
    return new ValueSet();
  }

  /**
   * Returns a view of the mappings of this map. The set and its entries use object identity to
   * compare keys and values. {@link Map.Entry#setValue} throws {@link IllegalArgumentException} if
   * the new value is already associated with a different key. As with any {@link Map.Entry}, the
   * behavior of an entry is undefined if the map has been modified, other than through the entry's
   * {@code setValue} method, since the iterator returned the entry.
   *
   * @return a view of the mappings of this map
   */
  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  /**
   * Compares the given object with this map for equality. If the given object is an {@code
   * IdentityBiMap} or an {@link IdentityHashMap}, keys and values are compared by identity. If the
   * given object is some other {@link Map}, the comparison is the one that {@link
   * java.util.AbstractMap#equals} performs when its receiver is {@code o} and its argument is this
   * map: each value of {@code o} is compared using {@code equals()} to this map's value for the
   * same key, where this map looks up keys by identity. Therefore, the result is the same as that
   * of {@code o.equals(this)}, as is required for symmetry, when {@code o} is an {@link
   * IdentityHashMap}, a {@link java.util.HashMap}, or another map that inherits {@code
   * AbstractMap.equals}.
   *
   * <p>Like {@link IdentityHashMap}, this class violates the general contract of {@link
   * Object#hashCode}: an {@code IdentityBiMap} and a map that is not an {@code IdentityBiMap} may
   * be equal but have different hash codes, because {@link #hashCode} uses identity hash codes.
   *
   * @param o the object to compare with this map
   * @return true if the given object is equal to this map
   */
  @Override
  @SuppressWarnings({
    "IdentityHashMapUsage", // comparing to an arbitrary Map is intended
    "allcheckers:purity.call", // the result does not depend on iteration order
    "allcheckers:purity.catch", // the result does not depend on which value's equals() throws
  })
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof IdentityBiMap<?, ?> other) {
      return forward.equals(other.forward);
    }
    if (!(o instanceof Map<?, ?> other) || other.size() != size()) {
      return false;
    }
    // Compare directly rather than calling `other.equals(this)`, which would recurse forever if
    // `other.equals()` delegated back to this method.
    // IdentityHashMap.equals() compares values by identity; AbstractMap.equals() uses equals().
    // The result is not symmetric if `other` is a wrapper, such as one returned by
    // Collections.unmodifiableMap(), around an IdentityBiMap or IdentityHashMap: then
    // `other.equals(this)` compares values by identity, but this method uses equals().  This
    // method cannot detect such wrappers.
    boolean identityValues = other instanceof IdentityHashMap<?, ?>;
    for (Map.Entry<?, ?> e : other.entrySet()) {
      Object key = e.getKey();
      Object value = e.getValue();
      boolean matches;
      try {
        matches =
            (identityValues || value == null)
                ? containsMapping(key, value)
                : value.equals(forward.get(key));
      } catch (ClassCastException | NullPointerException ex) {
        // AbstractMap.equals() also returns false in this case.
        return false;
      }
      if (!matches) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the hash code of this map, which is computed from the identity hash codes of its keys
   * and values. See {@link #equals} for how this violates the general contract of {@link
   * Object#hashCode}.
   *
   * @return the hash code of this map
   */
  @Override
  public int hashCode() {
    return forward.hashCode();
  }

  @Override
  @SuppressWarnings("allcheckers:purity.call") // modifies only a local StringBuilder
  public String toString() {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<K, V> e : forward.entrySet()) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      sb.append(toStringElement(e.getKey()));
      sb.append('=');
      sb.append(toStringElement(e.getValue()));
    }
    sb.append('}');
    return sb.toString();
  }

  /**
   * Returns a printed representation of a key or value of this map. Avoids infinite recursion if
   * the argument is this map or its inverse.
   *
   * @param o a key or value of this map
   * @return a printed representation of {@code o}
   */
  @SideEffectFree
  private String toStringElement(@Nullable Object o) {
    if (o == this) {
      return "(this Map)";
    } else if (o == inverse) {
      return "(this Map's inverse)";
    } else {
      return String.valueOf(o);
    }
  }

  /**
   * An iterator over the mappings of this map, whose {@code remove} method updates both {@link
   * #forward} and {@link #backward}.
   *
   * @param <T> the type of the elements returned by the iterator
   */
  private abstract class MappingIterator<T> implements Iterator<T> {

    /** The underlying iterator. */
    private final Iterator<Map.Entry<K, V>> itor = forward.entrySet().iterator();

    /** True if {@link #lastKey} is valid, that is, if {@link #remove} may be called. */
    private boolean canRemove = false;

    /** The key of the mapping most recently returned by the underlying iterator. */
    private @Nullable K lastKey = null;

    /** Creates a new MappingIterator. */
    @SideEffectFree
    MappingIterator() {}

    /**
     * Returns the iterator element for the given mapping.
     *
     * @param key a key in this map
     * @param value the value for {@code key}
     * @return the iterator element for the mapping
     */
    @SideEffectFree
    abstract T element(K key, V value);

    @Override
    public boolean hasNext() {
      return itor.hasNext();
    }

    @Override
    public T next() {
      Map.Entry<K, V> e = itor.next();
      K key = e.getKey();
      lastKey = key;
      canRemove = true;
      return element(key, e.getValue());
    }

    @Override
    public void remove() {
      if (!canRemove) {
        throw new IllegalStateException();
      }
      // The value may have changed since `next()` returned, so look it up.
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
    K element(K key, V value) {
      return key;
    }
  }

  /** An iterator over the values of this map. */
  private final class ValueIterator extends MappingIterator<V> {

    /** Creates a new ValueIterator. */
    @SideEffectFree
    ValueIterator() {}

    @Override
    @SideEffectFree
    V element(K key, V value) {
      return value;
    }
  }

  /** An iterator over the entries of this map. */
  private final class EntryIterator extends MappingIterator<Map.Entry<K, V>> {

    /** Creates a new EntryIterator. */
    @SideEffectFree
    EntryIterator() {}

    @Override
    @SideEffectFree
    Map.Entry<K, V> element(K key, V value) {
      return new Entry(key, value);
    }
  }

  /**
   * A view of the keys, values, or entries of an {@link IdentityBiMap}.
   *
   * @param <E> the type of the elements of the view
   */
  private abstract class View<E> extends AbstractSet<E> {

    /** Creates a new View. */
    @SuppressWarnings("allcheckers:purity.call") // calls `super`
    @SideEffectFree
    View() {}

    @Override
    public @NonNegative int size() {
      return forward.size();
    }

    @Override
    public void clear() {
      IdentityBiMap.this.clear();
    }

    // AbstractSet.removeAll() uses either this.remove() or c.contains(), depending on which
    // collection is larger; the results differ when c does not compare by identity.
    @Override
    @SuppressWarnings("nullness:methodref.param") // as in AbstractCollection, may pass null to c
    public boolean removeAll(Collection<?> c) {
      return removeIf(c::contains);
    }
  }

  /** The keys of an {@link IdentityBiMap}. */
  private final class KeySet extends View<K> {

    /** Creates a new KeySet. */
    @SideEffectFree
    KeySet() {}

    @Override
    public boolean contains(@Nullable Object o) {
      return forward.containsKey(o);
    }

    @Override
    public boolean remove(@Nullable Object o) {
      if (!forward.containsKey(o)) {
        return false;
      }
      removePresentKey(o);
      return true;
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

  /** The values of an {@link IdentityBiMap}. */
  private final class ValueSet extends View<V> {

    /** Creates a new ValueSet. */
    @SideEffectFree
    ValueSet() {}

    @Override
    public boolean contains(@Nullable Object o) {
      return backward.containsKey(o);
    }

    @Override
    public boolean remove(@Nullable Object o) {
      if (!backward.containsKey(o)) {
        return false;
      }
      removePresentKey(backward.get(o));
      return true;
    }

    @Override
    public Iterator<V> iterator() {
      return new ValueIterator();
    }

    @Override
    @SuppressWarnings({
      "allcheckers:purity.call", // the hash code does not depend on iteration order
      "PMD.OverrideBothEqualsAndHashcode", // the inherited equals() uses contains(), which uses ==
    })
    public int hashCode() {
      // Use identity hash codes, consistent with `contains`.
      return backward.keySet().hashCode();
    }
  }

  /** The entries of an {@link IdentityBiMap}. */
  private final class EntrySet extends View<Map.Entry<K, V>> {

    /** Creates a new EntrySet. */
    @SideEffectFree
    EntrySet() {}

    @Override
    public boolean contains(@Nullable Object o) {
      return o instanceof Map.Entry<?, ?> e && containsMapping(e.getKey(), e.getValue());
    }

    @Override
    public boolean remove(@Nullable Object o) {
      if (!(o instanceof Map.Entry<?, ?> e) || !contains(e)) {
        return false;
      }
      removePresentKey(e.getKey());
      return true;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new EntryIterator();
    }

    @Override
    @SuppressWarnings({
      "allcheckers:purity.call", // the hash code does not depend on iteration order
      "PMD.OverrideBothEqualsAndHashcode", // the inherited equals() uses contains(), which uses ==
    })
    public int hashCode() {
      // Equal to the sum of the hash codes of the Entry objects, but faster.
      return forward.entrySet().hashCode();
    }
  }

  /**
   * A mapping in an {@link IdentityBiMap}. It records its key and value; {@link #setValue} also
   * writes the map. It uses object identity to compare keys and values.
   */
  private final class Entry implements Map.Entry<K, V> {

    /** The key. */
    private final K key;

    /** The value. */
    private V value;

    /**
     * Creates a new Entry.
     *
     * @param key the key
     * @param value the value
     */
    @SideEffectFree
    Entry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    /**
     * Replaces the value of this entry and of its mapping in the map.
     *
     * @param value the new value
     * @return the old value
     * @throws IllegalArgumentException if {@code value} is already associated with a different key
     */
    @Override
    public V setValue(V value) {
      V oldValue = this.value;
      put(key, value);
      this.value = value;
      return oldValue;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      return o instanceof Map.Entry<?, ?> e && key == e.getKey() && value == e.getValue();
    }

    @Override
    public int hashCode() {
      // Consistent with IdentityHashMap's entries.
      return System.identityHashCode(key) ^ System.identityHashCode(value);
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }
  }
}
