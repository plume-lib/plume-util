package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.plumelib.util.EntryReader.CommentFormat;
import org.plumelib.util.EntryReader.EntryFormat;

/** Test the EntryReader class. */
@SuppressWarnings({
  "nullness", // run-time errors are acceptable
  "initializedfields:contracts.postcondition", // @TempDir causes injection
  "PMD.TooManyStaticImports"
})
final class EntryReaderTest {

  EntryReaderTest() {}

  /** Do not assign; JUnit will do so, thanks to the {@code @TempDir} annotation. */
  @TempDir Path tempDir;

  /** Test basic line reading without comments or includes. */
  @Test
  void testBasicLineReading() throws IOException {
    String content = "line1\nline2\nline3\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertEquals("line1", reader.readLine());
      assertEquals("line2", reader.readLine());
      assertEquals("line3", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test reading from InputStream. */
  @Test
  void testInputStreamReading() throws IOException {
    String content = "line1\nline2\n";
    InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    try (EntryReader reader = new EntryReader(inputStream, "test")) {
      assertEquals("line1", reader.readLine());
      assertEquals("line2", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test reading from a file. */
  @Test
  void testFileReading() throws IOException {
    Path testFile = tempDir.resolve("test.txt");
    Files.write(testFile, "line1\nline2\nline3\n".getBytes(StandardCharsets.UTF_8));

    try (EntryReader reader = new EntryReader(testFile)) {
      assertEquals("line1", reader.readLine());
      assertEquals("line2", reader.readLine());
      assertEquals("line3", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test comment removal. */
  @Test
  void testCommentRemoval() throws IOException {
    String content = "line1\n% comment line\nline2 % inline comment\nline3\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content),
            "test",
            EntryFormat.DEFAULT,
            CommentFormat.TEX_AT_START_OF_LINE,
            null)) {
      assertEquals("line1", reader.readLine());
      assertEquals("line2 % inline comment", reader.readLine()); // no inline comment removal
      assertEquals("line3", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test that lines that are entirely comments are skipped. */
  @Test
  void testFullLineCommentSkipped() throws IOException {
    String content = "line1\n% full comment\nline2\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content),
            "test",
            EntryFormat.DEFAULT,
            CommentFormat.TEX_AT_START_OF_LINE,
            null)) {
      assertEquals("line1", reader.readLine());
      assertEquals("line2", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test iterator functionality. */
  @Test
  void test_iterator() throws IOException {
    String content = "line1\nline2\nline3\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      List<String> lines = new ArrayList<>();
      for (String line : reader) {
        lines.add(line);
      }
      assertEquals(3, lines.size());
      assertEquals("line1", lines.get(0));
      assertEquals("line2", lines.get(1));
      assertEquals("line3", lines.get(2));
    }
  }

  /** Test hasNext() method. */
  @Test
  void test_hasNext() throws IOException {
    String content = "line1\nline2\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertTrue(reader.hasNext());
      reader.next();
      assertTrue(reader.hasNext());
      reader.next();
      assertFalse(reader.hasNext());
    }
  }

  /** Test next() method throws NoSuchElementException at end. */
  @Test
  void testNextThrowsAtEnd() throws IOException {
    String content = "line1\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      reader.next();
      assertThrows(NoSuchElementException.class, reader::next);
    }
  }

  /** Test remove() is not supported. */
  @Test
  void testRemoveNotSupported() throws IOException {
    String content = "line1\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertThrows(UnsupportedOperationException.class, reader::remove);
    }
  }

  /** Test getEntry() for blank-line-separated entries. */
  @Test
  void testGetEntryBlankSeparated() throws IOException {
    String content1 = "para1 line1\npara1 line2\n\npara2 line1\npara2 line2\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content1), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertEquals("para1 line1\npara1 line2\n", reader.getEntry().body);
      assertEquals("para2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }

    String content2 = "para1 line1\npara1 line2\n\n\npara2 line1\npara2 line2";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content2), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertEquals("para1 line1\npara1 line2\n", reader.getEntry().body);
      assertEquals("para2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }

    String content3 = "\n\n\npara1 line1\npara1 line2\n\npara2 line1\npara2 line2\n\n\n\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content3), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertEquals("para1 line1\npara1 line2\n", reader.getEntry().body);
      assertEquals("para2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }
  }

  /** Test getEntry() for two-blank-line-separated entries. */
  @Test
  void testGetEntryTwoBlankSeparated() throws IOException {
    String content1 = "para1 line1\npara1 line2\n\npara2 line1\npara2 line2\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content1),
            "test",
            EntryFormat.TWO_BLANK_LINES,
            CommentFormat.NONE,
            null)) {
      assertEquals(
          "para1 line1\npara1 line2\n\npara2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }

    String content1a = "para1 line1\npara1 line2\n \npara2 line1\npara2 line2\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content1a),
            "test",
            EntryFormat.TWO_BLANK_LINES,
            CommentFormat.NONE,
            null)) {
      assertEquals(
          "para1 line1\npara1 line2\n \npara2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }

    String content2 = "para1 line1\npara1 line2\n\n\npara2 line1\npara2 line2";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content2),
            "test",
            EntryFormat.TWO_BLANK_LINES,
            CommentFormat.NONE,
            null)) {
      assertEquals("para1 line1\npara1 line2\n", reader.getEntry().body);
      assertEquals("para2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }

    String content2a = "para1 line1\npara1 line2\n \n\npara2 line1\npara2 line2";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content2a),
            "test",
            EntryFormat.TWO_BLANK_LINES,
            CommentFormat.NONE,
            null)) {
      assertEquals("para1 line1\npara1 line2\n", reader.getEntry().body);
      assertEquals("para2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }

    String content3 = "\n\n\npara1 line1\npara1 line2\n\n\n\npara2 line1\npara2 line2\n\n\n\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content3),
            "test",
            EntryFormat.TWO_BLANK_LINES,
            CommentFormat.NONE,
            null)) {
      assertEquals("para1 line1\npara1 line2\n", reader.getEntry().body);
      assertEquals("para2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }

    String content3a =
        "\n \n \npara1 line1\npara1 line2\n \n \n\npara2 line1\npara2 line2\n \n \n \n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content3a),
            "test",
            EntryFormat.TWO_BLANK_LINES,
            CommentFormat.NONE,
            null)) {
      assertEquals("para1 line1\npara1 line2\n", reader.getEntry().body);
      assertEquals("para2 line1\npara2 line2\n", reader.getEntry().body);
      assertNull(reader.getEntry());
    }
  }

  /** Test getEntry() with start/stop patterns. */
  @Test
  void testGetEntryWithStartStop() throws IOException {
    String content =
        "START entry1\nentry1 line2\nentry1 line3\nEND\nSTART entry2\nentry2 line2\nEND\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content),
            "test",
            new EntryFormat("^START (.*)$", "^END$", false, false),
            CommentFormat.NONE,
            null)) {

      EntryReader.Entry entry1 = reader.getEntry();
      assertNotNull(entry1);
      assertEquals("entry1", entry1.firstLine);
      assertTrue(entry1.body.contains("entry1"));
      assertTrue(entry1.body.contains("entry1 line2"));
      assertTrue(entry1.body.contains("entry1 line3"));
      assertFalse(entry1.shortEntry);

      EntryReader.Entry entry2 = reader.getEntry();
      assertNotNull(entry2);
      assertEquals("entry2", entry2.firstLine);
      assertTrue(entry2.body.contains("entry2"));
      assertFalse(entry2.shortEntry);

      assertNull(reader.getEntry());
    }
  }

  /** Test line number tracking. */
  @Test
  void testLineNumberTracking() throws IOException {
    String content = "line1\nline2\nline3\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      reader.readLine();
      assertEquals(1, reader.getLineNumber());
      reader.readLine();
      assertEquals(2, reader.getLineNumber());
      reader.readLine();
      assertEquals(3, reader.getLineNumber());
    }
  }

  /** Test setLineNumber(). */
  @Test
  void test_setLineNumber() throws IOException {
    String content = "line1\nline2\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      reader.readLine();
      reader.setLineNumber(10);
      assertEquals(10, reader.getLineNumber());
    }
  }

