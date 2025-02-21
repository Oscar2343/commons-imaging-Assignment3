/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.imaging.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Map;

import org.apache.commons.imaging.Coverage;
import org.apache.commons.imaging.ImagingException;

/**
 * A rudimentary preprocessor and parser for the C programming language.
 *
 * FIXME replace this by a parser generated via ANTLR (if we really need it?!)
 */
public class BasicCParser {
    /**
     * Parses the hexadecimal-base escape-sequence found at index {@code i} of {@code string}.
     *
     * <p>
     * Helper-function for {@code unescapeString()}.
     * </p>
     *
     * @param i             the index of the escape-sequence in the string
     * @param stringBuilder the stringBuilder to append the escape-char to
     * @param string        the string whose chars are parsed
     * @return the new index i
     * @since 1.0-alpha3
     */
    private static int appendHex(int i, final StringBuilder stringBuilder, final String string) throws ImagingException {
        if (i + 2 >= string.length()) {
            throw new ImagingException("Parsing XPM file failed, " + "hex constant in string too short");
        }
        final char hex1 = string.charAt(i + 1);
        final char hex2 = string.charAt(i + 2);
        i += 2;
        final int constant;
        try {
            constant = Integer.parseInt(hex1 + Character.toString(hex2), 16);
        } catch (final NumberFormatException nfe) {
            throw new ImagingException("Parsing XPM file failed, " + "hex constant invalid", nfe);
        }
        stringBuilder.append((char) constant);
        return i;
    }

    /**
     * Parses the octal-base escape-sequence found at index {@code i} of {@code string}.
     *
     * <p>
     * Helper-function for {@code unescapeString()}.
     * </p>
     *
     * @param i             the index of the escape-sequence in the string
     * @param stringBuilder the stringBuilder to append the escape-char to
     * @param string        the string whose chars are parsed
     * @return the new index i
     * @since 1.0-alpha3
     */
    private static int appendOct(int i, final StringBuilder stringBuilder, final String string) {
        int length = 1;
        if (i + 1 < string.length() && '0' <= string.charAt(i + 1) && string.charAt(i + 1) <= '7') {
            ++length;
        }
        if (i + 2 < string.length() && '0' <= string.charAt(i + 2) && string.charAt(i + 2) <= '7') {
            ++length;
        }
        int constant = 0;
        for (int j = 0; j < length; j++) {
            constant *= 8;
            constant += string.charAt(i + j) - '0';
        }
        i += length - 1;
        stringBuilder.append((char) constant);
        return i;
    }

    /**
     * Parses the {@code i:th} escape-char in the input {@code string} and appends it to {@code stringBuilder}.
     *
     * <p>
     * Helper-function for {@code unescapeString()}.
     * </p>
     *
     * @param i             the index of the escape-char in the string
     * @param stringBuilder the stringBuilder to append the escape-char to
     * @param string        the string whose chars are parsed
     * @return the new index i
     * @since 1.0-alpha3
     */
    private static int parseEscape(int i, final StringBuilder stringBuilder, final String string) throws ImagingException {
        final char c = string.charAt(i);
        switch (c) {
        case '\\':
            stringBuilder.append('\\');
            break;
        case '"':
            stringBuilder.append('"');
            break;
        case '\'':
            stringBuilder.append('\'');
            break;
        case 'x':
            i = appendHex(i, stringBuilder, string);
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
            i = appendOct(i, stringBuilder, string);
            break;
        case 'a':
            stringBuilder.append((char) 0x07);
            break;
        case 'b':
            stringBuilder.append((char) 0x08);
            break;
        case 'f':
            stringBuilder.append((char) 0x0c);
            break;
        case 'n':
            stringBuilder.append((char) 0x0a);
            break;
        case 'r':
            stringBuilder.append((char) 0x0d);
            break;
        case 't':
            stringBuilder.append((char) 0x09);
            break;
        case 'v':
            stringBuilder.append((char) 0x0b);
            break;
        default:
            throw new ImagingException("Parsing XPM file failed, " + "invalid escape sequence");
        }
        return i;

    }

