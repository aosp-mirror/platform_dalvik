/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dexdeps;

/**
 * Generate fancy output.
 */
public class Output {
    public static void generate(DexData dexData, String format) {
        if (format.equals("brief")) {
            printBrief(dexData);
        } else if (format.equals("xml")) {
            printXml(dexData);
        } else {
            /* should've been trapped in arg handler */
            throw new RuntimeException("unknown output format");
        }
    }

    /**
     * Prints the data in a simple human-readable format.
     */
    static void printBrief(DexData dexData) {
        FieldRef[] externFieldRefs = dexData.getExternalFieldReferences();
        MethodRef[] externMethodRefs = dexData.getExternalMethodReferences();

        printFieldRefs(externFieldRefs);
        printMethodRefs(externMethodRefs);
    }

    /**
     * Prints the list of fields in a simple human-readable format.
     */
    static void printFieldRefs(FieldRef[] fields) {
        System.out.println("Fields:");
        for (int i = 0; i < fields.length; i++) {
            FieldRef ref = fields[i];

            System.out.println(descriptorToDot(ref.getDeclClassName()) + "." +
                ref.getName() + " : " + ref.getTypeName());
        }
    }

    /**
     * Prints the list of methods in a simple human-readable format.
     */
    static void printMethodRefs(MethodRef[] methods) {
        System.out.println("Methods:");
        for (int i = 0; i < methods.length; i++) {
            MethodRef ref = methods[i];

            System.out.println(descriptorToDot(ref.getDeclClassName()) +
                "." + ref.getName() + " : " + ref.getDescriptor());
        }
    }


    /**
     * Prints the output in XML format.
     *
     * We shouldn't need to XML-escape the field/method info.
     */
    static void printXml(DexData dexData) {
        final String IN0 = "";
        final String IN1 = "  ";
        final String IN2 = "    ";
        final String IN3 = "      ";
        FieldRef[] externFieldRefs = dexData.getExternalFieldReferences();
        MethodRef[] externMethodRefs = dexData.getExternalMethodReferences();
        String prevClass = null;

        System.out.println(IN0 + "<external>");

        /* print fields */
        for (int i = 0; i < externFieldRefs.length; i++) {
            FieldRef fref = externFieldRefs[i];
            String declClassName = fref.getDeclClassName();

            if (prevClass != null && !prevClass.equals(declClassName)) {
                System.out.println(IN1 + "</class>");
            }
            if (!declClassName.equals(prevClass)) {
                String className = classNameOnly(declClassName);
                String packageName = packageNameOnly(declClassName);
                System.out.println(IN1 + "<class package=\"" + packageName +
                    "\" name=\"" + className + "\">");
                prevClass = declClassName;
            }

            System.out.println(IN2 + "<field name=\"" + fref.getName() +
                "\" type=\"" + descriptorToDot(fref.getTypeName()) + "\"/>");
        }

        /* print methods */
        for (int i = 0; i < externMethodRefs.length; i++) {
            MethodRef mref = externMethodRefs[i];
            String declClassName = mref.getDeclClassName();
            boolean constructor;

            if (prevClass != null && !prevClass.equals(declClassName)) {
                System.out.println(IN1 + "</class>");
            }
            if (!declClassName.equals(prevClass)) {
                String className = classNameOnly(declClassName);
                String packageName = packageNameOnly(declClassName);
                System.out.println(IN1 + "<class package=\"" + packageName +
                    "\" name=\"" + className + "\">");
                prevClass = declClassName;
            }

            constructor = mref.getName().equals("<init>");
            if (constructor) {
                /* use class name instead of method name */
                System.out.println(IN2 + "<constructor name=\"" +
                    classNameOnly(declClassName) + "\" return=\"" +
                    descriptorToDot(mref.getReturnTypeName()) + "\">");
            } else {
                System.out.println(IN2 + "<method name=\"" + mref.getName() +
                    "\" return=\"" + descriptorToDot(mref.getReturnTypeName()) +
                    "\">");
            }
            String[] args = mref.getArgumentTypeNames();
            for (int j = 0; j < args.length; j++) {
                System.out.println(IN3 + "<parameter type=\"" +
                    descriptorToDot(args[j]) + "\"/>");
            }
            if (constructor) {
                System.out.println(IN2 + "</constructor>");
            } else {
                System.out.println(IN2 + "</method>");
            }
        }

        if (prevClass != null)
            System.out.println(IN1 + "</class>");
        System.out.println(IN0 + "</external>");
    }


    /*
     * =======================================================================
     *      Utility functions
     * =======================================================================
     */

    /**
     * Converts a single-character primitive type into its human-readable
     * equivalent.
     */
    static String primitiveTypeLabel(char typeChar) {
        /* primitive type; substitute human-readable name in */
        switch (typeChar) {
            case 'B':   return "byte";
            case 'C':   return "char";
            case 'D':   return "double";
            case 'F':   return "float";
            case 'I':   return "int";
            case 'J':   return "long";
            case 'S':   return "short";
            case 'V':   return "void";
            case 'Z':   return "boolean";
            default:
                /* huh? */
                System.err.println("Unexpected class char " + typeChar);
                assert false;
                return "UNKNOWN";
        }
    }

    /**
     * Converts a type descriptor to human-readable "dotted" form.  For
     * example, "Ljava/lang/String;" becomes "java.lang.String", and
     * "[I" becomes "int[].
     */
    static String descriptorToDot(String descr) {
        int targetLen = descr.length();
        int offset = 0;
        int arrayDepth = 0;

        /* strip leading [s; will be added to end */
        while (targetLen > 1 && descr.charAt(offset) == '[') {
            offset++;
            targetLen--;
        }
        arrayDepth = offset;

        if (targetLen == 1) {
            descr = primitiveTypeLabel(descr.charAt(offset));
            offset = 0;
            targetLen = descr.length();
        } else {
            /* account for leading 'L' and trailing ';' */
            if (targetLen >= 2 && descr.charAt(offset) == 'L' &&
                descr.charAt(offset+targetLen-1) == ';')
            {
                targetLen -= 2;     /* two fewer chars to copy */
                offset++;           /* skip the 'L' */
            }
        }

        char[] buf = new char[targetLen + arrayDepth * 2];

        /* copy class name over */
        int i;
        for (i = 0; i < targetLen; i++) {
            char ch = descr.charAt(offset + i);
            buf[i] = (ch == '/') ? '.' : ch;
        }

        /* add the appopriate number of brackets for arrays */
        while (arrayDepth-- > 0) {
            buf[i++] = '[';
            buf[i++] = ']';
        }
        assert i == buf.length;

        return new String(buf);
    }

    /**
     * Extracts the class name from a type descriptor.
     */
    static String classNameOnly(String typeName) {
        String dotted = descriptorToDot(typeName);

        int start = dotted.lastIndexOf(".");
        if (start < 0) {
            return dotted;
        } else {
            return dotted.substring(start+1);
        }
    }

    /**
     * Extracts the package name from a type descriptor, and returns it in
     * dotted form.
     */
    static String packageNameOnly(String typeName) {
        String dotted = descriptorToDot(typeName);

        int end = dotted.lastIndexOf(".");
        if (end < 0) {
            /* lives in default package */
            return "";
        } else {
            return dotted.substring(0, end);
        }
    }
}

