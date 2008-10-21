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

package tests.api.java.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import tests.support.Support_Configuration;
import tests.support.Support_PortManager;

public class DatagramPacketTest extends junit.framework.TestCase {

	DatagramPacket dp;

	volatile boolean started = false;

	/**
	 * @tests java.net.DatagramPacket#DatagramPacket(byte[], int)
	 */
	public void test_Constructor$BI() {
		// Test for method java.net.DatagramPacket(byte [], int)
		try {
			dp = new DatagramPacket("Hello".getBytes(), 5);
			assertEquals("Created incorrect packet", "Hello", new String(dp.getData(), 0,
					dp.getData().length));
			assertEquals("Wrong length", 5, dp.getLength());
		} catch (Exception e) {
			fail("Exception during Constructor test: " + e.toString());
		}
        //regression for Harmony-890
        dp = new DatagramPacket(new byte[942],4);
        assertEquals(-1, dp.getPort());
        try{
            dp.getSocketAddress();
            fail("Should throw IllegalArgumentException");            
        }catch(IllegalArgumentException e){
            //expected
        }
	}

	/**
	 * @tests java.net.DatagramPacket#DatagramPacket(byte[], int, int)
	 */
	public void test_Constructor$BII() {
		try {
			dp = new DatagramPacket("Hello".getBytes(), 2, 3);
			assertEquals("Created incorrect packet", "Hello", new String(dp.getData(), 0,
					dp.getData().length));
			assertEquals("Wrong length", 3, dp.getLength());
			assertEquals("Wrong offset", 2, dp.getOffset());
		} catch (Exception e) {
			fail("Exception during Constructor test: " + e.toString());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#DatagramPacket(byte[], int, int,
	 *        java.net.InetAddress, int)
	 */
	public void test_Constructor$BIILjava_net_InetAddressI() {
		try {
			dp = new DatagramPacket("Hello".getBytes(), 2, 3, InetAddress
					.getLocalHost(), 0);
			assertTrue("Created incorrect packet", dp.getAddress().equals(
					InetAddress.getLocalHost())
					&& dp.getPort() == 0);
			assertEquals("Wrong length", 3, dp.getLength());
			assertEquals("Wrong offset", 2, dp.getOffset());
		} catch (Exception e) {
			fail("Exception during Constructor test: " + e.toString());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#DatagramPacket(byte[], int,
	 *        java.net.InetAddress, int)
	 */
	public void test_Constructor$BILjava_net_InetAddressI() {
		// Test for method java.net.DatagramPacket(byte [], int,
		// java.net.InetAddress, int)
		try {
			dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
					.getLocalHost(), 0);
			assertTrue("Created incorrect packet", dp.getAddress().equals(
					InetAddress.getLocalHost())
					&& dp.getPort() == 0);
			assertEquals("Wrong length", 5, dp.getLength());
		} catch (Exception e) {
			fail("Exception during Constructor test: " + e.toString());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#getAddress()
	 */
	public void test_getAddress() {
		// Test for method java.net.InetAddress
		// java.net.DatagramPacket.getAddress()
		try {
			dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
					.getLocalHost(), 0);
			assertTrue("Incorrect address returned", dp.getAddress().equals(
					InetAddress.getLocalHost()));
		} catch (Exception e) {
			fail("Exception during getAddress test:" + e.toString());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#getData()
	 */
	public void test_getData() {
		// Test for method byte [] java.net.DatagramPacket.getData()

		dp = new DatagramPacket("Hello".getBytes(), 5);
		assertEquals("Incorrect length returned", "Hello", new String(dp.getData(), 0, dp
				.getData().length));
	}

	/**
	 * @tests java.net.DatagramPacket#getLength()
	 */
	public void test_getLength() {
		// Test for method int java.net.DatagramPacket.getLength()

		dp = new DatagramPacket("Hello".getBytes(), 5);
		assertEquals("Incorrect length returned", 5, dp.getLength());
	}

	/**
	 * @tests java.net.DatagramPacket#getOffset()
	 */
	public void test_getOffset() {
		dp = new DatagramPacket("Hello".getBytes(), 3, 2);
		assertEquals("Incorrect length returned", 3, dp.getOffset());
	}

	/**
	 * @tests java.net.DatagramPacket#getPort()
	 */
	public void test_getPort() {
		// Test for method int java.net.DatagramPacket.getPort()
		try {
			dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
					.getLocalHost(), 1000);
			assertEquals("Incorrect port returned", 1000, dp.getPort());
		} catch (Exception e) {
			fail("Exception during getPort test : " + e.getMessage());
		}

		InetAddress localhost = null;
		try {
			localhost = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			fail("Unexpected UnknownHostException : " + e.getMessage());
		}
		int[] ports = Support_PortManager.getNextPortsForUDP(2);
		final int port = ports[0];
		final Object lock = new Object();

		Thread thread = new Thread(new Runnable() {
			public void run() {
				DatagramSocket socket = null;
				try {
					socket = new DatagramSocket(port);
					synchronized (lock) {
						started = true;
						lock.notifyAll();
					}
					socket.setSoTimeout(3000);
					DatagramPacket packet = new DatagramPacket(new byte[256],
							256);
					socket.receive(packet);
					socket.send(packet);
					socket.close();
				} catch (IOException e) {
					System.out.println("thread exception: " + e);
					if (socket != null)
						socket.close();
				}
			}
		});
		thread.start();

		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(ports[1]);
			socket.setSoTimeout(3000);
			DatagramPacket packet = new DatagramPacket(new byte[] { 1, 2, 3, 4,
					5, 6 }, 6, localhost, port);
			synchronized (lock) {
				try {
					if (!started)
						lock.wait();
				} catch (InterruptedException e) {
					fail(e.toString());
				}
			}
			socket.send(packet);
			socket.receive(packet);
			socket.close();
			assertTrue("datagram received wrong port: " + packet.getPort(),
					packet.getPort() == port);
		} catch (IOException e) {
			if (socket != null)
				socket.close();
			System.err.println("port: " + port + " datagram server error: ");
			e.printStackTrace();
			fail("port : " + port + " datagram server error : "
					+ e.getMessage());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#setAddress(java.net.InetAddress)
	 */
	public void test_setAddressLjava_net_InetAddress() {
		// Test for method void
		// java.net.DatagramPacket.setAddress(java.net.InetAddress)
		try {
			InetAddress ia = InetAddress
					.getByName(Support_Configuration.InetTestIP);
			dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
					.getLocalHost(), 0);
			dp.setAddress(ia);
			assertTrue("Incorrect address returned", dp.getAddress().equals(ia));
		} catch (Exception e) {
			fail("Exception during getAddress test:" + e.toString());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#setData(byte[], int, int)
	 */
	public void test_setData$BII() {
		dp = new DatagramPacket("Hello".getBytes(), 5);
		dp.setData("Wagga Wagga".getBytes(), 2, 3);
		assertEquals("Incorrect data set", "Wagga Wagga", new String(dp.getData())
				);
	}

	/**
	 * @tests java.net.DatagramPacket#setData(byte[])
	 */
	public void test_setData$B() {
		// Test for method void java.net.DatagramPacket.setData(byte [])
		dp = new DatagramPacket("Hello".getBytes(), 5);
		dp.setData("Ralph".getBytes());
		assertEquals("Incorrect data set", "Ralph", new String(dp.getData(), 0, dp
				.getData().length));
	}

	/**
	 * @tests java.net.DatagramPacket#setLength(int)
	 */
	public void test_setLengthI() {
		// Test for method void java.net.DatagramPacket.setLength(int)
		dp = new DatagramPacket("Hello".getBytes(), 5);
		dp.setLength(1);
		assertEquals("Failed to set packet length", 1, dp.getLength());
	}

	/**
	 * @tests java.net.DatagramPacket#setPort(int)
	 */
	public void test_setPortI() {
		// Test for method void java.net.DatagramPacket.setPort(int)
		try {
			dp = new DatagramPacket("Hello".getBytes(), 5, InetAddress
					.getLocalHost(), 1000);
			dp.setPort(2000);
			assertEquals("Port not set", 2000, dp.getPort());
		} catch (Exception e) {
			fail("Exception during setPort test : " + e.getMessage());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#DatagramPacket(byte[], int,
	 *        java.net.SocketAddress)
	 */
	public void test_Constructor$BILjava_net_SocketAddress() {
		class mySocketAddress extends SocketAddress {

			public mySocketAddress() {
			}
		}

		try {
			// unsupported SocketAddress subclass
			byte buf[] = new byte[1];
			try {
				DatagramPacket thePacket = new DatagramPacket(buf, 1,
						new mySocketAddress());
				fail("No exception when constructing using unsupported SocketAddress subclass");
			} catch (IllegalArgumentException ex) {
			}

			// case were we try to pass in null
			// unsupported SocketAddress subclass

			try {
				DatagramPacket thePacket = new DatagramPacket(buf, 1, null);
				fail("No exception when constructing address using null");
			} catch (IllegalArgumentException ex) {
			}

			// now validate we can construct
			InetSocketAddress theAddress = new InetSocketAddress(InetAddress
					.getLocalHost(), Support_PortManager.getNextPortForUDP());
			DatagramPacket thePacket = new DatagramPacket(buf, 1, theAddress);
			assertTrue("Socket address not set correctly (1)", theAddress
					.equals(thePacket.getSocketAddress()));
			assertTrue("Socket address not set correctly (2)", theAddress
					.equals(new InetSocketAddress(thePacket.getAddress(),
							thePacket.getPort())));
		} catch (Exception e) {
			fail("Exception during constructor test(1):" + e.toString());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#DatagramPacket(byte[], int, int,
	 *        java.net.SocketAddress)
	 */
	public void test_Constructor$BIILjava_net_SocketAddress() {
		class mySocketAddress extends SocketAddress {

			public mySocketAddress() {
			}
		}

		try {
			// unsupported SocketAddress subclass
			byte buf[] = new byte[2];
			try {
				DatagramPacket thePacket = new DatagramPacket(buf, 1, 1,
						new mySocketAddress());
				fail("No exception when constructing using unsupported SocketAddress subclass");
			} catch (IllegalArgumentException ex) {
			}

			// case were we try to pass in null
			// unsupported SocketAddress subclass

			try {
				DatagramPacket thePacket = new DatagramPacket(buf, 1, 1, null);
				fail("No exception when constructing address using null");
			} catch (IllegalArgumentException ex) {
			}

			// now validate we can construct
			InetSocketAddress theAddress = new InetSocketAddress(InetAddress
					.getLocalHost(), Support_PortManager.getNextPortForUDP());
			DatagramPacket thePacket = new DatagramPacket(buf, 1, 1, theAddress);
			assertTrue("Socket address not set correctly (1)", theAddress
					.equals(thePacket.getSocketAddress()));
			assertTrue("Socket address not set correctly (2)", theAddress
					.equals(new InetSocketAddress(thePacket.getAddress(),
							thePacket.getPort())));
			assertEquals("Offset not set correctly", 1, thePacket.getOffset());
		} catch (Exception e) {
			fail("Exception during constructor test(2):" + e.toString());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#getSocketAddress()
	 */
	public void test_getSocketAddress() {
		try {
			byte buf[] = new byte[1];
			DatagramPacket thePacket = new DatagramPacket(buf, 1);

			// validate get returns the value we set
			InetSocketAddress theAddress = new InetSocketAddress(InetAddress
					.getLocalHost(), Support_PortManager.getNextPortForUDP());
			thePacket = new DatagramPacket(buf, 1);
			thePacket.setSocketAddress(theAddress);
			assertTrue("Socket address not set correctly (1)", theAddress
					.equals(thePacket.getSocketAddress()));
		} catch (Exception e) {
			fail(
					"Exception during getSocketAddress test:" + e.toString());
		}
	}

	/**
	 * @tests java.net.DatagramPacket#setSocketAddress(java.net.SocketAddress)
	 */
	public void test_setSocketAddressLjava_net_SocketAddress() {

		class mySocketAddress extends SocketAddress {

			public mySocketAddress() {
			}
		}

		try {
			// unsupported SocketAddress subclass
			byte buf[] = new byte[1];
			DatagramPacket thePacket = new DatagramPacket(buf, 1);
			try {
				thePacket.setSocketAddress(new mySocketAddress());
				fail("No exception when setting address using unsupported SocketAddress subclass");
			} catch (IllegalArgumentException ex) {
			}

			// case were we try to pass in null
			// unsupported SocketAddress subclass
			thePacket = new DatagramPacket(buf, 1);
			try {
				thePacket.setSocketAddress(null);
				fail("No exception when setting address using null");
			} catch (IllegalArgumentException ex) {
			}

			// now validate we can set it correctly
			InetSocketAddress theAddress = new InetSocketAddress(InetAddress
					.getLocalHost(), Support_PortManager.getNextPortForUDP());
			thePacket = new DatagramPacket(buf, 1);
			thePacket.setSocketAddress(theAddress);
			assertTrue("Socket address not set correctly (1)", theAddress
					.equals(thePacket.getSocketAddress()));
			assertTrue("Socket address not set correctly (2)", theAddress
					.equals(new InetSocketAddress(thePacket.getAddress(),
							thePacket.getPort())));
		} catch (Exception e) {
			fail(
					"Exception during setSocketAddress test:" + e.toString());
		}
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() {
	}

	protected void doneSuite() {
	}
}
