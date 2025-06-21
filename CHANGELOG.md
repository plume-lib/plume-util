# Plume-Util change log

## 2.0.0 (????-??-??)

- Require Java 11.
- Renamed `*Plume` classes to `*P`, for brevity; for example, use `CollectionsP` instead of `CollectionsPlume`.
- Removed all deprecated classes and mehods.

## 1.11.0 (2025-06-??)

- New methods in `CollectionsPlume`:
   * `addAll(Iterable)`
   * `iteratorPlusOne()`
   * `mergedIterator2()`
   * `mergedIterator()`
   * `filteredIterator()`
   * `removeFirstAndLastIterator()`
- Use of nested classes in `CollectionsPlume` is deprecated.

- Moved methods from `StringsPlume` to `CollectionsPlume`:
   * `mapToStringAndClass()`
   * `mapToStringLinewise()`
   * The old versions are deprecated.
- Renamed methods `mapToString()` and `mapToString()` to `mapToStringMultiLine()`.
   * The old versions are deprecated.
- Overloaded versions of `mapToStringMultiLine()` that take a line prefix as an argument.

## 1.10.1 (2025-04-02)

- New methods in `ArraysPlume`:
   * `contains()`
   * `mapArray()`
   * `replaceAll()`
- Do not require Java 11; runs under JDK 8.

## 1.10.0 (2024-10-26)

- `OrderedPairIterator`: uses `IPair` instead of `MPair`
- `ArraysPlume`:  deprecated `sorted()` in favor of `isSorted()`
- `StringsPlume`:
    * `firstLineSeparator()` can return null
    * new method `mapToStringLinewise()`
    * new method `isVersionNumberLE()`
    * new class `VersionNumberComparator`
- `CollectionsPlume`:  new methods `anyMatch()`, `allMatch()`, `noneMatch()`

## 1.9.3 (2024-04-19)

- `CollectionsPlume`:
    * deprecated `Filter`; use `java.util.function.Predicate` instead
    * new method `replace(Collection<T> c, Collection<Replacement<T>> replacements)`
    * new method `isSubsequenceMaybeNonContiguous(Iterable<T>, Iterable<T>)`
- `StringsPlume`:
    * new methods `splitRetainSeparators()` and `splitLinesRetainSeparators()`
- `FilesPlume`:
    * deprecate `fileContents()` in favor of new method `readString()`
    * deprecate `writeFile()` in favor of new method `writeString()`
    * new method `readLinesRetainingSeparators()`
    * new method `writeString(Path, String)`

## 1.9.2 (2024-04-07)

- Don't use `@InlineMe`, which breaks javac in some clients.

## 1.9.1 (2024-04-06)

- `FilesPlume`:
    * deprecate `readFile()` in favor of `fileContents()`
- `StringsPlume`:
   * new method `firstLineSeparator(String)`
   * improved behavior of `splitLines(String)`
- `CollectionsPlume`:
   * new method `indexOf(list, value, startIndex)`

## 1.9.0 (2023-12-08)

- `StringsPlume`:
    * `rpad` and `lpad` add an ellipsis ("...") if it truncates
    * `rpad(double, ...)` does not truncate values before the decimal point
    * add `rpad` that pads with an arbitrary character
    * add `rpad` that never truncates
    * add `lpad` that never truncates
- `CollectionsPlume`:
   * add methods `duplicates()` and `listFilter()`
   * add an overload for `mapCapacity()`
   * deprecated `noDuplicates()`; use `hasNoDuplicates()` instead
   * deprecated `listFilter()`; use `filter()` instead
- `ArraysPlume`:
   * deprecated `noDuplicates()`; use `hasNoDuplicates()` instead

## 1.8.1 (2023-06-02)

- `CollectionsPlume`: remove conflicting overrides of `deepCopy()`
- `IPair` and `MPair`: rename fields from `a` and `b` to `first` and `second`
- `MPair`: new methods `cloneElements()`, `deepCopy()`, `deepCopyFirst()`, `deepCopySecond()`
- `StringsPlume`: add `rpad` that pads with an arbitrary character
- `FilesPlume`: new `InputStream` methods `available()`, `isWhitespaceOnly()`, `readCodePoint()`

## 1.8.0 (2023-06-02)

- Deprecated `Pair` class for mutable pairs; use `MPair` instead.
  `OrderedPairIterator` uses `MPair` instead of `Pair`.
- New class `IPair` for immutable pairs.
- The `WeakIdentityPair` constructor is deprecated in favor of the `of()` method.
- New interface `DeepCopyable`.
- New methods in `UtilPlume`:
   * `firstNonNull()`
