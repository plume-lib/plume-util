package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.jupiter.api.Test;
import org.plumelib.util.StringsP.VersionNumberComparator;

/** Test the StringsP class. */
final class StringsPTest {

  StringsPTest() {}

  // //////////////////////////////////////////////////////////////////////
  // Replacement
  //

  /** Test replacePrefix(). */
  @Test
  void test_replacePrefix() {

    // public static String replacePrefix(String target, String oldStr, String newStr)

    assertEquals("hijkdefg", StringsP.replacePrefix("abcdefg", "abc", "hijk"));
    assertEquals("abcdefg", StringsP.replacePrefix("abcdefg", "bcd", "hijk"));
    assertEquals("defg", StringsP.replacePrefix("abcdefg", "abc", ""));
    assertEquals("abcdefg", StringsP.replacePrefix("abcdefg", "bcd", ""));
  }

  /** Test replaceSuffix(). */
  @Test
  void test_replaceSuffix() {

    // public static String replaceSuffix(String target, String oldStr, String newStr)

    assertEquals("abchijk", StringsP.replaceSuffix("abcdefg", "defg", "hijk"));
    assertEquals("abcdefg", StringsP.replaceSuffix("abcdefg", "cdef", "hijk"));
    assertEquals("abc", StringsP.replaceSuffix("abcdefg", "defg", ""));
    assertEquals("abcdefg", StringsP.replaceSuffix("abcdefg", "cdef", ""));
  }

  /** Test replaceAll(). */
  @Test
  void test_replaceAll() {

    assertEquals("avarywhara", StringsP.replaceAll("everywhere", Pattern.compile("e"), "a"));
    assertEquals("aabaa", StringsP.replaceAll("abababa", Pattern.compile("aba"), "aa"));
    assertEquals("abbababba", StringsP.replaceAll("abababa", Pattern.compile("a(b)a"), "a$1$1a"));
  }

  // //////////////////////////////////////////////////////////////////////
  // Prefixing and indentation
  //

  /** Test prefixLines(). */
  @Test
  void test_prefixLines() {

    // public static String prefixLines(String prefix, String s) {

    assertEquals(
        StringsP.joinLines("  1", "  2", "  3"),
        StringsP.prefixLines("  ", StringsP.joinLines("1", "2", "3")));
    assertEquals(
        StringsP.joinLines("  ", "  1", "  ", "  2", "  "),
        StringsP.prefixLines("  ", StringsP.joinLines("", "1", "", "2", "")));
  }

  /** Test prefixLinesExceptFirst(). */
  @Test
  void test_prefixLinesExceptFirst() {
    // Tests go here.
  }

  /** Test indentLines(). */
  @Test
  void test_indentLines() {

    // public static String indentLines(int indent, String s) {

    assertEquals(
        StringsP.prefixLines("  ", StringsP.joinLines("1", "2", "3")),
        StringsP.indentLines(2, StringsP.joinLines("1", "2", "3")));
    assertEquals(
        StringsP.prefixLines("  ", StringsP.joinLines("", "1", "", "2", "")),
        StringsP.indentLines(2, StringsP.joinLines("", "1", "", "2", "")));
  }

  /** Test indentLinesExceptFirst(). */
  @Test
  void test_indentLinesExceptFirst() {
    // Tests go here.
  }

  // //////////////////////////////////////////////////////////////////////
  // Splitting and joining
  //

  /** Test splitLines(). */
  @Test
  void test_splitLines() {
    String str = "one\ntwo\n\rthree\r\nfour\rfive\n\n\nsix\r\n\r\n\r\n";
    @SuppressWarnings("value") // method that returns an array is not StaticallyExecutable
    String @ArrayLen(12) [] sa = StringsP.splitLines(str);
    // for (String s : sa)
    //   System.out.printf ("'%s'%n", s);
    assertEquals(12, sa.length);
    assertEquals("one", sa[0]);
    assertEquals("two", sa[1]);
    assertEquals("", sa[2]);
    assertEquals("three", sa[3]);
    assertEquals("four", sa[4]);
    assertEquals("five", sa[5]);
    assertEquals("", sa[6]);
    assertEquals("", sa[7]);
    assertEquals("six", sa[8]);
    assertEquals("", sa[9]);
    assertEquals("", sa[10]);
    assertEquals("", sa[11]);
  }

