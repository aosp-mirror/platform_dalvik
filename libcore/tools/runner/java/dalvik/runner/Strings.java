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


package dalvik.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Utility methods for strings.
 */
public class Strings {

    static String readFile(File f) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader in =
                new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
            result.append(line);
            result.append('\n');
        }
        in.close();
        return result.toString();
    }

    static String join(Object[] objects, String delimiter) {
        return join(Arrays.asList(objects), delimiter);
    }

    static String join(Iterable<?> objects, String delimiter) {
        Iterator<?> i = objects.iterator();
        if (!i.hasNext()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(i.next());
        while(i.hasNext()) {
            result.append(delimiter).append(i.next());
        }
        return result.toString();
    }

    static String[] objectsToStrings(Object[] objects) {
        String[] result = new String[objects.length];
        int i = 0;
        for (Object o : objects) {
            result[i++] = o.toString();
        }
        return result;
    }

    static String[] objectsToStrings(Collection<?> objects) {
        return objectsToStrings(objects.toArray());
    }
}
