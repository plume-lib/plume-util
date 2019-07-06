import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Defines a method {@link #dumpHeap} that dumps the heap to an .hprof file. */
public class DumpHeap {

  /**
   * The HotSpot Diagnostic MBean. Its type is Object, in case HotSpotDiagnosticMXBean is not
   * available at compile time.
   */
  private static volatile Object hotspotMBean;

  /** The method com.sun.management.HotSpotDiagnosticMXBean#dumpHeap. */
  private static Method dumpHeapMethod;

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
   * @param fileName file into which to dump a heap snapshot
   */
  static void dumpHeap(String fileName) {
    dumpHeap(fileName, true);
  }

  /**
   * Dumps a heap snapshot into a file.
   *
   * @param fileName file into which to dump a heap snapshot
   * @param live if true, dump only the live objects
   */
  static void dumpHeap(String fileName, boolean live) {
    initializeFields();
    try {
      // reflective way to do:  hotspotMBean.dumpHeap(fileName, live);
      dumpHeapMethod.invoke(hotspotMBean, fileName, live);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new Error(e);
    }
  }
}
