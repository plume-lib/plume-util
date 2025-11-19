package org.plumelib.util;


import java.util.BitSet;
import java.util.Random;
import org.checkerframework.checker.index.qual.NonNegative;

/** Test the UtilPlume class. */
final class UtilPlumeTest {

  UtilPlumeTest() {}

  // ///////////////////////////////////////////////////////////////////////////
  // Helper functions for testing
  //

  private static BitSet randomBitSet(@NonNegative int length, Random r) {
    BitSet result = new BitSet(length);
    for (int i = 0; i < length; i++) {
      result.set(i, r.nextBoolean());
    }
    return result;
  }

  // public static ArrayList randomElements(Iterator itor, int numElts)
  // public static ArrayList randomElements(Iterator itor, int numElts, Random random)

  // public static <T> @Nullable Integer incrementMap(Map<T,Integer> m, T key, int count) {
  // public static <K,V> String mapToString(Map<K,V> m) {
  // public static <K,V> void mapToString(Appendable sb, Map<K,V> m, String linePrefix) {
  // public static <K extends Comparable<? super K>,V> Collection<@KeyFor("#1") K>
  //     sortedKeySet(Map<K,V> m) {
  // public static <K,V> Collection<@KeyFor("#1") K>
  //     sortedKeySet(Map<K,V> m, Comparator<K> comparator) {

  // public static boolean gotBooleanProperty(Properties p, String key)
  // public static String appendProperty(Properties p, String key, String value)
  // public static String setDefault(Properties p, String key, String value)
  // public static void streamCopy(java.io.InputStream from, java.io.OutputStream to)

}
