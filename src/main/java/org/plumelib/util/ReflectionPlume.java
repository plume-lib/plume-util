// If you edit this file, you must also edit its tests.
// For tests of this and the entire plume package, see class TestPlume.

package org.plumelib.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/*>>>
import org.checkerframework.checker.index.qual.*;
import org.checkerframework.checker.lock.qual.*;
import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.checker.regex.qual.*;
import org.checkerframework.checker.signature.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;
*/

/** Utility functions related to reflection, Class, Method, ClassLoader, and classpath. */
public final class ReflectionPlume {

  /** This class is a collection of methods; it does not represent anything. */
  private ReflectionPlume() {
    throw new Error("do not instantiate");
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
        argnames = UtilPlume.split(all_argnames, ',');
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
}
