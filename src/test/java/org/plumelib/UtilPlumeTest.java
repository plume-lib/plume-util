package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Locale;
import java.util.Random;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.common.value.qual.ArrayLen;
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

  @SuppressWarnings("deprecation")
  @Test
  public void test_replaceString() {

    // public static String replaceString(String target, String oldStr, String newStr)

    assert UtilPlume.replaceString("hello dolly well hello dolly", " ", "  ")
        .equals("hello  dolly  well  hello  dolly");
    assert UtilPlume.replaceString("  hello  dolly well hello dolly  ", " ", "  ")
        .equals("    hello    dolly  well  hello  dolly    ");
    assert UtilPlume.replaceString("hello dolly well hello dolly", "ll", "y")
        .equals("heyo doyy wey heyo doyy");
    assert UtilPlume.replaceString("hello dolly well hello dolly", "q", "yyy")
        .equals("hello dolly well hello dolly");
  }

  @Test
  public void test_replacePrefix() {

    // public static String replacePrefix(String target, String oldStr, String newStr)

    assert UtilPlume.replacePrefix("abcdefg", "abc", "hijk").equals("hijkdefg");
    assert UtilPlume.replacePrefix("abcdefg", "bcd", "hijk").equals("abcdefg");
    assert UtilPlume.replacePrefix("abcdefg", "abc", "").equals("defg");
    assert UtilPlume.replacePrefix("abcdefg", "bcd", "").equals("abcdefg");
  }

  @Test
  public void test_replaceSuffix() {

    // public static String replaceSuffix(String target, String oldStr, String newStr)

    assert UtilPlume.replaceSuffix("abcdefg", "defg", "hijk").equals("abchijk");
    assert UtilPlume.replaceSuffix("abcdefg", "cdef", "hijk").equals("abcdefg");
    assert UtilPlume.replaceSuffix("abcdefg", "defg", "").equals("abc");
    assert UtilPlume.replaceSuffix("abcdefg", "cdef", "").equals("abcdefg");
  }

  @Test
  public void test_prefixLines() {

    // public static String prefixLines(String prefix, String s) {

    assertEquals(
        UtilPlume.joinLines("  1", "  2", "  3"),
        UtilPlume.prefixLines("  ", UtilPlume.joinLines("1", "2", "3")));
    assertEquals(
        UtilPlume.joinLines("  ", "  1", "  ", "  2", "  "),
        UtilPlume.prefixLines("  ", UtilPlume.joinLines("", "1", "", "2", "")));
  }

  @Test
  public void test_indentLines() {

    // public static String indentLines(int indent, String s) {

    assertEquals(
        UtilPlume.prefixLines("  ", UtilPlume.joinLines("1", "2", "3")),
        UtilPlume.indentLines(2, UtilPlume.joinLines("1", "2", "3")));
    assertEquals(
        UtilPlume.prefixLines("  ", UtilPlume.joinLines("", "1", "", "2", "")),
        UtilPlume.indentLines(2, UtilPlume.joinLines("", "1", "", "2", "")));
  }

  @Test
  public void test_split() {

    // public static String[] split(String s, char delim)
    // public static String[] split(String s, String delim)

    assertTrue(
        Arrays.equals(UtilPlume.split("foo,bar,baz", ','), new String[] {"foo", "bar", "baz"}));
    assertTrue(Arrays.equals(UtilPlume.split("foo", ','), new String[] {"foo"}));
    assertTrue(Arrays.equals(UtilPlume.split("", ','), new String[] {""}));
    assertTrue(Arrays.equals(UtilPlume.split(",foo,", ','), new String[] {"", "foo", ""}));
    assertTrue(
        Arrays.equals(UtilPlume.split("foo,bar,baz", ","), new String[] {"foo", "bar", "baz"}));
    assertTrue(Arrays.equals(UtilPlume.split("foo", ","), new String[] {"foo"}));
    assertTrue(Arrays.equals(UtilPlume.split("", ","), new String[] {""}));
    assertTrue(Arrays.equals(UtilPlume.split(",foo,", ","), new String[] {"", "foo", ""}));
    assert Arrays.equals(
        UtilPlume.split("foo, bar, baz", ", "), new String[] {"foo", "bar", "baz"});
    assertTrue(Arrays.equals(UtilPlume.split("foo", ", "), new String[] {"foo"}));
    assertTrue(Arrays.equals(UtilPlume.split("", ", "), new String[] {""}));
    assertTrue(Arrays.equals(UtilPlume.split(", foo, ", ", "), new String[] {"", "foo", ""}));
  }

  @Test
  public void test_join() {

    // public static String join(Object[] a, String delim)
    // public static String join(ArrayList v, String delim)

    assertTrue(UtilPlume.join(", ", new String[] {"foo", "bar", "baz"}).equals("foo, bar, baz"));
    assertTrue(UtilPlume.join(", ", "foo", "bar", "baz").equals("foo, bar, baz"));
    assertTrue(UtilPlume.join(", ", new String[] {"foo"}).equals("foo"));
    assertTrue(UtilPlume.join(", ", "foo").equals("foo"));
    assertTrue(UtilPlume.join(", ", new String[] {}).equals(""));
    assertTrue(UtilPlume.join(", ").equals(""));
    assertTrue(UtilPlume.join("", new Integer[] {0, 1, 2, 3, 4}).equals("01234"));
    assertTrue(UtilPlume.join("", 0, 1, 2, 3, 4).equals("01234"));
    ArrayList<Object> potpourri = new ArrayList<>();
    potpourri.add("day");
    potpourri.add(2);
    potpourri.add("day");
    assertEquals("day 2 day", UtilPlume.join(" ", potpourri));
  }

  private void oneEscapeJava(String s, String escaped) {
    assertEquals(escaped, UtilPlume.escapeJava(s));
    assertEquals(s, UtilPlume.unescapeJava(escaped));
  }

  @Test
  public void test_escapeJava() {

    // public static String escapeJava(String orig)
    // public static String escapeJava(char c)
    // public static String unescapeJava(String orig)
    // public static String unescapeJava(char c)

    oneEscapeJava("foobar", "foobar");
    oneEscapeJava("", "");
    oneEscapeJava("\\", "\\\\");
    oneEscapeJava("\"", "\\\"");
    oneEscapeJava("\n", "\\n");
    oneEscapeJava("\r", "\\r");
    oneEscapeJava("\\\n", "\\\\\\n");
    oneEscapeJava("\n\r", "\\n\\r");
    oneEscapeJava("\\\n\r\"", "\\\\\\n\\r\\\"");
    oneEscapeJava("split\nlines", "split\\nlines");
    oneEscapeJava("\\relax", "\\\\relax");
    oneEscapeJava("\"hello\"", "\\\"hello\\\"");
    oneEscapeJava("\"hello\" \"world\"", "\\\"hello\\\" \\\"world\\\"");
    oneEscapeJava("foo\\", "foo\\\\");
    oneEscapeJava("foo\0bar", "foo\\000bar");
    oneEscapeJava("foo\tbar", "foo\\tbar");
    oneEscapeJava("\b\f\n\r\t\1\377", "\\b\\f\\n\\r\\t\\001\\377");
    oneEscapeJava("\222", "\\222");
    oneEscapeJava("\300", "\\300");
    oneEscapeJava("\u12345", "\\u12345");
    oneEscapeJava("\u1234A", "\\u1234A");
    oneEscapeJava("\u54321", "\\u54321");
    oneEscapeJava("\u5432A", "\\u5432A");
    // Should add more tests here.

    // These tests are not symmetric because the argument is not a value that escapeJava would ever
    // return.
    assertTrue(UtilPlume.unescapeJava("\\").equals("\\"));
    assertTrue(UtilPlume.unescapeJava("foo\\").equals("foo\\"));
    assertTrue(UtilPlume.unescapeJava("\\*abc").equals("*abc"));
    assertTrue(UtilPlume.unescapeJava("\\101").equals("A"));
    assertTrue(UtilPlume.unescapeJava("A\\102C").equals("ABC"));

    assertEquals("(1", UtilPlume.unescapeJava("\0501"));
    assertEquals("(1", UtilPlume.unescapeJava("\501"));
    assertEquals("?7", UtilPlume.unescapeJava("\0777")); // '?' = \077
    assertEquals("?7", UtilPlume.unescapeJava("\777")); // '?' = \077
    assertEquals(" M ", UtilPlume.unescapeJava(" \uuu004D "));

    // public static String escapeNonASCII(String orig)

    assertTrue(UtilPlume.escapeNonASCII("foobar").equals("foobar"));
    assertTrue(UtilPlume.escapeNonASCII("").equals(""));
    assertTrue(UtilPlume.escapeNonASCII("\\").equals("\\\\"));
    assertTrue(UtilPlume.escapeNonASCII("\\\n\r\"").equals("\\\\\\n\\r\\\""));
    assertTrue(UtilPlume.escapeNonASCII("split\nlines").equals("split\\nlines"));
    assertTrue(UtilPlume.escapeNonASCII("\\relax").equals("\\\\relax"));
    assertTrue(UtilPlume.escapeNonASCII("\"hello\"").equals("\\\"hello\\\""));
    assertTrue(
        UtilPlume.escapeNonASCII("\"hello\" \"world\"").equals("\\\"hello\\\" \\\"world\\\""));
    assert UtilPlume.escapeNonASCII("\0\1\2\7\12\70\100\111\222")
        .equals("\\000\\001\\002\\007\\n8@I\\222");
    assert UtilPlume.escapeNonASCII("\u0100\u1000\ucafe\uffff")
        .equals("\\u0100\\u1000\\ucafe\\uffff");

    // private static String escapeNonASCII(char c)

    // Unfortunately, there isn't yet a unescapeNonASCII function.
    // If implemented, it should have the following behavior:
    // assertTrue(UtilPlume.unescapeNonASCII("\\115").equals("M"));
    // assertTrue(UtilPlume.unescapeNonASCII("\\115\\111\\124").equals("MIT"));
  }

  @Test
  public void test_removeWhitespace() {

    // public static String removeWhitespaceAround(String arg, String delimiter)
    // public static String removeWhitespaceAfter(String arg, String delimiter)
    // public static String removeWhitespaceBefore(String arg, String delimiter)

    assertTrue(UtilPlume.removeWhitespaceBefore("a,b", ",").equals("a,b"));
    assertTrue(UtilPlume.removeWhitespaceBefore("a, b", ",").equals("a, b"));
    assertTrue(UtilPlume.removeWhitespaceBefore("a ,b", ",").equals("a,b"));
    assertTrue(UtilPlume.removeWhitespaceBefore("a , b", ",").equals("a, b"));
    assertTrue(UtilPlume.removeWhitespaceBefore("ab=>cd", "=>").equals("ab=>cd"));
    assertTrue(UtilPlume.removeWhitespaceBefore("ab=> cd", "=>").equals("ab=> cd"));
    assertTrue(UtilPlume.removeWhitespaceBefore("ab =>cd", "=>").equals("ab=>cd"));
    assertTrue(UtilPlume.removeWhitespaceBefore("ab => cd", "=>").equals("ab=> cd"));
    assertTrue(UtilPlume.removeWhitespaceBefore("123cd", "123").equals("123cd"));
    assertTrue(UtilPlume.removeWhitespaceBefore(" 123 cd", "123").equals("123 cd"));
    assertTrue(UtilPlume.removeWhitespaceBefore(" 123cd", "123").equals("123cd"));
    assertTrue(UtilPlume.removeWhitespaceBefore("123 cd", "123").equals("123 cd"));
    assertTrue(UtilPlume.removeWhitespaceBefore("cd123", "123").equals("cd123"));
    assertTrue(UtilPlume.removeWhitespaceBefore("cd 123 ", "123").equals("cd123 "));
    assertTrue(UtilPlume.removeWhitespaceBefore("cd123 ", "123").equals("cd123 "));
    assertTrue(UtilPlume.removeWhitespaceBefore("cd 123", "123").equals("cd123"));

    assertTrue(UtilPlume.removeWhitespaceAfter("a,b", ",").equals("a,b"));
    assertTrue(UtilPlume.removeWhitespaceAfter("a, b", ",").equals("a,b"));
    assertTrue(UtilPlume.removeWhitespaceAfter("a ,b", ",").equals("a ,b"));
    assertTrue(UtilPlume.removeWhitespaceAfter("a , b", ",").equals("a ,b"));
    assertTrue(UtilPlume.removeWhitespaceAfter("ab=>cd", "=>").equals("ab=>cd"));
    assertTrue(UtilPlume.removeWhitespaceAfter("ab=> cd", "=>").equals("ab=>cd"));
    assertTrue(UtilPlume.removeWhitespaceAfter("ab =>cd", "=>").equals("ab =>cd"));
    assertTrue(UtilPlume.removeWhitespaceAfter("ab => cd", "=>").equals("ab =>cd"));
    assertTrue(UtilPlume.removeWhitespaceAfter("123cd", "123").equals("123cd"));
    assertTrue(UtilPlume.removeWhitespaceAfter(" 123 cd", "123").equals(" 123cd"));
    assertTrue(UtilPlume.removeWhitespaceAfter(" 123cd", "123").equals(" 123cd"));
    assertTrue(UtilPlume.removeWhitespaceAfter("123 cd", "123").equals("123cd"));
    assertTrue(UtilPlume.removeWhitespaceAfter("cd123", "123").equals("cd123"));
    assertTrue(UtilPlume.removeWhitespaceAfter("cd 123 ", "123").equals("cd 123"));
    assertTrue(UtilPlume.removeWhitespaceAfter("cd123 ", "123").equals("cd123"));
    assertTrue(UtilPlume.removeWhitespaceAfter("cd 123", "123").equals("cd 123"));

    assertTrue(UtilPlume.removeWhitespaceAround("a,b", ",").equals("a,b"));
    assertTrue(UtilPlume.removeWhitespaceAround("a, b", ",").equals("a,b"));
    assertTrue(UtilPlume.removeWhitespaceAround("a ,b", ",").equals("a,b"));
    assertTrue(UtilPlume.removeWhitespaceAround("a , b", ",").equals("a,b"));
    assertTrue(UtilPlume.removeWhitespaceAround("ab=>cd", "=>").equals("ab=>cd"));
    assertTrue(UtilPlume.removeWhitespaceAround("ab=> cd", "=>").equals("ab=>cd"));
    assertTrue(UtilPlume.removeWhitespaceAround("ab =>cd", "=>").equals("ab=>cd"));
    assertTrue(UtilPlume.removeWhitespaceAround("ab => cd", "=>").equals("ab=>cd"));
    assertTrue(UtilPlume.removeWhitespaceAround("123cd", "123").equals("123cd"));
    assertTrue(UtilPlume.removeWhitespaceAround(" 123 cd", "123").equals("123cd"));
    assertTrue(UtilPlume.removeWhitespaceAround(" 123cd", "123").equals("123cd"));
    assertTrue(UtilPlume.removeWhitespaceAround("123 cd", "123").equals("123cd"));
    assertTrue(UtilPlume.removeWhitespaceAround("cd123", "123").equals("cd123"));
    assertTrue(UtilPlume.removeWhitespaceAround("cd 123 ", "123").equals("cd123"));
    assertTrue(UtilPlume.removeWhitespaceAround("cd123 ", "123").equals("cd123"));
    assertTrue(UtilPlume.removeWhitespaceAround("cd 123", "123").equals("cd123"));
  }

  @Test
  public void test_nplural() {

    // public static String nplural(int n, String noun)

    assertTrue(UtilPlume.nplural(0, "fuss").equals("0 fusses"));
    assertTrue(UtilPlume.nplural(1, "fuss").equals("1 fuss"));
    assertTrue(UtilPlume.nplural(2, "fuss").equals("2 fusses"));
    assertTrue(UtilPlume.nplural(0, "fox").equals("0 foxes"));
    assertTrue(UtilPlume.nplural(1, "fox").equals("1 fox"));
    assertTrue(UtilPlume.nplural(2, "fox").equals("2 foxes"));
    assertTrue(UtilPlume.nplural(0, "fish").equals("0 fishes"));
    assertTrue(UtilPlume.nplural(1, "fish").equals("1 fish"));
    assertTrue(UtilPlume.nplural(2, "fish").equals("2 fishes"));
    assertTrue(UtilPlume.nplural(0, "fletch").equals("0 fletches"));
    assertTrue(UtilPlume.nplural(1, "fletch").equals("1 fletch"));
    assertTrue(UtilPlume.nplural(2, "fletch").equals("2 fletches"));
    assertTrue(UtilPlume.nplural(0, "fund").equals("0 funds"));
    assertTrue(UtilPlume.nplural(1, "fund").equals("1 fund"));
    assertTrue(UtilPlume.nplural(2, "fund").equals("2 funds"));
    assertTrue(UtilPlume.nplural(0, "f-stop").equals("0 f-stops"));
    assertTrue(UtilPlume.nplural(1, "f-stop").equals("1 f-stop"));
    assertTrue(UtilPlume.nplural(2, "f-stop").equals("2 f-stops"));
  }

  @Test
  public void test_rpad() {

    // public static String rpad(String s, int length)
    // public static String rpad(int num, int length)
    // public static String rpad(double num, int length)

    assertTrue(UtilPlume.rpad("", 5).equals("     "));
    assertTrue(UtilPlume.rpad("abcd", 5).equals("abcd "));
    assertTrue(UtilPlume.rpad("abcde", 5).equals("abcde"));
    assertTrue(UtilPlume.rpad("abcdef", 5).equals("abcde"));
    assertTrue(UtilPlume.rpad("abcde ghij", 5).equals("abcde"));
    assertTrue(UtilPlume.rpad(10, 5).equals("10   "));
    assertTrue(UtilPlume.rpad(3.14, 5).equals("3.14 "));

    // public static class NullableStringComparator
    //   public int compare(Object o1, Object o2)

  }

  @Test
  public void test_count() {

    // public static int count(String s, int ch)
    // public static int count(String s, String sub)

    assertTrue(UtilPlume.count("abcde", 'a') == 1);
    assertTrue(UtilPlume.count("abcde", 'c') == 1);
    assertTrue(UtilPlume.count("abcde", 'e') == 1);
    assertTrue(UtilPlume.count("abcde", 'z') == 0);
    assertTrue(UtilPlume.count("abacadaea", 'a') == 5);
    assertTrue(UtilPlume.count("aaa aea", 'a') == 5);
    assertTrue(UtilPlume.count("daeaaa", 'a') == 4);
  }

  // This will be easy to write tests for, when I get around to it.
  // public static ArrayList tokens(String str, String delim, boolean returnTokens)
  // public static ArrayList tokens(String str, String delim)
  // public static ArrayList tokens(String str)

  // This is tested by the tokens methods.
  // public static ArrayList makeArrayList(Enumeration e)

  @Test
  public void test_abbreviateNumber() {

    Locale.setDefault(Locale.US);
    assertTrue(UtilPlume.abbreviateNumber(5).equals("5.00"));
    assertTrue(UtilPlume.abbreviateNumber(5000).equals("5.00K"));
    assertTrue(UtilPlume.abbreviateNumber(5000000).equals("5.00M"));
    assertTrue(UtilPlume.abbreviateNumber(1000000000).equals("1.00G"));
    assertTrue(UtilPlume.abbreviateNumber(1).equals("1.00"));
    assertTrue(UtilPlume.abbreviateNumber(12).equals("12.0"));
    assertTrue(UtilPlume.abbreviateNumber(123).equals("123"));
    assertTrue(UtilPlume.abbreviateNumber(1234).equals("1.23K"));
    assertTrue(UtilPlume.abbreviateNumber(12345).equals("12.3K"));
    assertTrue(UtilPlume.abbreviateNumber(123456).equals("123K"));
    assertTrue(UtilPlume.abbreviateNumber(1234567).equals("1.23M"));
    assertTrue(UtilPlume.abbreviateNumber(12345678).equals("12.3M"));
    assertTrue(UtilPlume.abbreviateNumber(123456789).equals("123M"));
    assertTrue(UtilPlume.abbreviateNumber(1234567890).equals("1.23G"));
    assertTrue(UtilPlume.abbreviateNumber(9).equals("9.00"));
    assertTrue(UtilPlume.abbreviateNumber(98).equals("98.0"));
    assertTrue(UtilPlume.abbreviateNumber(987).equals("987"));
    assertTrue(UtilPlume.abbreviateNumber(9876).equals("9.88K"));
    assertTrue(UtilPlume.abbreviateNumber(98765).equals("98.8K"));
    assertTrue(UtilPlume.abbreviateNumber(987654).equals("988K"));
    assertTrue(UtilPlume.abbreviateNumber(9876543).equals("9.88M"));
    assertTrue(UtilPlume.abbreviateNumber(98765432).equals("98.8M"));
    assertTrue(UtilPlume.abbreviateNumber(987654321).equals("988M"));
    assertTrue(UtilPlume.abbreviateNumber(9876543210L).equals("9.88G"));
  }

  @Test
  public void testCountFormatArguments() {
    assertEquals(0, UtilPlume.countFormatArguments("No specifiier."));
    assertEquals(0, UtilPlume.countFormatArguments("This is 100%"));
    assertEquals(0, UtilPlume.countFormatArguments("This is 100%% excellent."));
    assertEquals(0, UtilPlume.countFormatArguments("Newline%n is not%na specifier."));
    assertEquals(1, UtilPlume.countFormatArguments("This is my %s"));
    assertEquals(1, UtilPlume.countFormatArguments("This is my %s."));
    assertEquals(2, UtilPlume.countFormatArguments("Two %d and %d"));
    assertEquals(3, UtilPlume.countFormatArguments("%f and %s and %d makes three"));
    assertEquals(
        3, UtilPlume.countFormatArguments("Hi! My name is %s and I have %d dogs and a %d cats."));

    assertEquals(2, UtilPlume.countFormatArguments("%f and %1$f and %d and %1$f makes two"));
    assertEquals(14, UtilPlume.countFormatArguments("%f and %14$f makes fourteen"));
  }

  @Test
  public void testSplitLines() {

    String str = "one\ntwo\n\rthree\r\nfour\rfive\n\n\nsix\r\n\r\n\r\n";
    @SuppressWarnings("value") // method that returns an array is not StaticallyExecutable
    String @ArrayLen(11) [] sa = UtilPlume.splitLines(str);
    // for (String s : sa)
    //   System.out.printf ("'%s'%n", s);
    assertTrue(sa.length == 11);
    assertTrue(sa[0].equals("one"));
    assertTrue(sa[1].equals("two"));
    assertTrue(sa[2].equals("three"));
    assertTrue(sa[3].equals("four"));
    assertTrue(sa[4].equals("five"));
    assertTrue(sa[5].equals(""));
    assertTrue(sa[6].equals(""));
    assertTrue(sa[7].equals("six"));
    assertTrue(sa[8].equals(""));
    assertTrue(sa[9].equals(""));
    assertTrue(sa[10].equals(""));
  }
}