    public static ByteArrayOutputStream preprocess(final InputStream is, final StringBuilder firstComment, final Map<String, String> defines)
            throws IOException, ImagingException {
        boolean inSingleQuotes = false;
        boolean inString = false;
        boolean inComment = false;
        boolean inDirective = false;
        boolean hadSlash = false;
        boolean hadStar = false;
        boolean hadBackSlash = false;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean seenFirstComment = firstComment == null;
        final StringBuilder directiveBuffer = new StringBuilder();
        for (int c = is.read(); c != -1; c = is.read()) {
            if (inComment) {
                Coverage.coverageMap.put(1, true);
                if (c == '*') {
                    Coverage.coverageMap.put(2, true);
                    if (hadStar && !seenFirstComment) {
                        Coverage.coverageMap.put(3, true);
                        firstComment.append('*');
                    }else{
                        Coverage.coverageMap.put(4, true);
                    }
                    hadStar = true;
                } else if (c == '/') {
                    Coverage.coverageMap.put(5, true);
                    if (hadStar) {
                        Coverage.coverageMap.put(6, true);
                        hadStar = false;
                        inComment = false;
                        seenFirstComment = true;
                    } else if (!seenFirstComment) {
                        Coverage.coverageMap.put(7, true);
                        firstComment.append((char) c);
                    }else{
                        Coverage.coverageMap.put(8, true);
                    }
                } else {
                    Coverage.coverageMap.put(9, true);
                    if (hadStar && !seenFirstComment) {
                        Coverage.coverageMap.put(10, true);
                        firstComment.append('*');
                    }else{
                        Coverage.coverageMap.put(11, true);
                    }
                    hadStar = false;
                    if (!seenFirstComment) {
                        Coverage.coverageMap.put(12, true);
                        firstComment.append((char) c);
                    }else{
                        Coverage.coverageMap.put(13, true);
                    }
                }
            } else if (inSingleQuotes) {
                Coverage.coverageMap.put(14, true);
                switch (c) {
                case '\\':
                    Coverage.coverageMap.put(15, true);
                    if (hadBackSlash) {
                        Coverage.coverageMap.put(16, true);
                        out.write('\\');
                        out.write('\\');
                        hadBackSlash = false;
                    } else {
                        Coverage.coverageMap.put(17, true);
                        hadBackSlash = true;
                    }
                    break;
                case '\'':
                    Coverage.coverageMap.put(18, true);
                    if (hadBackSlash) {
                        Coverage.coverageMap.put(19, true);
                        out.write('\\');
                        hadBackSlash = false;
                    } else {
                        Coverage.coverageMap.put(20, true);
                        inSingleQuotes = false;
                    }
                    out.write('\'');
                    break;
                case '\r':
                    Coverage.coverageMap.put(21, true);
                case '\n':
                    Coverage.coverageMap.put(22, true);
                    throw new ImagingException("Unterminated single quote in file");
                default:
                    Coverage.coverageMap.put(23, true);
                    if (hadBackSlash) {
                        Coverage.coverageMap.put(24, true);
                        out.write('\\');
                        hadBackSlash = false;
                    }else{
                        Coverage.coverageMap.put(25, true);
                    }
                    out.write(c);
                    break;
                }
            } else if (inString) {
                Coverage.coverageMap.put(27, true);
                switch (c) {
                case '\\':
                    Coverage.coverageMap.put(28, true);
                    if (hadBackSlash) {
                        Coverage.coverageMap.put(29, true);
                        out.write('\\');
                        out.write('\\');
                        hadBackSlash = false;
                    } else {
                        Coverage.coverageMap.put(30, true);
                        hadBackSlash = true;
                    }
                    break;
                case '"':
                    Coverage.coverageMap.put(31, true);
                    if (hadBackSlash) {
                        Coverage.coverageMap.put(32, true);
                        out.write('\\');
                        hadBackSlash = false;
                    } else {
                        Coverage.coverageMap.put(33, true);
                        inString = false;
                    }
                    out.write('"');
                    break;
                case '\r':
                    Coverage.coverageMap.put(34, true);
                case '\n':
                    Coverage.coverageMap.put(35, true);
                    throw new ImagingException("Unterminated string in file");
                default:
                    Coverage.coverageMap.put(36, true);
                    if (hadBackSlash) {
                        Coverage.coverageMap.put(37, true);
                        out.write('\\');
                        hadBackSlash = false;
                    }else{
                        Coverage.coverageMap.put(38, true);
                    }
                    out.write(c);
                    break;
                }
            } else if (inDirective) {
                Coverage.coverageMap.put(39, true);
                if (c == '\r' || c == '\n') {
                    Coverage.coverageMap.put(40, true);
                    inDirective = false;
                    final String[] tokens = tokenizeRow(directiveBuffer.toString());
                    if (tokens.length < 2 || tokens.length > 3) {
                        Coverage.coverageMap.put(41, true);
                        throw new ImagingException("Bad preprocessor directive");
                    }else{
                        Coverage.coverageMap.put(42, true);
                    }
                    if (!tokens[0].equals("define")) {
                        Coverage.coverageMap.put(43, true);
                        throw new ImagingException("Invalid/unsupported " + "preprocessor directive '" + tokens[0] + "'");
                    }else{
                        Coverage.coverageMap.put(44, true);
                    }
                    defines.put(tokens[1], tokens.length == 3 ? tokens[2] : null);
                    directiveBuffer.setLength(0);
                } else {
                    Coverage.coverageMap.put(45, true);
                    directiveBuffer.append((char) c);
                }
            } else {
                Coverage.coverageMap.put(47, true);
                switch (c) {
                case '/':
                    if (hadSlash) {
                        Coverage.coverageMap.put(48, true);
                        out.write('/');
                    }else{
                        Coverage.coverageMap.put(49, true);
                    }
                    hadSlash = true;
                    break;
                case '*':
                    Coverage.coverageMap.put(50, true);
                    if (hadSlash) {
                        Coverage.coverageMap.put(51, true);
                        inComment = true;
                        hadSlash = false;
                    } else {
                        Coverage.coverageMap.put(52, true);
                        out.write(c);
                    }
                    break;
                case '\'':
                    Coverage.coverageMap.put(53, true);
                    if (hadSlash) {
                        Coverage.coverageMap.put(54, true);
                        out.write('/');
                    }else{
                        Coverage.coverageMap.put(55, true);
                    }
                    hadSlash = false;
                    out.write(c);
                    inSingleQuotes = true;
                    break;
                case '"':
                    Coverage.coverageMap.put(56, true);
                    if (hadSlash) {
                        Coverage.coverageMap.put(57, true);
                        out.write('/');
                    }else{
                        Coverage.coverageMap.put(58, true);
                    }
                    hadSlash = false;
                    out.write(c);
                    inString = true;
                    break;
                case '#':
                    Coverage.coverageMap.put(59, true);
                    if (defines == null) {
                        Coverage.coverageMap.put(60, true);
                        throw new ImagingException("Unexpected preprocessor directive");
                    }else{
                        Coverage.coverageMap.put(61, true);
                    }
                    inDirective = true;
                    break;
                default:
                    Coverage.coverageMap.put(62, true);
                    if (hadSlash) {
                        Coverage.coverageMap.put(63, true);
                        out.write('/');
                    }else{
                        Coverage.coverageMap.put(64, true);
                    }
                    hadSlash = false;
                    out.write(c);
                    // Only whitespace allowed before first comment:
                    if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                        Coverage.coverageMap.put(65, true);
                        seenFirstComment = true;
                    }else{
                        Coverage.coverageMap.put(66, true);
                    }
                    break;
                }
            }
        }
        if (hadSlash) {
            Coverage.coverageMap.put(67, true);
            out.write('/');
        }else{
            Coverage.coverageMap.put(68, true);
        }
        if (hadStar) {
            Coverage.coverageMap.put(69, true);
            out.write('*');
        }else{
            Coverage.coverageMap.put(70, true);
        }
        if (inString) {
            Coverage.coverageMap.put(71, true);
            throw new ImagingException("Unterminated string at the end of file");
        }else{
            Coverage.coverageMap.put(72, true);
        }
        if (inComment) {
            Coverage.coverageMap.put(73, true);
            throw new ImagingException("Unterminated comment at the end of file");
        }else{
            Coverage.coverageMap.put(74, true);
        }
        return out;
    }

    public static String[] tokenizeRow(final String row) {
        final String[] tokens = row.split("[ \t]");
        int numLiveTokens = 0;
        for (final String token : tokens) {
            if (token != null && !token.isEmpty()) {
                ++numLiveTokens;
            }
        }
        final String[] liveTokens = Allocator.array(numLiveTokens, String[]::new, 24);
        int next = 0;
        for (final String token : tokens) {
            if (token != null && !token.isEmpty()) {
                liveTokens[next++] = token;
            }
        }
        return liveTokens;
    }

    public static void unescapeString(final StringBuilder stringBuilder, final String string) throws ImagingException {
        if (string.length() < 2) {
            throw new ImagingException("Parsing XPM file failed, " + "string is too short");
        }
        if (string.charAt(0) != '"' || string.charAt(string.length() - 1) != '"') {
            throw new ImagingException("Parsing XPM file failed, " + "string not surrounded by '\"'");
        }
        boolean hadBackSlash = false;
        for (int i = 1; i < string.length() - 1; i++) {
            final char c = string.charAt(i);
            if (hadBackSlash) {
                i = parseEscape(i, stringBuilder, string);
                hadBackSlash = false;
            } else if (c == '\\') {
                hadBackSlash = true;
            } else if (c == '"') {
                throw new ImagingException("Parsing XPM file failed, " + "extra '\"' found in string");
            } else {
                stringBuilder.append(c);
            }
        }
        if (hadBackSlash) {
            throw new ImagingException("Parsing XPM file failed, " + "unterminated escape sequence found in string");
        }
    }

    private final PushbackInputStream is;

    public BasicCParser(final ByteArrayInputStream is) {
        this.is = new PushbackInputStream(is);
    }

    public String nextToken() throws IOException, ImagingException {
        // I don't know how complete the C parsing in an XPM file
        // is meant to be, this is just the very basics...

        boolean inString = false;
        boolean inIdentifier = false;
        boolean hadBackSlash = false;
        final StringBuilder token = new StringBuilder();
        for (int c = is.read(); c != -1; c = is.read()) {
            if (inString) {
                switch (c) {
                case '\\':
                    token.append('\\');
                    hadBackSlash = !hadBackSlash;
                    break;
                case '"':
                    token.append('"');
                    if (!hadBackSlash) {
                        return token.toString();
                    }
                    hadBackSlash = false;
                    break;
                case '\r':
                case '\n':
                    throw new ImagingException("Unterminated string in XPM file");
                default:
                    token.append((char) c);
                    hadBackSlash = false;
                    break;
                }
            } else if (inIdentifier) {
                if (!Character.isLetterOrDigit(c) && c != '_') {
                    is.unread(c);
                    return token.toString();
                }
                token.append((char) c);
            } else if (c == '"') {
                token.append('"');
                inString = true;
            } else if (Character.isLetterOrDigit(c) || c == '_') {
                token.append((char) c);
                inIdentifier = true;
            } else if (c == '{' || c == '}' || c == '[' || c == ']' || c == '*' || c == ';' || c == '=' || c == ',') {
                token.append((char) c);
                return token.toString();
            } else if (c == ' ' || c == '\t' || c == '\r' || c == '\n') { // NOPMD
                // ignore
            } else {
                throw new ImagingException("Unhandled/invalid character '" + (char) c + "' found in XPM file");
            }
        }

        if (inIdentifier) {
            return token.toString();
        }
        if (inString) {
            throw new ImagingException("Unterminated string ends XMP file");
        }
        return null;
    }

}
