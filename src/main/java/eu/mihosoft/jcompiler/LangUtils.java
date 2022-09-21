/*
 * Copyright 2020-2022 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Code is based on abandoned project by Trung (https://github.com/trung/InMemoryJavaCompiler).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mihosoft.jcompiler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Language utilities based on VRLs VLangUtils.
 * 
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
/*pkg private*/ final class LangUtils {

    private LangUtils() {
        throw new AssertionError("Don't instantiate me!");
    }

    private static String filterComments(String code) {
        
        StringBuilder result = new StringBuilder();

        String[] lines = code.split("\n");

        boolean multiLineComment = false;
        boolean insideComment = false;

        for (String l : lines) {

            char lastChar = 0;
            boolean doubleQuotes = false;
            boolean singleLineComment = false;
            boolean multiLineCommentStop = false;

            for (int i = 0; i < l.length(); i++) {

                char ch = l.charAt(i);

                // find double quotes (handles encapsulated quotes correctly)
                if (ch == '\"' && lastChar != '\\') {
                    doubleQuotes = !doubleQuotes;
                }

                // single-line comment
                if (ch == '/' && lastChar == '/') {
                    singleLineComment = !doubleQuotes || singleLineComment;
                }

                // multi-line comment start
                if (lastChar == '/' && ch == '*') {
                    multiLineComment = (!doubleQuotes || multiLineComment)
                            && !multiLineCommentStop;
                }

                // multi-line comment stop
                if (lastChar == '*' && ch == '/') {
                    multiLineComment = doubleQuotes && multiLineComment;
                    multiLineCommentStop = true && !doubleQuotes;
                }

                insideComment = singleLineComment || multiLineComment;

                // if we are in a single-line comment there is nothing to check,
                // we can break
                if (singleLineComment) {
                    break;
                }

                // if we are not inside of a comment, we write characters
                if (!insideComment) {
                    // we did read ahead last time to check whether we are
                    // inside of a comment. as we were not, we write the
                    // last char now.
                    if (lastChar == '/' && !multiLineCommentStop) {
                        result.append(lastChar);
                    }
                    // if the current character is a '/' we do not write this
                    // character as we do not know if this is the beginning of
                    // a comment
                    if (ch != '/') {
                        multiLineCommentStop = false;
                        result.append(ch);
                    }
                }

                lastChar = ch;
            }

            if (!insideComment && lastChar!='/') {
                result.append('\n');
            }

        } // end for lines

        return result.toString();
    }

    private static String filterStrings(String code) {

        StringBuilder result = new StringBuilder();

            boolean insideString = false;
            boolean insideChar = false;
            boolean insideEscape = false;

            for (int i = 0; i < code.length(); i++) {

                char ch = code.charAt(i);

                if (ch == '\\') {
                    insideEscape = !insideEscape;
                }

                // find quotes (handles encapsulated quotes correctly)
                if (ch == '\'' && !(insideEscape || insideString)) {
                    insideChar = !insideChar;
                }

                // find double quotes (handles encapsulated quotes correctly)
                if (ch == '\"' && !(insideEscape || insideChar)) {
                    insideString = !insideString;
                }

                // we print the character if we are not inside escape or inside char
                if (!insideString && (ch != '\"' || insideChar || insideEscape)) {
                    result.append(ch);
                }

                // current char is no \ (backslash).
                // thus, we are defenitely not "inside escape"
                if (ch != '\\') {
                    insideEscape = false;
                }
            }

        return result.toString();
    }

    private static String filterChars(String code) {

        StringBuilder result = new StringBuilder();

            boolean insideString = false;
            boolean insideChar = false;
            boolean insideEscape = false;

            for (int i = 0; i < code.length(); i++) {

                char ch = code.charAt(i);

                if (ch == '\\') {
                    insideEscape = !insideEscape;
                }
                
                // find double quotes (handles encapsulated quotes correctly)
                if (ch == '\"' && !(insideEscape || insideChar)) {
                    insideString = !insideString;
                }

                // find quotes (handles encapsulated quotes correctly)
                if (ch == '\'' && !(insideEscape || insideString)) {
                    insideChar = !insideChar;
                }

                // we print the character if we are not inside escape or inside char
                if (!insideChar && (ch != '\'' || insideString || insideEscape)) {
                    result.append(ch);
                }

                // current char is no \ (backslash).
                // thus, we are defenitely not "inside escape"
                if (ch != '\\') {
                    insideEscape = false;
                }
            }

        return result.toString();
    }

    private static String removeTypeBodyContent(String code) {

        code = removeCommentsAndStringsFromCode(code);

        StringBuilder result = new StringBuilder();

            int depthCounter = 0;

            for (int i = 0; i < code.length(); i++) {

                char ch = code.charAt(i);

                if (ch == '{') {
                    depthCounter++;
                }

                // we print the character if we are not inside escape or inside char
                if (depthCounter==0) {
                    result.append(ch);
                }

                if(ch == '}') {
                    depthCounter--;
                }
            }

        return result.toString();
    }

    /**
     * Removes comments, strings and chars from code, i.e.
     *
     * <pre>
     * 1: // comment 1
     * 2: /* comment 2
     * 3:    still in comment2 *&#47;
     * 4:
     * 5: String s = "Classname";
     * 6:
     * 7: char c = 'A';
     * 8:
     * </pre>
     *
     * becomes
     *
     * <pre>
     * 1:
     * 2:
     * 3:
     * 4:
     * 5: String s = ;
     * 6:
     * 7: char c = ;
     * 8:
     * </pre>
     *
     * This is usefull for methods that search for class dependencies in code
     * where strings inside comments or string literals must not be matched.
     */
    public static String removeCommentsAndStringsFromCode(String code) {

        // filter comments (classname could occur in comment)
        code = filterComments(code);

        // filter strings (classname could occur in strings)
        code = filterStrings(code);

        // filter chars (classname could occur in chars for one-char names
        // like A,B,C etc.)
        code = filterChars(code);
        
        return code;
    }

    /**
     * Returns the class name of either the public class/interface or alternatively the first class/interface
     * defined in the given source code.
     *
     * @param code code to analyze
     * @return class name of either the first public class/interface defined in the given source code or the first class/interface if no public type definition can be found or
     * an empty string if no class/interface has been defined
     */
    public static String getClassNameFromCode(String code) {

        // remove coontents of class bodies, comments, strings and chars from code
        // -> remaining string can be safely matched with regex
        code = removeTypeBodyContent(code);

        String result = "";

        Matcher m = Patterns.CLASS_OR_INTERFACE_DEFINITION.matcher(code);

        String first = null;

        String pkgName = getPackageNameFromCode(code);

        while (m.find()) {
            String clsDef = m.group();

            boolean publicCls = clsDef.contains("public");

            // remove modifiers and 'class' and 'interface' keywords
            result = clsDef.replaceFirst(
                Patterns.CLASS_OR_INTERFACE_DEFINITION_WITHOUT_IDENTIFIER_STRING, "")
                    .split(" ")[0];

            // add package name if present
            result=(pkgName.isEmpty()?"":pkgName+".")+result;

            // remember first type declaration in code 
            if(first==null) {
                first = result;
            }

            // return first occurance of public type declaration
            if(publicCls) return result;
        }

        return first;
    }

    /**
     * @return a list of all class or interface declarations in order of appearance
     */
    public static List<String> getClassNamesFromCode(String code) {

        // remove comments, strings and chars from code
        // -> remaining string can be safely matched with regex
        code = removeCommentsAndStringsFromCode(code);

        String pkgName = getPackageNameFromCode(code);

        List<String> result = new ArrayList<>();

        Matcher m = Patterns.CLASS_OR_INTERFACE_DEFINITION.matcher(code);

        while (m.find()) {
            String clsDef = m.group();

            // remove modifiers and 'class' and 'interface' keywords
            String clsName = clsDef.replaceFirst(
                Patterns.CLASS_OR_INTERFACE_DEFINITION_WITHOUT_IDENTIFIER_STRING, "")
                    .split(" ")[0];

            result.add((pkgName.isEmpty()?"":pkgName+".")+clsName);
        }

        return result;
    }

     /**
     * Get package name defined in the given source code.
     *
     * @param code code to analyze
     * @return package name defined in the given source code or empty string if
     * no package could be found
     */
    private static String getPackageNameFromCode(String code) {
        String result = "";

        code = removeCommentsAndStringsFromCode(code);

        String[] lines = code.split("\\n");

        // match example: ^package eu.mihosoft.vrl;$
        Pattern p1 =
                Pattern.compile(
                "^\\s*package\\s+" + Patterns.PACKAGE_NAME_STRING + ";",
                Pattern.DOTALL);

        for (String l : lines) {

            l = l.trim();

            Matcher m1 = p1.matcher(l);

            if (m1.find()) {

                l = m1.group();

                result =
                        l.replaceFirst("^\\s*package\\s+", "").
                        split(" ")[0].replace(";", "");
                break;
            }
        }

        return result;
    }

    /**
     *
     * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
     */
    private static class Patterns {

        /**
         * Regular expression for a valid identifier (variable, class or method
         * names). This is the unicode version of "[a-zA-Z$_][a-zA-Z$_0-9]*". 
         * It does not exclude keywords. This should be checked separately.
         */
        public static final String IDENTIFIER_STRING = "[\\p{L}\\p{Pc}$][\\p{L}\\p{N}\\p{Pc}$]*";

        /**
         * Regular expression for a list of valid identifiers.
         */
        public static final String IDENTIFIER_LIST_STRING =
                "(" + IDENTIFIER_STRING + "\\s*,\\s*)*"
                + IDENTIFIER_STRING;

        /**
         * <p>Regular expression to match block-comments.</p> <p><b>Note:</b> does
         * also match block-comments inside strings! Thus, to work correctly strings
         * have to be removed before using this expression.</p>
         */
        public static final String BLOCK_COMMENT_STRING =
                "/\\*(?:.|[\\n\\r])*?\\*/";

        /**
         * Pattern to match an identifier.
         */
        public static final Pattern IDENTIFIER = Pattern.compile(IDENTIFIER_STRING);
        /**
         * Pattern to match an identifier list.
         */
        public static final Pattern IDENTIFIER_LIST = Pattern.compile(IDENTIFIER_LIST_STRING);
        /**
         * <p>Regular expression to match package names.</p> (default package, i.e.,
         * empty string is NOT supported)
         */
        public static final String PACKAGE_NAME_STRING =
                "(" + IDENTIFIER_STRING + ")" + "(\\." + IDENTIFIER_STRING + ")*";
        /**
         * Pattern to match package name (default package, i.e., empty string is NOT
         * supported)
         */
        public static final Pattern PACKAGE_NAME =
                Pattern.compile(PACKAGE_NAME_STRING,
                Pattern.DOTALL);
        /**
         * Regular expression to match import definition.
         */
        // \\b is stands for word boundary
        public static final String IMPORT_DEFINITION_STRING =
                "\\bimport\\b\\s+" + Patterns.PACKAGE_NAME_STRING
                + "\\s*(;|\\.\\*\\s*;)";

        /**
         * Pattern to match import definition.
         */
        public static final Pattern IMPORT_DEFINITION =
                Pattern.compile(IMPORT_DEFINITION_STRING,
                Pattern.DOTALL);
        /**
         * Regular expression to match class definition (without class name).
         */
        public static final String CLASS_OR_INTERFACE_DEFINITION_WITHOUT_IDENTIFIER_STRING =
                "(\\s+|^|(\\s+|^)public\\s+|(\\s+|^)protected\\s+|(\\s+|^)"
                + "private\\s+)(static\\s+|abstract\\s+|final\\s+|)(class|interface|enum)\\s+";
        /**
         * Regular expression to match class definition.
         */
        public static final String CLASS_OR_INTERFACE_DEFINITION_STRING =
            CLASS_OR_INTERFACE_DEFINITION_WITHOUT_IDENTIFIER_STRING
                + IDENTIFIER_STRING;
        /**
         * Pattern to match class definition. Example:
         * <code>
         * public class Sample01
         * </code>
         */
        public static final Pattern CLASS_OR_INTERFACE_DEFINITION =
                Pattern.compile(CLASS_OR_INTERFACE_DEFINITION_STRING);
    }
}
