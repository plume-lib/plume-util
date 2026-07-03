package org.plumelib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/** Test the MapsP class. */
final class MapsPTest {

  MapsPTest() {}

  /** The system-specific line separator. */
  private static final String lineSep = System.lineSeparator();

  private static final Map<Integer, String> tensNumberConversion = new TreeMap<>();

  static {
    tensNumberConversion.put(2, "twenty");
    tensNumberConversion.put(3, "thirty");
    tensNumberConversion.put(4, "forty");
    tensNumberConversion.put(5, "fifty");
    tensNumberConversion.put(6, "sixty");
    tensNumberConversion.put(7, "seventy");
    tensNumberConversion.put(8, "eighty");
    tensNumberConversion.put(9, "ninety");
  }

  private static Map<String, Integer> giftsToQuantity1 = new TreeMap<>();
  private static Map<String, Integer> giftsToQuantity2 = new TreeMap<>();
  private static Map<String, Integer> giftsToQuantity3 = new TreeMap<>();
  private static Map<String, Integer> giftsToQuantity4 = new TreeMap<>();

  static {
    giftsToQuantity1.put("partridge in a pear tree", 1);
    giftsToQuantity1.put("turtle doves", 2);
    giftsToQuantity1.put("French hens", 3);
    giftsToQuantity3.put("calling birds", 4);
    giftsToQuantity4.put("gold rings", 5);
    giftsToQuantity4.put("geese a-laying", 6);
    giftsToQuantity4.put("swans a-swimming", 7);
    giftsToQuantity4.put("maids a-milking", 8);
    giftsToQuantity4.put("ladies dancing", 9);
    giftsToQuantity4.put("lords a-leaping", 10);
    giftsToQuantity4.put("pipers piping", 11);
    giftsToQuantity4.put("drummers drumming", 12);
  }

  private static final Map<String, Map<String, Integer>> allGifts = new TreeMap<>();

  static {
    allGifts.put("A", giftsToQuantity1);
    allGifts.put("B", giftsToQuantity2);
    allGifts.put("C", giftsToQuantity3);
    allGifts.put("D", giftsToQuantity4);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // The tests themselves
  //

  @Test
  void test_mapToString() {

    // mapToStringMultiLine(Appendable sb, Map<K, V> m, String linePrefix) {
    // mapMapToStringMultiLine(
    //           Appendable sb, String innerHeader, Map<K1, Map<K2, V2>> mapMap, String linePrefix)
    // mapToStringMultiLine(Map<K, V> m) {
    // mapToStringMultiLine(Map<K, V> m, String linePrefix) {
    // mapToStringAndClassMultiLine(Map<K, V> m) {

    StringBuilder sb1 = new StringBuilder();
    MapsP.mapToStringMultiLine(sb1, giftsToQuantity1, "   ");
    assertEquals(
        String.join(
            lineSep,
            "   French hens => 3",
            "   partridge in a pear tree => 1",
            "   turtle doves => 2",
            ""),
        sb1.toString());
    StringBuilder sb2 = new StringBuilder();
    MapsP.mapMapToStringMultiLine(sb2, "inner:", allGifts, "x");
    assertEquals(
        String.join(
            lineSep,
            "xinner:A",
            "x  French hens => 3",
            "x  partridge in a pear tree => 1",
            "x  turtle doves => 2",
            "xinner:B",
            "xinner:C",
            "x  calling birds => 4",
            "xinner:D",
            "x  drummers drumming => 12",
            "x  geese a-laying => 6",
            "x  gold rings => 5",
            "x  ladies dancing => 9",
            "x  lords a-leaping => 10",
            "x  maids a-milking => 8",
            "x  pipers piping => 11",
            "x  swans a-swimming => 7",
            ""),
        sb2.toString());
  }

  //
  // Map to string
  //

  // First, versions that append to an Appendable.

  // Second, versions that return a String.

}
