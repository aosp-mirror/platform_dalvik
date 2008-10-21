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

package java.lang;

import java.util.Random;

/**
 * Class StrictMath provides various numeric operations using the standards set
 * by the known "Freely Distributable Math Library" (fdlibm). The standard is
 * set by the January 4th, 1995 version of the library.
 */
public final class StrictMath {

    /**
     * Standard math constant
     */
    public final static double E = Math.E;

    /**
     * Standard math constant
     */
    public final static double PI = Math.PI;

    private static java.util.Random random;

    /**
     * Prevents this class from being instantiated.
     */
    private StrictMath() {
    }

    /**
     * Returns the absolute value of the argument.
     * 
     * @param d
     *            the value to be converted
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static double abs(double d) {
        long bits = Double.doubleToLongBits(d);
        bits &= 0x7fffffffffffffffL;
        return Double.longBitsToDouble(bits);
    }

    /**
     * Returns the absolute value of the argument.
     * 
     * @param f
     *            the value to be converted
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static float abs(float f) {
        int bits = Float.floatToIntBits(f);
        bits &= 0x7fffffff;
        return Float.intBitsToFloat(bits);
    }

    /**
     * Returns the absolute value of the argument.
     * 
     * @param i
     *            the value to be converted
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static int abs(int i) {
        return i >= 0 ? i : -i;
    }

    /**
     * Returns the absolute value of the argument.
     * 
     * @param l
     *            the value to be converted
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static long abs(long l) {
        return l >= 0 ? l : -l;
    }

    /**
     * Returns the closest double approximation of the arc cosine of the
     * argument
     * 
     * @param d
     *            the value to compute acos of
     * @return the arc cosine of the argument.
     */
    public static native double acos(double d);

    /**
     * Returns the closest double approximation of the arc sine of the argument
     * 
     * @param d
     *            the value to compute asin of
     * @return the arc sine of the argument.
     */
    public static native double asin(double d);

    /**
     * Returns the closest double approximation of the arc tangent of the
     * argument
     * 
     * @param d
     *            the value to compute atan of
     * @return the arc tangent of the argument.
     */
    public static native double atan(double d);

    /**
     * Returns the closest double approximation of the arc tangent of the result
     * of dividing the first argument by the second argument.
     * 
     * @param d1
     *            the numerator of the value to compute atan of
     * @param d2
     *            the denominator of the value to compute atan of
     * @return the arc tangent of d1/d2.
     */
    public static native double atan2(double d1, double d2);
    
     /**
     * Returns the closest double approximation of the cube root of the
     * argument. 
     * 
     * @param d
     *             the value to compute cube root of
     * @return the cube root of the argument.
     */
    public static native double cbrt(double d);

    /**
     * Returns the double conversion of the most negative (i.e. closest to
     * negative infinity) integer value which is greater than the argument.
     * 
     * @param d
     *            the value to be converted
     * @return the ceiling of the argument.
     */
    public static native double ceil(double d);
    
    
    /**
     * Returns the closest double approximation of the hyperbolic cosine of the
     * argument.
     * 
     * @param d
     *            the value to compute hyperbolic cosine of
     * @return the hyperbolic cosine of the argument.
     */
    public static native double cosh(double d);

    /**
     * Returns the closest double approximation of the cosine of the argument
     * 
     * @param d
     *            the value to compute cos of
     * @return the cosine of the argument.
     */
    public static native double cos(double d);

    /**
     * Returns the closest double approximation of the raising "e" to the power
     * of the argument
     * 
     * @param d
     *            the value to compute the exponential of
     * @return the exponential of the argument.
     */
    public static native double exp(double d);
    
    /**
     * Returns the closest double approximation of <i>e</i><sup>d</sup> - 1.
     * If the argument is very close to 0, it is much more accurate to use
     * expm1(d)+1 than exp(d).
     * 
     * @param d
     *            the value to compute the <i>e</i><sup>d</sup> - 1 of
     * @return the <i>e</i><sup>d</sup> - 1 value of the argument.
     */
    public static native double expm1(double d);

