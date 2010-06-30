/*
 * Copyright 2006 The Android Open Source Project
 */

import java.lang.reflect.Array;

/**
 * Test java.lang.reflect.Array.
 */
public class Main {
    public static void main(String[] args) {
        testSingleInt();
        testSingle();
        testMultiInt();
        testMulti();

        System.out.println("ReflectArrayTest passed");
    }

    static void testSingleInt() {
        Object intArray;

        intArray = Array.newInstance(Integer.TYPE, 2);

        int[] array = (int[]) intArray;
        array[0] = 5;
        Array.setInt(intArray, 1, 6);

        if (Array.getInt(intArray, 0) != 5)
            throw new RuntimeException();
        if (array[1] != 6)
            throw new RuntimeException();
        try {
            array[2] = 27;
            throw new RuntimeException("store should have failed");
        }
        catch (ArrayIndexOutOfBoundsException abe) {
        }
        if (array.length != Array.getLength(intArray) ||
            array.length != 2)
        {
            throw new RuntimeException("bad len");
        }

        int[][] wrongArray;
        try {
            wrongArray = (int[][]) intArray;
            throw new RuntimeException("cast should have failed");
        }
        catch (ClassCastException cce) {
        }

        intArray = Array.newInstance(Integer.TYPE, 0);
        if (Array.getLength(intArray) != 0)
            throw new RuntimeException();
        System.out.println("ReflectArrayTest.testSingleInt passed");
    }

    static void testSingle() {
        Object strArray;

        strArray = Array.newInstance(String.class, 2);

        String[] array = (String[]) strArray;
        array[0] = "entry zero";
        Array.set(strArray, 1, "entry one");

        //System.out.println("array: " + array);

        if (!"entry zero".equals(Array.get(strArray, 0)))
            throw new RuntimeException();
        if (!"entry one".equals(array[1]))
            throw new RuntimeException();

        if (array.length != Array.getLength(strArray) ||
            array.length != 2)
        {
            throw new RuntimeException("bad len");
        }
        System.out.println("ReflectArrayTest.testSingle passed");
    }

    static void testMultiInt() {
        Object intIntIntArray;
        int[] dimensions = { 3, 2, 1 };

        intIntIntArray = Array.newInstance(Integer.TYPE, dimensions);
        int[][][] array3 = (int[][][]) intIntIntArray;

        array3[0][0][0] = 123;      // trouble
        array3[2][1][0] = 456;

        try {
            array3[2][1][1] = 768;
            throw new RuntimeException("store should have failed");
        }
        catch (ArrayIndexOutOfBoundsException abe) {
        }
        System.out.println("ReflectArrayTest.testMultiInt passed");
    }

    static void testMulti() {
        Object strStrStrArray;
        int[] dimensions = { 1, 2, 3 };

        strStrStrArray = Array.newInstance(String.class, dimensions);
        String[][][] array3 = (String[][][]) strStrStrArray;

        array3[0][0][0] = "zero zero zero";
        array3[0][1][2] = "zero one two";

        try {
            array3[1][0][0] = "bad store";
            throw new RuntimeException("store should have failed");
        }
        catch (ArrayIndexOutOfBoundsException abe) {
        }

        try {
            String[][] array2 = (String[][]) strStrStrArray;
            throw new RuntimeException("expecting bad cast");
        }
        catch (ClassCastException cce) {
        }

        String[] strar = new String[4];
        strar[2] = "zero one two ++";
        array3[0][1] = strar;
        System.out.println(array3[0][1][2]);
        //System.out.println("array3: " + array3);


        int[] dimensions2 = { 1, 2 };
        strStrStrArray = Array.newInstance(String[].class, dimensions2);
        array3 = (String[][][]) strStrStrArray;

        array3[0][1] = new String[3];
        array3[0][1][2] = "zero one two";
        try {
            array3[1][0][0] = "bad store";
            throw new RuntimeException("store should have failed");
        }
        catch (ArrayIndexOutOfBoundsException abe) {
        }
        System.out.println("ReflectArrayTest.testMulti passed");
    }
}
