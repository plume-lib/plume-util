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
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*>>>
import org.checkerframework.checker.lock.qual.*;
import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.regex.qual.*;
*/

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
 * @see #get_entry() and @see #set_entry_start_stop(String,String)
 */
@SuppressWarnings("IterableAndIterator")
public class EntryReader extends LineNumberReader implements Iterable<String>, Iterator<String> {

  ///
  /// User configuration variables
  ///

  /** Regular expression that specifies an include file. */
  private final /*@Nullable*/ /*@Regex(1)*/ Pattern include_re;

  /** Regular expression that matches a comment. */
  private final /*@Nullable*/ Pattern comment_re;

  /**
   * Regular expression that starts a long entry.
   *
   * <p>If the first line of an entry matches this regexp, then the entry is terminated by: {@link
   * #entry_stop_re}, another line that matches {@code entry_start_re} (even not following a
   * newline), or the end of the current file.
   *
   * <p>Otherwise, the first line of an entry does NOT match this regexp (or the regexp is null), in
   * which case the entry is terminated by a blank line or the end of the current file.
   */
  public /*@MonotonicNonNull*/ /*@Regex(1)*/ Pattern entry_start_re = null;

  /** @see #entry_start_re */
  public /*@MonotonicNonNull*/ Pattern entry_stop_re = null;

  ///
  /// Internal implementation variables
  ///

  /** Stack of readers. Used to support include files. */
  private final ArrayDeque<FlnReader> readers = new ArrayDeque<FlnReader>();

  /** Line that is pushed back to be reread. */
  /*@Nullable*/ String pushback_line = null;

