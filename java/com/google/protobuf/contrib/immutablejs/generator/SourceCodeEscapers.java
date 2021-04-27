/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.protobuf.contrib.immutablejs.generator;

import com.google.common.escape.ArrayBasedCharEscaper;
import com.google.common.escape.Escaper;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for Escaper instances used to escape strings for safe use in various common programming
 * languages.
 *
 * <p>This is a subset of source code escapers that are in the process of being open-sources as part
 * of guava, see: https://github.com/google/guava/issues/1620
 */
public final class SourceCodeEscapers {
  private SourceCodeEscapers() {}

  // For each xxxEscaper() method, please add links to external reference pages
  // that are considered authoritative for the behavior of that escaper.

  // From: http://en.wikipedia.org/wiki/ASCII#ASCII_printable_characters
  private static final char PRINTABLE_ASCII_MIN = 0x20; // ' '
  private static final char PRINTABLE_ASCII_MAX = 0x7E; // '~'

  private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

  /**
   * Returns an {@link Escaper} instance that escapes special characters in a string so it can
   * safely be included in either a Java character literal or string literal. This is the preferred
   * way to escape Java characters for use in String or character literals.
   *
   * <p>For more details, see <a href="http://goo.gl/NsGW7">Escape Sequences for Character and
   * String Literals</a> in The Java Language Specification.
   */
  public static Escaper javaCharEscaper() {
    return JAVA_CHAR_ESCAPER;
  }

  private static final Escaper JAVA_CHAR_ESCAPER;

  static {
    Map<Character, String> javaMap = new HashMap<>();
    javaMap.put('\b', "\\b");
    javaMap.put('\f', "\\f");
    javaMap.put('\n', "\\n");
    javaMap.put('\r', "\\r");
    javaMap.put('\t', "\\t");
    javaMap.put('\"', "\\\"");
    javaMap.put('\\', "\\\\");
    javaMap.put('\'', "\\'");
    JAVA_CHAR_ESCAPER = new JavaCharEscaper(javaMap);
  }

  // This escaper does not produce octal escape sequences.
  // "Octal escapes are provided for compatibility with C, but can express
  // only Unicode values \u0000 through \u00FF, so Unicode escapes are
  // usually preferred."
  private static class JavaCharEscaper extends ArrayBasedCharEscaper {
    JavaCharEscaper(Map<Character, String> replacements) {
      super(replacements, PRINTABLE_ASCII_MIN, PRINTABLE_ASCII_MAX);
    }

    @Override
    protected char[] escapeUnsafe(char c) {
      return asUnicodeHexEscape(c);
    }
  }

  /**
   * Returns an {@link Escaper} instance that replaces non-ASCII characters in a string with their
   * equivalent Javascript UTF-16 escape sequences "{@literal \}unnnn", "\xnn" or special
   * replacement sequences "\b", "\t", "\n", "\f", "\r" or "\\".
   *
   * <p><b>Warning:</b> This escaper is <b>not</b> suitable for JSON. JSON users may wish to use <a
   * href="http://code.google.com/p/google-gson/">GSON</a> or other high-level APIs when possible.
   */
  public static Escaper javascriptEscaper() {
    return JAVASCRIPT_ESCAPER;
  }

  /**
   * An Escaper for javascript strings. Turns all non-ASCII characters into ASCII javascript escape
   * sequences.
   */
  private static final Escaper JAVASCRIPT_ESCAPER;

  static {
    Map<Character, String> jsMap = new HashMap<>();
    jsMap.put('\'', "\\x27");
    jsMap.put('"', "\\x22");
    jsMap.put('<', "\\x3c");
    jsMap.put('=', "\\x3d");
    jsMap.put('>', "\\x3e");
    jsMap.put('&', "\\x26");
    jsMap.put('\b', "\\b");
    jsMap.put('\t', "\\t");
    jsMap.put('\n', "\\n");
    jsMap.put('\f', "\\f");
    jsMap.put('\r', "\\r");
    jsMap.put('\\', "\\\\");
    JAVASCRIPT_ESCAPER =
        new ArrayBasedCharEscaper(jsMap, PRINTABLE_ASCII_MIN, PRINTABLE_ASCII_MAX) {
          @Override
          protected char[] escapeUnsafe(char c) {
            // Do two digit hex escape for value less than 0x100.
            if (c < 0x100) {
              char[] r = new char[4];
              r[3] = HEX_DIGITS[c & 0xF];
              c >>>= 4;
              r[2] = HEX_DIGITS[c & 0xF];
              r[1] = 'x';
              r[0] = '\\';
              return r;
            }
            return asUnicodeHexEscape(c);
          }
        };
  }

  // Helper for common case of escaping a single char.
  private static char[] asUnicodeHexEscape(char c) {
    // Equivalent to String.format("\\u%04x", (int) c);
    char[] r = new char[6];
    r[0] = '\\';
    r[1] = 'u';
    r[5] = HEX_DIGITS[c & 0xF];
    c >>>= 4;
    r[4] = HEX_DIGITS[c & 0xF];
    c >>>= 4;
    r[3] = HEX_DIGITS[c & 0xF];
    c >>>= 4;
    r[2] = HEX_DIGITS[c & 0xF];
    return r;
  }
}