  /** Test firstLineSeparator(). */
  @Test
  void test_firstLineSeparator() {
    assertEquals(null, StringsP.firstLineSeparator("hello"));
    assertEquals("\n", StringsP.firstLineSeparator("hello\ngoodbye"));
    assertEquals("\n", StringsP.firstLineSeparator("hello\ngoodbye\rau revior"));
    assertEquals("\n", StringsP.firstLineSeparator("hello\ngoodbye\rau revior\r\nWindows"));
    assertEquals("\n", StringsP.firstLineSeparator("hello\n\rgoodbye\rau revior\r\nWindows"));

    assertEquals("\r", StringsP.firstLineSeparator("hello\rgoodbye"));
    assertEquals("\r", StringsP.firstLineSeparator("hello\rgoodbye\nau revior"));
    assertEquals("\r", StringsP.firstLineSeparator("hello\rgoodbye\nau revior\r\nWindows"));

    assertEquals("\r\n", StringsP.firstLineSeparator("hello\r\ngoodbye"));
    assertEquals("\r\n", StringsP.firstLineSeparator("hello\r\ngoodbye\nau revior"));
    assertEquals("\r\n", StringsP.firstLineSeparator("hello\r\ngoodbye\nau revior\rold MacOS"));
  }

  /** Test splitLinesRetainSeparators(). */
  @Test
  void test_splitLinesRetainSeparators() {
    String text = "hello\rworld\nhello\r\nworld\n\rfoo";
    List<String> result = StringsP.splitLinesRetainSeparators(text);
    List<String> expected =
        Arrays.asList("hello\r", "world\n", "hello\r\n", "world\n", "\r", "foo");
    assertEquals(expected, result);
  }

  /** Test splitRetainSeparators(). */
  @Test
  void test_splitRetainSeparators() {
    // There are two overloaded methods to test here.
  }

  /** Test join(). */
  @SuppressWarnings("PMD.UnnecessaryVarargsArrayCreation") // testing with & without varargs array
  @Test
  void test_join() {

    // public static String join(Object[] a, String delim)
    // public static String join(ArrayList v, String delim)

    assertEquals("foo, bar, baz", StringsP.join(", ", new String[] {"foo", "bar", "baz"}));
    assertEquals("foo, bar, baz", StringsP.join(", ", "foo", "bar", "baz"));
    assertEquals("foo", StringsP.join(", ", new String[] {"foo"}));
    assertEquals("foo", StringsP.join(", ", "foo"));
    assertEquals("", StringsP.join(", ", new String[] {}));
    assertEquals("", StringsP.join(", "));
    assertEquals("01234", StringsP.join("", new Integer[] {0, 1, 2, 3, 4}));
    assertEquals("01234", StringsP.join("", 0, 1, 2, 3, 4));

    ArrayList<Object> potpourri = new ArrayList<>();
    potpourri.add("day");
    potpourri.add(2);
    potpourri.add("day");
    assertEquals("day 2 day", StringsP.join(" ", potpourri));
  }

  /** Test joinLines(). */
  @Test
  void test_joinLines() {
    // Tests go here.
  }

  // //////////////////////////////////////////////////////////////////////
  // Quoting and escaping
  //

  private void oneEscapeJava(String s, String escaped) {
    assertEquals(escaped, StringsP.escapeJava(s));
    assertEquals(s, StringsP.unescapeJava(escaped));
  }

