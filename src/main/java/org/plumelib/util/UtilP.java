// If you edit this file, you must also edit its tests.

package org.plumelib.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.checkerframework.checker.signedness.qual.PolySigned;
import org.checkerframework.dataflow.qual.Pure;

/**
 * Utility methods that do not belong elsewhere in the plume package: BitSet; hashing;
 * ProcessBuilder; properties; Throwable.
 */
public final class UtilP {

  /** This class is a collection of methods; it does not represent anything. */
  private UtilP() {
    throw new Error("do not instantiate");
  }

  // //////////////////////////////////////////////////////////////////////
  // Object
  //

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

  /**
   * Returns the first argument if it is non-null, otherwise returns the second argument. Throws an
   * exception if both arguments are null.
   *
   * @param <T> the type of the arguments
   * @param first a reference
   * @param second a reference
   * @return the first argument that is non-null
   */
  public static <T> T firstNonNull(T first, T second) {
    if (first != null) {
      return first;
    } else if (second != null) {
      return second;
    } else {
      throw new IllegalArgumentException();
    }
  }

  // //////////////////////////////////////////////////////////////////////
  // Hashing
  //

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

  // //////////////////////////////////////////////////////////////////////
  // ProcessBuilder
  //

  /**
   * Execute the given command, and return all its output as a string.
   *
   * @param command a command to execute on the command line
   * @return all the output of the command
   */
  public static String backticks(String... command) {
    return backticks(null, Arrays.asList(command));
  }

  /**
   * Execute the given command in the given directory, and return all its output as a string.
   *
   * @param dir the directory in which to execute the command
   * @param command a command to execute on the command line
   * @return all the output of the command
   */
  public static String backticks(File dir, String... command) {
    return backticks(dir, Arrays.asList(command));
  }

  /**
   * Execute the given command, and return all its output as a string.
   *
   * @param command a command to execute on the command line, as a list of strings (the command,
   *     then its arguments)
   * @return all the output of the command
   */
  public static String backticks(List<String> command) {
    return backticks(null, command);
  }

  /**
   * Execute the given command in the given directory, and return all its output as a string.
   *
   * @param dir the directory in which to execute the command
   * @param command a command to execute on the command line, as a list of strings (the command,
   *     then its arguments)
   * @return all the output of the command
   */
  public static String backticks(@Nullable File dir, List<String> command) {
    ProcessBuilder pb = new ProcessBuilder(command);
    if (dir != null) {
      pb.directory(dir);
    }
    pb.redirectErrorStream(true);
    // TimeLimitProcess p = new TimeLimitProcess(pb.start(), TIMEOUT_SEC * 1000);
    try {
      Process p = pb.start();
      String output = FilesP.streamString(p.getInputStream());
      return output;
    } catch (IOException e) {
      return "IOException: " + e.getMessage();
    }
  }

  // //////////////////////////////////////////////////////////////////////
  // Properties
  //

  /**
   * Returns true if a property has a string value that represents true: "true", "yes", or "1". Errs
   * if the property is set to a value that is not one of "true", "false", "yes", "no", "1", or "0".
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
    return switch (pvalue.toLowerCase(Locale.getDefault())) {
      case "true", "yes", "1" -> true;
      case "false", "no", "0" -> false;
      default ->
          throw new Error(
              String.format(
                  "Property %s is set to \"%s\" which is not a boolean value", key, pvalue));
    };
  }

  /**
   * Returns true if a property has a string value that represents true: "true", "yes", or "1". Errs
   * if the property is set to a value that is not one of "true", "false", "yes", "no", "1", or "0".
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

  // //////////////////////////////////////////////////////////////////////
  // Throwable
  //

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
