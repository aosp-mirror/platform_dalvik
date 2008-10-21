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

import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * A pipe contains two channels. One is the writable sink channel and the other is
 * readable source channel. When bytes are written into the writable channel they
 * can be read from readable channel. The order of these bytes remains unchanged. 
 * 
 */
public abstract class Pipe {

    /**
     * Writable sink channel for writing into.
     */
    public static abstract class SinkChannel extends AbstractSelectableChannel
            implements WritableByteChannel, GatheringByteChannel {
        
        /**
         * The Constructor.
         * 
         * @param provider
         *            the provider of the channel
         */
        protected SinkChannel(SelectorProvider provider) {
            super(provider);
        }

        /**
         * Indicates this channel supports only writing.
         * 
         * @return a static value of OP_WRITE
         */
        public final int validOps() {
            return SelectionKey.OP_WRITE;
        }
    }

    /**
     * Readable source channel for reading from.
     */
    public static abstract class SourceChannel extends
            AbstractSelectableChannel implements ReadableByteChannel,
            ScatteringByteChannel {
        
        /**
         * The Constructor.
         * 
         * @param provider
         *            the provider of the channel
         */
        protected SourceChannel(SelectorProvider provider) {
            super(provider);
        }

        /**
         * Indicates this channel supports only reading.
         * 
         * @return a static value of OP_READ
         */
        public final int validOps() {
            return SelectionKey.OP_READ;
        }

    }

    /**
     * Initializes a pipe.
     * 
     * @return a new instance of pipe
     * 
     * @throws IOException
     *             if I/O error occurs
     */
    public static Pipe open() throws IOException {
        return SelectorProvider.provider().openPipe();
    }

    /**
     * The protected constructor.
     */
    protected Pipe() {
        super();
    }

    /**
     * Returns the sink channel of the pipe.
     * 
     * @return a writable sink channel of the pipe
     */
    public abstract SinkChannel sink();

    /**
     * Returns the source channel of the pipe.
     * 
     * @return a readable source channel of the pipe
     */
    public abstract SourceChannel source();

}
