package org.plumelib.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * A union-find (disjoint-set) data structure. Elements must not be null. Elements are used as hash
 * keys, so an element's {@code equals} and {@code hashCode} must not change while it is in the
 * structure. This class is not thread-safe.
 *
 * <p>The structure partitions its elements into disjoint sets. Each set has a distinguished
 * element, its <i>representative</i>. Two elements are in the same set if and only if they have the
 * same representative. The two fundamental operations are:
 *
 * <ul>
 *   <li>{@link #find}: return the representative of the set containing an element.
 *   <li>{@link #union}: merge the two sets containing two elements into a single set.
 * </ul>
 *
 * <p>Both operations run in nearly constant amortized time, using union by rank and path
 * compression.
 *
 * <p>In addition to the usual union-find operations, this implementation supports:
 *
 * <ul>
 *   <li>Listing all the elements of a set; see {@link #elementsInSameSetAs} and {@link #allSets}.
 *   <li>Maintaining one unary predicate and one binary predicate over the sets; see {@link
 *       #test(Object)} and {@link #test(Object, Object)}.
 * </ul>
 *
 * <p><b>Predicates.</b> A client may supply, to the constructor, a unary predicate and/or a binary
 * predicate. Functions {@link #test(Object)} and {@link #test(Object, Object)} lift them to
 * predicates over sets:
 *
 * <ul>
 *   <li>{@code test(S)} is true if {@code p(x)} is true for some element {@code x} of {@code S}.
 *   <li>{@code test(S1, S2)} is true if {@code q(x, y)} is true for some {@code x} in {@code S1}
 *       and some {@code y} in {@code S2}.
 * </ul>
 *
 * <p>Querying through any member of a set gives the same answer as through any other member.
 *
 * <p>The lifted predicates are existential ("there exists") properties, so they are monotonic: as a
 * set grows, the predicate can only change from false to true, never the reverse. Therefore
 * unioning preserves them:
 *
 * <ul>
 *   <li>If {@code p(a)} is true, then after {@code z = union(a, e)}, {@code p(z)} is true.
 *   <li>If {@code q(a, b)} is true, then:
 *       <ul>
 *         <li>after {@code x = union(a, c)}, {@code q(x, b)} is true, and
 *         <li>after {@code y = union(b, d)}, {@code q(a, y)} is true.
 *       </ul>
 * </ul>
 *
 * <p><b>Lazy evaluation and caching.</b> A lifted predicate's value for a set (or ordered pair of
 * sets) is computed on demand, cached, and incrementally maintained. The binary-predicate cache can
 * hold an entry per ordered pair of sets, so it may use space quadratic in the number of sets, and
 * each {@link #union} scans the cached pairs to maintain them. Clients that query many cross-pairs
 * among many small sets pay for this in time and space.
 *
 * @param <E> the type of the elements
 */
@SuppressWarnings("lock") // not yet annotated for the Lock Checker
public class UnionFind<E extends Object> {

  /**
   * Maps each element to its parent in the union-find forest. An element is a representative (a
   * root) if and only if it maps to itself. Every element that has been added is a key.
   */
  private final Map<E, E> parent = new HashMap<>();

  /**
   * Maps each representative to the rank (an upper bound on the height) of its tree. Non-root
   * elements' ranks are not meaningful and are not consulted.
   */
  private final Map<E, Integer> rank = new HashMap<>();

  /** Maps each representative to the list of all elements in its set. */
  private final Map<E, List<E>> members = new HashMap<>();

  /** The client's unary predicate, or null if none was supplied. */
  private final @Nullable Predicate<? super E> unaryPredicate;

  /** The client's binary predicate, or null if none was supplied. */
  private final @Nullable BiPredicate<? super E, ? super E> binaryPredicate;

  // The cached value is in one of three states: true, false, or not-yet-computed; only the first
  // two are ever visible to a client, through the boolean that a getter returns. A getter for a
  // not-yet-computed value evaluates the predicate over all the relevant elements. A cached false
  // value is maintained incrementally: when a union enlarges a set, the client's predicate is
  // evaluated only on the newly-formed elements or pairs, never re-evaluated on those already
  // checked.

  /**
   * Caches the value of the lifted unary predicate for each set. The key is the set's
   * representative. A value of {@code Boolean.TRUE} or {@code Boolean.FALSE} is the cached value;
   * an absent key means the value has not been computed.
   */
  private final Map<E, Boolean> unaryCache = new HashMap<>();

  /**
   * Caches the value of the lifted binary predicate for each ordered pair of sets. The value for
   * the pair {@code (r1, r2)} of representatives is {@code binaryCache.get(r1).get(r2)}. A value of
   * {@code Boolean.TRUE} or {@code Boolean.FALSE} is the cached value; an absent inner or outer key
   * means the value has not been computed.
   */
  private final Map<E, Map<E, Boolean>> binaryCache = new HashMap<>();

  /** Creates a new, empty union-find structure with no predicates. */
  public UnionFind() {
    this(null, null);
  }

  /**
   * Creates a new, empty union-find structure with the given predicates.
   *
   * @param unaryPredicate the unary predicate to lift to sets, or null for none
   * @param binaryPredicate the binary predicate to lift to ordered pairs of sets, or null for none
   */
  public UnionFind(
      @Nullable Predicate<? super E> unaryPredicate,
      @Nullable BiPredicate<? super E, ? super E> binaryPredicate) {
    this.unaryPredicate = unaryPredicate;
    this.binaryPredicate = binaryPredicate;
  }

  /**
   * Adds an element as a new singleton set. Does nothing if the element is already present.
   *
   * @param e the element to add
   * @return true if the element was added, false if it was already present
   */
  public boolean add(E e) {
    if (e == null) {
      throw new IllegalArgumentException("cannot add null to a UnionFind");
    }
    if (parent.containsKey(e)) {
      return false;
    }
    parent.put(e, e);
    rank.put(e, 0);
    List<E> singleton = new ArrayList<>();
    singleton.add(e);
    members.put(e, singleton);
    return true;
  }

  /**
   * Adds each element as a new singleton set. Elements that are already present are left unchanged.
   *
   * @param es the elements to add
   * @return true if this structure changed as a result of the call
   */
  public boolean addAll(Collection<? extends E> es) {
    boolean changed = false;
    for (E e : es) {
      changed |= add(e);
    }
    return changed;
  }

  /**
   * Returns true if the element is present in this structure.
   *
   * @param e the element to test
   * @return true if the element has been added
   */
  public boolean contains(E e) {
    return parent.containsKey(e);
  }

  /**
   * Returns the number of elements in this structure.
   *
   * @return the number of elements
   */
  public int size() {
    return parent.size();
  }

  /**
   * Returns the number of disjoint sets.
   *
   * @return the number of sets
   */
  public int numberOfSets() {
    return members.size();
  }

  /**
   * Returns the representative of the set containing the given element. Two elements are in the
   * same set if and only if this method returns the same representative for both.
   *
   * @param e the element whose representative to return
   * @return the representative of the set containing {@code e}
   * @throws NoSuchElementException if {@code e} is not present
   */
  public E find(E e) {
    E p = parent.get(e);
    if (p == null) {
      throw new NoSuchElementException("Not in union-find structure: " + e);
    }
    if (p.equals(e)) {
      return e;
    }
    E root = find(p);

    // Path compression makes every element on the path from {@code e} to the
    // root point directly at the root, which speeds up future calls.
    parent.put(e, root);

    return root;
  }

  /**
   * Merges the set containing {@code a} and the set containing {@code b} into a single set, and
   * returns the representative of the merged set. Adds {@code a} or {@code b} as a new singleton
   * set if it is not already present.
   *
   * <p>Cached predicate values are preserved. A true value stays true. A false value is maintained
   * incrementally: the client's predicate is evaluated on the newly-formed elements or pairs, and
   * the value becomes true if any of them satisfies it.
   *
   * @param a an element
   * @param b an element
   * @return the representative of the set that contains both {@code a} and {@code b}
   */
  public E union(E a, E b) {
    add(a);
    add(b);
    E ra = find(a);
    E rb = find(b);
    if (ra.equals(rb)) {
      return ra;
    }

    // Union by rank: attach the lower-rank tree under the higher-rank tree, so the winner becomes
    // the representative of the merged set.
    int rankA = rankOf(ra);
    int rankB = rankOf(rb);
    E winner;
    E loser;
    if (rankA < rankB) {
      winner = rb;
      loser = ra;
    } else {
      winner = ra;
      loser = rb;
      if (rankA == rankB) {
        rank.put(winner, rankA + 1);
      }
    }
    rank.remove(loser);

    transferMetadata(loser, winner);

    parent.put(loser, winner);
    return winner;
  }

  /**
   * Moves the loser's members and maintains cached predicate values, when {@code loser} stops being
   * a representative because its set was merged into the winner's set.
   *
   * @param loser the former representative
   * @param winner the new representative
   */
  private void transferMetadata(E loser, E winner) {
    List<E> loserMembers = members.get(loser);
    assert loserMembers != null : "@AssumeAssertion(nullness): loser was a representative";
    // The winner's members before the merge; that is, the elements of the winning set.
    // It is not mutated by `maintainUnary()` or `maintainBinary()`.
    List<E> oldWinnerMembers = membersOf(winner);

    maintainUnary(winner, loser, oldWinnerMembers, loserMembers);
    maintainBinary(winner, loser, oldWinnerMembers, loserMembers);

    // Combine the two member lists into one, which is stored under the winner (the representative
    // of the merged set). Union by rank chooses the representative but does not bound the two sets'
    // sizes, so append the smaller list to the larger one -- independent of which set won -- to
    // keep total member-list maintenance to O(n log n).
    if (oldWinnerMembers.size() >= loserMembers.size()) {
      oldWinnerMembers.addAll(loserMembers);
    } else {
      loserMembers.addAll(oldWinnerMembers);
      members.put(winner, loserMembers);
    }
    members.remove(loser);
  }

  /**
   * Returns true if the two elements are in the same set. Adds neither element.
   *
   * @param a an element
   * @param b an element
   * @return true if {@code a} and {@code b} are in the same set
   * @throws NoSuchElementException if {@code a} or {@code b} is not present
   */
  public boolean sameSet(E a, E b) {
    return find(a).equals(find(b));
  }

  /**
   * Returns all the elements in the same set as the given element. The returned list is an
   * unmodifiable snapshot; it is not affected by subsequent operations.
   *
   * @param e an element
   * @return all elements in the same set as {@code e}, including {@code e} itself
   * @throws NoSuchElementException if {@code e} is not present
   */
  public List<E> elementsInSameSetAs(E e) {
    E root = find(e);
    return Collections.unmodifiableList(new ArrayList<>(membersOf(root)));
  }

  /**
   * Returns the member list of a representative.
   *
   * @param root a representative
   * @return the list of members of {@code root}'s set
   */
  private List<E> membersOf(E root) {
    List<E> result = members.get(root);
    if (result == null) {
      throw new Error("not a representative: " + root);
    }
    return result;
  }

  /**
   * Returns all the sets, each as a list of its elements. The returned collection and its lists are
   * unmodifiable snapshots.
   *
   * @return all the sets
   */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree") // side effect to local state
  @SideEffectFree
  public Collection<List<E>> allSets() {
    List<List<E>> result = new ArrayList<>(members.size());
    for (List<E> set : members.values()) {
      result.add(Collections.unmodifiableList(new ArrayList<>(set)));
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns the rank of a representative.
   *
   * @param root a representative
   * @return the rank of {@code root}
   */
  private int rankOf(E root) {
    Integer result = rank.get(root);
    assert result != null : "@AssumeAssertion(nullness): every representative has a rank";
    return result;
  }

  @SuppressWarnings("allcheckers:purity.not.sideeffectfree") // side effect to local state
  @SideEffectFree
  @Override
  public String toString() {
    return "UnionFind" + allSets();
  }

  // ---------------------------------------------------------------------------
  // Predicates
  // ---------------------------------------------------------------------------

  /**
   * Returns true if the unary predicate holds of the set containing the given element; that is, if
   * the client's unary predicate is true of some element of that set.
   *
   * @param e an element of the set
   * @return true if the unary predicate holds of {@code e}'s set
   * @throws IllegalStateException if no unary predicate was supplied to the constructor
   * @throws NoSuchElementException if {@code e} is not present
   */
  public boolean test(E e) {
    if (unaryPredicate == null) {
      throw new IllegalStateException("no unary predicate was supplied");
    }
    if (!contains(e)) {
      throw new NoSuchElementException("Not in union-find structure: " + e);
    }
    E root = find(e);
    Boolean cached = unaryCache.get(root);
    if (cached != null) {
      return cached;
    }
    boolean result = anyUnary(membersOf(root));
    unaryCache.put(root, result);
    return result;
  }

  /**
   * Returns true if the binary predicate holds of the ordered pair of sets containing the given
   * elements; that is, if the client's binary predicate is true of some ordered pair of an element
   * of {@code a}'s set and an element of {@code b}'s set.
   *
   * @param a an element of the first set
   * @param b an element of the second set
   * @return true if the binary predicate holds of the ordered pair ({@code a}'s set, {@code b}'s
   *     set)
   * @throws IllegalStateException if no binary predicate was supplied to the constructor
   * @throws NoSuchElementException if {@code a} or {@code b} is not present
   */
  public boolean test(E a, E b) {
    if (binaryPredicate == null) {
      throw new IllegalStateException("no binary predicate was supplied");
    }
    if (!contains(a)) {
      throw new NoSuchElementException("Not in union-find structure: " + a);
    }
    if (!contains(b)) {
      throw new NoSuchElementException("Not in union-find structure: " + b);
    }
    E r1 = find(a);
    E r2 = find(b);
    Boolean cached = rowGet(binaryCache.get(r1), r2);
    if (cached != null) {
      return cached;
    }
    boolean result = anyBinary(membersOf(r1), membersOf(r2));
    store(r1, r2, result);
    return result;
  }

  /**
   * Updates the unary-predicate cache when the winning and losing sets merge.
   *
   * @param winner the new representative
   * @param loser the former representative
   * @param oldWinnerMembers the elements of the winning set (before the merge)
   * @param loserMembers the elements of the losing set
   */
  private void maintainUnary(E winner, E loser, List<E> oldWinnerMembers, List<E> loserMembers) {
    if (unaryPredicate == null) {
      return;
    }
    // The merged value is the "or" of the two sets' values.
    Boolean merged =
        combineUnary(
            unaryCache.get(winner), oldWinnerMembers, unaryCache.remove(loser), loserMembers);
    if (merged == null) {
      unaryCache.remove(winner);
    } else {
      unaryCache.put(winner, merged);
    }
  }

  /**
   * Combines two sets' unary-predicate values into the value for their union. Returns true if
   * either value is known true; null (not computed) if neither value is known; otherwise a definite
   * value, computing an unknown value by evaluating the predicate on that set's (new) elements.
   *
   * @param value1 the first set's cached value, or null if not computed
   * @param elements1 the first set's elements
   * @param value2 the second set's cached value, or null if not computed
   * @param elements2 the second set's elements
   * @return the combined value, or null if it remains not computed
   */
  @RequiresNonNull("unaryPredicate")
  private @Nullable Boolean combineUnary(
      @Nullable Boolean value1, List<E> elements1, @Nullable Boolean value2, List<E> elements2) {
    if (Boolean.TRUE.equals(value1) || Boolean.TRUE.equals(value2)) {
      return Boolean.TRUE;
    }
    // Each value is now null or false.
    if (value1 == null && value2 == null) {
      return null;
    }
    if (value1 == null && anyUnary(elements1)) {
      return Boolean.TRUE;
    }
    if (value2 == null && anyUnary(elements2)) {
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  /**
   * Returns true if the client's unary predicate is true of any of the given elements.
   *
   * @param elements the elements to test
   * @return true if the predicate holds of any element
   */
  @RequiresNonNull("unaryPredicate")
  private boolean anyUnary(List<E> elements) {
    for (E x : elements) {
      if (unaryPredicate.test(x)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Updates the binary-predicate cache when the winning and losing sets merge.
   *
   * <p>Every cached pair that mentions the winner or loser must be rewritten to mention only the
   * winner, combining values by "or". This is done in place: the winner's cached values are updated
   * but never removed, and only loser-related entries are deleted. The merged set's pairs with any
   * other set {@code C} decompose into the winner's block and the loser's block against {@code C};
   * the merged set's self-pair decomposes into the four blocks of {winner, loser} × {winner,
   * loser}. A cached value is reused rather than recomputed; a not-yet-cached block is evaluated
   * only when its value is needed.
   *
   * @param winner the new representative
   * @param loser the former representative
   * @param oldWinnerMembers the elements of the winning set (before the merge)
   * @param loserMembers the elements of the losing set
   */
  private void maintainBinary(E winner, E loser, List<E> oldWinnerMembers, List<E> loserMembers) {
    if (binaryPredicate == null) {
      return;
    }

    // Self-pair (winner, winner): the four blocks of {winner, loser} × {winner, loser}.
    Boolean ww = rowGet(binaryCache.get(winner), winner);
    Boolean ll = rowGet(binaryCache.get(loser), loser);
    Boolean wl = rowGet(binaryCache.get(winner), loser);
    Boolean lw = rowGet(binaryCache.get(loser), winner);
    if (ww != null || ll != null || wl != null || lw != null) {
      if (Boolean.TRUE.equals(ww)) {
        // The winner's self-pair is already true; nothing to do.
      } else if (Boolean.TRUE.equals(ll) || Boolean.TRUE.equals(wl) || Boolean.TRUE.equals(lw)) {
        store(winner, winner, true);
      } else {
        boolean result =
            testNoCache(winner, oldWinnerMembers, winner, oldWinnerMembers)
                || testNoCache(loser, loserMembers, loser, loserMembers)
                || testNoCache(winner, oldWinnerMembers, loser, loserMembers)
                || testNoCache(loser, loserMembers, winner, oldWinnerMembers);
        store(winner, winner, result);
      }
      removePair(loser, loser);
      removePair(winner, loser);
      removePair(loser, winner);
    }

    // Pairs (winner, x): fold the loser's block (loser, x) into the winner's row.
    Map<E, Boolean> winnerRow = binaryCache.get(winner);
    if (winnerRow != null) {
      for (E x : new ArrayList<>(winnerRow.keySet())) {
        if (!x.equals(winner)) {
          assert !x.equals(loser) : "(winner, loser) was removed by the self-pair block";
          if (!Boolean.TRUE.equals(winnerRow.get(x))
              && testNoCache(loser, loserMembers, x, membersOf(x))) {
            store(winner, x, true);
          }
        }
        removePair(loser, x);
      }
    }

    // Pairs (x, winner): fold the loser's block (x, loser) into the winner's column.
    for (E x : new ArrayList<>(binaryCache.keySet())) {
      if (x.equals(winner)) {
        continue;
      }
      Map<E, Boolean> row = binaryCache.get(x);
      if (row == null || !row.containsKey(winner)) {
        continue;
      }
      assert !x.equals(loser) : "(loser, winner) was removed by the self-pair block";
      if (!Boolean.TRUE.equals(row.get(winner))
          && testNoCache(x, membersOf(x), loser, loserMembers)) {
        store(x, winner, true);
      }
      removePair(x, loser);
    }

    // Remaining pairs (loser, x): (winner, x) is absent here, so the merged (winner, x) is the
    // loser's block if it is true, else the winner's block.
    Map<E, Boolean> loserRow = binaryCache.get(loser);
    if (loserRow != null) {
      for (E x : new ArrayList<>(loserRow.keySet())) {
        assert rowGet(binaryCache.get(winner), x) == null : "(winner, x) is absent";
        if (Boolean.TRUE.equals(loserRow.get(x))) {
          store(winner, x, true);
        } else {
          store(winner, x, anyBinary(oldWinnerMembers, membersOf(x)));
        }
      }
      binaryCache.remove(loser);
    }

    // Remaining pairs (x, loser): (x, winner) is absent here.
    for (E x : new ArrayList<>(binaryCache.keySet())) {
      Map<E, Boolean> row = binaryCache.get(x);
      if (row == null || !row.containsKey(loser)) {
        continue;
      }
      assert rowGet(binaryCache.get(x), winner) == null : "(x, winner) is absent";
      if (Boolean.TRUE.equals(row.get(loser))) {
        store(x, winner, true);
      } else {
        store(x, winner, anyBinary(membersOf(x), oldWinnerMembers));
      }
      removePair(x, loser);
    }
  }

  /**
   * Like {@link #test(Object, Object)} on two representatives, but does not write to the cache and
   * takes each representative's members explicitly. Reads the cache; on a miss, evaluates the
   * predicate over the given members without caching the result. Passing the members avoids a
   * lookup in the members map.
   *
   * @param r1 the first representative
   * @param members1 the members of {@code r1}'s set
   * @param r2 the second representative
   * @param members2 the members of {@code r2}'s set
   * @return true if the lifted binary predicate holds of the ordered pair ({@code r1}'s set, {@code
   *     r2}'s set)
   */
  @RequiresNonNull("binaryPredicate")
  private boolean testNoCache(E r1, List<E> members1, E r2, List<E> members2) {
    Boolean cached = rowGet(binaryCache.get(r1), r2);
    if (cached != null) {
      return cached;
    }
    return anyBinary(members1, members2);
  }

  /**
   * Removes the cached binary-predicate value for a pair of representatives, pruning the row if it
   * becomes empty. Does nothing if the pair is not cached.
   *
   * @param r1 the first representative
   * @param r2 the second representative
   */
  private void removePair(E r1, E r2) {
    Map<E, Boolean> row = binaryCache.get(r1);
    if (row != null) {
      row.remove(r2);
      if (row.isEmpty()) {
        binaryCache.remove(r1);
      }
    }
  }

  /**
   * Returns the cached binary-predicate value for a pair of representatives, or null if the given
   * row is null or has no entry for the second representative.
   *
   * @param <T> the type of the elements
   * @param row a row of the binary cache (values keyed by the second representative), or null
   * @param second the second representative
   * @return the cached value, or null if not present
   */
  private static <T extends Object> @Nullable Boolean rowGet(
      @Nullable Map<T, Boolean> row, T second) {
    return row == null ? null : row.get(second);
  }

  /**
   * Stores a computed binary-predicate value, unless it is null (not computed).
   *
   * @param r1 the first representative
   * @param r2 the second representative
   * @param value the value to store, or null to leave the pair uncached
   */
  private void store(E r1, E r2, @Nullable Boolean value) {
    if (value != null) {
      binaryCache.computeIfAbsent(r1, k -> new HashMap<>()).put(r2, value);
    }
  }

  /**
   * Returns true if the client's binary predicate is true of any ordered pair drawn from the two
   * lists.
   *
   * @param lefts the candidate first elements
   * @param rights the candidate second elements
   * @return true if the predicate holds of any ordered pair
   */
  @RequiresNonNull("binaryPredicate")
  private boolean anyBinary(List<E> lefts, List<E> rights) {
    for (E x : lefts) {
      for (E y : rights) {
        if (binaryPredicate.test(x, y)) {
          return true;
        }
      }
    }
    return false;
  }
}
