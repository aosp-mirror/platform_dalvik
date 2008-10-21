/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio.channels;


/**
 * Thrown when a thread is interrupted in a blocking IO operation.
 * <p>
 * When the thread is interrupted by a call to <code>interrupt()</code>
 * it will close the channel, set the interrupt status of the thread to true,
 * and throw a <code>ClosedByInterruptException</code>.
 * 
 */
public class ClosedByInterruptException extends AsynchronousCloseException {

    private static final long serialVersionUID = -4488191543534286750L;

    /**
     * Default constructor.
     *
     */
    public ClosedByInterruptException() {
        super();
    }
}
