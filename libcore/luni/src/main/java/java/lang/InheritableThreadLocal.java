/*
 * Copyright (C) 2008 The Android Open Source Project
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

package java.lang;


/**
 * A variable for which each thread has its own value; child threads will
 * inherit the value at thread creation time.
 * 
 * @see java.lang.Thread
 * @see java.lang.ThreadLocal
 * @author Bob Lee
 */
public class InheritableThreadLocal<T> extends ThreadLocal<T> {

    /**
     * Creates a new inheritable thread local variable.
     */
    public InheritableThreadLocal() {}

    /**
     * Creates a value for the child thread given the parent thread's value.
     * Called from the parent thread when creating a child thread. The default
     * implementation returns the parent thread's value.
     */
    protected T childValue(T parentValue) {
        return parentValue;
    }

    @Override
    Values values(Thread current) {
        return current.inheritableValues;
    }

    @Override
    Values initializeValues(Thread current) {
        return current.inheritableValues = new Values();
    }
}
