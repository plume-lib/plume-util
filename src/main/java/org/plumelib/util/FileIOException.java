package org.plumelib.util;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.SideEffectFree;

// TODO:  A better name would be LineNumberException.
// And then it needn't really extend IOException.

// TODO:  Maybe the constructors should take a Reader and check at run time
// whether it's a LineNumberReader.  Easier for clients, but easier to
// forget to provide the right type of Reader, too.

/**
 * This class extends IOException by also reporting a file name and line number at which the
 * exception occurred. It requires use of a {@link LineNumberReader}.
 */
public class FileIOException extends IOException {
  /** Unique identifier for serialization. If you add or remove fields, change this number. */
  static final long serialVersionUID = 20050923L;

  /** The file being read when the IOException occurred. */
  public final @Nullable String fileName;
  /** The line being read when the IOException occurred. */
  public final int lineNumber;

  ///
  /// Empty constructor
  ///

  /** Create a dummy FileIOException. */
  public FileIOException() {
    super();
    fileName = null;
    lineNumber = -1;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Without a message (with a Throwable instead)
  ///

  // If cause is null, the super call throws a null pointer exception.
  // This looks like a JDK bug.  -Plume 12/9/2008
  /**
   * Create a FileIOException.
   *
   * @param cause the exception that occurred
   */
  public FileIOException(@Nullable Throwable cause) {
    // The "super(Throwable)" constructor exists in Java 6 and later.
    // For backward compatibility, use the initCause method instead.
    initCause(cause);
    fileName = null;
    lineNumber = -1;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Without a Reader
  ///

  /**
   * Create a FileIOException with no known file name or line number (which is kind of pointless;
   * you might as well just have a regular exception).
   *
   * @param message the detail message
   */
  public FileIOException(@Nullable String message) {
    super(message);
    fileName = null;
    lineNumber = -1;
  }

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param cause the exception that occurred
   */
  public FileIOException(@Nullable String message, @Nullable Throwable cause) {
    // The "super(String, Throwable) constructor exists in Java 6 and later.
    // For backward compatibility, use the initCause method instead.
    super(message);
    initCause(cause);
    fileName = null;
    lineNumber = -1;
  }

  // Design choice:  require filename and linenumber, don't support
  // interface with just one or the other.

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param fileName the name of the file being read
   * @param lineNumber the line number to which the file has been read
   */
  public FileIOException(@Nullable String message, @Nullable String fileName, int lineNumber) {
    super(message);
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param cause the exception that occurred
   * @param fileName the name of the file being read
   * @param lineNumber the line number to which the file has been read
   */
  public FileIOException(
      @Nullable String message,
      @Nullable Throwable cause,
      @Nullable String fileName,
      int lineNumber) {
    // The "super(String, Throwable) constructor exists in Java 6 and later.
    // For backward compatibility, use the initCause method instead.
    super(message);
    initCause(cause);
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Without a filename or File
  ///

  // I cannot infer the filename from the reader, because LineNumberReader
  // gives no access to the underlying stream.

  /**
   * Create a FileIOException.
   *
   * @param reader the reader for the file being read (used for the line number only; no file name
   *     is known)
   * @param cause the exception that occurred
   */
  public FileIOException(@Nullable LineNumberReader reader, @Nullable Throwable cause) {
    this(reader, /*fileName=*/ (@Nullable String) null, cause);
  }

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param reader indicates the line number at which the exception occurred; there is no known file
   *     name
   */
  public FileIOException(@Nullable String message, @Nullable LineNumberReader reader) {
    this(message, reader, /*fileName=*/ (@Nullable String) null);
  }

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param reader the reader for the file being read
   * @param cause the exception that occurred
   */
  public FileIOException(
      @Nullable String message, @Nullable LineNumberReader reader, @Nullable Throwable cause) {
    this(message, reader, /*fileName=*/ (@Nullable String) null, cause);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// With a filename
  ///

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param reader the reader for the file being read (used for the line number only)
   * @param fileName the name of the file being read
   */
  public FileIOException(
      @Nullable String message, @Nullable LineNumberReader reader, @Nullable String fileName) {
    super(message);
    this.fileName = fileName;
    this.lineNumber = getLineNumber(reader);
  }

  /**
   * Create a FileIOException.
   *
   * @param reader the reader for the file being read (used for the line number only)
   * @param fileName the name of the file being read
   * @param cause the exception that occurred
   */
  public FileIOException(
      @Nullable LineNumberReader reader, @Nullable String fileName, @Nullable Throwable cause) {
    // The "super(Throwable) constructor exists in Java 6 and later.
    // For backward compatibility, use the initCause method instead.
    initCause(cause);
    this.fileName = fileName;
    this.lineNumber = getLineNumber(reader);
  }

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param reader the reader for the file being read (used for the line number only)
   * @param fileName the name of the file being read
   * @param cause the exception that occurred
   */
  public FileIOException(
      @Nullable String message,
      @Nullable LineNumberReader reader,
      @Nullable String fileName,
      @Nullable Throwable cause) {
    // The "super(String, Throwable) constructor exists in Java 6 and later.
    // For backward compatibility, use the initCause method instead.
    super(message);
    initCause(cause);
    this.fileName = fileName;
    this.lineNumber = getLineNumber(reader);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// With a File
  ///

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param reader the reader for the file being read (used for the line number only)
   * @param file the file being read (used for its name only)
   */
  public FileIOException(@Nullable String message, @Nullable LineNumberReader reader, File file) {
    this(message, reader, file.getName());
  }

  /**
   * Create a FileIOException.
   *
   * @param message the detail message for the exception
   * @param reader the reader for the file being read (used for the line number only)
   * @param file the file being read (used for its name only)
   * @param cause the exception that occurred
   */
  public FileIOException(
      @Nullable String message,
      @Nullable LineNumberReader reader,
      File file,
      @Nullable Throwable cause) {
    this(message, reader, file.getName(), cause);
  }

  /**
   * Create a FileIOException.
   *
   * @param reader the reader for the file being read (used for the line number only)
   * @param file the file being read (used for its name only)
   * @param cause the exception that occurred
   */
  public FileIOException(@Nullable LineNumberReader reader, File file, @Nullable Throwable cause) {
    // The "super(Throwable) constructor exists in Java 6 and later.
    // For backward compatibility, use the initCause method instead.
    initCause(cause);
    this.fileName = file.getName();
    this.lineNumber = getLineNumber(reader);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Utility and helper methods
  ///

  @SuppressWarnings("lock:override.sideeffect") // temporary until after CF 3.0.1
  @SideEffectFree
  @Override
  public String getMessage(@GuardSatisfied FileIOException this) {
    String result = super.getMessage();
    if (result == null) {
      result = this.getClass().getName();
    }
    if (fileName != null) {
      result += " in file " + fileName;
    }
    if (lineNumber != -1) {
      result += " at line " + lineNumber;
    }
    return result;
  }

  // There is no setter method because field lineNumber is final.

  /**
   * Infers the line number from the given {@code reader}. Returns -1 if {@code reader} is null.
   *
   * @param reader the LineNumberReader whose line to return, or null
   * @return the line number of {@code reader}, or -1 if {@code reader} is null
   */
  private static int getLineNumber(@Nullable LineNumberReader reader) {
    if (reader != null) {
      return reader.getLineNumber();
    } else {
      return -1;
    }
  }
}
