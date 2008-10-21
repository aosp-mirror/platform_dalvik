/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.util.regex;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestStatus;

import junit.framework.TestCase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@TestTargetClass(Matcher.class)
public class MatcherTest extends TestCase {
    String[] testPatterns = {
            "(a|b)*abb",
            "(1*2*3*4*)*567",
            "(a|b|c|d)*aab",
            "(1|2|3|4|5|6|7|8|9|0)(1|2|3|4|5|6|7|8|9|0)*",
            "(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ)*",
            "(a|b)*(a|b)*A(a|b)*lice.*",
            "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)(a|b|c|d|e|f|g|h|"
                    + "i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)*(1|2|3|4|5|6|7|8|9|0)*|while|for|struct|if|do" };

    String[] groupPatterns = { "(a|b)*aabb", "((a)|b)*aabb", "((a|b)*)a(abb)",
            "(((a)|(b))*)aabb", "(((a)|(b))*)aa(b)b", "(((a)|(b))*)a(a(b)b)" };

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies appendReplacement(StringBuffer sb, " +
                    "String replacement) method.",
            targets = { @TestTarget(methodName = "appendReplacement", 
                                    methodArgs = { java.lang.StringBuffer.class,
                                                   java.lang.String.class })                         
            }
    )  
    public void testAppendReplacement() {
        Pattern pat = Pattern.compile("XX");
        Matcher m = pat.matcher("Today is XX-XX-XX ...");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; m.find(); i++) {
            m.appendReplacement(sb, new Integer(i * 10 + i).toString());
        }
        m.appendTail(sb);
        assertEquals("Today is 0-11-22 ...", sb.toString());
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies appendReplacement(StringBuffer sb, " +
                    "String replacement) method.",
            targets = { @TestTarget(methodName = "appendReplacement", 
                                    methodArgs = { java.lang.StringBuffer.class,
                                                   java.lang.String.class })                         
            }
    )  
    public void testAppendReplacementRef() {
        Pattern p = Pattern.compile("xx (rur|\\$)");
        Matcher m = p.matcher("xx $ equals to xx rur.");
        StringBuffer sb = new StringBuffer();
        for (int i = 1; m.find(); i *= 30) {
            String rep = new Integer(i).toString() + " $1";
            m.appendReplacement(sb, rep);
        }
        m.appendTail(sb);
        assertEquals("1 $ equals to 30 rur.", sb.toString());
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies replaceAll(String replacement) method.",
            targets = { @TestTarget(methodName = "replaceAll", 
                                    methodArgs = {java.lang.String.class}) 
            }
    )
    public void testReplaceAll() {
        String input = "aabfooaabfooabfoob";
        String pattern = "a*b";
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(input);

        assertEquals("-foo-foo-foo-", mat.replaceAll("-"));
    }