  /** Test escapeJava(). */
  @SuppressWarnings({"UnicodeEscape", "PMD.SuspiciousOctalEscape"})
  @Test
  void test_escapeJava() {

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
    oneEscapeJava("'hello'", "'hello'");
    oneEscapeJava("foo\\", "foo\\\\");
    oneEscapeJava("foo\0bar", "foo\\000bar");
    oneEscapeJava("foo\tbar", "foo\\tbar");
    oneEscapeJava("\b\f\n\r\t\1\377", "\\b\\f\\n\\r\\t\\001\\377");
    oneEscapeJava("foo\b\f\n\r\t\1\377", "foo\\b\\f\\n\\r\\t\\001\\377");
    oneEscapeJava("\222", "\\222");
    oneEscapeJava("foo\222", "foo\\222");
    oneEscapeJava("\300", "\\300");
    oneEscapeJava("foo\300", "foo\\300");
    oneEscapeJava("\u12345", "\\u12345");
    oneEscapeJava("foo\u12345", "foo\\u12345");
    oneEscapeJava("\u1234A", "\\u1234A");
    oneEscapeJava("foo\u1234A", "foo\\u1234A");
    oneEscapeJava("\u54321", "\\u54321");
    oneEscapeJava("foo\u54321", "foo\\u54321");
    oneEscapeJava("\u5432A", "\\u5432A");
    oneEscapeJava("foo\u5432A", "foo\\u5432A");
    // Should add more tests here.

    // These tests are not symmetric because the argument is not a value that escapeJava would ever
    // return.
    assertEquals("\\", StringsP.unescapeJava("\\"));
    assertEquals("foo\\", StringsP.unescapeJava("foo\\"));
    assertEquals("*abc", StringsP.unescapeJava("\\*abc"));
    assertEquals("A", StringsP.unescapeJava("\\101"));
    assertEquals("ABC", StringsP.unescapeJava("A\\102C"));
    assertEquals("don't", StringsP.unescapeJava("don\\'t"));

    assertEquals("(1", StringsP.unescapeJava("\0501"));
    assertEquals("(1", StringsP.unescapeJava("\501"));
    assertEquals("?7", StringsP.unescapeJava("\0777")); // '?' = \077
    assertEquals("?7", StringsP.unescapeJava("\777")); // '?' = \077

    // public static String escapeNonASCII(String orig)

    assertEquals("foobar", StringsP.escapeNonASCII("foobar"));
    assertEquals("", StringsP.escapeNonASCII(""));
    assertEquals("\\\\", StringsP.escapeNonASCII("\\"));
    assertEquals("\\\\\\n\\r\\\"", StringsP.escapeNonASCII("\\\n\r\""));
    assertEquals("split\\nlines", StringsP.escapeNonASCII("split\nlines"));
    assertEquals("\\\\relax", StringsP.escapeNonASCII("\\relax"));
    assertEquals("\\\"hello\\\"", StringsP.escapeNonASCII("\"hello\""));
    assertEquals("\\\"hello\\\" \\\"world\\\"", StringsP.escapeNonASCII("\"hello\" \"world\""));
    assertEquals(
        "\\000\\001\\002\\007\\n8@I\\222", StringsP.escapeNonASCII("\0\1\2\7\12\70\100\111\222"));
    assertEquals(
        "\\u0100\\u1000\\ucafe\\uffff", StringsP.escapeNonASCII("\u0100\u1000\ucafe\uffff"));

    // private static String escapeNonASCII(char c)

    // Unfortunately, there isn't yet a unescapeNonASCII function.
    // If implemented, it should have the following behavior:
    // assertEquals("M", StringsP.unescapeNonASCII("\\115")));
    // assertEquals("MIT", StringsP.unescapeNonASCII("\\115\\111\\124")));
  }

  /** Test charLiteral(). */
  @Test
  void test_charLiteral() {
    assertEquals("'a'", StringsP.charLiteral('a'));
    assertEquals("'\\''", StringsP.charLiteral('\''));
    assertEquals("'\"'", StringsP.charLiteral('\"'));
  }

  /** Test escapeNonASCII(). */
  @Test
  void test_escapeNonASCII() {
    // Tests go here.
  }

  /** Test unescapeJava(). */
  @Test
  void test_unescapeJava() {
    // Tests go here.
  }