    /**
     * Returns the double conversion of the most positive (i.e. closest to
     * positive infinity) integer value which is less than the argument.
     * 
     * 
     * @param d
     *            the value to be converted
     * @return the ceiling of the argument.
     */
    public static native double floor(double d);
    
    /**
     * Returns sqrt(<i>x</i><sup>2</sup>+<i>y</i><sup>2</sup>). The
     * final result is without medium underflow or overflow.
     * 
     * @param x
     *            a double number
     * @param y
     *            a double number
     * @return the sqrt(<i>x</i><sup>2</sup>+<i>y</i><sup>2</sup>) value
     *         of the arguments.
     */
    public static native double hypot(double x, double y);

    /**
     * Returns the remainder of dividing the first argument by the second using
     * the IEEE 754 rules.
     * 
     * @param d1
     *            the numerator of the operation
     * @param d2
     *            the denominator of the operation
     * @return the result of d1/d2.
     */
    public static native double IEEEremainder(double d1, double d2);

    /**
     * Returns the closest double approximation of the natural logarithm of the
     * argument
     * 
     * @param d
     *            the value to compute the log of
     * @return the natural logarithm of the argument.
     */
    public static native double log(double d);
    
    /**
     * Returns the logarithm of the argument and the base is 10.
     * 
     * @param d
     *            the value to compute the base 10 log of
     * @return the base 10 logarithm of the argument.
     */
    public static native double log10(double d);
    
    /**
     * Returns the closest double approximation of the natural logarithm of the
     * sum of the argument and 1. If the argument is very close to 0, it is much
     * more accurate to use log1p(d) than log(1.0+d).
     * 
     * @param d
     *            the value to compute the ln(1+d) of
     * @return the natural logarithm of the sum of the argument and 1.
     */
    public static native double log1p(double d);

    /**
     * Returns the most positive (i.e. closest to positive infinity) of the two
     * arguments.
     * 
     * @param d1
     *            the first argument to check
     * @param d2
     *            the second argument
     * @return the larger of d1 and d2.
     */
    public static double max(double d1, double d2) {
        if (d1 > d2)
            return d1;
        if (d1 < d2)
            return d2;
        /* if either arg is NaN, return NaN */
        if (d1 != d2)
            return Double.NaN;
        /* max( +0.0,-0.0) == +0.0 */
        if (d1 == 0.0
                && ((Double.doubleToLongBits(d1) & Double.doubleToLongBits(d2)) & 0x8000000000000000L) == 0)
            return 0.0;
        return d1;
    }

    /**
     * Returns the most positive (i.e. closest to positive infinity) of the two
     * arguments.
     * 
     * @param f1
     *            the first argument to check
     * @param f2
     *            the second argument
     * @return the larger of f1 and f2.
     */
    public static float max(float f1, float f2) {
        if (f1 > f2)
            return f1;
        if (f1 < f2)
            return f2;
        /* if either arg is NaN, return NaN */
        if (f1 != f2)
            return Float.NaN;
        /* max( +0.0,-0.0) == +0.0 */
        if (f1 == 0.0f
                && ((Float.floatToIntBits(f1) & Float.floatToIntBits(f2)) & 0x80000000) == 0)
            return 0.0f;
        return f1;
    }

    /**
     * Returns the most positive (i.e. closest to positive infinity) of the two
     * arguments.
     * 
     * @param i1
     *            the first argument to check
     * @param i2
     *            the second argument
     * @return the larger of i1 and i2.
     */
    public static int max(int i1, int i2) {
        return i1 > i2 ? i1 : i2;
    }

    /**
     * Returns the most positive (i.e. closest to positive infinity) of the two
     * arguments.
     * 
     * @param l1
     *            the first argument to check
     * @param l2
     *            the second argument
     * @return the larger of l1 and l2.
     */
    public static long max(long l1, long l2) {
        return l1 > l2 ? l1 : l2;
    }

