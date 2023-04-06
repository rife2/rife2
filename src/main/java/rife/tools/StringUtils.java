/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.config.RifeConfig;
import rife.datastructures.DocumentPosition;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General purpose class containing common {@code String} manipulation
 * methods.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public final class StringUtils {
    public static String ENCODING_US_ASCII = StandardCharsets.US_ASCII.name();
    public static String ENCODING_ISO_8859_1 = StandardCharsets.ISO_8859_1.name();
    public static String ENCODING_ISO_8859_2 = "ISO-8859-2";
    public static String ENCODING_ISO_8859_5 = "ISO-8859-5";
    public static String ENCODING_UTF_8 = StandardCharsets.UTF_8.name();
    public static String ENCODING_UTF_16BE = StandardCharsets.UTF_16BE.name();
    public static String ENCODING_UTF_16LE = StandardCharsets.UTF_16LE.name();
    public static String ENCODING_UTF_16 = StandardCharsets.UTF_16.name();

    public static Charset CHARSET_US_ASCII = Charset.forName(StringUtils.ENCODING_US_ASCII);

    public static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    public static final char[] HEX_DIGITS_LOWER = "0123456789abcdef".toCharArray();

    enum BbcodeOption {
        SHORTEN_URL, SANITIZE_URL, CONVERT_BARE_URLS, NO_FOLLOW_LINKS
    }

    public static final Pattern BBCODE_COLOR = Pattern.compile("\\[color\\s*=\\s*([#\\w]*)\\s*\\]", Pattern.CASE_INSENSITIVE);
    public static final Pattern BBCODE_SIZE = Pattern.compile("\\[size\\s*=\\s*([+\\-]?[0-9]*)\\s*\\]", Pattern.CASE_INSENSITIVE);
    public static final Pattern BBCODE_URL_SHORT = Pattern.compile("\\[url\\]\\s*([^\\s]*)\\s*\\[\\/url\\]", Pattern.CASE_INSENSITIVE);
    public static final Pattern BBCODE_URL_LONG = Pattern.compile("\\[url=([^\\[]*)\\]([^\\[]*)\\[/url\\]", Pattern.CASE_INSENSITIVE);
    public static final Pattern BBCODE_IMG = Pattern.compile("\\[img\\]\\s*([^\\s]*)\\s*\\[\\/img\\]", Pattern.CASE_INSENSITIVE);
    public static final Pattern BBCODE_QUOTE_LONG = Pattern.compile("\\[quote=([^\\]]+\\]*)\\]", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    public static final Pattern BBCODE_BAREURL = Pattern.compile("(?:[^\"'=>\\]]|^)((?:http|ftp)s?://(?:%[\\p{Digit}A-Fa-f][\\p{Digit}A-Fa-f]|[\\-_\\.!~*';\\|/?:@#&=\\+$,\\p{Alnum}])+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Map<Character, String> AGGRESSIVE_HTML_ENCODE_MAP = new HashMap<>();
    private static final Map<Character, String> DEFENSIVE_HTML_ENCODE_MAP = new HashMap<>();
    private static final Map<Character, String> XML_ENCODE_MAP = new HashMap<>();
    private static final Map<Character, String> STRING_ENCODE_MAP = new HashMap<>();
    private static final Map<Character, String> SQL_ENCODE_MAP = new HashMap<>();
    private static final Map<Character, String> LATEX_ENCODE_MAP = new HashMap<>();

    private static final Map<String, Character> HTML_DECODE_MAP = new HashMap<>();

    private static final HtmlEncoderFallbackHandler HTML_ENCODER_FALLBACK = new HtmlEncoderFallbackHandler();

    static {
        // Html encoding mapping according to the HTML 4.0 spec
        // http://www.w3.org/TR/REC-html40/sgml/entities.html

        // Special characters for HTML
        AGGRESSIVE_HTML_ENCODE_MAP.put('\u0026', "&amp;");
        AGGRESSIVE_HTML_ENCODE_MAP.put('\u003C', "&lt;");
        AGGRESSIVE_HTML_ENCODE_MAP.put('\u003E', "&gt;");
        AGGRESSIVE_HTML_ENCODE_MAP.put('\u0022', "&quot;");

        DEFENSIVE_HTML_ENCODE_MAP.put('\u0152', "&OElig;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0153', "&oelig;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0160', "&Scaron;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0161', "&scaron;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0178', "&Yuml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u02C6', "&circ;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u02DC', "&tilde;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2002', "&ensp;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2003', "&emsp;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2009', "&thinsp;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u200C', "&zwnj;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u200D', "&zwj;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u200E', "&lrm;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u200F', "&rlm;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2013', "&ndash;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2014', "&mdash;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2018', "&lsquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2019', "&rsquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u201A', "&sbquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u201C', "&ldquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u201D', "&rdquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u201E', "&bdquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2020', "&dagger;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2021', "&Dagger;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2030', "&permil;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2039', "&lsaquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u203A', "&rsaquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u20AC', "&euro;");

        // Character entity references for ISO 8859-1 characters
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A0', "&nbsp;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A1', "&iexcl;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A2', "&cent;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A3', "&pound;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A4', "&curren;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A5', "&yen;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A6', "&brvbar;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A7', "&sect;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A8', "&uml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00A9', "&copy;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00AA', "&ordf;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00AB', "&laquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00AC', "&not;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00AD', "&shy;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00AE', "&reg;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00AF', "&macr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B0', "&deg;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B1', "&plusmn;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B2', "&sup2;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B3', "&sup3;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B4', "&acute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B5', "&micro;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B6', "&para;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B7', "&middot;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B8', "&cedil;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00B9', "&sup1;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00BA', "&ordm;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00BB', "&raquo;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00BC', "&frac14;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00BD', "&frac12;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00BE', "&frac34;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00BF', "&iquest;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C0', "&Agrave;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C1', "&Aacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C2', "&Acirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C3', "&Atilde;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C4', "&Auml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C5', "&Aring;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C6', "&AElig;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C7', "&Ccedil;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C8', "&Egrave;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00C9', "&Eacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00CA', "&Ecirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00CB', "&Euml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00CC', "&Igrave;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00CD', "&Iacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00CE', "&Icirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00CF', "&Iuml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D0', "&ETH;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D1', "&Ntilde;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D2', "&Ograve;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D3', "&Oacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D4', "&Ocirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D5', "&Otilde;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D6', "&Ouml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D7', "&times;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D8', "&Oslash;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00D9', "&Ugrave;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00DA', "&Uacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00DB', "&Ucirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00DC', "&Uuml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00DD', "&Yacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00DE', "&THORN;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00DF', "&szlig;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E0', "&agrave;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E1', "&aacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E2', "&acirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E3', "&atilde;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E4', "&auml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E5', "&aring;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E6', "&aelig;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E7', "&ccedil;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E8', "&egrave;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00E9', "&eacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00EA', "&ecirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00EB', "&euml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00EC', "&igrave;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00ED', "&iacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00EE', "&icirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00EF', "&iuml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F0', "&eth;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F1', "&ntilde;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F2', "&ograve;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F3', "&oacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F4', "&ocirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F5', "&otilde;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F6', "&ouml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F7', "&divide;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F8', "&oslash;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00F9', "&ugrave;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00FA', "&uacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00FB', "&ucirc;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00FC', "&uuml;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00FD', "&yacute;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00FE', "&thorn;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u00FF', "&yuml;");

        // Mathematical, Greek and Symbolic characters for HTML
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0192', "&fnof;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0391', "&Alpha;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0392', "&Beta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0393', "&Gamma;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0394', "&Delta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0395', "&Epsilon;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0396', "&Zeta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0397', "&Eta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0398', "&Theta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u0399', "&Iota;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u039A', "&Kappa;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u039B', "&Lambda;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u039C', "&Mu;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u039D', "&Nu;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u039E', "&Xi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u039F', "&Omicron;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A0', "&Pi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A1', "&Rho;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A3', "&Sigma;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A4', "&Tau;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A5', "&Upsilon;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A6', "&Phi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A7', "&Chi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A8', "&Psi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03A9', "&Omega;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B1', "&alpha;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B2', "&beta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B3', "&gamma;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B4', "&delta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B5', "&epsilon;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B6', "&zeta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B7', "&eta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B8', "&theta;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03B9', "&iota;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03BA', "&kappa;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03BB', "&lambda;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03BC', "&mu;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03BD', "&nu;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03BE', "&xi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03BF', "&omicron;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C0', "&pi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C1', "&rho;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C2', "&sigmaf;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C3', "&sigma;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C4', "&tau;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C5', "&upsilon;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C6', "&phi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C7', "&chi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C8', "&psi;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03C9', "&omega;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03D1', "&thetasym;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03D2', "&upsih;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u03D6', "&piv;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2022', "&bull;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2026', "&hellip;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2032', "&prime;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2033', "&Prime;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u203E', "&oline;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2044', "&frasl;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2118', "&weierp;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2111', "&image;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u211C', "&real;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2122', "&trade;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2135', "&alefsym;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2190', "&larr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2191', "&uarr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2192', "&rarr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2193', "&darr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2194', "&harr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u21B5', "&crarr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u21D0', "&lArr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u21D1', "&uArr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u21D2', "&rArr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u21D3', "&dArr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u21D4', "&hArr;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2200', "&forall;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2202', "&part;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2203', "&exist;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2205', "&empty;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2207', "&nabla;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2208', "&isin;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2209', "&notin;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u220B', "&ni;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u220F', "&prod;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2211', "&sum;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2212', "&minus;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2217', "&lowast;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u221A', "&radic;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u221D', "&prop;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u221E', "&infin;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2220', "&ang;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2227', "&and;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2228', "&or;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2229', "&cap;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u222A', "&cup;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u222B', "&int;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2234', "&there4;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u223C', "&sim;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2245', "&cong;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2248', "&asymp;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2260', "&ne;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2261', "&equiv;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2264', "&le;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2265', "&ge;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2282', "&sub;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2283', "&sup;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2284', "&nsub;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2286', "&sube;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2287', "&supe;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2295', "&oplus;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2297', "&otimes;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u22A5', "&perp;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u22C5', "&sdot;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2308', "&lceil;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2309', "&rceil;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u230A', "&lfloor;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u230B', "&rfloor;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2329', "&lang;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u232A', "&rang;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u25CA', "&loz;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2660', "&spades;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2663', "&clubs;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2665', "&hearts;");
        DEFENSIVE_HTML_ENCODE_MAP.put('\u2666', "&diams;");

        var aggresive_entries = AGGRESSIVE_HTML_ENCODE_MAP.entrySet();
        for (var entry : aggresive_entries) {
            HTML_DECODE_MAP.put(entry.getValue(), entry.getKey());
        }

        var defensive_entries = DEFENSIVE_HTML_ENCODE_MAP.entrySet();
        for (var entry : defensive_entries) {
            HTML_DECODE_MAP.put(entry.getValue(), entry.getKey());
        }

        XML_ENCODE_MAP.put('\u0026', "&amp;");
        XML_ENCODE_MAP.put('\'', "&apos;");
        XML_ENCODE_MAP.put('\u0022', "&quot;");
        XML_ENCODE_MAP.put('\u003C', "&lt;");
        XML_ENCODE_MAP.put('\u003E', "&gt;");

        SQL_ENCODE_MAP.put('\'', "''");

        STRING_ENCODE_MAP.put('\\', "\\\\");
        STRING_ENCODE_MAP.put('\n', "\\n");
        STRING_ENCODE_MAP.put('\r', "\\r");
        STRING_ENCODE_MAP.put('\t', "\\t");
        STRING_ENCODE_MAP.put('"', "\\\"");

        LATEX_ENCODE_MAP.put('\\', "\\\\");
        LATEX_ENCODE_MAP.put('#', "\\#");
        LATEX_ENCODE_MAP.put('$', "\\$");
        LATEX_ENCODE_MAP.put('%', "\\%");
        LATEX_ENCODE_MAP.put('&', "\\&");
        LATEX_ENCODE_MAP.put('~', "\\~");
        LATEX_ENCODE_MAP.put('_', "\\_");
        LATEX_ENCODE_MAP.put('^', "\\^");
        LATEX_ENCODE_MAP.put('{', "\\{");
        LATEX_ENCODE_MAP.put('}', "\\}");
        LATEX_ENCODE_MAP.put('\u00A1', "!'");
        LATEX_ENCODE_MAP.put('\u00BF', "?'");
        LATEX_ENCODE_MAP.put('\u00C0', "\\`{A}");
        LATEX_ENCODE_MAP.put('\u00C1', "\\'{A}");
        LATEX_ENCODE_MAP.put('\u00C2', "\\^{A}");
        LATEX_ENCODE_MAP.put('\u00C3', "\\H{A}");
        LATEX_ENCODE_MAP.put('\u00C4', "\\\"{A}");
        LATEX_ENCODE_MAP.put('\u00C5', "\\AA");
        LATEX_ENCODE_MAP.put('\u00C6', "\\AE");
        LATEX_ENCODE_MAP.put('\u00C7', "\\c{C}");
        LATEX_ENCODE_MAP.put('\u00C8', "\\`{E}");
        LATEX_ENCODE_MAP.put('\u00C9', "\\'{E}");
        LATEX_ENCODE_MAP.put('\u00CA', "\\^{E}");
        LATEX_ENCODE_MAP.put('\u00CB', "\\\"{E}");
        LATEX_ENCODE_MAP.put('\u00CC', "\\`{I}");
        LATEX_ENCODE_MAP.put('\u00CD', "\\'{I}");
        LATEX_ENCODE_MAP.put('\u00CE', "\\^{I}");
        LATEX_ENCODE_MAP.put('\u00CF', "\\\"{I}");
// todo \u00D0
        LATEX_ENCODE_MAP.put('\u00D1', "\\H{N}");
        LATEX_ENCODE_MAP.put('\u00D2', "\\`{O}");
        LATEX_ENCODE_MAP.put('\u00D3', "\\'{O}");
        LATEX_ENCODE_MAP.put('\u00D4', "\\^{O}");
        LATEX_ENCODE_MAP.put('\u00D5', "\\H{O}");
        LATEX_ENCODE_MAP.put('\u00D6', "\\\"{O}");
// todo \u00D7
        LATEX_ENCODE_MAP.put('\u00D8', "\\O");
        LATEX_ENCODE_MAP.put('\u00D9', "\\`{U}");
        LATEX_ENCODE_MAP.put('\u00DA', "\\'{U}");
        LATEX_ENCODE_MAP.put('\u00DB', "\\^{U}");
        LATEX_ENCODE_MAP.put('\u00DC', "\\\"{U}");
        LATEX_ENCODE_MAP.put('\u00DD', "\\'{Y}");
// todo \u00DE
        LATEX_ENCODE_MAP.put('\u00DF', "\\ss");
        LATEX_ENCODE_MAP.put('\u00E0', "\\`{a}");
        LATEX_ENCODE_MAP.put('\u00E1', "\\'{a}");
        LATEX_ENCODE_MAP.put('\u00E2', "\\^{a}");
        LATEX_ENCODE_MAP.put('\u00E3', "\\H{a}");
        LATEX_ENCODE_MAP.put('\u00E4', "\\\"{a}");
        LATEX_ENCODE_MAP.put('\u00E5', "\\aa");
        LATEX_ENCODE_MAP.put('\u00E6', "\\ae");
        LATEX_ENCODE_MAP.put('\u00E7', "\\c{c}");
        LATEX_ENCODE_MAP.put('\u00E8', "\\`{e}");
        LATEX_ENCODE_MAP.put('\u00E9', "\\'{e}");
        LATEX_ENCODE_MAP.put('\u00EA', "\\^{e}");
        LATEX_ENCODE_MAP.put('\u00EB', "\\\"{e}");
        LATEX_ENCODE_MAP.put('\u00EC', "\\`{i}");
        LATEX_ENCODE_MAP.put('\u00ED', "\\'{i}");
        LATEX_ENCODE_MAP.put('\u00EE', "\\^{i}");
        LATEX_ENCODE_MAP.put('\u00EF', "\\\"{i}");
// todo \u00F0
        LATEX_ENCODE_MAP.put('\u00F1', "\\H{n}");
        LATEX_ENCODE_MAP.put('\u00F2', "\\`{o}");
        LATEX_ENCODE_MAP.put('\u00F3', "\\'{o}");
        LATEX_ENCODE_MAP.put('\u00F4', "\\^{o}");
        LATEX_ENCODE_MAP.put('\u00F5', "\\H{o}");
        LATEX_ENCODE_MAP.put('\u00F6', "\\\"{o}");
// todo \u00F7
        LATEX_ENCODE_MAP.put('\u00F8', "\\o");
        LATEX_ENCODE_MAP.put('\u00F9', "\\`{u}");
        LATEX_ENCODE_MAP.put('\u00FA', "\\'{u}");
        LATEX_ENCODE_MAP.put('\u00FB', "\\^{u}");
        LATEX_ENCODE_MAP.put('\u00FC', "\\\"{u}");
        LATEX_ENCODE_MAP.put('\u00FD', "\\'{y}");
// todo \u00FE
        LATEX_ENCODE_MAP.put('\u00FF', "\\\"{y}");
    }

    private StringUtils() {
        // no-op
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid characters for a java class name.
     *
     * @param name The string that has to be transformed into a valid class
     *             name.
     * @return The encoded {@code String} object.
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeClassname(String name) {
        if (null == name) {
            return null;
        }

        var pattern = Pattern.compile("[^\\w]");
        var matcher = pattern.matcher(name);

        return matcher.replaceAll("_");
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid URL characters in the UTF-8 encoding.
     *
     * @param source The string that has to be transformed into a valid URL
     *               string.
     * @return The encoded {@code String} object.
     * @see #decodeUrl(String)
     * @see #encodeClassname(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeUrl(String source) {
        return encodeUrl(source, (String) null);
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid URL characters in the UTF-8 encoding.
     *
     * @param source The string that has to be transformed into a valid URL
     *               string.
     * @param allow  Additional characters to allow.
     * @return The encoded {@code String} object.
     * @see #decodeUrl(String)
     * @see #encodeClassname(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeUrl(String source, char... allow) {
        return encodeUrl(source, new String(allow));
    }


    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid URL characters in the UTF-8 encoding.
     *
     * @param source The string that has to be transformed into a valid URL
     *               string.
     * @return The encoded {@code String} object.
     * @see #decodeUrl(String)
     * @see #encodeClassname(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeUrl(String source, String allow) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        StringBuilder out = null;
        char ch;
        var i = 0;
        while (i < source.length()) {
            ch = source.charAt(i);
            if (isUnreservedUriChar(ch) || (allow != null && allow.indexOf(ch) != -1)) {
                if (out != null) {
                    out.append(ch);
                }
                i += 1;
            } else {
                if (out == null) {
                    out = new StringBuilder(source.length());
                    out.append(source, 0, i);
                }

                var cp = source.codePointAt(i);
                if (cp < 0x80) {
                    appendUrlEncodedByte(out, cp);
                    i += 1;
                } else if (Character.isBmpCodePoint(cp)) {
                    for (var b : Character.toString(ch).getBytes(StandardCharsets.UTF_8)) {
                        appendUrlEncodedByte(out, b);
                    }
                    i += 1;
                } else if (Character.isSupplementaryCodePoint(cp)) {
                    var high = Character.highSurrogate(cp);
                    var low = Character.lowSurrogate(cp);
                    for (var b : new String(new char[]{high, low}).getBytes(StandardCharsets.UTF_8)) {
                        appendUrlEncodedByte(out, b);
                    }
                    i += 2;
                }
            }
        }

        if (out == null) {
            return source;
        }

        return out.toString();
    }

    static final BitSet UNRESERVED_URI_CHARS;

    static {
        // see https://www.rfc-editor.org/rfc/rfc3986#page-13
        // and https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set
        var unreserved = new BitSet('z' + 1);
        unreserved.set('-');
        unreserved.set('.');
        for (int c = '0'; c <= '9'; ++c) unreserved.set(c);
        for (int c = 'A'; c <= 'Z'; ++c) unreserved.set(c);
        unreserved.set('_');
        for (int c = 'a'; c <= 'z'; ++c) unreserved.set(c);
        UNRESERVED_URI_CHARS = unreserved;
    }

    // see https://www.rfc-editor.org/rfc/rfc3986#page-13
    // and https://url.spec.whatwg.org/#application-x-www-form-urlencoded-percent-encode-set
    private static boolean isUnreservedUriChar(char ch) {
        if (ch > 'z') return false;
        return UNRESERVED_URI_CHARS.get(ch);
    }

    private static final char[] BASE32_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    /**
     * Appends the hexadecimal digit of the provided number.
     *
     * @param out    the string builder to append to
     * @param number the number who's first digit will be appended in hexadecimal
     * @since 1.0
     */
    public static void appendHexDigit(StringBuilder out, int number) {
        out.append(HEX_DIGITS[number & 0x0F]);
    }

    /**
     * Appends the lowercase hexadecimal digit of the provided number.
     *
     * @param out    the string builder to append to
     * @param number the number who's first digit will be appended in hexadecimal
     * @since 1.5.7
     */
    public static void appendHexDigitLower(StringBuilder out, int number) {
        out.append(HEX_DIGITS_LOWER[number & 0x0F]);
    }

    private static void appendUrlEncodedByte(StringBuilder out, int ch) {
        out.append("%");
        appendHexDigit(out, ch >> 4);
        appendHexDigit(out, ch);
    }

    /**
     * Transforms a provided {@code String} URL into a new string,
     * containing decoded URL characters in the UTF-8 encoding.
     *
     * @param source The string URL that has to be decoded
     * @return The decoded {@code String} object.
     * @see #encodeUrl(String)
     * @since 1.0
     */
    public static String decodeUrl(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        var length = source.length();
        StringBuilder out = null;
        char ch;
        byte[] bytes_buffer = null;
        var bytes_pos = 0;
        var i = 0;
        while (i < length) {
            ch = source.charAt(i);

            if (ch == '%') {
                if (out == null) {
                    out = new StringBuilder(source.length());
                    out.append(source, 0, i);
                }

                if (bytes_buffer == null) {
                    // the remaining characters divided by the length
                    // of the encoding format %xx, is the maximum number of
                    // bytes that can be extracted
                    bytes_buffer = new byte[(length - i) / 3];
                    bytes_pos = 0;
                }

                i += 1;
                if (length < i + 2) {
                    throw new IllegalArgumentException("StringUtils.decodeUrl: Illegal escape sequence");
                }
                try {
                    var v = Integer.parseInt(source, i, i + 2, 16);
                    if (v < 0 || v > 0xFF) {
                        throw new IllegalArgumentException("StringUtils.decodeUrl: Illegal escape value");
                    }

                    bytes_buffer[bytes_pos++] = (byte) v;

                    i += 2;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("StringUtils.decodeUrl: Illegal characters in escape sequence" + e.getMessage());
                }
            } else {
                if (bytes_buffer != null) {
                    out.append(new String(bytes_buffer, 0, bytes_pos, StandardCharsets.UTF_8));

                    bytes_buffer = null;
                    bytes_pos = 0;
                }

                if (out != null) {
                    out.append(ch);
                }

                i += 1;
            }
        }

        if (out == null) {
            return source;
        }

        if (bytes_buffer != null) {
            out.append(new String(bytes_buffer, 0, bytes_pos, StandardCharsets.UTF_8));
        }

        return out.toString();
    }

    private static boolean needsHtmlEncoding(String source, boolean defensive) {
        if (null == source) {
            return false;
        }

        var encode = false;
        char ch;
        for (var i = 0; i < source.length(); i++) {
            ch = source.charAt(i);

            if ((defensive || (ch != '\u0022' && ch != '\u0026' && ch != '\u003C' && ch != '\u003E')) &&
                ch < '\u00A0') {
                continue;
            }

            encode = true;
            break;
        }

        return encode;
    }

    /**
     * @since 1.0
     */
    public static String decodeHtml(String source) {
        if (null == source ||
            0 == source.length()) {
            return source;
        }

        var current_index = 0;
        var delimiter_start_index = 0;
        var delimiter_end_index = 0;

        StringBuilder result = null;

        while (current_index <= source.length()) {
            delimiter_start_index = source.indexOf('&', current_index);
            if (delimiter_start_index != -1) {
                delimiter_end_index = source.indexOf(';', delimiter_start_index + 1);
                if (delimiter_end_index != -1) {
                    // ensure that the string builder is setup correctly
                    if (null == result) {
                        result = new StringBuilder();
                    }

                    // add the text that leads up to this match
                    if (delimiter_start_index > current_index) {
                        result.append(source, current_index, delimiter_start_index);
                    }

                    // add the decoded entity
                    var entity = source.substring(delimiter_start_index, delimiter_end_index + 1);

                    current_index = delimiter_end_index + 1;

                    // try to decoded numeric entities
                    if (entity.charAt(1) == '#') {
                        var start = 2;
                        var radix = 10;
                        // check if the number is hexadecimal
                        if (entity.charAt(2) == 'X' || entity.charAt(2) == 'x') {
                            start++;
                            radix = 16;
                        }
                        try {
                            Character c = (char) Integer.parseInt(entity.substring(start, entity.length() - 1), radix);
                            result.append(c);
                        }
                        // when the number of the entity can't be parsed, add the entity as-is
                        catch (NumberFormatException e) {
                            result.append(entity);
                        }
                    } else {
                        // try to decode the entity as a literal
                        var decoded = HTML_DECODE_MAP.get(entity);
                        if (decoded != null) {
                            result.append(decoded);
                        }
                        // if there was no match, add the entity as-is
                        else {
                            result.append(entity);
                        }
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        if (null == result) {
            return source;
        } else if (current_index < source.length()) {
            result.append(source.substring(current_index));
        }

        return result.toString();
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid Html characters.
     *
     * @param source The string that has to be transformed into a valid Html
     *               string.
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeHtml(String source) {
        if (needsHtmlEncoding(source, false)) {
            return encode(source, HTML_ENCODER_FALLBACK, AGGRESSIVE_HTML_ENCODE_MAP, DEFENSIVE_HTML_ENCODE_MAP);
        }
        return source;
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing as much as possible Html characters. It is safe to already
     * feed existing Html to this method since &amp;, &lt; and &gt; will not
     * be encoded.
     *
     * @param source The string that has to be transformed into a valid Html
     *               string.
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeHtmlDefensive(String source) {
        if (needsHtmlEncoding(source, true)) {
            return encode(source, null, DEFENSIVE_HTML_ENCODE_MAP);
        }
        return source;
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid XML characters.
     *
     * @param source The string that has to be transformed into a valid XML
     *               string.
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeXml(String source) {
        return encode(source, null, XML_ENCODE_MAP);
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid {@code String} characters.
     *
     * @param source The string that has to be transformed into a valid
     *               sequence of {@code String} characters.
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeString(String source) {
        return encode(source, null, STRING_ENCODE_MAP);
    }

    /**
     * Transforms a provided {@code String} object into a series of
     * unicode escape codes.
     *
     * @param source The string that has to be transformed into a valid
     *               sequence of unicode escape codes
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeUnicode(String source) {
        if (null == source) {
            return null;
        }

        var encoded = new StringBuilder();
        String hexstring = null;
        for (var i = 0; i < source.length(); i++) {
            hexstring = Integer.toHexString(source.charAt(i)).toUpperCase();
            encoded.append("\\u");
            // fill with zeros
            for (var j = hexstring.length(); j < 4; j++) {
                encoded.append("0");
            }
            encoded.append(hexstring);
        }

        return encoded.toString();
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid Sql characters.
     *
     * @param source The string that has to be transformed into a valid Sql
     *               string.
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeSql(String source) {
        return encode(source, null, SQL_ENCODE_MAP);
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid LaTeX characters.
     *
     * @param source The string that has to be transformed into a valid LaTeX
     *               string.
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeRegexp(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeLatex(String source) {
        if (null == source) {
            return null;
        }

        source = encode(source, null, LATEX_ENCODE_MAP);
        source = StringUtils.replace(source, "latex", "\\LaTeX", false);

        return source;
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * using the mapping that are provided through the supplied encoding
     * table.
     *
     * @param source         The string that has to be transformed into a valid
     *                       string, using the mappings that are provided through the supplied
     *                       encoding table.
     * @param encodingTables A {@code Map} object containing the mappings
     *                       to transform characters into valid entities. The keys of this map
     *                       should be {@code Character} objects and the values
     *                       {@code String} objects.
     * @return The encoded {@code String} object.
     * @since 1.0
     */
    private static String encode(String source, EncoderFallbackHandler fallbackHandler, Map<Character, String>... encodingTables) {
        if (null == source) {
            return null;
        }

        if (null == encodingTables ||
            0 == encodingTables.length) {
            return source;
        }

        StringBuilder encoded_string = null;
        var string_to_encode_array = source.toCharArray();
        var last_match = -1;

        for (var i = 0; i < string_to_encode_array.length; i++) {
            var char_to_encode = string_to_encode_array[i];
            for (var encoding_table : encodingTables) {
                if (encoding_table.containsKey(char_to_encode)) {
                    encoded_string = prepareEncodedString(source, encoded_string, i, last_match, string_to_encode_array);

                    encoded_string.append(encoding_table.get(char_to_encode));
                    last_match = i;
                }
            }

            if (fallbackHandler != null &&
                last_match < i &&
                fallbackHandler.hasFallback(char_to_encode)) {
                encoded_string = prepareEncodedString(source, encoded_string, i, last_match, string_to_encode_array);

                fallbackHandler.appendFallback(encoded_string, char_to_encode);
                last_match = i;
            }
        }

        if (null == encoded_string) {
            return source;
        } else {
            var difference = string_to_encode_array.length - (last_match + 1);
            if (difference > 0) {
                encoded_string.append(string_to_encode_array, last_match + 1, difference);
            }
            return encoded_string.toString();
        }
    }

    private static StringBuilder prepareEncodedString(String source, StringBuilder encodedString, int i, int lastMatch, char[] stringToEncodeArray) {
        if (null == encodedString) {
            encodedString = new StringBuilder(source.length());
        }

        var difference = i - (lastMatch + 1);
        if (difference > 0) {
            encodedString.append(stringToEncodeArray, lastMatch + 1, difference);
        }

        return encodedString;
    }

    private interface EncoderFallbackHandler {
        boolean hasFallback(char character);

        void appendFallback(StringBuilder encodedBuffer, char character);
    }

    private static class HtmlEncoderFallbackHandler implements EncoderFallbackHandler {
        private static final String PREFIX = "&#";
        private static final String SUFFIX = ";";

        public boolean hasFallback(char character) {
            return character >= '\u00A0';
        }

        public void appendFallback(StringBuilder encodedBuffer, char character) {
            encodedBuffer.append(PREFIX);
            encodedBuffer.append((int) character);
            encodedBuffer.append(SUFFIX);
        }
    }

    /**
     * Transforms a provided {@code String} object into a new string,
     * containing only valid Json characters.
     *
     * @param source The string that has to be transformed into a valid LaTeX
     *               string.
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeJson(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        var encoded_string = new StringBuilder();

        char b;
        char c = 0;
        String hhhh;
        int i;
        var len = source.length();

        for (i = 0; i < len; i += 1) {
            b = c;
            c = source.charAt(i);
            switch (c) {
                case '\\', '"' -> {
                    encoded_string.append('\\');
                    encoded_string.append(c);
                }
                case '/' -> {
                    if (b == '<') {
                        encoded_string.append('\\');
                    }
                    encoded_string.append(c);
                }
                case '\b' -> encoded_string.append("\\b");
                case '\t' -> encoded_string.append("\\t");
                case '\n' -> encoded_string.append("\\n");
                case '\f' -> encoded_string.append("\\f");
                case '\r' -> encoded_string.append("\\r");
                default -> {
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                        || (c >= '\u2000' && c < '\u2100')) {
                        encoded_string.append("\\u");
                        hhhh = Integer.toHexString(c);
                        encoded_string.append("0000", 0, 4 - hhhh.length());
                        encoded_string.append(hhhh);
                    } else {
                        encoded_string.append(c);
                    }
                }
            }
        }

        return encoded_string.toString();
    }

    /**
     * Transforms a provided {@code String} object into a literal that can
     * be included into a regular expression {@link Pattern} as-is. None of the
     * regular expression escapes in the string will be functional anymore.
     *
     * @param source The string that has to be escaped as a literal
     * @return The encoded {@code String} object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @see #encodeJson(String)
     * @since 1.0
     */
    public static String encodeRegexp(String source) {
        var regexp_quote_start = source.indexOf("\\E");
        if (-1 == regexp_quote_start) {
            return "\\Q" + source + "\\E";
        }

        var buffer = new StringBuilder(source.length() * 2);
        buffer.append("\\Q");

        regexp_quote_start = 0;

        var current = 0;
        while (-1 == (regexp_quote_start = source.indexOf("\\E", current))) {
            buffer.append(source, current, regexp_quote_start);
            current = regexp_quote_start + 2;
            buffer.append("\\E\\\\E\\Q");
        }

        buffer.append(source.substring(current));
        buffer.append("\\E");

        return buffer.toString();
    }

    /**
     * Generates an uppercase hexadecimal string for the provided byte array.
     *
     * @param bytes the byte array to convert to a hex string
     * @return the converted hexadecimal string
     * @since 1.0
     */
    public static String encodeHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        var out = new StringBuilder();
        for (var b : bytes) {
            appendHexDigit(out, b >> 4);
            appendHexDigit(out, b);
        }
        return out.toString();
    }

    /**
     * Generates a lowercase hexadecimal string for the provided byte array.
     *
     * @param bytes the byte array to convert to a hex string
     * @return the converted hexadecimal string
     * @since 1.5.7
     */
    public static String encodeHexLower(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        var out = new StringBuilder();
        for (var b : bytes) {
            appendHexDigitLower(out, b >> 4);
            appendHexDigitLower(out, b);
        }
        return out.toString();
    }

    /**
     * Encodes byte array to Base64 String.
     *
     * @param bytes Bytes to encode.
     * @return Encoded byte array <code>bytes</code> as a String.
     * @since 1.1
     */
    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Encodes byte array to Base32 String.
     *
     * @param bytes Bytes to encode.
     * @return Encoded byte array <code>bytes</code> as a String.
     * @since 1.0
     */
    public static String encodeBase32(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        int i = 0, index = 0, digit = 0;
        int curr_byte, next_byte;
        var base32 = new StringBuilder((bytes.length + 7) * 8 / 5);

        while (i < bytes.length) {
            curr_byte = (bytes[i] >= 0) ? bytes[i] : (bytes[i] + 256); // un-sign

            /* Is the current digit going to span a byte boundary? */
            if (index > 3) {
                if ((i + 1) < bytes.length) {
                    next_byte =
                        (bytes[i + 1] >= 0) ? bytes[i + 1] : (bytes[i + 1] + 256);
                } else {
                    next_byte = 0;
                }

                digit = curr_byte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= next_byte >> (8 - index);
                i++;
            } else {
                digit = (curr_byte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0)
                    i++;
            }
            base32.append(BASE32_DIGITS[digit]);
        }

        return base32.toString();
    }

    /**
     * Counts the number of times a substring occures in a provided string in
     * a case-sensitive manner.
     *
     * @param source    The {@code String} object that will be searched in.
     * @param substring The string whose occurances will we counted.
     * @return An {@code int} value containing the number of occurances
     * of the substring.
     * @since 1.0
     */
    public static int count(String source, String substring) {
        return count(source, substring, true);
    }

    /**
     * Counts the number of times a substring occures in a provided string.
     *
     * @param source    The {@code String} object that will be searched in.
     * @param substring The string whose occurances will we counted.
     * @param matchCase A {@code boolean} indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return An {@code int} value containing the number of occurances
     * of the substring.
     * @since 1.0
     */
    public static int count(String source, String substring, boolean matchCase) {
        if (null == source) {
            return 0;
        }

        if (null == substring) {
            return 0;
        }

        var current_index = 0;
        var substring_index = 0;
        var count = 0;

        if (!matchCase) {
            source = source.toLowerCase();
            substring = substring.toLowerCase();
        }

        while (current_index < source.length() - 1) {
            substring_index = source.indexOf(substring, current_index);

            if (-1 == substring_index) {
                break;
            } else {
                current_index = substring_index + substring.length();
                count++;
            }
        }

        return count;
    }

    /**
     * Splits a string into different parts, using a separator string to
     * detect the seperation boundaries in a case-sensitive manner. The
     * separator will not be included in the list of parts.
     *
     * @param source    The string that will be split into parts.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @return An {@code ArrayList} containing the parts as
     * {@code String} objects.
     * @since 1.0
     */
    public static List<String> split(String source, String separator) {
        return split(source, separator, true);
    }

    /**
     * Splits a string into different parts, using a separator string to
     * detect the seperation boundaries. The separator will not be included in
     * the list of parts.
     *
     * @param source    The string that will be split into parts.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @param matchCase A {@code boolean} indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return An {@code ArrayList} containing the parts as
     * {@code String} objects.
     * @since 1.0
     */
    public static List<String> split(String source, String separator, boolean matchCase) {
        var substrings = new ArrayList<String>();

        if (null == source) {
            return substrings;
        }

        if (null == separator) {
            substrings.add(source);
            return substrings;
        }

        var current_index = 0;
        var delimiter_index = 0;
        String element = null;

        String source_lookup_reference = null;
        if (!matchCase) {
            source_lookup_reference = source.toLowerCase();
            separator = separator.toLowerCase();
        } else {
            source_lookup_reference = source;
        }

        while (current_index <= source_lookup_reference.length()) {
            delimiter_index = source_lookup_reference.indexOf(separator, current_index);

            if (-1 == delimiter_index) {
                element = source.substring(current_index);
                substrings.add(element);
                current_index = source.length() + 1;
            } else {
                element = source.substring(current_index, delimiter_index);
                substrings.add(element);
                current_index = delimiter_index + separator.length();
            }
        }

        return substrings;
    }

    /**
     * Splits a string into different parts, using a separator string to
     * detect the seperation boundaries in a case-sensitive manner. The
     * separator will not be included in the parts array.
     *
     * @param source    The string that will be split into parts.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @return A {@code String[]} array containing the seperated parts.
     * @since 1.0
     */
    public static String[] splitToArray(String source, String separator) {
        return splitToArray(source, separator, true);
    }

    /**
     * Splits a string into different parts, using a separator string to
     * detect the seperation boundaries. The separator will not be included in
     * the parts array.
     *
     * @param source    The string that will be split into parts.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @param matchCase A {@code boolean} indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return A {@code String[]} array containing the seperated parts.
     * @since 1.0
     */
    public static String[] splitToArray(String source, String separator, boolean matchCase) {
        var substrings = split(source, separator, matchCase);
        var substrings_array = new String[substrings.size()];
        substrings_array = substrings.toArray(substrings_array);

        return substrings_array;
    }

    /**
     * Splits a string into integers, using a separator string to detect the
     * seperation boundaries in a case-sensitive manner. If a part couldn't be
     * converted to an integer, it will be omitted from the resulting array.
     *
     * @param source    The string that will be split into integers.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @return An {@code int[]} array containing the seperated parts.
     * @since 1.0
     */
    public static int[] splitToIntArray(String source, String separator) {
        return splitToIntArray(source, separator, true);
    }

    /**
     * Splits a string into integers, using a separator string to detect the
     * seperation boundaries. If a part couldn't be converted to an integer,
     * it will be omitted from the resulting array.
     *
     * @param source    The string that will be split into integers.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @param matchCase A {@code boolean} indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return An {@code int[]} array containing the seperated parts.
     * @since 1.0
     */
    public static int[] splitToIntArray(String source, String separator, boolean matchCase) {
        var string_parts = split(source, separator, matchCase);
        var number_of_valid_parts = 0;

        for (var string_part : string_parts) {
            try {
                Integer.parseInt(string_part);
                number_of_valid_parts++;
            } catch (NumberFormatException e) {
                // just continue
            }
        }

        var string_parts_int = (int[]) Array.newInstance(int.class, number_of_valid_parts);
        var added_parts = 0;

        for (var string_part : string_parts) {
            try {
                string_parts_int[added_parts] = Integer.parseInt(string_part);
                added_parts++;
            } catch (NumberFormatException e) {
                // just continue
            }
        }

        return string_parts_int;
    }

    /**
     * Splits a string into bytes, using a separator string to detect the
     * seperation boundaries in a case-sensitive manner. If a part couldn't be
     * converted to a {@code byte}, it will be omitted from the resulting
     * array.
     *
     * @param source    The string that will be split into bytes.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @return A {@code byte[]} array containing the bytes.
     * @since 1.0
     */
    public static byte[] splitToByteArray(String source, String separator) {
        return splitToByteArray(source, separator, true);
    }

    /**
     * Splits a string into bytes, using a separator string to detect the
     * seperation boundaries. If a part couldn't be converted to a
     * {@code byte}, it will be omitted from the resulting array.
     *
     * @param source    The string that will be split into bytes.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @param matchCase A {@code boolean} indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return A {@code byte[]} array containing the bytes.
     * @since 1.0
     */
    public static byte[] splitToByteArray(String source, String separator, boolean matchCase) {
        var string_parts = split(source, separator, matchCase);
        var number_of_valid_parts = 0;
        for (var string_part : string_parts) {
            try {
                Byte.parseByte(string_part);
                number_of_valid_parts++;
            } catch (NumberFormatException e) {
                // just continue
            }
        }

        var string_parts_byte = (byte[]) Array.newInstance(byte.class, number_of_valid_parts);
        var added_parts = 0;
        for (var string_part : string_parts) {
            try {
                string_parts_byte[added_parts] = Byte.parseByte(string_part);
                added_parts++;
            } catch (NumberFormatException e) {
                // just continue
            }
        }

        return string_parts_byte;
    }

    /**
     * Removes all occurances of a string from the front of another string in
     * a case-sensitive manner.
     *
     * @param source        The string in which the matching will be done.
     * @param stringToStrip The string that will be stripped from the front.
     * @return A new {@code String} containing the stripped result.
     * @since 1.0
     */
    public static String stripFromFront(String source, String stringToStrip) {
        return stripFromFront(source, stringToStrip, true);
    }

    /**
     * Removes all occurances of a string from the front of another string.
     *
     * @param source        The string in which the matching will be done.
     * @param stringToStrip The string that will be stripped from the front.
     * @param matchCase     A {@code boolean} indicating if the match is
     *                      going to be performed in a case-sensitive manner or not.
     * @return A new {@code String} containing the stripping result.
     * @since 1.0
     */
    public static String stripFromFront(String source, String stringToStrip, boolean matchCase) {
        if (null == source) {
            return null;
        }

        if (null == stringToStrip) {
            return source;
        }

        var strip_length = stringToStrip.length();
        var last_index = 0;

        String source_lookup_reference = null;
        if (!matchCase) {
            source_lookup_reference = source.toLowerCase();
            stringToStrip = stringToStrip.toLowerCase();
        } else {
            source_lookup_reference = source;
        }

        var new_index = source_lookup_reference.indexOf(stringToStrip);
        if (0 == new_index) {
            do {
                last_index = new_index;
                new_index = source_lookup_reference.indexOf(stringToStrip, new_index + strip_length);
            }
            while (new_index != -1 &&
                   new_index == last_index + strip_length);

            return source.substring(last_index + strip_length);
        } else {
            return source;
        }
    }

    /**
     * Removes all occurances of a string from the end of another string in a
     * case-sensitive manner.
     *
     * @param source        The string in which the matching will be done.
     * @param stringToStrip The string that will be stripped from the end.
     * @return A new {@code String} containing the stripped result.
     * @since 1.0
     */
    public static String stripFromEnd(String source, String stringToStrip) {
        return stripFromEnd(source, stringToStrip, true);
    }

    /**
     * Removes all occurances of a string from the end of another string.
     *
     * @param source        The string in which the matching will be done.
     * @param stringToStrip The string that will be stripped from the end.
     * @param matchCase     A {@code boolean} indicating if the match is
     *                      going to be performed in a case-sensitive manner or not.
     * @return A new {@code String} containing the stripped result.
     * @since 1.0
     */
    public static String stripFromEnd(String source, String stringToStrip, boolean matchCase) {
        if (null == source) {
            return null;
        }

        if (null == stringToStrip) {
            return source;
        }

        var strip_length = stringToStrip.length();
        var new_index = 0;
        var last_index = 0;

        String source_lookup_reference = null;
        if (!matchCase) {
            source_lookup_reference = source.toLowerCase();
            stringToStrip = stringToStrip.toLowerCase();
        } else {
            source_lookup_reference = source;
        }

        new_index = source_lookup_reference.lastIndexOf(stringToStrip);
        if (new_index != -1 &&
            source.length() == new_index + strip_length) {
            do {
                last_index = new_index;
                new_index = source_lookup_reference.lastIndexOf(stringToStrip, last_index - 1);
            }
            while (new_index != -1 &&
                   new_index == last_index - strip_length);

            return source.substring(0, last_index);
        } else {
            return source;
        }
    }

    /**
     * Searches for a string within a specified string in a case-sensitive
     * manner and replaces every match with another string.
     *
     * @param source            The string in which the matching parts will be replaced.
     * @param stringToReplace   The string that will be searched for.
     * @param replacementString The string that will replace each matching
     *                          part.
     * @return A new {@code String} object containing the replacement
     * result.
     * @since 1.0
     */
    public static String replace(String source, String stringToReplace, String replacementString) {
        return replace(source, stringToReplace, replacementString, true);
    }

    /**
     * Searches for a string within a specified string and replaces every
     * match with another string.
     *
     * @param source            The string in which the matching parts will be replaced.
     * @param stringToReplace   The string that will be searched for.
     * @param replacementString The string that will replace each matching
     *                          part.
     * @param matchCase         A {@code boolean} indicating if the match is
     *                          going to be performed in a case-sensitive manner or not.
     * @return A new {@code String} object containing the replacement
     * result.
     * @since 1.0
     */
    public static String replace(String source, String stringToReplace, String replacementString, boolean matchCase) {
        if (null == source) {
            return null;
        }

        if (null == stringToReplace) {
            return source;
        }

        if (null == replacementString) {
            return source;
        }

        var string_parts = split(source, stringToReplace, matchCase).iterator();
        var new_string = new StringBuilder();

        while (string_parts.hasNext()) {
            var string_part = string_parts.next();
            new_string.append(string_part);
            if (string_parts.hasNext()) {
                new_string.append(replacementString);
            }
        }

        return new_string.toString();
    }

    /**
     * Creates a new string that contains the provided string a number of
     * times.
     *
     * @param source The string that will be repeated.
     * @param count  The number of times that the string will be repeated.
     * @return A new {@code String} object containing the repeated
     * concatenation result.
     * @since 1.0
     */
    public static String repeat(String source, int count) {
        if (null == source) {
            return null;
        }

        if (count > 0) {
            return source.repeat(count);
        } else {
            return "";
        }
    }

    /**
     * Creates a {@code String} for the provided byte array and encoding
     *
     * @param bytes    The byte array to convert.
     * @param encoding The encoding to use for the string conversion.
     * @return The converted {@code String}.
     * @since 1.0
     */
    public static String toString(byte[] bytes, String encoding) {
        String string;
        if (encoding != null && Charset.isSupported(encoding)) {
            try {
                string = new String(bytes, encoding);
            } catch (UnsupportedEncodingException e) {
                string = new String(bytes);
            }
        } else {
            string = new String(bytes);
        }

        return string;
    }

    /**
     * Creates a new array of {@code String} objects, containing the
     * elements of a supplied {@code Iterator}.
     *
     * @param iterator The iterator containing the elements to create the
     *                 array with.
     * @return The new {@code String} array.
     * @since 1.0
     */
    public static String[] toStringArray(Iterator<String> iterator) {
        if (null == iterator) {
            return new String[0];
        }

        var strings = new ArrayList<String>();
        iterator.forEachRemaining(strings::add);

        var string_array = new String[strings.size()];
        strings.toArray(string_array);

        return string_array;
    }

    /**
     * Creates a new {@code ArrayList}, containing the elements of a
     * supplied array of {@code String} objects.
     *
     * @param stringArray The array of {@code String} objects that have
     *                    to be converted.
     * @return The new {@code ArrayList} with the elements of the
     * {@code String} array.
     * @since 1.0
     */
    public static List<String> toArrayList(String[] stringArray) {
        var strings = new ArrayList<String>();

        if (null == stringArray) {
            return strings;
        }

        Collections.addAll(strings, stringArray);

        return strings;
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied {@code Collection} of {@code String} objects joined
     * by a given separator.
     *
     * @param collection The {@code Collection} containing the elements
     *                   to join.
     * @param separator  The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(Collection<?> collection, String separator) {
        if (null == collection) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == collection.size()) {
            return "";
        } else {
            var result = new StringBuilder();
            for (var element : collection) {
                result.append(element);
                result.append(separator);
            }

            result.setLength(result.length() - separator.length());
            return result.toString();
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The object array containing the elements to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(Object[] array, String separator) {
        return join(array, separator, null, false);
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The object array containing the elements to join.
     * @param separator The separator used to join the string elements.
     * @param delimiter The delimiter used to surround the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(Object[] array, String separator, String delimiter) {
        return join(array, separator, delimiter, false);
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array         The object array containing the elements to join.
     * @param separator     The separator used to join the string elements.
     * @param delimiter     The delimiter used to surround the string elements.
     * @param encodeStrings Indicates whether the characters of the string
     *                      representation of the Array values should be encoded.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(Object[] array, String separator, String delimiter, boolean encodeStrings) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (null == delimiter) {
            delimiter = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            String array_value = null;
            var result = new StringBuilder();
            while (current_index < array.length - 1) {
                if (null == array[current_index]) {
                    result.append("null");
                } else {
                    array_value = String.valueOf(array[current_index]);
                    if (encodeStrings) {
                        array_value = encodeString(array_value);
                    }
                    result.append(delimiter);
                    result.append(array_value);
                    result.append(delimiter);
                }
                result.append(separator);
                current_index++;
            }

            if (null == array[current_index]) {
                result.append("null");
            } else {
                array_value = String.valueOf(array[current_index]);
                if (encodeStrings) {
                    array_value = encodeString(array_value);
                }
                result.append(delimiter);
                result.append(array_value);
                result.append(delimiter);
            }
            return result.toString();
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The boolean array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(boolean[] array, String separator) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            var result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The byte array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(byte[] array, String separator) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            var result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The double array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(double[] array, String separator) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            var result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The float array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(float[] array, String separator) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            var result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The integer array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(int[] array, String separator) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            var result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The long array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(long[] array, String separator) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            var result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The short array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(short[] array, String separator) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            var result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The char array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(char[] array, String separator) {
        return join(array, separator, null);
    }

    /**
     * Creates a new {@code String} object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The char array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @param delimiter The delimiter used to surround the string elements.
     * @return A new {@code String} with the join result.
     * @since 1.0
     */
    public static String join(char[] array, String separator, String delimiter) {
        if (null == array) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (null == delimiter) {
            delimiter = "";
        }

        if (0 == array.length) {
            return "";
        } else {
            var current_index = 0;
            var result = new StringBuilder();
            while (current_index < array.length - 1) {
                result.append(delimiter);
                result.append(array[current_index]);
                result.append(delimiter);
                result.append(separator);
                current_index++;
            }

            result.append(delimiter);
            result.append(array[current_index]);
            result.append(delimiter);
            return result.toString();
        }
    }

    /**
     * Returns an array that contains all the occurances of a substring in a
     * string in the correct order. The search will be performed in a
     * case-sensitive manner.
     *
     * @param source    The {@code String} object that will be searched in.
     * @param substring The string whose occurances will we counted.
     * @return An {@code int[]} array containing the indices of the
     * substring.
     * @since 1.0
     */
    public static int[] indicesOf(String source, String substring) {
        return indicesOf(source, substring, true);
    }

    /**
     * Returns an array that contains all the occurances of a substring in a
     * string in the correct order.
     *
     * @param source    The {@code String} object that will be searched in.
     * @param substring The string whose occurances will we counted.
     * @param matchCase A {@code boolean} indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return An {@code int[]} array containing the indices of the
     * substring.
     * @since 1.0
     */
    public static int[] indicesOf(String source, String substring, boolean matchCase) {
        if (null == source ||
            null == substring) {
            return new int[0];
        }

        String source_lookup_reference = null;
        if (!matchCase) {
            source_lookup_reference = source.toLowerCase();
            substring = substring.toLowerCase();
        } else {
            source_lookup_reference = source;
        }

        var current_index = 0;
        var substring_index = 0;
        var count = count(source_lookup_reference, substring);
        var indices = new int[count];
        var counter = 0;

        while (current_index < source.length() - 1) {
            substring_index = source_lookup_reference.indexOf(substring, current_index);

            if (-1 == substring_index) {
                break;
            } else {
                current_index = substring_index + substring.length();
                indices[counter] = substring_index;
                counter++;
            }
        }

        return indices;
    }

    /**
     * Matches a collection of regular expressions against a string.
     *
     * @param value   The {@code String} that will be checked.
     * @param regexps The collection of regular expressions against which the
     *                match will be performed.
     * @return The {@code Matcher} instance that corresponds to the
     * {@code String} that returned a successful match; or
     * <p>{@code null} if no match could be found.
     * @since 1.0
     */
    public static Matcher getMatchingRegexp(String value, Collection<Pattern> regexps) {
        if (value != null &&
            value.length() > 0 &&
            regexps != null &&
            regexps.size() > 0) {
            Matcher matcher = null;
            for (var regexp : regexps) {
                matcher = regexp.matcher(value);
                if (matcher.matches()) {
                    return matcher;
                }
            }
        }

        return null;
    }

    /**
     * Matches a collection of strings against a regular expression.
     *
     * @param values The {@code Collection} of {@code String}
     *               objects that will be checked.
     * @param regexp The regular expression {@code Pattern} against which
     *               the matches will be performed.
     * @return The {@code Matcher} instance that corresponds to the
     * {@code String} that returned a successful match; or
     * <p>{@code null} if no match could be found.
     * @since 1.0
     */
    public static Matcher getRegexpMatch(Collection<String> values, Pattern regexp) {
        if (values != null &&
            values.size() > 0 &&
            regexp != null) {
            Matcher matcher = null;
            for (var value : values) {
                matcher = regexp.matcher(value);
                if (matcher.matches()) {
                    return matcher;
                }
            }
        }

        return null;
    }

    /**
     * Checks if the name filters through an including and an excluding
     * regular expression.
     *
     * @param name     The {@code String} that will be filtered.
     * @param included The regular expressions that needs to succeed
     * @param excluded The regular expressions that needs to fail
     * @return {@code true} if the name filtered through correctly; or
     * <p>{@code false} otherwise.
     * @since 1.0
     */
    public static boolean filter(String name, Pattern included, Pattern excluded) {
        return filter(name, included, excluded, true);
    }

    /**
     * Checks if the name filters through an including and an excluding
     * regular expression.
     *
     * @param name     The {@code String} that will be filtered.
     * @param included The regular expressions that needs to succeed
     * @param excluded The regular expressions that needs to fail
     * @param matches  Indicates whether it should be a full match or a contains
     * @return {@code true} if the name filtered through correctly; or
     * <p>{@code false} otherwise.
     * @since 1.5.18
     */
    public static boolean filter(String name, Pattern included, Pattern excluded, boolean matches) {
        Pattern[] included_array = null;
        if (included != null) {
            included_array = new Pattern[]{included};
        }

        Pattern[] excluded_array = null;
        if (excluded != null) {
            excluded_array = new Pattern[]{excluded};
        }

        return filter(name, included_array, excluded_array, matches);
    }

    /**
     * Checks if the name filters through a series of including and excluding
     * regular expressions.
     *
     * @param name     The {@code String} that will be filtered.
     * @param included A list of regular expressions that need to succeed
     * @param excluded A list of regular expressions that need to fail
     * @return {@code true} if the name filtered through correctly; or
     * <p>{@code false} otherwise.
     * @since 1.5
     */
    public static boolean filter(String name, List<Pattern> included, List<Pattern> excluded) {
        return filter(name, included, excluded, true);
    }

    /**
     * Checks if the name filters through a series of including and excluding
     * regular expressions.
     *
     * @param name     The {@code String} that will be filtered.
     * @param included A list of regular expressions that need to succeed
     * @param excluded A list of regular expressions that need to fail
     * @param matches  Indicates whether it should be a full match or a contains
     * @return {@code true} if the name filtered through correctly; or
     * <p>{@code false} otherwise.
     * @since 1.5.18
     */
    public static boolean filter(String name, List<Pattern> included, List<Pattern> excluded, boolean matches) {
        var included_array = new Pattern[included.size()];
        var excluded_array = new Pattern[excluded.size()];
        included.toArray(included_array);
        excluded.toArray(excluded_array);
        return filter(name, included_array, excluded_array, matches);
    }

    /**
     * Checks if the name filters through a series of including and excluding
     * regular expressions.
     *
     * @param name     The {@code String} that will be filtered.
     * @param included An array of regular expressions that need to succeed
     * @param excluded An array of regular expressions that need to fail
     * @return {@code true} if the name filtered through correctly; or
     * <p>{@code false} otherwise.
     * @since 1.0
     */
    public static boolean filter(String name, Pattern[] included, Pattern[] excluded) {
        return filter(name, included, excluded, true);
    }

    /**
     * Checks if the name filters through a series of including and excluding
     * regular expressions.
     *
     * @param name     The {@code String} that will be filtered.
     * @param included An array of regular expressions that need to succeed
     * @param excluded An array of regular expressions that need to fail
     * @param matches  Indicates whether it should be a full match or a contains
     * @return {@code true} if the name filtered through correctly; or
     * <p>{@code false} otherwise.
     * @since 1.5.18
     */
    public static boolean filter(String name, Pattern[] included, Pattern[] excluded, boolean matches) {
        if (null == name) {
            return false;
        }

        var accepted = false;

        // retain only the includes
        if (null == included || included.length == 0) {
            accepted = true;
        } else {
            for (var pattern : included) {
                if (pattern != null) {
                    var matcher = pattern.matcher(name);
                    if ((matches && matcher.matches()) ||
                        (!matches && matcher.find())) {
                        accepted = true;
                        break;
                    }
                }
            }
        }

        // remove the excludes
        if (accepted &&
            excluded != null) {
            for (var pattern : excluded) {
                if (pattern != null) {
                    var matcher = pattern.matcher(name);
                    if ((matches && matcher.matches()) ||
                        (!matches && matcher.find())) {
                        accepted = false;
                        break;
                    }
                }
            }
        }

        return accepted;
    }

    /**
     * Ensure that the first character of the provided string is upper case.
     *
     * @param source The {@code String} to capitalize.
     * @return The capitalized {@code String}.
     * @since 1.0
     */
    public static String capitalize(String source) {
        if (source == null || source.length() == 0) {
            return source;
        }

        if (source.length() == 1) {
            return source.toUpperCase(Localization.getLocale());
        } else {
            return source.substring(0, 1).toUpperCase(Localization.getLocale()) + source.substring(1);
        }
    }

    /**
     * Ensure that the first character of the provided string is lower case.
     *
     * @param source The {@code String} to uncapitalize.
     * @return The uncapitalized {@code String}.
     * @since 1.0
     */
    public static String uncapitalize(String source) {
        if (source == null || source.length() == 0) {
            return source;
        }

        if (source.length() == 1) {
            return source.toLowerCase(Localization.getLocale());
        } else {
            return source.substring(0, 1).toLowerCase(Localization.getLocale()) + source.substring(1);
        }
    }

    private static String convertUrl(String source, Pattern pattern, boolean shorten, boolean sanitize, boolean no_follow) {
        var max_length = RifeConfig.tools().getMaxVisualUrlLength();

        var result = source;

        var url_matcher = pattern.matcher(source);
        var found = url_matcher.find();
        if (found) {
            String visual_url = null;
            String actual_url = null;
            var last = 0;
            var sb = new StringBuilder();
            do {
                actual_url = url_matcher.group(1);
                if (url_matcher.groupCount() > 1) {
                    visual_url = url_matcher.group(2);
                } else {
                    visual_url = actual_url;
                }

                if (sanitize) {
                    // defang javascript
                    actual_url = StringUtils.replace(actual_url, "javascript:", "");

                    // fill in http:// for URLs that don't begin with /
                    if ((!actual_url.contains("://")) &&
                        (!actual_url.startsWith("/"))) {
                        actual_url = "https://" + actual_url;
                    }
                }

                if (pattern.equals(BBCODE_BAREURL)) {
                    sb.append(source, last, url_matcher.start(1));
                } else {
                    sb.append(source, last, url_matcher.start(0));
                }
                sb.append("<a href=\"");
                sb.append(actual_url);
                sb.append("\"");
                if (actual_url.startsWith("http://") ||
                    actual_url.startsWith("https://")) {
                    sb.append(" target=\"_blank\"");
                }
                if (no_follow) {
                    sb.append(" rel=\"nofollow\"");
                }
                sb.append(">");
                if (visual_url.length() <= max_length || !shorten) {
                    sb.append(visual_url);
                } else {
                    var ellipsis = "...";
                    var query_index = visual_url.indexOf("?");

                    // remove query string but keep '?'
                    if (query_index != -1) {
                        visual_url = visual_url.substring(0, query_index + 1) + ellipsis;
                    }

                    if (visual_url.length() >= max_length) {
                        var last_slash = visual_url.lastIndexOf("/");
                        var start_slash = visual_url.indexOf("/", visual_url.indexOf("://") + 3);

                        if (last_slash != start_slash) {
                            visual_url = visual_url.substring(0, start_slash + 1) + ellipsis + visual_url.substring(last_slash);
                        }
                    }

                    sb.append(visual_url);
                }
                sb.append("</a>");

                if (pattern.equals(BBCODE_BAREURL)) {
                    last = url_matcher.end(1);
                } else {
                    last = url_matcher.end(0);
                }

                found = url_matcher.find();
            }
            while (found);

            sb.append(source.substring(last));
            result = sb.toString();
        }

        return result;
    }

    /**
     * Converts a BBCode marked-up text to regular html.
     *
     * @param source The text with BBCode tags.
     * @return A {@code String} with the corresponding HTML code
     * @since 1.0
     */
    public static String convertBbcode(String source) {
        if (null == source) {
            return null;
        }

        return convertBbcode(source, (BbcodeOption[]) null);
    }

    /**
     * Converts a BBCode marked-up text to regular html.
     *
     * @param source The text with BBCode tags.
     * @return A {@code String} with the corresponding HTML code
     * @since 1.0
     */
    public static String convertBbcode(final String source, BbcodeOption... options) {
        if (null == source) {
            return null;
        }

        var shorten = false;
        var sanitize = false;
        var convert_bare = false;
        var no_follow_links = false;
        if (options != null) {
            for (var option : options) {
                switch (option) {
                    case SHORTEN_URL -> shorten = true;
                    case SANITIZE_URL -> sanitize = true;
                    case CONVERT_BARE_URLS -> convert_bare = true;
                    case NO_FOLLOW_LINKS -> no_follow_links = true;
                }
            }
        }

        var source_copy = source;
        var result = new StringBuilder(source.length());

        int startindex;
        int endIndex;
        int nextCodeIndex;
        while (-1 != (startindex = source_copy.indexOf("[code]"))) {
            // handle parsed
            var parsed = source_copy.substring(0, startindex);
            endIndex = source_copy.indexOf("[/code]") + 7;                       // 7 == the sizeof "[/code]"
            nextCodeIndex = source_copy.indexOf("[code]", startindex + 6);       // 6 == the sizeof "[code]"

            if (endIndex < 0) {
                // not ended... set to end of string
                endIndex = source_copy.length() - 1;
            }

            if (nextCodeIndex < endIndex && nextCodeIndex > 0) {
                // nested [code] tags

                /* must end before the next [code]
                 * this will leave a dangling [/code] but the HTML is valid
                 */
                source_copy = source_copy.substring(0, nextCodeIndex) +
                              "[/code]" +
                              source_copy.substring(nextCodeIndex);

                endIndex = source_copy.indexOf("[/code]") + 7;
            }

            if (startindex > endIndex) {
                // dangling [/code]
                endIndex = source_copy.indexOf("[/code]", endIndex + 7) + 7;     // 7 == the sizeof "[/code]"
                if (endIndex < 0) {
                    endIndex = source_copy.length() - 1;
                }
            }

            var code = source_copy.substring(startindex, endIndex);

            parsed = parseBBCode(parsed, shorten, sanitize, convert_bare, no_follow_links);

            // handle raw
            code = StringUtils.replace(code, "[code]", "<div class=\"codebody\"><pre>", false);
            code = StringUtils.replace(code, "[/code]", "</pre></div>", false);

            result
                .append(parsed)
                .append(code);

            source_copy = source_copy.substring(endIndex);
        }

        result.append(parseBBCode(source_copy, shorten, sanitize, convert_bare, no_follow_links));

        return result.toString();
    }

    private static String parseBBCode(String source, boolean shorten, boolean sanitize, boolean convert_bare, boolean no_follow) {
        var result = source;

        result = StringUtils.replace(result, "[b]", "<b>", false);
        result = StringUtils.replace(result, "[/b]", "</b>", false);
        result = StringUtils.replace(result, "[u]", "<u>", false);
        result = StringUtils.replace(result, "[/u]", "</u>", false);
        result = StringUtils.replace(result, "[i]", "<i>", false);
        result = StringUtils.replace(result, "[/i]", "</i>", false);
        result = StringUtils.replace(result, "[pre]", "<pre>", false);
        result = StringUtils.replace(result, "[/pre]", "</pre>", false);

        var resultCopy = result;
        var resultLowerCopy = result.toLowerCase();
        var buffer = new StringBuilder();
        int startIndex;
        int endIndex;
        while (-1 != (startIndex = resultLowerCopy.indexOf("[*]"))) {
            var begin = resultLowerCopy.indexOf("[list]", startIndex + 3);
            var end = resultLowerCopy.indexOf("[/list]", startIndex + 3);
            var next = resultLowerCopy.indexOf("[*]", startIndex + 3); // 3 == sizeof [*]

            if (begin == -1) {
                begin = Integer.MAX_VALUE;
            }

            if (end == -1) {
                end = Integer.MAX_VALUE;
            }

            if (next == -1) {
                next = Integer.MAX_VALUE;
            }

            if (next < begin && next < end) {
                endIndex = next;
            } else if (begin < next && begin < end) {
                endIndex = begin;
            } else if (end < next && end < begin) {
                endIndex = end;
            } else {
                endIndex = resultLowerCopy.length();
            }

            buffer
                .append(resultCopy, 0, startIndex)
                .append("<li>")
                .append(resultCopy, startIndex + 3, endIndex) // 3 == sizeof [*]
                .append("</li>");

            resultCopy = resultCopy.substring(endIndex);
            resultLowerCopy = resultLowerCopy.substring(endIndex);
        }
        buffer.append(resultCopy);

        result = buffer.toString();

        result = StringUtils.replace(result, "[list]", "<ul>", false);
        result = StringUtils.replace(result, "[/list]", "</ul>", false);

        var color_matcher = BBCODE_COLOR.matcher(result);
        result = color_matcher.replaceAll("<font color=\"$1\">");
        result = StringUtils.replace(result, "[/color]", "</font>", false);

        var size_matcher = BBCODE_SIZE.matcher(result);
        result = size_matcher.replaceAll("<font size=\"$1\">");
        result = StringUtils.replace(result, "[/size]", "</font>", false);

        result = convertUrl(result, BBCODE_URL_SHORT, shorten, sanitize, no_follow);
        result = convertUrl(result, BBCODE_URL_LONG, shorten, sanitize, no_follow);

        if (convert_bare) {
            result = convertUrl(result, BBCODE_BAREURL, shorten, sanitize, no_follow);
        }

        var img_matcher = BBCODE_IMG.matcher(result);
        result = img_matcher.replaceAll("<div class=\"bbcode_img\"><img src=\"$1\" border=\"0\" alt=\"\" /></div>");

        var quote_matcher_long = BBCODE_QUOTE_LONG.matcher(result);
        result = quote_matcher_long.replaceAll("<div class=\"quoteaccount\">$1:</div><div class=\"quotebody\">");
        result = StringUtils.replace(result, "[quote]", "<div class=\"quotebody\">", false);
        result = StringUtils.replace(result, "[/quote]", "</div>", false);

        result = StringUtils.replace(result, "\r\n", "<br />\r");
        result = StringUtils.replace(result, "\n", "<br />\n");
        result = StringUtils.replace(result, "\r", "\r\n");

        // remove the BR that could be added due to code formatting ppl
        // use to format lists
        result = StringUtils.replace(result, "ul><br />\r\n", "ul>\r\n");
        result = StringUtils.replace(result, "ul><br />\n", "ul>\n");

        return result;
    }

    /**
     * Converts a {@code String} to a {@code boolean} value.
     *
     * @param value The {@code String} to convert.
     * @return The corresponding {@code boolean} value.
     * @since 1.0
     */
    public static boolean convertToBoolean(String value) {
        if (null == value) {
            return false;
        }

        return value.equals("1") ||
               value.equalsIgnoreCase("t") ||
               value.equalsIgnoreCase("true") ||
               value.equalsIgnoreCase("y") ||
               value.equalsIgnoreCase("yes") ||
               value.equalsIgnoreCase("on");
    }

    /**
     * Converts all tabs on a line to spaces according to the provided tab
     * width.
     *
     * @param line     The line whose tabs have to be converted.
     * @param tabWidth The tab width.
     * @return A new {@code String} object containing the line with the
     * replaced tabs.
     * @since 1.0
     */
    public static String convertTabsToSpaces(String line, int tabWidth) {
        var result = new StringBuilder();
        var tab_index = -1;
        var last_tab_index = 0;
        var added_chars = 0;
        int tab_size;
        while ((tab_index = line.indexOf("\t", last_tab_index)) != -1) {
            tab_size = tabWidth - ((tab_index + added_chars) % tabWidth);
            if (0 == tab_size) {
                tab_size = tabWidth;
            }
            added_chars += tab_size - 1;
            result.append(line, last_tab_index, tab_index);
            result.append(StringUtils.repeat(" ", tab_size));
            last_tab_index = tab_index + 1;
        }
        if (0 == last_tab_index) {
            return line;
        } else {
            result.append(line.substring(last_tab_index));
        }

        return result.toString();
    }

    /**
     * Ensures that all whitespace is removed from a {@code String}.
     * <p>It also works with a {@code null} argument.
     *
     * @param source The {@code String} to trim.
     * @return The trimmed {@code String}.
     * @since 1.0
     */
    public static String trim(String source) {
        if (source == null || source.length() == 0) {
            return source;
        }

        return source.trim();
    }

    /**
     * Calculates the {@link DocumentPosition} of a character index in a
     * document.
     *
     * @param document       a {@code String} with the document where the
     *                       position should be looked up in
     * @param characterIndex the index of the character
     * @return the resulting {@code DocumentPosition} instance; or
     * <p>{@code null} if the {@code characterIndex} was invalid or
     * if the {@code document} was null
     * @since 1.0
     */
    public static DocumentPosition getDocumentPosition(String document, int characterIndex) {
        if (null == document ||
            characterIndex < 0 ||
            characterIndex > document.length()) {
            return null;
        }

        var line = 0;
        int column;

        var linebreaks = new String[]{"\r\n", "\n", "\r"};
        var last_linebreak_index = 0;
        var next_linebreak_index = document.length();
        var match = -1;
        do {
            line++;

            for (var linebreak : linebreaks) {
                match = document.indexOf(linebreak, last_linebreak_index);
                if (match != -1) {
                    if (match >= characterIndex) {
                        next_linebreak_index = match;
                        match = -1;
                        break;
                    }

                    last_linebreak_index = match + linebreak.length();
                    break;
                }
            }
        } while (match != -1);

        column = characterIndex - last_linebreak_index + 1;

        return new DocumentPosition(document.substring(last_linebreak_index, next_linebreak_index), line, column, 1);
    }

    /**
     * Reformats a string where lines that are longer than {@code width}
     * are split apart at the earliest wordbreak or at maxLength, whichever is
     * sooner. If the width specified is less than 5 or greater than the input
     * Strings length the string will be returned as is.
     * <p>
     * Please note that this method can be lossy - trailing spaces on wrapped
     * lines may be trimmed.
     *
     * @param input the String to reformat.
     * @param width the maximum length of any one line.
     * @return a new String with reformatted as needed.
     */
    public static String wordWrap(String input, int width, Locale locale) {
        // handle invalid input
        if (input == null) {
            return "";
        } else if (width < 5) {
            return input;
        } else if (width >= input.length()) {
            return input;
        }

        // default locale
        if (locale == null) {
            locale = Locale.US;
        }

        var buffer = new StringBuilder(input.length());
        var current_index = 0;
        var delimiter_index = 0;
        var seperator = "\n";
        var line = "";

        // go over the input string and jump from line to line
        while (current_index <= input.length()) {
            // look for the next linebreak
            delimiter_index = input.indexOf(seperator, current_index);

            // get the line that corresponds to it
            if (-1 == delimiter_index) {
                line = input.substring(current_index);
                current_index = input.length() + 1;
            } else {
                line = input.substring(current_index, delimiter_index);
                current_index = delimiter_index + seperator.length();
            }

            // handle the wrapping of the line
            var breaks = BreakIterator.getLineInstance(locale);
            breaks.setText(line);

            var line_start = 0;
            var start = breaks.first();
            var end = breaks.next();
            while (end != BreakIterator.DONE) {
                // check if the width has been exceeded
                if (end - 1 - line_start >= width) {
                    var break_line = true;

                    // first check if the last characters were spaces,
                    // if they were and by removing them the width is not
                    // exceeded, just continue
                    if (Character.isWhitespace(line.charAt(end - 1))) {
                        for (var j = end - 1; j >= 0; j--) {
                            if (!Character.isWhitespace(line.charAt(j))) {
                                if (j - line_start < width) {
                                    break_line = false;
                                }

                                break;
                            }
                        }
                    }

                    if (break_line) {
                        var line_breaked = line.substring(line_start, start);
                        // this can happen with trailing whitespace
                        if (line_breaked.length() > width) {
                            line_breaked = line_breaked.substring(0, width);
                        }
                        buffer.append(line_breaked);

                        buffer.append("\n");

                        line_start = start;
                    }
                }

                start = end;
                end = breaks.next();
            }

            if (line_start < line.length()) {
                buffer.append(line.substring(line_start));
            }

            if (delimiter_index != -1) {
                buffer.append("\n");
            }
        }

        return buffer.toString();
    }

    /**
     * Removes all blank lines from text.
     *
     * @param text the text to strip blank lines from
     * @return the text without any blank lines
     * @since 1.5.7
     */
    public static String stripBlankLines(String text) {
        var result = new StringBuilder();
        var tokenizer = new StringTokenizer(text, "\r\n", true);
        var non_blank = false;
        var added_cr = false;
        var added_nl = false;
        while (tokenizer.hasMoreTokens()) {
            var token = tokenizer.nextToken();
            if (!added_cr && token.equals("\r")) {
                if (non_blank) {
                    result.append(token);
                    added_cr = true;
                }
            } else if (!added_nl && token.equals("\n")) {
                if (non_blank) {
                    result.append(token);
                    added_nl = true;
                }
            } else if (!token.isBlank()) {
                result.append(token);
                non_blank = true;
                added_cr = false;
                added_nl = false;
            } else {
                non_blank = false;
            }
        }
        return result.toString();
    }
}