- New methods in `CollectionsPlume`:
   * for `Collection`s: `cloneElements()`, `deepCopy()`
   * for `Map`s: `cloneElements()`, `cloneValues()`, `createLruCache()`

## 1.7.0 (2023-05-10)

- Moved classes `Hasher`, `WeakHasherMap`, and `WeakIdentityHashMap`
  to a new project, hashmap-util.  It is available on Maven Central.
- In `ArraySet`:
   * new method `ArraySet.sort`.
   * widened the formal parameter type of `newArraySetOrHashSet` and `newArraySetOrLinkedHashSet`.
- New methods in `CollectionsPlume`:
   * `sortedSetContainsAll`, `sortedSetEquals`, and `withoutDuplicatesSorted`.
   * `deepCopy(List)`, `deepCopy(TreeSet)`, `deepCopy(Map)`, and `deepCopyValues(Map)`.
- New method `UtilPlume.clone(Object)`.
- New method `StringsPlume.toStringTruncated(Object, int)`.
- In `ArraysPlume`, the generic signatures of `isSubarrayEq` and `indexOfEq`
  have changed.  Let us know if this causes a problem.

## 1.6.5 (2023-01-08)

- New methods `newArrayMapOrHashMap`, `newArrayMapOrLinkedHashMap`,
  `newArraySetOrHashSet`, and `newArraySetOrLinkedHashSet`.
- Bug fixes in collection methods.

## 1.6.2 (2022-01-07)

- New method `StringsPlume.isBlank()`.
- Don't use `@InlineMe`, which breaks javac in some clients.

## 1.6.1 (2022-01-06)

- New methods in `CollectionsPlume`: `adjoin`, `adjoinAll`, `listUnion`, `listIntersection`.
- New classes `ArraySet` and `IdentityArraySet` for very small sets.
- New classes `MostlySingletonSet`, `AbstractMostlySingletonSet`, and `IdentityMostlySingletonSet`.
- New class `UnmodifiableIdentityHashMap`.

## 1.6.0 (2022-10-17)

- New class `ArrayMap` is an efficient representation for very small maps.
- New methods `FilesPlume.createTempFile(...)` create sequential file names.
- New methods in `RegexUtil`: `matchesSomeRegex`, `everyStringMatchesSomeRegex`, `matchesNoRegex`, `noStringMatchesAnyRegex`.
- Some constructors are deprecated, for efficiency.
- Support Java 18.

## 1.5.9 (2022-07-12)

- Java 17 is supported.

## 1.5.8 (2021-07-20)

- Bug fix in `System.gcPercentage`.

## 1.5.7 (2021-07-20)

- Bug fix: make `System.gcUsageMessage` static.

## 1.5.6 (2021-07-20)

- New method `System.gcUsageMessage` is easier to use than `gcPercentage`.
- Method `SystemPlume.gcPercentage` is now more efficient.

## 1.5.5 (2021-06-08)

- Fix bug where `.gz` files were read and written uncompressed.
- Fix nullness type of class `StringsPlume.NullableStringComparator`.

## 1.5.4 (2021-06-08)

- Expand the contract of `CollectionsPlume.withoutDuplicates`.
- Add `withoutDuplicatesComparable` which is more efficient.

## 1.5.3 (2021-05-04)

- Fix problem with .jar file in previous release.

## 1.5.2 (2021-04-28)

- New methods `CollectionsPlume.mapCapacity` to compute the size for a newly-allocated map.
- New methods `hasDuplicates` in `ArraysPlume` and `ColletionsPlume`
- Renamed methods (the old versions still work but are deprecated):
   - `ArraysPlume.noDuplicates(List)` to `ColletionsPlume.noDuplicates`
- Moved methods (the old versions still work but are deprecated):
   - `UtilPlume.intersectionCardinality` and `intersectionCardinalityAtLeast` to `CollectionsPlume`

## 1.5.1 (2021-03-28)

- Expand the applicability of `CollectionsPlume.mapList` and `transform`

## 1.5.0 (2021-03-28)

- New methods for writing possibly-compressed files:
   - `FilesPlume.newFileOutputStream` (3 overloads)
   - `FilesPlume.newFileWriter` (5 overloads)
- New methods:
   - `ArraysPlume`:
       - `append`: creates a new array with an element added to the end
       - `nCopies`: produces an array that is multiple copies of a value
       - `sameContents`: tests whether two arrays are the same, setwise
   - `CollectionsPlume`:
       - `append`: creates a new list with an element added to the end
       - `concatenate`: like `concat`, but always returns a new list
       - `isSortedNoDuplicates`: tests whether a list is sorted and has no duplicates
       - `isSorted`: tests whether a list is sorted
       - `listOf`: like `List.of` in Java 9+
       - `transform`: like `mapList` but with args in opposite order
   - `StringsPlume`:
       - `charLiteral`: to quote a character as a Java character literal
       - `conjunction`: to produce text like "a, b, c, or d"
   - `SystemPlume.getBooleanSystemProperty`: interpret a system property as a boolean
   - `UtilPlume.getBooleanProperty` interpret a property as a boolean
