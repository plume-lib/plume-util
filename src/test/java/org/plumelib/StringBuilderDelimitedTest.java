package org.plumelib.util;

import org.junit.Test;

@SuppressWarnings({
  "UseCorrectAssertInTests" // `assert` works fine in tests
})
public final class StringBuilderDelimitedTest {

  @Test
  public void testStringBuilderDelimited() {
    compareJoinAndSBD(new String[] {"foo", "bar", "baz"});
    compareJoinAndSBD(new String[] {"foo"});
    compareJoinAndSBD(new String[] {});
  }

  public void compareJoinAndSBD(String[] strings) {
    StringBuilderDelimited sbd = new StringBuilderDelimited(",");
    for (String str : strings) {
      sbd.add(str);
    }
    assert sbd.toString().equals(UtilPlume.join(strings, ","));
  }
}
