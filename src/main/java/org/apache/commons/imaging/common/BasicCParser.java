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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.imaging.ImagingException;

/**
 * A rudimentary preprocessor and parser for the C programming language.
 *
 * FIXME replace this by a parser generated via ANTLR (if we really need it?!)
 */
public class BasicCParser {

    public static final HashMap<String, Boolean> Types = new HashMap<>();
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

    public static void preprocessinComment(int c, final ByteArrayOutputStream out,final StringBuilder firstComment  ){

        if (c == '*') {//tested
            if (Types.get("hadStar") && !Types.get("seenFirstComment")) {        //untested
                firstComment.append('*');
            }
            Types.put("hadStar",true);
        } else if (c == '/') {//tested
            if (Types.get("hadStar")) {          //untested
                Types.put("hadStar",false);
                Types.put("inComment",false);
                Types.put("seenFirstComment",true);
            } else if (!Types.get("seenFirstComment")) {          //untested
                firstComment.append((char) c);
            }
        } else {//tested
            if (Types.get("hadStar") && !Types.get("seenFirstComment")) {
                firstComment.append('*');
            }
            Types.put("hadStar",false);
            if (!Types.get("seenFirstComment")) {//tested
                firstComment.append((char) c);
            }
        }
    }

    public static void preprocessinString(int c, final ByteArrayOutputStream out,final StringBuilder firstComment  )
    throws IOException, ImagingException{
        switch (c) {
            case '\\':          //untested
                if (Types.get("hadBackSlash")) {          //untested
                    out.write('\\');
                    out.write('\\');
                    Types.put("hadBackSlash",false);
                } else {          //untested
                    Types.put("hadBackSlash",true);
                }
                break;
            case '"':
                if (Types.get("hadBackSlash")) {          //untested
                    out.write('\\');
                    Types.put("hadBackSlash",false);
                } else {//tested
                    Types.put("inString",false);
                }
                out.write('"');
                break;
            case '\r':          //untested
            case '\n':                      //untested for true --> now testet
                throw new ImagingException("Unterminated string in file");
            default://tested
                if (Types.get("hadBackSlash")) {//tested
                    out.write('\\');
                    Types.put("hadBackSlash",false);
                }
                out.write(c);
                break;
            }

    }

    public static void preprocessinDirective(int c, final ByteArrayOutputStream out,final StringBuilder firstComment , final StringBuilder directiveBuffer, final Map<String, String> defines )
    throws IOException, ImagingException{
        if (c == '\r' || c == '\n') {//tested
            Types.put("inDirective",false);
            final String[] tokens = tokenizeRow(directiveBuffer.toString());
            if (tokens.length < 2 || tokens.length > 3) {                         //untested
                throw new ImagingException("Bad preprocessor directive");
            }
            if (!tokens[0].equals("define")) {                         //untested
                throw new ImagingException("Invalid/unsupported " + "preprocessor directive '" + tokens[0] + "'");
            }
            defines.put(tokens[1], tokens.length == 3 ? tokens[2] : null);
            directiveBuffer.setLength(0);
        } else {//tested
            directiveBuffer.append((char) c);
        }

    }

    public static void preprocessinSingleQuotes(int c, final ByteArrayOutputStream out,final StringBuilder firstComment  )
    throws IOException, ImagingException{

        switch (c) {
            case '\\':          //untested
                if (Types.get("hadBackSlash")) {          //untested
                    out.write('\\');
                    out.write('\\');
                    Types.put("hadBackSlash",false);
                } else {          //untested
                    Types.put("hadBackSlash",true);
                }
                break;
            case '\'':          //untested
                if (Types.get("hadBackSlash")) {          //untested
                    out.write('\\');
                    Types.put("hadBackSlash",false);
                } else {          //untested
                    Types.put("inSingleQuotes",false);
                }
                out.write('\'');
                break;
            case '\r':          //untested
            case '\n':          //untested
                throw new ImagingException("Unterminated single quote in file");
            default:          //untested
                if (Types.get("hadBackSlash")) {          //untested
                    out.write('\\');
                    Types.put("hadBackSlash",false);
                }
                out.write(c);
                break;
            }
    }
        

    public static void preprocessHelper(int c, final ByteArrayOutputStream out,final StringBuilder firstComment,final Map<String, String> defines)
    throws IOException, ImagingException{
        switch (c) {
            case '/'://tested
                if (Types.get("hadSlash")) {                         //untested
                    out.write('/');
                }
                Types.put("hadSlash",true);
                break;
            case '*'://tested
                if (Types.get("hadSlash")) {//tested
                    Types.put("inComment",true);
                    Types.put("hadSlash",false);
                } else {//tested
                    out.write(c);
                }
                break;
            case '\'':                         //untested
                if (Types.get("hadSlash")) {                         //untested
                    out.write('/');
                }
                Types.put("hadSlash",false);
                out.write(c);
                Types.put("inSingleQuotes",true);
                break;
            case '"'://tested
                if (Types.get("hadSlash")) {                         //untested
                    out.write('/');
                }
                Types.put("hadSlash",false);
                out.write(c);
                Types.put("inString",true);
                break;
            case '#'://tested
                if (defines == null) {          //untested for true --> now testet
                    throw new ImagingException("Unexpected preprocessor directive");
                }
                Types.put("inDirective",true);
                break;
            default://tested
                if (Types.get("hadSlash")) {                         //untested
                    out.write('/');
                }
                Types.put("hadSlash",false);
                out.write(c);
                // Only whitespace allowed before first comment:
                if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {//tested
                    Types.put("seenFirstComment",true);
                }
                break;
            }
    }



    public static ByteArrayOutputStream preprocess(final InputStream is, final StringBuilder firstComment, final Map<String, String> defines)
        throws IOException, ImagingException {



        Types.put("inSingleQuotes", false);
        Types.put("inString", false);
        Types.put("inComment", false);
        Types.put("inDirective", false);
        Types.put("hadSlash", false);
        Types.put("hadStar", false);
        Types.put("hadBackSlash", false);
        Types.put("seenFirstComment", firstComment == null);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final StringBuilder directiveBuffer = new StringBuilder();


        for (int c = is.read(); c != -1; c = is.read()) {
            if (Types.get("inComment")) {//tested
                preprocessinComment(c,out, firstComment);
                
            } else if (Types.get("inSingleQuotes")) {           //untested
                try {
                    preprocessinSingleQuotes(c,out, firstComment);
                } catch (ImagingException e) {
                    throw e;
                }
                
            } else if (Types.get("inString")) {//tested
                try {
                    preprocessinString(c,out, firstComment);
                } catch (ImagingException e) {
                    throw e;
                }
                
            } else if (Types.get("inDirective")) {//tested
                try {
                    preprocessinDirective(c,out, firstComment, directiveBuffer, defines);
                } catch (ImagingException e) {
                    throw e;
                }
                
            } else {//tested
                try {
                    preprocessHelper(c,out, firstComment,defines);
                } catch (ImagingException e) {
                    throw e;
                }
                
                
            }
        }
        if (Types.get("hadSlash")) {                         //untested
            out.write('/');
        }
        if (Types.get("hadStar")) {                         //untested
            out.write('*');
        }
        if (Types.get("inString")) {             //untested for true --> now tested
            throw new ImagingException("Unterminated string at the end of file");
        }
        if (Types.get("inComment")) {            //untested for true --> now testet
            throw new ImagingException("Unterminated comment at the end of file");
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