  // //////////////////////////////////////////////////////////////////////
  // Whitespace
  //

  /** Test isBlank(). */
  @Test
  void test_isBlank() {
    // Tests go here.
  }

  @Test
  void test_removeWhitespace() {

    // public static String removeWhitespaceAround(String arg, String delimiter)
    // public static String removeWhitespaceAfter(String arg, String delimiter)
    // public static String removeWhitespaceBefore(String arg, String delimiter)

    assertEquals("a,b", StringsP.removeWhitespaceBefore("a,b", ","));
    assertEquals("a, b", StringsP.removeWhitespaceBefore("a, b", ","));
    assertEquals("a,b", StringsP.removeWhitespaceBefore("a ,b", ","));
    assertEquals("a, b", StringsP.removeWhitespaceBefore("a , b", ","));
    assertEquals("ab=>cd", StringsP.removeWhitespaceBefore("ab=>cd", "=>"));
    assertEquals("ab=> cd", StringsP.removeWhitespaceBefore("ab=> cd", "=>"));
    assertEquals("ab=>cd", StringsP.removeWhitespaceBefore("ab =>cd", "=>"));
    assertEquals("ab=> cd", StringsP.removeWhitespaceBefore("ab => cd", "=>"));
    assertEquals("123cd", StringsP.removeWhitespaceBefore("123cd", "123"));
    assertEquals("123 cd", StringsP.removeWhitespaceBefore(" 123 cd", "123"));
    assertEquals("123cd", StringsP.removeWhitespaceBefore(" 123cd", "123"));
    assertEquals("123 cd", StringsP.removeWhitespaceBefore("123 cd", "123"));
    assertEquals("cd123", StringsP.removeWhitespaceBefore("cd123", "123"));
    assertEquals("cd123 ", StringsP.removeWhitespaceBefore("cd 123 ", "123"));
    assertEquals("cd123 ", StringsP.removeWhitespaceBefore("cd123 ", "123"));
    assertEquals("cd123", StringsP.removeWhitespaceBefore("cd 123", "123"));

    assertEquals("a,b", StringsP.removeWhitespaceAfter("a,b", ","));
    assertEquals("a,b", StringsP.removeWhitespaceAfter("a, b", ","));
    assertEquals("a ,b", StringsP.removeWhitespaceAfter("a ,b", ","));
    assertEquals("a ,b", StringsP.removeWhitespaceAfter("a , b", ","));
    assertEquals("ab=>cd", StringsP.removeWhitespaceAfter("ab=>cd", "=>"));
    assertEquals("ab=>cd", StringsP.removeWhitespaceAfter("ab=> cd", "=>"));
    assertEquals("ab =>cd", StringsP.removeWhitespaceAfter("ab =>cd", "=>"));
    assertEquals("ab =>cd", StringsP.removeWhitespaceAfter("ab => cd", "=>"));
    assertEquals("123cd", StringsP.removeWhitespaceAfter("123cd", "123"));
    assertEquals(" 123cd", StringsP.removeWhitespaceAfter(" 123 cd", "123"));
    assertEquals(" 123cd", StringsP.removeWhitespaceAfter(" 123cd", "123"));
    assertEquals("123cd", StringsP.removeWhitespaceAfter("123 cd", "123"));
    assertEquals("cd123", StringsP.removeWhitespaceAfter("cd123", "123"));
    assertEquals("cd 123", StringsP.removeWhitespaceAfter("cd 123 ", "123"));
    assertEquals("cd123", StringsP.removeWhitespaceAfter("cd123 ", "123"));
    assertEquals("cd 123", StringsP.removeWhitespaceAfter("cd 123", "123"));

    assertEquals("a,b", StringsP.removeWhitespaceAround("a,b", ","));
    assertEquals("a,b", StringsP.removeWhitespaceAround("a, b", ","));
    assertEquals("a,b", StringsP.removeWhitespaceAround("a ,b", ","));
    assertEquals("a,b", StringsP.removeWhitespaceAround("a , b", ","));
    assertEquals("ab=>cd", StringsP.removeWhitespaceAround("ab=>cd", "=>"));
    assertEquals("ab=>cd", StringsP.removeWhitespaceAround("ab=> cd", "=>"));
    assertEquals("ab=>cd", StringsP.removeWhitespaceAround("ab =>cd", "=>"));
    assertEquals("ab=>cd", StringsP.removeWhitespaceAround("ab => cd", "=>"));
    assertEquals("123cd", StringsP.removeWhitespaceAround("123cd", "123"));
    assertEquals("123cd", StringsP.removeWhitespaceAround(" 123 cd", "123"));
    assertEquals("123cd", StringsP.removeWhitespaceAround(" 123cd", "123"));
    assertEquals("123cd", StringsP.removeWhitespaceAround("123 cd", "123"));
    assertEquals("cd123", StringsP.removeWhitespaceAround("cd123", "123"));
    assertEquals("cd123", StringsP.removeWhitespaceAround("cd 123 ", "123"));
    assertEquals("cd123", StringsP.removeWhitespaceAround("cd123 ", "123"));
    assertEquals("cd123", StringsP.removeWhitespaceAround("cd 123", "123"));
  }