  /** Test getFileName(). */
  @Test
  void test_getFileName() throws IOException {
    String content = "line1\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content),
            "myfile.txt",
            EntryFormat.DEFAULT,
            CommentFormat.NONE,
            null)) {
      assertEquals("myfile.txt", reader.getFileName());
    }
  }

  /** Test putback(). */
  @Test
  void test_putback() throws IOException {
    String content = "line1\nline2\nline3\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      String line1 = reader.readLine();
      assertEquals("line1", line1);
      reader.putback(line1);
      assertEquals("line1", reader.readLine());
      assertEquals("line2", reader.readLine());
    }
  }

  /** Test that putback() cannot be called twice in a row. */
  @Test
  void testPutbackTwiceThrows() throws IOException {
    String content = "line1\nline2\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      String line1 = reader.readLine();
      reader.putback(line1);
      assertThrows(Error.class, () -> reader.putback("line2"));
    }
  }

  /** Test Entry.getDescription() without regex. */
  @Test
  void testEntryGetDescriptionNoRegex() throws IOException {
    String content = "first line\nsecond line\n\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      EntryReader.Entry entry = reader.getEntry();
      assertNotNull(entry);
      assertEquals("first line", entry.getDescription(null));
    }
  }

  /** Test Entry.getDescription() with regex match. */
  @Test
  void testEntryGetDescriptionWithRegex() throws IOException {
    String content = "Some text with [DESCRIPTION] in body\nmore text\n\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      EntryReader.Entry entry = reader.getEntry();
      assertNotNull(entry);
      String description = entry.getDescription(java.util.regex.Pattern.compile("\\[.*?\\]"));
      assertEquals("[DESCRIPTION]", description);
    }
  }

  /** Test Entry.getDescription() with regex that doesn't match. */
  @Test
  void testEntryGetDescriptionNoMatch() throws IOException {
    String content = "first line\nsecond line\n\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      EntryReader.Entry entry = reader.getEntry();
      assertNotNull(entry);
      String description = entry.getDescription(java.util.regex.Pattern.compile("NOMATCH"));
      assertEquals("first line", description); // falls back to firstLine
    }
  }

  /** Test with empty input. */
  @Test
  void testEmptyInput() throws IOException {
    String content = "";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertNull(reader.readLine());
      assertFalse(reader.hasNext());
      assertNull(reader.getEntry());
    }
  }

  /** Test with only blank lines. */
  @Test
  void testOnlyBlankLines() throws IOException {
    String content = "\n\n\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertNull(reader.getEntry()); // blank lines are skipped
    }
  }

  /** Test reading File constructor. */
  @Test
  void testFileConstructor() throws IOException {
    Path testFile = tempDir.resolve("test.txt");
    Files.write(testFile, "line1\nline2\n".getBytes(StandardCharsets.UTF_8));

    try (EntryReader reader = new EntryReader(testFile.toFile())) {
      assertEquals("line1", reader.readLine());
      assertEquals("line2", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test that iterator returns the same instance. */
  @Test
  void testIteratorReturnsSameInstance() throws IOException {
    String content = "line1\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      assertSame(reader, reader.iterator()); // identity test
    }
  }

  /** Test mixed comments and content. */
  @Test
  void testMixedCommentsAndContent() throws IOException {
    String content = "# comment1\nline1\n# comment2\n# comment3\nline2\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content),
            "test",
            EntryFormat.DEFAULT,
            CommentFormat.SHELL_AT_START_OF_LINE,
            null)) {
      assertEquals("line1", reader.readLine());
      assertEquals("line2", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test getEntry() with leading blank lines. */
  @Test
  void testGetEntryWithLeadingBlankLines() throws IOException {
    String content = "\n\nline1\nline2\n\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, CommentFormat.NONE, null)) {
      EntryReader.Entry entry = reader.getEntry();
      assertNotNull(entry);
      assertEquals("line1", entry.firstLine);
      assertNull(reader.getEntry());
    }
  }

  /** Test Entry metadata (filename and lineNumber). */
  @Test
  void testEntryMetadata() throws IOException {
    String content = "\nline1\nline2\n\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content),
            "testfile.txt",
            EntryFormat.DEFAULT,
            CommentFormat.NONE,
            null)) {
      EntryReader.Entry entry = reader.getEntry();
      assertNotNull(entry);
      assertEquals("testfile.txt", entry.filename);
      assertEquals(2, entry.lineNumber); // line 2 after the leading blank line
    }
  }

  /** A comment format that has both single-line and multi-line comments. */
  private static final CommentFormat SHELL_AND_HTML = new CommentFormat("#.*", "<!--", "-->");

  /** An entry format that enables Markdown fenced code blocks. */
  private static final EntryFormat MARKDOWN_ENTRY =
      new EntryFormat((java.util.regex.Pattern) null, (java.util.regex.Pattern) null, false, true);

  /** Test multi-line comments. */
  @Test
  void testMultilineComments() throws IOException {
    String content = String.join(System.lineSeparator(), "<!--", "line1", "line2", "line3", "-->");

    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {
      assertNull(reader.getEntry());
    }
  }

  /** Test multi-line comments surrounded by content. */
  @Test
  void testMultilineCommentsWithContent() throws IOException {
    String content =
        String.join(
            System.lineSeparator(),
            "line1",
            "line2",
            "<!--",
            "cline1",
            "cline2",
            "cline3",
            "-->",
            "line3");

    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {

      assertEquals("line1", reader.readLine());
      assertEquals("line2", reader.readLine());
      assertEquals("line3", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test multi-line comments that contain a comment and a blank line inside. */
  @Test
  void testMultilineCommentsWithCommentInside() throws IOException {
    String content =
        String.join(
            System.lineSeparator(), "<!--", "cline1", "# comment", "cline2", "", "cline3", "-->");

    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {

      assertNull(reader.getEntry());
    }
  }

  /** Test fenced code blocks (including a blank line inside). */
  @Test
  void testFencedCodeBlockWithBlankLine() throws IOException {
    String content =
        String.join(
            System.lineSeparator(),
            "pre",
            "```sh",
            "code1",
            "", // blank line inside the fenced block
            "code2",
            "```",
            "post");

    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {

      assertEquals("pre", reader.readLine());
      assertEquals("```sh", reader.readLine());
      assertEquals("code1", reader.readLine());
      assertEquals("", reader.readLine());
      assertEquals("code2", reader.readLine());
      assertEquals("```", reader.readLine());
      assertEquals("post", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Unterminated multi-line comment should throw an error. */
  @Test
  void testUnterminatedMultilineComment() throws IOException {
    String content = String.join(System.lineSeparator(), "<!--", "line1", "line2");

    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {

      assertThrows(IllegalArgumentException.class, reader::getEntry);
    }
  }

  /** If both // and /* occur, and // comes first, then // wins (multiline is ignored). */
  @Test
  void testCommentPrecedence_singleLineBeforeMultiline() throws IOException {
    // On the first line, // starts before /*, so we should NOT enter multiline mode.
    // The next line that looks like a multiline end should be returned as normal text.
    String content = "code#slc <!-- not-started\n" + "-->still-text\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {
      assertEquals("code", reader.readLine());
      assertEquals("-->still-text", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** If both // and /* occur, and /* comes first, then multiline wins and suffix is preserved. */
  @Test
  void testCommentPrecedence_multilineBeforeSingleLine() throws IOException {
    // /* occurs before //, so multiline is stripped first; then // is stripped from the suffix.
    String content = "code<!--ml-->more#slc\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {
      assertEquals("codemore", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Multiline comment on a single line: preserve prefix and suffix as if comment were absent. */
  @Test
  void testMultilineSameLine_preservePrefixAndSuffix() throws IOException {
    String content = "pre<!--mid-->post\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {
      assertEquals("prepost", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Multiline comment across lines: preserve prefix from first line and suffix from last line. */
  @Test
  void testMultilineAcrossLines_preservePrefixAndSuffix() throws IOException {
    String content = "pre<!--mid\n" + "stillmid\n" + "end-->post\n" + "after\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {
      assertEquals("prepost", reader.readLine());
      assertEquals("after", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** If stripping a multiline comment yields an empty logical line, readLine skips to the next. */
  @Test
  void testMultilineWholeLineSkipped_sameLine() throws IOException {
    String content = "<!--wholeline-->\n" + "x\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {
      assertEquals("x", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** If stripping a multiline comment yields an empty logical line, readLine skips to the next. */
  @Test
  void testMultilineWholeLineSkipped_acrossLines() throws IOException {
    String content = "<!--start\n" + "middle\n" + "end-->\n" + "y\n";
    try (EntryReader reader =
        new EntryReader(
            new StringReader(content), "test", EntryFormat.DEFAULT, SHELL_AND_HTML, null)) {
      assertEquals("y", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /** Test fenced code blocks (including a blank line inside), with Markdown enabled. */
  @Test
  void testFencedCodeBlocksWithBlankLine_markdown() throws IOException {
    String content =
        String.join(
            System.lineSeparator(),
            "pre",
            "```sh",
            "code1",
            "", 
            "code2",
            "```",
            "post");

    try (EntryReader reader =
        new EntryReader(new StringReader(content), "test", MARKDOWN_ENTRY, SHELL_AND_HTML, null)) {

  
      assertEquals("pre", reader.readLine());
      assertEquals("```sh", reader.readLine());
      assertEquals("code1", reader.readLine());
      assertEquals("", reader.readLine());
      assertEquals("code2", reader.readLine());
      assertEquals("```", reader.readLine());
      assertEquals("post", reader.readLine());
      assertNull(reader.readLine());
    }
  }

  /**
   * Test both: comments stripped outside fences, but preserved inside fences (Markdown enabled).
   */
  @Test
  void testFencedCodeBlocksAndComments_markdown() throws IOException {
    String content =
        String.join(
            System.lineSeparator(),
            "pre",
            "<!--",
            "outside",
            "-->",
            "mid",
            "```sh",
            "<!-- inside fence should be preserved -->",
            "",
            "# inside fence should be preserved",
            "code",
            "```",
            "<!--",
            "outside2",
            "-->",
            "post");

    try (EntryReader reader =
        new EntryReader(new StringReader(content), "test", MARKDOWN_ENTRY, SHELL_AND_HTML, null)) {

      // Outside fenced block: multiline comments are stripped.
      assertEquals("pre", reader.readLine());
      assertEquals("mid", reader.readLine());

      // Inside fenced block: lines are returned verbatim (no comment stripping).
      assertEquals("```sh", reader.readLine());
      assertEquals("<!-- inside fence should be preserved -->", reader.readLine());
      assertEquals("", reader.readLine());
      assertEquals("# inside fence should be preserved", reader.readLine());
      assertEquals("code", reader.readLine());
      assertEquals("```", reader.readLine());

      // Outside again: multiline comments stripped.
      assertEquals("post", reader.readLine());
      assertNull(reader.readLine());
    }
  }
}
