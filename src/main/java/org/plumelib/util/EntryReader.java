package org.plumelib.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.index.qual.IndexOrLow;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.mustcall.qual.MustCall;
import org.checkerframework.checker.mustcall.qual.MustCallAlias;
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
//  * It should have constructors that take a Reader
//    (in addition to the current BufferedReader, File, InputStream, and String versions).
//  * It should have a `close()` method (it already implements AutoCloseable,
//    though I don't know whether it does so adequately).
//  * It should automatically close the underlying file/etc. when the
//    iterator gets to the end (or the end is otherwise reached) -- or, better,
//    have the `close()` method do so.

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
 * <p>The syntax of each of these is customizable.
 *
 * <p>Here are example uses. The first reads by lines and the second reads by entries.
 *
 * <pre>{@code
 * // EntryReader constructor args are: filename, EntryFormat, CommentFormat, include regex.
 * // First argument can also be a File or Path; additional constructors also exist.
 * try (EntryReader er = new EntryReader(filename,
 *     EntryFormat.DEFAULT, CommentFormat.TEX, "\\\\include\\{(.*)\\}")) {
 *   for (String line : er) {
 *     ...
 *   }
 * } catch (IOException e) {
 *   System.err.println("Problem reading " + filename + ": " + e.getMessage());
 * }
 *
 * try (EntryReader er = new EntryReader(filename,
 *     EntryFormat.TWO_BLANK_LINES_AND_FENCED_CODE_BLOCKS,
 *     CommentFormat.HTML,
 *     null)) {
 *   for (EntryReader.Entry entry = er.getEntry(); entry != null; entry = er.getEntry()) {
 *     ...
 *   }
 * } catch (IOException e) {
 *   System.err.println("Problem reading " + filename + ": " + e.getMessage());
 * }
 * }</pre>
 *
 * @see #getEntry()
 */
@SuppressWarnings({
  "IterableAndIterator",
  "builder:required.method.not.called", // Collection `readers` has element type @MustCall("close")
  "PMD.CloseResource",
})
public class EntryReader extends LineNumberReader implements Iterable<String>, Iterator<String> {

  // ///////////////////////////////////////////////////////////////////////////
  // User configuration variables
  //

  /** Regular expression that specifies an include file. */
  private final @Nullable @Regex(1) Pattern includeRegex;

  /** Specifies how an entry begins and ends. */
  public final EntryFormat entryFormat;

  /** Specifies the syntax of comments (if comments are supported). */
  public final CommentFormat commentFormat;

  /** If true, output diagnostics. */
  public boolean debug = false;

  // ///////////////////////////////////////////////////////////////////////////
  // Internal implementation variables
  //

  // @MustCall({}) because the RLC does not (yet) support verifying collections of resources.
  /** Stack of readers. Used to support include files. */
  private final ArrayDeque<@MustCall({}) FlnReader> readers = new ArrayDeque<>();

  /** Line that is pushed back to be reread. */
  @Nullable String pushbackLine = null;

  /** Platform-specific line separator. */
  private static final String lineSep = System.lineSeparator();

  /** True if currently inside a fenced code block (``` ... ```). */
  private boolean inFencedCodeBlock = false;

  // ///////////////////////////////////////////////////////////////////////////
  // Constructors
  //

  // InputStream and charset constructors