  /** Test lpad(). */
  @Test
  void test_lpad() {
    // Tests go here.
  }

  /** Test rpad(). */
  @Test
  void test_rpad() {

    // public static String rpad(String s, int length)
    // public static String rpad(int num, int length)
    // public static String rpad(double num, int length)

    assertEquals("     ", StringsP.rpad("", 5));
    assertEquals("abcd ", StringsP.rpad("abcd", 5));
    assertEquals("abcde", StringsP.rpad("abcde", 5));
    assertEquals("ab...", StringsP.rpad("abcdef", 5));
    assertEquals("ab...", StringsP.rpad("abcde ghij", 5));
    assertEquals("10   ", StringsP.rpad(10, 5));
    assertEquals("3.14 ", StringsP.rpad(3.14, 5));
    assertEquals("3.141", StringsP.rpad(3.141_592, 5));
    assertEquals("3141592", StringsP.rpad(3_141_592, 5));
    assertEquals("12", StringsP.rpad(12.345_67, 1));
    assertEquals("12", StringsP.rpad(12.345_67, 2));
    assertEquals("12 ", StringsP.rpad(12.345_67, 3));
    assertEquals("12.3", StringsP.rpad(12.345_67, 4));
    assertEquals("12.34", StringsP.rpad(12.345_67, 5));
    assertEquals("12.345", StringsP.rpad(12.345_67, 6));
  }

  // public static class NullableStringComparator
  //   public int compare(Object o1, Object o2)

  // //////////////////////////////////////////////////////////////////////
  // Comparisons
  //

  // //////////////////////////////////////////////////////////////////////
  // StringTokenizer
  //

  /** Test tokens(). */
  @Test
  void test_tokens() {
    // Tests go here.
  }

  // //////////////////////////////////////////////////////////////////////
  // Version numbers
  //

  /** Test isVersionNumber(). */
  @Test
  void test_isVersionNumber() {
    // Tests go here.
  }

  /** Test isVersionNumberLE(). */
  @Test
  void test_isVersionNumberLE() {

    VersionNumberComparator vnc = new VersionNumberComparator();

    assertEquals(0, vnc.compare("123.456.789", "123.456.789"));
    assertEquals(-1, vnc.compare("113.456.789", "123.456.789"));
    assertEquals(-1, vnc.compare("123.416.789", "123.456.789"));
    assertEquals(-1, vnc.compare("123.456.719", "123.456.789"));
    assertEquals(-1, vnc.compare("123.456.789", "193.456.789"));
    assertEquals(-1, vnc.compare("123.456.789", "123.496.789"));
    assertEquals(-1, vnc.compare("123.456.789", "123.456.799"));
    assertEquals(-1, vnc.compare("123", "123.456.789"));
    assertEquals(-1, vnc.compare("123.456", "123.456.789"));
    assertEquals(1, vnc.compare("123.456.789", "123"));
    assertEquals(1, vnc.compare("123.456.789", "123.456"));
  }

