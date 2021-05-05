package org.plumelib.util;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/** Defines a static method {@link #dumpHeap} that dumps the heap to an .hprof file. */
public class DumpHeap {

  /**
   * The HotSpot Diagnostic MBean. Its type is Object, in case HotSpotDiagnosticMXBean is not
   * available at compile time.
   */
  private static volatile @MonotonicNonNull Object hotspotMBean;

  /** The method com.sun.management.HotSpotDiagnosticMXBean#dumpHeap. */
  private static @MonotonicNonNull Method dumpHeapMethod;

  /** Initialize the fields of this class. */
  @SuppressWarnings({"nullness:assignment", "nullness:contracts.postcondition"}) // reflection
  @EnsuresNonNull({"hotspotMBean", "dumpHeapMethod"})
  private static synchronized void initializeFields() {
    try {
      Class<?> mxbeanClass = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
      hotspotMBean =
          ManagementFactory.newPlatformMXBeanProxy(
              ManagementFactory.getPlatformMBeanServer(),
              "com.sun.management:type=HotSpotDiagnostic",
              mxbeanClass);
      dumpHeapMethod = mxbeanClass.getMethod("dumpHeap", String.class, boolean.class);
    } catch (ClassNotFoundException | IOException | NoSuchMethodException e) {
      throw new Error(e);
    }
  }

  /**
   * Dumps a heap snapshot (of only the live objects) into a file.
   *
   * @param fileName file into which to dump a heap snapshot; is overwritten if it exists
   */
  public static void dumpHeap(String fileName) {
    dumpHeap(fileName, true);
  }

  /**
   * Dumps a heap snapshot into a file.
   *
   * @param fileName file into which to dump a heap snapshot; is overwritten if it exists
   * @param live if true, dump only the live objects
   */
  public static void dumpHeap(String fileName, boolean live) {
    initializeFields();
    File heapFile = new File(fileName);
    if (heapFile.exists()) {
      heapFile.delete();
    }
    try {
      // reflective way to do:  hotspotMBean.dumpHeap(fileName, live);
      dumpHeapMethod.invoke(hotspotMBean, fileName, live);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new Error(e);
    }
  }
}
