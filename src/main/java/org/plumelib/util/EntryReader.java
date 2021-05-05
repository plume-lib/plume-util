package org.plumelib.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.IndexOrLow;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.regex.qual.Regex;

// TODO:
// EntryReader has a public concept of "short entry", but I don't think that
// concept is logically part of EntryReader.  I think it would be better for
// Lookup to make this decision itself, for instance by checking whether there
// are any line separators in the entry that it gets back.
//
// Here are some useful features that EntryReader should have.
//  * It should implement some unimplemented methods from LineNumberReader (see
//    "not yet implemented" in this file).
//  * It should have constructors that take an InputStream or Reader
//    (in addition to the current BufferedReader, File, and String versions).
//  * It should have a close method.
//  * It should automatically close the underlying file/etc. when the
//    iterator gets to the end (or the end is otherwise reached).

/**
 * Class that reads records or "entries" from a file. In the simplest case, entries can be lines. It
 * supports:
 *
 * <ul>
 *   <li>include files,
 *   <li>comments, and
 *   <li>multi-line entries (paragraphs).
 * </ul>
 *
 * The syntax of each of these is customizable.
 *
 * <p>Example use:
 *
 * <pre>{@code
 * // EntryReader constructor args are: filename, comment regexp, include regexp
 * try (EntryReader er = new EntryReader(filename, "^#.*", null)) {
 *   for (String line : er) {
 *     ...
 *   }
 * } catch (IOException e) {
 *   System.err.println("Problem reading " + filename + ": " + e.getMessage());
 * }
 * }</pre>
 *
 * @see #getEntry() and @see #setEntryStartStop(String,String)
 */
@SuppressWarnings({"IterableAndIterator"})
public class EntryReader extends LineNumberReader implements Iterable<String>, Iterator<String> {

  ///
  /// User configuration variables
  ///

  /** Regular expression that specifies an include file. */
  private final @Nullable @Regex(1) Pattern includeRegex;

  /** Regular expression that matches a comment. */
  private final @Nullable Pattern commentRegex;

  /**
   * Regular expression that starts a long entry.
   *
   * <p>If the first line of an entry matches this regexp, then the entry is terminated by: {@link
   * #entryStopRegex}, another line that matches {@code entryStartRegex} (even not following a
   * newline), or the end of the current file.
   *
   * <p>Otherwise, the first line of an entry does NOT match this regexp (or the regexp is null), in
   * which case the entry is terminated by a blank line or the end of the current file.
   */
  public @MonotonicNonNull @Regex(1) Pattern entryStartRegex = null;

  /**
   * See {@link entryStartRegex}.
   *
   * @see #entryStartRegex
   */
  public @MonotonicNonNull Pattern entryStopRegex = null;

  ///
  /// Internal implementation variables
  ///

  /** Stack of readers. Used to support include files. */
  private final ArrayDeque<FlnReader> readers = new ArrayDeque<>();

  /** Line that is pushed back to be reread. */
  @Nullable String pushbackLine = null;

  /** Platform-specific line separator. */
  private static final String lineSep = System.lineSeparator();

  ///
  /// Helper classes
  ///

  /**
   * Like LineNumberReader, but also has a filename field. "FlnReader" stands for "Filename and Line
   * Number Reader".
   */
  private static class FlnReader extends LineNumberReader {
    /** The file being read. */
    public String filename;

    /**
     * Create a FlnReader.
     *
     * @param reader source from which to read entries
     * @param filename file name corresponding to reader, for use in error messages. Must be
     *     non-null; if there isn't a name, clients should provide a dummy value.
     */
    public FlnReader(Reader reader, String filename) {
      super(reader);
      this.filename = filename;
    }

    /**
     * Create a FlnReader.
     *
     * @param filename file from which to read
     * @throws IOException if there is trobule reading the file
     */
    public FlnReader(String filename) throws IOException {
      super(FilesPlume.newFileReader(filename));
      this.filename = filename;
    }
  }

