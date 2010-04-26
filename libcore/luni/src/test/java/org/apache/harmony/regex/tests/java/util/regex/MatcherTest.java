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

package org.apache.harmony.regex.tests.java.util.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

@TestTargetClass(
        value = Matcher.class,
        untestedMethods= {
            @TestTargetNew(
                level = TestLevel.NOT_FEASIBLE,
                notes = "finalize is hard to test since the implementation only calls a native function",
                method = "finalize",
                args = {}
            )
        }        

)
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

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "appendReplacement",
        args = {java.lang.StringBuffer.class, java.lang.String.class}
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
        
        pat = Pattern.compile("cat");
        m = pat.matcher("one-cat-two-cats-in-the-yard");
        sb = new StringBuffer();
        Throwable t = null;
        m.find();
        try {
            m.appendReplacement(null, "dog");
        } catch (NullPointerException e) {
            t = e;
        }
        assertNotNull(t);
        t = null;
        m.find();
        try {
            m.appendReplacement(sb, null);
        } catch (NullPointerException e) {
            t = e;
        }
        assertNotNull(t);
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "appendReplacement",
        args = {java.lang.StringBuffer.class, java.lang.String.class}
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

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "replaceAll",
        args = {java.lang.String.class}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the reset(CharSequence input) method.",
        method = "reset",
        args = {java.lang.CharSequence.class}
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

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "appendReplacement",
        args = {java.lang.StringBuffer.class, java.lang.String.class}
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

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "appendTail",
        args = {java.lang.StringBuffer.class}
    )  
    public void testAppendTail() {
        Pattern p = Pattern.compile("cat");
        Matcher m = p.matcher("one-cat-two-cats-in-the-yard");
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "dog");
        }
        m.appendTail(sb);
        assertEquals("one-dog-two-dogs-in-the-yard", sb.toString());

        p = Pattern.compile("cat|yard");
        m = p.matcher("one-cat-two-cats-in-the-yard");
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "dog");
        }
        assertEquals("one-dog-two-dogs-in-the-dog", sb.toString());
        m.appendTail(sb);
        assertEquals("one-dog-two-dogs-in-the-dog", sb.toString());
        
        p = Pattern.compile("cat");
        m = p.matcher("one-cat-two-cats-in-the-yard");
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "dog");
        }
        Throwable t = null;
        try {
            m.appendTail(null);
        } catch (NullPointerException e) {
            t = e;
        }
        assertNotNull(t);
    }
    
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies replaceFirst(String replacement) method. ",
        method = "replaceFirst",
        args = {java.lang.String.class}
    )
    public void testReplaceFirst() {
        String input = "zzzdogzzzdogzzz";
        String pattern = "dog";
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(input);

        assertEquals("zzzcatzzzdogzzz", mat.replaceFirst("cat"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies pattern() method.",
        method = "pattern",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the reset() method. ",
        method = "reset",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies group(int group) method.",
        method = "group",
        args = {int.class}
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
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies group() and group(int group) methods.",
            method = "group",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies group() and group(int group) methods.",
            method = "group",
            args = {int.class}
        )
    }) 
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

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies group(int group) method.",
        method = "group",
        args = {int.class}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that hasAnchoringBounds method returns correct value.",
        method = "hasAnchoringBounds",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that hasTransparentBound method returns correct value.",
        method = "hasTransparentBounds",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the start(int group) method.",
        method = "start",
        args = {int.class}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the basic functionality of end(int group) method.",
        method = "end",
        args = {int.class}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies matches() method in miscellaneous cases.",
        method = "matches",
        args = {}
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

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Stress test for matches() method.",
        method = "matches",
        args = {}
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
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods for specific pattern.",
            method = "matches",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods for specific pattern.",
            method = "group",
            args = {int.class}
        )
    })
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that lookingAt() method returns correct value.",
        method = "lookingAt",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that find() method returns correct value.",
        method = "find",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "find",
        args = {int.class}
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

        // Starting index out of region
        Pattern pat3 = Pattern.compile("new");
        Matcher mat3 = pat3.matcher("Brave new world");
        
        assertTrue(mat3.find(-1));
        assertTrue(mat3.find(6));
        assertFalse(mat3.find(7));

        mat3.region(7, 10);
        
        assertFalse(mat3.find(3));
        assertFalse(mat3.find(6));
        assertFalse(mat3.find(7));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies matches() method for predefined.",
        method = "replaceFirst",
        args = {java.lang.String.class}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the start() method.",
        method = "start",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the basic functionality of end() method. ",
        method = "end",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies groupCount() method.",
        method = "groupCount",
        args = {}
    ) 
    public void testGroupCount() {
        for (int i = 0; i < groupPatterns.length; i++) {
            Pattern test = Pattern.compile(groupPatterns[i]);
            Matcher mat = test.matcher("ababababbaaabb");
            mat.matches();
            assertEquals(i + 1, mat.groupCount());
        }
    }
    
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "region",
        args = {int.class, int.class}
    )          
    public void testRegion() {
        Pattern p = Pattern.compile("abba");
        Matcher m = p.matcher("Gabba gabba hey");

        m.region(0, 15);
        assertTrue(m.find());
        assertTrue(m.find());
        assertFalse(m.find());
        
        m.region(5, 15);
        assertTrue(m.find());
        assertFalse(m.find());

        m.region(10, 15);
        assertFalse(m.find());
        
        Throwable t = null;
        
        try {
            m.region(-1, 15);
        } catch (IllegalArgumentException e) {
            t = e;
        }
        assertNotNull(t);
        
        t = null;
        try {
            m.region(0, 16);
        } catch (IllegalArgumentException e) {
            t = e;
        }
        assertNotNull(t);
    }
    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods for specific pattern.",
            method = "matches",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods for specific pattern.",
            method = "group",
            args = {int.class}
        )
    })
    public void testRelactantQuantifiers() {
        Pattern pat = Pattern.compile("(ab*)*b");
        Matcher mat = pat.matcher("abbbb");

        if (mat.matches()) {
            assertEquals("abbb", mat.group(1));
        } else {
            fail("Match expected: (ab*)*b vs abbbb");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies find() method.",
        method = "find",
        args = {}
    )  
    public void testEnhancedFind() {
        String input = "foob";
        String pattern = "a*b";
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(input);

        mat.find();
        assertEquals("b", mat.group());
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies matches method for input sequence specified by URL.",
        method = "matches",
        args = {}
    )
    public void testMatchesURI() {
        Pattern pat = Pattern.
                compile("^(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
        Matcher mat = pat
                .matcher("file:/c:/workspace/api/build.win32/classes/META-INF/"
                        + "services/javax.xml.parsers.DocumentBuilderFactory");
        assertTrue(mat.matches());
    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies matches() method for composite pattern groups.",
        method = "matches",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies  matches() method for specific patterns.",
        method = "matches",
        args = {}
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
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods for specific pattern.",
            method = "matches",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods for specific pattern.",
            method = "group",
            args = {int.class}
        )
    })
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
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods for specific pattern.",
            method = "matches",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods for specific pattern.",
            method = "group",
            args = {int.class}
        )
    })
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

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies matches() method for case insensitive pattern.",
        method = "matches",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "quoteReplacement",
        args = {java.lang.String.class}
    )
    public void testQuoteReplacement() {
        assertEquals("\\$dollar and slash\\\\", Matcher.quoteReplacement("$dollar and slash\\"));
    }

    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods.",
            method = "matches",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies matches() and group(int group) methods.",
            method = "group",
            args = {int.class}
        )
    })
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "matches",
        args = {}
    )
    public void testUnicode() {

        assertTrue(Pattern.compile("\\x61a").matcher("aa").matches());
//        assertTrue(Pattern.matches("\\u0061a", "aa"));
        assertTrue(Pattern.compile("\\0141a").matcher("aa").matches());
        assertTrue(Pattern.compile("\\0777").matcher("?7").matches());

    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "matches",
        args = {}
    )
    public void testUnicodeCategory() {
        assertTrue(Pattern.compile("\\p{Ll}").matcher("k").matches()); // Unicode lower case
        assertTrue(Pattern.compile("\\P{Ll}").matcher("K").matches()); // Unicode non-lower
        // case
        assertTrue(Pattern.compile("\\p{Lu}").matcher("K").matches()); // Unicode upper case
        assertTrue(Pattern.compile("\\P{Lu}").matcher("k").matches()); // Unicode non-upper
        // case
        // combinations
        assertTrue(Pattern.compile("[\\p{L}&&[^\\p{Lu}]]").matcher("k").matches());
        assertTrue(Pattern.compile("[\\p{L}&&[^\\p{Ll}]]").matcher("K").matches());
        assertFalse(Pattern.compile("[\\p{L}&&[^\\p{Lu}]]").matcher("K").matches());
        assertFalse(Pattern.compile("[\\p{L}&&[^\\p{Ll}]]").matcher("k").matches());

        // category/character combinations
        assertFalse(Pattern.compile("[\\p{L}&&[^a-z]]").matcher("k").matches());
        assertTrue(Pattern.compile("[\\p{L}&&[^a-z]]").matcher("K").matches());

        assertTrue(Pattern.compile("[\\p{Lu}a-z]").matcher("k").matches());
        assertTrue(Pattern.compile("[a-z\\p{Lu}]").matcher("k").matches());

        assertFalse(Pattern.compile("[\\p{Lu}a-d]").matcher("k").matches());
        assertTrue(Pattern.compile("[a-d\\p{Lu}]").matcher("K").matches());

        //        assertTrue(Pattern.matches("[\\p{L}&&[^\\p{Lu}&&[^K]]]", "K"));
        assertFalse(Pattern.compile("[\\p{L}&&[^\\p{Lu}&&[^G]]]").matcher("K").matches());

    }

    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that find() method works correctly with $ pattern.",
        method = "find",
        args = {}
    )  
    public void testFindDollar() {
        Matcher mat = Pattern.compile("a$").matcher("a\n");
        assertTrue(mat.find());
        assertEquals("a", mat.group());
    }

    /*
     * Verify if the Matcher can match the input when region is changed
     */
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies matches() method for the specified region.",
        method = "matches",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "The stress test for matches(String regex) method from String class.",
        clazz = String.class,
        method = "matches",
        args = {java.lang.String.class}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies find() method for specified region in positive case.",
        method = "find",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies find() method for specified region in negative case.",
        method = "find",
        args = {}
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
     * @test java.util.regex.Matcher#regionStart()
     * test regionStart() method.
     */
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the regionStart() method.",
        method = "regionStart",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the regionEnd() method.",
        method = "regionEnd",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the toMatchResult method.",
        method = "toMatchResult",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the usePattern(Pattern newPattern) method.",
        method = "usePattern",
        args = {java.util.regex.Pattern.class}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "useAnchoringBounds",
        args = {boolean.class}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the useTransparentBounds(boolean b) method.",
        method = "useTransparentBounds",
        args = {boolean.class}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that hitEnd() method returns correct value. ",
        method = "hitEnd",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the requireEnd() method.",
        method = "requireEnd",
        args = {}
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Special regression test for matches() method.",
        method = "matches",
        args = {}
    )    
    public void testPatternMatcher() throws Exception {
        Pattern pattern = Pattern.compile("(?:\\d+)(?:pt)");
        assertTrue(pattern.matcher("14pt").matches());
    }

}
