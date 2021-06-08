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
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

/** Utility methods that create and manipulate files, directories, streams, readers, and writers. */
public final class FilesPlume {

  /** This class is a collection of methods; it does not represent anything. */
  private FilesPlume() {
    throw new Error("do not instantiate");
  }

  /** The system-specific line separator string. */
  private static final String lineSep = System.lineSeparator();

  ///////////////////////////////////////////////////////////////////////////
  /// File readers
  ///

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
  public static InputStream newFileInputStream(Path path) throws IOException {
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
  public static InputStream newFileInputStream(File file) throws IOException {
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
  public static InputStreamReader newFileReader(String filename)
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
  public static InputStreamReader newFileReader(Path path)
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
  public static InputStreamReader newFileReader(Path path, @Nullable String charsetName)
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
  public static InputStreamReader newFileReader(File file)
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
  public static InputStreamReader newFileReader(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return newFileReader(file.toPath(), charsetName);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Buffered file readers and line number readers
  ///

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
  public static BufferedReader newBufferedFileReader(String filename)
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
  public static BufferedReader newBufferedFileReader(File file)
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
  public static BufferedReader newBufferedFileReader(String filename, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
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
  public static BufferedReader newBufferedFileReader(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
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
  public static LineNumberReader newLineNumberFileReader(String filename)
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
  public static LineNumberReader newLineNumberFileReader(File file)
      throws FileNotFoundException, IOException {
    Reader fileReader = newFileReader(file, null);
    return new LineNumberReader(fileReader);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// File writers
  ///

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
  public static OutputStream newFileOutputStream(Path path) throws IOException {
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
  public static OutputStream newFileOutputStream(Path path, boolean append) throws IOException {
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
  public static OutputStream newFileOutputStream(File file) throws IOException {
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
  public static OutputStreamWriter newFileWriter(String filename)
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
  public static OutputStreamWriter newFileWriter(Path path)
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
  public static OutputStreamWriter newFileWriter(Path path, @Nullable String charsetName)
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
  public static OutputStreamWriter newFileWriter(File file)
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
  public static OutputStreamWriter newFileWriter(File file, @Nullable String charsetName)
      throws FileNotFoundException, IOException {
    return newFileWriter(file.toPath(), charsetName);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Buffered file writers
  ///

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
  public static BufferedWriter newBufferedFileWriter(String filename) throws IOException {
    return newBufferedFileWriter(filename, false);
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
  public static BufferedOutputStream newBufferedFileOutputStream(String filename, boolean append)
      throws IOException {
    OutputStream os = newFileOutputStream(new File(filename).toPath(), append);
    return new BufferedOutputStream(os);
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
  public static BufferedWriter newBufferedFileWriter(String filename, boolean append)
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

  ///////////////////////////////////////////////////////////////////////////
  /// File
  ///

  /**
   * Count the number of lines in the specified file.
   *
   * @param filename file whose size to count
   * @return number of lines in filename
   * @throws IOException if there is trouble reading the file
   */
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
  /// Directories
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

  ///
  /// File names (aka filenames)
  ///

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
  public static String expandFilename(String name) {
    if (name.contains("~")) {
      return (name.replace("~", userHome));
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
   * windows path separators. That seems like a reasonable assumption.
   *
   * @param name file whose name to quote
   * @return a string version of the name that can be used in Java source
   */
  public static String javaSource(File name) {

    return name.getPath().replace("\\", "\\\\");
  }

  ///
  /// Reading and writing
  ///

  /**
   * Writes an Object to a File.
   *
   * @param o the object to write
   * @param file the file to which to write the object
   * @throws IOException if there is trouble writing the file
   */
  public static void writeObject(Object o, File file) throws IOException {
    OutputStream bytes = newBufferedFileOutputStream(file.toString(), false);
    try (ObjectOutputStream objs = new ObjectOutputStream(bytes)) {
      objs.writeObject(o);
    } finally {
      // In case objs was never set.
      bytes.close();
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
  @SuppressWarnings("BanSerializableRead") // wrapper around dangerous API
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
   * @param r the Reader to read
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
   */
  public static String readFile(File file) {

    try (BufferedReader reader = newBufferedFileReader(file)) {
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
   */
  public static void writeFile(File file, String contents) {

    try (Writer writer = Files.newBufferedWriter(file.toPath(), UTF_8)) {
      writer.write(contents, 0, contents.length());
    } catch (Exception e) {
      throw new Error("Unexpected error in writeFile(" + file + ")", e);
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Stream
  ///

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
    return baos.toString();
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
}
