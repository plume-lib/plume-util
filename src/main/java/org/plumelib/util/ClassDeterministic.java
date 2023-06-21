package org.plumelib.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import org.checkerframework.checker.interning.qual.Interned;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Deterministic versions of {@code java.lang.Class} methods, which return arrays in sorted order.
 */
public class ClassDeterministic {

  /** Do not call; this class is a collection of methods and does not represent anything. */
  private ClassDeterministic() {
    throw new Error("do not instantiate");
  }

  /**
   * Like {@link Class#getAnnotations()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose annotations to return
   * @return the class's annotations
   */
  public static Annotation[] getAnnotations(Class<?> c) {
    Annotation[] result = c.getAnnotations();
    Arrays.sort(result, annotationComparator);
    return result;
  }

  /**
   * Like {@link Class#getDeclaredAnnotations()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared annotations to return
   * @return the class's declared annotations
   */
  public static Annotation[] getDeclaredAnnotations(Class<?> c) {
    Annotation[] result = c.getDeclaredAnnotations();
    Arrays.sort(result, annotationComparator);
    return result;
  }

  /**
   * Like {@link Class#getClasses()}, but returns the classes in deterministic order.
   *
   * @param c the Class whose member classes to return
   * @return the class's member classes
   */
  public static Class<?>[] getClasses(Class<?> c) {
    Class<?>[] result = c.getClasses();
    Arrays.sort(result, classComparator);
    return result;
  }

  /**
   * Like {@link Class#getDeclaredClasses()}, but returns the classes in deterministic order.
   *
   * @param c the Class whose declared member classes to return
   * @return the class's declared member classes
   */
  public static Class<?>[] getDeclaredClasses(Class<?> c) {
    Class<?>[] result = c.getDeclaredClasses();
    Arrays.sort(result, classComparator);
    return result;
  }

  /**
   * Like {@link Class#getEnumConstants()}, but returns the methods in deterministic order. Returns
   * null if the argument is not an enum class.
   *
   * @param <T> the class's type parameter
   * @param c the Class whose enum constants to return
   * @return the class's enum constants
   */
  @SuppressWarnings("signedness") // ToStringComparator problem
  public static <@Interned T> T @Nullable [] getEnumConstants(Class<T> c) {
    @NonNull T[] result = c.getEnumConstants();
    if (result == null) {
      return null;
    }
    Arrays.sort(result, toStringComparator);
    return result;
  }

  /**
   * Like {@link Class#getConstructors()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose constructors to return
   * @return the class's constructors
   */
  public static Constructor<?>[] getConstructors(Class<?> c) {
    Constructor<?>[] result = c.getConstructors();
    Arrays.sort(result, constructorComparator);
    return result;
  }

  /**
   * Like {@link Class#getDeclaredConstructors()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared constructors to return
   * @return the class's declared constructors
   */
  public static Constructor<?>[] getDeclaredConstructors(Class<?> c) {
    Constructor<?>[] result = c.getDeclaredConstructors();
    Arrays.sort(result, constructorComparator);
    return result;
  }

  /**
   * Like {@link Class#getFields()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose fields to return
   * @return the class's fields
   */
  public static Field[] getFields(Class<?> c) {
    Field[] result = c.getFields();
    Arrays.sort(result, fieldComparator);
    return result;
  }

  /**
   * Like {@link Class#getDeclaredFields()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared fields to return
   * @return the class's declared fields
   */
  public static Field[] getDeclaredFields(Class<?> c) {
    Field[] result = c.getDeclaredFields();
    Arrays.sort(result, fieldComparator);
    return result;
  }

  /**
   * Like {@link Class#getMethods()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose methods to return
   * @return the class's methods
   */
  public static Method[] getMethods(Class<?> c) {
    Method[] result = c.getMethods();
    Arrays.sort(result, methodComparator);
    return result;
  }

  /**
   * Like {@link Class#getDeclaredMethods()}, but returns the methods in deterministic order.
   *
   * @param c the Class whose declared methods to return
   * @return the class's declared methods
   */
  public static Method[] getDeclaredMethods(Class<?> c) {
    Method[] result = c.getDeclaredMethods();
    Arrays.sort(result, methodComparator);
    return result;
  }

  /// Helper routines

  // /**
  //  * Creates a sorted list from an array of elements using the given classComparator.
  //  *
  //  * @param array the array of elements to be sorted
  //  * @param comparator the classComparator over the element type
  //  * @param <T> the element type
  //  * @return the sorted list of elements of the given array
  //  */
  // private <T> List<T> toSortedList(T[] array, Comparator<T> comparator) {
  //   List<T> list = new ArrayList<>();
  //   Collections.addAll(list, array);
  //   Collections.sort(list, comparator);
  //   return list;
  // }

