# Plume-Util change log

## 1.1.1

Added CombinationIterator class.

## 1.1.0

- Switched the order of the arguments to UtilPlume.join()
- Made UtilPlume.join generic, so it handles more types of arguments
- Renamings:
   - PlumeUtil.backTrace() to stackTraceToString()
   - PlumeUtil.escapeNonJava() to escapeJava()
   - PlumeUtil.unescapeNonJava() to unescapeJava()
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
