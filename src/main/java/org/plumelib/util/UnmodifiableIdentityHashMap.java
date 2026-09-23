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
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.IteratorPolyMod;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.PolyShrinkable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.Ungrowable;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.checker.nonempty.qual.PolyNonEmpty;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;

/**
 * Returns an unmodifiable view of an {@link IdentityHashMap}. All mutating operations throw {@link
 * UnsupportedOperationException}, and all other operations delegate to the underlying map. It is
 * possible that another alias to the underlying map asynchronously changes the contents of the
 * underlying map.
 *
 * <p>This class extends {@link IdentityHashMap} only so it is assignable to variables/fields of
 * static type {@link IdentityHashMap}. Any inherited state from the superclass is unused, because
 * all valid operations are delegated to the wrapped map.
 *
 * @param <K> the type of keys of the map
 * @param <V> the type of values of the map
 */
@SuppressWarnings("keyfor") // keyfor: keys for `this` are also keys for `this.map`
public final class UnmodifiableIdentityHashMap<K, V> extends IdentityHashMap<K, V> {

  /** The serial version UID. */
  private static final long serialVersionUID = -5147442142854693854L;

  /**
   * The wrapped map. It may be modifiable, but it will not be modified via this {@code
   * UnmodifiableIdentityHashMap}.
   */
  private final IdentityHashMap<K, V> map;

  /**
   * Creates an UnmodifiableIdentityHashMap. Clients should use {@link #wrap} instead.
   *
   * @param map the map to wrap
   */
  private @Unmodifiable UnmodifiableIdentityHashMap(IdentityHashMap<K, V> map) {
    super();
    this.map = map;
  }

  /**
   * Creates an {@link UnmodifiableIdentityHashMap} wrapper for a map. Returns the argument if it is
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
  public @Nullable V put(
      @Growable @Replaceable @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this,
      K key,
      V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(
      @Growable @Replaceable @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this,
      Map<? extends K, ? extends V> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable V remove(
      @Shrinkable @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this,
      @GuardSatisfied @Nullable @UnknownSignedness Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear(@Shrinkable @GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
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

  // TODO: Implement `clone()`.

  @Override
  @SuppressWarnings(
      "modifiability:return" // returning unmodifiable is OK because this is an unmodifiable map
  )
  public @IteratorPolyMod @PolyShrinkable @Ungrowable @PolyNonEmpty Set<K> keySet(
      @GuardSatisfied @PolyNonEmpty @PolyShrinkable UnmodifiableIdentityHashMap<K, V> this) {
    return Collections.unmodifiableSet(map.keySet());
  }

  @Override
  @SuppressWarnings(
      "modifiability:return" // returning unmodifiable is OK because this is an unmodifiable map
  )
  public @IteratorPolyMod @PolyShrinkable @Ungrowable @PolyNonEmpty Collection<V> values(
      @PolyShrinkable @GuardSatisfied @PolyNonEmpty UnmodifiableIdentityHashMap<K, V> this) {
    return Collections.unmodifiableCollection(map.values());
  }

  @Override
  @SuppressWarnings(
      "modifiability:return" // returning unmodifiable is OK because this is an unmodifiable map
  )
  public @IteratorPolyMod @PolyShrinkable @Ungrowable @PolyNonEmpty Set<
          Map.@PolyModifiable Entry<@KeyFor({"this"}) K, V>>
      entrySet(
          @PolyModifiable @GuardSatisfied @PolyNonEmpty UnmodifiableIdentityHashMap<K, V> this) {
    return Collections.unmodifiableMap(map).entrySet();
  }

  // `action` has no side effects on the map, because it is only passed keys and values.
  @Override
  public void forEach(BiConsumer<? super K, ? super V> action) {
    map.forEach(action);
  }

  @Override
  public void replaceAll(
      @Replaceable UnmodifiableIdentityHashMap<K, V> this,
      BiFunction<? super K, ? super V, ? extends V> function) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString(@GuardSatisfied UnmodifiableIdentityHashMap<K, V> this) {
    return map.toString();
  }

  @Override
  @SuppressWarnings(
      "modifiability:return" // getOrDefault: given `ArrayMap<K, @PolyModifiable V> this` (where
  // @PolyModifiable cannot vary), @PolyModifiable V is a supertype of V.
  )
  public @PolyModifiable V getOrDefault(
      UnmodifiableIdentityHashMap<K, @PolyModifiable V> this,
      @GuardSatisfied @UnknownSignedness Object key,
      @PolyModifiable V defaultValue) {
    return map.getOrDefault(key, defaultValue);
  }

  @Override
  public V putIfAbsent(@Growable UnmodifiableIdentityHashMap<K, V> this, K key, V value) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings({"lock:unneeded.suppression", "lock:override.param"})
  @Override
  public boolean remove(
      @Shrinkable UnmodifiableIdentityHashMap<K, V> this,
      @GuardSatisfied @UnknownSignedness Object key,
      @GuardSatisfied @UnknownSignedness Object value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean replace(
      @Replaceable UnmodifiableIdentityHashMap<K, V> this, K key, V oldValue, V newValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public V replace(@Replaceable UnmodifiableIdentityHashMap<K, V> this, K key, V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @PolyNull V computeIfAbsent(
      @Growable UnmodifiableIdentityHashMap<K, V> this,
      K key,
      Function<? super K, ? extends @PolyNull V> mappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable V computeIfPresent(
      @Shrinkable @Replaceable UnmodifiableIdentityHashMap<K, V> this,
      K key,
      BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable V compute(
      @Modifiable UnmodifiableIdentityHashMap<K, V> this,
      K key,
      BiFunction<? super K, ? super V, ? extends @Nullable V> remappingFunction) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable V merge(
      @Modifiable UnmodifiableIdentityHashMap<K, V> this,
      K key,
      V value,
      BiFunction<? super V, ? super V, ? extends @Nullable V> remappingFunction) {
    throw new UnsupportedOperationException();
  }
}
