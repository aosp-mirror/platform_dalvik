/*
 * Copyright (C) 2010 The Android Open Source Project
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An outcome of an action. Some actions may have multiple outcomes. For
 * example, JUnit tests have one outcome for each test method.
 */
final class Outcome {

    private final String outcomeName;
    private final String actionName;
    private final Result result;
    private final List<String> outputLines;

    public Outcome(String outcomeName, String actionName, Result result,
            List<String> outputLines) {
        this.outcomeName = outcomeName;
        this.actionName = actionName;
        this.result = result;
        this.outputLines = outputLines;
    }

    public Outcome(String actionName, Result result, String outputLine) {
        this.outcomeName = actionName;
        this.actionName = actionName;
        this.result = result;
        this.outputLines = Collections.singletonList(outputLine);
    }

    public Outcome(String actionName, Result result, Throwable throwable) {
        this.outcomeName = actionName;
        this.actionName = actionName;
        this.result = result;
        this.outputLines = throwableToLines(throwable);
    }

    public String getName() {
        return outcomeName;
    }

    public String getActionName() {
        return actionName;
    }

    public Result getResult() {
        return result;
    }

    public List<String> getOutputLines() {
        return outputLines;
    }

    private static List<String> throwableToLines(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        t.printStackTrace(out);
        return Arrays.asList(writer.toString().split("\\n"));
    }

    /**
     * Returns the action's suite name, such as java.lang.Integer or
     * java.lang.IntegerTest.
     */
    public String getSuiteName() {
        int split = split(outcomeName);
        return split == -1 ? "defaultpackage" : outcomeName.substring(0, split);
    }

    /**
     * Returns the specific action name, such as BitTwiddle or testBitTwiddle.
     */
    public String getTestName() {
        int split = split(outcomeName);
        return split == -1 ? outcomeName : outcomeName.substring(split + 1);
    }

    private static int split(String name) {
        int lastHash = name.indexOf('#');
        return lastHash == -1 ? name.lastIndexOf('.') : lastHash;
    }
}
