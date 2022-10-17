package org.plumelib.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.checkerframework.checker.lock.qual.GuardedByUnknown;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Just like {@code FileWriter}, but adds a {@link #getFileName()} method and overrides {@code
 * #toString()} to give the file name.
 */
public final class FileWriterWithName extends FileWriter {

  /** The file being written by this. */
  private final String fileName;

  /**
   * Constructs a FileWriterWithName object given a file name.
   *
   * @param fileName the system-dependent filename
   * @throws IOException if the named file exists but is a directory rather than a regular file,
   *     does not exist but cannot be created, or cannot be opened for any other reason
   */
  public FileWriterWithName(String fileName) throws IOException {
    super(fileName);
    this.fileName = fileName;
  }

  /**
   * Constructs a FileWriterWithName object given a file name with a boolean indicating whether or
   * not to append the data written.
   *
   * @param fileName the system-dependent filename
   * @param append boolean if {@code true}, then data will be written to the end of the file rather
   *     than the beginning.
   * @throws IOException if the named file exists but is a directory rather than a regular file,
   *     does not exist but cannot be created, or cannot be opened for any other reason
   */
  public FileWriterWithName(String fileName, boolean append) throws IOException {
    super(fileName, append);
    this.fileName = fileName;
  }

  /**
   * Constructs a FileWriterWithName object given a File object.
   *
   * @param file a File object to write to
   * @throws IOException if the file exists but is a directory rather than a regular file, does not
   *     exist but cannot be created, or cannot be opened for any other reason
   */
  public FileWriterWithName(File file) throws IOException {
    super(file);
    this.fileName = file.getAbsolutePath();
  }

  /**
   * Constructs a FileWriterWithName object given a File object. If the second argument is {@code
   * true}, then bytes will be written to the end of the file rather than the beginning.
   *
   * @param file a File object to write to
   * @param append if {@code true}, then bytes will be written to the end of the file rather than
   *     the beginning
   * @throws IOException if the file exists but is a directory rather than a regular file, does not
   *     exist but cannot be created, or cannot be opened for any other reason
   * @since 1.4
   */
  public FileWriterWithName(File file, boolean append) throws IOException {
    super(file, append);
    this.fileName = file.getAbsolutePath();
  }

  /**
   * Returns the name of the file being written by this.
   *
   * @return the name of the file being written by this
   */
  @SuppressWarnings(
      "lock:lock.not.held") // Lock Checker bug? fileName is final, no lock is needed to access it
  @Pure
  public String getFileName(@GuardedByUnknown FileWriterWithName this) {
    return fileName;
  }

  /**
   * Returns the name of the file being written by this.
   *
   * @return the name of the file being written by this
   */
  @Pure
  @Override
  public String toString(@GuardedByUnknown FileWriterWithName this) {
    return getFileName();
  }
}
