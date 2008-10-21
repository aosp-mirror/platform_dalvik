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

/**
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package tests.api.javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import junit.framework.TestCase;


/**
 * Tests for <code>SocketFactory</code> class methods.
 */
public class SocketFactoryTest extends TestCase {

	/**
	 * @tests javax.net.SocketFactory#SocketFactory()
	 */
	public void test_Constructor() {
		try {
			new MySocketFactory();
		} catch (Exception e) {
			fail("Unexpected exception " + e.toString());
		}
	}
	
    /**
     * @tests javax.net.SocketFactory#createSocket()
     */
    public final void test_createSocket() {
        SocketFactory sf = new MySocketFactory();
        try {
            sf.createSocket();
            fail("No expected SocketException");
        } catch (SocketException e) {        
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    /**
     * @tests javax.net.SocketFactory#getDefault()
     */
    public final void test_getDefault() {
        SocketFactory sf = SocketFactory.getDefault();
        Socket s;
        try {
            s = sf.createSocket("localhost", 8082);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket("localhost", 8081, InetAddress.getLocalHost(), 8082);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket(InetAddress.getLocalHost(), 8081);
            s.close();
        } catch (IOException e) {
        } 
        try {
            s = sf.createSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            s.close();
        } catch (IOException e) {
        }     
    }
}

class MySocketFactory extends SocketFactory {
	
	public MySocketFactory() {
		super();
	}
	
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        throw new IOException();
    }
    
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        throw new IOException();
    }
    
    public Socket createSocket(InetAddress host, int port) throws IOException {
        throw new IOException();
     }
    
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        throw new IOException();
     }

}
