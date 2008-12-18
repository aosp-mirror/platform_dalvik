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
/**
 * @author Elena Semukhina
 * @version $Revision$
 */

package org.apache.harmony.tests.java.math;

import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestInfo;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTarget;

import java.math.*;

import junit.framework.TestCase;
@TestTargetClass(BigDecimal.class)
/**
 * Class:  java.math.BigDecimal
 * Methods: movePointLeft, movePointRight, scale, setScale, unscaledValue * 
 */
public class BigDecimalScaleOperationsTest extends TestCase {
    /**
     * Check the default scale
     */
@TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "This is a complete subset of tests for scale method.",
      targets = {
        @TestTarget(
          methodName = "scale",
          methodArgs = {}
        )
    })
    public void testScaleDefault() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int cScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a));
        assertTrue("incorrect scale", aNumber.scale() == cScale);
    }

    /**
     * Check a negative scale
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for scale method.",
          targets = {
            @TestTarget(
              methodName = "scale",
              methodArgs = {}
            )
        })
    public void testScaleNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = -10;
        int cScale = -10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        assertTrue("incorrect scale", aNumber.scale() == cScale);
    }

    /**
     * Check a positive scale
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for scale method.",
          targets = {
            @TestTarget(
              methodName = "scale",
              methodArgs = {}
            )
        })
    public void testScalePos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 10;
        int cScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        assertTrue("incorrect scale", aNumber.scale() == cScale);
    }

    /**
     * Check the zero scale
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for scale method.",
          targets = {
            @TestTarget(
              methodName = "scale",
              methodArgs = {}
            )
        })
    public void testScaleZero() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 0;
        int cScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        assertTrue("incorrect scale", aNumber.scale() == cScale);
    }

    /**
     * Check the unscaled value
     */
@TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "unscaledValue",
          methodArgs = {}
        )
    })
    public void testUnscaledValue() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 100;
        BigInteger bNumber = new BigInteger(a);
        BigDecimal aNumber = new BigDecimal(bNumber, aScale);
        assertTrue("incorrect unscaled value", aNumber.unscaledValue().equals(bNumber));
    }
    
    /**
     * Set a greater new scale
     */
@TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "This is a complete subset of tests for setScale method.",
      targets = {
        @TestTarget(
          methodName = "setScale",
          methodArgs = {int.class}
        )
    })
    public void testSetScaleGreater() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 18;
        int newScale = 28;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertEquals("incorrect value", 0, bNumber.compareTo(aNumber));
    }

    /**
     * Set a less new scale; this.scale == 8; newScale == 5.
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for setScale method.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class}
            )
        })
    public void testSetScaleLess() {
        String a = "2.345726458768760000E+10";
        int newScale = 5;
        BigDecimal aNumber = new BigDecimal(a);
        BigDecimal bNumber = aNumber.setScale(newScale);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertEquals("incorrect value", 0, bNumber.compareTo(aNumber));
    }

    /**
     * Verify an exception when setting a new scale
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for setScale method.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class}
            )
        })
    public void testSetScaleException() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        try {
            aNumber.setScale(newScale);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Rounding necessary", e.getMessage());
        }
    }

    /**
     * Set the same new scale
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for setScale method.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class}
            )
        })
    public void testSetScaleSame() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 18;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.equals(aNumber));
    }

    /**
     * Set a new scale
     */
@TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "Exception checking missed.",
      targets = {
        @TestTarget(
          methodName = "setScale",
          methodArgs = {int.class, int.class}
        )
    })
    public void testSetScaleRoundUp() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478139";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_UP);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }

    /**
     * Set a new scale
     */
@TestInfo(
          level = TestLevel.PARTIAL,
          purpose = "Exception checking missed.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class, int.class}
            )
        })
    public void testSetScaleRoundDown() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_DOWN);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }

    /**
     * Set a new scale
     */
@TestInfo(
          level = TestLevel.PARTIAL,
          purpose = "Exception checking missed.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class, int.class}
            )
        })
    public void testSetScaleRoundCeiling() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478139";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_CEILING);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }

    /**
     * Set a new scale
     */
@TestInfo(
          level = TestLevel.PARTIAL,
          purpose = "Exception checking missed.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class, int.class}
            )
        })
    public void testSetScaleRoundFloor() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_FLOOR);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }

    /**
     * Set a new scale
     */
@TestInfo(
          level = TestLevel.PARTIAL,
          purpose = "Exception checking missed.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class, int.class}
            )
        })
    public void testSetScaleRoundHalfUp() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_HALF_UP);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }

    /**
     * Set a new scale
     */
@TestInfo(
          level = TestLevel.PARTIAL,
          purpose = "Exception checking missed.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class, int.class}
            )
        })
    public void testSetScaleRoundHalfDown() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_HALF_DOWN);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }

    /**
     * Set a new scale
     */