  /** Descriptor for an entry (record, paragraph, etc.). */
  public static class Entry {
    /** First line of the entry. */
    public final String firstLine;
    /** Complete body of the entry including the first line. */
    public final String body;
    /** True if this is a short entry (blank-line-separated). */
    public final boolean shortEntry;
    /** Filename in which the entry was found. */
    public final String filename;
    /** Line number of first line of entry. */
    public final long lineNumber;

    /**
     * Create an entry.
     *
     * @param firstLine first line of the entry
     * @param body complete body of the entry including the first line
     * @param shortEntry true if this is a short entry (blank-line-separated)
     * @param filename filename in which the entry was found
     * @param lineNumber line number of first line of entry
     */
    public Entry(
        String firstLine, String body, String filename, long lineNumber, boolean shortEntry) {
      this.firstLine = firstLine;
      this.body = body;
      this.filename = filename;
      this.lineNumber = lineNumber;
      this.shortEntry = shortEntry;
    }

    /**
     * Returns a substring of the entry body that matches the specified regular expression. If no
     * match is found, returns the firstLine.
     *
     * @param re regex to match
     * @return a substring that matches re
     */
    public String getDescription(@Nullable Pattern re) {

      if (re == null) {
        return firstLine;
      }

      Matcher descr = re.matcher(body);
      if (descr.find()) {
        return descr.group();
      } else {
        return firstLine;
      }
    }
  }

  ///
  /// Constructors
  ///

  /// Inputstream and charset constructors

