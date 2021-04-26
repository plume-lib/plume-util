package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;
import java.util.Random;
import org.checkerframework.checker.index.qual.NonNegative;
import org.junit.jupiter.api.Test;

public final class UtilPlumeTest {

  private static BitSet randomBitSet(@NonNegative int length, Random r) {
    BitSet result = new BitSet(length);
    for (int i = 0; i < length; i++) {
      result.set(i, r.nextBoolean());
    }
    return result;
  }

  @Test
  public void test_intersectionCardinalityAtLeast() {

    // public static intersectionCardinalityAtLeast(BitSet a, BitSet b, int i)

    Random r = new Random(20031008);
    for (int i = 0; i < 100; i++) {
      BitSet b1 = randomBitSet(r.nextInt(100), r);
      BitSet b2 = randomBitSet(r.nextInt(100), r);
      BitSet b3 = randomBitSet(r.nextInt(100), r);
      BitSet intersection = (BitSet) b1.clone();
      intersection.and(b2);
      int card = intersection.cardinality();
      for (int j = 0; j < 100; j++) {
        assertTrue(CollectionsPlume.intersectionCardinalityAtLeast(b1, b2, j) == (card >= j));
      }
      intersection.and(b3);
      card = intersection.cardinality();
      for (int j = 0; j < 100; j++) {
        assertTrue(CollectionsPlume.intersectionCardinalityAtLeast(b1, b2, b3, j) == (card >= j));
      }
    }
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
