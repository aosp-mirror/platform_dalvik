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

import java.util.List;

/**
 * Thrown when an out of process executable does not return normally.
 */
class CommandFailedException extends RuntimeException {

    private final List<String> args;
    private final List<String> outputLines;

    public CommandFailedException(List<String> args, List<String> outputLines) {
        super(formatMessage(args, outputLines));
        this.args = args;
        this.outputLines = outputLines;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<String> getOutputLines() {
        return outputLines;
    }

    public static String formatMessage(List<String> args, List<String> outputLines) {
        StringBuilder result = new StringBuilder();
        result.append("Command failed:");
        for (String arg : args) {
            result.append(" ").append(arg);
        }
        for (String outputLine : outputLines) {
            result.append("\n  ").append(outputLine);
        }
        return result.toString();
    }
}