  /** Platform-specific line separator. */
  private static final String lineSep = System.getProperty("line.separator");

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
     */
    public FlnReader(String filename) throws IOException {
      super(UtilPlume.fileReader(filename));
      this.filename = filename;
    }
  }

  /** Descriptor for an entry (record, paragraph, etc.). */
  public static class Entry {
    /** First line of the entry. */
    public String first_line;
    /** Complete body of the entry including the first line. */
    public String body;
    /** True if this is a short entry (blank-line-separated). */
    boolean short_entry;
    /** Filename in which the entry was found. */
    String filename;
    /** Line number of first line of entry. */
    long line_number;

    /**
     * Create an entry.
     *
     * @param first_line first line of the entry
     * @param body complete body of the entry including the first line
     * @param short_entry true if this is a short entry (blank-line-separated)
     * @param filename filename in which the entry was found
     * @param line_number line number of first line of entry
     */
    Entry(String first_line, String body, String filename, long line_number, boolean short_entry) {
      this.first_line = first_line;
      this.body = body;
      this.filename = filename;
      this.line_number = line_number;
      this.short_entry = short_entry;
    }

    /**
     * Return a substring of the entry body that matches the specified regular expression. If no
     * match is found, returns the first_line.
     *
     * @param re regex to match
     * @return a substring that matches re
     */
    String get_description(/*@Nullable*/ Pattern re) {

      if (re == null) {
        return first_line;
      }

      Matcher descr = re.matcher(body);
      if (descr.find()) {
        return descr.group();
      } else {
        return first_line;
      }
    }
  }

  ///
  /// Constructors
  ///

  /// Inputstream and charset constructors

  /**
   * Create a EntryReader that uses the given character set.
   *
   * @throws UnsupportedEncodingException if the charset encoding is not supported
   * @param in source from which to read entries
   * @param charsetName the character set to use
   * @param filename non-null file name for stream being read
   * @param comment_re_string regular expression that matches comments. Any text that matches
   *     comment_re is removed. A line that is entirely a comment is ignored.
   * @param include_re_string regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @see #EntryReader(InputStream,String,String,String)
   */
  public EntryReader(
      InputStream in,
      String charsetName,
      String filename,
      /*@Nullable*/ /*@Regex*/ String comment_re_string,
      /*@Nullable*/ /*@Regex(1)*/ String include_re_string)
      throws UnsupportedEncodingException {
    this(new InputStreamReader(in, charsetName), filename, comment_re_string, include_re_string);
  }

  /**
   * Create a EntryReader that does not support comments or include directives.
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
   * Create a EntryReader.
   *
   * @param in source from which to read entries
   * @param filename non-null file name for stream being read
   * @param comment_re_string regular expression that matches comments. Any text that matches
   *     comment_re is removed. A line that is entirely a comment is ignored.
   * @param include_re_string regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   */
  public EntryReader(
      InputStream in,
      String filename,
      /*@Nullable*/ /*@Regex*/ String comment_re_string,
      /*@Nullable*/ /*@Regex(1)*/ String include_re_string) {
    this(new InputStreamReader(in, UTF_8), filename, comment_re_string, include_re_string);
  }

  /**
   * Create a EntryReader that uses the default character set and does not support comments or
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
   * Create a EntryReader that does not support comments or include directives.
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
    public void close(/*>>>@GuardSatisfied DummyReader this*/) {
      // No error, because closing is OK if it appears in try-with-resources.
      // Later maybe create two versions (with and without exception here).
    }

    @Override
    public void mark(/*>>>@GuardSatisfied DummyReader this, */ int readAheadLimit) {
      throw new Error("DummyReader");
    }

    @Override
    public boolean markSupported() {
      throw new Error("DummyReader");
    }

    @Override
    public /*@GTENegativeOne*/ int read(/*>>>@GuardSatisfied DummyReader this*/) {
      throw new Error("DummyReader");
    }

    @Override
    public /*@IndexOrLow("#1")*/ int read(/*>>>@GuardSatisfied DummyReader this, */ char[] cbuf) {
      throw new Error("DummyReader");
    }

    @Override
    public /*@IndexOrLow("#1")*/ int read(
        /*>>>@GuardSatisfied DummyReader this, */ char[] cbuf, int off, int len) {
      throw new Error("DummyReader");
    }

    @Override
    public /*@GTENegativeOne*/ int read(
        /*>>>@GuardSatisfied DummyReader this, */ CharBuffer target) {
      throw new Error("DummyReader");
    }

    @Override
    public boolean ready() {
      throw new Error("DummyReader");
    }

    @Override
    public void reset(/*>>>@GuardSatisfied DummyReader this*/) {
      throw new Error("DummyReader");
    }

    @Override
    public long skip(/*>>>@GuardSatisfied DummyReader this, */ long n) {
      throw new Error("DummyReader");
    }
  }

  /**
   * Create a EntryReader.
   *
   * @param reader source from which to read entries
   * @param filename file name corresponding to reader, for use in error messages
   * @param comment_re_string regular expression that matches comments. Any text that matches
   *     comment_re is removed. A line that is entirely a comment is ignored
   * @param include_re_string regular expression that matches include directives. The expression
   *     should define one group that contains the include file name
   */
  public EntryReader(
      Reader reader,
      String filename,
      /*@Nullable*/ /*@Regex*/ String comment_re_string,
      /*@Nullable*/ /*@Regex(1)*/ String include_re_string) {
    // we won't use superclass methods, but passing null as an argument
    // leads to a NullPointerException.
    super(new DummyReader());
    readers.addFirst(new FlnReader(reader, filename));
    if (comment_re_string == null) {
      comment_re = null;
    } else {
      comment_re = Pattern.compile(comment_re_string);
    }
    if (include_re_string == null) {
      include_re = null;
    } else {
      include_re = Pattern.compile(include_re_string);
    }
  }

  /**
   * Create a EntryReader that does not support comments or include directives.
   *
   * @param reader source from which to read entries
   * @see #EntryReader(Reader,String,String,String)
   */
  public EntryReader(Reader reader) {
    this(reader, reader.toString(), null, null);
  }

  /// File Constructors

  /**
   * Create an EntryReader.
   *
   * @param file initial file to read
   * @param comment_re regular expression that matches comments. Any text that matches comment_re is
   *     removed. A line that is entirely a comment is ignored.
   * @param include_re regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   */
  public EntryReader(
      File file,
      /*@Nullable*/ /*@Regex*/ String comment_re,
      /*@Nullable*/ /*@Regex(1)*/ String include_re)
      throws IOException {
    this(UtilPlume.fileReader(file), file.toString(), comment_re, include_re);
  }

  /**
   * Create a EntryReader that does not support comments or include directives.
   *
   * @param file the file to read
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,String,String)
   */
  public EntryReader(File file) throws IOException {
    this(file, null, null);
  }

  /**
   * Create a EntryReader that does not support comments or include directives.
   *
   * @param file the file to read
   * @param charsetName the character set to use
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,String,String)
   */
  public EntryReader(File file, String charsetName) throws IOException {
    this(UtilPlume.fileInputStream(file), charsetName, file.toString(), null, null);
  }

  /// Filename constructors

  /**
   * Create a new EntryReader starting with the specified file.
   *
   * @param filename initial file to read
   * @param comment_re regular expression that matches comments. Any text that matches {@code
   *     comment_re} is removed. A line that is entirely a comment is ignored.
   * @param include_re regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,String,String)
   */
  public EntryReader(
      String filename,
      /*@Nullable*/ /*@Regex*/ String comment_re,
      /*@Nullable*/ /*@Regex(1)*/ String include_re)
      throws IOException {
    this(new File(filename), comment_re, include_re);
  }

  /**
   * Create a EntryReader that does not support comments or include directives.
   *
   * @param filename source from which to read entries
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(String,String,String)
   */
  public EntryReader(String filename) throws IOException {
    this(filename, null, null);
  }

  /**
   * Create a EntryReader that does not support comments or include directives.
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
  public /*@Nullable*/ String readLine(
      /*>>>@GuardSatisfied EntryReader this*/) throws IOException {

    // System.out.printf ("Entering size = %d%n", readers.size());

    // If a line has been pushed back, return it instead
    if (pushback_line != null) {
      String line = pushback_line;
      pushback_line = null;
      return line;
    }

    String line = get_next_line();
    if (comment_re != null) {
      while (line != null) {
        Matcher cmatch = comment_re.matcher(line);
        if (cmatch.find()) {
          line = cmatch.replaceFirst("");
          if (line.length() > 0) {
            break;
          }
        } else {
          break;
        }
        line = get_next_line();
        // System.out.printf ("get_next_line = %s%n", line);
      }
    }

    if (line == null) {
      return null;
    }

    // Handle include files.  Non-absolute pathnames are relative
    // to the including file (the current file)
    if (include_re != null) {
      Matcher m = include_re.matcher(line);
      if (m.matches()) {
        String filename_string = m.group(1);
        if (filename_string == null) {
          throw new Error(
              String.format("include_re (%s) does not capture group 1 in %s", include_re, line));
        }
        File filename = new File(UtilPlume.expandFilename(filename_string));
        // System.out.printf ("Trying to include filename %s%n", filename);
        if (!filename.isAbsolute()) {
          FlnReader reader = readers.getFirst();
          File current_filename = new File(reader.filename);
          File current_parent = current_filename.getParentFile();
          filename = new File(current_parent, filename.toString());
          // System.out.printf ("absolute filename = %s %s %s%n",
          //                     current_filename, current_parent, filename);
        }
        readers.addFirst(new FlnReader(filename.getAbsolutePath()));
        return readLine();
      }
    }

    // System.out.printf ("Returning [%d] '%s'%n", readers.size(), line);
    return (line);
  }

  /**
   * Return a line-by-line iterator for this file.
   *
   * <p><b>Warning:</b> This does not return a fresh iterator each time. The iterator is a
   * singleton, the same one is returned each time, and a new one can never be created after it is
   * exhausted.
   *
   * @return a line-by-line iterator for this file
   */
  @Override
  public Iterator<String> iterator() {
    return this;
  }

  /**
   * Returns whether or not there is another line to read. Any IOExceptions are turned into errors
   * (because the definition of hasNext() in Iterator doesn't throw any exceptions).
   *
   * @return whether there is another line to read
   */
  @Override
  public boolean hasNext(/*>>>@GuardSatisfied EntryReader this*/) {
    if (pushback_line != null) {
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
   * Return the next line in the multi-file.
   *
   * @return the next line in the multi-file
   * @throws NoSuchElementException at end of file
   */
  @Override
  public String next(/*>>>@GuardSatisfied EntryReader this*/) {
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
  public void remove(/*>>>@GuardSatisfied EntryReader this*/) {
    throw new UnsupportedOperationException("can't remove lines from file");
  }

  /**
   * Returns the next entry (paragraph) in the file. Entries are separated by blank lines unless the
   * entry started with {@link #entry_start_re} (see {@link #set_entry_start_stop}). If no more
   * entries are available, returns null.
   *
   * @return the next entry (paragraph) in the file
   * @throws IOException if there is a problem reading the file
   */
  public /*@Nullable*/ Entry get_entry(
      /*>>>@GuardSatisfied EntryReader this*/) throws IOException {

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
    long line_number = getLineNumber();

    // If first line matches entry_start_re, this is a long entry.
    /*@Regex(1)*/ Matcher entry_match = null;
    if (entry_start_re != null) {
      entry_match = entry_start_re.matcher(line);
    }
    if ((entry_match != null) && entry_match.find()) {
      assert entry_start_re != null : "@AssumeAssertion(nullness): dependent: entry_match != null";
      assert entry_stop_re != null
          : "@AssumeAssertion(nullness): dependent: entry_start_re != null";

      // Remove entry match from the line
      if (entry_match.groupCount() > 0) {
        @SuppressWarnings(
            "nullness") // dependent: groupCount() checked group; https://tinyurl.com/cfissue/291
        /*@NonNull*/ String match_group_1 = entry_match.group(1);
        line = entry_match.replaceFirst(match_group_1);
      }

      // Description is the first line
      String description = line;

      // Read until we find the termination of the entry
      Matcher end_entry_match = entry_stop_re.matcher(line);
      while ((line != null)
          && !entry_match.find()
          && !end_entry_match.find()
          && filename.equals(getFileName())) {
        body.append(line);
        body.append(lineSep);
        line = readLine();
        if (line == null) {
          break; // end of file serves as entry terminator
        }
        entry_match = entry_start_re.matcher(line);
        end_entry_match = entry_stop_re.matcher(line);
      }

      // If this entry was terminated by the start of the next one,
      // put that line back
      if ((line != null) && (entry_match.find(0) || !filename.equals(getFileName()))) {
        putback(line);
      }

      entry = new Entry(description, body.toString(), filename, line_number, false);

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

      entry = new Entry(description, body.toString(), filename, line_number, true);
    }

    return (entry);
  }

  /**
   * Reads the next line from the current reader. If EOF is encountered pop out to the next reader.
   * Returns null if there is no more input.
   *
   * @return next line from the reader, or null if there is no more input
   */
  private /*@Nullable*/ String get_next_line(
      /*>>>@GuardSatisfied EntryReader this*/) throws IOException {

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
  public String getFileName(/*>>>@GuardSatisfied EntryReader this*/) {
    FlnReader ri = readers.peekFirst();
    if (ri == null) {
      throw new Error("Past end of input");
    }
    return ri.filename;
  }

  /**
   * Return the current line number in the current file.
   *
   * @return the current line number
   */
  @Override
  public /*@NonNegative*/ int getLineNumber(/*>>>@GuardSatisfied EntryReader this*/) {
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
  public void setLineNumber(
      /*>>>@GuardSatisfied EntryReader this, */
      /*@NonNegative*/ int lineNumber) {
    FlnReader ri = readers.peekFirst();
    if (ri == null) {
      throw new Error("Past end of input");
    }
    ri.setLineNumber(lineNumber);
  }

  /**
   * Set the regular expressions for the start and stop of long entries (multiple lines that are
   * read as a group by get_entry()).
   *
   * @param entry_start_re regular expression that starts a long entry
   * @param entry_stop_re regular expression that ends a long entry
   */
  public void set_entry_start_stop(
      /*>>>@GuardSatisfied EntryReader this, */
      /*@Regex(1)*/ String entry_start_re, /*@Regex*/ String entry_stop_re) {
    this.entry_start_re = Pattern.compile(entry_start_re);
    this.entry_stop_re = Pattern.compile(entry_stop_re);
  }

  /**
   * Set the regular expressions for the start and stop of long entries (multiple lines that are
   * read as a group by get_entry()).
   *
   * @param entry_start_re regular expression that starts a long entry
   * @param entry_stop_re regular expression that ends a long entry
   */
  public void set_entry_start_stop(
      /*>>>@GuardSatisfied EntryReader this, */
      /*@Regex(1)*/ Pattern entry_start_re, Pattern entry_stop_re) {
    this.entry_start_re = entry_start_re;
    this.entry_stop_re = entry_stop_re;
  }

  /**
   * Puts the specified line back in the input. Only one line can be put back.
   *
   * @param line the line to be put back in the input
   */
  // TODO:  This would probably be better implemented with the "mark" mechanism
  // of BufferedReader (which is also in LineNumberReader and FlnReader).
  public void putback(/*>>>@GuardSatisfied EntryReader this, */ String line) {
    assert pushback_line == null
        : "push back '" + line + "' when '" + pushback_line + "' already back";
    pushback_line = line;
  }

  // No Javadoc on these methods, so the Javadoc is inherited.
  @Override
  public void mark(/*>>>@GuardSatisfied EntryReader this, */ int readAheadLimit) {
    throw new Error("not yet implemented");
  }

  @Override
  public /*@GTENegativeOne*/ int read(/*>>>@GuardSatisfied EntryReader this*/) {
    throw new Error("not yet implemented");
  }

  @Override
  public /*@IndexOrLow("#1")*/ int read(
      /*>>>@GuardSatisfied EntryReader this, */ char[] cbuf, int off, int len) {
    throw new Error("not yet implemented");
  }

  @Override
  public void reset(/*>>>@GuardSatisfied EntryReader this*/) {
    throw new Error("not yet implemented");
  }

  @Override
  public long skip(/*>>>@GuardSatisfied EntryReader this, */ long n) {
    throw new Error("not yet implemented");
  }

  /**
   * Simple usage example.
   *
   * @param args command-line arguments: filename [comment_re [include_re]]
   * @throws IOException if there is a problem reading a file
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 1 || args.length > 3) {
      System.err.println(
          "EntryReader sample program requires 1-3 args: filename [comment_re [include_re]]");
      System.exit(1);
    }
    String filename = args[0];
    String comment_re = null;
    String include_re = null;
    if (args.length >= 2) {
      comment_re = args[1];
      if (!RegexUtil.isRegex(comment_re)) {
        System.err.println(
            "Error parsing comment regex \""
                + comment_re
                + "\": "
                + RegexUtil.regexError(comment_re));
        System.exit(1);
      }
    }
    if (args.length >= 3) {
      include_re = args[2];
      if (!RegexUtil.isRegex(include_re, 1)) {
        System.err.println(
            "Error parsing include regex \""
                + include_re
                + "\": "
                + RegexUtil.regexError(include_re));
        System.exit(1);
      }
    }
    EntryReader reader = new EntryReader(filename, comment_re, include_re);

    String line = reader.readLine();
    while (line != null) {
      System.out.printf("%s: %d: %s%n", reader.getFileName(), reader.getLineNumber(), line);
      line = reader.readLine();
    }
  }
}
