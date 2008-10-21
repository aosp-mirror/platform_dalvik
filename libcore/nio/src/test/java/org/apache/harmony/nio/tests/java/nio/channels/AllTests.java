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

package org.apache.harmony.nio.tests.java.nio.channels;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(AlreadyConnectedExceptionTest.class);
        suite.addTestSuite(AsynchronousCloseExceptionTest.class);
        suite.addTestSuite(CancelledKeyExceptionTest.class);
        suite.addTestSuite(ChannelsTest.class);
        suite.addTestSuite(ClosedByInterruptExceptionTest.class);
        suite.addTestSuite(ClosedChannelExceptionTest.class);
        suite.addTestSuite(ClosedSelectorExceptionTest.class);
        suite.addTestSuite(ConnectionPendingExceptionTest.class);
        suite.addTestSuite(DatagramChannelTest.class); 
        suite.addTestSuite(FileChannelLockingTest.class);
        suite.addTestSuite(FileChannelTest.class);
        suite.addTestSuite(FileLockInterruptionExceptionTest.class);
        suite.addTestSuite(FileLockTest.class);
        suite.addTestSuite(IllegalBlockingModeExceptionTest.class);
        suite.addTestSuite(IllegalSelectorExceptionTest.class);
        suite.addTestSuite(MapModeTest.class);
        suite.addTestSuite(NoConnectionPendingExceptionTest.class);
        suite.addTestSuite(NonReadableChannelExceptionTest.class);
        suite.addTestSuite(NonWritableChannelExceptionTest.class);
        suite.addTestSuite(NotYetBoundExceptionTest.class);
        suite.addTestSuite(NotYetConnectedExceptionTest.class);
        suite.addTestSuite(OverlappingFileLockExceptionTest.class);
        suite.addTestSuite(PipeTest.class);
        suite.addTestSuite(SelectableChannelTest.class);
        suite.addTestSuite(SelectionKeyTest.class);
        suite.addTestSuite(SelectorTest.class);
        suite.addTestSuite(ServerSocketChannelTest.class);
        suite.addTestSuite(SinkChannelTest.class);
        suite.addTestSuite(SocketChannelTest.class);
        suite.addTestSuite(SourceChannelTest.class);
        suite.addTestSuite(UnresolvedAddressExceptionTest.class);
        suite.addTestSuite(UnsupportedAddressTypeExceptionTest.class);
        // $JUnit-END$
        return suite;
    }

}