- Deprecated class:
   - `StringsPlume.NullableStringComparator` (use `Comparator.nullsFirst(Comparator.naturalOrder())`)
- Deprecated `StringsPlume.escapeJava(char)` and `StringsPlume.escapeJava(char)`;
  use `escapeJava(String)` or `charLiteral()`
- Renamed methods (the old versions still work but are deprecated):
   - `UtilPlume.propertyIsTrue` to `getBooleanProperty`
   - `CollectionsPlume.removeDuplicates` to `withoutDuplicates`
- Moved methods (the old versions still work but are deprecated):
   - Moved file, directory, and stream methods from `UtilPlume` to new class `FilesPlume`

## 1.4.1 (2021-01-06)

- New class:
   - `LimitedSizeLongSet`

## 1.4.0

- New methods:
   - `StringsPlume.toStringAndClass(Object, boolean)`
   - `SystemPlume.gcPercentage` (two overloads)
- Deprecated methods:
   - `UtilPlume.fileLines` (use `Files.readAllLines`)
   - `UtilPlume.hash` methods (use `Objects.hash` or `Arrays.hashCode`)
- Moved methods (the old versions still work but are deprecated):
   - Moved system methods from `UtilPlume` to new class `SystemPlume`.
   - moved from `UtilPlume` to `StringsPlume`:
      - `mapToStringAndClass`
      - `toStringAndClass`
- Removed classes that were deprecated in 2018, over 2 years ago:
   - `ReflectionPlume`: use `org.plumelib.reflection.ReflectionPlume` instead
   - `StringBuilderDelimited`: use the JDK's `StringJoiner` instead

## 1.3.0

- New class `ToStringComparator` compares two objects according to the result of `toString`
- New methods:
   - `UtilPlume.usedMemory` with no formal parameter
   - `UtilPlume.mapList`
- Moved string methods from `UtilPlume` to new class `StringsPlume`;
  the old versions still work but are deprecated.

## 1.2.0

- New methods:
   - `UtilPlume.toStringAndClass`
   - `UtilPlume.mapToStringAndClass`
   - `UtilPlume.gc`
   - `UtilPlume.usedMemory`
- Remove field `UniqueId.nextUid`

## 1.1.7

- New class `UniqueIdMap`
- New methods:
   - `UtilPlume.replacePrefix`
   - `UtilPlume.replaceSuffix`

## 1.1.6

- New interface `UniqueId` for objects that have a unique ID.
- New methods:
   - `UtilPlume.sleep`
   - `UtilPlume.countFormatArguments`
   - `UtilPlume.prefixLines`
   - `UtilPlume.prefixLinesExceptFirst`
   - `UtilPlume.indentLines`
   - `UtilPlume.indentLinesExceptFirst`

## 1.1.5

Expand types of join() methods that take an Iterator.

## 1.1.4

Support Unicode escaping and unescaping, improve octal unescaping.

## 1.1.3

Added CombinationIterator class.

## 1.1.0

- Switched the order of the arguments to UtilPlume.join()
- Made UtilPlume.join generic, so it handles more types of arguments
- Renamings:
   - UtilPlume.backTrace() to stackTraceToString()
   - UtilPlume.escapeNonJava() to escapeJava()
   - UtilPlume.unescapeNonJava() to unescapeJava()
- Added char versions of escapeJava and unescapeJava

The old versions of all methods continue to work, but are deprecated.

## 1.0.11

- No user-visible changes

## 1.0.10

- Fix visibility of DumpHeap methods

## 1.0.9

- Add DumpHeap class, for creating a .hprof file of the current heap.

## 1.0.8

- Update dependencies.

## 1.0.7

- No user-visible changes.

## 1.0.6

- Add ImmutableTypes class

## 1.0.5

- Add FileWriterWithName class
- Deprecate ReflectionPlume class in favor of org.plumelib.reflection.ReflectionPlume
- Documentation improvements

## 1.0.4

- Deprecate StringBuilderDelimited; clients should use StringJoiner instead

## 1.0.0

- Require Java 8
- Add version of `incrementMap` with an implicit count of 1
- Improve documentation, and eliminate `_` from identifier names
- Remove unnecessary command-line arguments