  /**
   * Create an EntryReader that uses the given character set.
   *
   * @throws UnsupportedEncodingException if the charset encoding is not supported
   * @param in source from which to read entries
   * @param charsetName the character set to use
   * @param filename non-null file name for stream being read
   * @param commentRegexString regular expression that matches comments. Any text that matches
   *     commentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @see #EntryReader(InputStream,String,String,String)
   */
  public EntryReader(
      InputStream in,
      String charsetName,
      String filename,
      @Nullable @Regex String commentRegexString,
      @Nullable @Regex(1) String includeRegexString)
      throws UnsupportedEncodingException {
    this(new InputStreamReader(in, charsetName), filename, commentRegexString, includeRegexString);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param in the InputStream
   * @param charsetName the character set to use
   * @param filename the file name
   * @throws UnsupportedEncodingException if the charset encoding is not supported
   * @see #EntryReader(InputStream,String,String,String)
   */
  public EntryReader(InputStream in, String charsetName, String filename)
      throws UnsupportedEncodingException {
    this(in, charsetName, filename, null, null);
  }

  /// Inputstream (no charset) constructors

  /**
   * Create an EntryReader.
   *
   * @param in source from which to read entries
   * @param filename non-null file name for stream being read
   * @param commentRegexString regular expression that matches comments. Any text that matches
   *     commentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   */
  public EntryReader(
      InputStream in,
      String filename,
      @Nullable @Regex String commentRegexString,
      @Nullable @Regex(1) String includeRegexString) {
    this(new InputStreamReader(in, UTF_8), filename, commentRegexString, includeRegexString);
  }

  /**
   * Create an EntryReader that uses the default character set and does not support comments or
   * include directives.
   *
   * @param in the InputStream
   * @param filename the file name
   * @see #EntryReader(InputStream,String,String,String,String)
   */
  public EntryReader(InputStream in, String filename) {
    this(in, filename, null, null);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param in the InputStream
   * @see #EntryReader(InputStream,String,String,String)
   */
  public EntryReader(InputStream in) {
    this(in, "(InputStream)", null, null);
  }

  /** A dummy Reader to be used when null is not acceptable. */
  private static class DummyReader extends Reader {
    @Override
    public void close(@GuardSatisfied DummyReader this) {
      // No error, because closing is OK if it appears in try-with-resources.
      // Later maybe create two versions (with and without exception here).
    }

    @Override
    public void mark(@GuardSatisfied DummyReader this, int readAheadLimit) {
      throw new Error("DummyReader");
    }

    @Override
    public boolean markSupported() {
      throw new Error("DummyReader");
    }

    @Override
    public @GTENegativeOne int read(@GuardSatisfied DummyReader this) {
      throw new Error("DummyReader");
    }

    @Override
    public @IndexOrLow("#1") int read(@GuardSatisfied DummyReader this, char[] cbuf) {
      throw new Error("DummyReader");
    }

    @Override
    public @IndexOrLow("#1") int read(
        @GuardSatisfied DummyReader this, char[] cbuf, int off, int len) {
      throw new Error("DummyReader");
    }

    @Override
    public @GTENegativeOne int read(@GuardSatisfied DummyReader this, CharBuffer target) {
      throw new Error("DummyReader");
    }

    @Override
    public boolean ready() {
      throw new Error("DummyReader");
    }

    @Override
    public void reset(@GuardSatisfied DummyReader this) {
      throw new Error("DummyReader");
    }

    @Override
    public @NonNegative long skip(@GuardSatisfied DummyReader this, long n) {
      throw new Error("DummyReader");
    }
  }

  /**
   * Create an EntryReader.
   *
   * @param reader source from which to read entries
   * @param filename file name corresponding to reader, for use in error messages
   * @param commentRegexString regular expression that matches comments. Any text that matches
   *     commentRegex is removed. A line that is entirely a comment is ignored
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name
   */
  public EntryReader(
      Reader reader,
      String filename,
      @Nullable @Regex String commentRegexString,
      @Nullable @Regex(1) String includeRegexString) {
    // we won't use superclass methods, but passing null as an argument
    // leads to a NullPointerException.
    super(new DummyReader());
    readers.addFirst(new FlnReader(reader, filename));
    if (commentRegexString == null) {
      commentRegex = null;
    } else {
      commentRegex = Pattern.compile(commentRegexString);
    }
    if (includeRegexString == null) {
      includeRegex = null;
    } else {
      includeRegex = Pattern.compile(includeRegexString);
    }
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param reader source from which to read entries
   * @see #EntryReader(Reader,String,String,String)
   */
  public EntryReader(Reader reader) {
    this(reader, reader.toString(), null, null);
  }

  /// Path constructors

  /**
   * Create an EntryReader.
   *
   * @param path initial file to read
   * @param commentRegex regular expression that matches comments. Any text that matches
   *     commentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   */
  public EntryReader(
      Path path, @Nullable @Regex String commentRegex, @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(FilesPlume.newFileReader(path), path.toString(), commentRegex, includeRegex);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param path the file to read
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,String,String)
   */
  public EntryReader(Path path) throws IOException {
    this(path, null, null);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param path the file to read
   * @param charsetName the character set to use
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(Path,String,String)
   */
  public EntryReader(Path path, String charsetName) throws IOException {
    this(FilesPlume.newFileInputStream(path), charsetName, path.toString(), null, null);
  }

  /// File constructors

  /**
   * Create an EntryReader.
   *
   * @param file initial file to read
   * @param commentRegex regular expression that matches comments. Any text that matches
   *     commentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   */
  public EntryReader(
      File file, @Nullable @Regex String commentRegex, @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(FilesPlume.newFileReader(file), file.toString(), commentRegex, includeRegex);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param file the file to read
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,String,String)
   */
  public EntryReader(File file) throws IOException {
    this(file, null, null);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param file the file to read
   * @param charsetName the character set to use
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,String,String)
   */
  public EntryReader(File file, String charsetName) throws IOException {
    this(FilesPlume.newFileInputStream(file), charsetName, file.toString(), null, null);
  }

  /// Filename constructors

  /**
   * Create a new EntryReader starting with the specified file.
   *
   * @param filename initial file to read
   * @param commentRegex regular expression that matches comments. Any text that matches {@code
   *     commentRegex} is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,String,String)
   */
  public EntryReader(
      String filename,
      @Nullable @Regex String commentRegex,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(new File(filename), commentRegex, includeRegex);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param filename source from which to read entries
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(String,String,String)
   */
  public EntryReader(String filename) throws IOException {
    this(filename, null, null);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param filename source from which to read entries
   * @param charsetName the character set to use
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(String,String,String)
   */
  public EntryReader(String filename, String charsetName) throws IOException {
    this(new FileInputStream(filename), charsetName, filename, null, null);
  }

  ///
  /// Methods
  ///

  /**
   * Read a line, ignoring comments and processing includes. Note that a line that is completely a
   * comment is completely ignored (and not returned as a blank line). Returns null at end of file.
   *
   * @return the string that was read, or null at end of file
   */
  @Override
  public @Nullable String readLine(@GuardSatisfied EntryReader this) throws IOException {

    // System.out.printf ("Entering size = %d%n", readers.size());

    // If a line has been pushed back, return it instead
    if (pushbackLine != null) {
      String line = pushbackLine;
      pushbackLine = null;
      return line;
    }

    String line = getNextLine();
    if (commentRegex != null) {
      while (line != null) {
        Matcher cmatch = commentRegex.matcher(line);
        if (cmatch.find()) {
          line = cmatch.replaceFirst("");
          if (line.length() > 0) {
            break;
          }
        } else {
          break;
        }
        line = getNextLine();
        // System.out.printf ("getNextLine = %s%n", line);
      }
    }

    if (line == null) {
      return null;
    }

    // Handle include files.  Non-absolute pathnames are relative
    // to the including file (the current file)
    if (includeRegex != null) {
      Matcher m = includeRegex.matcher(line);
      if (m.matches()) {
        String filenameString = m.group(1);
        if (filenameString == null) {
          throw new Error(
              String.format(
                  "includeRegex (%s) does not capture group 1 in %s", includeRegex, line));
        }
        File filename = new File(FilesPlume.expandFilename(filenameString));
        // System.out.printf ("Trying to include filename %s%n", filename);
        if (!filename.isAbsolute()) {
          FlnReader reader = readers.getFirst();
          File currentFilename = new File(reader.filename);
          File currentParent = currentFilename.getParentFile();
          filename = new File(currentParent, filename.toString());
          // System.out.printf ("absolute filename = %s %s %s%n",
          //                     currentFilename, currentParent, filename);
        }
        readers.addFirst(new FlnReader(filename.getAbsolutePath()));
        return readLine();
      }
    }

    // System.out.printf ("Returning [%d] '%s'%n", readers.size(), line);
    return (line);
  }

  /**
   * Returns a line-by-line iterator for this file.
   *
   * <p><b>Warning:</b> This does not return a fresh iterator each time. The iterator is a
   * singleton, the same one is returned each time, and a new one can never be created after it is
   * exhausted.
   *
   * @return a line-by-line iterator for this file
   */
  @Override
  public Iterator<String> iterator(EntryReader this) {
    return this;
  }

  /**
   * Returns whether or not there is another line to read. Any IOExceptions are turned into errors
   * (because the definition of hasNext() in Iterator doesn't throw any exceptions).
   *
   * @return whether there is another line to read
   */
  @SuppressWarnings({
    "allcheckers:purity",
    "lock:method.guarantee.violated"
  }) // readLine might throw, has side effects
  @Override
  public boolean hasNext(@GuardSatisfied EntryReader this) {
    if (pushbackLine != null) {
      return true;
    }

    String line = null;
    try {
      line = readLine();
    } catch (IOException e) {
      throw new Error("unexpected IOException: ", e);
    }

    if (line == null) {
      return false;
    }

    putback(line);
    return true;
  }

  /**
   * Returns the next line in the multi-file.
   *
   * @return the next line in the multi-file
   * @throws NoSuchElementException at end of file
   */
  @Override
  public String next(@GuardSatisfied EntryReader this) {
    try {
      String result = readLine();
      if (result != null) {
        return result;
      } else {
        throw new NoSuchElementException();
      }
    } catch (IOException e) {
      throw new Error("unexpected IOException", e);
    }
  }

  /** remove() is not supported. */
  @Override
  public void remove(@GuardSatisfied EntryReader this) {
    throw new UnsupportedOperationException("can't remove lines from file");
  }

  /**
   * Returns the next entry (paragraph) in the file. Entries are separated by blank lines unless the
   * entry started with {@link #entryStartRegex} (see {@link #setEntryStartStop}). If no more
   * entries are available, returns null.
   *
   * @return the next entry (paragraph) in the file
   * @throws IOException if there is a problem reading the file
   */
  public @Nullable Entry getEntry(@GuardSatisfied EntryReader this) throws IOException {

    // Skip any preceding blank lines
    String line = readLine();
    while ((line != null) && (line.trim().length() == 0)) {
      line = readLine();
    }
    if (line == null) {
      return (null);
    }

    StringBuilder body = new StringBuilder(10000);
    Entry entry = null;
    String filename = getFileName();
    long lineNumber = getLineNumber();

    // If first line matches entryStartRegex, this is a long entry.
    @Regex(1) Matcher entryMatch = null;
    if (entryStartRegex != null) {
      entryMatch = entryStartRegex.matcher(line);
    }
    if ((entryMatch != null) && entryMatch.find()) {
      assert entryStartRegex != null : "@AssumeAssertion(nullness): dependent: entryMatch != null";
      assert entryStopRegex != null
          : "@AssumeAssertion(nullness): dependent: entryStartRegex != null";

      // Remove entry match from the line
      if (entryMatch.groupCount() > 0) {
        @SuppressWarnings(
            "nullness") // dependent: groupCount() checked group; https://tinyurl.com/cfissue/291
        @NonNull String matchGroup1 = entryMatch.group(1);
        line = entryMatch.replaceFirst(matchGroup1);
      }

      // Description is the first line
      String description = line;

      // Read until we find the termination of the entry
      Matcher endEntryMatch = entryStopRegex.matcher(line);
      while ((line != null)
          && !entryMatch.find()
          && !endEntryMatch.find()
          && filename.equals(getFileName())) {
        body.append(line);
        body.append(lineSep);
        line = readLine();
        if (line == null) {
          break; // end of file serves as entry terminator
        }
        entryMatch = entryStartRegex.matcher(line);
        endEntryMatch = entryStopRegex.matcher(line);
      }

      // If this entry was terminated by the start of the next one,
      // put that line back
      if ((line != null) && (entryMatch.find(0) || !filename.equals(getFileName()))) {
        putback(line);
      }

      entry = new Entry(description, body.toString(), filename, lineNumber, false);

    } else { // blank-separated entry

      String description = line;

      // Read until we find another blank line
      while ((line != null) && (line.trim().length() != 0) && filename.equals(getFileName())) {
        body.append(line);
        body.append(lineSep);
        line = readLine();
      }

      // If this entry was terminated by the start of a new input file
      // put that line back
      if ((line != null) && !filename.equals(getFileName())) {
        putback(line);
      }

      entry = new Entry(description, body.toString(), filename, lineNumber, true);
    }

    return (entry);
  }

  /**
   * Reads the next line from the current reader. If EOF is encountered pop out to the next reader.
   * Returns null if there is no more input.
   *
   * @return next line from the reader, or null if there is no more input
   * @throws IOException if there is trouble with the reader
   */
  private @Nullable String getNextLine(@GuardSatisfied EntryReader this) throws IOException {

    if (readers.size() == 0) {
      return (null);
    }

    FlnReader ri1 = readers.getFirst();
    String line = ri1.readLine();
    while (line == null) {
      readers.removeFirst();
      if (readers.isEmpty()) {
        return (null);
      }
      FlnReader ri2 = readers.peekFirst();
      line = ri2.readLine();
    }
    return (line);
  }

  /**
   * Returns the current filename.
   *
   * @return the current filename
   */
  public String getFileName(@GuardSatisfied EntryReader this) {
    FlnReader ri = readers.peekFirst();
    if (ri == null) {
      throw new Error("Past end of input");
    }
    return ri.filename;
  }

  /**
   * Returns the current line number in the current file.
   *
   * @return the current line number
   */
  @Override
  public @NonNegative int getLineNumber(@GuardSatisfied EntryReader this) {
    FlnReader ri = readers.peekFirst();
    if (ri == null) {
      throw new Error("Past end of input");
    }
    return ri.getLineNumber();
  }

  /**
   * Set the current line number in the current file.
   *
   * @param lineNumber new line number for the current file
   */
  @Override
  public void setLineNumber(@GuardSatisfied EntryReader this, @NonNegative int lineNumber) {
    FlnReader ri = readers.peekFirst();
    if (ri == null) {
      throw new Error("Past end of input");
    }
    ri.setLineNumber(lineNumber);
  }

  /**
   * Set the regular expressions for the start and stop of long entries (multiple lines that are
   * read as a group by getEntry()).
   *
   * @param entryStartRegex regular expression that starts a long entry
   * @param entryStopRegex regular expression that ends a long entry
   */
  public void setEntryStartStop(
      @GuardSatisfied EntryReader this,
      @Regex(1) String entryStartRegex,
      @Regex String entryStopRegex) {
    this.entryStartRegex = Pattern.compile(entryStartRegex);
    this.entryStopRegex = Pattern.compile(entryStopRegex);
  }

  /**
   * Set the regular expressions for the start and stop of long entries (multiple lines that are
   * read as a group by getEntry()).
   *
   * @param entryStartRegex regular expression that starts a long entry
   * @param entryStopRegex regular expression that ends a long entry
   */
  public void setEntryStartStop(
      @GuardSatisfied EntryReader this, @Regex(1) Pattern entryStartRegex, Pattern entryStopRegex) {
    this.entryStartRegex = entryStartRegex;
    this.entryStopRegex = entryStopRegex;
  }

  /**
   * Puts the specified line back in the input. Only one line can be put back.
   *
   * @param line the line to be put back in the input
   */
  // TODO:  This would probably be better implemented with the "mark" mechanism
  // of BufferedReader (which is also in LineNumberReader and FlnReader).
  public void putback(@GuardSatisfied EntryReader this, String line) {
    if (pushbackLine != null) {
      throw new Error(
          "Cannot put back '" + line + "' because already put back '" + pushbackLine + "'");
    }
    pushbackLine = line;
  }

  // No Javadoc on these methods, so the Javadoc is inherited.
  @Override
  public void mark(@GuardSatisfied EntryReader this, int readAheadLimit) {
    throw new Error("not yet implemented");
  }

  @Override
  public @GTENegativeOne int read(@GuardSatisfied EntryReader this) {
    throw new Error("not yet implemented");
  }

  @Override
  public @IndexOrLow("#1") int read(
      @GuardSatisfied EntryReader this, char[] cbuf, int off, int len) {
    throw new Error("not yet implemented");
  }

  @Override
  public void reset(@GuardSatisfied EntryReader this) {
    throw new Error("not yet implemented");
  }

  @Override
  public @NonNegative long skip(@GuardSatisfied EntryReader this, long n) {
    throw new Error("not yet implemented");
  }

  /**
   * Simple usage example.
   *
   * @param args command-line arguments: filename [commentRegex [includeRegex]]
   * @throws IOException if there is a problem reading a file
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 1 || args.length > 3) {
      System.err.println(
          "EntryReader sample program requires 1-3 args: filename [commentRegex [includeRegex]]");
      System.exit(1);
    }
    String filename = args[0];
    String commentRegex = null;
    String includeRegex = null;
    if (args.length >= 2) {
      commentRegex = args[1];
      if (!RegexUtil.isRegex(commentRegex)) {
        System.err.println(
            "Error parsing comment regex \""
                + commentRegex
                + "\": "
                + RegexUtil.regexError(commentRegex));
        System.exit(1);
      }
    }
    if (args.length >= 3) {
      includeRegex = args[2];
      if (!RegexUtil.isRegex(includeRegex, 1)) {
        System.err.println(
            "Error parsing include regex \""
                + includeRegex
                + "\": "
                + RegexUtil.regexError(includeRegex));
        System.exit(1);
      }
    }
    EntryReader reader = new EntryReader(filename, commentRegex, includeRegex);

    String line = reader.readLine();
    while (line != null) {
      System.out.printf("%s: %d: %s%n", reader.getFileName(), reader.getLineNumber(), line);
      line = reader.readLine();
    }
  }
}