  // //////////////////////////////////////////////////////////////////////
  // Debugging variants of toString
  //

  /** Test toStringAndClass(). */
  @Test
  void test_toStringAndClass() {
    // Tests go here.
  }

  /** Test listToStringAndClass(). */
  @Test
  void test_listToStringAndClass() {
    // Tests go here.
  }

  /** Test listToString(). */
  @Test
  void test_listToString() {
    // Tests go here.
  }

  /** Test arrayToStringAndClass(). */
  @Test
  void test_arrayToStringAndClass() {
    // Tests go here.
  }

  //
  // Diagnostic output
  //

  /** Test mapToStringAndClass(). */
  @Test
  void test_mapToStringAndClass() {
    // Tests go here.
  }

  /** Test mapToStringLinewise(). */
  @Test
  void test_mapToStringLinewise() {
    // Tests go here.
  }

  // //////////////////////////////////////////////////////////////////////
  // English text
  //

  /** Test nPlural(). */
  @Test
  void test_nPlural() {

    // public static String nPlural(int n, String noun)

    assertEquals("0 fusses", StringsP.nPlural(0, "fuss"));
    assertEquals("1 fuss", StringsP.nPlural(1, "fuss"));
    assertEquals("2 fusses", StringsP.nPlural(2, "fuss"));
    assertEquals("0 foxes", StringsP.nPlural(0, "fox"));
    assertEquals("1 fox", StringsP.nPlural(1, "fox"));
    assertEquals("2 foxes", StringsP.nPlural(2, "fox"));
    assertEquals("0 wishes", StringsP.nPlural(0, "wish"));
    assertEquals("1 wish", StringsP.nPlural(1, "wish"));
    assertEquals("2 wishes", StringsP.nPlural(2, "wish"));
    assertEquals("0 fletches", StringsP.nPlural(0, "fletch"));
    assertEquals("1 fletch", StringsP.nPlural(1, "fletch"));
    assertEquals("2 fletches", StringsP.nPlural(2, "fletch"));
    assertEquals("0 funds", StringsP.nPlural(0, "fund"));
    assertEquals("1 fund", StringsP.nPlural(1, "fund"));
    assertEquals("2 funds", StringsP.nPlural(2, "fund"));
    assertEquals("0 f-stops", StringsP.nPlural(0, "f-stop"));
    assertEquals("1 f-stop", StringsP.nPlural(1, "f-stop"));
    assertEquals("2 f-stops", StringsP.nPlural(2, "f-stop"));
    assertEquals("0 facilities", StringsP.nPlural(0, "facility"));
    assertEquals("1 facility", StringsP.nPlural(1, "facility"));
    assertEquals("2 facilities", StringsP.nPlural(2, "facility"));
    assertEquals("0 factories", StringsP.nPlural(0, "factory"));
    assertEquals("1 factory", StringsP.nPlural(1, "factory"));
    assertEquals("2 factories", StringsP.nPlural(2, "factory"));
    assertEquals("0 fairways", StringsP.nPlural(0, "fairway"));
    assertEquals("1 fairway", StringsP.nPlural(1, "fairway"));
    assertEquals("2 fairways", StringsP.nPlural(2, "fairway"));
    assertEquals("0 fanboys", StringsP.nPlural(0, "fanboy"));
    assertEquals("1 fanboy", StringsP.nPlural(1, "fanboy"));
    assertEquals("2 fanboys", StringsP.nPlural(2, "fanboy"));

    // Exceptions
    assertEquals("0 fish", StringsP.nPlural(0, "fish"));
    assertEquals("1 fish", StringsP.nPlural(1, "fish"));
    assertEquals("2 fish", StringsP.nPlural(2, "fish"));

    // public static String nPlural(Collection c, String noun)

    Collection<String> size0 = Collections.emptyList();
    Map<Integer, Double> size1 = Collections.singletonMap(1, 2.0);
    String[] size2 = {"a", "string"};

    assertEquals("0 fusses", StringsP.nPlural(size0, "fuss"));
    assertEquals("1 fuss", StringsP.nPlural(size1, "fuss"));
    assertEquals("2 fusses", StringsP.nPlural(size2, "fuss"));
    assertEquals("0 foxes", StringsP.nPlural(size0, "fox"));
    assertEquals("1 fox", StringsP.nPlural(size1, "fox"));
    assertEquals("2 foxes", StringsP.nPlural(size2, "fox"));
    assertEquals("0 fish", StringsP.nPlural(size0, "fish"));
    assertEquals("1 fish", StringsP.nPlural(size1, "fish"));
    assertEquals("2 fish", StringsP.nPlural(size2, "fish"));
  }

