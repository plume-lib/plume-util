// If you edit this file, you must also edit its tests.

package org.plumelib.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.signedness.qual.PolySigned;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Utility methods that do not belong elsewhere in the plume package: BitSet; hashing;
 * ProcessBuilder; properties; Throwable.
 */
public final class UtilPlume {

  /** This class is a collection of methods; it does not represent anything. */
  private UtilPlume() {
    throw new Error("do not instantiate");
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Object
  ///

  /**
   * Clones the given object by calling {@code clone()} reflectively. It is not possible to call
   * {@code Object.clone()} directly because it has protected visibility.
   *
   * @param <T> the type of the object to clone
   * @param data the object to clone
   * @return a clone of the object
   */
  @SuppressWarnings({
    "nullness:return", // result of clone() is non-null
    "signedness", // signedness is not relevant
    "unchecked"
  })
  public static <T> @PolyNull @PolySigned T clone(@PolyNull @PolySigned T data) {
    if (data == null) {
      return null;
    }
    try {
      return (T) data.getClass().getMethod("clone").invoke(data);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new Error(e);
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  /// BitSet (this section is deprecated in favor of CollectionsPlume)
  ///

  /**
   * Returns true if the cardinality of the intersection of the two BitSets is at least the given
   * value.
   *
   * @param a the first BitSet to intersect
   * @param b the second BitSet to intersect
   * @param i the cardinality bound
   * @return true iff size(a intersect b) &ge; i
   * @deprecated use CollectionsPlume.intersectionCardinalityAtLeast
   */
  @Deprecated // 2021-04-24
  // @InlineMe(
  //     replacement = "CollectionsPlume.intersectionCardinalityAtLeast(a, b, i)",
  //     imports = "org.plumelib.util.CollectionsPlume")
  @Pure
  public static boolean intersectionCardinalityAtLeast(BitSet a, BitSet b, @NonNegative int i) {
    return CollectionsPlume.intersectionCardinalityAtLeast(a, b, i);
  }

  /**
   * Returns true if the cardinality of the intersection of the three BitSets is at least the given
   * value.
   *
   * @param a the first BitSet to intersect
   * @param b the second BitSet to intersect
   * @param c the third BitSet to intersect
   * @param i the cardinality bound
   * @return true iff size(a intersect b intersect c) &ge; i
   * @deprecated use CollectionsPlume.intersectionCardinalityAtLeast
   */
  @Deprecated // 2021-04-24
  // @InlineMe(
  //     replacement = "CollectionsPlume.intersectionCardinalityAtLeast(a, b, c, i)",
  //     imports = "org.plumelib.util.CollectionsPlume")
  @Pure
  public static boolean intersectionCardinalityAtLeast(
      BitSet a, BitSet b, BitSet c, @NonNegative int i) {
    return CollectionsPlume.intersectionCardinalityAtLeast(a, b, c, i);
  }

  /**
   * Returns the cardinality of the intersection of the two BitSets.
   *
   * @param a the first BitSet to intersect
   * @param b the second BitSet to intersect
   * @return size(a intersect b)
   * @deprecated use CollectionsPlume.intersectionCardinality
   */
  @Deprecated // 2021-04-24
  // @InlineMe(
  //     replacement = "CollectionsPlume.intersectionCardinality(a, b)",
  //     imports = "org.plumelib.util.CollectionsPlume")
  @Pure
  public static int intersectionCardinality(BitSet a, BitSet b) {
    return CollectionsPlume.intersectionCardinality(a, b);
  }

  /**
   * Returns the cardinality of the intersection of the three BitSets.
   *
   * @param a the first BitSet to intersect
   * @param b the second BitSet to intersect
   * @param c the third BitSet to intersect
   * @return size(a intersect b intersect c)
   * @deprecated use CollectionsPlume.intersectionCardinality
   */
  @Deprecated // 2021-04-24
  // @InlineMe(
  //     replacement = "CollectionsPlume.intersectionCardinality(a, b, c)",
  //     imports = "org.plumelib.util.CollectionsPlume")
  @SuppressWarnings({"lock"}) // side effect to local state (BitSet)
  @Pure
  public static int intersectionCardinality(BitSet a, BitSet b, BitSet c) {
    return CollectionsPlume.intersectionCardinality(a, b, c);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// BufferedFileReader (this section is deprecated in favor of FilesPlume)
  ///

  // Convenience methods for creating InputStreams, Readers, BufferedReaders, and LineNumberReaders.

  /**
   * Returns an InputStream for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param path the possibly-compressed file to read
   * @return an InputStream for file
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newFileInputStream}
   */
  @Deprecated // deprecated 2020-02-20
  // @InlineMe(
  //     replacement = "FilesPlume.newFileInputStream(path)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static InputStream fileInputStream(Path path) throws IOException {
    return FilesPlume.newFileInputStream(path);
  }

  /**
   * Returns an InputStream for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibly-compressed file to read
   * @return an InputStream for file
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newFileInputStream}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newFileInputStream(file)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static InputStream fileInputStream(File file) throws IOException {
    return FilesPlume.newFileInputStream(file);
  }

  /**
   * Returns a Reader for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param filename the possibly-compressed file to read
   * @return an InputStream for filename
   * @throws IOException if there is trouble reading the file
   * @throws FileNotFoundException if the file is not found
   * @deprecated use {@link FilesPlume#newFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newFileReader(filename)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static InputStreamReader fileReader(String filename)
      throws FileNotFoundException, IOException {
    return FilesPlume.newFileReader(filename);
  }

  /**
   * Returns a Reader for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param path the possibly-compressed file to read
   * @return an InputStreamReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newFileReader(path)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static InputStreamReader fileReader(Path path) throws FileNotFoundException, IOException {
    return FilesPlume.newFileReader(path);
  }

  /**
   * Returns a Reader for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param path the possibly-compressed file to read
   * @param charsetName null, or the name of a Charset to use when reading the file
   * @return an InputStreamReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newFileReader(path, charsetName)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static InputStreamReader fileReader(Path path, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return FilesPlume.newFileReader(path, charsetName);
  }

  /**
   * Returns a Reader for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibly-compressed file to read
   * @return an InputStreamReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newFileReader(file)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static InputStreamReader fileReader(File file) throws FileNotFoundException, IOException {
    return FilesPlume.newFileReader(file);
  }

  /**
   * Returns a Reader for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibly-compressed file to read
   * @param charsetName null, or the name of a Charset to use when reading the file
   * @return an InputStreamReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newFileReader(file, charsetName)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static InputStreamReader fileReader(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return FilesPlume.newFileReader(file, charsetName);
  }

  /**
   * Returns a BufferedReader for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param filename the possibly-compressed file to read
   * @return a BufferedReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newBufferedFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newBufferedFileReader(filename)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static BufferedReader bufferedFileReader(String filename)
      throws FileNotFoundException, IOException {
    return FilesPlume.newBufferedFileReader(filename);
  }

  /**
   * Returns a BufferedReader for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibility-compressed file to read
   * @return a BufferedReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newBufferedFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newBufferedFileReader(file)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static BufferedReader bufferedFileReader(File file)
      throws FileNotFoundException, IOException {
    return FilesPlume.newBufferedFileReader(file);
  }

  /**
   * Returns a BufferedReader for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param filename the possibly-compressed file to read
   * @param charsetName the character set to use when reading the file
   * @return a BufferedReader for filename
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newBufferedFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newBufferedFileReader(filename)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static BufferedReader bufferedFileReader(String filename, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return FilesPlume.newBufferedFileReader(filename);
  }

  /**
   * Returns a BufferedReader for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibly-compressed file to read
   * @param charsetName the character set to use when reading the file
   * @return a BufferedReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newBufferedFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newBufferedFileReader(file, charsetName)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static BufferedReader bufferedFileReader(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return FilesPlume.newBufferedFileReader(file, charsetName);
  }

  /**
   * Returns a LineNumberReader for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param filename the possibly-compressed file to read
   * @return a LineNumberReader for filename
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newLineNumberFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newLineNumberFileReader(filename)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static LineNumberReader lineNumberFileReader(String filename)
      throws FileNotFoundException, IOException {
    return FilesPlume.newLineNumberFileReader(filename);
  }

  /**
   * Returns a LineNumberReader for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibly-compressed file to read
   * @return a LineNumberReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#newLineNumberFileReader}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newLineNumberFileReader(file)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static LineNumberReader lineNumberFileReader(File file)
      throws FileNotFoundException, IOException {
    return FilesPlume.newLineNumberFileReader(file);
  }

  /**
   * Returns a BufferedWriter for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param filename the possibly-compressed file to write
   * @return a BufferedWriter for filename
   * @throws IOException if there is trouble writing the file
   * @deprecated use {@link FilesPlume#newBufferedFileWriter}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newBufferedFileWriter(filename)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static BufferedWriter bufferedFileWriter(String filename) throws IOException {
    return FilesPlume.newBufferedFileWriter(filename);
  }

  /**
   * Returns a BufferedWriter for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param filename the possibly-compressed file to write
   * @param append if true, the resulting BufferedWriter appends to the end of the file instead of
   *     the beginning
   * @return a BufferedWriter for filename
   * @throws IOException if there is trouble writing the file
   * @deprecated use {@link FilesPlume#newBufferedFileWriter}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newBufferedFileWriter(filename, append)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static BufferedWriter bufferedFileWriter(String filename, boolean append)
      throws IOException {
    return FilesPlume.newBufferedFileWriter(filename, append);
  }

  /**
   * Returns a BufferedOutputStream for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param filename the possibly-compressed file to write
   * @param append if true, the resulting BufferedOutputStream appends to the end of the file
   *     instead of the beginning
   * @return a BufferedOutputStream for filename
   * @throws IOException if there is trouble writing the file
   * @deprecated use {@link FilesPlume#newBufferedFileOutputStream}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.newBufferedFileOutputStream(filename, append)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static BufferedOutputStream bufferedFileOutputStream(String filename, boolean append)
      throws IOException {
    return FilesPlume.newBufferedFileOutputStream(filename, append);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// File (this section is deprecated in favor of FilesPlume)
  ///

  /**
   * Count the number of lines in the specified file.
   *
   * @param filename file whose size to count
   * @return number of lines in filename
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#countLines}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.countLines(filename)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static long countLines(String filename) throws IOException {
    return FilesPlume.countLines(filename);
  }

  /**
   * Returns the contents of the file, as a list of strings, one per line. The lines do not include
   * any line termination characters.
   *
   * @param filename the file whose contents to return
   * @return the contents of {@code filename}, one string per line
   * @throws IOException if there was a problem reading the file
   * @deprecated use {@link Files#readAllLines}
   */
  @Deprecated // 2021-01-03
  // @InlineMe(
  //     replacement = "Files.readAllLines(Paths.get(filename))",
  //     imports = {"java.nio.file.Files", "java.nio.file.Paths"})
  public static List<String> fileLines(String filename) throws IOException {
    return Files.readAllLines(Paths.get(filename));
  }

  /**
   * Tries to infer the line separator used in a file.
   *
   * @param filename the file to infer a line separator from
   * @return the inferred line separator used in filename
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#inferLineSeparator}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.inferLineSeparator(filename)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static String inferLineSeparator(String filename) throws IOException {
    return FilesPlume.inferLineSeparator(filename);
  }

  /**
   * Tries to infer the line separator used in a file.
   *
   * @param file the file to infer a line separator from
   * @return the inferred line separator used in filename
   * @throws IOException if there is trouble reading the file
   * @deprecated use {@link FilesPlume#inferLineSeparator}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.inferLineSeparator(file)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static String inferLineSeparator(File file) throws IOException {
    return FilesPlume.inferLineSeparator(file);
  }

  /**
   * Returns true iff files have the same contents.
   *
   * @param file1 first file to compare
   * @param file2 second file to compare
   * @return true iff the files have the same contents
   * @deprecated use {@link FilesPlume#equalFiles}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.equalFiles(file1, file2, false)",
  //     imports = "org.plumelib.util.FilesPlume")
  @Pure
  public static boolean equalFiles(String file1, String file2) {
    return FilesPlume.equalFiles(file1, file2, false);
  }

  /**
   * Returns true iff the files have the same contents.
   *
   * @param file1 first file to compare
   * @param file2 second file to compare
   * @param trimLines if true, call String.trim on each line before comparing
   * @return true iff the files have the same contents
   * @deprecated use {@link FilesPlume#equalFiles}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.equalFiles(file1, file2, trimLines)",
  //     imports = "org.plumelib.util.FilesPlume")
  @SuppressWarnings({"lock"}) // reads files, side effects local state
  @Pure
  public static boolean equalFiles(String file1, String file2, boolean trimLines) {
    return FilesPlume.equalFiles(file1, file2, trimLines);
  }

  /**
   * Returns true if the file exists and is writable, or if the file can be created.
   *
   * @param file the file to create and write
   * @return true iff the file can be created and written
   * @deprecated use {@link FilesPlume#canCreateAndWrite}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.canCreateAndWrite(file)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static boolean canCreateAndWrite(File file) {
    return FilesPlume.canCreateAndWrite(file);
  }

  ///
  /// Directories (this section is deprecated in favor of FilesPlume)
  ///

  /**
   * Creates an empty directory in the default temporary-file directory, using the given prefix and
   * suffix to generate its name. For example, calling createTempDir("myPrefix", "mySuffix") will
   * create the following directory: temporaryFileDirectory/myUserName/myPrefix_someString_suffix.
   * someString is internally generated to ensure no temporary files of the same name are generated.
   *
   * @param prefix the prefix string to be used in generating the file's name; must be at least
   *     three characters long
   * @param suffix the suffix string to be used in generating the file's name; may be null, in which
   *     case the suffix ".tmp" will be used Returns: An abstract pathname denoting a newly-created
   *     empty file
   * @return a File representing the newly-created temporary directory
   * @throws IllegalArgumentException If the prefix argument contains fewer than three characters
   * @throws IOException If a file could not be created
   * @throws SecurityException If a security manager exists and its
   *     SecurityManager.checkWrite(java.lang.String) method does not allow a file to be created
   * @see java.io.File#createTempFile(String, String, File)
   * @deprecated use {@link FilesPlume#createTempDir}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.createTempDir(prefix, suffix)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static File createTempDir(String prefix, String suffix) throws IOException {
    return FilesPlume.createTempDir(prefix, suffix);
  }

  /**
   * Deletes the directory at dirName and all its files. Also works on regular files.
   *
   * @param dirName the directory to delete
   * @return true if and only if the file or directory is successfully deleted; false otherwise
   * @deprecated use {@link FilesPlume#deleteDir}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(replacement = "FilesPlume.deleteDir(dirName)", imports =
  // "org.plumelib.util.FilesPlume")
  public static boolean deleteDir(String dirName) {
    return FilesPlume.deleteDir(dirName);
  }

  /**
   * Deletes the directory at dir and all its files. Also works on regular files.
   *
   * @param dir the directory to delete
   * @return true if and only if the file or directory is successfully deleted; false otherwise
   * @deprecated use {@link FilesPlume#deleteDir}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(replacement = "FilesPlume.deleteDir(dir)", imports = "org.plumelib.util.FilesPlume")
  public static boolean deleteDir(File dir) {
    return FilesPlume.deleteDir(dir);
  }

  ///
  /// File names (aka filenames) (this section is deprecated in favor of FilesPlume)
  ///

  // Someone must have already written this.  Right?
  // There is Apache Commons IO WildcardFileFilter or, using standard Java utilities,
  // https://stackoverflow.com/a/31685610/173852 .

  /**
   * A FilenameFilter that accepts files whose name matches the given wildcard. The wildcard must
   * contain exactly one "*".
   *
   * @deprecated use {@link FilesPlume.WildcardFilter}
   */
  @Deprecated // deprecated 2021-02-25
  public static final class WildcardFilter implements FilenameFilter {
    /** The text before the wildcard. */
    String prefix;
    /** The text after the wildcard. */
    String suffix;

    /**
     * Create a filter that accepts files whose name matches the given wildcard.
     *
     * @param wildcard a string that must contain exactly one "*"
     */
    public WildcardFilter(String wildcard) {
      int astloc = wildcard.indexOf('*');
      if (astloc == -1) {
        throw new Error("No asterisk in wildcard argument: " + wildcard);
      }
      prefix = wildcard.substring(0, astloc);
      suffix = wildcard.substring(astloc + 1);
      if (wildcard.indexOf('*') != -1) {
        throw new Error("Multiple asterisks in wildcard argument: " + wildcard);
      }
    }

    @Override
    public boolean accept(File dir, String name) {
      // TODO: This is incorrect.  For example, the wildcard "ax*xb" would match the string "axb".
      return name.startsWith(prefix) && name.endsWith(suffix);
    }
  }

  /** The user's home directory. */
  static final String userHome = System.getProperty("user.home");

  /**
   * Does tilde expansion on a file name (to the user's home directory).
   *
   * @param name file whose name to expand
   * @return file with expanded file
   * @deprecated use {@link FilesPlume#expandFilename}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.expandFilename(name)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static File expandFilename(File name) {
    return FilesPlume.expandFilename(name);
  }

  /**
   * Does tilde expansion on a file name (to the user's home directory).
   *
   * @param name filename to expand
   * @return expanded filename
   * @deprecated use {@link FilesPlume#expandFilename}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.expandFilename(name)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static String expandFilename(String name) {
    return FilesPlume.expandFilename(name);
  }

  /**
   * Returns a string version of the filename that can be used in Java source. On Windows, the file
   * will return a backslash separated string. Since backslash is an escape character, it must be
   * quoted itself inside the string.
   *
   * <p>The current implementation presumes that backslashes don't appear in filenames except as
   * windows path separators. That seems like a reasonable assumption.
   *
   * @param name file whose name to quote
   * @return a string version of the name that can be used in Java source
   * @deprecated use {@link FilesPlume#javaSource}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(replacement = "FilesPlume.javaSource(name)", imports =
  // "org.plumelib.util.FilesPlume")
  public static String javaSource(File name) {

    return FilesPlume.javaSource(name);
  }

  ///
  /// Reading and writing (this section is deprecated in favor of FilesPlume)
  ///

  /**
   * Writes an Object to a File.
   *
   * @param o the object to write
   * @param file the file to which to write the object
   * @throws IOException if there is trouble writing the file
   * @deprecated use {@link FilesPlume#writeObject}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.writeObject(o, file)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static void writeObject(Object o, File file) throws IOException {
    FilesPlume.writeObject(o, file);
  }

  /**
   * Reads an Object from a File. This is a wrapper around {@link ObjectInputStream#readObject}, but
   * it takes a {@link File} as an argument. Note that use of that method can lead to security
   * vulnerabilities.
   *
   * @param file the file from which to read
   * @return the object read from the file
   * @throws IOException if there is trouble reading the file
   * @throws ClassNotFoundException if the object's class cannot be found
   * @deprecated use {@link FilesPlume#readObject}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(replacement = "FilesPlume.readObject(file)", imports =
  // "org.plumelib.util.FilesPlume")
  @SuppressWarnings("BanSerializableRead") // wrapper around dangerous API
  public static Object readObject(File file) throws IOException, ClassNotFoundException {
    return FilesPlume.readObject(file);
  }

  /**
   * Reads the entire contents of the reader and returns it as a string. Any IOException encountered
   * will be turned into an Error.
   *
   * @param r the Reader to read
   * @return the entire contents of the reader, as a string
   * @deprecated use {@link FilesPlume#readerContents}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(replacement = "FilesPlume.readerContents(r)", imports =
  // "org.plumelib.util.FilesPlume")
  public static String readerContents(Reader r) {
    return FilesPlume.readerContents(r);
  }

  // an alternate name would be "fileContents".
  /**
   * Reads the entire contents of the file and returns it as a string. Any IOException encountered
   * will be turned into an Error.
   *
   * <p>You could use {@code new String(Files.readAllBytes(...))}, but it requires a Path rather
   * than a File, and it can throw IOException which has to be caught.
   *
   * @param file the file to read
   * @return the entire contents of the reader, as a string
   * @deprecated use {@link FilesPlume#readFile}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(replacement = "FilesPlume.readFile(file)", imports = "org.plumelib.util.FilesPlume")
  public static String readFile(File file) {
    return FilesPlume.readFile(file);
  }

  /**
   * Creates a file with the given name and writes the specified string to it. If the file currently
   * exists (and is writable) it is overwritten Any IOException encountered will be turned into an
   * Error.
   *
   * @param file the file to write to
   * @param contents the text to put in the file
   * @deprecated use {@link FilesPlume#writeFile}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.writeFile(file, contents)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static void writeFile(File file, String contents) {
    FilesPlume.writeFile(file, contents);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Hashing
  ///

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(double @Nullable [] a, double @Nullable [] b) {
    return Objects.hash(Arrays.hashCode(a), Arrays.hashCode(b));
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(long @Nullable [] a, long @Nullable [] b) {
    return Objects.hash(Arrays.hashCode(a), Arrays.hashCode(b));
  }

  // In hashing, there are two separate issues.  First, one must convert the input datum into a
  // (possibly large) integer; this is also known as fingerprinting.  Then, one must transform the
  // resulting integer in a pseudorandom way so as to result in a number that is far separated from
  // other values that may have been near it to begin with.  Often these two steps are combined,
  // particularly if one wishes to avoid creating too large an integer (losing information off the
  // top bits).

  // This part of the file focuses on the fingerprinting issue, and uses simplistic approaches for
  // the second part.

  // TODO: What is the advantage of these methods over the built-in Java hash code routines?
  // I should document that, or deprecate this section of the file.

  /**
   * Returns a hash of the arguments. Note that this differs from the result of {@link
   * Double#hashCode()}.
   *
   * @param x value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Double#hashCode()}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Double.hashCode(x)")
  public static int hash(double x) {
    return Double.hashCode(x);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Objects#hash}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Objects.hash(a, b)", imports = "java.util.Objects")
  public static int hash(double a, double b) {
    return Objects.hash(a, b);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @param c value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Objects#hash}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Objects.hash(a, b, c)", imports = "java.util.Objects")
  public static int hash(double a, double b, double c) {
    return Objects.hash(a, b, c);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Arrays#hashCode}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Arrays.hashCode(a)", imports = "java.util.Arrays")
  public static int hash(double @Nullable [] a) {
    return Arrays.hashCode(a);
  }

  // Don't define hash with int args; use the long versions instead.

  /**
   * Returns a hash of the arguments. Note that this differs from the result of {@link
   * Long#hashCode()}. A problem with {@link Long#hashCode()} is that it maps -1 and 0 to the same
   * value, 0.
   *
   * @param l value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Long#hashCode()}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Long.hashCode(l)")
  public static int hash(long l) {
    return Long.hashCode(l);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Objects#hash}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Objects.hash(a, b)", imports = "java.util.Objects")
  public static int hash(long a, long b) {
    return Objects.hash(a, b);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @param c value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Objects#hash}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Objects.hash(a, b, c)", imports = "java.util.Objects")
  public static int hash(long a, long b, long c) {
    return Objects.hash(a, b, c);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Arrays#hashCode}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Arrays.hashCode(a)", imports = "java.util.Arrays")
  public static int hash(long @Nullable [] a) {
    return Arrays.hashCode(a);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link String#hashCode}
   */
  @Deprecated // use Objects.hashCode; deprecated 2021-01-05
  // @InlineMe(replacement = "Objects.hashCode(a)", imports = "java.util.Objects")
  public static int hash(@Nullable String a) {
    return Objects.hashCode(a);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Objects#hash}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Objects.hash(a, b)", imports = "java.util.Objects")
  public static int hash(@Nullable String a, @Nullable String b) {
    return Objects.hash(a, b);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @param c value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Objects#hash}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Objects.hash(a, b, c)", imports = "java.util.Objects")
  public static int hash(@Nullable String a, @Nullable String b, @Nullable String c) {
    return Objects.hash(a, b, c);
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Arrays#hashCode}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "Arrays.hashCode(a)", imports = "java.util.Arrays")
  public static int hash(@Nullable String @Nullable [] a) {
    return Arrays.hashCode(a);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Map (this section is deprecated in favor of StringsPlume)
  ///

  /**
   * Convert a map to a string, printing the runtime class of keys and values.
   *
   * @param m a map
   * @return a string representation of the map
   * @deprecated use {@link StringsPlume#mapToStringAndClass}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.mapToStringAndClass(m)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String mapToStringAndClass(
      Map<? extends @Signed @PolyNull Object, ? extends @Signed @PolyNull Object> m) {
    return StringsPlume.mapToStringAndClass(m);
  }

  /**
   * Returns a string representation of a value and its run-time class.
   *
   * @param o an object
   * @return a string representation of the value and its run-time class
   * @deprecated use {@link StringsPlume#toStringAndClass}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.toStringAndClass(o)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String toStringAndClass(@Nullable Object o) {
    return StringsPlume.toStringAndClass(o);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// ProcessBuilder
  ///

  /**
   * Execute the given command, and return all its output as a string.
   *
   * @param command a command to execute on the command line
   * @return all the output of the command
   */
  public static String backticks(String... command) {
    return backticks(Arrays.asList(command));
  }

  /**
   * Execute the given command, and return all its output as a string.
   *
   * @param command a command to execute on the command line, as a list of strings (the command,
   *     then its arguments)
   * @return all the output of the command
   */
  public static String backticks(List<String> command) {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true);
    // TimeLimitProcess p = new TimeLimitProcess(pb.start(), TIMEOUT_SEC * 1000);
    try {
      Process p = pb.start();
      String output = FilesPlume.streamString(p.getInputStream());
      return output;
    } catch (IOException e) {
      return "IOException: " + e.getMessage();
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Properties
  ///

  /**
   * Determines whether a property has value "true", "yes", or "1".
   *
   * @see Properties#getProperty
   * @param p a Properties object in which to look up the property
   * @param key name of the property to look up
   * @return true iff the property has value "true", "yes", or "1"
   * @deprecated use {@link getBooleanProperty}
   */
  @Pure
  @Deprecated // 2021-03-28
  // @InlineMe(
  //     replacement = "UtilPlume.getBooleanProperty(p, key)",
  //     imports = "org.plumelib.util.UtilPlume")
  public static boolean propertyIsTrue(Properties p, String key) {
    return getBooleanProperty(p, key);
  }

  /**
   * Determines whether a property has a string value that represents true: "true", "yes", or "1".
   * Errs if the property is set to a value that is not one of "true", "false", "yes", "no", "1", or
   * "0".
   *
   * @see Properties#getProperty
   * @param p a Properties object in which to look up the property
   * @param key name of the property to look up
   * @param defaultValue the value to return if the property is not set
   * @return true iff the property has value "true", "yes", or "1"
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // does not depend on object identity
  @Pure
  public static boolean getBooleanProperty(Properties p, String key, boolean defaultValue) {
    String pvalue = p.getProperty(key);
    if (pvalue == null) {
      return defaultValue;
    }
    switch (pvalue.toLowerCase()) {
      case "true":
      case "yes":
      case "1":
        return true;
      case "false":
      case "no":
      case "0":
        return false;
      default:
        throw new Error(
            String.format(
                "Property %s is set to \"%s\" which is not a boolean value", key, pvalue));
    }
  }

  /**
   * Determines whether a property has a string value that represents true: "true", "yes", or "1".
   * Errs if the property is set to a value that is not one of "true", "false", "yes", "no", "1", or
   * "0".
   *
   * @see Properties#getProperty
   * @param p a Properties object in which to look up the property
   * @param key name of the property to look up
   * @return true iff the property has value "true", "yes", or "1"
   */
  @Pure
  public static boolean getBooleanProperty(Properties p, String key) {
    return getBooleanProperty(p, key, false);
  }

  /**
   * Set the property to its previous value concatenated to the given value. Return the previous
   * value.
   *
   * @param p a Properties object in which to look up the property
   * @param key name of the property to look up
   * @param value value to concatenate to the previous value of the property
   * @return the previous value of the property
   * @see Properties#getProperty
   * @see Properties#setProperty
   */
  public static @Nullable String appendProperty(Properties p, String key, String value) {
    return (String) p.setProperty(key, p.getProperty(key, "") + value);
  }

  /**
   * Set the property only if it was not previously set.
   *
   * @see Properties#getProperty
   * @see Properties#setProperty
   * @param p a Properties object in which to look up the property
   * @param key name of the property to look up
   * @param value value to set the property to, if it is not already set
   * @return the previous value of the property
   */
  public static @Nullable String setDefaultMaybe(Properties p, String key, String value) {
    String currentValue = p.getProperty(key);
    if (currentValue == null) {
      p.setProperty(key, value);
    }
    return currentValue;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Stream (this section is deprecated in favor of FilesPlume)
  ///

  /**
   * Copy the contents of the input stream to the output stream.
   *
   * @param from input stream
   * @param to output stream
   * @deprecated use {@link FilesPlume#streamCopy}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.streamCopy(from, to)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static void streamCopy(InputStream from, OutputStream to) {
    FilesPlume.streamCopy(from, to);
  }

  /**
   * Returns a String containing all the characters from the input stream.
   *
   * @param is input stream to read
   * @return a String containing all the characters from the input stream
   * @deprecated use {@link FilesPlume#streamString}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(replacement="FilesPlume.streamString(is)", imports="org.plumelib.util.FilesPlume")
  public static String streamString(InputStream is) {
    return FilesPlume.streamString(is);
  }

  /**
   * Reads all lines from the stream and returns them in a {@code List<String>}.
   *
   * @param stream the stream to read from
   * @return the list of lines read from the stream
   * @throws IOException if there is an error reading from the stream
   * @deprecated use {@link FilesPlume#streamLines}
   */
  @Deprecated // deprecated 2021-02-25
  // @InlineMe(
  //     replacement = "FilesPlume.streamLines(stream)",
  //     imports = "org.plumelib.util.FilesPlume")
  public static List<String> streamLines(InputStream stream) throws IOException {
    return FilesPlume.streamLines(stream);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// String (these methods are all deprecated in favor of StringsPlume)
  ///

  /**
   * Returns a new string which is the text of target with all instances of oldStr replaced by
   * newStr.
   *
   * @param target the string to do replacement in
   * @param oldStr the substring to replace
   * @param newStr the replacement
   * @return target with all instances of oldStr replaced by newStr
   * @deprecated use {@link String#replace}
   */
  @Deprecated // 2020-09-07
  // @InlineMe(replacement = "target.replace(oldStr, newStr)")
  public static String replaceString(String target, String oldStr, String newStr) {
    return target.replace(oldStr, newStr);
  }

  /**
   * Returns the target with an occurrence of oldStr at the start replaced by newStr. Returns the
   * target if it does not strt with oldStr.
   *
   * <p>An alternative to this is to use regular expressions: {@code target.replaceFirst("^" +
   * Pattern.quote(oldStr), newStr)}
   *
   * @param target the string to do replacement in
   * @param oldStr the prefix to replace
   * @param newStr the replacement
   * @return the target with an occurrence of oldStr at the start replaced by newStr; returns the
   *     target if it does not start with oldStr
   * @deprecated use {@link StringsPlume#replacePrefix}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.replacePrefix(target, oldStr, newStr)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String replacePrefix(String target, String oldStr, String newStr) {
    return StringsPlume.replacePrefix(target, oldStr, newStr);
  }

  /**
   * Returns the target with an occurrence of oldStr at the end replaced by newStr. Returns the
   * target if it does not end with oldStr.
   *
   * <p>An alternative to this is to use regular expressions: {@code
   * target.replaceLast(Pattern.quote(oldStr) + "$", newStr)}
   *
   * @param target the string to do replacement in
   * @param oldStr the substring to replace
   * @param newStr the replacement
   * @return the target with an occurrence of oldStr at the start replaced by newStr; returns the
   *     target if it does not start with oldStr
   * @deprecated use {@link StringsPlume#replaceSuffix}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.replaceSuffix(target, oldStr, newStr)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String replaceSuffix(String target, String oldStr, String newStr) {
    return StringsPlume.replaceSuffix(target, oldStr, newStr);
  }

  /**
   * Returns the printed represenation of a value, with each line prefixed by another string.
   *
   * @param prefix the prefix to place before each line
   * @param o the value to be printed
   * @return the printed representation of {@code o}, with each line prefixed by the given prefix
   * @deprecated use {@link StringsPlume#prefixLines}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.prefixLines(prefix, o)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String prefixLines(String prefix, @Nullable Object o) {
    return StringsPlume.prefixLines(prefix, o);
  }

  /**
   * Returns the printed represenation of a value, with each line (except the first) prefixed by
   * another string.
   *
   * @param prefix the prefix to place before each line
   * @param o the value to be printed
   * @return the printed representation of {@code o}, with each line (except the first) prefixed by
   *     the given prefix
   * @deprecated use {@link StringsPlume#prefixLinesExceptFirst}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.prefixLinesExceptFirst(prefix, o)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String prefixLinesExceptFirst(String prefix, @Nullable Object o) {
    return StringsPlume.prefixLinesExceptFirst(prefix, o);
  }

  /**
   * Returns the printed representation of a value, with each line indented by {@code indent}
   * spaces.
   *
   * @param indent the number of spaces to indent
   * @param o the value whose printed representation string to increase indentation of
   * @return the printed representation of {@code o}, with each line prefixed by {@code indent}
   *     space characters
   * @deprecated use {@link StringsPlume#indentLines}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.indentLines(indent, o)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String indentLines(@NonNegative int indent, @Nullable Object o) {
    return StringsPlume.indentLines(indent, o);
  }

  /**
   * Returns the printed representation of a value, with each line (except the first) indented by
   * {@code indent} spaces.
   *
   * @param indent the number of spaces to indent
   * @param o the value whose printed representation string to increase indentation of
   * @return the printed representation of {@code o}, with each line (except the first) prefixed by
   *     {@code indent} space characters
   * @deprecated use {@link StringsPlume#indentLinesExceptFirst}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.indentLinesExceptFirst(indent, o)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String indentLinesExceptFirst(@NonNegative int indent, @Nullable Object o) {
    return StringsPlume.indentLinesExceptFirst(indent, o);
  }

  /**
   * Returns an array of Strings representing the characters between successive instances of the
   * delimiter character. Always returns an array of length at least 1 (it might contain only the
   * empty string).
   *
   * <p>Consider using the built-in <a
   * href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#split(java.lang.String)">String.split</a>
   * method, which takes a regular expression whereas this method takes a string.
   *
   * @see #split(String s, String delim)
   * @param s the string to split
   * @param delim delimiter to split the string on
   * @return array of length at least 1, containing s split on delimiter
   * @deprecated use {@link String#split}
   */
  @Deprecated // use String.split; deprecated 2020-12-02
  // @InlineMe(replacement = "s.split(\"\\\\\" + delim)")
  @SuppressWarnings("regex:argument") // todo: "\\" + char is a regex
  public static String[] split(String s, char delim) {
    return s.split("\\" + delim);
  }

  /**
   * Returns an array of Strings representing the characters between successive instances of the
   * delimiter String. Always returns an array of length at least 1 (it might contain only the empty
   * string).
   *
   * <p>Consider using the built-in <a
   * href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html#split(java.lang.String)">String.split</a>
   * method, method, which takes a regular expression whereas this method takes a character that is
   * interpreted literally.
   *
   * @see #split(String s, char delim)
   * @param s the string to split
   * @param delim delimiter to split the string on
   * @return array of length at least 1, containing s split on delimiter
   * @deprecated use {@link String#split}
   */
  @Deprecated // use String.split; deprecated 2020-12-02
  // @InlineMe(replacement = "s.split(Pattern.quote(delim))", imports = "java.util.regex.Pattern")
  public static String[] split(String s, String delim) {
    return s.split(Pattern.quote(delim));
  }

  /**
   * Returns an array of Strings, one for each line in the argument. Always returns an array of
   * length at least 1 (it might contain only the empty string). All common line separators (cr, lf,
   * cr-lf, or lf-cr) are supported. Note that a string that ends with a line separator will return
   * an empty string as the last element of the array.
   *
   * @see #split(String s, char delim)
   * @param s the string to split
   * @return an array of Strings, one for each line in the argument
   * @deprecated use {@link StringsPlume#splitLines}
   */
  @SuppressWarnings("value:statically.executable.not.pure") // pure wrt `equals()` but not `==`
  @SideEffectFree
  @StaticallyExecutable
  @Deprecated // 2020-12-02
  // @InlineMe(replacement="StringsPlume.splitLines(s)", imports="org.plumelib.util.StringsPlume")
  public static String[] splitLines(String s) {
    return StringsPlume.splitLines(s);
  }

  /**
   * Concatenate the string representations of the array elements, placing the delimiter between
   * them.
   *
   * <p>This differs from the built-in {@code String.join()} method added in Java 8, in that this
   * takes an array of Objects but that method takes an array of CharSequences. Use the Java 8
   * {@code String.join()} method when the arguments are CharSequences.
   *
   * @param <T> the type of array elements
   * @param a array of values to concatenate
   * @param delim delimiter to place between printed representations
   * @return the concatenation of the string representations of the values, with the delimiter
   *     between
   * @deprecated use {@link #join(CharSequence, Object...)} which has the arguments in the other
   *     order
   */
  @Deprecated // 2020-02-20
  // @InlineMe(replacement="StringsPlume.join(delim, a)", imports="org.plumelib.util.StringsPlume")
  @SuppressWarnings("nullness:type.argument")
  public static <T> String join(@Signed T[] a, CharSequence delim) {
    return StringsPlume.join(delim, a);
  }

  /**
   * Concatenate the string representations of the array elements, placing the delimiter between
   * them.
   *
   * <p>This differs from the built-in {@code String.join()} method added in Java 8, in that this
   * takes any arbitrary array but that method takes an array of CharSequences. Use the String
   * method when the arguments are CharSequences.
   *
   * @param <T> the type of array elements
   * @param a array of values to concatenate
   * @param delim delimiter to place between printed representations
   * @return the concatenation of the string representations of the values, with the delimiter
   *     between
   * @deprecated use {@link StringsPlume#join}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(replacement="StringsPlume.join(delim, a)", imports="org.plumelib.util.StringsPlume")
  @SafeVarargs
  @SuppressWarnings({"varargs", "nullness:type.argument"})
  public static <T> String join(CharSequence delim, @Signed T... a) {
    return StringsPlume.join(delim, a);
  }

  /**
   * Concatenate the string representations of the objects, placing the system-specific line
   * separator between them.
   *
   * @param <T> the type of array elements
   * @param a array of values to whose string representation to concatenate
   * @return the concatenation of the string representations of the values, each on its own line
   * @deprecated use {@link StringsPlume#joinLines}
   */
  @SafeVarargs
  @SuppressWarnings({"varargs", "nullness:type.argument"})
  @Deprecated // 2020-12-02
  // @InlineMe(replacement="StringsPlume.joinLines(a)", imports="org.plumelib.util.StringsPlume")
  public static <T> String joinLines(@Signed T... a) {
    return StringsPlume.joinLines(a);
  }

  /**
   * Concatenate the string representations of the objects, placing the delimiter between them.
   *
   * <p>This differs from the {@code String.join()} method added in Java 8, in that this takes any
   * array but that method takes an array of CharSequences.
   *
   * @see java.util.AbstractCollection#toString()
   * @param v collection of values to concatenate
   * @param delim delimiter to place between printed representations
   * @return the concatenation of the string representations of the values, with the delimiter
   *     between
   * @deprecated use {@link #join(CharSequence, Iterable)} which has the arguments in the other
   *     order
   */
  @Deprecated // 2020-12-02
  // @InlineMe(replacement="StringsPlume.join(delim, v)", imports="org.plumelib.util.StringsPlume")
  public static String join(Iterable<? extends @Signed @PolyNull Object> v, CharSequence delim) {
    return StringsPlume.join(delim, v);
  }

  /**
   * Concatenate the string representations of the objects, placing the delimiter between them.
   *
   * <p>This differs from the {@code String.join()} method added in Java 8, in that this takes any
   * Iterable but that method takes only {@code Iterable<? extends CharSequence>}.
   *
   * @see java.util.AbstractCollection#toString()
   * @param v collection of values to concatenate
   * @param delim delimiter to place between printed representations
   * @return the concatenation of the string representations of the values, with the delimiter
   *     between
   * @deprecated use {@link StringsPlume#join}
   */
  @Deprecated // deprecated 2020-12-02
  // @InlineMe(replacement="StringsPlume.join(delim, v)", imports="org.plumelib.util.StringsPlume")
  public static String join(CharSequence delim, Iterable<? extends @Signed @PolyNull Object> v) {
    return StringsPlume.join(delim, v);
  }

  /**
   * Concatenate the string representations of the objects, placing the system-specific line
   * separator between them.
   *
   * @see java.util.AbstractCollection#toString()
   * @param v list of values to concatenate
   * @return the concatenation of the string representations of the values, each on its own line
   * @deprecated use {@link StringsPlume#joinLines}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(replacement="StringsPlume.joinLines(v)", imports="org.plumelib.util.StringsPlume")
  public static String joinLines(Iterable<? extends @Signed @PolyNull Object> v) {
    return StringsPlume.joinLines(v);
  }

  /**
   * @param orig string to quote
   * @return quoted version of orig
   * @deprecated use {@link #escapeJava(String)}
   */
  @Deprecated // 2020-02-20
  // @InlineMe(
  //     replacement = "StringsPlume.escapeJava(orig)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String escapeNonJava(String orig) {
    return StringsPlume.escapeJava(orig);
  }

  /**
   * Escapes a String so that it is expressible in Java source code. By surrounding the return value
   * with double quote marks, the result will be a Java string literal denoting the original string.
   *
   * <p>Returns a new string only if any modifications were necessary.
   *
   * <p>Compared to the `escapeJava` method in Apache Commons Text StringEscapeUtils, this one
   * correctly handles non-printable ASCII characters.
   *
   * @param orig string to quote
   * @return quoted version of orig
   * @deprecated use {@link StringsPlume#escapeJava}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.escapeJava(orig)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String escapeJava(String orig) {
    return StringsPlume.escapeJava(orig);
  }

  /**
   * @param ch character to quote
   * @return quoted version of ch
   * @deprecated use {@link #escapeJava(Character)}
   */
  @Deprecated // use escapeJava(Character); deprecated 2020-02-20
  public static String escapeNonJava(Character ch) {
    return escapeJava(ch);
  }

  // If the overhead of this is too high to call in escapeJava(String), then inline it there.
  /**
   * Like {@link #escapeJava(String)}, but for a single character.
   *
   * @param ch character to quote
   * @return quoted version of ch
   * @deprecated use {@link StringsPlume#escapeJava}
   */
  @Deprecated // use StringsPlume.escapeJava; deprecated 2020-12-02
  public static String escapeJava(Character ch) {
    return StringsPlume.escapeJava(ch);
  }

  // If the overhead of this is too high to call in escapeJava(String), then inline it there.
  /**
   * Like {@link #escapeJava(String)}, but for a single character.
   *
   * @param c character to quote
   * @return quoted version of ch
   * @deprecated use {@link StringsPlume#escapeJava}
   */
  @Deprecated // use StringsPlume.escapeJava; deprecated 2020-12-02
  public static String escapeJava(char c) {
    return StringsPlume.escapeJava(c);
  }

  /**
   * Escape unprintable characters in the target, following the usual Java backslash conventions, so
   * that the result is sure to be printable ASCII. Returns a new string.
   *
   * @param orig string to quote
   * @return quoted version of orig
   * @deprecated use {@link StringsPlume#escapeNonASCII}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.escapeNonASCII(orig)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String escapeNonASCII(String orig) {
    return StringsPlume.escapeNonASCII(orig);
  }

  /**
   * Convert a string from Java source code format (with escape sequences) into the string it would
   * represent at run time. This is the inverse operation of {@link #escapeJava}, but it is
   * <em>not</em> a general unescaping mechanism for Java strings.
   *
   * <p>Compared to the `unescapeJava` method in Apache Commons Text StringEscapeUtils, this one
   * correctly handles non-printable ASCII characters.
   *
   * @param orig string to quote
   * @return quoted version of orig
   * @deprecated use {@link #unescapeJava(String)}
   */
  @Deprecated // 2020-02-20
  // @InlineMe(
  //     replacement = "StringsPlume.unescapeJava(orig)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String unescapeNonJava(String orig) {
    return StringsPlume.unescapeJava(orig);
  }

  /**
   * Convert a string from Java source code format (with escape sequences) into the string it would
   * represent at run time. This is the inverse operation of {@link #escapeJava}, but it is
   * <em>not</em> a general unescaping mechanism for Java strings.
   *
   * <p>Compared to the `unescapeJava` method in Apache Commons Text StringEscapeUtils, this one
   * correctly handles non-printable ASCII characters.
   *
   * @param orig string to quote
   * @return quoted version of orig
   * @deprecated use {@link StringsPlume#unescapeJava}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.unescapeJava(orig)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String unescapeJava(String orig) {
    return StringsPlume.unescapeJava(orig);
  }

  /**
   * Remove all whitespace before or after instances of delimiter.
   *
   * @param arg string to remove whitespace in
   * @param delimiter string to remove whitespace abutting
   * @return version of arg, with whitespace abutting delimiter removed
   * @deprecated use {@link StringsPlume#removeWhitespaceAround}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.removeWhitespaceAround(arg, delimiter)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String removeWhitespaceAround(String arg, String delimiter) {
    return StringsPlume.removeWhitespaceAround(arg, delimiter);
  }

  /**
   * Remove all whitespace after instances of delimiter.
   *
   * @param arg string to remove whitespace in
   * @param delimiter string to remove whitespace after
   * @return version of arg, with whitespace after delimiter removed
   * @deprecated use {@link StringsPlume#removeWhitespaceAfter}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.removeWhitespaceAfter(arg, delimiter)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String removeWhitespaceAfter(String arg, String delimiter) {
    return StringsPlume.removeWhitespaceAfter(arg, delimiter);
  }

  /**
   * Remove all whitespace before instances of delimiter.
   *
   * @param arg string to remove whitespace in
   * @param delimiter string to remove whitespace before
   * @return version of arg, with whitespace before delimiter removed
   * @deprecated use {@link StringsPlume#removeWhitespaceBefore}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.removeWhitespaceBefore(arg, delimiter)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String removeWhitespaceBefore(String arg, String delimiter) {
    return StringsPlume.removeWhitespaceBefore(arg, delimiter);
  }

  /**
   * Returns either "n <em>noun</em>" or "n <em>noun</em>s" depending on n. Adds "es" to words
   * ending with "ch", "s", "sh", or "x".
   *
   * @param n count of nouns
   * @param noun word being counted
   * @return noun, if n==1; otherwise, pluralization of noun
   * @deprecated use {@link StringsPlume#nplural}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.nplural(n, noun)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String nplural(int n, String noun) {
    return StringsPlume.nplural(n, noun);
  }

  /**
   * Returns a string of the specified length, truncated if necessary, and padded with spaces to the
   * left if necessary.
   *
   * @param s string to truncate or pad
   * @param length goal length
   * @return s truncated or padded to length characters
   * @deprecated use {@link StringsPlume#lpad}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.lpad(s, length)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String lpad(String s, @NonNegative int length) {
    return StringsPlume.lpad(s, length);
  }

  /**
   * Returns a string of the specified length, truncated if necessary, and padded with spaces to the
   * right if necessary.
   *
   * @param s string to truncate or pad
   * @param length goal length
   * @return s truncated or padded to length characters
   * @deprecated use {@link StringsPlume#rpad}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.rpad(s, length)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String rpad(String s, @NonNegative int length) {
    return StringsPlume.rpad(s, length);
  }

  /**
   * Converts the int to a String, then formats it using {@link #rpad(String,int)}.
   *
   * @param num int whose string representation to truncate or pad
   * @param length goal length
   * @return a string representation of num truncated or padded to length characters
   * @deprecated use {@link StringsPlume#rpad}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.rpad(num, length)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String rpad(int num, @NonNegative int length) {
    return StringsPlume.rpad(num, length);
  }

  /**
   * Converts the double to a String, then formats it using {@link #rpad(String,int)}.
   *
   * @param num double whose string representation to truncate or pad
   * @param length goal length
   * @return a string representation of num truncated or padded to length characters
   * @deprecated use {@link StringsPlume#rpad}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.rpad(num, length)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String rpad(double num, @NonNegative int length) {
    return StringsPlume.rpad(num, length);
  }

  /**
   * Same as built-in String comparison, but accept null arguments, and place them at the beginning.
   *
   * @deprecated use {@link StringsPlume.NullableStringComparator}
   */
  @Deprecated // 2020-12-02
  public static class NullableStringComparator
      implements Comparator<@Nullable String>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

    /** Create a new NullableStringComparator. */
    public NullableStringComparator() {}

    @Pure
    @Override
    public int compare(@Nullable String s1, @Nullable String s2) {
      if (s1 == null) {
        if (s2 == null) {
          return 0;
        } else {
          return 1;
        }
      }
      if (s2 == null) {
        return -1;
      }
      return s1.compareTo(s2);
    }
  }

  // This could test the types of the elements, and do something more sophisticated based on the
  // types.
  /**
   * Attempt to order Objects. Puts null at the beginning. Returns 0 for equal elements. Otherwise,
   * orders by the result of {@code toString()}.
   *
   * <p>Note: if toString returns a nondeterministic value, such as one that depends on the result
   * of {@code hashCode()}, then this comparator may yield different orderings from run to run of a
   * program.
   *
   * @deprecated use {@link StringsPlume.ObjectComparator}
   */
  @Deprecated // 2020-12-02
  public static class ObjectComparator implements Comparator<@Nullable Object>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20170420L;

    /** Create a new ObjectComparator. */
    public ObjectComparator() {}

    @SuppressWarnings({
      "allcheckers:purity.not.deterministic.call",
      "lock"
    }) // toString is being used in a deterministic way
    @Pure
    @Override
    public int compare(@Nullable Object o1, @Nullable Object o2) {
      // Make null compare smaller than anything else
      if ((o1 == o2)) {
        return 0;
      }
      if (o1 == null) {
        return -1;
      }
      if (o2 == null) {
        return 1;
      }
      if (o1.equals(o2)) {
        return 0;
      }
      // Don't compare output of hashCode() because it is non-deterministic from run to run.
      String s1 = o1.toString();
      String s2 = o2.toString();
      return s1.compareTo(s2);
    }
  }

  /**
   * Returns the number of times the character appears in the string.
   *
   * @param s string to search in
   * @param ch character to search for
   * @return number of times the character appears in the string
   * @deprecated use {@link StringsPlume#count}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(replacement = "StringsPlume.count(s, ch)", imports="org.plumelib.util.StringsPlume")
  public static int count(String s, int ch) {
    return StringsPlume.count(s, ch);
  }

  /**
   * Returns the number of times the second string appears in the first.
   *
   * @param s string to search in
   * @param sub non-empty string to search for
   * @return number of times the substring appears in the string
   * @deprecated use {@link StringsPlume#count}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(replacement = "StringsPlume.count(s, sub)", imports="org.plumelib.util.StringsPlume")
  public static int count(String s, String sub) {
    return StringsPlume.count(s, sub);
  }

  /**
   * Convert a number into an abbreviation such as "5.00K" for 5000 or "65.0M" for 65000000. K
   * stands for 1000, not 1024; M stands for 1000000, not 1048576, etc. There are always exactly 3
   * decimal digits of precision in the result (counting both sides of the decimal point).
   *
   * @param val a numeric value
   * @return an abbreviated string representation of the value
   * @deprecated use {@link StringsPlume#abbreviateNumber}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.abbreviateNumber(val)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static String abbreviateNumber(long val) {
    return StringsPlume.abbreviateNumber(val);
  }

  /**
   * Returns the number of arguments that the given format string takes. This is the number of
   * specifiers that take arguments (some, like {@code %n} and {@code %%}, do not take arguments).
   *
   * @param s a string
   * @return the number of format specifiers in the string
   * @deprecated use {@link StringsPlume#countFormatArguments}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.countFormatArguments(s)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static int countFormatArguments(String s) {
    return StringsPlume.countFormatArguments(s);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// StringTokenizer (these methods are all deprecated in favor of StringsPlume)
  ///

  /**
   * Returns a ArrayList of the Strings returned by {@link
   * java.util.StringTokenizer#StringTokenizer(String,String,boolean)} with the given arguments.
   *
   * <p>The static type is {@code ArrayList<Object>} because StringTokenizer extends {@code
   * Enumeration<Object>} instead of {@code Enumeration<String>} as it should (probably due to
   * backward-compatibility).
   *
   * @param str a string to be parsed
   * @param delim the delimiters
   * @param returnDelims flag indicating whether to return the delimiters as tokens
   * @return vector of strings resulting from tokenization
   * @deprecated use {@link StringsPlume#tokens}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.tokens(str, delim, returnDelims)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static ArrayList<Object> tokens(String str, String delim, boolean returnDelims) {
    return StringsPlume.tokens(str, delim, returnDelims);
  }

  /**
   * Returns a ArrayList of the Strings returned by {@link
   * java.util.StringTokenizer#StringTokenizer(String,String)} with the given arguments.
   *
   * @param str a string to be parsed
   * @param delim the delimiters
   * @return vector of strings resulting from tokenization
   * @deprecated use {@link StringsPlume#tokens}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(
  //     replacement = "StringsPlume.tokens(str, delim)",
  //     imports = "org.plumelib.util.StringsPlume")
  public static ArrayList<Object> tokens(String str, String delim) {
    return StringsPlume.tokens(str, delim);
  }

  /**
   * Returns a ArrayList of the Strings returned by {@link
   * java.util.StringTokenizer#StringTokenizer(String)} with the given arguments.
   *
   * @param str a string to be parsed
   * @return vector of strings resulting from tokenization
   * @deprecated use {@link StringsPlume#tokens}
   */
  @Deprecated // 2020-12-02
  // @InlineMe(replacement = "StringsPlume.tokens(str)", imports = "org.plumelib.util.StringsPlume")
  public static ArrayList<Object> tokens(String str) {
    return StringsPlume.tokens(str);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// System (this section is deprecated in favor of SystemPlume)
  ///

  /**
   * Like Thread.sleep, but does not throw any checked exceptions, so it is easier for clients to
   * use. Causes the currently executing thread to sleep (temporarily cease execution) for the
   * specified number of milliseconds.
   *
   * @param millis the length of time to sleep in milliseconds
   * @deprecated use {@link SystemPlume#sleep}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "SystemPlume.sleep(millis)", imports = "org.plumelib.util.SystemPlume")
  public static void sleep(long millis) {
    SystemPlume.sleep(millis);
  }

  /**
   * Returns the amount of used memory in the JVM.
   *
   * <p>To force a garbage collection, which gives a more accurate overapproximation of the memory
   * used, but is also slower, use {@link #usedMemory(boolean)}
   *
   * @return the amount of used memory
   * @deprecated use {@link SystemPlume#usedMemory()}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "SystemPlume.usedMemory()", imports = "org.plumelib.util.SystemPlume")
  public static long usedMemory() {
    return SystemPlume.usedMemory();
  }

  /**
   * Returns the amount of used memory in the JVM.
   *
   * @param forceGc if true, force a garbage collection, which gives a more accurate
   *     overapproximation of the memory used, but is also slower
   * @return the amount of used memory
   * @deprecated use {@link SystemPlume#usedMemory(boolean)}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(
  //     replacement = "SystemPlume.usedMemory(forceGc)",
  //     imports = "org.plumelib.util.SystemPlume")
  public static long usedMemory(boolean forceGc) {
    return SystemPlume.usedMemory(forceGc);
  }

  /**
   * Perform garbage collection. Like System.gc, but waits to return until garbage collection has
   * completed.
   *
   * @deprecated use {@link SystemPlume#gc}
   */
  @Deprecated // 2021-01-05
  // @InlineMe(replacement = "SystemPlume.gc()", imports = "org.plumelib.util.SystemPlume")
  public static void gc() {
    SystemPlume.gc();
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Throwable
  ///

  /**
   * Returns a String representation of the backtrace of the given Throwable. To see a backtrace at
   * the current location, do {@code backtrace(new Throwable())}.
   *
   * @param t the Throwable to obtain a backtrace of
   * @return a String representation of the backtrace of the given Throwable
   * @deprecated use {@link #stackTraceToString}
   */
  @Deprecated // 2020-02-20
  // @InlineMe(
  //     replacement = "UtilPlume.stackTraceToString(t)",
  //     imports = "org.plumelib.util.UtilPlume")
  public static String backTrace(Throwable t) {
    return stackTraceToString(t);
  }

  /**
   * Returns a String representation of the stack trace (the backtrace) of the given Throwable. For
   * a stack trace at the current location, do {@code stackTraceToString(new Throwable())}.
   *
   * @param t the Throwable to obtain a stack trace of
   * @return a String representation of the stack trace of the given Throwable
   */
  public static String stackTraceToString(Throwable t) {
    try (StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw)) {
      t.printStackTrace(pw);
      return sw.toString();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
