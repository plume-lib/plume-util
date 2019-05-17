package org.plumelib.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Locale;
import java.util.Random;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class UtilPlumeTest {

  ///////////////////////////////////////////////////////////////////////////

  /// UtilPlume
  ///

  private static BitSet randomBitSet(@NonNegative int length, Random r) {
    BitSet result = new BitSet(length);
    for (int i = 0; i < length; i++) {
      result.set(i, r.nextBoolean());
    }
    return result;
  }

  // This cannot be static because it instantiates an inner class.
  @SuppressWarnings("ArrayEquals")
  @Test
  public void testUtilPlume() {

    // public static intersectionCardinalityAtLeast(BitSet a, BitSet b, int i)
    {
      Random r = new Random(20031008);
      for (int i = 0; i < 100; i++) {
        BitSet b1 = randomBitSet(r.nextInt(100), r);
        BitSet b2 = randomBitSet(r.nextInt(100), r);
        BitSet b3 = randomBitSet(r.nextInt(100), r);
        BitSet intersection = (BitSet) b1.clone();
        intersection.and(b2);
        int card = intersection.cardinality();
        for (int j = 0; j < 100; j++) {
          assert UtilPlume.intersectionCardinalityAtLeast(b1, b2, j) == (card >= j);
        }
        intersection.and(b3);
        card = intersection.cardinality();
        for (int j = 0; j < 100; j++) {
          assert UtilPlume.intersectionCardinalityAtLeast(b1, b2, b3, j) == (card >= j);
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

    try {
      assert UtilPlume.canCreateAndWrite(new File("TestPlume.java"));

      // This test fails if run by the superuser (who can overwrite
      // any file).
      if (!System.getProperty("user.name").equals("root")) {
        File readOnly = new File("temp");
        readOnly.createNewFile();
        readOnly.setReadOnly();
        assert !UtilPlume.canCreateAndWrite(readOnly);
        readOnly.delete();
      }

      assert UtilPlume.canCreateAndWrite(new File("temp"));
      assert !UtilPlume.canCreateAndWrite(new File("temp/temp"));
    } catch (IOException e) {
      e.printStackTrace();
      org.junit.Assert.fail("failure while testing UtilPlume.canCreateAndWrite(): " + e.toString());
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

    // public static String replaceString(String target, String oldStr, String newStr)

    assert UtilPlume.replaceString("hello dolly well hello dolly", " ", "  ")
        .equals("hello  dolly  well  hello  dolly");
    assert UtilPlume.replaceString("  hello  dolly well hello dolly  ", " ", "  ")
        .equals("    hello    dolly  well  hello  dolly    ");
    assert UtilPlume.replaceString("hello dolly well hello dolly", "ll", "y")
        .equals("heyo doyy wey heyo doyy");
    assert UtilPlume.replaceString("hello dolly well hello dolly", "q", "yyy")
        .equals("hello dolly well hello dolly");

    // public static String[] split(String s, char delim)
    // public static String[] split(String s, String delim)

    assert Arrays.equals(UtilPlume.split("foo,bar,baz", ','), new String[] {"foo", "bar", "baz"});
    assert Arrays.equals(UtilPlume.split("foo", ','), new String[] {"foo"});
    assert Arrays.equals(UtilPlume.split("", ','), new String[] {""});
    assert Arrays.equals(UtilPlume.split(",foo,", ','), new String[] {"", "foo", ""});
    assert Arrays.equals(UtilPlume.split("foo,bar,baz", ","), new String[] {"foo", "bar", "baz"});
    assert Arrays.equals(UtilPlume.split("foo", ","), new String[] {"foo"});
    assert Arrays.equals(UtilPlume.split("", ","), new String[] {""});
    assert Arrays.equals(UtilPlume.split(",foo,", ","), new String[] {"", "foo", ""});
    assert Arrays.equals(
        UtilPlume.split("foo, bar, baz", ", "), new String[] {"foo", "bar", "baz"});
    assert Arrays.equals(UtilPlume.split("foo", ", "), new String[] {"foo"});
    assert Arrays.equals(UtilPlume.split("", ", "), new String[] {""});
    assert Arrays.equals(UtilPlume.split(", foo, ", ", "), new String[] {"", "foo", ""});

    // public static String join(Object[] a, String delim)
    // public static String join(ArrayList v, String delim)

    assert UtilPlume.join(new String[] {"foo", "bar", "baz"}, ", ").equals("foo, bar, baz");
    assert UtilPlume.join(new String[] {"foo"}, ", ").equals("foo");
    assert UtilPlume.join(new String[] {}, ", ").equals("");
    assert UtilPlume.join(new Integer[] {0, 1, 2, 3, 4}, "").equals("01234");
    ArrayList<Object> potpourri = new ArrayList<>();
    potpourri.add("day");
    potpourri.add(2);
    potpourri.add("day");
    assert UtilPlume.join(potpourri, " ").equals("day 2 day");

    // public static String escapeNonJava(String orig)
    // public static String escapeNonJava(Character ch)

    assert UtilPlume.escapeNonJava("foobar").equals("foobar");
    assert UtilPlume.escapeNonJava("").equals("");
    assert UtilPlume.escapeNonJava("\\").equals("\\\\");
    assert UtilPlume.escapeNonJava("\\\n\r\"").equals("\\\\\\n\\r\\\"");
    assert UtilPlume.escapeNonJava("split\nlines").equals("split\\nlines");
    assert UtilPlume.escapeNonJava("\\relax").equals("\\\\relax");
    assert UtilPlume.escapeNonJava("\"hello\"").equals("\\\"hello\\\"");
    assert UtilPlume.escapeNonJava("\"hello\" \"world\"").equals("\\\"hello\\\" \\\"world\\\"");

    // public static String escapeNonASCII(String orig)

    assert UtilPlume.escapeNonASCII("foobar").equals("foobar");
    assert UtilPlume.escapeNonASCII("").equals("");
    assert UtilPlume.escapeNonASCII("\\").equals("\\\\");
    assert UtilPlume.escapeNonASCII("\\\n\r\"").equals("\\\\\\n\\r\\\"");
    assert UtilPlume.escapeNonASCII("split\nlines").equals("split\\nlines");
    assert UtilPlume.escapeNonASCII("\\relax").equals("\\\\relax");
    assert UtilPlume.escapeNonASCII("\"hello\"").equals("\\\"hello\\\"");
    assert UtilPlume.escapeNonASCII("\"hello\" \"world\"").equals("\\\"hello\\\" \\\"world\\\"");
    assert UtilPlume.escapeNonASCII("\0\1\2\7\12\70\100\111\222")
        .equals("\\000\\001\\002\\007\\n8@I\\222");
    assert UtilPlume.escapeNonASCII("\u0100\u1000\ucafe\uffff")
        .equals("\\u0100\\u1000\\ucafe\\uffff");

    // private static String escapeNonASCII(char c)

    // public static String unescapeNonJava(String orig)

    assert UtilPlume.unescapeNonJava("foobar").equals("foobar");
    assert UtilPlume.unescapeNonJava("").equals("");
    assert UtilPlume.unescapeNonJava("\\\\").equals("\\");
    assert UtilPlume.unescapeNonJava("\\\"").equals("\"");
    assert UtilPlume.unescapeNonJava("\\n").equals("\n"); // not lineSep
    assert UtilPlume.unescapeNonJava("\\r").equals("\r");
    assert UtilPlume.unescapeNonJava("split\\nlines").equals("split\nlines");
    assert UtilPlume.unescapeNonJava("\\\\\\n").equals("\\\n"); // not lineSep
    assert UtilPlume.unescapeNonJava("\\n\\r").equals("\n\r"); // not lineSep
    assert UtilPlume.unescapeNonJava("\\\\\\n\\r\\\"").equals("\\\n\r\"");
    assert UtilPlume.unescapeNonJava("\\\\relax").equals("\\relax");
    assert UtilPlume.unescapeNonJava("\\\"hello\\\"").equals("\"hello\"");
    assert UtilPlume.unescapeNonJava("\\\"hello\\\" \\\"world\\\"").equals("\"hello\" \"world\"");
    assert UtilPlume.unescapeNonJava("\\").equals("\\");
    assert UtilPlume.unescapeNonJava("foo\\").equals("foo\\");
    assert UtilPlume.unescapeNonJava("\\*abc").equals("*abc");
    assert UtilPlume.unescapeNonJava("\\101").equals("A");
    assert UtilPlume.unescapeNonJava("A\\102C").equals("ABC");
    // Should add more tests here.

    // Unfortunately, there isn't yet a unescapeNonASCII function.
    // If implemented, it should have the following behavior:
    // assert UtilPlume.unescapeNonASCII("\\115").equals("M");
    // assert UtilPlume.unescapeNonASCII("\\115\\111\\124").equals("MIT");

    // public static String removeWhitespaceAround(String arg, String delimiter)
    // public static String removeWhitespaceAfter(String arg, String delimiter)
    // public static String removeWhitespaceBefore(String arg, String delimiter)

    assert UtilPlume.removeWhitespaceBefore("a,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceBefore("a, b", ",").equals("a, b");
    assert UtilPlume.removeWhitespaceBefore("a ,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceBefore("a , b", ",").equals("a, b");
    assert UtilPlume.removeWhitespaceBefore("ab=>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceBefore("ab=> cd", "=>").equals("ab=> cd");
    assert UtilPlume.removeWhitespaceBefore("ab =>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceBefore("ab => cd", "=>").equals("ab=> cd");
    assert UtilPlume.removeWhitespaceBefore("123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceBefore(" 123 cd", "123").equals("123 cd");
    assert UtilPlume.removeWhitespaceBefore(" 123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceBefore("123 cd", "123").equals("123 cd");
    assert UtilPlume.removeWhitespaceBefore("cd123", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceBefore("cd 123 ", "123").equals("cd123 ");
    assert UtilPlume.removeWhitespaceBefore("cd123 ", "123").equals("cd123 ");
    assert UtilPlume.removeWhitespaceBefore("cd 123", "123").equals("cd123");

    assert UtilPlume.removeWhitespaceAfter("a,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAfter("a, b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAfter("a ,b", ",").equals("a ,b");
    assert UtilPlume.removeWhitespaceAfter("a , b", ",").equals("a ,b");
    assert UtilPlume.removeWhitespaceAfter("ab=>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAfter("ab=> cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAfter("ab =>cd", "=>").equals("ab =>cd");
    assert UtilPlume.removeWhitespaceAfter("ab => cd", "=>").equals("ab =>cd");
    assert UtilPlume.removeWhitespaceAfter("123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAfter(" 123 cd", "123").equals(" 123cd");
    assert UtilPlume.removeWhitespaceAfter(" 123cd", "123").equals(" 123cd");
    assert UtilPlume.removeWhitespaceAfter("123 cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAfter("cd123", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAfter("cd 123 ", "123").equals("cd 123");
    assert UtilPlume.removeWhitespaceAfter("cd123 ", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAfter("cd 123", "123").equals("cd 123");

    assert UtilPlume.removeWhitespaceAround("a,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAround("a, b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAround("a ,b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAround("a , b", ",").equals("a,b");
    assert UtilPlume.removeWhitespaceAround("ab=>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAround("ab=> cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAround("ab =>cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAround("ab => cd", "=>").equals("ab=>cd");
    assert UtilPlume.removeWhitespaceAround("123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAround(" 123 cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAround(" 123cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAround("123 cd", "123").equals("123cd");
    assert UtilPlume.removeWhitespaceAround("cd123", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAround("cd 123 ", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAround("cd123 ", "123").equals("cd123");
    assert UtilPlume.removeWhitespaceAround("cd 123", "123").equals("cd123");

    // public static String nplural(int n, String noun)

    assert UtilPlume.nplural(0, "fuss").equals("0 fusses");
    assert UtilPlume.nplural(1, "fuss").equals("1 fuss");
    assert UtilPlume.nplural(2, "fuss").equals("2 fusses");
    assert UtilPlume.nplural(0, "fox").equals("0 foxes");
    assert UtilPlume.nplural(1, "fox").equals("1 fox");
    assert UtilPlume.nplural(2, "fox").equals("2 foxes");
    assert UtilPlume.nplural(0, "fish").equals("0 fishes");
    assert UtilPlume.nplural(1, "fish").equals("1 fish");
    assert UtilPlume.nplural(2, "fish").equals("2 fishes");
    assert UtilPlume.nplural(0, "fletch").equals("0 fletches");
    assert UtilPlume.nplural(1, "fletch").equals("1 fletch");
    assert UtilPlume.nplural(2, "fletch").equals("2 fletches");
    assert UtilPlume.nplural(0, "fund").equals("0 funds");
    assert UtilPlume.nplural(1, "fund").equals("1 fund");
    assert UtilPlume.nplural(2, "fund").equals("2 funds");
    assert UtilPlume.nplural(0, "f-stop").equals("0 f-stops");
    assert UtilPlume.nplural(1, "f-stop").equals("1 f-stop");
    assert UtilPlume.nplural(2, "f-stop").equals("2 f-stops");

    // public static String rpad(String s, int length)
    // public static String rpad(int num, int length)
    // public static String rpad(double num, int length)

    assert UtilPlume.rpad("", 5).equals("     ");
    assert UtilPlume.rpad("abcd", 5).equals("abcd ");
    assert UtilPlume.rpad("abcde", 5).equals("abcde");
    assert UtilPlume.rpad("abcdef", 5).equals("abcde");
    assert UtilPlume.rpad("abcde ghij", 5).equals("abcde");
    assert UtilPlume.rpad(10, 5).equals("10   ");
    assert UtilPlume.rpad(3.14, 5).equals("3.14 ");

    // public static class NullableStringComparator
    //   public int compare(Object o1, Object o2)

    // public static int count(String s, int ch)
    // public static int count(String s, String sub)

    assert UtilPlume.count("abcde", 'a') == 1;
    assert UtilPlume.count("abcde", 'c') == 1;
    assert UtilPlume.count("abcde", 'e') == 1;
    assert UtilPlume.count("abcde", 'z') == 0;
    assert UtilPlume.count("abacadaea", 'a') == 5;
    assert UtilPlume.count("aaa aea", 'a') == 5;
    assert UtilPlume.count("daeaaa", 'a') == 4;

    // This will be easy to write tests for, when I get around to it.
    // public static ArrayList tokens(String str, String delim, boolean returnTokens)
    // public static ArrayList tokens(String str, String delim)
    // public static ArrayList tokens(String str)

    // This is tested by the tokens methods.
    // public static ArrayList makeArrayList(Enumeration e)

    Locale.setDefault(Locale.US);
    assert UtilPlume.abbreviateNumber(5).equals("5.00");
    assert UtilPlume.abbreviateNumber(5000).equals("5.00K");
    assert UtilPlume.abbreviateNumber(5000000).equals("5.00M");
    assert UtilPlume.abbreviateNumber(1000000000).equals("1.00G");
    assert UtilPlume.abbreviateNumber(1).equals("1.00");
    assert UtilPlume.abbreviateNumber(12).equals("12.0");
    assert UtilPlume.abbreviateNumber(123).equals("123");
    assert UtilPlume.abbreviateNumber(1234).equals("1.23K");
    assert UtilPlume.abbreviateNumber(12345).equals("12.3K");
    assert UtilPlume.abbreviateNumber(123456).equals("123K");
    assert UtilPlume.abbreviateNumber(1234567).equals("1.23M");
    assert UtilPlume.abbreviateNumber(12345678).equals("12.3M");
    assert UtilPlume.abbreviateNumber(123456789).equals("123M");
    assert UtilPlume.abbreviateNumber(1234567890).equals("1.23G");
    assert UtilPlume.abbreviateNumber(9).equals("9.00");
    assert UtilPlume.abbreviateNumber(98).equals("98.0");
    assert UtilPlume.abbreviateNumber(987).equals("987");
    assert UtilPlume.abbreviateNumber(9876).equals("9.88K");
    assert UtilPlume.abbreviateNumber(98765).equals("98.8K");
    assert UtilPlume.abbreviateNumber(987654).equals("988K");
    assert UtilPlume.abbreviateNumber(9876543).equals("9.88M");
    assert UtilPlume.abbreviateNumber(98765432).equals("98.8M");
    assert UtilPlume.abbreviateNumber(987654321).equals("988M");
    assert UtilPlume.abbreviateNumber(9876543210L).equals("9.88G");
  }

  @Test
  public void testSplitLines() {

    String str = "one\ntwo\n\rthree\r\nfour\rfive\n\n\nsix\r\n\r\n\r\n";
    @SuppressWarnings("value") // method that returns an array is not StaticallyExecutable
    String @ArrayLen(11) [] sa = UtilPlume.splitLines(str);
    // for (String s : sa)
    //   System.out.printf ("'%s'%n", s);
    assert sa.length == 11;
    assert sa[0].equals("one");
    assert sa[1].equals("two");
    assert sa[2].equals("three");
    assert sa[3].equals("four");
    assert sa[4].equals("five");
    assert sa[5].equals("");
    assert sa[6].equals("");
    assert sa[7].equals("six");
    assert sa[8].equals("");
    assert sa[9].equals("");
    assert sa[10].equals("");
  }
}