@TestInfo(
          level = TestLevel.PARTIAL,
          purpose = "Exception checking missed.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class, int.class}
            )
        })
    public void testSetScaleRoundHalfEven() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.setScale(newScale, BigDecimal.ROUND_HALF_EVEN);
        assertTrue("incorrect scale", bNumber.scale() == newScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
    
    /**
     * SetScale(int, RoundingMode)
     */
@TestInfo(
          level = TestLevel.PARTIAL,
          purpose = "Exception checking missed.",
          targets = {
            @TestTarget(
              methodName = "setScale",
              methodArgs = {int.class, int.class}
            )
        })
    public void testSetScaleIntRoundingMode() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int newScale = 18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal result = aNumber.setScale(newScale, RoundingMode.HALF_EVEN);
        String res = "123121247898748298842980.877981045763478138";
        int resScale = 18;
        assertEquals("incorrect value", res, result.toString());
        assertEquals("incorrect scale", resScale, result.scale());
    }
    
    /**
     * Move the decimal point to the left; the shift value is positive
     */
@TestInfo(
      level = TestLevel.PARTIAL,
      purpose = "ArithmeticException checking missed",
      targets = {
        @TestTarget(
          methodName = "movePointLeft",
          methodArgs = {int.class}
        )
    })
    public void testMovePointLeftPos() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int shift = 18;
        int resScale = 46;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointLeft(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(a));
    }
        
    /**
     * Move the decimal point to the left; the shift value is positive
     */
@TestInfo(
          level = TestLevel.PARTIAL,
          purpose = "ArithmeticException checking missed",
          targets = {
            @TestTarget(
              methodName = "movePointLeft",
              methodArgs = {int.class}
            )
        })
    public void testMovePointLeftNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int shift = -18;
        int resScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointLeft(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(a));
    }

    /**
     * Move the decimal point to the right; the shift value is positive
     */
@TestInfo(
      level = TestLevel.PARTIAL_OK,
      purpose = "This is a complete subset of tests for movePointRight method.",
      targets = {
        @TestTarget(
          methodName = "movePointRight",
          methodArgs = {int.class}
        )
    })
    public void testMovePointRightPosGreater() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int shift = 18;
        int resScale = 10;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointRight(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(a));
    }
        
    /**
     * Move the decimal point to the right; the shift value is positive
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for movePointRight method.",
          targets = {
            @TestTarget(
              methodName = "movePointRight",
              methodArgs = {int.class}
            )
        })
    public void testMovePointRightPosLess() {
        String a = "1231212478987482988429808779810457634781384756794987";
        String b = "123121247898748298842980877981045763478138475679498700";
        int aScale = 28;
        int shift = 30;
        int resScale = 0;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointRight(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(b));
    }
        
    /**
     * Move the decimal point to the right; the shift value is positive
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for movePointRight method.",
          targets = {
            @TestTarget(
              methodName = "movePointRight",
              methodArgs = {int.class}
            )
        })
    public void testMovePointRightNeg() {
        String a = "1231212478987482988429808779810457634781384756794987";
        int aScale = 28;
        int shift = -18;
        int resScale = 46;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        BigDecimal bNumber = aNumber.movePointRight(shift);
        assertTrue("incorrect scale", bNumber.scale() == resScale);
        assertTrue("incorrect value", bNumber.unscaledValue().toString().equals(a));
    }

    /**
     * Move the decimal point to the right when the scale overflows
     */
@TestInfo(
          level = TestLevel.PARTIAL_OK,
          purpose = "This is a complete subset of tests for movePointRight method.",
          targets = {
            @TestTarget(
              methodName = "movePointRight",
              methodArgs = {int.class}
            )
        })
    public void testMovePointRightException() {
        String a = "12312124789874829887348723648726347429808779810457634781384756794987";
        int aScale = Integer.MAX_VALUE; //2147483647
        int shift = -18;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        try {
            aNumber.movePointRight(shift);
            fail("ArithmeticException has not been caught");
        } catch (ArithmeticException e) {
            assertEquals("Improper exception message", "Underflow", e.getMessage());
        }
    }

    /**
     * precision()
     */
@TestInfo(
      level = TestLevel.COMPLETE,
      purpose = "",
      targets = {
        @TestTarget(
          methodName = "precision",
          methodArgs = {}
        )
    })
    public void testPrecision() {
        String a = "12312124789874829887348723648726347429808779810457634781384756794987";
        int aScale = 14;
        BigDecimal aNumber = new BigDecimal(new BigInteger(a), aScale);
        int prec = aNumber.precision();
        assertEquals(68, prec);
    }
    
/// ANDROID ADDED
    
    /**
     * check that setScale with a scale greater to the existing scale does not
     * change the value.
     */
@TestInfo(
          level = TestLevel.COMPLETE,
          purpose = "",
          targets = {
            @TestTarget(
              methodName = "precision",
              methodArgs = {}
            )
        })
    public void testSetScale() {
        BigDecimal x1 = new BigDecimal(1.23400);
        BigDecimal x2 = x1.setScale(75);
        
        assertEquals(0, x1.compareTo(x2));
        assertEquals(0, x2.compareTo(x1));
        
        x1.precision();
        
        assertEquals(0, x1.compareTo(x2));
        assertEquals(0, x2.compareTo(x1));
       
        x2.precision();
        
        assertEquals(0, x1.compareTo(x2));
        assertEquals(0, x2.compareTo(x1));
    }
    

}
