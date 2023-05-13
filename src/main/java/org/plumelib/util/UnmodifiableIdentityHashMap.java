package org.plumelib.util;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;

/**
 * A wrapper around an {@link IdentityHashMap} that makes it unmodifiable. All mutating operations
 * throw {@link UnsupportedOperationException}, and all other operations delegate to the underlying
 * map.
 *
 * <p>This class extends {@link IdentityHashMap} only so it is assignable to variables / fields of
 * static type {@link IdentityHashMap}. All valid operations are delegated to the wrapped map, and
 * any inherited state from the superclass is unused.
 *
 * @param <K> the type of keys of the map
 * @param <V> the type of values of the map
 */
@SuppressWarnings("keyfor") // Keys for `this` are also keys for `this.map`
public class UnmodifiableIdentityHashMap<K, V> extends IdentityHashMap<K, V> {

  /** The serial version UID. */
  private static final long serialVersionUID = -5147442142854693854L;

  /** The wrapped map. */
  private final IdentityHashMap<K, V> map;

  /**
   * Create an UnmodifiableIdentityHashMap. Clients should use {@link #wrap} instead.
   *
   * @param map the map to wrap
   */
  private UnmodifiableIdentityHashMap(IdentityHashMap<K, V> map) {
    this.map = map;
  }

  /**
   * Create an {@link UnmodifiableIdentityHashMap} wrapper for a map. Returns the argument if it is
   * already an {@link UnmodifiableIdentityHashMap}.
   *
   * @param map the map to wrap
   * @return the wrapper
   * @param <K> the key type
   * @param <V> the value type
   */
  public static <K, V> UnmodifiableIdentityHashMap<K, V> wrap(IdentityHashMap<K, V> map) {
    // avoid repeated wrapping
    if (map instanceof UnmodifiableIdentityHashMap) {
      return (UnmodifiableIdentityHashMap<K, V>) map;
    }
    return new UnmodifiableIdentityHashMap<>(map);
  }

  @Override
  public @NonNegative int size(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    return map.size();
  }

  @Override
  public boolean isEmpty(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    return map.isEmpty();
  }

  @Override
  public @Nullable V get(
      @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this,
      @GuardSatisfied @Nullable @UnknownSignedness Object key) {
    return map.get(key);
  }

  @Override
  public boolean containsKey(
      @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this,
      @GuardSatisfied @Nullable @UnknownSignedness Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(
      @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this,
      @GuardSatisfied @Nullable @UnknownSignedness Object value) {
    return map.containsValue(value);
  }

  @Override
  public @Nullable V put(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this, K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(
      @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this, Map<? extends K, ? extends V> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable V remove(
      @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this,
      @GuardSatisfied @Nullable @UnknownSignedness Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("IdentityHashMapUsage")
  public boolean equals(
      @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this, @Nullable @GuardSatisfied Object o) {
    return map.equals(o);
  }

  @Override
  public int hashCode(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    return map.hashCode();
  }

  @Override
  public Set<K> keySet(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    return Collections.unmodifiableSet(map.keySet());
  }

  @Override
  public Collection<V> values(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    return Collections.unmodifiableCollection(map.values());
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    return Collections.unmodifiableSet(map.entrySet());
  }

  // `action` has no side effects on the map, because it is only passed keys and values.
  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    map.forEach(action);
  }

  @Override
  public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    return map.toString();
  }

  @Override
  public V getOrDefault(@GuardSatisfied @UnknownSignedness Object key, V defaultValue) {
    return map.getOrDefault(key, defaultValue);
  }

  @Override
  public V putIfAbsent(K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(
      @GuardSatisfied @UnknownSignedness Object key,
      @GuardSatisfied @UnknownSignedness Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public V replace(K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @PolyNull V computeIfAbsent(
      K key, Function<? super K, ? extends @PolyNull V> mappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @PolyNull V computeIfPresent(
      K key, BiFunction<? super K, ? super V, ? extends @PolyNull V> remappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @PolyNull V compute(
      K key, BiFunction<? super K, ? super V, ? extends @PolyNull V> remappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @PolyNull V merge(
      K key, V value, BiFunction<? super V, ? super V, ? extends @PolyNull V> remappingFunction) {
    throw new UnsupportedOperationException();
  }
}
