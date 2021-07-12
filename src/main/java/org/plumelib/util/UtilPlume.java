// If you edit this file, you must also edit its tests.

package org.plumelib.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTEqLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.regex.qual.Regex;
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

  /** The system-specific line separator string. */
  private static final String lineSep = System.lineSeparator();

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
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (BitSet)
  @Pure
  public static boolean intersectionCardinalityAtLeast(BitSet a, BitSet b, @NonNegative int i) {
    // Here are three implementation strategies to determine the
    // cardinality of the intersection:
    // 1. a.clone().and(b).cardinality()
    // 2. do the above, but copy only a subset of the bits initially -- enough
    //    that it should exceed the given number -- and if that fails, do the
    //    whole thing.  Unfortunately, bits.get(int, int) isn't optimized
    //    for the case where the indices line up, so I'm not sure at what
    //    point this approach begins to dominate #1.
    // 3. iterate through both sets with nextSetBit()

    int size = Math.min(a.length(), b.length());
    if (size > 10 * i) {
      // The size is more than 10 times the limit.  So first try processing
      // just a subset of the bits (4 times the limit).
      BitSet intersection = a.get(0, 4 * i);
      intersection.and(b);
      if (intersection.cardinality() >= i) {
        return true;
      }
    }
    return (intersectionCardinality(a, b) >= i);
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
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (BitSet)
  @Pure
  public static boolean intersectionCardinalityAtLeast(
      BitSet a, BitSet b, BitSet c, @NonNegative int i) {
    // See comments in intersectionCardinalityAtLeast(BitSet, BitSet, int).
    // This is a copy of that.

    int size = Math.min(a.length(), b.length());
    size = Math.min(size, c.length());
    if (size > 10 * i) {
      // The size is more than 10 times the limit.  So first try processing
      // just a subset of the bits (4 times the limit).
      BitSet intersection = a.get(0, 4 * i);
      intersection.and(b);
      intersection.and(c);
      if (intersection.cardinality() >= i) {
        return true;
      }
    }
    return (intersectionCardinality(a, b, c) >= i);
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
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (BitSet)
  @Pure
  public static int intersectionCardinality(BitSet a, BitSet b) {
    BitSet intersection = (BitSet) a.clone();
    intersection.and(b);
    return intersection.cardinality();
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
  @SuppressWarnings({"allcheckers:purity", "lock"}) // side effect to local state (BitSet)
  @Pure
  public static int intersectionCardinality(BitSet a, BitSet b, BitSet c) {
    BitSet intersection = (BitSet) a.clone();
    intersection.and(b);
    intersection.and(c);
    return intersection.cardinality();
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
  public static InputStream fileInputStream(Path path) throws IOException {
    InputStream in;
    if (path.endsWith(".gz")) {
      try {
        in = new GZIPInputStream(new FileInputStream(path.toFile()));
      } catch (IOException e) {
        throw new IOException("Problem while reading " + path, e);
      }
    } else {
      in = new FileInputStream(path.toFile());
    }
    return in;
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
  public static InputStream fileInputStream(File file) throws IOException {
    InputStream in;
    if (file.getName().endsWith(".gz")) {
      try {
        in = new GZIPInputStream(new FileInputStream(file));
      } catch (IOException e) {
        throw new IOException("Problem while reading " + file, e);
      }
    } else {
      in = new FileInputStream(file);
    }
    return in;
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
  public static InputStreamReader fileReader(String filename)
      throws FileNotFoundException, IOException {
    // return fileReader(filename, "ISO-8859-1");
    return fileReader(new File(filename), null);
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
  public static InputStreamReader fileReader(Path path) throws FileNotFoundException, IOException {
    return fileReader(path.toFile(), null);
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
  public static InputStreamReader fileReader(Path path, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    InputStream in = new FileInputStream(path.toFile());
    InputStreamReader fileReader;
    if (charsetName == null) {
      fileReader = new InputStreamReader(in, UTF_8);
    } else {
      fileReader = new InputStreamReader(in, charsetName);
    }
    return fileReader;
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
  public static InputStreamReader fileReader(File file) throws FileNotFoundException, IOException {
    return fileReader(file, null);
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
  public static InputStreamReader fileReader(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    InputStream in = new FileInputStream(file);
    InputStreamReader fileReader;
    if (charsetName == null) {
      fileReader = new InputStreamReader(in, UTF_8);
    } else {
      fileReader = new InputStreamReader(in, charsetName);
    }
    return fileReader;
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
  public static BufferedReader bufferedFileReader(String filename)
      throws FileNotFoundException, IOException {
    return bufferedFileReader(new File(filename));
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
  public static BufferedReader bufferedFileReader(File file)
      throws FileNotFoundException, IOException {
    return (bufferedFileReader(file, null));
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
  public static BufferedReader bufferedFileReader(String filename, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return bufferedFileReader(new File(filename), charsetName);
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
  public static BufferedReader bufferedFileReader(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    Reader fileReader = fileReader(file, charsetName);
    return new BufferedReader(fileReader);
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
  public static LineNumberReader lineNumberFileReader(String filename)
      throws FileNotFoundException, IOException {
    return lineNumberFileReader(new File(filename));
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
  public static LineNumberReader lineNumberFileReader(File file)
      throws FileNotFoundException, IOException {
    Reader fileReader;
    if (file.getName().endsWith(".gz")) {
      try {
        fileReader =
            new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "ISO-8859-1");
      } catch (IOException e) {
        throw new IOException("Problem while reading " + file, e);
      }
    } else {
      fileReader = new InputStreamReader(new FileInputStream(file), "ISO-8859-1");
    }
    return new LineNumberReader(fileReader);
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
  public static BufferedWriter bufferedFileWriter(String filename) throws IOException {
    return bufferedFileWriter(filename, false);
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
  // Question:  should this be rewritten as a wrapper around bufferedFileOutputStream?
  public static BufferedWriter bufferedFileWriter(String filename, boolean append)
      throws IOException {
    if (filename.endsWith(".gz")) {
      return new BufferedWriter(
          new OutputStreamWriter(
              new GZIPOutputStream(new FileOutputStream(filename, append)), UTF_8));
    } else {
      return Files.newBufferedWriter(
          Paths.get(filename),
          UTF_8,
          append ? new StandardOpenOption[] {CREATE, APPEND} : new StandardOpenOption[] {CREATE});
    }
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
  public static BufferedOutputStream bufferedFileOutputStream(String filename, boolean append)
      throws IOException {
    OutputStream os = new FileOutputStream(filename, append);
    if (filename.endsWith(".gz")) {
      os = new GZIPOutputStream(os);
    }
    return new BufferedOutputStream(os);
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
  public static long countLines(String filename) throws IOException {
    long count = 0;
    try (LineNumberReader reader = UtilPlume.lineNumberFileReader(filename)) {
      while (reader.readLine() != null) {
        count++;
      }
    }
    return count;
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
  @Deprecated // use Files.readAllLines; deprecated 2021-01-03
  public static List<String> fileLines(String filename) throws IOException {
    List<String> textList = new ArrayList<>();
    try (LineNumberReader reader = UtilPlume.lineNumberFileReader(filename)) {
      String line;
      while ((line = reader.readLine()) != null) {
        textList.add(line);
      }
    }
    return textList;
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
  public static String inferLineSeparator(String filename) throws IOException {
    return inferLineSeparator(new File(filename));
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
  public static String inferLineSeparator(File file) throws IOException {
    try (BufferedReader r = UtilPlume.bufferedFileReader(file)) {
      int unix = 0;
      int dos = 0;
      int mac = 0;
      while (true) {
        String s = r.readLine();
        if (s == null) {
          break;
        }
        if (s.endsWith("\r\n")) {
          dos++;
        } else if (s.endsWith("\r")) {
          mac++;
        } else if (s.endsWith("\n")) {
          unix++;
        } else {
          // This can happen only if the last line is not terminated.
        }
      }
      if ((dos > mac && dos > unix) || (lineSep.equals("\r\n") && dos >= unix && dos >= mac)) {
        return "\r\n";
      }
      if ((mac > dos && mac > unix) || (lineSep.equals("\r") && mac >= dos && mac >= unix)) {
        return "\r";
      }
      if ((unix > dos && unix > mac) || (lineSep.equals("\n") && unix >= dos && unix >= mac)) {
        return "\n";
      }
      // The two non-preferred line endings are tied and have more votes than
      // the preferred line ending.  Give up and return the line separator
      // for the system on which Java is currently running.
      return lineSep;
    }
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
  @Pure
  public static boolean equalFiles(String file1, String file2) {
    return equalFiles(file1, file2, false);
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
  @SuppressWarnings({"allcheckers:purity", "lock"}) // reads files, side effects local state
  @Pure
  public static boolean equalFiles(String file1, String file2, boolean trimLines) {
    try (LineNumberReader reader1 = UtilPlume.lineNumberFileReader(file1);
        LineNumberReader reader2 = UtilPlume.lineNumberFileReader(file2); ) {
      String line1 = reader1.readLine();
      String line2 = reader2.readLine();
      while (line1 != null && line2 != null) {
        if (trimLines) {
          line1 = line1.trim();
          line2 = line2.trim();
        }
        if (!line1.equals(line2)) {
          return false;
        }
        line1 = reader1.readLine();
        line2 = reader2.readLine();
      }
      if (line1 == null && line2 == null) {
        return true;
      }
      return false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns true if the file exists and is writable, or if the file can be created.
   *
   * @param file the file to create and write
   * @return true iff the file can be created and written
   * @deprecated use {@link FilesPlume#canCreateAndWrite}
   */
  @Deprecated // deprecated 2021-02-25
  public static boolean canCreateAndWrite(File file) {
    if (file.exists()) {
      return file.canWrite();
    } else {
      File directory = file.getParentFile();
      if (directory == null) {
        directory = new File(".");
      }
      // Does this test need "directory.canRead()" also?
      return directory.canWrite();
    }

    /// Old implementation; is this equivalent to the new one, above??
    // try {
    //   if (file.exists()) {
    //     return file.canWrite();
    //   } else {
    //     file.createNewFile();
    //     file.delete();
    //     return true;
    //   }
    // } catch (IOException e) {
    //   return false;
    // }
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
  public static File createTempDir(String prefix, String suffix) throws IOException {
    String fs = File.separator;
    String path = System.getProperty("java.io.tmpdir") + fs + System.getProperty("user.name") + fs;
    File pathFile = new File(path);
    if (!pathFile.isDirectory()) {
      if (!pathFile.mkdirs()) {
        throw new IOException("Could not create directory: " + pathFile);
      }
    }
    // Call Java runtime to create a file with a unique name
    File tmpfile = File.createTempFile(prefix + "_", "_", pathFile);
    String tmpDirPath = tmpfile.getPath() + suffix;
    File tmpDir = new File(tmpDirPath);
    if (!tmpDir.mkdirs()) {
      throw new IOException("Could not create directory: " + tmpDir);
    }
    // Now that we have created our directory, we should get rid
    // of the intermediate TempFile we created.
    tmpfile.delete();
    return tmpDir;
  }

  /**
   * Deletes the directory at dirName and all its files. Also works on regular files.
   *
   * @param dirName the directory to delete
   * @return true if and only if the file or directory is successfully deleted; false otherwise
   * @deprecated use {@link FilesPlume#deleteDir}
   */
  @Deprecated // deprecated 2021-02-25
  public static boolean deleteDir(String dirName) {
    return deleteDir(new File(dirName));
  }

  /**
   * Deletes the directory at dir and all its files. Also works on regular files.
   *
   * @param dir the directory to delete
   * @return true if and only if the file or directory is successfully deleted; false otherwise
   * @deprecated use {@link FilesPlume#deleteDir}
   */
  @Deprecated // deprecated 2021-02-25
  public static boolean deleteDir(File dir) {
    File[] children = dir.listFiles();
    if (children != null) { // null means not a directory, or I/O error occurred.
      for (File child : children) {
        deleteDir(child);
      }
    }
    return dir.delete();
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
  public static File expandFilename(File name) {
    String path = name.getPath();
    String newname = expandFilename(path);
    @SuppressWarnings({"interning", "ReferenceEquality"})
    boolean changed = newname != path;
    if (changed) {
      return new File(newname);
    } else {
      return name;
    }
  }

  /**
   * Does tilde expansion on a file name (to the user's home directory).
   *
   * @param name filename to expand
   * @return expanded filename
   * @deprecated use {@link FilesPlume#expandFilename}
   */
  @Deprecated // deprecated 2021-02-25
  public static String expandFilename(String name) {
    if (name.contains("~")) {
      return (name.replace("~", userHome));
    } else {
      return name;
    }
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
  public static String javaSource(File name) {

    return name.getPath().replace("\\", "\\\\");
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
  public static void writeObject(Object o, File file) throws IOException {
    // 8192 is the buffer size in BufferedReader
    OutputStream bytes = new BufferedOutputStream(new FileOutputStream(file), 8192);
    if (file.getName().endsWith(".gz")) {
      bytes = new GZIPOutputStream(bytes);
    }
    ObjectOutputStream objs = new ObjectOutputStream(bytes);
    objs.writeObject(o);
    objs.close();
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
  @SuppressWarnings("BanSerializableRead") // wrapper around dangerous API
  public static Object readObject(File file) throws IOException, ClassNotFoundException {
    // 8192 is the buffer size in BufferedReader
    InputStream istream = new BufferedInputStream(new FileInputStream(file), 8192);
    if (file.getName().endsWith(".gz")) {
      try {
        istream = new GZIPInputStream(istream);
      } catch (IOException e) {
        throw new IOException("Problem while reading " + file, e);
      }
    }
    ObjectInputStream objs = new ObjectInputStream(istream);
    return objs.readObject();
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
  public static String readerContents(Reader r) {
    try {
      StringBuilder contents = new StringBuilder();
      int ch;
      while ((ch = r.read()) != -1) {
        contents.append((char) ch);
      }
      r.close();
      return contents.toString();
    } catch (Exception e) {
      throw new Error("Unexpected error in readerContents(" + r + ")", e);
    }
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
  public static String readFile(File file) {

    try {
      BufferedReader reader = UtilPlume.bufferedFileReader(file);
      StringBuilder contents = new StringBuilder();
      String line = reader.readLine();
      while (line != null) {
        contents.append(line);
        // Note that this converts line terminators!
        contents.append(lineSep);
        line = reader.readLine();
      }
      reader.close();
      return contents.toString();
    } catch (Exception e) {
      throw new Error("Unexpected error in readFile(" + file + ")", e);
    }
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
  public static void writeFile(File file, String contents) {

    try {
      Writer writer = Files.newBufferedWriter(file.toPath(), UTF_8);
      writer.write(contents, 0, contents.length());
      writer.close();
    } catch (Exception e) {
      throw new Error("Unexpected error in writeFile(" + file + ")", e);
    }
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
    return hash(hash(a), hash(b));
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
  @Deprecated // use Double.hashCode(); deprecated 2021-01-05
  public static int hash(double x) {
    return hash(Double.doubleToLongBits(x));
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Objects#hash}
   */
  @Deprecated // use Objects.hash; deprecated 2021-01-05
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
  @Deprecated // use Objects.hash; deprecated 2021-01-05
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
  @Deprecated // use Arrays.hashCode; deprecated 2021-01-05
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
  @Deprecated // use Long.hashCode; deprecated 2021-01-05
  public static int hash(long l) {
    // If possible, use the value itself.
    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
      return (int) l;
    }

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
  @Deprecated // use Objects.hash; deprecated 2021-01-05
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
  @Deprecated // use Objects.hash; deprecated 2021-01-05
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
  @Deprecated // use Arrays.hashCode; deprecated 2021-01-05
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
  @Deprecated // use String.hashCode; deprecated 2021-01-05
  public static int hash(@Nullable String a) {
    return (a == null) ? 0 : a.hashCode();
  }

  /**
   * Returns a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   * @deprecated use {@link Objects#hash}
   */
  @Deprecated // use Objects.hash; deprecated 2021-01-05
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
  @Deprecated // use Objects.hash; deprecated 2021-01-05
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
  @Deprecated // use Arrays.hashCode; deprecated 2021-01-05
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
  @Deprecated // use StringsPlume.mapToStringAndClass; deprecated 2020-12-02
  public static String mapToStringAndClass(Map<?, ?> m) {
    return StringsPlume.mapToStringAndClass(m);
  }

  /**
   * Returns a string representation of a value and its run-time class.
   *
   * @param o an object
   * @return a string representation of the value and its run-time class
   * @deprecated use {@link StringsPlume#toStringAndClass}
   */
  @Deprecated // use StringsPlume.toStringAndClass; deprecated 2020-12-02
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
      String output = UtilPlume.streamString(p.getInputStream());
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
  public static void streamCopy(InputStream from, OutputStream to) {
    byte[] buffer = new byte[1024];
    int bytes;
    try {
      while (true) {
        bytes = from.read(buffer);
        if (bytes == -1) {
          return;
        }
        to.write(buffer, 0, bytes);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new Error(e);
    }
  }

  /**
   * Returns a String containing all the characters from the input stream.
   *
   * @param is input stream to read
   * @return a String containing all the characters from the input stream
   * @deprecated use {@link FilesPlume#streamString}
   */
  @Deprecated // deprecated 2021-02-25
  public static String streamString(InputStream is) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    streamCopy(is, baos);
    return baos.toString();
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
  public static List<String> streamLines(InputStream stream) throws IOException {
    List<String> outputLines = new ArrayList<>();
    try (BufferedReader rdr = new BufferedReader(new InputStreamReader(stream, UTF_8))) {
      String line;
      while ((line = rdr.readLine()) != null) {
        outputLines.add(line);
      }
    }
    return outputLines;
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
  @Deprecated // use String.replace; deprecated 2020-09-07
  public static String replaceString(String target, String oldStr, String newStr) {
    if (oldStr.equals("")) {
      throw new IllegalArgumentException();
    }

    StringBuilder result = new StringBuilder();
    @IndexOrHigh("target") int lastend = 0;
    int pos;
    while ((pos = target.indexOf(oldStr, lastend)) != -1) {
      result.append(target.substring(lastend, pos));
      result.append(newStr);
      lastend = pos + oldStr.length();
    }
    result.append(target.substring(lastend));
    return result.toString();
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
  @SuppressWarnings("index:argument") // startsWith implies indexes fit
  @Deprecated // use StringsPlume.replacePrefix; deprecated 2020-12-02
  public static String replacePrefix(String target, String oldStr, String newStr) {
    if (target.startsWith(oldStr)) {
      if (newStr.isEmpty()) {
        return target.substring(oldStr.length());
      } else {
        return newStr + target.substring(oldStr.length());
      }
    } else {
      return target;
    }
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
  @SuppressWarnings("lowerbound:argument") // endsWith implies indexes fit
  @Deprecated // use StringsPlume.replaceSuffix; deprecated 2020-12-02
  public static String replaceSuffix(String target, String oldStr, String newStr) {
    if (target.endsWith(oldStr)) {
      if (newStr.isEmpty()) {
        return target.substring(0, target.length() - oldStr.length());
      } else {
        return target.substring(0, target.length() - oldStr.length()) + newStr;
      }
    } else {
      return target;
    }
  }

  /**
   * Returns the printed represenation of a value, with each line prefixed by another string.
   *
   * @param prefix the prefix to place before each line
   * @param o the value to be printed
   * @return the printed representation of {@code o}, with each line prefixed by the given prefix
   * @deprecated use {@link StringsPlume#prefixLines}
   */
  @Deprecated // use StringsPlume.prefixLines; deprecated 2020-12-02
  public static String prefixLines(String prefix, @Nullable Object o) {
    return prefix + prefixLinesExceptFirst(prefix, o);
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
  @Deprecated // use StringsPlume.prefixLinesExceptFirst; deprecated 2020-12-02
  public static String prefixLinesExceptFirst(String prefix, @Nullable Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace(System.lineSeparator(), System.lineSeparator() + prefix);
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
  @Deprecated // use StringsPlume.indentLines; deprecated 2020-12-02
  public static String indentLines(@NonNegative int indent, @Nullable Object o) {
    if (indent == 0) {
      return (o == null) ? "null" : o.toString();
    }
    String prefix = new String(new char[indent]).replace('\0', ' ');
    return prefixLines(prefix, o);
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
  @Deprecated // use StringsPlume.indentLinesExceptFirst; deprecated 2020-12-02
  public static String indentLinesExceptFirst(@NonNegative int indent, @Nullable Object o) {
    if (indent == 0) {
      return (o == null) ? "null" : o.toString();
    }
    String prefix = new String(new char[indent]).replace('\0', ' ');
    return prefixLinesExceptFirst(prefix, o);
  }

  /**
   * Returns an array of Strings representing the characters between successive instances of the
   * delimiter character. Always returns an array of length at least 1 (it might contain only the
   * empty string).
   *
   * <p>Consider using the built-in <a
   * href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html#split(java.lang.String)">String.split</a>
   * method, which takes a regular expression whereas this method takes a string.
   *
   * @see #split(String s, String delim)
   * @param s the string to split
   * @param delim delimiter to split the string on
   * @return array of length at least 1, containing s split on delimiter
   * @deprecated use {@link String#split}
   */
  @Deprecated // use String.split; deprecated 2020-12-02
  public static String[] split(String s, char delim) {
    ArrayList<String> resultList = new ArrayList<>();
    for (int delimpos = s.indexOf(delim); delimpos != -1; delimpos = s.indexOf(delim)) {
      resultList.add(s.substring(0, delimpos));
      s = s.substring(delimpos + 1);
    }
    resultList.add(s);
    String[] result = resultList.toArray(new @NonNull String[resultList.size()]);
    return result;
  }

  /**
   * Returns an array of Strings representing the characters between successive instances of the
   * delimiter String. Always returns an array of length at least 1 (it might contain only the empty
   * string), which takes a regular expression whereas this method takes a string.
   *
   * <p>Consider using the built-in <a
   * href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/String.html#split(java.lang.String)">String.split</a>
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
  public static String[] split(String s, String delim) {
    int delimlen = delim.length();
    if (delimlen == 0) {
      throw new Error("Second argument to split was empty.");
    }
    ArrayList<String> resultList = new ArrayList<>();
    for (int delimpos = s.indexOf(delim); delimpos != -1; delimpos = s.indexOf(delim)) {
      resultList.add(s.substring(0, delimpos));
      s = s.substring(delimpos + delimlen);
    }
    resultList.add(s);
    String[] result = resultList.toArray(new @NonNull String[resultList.size()]);
    return result;
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
  @Deprecated // use StringsPlume.splitLines; deprecated 2020-12-02
  public static String[] splitLines(String s) {
    return s.split("\r\n?|\n\r?", -1);
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
  @Deprecated // use join(CharSequence, Object...) which has the arguments in the other order;
  // deprecated 2020-02-20
  public static <T> String join(T[] a, CharSequence delim) {
    if (a.length == 0) {
      return "";
    }
    if (a.length == 1) {
      return String.valueOf(a[0]);
    }
    StringBuilder sb = new StringBuilder(String.valueOf(a[0]));
    for (int i = 1; i < a.length; i++) {
      sb.append(delim).append(a[i]);
    }
    return sb.toString();
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
  @SafeVarargs
  @Deprecated // use StringsPlume.join; deprecated 2020-12-02
  public static <T> String join(CharSequence delim, T... a) {
    if (a.length == 0) {
      return "";
    }
    if (a.length == 1) {
      return String.valueOf(a[0]);
    }
    StringBuilder sb = new StringBuilder(String.valueOf(a[0]));
    for (int i = 1; i < a.length; i++) {
      sb.append(delim).append(a[i]);
    }
    return sb.toString();
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
  @SuppressWarnings("varargs")
  @Deprecated // use StringsPlume.joinLines; deprecated 2020-12-02
  public static <T> String joinLines(T... a) {
    return join(lineSep, a);
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
  @Deprecated // use join(CharSequence, Iterable) which has the arguments in the other order;
  // deprecated 2020-12-02
  public static String join(Iterable<?> v, CharSequence delim) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    Iterator<?> itor = v.iterator();
    while (itor.hasNext()) {
      if (first) {
        first = false;
      } else {
        sb.append(delim);
      }
      sb.append(itor.next());
    }
    return sb.toString();
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
  @Deprecated // use StringsPlume.join; deprecated 2020-12-02
  public static String join(CharSequence delim, Iterable<?> v) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    Iterator<?> itor = v.iterator();
    while (itor.hasNext()) {
      if (first) {
        first = false;
      } else {
        sb.append(delim);
      }
      sb.append(itor.next());
    }
    return sb.toString();
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
  @Deprecated // use StringsPlume.joinLines; deprecated 2020-12-02
  public static String joinLines(Iterable<?> v) {
    return join(lineSep, v);
  }

  /**
   * @param orig string to quote
   * @return quoted version of orig
   * @deprecated use {@link #escapeJava(String)}
   */
  @Deprecated // use escapeJava(String); deprecated 2020-02-20
  public static String escapeNonJava(String orig) {
    return escapeJava(orig);
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
  @Deprecated // use StringsPlume.escapeJava; deprecated 2020-12-02
  public static String escapeJava(String orig) {
    StringBuilder sb = new StringBuilder();
    // The previous escape character was seen right before this position.
    @IndexOrHigh("orig") int postEsc = 0;
    int origLen = orig.length();
    for (int i = 0; i < origLen; i++) {
      char c = orig.charAt(i);
      switch (c) {
        case '\"':
        case '\\':
        case '\b':
        case '\f':
        case '\n': // not lineSep
        case '\r':
        case '\t':
          if (postEsc < i) {
            sb.append(orig.substring(postEsc, i));
          }
          sb.append(escapeJava(c));
          postEsc = i + 1;
          break;

        default:
          if (c >= ' ' && c <= '~') {
            // Nothing to do: i gets incremented
          } else if (c <= '\377') {
            if (postEsc < i) {
              sb.append(orig.substring(postEsc, i));
            }
            sb.append("\\");
            int cAsInt = (int) c;
            sb.append(String.format("%03o", cAsInt));
            postEsc = i + 1;
            break;
          } else {
            sb.append("\\u");
            sb.append(String.format("%04x", (int) c));
            postEsc = i + 1;
            break;
          }
      }
    }
    if (sb.length() == 0) {
      return orig;
    }
    sb.append(orig.substring(postEsc));
    return sb.toString();
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
    return escapeJava(ch.charValue());
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
    switch (c) {
      case '\"':
        return "\\\"";
      case '\\':
        return "\\\\";
      case '\b':
        return "\\b";
      case '\f':
        return "\\f";
      case '\n': // not lineSep
        return "\\n"; // not lineSep
      case '\r':
        return "\\r";
      case '\t':
        return "\\t";
      default:
        return new String(new char[] {c});
    }
  }

  /**
   * Escape unprintable characters in the target, following the usual Java backslash conventions, so
   * that the result is sure to be printable ASCII. Returns a new string.
   *
   * @param orig string to quote
   * @return quoted version of orig
   * @deprecated use {@link StringsPlume#escapeNonASCII}
   */
  @Deprecated // use StringsPlume.escapeNonASCII; deprecated 2020-12-02
  public static String escapeNonASCII(String orig) {
    StringBuilder sb = new StringBuilder();
    int origLen = orig.length();
    for (int i = 0; i < origLen; i++) {
      char c = orig.charAt(i);
      sb.append(escapeNonASCII(c));
    }
    return sb.toString();
  }

  /**
   * Like escapeJava(), but quote more characters so that the result is sure to be printable ASCII.
   *
   * <p>This implementation is not particularly optimized.
   *
   * @param c character to quote
   * @return quoted version of c
   */
  private static String escapeNonASCII(char c) {
    if (c == '"') {
      return "\\\"";
    } else if (c == '\\') {
      return "\\\\";
    } else if (c == '\n') { // not lineSep
      return "\\n"; // not lineSep
    } else if (c == '\r') {
      return "\\r";
    } else if (c == '\t') {
      return "\\t";
    } else if (c >= ' ' && c <= '~') {
      return new String(new char[] {c});
    } else if (c < 256) {
      String octal = Integer.toOctalString(c);
      while (octal.length() < 3) {
        octal = '0' + octal;
      }
      return "\\" + octal;
    } else {
      String hex = Integer.toHexString(c);
      while (hex.length() < 4) {
        hex = "0" + hex;
      }
      return "\\u" + hex;
    }
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
  @Deprecated // use unescapeJava(String); deprecated 2020-02-20
  public static String unescapeNonJava(String orig) {
    return unescapeJava(orig);
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
  @Deprecated // use StringsPlume.unescapeJava; deprecated 2020-12-02
  public static String unescapeJava(String orig) {
    StringBuilder sb = new StringBuilder();
    // The previous escape character was seen just before this position.
    @LTEqLengthOf("orig") int postEsc = 0;
    int thisEsc = orig.indexOf('\\');
    while (thisEsc != -1) {
      if (thisEsc == orig.length() - 1) {
        sb.append(orig.substring(postEsc, thisEsc + 1));
        postEsc = thisEsc + 1;
        break;
      }
      switch (orig.charAt(thisEsc + 1)) {
        case 'b':
          sb.append(orig.substring(postEsc, thisEsc));
          sb.append('\b');
          postEsc = thisEsc + 2;
          break;
        case 'f':
          sb.append(orig.substring(postEsc, thisEsc));
          sb.append('\f');
          postEsc = thisEsc + 2;
          break;
        case 'n':
          sb.append(orig.substring(postEsc, thisEsc));
          sb.append('\n'); // not lineSep
          postEsc = thisEsc + 2;
          break;
        case 'r':
          sb.append(orig.substring(postEsc, thisEsc));
          sb.append('\r');
          postEsc = thisEsc + 2;
          break;
        case 't':
          sb.append(orig.substring(postEsc, thisEsc));
          sb.append('\t');
          postEsc = thisEsc + 2;
          break;
        case '\\':
          // This is not in the default case because the search would find
          // the quoted backslash.  Here we include the first backslash in
          // the output, but not the first.
          sb.append(orig.substring(postEsc, thisEsc + 1));
          postEsc = thisEsc + 2;
          break;

        case 'u':
          // Unescape Unicode characters.
          sb.append(orig.substring(postEsc, thisEsc));
          char unicodeChar = 0;
          int ii = thisEsc + 2;
          // The specification permits one or more 'u' characters.
          while (ii < orig.length() && orig.charAt(ii) == 'u') {
            ii++;
          }
          // The specification requires exactly 4 hexadecimal characters.
          // This is more liberal.  (Should it be?)
          int limit = Math.min(ii + 4, orig.length());
          while (ii < limit) {
            int thisDigit = Character.digit(orig.charAt(ii), 16);
            if (thisDigit == -1) {
              break;
            }
            unicodeChar = (char) ((unicodeChar * 16) + thisDigit);
            ii++;
          }
          sb.append(unicodeChar);
          postEsc = ii;
          break;

        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
          // Unescape octal characters.
          sb.append(orig.substring(postEsc, thisEsc));
          char octalChar = 0;
          int iii = thisEsc + 1;
          while (iii < Math.min(thisEsc + 4, orig.length())) {
            int thisDigit = Character.digit(orig.charAt(iii), 8);
            if (thisDigit == -1) {
              break;
            }
            int newValue = (octalChar * 8) + thisDigit;
            if (newValue > 0377) {
              break;
            }
            octalChar = (char) newValue;
            iii++;
          }
          sb.append(octalChar);
          postEsc = iii;
          break;

        default:
          // In the default case, retain the character following the backslash,
          // but discard the backslash itself.  "\*" is just a one-character string.
          sb.append(orig.substring(postEsc, thisEsc));
          postEsc = thisEsc + 1;
          break;
      }
      thisEsc = orig.indexOf('\\', postEsc);
    }
    if (postEsc == 0) {
      return orig;
    }
    sb.append(orig.substring(postEsc));
    return sb.toString();
  }

  /**
   * Remove all whitespace before or after instances of delimiter.
   *
   * @param arg string to remove whitespace in
   * @param delimiter string to remove whitespace abutting
   * @return version of arg, with whitespace abutting delimiter removed
   * @deprecated use {@link StringsPlume#removeWhitespaceAround}
   */
  @Deprecated // use StringsPlume.removeWhitespaceAround; deprecated 2020-12-02
  public static String removeWhitespaceAround(String arg, String delimiter) {
    arg = removeWhitespaceBefore(arg, delimiter);
    arg = removeWhitespaceAfter(arg, delimiter);
    return arg;
  }

  /**
   * Remove all whitespace after instances of delimiter.
   *
   * @param arg string to remove whitespace in
   * @param delimiter string to remove whitespace after
   * @return version of arg, with whitespace after delimiter removed
   * @deprecated use {@link StringsPlume#removeWhitespaceAfter}
   */
  @Deprecated // use StringsPlume.removeWhitespaceAfter; deprecated 2020-12-02
  public static String removeWhitespaceAfter(String arg, String delimiter) {
    if (delimiter == null || delimiter.equals("")) {
      throw new IllegalArgumentException("Bad delimiter: \"" + delimiter + "\"");
    }
    // String orig = arg;
    int delimLen = delimiter.length();
    int delimIndex = arg.indexOf(delimiter);
    while (delimIndex > -1) {
      int nonWsIndex = delimIndex + delimLen;
      while (nonWsIndex < arg.length() && Character.isWhitespace(arg.charAt(nonWsIndex))) {
        nonWsIndex++;
      }
      // if (nonWsIndex == arg.length()) {
      //   System.out.println("No nonspace character at end of: " + arg);
      // } else {
      //   System.out.println("'" + arg.charAt(nonWsIndex) + "' not a space character at " +
      //       nonWsIndex + " in: " + arg);
      // }
      if (nonWsIndex != delimIndex + delimLen) {
        arg = arg.substring(0, delimIndex + delimLen) + arg.substring(nonWsIndex);
      }
      delimIndex = arg.indexOf(delimiter, delimIndex + 1);
    }
    return arg;
  }

  /**
   * Remove all whitespace before instances of delimiter.
   *
   * @param arg string to remove whitespace in
   * @param delimiter string to remove whitespace before
   * @return version of arg, with whitespace before delimiter removed
   * @deprecated use {@link StringsPlume#removeWhitespaceBefore}
   */
  @Deprecated // use StringsPlume.removeWhitespaceBefore; deprecated 2020-12-02
  public static String removeWhitespaceBefore(String arg, String delimiter) {
    if (delimiter == null || delimiter.equals("")) {
      throw new IllegalArgumentException("Bad delimiter: \"" + delimiter + "\"");
    }
    // System.out.println("removeWhitespaceBefore(\"" + arg + "\", \"" + delimiter + "\")");
    // String orig = arg;
    int delimIndex = arg.indexOf(delimiter);
    while (delimIndex > -1) {
      int nonWsIndex = delimIndex - 1;
      while (nonWsIndex >= 0 && Character.isWhitespace(arg.charAt(nonWsIndex))) {
        nonWsIndex--;
      }
      // if (nonWsIndex == -1) {
      //   System.out.println("No nonspace character at front of: " + arg);
      // } else {
      //   System.out.println("'" + arg.charAt(nonWsIndex) + "' not a space character at " +
      //       nonWsIndex + " in: " + arg);
      // }
      if (nonWsIndex != delimIndex - 1) {
        arg = arg.substring(0, nonWsIndex + 1) + arg.substring(delimIndex);
      }
      delimIndex = arg.indexOf(delimiter, nonWsIndex + 2);
    }
    return arg;
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
  @Deprecated // use StringsPlume.nplural; deprecated 2020-12-02
  public static String nplural(int n, String noun) {
    if (n == 1) {
      return n + " " + noun;
    } else if (noun.endsWith("ch")
        || noun.endsWith("s")
        || noun.endsWith("sh")
        || noun.endsWith("x")) {
      return n + " " + noun + "es";
    } else {
      return n + " " + noun + "s";
    }
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
  @Deprecated // use StringsPlume.lpad; deprecated 2020-12-02
  public static String lpad(String s, @NonNegative int length) {
    if (s.length() < length) {
      StringBuilder buf = new StringBuilder();
      for (int i = s.length(); i < length; i++) {
        buf.append(' ');
      }
      return buf.toString() + s;
    } else {
      return s.substring(0, length);
    }
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
  @Deprecated // use StringsPlume.rpad; deprecated 2020-12-02
  public static String rpad(String s, @NonNegative int length) {
    if (s.length() < length) {
      StringBuilder buf = new StringBuilder(s);
      for (int i = s.length(); i < length; i++) {
        buf.append(' ');
      }
      return buf.toString();
    } else {
      return s.substring(0, length);
    }
  }

  /**
   * Converts the int to a String, then formats it using {@link #rpad(String,int)}.
   *
   * @param num int whose string representation to truncate or pad
   * @param length goal length
   * @return a string representation of num truncated or padded to length characters
   * @deprecated use {@link StringsPlume#rpad}
   */
  @Deprecated // use StringsPlume.rpad; deprecated 2020-12-02
  public static String rpad(int num, @NonNegative int length) {
    return rpad(String.valueOf(num), length);
  }

  /**
   * Converts the double to a String, then formats it using {@link #rpad(String,int)}.
   *
   * @param num double whose string representation to truncate or pad
   * @param length goal length
   * @return a string representation of num truncated or padded to length characters
   * @deprecated use {@link StringsPlume#rpad}
   */
  @Deprecated // use StringsPlume.rpad; deprecated 2020-12-02
  public static String rpad(double num, @NonNegative int length) {
    return rpad(String.valueOf(num), length);
  }

  /**
   * Same as built-in String comparison, but accept null arguments, and place them at the beginning.
   *
   * @deprecated use {@link StringsPlume.NullableStringComparator}
   */
  @Deprecated // use StringsPlume.NullableStringComparator; deprecated 2020-12-02
  public static class NullableStringComparator
      implements Comparator<@Nullable String>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20150812L;

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
  @Deprecated // use StringsPlume.ObjectComparator; deprecated 2020-12-02
  public static class ObjectComparator implements Comparator<@Nullable Object>, Serializable {
    /** Unique identifier for serialization. If you add or remove fields, change this number. */
    static final long serialVersionUID = 20170420L;

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
  @Deprecated // use StringsPlume.count; deprecated 2020-12-02
  public static int count(String s, int ch) {
    int result = 0;
    int pos = s.indexOf(ch);
    while (pos > -1) {
      result++;
      pos = s.indexOf(ch, pos + 1);
    }
    return result;
  }

  /**
   * Returns the number of times the second string appears in the first.
   *
   * @param s string to search in
   * @param sub non-empty string to search for
   * @return number of times the substring appears in the string
   * @deprecated use {@link StringsPlume#count}
   */
  @Deprecated // use StringsPlume.count; deprecated 2020-12-02
  public static int count(String s, String sub) {
    if (sub.equals("")) {
      throw new IllegalArgumentException("second argument must not be empty");
    }
    int result = 0;
    int pos = s.indexOf(sub);
    while (pos > -1) {
      result++;
      pos = s.indexOf(sub, pos + 1);
    }
    return result;
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
  @Deprecated // use StringsPlume.abbreviateNumber; deprecated 2020-12-02
  public static String abbreviateNumber(long val) {

    double dval = (double) val;
    String mag = "";

    if (val < 1000) {
      // nothing to do
    } else if (val < 1000000) {
      dval = val / 1000.0;
      mag = "K";
    } else if (val < 1000000000) {
      dval = val / 1000000.0;
      mag = "M";
    } else {
      dval = val / 1000000000.0;
      mag = "G";
    }

    String precision = "0";
    if (dval < 10) {
      precision = "2";
    } else if (dval < 100) {
      precision = "1";
    }

    @SuppressWarnings("formatter") // format string computed from precision and mag
    String result = String.format("%,1." + precision + "f" + mag, dval);
    return result;
  }

  // From
  // https://stackoverflow.com/questions/37413816/get-number-of-placeholders-in-formatter-format-string
  /** Regex that matches a format specifier. Some correspond to arguments and some do not. */
  private static @Regex(6) Pattern formatSpecifier =
      Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

  /**
   * Returns the number of arguments that the given format string takes. This is the number of
   * specifiers that take arguments (some, like {@code %n} and {@code %%}, do not take arguments).
   *
   * @param s a string
   * @return the number of format specifiers in the string
   * @deprecated use {@link StringsPlume#countFormatArguments}
   */
  @Deprecated // use StringsPlume.countFormatArguments; deprecated 2020-12-02
  public static int countFormatArguments(String s) {
    int result = 0;
    int maxIndex = 0;
    Matcher matcher = formatSpecifier.matcher(s);
    while (matcher.find()) {
      String argumentIndex = matcher.group(1);
      if (argumentIndex != null) {
        @SuppressWarnings("lowerbound:argument") // group contains >= 2 chars
        int thisIndex = Integer.parseInt(argumentIndex.substring(0, argumentIndex.length() - 1));
        maxIndex = Math.max(maxIndex, thisIndex);
        continue;
      }
      String conversion = matcher.group(6);
      assert conversion != null : "@AssumeAssertion(nullness): nonempty capturing group";
      if (!(conversion.equals("%") || conversion.equals("n"))) {
        result++;
      }
    }
    return Math.max(maxIndex, result);
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
  @Deprecated // use StringsPlume.tokens; deprecated 2020-12-02
  public static ArrayList<Object> tokens(String str, String delim, boolean returnDelims) {
    return CollectionsPlume.makeArrayList(new StringTokenizer(str, delim, returnDelims));
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
  @Deprecated // use StringsPlume.tokens; deprecated 2020-12-02
  public static ArrayList<Object> tokens(String str, String delim) {
    return CollectionsPlume.makeArrayList(new StringTokenizer(str, delim));
  }

  /**
   * Returns a ArrayList of the Strings returned by {@link
   * java.util.StringTokenizer#StringTokenizer(String)} with the given arguments.
   *
   * @param str a string to be parsed
   * @return vector of strings resulting from tokenization
   * @deprecated use {@link StringsPlume#tokens}
   */
  @Deprecated // use StringsPlume.tokens; deprecated 2020-12-02
  public static ArrayList<Object> tokens(String str) {
    return CollectionsPlume.makeArrayList(new StringTokenizer(str));
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
  @Deprecated // use SystemPlume.sleep; deprecated 2021-01-05
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
  @Deprecated // use SystemPlume.usedMemory(); deprecated 2021-01-05
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
  @Deprecated // use SystemPlume.usedMemory(boolean); deprecated 2021-01-05
  public static long usedMemory(boolean forceGc) {
    return SystemPlume.usedMemory(forceGc);
  }

  /**
   * Perform garbage collection. Like System.gc, but waits to return until garbage collection has
   * completed.
   *
   * @deprecated use {@link SystemPlume#gc}
   */
  @Deprecated // use SystemPlume.gc; deprecated 2021-01-05
  public static void gc() {
    SystemPlume.gc();
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Throwable
  ///

  /**
   * Returns a String representation of the backtrace of the given Throwable. To see a backtrace at
   * the the current location, do {@code backtrace(new Throwable())}.
   *
   * @param t the Throwable to obtain a backtrace of
   * @return a String representation of the backtrace of the given Throwable
   * @deprecated use {@link #stackTraceToString}
   */
  @Deprecated // use stackTraceToString; deprecated 2020-02-20
  public static String backTrace(Throwable t) {
    return stackTraceToString(t);
  }

  /**
   * Returns a String representation of the stack trace (the backtrace) of the given Throwable. For
   * a stack trace at the the current location, do {@code stackTraceToString(new Throwable())}.
   *
   * @param t the Throwable to obtain a stack trace of
   * @return a String representation of the stack trace of the given Throwable
   */
  public static String stackTraceToString(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.close();
    String result = sw.toString();
    return result;
  }
}
