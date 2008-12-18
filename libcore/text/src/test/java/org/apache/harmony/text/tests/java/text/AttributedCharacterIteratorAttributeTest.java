/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.text.tests.java.text;

import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;
import dalvik.annotation.TestTargetClass;

import java.io.InvalidObjectException;
import java.text.AttributedCharacterIterator;

@TestTargetClass(AttributedCharacterIterator.Attribute.class) 
public class AttributedCharacterIteratorAttributeTest extends
        junit.framework.TestCase {

    private class MockAttributedCharacterIteratorAttribute extends
            AttributedCharacterIterator.Attribute {

        private static final long serialVersionUID = 1L;

        public MockAttributedCharacterIteratorAttribute(String name) {
            super(name);
        }

        @Override
        public String getName() {
            return super.getName();
        }

        @Override
        public Object readResolve() throws InvalidObjectException {
            return super.readResolve();
        }
    }

    private class TestAttributedCharacterIteratorAttribute extends
            AttributedCharacterIterator.Attribute {
        private static final long serialVersionUID = -2917613373935785179L;

        public TestAttributedCharacterIteratorAttribute(String name) {
            super(name);
        }
    }

    /**
     * @tests java.text.AttributedCharacterIterator.Attribute#AttributedCharacterIterator.Attribute(java.lang.String)
     *        Test of method
     *        java.text.AttributedCharacterIterator.Attribute#AttributedCharacterIterator.Attribute(java.lang.String).
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "Attribute",
          methodArgs = {java.lang.String.class}
        )
    })
    public void test_Constructor() {
        try {
            new MockAttributedCharacterIteratorAttribute("test");
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.AttributedCharacterIterator.Attribute#equals(java.lang.Object)
     *        Test of method
     *        java.text.AttributedCharacterIterator.Attribute#equals(java.lang.Object).
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "equals",
          methodArgs = {java.lang.Object.class}
        )
    })
    public void test_equalsLjava_lang_Object() {
        try {
            MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                    "test1");
            MockAttributedCharacterIteratorAttribute mac2 = new MockAttributedCharacterIteratorAttribute(
                    "test2");

            assertFalse("Attributes are equal", mac2.equals(mac1));

            TestAttributedCharacterIteratorAttribute mac3 = new TestAttributedCharacterIteratorAttribute(
                    "test1");

            assertFalse("Attributes are equal", mac3.equals(mac1));

            AttributedCharacterIterator.Attribute mac4 = mac1;

            assertTrue("Attributes are non-equal", mac4.equals(mac1));
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.AttributedCharacterIterator.Attribute#getName() Test of
     *        method java.text.AttributedCharacterIterator.Attribute#getName().
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "getName",
          methodArgs = {}
        )
    })
    public void test_getName() {
        try {
            MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                    "test1");
            assertEquals("Incorrect attribute name", "test1", mac1.getName());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.AttributedCharacterIterator.Attribute#hashCode()
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "hashCode",
          methodArgs = {}
        )
    })
    public void test_hashCode() {
        try {
            MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                    "test1");
            TestAttributedCharacterIteratorAttribute mac2 = new TestAttributedCharacterIteratorAttribute(
                    "test1");

            assertTrue("The hash codes of same attributes are not equal", mac1
                    .hashCode() == mac2.hashCode());

            MockAttributedCharacterIteratorAttribute mac3 = new MockAttributedCharacterIteratorAttribute(
                    "test2");

            assertTrue("The hash codes of different attributes are equal", mac1
                    .hashCode() != mac3.hashCode());

            AttributedCharacterIterator.Attribute mac4 = mac1;

            assertTrue("The hash codes of same attributes but different hierarchy classes are not equal",
                    mac1.hashCode() == mac4.hashCode());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }

    /**
     * @tests java.text.AttributedCharacterIterator.Attribute#readResolve() Test
     *        of method
     *        java.text.AttributedCharacterIterator.Attribute#readResolve().
     */
    @TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Checks InvalidObjectException.",
      targets = {
        @TestTarget(
          methodName = "readResolve",
          methodArgs = {}
        )
    })
    public void test_readResolve() {
        MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                "test");
        try {
            mac1.readResolve();
            fail("InvalidObjectException has not been thrown");
        } catch (InvalidObjectException e) {
            // expected
        }
    }

    /**
     * @tests java.text.AttributedCharacterIterator.Attribute#toString() Test of
     *        method java.text.AttributedCharacterIterator.Attribute#toString().
     */
    @TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "toString",
          methodArgs = {}
        )
    })
    public void test_toString() {
        MockAttributedCharacterIteratorAttribute mac1 = new MockAttributedCharacterIteratorAttribute(
                null);
        assertEquals("Unexpected class representation string", mac1.toString(),
                getClass().getName()
                        + "$MockAttributedCharacterIteratorAttribute(null)");
        TestAttributedCharacterIteratorAttribute mac2 = new TestAttributedCharacterIteratorAttribute(
                "test1");
        assertEquals("Unexpected class representation string", mac2.toString(),
                getClass().getName()
                        + "$TestAttributedCharacterIteratorAttribute(test1)");
    }
}
