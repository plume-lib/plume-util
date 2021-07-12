/**
 *
 *
 * <h2>Plume-lib Util: Utility libraries for Java</h2>
 *
 * <h3 id="Collections_and_iterators">Collections and iterators</h3>
 *
 * <dl>
 *   <dt>{@link org.plumelib.util.ArraysPlume ArraysPlume}
 *   <dd>Utilities for manipulating arrays and collections. This complements java.util.Arrays and
 *       java.util.Collections.
 *   <dt>{@link org.plumelib.util.CollectionsPlume CollectionsPlume}
 *   <dd>Utilities for manipulating collections, iterators, lists, maps, and sets.
 *   <dt>{@link org.plumelib.util.CombinationIterator CombinationIterator}
 *   <dd>Given a set of collections, return all combinations that take one element from each
 *       collection.
 *   <dt>{@link org.plumelib.util.LimitedSizeSet LimitedSizeSet}
 *   <dd>Stores up to some maximum number of unique values, at which point its rep is nulled, in
 *       order to save space.
 *   <dt>{@link org.plumelib.util.LimitedSizeIntSet LimitedSizeIntSet}
 *   <dd>Stores up to some maximum number of unique integer values, at which point its rep is
 *       nulled, in order to save space. More efficient than {@code LimitedSizeSet<Integer>}.
 *   <dt>{@link org.plumelib.util.LimitedSizeLongSet LimitedSizeLongSet}
 *   <dd>Stores up to some maximum number of unique long values, at which point its rep is nulled,
 *       in order to save space. More efficient than {@code LimitedSizeSet<Long>}.
 *   <dt>{@link org.plumelib.util.OrderedPairIterator OrderedPairIterator}
 *   <dd>Given two sequences/iterators/whatever, OrderedPairIterator returns a new
 *       sequence/iterator/whatever that pairs the matching elements of the inputs, according to
 *       their respective sort orders. (This operation is sometimes called "zipping".)
 *   <dt>{@link org.plumelib.util.WeakHasherMap WeakHasherMap}
 *   <dd>WeakHashMap is a modified version of WeakHashMap from JDK 1.2.2, that adds a constructor
 *       that takes a {@link org.plumelib.util.Hasher Hasher} argument.
 *   <dt>{@link org.plumelib.util.WeakIdentityHashMap WeakIdentityHashMap}
 *   <dd>WeakIdentityHashMap is a modified version of WeakHashMap from JDK 1.5, that uses
 *       System.identityHashCode() rather than the object's hash code.
 * </dl>
 *
 * <h3 id="Text_processing">Text processing</h3>
 *
 * <dl>
 *   <dt>{@link org.plumelib.util.EntryReader EntryReader}
 *   <dd>Class that reads "entries" from a file. In the simplest case, entries can be lines. It
 *       supports: include files, comments, and multi-line entries (paragraphs). The syntax of each
 *       of these is customizable.
 *   <dt>{@link org.plumelib.util.FileIOException FileIOException}
 *   <dd>Extends IOException by also reporting a file name and line number at which the exception
 *       occurred.
 *   <dt>{@link org.plumelib.util.FilesPlume}
 *   <dd>Utility methods that create and manipulate files, directories, streams, readers, and
 *       writers.
 *   <dt>{@link org.plumelib.util.FileWriterWithName FileWriterWithName}
 *   <dd>Just like {@code FileWriter}, but adds a {@code getFileName()} method and overrides {@code
 *       toString()} to give the file name.
 *   <dt>{@link org.plumelib.util.RegexUtil RegexUtil}
 *   <dd>Utility methods for regular expressions, most notably for testing whether a string is a
 *       regular expression.
 *   <dt>{@link org.plumelib.util.StringsPlume}
 *   <dd>Utility methods that manipulate Strings: replacement; prefixing and indentation; splitting
 *       and joining; quoting and escaping; whitespace; comparisons; StringTokenizer; debugging
 *       variants of toString; diagnostic output; miscellaneous.
 *       <!--
 *   <dt>{link org.plumelib.util.CountingPrintWriter CountingPrintWriter}
 *   <dd>Prints formatted representations of objects to a text-output stream counting the number of
 *       bytes and characters printed.
 * -->
 *       <!--
 *   <dt>{link org.plumelib.util.Digest Digest}
 *   <dd>Computes a message digest for a file.
 * -->
 * </dl>
 *
 * <h3 id="Math">Math</h3>
 *
 * <dl>
 *   <dt>{@link org.plumelib.util.MathPlume MathPlume}
 *   <dd>Mathematical utilities.
 *   <dt>{@link org.plumelib.util.FuzzyFloat FuzzyFloat}
 *   <dd>Routines for doing approximate ('fuzzy') floating point comparisons. Those are comparisons
 *       that only require the floating point numbers to be relatively close to one another to be
 *       equal, rather than exactly equal.
 * </dl>
 *
 * <h3 id="Random_selection">Random selection</h3>
 *
 * <dl>
 *   <dt>{@link org.plumelib.util.RandomSelector RandomSelector}
 *   <dd>Selects k elements uniformly at random from an arbitrary iterator, using <em>O(k)</em>
 *       space.
 *   <dt>{@link org.plumelib.util.MultiRandSelector MultiRandSelector}
 *   <dd>Like RandomSelector, performs a uniform random selection over an iterator. However, the
 *       objects in the iteration may be partitioned so that the random selection chooses the same
 *       number from each group.
 * </dl>
 *
 * <h3 id="Determinism">Determinism and immutability</h3>
 *
 * <dl>
 *   <dt>{@link org.plumelib.util.DeterministicObject DeterministicObject}
 *   <dd>A version of {@code Object} with a deterministic {@code hashCode()} method. Instantiate
 *       this instead of {@code Object} to remove a source of nondeterminism from your programs.
 *   <dt>{@link org.plumelib.util.ClassDeterministic ClassDeterministic}
 *   <dd>Deterministic versions of {@code java.lang.Class} methods, which return arrays in sorted
 *       order.
 *   <dt>{@link org.plumelib.util.UniqueId UniqueId}
 *   <dd>An interface for objects that have a unique ID. If you are tempted to print the value of
 *       {@code System.identityHashCode()}, consider using this instead.
 *   <dt>{@link org.plumelib.util.UniqueIdMap UniqueIdMap}
 *   <dd>Provides a unique ID (like the {@link org.plumelib.util.UniqueId UniqueId} class) for
 *       classes that you cannot modify.
 *   <dt>{@link org.plumelib.util.ImmutableTypes ImmutableTypes}
 *   <dd>Indicates which types in the JDK are immutable.
 * </dl>
 *
 * <h3 id="interfaces">Utility interfaces</h3>
 *
 * <dl>
 *   <dt>{@link org.plumelib.util.Filter Filter}
 *   <dd>Interface for things that make boolean decisions. This is inspired by {@code
 *       java.io.FilenameFilter}.
 *   <dt>{@link org.plumelib.util.Partitioner Partitioner}
 *   <dd>A Partitioner accepts Objects and assigns them to an equivalence class.
 * </dl>
 *
 * <h3 id="system">JVM runtime system</h3>
 *
 * <dl>
 *   <dt>{@link org.plumelib.util.DumpHeap DumpHeap}
 *   <dd>Dumps the heap into a {@code .hprof} file.
 *   <dt>{@link org.plumelib.util.SystemPlume SystemPlume}
 *   <dd>Utility methods relating to the JVM runtime system: sleep and garbage collection.
 * </dl>
 *
 * <h3 id="miscellaneous">Miscellaneous</h3>
 *
 * <dl>
 *   <dt>{@link org.plumelib.util.GraphPlume GraphPlume}
 *   <dd>Graph utility methods. This class does not model a graph: all methods are static.
 *   <dt>{@link org.plumelib.util.Intern Intern}
 *   <dd>Utilities for interning objects. Interning is also known as canonicalization or
 *       hash-consing: it returns a single representative object that {@code .equals()} the object,
 *       and the client discards the argument and uses the result instead.
 *   <dt>{@link org.plumelib.util.Pair Pair}
 *   <dd>Mutable pair class: type-safely holds two objects of possibly-different types.
 *   <dt>{@link org.plumelib.util.WeakIdentityPair WeakIdentityPair}
 *   <dd>Immutable pair class: type-safely holds two objects of possibly-different types. Differs
 *       from {@code Pair} in the following ways: is immutable, cannot hold null, holds its elements
 *       with weak pointers, and its equals() method uses object equality to compare its elements.
 *   <dt>{@link org.plumelib.util.UtilPlume UtilPlume}
 *   <dd>Utility methods that do not belong elsewhere in the plume package: BitSet; File;
 *       directories; file names; reading and writing; hashing; ProcessBuilder; properties; Stream;
 *       Throwable.
 * </dl>
 */
package org.plumelib.util;