  /** Compares Annotation objects by type name. */
  static AnnotationComparator annotationComparator = new AnnotationComparator();

  /** Compares Annotation objects by type name. */
  private static class AnnotationComparator implements Comparator<Annotation> {

    /** Create a new AnnotationComparator. */
    public AnnotationComparator() {}

    @Override
    public int compare(Annotation a1, Annotation a2) {
      return classComparator.compare(a1.annotationType(), a2.annotationType());
    }
  }

  /** Compares Class objects by fully-qualified name. */
  static ClassComparator classComparator = new ClassComparator();

  /** Compares Class objects by fully-qualified name. */
  private static class ClassComparator implements Comparator<Class<?>> {

    /** Create a new ClassComparator. */
    public ClassComparator() {}

    @Override
    public int compare(Class<?> c1, Class<?> c2) {
      return c1.getName().compareTo(c2.getName());
    }
  }

  /**
   * Compares Method objects by signature: compares name, number of parameters, parameter type
   * names, declaring class, and return type (which is necessary to distinguish bridge methods).
   */
  static MethodComparator methodComparator = new MethodComparator();

  /**
   * Compares Method objects by signature: compares name, number of parameters, parameter type
   * names, declaring class, and return type (which is necessary to distinguish bridge methods).
   */
  private static class MethodComparator implements Comparator<Method> {

    /** Create a new MethodComparator. */
    public MethodComparator() {}

    @Override
    public int compare(Method m1, Method m2) {
      int result;
      result = m1.getName().compareTo(m2.getName());
      if (result != 0) {
        return result;
      }
      Class<?>[] ptypes1 = m1.getParameterTypes();
      Class<?>[] ptypes2 = m2.getParameterTypes();
      result = ptypes1.length - ptypes2.length;
      if (result != 0) {
        return result;
      }
      assert ptypes1.length == ptypes2.length
          : "@AssumeAssertion(index): difference of lengths is 0;"
              + " https://github.com/kelloggm/checker-framework/issues/231";
      for (int i = 0; i < ptypes1.length; i++) {
        result = classComparator.compare(ptypes1[i], ptypes2[i]);
        if (result != 0) {
          return result;
        }
      }
      // Consider the declaring class last.  This minimizes differences in order when overriding
      // relationships in a library have changed.
      result = classComparator.compare(m1.getDeclaringClass(), m2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      // Two methods in a classfile can have the same name and argument types
      // if one is a bridge method.  Distinguish them by their return type.
      result = classComparator.compare(m1.getReturnType(), m2.getReturnType());
      if (result != 0) {
        return result;
      }
      return result;
    }
  }

  /**
   * Compares Constructor objects by signature: compares name, number of parameters, and parameter
   * type names.
   */
  static ConstructorComparator constructorComparator = new ConstructorComparator();

  /**
   * Compares Constructor objects by signature: compares name, number of parameters, and parameter
   * type names.
   */
  private static class ConstructorComparator implements Comparator<Constructor<?>> {

    /** Create a new ConstructorComparator. */
    public ConstructorComparator() {}

    @Override
    public int compare(Constructor<?> c1, Constructor<?> c2) {
      int result = classComparator.compare(c1.getDeclaringClass(), c2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      Class<?>[] ptypes1 = c1.getParameterTypes();
      Class<?>[] ptypes2 = c2.getParameterTypes();
      result = ptypes1.length - ptypes2.length;
      if (result != 0) {
        return result;
      }
      assert ptypes1.length == ptypes2.length
          : "@AssumeAssertion(index): difference of lengths is 0;"
              + " https://github.com/kelloggm/checker-framework/issues/231";
      for (int i = 0; i < ptypes1.length; i++) {
        result = classComparator.compare(ptypes1[i], ptypes2[i]);
        if (result != 0) {
          return result;
        }
      }
      return result;
    }
  }

  /** Compares Field objects by name. */
  static FieldComparator fieldComparator = new FieldComparator();

  /** Compares Field objects by name. */
  private static class FieldComparator implements Comparator<Field> {

    /** Create a new FieldComparator. */
    public FieldComparator() {}

    @Override
    public int compare(Field f1, Field f2) {
      int result = classComparator.compare(f1.getDeclaringClass(), f2.getDeclaringClass());
      if (result != 0) {
        return result;
      }
      return f1.getName().compareTo(f2.getName());
    }
  }

  /** Compares objects by the result of toString(). */
  static ToStringComparator toStringComparator = new ToStringComparator();

  /** Compares objects by the result of toString(). */
  private static class ToStringComparator implements Comparator<Object> {

    /** Create a new ToStringComparator. */
    public ToStringComparator() {}

    @Override
    public int compare(Object o1, Object o2) {
      return o1.toString().compareTo(o2.toString());
    }
  }
}