  /** Test vPlural(). */
  @Test
  void test_vPlural() {

    // public static String [Plural(int n, String verb)

    assertEquals("were", StringsP.vPlural(0, "was"));
    assertEquals("was", StringsP.vPlural(1, "was"));
    assertEquals("were", StringsP.vPlural(2, "was"));
    assertEquals("are", StringsP.vPlural(0, "is"));
    assertEquals("is", StringsP.vPlural(1, "is"));
    assertEquals("are", StringsP.vPlural(2, "is"));
    assertEquals("eat", StringsP.vPlural(0, "eat"));
    assertEquals("eat", StringsP.vPlural(1, "eat"));
    assertEquals("eat", StringsP.vPlural(2, "eat"));
  }

  /** Test nvPlural(). */
  @Test
  void test_nvPlural() {

    // public static String nvPlural(int n, String noun, String verb)

    assertEquals("0 foxes were", StringsP.nvPlural(0, "fox", "was"));
    assertEquals("1 fox was", StringsP.nvPlural(1, "fox", "was"));
    assertEquals("2 foxes were", StringsP.nvPlural(2, "fox", "was"));
    assertEquals("0 wishes are", StringsP.nvPlural(0, "wish", "is"));
    assertEquals("1 wish is", StringsP.nvPlural(1, "wish", "is"));
    assertEquals("2 wishes are", StringsP.nvPlural(2, "wish", "is"));
  }

  /** Test conjunction(). */
  @Test
  void test_conjunction() {

    // public static String conjunction(String conjunction, List<?> elements)

    assertEquals("a", StringsP.conjunction("and", Arrays.asList("a")));
    assertEquals("a and b", StringsP.conjunction("and", Arrays.asList("a", "b")));
    assertEquals("a, b, and c", StringsP.conjunction("and", Arrays.asList("a", "b", "c")));
    assertEquals("a, b, c, and d", StringsP.conjunction("and", Arrays.asList("a", "b", "c", "d")));
    assertEquals("a", StringsP.conjunction("or", Arrays.asList("a")));
    assertEquals("a or b", StringsP.conjunction("or", Arrays.asList("a", "b")));
    assertEquals("a, b, or c", StringsP.conjunction("or", Arrays.asList("a", "b", "c")));
    assertEquals("a, b, c, or d", StringsP.conjunction("or", Arrays.asList("a", "b", "c", "d")));
  }

  // //////////////////////////////////////////////////////////////////////
  // Miscellaneous
  //

  /** Test count(). */
  @Test
  void test_count() {

    // public static int count(String s, int ch)
    // public static int count(String s, String sub)

    assertEquals(1, StringsP.count("abcde", 'a'));
    assertEquals(1, StringsP.count("abcde", 'c'));
    assertEquals(1, StringsP.count("abcde", 'e'));
    assertEquals(0, StringsP.count("abcde", 'z'));
    assertEquals(5, StringsP.count("abacadaea", 'a'));
    assertEquals(5, StringsP.count("aaa aea", 'a'));
    assertEquals(4, StringsP.count("daeaaa", 'a'));
  }