    /**
     * Returns the most negative (i.e. closest to negative infinity) of the two
     * arguments.
     * 
     * @param d1
     *            the first argument to check
     * @param d2
     *            the second argument
     * @return the smaller of d1 and d2.
     */
    public static double min(double d1, double d2) {
        if (d1 > d2)
            return d2;
        if (d1 < d2)
            return d1;
        /* if either arg is NaN, return NaN */
        if (d1 != d2)
            return Double.NaN;
        /* min( +0.0,-0.0) == -0.0 */
        if (d1 == 0.0
                && ((Double.doubleToLongBits(d1) | Double.doubleToLongBits(d2)) & 0x8000000000000000l) != 0)
            return 0.0 * (-1.0);
        return d1;
    }

    /**
     * Returns the most negative (i.e. closest to negative infinity) of the two
     * arguments.
     * 
     * @param f1
     *            the first argument to check
     * @param f2
     *            the second argument
     * @return the smaller of f1 and f2.
     */
    public static float min(float f1, float f2) {
        if (f1 > f2)
            return f2;
        if (f1 < f2)
            return f1;
        /* if either arg is NaN, return NaN */
        if (f1 != f2)
            return Float.NaN;
        /* min( +0.0,-0.0) == -0.0 */
        if (f1 == 0.0f
                && ((Float.floatToIntBits(f1) | Float.floatToIntBits(f2)) & 0x80000000) != 0)
            return 0.0f * (-1.0f);
        return f1;
    }

    /**
     * Returns the most negative (i.e. closest to negative infinity) of the two
     * arguments.
     * 
     * @param i1
     *            the first argument to check
     * @param i2
     *            the second argument
     * @return the smaller of i1 and i2.
     */
    public static int min(int i1, int i2) {
        return i1 < i2 ? i1 : i2;
    }

    /**
     * Returns the most negative (i.e. closest to negative infinity) of the two
     * arguments.
     * 
     * @param l1
     *            the first argument to check
     * @param l2
     *            the second argument
     * @return the smaller of l1 and l2.
     */
    public static long min(long l1, long l2) {
        return l1 < l2 ? l1 : l2;
    }

    /**
     * Returns the closest double approximation of the result of raising the
     * first argument to the power of the second.
     * 
     * @param d1
     *            the base of the operation.
     * @param d2
     *            the exponent of the operation.
     * @return d1 to the power of d2
     */
    public static native double pow(double d1, double d2);

    /**
     * Returns a pseudo-random number between 0.0 and 1.0.
     * 
     * @return a pseudo-random number
     */
    public static double random() {
        if (random == null)
            random = new Random();
        return random.nextDouble();
    }

    /**
     * Returns the double conversion of the result of rounding the argument to
     * an integer.
     * 
     * @param d
     *            the value to be converted
     * @return the closest integer to the argument (as a double).
     */
    public static native double rint(double d);

    /**
     * Returns the result of rounding the argument to an integer.
     * 
     * @param d
     *            the value to be converted
     * @return the closest integer to the argument.
     */
    public static long round(double d) {
        // check for NaN
        if (d != d)
            return 0L;
        return (long) Math.floor(d + 0.5d);
    }

    /**
     * Returns the result of rounding the argument to an integer.
     * 
     * @param f
     *            the value to be converted
     * @return the closest integer to the argument.
     */
    public static int round(float f) {
        // check for NaN
        if (f != f)
            return 0;
        return (int) Math.floor(f + 0.5f);
    }
    
    /**
     * Returns the signum function of the argument. If the argument is less than
     * zero, it returns -1.0. If greater than zero, 1.0 is returned. It returns
     * zero if the argument is also zero.
     * 
     * @param d
     *            the value to compute signum function of
     * @return the value of the signum function.
     */
    public static double signum(double d){
        if(Double.isNaN(d)){
            return Double.NaN;
        }
        double sig = d;
        if(d > 0){
            sig = 1.0;
        }else if (d < 0){
            sig = -1.0;
        }
        return sig;
    }
    
