// If you edit this file, you must also edit its tests.
// For tests of this and the entire plume package, see class TestPlume.

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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*>>>
import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.lock.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.regex.qual.*;
import org.checkerframework.checker.signature.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;
*/

/** Utility functions that do not belong elsewhere in the plume package. */
public final class UtilPlume {

  /** This class is a collection of methods; it does not represent anything. */
  private UtilPlume() {
    throw new Error("do not instantiate");
  }

  private static final String lineSep = System.getProperty("line.separator");

  ///////////////////////////////////////////////////////////////////////////
  /// Array
  ///

  // For arrays, see ArraysPlume.java.

  ///////////////////////////////////////////////////////////////////////////
  /// BitSet
  ///

  /**
   * Returns true if the cardinality of the intersection of the two BitSets is at least the given
   * value.
   *
   * @param a the first BitSet to intersect
   * @param b the second BitSet to intersect
   * @param i the cardinality bound
   * @return true iff size(a intersect b) &ge; i
   */
  @SuppressWarnings({"purity", "lock"}) // side effect to local state (BitSet)
  /*@Pure*/
  public static boolean intersectionCardinalityAtLeast(BitSet a, BitSet b, /*@NonNegative*/ int i) {
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
   */
  @SuppressWarnings({"purity", "lock"}) // side effect to local state (BitSet)
  /*@Pure*/
  public static boolean intersectionCardinalityAtLeast(
      BitSet a, BitSet b, BitSet c, /*@NonNegative*/ int i) {
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
   */
  @SuppressWarnings({"purity", "lock"}) // side effect to local state (BitSet)
  /*@Pure*/
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
   */
  @SuppressWarnings({"purity", "lock"}) // side effect to local state (BitSet)
  /*@Pure*/
  public static int intersectionCardinality(BitSet a, BitSet b, BitSet c) {
    BitSet intersection = (BitSet) a.clone();
    intersection.and(b);
    intersection.and(c);
    return intersection.cardinality();
  }

  ///////////////////////////////////////////////////////////////////////////
  /// BufferedFileReader
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
   * @param file the possibly-compressed file to read
   * @return an InputStream for file
   * @throws IOException if there is trouble reading the file
   */
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
   */
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
   * @param file the possibly-compressed file to read
   * @return an InputStreamReader for file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
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
   */
  public static InputStreamReader fileReader(File file, /*@Nullable*/ String charsetName)
      throws FileNotFoundException, IOException {
    InputStream in = new FileInputStream(file);
    InputStreamReader file_reader;
    if (charsetName == null) {
      file_reader = new InputStreamReader(in, UTF_8);
    } else {
      file_reader = new InputStreamReader(in, charsetName);
    }
    return file_reader;
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
   */
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
   */
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
   */
  public static BufferedReader bufferedFileReader(String filename, /*@Nullable*/ String charsetName)
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
   */
  public static BufferedReader bufferedFileReader(File file, /*@Nullable*/ String charsetName)
      throws FileNotFoundException, IOException {
    Reader file_reader = fileReader(file, charsetName);
    return new BufferedReader(file_reader);
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
   */
  public static LineNumberReader lineNumberFileReader(File file)
      throws FileNotFoundException, IOException {
    Reader file_reader;
    if (file.getName().endsWith(".gz")) {
      try {
        file_reader =
            new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "ISO-8859-1");
      } catch (IOException e) {
        throw new IOException("Problem while reading " + file, e);
      }
    } else {
      file_reader = new InputStreamReader(new FileInputStream(file), "ISO-8859-1");
    }
    return new LineNumberReader(file_reader);
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
   */
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
   */
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
   */
  public static BufferedOutputStream bufferedFileOutputStream(String filename, boolean append)
      throws IOException {
    OutputStream os = new FileOutputStream(filename, append);
    if (filename.endsWith(".gz")) {
      os = new GZIPOutputStream(os);
    }
    return new BufferedOutputStream(os);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Class
  ///

  /**
   * Return true iff sub is a subtype of sup. If sub == sup, then sub is considered a subtype of sub
   * and this method returns true.
   *
   * @param sub class to test for being a subtype
   * @param sup class to test for being a supertype
   * @return true iff sub is a subtype of sup
   */
  /*@Pure*/
  public static boolean isSubtype(Class<?> sub, Class<?> sup) {
    if (sub == sup) {
      return true;
    }

    // Handle superclasses
    Class<?> parent = sub.getSuperclass();
    // If parent == null, sub == Object
    if ((parent != null) && (parent == sup || isSubtype(parent, sup))) {
      return true;
    }

    // Handle interfaces
    for (Class<?> ifc : sub.getInterfaces()) {
      if (ifc == sup || isSubtype(ifc, sup)) {
        return true;
      }
    }

    return false;
  }

  /** Used by {@link #classForName}. */
  private static HashMap<String, Class<?>> primitiveClasses = new HashMap<String, Class<?>>(8);

  static {
    primitiveClasses.put("boolean", Boolean.TYPE);
    primitiveClasses.put("byte", Byte.TYPE);
    primitiveClasses.put("char", Character.TYPE);
    primitiveClasses.put("double", Double.TYPE);
    primitiveClasses.put("float", Float.TYPE);
    primitiveClasses.put("int", Integer.TYPE);
    primitiveClasses.put("long", Long.TYPE);
    primitiveClasses.put("short", Short.TYPE);
  }

  // TODO: should create a method that handles any ClassGetName (including
  // primitives), but not fully-qualified names.
  /**
   * Like {@link Class#forName(String)}, but also works when the string represents a primitive type
   * or a fully-qualified name (as opposed to a binary name).
   *
   * <p>If the given name can't be found, this method changes the last '.' to a dollar sign ($) and
   * tries again. This accounts for inner classes that are incorrectly passed in in fully-qualified
   * format instead of binary format. (It should try multiple dollar signs, not just at the last
   * position.)
   *
   * <p>Recall the rather odd specification for {@link Class#forName(String)}: the argument is a
   * binary name for non-arrays, but a field descriptor for arrays. This method uses the same rules,
   * but additionally handles primitive types and, for non-arrays, fully-qualified names.
   *
   * @param className name of the class
   * @return the Class corresponding to className
   * @throws ClassNotFoundException if the class is not found
   */
  // The annotation encourages proper use, even though this can take a
  // fully-qualified name (only for a non-array).
  public static Class<?> classForName(
      /*@ClassGetName*/ String className) throws ClassNotFoundException {
    Class<?> result = primitiveClasses.get(className);
    if (result != null) {
      return result;
    } else {
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e) {
        int pos = className.lastIndexOf('.');
        if (pos < 0) {
          throw e;
        }
        @SuppressWarnings("signature") // checked below & exception is handled
        /*@ClassGetName*/ String inner_name =
            className.substring(0, pos) + "$" + className.substring(pos + 1);
        try {
          return Class.forName(inner_name);
        } catch (ClassNotFoundException ee) {
          throw e;
        }
      }
    }
  }

  @Deprecated
  private static HashMap</*@SourceNameForNonArrayNonInner*/ String, /*@FieldDescriptor*/ String>
      primitiveClassesJvm =
          new HashMap</*@SourceNameForNonArrayNonInner*/ String, /*@FieldDescriptor*/ String>(8);

  static {
    primitiveClassesJvm.put("boolean", "Z");
    primitiveClassesJvm.put("byte", "B");
    primitiveClassesJvm.put("char", "C");
    primitiveClassesJvm.put("double", "D");
    primitiveClassesJvm.put("float", "F");
    primitiveClassesJvm.put("int", "I");
    primitiveClassesJvm.put("long", "J");
    primitiveClassesJvm.put("short", "S");
  }

  /**
   * Convert a binary name to a field descriptor. For example, convert "java.lang.Object[]" to
   * "[Ljava/lang/Object;" or "int" to "I".
   *
   * @param classname name of the class, in binary class name format
   * @return name of the class, in field descriptor format
   * @deprecated use version in org.plumelib.bcelutil instead
   */
  @Deprecated
  @SuppressWarnings("signature") // conversion routine
  public static /*@FieldDescriptor*/ String binaryNameToFieldDescriptor(
      /*@BinaryName*/ String classname) {
    int dims = 0;
    String sans_array = classname;
    while (sans_array.endsWith("[]")) {
      dims++;
      sans_array = sans_array.substring(0, sans_array.length() - 2);
    }
    String result = primitiveClassesJvm.get(sans_array);
    if (result == null) {
      result = "L" + sans_array + ";";
    }
    for (int i = 0; i < dims; i++) {
      result = "[" + result;
    }
    return result.replace('.', '/');
  }

  /**
   * Convert a primitive java type name (e.g., "int", "double", etc.) to a field descriptor (e.g.,
   * "I", "D", etc.).
   *
   * @param primitive_name name of the type, in Java format
   * @return name of the type, in field descriptor format
   * @throws IllegalArgumentException if primitive_name is not a valid primitive type name
   * @deprecated use version in org.plumelib.bcelutil instead
   */
  @Deprecated
  public static /*@FieldDescriptor*/ String primitiveTypeNameToFieldDescriptor(
      String primitive_name) {
    String result = primitiveClassesJvm.get(primitive_name);
    if (result == null) {
      throw new IllegalArgumentException("Not the name of a primitive type: " + primitive_name);
    }
    return result;
  }

  /**
   * Convert from a BinaryName to the format of {@link Class#getName()}.
   *
   * @param bn the binary name to convert
   * @return the class name, in Class.getName format
   * @deprecated use version in org.plumelib.bcelutil instead
   */
  @Deprecated
  @SuppressWarnings("signature") // conversion routine
  public static /*@ClassGetName*/ String binaryNameToClassGetName(/*BinaryName*/ String bn) {
    if (bn.endsWith("[]")) {
      return binaryNameToFieldDescriptor(bn).replace('/', '.');
    } else {
      return bn;
    }
  }

  /**
   * Convert from a FieldDescriptor to the format of {@link Class#getName()}.
   *
   * @param fd the class, in field descriptor format
   * @return the class name, in Class.getName format
   * @deprecated use version in org.plumelib.bcelutil instead
   */
  @Deprecated
  @SuppressWarnings("signature") // conversion routine
  public static /*@ClassGetName*/ String fieldDescriptorToClassGetName(
      /*FieldDescriptor*/ String fd) {
    if (fd.startsWith("[")) {
      return fd.replace('/', '.');
    } else {
      return fieldDescriptorToBinaryName(fd);
    }
  }

  /**
   * Convert a fully-qualified argument list from Java format to JVML format. For example, convert
   * "(java.lang.Integer[], int, java.lang.Integer[][])" to
   * "([Ljava/lang/Integer;I[[Ljava/lang/Integer;)".
   *
   * @param arglist an argument list, in Java format
   * @return argument list, in JVML format
   * @deprecated use version in org.plumelib.bcelutil instead
   */
  @Deprecated
  public static String arglistToJvm(String arglist) {
    if (!(arglist.startsWith("(") && arglist.endsWith(")"))) {
      throw new Error("Malformed arglist: " + arglist);
    }
    String result = "(";
    String comma_sep_args = arglist.substring(1, arglist.length() - 1);
    StringTokenizer args_tokenizer = new StringTokenizer(comma_sep_args, ",", false);
    while (args_tokenizer.hasMoreTokens()) {
      @SuppressWarnings("signature") // substring
      /*@BinaryName*/ String arg = args_tokenizer.nextToken().trim();
      result += binaryNameToFieldDescriptor(arg);
    }
    result += ")";
    // System.out.println("arglistToJvm: " + arglist + " => " + result);
    return result;
  }

  @Deprecated
  private static HashMap<String, String> primitiveClassesFromJvm = new HashMap<String, String>(8);

  static {
    primitiveClassesFromJvm.put("Z", "boolean");
    primitiveClassesFromJvm.put("B", "byte");
    primitiveClassesFromJvm.put("C", "char");
    primitiveClassesFromJvm.put("D", "double");
    primitiveClassesFromJvm.put("F", "float");
    primitiveClassesFromJvm.put("I", "int");
    primitiveClassesFromJvm.put("J", "long");
    primitiveClassesFromJvm.put("S", "short");
  }

  // does not convert "V" to "void".  Should it?
  /**
   * Convert a field descriptor to a binary name. For example, convert "[Ljava/lang/Object;" to
   * "java.lang.Object[]" or "I" to "int".
   *
   * @param classname name of the type, in JVML format
   * @return name of the type, in Java format
   * @deprecated use version in org.plumelib.bcelutil instead
   */
  @Deprecated
  @SuppressWarnings("signature") // conversion routine
  public static /*@BinaryName*/ String fieldDescriptorToBinaryName(String classname) {
    if (classname.equals("")) {
      throw new Error("Empty string passed to fieldDescriptorToBinaryName");
    }
    int dims = 0;
    while (classname.startsWith("[")) {
      dims++;
      classname = classname.substring(1);
    }
    String result;
    if (classname.startsWith("L") && classname.endsWith(";")) {
      result = classname.substring(1, classname.length() - 1);
    } else {
      result = primitiveClassesFromJvm.get(classname);
      if (result == null) {
        throw new Error("Malformed base class: " + classname);
      }
    }
    for (int i = 0; i < dims; i++) {
      result += "[]";
    }
    return result.replace('/', '.');
  }

  /**
   * Convert an argument list from JVML format to Java format. For example, convert
   * "([Ljava/lang/Integer;I[[Ljava/lang/Integer;)" to "(java.lang.Integer[], int,
   * java.lang.Integer[][])".
   *
   * @param arglist an argument list, in JVML format
   * @return argument list, in Java format
   * @deprecated use version in org.plumelib.bcelutil instead
   */
  @Deprecated
  public static String arglistFromJvm(String arglist) {
    if (!(arglist.startsWith("(") && arglist.endsWith(")"))) {
      throw new Error("Malformed arglist: " + arglist);
    }
    String result = "(";
    /*@Positive*/ int pos = 1;
    while (pos < arglist.length() - 1) {
      if (pos > 1) {
        result += ", ";
      }
      int nonarray_pos = pos;
      while (arglist.charAt(nonarray_pos) == '[') {
        nonarray_pos++;
        if (nonarray_pos >= arglist.length()) {
          throw new Error("Malformed arglist: " + arglist);
        }
      }
      char c = arglist.charAt(nonarray_pos);
      if (c == 'L') {
        int semi_pos = arglist.indexOf(';', nonarray_pos);
        if (semi_pos == -1) {
          throw new Error("Malformed arglist: " + arglist);
        }
        String fieldDescriptor = arglist.substring(pos, semi_pos + 1);
        result += fieldDescriptorToBinaryName(fieldDescriptor);
        pos = semi_pos + 1;
      } else {
        String maybe = fieldDescriptorToBinaryName(arglist.substring(pos, nonarray_pos + 1));
        if (maybe == null) {
          // return null;
          throw new Error("Malformed arglist: " + arglist);
        }
        result += maybe;
        pos = nonarray_pos + 1;
      }
    }
    return result + ")";
  }

  ///////////////////////////////////////////////////////////////////////////
  /// ClassLoader
  ///

  /**
   * This static nested class has no purpose but to define defineClassFromFile.
   * ClassLoader.defineClass is protected, so I subclass ClassLoader in order to call defineClass.
   */
  private static class PromiscuousLoader extends ClassLoader {
    /**
     * Converts the bytes in a file into an instance of class Class, and also resolves (links) the
     * class. Delegates the real work to defineClass.
     *
     * @see ClassLoader#defineClass(String,byte[],int,int)
     * @param className the expected binary name of the class to define, or null if not known
     * @param pathname the file from which to load the class
     * @return the {@code Class} object that was created
     */
    public Class<?> defineClassFromFile(
        /*@BinaryName*/ String className, String pathname)
        throws FileNotFoundException, IOException {
      FileInputStream fi = new FileInputStream(pathname);
      int numbytes = fi.available();
      byte[] classBytes = new byte[numbytes];
      int bytesRead = fi.read(classBytes);
      fi.close();
      if (bytesRead < numbytes) {
        throw new Error(
            String.format(
                "Expected to read %d bytes from %s, got %d", numbytes, pathname, bytesRead));
      }
      Class<?> return_class = defineClass(className, classBytes, 0, numbytes);
      resolveClass(return_class); // link the class
      return return_class;
    }
  }

  private static PromiscuousLoader thePromiscuousLoader = new PromiscuousLoader();

  /**
   * Converts the bytes in a file into an instance of class Class, and resolves (links) the class.
   * Like {@link ClassLoader#defineClass(String,byte[],int,int)}, but takes a file name rather than
   * an array of bytes as an argument, and also resolves (links) the class.
   *
   * @see ClassLoader#defineClass(String,byte[],int,int)
   * @param className the name of the class to define, or null if not known
   * @param pathname the pathname of a .class file
   * @return a Java Object corresponding to the Class defined in the .class file
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if there is trouble reading the file
   */
  // Also throws UnsupportedClassVersionError and some other exceptions.
  public static Class<?> defineClassFromFile(
      /*@BinaryName*/ String className, String pathname) throws FileNotFoundException, IOException {
    return thePromiscuousLoader.defineClassFromFile(className, pathname);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Classpath
  ///

  // Perhaps abstract out the simpler addToPath from this
  /**
   * Add the directory to the system classpath.
   *
   * @param dir directory to add to the system classpath
   */
  public static void addToClasspath(String dir) {
    // If the dir isn't on CLASSPATH, add it.
    String pathSep = System.getProperty("path.separator");
    // what is the point of the "replace()" call?
    String cp = System.getProperty("java.class.path", ".").replace('\\', '/');
    StringTokenizer tokenizer = new StringTokenizer(cp, pathSep, false);
    boolean found = false;
    while (tokenizer.hasMoreTokens() && !found) {
      found = tokenizer.nextToken().equals(dir);
    }
    if (!found) {
      System.setProperty("java.class.path", dir + pathSep + cp);
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
  public static long count_lines(String filename) throws IOException {
    long count = 0;
    try (LineNumberReader reader = UtilPlume.lineNumberFileReader(filename)) {
      while (reader.readLine() != null) {
        count++;
      }
    }
    return count;
  }

  /**
   * Return the contents of the file, as a list of strings, one per line.
   *
   * @param filename the file whose contents to return
   * @return the contents of {@code filename}, one string per line
   * @throws IOException if there was a problem reading the file
   */
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
   * Return true iff files have the same contents.
   *
   * @param file1 first file to compare
   * @param file2 second file to compare
   * @return true iff the files have the same contents
   */
  /*@Pure*/
  public static boolean equalFiles(String file1, String file2) {
    return equalFiles(file1, file2, false);
  }

  /**
   * Return true iff the files have the same contents.
   *
   * @param file1 first file to compare
   * @param file2 second file to compare
   * @param trimLines if true, call String.trim on each line before comparing
   * @return true iff the files have the same contents
   */
  @SuppressWarnings({"purity", "lock"}) // reads files, side effects local state
  /*@Pure*/
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
        if (!(line1.equals(line2))) {
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

  /**
   * A FilenameFilter that accepts files whose name matches the given wildcard. The wildcard must
   * contain exactly one "*".
   */
  public static final class WildcardFilter implements FilenameFilter {
    String prefix;
    String suffix;

    public WildcardFilter(String filename) {
      int astloc = filename.indexOf('*');
      if (astloc == -1) {
        throw new Error("No asterisk in wildcard argument: " + filename);
      }
      prefix = filename.substring(0, astloc);
      suffix = filename.substring(astloc + 1);
      if (filename.indexOf('*') != -1) {
        throw new Error("Multiple asterisks in wildcard argument: " + filename);
      }
    }

    @Override
    public boolean accept(File dir, String name) {
      return name.startsWith(prefix) && name.endsWith(suffix);
    }
  }

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
    @SuppressWarnings("interning")
    boolean changed = (newname != path);
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
   * Return a string version of the filename that can be used in Java source. On Windows, the file
   * will return a backslash separated string. Since backslash is an escape character, it must be
   * quoted itself inside the string.
   *
   * <p>The current implementation presumes that backslashes don't appear in filenames except as
   * windows path separators. That seems like a reasonable assumption.
   *
   * @param name file whose name to quote
   * @return a string version of the name that can be used in Java source
   */
  public static String java_source(File name) {

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
   * Reads an Object from a File.
   *
   * @param file the file from which to read
   * @return the object read from the file
   * @throws IOException if there is trouble reading the file
   * @throws ClassNotFoundException if the object's class cannot be found
   */
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
   */
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

  // In hashing, there are two separate issues.  First, one must convert
  // the input datum into an integer.  Then, one must transform the
  // resulting integer in a pseudorandom way so as to result in a number
  // that is far separated from other values that may have been near it to
  // begin with.  Often these two steps are combined, particularly if
  // one wishes to avoid creating too large an integer (losing information
  // off the top bits).

  // http://burtleburtle.net/bob/hash/hashfaq.html says (of combined methods):
  //  * for (h=0, i=0; i<len; ++i) { h += key[i]; h += (h<<10); h ^= (h>>6); }
  //    h += (h<<3); h ^= (h>>11); h += (h<<15);
  //    is good.
  //  * for (h=0, i=0; i<len; ++i) h = tab[(h^key[i])&0xff]; may be good.
  //  * for (h=0, i=0; i<len; ++i) h = (h>>8)^tab[(key[i]+h)&0xff]; may be good.

  // In this part of the file, perhaps I will eventually write good hash
  // functions.  For now, write cheesy ones that primarily deal with the
  // first issue, transforming a data structure into a single number.  This
  // is also known as fingerprinting.

  /**
   * Return a hash of the arguments. Note that this differs from the result of {@link
   * Double#hashCode()}.
   *
   * @param x value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(double x) {
    return hash(Double.doubleToLongBits(x));
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(double a, double b) {
    double result = 17;
    result = result * 37 + a;
    result = result * 37 + b;
    return hash(result);
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @param c value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(double a, double b, double c) {
    double result = 17;
    result = result * 37 + a;
    result = result * 37 + b;
    result = result * 37 + c;
    return hash(result);
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(double /*@Nullable*/ [] a) {
    double result = 17;
    if (a != null) {
      result = result * 37 + a.length;
      for (int i = 0; i < a.length; i++) {
        result = result * 37 + a[i];
      }
    }
    return hash(result);
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(double /*@Nullable*/ [] a, double /*@Nullable*/ [] b) {
    return hash(hash(a), hash(b));
  }

  /// Don't define hash with int args; use the long versions instead.

  /**
   * Return a hash of the arguments. Note that this differs from the result of {@link
   * Long#hashCode()}. But it doesn't map -1 and 0 to the same value.
   *
   * @param l value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(long l) {
    // If possible, use the value itself.
    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
      return (int) l;
    }

    int result = 17;
    int hibits = (int) (l >> 32);
    int lobits = (int) l;
    result = result * 37 + hibits;
    result = result * 37 + lobits;
    return result;
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(long a, long b) {
    long result = 17;
    result = result * 37 + a;
    result = result * 37 + b;
    return hash(result);
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @param c value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(long a, long b, long c) {
    long result = 17;
    result = result * 37 + a;
    result = result * 37 + b;
    result = result * 37 + c;
    return hash(result);
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(long /*@Nullable*/ [] a) {
    long result = 17;
    if (a != null) {
      result = result * 37 + a.length;
      for (int i = 0; i < a.length; i++) {
        result = result * 37 + a[i];
      }
    }
    return hash(result);
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(long /*@Nullable*/ [] a, long /*@Nullable*/ [] b) {
    return hash(hash(a), hash(b));
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(/*@Nullable*/ String a) {
    return (a == null) ? 0 : a.hashCode();
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(/*@Nullable*/ String a, /*@Nullable*/ String b) {
    long result = 17;
    result = result * 37 + hash(a);
    result = result * 37 + hash(b);
    return hash(result);
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @param b value to be hashed
   * @param c value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(/*@Nullable*/ String a, /*@Nullable*/ String b, /*@Nullable*/ String c) {
    long result = 17;
    result = result * 37 + hash(a);
    result = result * 37 + hash(b);
    result = result * 37 + hash(c);
    return hash(result);
  }

  /**
   * Return a hash of the arguments.
   *
   * @param a value to be hashed
   * @return a hash of the arguments
   */
  public static int hash(/*@Nullable*/ String /*@Nullable*/ [] a) {
    long result = 17;
    if (a != null) {
      result = result * 37 + a.length;
      for (int i = 0; i < a.length; i++) {
        result = result * 37 + hash(a[i]);
      }
    }
    return hash(result);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Iterator
  ///

  /**
   * Converts an Iterator to an Iterable. The resulting Iterable can be used to produce a single,
   * working Iterator (the one that was passed in). Subsequent calls to its iterator() method will
   * fail, because otherwise they would return the same Iterator instance, which may have been
   * exhausted, or otherwise be in some indeterminate state. Calling iteratorToIterable twice on the
   * same argument can have similar problems, so don't do that.
   *
   * @param source the Iterator to be converted to Iterable
   * @param <T> the element type
   * @return source, converted to Iterable
   */
  public static <T> Iterable<T> iteratorToIterable(final Iterator<T> source) {
    if (source == null) {
      throw new NullPointerException();
    }
    return new Iterable<T>() {
      private AtomicBoolean used = new AtomicBoolean();

      @Override
      public Iterator<T> iterator() {
        if (used.getAndSet(true)) {
          throw new Error("Call iterator() just once");
        }
        return source;
      }
    };
  }

  // Making these classes into functions didn't work because I couldn't get
  // their arguments into a scope that Java was happy with.

  /** Converts an Enumeration into an Iterator. */
  public static final class EnumerationIterator<T> implements Iterator<T> {
    Enumeration<T> e;

    public EnumerationIterator(Enumeration<T> e) {
      this.e = e;
    }

    @Override
    public boolean hasNext(/*>>>@GuardSatisfied EnumerationIterator<T> this*/) {
      return e.hasMoreElements();
    }

    @Override
    public T next(/*>>>@GuardSatisfied EnumerationIterator<T> this*/) {
      return e.nextElement();
    }

    @Override
    public void remove(/*>>>@GuardSatisfied EnumerationIterator<T> this*/) {
      throw new UnsupportedOperationException();
    }
  }

  /** Converts an Iterator into an Enumeration. */
  @SuppressWarnings("JdkObsolete")
  public static final class IteratorEnumeration<T> implements Enumeration<T> {
    Iterator<T> itor;

    public IteratorEnumeration(Iterator<T> itor) {
      this.itor = itor;
    }

    @Override
    public boolean hasMoreElements() {
      return itor.hasNext();
    }

    @Override
    public T nextElement() {
      return itor.next();
    }
  }

  // This must already be implemented someplace else.  Right??
  /**
   * An Iterator that returns first the elements returned by its first argument, then the elements
   * returned by its second argument. Like {@link MergedIterator}, but specialized for the case of
   * two arguments.
   */
  public static final class MergedIterator2<T> implements Iterator<T> {
    Iterator<T> itor1, itor2;

    public MergedIterator2(Iterator<T> itor1_, Iterator<T> itor2_) {
      this.itor1 = itor1_;
      this.itor2 = itor2_;
    }

    @Override
    public boolean hasNext(/*>>>@GuardSatisfied MergedIterator2<T> this*/) {
      return (itor1.hasNext() || itor2.hasNext());
    }

    @Override
    public T next(/*>>>@GuardSatisfied MergedIterator2<T> this*/) {
      if (itor1.hasNext()) {
        return itor1.next();
      } else if (itor2.hasNext()) {
        return itor2.next();
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove(/*>>>@GuardSatisfied MergedIterator2<T> this*/) {
      throw new UnsupportedOperationException();
    }
  }

  // This must already be implemented someplace else.  Right??
  /**
   * An Iterator that returns the elements in each of its argument Iterators, in turn. The argument
   * is an Iterator of Iterators. Like {@link MergedIterator2}, but generalized to arbitrary number
   * of iterators.
   */
  public static final class MergedIterator<T> implements Iterator<T> {
    Iterator<Iterator<T>> itorOfItors;

    public MergedIterator(Iterator<Iterator<T>> itorOfItors) {
      this.itorOfItors = itorOfItors;
    }

    // an empty iterator to prime the pump
    Iterator<T> current = new ArrayList<T>().iterator();

    @Override
    public boolean hasNext(/*>>>@GuardSatisfied MergedIterator<T> this*/) {
      while ((!current.hasNext()) && (itorOfItors.hasNext())) {
        current = itorOfItors.next();
      }
      return current.hasNext();
    }

    @Override
    public T next(/*>>>@GuardSatisfied MergedIterator<T> this*/) {
      hasNext(); // for side effect
      return current.next();
    }

    @Override
    public void remove(/*>>>@GuardSatisfied MergedIterator<T> this*/) {
      throw new UnsupportedOperationException();
    }
  }

  /** An iterator that only returns elements that match the given Filter. */
  @SuppressWarnings("assignment.type.incompatible") // problems in DFF branch
  public static final class FilteredIterator<T> implements Iterator<T> {
    Iterator<T> itor;
    Filter<T> filter;

    public FilteredIterator(Iterator<T> itor, Filter<T> filter) {
      this.itor = itor;
      this.filter = filter;
    }

    @SuppressWarnings("unchecked")
    T invalid_t = (T) new Object();

    T current = invalid_t;
    boolean current_valid = false;

    @Override
    public boolean hasNext(/*>>>@GuardSatisfied FilteredIterator<T> this*/) {
      while ((!current_valid) && itor.hasNext()) {
        current = itor.next();
        current_valid = filter.accept(current);
      }
      return current_valid;
    }

    @Override
    public T next(/*>>>@GuardSatisfied FilteredIterator<T> this*/) {
      if (hasNext()) {
        current_valid = false;
        @SuppressWarnings("interning")
        boolean ok = (current != invalid_t);
        assert ok;
        return current;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove(/*>>>@GuardSatisfied FilteredIterator<T> this*/) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Returns an iterator just like its argument, except that the first and last elements are
   * removed. They can be accessed via the getFirst and getLast methods.
   */
  @SuppressWarnings("assignment.type.incompatible") // problems in DFF branch
  public static final class RemoveFirstAndLastIterator<T> implements Iterator<T> {
    Iterator<T> itor;
    // I don't think this works, because the iterator might itself return null
    // /*@Nullable*/ T nothing = (/*@Nullable*/ T) null;
    @SuppressWarnings("unchecked")
    T nothing = (T) new Object();

    T first = nothing;
    T current = nothing;

    public RemoveFirstAndLastIterator(Iterator<T> itor) {
      this.itor = itor;
      if (itor.hasNext()) {
        first = itor.next();
      }
      if (itor.hasNext()) {
        current = itor.next();
      }
    }

    @Override
    public boolean hasNext(/*>>>@GuardSatisfied RemoveFirstAndLastIterator<T> this*/) {
      return itor.hasNext();
    }

    @Override
    public T next(/*>>>@GuardSatisfied RemoveFirstAndLastIterator<T> this*/) {
      if (!itor.hasNext()) {
        throw new NoSuchElementException();
      }
      T tmp = current;
      current = itor.next();
      return tmp;
    }

    public T getFirst() {
      @SuppressWarnings("interning") // check for equality to a special value
      boolean invalid = (first == nothing);
      if (invalid) {
        throw new NoSuchElementException();
      }
      return first;
    }

    // Throws an error unless the RemoveFirstAndLastIterator has already
    // been iterated all the way to its end (so the delegate is pointing to
    // the last element).  Also, this is buggy when the delegate is empty.
    public T getLast() {
      if (itor.hasNext()) {
        throw new Error();
      }
      return current;
    }

    @Override
    public void remove(/*>>>@GuardSatisfied RemoveFirstAndLastIterator<T> this*/) {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Return a List containing num_elts randomly chosen elements from the iterator, or all the
   * elements of the iterator if there are fewer. It examines every element of the iterator, but
   * does not keep them all in memory.
   *
   * @param <T> type of the iterator elements
   * @param itor elements to be randomly selected from
   * @param num_elts number of elements to select
   * @return list of num_elts elements from itor
   */
  public static <T> List<T> randomElements(Iterator<T> itor, int num_elts) {
    return randomElements(itor, num_elts, r);
  }

  private static Random r = new Random();

  /**
   * Return a List containing num_elts randomly chosen elements from the iterator, or all the
   * elements of the iterator if there are fewer. It examines every element of the iterator, but
   * does not keep them all in memory.
   *
   * @param <T> type of the iterator elements
   * @param itor elements to be randomly selected from
   * @param num_elts number of elements to select
   * @param random the Random instance to use to make selections
   * @return list of num_elts elements from itor
   */
  public static <T> List<T> randomElements(Iterator<T> itor, int num_elts, Random random) {
    // The elements are chosen with the following probabilities,
    // where n == num_elts:
    //   n n/2 n/3 n/4 n/5 ...

    RandomSelector<T> rs = new RandomSelector<T>(num_elts, random);

    while (itor.hasNext()) {
      rs.accept(itor.next());
    }
    return rs.getValues();

    /*
    ArrayList<T> result = new ArrayList<T>(num_elts);
    int i=1;
    for (int n=0; n<num_elts && itor.hasNext(); n++, i++) {
      result.add(itor.next());
    }
    for (; itor.hasNext(); i++) {
      T o = itor.next();
      // test random < num_elts/i
      if (random.nextDouble() * i < num_elts) {
        // This element will replace one of the existing elements.
        result.set(random.nextInt(num_elts), o);
      }
    }
    return result;

    */
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Map
  ///

  // In Python, inlining this gave a 10x speed improvement.
  // Will the same be true for Java?
  /**
   * Increment the Integer which is indexed by key in the Map. If the key isn't in the Map, it is
   * added.
   *
   * @param <T> type of keys in the map
   * @param m map to have one of its values incremented
   * @param key the key for the element whose value will be incremented
   * @param count how much to increment the value by
   * @return the old value, before it was incremented
   * @throws Error if the key is in the Map but maps to a non-Integer
   */
  public static <T> /*@Nullable*/ Integer incrementMap(Map<T, Integer> m, T key, int count) {
    Integer old = m.get(key);
    int new_total;
    if (old == null) {
      new_total = count;
    } else {
      new_total = old.intValue() + count;
    }
    return m.put(key, new_total);
  }

  /**
   * Returns a multi-line string representation of a map.
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param m map to be converted to a string
   * @return a multi-line string representation of m
   */
  public static <K, V> String mapToString(Map<K, V> m) {
    StringBuilder sb = new StringBuilder();
    mapToString(sb, m, "");
    return sb.toString();
  }

  /**
   * Write a multi-line representation of the map into the given Appendable (e.g., a StringBuilder).
   *
   * @param <K> type of map keys
   * @param <V> type of map values
   * @param sb an Appendable (such as StringBuilder) to which to write a multi-line string
   *     representation of m
   * @param m map to be converted to a string
   * @param linePrefix prefix to write at the beginning of each line
   */
  public static <K, V> void mapToString(Appendable sb, Map<K, V> m, String linePrefix) {
    try {
      for (Map.Entry<K, V> entry : m.entrySet()) {
        sb.append(linePrefix);
        sb.append(Objects.toString(entry.getKey()));
        sb.append(" => ");
        sb.append(Objects.toString(entry.getValue()));
        sb.append(lineSep);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a sorted version of m.keySet().
   *
   * @param <K> type of the map keys
   * @param <V> type of the map values
   * @param m a map whose keyset will be sorted
   * @return a sorted version of m.keySet()
   */
  public static <K extends Comparable<? super K>, V> Collection</*@KeyFor("#1")*/ K> sortedKeySet(
      Map<K, V> m) {
    ArrayList</*@KeyFor("#1")*/ K> theKeys = new ArrayList</*@KeyFor("#1")*/ K>(m.keySet());
    Collections.sort(theKeys);
    return theKeys;
  }

  /**
   * Returns a sorted version of m.keySet().
   *
   * @param <K> type of the map keys
   * @param <V> type of the map values
   * @param m a map whose keyset will be sorted
   * @param comparator the Comparator to use for sorting
   * @return a sorted version of m.keySet()
   */
  public static <K, V> Collection</*@KeyFor("#1")*/ K> sortedKeySet(
      Map<K, V> m, Comparator<K> comparator) {
    ArrayList</*@KeyFor("#1")*/ K> theKeys = new ArrayList</*@KeyFor("#1")*/ K>(m.keySet());
    Collections.sort(theKeys, comparator);
    return theKeys;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Method
  ///

  /**
   * Maps from a comma-delimited string of arg types, such as appears in a method signature, to an
   * array of Class objects, one for each arg type. Example keys include: "java.lang.String,
   * java.lang.String, java.lang.Class[]" and "int,int".
   */
  static HashMap<String, Class<?>[]> args_seen = new HashMap<String, Class<?>[]>();

  /**
   * Given a method signature, return the method.
   *
   * <p>Example calls are:
   *
   * <pre>
   * UtilPlume.methodForName("org.plumelib.util.UtilPlume.methodForName(java.lang.String, java.lang.String, java.lang.Class[])")
   * UtilPlume.methodForName("org.plumelib.util.UtilPlume.methodForName(java.lang.String,java.lang.String,java.lang.Class[])")
   * UtilPlume.methodForName("java.lang.Math.min(int,int)")
   * </pre>
   *
   * @param method a method signature
   * @return the method corresponding to the given signature
   * @throws ClassNotFoundException if the class is not found
   * @throws NoSuchMethodException if the method is not found
   */
  public static Method methodForName(String method)
      throws ClassNotFoundException, NoSuchMethodException, SecurityException {

    int oparenpos = method.indexOf('(');
    int dotpos = method.lastIndexOf('.', oparenpos);
    int cparenpos = method.indexOf(')', oparenpos);
    if ((dotpos == -1) || (oparenpos == -1) || (cparenpos == -1)) {
      throw new Error(
          "malformed method name should contain a period, open paren, and close paren: "
              + method
              + " <<"
              + dotpos
              + ","
              + oparenpos
              + ","
              + cparenpos
              + ">>");
    }
    for (int i = cparenpos + 1; i < method.length(); i++) {
      if (!Character.isWhitespace(method.charAt(i))) {
        throw new Error(
            "malformed method name should contain only whitespace following close paren");
      }
    }

    @SuppressWarnings("signature") // throws exception if class does not exist
    /*@BinaryNameForNonArray*/ String classname = method.substring(0, dotpos);
    String methodname = method.substring(dotpos + 1, oparenpos);
    String all_argnames = method.substring(oparenpos + 1, cparenpos).trim();
    Class<?>[] argclasses = args_seen.get(all_argnames);
    if (argclasses == null) {
      String[] argnames;
      if (all_argnames.equals("")) {
        argnames = new String[0];
      } else {
        argnames = split(all_argnames, ',');
      }

      /*@MonotonicNonNull*/ Class<?>[] argclasses_tmp = new Class<?>[argnames.length];
      for (int i = 0; i < argnames.length; i++) {
        String bnArgname = argnames[i].trim();
        /*@ClassGetName*/ String cgnArgname = binaryNameToClassGetName(bnArgname);
        argclasses_tmp[i] = classForName(cgnArgname);
      }
      @SuppressWarnings("cast")
      Class<?>[] argclasses_res = (/*@NonNull*/ Class<?>[]) argclasses_tmp;
      argclasses = argclasses_res;
      args_seen.put(all_argnames, argclasses_res);
    }
    return methodForName(classname, methodname, argclasses);
  }

  /**
   * Given a class name and a method name in that class, return the method.
   *
   * @param classname class in which to find the method
   * @param methodname the method name
   * @param params the parameters of the method
   * @return the method named classname.methodname with parameters params
   * @throws ClassNotFoundException if the class is not found
   * @throws NoSuchMethodException if the method is not found
   */
  public static Method methodForName(
      /*@BinaryNameForNonArray*/ String classname, String methodname, Class<?>[] params)
      throws ClassNotFoundException, NoSuchMethodException, SecurityException {

    Class<?> c = Class.forName(classname);
    Method m = c.getDeclaredMethod(methodname, params);
    return m;
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
      @SuppressWarnings("nullness") // didn't redirect stream, so getter returns non-null
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
   */
  @SuppressWarnings({"purity", "lock"}) // does not depend on object identity
  /*@Pure*/
  public static boolean propertyIsTrue(Properties p, String key) {
    String pvalue = p.getProperty(key);
    if (pvalue == null) {
      return false;
    }
    pvalue = pvalue.toLowerCase();
    return (pvalue.equals("true") || pvalue.equals("yes") || pvalue.equals("1"));
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
  public static /*@Nullable*/ String appendProperty(Properties p, String key, String value) {
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
  public static /*@Nullable*/ String setDefaultMaybe(Properties p, String key, String value) {
    String currentValue = p.getProperty(key);
    if (currentValue == null) {
      p.setProperty(key, value);
    }
    return currentValue;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Regexp (regular expression)
  ///

  // See RegexUtil class.

  ///////////////////////////////////////////////////////////////////////////
  /// Reflection
  ///

  // TODO: add method invokeMethod; see
  // java/Translation/src/graph/tests/Reflect.java (but handle returning a
  // value).

  // TODO: make this restore the access to its original value, such as private?
  /**
   * Sets the given field, which may be final and/or private. Leaves the field accessible. Intended
   * for use in readObject and nowhere else!
   *
   * @param o object in which to set the field
   * @param fieldName name of field to set
   * @param value new value of field
   * @throws NoSuchFieldException if the field does not exist in the object
   */
  public static void setFinalField(Object o, String fieldName, /*@Nullable*/ Object value)
      throws NoSuchFieldException {
    Class<?> c = o.getClass();
    while (c != Object.class) { // Class is interned
      // System.out.printf ("Setting field %s in %s%n", fieldName, c);
      try {
        Field f = c.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(o, value);
        return;
      } catch (NoSuchFieldException e) {
        if (c.getSuperclass() == Object.class) { // Class is interned
          throw e;
        }
      } catch (IllegalAccessException e) {
        throw new Error("This can't happen: " + e);
      }
      c = c.getSuperclass();
      assert c != null : "@AssumeAssertion(nullness): c was not Object, so is not null now";
    }
    throw new NoSuchFieldException(fieldName);
  }

  // TODO: make this restore the access to its original value, such as private?
  /**
   * Reads the given field, which may be private. Leaves the field accessible. Use with care!
   *
   * @param o object in which to set the field
   * @param fieldName name of field to set
   * @return new value of field
   * @throws NoSuchFieldException if the field does not exist in the object
   */
  public static /*@Nullable*/ Object getPrivateField(Object o, String fieldName)
      throws NoSuchFieldException {
    Class<?> c = o.getClass();
    while (c != Object.class) { // Class is interned
      // System.out.printf ("Setting field %s in %s%n", fieldName, c);
      try {
        Field f = c.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(o);
      } catch (IllegalAccessException e) {
        System.out.println("in getPrivateField, IllegalAccessException: " + e);
        throw new Error("This can't happen: " + e);
      } catch (NoSuchFieldException e) {
        if (c.getSuperclass() == Object.class) { // Class is interned
          throw e;
        }
        // nothing to do; will now examine superclass
      }
      c = c.getSuperclass();
      assert c != null : "@AssumeAssertion(nullness): c was not Object, so is not null now";
    }
    throw new NoSuchFieldException(fieldName);
  }

  /**
   * Returns the least upper bound of the given classes.
   *
   * @param a a class
   * @param b a class
   * @return the least upper bound of the two classes, or null if both are null
   */
  public static <T> /*@Nullable*/ Class<T> leastUpperBound(
      /*@Nullable*/ Class<T> a, /*@Nullable*/ Class<T> b) {
    if (a == b) {
      return a;
    } else if (a == null) {
      return b;
    } else if (b == null) {
      return a;
    } else if (a == Void.TYPE) {
      return b;
    } else if (b == Void.TYPE) {
      return a;
    } else if (a.isAssignableFrom(b)) {
      return a;
    } else if (b.isAssignableFrom(a)) {
      return b;
    } else {
      // There may not be a unique least upper bound.
      // Probably return some specific class rather than a wildcard.
      throw new Error("Not yet implemented");
    }
  }

  /**
   * Returns the least upper bound of all the given classes.
   *
   * @param classes a non-empty list of classes
   * @return the least upper bound of all the given classes
   */
  public static <T> /*@Nullable*/ Class<T> leastUpperBound(/*@Nullable*/ Class<T>[] classes) {
    Class<T> result = null;
    for (Class<T> clazz : classes) {
      result = leastUpperBound(result, clazz);
    }
    return result;
  }

  /**
   * Returns the least upper bound of the classes of the given objects.
   *
   * @param objects a list of objects
   * @return the least upper bound of the classes of the given objects, or null if all arguments are
   *     null
   */
  public static <T> /*@Nullable*/ Class<T> leastUpperBound(/*@PolyNull*/ Object[] objects) {
    Class<T> result = null;
    for (Object obj : objects) {
      if (obj != null) {
        result = leastUpperBound(result, (Class<T>) obj.getClass());
      }
    }
    return result;
  }

  /**
   * Returns the least upper bound of the classes of the given objects.
   *
   * @param objects a non-empty list of objects
   * @return the least upper bound of the classes of the given objects, or null if all arguments are
   *     null
   */
  public static <T> /*@Nullable*/ Class<T> leastUpperBound(
      List<? extends /*@Nullable*/ Object> objects) {
    Class<T> result = null;
    for (Object obj : objects) {
      if (obj != null) {
        result = leastUpperBound(result, (Class<T>) obj.getClass());
      }
    }
    return result;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Set
  ///

  /**
   * Return the object in this set that is equal to key. The Set abstraction doesn't provide this;
   * it only provides "contains". Returns null if the argument is null, or if it isn't in the set.
   *
   * @param set a set in which to look up the value
   * @param key the value to look up in the set
   * @return the object in this set that is equal to key, or null
   */
  public static /*@Nullable*/ Object getFromSet(Set<?> set, Object key) {
    if (key == null) {
      return null;
    }
    for (Object elt : set) {
      if (key.equals(elt)) {
        return elt;
      }
    }
    return null;
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
   * Return a String containing all the characters from the input stream.
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

  ///////////////////////////////////////////////////////////////////////////
  /// String
  ///

  /**
   * Return a new string which is the text of target with all instances of oldStr replaced by
   * newStr.
   *
   * @param target the string to do replacement in
   * @param oldStr the substring to replace
   * @param newStr the replacement
   * @return target with all instances of oldStr replaced by newStr
   */
  public static String replaceString(String target, String oldStr, String newStr) {
    if (oldStr.equals("")) {
      throw new IllegalArgumentException();
    }

    StringBuilder result = new StringBuilder();
    /*@IndexOrHigh("target")*/ int lastend = 0;
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
   * Return an array of Strings representing the characters between successive instances of the
   * delimiter character. Always returns an array of length at least 1 (it might contain only the
   * empty string).
   *
   * @see #split(String s, String delim)
   * @param s the string to split
   * @param delim delimiter to split the string on
   * @return array of length at least 1, containing s split on delimiter
   */
  public static String[] split(String s, char delim) {
    ArrayList<String> result_list = new ArrayList<String>();
    for (int delimpos = s.indexOf(delim); delimpos != -1; delimpos = s.indexOf(delim)) {
      result_list.add(s.substring(0, delimpos));
      s = s.substring(delimpos + 1);
    }
    result_list.add(s);
    String[] result = result_list.toArray(new /*@NonNull*/ String[result_list.size()]);
    return result;
  }

  /**
   * Return an array of Strings representing the characters between successive instances of the
   * delimiter String. Always returns an array of length at least 1 (it might contain only the empty
   * string).
   *
   * @see #split(String s, char delim)
   * @param s the string to split
   * @param delim delimiter to split the string on
   * @return array of length at least 1, containing s split on delimiter
   */
  public static String[] split(String s, String delim) {
    int delimlen = delim.length();
    if (delimlen == 0) {
      throw new Error("Second argument to split was empty.");
    }
    ArrayList<String> result_list = new ArrayList<String>();
    for (int delimpos = s.indexOf(delim); delimpos != -1; delimpos = s.indexOf(delim)) {
      result_list.add(s.substring(0, delimpos));
      s = s.substring(delimpos + delimlen);
    }
    result_list.add(s);
    @SuppressWarnings("index") // index checker has no list support: vectors
    String[] result = result_list.toArray(new /*@NonNull*/ String[result_list.size()]);
    return result;
  }

  /**
   * Return an array of Strings, one for each line in the argument. Always returns an array of
   * length at least 1 (it might contain only the empty string). All common line separators (cr, lf,
   * cr-lf, or lf-cr) are supported. Note that a string that ends with a line separator will return
   * an empty string as the last element of the array.
   *
   * @see #split(String s, char delim)
   * @param s the string to split
   * @return an array of Strings, one for each line in the argument
   */
  /*@SideEffectFree*/
  /*@StaticallyExecutable*/
  public static String[] splitLines(String s) {
    return s.split("\r\n?|\n\r?", -1);
  }

  /**
   * Concatenate the string representations of the array elements, placing the delimiter between
   * them.
   *
   * <p>If you are using Java 8 or later, then use the {@code String.join()} method instead.
   *
   * @see org.plumelib.util.ArraysPlume#toString(int[])
   * @param a array of values to concatenate
   * @param delim delimiter to place between printed representations
   * @return the concatenation of the string representations of the values, with the delimiter
   *     between
   */
  public static String join(Object[] a, String delim) {
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
   * @see org.plumelib.util.ArraysPlume#toString(int[])
   * @param a array of values to concatenate
   * @return the concatenation of the string representations of the values, each on its own line
   */
  public static String joinLines(Object... a) {
    return join(a, lineSep);
  }

  /**
   * Concatenate the string representations of the objects, placing the delimiter between them.
   *
   * @see java.util.AbstractCollection#toString()
   * @param v collection of values to concatenate
   * @param delim delimiter to place between printed representations
   * @return the concatenation of the string representations of the values, with the delimiter
   *     between
   */
  public static String join(Iterable<? extends Object> v, String delim) {
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
   */
  public static String joinLines(List<String> v) {
    return join(v, lineSep);
  }

  /**
   * Escape \, ", newline, and carriage-return characters in the target as \\, \", \n, and \r;
   * return a new string if any modifications were necessary. The intent is that by surrounding the
   * return value with double quote marks, the result will be a Java string literal denoting the
   * original string.
   *
   * @param orig string to quote
   * @return quoted version of orig
   */
  public static String escapeNonJava(String orig) {
    StringBuilder sb = new StringBuilder();
    // The previous escape character was seen right before this position.
    /*@IndexOrHigh("orig")*/ int post_esc = 0;
    int orig_len = orig.length();
    for (int i = 0; i < orig_len; i++) {
      char c = orig.charAt(i);
      switch (c) {
        case '\"':
        case '\\':
          if (post_esc < i) {
            sb.append(orig.substring(post_esc, i));
          }
          sb.append('\\');
          post_esc = i;
          break;
        case '\n': // not lineSep
          if (post_esc < i) {
            sb.append(orig.substring(post_esc, i));
          }
          sb.append("\\n"); // not lineSep
          post_esc = i + 1;
          break;
        case '\r':
          if (post_esc < i) {
            sb.append(orig.substring(post_esc, i));
          }
          sb.append("\\r");
          post_esc = i + 1;
          break;
        default:
          // Nothing to do: i gets incremented
      }
    }
    if (sb.length() == 0) {
      return orig;
    }
    sb.append(orig.substring(post_esc));
    return sb.toString();
  }

  // The overhead of this is too high to call in escapeNonJava(String), so
  // it is inlined there.
  /**
   * Like {@link #escapeNonJava(String)}, but for a single character.
   *
   * @param ch character to quote
   * @return quoted version och ch
   */
  public static String escapeNonJava(Character ch) {
    char c = ch.charValue();
    switch (c) {
      case '\"':
        return "\\\"";
      case '\\':
        return "\\\\";
      case '\n': // not lineSep
        return "\\n"; // not lineSep
      case '\r':
        return "\\r";
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
   */
  public static String escapeNonASCII(String orig) {
    StringBuilder sb = new StringBuilder();
    int orig_len = orig.length();
    for (int i = 0; i < orig_len; i++) {
      char c = orig.charAt(i);
      sb.append(escapeNonASCII(c));
    }
    return sb.toString();
  }

  /**
   * Like escapeNonJava(), but quote more characters so that the result is sure to be printable
   * ASCII.
   *
   * <p>This implementatino is not particularly optimized.
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
   * Replace "\\", "\"", "\n", and "\r" sequences by their one-character equivalents. All other
   * backslashes are removed (for instance, octal/hex escape sequences are not turned into their
   * respective characters). This is the inverse operation of escapeNonJava(). Previously known as
   * unquote().
   *
   * @param orig string to quoto
   * @return quoted version of orig
   */
  public static String unescapeNonJava(String orig) {
    StringBuilder sb = new StringBuilder();
    // The previous escape character was seen just before this position.
    /*@LTEqLengthOf("orig")*/ int post_esc = 0;
    int this_esc = orig.indexOf('\\');
    while (this_esc != -1) {
      if (this_esc == orig.length() - 1) {
        sb.append(orig.substring(post_esc, this_esc + 1));
        post_esc = this_esc + 1;
        break;
      }
      switch (orig.charAt(this_esc + 1)) {
        case 'n':
          sb.append(orig.substring(post_esc, this_esc));
          sb.append('\n'); // not lineSep
          post_esc = this_esc + 2;
          break;
        case 'r':
          sb.append(orig.substring(post_esc, this_esc));
          sb.append('\r');
          post_esc = this_esc + 2;
          break;
        case '\\':
          // This is not in the default case because the search would find
          // the quoted backslash.  Here we incluce the first backslash in
          // the output, but not the first.
          sb.append(orig.substring(post_esc, this_esc + 1));
          post_esc = this_esc + 2;
          break;

        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          sb.append(orig.substring(post_esc, this_esc));
          char octal_char = 0;
          int ii = this_esc + 1;
          while (ii < orig.length()) {
            char ch = orig.charAt(ii++);
            if ((ch < '0') || (ch > '8')) {
              break;
            }
            octal_char = (char) ((octal_char * 8) + Character.digit(ch, 8));
          }
          sb.append(octal_char);
          post_esc = ii - 1;
          break;

        default:
          // In the default case, retain the character following the backslash,
          // but discard the backslash itself.  "\*" is just a one-character string.
          sb.append(orig.substring(post_esc, this_esc));
          post_esc = this_esc + 1;
          break;
      }
      this_esc = orig.indexOf('\\', post_esc);
    }
    if (post_esc == 0) {
      return orig;
    }
    sb.append(orig.substring(post_esc));
    return sb.toString();
  }

  /**
   * Remove all whitespace before or after instances of delimiter.
   *
   * @param arg string to remove whitespace in
   * @param delimiter string to remove whitespace abutting
   * @return version of arg, with whitespace abutting delimiter removed
   */
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
   */
  public static String removeWhitespaceAfter(String arg, String delimiter) {
    if (delimiter == null || delimiter.equals("")) {
      throw new IllegalArgumentException("Bad delimiter: \"" + delimiter + "\"");
    }
    // String orig = arg;
    int delim_len = delimiter.length();
    int delim_index = arg.indexOf(delimiter);
    while (delim_index > -1) {
      int non_ws_index = delim_index + delim_len;
      while ((non_ws_index < arg.length()) && (Character.isWhitespace(arg.charAt(non_ws_index)))) {
        non_ws_index++;
      }
      // if (non_ws_index == arg.length()) {
      //   System.out.println("No nonspace character at end of: " + arg);
      // } else {
      //   System.out.println("'" + arg.charAt(non_ws_index) + "' not a space character at " +
      //       non_ws_index + " in: " + arg);
      // }
      if (non_ws_index != delim_index + delim_len) {
        arg = arg.substring(0, delim_index + delim_len) + arg.substring(non_ws_index);
      }
      delim_index = arg.indexOf(delimiter, delim_index + 1);
    }
    return arg;
  }

  /**
   * Remove all whitespace before instances of delimiter.
   *
   * @param arg string to remove whitespace in
   * @param delimiter string to remove whitespace before
   * @return version of arg, with whitespace before delimiter removed
   */
  public static String removeWhitespaceBefore(String arg, String delimiter) {
    if (delimiter == null || delimiter.equals("")) {
      throw new IllegalArgumentException("Bad delimiter: \"" + delimiter + "\"");
    }
    // System.out.println("removeWhitespaceBefore(\"" + arg + "\", \"" + delimiter + "\")");
    // String orig = arg;
    int delim_index = arg.indexOf(delimiter);
    while (delim_index > -1) {
      int non_ws_index = delim_index - 1;
      while ((non_ws_index >= 0) && (Character.isWhitespace(arg.charAt(non_ws_index)))) {
        non_ws_index--;
      }
      // if (non_ws_index == -1) {
      //   System.out.println("No nonspace character at front of: " + arg);
      // } else {
      //   System.out.println("'" + arg.charAt(non_ws_index) + "' not a space character at " +
      //       non_ws_index + " in: " + arg);
      // }
      if (non_ws_index != delim_index - 1) {
        arg = arg.substring(0, non_ws_index + 1) + arg.substring(delim_index);
      }
      delim_index = arg.indexOf(delimiter, non_ws_index + 2);
    }
    return arg;
  }

  /**
   * Return either "n <em>noun</em>" or "n <em>noun</em>s" depending on n. Adds "es" to words ending
   * with "ch", "s", "sh", or "x".
   *
   * @param n count of nouns
   * @param noun word being counted
   * @return noun, if n==1; otherwise, pluralization of noun
   */
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
   */
  public static String lpad(String s, /*@NonNegative*/ int length) {
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
   */
  public static String rpad(String s, /*@NonNegative*/ int length) {
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
   */
  public static String rpad(int num, /*@NonNegative*/ int length) {
    return rpad(String.valueOf(num), length);
  }

  /**
   * Converts the double to a String, then formats it using {@link #rpad(String,int)}.
   *
   * @param num double whose string representation to truncate or pad
   * @param length goal length
   * @return a string representation of num truncated or padded to length characters
   */
  public static String rpad(double num, /*@NonNegative*/ int length) {
    return rpad(String.valueOf(num), length);
  }

  /**
   * Same as built-in String comparison, but accept null arguments, and place them at the beginning.
   */
  public static class NullableStringComparator implements Comparator<String>, Serializable {
    static final long serialVersionUID = 20150812L;

    /*@Pure*/
    @Override
    public int compare(String s1, String s2) {
      if (s1 == null && s2 == null) {
        return 0;
      }
      if (s1 == null && s2 != null) {
        return 1;
      }
      if (s1 != null && s2 == null) {
        return -1;
      }
      return s1.compareTo(s2);
    }
  }

  // This could test the types of the elemets, and do something more sophisticated based on the
  // types.
  /**
   * Attempt to order Objects. Puts null at the beginning. Returns 0 for equal elements. Otherwise,
   * orders by the result of {@code toString()}.
   *
   * <p>Note: if toString returns a nondeterministic value, such as one that depends on the result
   * of {@code hashCode()}, then this comparator may yield different orderings from run to run of a
   * program.
   */
  public static class ObjectComparator implements Comparator</*@Nullable*/ Object>, Serializable {
    static final long serialVersionUID = 20170420L;

    @SuppressWarnings({
      "purity.not.deterministic.call",
      "lock"
    }) // toString is being used in a deterministic way
    /*@Pure*/
    @Override
    public int compare(/*@Nullable*/ Object o1, /*@Nullable*/ Object o2) {
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
   * Return the number of times the character appears in the string.
   *
   * @param s string to search in
   * @param ch character to search for
   * @return number of times the character appears in the string
   */
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
   * Return the number of times the second string appears in the first.
   *
   * @param s string to search in
   * @param sub non-empty string to search for
   * @return number of times the substring appears in the string
   */
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

  ///////////////////////////////////////////////////////////////////////////
  /// StringTokenizer
  ///

  /**
   * Return a ArrayList of the Strings returned by {@link
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
   */
  public static ArrayList<Object> tokens(String str, String delim, boolean returnDelims) {
    return makeArrayList(new StringTokenizer(str, delim, returnDelims));
  }

  /**
   * Return a ArrayList of the Strings returned by {@link
   * java.util.StringTokenizer#StringTokenizer(String,String)} with the given arguments.
   *
   * @param str a string to be parsed
   * @param delim the delimiters
   * @return vector of strings resulting from tokenization
   */
  public static ArrayList<Object> tokens(String str, String delim) {
    return makeArrayList(new StringTokenizer(str, delim));
  }

  /**
   * Return a ArrayList of the Strings returned by {@link
   * java.util.StringTokenizer#StringTokenizer(String)} with the given arguments.
   *
   * @param str a string to be parsed
   * @return vector of strings resulting from tokenization
   */
  public static ArrayList<Object> tokens(String str) {
    return makeArrayList(new StringTokenizer(str));
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Throwable
  ///

  /**
   * Return a String representation of the backtrace of the given Throwable. To see a backtrace at
   * the the current location, do {@code backtrace(new Throwable())}.
   *
   * @param t the Throwable to obtain a backtrace of
   * @return a String representation of the backtrace of the given Throwable
   */
  public static String backTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.close();
    String result = sw.toString();
    return result;
  }

  ///////////////////////////////////////////////////////////////////////////
  /// Collections
  ///

  /**
   * Return the sorted version of the list. Does not alter the list. Simply calls {@code
   * Collections.sort(List<T>, Comparator<? super T>)}.
   *
   * @return a sorted version of the list
   * @param <T> type of elements of the list
   * @param l a list to sort
   * @param c a sorted version of the list
   */
  public static <T> List<T> sortList(List<T> l, Comparator<? super T> c) {
    List<T> result = new ArrayList<T>(l);
    Collections.sort(result, c);
    return result;
  }

  // This should perhaps be named withoutDuplicates to emphasize that
  // it does not side-effect its argument.
  /**
   * Return a copy of the list with duplicates removed. Retains the original order.
   *
   * @param <T> type of elements of the list
   * @param l a list to remove duplicates from
   * @return a copy of the list with duplicates removed
   */
  public static <T> List<T> removeDuplicates(List<T> l) {
    HashSet<T> hs = new LinkedHashSet<T>(l);
    List<T> result = new ArrayList<T>(hs);
    return result;
  }

  /** All calls to deepEquals that are currently underway. */
  private static HashSet<WeakIdentityPair<Object, Object>> deepEqualsUnderway =
      new HashSet<WeakIdentityPair<Object, Object>>();

  /**
   * Determines deep equality for the elements.
   *
   * <ul>
   *   <li>If both are primitive arrays, uses java.util.Arrays.equals.
   *   <li>If both are Object[], uses java.util.Arrays.deepEquals and does not recursively call this
   *       method.
   *   <li>If both are lists, uses deepEquals recursively on each element.
   *   <li>For other types, just uses equals() and does not recursively call this method.
   * </ul>
   *
   * @param o1 first value to compare
   * @param o2 second value to comare
   * @return true iff o1 and o2 are deeply equal
   */
  @SuppressWarnings({"purity", "lock"}) // side effect to static field deepEqualsUnderway
  /*@Pure*/
  public static boolean deepEquals(/*@Nullable*/ Object o1, /*@Nullable*/ Object o2) {
    @SuppressWarnings("interning")
    boolean sameObject = (o1 == o2);
    if (sameObject) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }

    if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
      return Arrays.equals((boolean[]) o1, (boolean[]) o2);
    }
    if (o1 instanceof byte[] && o2 instanceof byte[]) {
      return Arrays.equals((byte[]) o1, (byte[]) o2);
    }
    if (o1 instanceof char[] && o2 instanceof char[]) {
      return Arrays.equals((char[]) o1, (char[]) o2);
    }
    if (o1 instanceof double[] && o2 instanceof double[]) {
      return Arrays.equals((double[]) o1, (double[]) o2);
    }
    if (o1 instanceof float[] && o2 instanceof float[]) {
      return Arrays.equals((float[]) o1, (float[]) o2);
    }
    if (o1 instanceof int[] && o2 instanceof int[]) {
      return Arrays.equals((int[]) o1, (int[]) o2);
    }
    if (o1 instanceof long[] && o2 instanceof long[]) {
      return Arrays.equals((long[]) o1, (long[]) o2);
    }
    if (o1 instanceof short[] && o2 instanceof short[]) {
      return Arrays.equals((short[]) o1, (short[]) o2);
    }

    @SuppressWarnings({"purity", "lock"}) // creates local state
    WeakIdentityPair<Object, Object> mypair = new WeakIdentityPair<Object, Object>(o1, o2);
    if (deepEqualsUnderway.contains(mypair)) {
      return true;
    }

    if (o1 instanceof Object[] && o2 instanceof Object[]) {
      return Arrays.deepEquals((Object[]) o1, (Object[]) o2);
    }

    if (o1 instanceof List<?> && o2 instanceof List<?>) {
      List<?> l1 = (List<?>) o1;
      List<?> l2 = (List<?>) o2;
      if (l1.size() != l2.size()) {
        return false;
      }
      try {
        deepEqualsUnderway.add(mypair);
        for (int i = 0; i < l1.size(); i++) {
          Object e1 = l1.get(i);
          Object e2 = l2.get(i);
          if (!deepEquals(e1, e2)) {
            return false;
          }
        }
      } finally {
        deepEqualsUnderway.remove(mypair);
      }

      return true;
    }

    return o1.equals(o2);
  }

  ///////////////////////////////////////////////////////////////////////////
  /// ArrayList
  ///

  /**
   * Returns a vector containing the elements of the enumeration.
   *
   * @param <T> type of the enumeration and vector elements
   * @param e an enumeration to convert to a ArrayList
   * @return a vector containing the elements of the enumeration
   */
  public static <T> ArrayList<T> makeArrayList(Enumeration<T> e) {
    ArrayList<T> result = new ArrayList<T>();
    while (e.hasMoreElements()) {
      result.add(e.nextElement());
    }
    return result;
  }

  // Rather than writing something like ArrayListToStringArray, use
  //   v.toArray(new String[0])

  // Helper method
  /**
   * Compute (n choose k), which is (n! / (k!(n-k)!)).
   *
   * @param n number of elements from which to choose
   * @param k number of elements to choose
   * @return n choose k, or Long.MAX_VALUE if the value would overflow
   */
  private static long choose(int n, int k) {
    // From https://stackoverflow.com/questions/2201113/combinatoric-n-choose-r-in-java-math
    if (n < k) {
      return 0;
    }
    if (k == 0 || k == n) {
      return 1;
    }
    long a = choose(n - 1, k - 1);
    long b = choose(n - 1, k);
    if (a < 0 || a == Long.MAX_VALUE || b < 0 || b == Long.MAX_VALUE || a + b < 0) {
      return Long.MAX_VALUE;
    } else {
      return a + b;
    }
  }

  /**
   * Returns a list of lists of each combination (with repetition, but not permutations) of the
   * specified objects starting at index {@code start} over {@code dims} dimensions, for {@code dims
   * > 0}.
   *
   * <p>For example, create_combinations(1, 0, {a, b, c}) returns a 3-element list of singleton
   * lists:
   *
   * <pre>
   *    {a}, {b}, {c}
   * </pre>
   *
   * And create_combinations(2, 0, {a, b, c}) returns a 6-element list of 2-element lists:
   *
   * <pre>
   *    {a, a}, {a, b}, {a, c}
   *    {b, b}, {b, c},
   *    {c, c}
   * </pre>
   *
   * @param <T> type of the input list elements, and type of the innermost output list elements
   * @param dims number of dimensions: that is, size of each innermost list
   * @param start initial index
   * @param objs list of elements to create combinations of
   * @return list of lists of length dims, each of which combines elements from objs
   */
  public static <T> List<List<T>> create_combinations(
      /*@Positive*/ int dims, /*@NonNegative*/ int start, List<T> objs) {

    if (dims < 1) {
      throw new IllegalArgumentException();
    }

    List<List<T>> results = new ArrayList<List<T>>();

    for (int i = start; i < objs.size(); i++) {
      if (dims == 1) {
        List<T> simple = new ArrayList<T>();
        simple.add(objs.get(i));
        results.add(simple);
      } else {
        List<List<T>> combos = create_combinations(dims - 1, i, objs);
        for (List<T> lt : combos) {
          List<T> simple = new ArrayList<T>();
          simple.add(objs.get(i));
          simple.addAll(lt);
          results.add(simple);
        }
      }
    }

    return (results);
  }

  /**
   * Returns a list of lists of each combination (with repetition, but not permutations) of integers
   * from start to cnt (inclusive) over arity dimensions.
   *
   * <p>For example, create_combinations(1, 0, 2) returns a 3-element list of singleton lists:
   *
   * <pre>
   *    {0}, {1}, {2}
   * </pre>
   *
   * And create_combinations(2, 10, 2) returns a 6-element list of 2-element lists:
   *
   * <pre>
   *    {10, 10}, {10, 11}, {10, 12}, {11, 11}, {11, 12}, {12, 12}
   * </pre>
   *
   * The length of the list is (cnt multichoose arity), which is ((cnt + arity - 1) choose arity).
   *
   * @param arity size of each innermost list
   * @param start initial value
   * @param cnt maximum element value
   * @return list of lists of length arity, each of which combines integers from start to cnt
   */
  public static ArrayList<ArrayList<Integer>> create_combinations(
      int arity, /*@NonNegative*/ int start, int cnt) {

    long numResults = choose(cnt + arity - 1, arity);
    if (numResults > 100000000) {
      throw new Error("Do you really want to create more than 100 million lists?");
    }

    ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>();

    // Return a list with one zero length element if arity is zero
    if (arity == 0) {
      results.add(new ArrayList<Integer>());
      return (results);
    }

    for (int i = start; i <= cnt; i++) {
      ArrayList<ArrayList<Integer>> combos = create_combinations(arity - 1, i, cnt);
      for (ArrayList<Integer> li : combos) {
        ArrayList<Integer> simple = new ArrayList<Integer>();
        simple.add(i);
        simple.addAll(li);
        results.add(simple);
      }
    }

    return (results);
  }

  /**
   * Returns the simple unqualified class name that corresponds to the specified fully qualified
   * name. For example, if qualified_name is java.lang.String, String will be returned.
   *
   * @deprecated use {@link #fullyQualifiedNameToSimpleName} instead.
   * @param qualified_name the fully-qualified name of a class
   * @return the simple unqualified name of the class
   */
  @Deprecated
  public static /*@ClassGetSimpleName*/ String unqualified_name(
      /*@FullyQualifiedName*/ String qualified_name) {
    return fullyQualifiedNameToSimpleName(qualified_name);
  }

  /**
   * Returns the simple unqualified class name that corresponds to the specified fully qualified
   * name. For example, if qualified_name is java.lang.String, String will be returned.
   *
   * @param qualified_name the fully-qualified name of a class
   * @return the simple unqualified name of the class
   */
  // TODO: does not follow the specification for inner classes (where the
  // type name should be empty), but I think this is more informative anyway.
  @SuppressWarnings("signature") // string conversion
  public static /*@ClassGetSimpleName*/ String fullyQualifiedNameToSimpleName(
      /*@FullyQualifiedName*/ String qualified_name) {

    int offset = qualified_name.lastIndexOf('.');
    if (offset == -1) {
      return (qualified_name);
    }
    return (qualified_name.substring(offset + 1));
  }

  /**
   * Returns the simple unqualified class name that corresponds to the specified class. For example
   * if qualified name of the class is java.lang.String, String will be returned.
   *
   * @deprecated use {@link Class#getSimpleName()} instead.
   * @param cls a class
   * @return the simple unqualified name of the class
   */
  @Deprecated
  public static /*@ClassGetSimpleName*/ String unqualified_name(Class<?> cls) {
    return cls.getSimpleName();
  }

  /**
   * Convert a number into an abbreviation such as "5.00K" for 5000 or "65.0M" for 65000000. K
   * stands for 1000, not 1024; M stands for 1000000, not 1048576, etc. There are always exactly 3
   * decimal digits of precision in the result (counting both sides of the decimal point).
   *
   * @param val a numeric value
   * @return an abbreviated string representation of the value
   */
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
}