  // This will be easy to write tests for, when I get around to it.
  // public static ArrayList tokens(String str, String delim, boolean returnTokens)
  // public static ArrayList tokens(String str, String delim)
  // public static ArrayList tokens(String str)

  // This is tested by the tokens methods.
  // public static ArrayList makeArrayList(Enumeration e)

  /** Test abbreviateNumber(). */
  @Test
  void test_abbreviateNumber() {

    Locale.setDefault(Locale.US);
    assertEquals("5.00", StringsP.abbreviateNumber(5));
    assertEquals("5.00K", StringsP.abbreviateNumber(5000));
    assertEquals("5.00M", StringsP.abbreviateNumber(5_000_000));
    assertEquals("1.00G", StringsP.abbreviateNumber(1_000_000_000));
    assertEquals("1.00", StringsP.abbreviateNumber(1));
    assertEquals("12.0", StringsP.abbreviateNumber(12));
    assertEquals("123", StringsP.abbreviateNumber(123));
    assertEquals("1.23K", StringsP.abbreviateNumber(1234));
    assertEquals("12.3K", StringsP.abbreviateNumber(12_345));
    assertEquals("123K", StringsP.abbreviateNumber(123_456));
    assertEquals("1.23M", StringsP.abbreviateNumber(1_234_567));
    assertEquals("12.3M", StringsP.abbreviateNumber(12_345_678));
    assertEquals("123M", StringsP.abbreviateNumber(123_456_789));
    assertEquals("1.23G", StringsP.abbreviateNumber(1_234_567_890));
    assertEquals("9.00", StringsP.abbreviateNumber(9));
    assertEquals("98.0", StringsP.abbreviateNumber(98));
    assertEquals("987", StringsP.abbreviateNumber(987));
    assertEquals("9.88K", StringsP.abbreviateNumber(9876));
    assertEquals("98.8K", StringsP.abbreviateNumber(98_765));
    assertEquals("988K", StringsP.abbreviateNumber(987_654));
    assertEquals("9.88M", StringsP.abbreviateNumber(9_876_543));
    assertEquals("98.8M", StringsP.abbreviateNumber(98_765_432));
    assertEquals("988M", StringsP.abbreviateNumber(987_654_321));
    assertEquals("9.88G", StringsP.abbreviateNumber(9_876_543_210L));
  }

  /** Test countFormatArguments(). */
  @Test
  void test_countFormatArguments() {
    assertEquals(0, StringsP.countFormatArguments("No specifiier."));
    assertEquals(0, StringsP.countFormatArguments("This is 100%"));
    assertEquals(0, StringsP.countFormatArguments("This is 100%% excellent."));
    assertEquals(0, StringsP.countFormatArguments("Newline%n is not%na specifier."));
    assertEquals(1, StringsP.countFormatArguments("This is my %s"));
    assertEquals(1, StringsP.countFormatArguments("This is my %s."));
    assertEquals(2, StringsP.countFormatArguments("Two %d and %d"));
    assertEquals(3, StringsP.countFormatArguments("%f and %s and %d makes three"));
    assertEquals(
        3, StringsP.countFormatArguments("Hi! My name is %s and I have %d dogs and a %d cats."));

    assertEquals(2, StringsP.countFormatArguments("%f and %1$f and %d and %1$f makes two"));
    assertEquals(14, StringsP.countFormatArguments("%f and %14$f makes fourteen"));
  }

  /** Test toStringTruncated(). */
  @Test
  void test_toStringTruncated() {
    assertEquals("0123456789", StringsP.toStringTruncated("0123456789", 100));
    assertEquals("0123456789", StringsP.toStringTruncated("0123456789", 10));
    assertEquals("\"012...\"", StringsP.toStringTruncated("0123456789", 8));
    assertEquals("\"0...\"", StringsP.toStringTruncated("0123456789", 6));
    assertEquals("\"0...\"", StringsP.toStringTruncated("0123456", 6));
    assertEquals("012345", StringsP.toStringTruncated("012345", 6));
    assertEquals("01234", StringsP.toStringTruncated("01234", 6));
  }
}
