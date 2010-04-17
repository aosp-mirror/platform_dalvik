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
import vogar.target.CaliperRunner;
import vogar.target.Runner;

/**
 * Create {@link Action}s for {@code .java} files with Caliper benchmarks in
 * them.
 */
class CaliperSpec extends NamingPatternRunnerSpec {

    @Override protected boolean matches(File file) {
        return super.matches(file) && file.getName().endsWith("Benchmark.java");
    }

    public boolean supports(String className) {
        return className.endsWith("Benchmark");
    }

    public Class<? extends Runner> getRunnerClass() {
        return CaliperRunner.class;
    }

    public File getSource() {
        return new File(Vogar.HOME_JAVA, "vogar/target/CaliperRunner.java");
    }

    public Classpath getClasspath() {
        return Classpath.of(
                new File("dalvik/libcore/tools/runner/lib/jsr305.jar"),
                new File("dalvik/libcore/tools/runner/lib/guava.jar"),
                new File("dalvik/libcore/tools/runner/lib/caliper.jar"));
    }
}
