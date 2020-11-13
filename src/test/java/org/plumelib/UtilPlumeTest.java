package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Random;
import org.checkerframework.checker.index.qual.NonNegative;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
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
        assertTrue(UtilPlume.intersectionCardinalityAtLeast(b1, b2, j) == (card >= j));
      }
      intersection.and(b3);
      card = intersection.cardinality();
      for (int j = 0; j < 100; j++) {
        assertTrue(UtilPlume.intersectionCardinalityAtLeast(b1, b2, b3, j) == (card >= j));
      }
    }
  }

  // public static BufferedReader bufferedFileReader(String filename)
  // public static LineNumberReader lineNumberFileReader(String filename)
  // public static BufferedWriter bufferedFileWriter(String filename) throws IOException
  // public static Class classForName(String className)

  // public static void addToClasspath(String dir)
  // public static final class WildcardFilter implements FilenameFilter
  //   public WildcardFilter(String filename)
  //   public boolean accept(File dir, String name)
  // public static boolean canCreateAndWrite(File file)
  // public static void writeObject(Object o, File file) throws IOException
  // public static Object readObject(File file)
  // public static File createTempDir(String prefix, String suffix)

  // public Object incrementHashMap(HashMap hm, Object key, int count)

  @Test
  public void test_canCreateAndWrite() {

    try {
      assertTrue(UtilPlume.canCreateAndWrite(new File("TestPlume.java")));

      // This test fails if run by the superuser (who can overwrite
      // any file).
      if (!System.getProperty("user.name").equals("root")) {
        File readOnly = new File("temp");
        readOnly.createNewFile();
        readOnly.setReadOnly();
        assertTrue(!UtilPlume.canCreateAndWrite(readOnly));
        readOnly.delete();
      }

      assertTrue(UtilPlume.canCreateAndWrite(new File("temp")));
      assertTrue(!UtilPlume.canCreateAndWrite(new File("temp/temp")));
    } catch (IOException e) {
      e.printStackTrace();
      fail("failure while testing UtilPlume.canCreateAndWrite(): " + e.toString());
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

  // public static boolean propertyIsTrue(Properties p, String key)
  // public static String appendProperty(Properties p, String key, String value)
  // public static String setDefault(Properties p, String key, String value)
  // public static void streamCopy(java.io.InputStream from, java.io.OutputStream to)

}