  // This is the complete constructor that supplies all possible arguments.
  /**
   * Create an EntryReader that uses the given character set.
   *
   * @param in source from which to read entries
   * @param charsetName the character set to use
   * @param filename non-null file name for stream being read
   * @param entryFormat indicates how entries begin and end
   * @param commentFormat indicates the syntax of comments
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @throws UnsupportedEncodingException if the charset encoding is not supported
   */
  @SuppressWarnings("JdkObsolete") // due to use of string charsetName, remove in Java 11+
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in,
      String charsetName,
      String filename,
      EntryFormat entryFormat,
      CommentFormat commentFormat,
      @Nullable @Regex(1) String includeRegexString)
      throws UnsupportedEncodingException {
    this(
        new InputStreamReader(in, charsetName),
        filename,
        entryFormat,
        commentFormat,
        includeRegexString);
  }

  /**
   * Create an EntryReader that uses the given character set.
   *
   * @param in source from which to read entries
   * @param charsetName the character set to use
   * @param filename non-null file name for stream being read
   * @param entryFormat indicates how entries begin and end
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @throws UnsupportedEncodingException if the charset encoding is not supported
   * @deprecated use {@link
   *     #EntryReader(InputStream,String,String,EntryFormat,CommentFormat,String)}
   */
  @Deprecated // 2026-01-28
  @SuppressWarnings("JdkObsolete") // due to use of string charsetName, remove in Java 11+
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in,
      String charsetName,
      String filename,
      EntryFormat entryFormat,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString)
      throws UnsupportedEncodingException {
    this(
        new InputStreamReader(in, charsetName),
        filename,
        entryFormat,
        lineCommentRegexString,
        includeRegexString);
  }

  /**
   * Create an EntryReader that uses the given character set.
   *
   * @param in source from which to read entries
   * @param charsetName the character set to use
   * @param filename non-null file name for stream being read
   * @param twoBlankLines true if entries are separated by two blank lines rather than one
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @throws UnsupportedEncodingException if the charset encoding is not supported
   * @see #EntryReader(InputStream,String,String,String)
   * @deprecated use {@link #EntryReader(InputStream,String,String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-21
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in,
      String charsetName,
      String filename,
      boolean twoBlankLines,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString)
      throws UnsupportedEncodingException {
    this(
        in,
        charsetName,
        filename,
        twoBlankLines ? EntryFormat.TWO_BLANK_LINES : EntryFormat.DEFAULT,
        lineCommentRegexString,
        includeRegexString);
  }

  /**
   * Create an EntryReader that uses the given character set.
   *
   * @param in source from which to read entries
   * @param charsetName the character set to use
   * @param filename non-null file name for stream being read
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @throws UnsupportedEncodingException if the charset encoding is not supported
   * @see #EntryReader(InputStream,String,String,String)
   * @deprecated use {@link #EntryReader(InputStream,String,String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in,
      String charsetName,
      String filename,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString)
      throws UnsupportedEncodingException {
    this(
        in, charsetName, filename, EntryFormat.DEFAULT, lineCommentRegexString, includeRegexString);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param in the InputStream
   * @param charsetName the character set to use
   * @param filename the file name
   * @throws UnsupportedEncodingException if the charset encoding is not supported
   * @see #EntryReader(InputStream,String,String,String)
   * @deprecated use {@link #EntryReader(InputStream,String,String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in, String charsetName, String filename)
      throws UnsupportedEncodingException {
    this(in, charsetName, filename, EntryFormat.DEFAULT, CommentFormat.NONE, null);
  }

  // InputStream (no charset) constructors

  /**
   * Create an EntryReader.
   *
   * @param in source from which to read entries
   * @param filename non-null file name for stream being read
   * @param entryFormat indicates how entries begin and end
   * @param commentFormat indicates the syntax of comments
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   */
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in,
      String filename,
      EntryFormat entryFormat,
      CommentFormat commentFormat,
      @Nullable @Regex(1) String includeRegexString) {
    this(
        new InputStreamReader(in, UTF_8), filename, entryFormat, commentFormat, includeRegexString);
  }

  /**
   * Create an EntryReader.
   *
   * @param in source from which to read entries
   * @param filename non-null file name for stream being read
   * @param entryFormat indicates how entries begin and end
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @deprecated use {@link #EntryReader(InputStream,String,EntryFormat,CommentFormat,String)}
   */
  @Deprecated // 2026-01-28
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in,
      String filename,
      EntryFormat entryFormat,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString) {
    this(
        new InputStreamReader(in, UTF_8),
        filename,
        entryFormat,
        lineCommentRegexString,
        includeRegexString);
  }

  /**
   * Create an EntryReader.
   *
   * @param in source from which to read entries
   * @param filename non-null file name for stream being read
   * @param twoBlankLines true if entries are separated by two blank lines rather than one
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @deprecated use {@link #EntryReader(InputStream,String,String,EntryFormat,String,String)},
   *     passing {@code UTF_8} as the charset
   */
  @Deprecated // 2026-01-05
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in,
      String filename,
      boolean twoBlankLines,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString) {
    this(
        new InputStreamReader(in, UTF_8),
        filename,
        twoBlankLines ? EntryFormat.TWO_BLANK_LINES : EntryFormat.DEFAULT,
        lineCommentRegexString,
        includeRegexString);
  }

  /**
   * Create an EntryReader.
   *
   * @param in source from which to read entries
   * @param filename non-null file name for stream being read
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name.
   * @deprecated use {@link #EntryReader(InputStream,String,String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public @MustCallAlias EntryReader(
      @MustCallAlias InputStream in,
      String filename,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString) {
    this(in, filename, EntryFormat.DEFAULT, lineCommentRegexString, includeRegexString);
  }

  /**
   * Create an EntryReader that uses the default character set and does not support comments or
   * include directives.
   *
   * @param in the InputStream
   * @param filename the file name
   * @see #EntryReader(InputStream,String,String,String)
   */
  public @MustCallAlias EntryReader(@MustCallAlias InputStream in, String filename) {
    this(in, filename, EntryFormat.DEFAULT, CommentFormat.NONE, null);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param in the InputStream
   * @see #EntryReader(InputStream,String,String,String)
   */
  public @MustCallAlias EntryReader(@MustCallAlias InputStream in) {
    this(in, "(InputStream)", EntryFormat.DEFAULT, CommentFormat.NONE, null);
  }

  /**
   * Create an EntryReader.
   *
   * @param reader source from which to read entries
   * @param filename file name corresponding to reader, for use in error messages
   * @param entryFormat indicates how entries begin and end
   * @param commentFormat indicates the syntax of comments
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name
   */
  @SuppressWarnings("builder") // storing into a collection
  public @MustCallAlias EntryReader(
      @MustCallAlias Reader reader,
      String filename,
      EntryFormat entryFormat,
      CommentFormat commentFormat,
      @Nullable @Regex(1) String includeRegexString) {
    // We won't use superclass methods, but passing null as an argument
    // leads to a NullPointerException.
    super(DummyReader.it);
    readers.addFirst(new FlnReader(reader, filename));
    this.entryFormat = entryFormat;
    this.commentFormat = commentFormat;
    if (includeRegexString == null) {
      includeRegex = null;
    } else {
      includeRegex = Pattern.compile(includeRegexString);
    }
  }

  /**
   * Create an EntryReader.
   *
   * @param reader source from which to read entries
   * @param filename file name corresponding to reader, for use in error messages
   * @param entryFormat indicates how entries begin and end
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name
   * @deprecated see {@link #EntryReader(Reader,String,EntryFormat,CommentFormat,String)}
   */
  @Deprecated // 2026-01-28
  @SuppressWarnings("builder") // storing into a collection
  public @MustCallAlias EntryReader(
      @MustCallAlias Reader reader,
      String filename,
      EntryFormat entryFormat,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString) {
    // We won't use superclass methods, but passing null as an argument
    // leads to a NullPointerException.
    super(DummyReader.it);
    readers.addFirst(new FlnReader(reader, filename));
    this.entryFormat = entryFormat;
    this.commentFormat = new CommentFormat(lineCommentRegexString);
    if (includeRegexString == null) {
      includeRegex = null;
    } else {
      includeRegex = Pattern.compile(includeRegexString);
    }
  }

  /**
   * Create an EntryReader.
   *
   * @param reader source from which to read entries
   * @param filename file name corresponding to reader, for use in error messages
   * @param twoBlankLines if true, then entries are separated by two blank lines rather than one
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name
   * @deprecated use {@link #EntryReader(Reader,String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-21
  @SuppressWarnings("builder") // storing into a collection
  public @MustCallAlias EntryReader(
      @MustCallAlias Reader reader,
      String filename,
      boolean twoBlankLines,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString) {
    this(
        reader,
        filename,
        twoBlankLines ? EntryFormat.TWO_BLANK_LINES : EntryFormat.DEFAULT,
        lineCommentRegexString,
        includeRegexString);
  }

  /**
   * Create an EntryReader.
   *
   * @param reader source from which to read entries
   * @param filename file name corresponding to reader, for use in error messages
   * @param lineCommentRegexString regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored
   * @param includeRegexString regular expression that matches include directives. The expression
   *     should define one group that contains the include file name
   * @deprecated use {@link #EntryReader(Reader,String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  @SuppressWarnings("builder") // storing into a collection
  public @MustCallAlias EntryReader(
      @MustCallAlias Reader reader,
      String filename,
      @Nullable @Regex String lineCommentRegexString,
      @Nullable @Regex(1) String includeRegexString) {
    this(reader, filename, EntryFormat.DEFAULT, lineCommentRegexString, includeRegexString);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param reader source from which to read entries
   * @see #EntryReader(Reader,String,EntryFormat,String,String)
   */
  public @MustCallAlias EntryReader(@MustCallAlias Reader reader) {
    this(reader, reader.toString(), EntryFormat.DEFAULT, CommentFormat.NONE, null);
  }

  // Path constructors

  /**
   * Create an EntryReader.
   *
   * @param path initial file to read
   * @param entryFormat indicates how entries begin and end
   * @param commentFormat indicates the syntax of comments
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   */
  public EntryReader(
      Path path,
      EntryFormat entryFormat,
      CommentFormat commentFormat,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(FilesPlume.newFileReader(path), path.toString(), entryFormat, commentFormat, includeRegex);
  }

  /**
   * Create an EntryReader.
   *
   * @param path initial file to read
   * @param entryFormat indicates how entries begin and end
   * @param lineCommentRegex regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @deprecated see {@link #EntryReader(Path,EntryFormat,CommentFormat, String)}
   */
  @Deprecated // 2026-01-28
  public EntryReader(
      Path path,
      EntryFormat entryFormat,
      @Nullable @Regex String lineCommentRegex,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(
        FilesPlume.newFileReader(path),
        path.toString(),
        entryFormat,
        lineCommentRegex,
        includeRegex);
  }

  /**
   * Create an EntryReader.
   *
   * @param path initial file to read
   * @param twoBlankLines if true, then entries are separated by two blank lines rather than one
   * @param lineCommentRegex regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @deprecated use {@link #EntryReader(Path,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-21
  public EntryReader(
      Path path,
      boolean twoBlankLines,
      @Nullable @Regex String lineCommentRegex,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(
        path,
        twoBlankLines ? EntryFormat.TWO_BLANK_LINES : EntryFormat.DEFAULT,
        lineCommentRegex,
        includeRegex);
  }

  /**
   * Create an EntryReader.
   *
   * @param path initial file to read
   * @param lineCommentRegex regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @deprecated use {@link #EntryReader(Path,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public EntryReader(
      Path path, @Nullable @Regex String lineCommentRegex, @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(path, EntryFormat.DEFAULT, lineCommentRegex, includeRegex);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param path the file to read
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,EntryFormat,String,String)
   */
  public EntryReader(Path path) throws IOException {
    this(path, EntryFormat.DEFAULT, CommentFormat.NONE, null);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param path the file to read
   * @param charsetName the character set to use
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(InputStream,String,String,EntryFormat,String,String)
   * @deprecated use {@link #EntryReader(InputStream,String,String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public EntryReader(Path path, String charsetName) throws IOException {
    this(
        FilesPlume.newFileInputStream(path),
        charsetName,
        path.toString(),
        EntryFormat.DEFAULT,
        CommentFormat.NONE,
        null);
  }

  // File constructors

  /**
   * Create an EntryReader.
   *
   * @param file initial file to read
   * @param entryFormat indicates how entries begin and end
   * @param commentFormat indicates the syntax of comments
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   */
  public EntryReader(
      File file,
      EntryFormat entryFormat,
      CommentFormat commentFormat,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(FilesPlume.newFileReader(file), file.toString(), entryFormat, commentFormat, includeRegex);
  }

  /**
   * Create an EntryReader.
   *
   * @param file initial file to read
   * @param entryFormat indicates how entries begin and end
   * @param lineCommentRegex regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @deprecated see {@link #EntryReader(File, EntryFormat,CommentFormat,String)}
   */
  @Deprecated // 2026-01-28
  public EntryReader(
      File file,
      EntryFormat entryFormat,
      @Nullable @Regex String lineCommentRegex,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(
        FilesPlume.newFileReader(file),
        file.toString(),
        entryFormat,
        lineCommentRegex,
        includeRegex);
  }

  /**
   * Create an EntryReader.
   *
   * @param file initial file to read
   * @param twoBlankLines if true, then entries are separated by two blank lines rather than one
   * @param lineCommentRegex regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @deprecated use {@link #EntryReader(File,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-21
  public EntryReader(
      File file,
      boolean twoBlankLines,
      @Nullable @Regex String lineCommentRegex,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(
        file,
        twoBlankLines ? EntryFormat.TWO_BLANK_LINES : EntryFormat.DEFAULT,
        lineCommentRegex,
        includeRegex);
  }

  /**
   * Create an EntryReader.
   *
   * @param file initial file to read
   * @param lineCommentRegex regular expression that matches comments. Any text that matches
   *     lineCommentRegex is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @deprecated use {@link #EntryReader(File,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public EntryReader(
      File file, @Nullable @Regex String lineCommentRegex, @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(file, EntryFormat.DEFAULT, lineCommentRegex, includeRegex);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param file the file to read
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,EntryFormat,String,String)
   */
  public EntryReader(File file) throws IOException {
    this(file, EntryFormat.DEFAULT, CommentFormat.NONE, null);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param file the file to read
   * @param charsetName the character set to use
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,EntryFormat,String,String)
   * @deprecated use {@link #EntryReader(File,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public EntryReader(File file, String charsetName) throws IOException {
    this(
        FilesPlume.newFileInputStream(file),
        charsetName,
        file.toString(),
        EntryFormat.DEFAULT,
        CommentFormat.NONE,
        null);
  }

  // Filename constructors

  /**
   * Create a new EntryReader starting with the specified file.
   *
   * @param filename initial file to read
   * @param entryFormat indicates how entries begin and end
   * @param commentFormat indicates the syntax of comments
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,EntryFormat,String,String)
   */
  public EntryReader(
      String filename,
      EntryFormat entryFormat,
      CommentFormat commentFormat,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(new File(filename), entryFormat, commentFormat, includeRegex);
  }

  /**
   * Create a new EntryReader starting with the specified file.
   *
   * @param filename initial file to read
   * @param entryFormat indicates how entries begin and end
   * @param lineCommentRegex regular expression that matches comments. Any text that matches {@code
   *     lineCommentRegex} is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,EntryFormat,String,String)
   * @deprecated see {@link #EntryReader(String, EntryFormat,CommentFormat,String)}
   */
  @Deprecated // 2026-01-28
  public EntryReader(
      String filename,
      EntryFormat entryFormat,
      @Nullable @Regex String lineCommentRegex,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(new File(filename), entryFormat, lineCommentRegex, includeRegex);
  }

  /**
   * Create a new EntryReader starting with the specified file.
   *
   * @param filename initial file to read
   * @param twoBlankLines if true, then entries are separated by two blank lines rather than one
   * @param lineCommentRegex regular expression that matches comments. Any text that matches {@code
   *     lineCommentRegex} is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,EntryFormat,String,String)
   * @deprecated use {@link #EntryReader(String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-21
  public EntryReader(
      String filename,
      boolean twoBlankLines,
      @Nullable @Regex String lineCommentRegex,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(
        filename,
        twoBlankLines ? EntryFormat.TWO_BLANK_LINES : EntryFormat.DEFAULT,
        lineCommentRegex,
        includeRegex);
  }

  /**
   * Create a new EntryReader starting with the specified file.
   *
   * @param filename initial file to read
   * @param lineCommentRegex regular expression that matches comments. Any text that matches {@code
   *     lineCommentRegex} is removed. A line that is entirely a comment is ignored.
   * @param includeRegex regular expression that matches include directives. The expression should
   *     define one group that contains the include file name.
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(File,EntryFormat,String,String)
   * @deprecated use {@link #EntryReader(String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public EntryReader(
      String filename,
      @Nullable @Regex String lineCommentRegex,
      @Nullable @Regex(1) String includeRegex)
      throws IOException {
    this(filename, EntryFormat.DEFAULT, lineCommentRegex, includeRegex);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param filename source from which to read entries
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(String,EntryFormat,String,String)
   */
  public EntryReader(String filename) throws IOException {
    this(filename, EntryFormat.DEFAULT, CommentFormat.NONE, null);
  }

  /**
   * Create an EntryReader that does not support comments or include directives.
   *
   * @param filename source from which to read entries
   * @param charsetName the character set to use
   * @throws IOException if there is a problem reading the file
   * @see #EntryReader(String,EntryFormat,String,String)
   * @deprecated use {@link #EntryReader(String,EntryFormat,String,String)}
   */
  @Deprecated // 2026-01-05
  public EntryReader(String filename, String charsetName) throws IOException {
    this(
        Files.newInputStream(Path.of(filename)),
        charsetName,
        filename,
        EntryFormat.DEFAULT,
        CommentFormat.NONE,
        null);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Getters and setters
  //

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
   * Set debugging on or off.
   *
   * @param debug true if debugging is on
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Methods
  //

  /**
   * Read a line, ignoring comments and processing includes. Returns null at end of file.
   *
   * <p>A line that is completely a comment is ignored (and not returned as a blank line).
   *
   * @return the string that was read, not including any line termination characters, or null at end
   *     of file
   */
  @Override
  public @Nullable String readLine(@GuardSatisfied EntryReader this) throws IOException {

    if (debug) {
      System.err.printf("Entering readLine(), size = %d%n", readers.size());
    }

    // If a line has been pushed back, return it instead
    if (pushbackLine != null) {
      String line = pushbackLine;
      pushbackLine = null;
      return line;
    }

    String line = getNextLine();
    if (line == null) {
      return null;
    }

    // Handles fenced code blocks.
    if (entryFormat.supportsFences) {
      if (line.stripLeading().startsWith("```")) {
        inFencedCodeBlock = !inFencedCodeBlock;
        return line;
      }
    }

    if (inFencedCodeBlock) {
      return line;
    }

    // Handles comments (single-line and multi-line)
    Pattern multilineCommentStart = commentFormat.multilineCommentStart;
    Pattern lineCommentStart = commentFormat.lineCommentStart;
    int multilineCommentStartLine = -1;
    String multilineCommentStartFile = null;

    while (line != null) {

      // Find earliest single-line comment start (if any)
      int lineCommentIndex = Integer.MAX_VALUE;
      if (lineCommentStart != null) {
        Matcher lc = lineCommentStart.matcher(line);
        if (lc.find()) {
          lineCommentIndex = lc.start();
        }
      }

      // Find earliest multi-line comment start (if any)
      int multilineStartIndex = Integer.MAX_VALUE;
      Matcher ms = null;
      if (multilineCommentStart != null) {
        if (multilineCommentStartLine == -1) {
          multilineCommentStartLine = getLineNumber();
          multilineCommentStartFile = readers.getFirst().filename;
        }

        ms = multilineCommentStart.matcher(line);
        if (ms.find()) {
          multilineStartIndex = ms.start();
        }
      }

      // If neither exists, break out of the comment loop.
      if (lineCommentIndex == Integer.MAX_VALUE && multilineStartIndex == Integer.MAX_VALUE) {
        break;
      }

      if (lineCommentIndex == multilineStartIndex) {
        throw new IOException(
            String.format(
                "Both lineCommentStart (%s) and multilineCommentStart (%s) match at index %d"
                    + " in this line: %s",
                lineCommentStart, multilineCommentStart, lineCommentIndex, line));
      }
      if (lineCommentIndex < multilineStartIndex) {
        // Single-line comment comes first.
        if (lineCommentIndex >= 0 && lineCommentIndex <= line.length()) {
          line = line.substring(0, lineCommentIndex);
        }

        if (line.length() > 0) {
          break;
        }
        line = getNextLine();
        continue;
      }

      // Multi-line comment comes first: strip one multi-line comment occurrence.
      // At this point ms must be non-null and must have had a successful find() earlier.
      assert ms != null : "@AssumeAssertion(nullness)";
      @SuppressWarnings("index:assignment") // msEnd is an index into `line`
      @IndexFor("line") int msStart = ms.start();
      @SuppressWarnings("index:assignment") // msEnd is an index into `line`
      @IndexFor("line") int msEnd = ms.end();

      String prefix = line.substring(0, msStart);
      line = line.substring(msEnd);

      @SuppressWarnings("nullness") // if `multilineCommentStart` is non-null, so is `...End`
      @NonNull Pattern multilineCommentEnd = commentFormat.multilineCommentEnd;

      while (true) {
        Matcher me = multilineCommentEnd.matcher(line);
        if (me.find()) {
          @SuppressWarnings("index:assignment") // msEnd is an index into `line`
          @IndexFor("line") int meEnd = me.end();
          line = line.substring(meEnd);
          break;
        }

        line = getNextLine();
        if (line == null) {
          throw new IOException(
              String.format(
                  "Unterminated multi-line comment opened at %s:%d (reached end of file)",
                  multilineCommentStartFile, multilineCommentStartLine));
        }
      }

      line = prefix + line;

      if (line.isEmpty()) {
        line = getNextLine();
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
        if (debug) {
          System.err.printf("Trying to include filename %s%n", filename);
        }
        if (!filename.isAbsolute()) {
          FlnReader reader = readers.getFirst();
          File currentFilename = new File(reader.filename);
          File currentParent = currentFilename.getParentFile();
          filename = new File(currentParent, filename.toString());
          if (debug) {
            System.err.printf(
                "absolute filename = %s %s %s%n", currentFilename, currentParent, filename);
          }
        }
        FlnReader reader = new FlnReader(filename.getAbsolutePath());
        readers.addFirst(reader);
        return readLine();
      }
    }

    if (debug) {
      System.err.printf("Returning [%d] '%s'%n", readers.size(), line);
    }
    return line;
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
  @SuppressWarnings("mustcall:override.return")
  @Override
  public @MustCallAlias Iterator<String> iterator(@MustCallAlias EntryReader this) {
    return this;
  }

  /**
   * Returns true if there is another line to read. Any IOExceptions are turned into errors (because
   * the definition of hasNext() in Iterator doesn't throw any exceptions).
   *
   * @return true if there is another line to read
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

    String line;
    try {
      line = readLine();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
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
      throw new UncheckedIOException(e);
    }
  }

  /** remove() is not supported. */
  @Override
  public void remove(@GuardSatisfied EntryReader this) {
    throw new UnsupportedOperationException("can't remove lines from file");
  }

  /**
   * Returns the next entry (paragraph) in the file. If no more entries are available, returns null.
   *
   * <p>Entries are separated by one or two blank lines (two, if {@link EntryFormat#twoBlankLines}
   * is true), unless the entry started with {@link EntryFormat#entryStartRegex}.
   *
   * @return the next entry (paragraph) in the file
   * @throws IOException if there is a problem reading the file
   */
  public @Nullable Entry getEntry(@GuardSatisfied EntryReader this) throws IOException {

    // Skip any preceding blank lines
    String line = readLine();
    while (line != null && line.isBlank()) {
      line = readLine();
    }
    if (line == null) {
      return null;
    }

    StringBuilder body = new StringBuilder(500);
    String filename = getFileName();
    long lineNumber = getLineNumber();

    // If first line matches entryStartRegex, this is a long entry.
    final Pattern entryStartRegex = entryFormat.entryStartRegex;
    final Pattern entryStopRegex = entryFormat.entryStopRegex;
    @Regex Matcher entryMatch;
    if (entryStartRegex == null) {
      entryMatch = null;
    } else {
      entryMatch = entryStartRegex.matcher(line);
    }
    Entry entry;
    if (entryMatch != null && entryMatch.find()) {
      assert entryStartRegex != null : "@AssumeAssertion(nullness): dependent: entryMatch != null";

      // Remove entry start text from the line.
      String replacement;
      if (entryMatch.groupCount() == 0) {
        replacement = "";
      } else {
        // There is a group, so replace the whole match by the group.
        String group1 = entryMatch.group(1);
        if (group1 == null) {
          replacement = "";
        } else {
          replacement = group1;
        }
      }
      line = entryMatch.replaceFirst(replacement);

      // Description is the first line (possibly with part of the entry start removed).
      String description = line;

      // Read until we find the termination of the entry.
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
      // put that line back.
      if ((line != null) && (entryMatch.find(0) || !filename.equals(getFileName()))) {
        putback(line);
      }

      entry = new Entry(description, body.toString(), filename, lineNumber, false);

    } else { // blank-separated entry

      String description = line;

      // Read until we find blank line(s) that separate entries.
      String blankLineFound = null;
      while ((line != null) && filename.equals(getFileName())) {
        if (line.isBlank()) {
          if (inFencedCodeBlock) {
            // Don't treat blank lines inside fenced code blocks as entry separators.
            body.append(line);
            body.append(lineSep);
            line = readLine();
            continue;
          }
          if (!entryFormat.twoBlankLines) {
            break;
          } else if (blankLineFound != null) {
            break;
          } else {
            blankLineFound = line;
            line = readLine();
            continue;
          }
        }

        // The line is not blank.

        if (blankLineFound != null) {
          body.append(blankLineFound);
          body.append(lineSep);
          blankLineFound = null;
        }

        body.append(line);
        body.append(lineSep);
        line = readLine();
      }

      // If this entry was terminated by the start of a new input file
      // put that line back.
      if ((line != null) && !filename.equals(getFileName())) {
        putback(line);
      }

      entry = new Entry(description, body.toString(), filename, lineNumber, true);
    }

    return entry;
  }

  /**
   * Reads the next line from the current reader. If EOF is encountered pop out to the next reader.
   * Returns null if there is no more input.
   *
   * @return next line from the reader, or null if there is no more input
   * @throws IOException if there is trouble with the reader
   */
  private @Nullable String getNextLine(@GuardSatisfied EntryReader this) throws IOException {

    if (readers.isEmpty()) {
      return null;
    }

    FlnReader ri1 = readers.getFirst();
    String line = ri1.readLine();
    while (line == null) {
      readers.removeFirst().close();
      if (readers.isEmpty()) {
        return null;
      }
      FlnReader ri2 = readers.peekFirst();
      line = ri2.readLine();
    }
    return line;
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

  // ///////////////////////////////////////////////////////////////////////////
  // Usage example
  //

  /**
   * Simple usage example.
   *
   * @param args command-line arguments: filename [lineCommentStartRegex [includeRegex]]
   * @throws IOException if there is a problem reading a file
   */
  public static void main(String[] args) throws IOException {

    if (args.length < 1 || args.length > 3) {
      System.err.println(
          "EntryReader sample program requires 1-3 args:"
              + " filename [lineCommentStartRegex [includeRegex]]");
      System.exit(1);
    }
    final String filename = args[0];

    final CommentFormat commentFormat;
    if (args.length >= 2) {
      String lineCommentStartRegex = args[1];
      if (!RegexUtil.isRegex(lineCommentStartRegex)) {
        System.err.println(
            "Error parsing comment regex \""
                + lineCommentStartRegex
                + "\": "
                + RegexUtil.regexError(lineCommentStartRegex));
        System.exit(1);
      }
      commentFormat = new CommentFormat(lineCommentStartRegex);
    } else {
      commentFormat = CommentFormat.NONE;
    }

    final @Regex(1) String includeRegex;
    if (args.length >= 3) {
      @SuppressWarnings("regex:assignment") // about to be checked; flow isn't properly refining?
      @Regex(1) String arg3 = args[2];
      includeRegex = arg3;
      if (!RegexUtil.isRegex(includeRegex, 1)) {
        System.err.println(
            "Error parsing include regex \""
                + includeRegex
                + "\": "
                + RegexUtil.regexError(includeRegex));
        System.exit(1);
        throw new Error("unreachable");
      }
    } else {
      includeRegex = null;
    }

    try (EntryReader reader =
        new EntryReader(filename, EntryFormat.DEFAULT, commentFormat, includeRegex)) {
      String line = reader.readLine();
      while (line != null) {
        System.out.printf("%s: %d: %s%n", reader.getFileName(), reader.getLineNumber(), line);
        line = reader.readLine();
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // Helper classes
  //

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
     * @throws IOException if there is trouble reading the file
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
     * match is found, returns {@link #firstLine}.
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

  /** A dummy Reader to be used when null is not acceptable. */
  private static class DummyReader extends Reader {

    /** The canonical DummyReader. */
    public static final DummyReader it = new DummyReader();

    /**
     * Create a new DummyReader.
     *
     * @deprecated use {@link #it}.
     */
    @Deprecated // 2022-07-25; to make private
    public DummyReader() {}

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

  /** A regular expression that never matches. */
  private static final Pattern neverMatches = Pattern.compile("\\b\\B");

  /** This class informs {@link EntryReader} where an entry begins and ends. */
  public static class EntryFormat {

    /**
     * An EntryFormat using a single blank line to separate entries, with no multi-line entries and
     * no fenced code blocks.
     */
    public static final EntryFormat DEFAULT =
        new EntryFormat((Pattern) null, (Pattern) null, false, false);

    /**
     * An EntryFormat using two blank lines to separate entries, with no multi-line entries and no
     * fenced code blocks.
     */
    public static final EntryFormat TWO_BLANK_LINES =
        new EntryFormat((Pattern) null, (Pattern) null, true, false);

    /**
     * An EntryFormat using one blank line to separate entries, with fenced code blocks but no
     * multi-line entries.
     */
    public static final EntryFormat FENCED_CODE_BLOCKS =
        new EntryFormat((Pattern) null, (Pattern) null, false, true);

    /**
     * An EntryFormat using two blank lines to separate entries, with fenced code blocks but no
     * multi-line entries.
     */
    public static final EntryFormat TWO_BLANK_LINES_AND_FENCED_CODE_BLOCKS =
        new EntryFormat((Pattern) null, (Pattern) null, true, true);

    /**
     * Regular expression that starts a long entry. If null, there are no long entries, only short
     * entries. A short entry is terminated by one or two blank lines (depending on {@link
     * #twoBlankLines}) or the end of the current file.
     *
     * <p>If the first line of an entry matches this regexp, it is a long entry. It is terminated by
     * any of:
     *
     * <ul>
     *   <li>{@link #entryStopRegex}
     *   <li>another line that matches {@code entryStartRegex} (even not following a newline), or
     *   <li>the end of the current file.
     * </ul>
     *
     * <p>If the regular expression has a capturing group, the first capturing group is retained in
     * the output; otherwise, the whole match is removed.
     */
    public final @Nullable @Regex(1) Pattern entryStartRegex;

    /**
     * See {@link #entryStartRegex}.
     *
     * @see #entryStartRegex
     */
    public final Pattern entryStopRegex;

    /** If true, then entries are separated by two blank lines rather than one. */
    public final boolean twoBlankLines;

    /**
     * If true, then fenced code blocks are respected. The special "````" fence is not supported.
     */
    public final boolean supportsFences;

    /**
     * Creates an EntryFormat.
     *
     * @param entryStartRegex regular expression that starts a long entry; see {@link
     *     #entryStartRegex}
     * @param entryStopRegex regular expression that ends a long entry; see {@link #entryStartRegex}
     * @param twoBlankLines if true, then entries are separated by two blank lines rather than one
     * @param supportsFences if true, then fenced code blocks are respected
     */
    public EntryFormat(
        @Nullable @Regex(1) String entryStartRegex,
        @Nullable @Regex String entryStopRegex,
        boolean twoBlankLines,
        boolean supportsFences) {
      this(
          entryStartRegex == null ? null : Pattern.compile(entryStartRegex),
          entryStopRegex == null ? null : Pattern.compile(entryStopRegex),
          twoBlankLines,
          supportsFences);
    }

    /**
     * Creates an EntryFormat.
     *
     * @param entryStartRegex regular expression that starts a long entry; see {@link
     *     #entryStartRegex}
     * @param entryStopRegex regular expression that ends a long entry; see {@link #entryStartRegex}
     * @param twoBlankLines if true, then entries are separated by two blank lines rather than one
     * @param supportsFences if true, then fenced code blocks are respected
     */
    public EntryFormat(
        @Nullable @Regex(1) Pattern entryStartRegex,
        @Nullable Pattern entryStopRegex,
        boolean twoBlankLines,
        boolean supportsFences) {
      if (entryStartRegex == null && entryStopRegex != null) {
        throw new IllegalArgumentException(
            "entryStartRegex is null but entryStopRegex = \"" + entryStopRegex + "\"");
      }
      this.entryStartRegex = entryStartRegex;
      this.entryStopRegex = entryStopRegex == null ? neverMatches : entryStopRegex;
      this.twoBlankLines = twoBlankLines;
      this.supportsFences = supportsFences;
    }
  }

  /**
   * This class informs {@link EntryReader} where a comment begins and ends.
   *
   * <p>No quoting is supported. That is, {@code EntryReader} does not attempt to infer whether a
   * comment regex matches within (say) a string in the input text. To prevent a comment marker from
   * being matched, embed it in a fenced code block.
   */
  public static class CommentFormat {

    /** A CommentFormat that supports no comments. */
    public static final CommentFormat NONE = new CommentFormat(null);

    /** A CommentFormat for C-style comments. */
    public static final CommentFormat C = new CommentFormat("//.*", "/\\*", "\\*/");

    /** A CommentFormat for C-style comments, only at the beginning of a line. */
    public static final CommentFormat C_AT_START_OF_LINE =
        new CommentFormat("^//.*", "^/\\*", "^\\*/");

    /** A CommentFormat for HTML-style comments. */
    public static final CommentFormat HTML = new CommentFormat(null, "<!--", "-->");

    /** A CommentFormat for HTML-style comments, only at the beginning of a line. */
    public static final CommentFormat HTML_AT_START_OF_LINE =
        new CommentFormat(null, "^<!--", "^-->");

    /** A CommentFormat for Shell/Python-style comments. */
    public static final CommentFormat SHELL = new CommentFormat("#.*");

    /** A CommentFormat for Shell/Python-style comments, only at the beginning of a line. */
    public static final CommentFormat SHELL_AT_START_OF_LINE = new CommentFormat("^#.*");

    /** A CommentFormat for TeX/LaTeX-style comments. */
    public static final CommentFormat TEX = new CommentFormat("%.*");

    /** A CommentFormat for TeX/LaTeX-style comments, only at the beginning of a line. */
    public static final CommentFormat TEX_AT_START_OF_LINE = new CommentFormat("^%.*");

    /** Regular expression that matches the start of a single-line comment. */
    private final @Nullable Pattern lineCommentStart;

    /** Regular expression that matches the start of a multi-line comment. */
    private final @Nullable Pattern multilineCommentStart;

    /** Regular expression that matches the end of a multi-line comment. */
    private final @Nullable Pattern multilineCommentEnd;

    /**
     * Creates a CommentFormat.
     *
     * @param lineCommentStart regular expression that matches a single-line comment
     * @param multilineCommentStart regular expression that matches the start of a multi-line
     *     comment
     * @param multilineCommentEnd regular expression that matches the end of a multi-line comment
     */
    public CommentFormat(
        @Nullable @Regex String lineCommentStart,
        @Nullable @Regex String multilineCommentStart,
        @Nullable @Regex String multilineCommentEnd) {
      this(
          lineCommentStart == null ? null : Pattern.compile(lineCommentStart),
          multilineCommentStart == null ? null : Pattern.compile(multilineCommentStart),
          multilineCommentEnd == null ? null : Pattern.compile(multilineCommentEnd));
    }

    /**
     * Creates a CommentFormat that does not match multi-line comments.
     *
     * @param lineCommentStart regular expression that matches a single-line comment
     */
    public CommentFormat(@Nullable @Regex String lineCommentStart) {
      this(lineCommentStart == null ? null : Pattern.compile(lineCommentStart), null, null);
    }

    /**
     * Creates a CommentFormat.
     *
     * @param lineCommentStart regular expression that matches a single-line comment
     * @param multilineCommentStart regular expression that matches the start of a multi-line
     *     comment
     * @param multilineCommentEnd regular expression that matches the end of a multi-line comment
     */
    public CommentFormat(
        @Nullable Pattern lineCommentStart,
        @Nullable Pattern multilineCommentStart,
        @Nullable Pattern multilineCommentEnd) {
      if ((multilineCommentStart == null) != (multilineCommentEnd == null)) {
        throw new IllegalArgumentException(
            "multilineCommentStart and multilineCommentEnd must both be null or both be non-null");
      }
      this.lineCommentStart = lineCommentStart;
      this.multilineCommentStart = multilineCommentStart;
      this.multilineCommentEnd = multilineCommentEnd;
    }
  }
}
