/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.config.RifeConfig;
import rife.datastructures.DocumentPosition;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General purpose class containing common <code>String</code> manipulation
 * methods.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class StringUtils {
    public static String ENCODING_US_ASCII = "US-ASCII";
    public static String ENCODING_ISO_8859_1 = "ISO-8859-1";
    public static String ENCODING_ISO_8859_2 = "ISO-8859-2";
    public static String ENCODING_ISO_8859_5 = "ISO-8859-5";
    public static String ENCODING_UTF_8 = "UTF-8";
    public static String ENCODING_UTF_16BE = "UTF-16BE";
    public static String ENCODING_UTF_16LE = "UTF-16LE";
    public static String ENCODING_UTF_16 = "UTF-16";

    public static Charset CHARSET_US_ASCII = Charset.forName(StringUtils.ENCODING_US_ASCII);

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

    private static final Map<Character, String> AGGRESSIVE_HTML_ENCODE_MAP = new HashMap<Character, String>();
    private static final Map<Character, String> DEFENSIVE_HTML_ENCODE_MAP = new HashMap<Character, String>();
    private static final Map<Character, String> XML_ENCODE_MAP = new HashMap<Character, String>();
    private static final Map<Character, String> STRING_ENCODE_MAP = new HashMap<Character, String>();
    private static final Map<Character, String> SQL_ENCODE_MAP = new HashMap<Character, String>();
    private static final Map<Character, String> LATEX_ENCODE_MAP = new HashMap<Character, String>();

    private static final Map<String, Character> HTML_DECODE_MAP = new HashMap<String, Character>();

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

        Set<Map.Entry<Character, String>> aggresive_entries = AGGRESSIVE_HTML_ENCODE_MAP.entrySet();
        for (Map.Entry<Character, String> entry : aggresive_entries) {
            HTML_DECODE_MAP.put(entry.getValue(), entry.getKey());
        }

        Set<Map.Entry<Character, String>> defensive_entries = DEFENSIVE_HTML_ENCODE_MAP.entrySet();
        for (Map.Entry<Character, String> entry : defensive_entries) {
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

    /**
     * Transforms a provided <code>String</code> object into a new string,
     * containing only valid characters for a java class name.
     *
     * @param name The string that has to be transformed into a valid class
     *             name.
     * @return The encoded <code>String</code> object.
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeClassname(String name) {
        if (null == name) {
            return null;
        }

        Pattern pattern = Pattern.compile("[^\\w]");
        Matcher matcher = pattern.matcher(name);

        return matcher.replaceAll("_");
    }

    private static boolean needsUrlEncoding(String source) {
        if (null == source) {
            return false;
        }

        // check if the string needs encoding first since
        // the URLEncoder always allocates a StringBuffer, even when the
        // string is returned as-is
        var encode = false;
        char ch;
        for (int i = 0; i < source.length(); i++) {
            ch = source.charAt(i);

            if (ch >= 'a' && ch <= 'z' ||
                ch >= 'A' && ch <= 'Z' ||
                ch >= '0' && ch <= '9' ||
                ch == '-' || ch == '_' || ch == '.' || ch == '*') {
                continue;
            }

            encode = true;
            break;
        }

        return encode;
    }

    /**
     * Transforms a provided <code>String</code> object into a new string,
     * containing only valid URL characters.
     *
     * @param source The string that has to be transformed into a valid URL
     *               string.
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrlValue(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeUrl(String source) {
        if (!needsUrlEncoding(source)) {
            return source;
        }

        try {
            return URLEncoder.encode(source, ENCODING_ISO_8859_1);
        }
        ///CLOVER:OFF
        catch (UnsupportedEncodingException e) {
            // this should never happen, ISO-8859-1 is a standard encoding
            throw new RuntimeException(e);
        }
        ///CLOVER:ON
    }

    /**
     * Transforms a provided <code>String</code> object into a new string,
     * only pure US Ascii strings are preserved and URL encoded in a regular
     * way. Strings with characters from other encodings will be encoded in a
     * RIFE-specific manner to allow international data to be passed along the
     * query string.
     *
     * @param source The string that has to be transformed into a valid URL
     *               parameter string.
     * @return The encoded <code>String</code> object.
     * @see #decodeUrlValue(String)
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeUrlValue(String source) {
        if (!needsUrlEncoding(source)) {
            return source;
        }

        // check if the string is valid US-ASCII encoding
        var valid = true;
        var encoder = CHARSET_US_ASCII.newEncoder();
        try {
            encoder.encode(CharBuffer.wrap(source));
        } catch (CharacterCodingException e) {
            valid = false;
        }

        try {
            // if it is valid US-ASCII, use the regular URL encoding method
            if (valid) {
                return URLEncoder.encode(source, ENCODING_US_ASCII);
            }
            // otherwise, base-64 encode the UTF-8 bytes and mark the string
            // as being encoded in a special way
            else {
                var encoded = new StringBuilder("%02%02");
                var base64 = Base64.getEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8));
                var base64_urlsafe = replace(base64, "=", "%3D");
                encoded.append(base64_urlsafe);

                return encoded.toString();
            }
        }
        ///CLOVER:OFF
        catch (UnsupportedEncodingException e) {
            // this should never happen, ISO-8859-1 is a standard encoding
            throw new RuntimeException(e);
        }
        ///CLOVER:ON
    }

    /**
     * Decodes a <code>String</code> that has been encoded in a RIFE-specific
     * manner for URL usage. Before calling this method, you should first
     * verify if the value needs decoding by using the
     * <code>doesUrlValueNeedDecoding(String)</code> method.
     *
     * @param source the value that has been encoded for URL usage in a
     *               RIFE-specific way
     * @return The decoded <code>String</code> object.
     * @see #encodeUrlValue(String)
     * @see #doesUrlValueNeedDecoding(String)
     * @since 1.0
     */
    public static String decodeUrlValue(String source) {
        try {
            byte[] decoded = Base64.getDecoder().decode(source.substring(2));
            if (null == decoded) {
                return null;
            } else {
                return new String(decoded, StringUtils.ENCODING_UTF_8);
            }
        }
        ///CLOVER:OFF
        catch (UnsupportedEncodingException e) {
            // this should never happen, UTF-8 is a standard encoding
            throw new RuntimeException(e);
        }
        ///CLOVER:ON
    }

    /**
     * Checks if a <code>String</code> is encoded in a RIFE-specific manner
     * for URL usage.
     *
     * @param source the value that might have been encoded for URL usage in a
     *               RIFE-specific way
     * @return <code>true</code> if the value is encoded in the RIFE-specific
     * format; and
     * <p><code>false</code> otherwise
     * @see #encodeUrlValue(String)
     * @see #decodeUrlValue(String)
     * @since 1.0
     */
    public static boolean doesUrlValueNeedDecoding(String source) {
        return source != null &&
            source.length() > 2 &&
            source.startsWith("\u0002\u0002");
    }

    private static boolean needsHtmlEncoding(String source, boolean defensive) {
        if (null == source) {
            return false;
        }

        var encode = false;
        char ch;
        for (int i = 0; i < source.length(); i++) {
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
     * @since 1.6
     */
    public static String decodeHtml(String source) {
        if (null == source ||
            0 == source.length()) {
            return source;
        }

        int current_index = 0;
        int delimiter_start_index = 0;
        int delimiter_end_index = 0;

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
                    String entity = source.substring(delimiter_start_index, delimiter_end_index + 1);

                    current_index = delimiter_end_index + 1;

                    // try to decoded numeric entities
                    if (entity.charAt(1) == '#') {
                        int start = 2;
                        int radix = 10;
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
                        Character decoded = HTML_DECODE_MAP.get(entity);
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
     * Transforms a provided <code>String</code> object into a new string,
     * containing only valid Html characters.
     *
     * @param source The string that has to be transformed into a valid Html
     *               string.
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeUrlValue(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeHtml(String source) {
        if (needsHtmlEncoding(source, false)) {
            return encode(source, HTML_ENCODER_FALLBACK, AGGRESSIVE_HTML_ENCODE_MAP, DEFENSIVE_HTML_ENCODE_MAP);
        }
        return source;
    }

    /**
     * Transforms a provided <code>String</code> object into a new string,
     * containing as much as possible Html characters. It is safe to already
     * feed existing Html to this method since &amp;, &lt; and &gt; will not
     * be encoded.
     *
     * @param source The string that has to be transformed into a valid Html
     *               string.
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeUrlValue(String)
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
     * Transforms a provided <code>String</code> object into a new string,
     * containing only valid XML characters.
     *
     * @param source The string that has to be transformed into a valid XML
     *               string.
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeUrlValue(String)
     * @see #encodeHtml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeXml(String source) {
        return encode(source, null, XML_ENCODE_MAP);
    }

    /**
     * Transforms a provided <code>String</code> object into a new string,
     * containing only valid <code>String</code> characters.
     *
     * @param source The string that has to be transformed into a valid
     *               sequence of <code>String</code> characters.
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeUrlValue(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeString(String source) {
        return encode(source, null, STRING_ENCODE_MAP);
    }

    /**
     * Transforms a provided <code>String</code> object into a series of
     * unicode escape codes.
     *
     * @param source The string that has to be transformed into a valid
     *               sequence of unicode escape codes
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeUrlValue(String)
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
        for (int i = 0; i < source.length(); i++) {
            hexstring = Integer.toHexString(source.charAt(i)).toUpperCase();
            encoded.append("\\u");
            // fill with zeros
            for (int j = hexstring.length(); j < 4; j++) {
                encoded.append("0");
            }
            encoded.append(hexstring);
        }

        return encoded.toString();
    }

    /**
     * Transforms a provided <code>String</code> object into a new string,
     * containing only valid Sql characters.
     *
     * @param source The string that has to be transformed into a valid Sql
     *               string.
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeUrlValue(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @see #encodeRegexp(String)
     * @since 1.0
     */
    public static String encodeSql(String source) {
        return encode(source, null, SQL_ENCODE_MAP);
    }

    /**
     * Transforms a provided <code>String</code> object into a new string,
     * containing only valid LaTeX characters.
     *
     * @param source The string that has to be transformed into a valid LaTeX
     *               string.
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeUrlValue(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeRegexp(String)
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
     * Transforms a provided <code>String</code> object into a new string,
     * using the mapping that are provided through the supplied encoding
     * table.
     *
     * @param source         The string that has to be transformed into a valid
     *                       string, using the mappings that are provided through the supplied
     *                       encoding table.
     * @param encodingTables A <code>Map</code> object containing the mappings
     *                       to transform characters into valid entities. The keys of this map
     *                       should be <code>Character</code> objects and the values
     *                       <code>String</code> objects.
     * @return The encoded <code>String</code> object.
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

        for (int i = 0; i < string_to_encode_array.length; i++) {
            char char_to_encode = string_to_encode_array[i];
            for (Map<Character, String> encoding_table : encodingTables) {
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
            int difference = string_to_encode_array.length - (last_match + 1);
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
     * Transforms a provided <code>String</code> object into a literal that can
     * be included into a regular expression {@link Pattern} as-is. None of the
     * regular expression escapes in the string will be functional anymore.
     *
     * @param source The string that has to be escaped as a literal
     * @return The encoded <code>String</code> object.
     * @see #encodeClassname(String)
     * @see #encodeUrl(String)
     * @see #encodeUrlValue(String)
     * @see #encodeHtml(String)
     * @see #encodeXml(String)
     * @see #encodeSql(String)
     * @see #encodeString(String)
     * @see #encodeLatex(String)
     * @since 1.3
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
     * Counts the number of times a substring occures in a provided string in
     * a case-sensitive manner.
     *
     * @param source    The <code>String</code> object that will be searched in.
     * @param substring The string whose occurances will we counted.
     * @return An <code>int</code> value containing the number of occurances
     * of the substring.
     * @since 1.0
     */
    public static int count(String source, String substring) {
        return count(source, substring, true);
    }

    /**
     * Counts the number of times a substring occures in a provided string.
     *
     * @param source    The <code>String</code> object that will be searched in.
     * @param substring The string whose occurances will we counted.
     * @param matchCase A <code>boolean</code> indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return An <code>int</code> value containing the number of occurances
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
     * @return An <code>ArrayList</code> containing the parts as
     * <code>String</code> objects.
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
     * @param matchCase A <code>boolean</code> indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return An <code>ArrayList</code> containing the parts as
     * <code>String</code> objects.
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
     * @return A <code>String[]</code> array containing the seperated parts.
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
     * @param matchCase A <code>boolean</code> indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return A <code>String[]</code> array containing the seperated parts.
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
     * @return An <code>int[]</code> array containing the seperated parts.
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
     * @param matchCase A <code>boolean</code> indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return An <code>int[]</code> array containing the seperated parts.
     * @since 1.0
     */
    public static int[] splitToIntArray(String source, String separator, boolean matchCase) {
        var string_parts = split(source, separator, matchCase);
        var number_of_valid_parts = 0;

        for (String string_part : string_parts) {
            try {
                Integer.parseInt(string_part);
                number_of_valid_parts++;
            } catch (NumberFormatException e) {
                // just continue
            }
        }

        var string_parts_int = (int[]) Array.newInstance(int.class, number_of_valid_parts);
        var added_parts = 0;

        for (String string_part : string_parts) {
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
     * converted to a <code>byte</code>, it will be omitted from the resulting
     * array.
     *
     * @param source    The string that will be split into bytes.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @return A <code>byte[]</code> array containing the bytes.
     * @since 1.0
     */
    public static byte[] splitToByteArray(String source, String separator) {
        return splitToByteArray(source, separator, true);
    }

    /**
     * Splits a string into bytes, using a separator string to detect the
     * seperation boundaries. If a part couldn't be converted to a
     * <code>byte</code>, it will be omitted from the resulting array.
     *
     * @param source    The string that will be split into bytes.
     * @param separator The separator string that will be used to determine
     *                  the parts.
     * @param matchCase A <code>boolean</code> indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return A <code>byte[]</code> array containing the bytes.
     * @since 1.0
     */
    public static byte[] splitToByteArray(String source, String separator, boolean matchCase) {
        var string_parts = split(source, separator, matchCase);
        var number_of_valid_parts = 0;
        for (String string_part : string_parts) {
            try {
                Byte.parseByte(string_part);
                number_of_valid_parts++;
            } catch (NumberFormatException e) {
                // just continue
            }
        }

        var string_parts_byte = (byte[]) Array.newInstance(byte.class, number_of_valid_parts);
        var added_parts = 0;
        for (String string_part : string_parts) {
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
     * @return A new <code>String</code> containing the stripped result.
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
     * @param matchCase     A <code>boolean</code> indicating if the match is
     *                      going to be performed in a case-sensitive manner or not.
     * @return A new <code>String</code> containing the stripping result.
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
     * @return A new <code>String</code> containing the stripped result.
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
     * @param matchCase     A <code>boolean</code> indicating if the match is
     *                      going to be performed in a case-sensitive manner or not.
     * @return A new <code>String</code> containing the stripped result.
     * @since 1.0
     */
    public static String stripFromEnd(String source, String stringToStrip, boolean matchCase) {
        if (null == source) {
            return null;
        }

        if (null == stringToStrip) {
            return source;
        }

        int strip_length = stringToStrip.length();
        int new_index = 0;
        int last_index = 0;

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
     * @return A new <code>String</code> object containing the replacement
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
     * @param matchCase         A <code>boolean</code> indicating if the match is
     *                          going to be performed in a case-sensitive manner or not.
     * @return A new <code>String</code> object containing the replacement
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
            String string_part = string_parts.next();
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
     * @return A new <code>String</code> object containing the repeated
     * concatenation result.
     * @since 1.0
     */
    public static String repeat(String source, int count) {
        if (null == source) {
            return null;
        }

        var new_string = new StringBuilder();
        while (count > 0) {
            new_string.append(source);
            count--;
        }

        return new_string.toString();
    }

    /**
     * Creates a new array of <code>String</code> objects, containing the
     * elements of a supplied <code>Iterator</code>.
     *
     * @param iterator The iterator containing the elements to create the
     *                 array with.
     * @return The new <code>String</code> array.
     * @since 1.0
     */
    public static String[] toStringArray(Iterator<String> iterator) {
        if (null == iterator) {
            return new String[0];
        }

        var strings = new ArrayList<String>();

        while (iterator.hasNext()) {
            strings.add(iterator.next());
        }

        var string_array = new String[strings.size()];
        strings.toArray(string_array);

        return string_array;
    }

    /**
     * Creates a new <code>ArrayList</code>, containing the elements of a
     * supplied array of <code>String</code> objects.
     *
     * @param stringArray The array of <code>String</code> objects that have
     *                    to be converted.
     * @return The new <code>ArrayList</code> with the elements of the
     * <code>String</code> array.
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
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied <code>Collection</code> of <code>String</code> objects joined
     * by a given separator.
     *
     * @param collection The <code>Collection</code> containing the elements
     *                   to join.
     * @param separator  The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
     * @since 1.0
     */
    public static String join(Collection collection, String separator) {
        if (null == collection) {
            return null;
        }

        if (null == separator) {
            separator = "";
        }

        if (0 == collection.size()) {
            return "";
        } else {
            StringBuilder result = new StringBuilder();
            for (Object element : collection) {
                result.append(element);
                result.append(separator);
            }

            result.setLength(result.length() - separator.length());
            return result.toString();
        }
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The object array containing the elements to join.
     * @param separator The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
     * @since 1.0
     */
    public static String join(Object[] array, String separator) {
        return join(array, separator, null, false);
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The object array containing the elements to join.
     * @param separator The separator used to join the string elements.
     * @param delimiter The delimiter used to surround the string elements.
     * @return A new <code>String</code> with the join result.
     * @since 1.0
     */
    public static String join(Object[] array, String separator, String delimiter) {
        return join(array, separator, delimiter, false);
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array         The object array containing the elements to join.
     * @param separator     The separator used to join the string elements.
     * @param delimiter     The delimiter used to surround the string elements.
     * @param encodeStrings Indicates whether the characters of the string
     *                      representation of the Array values should be encoded.
     * @return A new <code>String</code> with the join result.
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
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The boolean array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
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
            int current_index = 0;
            String result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The byte array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
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
            int current_index = 0;
            String result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The double array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
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
            int current_index = 0;
            String result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The float array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
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
            int current_index = 0;
            String result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The integer array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
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
            int current_index = 0;
            String result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The long array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
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
            int current_index = 0;
            String result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The short array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @return A new <code>String</code> with the join result.
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
            int current_index = 0;
            String result = "";
            while (current_index < array.length - 1) {
                result = result + array[current_index] + separator;
                current_index++;
            }

            result = result + array[current_index];
            return result;
        }
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given a.
     *
     * @param array The char array containing the values to join.
     * @param a     The a used to join the string elements.
     * @return A new <code>String</code> with the join result.
     * @since 1.0
     */
    public static String join(char[] array, String a) {
        return join(array, a, null);
    }

    /**
     * Creates a new <code>String</code> object, containing the elements of a
     * supplied array, joined by a given separator.
     *
     * @param array     The char array containing the values to join.
     * @param separator The separator used to join the string elements.
     * @param delimiter The delimiter used to surround the string elements.
     * @return A new <code>String</code> with the join result.
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
            int current_index = 0;
            StringBuilder result = new StringBuilder();
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
     * @param source    The <code>String</code> object that will be searched in.
     * @param substring The string whose occurances will we counted.
     * @return An <code>int[]</code> array containing the indices of the
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
     * @param source    The <code>String</code> object that will be searched in.
     * @param substring The string whose occurances will we counted.
     * @param matchCase A <code>boolean</code> indicating if the match is
     *                  going to be performed in a case-sensitive manner or not.
     * @return An <code>int[]</code> array containing the indices of the
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
     * @param value   The <code>String</code> that will be checked.
     * @param regexps The collection of regular expressions against which the
     *                match will be performed.
     * @return The <code>Matcher</code> instance that corresponds to the
     * <code>String</code> that returned a successful match; or
     * <p><code>null</code> if no match could be found.
     * @since 1.0
     */
    public static Matcher getMatchingRegexp(String value, Collection<Pattern> regexps) {
        if (value != null &&
            value.length() > 0 &&
            regexps != null &&
            regexps.size() > 0) {
            Matcher matcher = null;
            for (Pattern regexp : regexps) {
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
     * @param values The <code>Collection</code> of <code>String</code>
     *               objects that will be checked.
     * @param regexp The regular expression <code>Pattern</code> against which
     *               the matches will be performed.
     * @return The <code>Matcher</code> instance that corresponds to the
     * <code>String</code> that returned a successful match; or
     * <p><code>null</code> if no match could be found.
     * @since 1.0
     */
    public static Matcher getRegexpMatch(Collection<String> values, Pattern regexp) {
        if (values != null &&
            values.size() > 0 &&
            regexp != null) {
            Matcher matcher = null;
            for (String value : values) {
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
     * @param name     The <code>String</code> that will be filtered.
     * @param included The regular expressions that needs to succeed
     * @param excluded The regular expressions that needs to fail
     * @return <code>true</code> if the name filtered through correctly; or
     * <p><code>false</code> otherwise.
     * @since 1.0
     */
    public static boolean filter(String name, Pattern included, Pattern excluded) {
        Pattern[] included_array = null;
        if (included != null) {
            included_array = new Pattern[]{included};
        }

        Pattern[] excluded_array = null;
        if (excluded != null) {
            excluded_array = new Pattern[]{excluded};
        }

        return filter(name, included_array, excluded_array);
    }

    /**
     * Checks if the name filters through a series of including and excluding
     * regular expressions.
     *
     * @param name     The <code>String</code> that will be filtered.
     * @param included An array of regular expressions that need to succeed
     * @param excluded An array of regular expressions that need to fail
     * @return <code>true</code> if the name filtered through correctly; or
     * <p><code>false</code> otherwise.
     * @since 1.0
     */
    public static boolean filter(String name, Pattern[] included, Pattern[] excluded) {
        if (null == name) {
            return false;
        }

        var accepted = false;

        // retain only the includes
        if (null == included) {
            accepted = true;
        } else {
            for (Pattern pattern : included) {
                if (pattern != null &&
                    pattern.matcher(name).matches()) {
                    accepted = true;
                    break;
                }
            }
        }

        // remove the excludes
        if (accepted &&
            excluded != null) {
            for (Pattern pattern : excluded) {
                if (pattern != null &&
                    pattern.matcher(name).matches()) {
                    accepted = false;
                    break;
                }
            }
        }

        return accepted;
    }

    /**
     * Ensure that the first character of the provided string is upper case.
     *
     * @param source The <code>String</code> to capitalize.
     * @return The capitalized <code>String</code>.
     * @since 1.0
     */
    public static String capitalize(String source) {
        if (source == null || source.length() == 0) {
            return source;
        }

        if (source.length() > 1 &&
            Character.isUpperCase(source.charAt(0))) {
            return source;
        }

        var chars = source.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * Ensure that the first character of the provided string lower case.
     *
     * @param source The <code>String</code> to uncapitalize.
     * @return The uncapitalized <code>String</code>.
     * @since 1.5
     */
    public static String uncapitalize(String source) {
        if (source == null || source.length() == 0) {
            return source;
        }

        if (source.length() > 1 &&
            Character.isLowerCase(source.charAt(0))) {
            return source;
        }

        var chars = source.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private static String convertUrl(String source, Pattern pattern, boolean shorten, boolean sanitize, boolean no_follow) {
        int max_length = RifeConfig.tools().maxVisualUrlLength();

        var result = source;

        var url_matcher = pattern.matcher(source);
        var found = url_matcher.find();
        if (found) {
            String visual_url = null;
            String actual_url = null;
            int last = 0;
            StringBuilder sb = new StringBuilder();
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
                    String ellipsis = "...";
                    int query_index = visual_url.indexOf("?");

                    // remove query string but keep '?'
                    if (query_index != -1) {
                        visual_url = visual_url.substring(0, query_index + 1) + ellipsis;
                    }

                    if (visual_url.length() >= max_length) {
                        int last_slash = visual_url.lastIndexOf("/");
                        int start_slash = visual_url.indexOf("/", visual_url.indexOf("://") + 3);

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
     * @return A <code>String</code> with the corresponding HTML code
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
     * @return A <code>String</code> with the corresponding HTML code
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
            for (BbcodeOption option : options) {
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
            String parsed = source_copy.substring(0, startindex);
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
                String sourcecopycopy = source_copy.substring(0, nextCodeIndex) +
                    "[/code]" +
                    source_copy.substring(nextCodeIndex);
                source_copy = sourcecopycopy;

                endIndex = source_copy.indexOf("[/code]") + 7;
            }

            if (startindex > endIndex) {
                // dangling [/code]
                endIndex = source_copy.indexOf("[/code]", endIndex + 7) + 7;     // 7 == the sizeof "[/code]"
                if (endIndex < 0) {
                    endIndex = source_copy.length() - 1;
                }
            }

            String code = source_copy.substring(startindex, endIndex);

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
            int begin = resultLowerCopy.indexOf("[list]", startIndex + 3);
            int end = resultLowerCopy.indexOf("[/list]", startIndex + 3);
            int next = resultLowerCopy.indexOf("[*]", startIndex + 3); // 3 == sizeof [*]

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

        Matcher color_matcher = BBCODE_COLOR.matcher(result);
        result = color_matcher.replaceAll("<font color=\"$1\">");
        result = StringUtils.replace(result, "[/color]", "</font>", false);

        Matcher size_matcher = BBCODE_SIZE.matcher(result);
        result = size_matcher.replaceAll("<font size=\"$1\">");
        result = StringUtils.replace(result, "[/size]", "</font>", false);

        result = convertUrl(result, BBCODE_URL_SHORT, shorten, sanitize, no_follow);
        result = convertUrl(result, BBCODE_URL_LONG, shorten, sanitize, no_follow);

        if (convert_bare) {
            result = convertUrl(result, BBCODE_BAREURL, shorten, sanitize, no_follow);
        }

        Matcher img_matcher = BBCODE_IMG.matcher(result);
        result = img_matcher.replaceAll("<div class=\"bbcode_img\"><img src=\"$1\" border=\"0\" alt=\"\" /></div>");

        Matcher quote_matcher_long = BBCODE_QUOTE_LONG.matcher(result);
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
     * Converts a <code>String</code> to a <code>boolean</code> value.
     *
     * @param value The <code>String</code> to convert.
     * @return The corresponding <code>boolean</code> value.
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
     * @return A new <code>String</code> object containing the line with the
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
     * Ensures that all whitespace is removed from a <code>String</code>.
     * <p>It also works with a <code>null</code> argument.
     *
     * @param source The <code>String</code> to trim.
     * @return The trimmed <code>String</code>.
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
     * @param document       a <code>String</code> with the document where the
     *                       position should be looked up in
     * @param characterIndex the index of the character
     * @return the resulting <code>DocumentPosition</code> instance; or
     * <p><code>null</code> if the <code>characterIndex</code> was invalid or
     * if the <code>document</code> was null
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

            for (String linebreak : linebreaks) {
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

        return new DocumentPosition(document.substring(last_linebreak_index, next_linebreak_index), line, column);
    }

    /**
     * Reformats a string where lines that are longer than <tt>width</tt>
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
                        for (int j = end - 1; j >= 0; j--) {
                            if (!Character.isWhitespace(line.charAt(j))) {
                                if (j - line_start < width) {
                                    break_line = false;
                                }

                                break;
                            }
                        }
                    }

                    if (break_line) {
                        String line_breaked = line.substring(line_start, start);
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
}