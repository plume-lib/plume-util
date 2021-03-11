package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class StringsPlumeTest {

  @Test
  public void test_replacePrefix() {

    // public static String replacePrefix(String target, String oldStr, String newStr)

    assert StringsPlume.replacePrefix("abcdefg", "abc", "hijk").equals("hijkdefg");
    assert StringsPlume.replacePrefix("abcdefg", "bcd", "hijk").equals("abcdefg");
    assert StringsPlume.replacePrefix("abcdefg", "abc", "").equals("defg");
    assert StringsPlume.replacePrefix("abcdefg", "bcd", "").equals("abcdefg");
  }

  @Test
  public void test_replaceSuffix() {

    // public static String replaceSuffix(String target, String oldStr, String newStr)

    assert StringsPlume.replaceSuffix("abcdefg", "defg", "hijk").equals("abchijk");
    assert StringsPlume.replaceSuffix("abcdefg", "cdef", "hijk").equals("abcdefg");
    assert StringsPlume.replaceSuffix("abcdefg", "defg", "").equals("abc");
    assert StringsPlume.replaceSuffix("abcdefg", "cdef", "").equals("abcdefg");
  }

  @Test
  public void test_prefixLines() {

    // public static String prefixLines(String prefix, String s) {

    assertEquals(
        StringsPlume.joinLines("  1", "  2", "  3"),
        StringsPlume.prefixLines("  ", StringsPlume.joinLines("1", "2", "3")));
    assertEquals(
        StringsPlume.joinLines("  ", "  1", "  ", "  2", "  "),
        StringsPlume.prefixLines("  ", StringsPlume.joinLines("", "1", "", "2", "")));
  }

  @Test
  public void test_indentLines() {

    // public static String indentLines(int indent, String s) {

    assertEquals(
        StringsPlume.prefixLines("  ", StringsPlume.joinLines("1", "2", "3")),
        StringsPlume.indentLines(2, StringsPlume.joinLines("1", "2", "3")));
    assertEquals(
        StringsPlume.prefixLines("  ", StringsPlume.joinLines("", "1", "", "2", "")),
        StringsPlume.indentLines(2, StringsPlume.joinLines("", "1", "", "2", "")));
  }

  @Test
  public void test_join() {

    // public static String join(Object[] a, String delim)
    // public static String join(ArrayList v, String delim)

    assertTrue(StringsPlume.join(", ", new String[] {"foo", "bar", "baz"}).equals("foo, bar, baz"));
    assertTrue(StringsPlume.join(", ", "foo", "bar", "baz").equals("foo, bar, baz"));
    assertTrue(StringsPlume.join(", ", new String[] {"foo"}).equals("foo"));
    assertTrue(StringsPlume.join(", ", "foo").equals("foo"));
    assertTrue(StringsPlume.join(", ", new String[] {}).equals(""));
    assertTrue(StringsPlume.join(", ").equals(""));
    assertTrue(StringsPlume.join("", new Integer[] {0, 1, 2, 3, 4}).equals("01234"));
    assertTrue(StringsPlume.join("", 0, 1, 2, 3, 4).equals("01234"));
    ArrayList<Object> potpourri = new ArrayList<>();
    potpourri.add("day");
    potpourri.add(2);
    potpourri.add("day");
    assertEquals("day 2 day", StringsPlume.join(" ", potpourri));
  }

  private void oneEscapeJava(String s, String escaped) {
    assertEquals(escaped, StringsPlume.escapeJava(s));
    assertEquals(s, StringsPlume.unescapeJava(escaped));
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
    assertTrue(StringsPlume.unescapeJava("\\").equals("\\"));
    assertTrue(StringsPlume.unescapeJava("foo\\").equals("foo\\"));
    assertTrue(StringsPlume.unescapeJava("\\*abc").equals("*abc"));
    assertTrue(StringsPlume.unescapeJava("\\101").equals("A"));
    assertTrue(StringsPlume.unescapeJava("A\\102C").equals("ABC"));

    assertEquals("(1", StringsPlume.unescapeJava("\0501"));
    assertEquals("(1", StringsPlume.unescapeJava("\501"));
    assertEquals("?7", StringsPlume.unescapeJava("\0777")); // '?' = \077
    assertEquals("?7", StringsPlume.unescapeJava("\777")); // '?' = \077
    assertEquals(" M ", StringsPlume.unescapeJava(" \uuu004D "));

    // public static String escapeNonASCII(String orig)

    assertTrue(StringsPlume.escapeNonASCII("foobar").equals("foobar"));
    assertTrue(StringsPlume.escapeNonASCII("").equals(""));
    assertTrue(StringsPlume.escapeNonASCII("\\").equals("\\\\"));
    assertTrue(StringsPlume.escapeNonASCII("\\\n\r\"").equals("\\\\\\n\\r\\\""));
    assertTrue(StringsPlume.escapeNonASCII("split\nlines").equals("split\\nlines"));
    assertTrue(StringsPlume.escapeNonASCII("\\relax").equals("\\\\relax"));
    assertTrue(StringsPlume.escapeNonASCII("\"hello\"").equals("\\\"hello\\\""));
    assertTrue(
        StringsPlume.escapeNonASCII("\"hello\" \"world\"").equals("\\\"hello\\\" \\\"world\\\""));
    assert StringsPlume.escapeNonASCII("\0\1\2\7\12\70\100\111\222")
        .equals("\\000\\001\\002\\007\\n8@I\\222");
    assert StringsPlume.escapeNonASCII("\u0100\u1000\ucafe\uffff")
        .equals("\\u0100\\u1000\\ucafe\\uffff");

    // private static String escapeNonASCII(char c)

    // Unfortunately, there isn't yet a unescapeNonASCII function.
    // If implemented, it should have the following behavior:
    // assertTrue(StringsPlume.unescapeNonASCII("\\115").equals("M"));
    // assertTrue(StringsPlume.unescapeNonASCII("\\115\\111\\124").equals("MIT"));
  }

  @Test
  public void test_removeWhitespace() {

    // public static String removeWhitespaceAround(String arg, String delimiter)
    // public static String removeWhitespaceAfter(String arg, String delimiter)
    // public static String removeWhitespaceBefore(String arg, String delimiter)

    assertTrue(StringsPlume.removeWhitespaceBefore("a,b", ",").equals("a,b"));
    assertTrue(StringsPlume.removeWhitespaceBefore("a, b", ",").equals("a, b"));
    assertTrue(StringsPlume.removeWhitespaceBefore("a ,b", ",").equals("a,b"));
    assertTrue(StringsPlume.removeWhitespaceBefore("a , b", ",").equals("a, b"));
    assertTrue(StringsPlume.removeWhitespaceBefore("ab=>cd", "=>").equals("ab=>cd"));
    assertTrue(StringsPlume.removeWhitespaceBefore("ab=> cd", "=>").equals("ab=> cd"));
    assertTrue(StringsPlume.removeWhitespaceBefore("ab =>cd", "=>").equals("ab=>cd"));
    assertTrue(StringsPlume.removeWhitespaceBefore("ab => cd", "=>").equals("ab=> cd"));
    assertTrue(StringsPlume.removeWhitespaceBefore("123cd", "123").equals("123cd"));
    assertTrue(StringsPlume.removeWhitespaceBefore(" 123 cd", "123").equals("123 cd"));
    assertTrue(StringsPlume.removeWhitespaceBefore(" 123cd", "123").equals("123cd"));
    assertTrue(StringsPlume.removeWhitespaceBefore("123 cd", "123").equals("123 cd"));
    assertTrue(StringsPlume.removeWhitespaceBefore("cd123", "123").equals("cd123"));
    assertTrue(StringsPlume.removeWhitespaceBefore("cd 123 ", "123").equals("cd123 "));
    assertTrue(StringsPlume.removeWhitespaceBefore("cd123 ", "123").equals("cd123 "));
    assertTrue(StringsPlume.removeWhitespaceBefore("cd 123", "123").equals("cd123"));

    assertTrue(StringsPlume.removeWhitespaceAfter("a,b", ",").equals("a,b"));
    assertTrue(StringsPlume.removeWhitespaceAfter("a, b", ",").equals("a,b"));
    assertTrue(StringsPlume.removeWhitespaceAfter("a ,b", ",").equals("a ,b"));
    assertTrue(StringsPlume.removeWhitespaceAfter("a , b", ",").equals("a ,b"));
    assertTrue(StringsPlume.removeWhitespaceAfter("ab=>cd", "=>").equals("ab=>cd"));
    assertTrue(StringsPlume.removeWhitespaceAfter("ab=> cd", "=>").equals("ab=>cd"));
    assertTrue(StringsPlume.removeWhitespaceAfter("ab =>cd", "=>").equals("ab =>cd"));
    assertTrue(StringsPlume.removeWhitespaceAfter("ab => cd", "=>").equals("ab =>cd"));
    assertTrue(StringsPlume.removeWhitespaceAfter("123cd", "123").equals("123cd"));
    assertTrue(StringsPlume.removeWhitespaceAfter(" 123 cd", "123").equals(" 123cd"));
    assertTrue(StringsPlume.removeWhitespaceAfter(" 123cd", "123").equals(" 123cd"));
    assertTrue(StringsPlume.removeWhitespaceAfter("123 cd", "123").equals("123cd"));
    assertTrue(StringsPlume.removeWhitespaceAfter("cd123", "123").equals("cd123"));
    assertTrue(StringsPlume.removeWhitespaceAfter("cd 123 ", "123").equals("cd 123"));
    assertTrue(StringsPlume.removeWhitespaceAfter("cd123 ", "123").equals("cd123"));
    assertTrue(StringsPlume.removeWhitespaceAfter("cd 123", "123").equals("cd 123"));

    assertTrue(StringsPlume.removeWhitespaceAround("a,b", ",").equals("a,b"));
    assertTrue(StringsPlume.removeWhitespaceAround("a, b", ",").equals("a,b"));
    assertTrue(StringsPlume.removeWhitespaceAround("a ,b", ",").equals("a,b"));
    assertTrue(StringsPlume.removeWhitespaceAround("a , b", ",").equals("a,b"));
    assertTrue(StringsPlume.removeWhitespaceAround("ab=>cd", "=>").equals("ab=>cd"));
    assertTrue(StringsPlume.removeWhitespaceAround("ab=> cd", "=>").equals("ab=>cd"));
    assertTrue(StringsPlume.removeWhitespaceAround("ab =>cd", "=>").equals("ab=>cd"));
    assertTrue(StringsPlume.removeWhitespaceAround("ab => cd", "=>").equals("ab=>cd"));
    assertTrue(StringsPlume.removeWhitespaceAround("123cd", "123").equals("123cd"));
    assertTrue(StringsPlume.removeWhitespaceAround(" 123 cd", "123").equals("123cd"));
    assertTrue(StringsPlume.removeWhitespaceAround(" 123cd", "123").equals("123cd"));
    assertTrue(StringsPlume.removeWhitespaceAround("123 cd", "123").equals("123cd"));
    assertTrue(StringsPlume.removeWhitespaceAround("cd123", "123").equals("cd123"));
    assertTrue(StringsPlume.removeWhitespaceAround("cd 123 ", "123").equals("cd123"));
    assertTrue(StringsPlume.removeWhitespaceAround("cd123 ", "123").equals("cd123"));
    assertTrue(StringsPlume.removeWhitespaceAround("cd 123", "123").equals("cd123"));
  }

  @Test
  public void test_nplural() {

    // public static String nplural(int n, String noun)

    assertTrue(StringsPlume.nplural(0, "fuss").equals("0 fusses"));
    assertTrue(StringsPlume.nplural(1, "fuss").equals("1 fuss"));
    assertTrue(StringsPlume.nplural(2, "fuss").equals("2 fusses"));
    assertTrue(StringsPlume.nplural(0, "fox").equals("0 foxes"));
    assertTrue(StringsPlume.nplural(1, "fox").equals("1 fox"));
    assertTrue(StringsPlume.nplural(2, "fox").equals("2 foxes"));
    assertTrue(StringsPlume.nplural(0, "fish").equals("0 fishes"));
    assertTrue(StringsPlume.nplural(1, "fish").equals("1 fish"));
    assertTrue(StringsPlume.nplural(2, "fish").equals("2 fishes"));
    assertTrue(StringsPlume.nplural(0, "fletch").equals("0 fletches"));
    assertTrue(StringsPlume.nplural(1, "fletch").equals("1 fletch"));
    assertTrue(StringsPlume.nplural(2, "fletch").equals("2 fletches"));
    assertTrue(StringsPlume.nplural(0, "fund").equals("0 funds"));
    assertTrue(StringsPlume.nplural(1, "fund").equals("1 fund"));
    assertTrue(StringsPlume.nplural(2, "fund").equals("2 funds"));
    assertTrue(StringsPlume.nplural(0, "f-stop").equals("0 f-stops"));
    assertTrue(StringsPlume.nplural(1, "f-stop").equals("1 f-stop"));
    assertTrue(StringsPlume.nplural(2, "f-stop").equals("2 f-stops"));
    assertEquals("0 facilities", StringsPlume.nplural(0, "facility"));
    assertEquals("1 facility", StringsPlume.nplural(1, "facility"));
    assertEquals("2 facilities", StringsPlume.nplural(2, "facility"));
    assertEquals("0 factories", StringsPlume.nplural(0, "factory"));
    assertEquals("1 factory", StringsPlume.nplural(1, "factory"));
    assertEquals("2 factories", StringsPlume.nplural(2, "factory"));
    assertEquals("0 fairways", StringsPlume.nplural(0, "fairway"));
    assertEquals("1 fairway", StringsPlume.nplural(1, "fairway"));
    assertEquals("2 fairways", StringsPlume.nplural(2, "fairway"));
    assertEquals("0 fanboys", StringsPlume.nplural(0, "fanboy"));
    assertEquals("1 fanboy", StringsPlume.nplural(1, "fanboy"));
    assertEquals("2 fanboys", StringsPlume.nplural(2, "fanboy"));
  }

  @Test
  public void test_conjunction() {

    // public static String conjunction(String conjunction, List<?> elements)

    assertEquals("a", StringsPlume.conjunction("and", Arrays.asList("a")));
    assertEquals("a and b", StringsPlume.conjunction("and", Arrays.asList("a", "b")));
    assertEquals("a, b, and c", StringsPlume.conjunction("and", Arrays.asList("a", "b", "c")));
    assertEquals(
        "a, b, c, and d", StringsPlume.conjunction("and", Arrays.asList("a", "b", "c", "d")));
    assertEquals("a", StringsPlume.conjunction("or", Arrays.asList("a")));
    assertEquals("a or b", StringsPlume.conjunction("or", Arrays.asList("a", "b")));
    assertEquals("a, b, or c", StringsPlume.conjunction("or", Arrays.asList("a", "b", "c")));
    assertEquals(
        "a, b, c, or d", StringsPlume.conjunction("or", Arrays.asList("a", "b", "c", "d")));
  }

  @Test
  public void test_rpad() {

    // public static String rpad(String s, int length)
    // public static String rpad(int num, int length)
    // public static String rpad(double num, int length)

    assertTrue(StringsPlume.rpad("", 5).equals("     "));
    assertTrue(StringsPlume.rpad("abcd", 5).equals("abcd "));
    assertTrue(StringsPlume.rpad("abcde", 5).equals("abcde"));
    assertTrue(StringsPlume.rpad("abcdef", 5).equals("abcde"));
    assertTrue(StringsPlume.rpad("abcde ghij", 5).equals("abcde"));
    assertTrue(StringsPlume.rpad(10, 5).equals("10   "));
    assertTrue(StringsPlume.rpad(3.14, 5).equals("3.14 "));

    // public static class NullableStringComparator
    //   public int compare(Object o1, Object o2)

  }

  @Test
  public void test_count() {

    // public static int count(String s, int ch)
    // public static int count(String s, String sub)

    assertTrue(StringsPlume.count("abcde", 'a') == 1);
    assertTrue(StringsPlume.count("abcde", 'c') == 1);
    assertTrue(StringsPlume.count("abcde", 'e') == 1);
    assertTrue(StringsPlume.count("abcde", 'z') == 0);
    assertTrue(StringsPlume.count("abacadaea", 'a') == 5);
    assertTrue(StringsPlume.count("aaa aea", 'a') == 5);
    assertTrue(StringsPlume.count("daeaaa", 'a') == 4);
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
    assertTrue(StringsPlume.abbreviateNumber(5).equals("5.00"));
    assertTrue(StringsPlume.abbreviateNumber(5000).equals("5.00K"));
    assertTrue(StringsPlume.abbreviateNumber(5000000).equals("5.00M"));
    assertTrue(StringsPlume.abbreviateNumber(1000000000).equals("1.00G"));
    assertTrue(StringsPlume.abbreviateNumber(1).equals("1.00"));
    assertTrue(StringsPlume.abbreviateNumber(12).equals("12.0"));
    assertTrue(StringsPlume.abbreviateNumber(123).equals("123"));
    assertTrue(StringsPlume.abbreviateNumber(1234).equals("1.23K"));
    assertTrue(StringsPlume.abbreviateNumber(12345).equals("12.3K"));
    assertTrue(StringsPlume.abbreviateNumber(123456).equals("123K"));
    assertTrue(StringsPlume.abbreviateNumber(1234567).equals("1.23M"));
    assertTrue(StringsPlume.abbreviateNumber(12345678).equals("12.3M"));
    assertTrue(StringsPlume.abbreviateNumber(123456789).equals("123M"));
    assertTrue(StringsPlume.abbreviateNumber(1234567890).equals("1.23G"));
    assertTrue(StringsPlume.abbreviateNumber(9).equals("9.00"));
    assertTrue(StringsPlume.abbreviateNumber(98).equals("98.0"));
    assertTrue(StringsPlume.abbreviateNumber(987).equals("987"));
    assertTrue(StringsPlume.abbreviateNumber(9876).equals("9.88K"));
    assertTrue(StringsPlume.abbreviateNumber(98765).equals("98.8K"));
    assertTrue(StringsPlume.abbreviateNumber(987654).equals("988K"));
    assertTrue(StringsPlume.abbreviateNumber(9876543).equals("9.88M"));
    assertTrue(StringsPlume.abbreviateNumber(98765432).equals("98.8M"));
    assertTrue(StringsPlume.abbreviateNumber(987654321).equals("988M"));
    assertTrue(StringsPlume.abbreviateNumber(9876543210L).equals("9.88G"));
  }

  @Test
  public void testCountFormatArguments() {
    assertEquals(0, StringsPlume.countFormatArguments("No specifiier."));
    assertEquals(0, StringsPlume.countFormatArguments("This is 100%"));
    assertEquals(0, StringsPlume.countFormatArguments("This is 100%% excellent."));
    assertEquals(0, StringsPlume.countFormatArguments("Newline%n is not%na specifier."));
    assertEquals(1, StringsPlume.countFormatArguments("This is my %s"));
    assertEquals(1, StringsPlume.countFormatArguments("This is my %s."));
    assertEquals(2, StringsPlume.countFormatArguments("Two %d and %d"));
    assertEquals(3, StringsPlume.countFormatArguments("%f and %s and %d makes three"));
    assertEquals(
        3,
        StringsPlume.countFormatArguments("Hi! My name is %s and I have %d dogs and a %d cats."));

    assertEquals(2, StringsPlume.countFormatArguments("%f and %1$f and %d and %1$f makes two"));
    assertEquals(14, StringsPlume.countFormatArguments("%f and %14$f makes fourteen"));
  }

  @Test
  public void testSplitLines() {

    String str = "one\ntwo\n\rthree\r\nfour\rfive\n\n\nsix\r\n\r\n\r\n";
    @SuppressWarnings("value") // method that returns an array is not StaticallyExecutable
    String @ArrayLen(11) [] sa = StringsPlume.splitLines(str);
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