    /**
     * Returns the signum function of the argument. If the argument is less than
     * zero, it returns -1.0. If greater than zero, 1.0 is returned. It returns
     * zero if the argument is also zero.
     * 
     * @param f
     *            the value to compute signum function of
     * @return the value of the signum function.
     */
    public static float signum(float f){
        if(Float.isNaN(f)){
            return Float.NaN;
        }
        float sig = f;
        if(f > 0){
            sig = 1.0f;
        }else if (f < 0){
            sig = -1.0f;
        }
        return sig;
    }

    /**
     * Returns the closest double approximation of the hyperbolic sine of the
     * argument. 
     * 
     * @param d
     *            the value to compute hyperbolic sine of
     * @return the hyperbolic sine of the argument.
     */
    public static native double sinh(double d);
    
    /**
     * Returns the closest double approximation of the sine of the argument
     * 
     * @param d
     *            the value to compute sin of
     * @return the sine of the argument.
     */
    public static native double sin(double d);

    /**
     * Returns the closest double approximation of the square root of the
     * argument
     * 
     * @param d
     *            the value to compute sqrt of
     * @return the square root of the argument.
     */
    public static native double sqrt(double d);

    /**
     * Returns the closest double approximation of the tangent of the argument
     * 
     * @param d
     *            the value to compute tan of
     * @return the tangent of the argument.
     */
    public static native double tan(double d);

    /**
     * Returns the closest double approximation of the hyperbolic tangent of the
     * argument. The absolute value is always less than 1. 
     * 
     * @param d
     *            the value to compute hyperbolic tangent of
     * @return the hyperbolic tangent of the argument.
     */
    public static native double tanh(double d);
    
    /**
     * Returns the measure in degrees of the supplied radian angle
     * 
     * @param angrad
     *            an angle in radians
     * @return the degree measure of the angle.
     */
    public static double toDegrees(double angrad) {
        return angrad * 180d / PI;
    }

    /**
     * Returns the measure in radians of the supplied degree angle
     * 
     * @param angdeg
     *            an angle in degrees
     * @return the radian measure of the angle.
     */
    public static double toRadians(double angdeg) {
        return angdeg / 180d * PI;
    }
    
    /**
     * Returns the argument's ulp. The size of a ulp of a double value is the
     * positive distance between this value and the double value next larger
     * in magnitude. For non-NaN x, ulp(-x) == ulp(x).
     * 
     * @param d
     *            the floating-point value to compute ulp of
     * @return the size of a ulp of the argument.
     */
    public static double ulp(double d) {
        // special cases
        if (Double.isInfinite(d)) {
            return Double.POSITIVE_INFINITY;
        } else if (d == Double.MAX_VALUE || d == -Double.MAX_VALUE) {
            return pow(2, 971);
        }
        d = Math.abs(d);
        return nextafter(d, Double.MAX_VALUE) - d;
    }

    /**
     * Returns the argument's ulp. The size of a ulp of a float value is the
     * positive distance between this value and the float value next larger
     * in magnitude. For non-NaN x, ulp(-x) == ulp(x).
     * 
     * @param f
     *            the floating-point value to compute ulp of
     * @return the size of a ulp of the argument.
     */
    public static float ulp(float f) {
        // special cases
        if (Float.isNaN(f)) {
            return Float.NaN;
        } else if (Float.isInfinite(f)) {
            return Float.POSITIVE_INFINITY;
        } else if (f == Float.MAX_VALUE || f == -Float.MAX_VALUE) {
            return (float) pow(2, 104);
        }
        f = Math.abs(f);
        return nextafterf(f, Float.MAX_VALUE) - f;
    }

    private native static double nextafter(double x, double y);

    private native static float nextafterf(float x, float y); 
}
