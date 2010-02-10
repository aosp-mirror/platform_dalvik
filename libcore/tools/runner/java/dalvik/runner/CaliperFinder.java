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

import java.io.File;

/**
 * Create {@link TestRun}s for {@code .java} files with Caliper benchmarks in
 * them.
 */
class CaliperFinder extends NamingPatternCodeFinder {

    @Override protected boolean matches(File file) {
        return file.getName().endsWith("Benchmark.java");
    }

    @Override protected String testName(File file) {
        return "caliper";
    }

    public Class<? extends Runner> getRunnerClass() {
        return CaliperRunner.class;
    }

    public File getRunnerJava() {
        return new File(DalvikRunner.HOME_JAVA, "dalvik/runner/CaliperRunner.java");
    }

    public Classpath getRunnerClasspath() {
        return Classpath.of(
            // TODO: we should be able to work with a shipping SDK, not depend on out/...
            // TODO: have a pre-packaged caliper-all.jar in our lib directory, with the jtreg stuff.
            // external/caliper
            new File("out/target/common/obj/JAVA_LIBRARIES/caliper_intermediates/classes.jar").getAbsoluteFile(),
            // external/guava for external/caliper
            new File("out/target/common/obj/JAVA_LIBRARIES/guava_intermediates/classes.jar").getAbsoluteFile(),
            // external/jsr305 for external/guava
            new File("out/target/common/obj/JAVA_LIBRARIES/jsr305_intermediates/classes.jar").getAbsoluteFile());

    }
}
