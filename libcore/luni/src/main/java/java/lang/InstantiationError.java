/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;


/**
 * This error is thrown when the VM notices that a an attempt is being made to
 * create a new instance of a class which has no visible constructors from the
 * location where new is invoked.
 * <p>
 * Note that this can only occur when inconsistent class files are being loaded.
 * 
 */
public class InstantiationError extends IncompatibleClassChangeError {
    private static final long serialVersionUID = -4885810657349421204L;

    /**
     * Constructs a new instance of this class with its walkback filled in.
     * 
     */
    public InstantiationError() {
        super();
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param detailMessage
     *            String The detail message for the exception.
     */
    public InstantiationError(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new instance of this class with its walkback and message
     * filled in.
     * 
     * @param clazz
     *            Class The class which cannot be instantiated.
     */
    InstantiationError(Class<?> clazz) {
        super(clazz.getName());
    }

}
