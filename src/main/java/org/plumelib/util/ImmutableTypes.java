package org.plumelib.util;

import java.util.HashSet;
import java.util.Set;

/** Records all the immutable types in the JDK, and can be queried. */
public class ImmutableTypes {

  /** Do not instantiate. */
  private ImmutableTypes() {
    throw new Error("Do not instantiate");
  }

  /**
   * The names of the immutable types. This is public so that clients can extend it with bug fixes
   * or application-specific types. (Or, make a pull request to add more types to this file.)
   *
   * <p>The set generally does not contain interfaces, because types that implement an interface may
   * be mutable even if the interface provides no mutating methods.
   */
  // It might be more efficient to use Class objects rather than Strings, but that would require
  // everything in the list to be loaded into the JDK at run time.
  public static Set<String> immutableTypeNames = new HashSet<>();

  // The set contains nothing in package javax.management, which is littered with comments like
  // "Instances of this class are immutable.  Subclasses may be mutable but this is not
  // recommended.", which means there is no guarantee that variables declared with the given type
  // are immutable.

  // TODO: Need to add enum types; see the 149 matches for "public.*\benum\b" in the JDK.

  static {
    immutableTypeNames.add("boolean");
    immutableTypeNames.add("byte");
    immutableTypeNames.add("char");
    immutableTypeNames.add("double");
    immutableTypeNames.add("float");
    immutableTypeNames.add("int");
    immutableTypeNames.add("long");
    immutableTypeNames.add("short");

    immutableTypeNames.add("com.sun.jmx.snmp.ThreadContext");
    immutableTypeNames.add("com.sun.security.auth.LdapPrincipal");
    immutableTypeNames.add("com.sun.security.auth.UserPrincipal");
    immutableTypeNames.add("java.awt.AWTEventMulticaster");
    immutableTypeNames.add("java.awt.AWTKeyStroke");
    immutableTypeNames.add("java.awt.BasicStroke");
    immutableTypeNames.add("java.awt.Color");
    immutableTypeNames.add("java.awt.Composite");
    immutableTypeNames.add("java.awt.Cursor");
    immutableTypeNames.add("java.awt.Font");
    immutableTypeNames.add("java.awt.GradientPaint,");
    immutableTypeNames.add("java.awt.LinearGradientPaint");
    immutableTypeNames.add("java.awt.RadialGradientPaint,");
    immutableTypeNames.add("java.awt.RenderingHints");
    immutableTypeNames.add("java.awt.font.GlyphMetrics");
    immutableTypeNames.add("java.awt.font.GraphicAttribute");
    immutableTypeNames.add("java.awt.font.TextLayout");
    immutableTypeNames.add("java.io.File");
    immutableTypeNames.add("java.io.ObjectStreamClass");
    immutableTypeNames.add("java.lang.Class");
    immutableTypeNames.add("java.lang.String");
    immutableTypeNames.add("java.lang.StackTraceElement");
    immutableTypeNames.add("java.lang.invoke.MethodHandle");
    immutableTypeNames.add("java.lang.invoke.MethodType");
    immutableTypeNames.add("java.math.BigDecimal");
    immutableTypeNames.add("java.math.BigInteger");
    immutableTypeNames.add("java.math.MathContext");
    immutableTypeNames.add("java.net.Inet4Address");
    immutableTypeNames.add("java.net.Inet6Address");
    immutableTypeNames.add("java.net.InetAddress");
    immutableTypeNames.add("java.net.InetSocketAddress");
    immutableTypeNames.add("java.net.Proxy");
    immutableTypeNames.add("java.net.SocketAddress");
    immutableTypeNames.add("java.net.URI");
    immutableTypeNames.add("java.net.URL");
    immutableTypeNames.add("java.nio.charset.Charset");
    immutableTypeNames.add("java.nio.file.Path");
    immutableTypeNames.add("java.nio.file.WatchEvent");
    immutableTypeNames.add("java.nio.file.attribute.AclEntry");
    immutableTypeNames.add("java.nio.file.attribute.FileTime");
    immutableTypeNames.add("java.security.AlgorithmConstraints");
    immutableTypeNames.add("java.security.CodeSigner");
    immutableTypeNames.add("java.security.Permission");
    immutableTypeNames.add("java.security.Provider");
    immutableTypeNames.add("java.security.Timestamp");
    immutableTypeNames.add("java.security.cert.CertPath");
    immutableTypeNames.add("java.security.cert.PolicyNode");
    immutableTypeNames.add("java.security.cert.PolicyQualifierInfo");
    immutableTypeNames.add("java.security.cert.TrustAnchor");
    immutableTypeNames.add("java.security.spec.DSAGenParameterSpec");
    immutableTypeNames.add("java.security.spec.ECFieldF2m");
    immutableTypeNames.add("java.security.spec.ECFieldFp");
    immutableTypeNames.add("java.security.spec.ECGenParameterSpec");
    immutableTypeNames.add("java.security.spec.ECParameterSpec");
    immutableTypeNames.add("java.security.spec.ECPoint");
    immutableTypeNames.add("java.security.spec.ECPrivateKeySpec");
    immutableTypeNames.add("java.security.spec.ECPublicKeySpec");
    immutableTypeNames.add("java.security.spec.EllipticCurve");
    immutableTypeNames.add("java.text.AttributedCharacterIterator");
    immutableTypeNames.add("java.time.Clock");
    immutableTypeNames.add("java.time.DateTimeException");
    immutableTypeNames.add("java.time.DayOfWeek");
    immutableTypeNames.add("java.time.Duration");
    immutableTypeNames.add("java.time.Instant");
    immutableTypeNames.add("java.time.LocalDate");
    immutableTypeNames.add("java.time.LocalDateTime");
    immutableTypeNames.add("java.time.LocalTime");
    immutableTypeNames.add("java.time.Month");
    immutableTypeNames.add("java.time.MonthDay");
    immutableTypeNames.add("java.time.OffsetDateTime");
    immutableTypeNames.add("java.time.OffsetTime");
    immutableTypeNames.add("java.time.Period");
    immutableTypeNames.add("java.time.Ser");
    immutableTypeNames.add("java.time.Year");
    immutableTypeNames.add("java.time.YearMonth");
    immutableTypeNames.add("java.time.ZoneId");
    immutableTypeNames.add("java.time.ZoneOffset");
    immutableTypeNames.add("java.time.ZoneRegion");
    immutableTypeNames.add("java.time.ZonedDateTime");
    immutableTypeNames.add("java.time.chrono.AbstractChronology");
    immutableTypeNames.add("java.time.chrono.ChronoLocalDate");
    immutableTypeNames.add("java.time.chrono.ChronoLocalDateImpl");
    immutableTypeNames.add("java.time.chrono.ChronoLocalDateTime");
    immutableTypeNames.add("java.time.chrono.ChronoLocalDateTimeImpl");
    immutableTypeNames.add("java.time.chrono.ChronoPeriod");
    immutableTypeNames.add("java.time.chrono.ChronoPeriodImpl");
    immutableTypeNames.add("java.time.chrono.ChronoZonedDateTime");
    immutableTypeNames.add("java.time.chrono.ChronoZonedDateTimeImpl");
    immutableTypeNames.add("java.time.chrono.Chronology");
    immutableTypeNames.add("java.time.chrono.Era");
    immutableTypeNames.add("java.time.chrono.HijrahChronology");
    immutableTypeNames.add("java.time.chrono.HijrahDate");
    immutableTypeNames.add("java.time.chrono.HijrahEra");
    immutableTypeNames.add("java.time.chrono.IsoChronology");
    immutableTypeNames.add("java.time.chrono.IsoEra");
    immutableTypeNames.add("java.time.chrono.JapaneseChronology");
    immutableTypeNames.add("java.time.chrono.JapaneseDate");
    immutableTypeNames.add("java.time.chrono.JapaneseEra");
    immutableTypeNames.add("java.time.chrono.MinguoChronology");
    immutableTypeNames.add("java.time.chrono.MinguoDate");
    immutableTypeNames.add("java.time.chrono.MinguoEra");
    immutableTypeNames.add("java.time.chrono.Ser");
    immutableTypeNames.add("java.time.chrono.ThaiBuddhistChronology");
    immutableTypeNames.add("java.time.chrono.ThaiBuddhistDate");
    immutableTypeNames.add("java.time.chrono.ThaiBuddhistEra");
    immutableTypeNames.add("java.time.format.DateTimeFormatter");
    immutableTypeNames.add("java.time.format.DateTimeFormatterBuilder");
    immutableTypeNames.add("java.time.format.DateTimeParseContext");
    immutableTypeNames.add("java.time.format.DateTimeParseException");
    immutableTypeNames.add("java.time.format.DateTimePrintContext");
    immutableTypeNames.add("java.time.format.DateTimeTextProvider");
    immutableTypeNames.add("java.time.format.DecimalStyle");
    immutableTypeNames.add("java.time.format.FormatStyle");
    immutableTypeNames.add("java.time.format.Parsed");
    immutableTypeNames.add("java.time.format.ResolverStyle");
    immutableTypeNames.add("java.time.format.SignStyle");
    immutableTypeNames.add("java.time.format.TextStyle");
    immutableTypeNames.add("java.time.temporal.ChronoField");
    immutableTypeNames.add("java.time.temporal.ChronoUnit");
    immutableTypeNames.add("java.time.temporal.IsoFields");
    immutableTypeNames.add("java.time.temporal.JulianFields");
    immutableTypeNames.add("java.time.temporal.Temporal");
    immutableTypeNames.add("java.time.temporal.TemporalAccessor");
    immutableTypeNames.add("java.time.temporal.TemporalAdjuster");
    immutableTypeNames.add("java.time.temporal.TemporalAdjusters");
    immutableTypeNames.add("java.time.temporal.TemporalAmount");
    immutableTypeNames.add("java.time.temporal.TemporalField");
    immutableTypeNames.add("java.time.temporal.TemporalQueries");
    immutableTypeNames.add("java.time.temporal.TemporalQuery");
    immutableTypeNames.add("java.time.temporal.TemporalUnit");
    immutableTypeNames.add("java.time.temporal.UnsupportedTemporalTypeException");
    immutableTypeNames.add("java.time.temporal.ValueRange");
    immutableTypeNames.add("java.time.temporal.WeekFields");
    immutableTypeNames.add("java.time.zone.Ser");
    immutableTypeNames.add("java.time.zone.TzdbZoneRulesProvider");
    immutableTypeNames.add("java.time.zone.ZoneOffsetTransition");
    immutableTypeNames.add("java.time.zone.ZoneOffsetTransitionRule");
    immutableTypeNames.add("java.time.zone.ZoneRules");
    immutableTypeNames.add("java.time.zone.ZoneRulesException");
    immutableTypeNames.add("java.time.zone.ZoneRulesProvider");
    immutableTypeNames.add("java.util.Locale");
    immutableTypeNames.add("java.util.UUID");
    immutableTypeNames.add("java.util.regex.Pattern");
    immutableTypeNames.add("javax.naming.StringRefAddr");
    immutableTypeNames.add("javax.net.ssl.CertPathTrustManagerParameters");
    immutableTypeNames.add("javax.net.ssl.SNIHostName");
    immutableTypeNames.add("javax.net.ssl.SNIMatcher");
    immutableTypeNames.add("javax.net.ssl.SNIServerName");
    immutableTypeNames.add("javax.print.attribute.DateTimeSyntax");
    immutableTypeNames.add("javax.print.attribute.IntegerSyntax");
    immutableTypeNames.add("javax.print.attribute.ResolutionSyntax");
    immutableTypeNames.add("javax.print.attribute.SetOfIntegerSyntax");
    immutableTypeNames.add("javax.print.attribute.Size2DSyntax");
    immutableTypeNames.add("javax.print.attribute.TextSyntax");
    immutableTypeNames.add("javax.print.attribute.URISyntax");
    immutableTypeNames.add("javax.smartcardio.ATR");
    immutableTypeNames.add("javax.smartcardio.CommandAPDU");
    immutableTypeNames.add("javax.smartcardio.ResponseAPDU");
    immutableTypeNames.add("javax.swing.KeyStroke");
    immutableTypeNames.add("javax.swing.plaf.synth.SynthContext");
    immutableTypeNames.add("javax.swing.text.TabSet");
    immutableTypeNames.add("javax.swing.text.TabStop");
    immutableTypeNames.add("sun.awt.SunHints");
    immutableTypeNames.add("sun.awt.im.InputMethodLocator");
    immutableTypeNames.add("sun.jvmstat.perfdata.monitor.MonitorStatus");
    immutableTypeNames.add("sun.net.www.protocol.http.HttpCallerInfo");
    immutableTypeNames.add("sun.security.internal.spec.TlsKeyMaterialParameterSpec");
    immutableTypeNames.add("sun.security.internal.spec.TlsKeyMaterialSpec");
    immutableTypeNames.add("sun.security.internal.spec.TlsMasterSecretParameterSpec");
    immutableTypeNames.add("sun.security.internal.spec.TlsPrfParameterSpec");
    immutableTypeNames.add("sun.security.internal.spec.TlsRsaPremasterSecretParameterSpec");
    immutableTypeNames.add("sun.security.jca.ProviderList");
    immutableTypeNames.add("sun.security.krb5.PrincipalName");
    immutableTypeNames.add("sun.security.krb5.Realm");
    immutableTypeNames.add("sun.security.krb5.internal.KerberosTime");
    immutableTypeNames.add("sun.security.provider.certpath.PolicyNodeImpl");
    immutableTypeNames.add("sun.security.ssl.CipherSuite");
    immutableTypeNames.add("sun.security.ssl.CipherSuiteList");
    immutableTypeNames.add("sun.security.ssl.HelloExtensions");
    immutableTypeNames.add("sun.security.ssl.ProtocolList");
    immutableTypeNames.add("sun.security.util.ECKeySizeParameterSpec");
    immutableTypeNames.add("sun.security.validator.PKIXValidator");
    immutableTypeNames.add("sun.security.validator.SimpleValidator");
    immutableTypeNames.add("sun.security.x509.AVA");
    immutableTypeNames.add("sun.security.x509.RDN");
    immutableTypeNames.add("sun.security.x509.X500Name");
  }

  /**
   * Returns true iff the class of the given name is immutable.
   *
   * @param typeName the fully-qualified name of the type
   * @return true iff the class of the given name is immutable
   */
  public static boolean isImmutable(String typeName) {
    return immutableTypeNames.contains(typeName);
  }
}