    /**
     * @test java.util.regex.Matcher#reset(String)
     * test reset(String) method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the reset(CharSequence input) method.",
            targets = { @TestTarget(methodName = "reset", 
                                    methodArgs = {java.lang.CharSequence.class})                         
            }
    )            
    public void test_resetLjava_lang_String() {
        String testPattern = "(abb)";
        String testString1 = "babbabbcccabbabbabbabbabb";
        String testString2 = "cddcddcddcddcddbbbb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString1);

        while (mat.find());
        assertEquals("Reset should return itself 1", mat, mat.reset(testString2));
        assertFalse("After reset matcher should not find pattern in given input", mat.find());
        assertEquals("Reset should return itself 2", mat, mat.reset(testString1));
        assertTrue("After reset matcher should find pattern in given input", mat.find());
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies appendReplacement(StringBuffer sb, " +
                    "String replacement) method with string of slashes as a parameter.",
            targets = { @TestTarget(methodName = "appendReplacement", 
                                    methodArgs = { java.lang.StringBuffer.class,
                                                   java.lang.String.class })                         
            }
    )  
    public void testAppendSlashes() {
        Pattern p = Pattern.compile("\\\\");
        Matcher m = p.matcher("one\\cat\\two\\cats\\in\\the\\yard");
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "\\\\");
        }
        m.appendTail(sb);
        assertEquals("one\\cat\\two\\cats\\in\\the\\yard", sb.toString());

    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies replaceFirst(String replacement) method.",
            targets = { @TestTarget(methodName = "replaceFirst", 
                                    methodArgs = {java.lang.String.class}) 
            }
    )
    public void testReplaceFirst() {
        String input = "zzzdogzzzdogzzz";
        String pattern = "dog";
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(input);

        assertEquals("zzzcatzzzdogzzz", mat.replaceFirst("cat"));
    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies pattern() method.",
            targets = { @TestTarget(methodName = "pattern", 
                                    methodArgs = {})
            }
    )
    public void testPattern() {
        for (String element : testPatterns) {
            Pattern test = Pattern.compile(element);
            assertEquals(test, test.matcher("aaa").pattern());
        }

        for (String element : testPatterns) {
            assertEquals(element, Pattern.compile(element).matcher("aaa")
                    .pattern().toString());
        }
    }

    /**
     * @test java.util.regex.Matcher#reset()
     * test reset() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the reset() method.",
            targets = { @TestTarget(methodName = "reset", 
                                    methodArgs = {})                         
            }
    )        
    public void test_reset() {
        String testPattern = "(abb)";
        String testString = "babbabbcccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        while (mat.find());
        assertEquals("Reset should return itself", mat, mat.reset());
        assertTrue("After reset matcher should find pattern in given input", mat.find());
    }

    /*
     * Class under test for String group(int)
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies group(int group) method.",
            targets = { @TestTarget(methodName = "group", 
                                    methodArgs = { int.class })                                            
            }
    ) 
    public void testGroupint() {
        String positiveTestString = "ababababbaaabb";
        String negativeTestString = "gjhfgdsjfhgcbv";

        // test IndexOutOfBoundsException
        // //
        for (int i = 0; i < groupPatterns.length; i++) {
            Pattern test = Pattern.compile(groupPatterns[i]);
            Matcher mat = test.matcher(positiveTestString);
            mat.matches();
            try {
                // groupPattern <index + 1> equals to number of groups
                // of the specified pattern
                // //
                mat.group(i + 2);
                fail("IndexOutBoundsException expected");
                mat.group(i + 100);
                fail("IndexOutBoundsException expected");
                mat.group(-1);
                fail("IndexOutBoundsException expected");
                mat.group(-100);
                fail("IndexOutBoundsException expected");
            } catch (IndexOutOfBoundsException iobe) {
            }
        }

        String[][] groupResults = { { "a" }, { "a", "a" },
                { "ababababba", "a", "abb" }, { "ababababba", "a", "a", "b" },
                { "ababababba", "a", "a", "b", "b" },
                { "ababababba", "a", "a", "b", "abb", "b" }, };

        for (int i = 0; i < groupPatterns.length; i++) {
            Pattern test = Pattern.compile(groupPatterns[i]);
            Matcher mat = test.matcher(positiveTestString);
            mat.matches();
            for (int j = 0; j < groupResults[i].length; j++) {
                assertEquals("i: " + i + " j: " + j, groupResults[i][j], mat
                        .group(j + 1));
            }

        }

    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies group() and group(int group) methods.",
            targets = { @TestTarget(methodName = "group", 
                                    methodArgs = {}),
                        @TestTarget(methodName = "group", 
                                    methodArgs = {int.class})                                    
            }
    ) 
    public void testGroup() {
        String positiveTestString = "ababababbaaabb";
        String negativeTestString = "gjhfgdsjfhgcbv";
        for (String element : groupPatterns) {
            Pattern test = Pattern.compile(element);
            Matcher mat = test.matcher(positiveTestString);
            mat.matches();
            // test result
            assertEquals(positiveTestString, mat.group());

            // test equal to group(0) result
            assertEquals(mat.group(0), mat.group());
        }

        for (String element : groupPatterns) {
            Pattern test = Pattern.compile(element);
            Matcher mat = test.matcher(negativeTestString);
            mat.matches();
            try {
                mat.group();
                fail("IllegalStateException expected for <false> matches result");
            } catch (IllegalStateException ise) {
            }
        }
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies group(int group) method.",
            targets = { @TestTarget(methodName = "group", 
                                    methodArgs = { int.class })                                            
            }
    ) 
    public void testGroupPossessive() {
        Pattern pat = Pattern.compile("((a)|(b))++c");
        Matcher mat = pat.matcher("aac");

        mat.matches();
        assertEquals("a", mat.group(1));
    }

    /**
     * @test java.util.regex.Matcher#hasAnchoringBounds()
     * test hasAnchoringBounds() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies that hasAnchoringBounds method returns" +
                    " correct value.",
            targets = { @TestTarget(methodName = "hasAnchoringBounds", 
                                    methodArgs = {})                         
            }
    )          
    public void test_hasAnchoringBounds() {
        String testPattern = "abb";
        String testString = "abb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        assertTrue("Matcher uses anchoring bound by default",
                mat.hasAnchoringBounds());

        Matcher mu = mat.useAnchoringBounds(true);
        assertTrue("Incorrect value of anchoring bounds",
                mu.hasAnchoringBounds());

        mu = mat.useAnchoringBounds(false);
        assertFalse("Incorrect value of anchoring bounds",
                mu.hasAnchoringBounds());
    }

    /**
     * @test java.util.regex.Matcher#hasTransparentBounds()
     * test hasTransparentBounds() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies that hasTransparentBound method returns" +
                    " correct value.",
            targets = { @TestTarget(methodName = "hasTransparentBound", 
                                    methodArgs = {})                         
            }
    )  
    public void test_hasTransparentBounds() {
        String testPattern = "abb";
        String testString = "ab\nb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        assertFalse("Matcher uses opaque bounds by default",
                mat.hasTransparentBounds());

        Matcher mu = mat.useTransparentBounds(true);
        assertTrue("Incorrect value of anchoring bounds",
                mu.hasTransparentBounds());

        mu = mat.useTransparentBounds(false);
        assertFalse("Incorrect value of anchoring bounds",
                mu.hasTransparentBounds());
    }

    /**
     * @test java.util.regex.Matcher#start(int)
     * test start(int) method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the start(int group) method.",
            targets = { @TestTarget(methodName = "start", 
                                    methodArgs = {int.class})                         
            }
    )     
    public void test_startI() {
        String testPattern = "(((abb)a)(bb))";
        String testString = "cccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);
        int start = 3;
        int end = 6;
        int i, j;

        for (j = 0; j < 3; j++) {
            while (mat.find(start + j - 2)) {
                for (i = 0; i < 4; i++) {
                    assertEquals("Start is wrong for group " + i + " :" + mat.group(i), start, mat.start(i));
                }
                assertEquals("Start is wrong for group " + i + " :" + mat.group(i), start + 4, mat.start(i));

                start = end;
                end += 3;
            }
        }
    }

    /**
     * @test java.util.regex.Matcher#end(int)
     * test end(int) method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the basic functionality of end(int group) method.",
            targets = { @TestTarget(methodName = "end", 
                                    methodArgs = {int.class})                         
            }
    )        
    public void test_endI() {
        String testPattern = "(((abb)a)(bb))";
        String testString = "cccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);
        int start = 3;
        int end = 6;
        int i, j;

        for (j = 0; j < 3; j++) {
            while (mat.find(start + j - 2)) {
                for (i = 0; i < 4; i++) {
                    assertEquals("End is wrong for group " + i + " :" + mat.group(i), start + mat.group(i).length(), mat.end(i));
                }
                assertEquals("End is wrong for group " + i + " :" + mat.group(i), start + 4 + mat.group(i).length(), mat.end(i));

                start = end;
                end += 3;
            }
        }
    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() method in miscellaneous cases.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {})                                            
            }
    ) 
    public void testMatchesMisc() {
        String[][] posSeq = {
                { "abb", "ababb", "abababbababb", "abababbababbabababbbbbabb" },
                { "213567", "12324567", "1234567", "213213567",
                        "21312312312567", "444444567" },
                { "abcdaab", "aab", "abaab", "cdaab", "acbdadcbaab" },
                { "213234567", "3458", "0987654", "7689546432", "0398576",
                        "98432", "5" },
                {
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" },
                { "ababbaAabababblice", "ababbaAliceababab", "ababbAabliceaaa",
                        "abbbAbbbliceaaa", "Alice" },
                { "a123", "bnxnvgds156", "for", "while", "if", "struct" }

        };

        for (int i = 0; i < testPatterns.length; i++) {
            Pattern pat = Pattern.compile(testPatterns[i]);
            for (int j = 0; j < posSeq[i].length; j++) {
                Matcher mat = pat.matcher(posSeq[i][j]);
                assertTrue("Incorrect match: " + testPatterns[i] + " vs "
                        + posSeq[i][j], mat.matches());
            }
        }
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "Stress test for matches() method.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {})                                            
            }
    )
    public void testMatchesQuantifiers() {
        String[] testPatternsSingles = { "a{5}", "a{2,4}", "a{3,}" };
        String[] testPatternsMultiple = { "((a)|(b)){1,2}abb",
                "((a)|(b)){2,4}", "((a)|(b)){3,}" };

        String[][] stringSingles = { { "aaaaa", "aaa" },
                { "aa", "a", "aaa", "aaaaaa", "aaaa", "aaaaa" },
                { "aaa", "a", "aaaa", "aa" }, };

        String[][] stringMultiples = { { "ababb", "aba" },
                { "ab", "b", "bab", "ababa", "abba", "abababbb" },
                { "aba", "b", "abaa", "ba" }, };

        for (int i = 0; i < testPatternsSingles.length; i++) {
            Pattern pat = Pattern.compile(testPatternsSingles[i]);
            for (int j = 0; j < stringSingles.length / 2; j++) {
                assertTrue("Match expected, but failed: " + pat.pattern()
                        + " : " + stringSingles[i][j], pat.matcher(
                        stringSingles[i][j * 2]).matches());
                assertFalse("Match failure expected, but match succeed: "
                        + pat.pattern() + " : " + stringSingles[i][j * 2 + 1],
                        pat.matcher(stringSingles[i][j * 2 + 1]).matches());
            }
        }

        for (int i = 0; i < testPatternsMultiple.length; i++) {
            Pattern pat = Pattern.compile(testPatternsMultiple[i]);
            for (int j = 0; j < stringMultiples.length / 2; j++) {
                assertTrue("Match expected, but failed: " + pat.pattern()
                        + " : " + stringMultiples[i][j], pat.matcher(
                        stringMultiples[i][j * 2]).matches());
                assertFalse(
                        "Match failure expected, but match succeed: "
                                + pat.pattern() + " : "
                                + stringMultiples[i][j * 2 + 1], pat.matcher(
                                stringMultiples[i][j * 2 + 1]).matches());
            }
        }
    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() and group(int group) methods" +
                    " for specific pattern.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {}),
                        @TestTarget(methodName = "group", 
                                    methodArgs = {int.class}) 
            }
    )
    public void testQuantVsGroup() {
        String patternString = "(d{1,3})((a|c)*)(d{1,3})((a|c)*)(d{1,3})";
        String testString = "dacaacaacaaddaaacaacaaddd";

        Pattern pat = Pattern.compile(patternString);
        Matcher mat = pat.matcher(testString);

        mat.matches();
        assertEquals("dacaacaacaaddaaacaacaaddd", mat.group());
        assertEquals("d", mat.group(1));
        assertEquals("acaacaacaa", mat.group(2));
        assertEquals("dd", mat.group(4));
        assertEquals("aaacaacaa", mat.group(5));
        assertEquals("ddd", mat.group(7));
    }

    /**
     * @test java.util.regex.Matcher#lookingAt()
     * test lookingAt() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies that lookingAt() method returns" +
                    " correct value.",
            targets = { @TestTarget(methodName = "lookingAt", 
                                    methodArgs = {})                         
            }
    )          
    public void test_lookingAt() {
        String testPattern = "(((abb)a)(bb))";
        String testString1 = "babbabbcccabbabbabbabbabb";
        String testString2 = "abbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat1 = pat.matcher(testString1);
        Matcher mat2 = pat.matcher(testString2);

        assertFalse("Should not find given pattern in 1 string", mat1.lookingAt());
        mat1.region(1, 10);
        assertTrue("Should find given pattern in region of string", mat1.lookingAt());
        assertTrue("Should find given pattern in 2 string", mat2.lookingAt());
    }

    /*
     * Class under test for boolean find()
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies that find() method returns" +
                    " correct value.",
            targets = { @TestTarget(methodName = "find", 
                                    methodArgs = {})                         
            }
    )      
    public void testFind() {
        String testPattern = "(abb)";
        String testString = "cccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);
        int start = 3;
        int end = 6;
        while (mat.find()) {
            assertEquals(start, mat.start(1));
            assertEquals(end, mat.end(1));

            start = end;
            end += 3;
        }

        testPattern = "(\\d{1,3})";
        testString = "aaaa123456789045";

        Pattern pat2 = Pattern.compile(testPattern);
        Matcher mat2 = pat2.matcher(testString);
        start = 4;
        int length = 3;
        while (mat2.find()) {
            assertEquals(testString.substring(start, start + length), mat2
                    .group(1));
            start += length;
        }
    }

    /**
     * @test java.util.regex.Matcher#find(int)
     * test find (int) method. Created via modifying method for find
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the basic functionality of find(int start) method.",
            targets = { @TestTarget(methodName = "find", 
                                    methodArgs = {int.class})                         
            }
    )          
    public void test_findI() {
        String testPattern = "(abb)";
        String testString = "cccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);
        int start = 3;
        int end = 6;
        int j;

        for (j = 0; j < 3; j++) {
            while (mat.find(start + j - 2)) {
                assertEquals(start, mat.start(1));
                assertEquals(end, mat.end(1));

                start = end;
                end += 3;
            }
            start = 6;
            end = 9;
        }

        testPattern = "(\\d{1,3})";
        testString = "aaaa123456789045";

        Pattern pat2 = Pattern.compile(testPattern);
        Matcher mat2 = pat2.matcher(testString);
        start = 4;
        int length = 3;
        for (j = 0; j < length; j++) {
            for (int i = 4 + j; i < testString.length() - length; i += length) {
                mat2.find(i);
                assertEquals(testString.substring(i, i + length), mat2.group(1));
            }
        }
    }
    @TestInfo(
            status = TestStatus.TODO,
            notes = "The test verifies matches() method for predefined " +
                    "characters in sequence.",
            targets = { @TestTarget(methodName = "replaceFirst", 
                                    methodArgs = {java.lang.String.class}) 
            }
    )
    public void testSEOLsymbols() {
        Pattern pat = Pattern.compile("^a\\(bb\\[$");
        Matcher mat = pat.matcher("a(bb[");

        assertTrue(mat.matches());
    }

    /**
     * @test java.util.regex.Matcher#start()
     * test start() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the start() method.",
            targets = { @TestTarget(methodName = "start", 
                                    methodArgs = {})                         
            }
    )      
    public void test_start() {
        String testPattern = "(abb)";
        String testString = "cccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);
        int start = 3;
        int end = 6;
        int j;

        for (j = 0; j < 3; j++) {
            while (mat.find()) {
                assertEquals("Start is wrong", start, mat.start());
               
                start = end;
                end += 3;
            }
        }
    }

    /**
     * @test java.util.regex.Matcher#end()
     * test end() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the basic functionality of end() method.",
            targets = { @TestTarget(methodName = "end", 
                                    methodArgs = {})                         
            }
    )        
    public void test_end() {
        String testPattern = "(abb)";
        String testString = "cccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);
        int start = 3;
        int end = 6;
        int j;

        for (j = 0; j < 3; j++) {
            while (mat.find()) {
                assertEquals("Start is wrong", end, mat.end());
               
                start = end;
                end += 3;
            }
        }
    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies groupCount() method.",
            targets = { @TestTarget(methodName = "groupCount", 
                                    methodArgs = {})                                    
            }
    ) 
    public void testGroupCount() {
        for (int i = 0; i < groupPatterns.length; i++) {
            Pattern test = Pattern.compile(groupPatterns[i]);
            Matcher mat = test.matcher("ababababbaaabb");
            mat.matches();
            assertEquals(i + 1, mat.groupCount());
        }
    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() and group(int group) methods" +
                    " for specific pattern.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {}),
                        @TestTarget(methodName = "group", 
                                    methodArgs = {int.class}) 
            }
    )
    public void testRelactantQuantifiers() {
        Pattern pat = Pattern.compile("(ab*)*b");
        Matcher mat = pat.matcher("abbbb");

        if (mat.matches()) {
            assertEquals("abbb", mat.group(1));
        } else {
            fail("Match expected: (ab*)*b vs abbbb");
        }
    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies find() method.",
            targets = { @TestTarget(methodName = "find", 
                                    methodArgs = {})                         
            }
    )  
    public void testEnhancedFind() {
        String input = "foob";
        String pattern = "a*b";
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(input);

        mat.find();
        assertEquals("b", mat.group());
    }

    @TestInfo(
            status = TestStatus.TODO,
            notes = "The test verifies matches method for input sequence " +
                    "specified by URL.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {})                         
            }
    )
    public void _testMatchesURI() {
        final Pattern pat = Pattern
                .compile("^(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
        Runnable r1 = new Runnable() {
            public void run() {
                Matcher mat = pat
                        .matcher("file:/c:/workspace/api/build.win32/classes/META-INF/"
                                + "services/javax.xml.parsers.DocumentBuilderFactory");
                int k1 = 1;
                while (mat.matches()) {
                }// System.err.println("1: " + mat.group());
                System.out.println("1: fail");
            }
        };

        Runnable r2 = new Runnable() {
            public void run() {
                Matcher mat = pat.matcher("file:/c:/workspace/"
                        + "services/javax.xml.parsers.DocumentBuilderFactory");
                int k1 = 1;
                while (mat.matches()) {
                }// System.err.println("2: " + mat.group());
                System.out.println("2: fail");
            }
        };

        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
        }

    }
    @TestInfo(
            status = TestStatus.TODO,
            notes = "TODO there is no any testing.",
            targets = { @TestTarget(methodName = "", 
                                    methodArgs = {})                         
            }
    )
    public void _testUnifiedQuantifiers() {
        // Pattern pat1 = Pattern.compile("\\s+a");
        Pattern pat2 = Pattern.compile(" *a");

        Matcher mat = pat2.matcher("      a");

        System.out.println("unified: " + mat.find());
    }

    @TestInfo(
            status = TestStatus.TODO,
            notes = "TODO there is no any testing.",
            targets = { @TestTarget(methodName = "", 
                                    methodArgs = {})                         
            }
    )
    public void _testCompositeGroupQuantifiers() {
        Pattern pat = Pattern.compile("(a|b){0,3}abb");
        Matcher mat = pat.matcher("ababababababababababaab");

        System.out.println("composite: " + mat.find());
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() method for " +
                    "composite pattern groups.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {})                         
            }
    )
    public void testPosCompositeGroup() {
        String[] posExamples = { "aabbcc", "aacc", "bbaabbcc" };
        String[] negExamples = { "aabb", "bb", "bbaabb" };
        Pattern posPat = Pattern.compile("(aa|bb){1,3}+cc");
        Pattern negPat = Pattern.compile("(aa|bb){1,3}+bb");

        Matcher mat;
        for (String element : posExamples) {
            mat = posPat.matcher(element);
            assertTrue(mat.matches());
        }

        for (String element : negExamples) {
            mat = negPat.matcher(element);
            assertFalse(mat.matches());
        }

        assertTrue(Pattern.matches("(aa|bb){1,3}+bb", "aabbaabb"));

    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies  matches() method for specific patterns.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {})
            }
    )   
    public void testPosAltGroup() {
        String[] posExamples = { "aacc", "bbcc", "cc" };
        String[] negExamples = { "bb", "aa" };
        Pattern posPat = Pattern.compile("(aa|bb)?+cc");
        Pattern negPat = Pattern.compile("(aa|bb)?+bb");

        Matcher mat;
        for (String element : posExamples) {
            mat = posPat.matcher(element);
            assertTrue(posPat.toString() + " vs: " + element, mat.matches());
        }

        for (String element : negExamples) {
            mat = negPat.matcher(element);
            assertFalse(mat.matches());
        }

        assertTrue(Pattern.matches("(aa|bb)?+bb", "aabb"));
    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() and group(int group) methods" +
                    " for specific pattern.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {}),
                        @TestTarget(methodName = "group", 
                                    methodArgs = {int.class}) 
            }
    )
    public void testRelCompGroup() {

        Matcher mat;
        Pattern pat;
        String res = "";
        for (int i = 0; i < 4; i++) {
            pat = Pattern.compile("((aa|bb){" + i + ",3}?).*cc");
            mat = pat.matcher("aaaaaacc");
            assertTrue(pat.toString() + " vs: " + "aaaaaacc", mat.matches());
            assertEquals(res, mat.group(1));
            res += "aa";
        }
    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() and group(int group) methods" +
                    " for specific pattern.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {}),
                        @TestTarget(methodName = "group", 
                                    methodArgs = {int.class}) 
            }
    )
    public void testRelAltGroup() {

        Matcher mat;
        Pattern pat;

        pat = Pattern.compile("((aa|bb)??).*cc");
        mat = pat.matcher("aacc");
        assertTrue(pat.toString() + " vs: " + "aacc", mat.matches());
        assertEquals("", mat.group(1));

        pat = Pattern.compile("((aa|bb)??)cc");
        mat = pat.matcher("aacc");
        assertTrue(pat.toString() + " vs: " + "aacc", mat.matches());
        assertEquals("aa", mat.group(1));
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() method for case insensitive " +
                    "pattern.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {})                                            
            }
    ) 
    public void testIgnoreCase() {
        Pattern pat = Pattern.compile("(aa|bb)*", Pattern.CASE_INSENSITIVE);
        Matcher mat = pat.matcher("aAbb");

        assertTrue(mat.matches());

        pat = Pattern.compile("(a|b|c|d|e)*", Pattern.CASE_INSENSITIVE);
        mat = pat.matcher("aAebbAEaEdebbedEccEdebbedEaedaebEbdCCdbBDcdcdADa");
        assertTrue(mat.matches());

        pat = Pattern.compile("[a-e]*", Pattern.CASE_INSENSITIVE);
        mat = pat.matcher("aAebbAEaEdebbedEccEdebbedEaedaebEbdCCdbBDcdcdADa");
        assertTrue(mat.matches());

    }
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies quoteReplacement(String s) method.",
            targets = { @TestTarget(methodName = "quoteReplacement", 
                                    methodArgs = {java.lang.String.class}) 
            }
    )
    public void testQuoteReplacement() {
        assertEquals("\\\\aaCC\\$1", Matcher.quoteReplacement("\\aaCC$1"));
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() and group(int group) methods.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {}),
                        @TestTarget(methodName = "group", 
                                    methodArgs = {int.class})
            }
    )
    public void testOverFlow() {
        Pattern tp = Pattern.compile("(a*)*");
        Matcher tm = tp.matcher("aaa");
        assertTrue(tm.matches());
        assertEquals("", tm.group(1));

        assertTrue(Pattern.matches("(1+)\\1+", "11"));
        assertTrue(Pattern.matches("(1+)(2*)\\2+", "11"));

        Pattern pat = Pattern.compile("(1+)\\1*");
        Matcher mat = pat.matcher("11");

        assertTrue(mat.matches());
        assertEquals("11", mat.group(1));

        pat = Pattern.compile("((1+)|(2+))(\\2+)");
        mat = pat.matcher("11");

        assertTrue(mat.matches());
        assertEquals("1", mat.group(2));
        assertEquals("1", mat.group(1));
        assertEquals("1", mat.group(4));
        assertNull(mat.group(3));

    }
    @TestInfo(
            status = TestStatus.TODO,
            notes = "The test doesn't verify Matcher and should be moved to PatterTest",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = { java.lang.String.class, 
                                                   java.lang.CharSequence.class }) 
            }
    )
    public void testUnicode() {

        assertTrue(Pattern.matches("\\x61a", "aa"));
//        assertTrue(Pattern.matches("\\u0061a", "aa"));
        assertTrue(Pattern.matches("\\0141a", "aa"));
        assertTrue(Pattern.matches("\\0777", "?7"));

    }
    @TestInfo(
            status = TestStatus.TODO,
            notes = "The test doesn't verify Matcher and should be moved to PatterTest",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = { java.lang.String.class, 
                                                   java.lang.CharSequence.class }) 
            }
    )
    public void testUnicodeCategory() {
        assertTrue(Pattern.matches("\\p{Ll}", "k")); // Unicode lower case
        assertTrue(Pattern.matches("\\P{Ll}", "K")); // Unicode non-lower
        // case
        assertTrue(Pattern.matches("\\p{Lu}", "K")); // Unicode upper case
        assertTrue(Pattern.matches("\\P{Lu}", "k")); // Unicode non-upper
        // case
        // combinations
        assertTrue(Pattern.matches("[\\p{L}&&[^\\p{Lu}]]", "k"));
        assertTrue(Pattern.matches("[\\p{L}&&[^\\p{Ll}]]", "K"));
        assertFalse(Pattern.matches("[\\p{L}&&[^\\p{Lu}]]", "K"));
        assertFalse(Pattern.matches("[\\p{L}&&[^\\p{Ll}]]", "k"));

        // category/character combinations
        assertFalse(Pattern.matches("[\\p{L}&&[^a-z]]", "k"));
        assertTrue(Pattern.matches("[\\p{L}&&[^a-z]]", "K"));

        assertTrue(Pattern.matches("[\\p{Lu}a-z]", "k"));
        assertTrue(Pattern.matches("[a-z\\p{Lu}]", "k"));

        assertFalse(Pattern.matches("[\\p{Lu}a-d]", "k"));
        assertTrue(Pattern.matches("[a-d\\p{Lu}]", "K"));

        //        assertTrue(Pattern.matches("[\\p{L}&&[^\\p{Lu}&&[^K]]]", "K"));
        assertFalse(Pattern.matches("[\\p{L}&&[^\\p{Lu}&&[^G]]]", "K"));

    }
    @TestInfo(
            status = TestStatus.TODO,
            notes = "The test doesn't verify Matcher and should be moved to PatterTest",
            targets = { @TestTarget(methodName = "split", 
                                    methodArgs = { java.lang.CharSequence.class, 
                                                   int.class }) 
            }
    )
    public void testSplitEmpty() {

        Pattern pat = Pattern.compile("");
        String[] s = pat.split("", -1);

        assertEquals(1, s.length);
        assertEquals("", s[0]);
    }

    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies that find() method works correctly " +
                    "with $ pattern.",
            targets = { @TestTarget(methodName = "find", 
                                    methodArgs = {})                         
            }
    )  
    public void testFindDollar() {
        Matcher mat = Pattern.compile("a$").matcher("a\n");
        assertTrue(mat.find());
        assertEquals("a", mat.group());
    }

    /*
     * Verify if the Matcher can match the input when region is changed
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies matches() method for the specified region.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {})                                            
            }
    )    
    public void testMatchesRegionChanged() {
        // Regression for HARMONY-610
        String input = " word ";
        Pattern pattern = Pattern.compile("\\w+");
        Matcher matcher = pattern.matcher(input);
        matcher.region(1, 5);
        assertTrue(matcher.matches());
    }

    // BEGIN android-note
    // Test took ages, now going in steps of 16 code points to speed things up.
    // END android-note
    @TestInfo(
            status = TestStatus.TODO,
            notes = "The stress test for matches(String regex) method from String class.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {java.lang.String.class})                         
            }
    )  
    public void testAllCodePoints() {
        // Regression for HARMONY-3145
        int[] codePoint = new int[1];
        Pattern p = Pattern.compile("(\\p{all})+");
        boolean res = true;
        int cnt = 0;
        String s;
        for (int i = 0; i < 0x110000; i = i + 0x10) {
            codePoint[0] = i;
            s = new String(codePoint, 0, 1);
            if (!s.matches(p.toString())) {
                cnt++;
                res = false;
            }
        }
        assertTrue(res);
        assertEquals(0, cnt);

        p = Pattern.compile("(\\P{all})+");
        res = true;
        cnt = 0;

        for (int i = 0; i < 0x110000; i = i + 0x10) {
            codePoint[0] = i;
            s = new String(codePoint, 0, 1);
            if (!s.matches(p.toString())) {
                cnt++;
                res = false;
            }
        }

        assertFalse(res);
        assertEquals(0x110000 / 0x10, cnt);
    }

    /*
     * Verify if the Matcher behaves correct when region is changed
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies find() method for specified region in " +
                    "positive case.",
            targets = { @TestTarget(methodName = "find", 
                                    methodArgs = {})                         
            }
    )      
    public void testFindRegionChanged() {
        // Regression for HARMONY-625
        Pattern pattern = Pattern.compile("(?s).*");
        Matcher matcher = pattern.matcher("abcde");
        matcher.find();
        assertEquals("abcde", matcher.group());

        matcher = pattern.matcher("abcde");
        matcher.region(0, 2);
        matcher.find();
        assertEquals("ab", matcher.group());

    }

    /*
     * Verify if the Matcher behaves correct with pattern "c" when region is
     * changed
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies find() method for specified region in " +
                    "negative case.",
            targets = { @TestTarget(methodName = "find", 
                                    methodArgs = {})                         
            }
    )      
    public void testFindRegionChanged2() {
        // Regression for HARMONY-713
        Pattern pattern = Pattern.compile("c");

        String inputStr = "aabb.c";
        Matcher matcher = pattern.matcher(inputStr);
        matcher.region(0, 3);

        assertFalse(matcher.find());
    }

    /**
     * @test java.util.regex.Matcher#quoteReplacement(String s)
     * test quoteReplacement(String) method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the quoteReplacement(String s) method.",
            targets = { @TestTarget(methodName = "lookingAt", 
                                    methodArgs = {java.lang.String.class})                         
            }
    )            
    public void test_quoteReplacementLjava_lang_String() {
        String testPattern = "(((abb)a)(bb))";
        String testString = "$dollar and slash\\";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        assertEquals("", "\\$dollar and slash\\\\", mat.quoteReplacement(testString));
   }

    /**
     * @test java.util.regex.Matcher#regionStart()
     * test regionStart() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the regionStart() method.",
            targets = { @TestTarget(methodName = "regionStart", 
                                    methodArgs = {})                         
            }
    )        
    public void test_regionStart() {
        String testPattern = "(abb)";
        String testString = "cccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        assertEquals("Region sould start from 0 position", 0, mat.regionStart());
        mat.region(1, 10);
        assertEquals("Region sould start from 1 position after setting new region", 1, mat.regionStart());
        mat.reset();
        assertEquals("Region sould start from 0 position after reset", 0, mat.regionStart());
    }

    /**
     * @test java.util.regex.Matcher#regionEnd()
     * test regionEnd() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the regionEnd() method.",
            targets = { @TestTarget(methodName = "regionEnd", 
                                    methodArgs = {})                         
            }
    )          
    public void test_regionEnd() {
        String testPattern = "(abb)";
        String testString = "cccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        assertEquals("Region end value should be equal to string length", testString.length(), mat.regionEnd());
        mat.region(1, 10);
        assertEquals("Region end value should be equal to 10 after setting new region", 10, mat.regionEnd());
        mat.reset();
        assertEquals("Region end value should be equal to string length after reset", testString.length(), mat.regionEnd());
    }

    /**
     * @test java.util.regex.Matcher#toMatchResult()
     * test toMatchResult() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the toMatchResult method.",
            targets = { @TestTarget(methodName = "toMatchResult", 
                                    methodArgs = {})                         
            }
    ) 
    public void test_toMatchResult() {
        String testPattern = "(((abb)a)(bb))";
        String testString = "babbabbcccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        mat.region(1, 7);
        assertTrue("matcher should find pattern in given region", mat.matches());
        assertEquals("matched section should start from 1 position", 1, mat.toMatchResult().start());
        assertEquals("matched section for 2 group should start from 1 position", 1, mat.toMatchResult().start(2));
        assertEquals("matched section for whole pattern should end on 7 position", 7, mat.toMatchResult().end());
        assertEquals("matched section for 3 group should end at 4 position", 4, mat.toMatchResult().end(3));
        assertEquals("group not matched", "abbabb", mat.toMatchResult().group());
        assertEquals("3 group not matched", "abb", mat.toMatchResult().group(3));
        assertEquals("Total number of groups does not matched with given pattern", 4, mat.toMatchResult().groupCount());
   }

    /**
     * @test java.util.regex.Matcher#usePattern(Pattern newPattern)
     * test usePattern(Pattern newPattern) method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the usePattern(Pattern newPattern) method.",
            targets = { @TestTarget(methodName = "usePattern", 
                                    methodArgs = {java.util.regex.Pattern.class})                         
            }
    )        
    public void test_usePatternLjava_util_regex_Pattern() {
        String testPattern1 = "(((abb)a)(bb))";
        String testPattern2 = "(abbabb)";
        String testPattern3 = "(babb)";
        String testString = "babbabbcccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern1);
        Matcher mat = pat.matcher(testString);

        mat.region(1, 7);
        assertTrue("matcher should find pattern in given region in case of groupe in pattern", mat.matches());
        assertEquals("", mat, mat.usePattern(Pattern.compile(testPattern2)));
        assertTrue("matcher should find pattern in given region", mat.matches());
        assertEquals("", mat, mat.usePattern(Pattern.compile(testPattern3)));
        assertFalse("matcher should not find pattern in given region", mat.matches());
   }
    
    /**
     * @test java.util.regex.Matcher#useAchoringBounds()
     * test useAchoringBounds() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies useAnchoringBounds method.",
            targets = { @TestTarget(methodName = " useAnchoringBounds", 
                                    methodArgs = {boolean.class})                         
            }
    )    
    public void test_anchoringBounds() {
        String testPattern = "^ro$";
        String testString = "android";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        mat.region(2, 5);
        mat.useAnchoringBounds(false);
        assertFalse("Shouldn't find pattern with non-anchoring bounds", mat.find(0));

        mat.region(2, 5);
        mat.useAnchoringBounds(true);
        assertFalse("Should find pattern with anchoring bounds", mat.find(0));
    }

    /**
     * @test java.util.regex.Matcher#useTransparentBounds()
     * test useTransparentBounds() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the useTransparentBounds(boolean b) method.",
            targets = { @TestTarget(methodName = "useTransparentBounds", 
                                    methodArgs = {boolean.class})                         
            }
    )     
    public void test_transparentBounds() {
        String testPattern = "and(?=roid)";
        String testString = "android";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        mat.region(0, 3);
        mat.useTransparentBounds(false);
        assertFalse("Shouldn't find pattern with opaque bounds", mat.matches());

        mat.useTransparentBounds(true);
        assertTrue("Should find pattern transparent bounds", mat.matches()); // ***
        
        testPattern = "and(?!roid)";
        testString = "android";
        pat = Pattern.compile(testPattern);
        mat = pat.matcher(testString);

        mat.region(0, 3);
        mat.useTransparentBounds(false);
        assertTrue("Should find pattern with opaque bounds", mat.matches());

        mat.useTransparentBounds(true);
        assertFalse("Shouldn't find pattern transparent bounds", mat.matches()); // ***
    }
    
    /**
     * @test java.util.regex.Matcher#hitEnd()
     * test hitEnd() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies that hitEnd() method returns" +
                    " correct value.",
            targets = { @TestTarget(methodName = "hitEnd", 
                                    methodArgs = {})                         
            }
    )      
    public void test_hitEnd() {
        String testPattern = "abb";
        String testString = "babbabbcccabbabbabbabbabb";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        while (mat.find()) {
            assertFalse("hitEnd should return false during parsing input", mat.hitEnd());
        }
        assertTrue("hitEnd should return true after finding last match", mat.hitEnd()); // ***
    }

    /**
     * @test java.util.regex.Matcher#requireEnd()
     * test requireEnd() method.
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "The test verifies the requireEnd() method.",
            targets = { @TestTarget(methodName = "requireEnd", 
                                    methodArgs = {})                         
            }
    )       
    public void test_requireEnd() {
        String testPattern = "bba";
        String testString = "abbbbba";
        Pattern pat = Pattern.compile(testPattern);
        Matcher mat = pat.matcher(testString);

        assertTrue(mat.find());
        assertFalse(mat.requireEnd());
        
        testPattern = "bba$";
        testString = "abbbbba";
        pat = Pattern.compile(testPattern);
        mat = pat.matcher(testString);

        assertTrue(mat.find());
        assertTrue(mat.requireEnd());
    }
    
    /*
     * Regression test for HARMONY-674
     */
    @TestInfo(
            status = TestStatus.LGTM,
            notes = "Special regression test for matches() method.",
            targets = { @TestTarget(methodName = "matches", 
                                    methodArgs = {})
            }
    )    
    public void testPatternMatcher() throws Exception {
        Pattern pattern = Pattern.compile("(?:\\d+)(?:pt)");
        assertTrue(pattern.matcher("14pt").matches());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(MatcherTest.class);
    }
    
}
