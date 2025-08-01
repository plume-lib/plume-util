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
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.IntVal;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

/** Utility methods that create and manipulate files, directories, streams, readers, and writers. */
public final class FilesPlume {

  /** This class is a collection of methods; it does not represent anything. */
  private FilesPlume() {
    throw new Error("do not instantiate");
  }

  /** The system-specific line separator string. */
  private static final String lineSep = System.lineSeparator();

  // //////////////////////////////////////////////////////////////////////
  // File readers
  //

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
   */
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @SideEffectFree
  public static @Owning InputStream newFileInputStream(Path path) throws IOException {
    FileInputStream fis = new FileInputStream(path.toFile());
    InputStream in;
    if (path.toString().endsWith(".gz")) {
      try {
        in = new GZIPInputStream(fis);
      } catch (IOException e) {
        fis.close();
        throw new IOException("Problem while reading " + path, e);
      }
    } else {
      in = fis;
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
   */
  @SideEffectFree
  public static @Owning InputStream newFileInputStream(File file) throws IOException {
    return newFileInputStream(file.toPath());
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
   */
  @SideEffectFree
  public static @Owning InputStreamReader newFileReader(String filename)
      throws FileNotFoundException, IOException {
    return newFileReader(new File(filename), null);
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
   */
  @SideEffectFree
  public static @Owning InputStreamReader newFileReader(Path path)
      throws FileNotFoundException, IOException {
    return newFileReader(path.toFile(), null);
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
   * @param charsetName the name of a Charset to use when reading the file, or null to use UTF-8
   * @return an InputStreamReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree.call") // needs JDK annotations
  @SideEffectFree
  public static @Owning InputStreamReader newFileReader(Path path, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    InputStream in = newFileInputStream(path.toFile());
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
   */
  @SideEffectFree
  public static @Owning InputStreamReader newFileReader(File file)
      throws FileNotFoundException, IOException {
    return newFileReader(file, null);
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
   * @param charsetName the name of a Charset to use when reading the file, or null to use UTF-8
   * @return an InputStreamReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  @SideEffectFree
  public static @Owning InputStreamReader newFileReader(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return newFileReader(file.toPath(), charsetName);
  }

  // //////////////////////////////////////////////////////////////////////
  // Buffered file readers and line number readers
  //

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
   */
  @SideEffectFree
  public static @Owning BufferedReader newBufferedFileReader(String filename)
      throws FileNotFoundException, IOException {
    return newBufferedFileReader(filename, null);
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
   */
  @SideEffectFree
  public static @Owning BufferedReader newBufferedFileReader(File file)
      throws FileNotFoundException, IOException {
    return newBufferedFileReader(file, null);
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
   * @param charsetName the character set to use when reading the file, or null to use UTF-8
   * @return a BufferedReader for filename
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  @SideEffectFree
  public static @Owning BufferedReader newBufferedFileReader(
      String filename, @Nullable String charsetName) throws FileNotFoundException, IOException {
    return newBufferedFileReader(new File(filename), charsetName);
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
   * @param charsetName the character set to use when reading the file, or null to use UTF-8
   * @return a BufferedReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree.call") // needs JDK annotations
  @SideEffectFree
  public static @Owning BufferedReader newBufferedFileReader(
      File file, @Nullable String charsetName) throws FileNotFoundException, IOException {
    Reader fileReader = newFileReader(file, charsetName);
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
   */
  @SideEffectFree
  public static @Owning LineNumberReader newLineNumberFileReader(String filename)
      throws FileNotFoundException, IOException {
    return newLineNumberFileReader(new File(filename));
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
   */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree.call") // needs JDK annotations
  @SideEffectFree
  public static @Owning LineNumberReader newLineNumberFileReader(File file)
      throws FileNotFoundException, IOException {
    Reader fileReader = newFileReader(file, null);
    return new LineNumberReader(fileReader);
  }

  // //////////////////////////////////////////////////////////////////////
  // File writers
  //

  /**
   * Returns an OutputStream for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param path the possibly-compressed file to read
   * @return an OutputStream for file
   * @throws IOException if there is trouble reading the file
   */
  @SideEffectFree
  public static @Owning OutputStream newFileOutputStream(Path path) throws IOException {
    return newFileOutputStream(path, false);
  }

  /**
   * Returns an OutputStream for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param path the possibly-compressed file to read
   * @param append if true, then bytes will be written to the end of the file rather than the
   *     beginning
   * @return an OutputStream for file
   * @throws IOException if there is trouble reading the file
   */
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @SideEffectFree
  public static @Owning OutputStream newFileOutputStream(Path path, boolean append)
      throws IOException {
    FileOutputStream fis = new FileOutputStream(path.toFile(), append);
    OutputStream in;
    if (path.toString().endsWith(".gz")) {
      try {
        in = new GZIPOutputStream(fis);
      } catch (IOException e) {
        fis.close();
        throw new IOException("Problem while reading " + path, e);
      }
    } else {
      in = fis;
    }
    return in;
  }

  /**
   * Returns an OutputStream for the file, accounting for the possibility that the file is
   * compressed. (A file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibly-compressed file to read
   * @return an OutputStream for file
   * @throws IOException if there is trouble reading the file
   */
  @SideEffectFree
  public static @Owning OutputStream newFileOutputStream(File file) throws IOException {
    return newFileOutputStream(file.toPath());
  }

  /**
   * Returns a Writer for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param filename the possibly-compressed file to read
   * @return an OutputStream for filename
   * @throws IOException if there is trouble reading the file
   * @throws FileNotFoundException if the file is not found
   */
  @SideEffectFree
  public static @Owning OutputStreamWriter newFileWriter(String filename)
      throws FileNotFoundException, IOException {
    return newFileWriter(new File(filename), null);
  }

  /**
   * Returns a Writer for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param path the possibly-compressed file to read
   * @return an OutputStreamWriter for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  @SideEffectFree
  public static @Owning OutputStreamWriter newFileWriter(Path path)
      throws FileNotFoundException, IOException {
    return newFileWriter(path.toFile(), null);
  }

  /**
   * Returns a Writer for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param path the possibly-compressed file to read
   * @param charsetName the name of a Charset to use when reading the file, or null to use UTF-8
   * @return an OutputStreamWriter for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree.call") // needs JDK annotations
  @SideEffectFree
  public static @Owning OutputStreamWriter newFileWriter(Path path, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    OutputStream in = newFileOutputStream(path.toFile());
    OutputStreamWriter fileWriter;
    if (charsetName == null) {
      fileWriter = new OutputStreamWriter(in, UTF_8);
    } else {
      fileWriter = new OutputStreamWriter(in, charsetName);
    }
    return fileWriter;
  }

  /**
   * Returns a Writer for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibly-compressed file to read
   * @return an OutputStreamWriter for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  @SideEffectFree
  public static @Owning OutputStreamWriter newFileWriter(File file)
      throws FileNotFoundException, IOException {
    return newFileWriter(file, null);
  }

  /**
   * Returns a Writer for the file, accounting for the possibility that the file is compressed. (A
   * file whose name ends with ".gz" is treated as compressed.)
   *
   * <p>Warning: The "gzip" program writes and reads files containing concatenated gzip files. As of
   * Java 1.4, Java reads just the first one: it silently discards all characters (including gzipped
   * files) after the first gzipped file.
   *
   * @param file the possibly-compressed file to read
   * @param charsetName the name of a Charset to use when reading the file, or null to use UTF-8
   * @return an OutputStreamWriter for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  @SideEffectFree
  public static @Owning OutputStreamWriter newFileWriter(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return newFileWriter(file.toPath(), charsetName);
  }

  // //////////////////////////////////////////////////////////////////////
  // Buffered file writers
  //

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
   */
  @SideEffectFree
  public static @Owning BufferedWriter newBufferedFileWriter(String filename) throws IOException {
    return newBufferedFileWriter(filename, false);
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
   */
  // Question:  should this be rewritten as a wrapper around newBufferedFileOutputStream?
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @SideEffectFree
  public static @Owning BufferedWriter newBufferedFileWriter(String filename, boolean append)
      throws IOException {
    if (filename.endsWith(".gz")) {
      return new BufferedWriter(
          new OutputStreamWriter(newFileOutputStream(Paths.get(filename), append), UTF_8));
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
   */
  @SuppressWarnings("allcheckers:purity.not.sideeffectfree.call") // needs JDK annotations
  @SideEffectFree
  public static @Owning BufferedOutputStream newBufferedFileOutputStream(
      String filename, boolean append) throws IOException {
    OutputStream os = newFileOutputStream(new File(filename).toPath(), append);
    return new BufferedOutputStream(os);
  }

  // //////////////////////////////////////////////////////////////////////
  // File
  //

  /**
   * Count the number of lines in the specified file.
   *
   * @param filename file whose size to count
   * @return number of lines in filename
   * @throws IOException if there is trouble reading the file
   */
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call",
    "allcheckers:purity.not.deterministic.call",
    "allcheckers:purity.not.deterministic.not.sideeffectfree.call",
    "lock:method.guarantee.violated"
  }) // side effect to local state
  @Pure
  public static long countLines(String filename) throws IOException {
    long count = 0;
    try (LineNumberReader reader = newLineNumberFileReader(filename)) {
      while (reader.readLine() != null) {
        count++;
      }
    }
    return count;
  }

  /**
   * Tries to infer the line separator used in a file.
   *
   * @param filename the file to infer a line separator from
   * @return the inferred line separator used in filename
   * @throws IOException if there is trouble reading the file
   */
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "allcheckers:purity.not.deterministic.object.creation" // create local state
  })
  @Pure
  public static String inferLineSeparator(String filename) throws IOException {
    return inferLineSeparator(new File(filename));
  }

  /**
   * Tries to infer the line separator used in a file.
   *
   * @param file the file to infer a line separator from
   * @return the inferred line separator used in filename
   * @throws IOException if there is trouble reading the file
   */
  @SuppressWarnings({
    "allcheckers:purity.not.deterministic.call", // side effect to local state
    "allcheckers:purity.not.deterministic.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @Pure
  public static String inferLineSeparator(File file) throws IOException {
    try (BufferedReader r = newBufferedFileReader(file)) {
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
   */
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
   */
  @SuppressWarnings({"allcheckers:purity", "lock"}) // reads files, side effects local state
  @Pure
  public static boolean equalFiles(String file1, String file2, boolean trimLines) {
    try (LineNumberReader reader1 = newLineNumberFileReader(file1);
        LineNumberReader reader2 = newLineNumberFileReader(file2); ) {
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
   */
  @SideEffectFree
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

    // // Old implementation; is this equivalent to the new one, above??
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

  /**
   * Creates a new empty file in the default temporary-file directory, using the given prefix and
   * suffix strings to generate its name. This is like {@link File#createTempFile}, but uses
   * sequential file names.
   *
   * @param prefix the prefix string to be used in generating the file's name; may be null
   * @param suffix the suffix string to be used in generating the file's name; may be null, in which
   *     case ".tmp" is used
   * @param attrs an optional list of file attributes to set atomically when creating the file
   * @return the path to the newly created file that did not exist before this method was invoked
   * @throws IOException if there is trouble creating the file
   */
  public static Path createTempFile(String prefix, String suffix, FileAttribute<?>... attrs)
      throws IOException {
    return createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), prefix, suffix, attrs);
  }

  /**
   * Creates a new empty file in the specified directory, using the given prefix and suffix strings
   * to generate its name. This is like {@link File#createTempFile}, but uses sequential file names.
   *
   * @param dir the path to directory in which to create the file
   * @param prefix the prefix string to be used in generating the file's name; may be null
   * @param suffix the suffix string to be used in generating the file's name; may be null, in which
   *     case ".tmp" is used
   * @param attrs an optional list of file attributes to set atomically when creating the file
   * @return the path to the newly created file that did not exist before this method was invoked
   * @throws IOException if there is trouble creating the file
   */
  public static Path createTempFile(
      Path dir, String prefix, String suffix, FileAttribute<?>... attrs) throws IOException {
    Path createdDir = Files.createDirectories(dir, attrs);
    for (int i = 1; i < Integer.MAX_VALUE; i++) {
      File candidate = new File(createdDir.toFile(), prefix + i + suffix);
      if (!candidate.exists()) {
        System.out.println("Created " + candidate);
        return candidate.toPath();
      }
    }
    throw new Error("every file exists");
  }

  //
  // Directories
  //

  // TODO: Document how this differs from Files.createTempDirectory, or deprecate this.
  /**
   * Creates an empty directory in the default temporary-file directory, using the given prefix and
   * suffix to generate its name. For example, calling {@code createTempDir("myPrefix", "mySuffix")}
   * will create the following directory: {@code
   * temporaryFileDirectory/myUserName/myPrefix_}<em>someString</em>{@code _suffix}.
   * <em>someString</em> is internally generated to ensure no temporary files of the same name are
   * generated.
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
   */
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
   */
  public static boolean deleteDir(String dirName) {
    return deleteDir(new File(dirName));
  }

  /**
   * Deletes the directory at dir and all its files. Also works on regular files.
   *
   * @param dir the directory to delete
   * @return true if and only if the file or directory is successfully deleted; false otherwise
   */
  public static boolean deleteDir(File dir) {
    File[] children = dir.listFiles();
    if (children != null) { // null means not a directory, or I/O error occurred.
      for (File child : children) {
        deleteDir(child);
      }
    }
    return dir.delete();
  }

  //
  // File names (aka filenames)
  //

  // Someone must have already written this.  Right?
  // There is Apache Commons IO WildcardFileFilter or, using standard Java utilities,
  // https://stackoverflow.com/a/31685610/173852 .

  /**
   * A FilenameFilter that accepts files whose name matches the given wildcard. The wildcard must
   * contain exactly one "*".
   */
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
    @SideEffectFree
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
   */
  @SideEffectFree
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
   */
  @SideEffectFree
  public static String expandFilename(String name) {
    if (name.contains("~")) {
      return name.replace("~", userHome);
    } else {
      return name;
    }
  }

  /**
   * Returns a string version of the filename that can be used in Java source. On Windows, the file
   * will return a backslash-separated string. Since backslash is an escape character, it must be
   * quoted itself inside the string.
   *
   * <p>The current implementation presumes that backslashes don't appear in filenames except as
   * Windows path separators. That seems like a reasonable assumption.
   *
   * @param name file whose name to quote
   * @return a string version of the name that can be used in Java source
   */
  @SideEffectFree
  public static String javaSource(File name) {
    return name.getPath().replace("\\", "\\\\");
  }

  //
  // Reading and writing
  //

  /**
   * Writes an Object to a File.
   *
   * @param o the object to write
   * @param file the file to which to write the object
   * @throws IOException if there is trouble writing the file
   */
  public static void writeObject(Object o, File file) throws IOException {
    try (OutputStream bytes = newBufferedFileOutputStream(file.toString(), false);
        ObjectOutputStream objs = new ObjectOutputStream(bytes)) {
      objs.writeObject(o);
    }
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
   */
  @SuppressWarnings({
    "BanSerializableRead", // wrapper around dangerous API
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @SideEffectFree
  public static Object readObject(File file) throws IOException, ClassNotFoundException {
    try (InputStream fis = newFileInputStream(file);
        // 8192 is the buffer size in BufferedReader
        InputStream istream = new BufferedInputStream(fis, 8192);
        ObjectInputStream objs = new ObjectInputStream(istream)) {
      return objs.readObject();
    }
  }

  /**
   * Reads the entire contents of the reader and returns it as a string. Any IOException encountered
   * will be turned into an Error.
   *
   * @param r the Reader to read; this method exhausts it and closes it
   * @return the entire contents of the reader, as a string
   */
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

  /**
   * Reads the entire contents of the file and returns it as a string. Any IOException encountered
   * will be turned into an Error.
   *
   * <p>You could use {@code new String(Files.readAllBytes(...))}, but it requires a Path rather
   * than a File, and it can throw IOException which has to be caught.
   *
   * @param file the file to read
   * @return the entire contents of the reader, as a string
   * @deprecated use {@link #readString}
   */
  // @InlineMe(replacement = "FilesPlume.fileContents(file)", imports =
  // "org.plumelib.util.FilesPlume")
  @Deprecated // 2023-03-02
  @SideEffectFree
  public static String readFile(File file) {
    return fileContents(file);
  }

  /**
   * Reads the entire contents of the file and returns it as a string.
   *
   * <p>The point of this method is that it does not throw any checked exception: any IOException
   * encountered will be turned into an Error.
   *
   * @param path the path to the file
   * @return a String containing the content read from the file
   */
  @SuppressWarnings({
    "allcheckers:purity.not.sideeffectfree.call", // side effect to local state
    "lock:method.guarantee.violated" // side effect to local state
  })
  @SideEffectFree
  public static String readString(Path path) {
    // In Java 11:
    // try {
    //   return Files.readString(path, UTF_8);
    // } catch (IOException e) {
    //   throw new Error(e);
    // }

    try (BufferedReader reader = newBufferedFileReader(path.toFile())) {
      StringBuilder contents = new StringBuilder();
      String line = reader.readLine();
      while (line != null) {
        contents.append(line);
        // Note that this converts line terminators!
        contents.append(lineSep);
        line = reader.readLine();
      }
      return contents.toString();
    } catch (Exception e) {
      throw new Error("Unexpected error in readString(" + path + ")", e);
    }
  }

  /**
   * Read the entire contents of the file and return it as a list of lines. Each line ends with a
   * line separator (except perhaps the last line).
   *
   * @param path the path to the file
   * @return the lines of the file
   */
  @SideEffectFree
  public static List<String> readLinesRetainingSeparators(Path path) {
    return StringsPlume.splitLinesRetainSeparators(readString(path));
  }

  /**
   * Reads the entire contents of the file and returns it as a string.
   *
   * <p>The point of this method is that it does not throw any checked exception: any IOException
   * encountered will be turned into an Error.
   *
   * <p>You could use {@code new String(Files.readAllBytes(...))}, but it requires a Path rather
   * than a File, and it can throw IOException which has to be caught.
   *
   * @param file the file to read
   * @return the entire contents of the reader, as a string
   * @deprecated use {@link #readString}
   */
  @Deprecated // 2024-04-14
  @SideEffectFree
  public static String fileContents(File file) {
    return readString(file.toPath());
  }

  /**
   * Creates a file with the given name and writes the specified string to it. If the file currently
   * exists (and is writable) it is overwritten.
   *
   * <p>The point of this method is that it does not throw any checked exception: any IOException
   * encountered will be turned into an Error.
   *
   * @param file the file to write to
   * @param contents the text to put in the file
   * @deprecated use {@link #writeString(File, String)}
   */
  @Deprecated // 2024-04-16
  public static void writeFile(File file, String contents) {
    writeString(file.toPath(), contents);
  }

  /**
   * Creates a file with the given name and writes the specified string to it. If the file currently
   * exists (and is writable) it is overwritten.
   *
   * <p>The point of this method is that it does not throw any checked exception: any IOException
   * encountered will be turned into an Error.
   *
   * @param file the file to write to
   * @param contents the text to put in the file
   */
  public static void writeString(File file, String contents) {
    writeString(file.toPath(), contents);
  }

  /**
   * Creates a file with the given name and writes the specified string to it. If the file currently
   * exists (and is writable) it is overwritten Any IOException encountered will be turned into an
   * Error.
   *
   * <p>The point of this method is that it does not throw any checked exception: any IOException
   * encountered will be turned into an Error.
   *
   * @param path the path to write to
   * @param contents the text to put in the file
   */
  public static void writeString(Path path, String contents) {
    // In Java 11:
    // try {
    //   Files.writeString(path, contents, StandardCharsets.UTF_8);
    // } catch (Exception e) {
    //   throw new Error("Unexpected error in writeFile(" + path + ")", e);
    // }

    try (Writer writer = Files.newBufferedWriter(path, UTF_8)) {
      writer.write(contents, 0, contents.length());
    } catch (Exception e) {
      Error newError = new Error("Unexpected error in writeString(" + path + ")", e);
      newError.printStackTrace(System.out);
      newError.printStackTrace(System.err);
      throw newError;
    }
  }

  // //////////////////////////////////////////////////////////////////////
  // Stream
  //

  /**
   * Copy the contents of the input stream to the output stream.
   *
   * @param from input stream
   * @param to output stream
   */
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
   */
  public static String streamString(InputStream is) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    streamCopy(is, baos);
    // In Java 11: String result = baos.toString(UTF_8);
    String result;
    try {
      result = baos.toString("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    }
    return result;
  }

  /**
   * Reads all lines from the stream and returns them in a {@code List<String>}.
   *
   * @param stream the stream to read from
   * @return the list of lines read from the stream
   * @throws IOException if there is an error reading from the stream
   */
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

  /**
   * Calls {@code InputStream.available()}, but returns null instead of throwing an IOException.
   *
   * @param is an input stream
   * @return {@code is.available()}, or null if that throws an exception
   */
  public static @Nullable Integer available(InputStream is) {
    try {
      return is.available();
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Returns true if the first {@code readLimit} bytes of the input stream consist only of
   * whitespace.
   *
   * @param is an input stream
   * @param readLimit how many bytes to look ahead in the input stream
   * @return null if {@code !is.markSupported()}; otherwise, true if the first {@code readLimit}
   *     characters of the input stream consist only of whitespace
   */
  public static @Nullable Boolean isWhitespaceOnly(InputStream is, @Positive int readLimit) {
    if (!is.markSupported()) {
      return null;
    }
    try {
      is.mark(readLimit * 4); // each character is at most 4 bytes, usually much less
      for (int bytesRead = 0; bytesRead < readLimit; bytesRead++) {
        int nextCodePoint = readCodePoint(is);
        if (nextCodePoint == -1) {
          return true;
        } else if (Character.isWhitespace(nextCodePoint)) {
          // do nothing, continue loop
        } else {
          return false;
        }
      }
      return true;
    } finally {
      try {
        is.reset();
      } catch (IOException e) {
        // Do nothing.
      }
    }
  }

  // From https://stackoverflow.com/a/54513347 .
  /**
   * Reads a Unicode code point from an input stream.
   *
   * @param is an input stream
   * @return the Unicode code point for the next character in the input stream
   */
  public static int readCodePoint(InputStream is) {
    try {
      int nextByte = is.read();
      if (nextByte == -1) {
        return -1;
      }
      byte firstByte = (byte) nextByte;
      int byteCount = getByteCount(firstByte);
      if (byteCount == 1) {
        return nextByte;
      }
      byte[] utf8Bytes = new byte[byteCount];
      utf8Bytes[0] = (byte) nextByte;
      for (int i = 1; i < byteCount; i++) { // Get any subsequent bytes for this UTF-8 character.
        nextByte = is.read();
        utf8Bytes[i] = (byte) nextByte;
      }
      int codePoint = new String(utf8Bytes, StandardCharsets.UTF_8).codePointAt(0);
      return codePoint;
    } catch (IOException e) {
      throw new Error("input stream = " + is, e);
    }
  }

  // From https://stackoverflow.com/a/54513347 .
  /**
   * Returns the number of bytes in a UTF-8 character based on the bit pattern of the supplied byte.
   * The only valid values are 1, 2 3 or 4. If the byte has an invalid bit pattern an
   * IllegalArgumentException is thrown.
   *
   * @param b the first byte of a UTF-8 character
   * @return the number of bytes for this UTF-* character
   * @throws IllegalArgumentException if the bit pattern is invalid
   */
  private static @IntVal({1, 2, 3, 4}) int getByteCount(byte b) throws IllegalArgumentException {
    if ((b >= 0)) return 1; // Pattern is 0xxxxxxx.
    if ((b >= (byte) 0b11000000) && (b <= (byte) 0b11011111)) return 2; // Pattern is 110xxxxx.
    if ((b >= (byte) 0b11100000) && (b <= (byte) 0b11101111)) return 3; // Pattern is 1110xxxx.
    if ((b >= (byte) 0b11110000) && (b <= (byte) 0b11110111)) return 4; // Pattern is 11110xxx.
    throw new IllegalArgumentException(); // Invalid first byte for UTF-8 character.
  }
}
