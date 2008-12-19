/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

package tests.api.java.util.concurrent;

import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;

public class CyclicBarrierTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());        
    }
    public static Test suite() {
        return new TestSuite(CyclicBarrierTest.class);
    }

    private volatile int countAction;
    private class MyAction implements Runnable {
        public void run() { ++countAction; }
    }
    
    /**
     * Creating with negative parties throws IAE
     */
    public void testConstructor1() {
        try {
            new CyclicBarrier(-1, (Runnable)null);
            shouldThrow();
        } catch(IllegalArgumentException e){}
    }

    /**
     * Creating with negative parties and no action throws IAE
     */
    public void testConstructor2() {
        try {
            new CyclicBarrier(-1);
            shouldThrow();
        } catch(IllegalArgumentException e){}
    }

    /**
     * getParties returns the number of parties given in constructor
     */
    public void testGetParties() {
        CyclicBarrier b = new CyclicBarrier(2);
        assertEquals(2, b.getParties());
        assertEquals(0, b.getNumberWaiting());
    }

    /**
     * A 1-party barrier triggers after single await
     */
    public void testSingleParty() {
        try {
            CyclicBarrier b = new CyclicBarrier(1);
            assertEquals(1, b.getParties());
            assertEquals(0, b.getNumberWaiting());
            b.await();
            b.await();
            assertEquals(0, b.getNumberWaiting());
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    
    /**
     * The supplied barrier action is run at barrier
     */
    public void testBarrierAction() {
        try {
            countAction = 0;
            CyclicBarrier b = new CyclicBarrier(1, new MyAction());
            assertEquals(1, b.getParties());
            assertEquals(0, b.getNumberWaiting());
            b.await();
            b.await();
            assertEquals(0, b.getNumberWaiting());
            assertEquals(countAction, 2);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * A 2-party/thread barrier triggers after both threads invoke await
     */
    public void testTwoParties() {
        final CyclicBarrier b = new CyclicBarrier(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        b.await();
                        b.await();
                        b.await();
                        b.await();
                    } catch(Exception e){
                        threadUnexpectedException();
                    }}});

        try {
            t.start();
            b.await();
            b.await();
            b.await();
            b.await();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * An interruption in one party causes others waiting in await to
     * throw BrokenBarrierException
     */
    public void testAwait1_Interrupted_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();
                    } catch(InterruptedException success){}                
                    catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            t1.interrupt();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * An interruption in one party causes others waiting in timed await to
     * throw BrokenBarrierException
     */
    public void testAwait2_Interrupted_BrokenBarrier() {
      final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(InterruptedException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            t1.interrupt();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    
    /**
     * A timeout in timed await throws TimeoutException
     */
    public void testAwait3_TimeOutException() {
        final CyclicBarrier c = new CyclicBarrier(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                        
                    }
                }
            });
        try {
            t.start();
            t.join(); 
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A timeout in one party causes others waiting in timed await to
     * throw BrokenBarrierException
     */
    public void testAwait4_Timeout_BrokenBarrier() {
      final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A timeout in one party causes others waiting in await to
     * throw BrokenBarrierException
     */
    public void testAwait5_Timeout_BrokenBarrier() {
      final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    
    /**
     * A reset of an active barrier causes waiting threads to throw
     * BrokenBarrierException
     */
    public void testReset_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();
                    } catch(BrokenBarrierException success){}                
                    catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            c.reset();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A reset before threads enter barrier does not throw
     * BrokenBarrierException
     */
    public void testReset_NoBrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            c.reset();
            t1.start();
            t2.start();
            c.await();
            t1.join(); 
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

}
