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

package vogar;

import java.io.File;
import vogar.target.JUnitRunner;
import vogar.target.Runner;

/**
 * Create {@link Action}s for {@code .java} files with JUnit tests in them.
 */
class JUnitSpec extends NamingPatternRunnerSpec {

    @Override protected boolean matches(File file) {
        String filename = file.getName();
        return super.matches(file) && (filename.endsWith("Test.java")
                || filename.endsWith("TestSuite.java")
                || filename.contains("Tests"));
    }

    public boolean supports(String className) {
        return className.endsWith("Test")
                || className.endsWith("TestSuite")
                || className.contains("Tests");
    }

    public Class<? extends Runner> getRunnerClass() {
        return JUnitRunner.class;
    }

    public File getSource() {
        return new File(Vogar.HOME_JAVA, "vogar/target/JUnitRunner.java");
    }

    public Classpath getClasspath() {
        // TODO: jar up just the junit classes and drop the jar in our lib/ directory.
        return Classpath.of(
                new File("out/host/common/obj/JAVA_LIBRARIES/junit_intermediates/javalib.jar").getAbsoluteFile());
    }
}
