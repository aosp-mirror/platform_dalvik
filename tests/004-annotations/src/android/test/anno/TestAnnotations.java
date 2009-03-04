package android.test.anno;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.TreeMap;

public class TestAnnotations {
    /**
     * Print the annotations in sorted order, so as to avoid
     * any (legitimate) non-determinism with regard to the iteration order.
     */
    static private void printAnnotationArray(String prefix, Annotation[] arr) {
        TreeMap<String, Annotation> sorted =
            new TreeMap<String, Annotation>();
        
        for (Annotation a : arr) {
            sorted.put(a.annotationType().getName(), a);
        }

        for (Annotation a : sorted.values()) {
            System.out.println(prefix + "  " + a);
            System.out.println(prefix + "    " + a.annotationType());
        }
    }
    
    static void printAnnotations(Class clazz) {
        Annotation[] annos;
        Annotation[][] parAnnos;

        annos = clazz.getAnnotations();
        System.out.println("annotations on TYPE " + clazz +
            "(" + annos.length + "):");
        printAnnotationArray("", annos);
        System.out.println();

        for (Constructor c: clazz.getDeclaredConstructors()) {
            annos = c.getDeclaredAnnotations();
            System.out.println("  annotations on CTOR " + c + ":");
            printAnnotationArray("  ", annos);

            System.out.println("    constructor parameter annotations:");
            for (Annotation[] pannos: c.getParameterAnnotations()) {
                printAnnotationArray("    ", pannos);
            }
        }

        for (Method m: clazz.getDeclaredMethods()) {
            annos = m.getDeclaredAnnotations();
            System.out.println("  annotations on METH " + m + ":");
            printAnnotationArray("  ", annos);

            System.out.println("    method parameter annotations:");
            for (Annotation[] pannos: m.getParameterAnnotations()) {
                printAnnotationArray("    ", pannos);
            }
        }

        for (Field f: clazz.getDeclaredFields()) {
            annos = f.getDeclaredAnnotations();
            System.out.println("  annotations on FIELD " + f + ":");
            printAnnotationArray("  ", annos);

            AnnoFancyField aff;
            aff = (AnnoFancyField) f.getAnnotation(AnnoFancyField.class);
            if (aff != null) {
                System.out.println("    aff: " + aff + " / " + aff.getClass());
                System.out.println("    --> nombre is '" + aff.nombre() + "'");
            }
        }
        System.out.println();
    }


    @ExportedProperty(mapping = {
        @IntToString(from = 0, to = "NORMAL_FOCUS"),
        @IntToString(from = 2, to = "WEAK_FOCUS")
    })
    public int getFocusType() {
        return 2;
    }

    public static void testArrayProblem() {
        Method meth;
        ExportedProperty property;
        final IntToString[] mapping;
        
        try {
            meth = TestAnnotations.class.getMethod("getFocusType",
                    (Class[])null);
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        }
        property = meth.getAnnotation(ExportedProperty.class);
        mapping = property.mapping();

        System.out.println("mapping is " + mapping.getClass() +
            "\n  0='" + mapping[0] + "'\n  1='" + mapping[1] + "'");
        System.out.println("");
    }



    public static void main(String[] args) {
        System.out.println("TestAnnotations...");

        testArrayProblem();
        //System.exit(0);

        System.out.println(
            "AnnoSimpleField " + AnnoSimpleField.class.isAnnotation() +
            ", SimplyNoted " + SimplyNoted.class.isAnnotation());

        Class clazz;
        clazz = SimplyNoted.class;
        printAnnotations(clazz);
        clazz = INoted.class;
        printAnnotations(clazz);
        clazz = SubNoted.class;
        printAnnotations(clazz);
        clazz = FullyNoted.class;
        printAnnotations(clazz);

        Annotation anno;

        // this is expected to be non-null
        anno = SimplyNoted.class.getAnnotation(AnnoSimpleType.class);
        System.out.println("SimplyNoted.get(AnnoSimpleType) = " + anno);
        // this is non-null if the @Inherited tag is present
        anno = SubNoted.class.getAnnotation(AnnoSimpleType.class);
        System.out.println("SubNoted.get(AnnoSimpleType) = " + anno);
    }
}

