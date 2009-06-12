package org.apache.harmony.luni.tests.java.util;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

@TestTargetClass(ArrayList.class) 
public class ArrayListTest extends TestCase {

    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Regression test.",
        method = "add",
        args = {java.lang.Object.class}
    )
    public void test_addAllCollectionOfQextendsE() {
        // Regression for HARMONY-539
        // https://issues.apache.org/jira/browse/HARMONY-539
        ArrayList<String> alist = new ArrayList<String>();
        ArrayList<String> blist = new ArrayList<String>();
        alist.add("a");
        alist.add("b");
        blist.add("c");
        blist.add("d");
        blist.remove(0);
        blist.addAll(0, alist);
        assertEquals("a", blist.get(0));
        assertEquals("b", blist.get(1));
        assertEquals("d", blist.get(2));
    }

    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Regression test.",
            method = "addAll",
            args = {java.util.Collection.class}
    )
    public void test_growForInsert() {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        arrayList.addAll(0, Arrays.asList(1, 2));
        arrayList.addAll(2, Arrays.asList(13));
        arrayList.addAll(0, Arrays.asList(0));
        arrayList.addAll(3, Arrays.asList(11, 12));
        arrayList.addAll(6, Arrays.asList(22, 23, 24, 25, 26, 27, 28, 29));
        arrayList.addAll(6, Arrays.asList(14, 15, 16, 17, 18, 19, 20, 21));
        arrayList.addAll(3, Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10));
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29),
                arrayList);
    }
}