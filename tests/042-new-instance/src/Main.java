/*
 * Copyright (C) 2007 The Android Open Source Project
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

/**
 * Test instance creation.
 */
public class Main {
    public static void main(String[] args) {
        // should succeed
        try {
            Class c = Class.forName("LocalClass");
            Object obj = c.newInstance();
            System.out.println("LocalClass succeeded");
        } catch (Exception ex) {
            System.err.println("LocalClass failed");
            ex.printStackTrace();
        }

        // should fail
        try {
            Class c = Class.forName("otherpackage.PackageAccess");
            Object obj = c.newInstance();
            System.err.println("ERROR: PackageAccess succeeded unexpectedly");
        } catch (IllegalAccessException iae) {
            System.out.println("Got expected PackageAccess complaint");
        } catch (Exception ex) {
            System.err.println("Got unexpected PackageAccess failure");
            ex.printStackTrace();
        }

        LocalClass2.main();

        try {
            MaybeAbstract ma = new MaybeAbstract();
            System.err.println("ERROR: MaybeAbstract succeeded unexpectedly");
        } catch (InstantiationError ie) {
            System.out.println("Got expected InstantationError");
        } catch (Exception ex) {
            System.err.println("Got unexpected MaybeAbstract failure");
        }
    }
}

class LocalClass {
  // this class has a default constructor with package visibility
}


class LocalClass2 {
    public static void main() {
        try {
            CC.newInstance();
            System.out.println("LocalClass2 succeeded");
        } catch (Exception ex) {
            System.err.println("Got unexpected LocalClass2 failure");
            ex.printStackTrace();
        }
    }

    static class CC {
        private CC() {}

        static Object newInstance() {
            try {
                Class c = CC.class;
                return c.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}

