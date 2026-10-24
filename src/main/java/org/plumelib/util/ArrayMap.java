package org.plumelib.util;

import java.util.AbstractCollection;
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
import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.SameLen;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.IteratorPolyMod;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.PolyShrinkable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.Ungrowable;
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
import org.checkerframework.dataflow.qual.SideEffectsOnly;

/**
 * A map backed by arrays of keys, values, and the keys' hash codes. It permits null keys and
 * values, and its iterator has deterministic ordering.
 *
 * <p>As with {@code HashMap}, a key's hash code must not change while the key is in the map.
 *
 * <p>Compared to a HashMap or LinkedHashMap: For very small maps, this uses much less space, has
 * comparable performance, and (like a LinkedHashMap) is deterministic, with elements returned in
 * the order their keys were inserted. For large maps, this is significantly less performant than
 * other map implementations.
 *
 * <p>Compared to a TreeMap: This uses somewhat less space, and it does not require defining a
 * comparator. This isn't sorted but does have deterministic ordering. For large maps, this is
 * significantly less performant than other map implementations.
 *
 * <p>This class is not thread-safe. Like {@code HashMap}, it is fail-fast: the iterators of its
 * views, and its methods that take a function argument (such as {@code forEach}, {@code compute},
 * and {@code replaceAll}), throw {@code ConcurrentModificationException} if the map's size is
 * changed other than through the iterator's own {@code remove} method. Fail-fast behavior is a
 * debugging aid, not a guarantee.
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
 * the more liberal MIT License. In addition, some of those implementations forbid nulls or
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
  "nullness", // temporary; nullness is tricky because of null-padded arrays
})
public class ArrayMap<K extends @UnknownSignedness Object, V extends @UnknownSignedness Object>
    implements Map<K, V>, Cloneable {

  // An alternate internal representation would be a list of Map.Entry objects (e.g.,
  // AbstractMap.SimpleEntry) instead of parallel arrays for keys and values.  That is a bad idea
  // because it both uses more memory and makes some operations more expensive.

  /** The keys. Null if capacity=0. */
  private @Nullable K @Nullable @SameLen({"values", "hashes"}) [] keys;

  /** The values. Null if capacity=0. */
  private @Nullable V @Nullable @SameLen({"keys", "hashes"}) [] values;

  /**
   * The hash codes of the keys: {@code hashes[i] == Objects.hashCode(keys[i])}. Comparing hash
   * codes before calling {@code equals} makes lookups faster. Null if capacity=0.
   */
  private int @Nullable @SameLen({"keys", "values"}) [] hashes;

  /** The number of used mappings in the representation of this. */
  private @NonNegative @LessThan("keys.length + 1") @IndexOrHigh({"keys", "values"}) int size = 0;

  /** A view of the keys. */
  private @MonotonicNonNull @IteratorPolyMod @Ungrowable Set<@KeyFor("this") K> keySet = null;

  /** The view of the values. */
  private @MonotonicNonNull @IteratorPolyMod @Ungrowable Collection<V> valuesCollection = null;

  /** The view of the entries. */
  // Unlike the other view fields, this has no modifiability annotations:  its element type would
  // need @PolyModifiable, which is not permitted on a field.  entrySet() suppresses the resulting
  // warnings.
  private @MonotonicNonNull Set<Map.Entry<@KeyFor("this") K, V>> entrySet = null;

  /**
   * The number of times this map's size has been modified by adding or removing an element
   * (changing the value associated with a key does not count as a change). This field is used to
   * make view iterators fail-fast.
   */
  private int sizeModificationCount = 0;

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
  })
  @SideEffectFree
  public @Modifiable ArrayMap(int initialCapacity) {
    super();
    if (initialCapacity < 0) {
      throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    }
    if (initialCapacity == 0) {
      this.keys = null;
      this.values = null;
      this.hashes = null;
    } else {
      this.keys = (K[]) new Object[initialCapacity];
      this.values = (V[]) new Object[initialCapacity];
      this.hashes = new int[initialCapacity];
    }
  }

  /**
   * Constructs an empty {@code ArrayMap}. Storage is allocated when the first mapping is added, so
   * a map that remains empty uses little memory.
   */
  @SideEffectFree
  public @Modifiable ArrayMap() {
    this(0);
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
    "PMD.ConstructorCallsOverridableMethod",
  })
  @SideEffectFree
  public @Modifiable ArrayMap(Map<? extends K, ? extends V> m) {
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
   * @param capacity the expected maximum number of mappings in the map
   * @return a new ArrayMap or HashMap with the given capacity
   */
  public static <K, V> Map<K, V> newArrayMapOrHashMap(int capacity) {
    if (capacity <= 4) {
      return new ArrayMap<>(capacity);
    } else {
      return new HashMap<>(MapsP.mapCapacity(capacity));
    }
  }

  /**
   * Returns a new ArrayMap or HashMap with the given mappings. Uses an ArrayMap if the given map is
   * small, and a HashMap otherwise.
   *
   * @param <K> the type of the keys
   * @param <V> the type of the values
   * @param m the mappings to put in the returned map
   * @return a new ArrayMap or HashMap with the given mappings
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
   * @param capacity the expected maximum number of mappings in the map
   * @return a new ArrayMap or LinkedHashMap with the given capacity
   */
  public static <K, V> Map<K, V> newArrayMapOrLinkedHashMap(int capacity) {
    if (capacity <= 4) {
      return new ArrayMap<>(capacity);
    } else {
      return new LinkedHashMap<>(MapsP.mapCapacity(capacity));
    }
  }

  /**
   * Returns a new ArrayMap or LinkedHashMap with the given mappings. Uses an ArrayMap if the given
   * map is small, and a LinkedHashMap otherwise.
   *
   * @param <K> the type of the keys
   * @param <V> the type of the values
   * @param m the mappings to put in the returned map
   * @return a new ArrayMap or LinkedHashMap with the given mappings
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
   * Adds a (key, value) mapping to this.
   *
   * @param index the index of {@code key} in {@code keys}. If -1, add a new mapping. Otherwise,
   *     replace the mapping at {@code index}.
   * @param key the key
   * @param hash the hash code of the key, {@code Objects.hashCode(key)}
   * @param value the value
   */
  @SuppressWarnings({
    "InvalidParam", // Error Prone stupidly warns about field `keys`
    "keyfor:contracts.postcondition" // insertion in keys array suffices
  })
  @EnsuresKeyFor(value = "#2", map = "this")
  @SideEffectsOnly("this")
  private void put(@GTENegativeOne int index, K key, int hash, V value) {
    if (index == -1) {
      // Add a new mapping.
      grow();
      keys[size] = key;
      values[size] = value;
      hashes[size] = hash;
      size++;
      sizeModificationCount++;
    } else {
      // Replace an existing mapping.
      assertIndexInBounds(index, "put");
      values[index] = value;
    }
  }

  /**
   * Returns the capacity of this map.
   *
   * @return the capacity of this map
   */
  @Pure
  private int capacity() {
    if (keys == null) {
      return 0;
    } else {
      return keys.length;
    }
  }

  /**
   * Throws an IndexOutOfBoundsException if the index is invalid.
   *
   * @param index an index into this
   * @param method the method that will use the index
   */
  @SideEffectFree
  private void assertIndexInBounds(int index, String method) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(
          method + "(" + index + ",...) called on ArrayMap of size " + size);
    }
  }

  /** Increases the capacity of the arrays, if necessary, so that one more mapping fits. */
  @SideEffectsOnly("this")
  private void grow() {
    ensureCapacity(size + 1);
  }

  /**
   * Increases the capacity of the arrays, if necessary, so that they can hold at least the given
   * number of mappings.
   *
   * @param minCapacity the minimum required capacity
   */
  @SuppressWarnings({"unchecked"}) // generic array cast
  @SideEffectsOnly("this")
  private void ensureCapacity(int minCapacity) {
    int capacity = capacity();
    if (minCapacity <= capacity) {
      return;
    }
    int newCapacity = Math.max(capacity == 0 ? 4 : 2 * capacity, minCapacity);
    if (capacity == 0) {
      this.keys = (K[]) new Object[newCapacity];
      this.values = (V[]) new Object[newCapacity];
      this.hashes = new int[newCapacity];
    } else {
      keys = Arrays.copyOf(keys, newCapacity);
      values = Arrays.copyOf(values, newCapacity);
      hashes = Arrays.copyOf(hashes, newCapacity);
    }
  }

  /**
   * Remove the mapping at the given index. Does nothing if index is -1.
   *
   * @param index the index of the mapping to remove
   * @return true if this map was modified
   */
  @SideEffectsOnly("this")
  private boolean removeIndex(@Shrinkable ArrayMap<K, V> this, @GTENegativeOne int index) {
    if (index == -1) {
      return false;
    }
    assertIndexInBounds(index, "removeIndex");
    System.arraycopy(keys, index + 1, keys, index, size - index - 1);
    System.arraycopy(values, index + 1, values, index, size - index - 1);
    System.arraycopy(hashes, index + 1, hashes, index, size - index - 1);
    size--;
    // Clear the now-unused slot so it does not retain references.
    keys[size] = null;
    values[size] = null;
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
    return indexOfKey(key, Objects.hashCode(key));
  }

  /**
   * Returns the index of the given key, or -1 if it does not appear. Uses {@code Objects.equals}
   * for comparison.
   *
   * @param key a key to find
   * @param hash the hash code of the key, {@code Objects.hashCode(key)}
   * @return the index of the given key, or -1 if it does not appear
   */
  @Pure
  private int indexOfKey(@GuardSatisfied @Nullable @UnknownSignedness Object key, int hash) {
    if (keys == null) {
      return -1;
    }
    for (int i = 0; i < size; i++) {
      if (hashes[i] == hash && Objects.equals(key, keys[i])) {
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
    if (values == null) {
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
    if (index == -1) {
      return null;
    }
    assertIndexInBounds(index, "getOrNull");
    return values[index];
  }

  // Modification Operations

  @Override
  public @Nullable V put(@Growable @Replaceable ArrayMap<K, V> this, K key, V value) {
    int hash = Objects.hashCode(key);
    int index = indexOfKey(key, hash);
    V currentValue = getOrNull(index);
    put(index, key, hash, value);
    return currentValue;
  }

  @Override
  public @Nullable V remove(
      @Shrinkable ArrayMap<K, V> this, @GuardSatisfied @Nullable @UnknownSignedness Object key) {
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

  @SuppressWarnings("allcheckers:purity.unknown.sideeffectsonly") // TEMPORARY, for @SideEffectsOnly
  @Override
  public void putAll(@Growable @Replaceable ArrayMap<K, V> this, Map<? extends K, ? extends V> m) {
    if (m.isEmpty()) {
      return;
    }
    // This may over-allocate if some keys of `m` are already in this map.
    ensureCapacity(size + m.size());
    for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear(@Shrinkable ArrayMap<K, V> this) {
    if (size != 0) {
      // Clear the slots so they do not retain references.  A nonzero size implies that the arrays
      // are non-null.
      Arrays.fill(keys, 0, size, null);
      Arrays.fill(values, 0, size, null);
      size = 0;
      sizeModificationCount++;
    }
  }

  // Views

  @Pure
  @SuppressWarnings({
    "allcheckers:purity", // update cache
    "modifiability:return" // The cache field cannot have a polymorphic type.  The cached view
    // delegates every operation to this map, so it has this map's capabilities no matter which
    // call created it.
  })
  @Override
  public @IteratorPolyMod @PolyShrinkable @Ungrowable Set<@KeyFor("this") K> keySet(
      @PolyShrinkable ArrayMap<K, V> this) {
    if (keySet == null) {
      keySet = new KeySet();
    }
    return keySet;
  }

  /** Represents a view of the keys. */
  private final class KeySet extends AbstractSet<@KeyFor("this") K> {

    /** Creates a new KeySet. */
    public @IteratorPolyMod @PolyShrinkable @Ungrowable KeySet(
        @PolyShrinkable ArrayMap<K, V> ArrayMap.this) {
      super();
    }

    @Pure
    @Override
    public @NonNegative int size() {
      return ArrayMap.this.size();
    }

    @SuppressWarnings({
      "modifiability:method.invocation", // wrapper around outer this
      "allcheckers:purity.unknown.sideeffectsonly" // TEMPORARY: @SideEffectsOnly
    })
    @Override
    public void clear(@Shrinkable KeySet this) {
      ArrayMap.this.clear();
    }

    @Override
    public Iterator<@KeyFor("this") K> iterator() {
      return new KeyIterator();
    }

    @Pure
    @Override
    public boolean contains(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      return containsKey(o);
    }

    @SuppressWarnings("modifiability:method.invocation") // wrapper around outer this
    @Override
    public boolean remove(
        @Shrinkable KeySet this, @GuardSatisfied @Nullable @UnknownSignedness Object o) {
      int index = indexOfKey(o);
      return removeIndex(index);
    }

    @SideEffectFree
    @Override
    public @PolySigned Object[] toArray() {
      // toArray must return a new array because clients are permitted to modify it.
      if (keys == null) {
        return new @PolySigned Object[0];
      }
      return (@PolySigned Object[]) Arrays.copyOf(keys, size);
    }

    @SuppressWarnings({
      "unchecked", // generic array cast
      "nullness", // Nullness Checker special-cases toArray
      "allcheckers:purity.assign.array", // `toArray(T[])` is inherited as @SideEffectFree, but its
      // specification requires writing into the caller-supplied array.
    })
    @Override
    public <T> @Nullable T[] toArray(@PolyNull T[] a) {
      T[] result;
      if (a.length >= size) {
        result = a;
      } else {
        result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
      }
      if (keys != null) {
        System.arraycopy(keys, 0, result, 0, size);
      }
      if (a.length > size) {
        result[size] = null;
      }
      return result;
    }

    @Override
    public void forEach(Consumer<? super K> action) {
      Objects.requireNonNull(action);
      if (keys == null) {
        return;
      }
      int oldSizeModificationCount = sizeModificationCount;
      for (int i = 0; i < size && oldSizeModificationCount == sizeModificationCount; i++) {
        K key = keys[i];
        action.accept(key);
      }
      if (oldSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  @Pure
  @SuppressWarnings({
    "allcheckers:purity", // update cache
    "modifiability:return" // The cache field cannot have a polymorphic type.  The cached view
    // delegates every operation to this map, so it has this map's capabilities no matter which
    // call created it.
  })
  @Override
  public @IteratorPolyMod @PolyShrinkable @Ungrowable Collection<V> values(
      @PolyShrinkable ArrayMap<K, V> this) {
    if (valuesCollection == null) {
      valuesCollection = new Values();
    }
    return valuesCollection;
  }

  /** Represents a view of the values. */
  private final class Values extends AbstractCollection<V> {

    /** Creates a new Values. */
    public @IteratorPolyMod @Ungrowable @PolyShrinkable Values(
        @PolyShrinkable ArrayMap<K, V> ArrayMap.this) {
      super();
    }

    @Pure
    @Override
    public @NonNegative int size() {
      return ArrayMap.this.size();
    }

    @SuppressWarnings({
      "modifiability:method.invocation", // wrapper around outer this
      "allcheckers:purity.unknown.sideeffectsonly" // TEMPORARY: @SideEffectsOnly
    })
    @Override
    public void clear(@Shrinkable Values this) {
      ArrayMap.this.clear();
    }

    @Override
    public Iterator<V> iterator() {
      return new ValueIterator();
    }

    @Pure
    @Override
    public boolean contains(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      return containsValue(o);
    }

    @SuppressWarnings({"nullness:override.return"}) // polymorphism problem
    @SideEffectFree
    @Override
    public @Nullable @PolySigned Object[] toArray() {
      // toArray must return a new array because clients are permitted to modify it.
      if (values == null) {
        return new @Nullable @PolySigned Object[0];
      }
      return (@Nullable @PolySigned Object[]) Arrays.copyOf(values, size);
    }

    @SuppressWarnings({
      "unchecked", // generic array cast
      "nullness", // Nullness Checker special-cases toArray
      "allcheckers:purity.assign.array", // `toArray(T[])` is inherited as @SideEffectFree, but its
      // specification requires writing into the caller-supplied array.
    })
    @Override
    public <T> @Nullable T[] toArray(@PolyNull T[] a) {
      T[] result;
      if (a.length >= size) {
        result = a;
      } else {
        result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
      }
      if (values != null) {
        System.arraycopy(values, 0, result, 0, size);
      }
      if (a.length > size) {
        result[size] = null;
      }
      return result;
    }

    @Override
    public void forEach(Consumer<? super V> action) {
      Objects.requireNonNull(action);
      if (values == null) {
        return;
      }
      int oldSizeModificationCount = sizeModificationCount;
      for (int i = 0; i < size && oldSizeModificationCount == sizeModificationCount; i++) {
        action.accept(values[i]);
      }
      if (oldSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  @SuppressWarnings({
    "allcheckers:purity", // update cache
    "modifiability:assignment", // The cache field cannot have a polymorphic type.  The cached
    // view delegates every operation to this map, so it has this map's capabilities no matter
    // which call created it.
    "modifiability:return" // see "modifiability:assignment" above
  })
  @Pure
  @Override
  public @IteratorPolyMod @PolyShrinkable @Ungrowable Set<
          Map.@PolyModifiable Entry<@KeyFor("this") K, V>>
      entrySet(@PolyModifiable ArrayMap<K, V> this) {
    if (entrySet == null) {
      entrySet = new EntrySet();
    }
    return entrySet;
  }

  /** Represents a view of the entries. */
  private final class EntrySet
      extends AbstractSet<Map.@PolyModifiable Entry<@KeyFor("this") K, V>> {

    /** Creates a new EntrySet. */
    public @IteratorPolyMod @Ungrowable @PolyShrinkable EntrySet(
        @PolyModifiable ArrayMap<K, V> ArrayMap.this) {
      super();
    }

    @Pure
    @Override
    public @NonNegative int size() {
      return ArrayMap.this.size();
    }

    @SuppressWarnings({
      "modifiability:method.invocation", // wrapper around outer this
      "allcheckers:purity.unknown.sideeffectsonly" // TEMPORARY: @SideEffectsOnly
    })
    @Override
    public void clear(@Shrinkable EntrySet this) {
      ArrayMap.this.clear();
    }

    @Override
    public Iterator<Map.@PolyModifiable Entry<@KeyFor("ArrayMap.this") K, V>> iterator() {
      return new EntryIterator();
    }

    @Pure
    @Override
    public boolean contains(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
      Object key = e.getKey();
      Object value = e.getValue();
      return containsEntry(key, value);
    }

    @SuppressWarnings({
      "modifiability:method.invocation", // wrapper around outer this
      "allcheckers:purity.unknown.sideeffectsonly" // TEMPORARY: @SideEffectsOnly
    })
    @Override
    public boolean remove(
        @Shrinkable EntrySet this, @GuardSatisfied @Nullable @UnknownSignedness Object o) {
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
      // "PMD.AvoidInstantiatingObjectsInLoops",
    })
    @Override
    public void forEach(
        Consumer<? super Map.@PolyModifiable Entry<@KeyFor("ArrayMap.this") K, V>> action) {
      Objects.requireNonNull(action);
      int oldSizeModificationCount = sizeModificationCount;
      for (int index = 0;
          index < size() && oldSizeModificationCount == sizeModificationCount;
          index++) {
        action.accept(new Entry(index));
      }
      if (oldSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  // //////////////////////////////////////////////////////////////////////
  // iterators

  /**
   * An iterator over the ArrayMap.
   *
   * @param <T> the type of the iteration value
   */
  private abstract class ArrayMapIterator<T> implements Iterator<T> {
    /** The first unread index; the index of the next value to return. */
    protected @NonNegative int index;

    /** True if remove() has been called since the last call to next(). */
    protected boolean removed;

    /** The modification count when the iterator is created, for fail-fast. */
    private int initialSizeModificationCount;

    /** Creates a new ArrayMapIterator. */
    @SideEffectFree
    private ArrayMapIterator() {
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
    public boolean hasNext() {
      return index < size();
    }

    @Override
    public abstract T next();

    /**
     * Prepares to return the next element: checks for concurrent modification and for the existence
     * of a next element.
     *
     * @throws ConcurrentModificationException if the map's size was changed other than through this
     *     iterator
     * @throws NoSuchElementException if there is no next element
     */
    @SideEffectsOnly("this")
    protected void beforeNext() {
      checkForComodification();
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      removed = false;
    }

    /**
     * Throws ConcurrentModificationException if the map's size was changed other than through this
     * iterator.
     *
     * @throws ConcurrentModificationException if the map's size was changed other than through this
     *     iterator
     */
    @SideEffectFree
    private void checkForComodification() {
      if (initialSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
    }

    /** Removes the previously-returned element. */
    @SuppressWarnings("modifiability:method.invocation") // wrapper around outer this
    @Override
    public void remove(@Shrinkable ArrayMapIterator<T> this) {
      if (removed) {
        throw new IllegalStateException(
            "Called remove() on ArrayMapIterator without calling next() first.");
      }
      checkForComodification();
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
  private final class KeyIterator extends ArrayMapIterator<@KeyFor("this") K> {
    /** Creates a new KeyIterator. */
    @SideEffectFree
    private KeyIterator() {
      super();
    }

    @Override
    public @KeyFor("ArrayMap.this") K next() {
      beforeNext();
      return keys[index++];
    }
  }

  /** An iterator over the values. */
  private final class ValueIterator extends ArrayMapIterator<V> {
    /** Creates a new ValueIterator. */
    @SideEffectFree
    private ValueIterator() {
      super();
    }

    @Override
    public V next() {
      beforeNext();
      return values[index++];
    }
  }

  /** An iterator over the entries. */
  private final class EntryIterator extends ArrayMapIterator<Map.@PolyModifiable Entry<K, V>> {
    /** Creates a new EntryIterator. */
    @SideEffectFree
    private EntryIterator() {
      super();
    }

    @Override
    public Map.@PolyModifiable Entry<K, V> next() {
      beforeNext();
      return new Entry(index++);
    }
  }

  // An Entry remembers its key, so it remains meaningful if the map is structurally modified
  // after the entry is created.  For example, a client may copy the entry set into a list (which
  // calls entrySet().toArray()) and then remove some of the listed mappings from the map.  The
  // index is a hint that makes the common case fast:  if the map has not been modified, the key is
  // still at the index.  If the mapping has been removed from the map, getValue returns the value
  // from when the entry was created or when setValue was last called on it, and setValue does not
  // affect the map.

  /** An entrySet() entry. Tracks the containing map, the key, and the key's probable index. */
  private final class Entry implements Map.Entry<K, V> {
    /** The key. */
    private final K key;

    /**
     * The value when this entry was created or when {@link #setValue} was last called. Used only if
     * {@link #key} has been removed from the map.
     */
    private V value;

    /** The index at which {@link #key} probably appears in {@link #keys}. */
    private final @NonNegative int index;

    /**
     * Creates a new map entry for the mapping at the given index.
     *
     * @param index the index of the mapping
     */
    @SideEffectFree
    public @PolyModifiable Entry(
        @PolyModifiable ArrayMap<K, V> ArrayMap.this, @NonNegative int index) {
      this.index = index;
      this.key = keys[index];
      this.value = values[index];
    }

    /**
     * Returns the current index of this entry's key, or -1 if the key is no longer in the map.
     *
     * @return the current index of this entry's key, or -1 if the key is no longer in the map
     */
    @Pure
    private int currentIndex() {
      @SuppressWarnings({"interning:not.interned", "ReferenceEquality"}) // fast special case test
      boolean atIndex = index < size && keys[index] == key;
      if (atIndex) {
        return index;
      }
      return indexOfKey(key);
    }

    @Pure
    @Override
    public K getKey() {
      return key;
    }

    @Pure
    @Override
    public V getValue() {
      int i = currentIndex();
      return i == -1 ? value : values[i];
    }

    @Override
    public V setValue(@Replaceable Entry this, V newValue) {
      V oldValue = getValue();
      int i = currentIndex();
      if (i != -1) {
        values[i] = newValue;
        // Do not increment sizeModificationCount.
      }
      value = newValue;
      return oldValue;
    }

    // Per the specification of Map.Entry, equality is determined by the key and value.
    @Pure
    @Override
    public boolean equals(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
      if (this == o) {
        return true;
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
    public int hashCode(ArrayMap<K, V>.Entry this) {
      // Per the specification of Map.Entry.hashCode().
      V currentValue = getValue();
      return (key == null ? 0 : key.hashCode())
          ^ (currentValue == null ? 0 : currentValue.hashCode());
    }

    @SuppressWarnings("signedness:unsigned.concat") // true positive: might be an unsigned value
    @SideEffectFree
    @Override
    public String toString(ArrayMap<K, V>.Entry this) {
      return getKey() + "=" + getValue();
    }
  }

  // //////////////////////////////////////////////////////////////////////
  // Comparison, hashing, and printing

  // ArrayMap implements Map directly rather than extending AbstractMap, because AbstractMap
  // declares fields that ArrayMap does not use, and those fields would enlarge every ArrayMap.

  @SuppressWarnings("allcheckers:purity.catch") // the result is deterministic despite the catch
  @Pure
  @Override
  public boolean equals(@GuardSatisfied @Nullable @UnknownSignedness Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Map<?, ?> other)) {
      return false;
    }
    if (other.size() != size) {
      return false;
    }
    try {
      for (int i = 0; i < size; i++) {
        K key = keys[i];
        V value = values[i];
        if (value == null) {
          if (!(other.get(key) == null && other.containsKey(key))) {
            return false;
          }
        } else if (!value.equals(other.get(key))) {
          return false;
        }
      }
    } catch (ClassCastException | NullPointerException unused) {
      // `other` does not permit some key of this map.
      return false;
    }
    return true;
  }

  @Pure
  @Override
  public int hashCode() {
    // Per the specification of Map.hashCode() and Map.Entry.hashCode().
    int result = 0;
    for (int i = 0; i < size; i++) {
      result += hashes[i] ^ Objects.hashCode(values[i]);
    }
    return result;
  }

  @SuppressWarnings({
    "allcheckers:purity.call", // side effect to local state (StringBuilder)
    "interning:not.interned", // detecting a self-reference requires reference equality
    "signedness:argument" // true positive: might be an unsigned value
  })
  @SideEffectFree
  @Override
  public String toString() {
    if (size == 0) {
      return "{}";
    }
    StringBuilder sb = new StringBuilder(16 * size);
    sb.append('{');
    for (int i = 0; i < size; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      K key = keys[i];
      V value = values[i];
      sb.append(key == this ? "(this Map)" : key);
      sb.append('=');
      sb.append(value == this ? "(this Map)" : value);
    }
    sb.append('}');
    return sb.toString();
  }

  // Defaultable methods

  @SuppressWarnings(
      "modifiability:return" // getOrDefault: given `ArrayMap<K, @PolyModifiable V> this` (where
  // @PolyModifiable cannot vary), @PolyModifiable V is a supertype of V.
  )
  @SideEffectFree
  @Override
  public @PolyModifiable V getOrDefault(
      ArrayMap<K, @PolyModifiable V> this,
      @GuardSatisfied @Nullable @UnknownSignedness Object key,
      @PolyModifiable V defaultValue) {
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
    for (int index = 0;
        index < size && oldSizeModificationCount == sizeModificationCount;
        index++) {
      action.accept(keys[index], values[index]);
    }
    if (oldSizeModificationCount != sizeModificationCount) {
      throw new ConcurrentModificationException();
    }
  }

  @Override
  public void replaceAll(
      @Replaceable ArrayMap<K, V> this, BiFunction<? super K, ? super V, ? extends V> function) {
    Objects.requireNonNull(function);
    if (keys == null) {
      return;
    }
    int oldSizeModificationCount = sizeModificationCount;
    for (int index = 0; index < size; index++) {
      V newValue = function.apply(keys[index], values[index]);
      // Check before writing, so that a stale slot is never overwritten.
      if (oldSizeModificationCount != sizeModificationCount) {
        throw new ConcurrentModificationException();
      }
      values[index] = newValue;
      // Do not increment sizeModificationCount.
    }
  }

  // The receiver is not @Replaceable, even though a key mapped to null gets a new value.  The
  // overridden method's receiver is only @Growable:  a key mapped to null is treated as absent.
  @Override
  public @Nullable V putIfAbsent(@Growable ArrayMap<K, V> this, K key, V value) {
    int hash = Objects.hashCode(key);
    int index = indexOfKey(key, hash);
    if (index == -1 || values[index] == null) {
      put(index, key, hash, value);
      return null;
    } else {
      return values[index];
    }
  }

  @Override
  public boolean remove(
      @Shrinkable ArrayMap<K, V> this,
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
  public boolean replace(@Replaceable ArrayMap<K, V> this, K key, V oldValue, V newValue) {
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
  public @Nullable V replace(@Replaceable ArrayMap<K, V> this, K key, V value) {
    int index = indexOfKey(key);
    if (index == -1) {
      return null;
    }
    V currentValue = values[index];
    values[index] = value;
    // Do not increment sizeModificationCount.
    return currentValue;
  }

  // The receiver is not @Replaceable; see the comment on putIfAbsent.
  @Override
  public @PolyNull V computeIfAbsent(
      @Growable ArrayMap<K, V> this,
      K key,
      Function<? super K, ? extends @PolyNull V> mappingFunction) {
    Objects.requireNonNull(mappingFunction);
    int hash = Objects.hashCode(key);
    int index = indexOfKey(key, hash);
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
      put(index, key, hash, newValue);
    }
    return newValue;
  }

  @Override
  public @PolyNull V computeIfPresent(
      @Shrinkable @Replaceable ArrayMap<K, V> this,
      K key,
      BiFunction<? super K, ? super V, ? extends @PolyNull V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    int index = indexOfKey(key);
    if (index == -1) {
      @SuppressWarnings("nullness:assignment") // computeIfPresent returns null if no mapping
      @PolyNull V result = null;
      return result;
    }
    V oldValue = values[index];
    if (oldValue == null) {
      @SuppressWarnings("nullness:assignment") // computeIfPresent returns null if no mapping
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
      @Modifiable ArrayMap<K, V> this,
      K key,
      BiFunction<? super K, ? super @Nullable V, ? extends @PolyNull V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    int hash = Objects.hashCode(key);
    int index = indexOfKey(key, hash);
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
      put(index, key, hash, newValue);
      return newValue;
    }
  }

  @Override
  public @PolyNull V merge(
      @Modifiable ArrayMap<K, V> this,
      K key,
      @NonNull V value,
      BiFunction<? super V, ? super V, ? extends @PolyNull V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    Objects.requireNonNull(value);
    int hash = Objects.hashCode(key);
    int index = indexOfKey(key, hash);
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
      put(index, key, hash, newValue);
    }
    return newValue;
  }

  /**
   * Returns a copy of this.
   *
   * @return a copy of this
   */
  @SuppressWarnings({
    "allcheckers:purity.assign.field", // side effect to local state (clone)
    "modifiability:override.return" // the clone is modifiable even if the receiver is not
  })
  @SideEffectFree
  @Override
  public @Modifiable ArrayMap<K, V> clone() {
    @Modifiable ArrayMap<K, V> result;
    try {
      @SuppressWarnings({
        "unchecked",
        "growable:assignment", // super.clone() returns a fresh, modifiable object
        "modifiability:assignment" // super.clone() returns a fresh, modifiable object
      })
      @Modifiable ArrayMap<K, V> resultAsArrayMap = (ArrayMap<K, V>) super.clone();
      result = resultAsArrayMap;
    } catch (CloneNotSupportedException e) {
      throw new Error(e); // can't happen
    }
    if (size == 0) {
      result.keys = null;
      result.values = null;
      result.hashes = null;
    } else {
      result.keys = Arrays.copyOf(keys, size);
      result.values = Arrays.copyOf(values, size);
      result.hashes = Arrays.copyOf(hashes, size);
    }
    // The views are inner-class instances bound to this map, so the clone needs its own.
    result.keySet = null;
    result.valuesCollection = null;
    result.entrySet = null;
    result.sizeModificationCount = 0;
    return result;
  }

  /**
   * Returns the internal representation, printed.
   *
   * @return the internal representation, printed
   */
  @SideEffectFree
  /*package*/ String repr() {
    return String.format(
        "size=%d capacity=%d %s %s",
        size, (keys == null ? 0 : keys.length), Arrays.toString(keys), Arrays.toString(values));
  }
}
