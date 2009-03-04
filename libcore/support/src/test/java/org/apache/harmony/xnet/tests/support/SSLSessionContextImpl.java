package org.apache.harmony.xnet.tests.support;

import java.util.*;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;

public class SSLSessionContextImpl implements SSLSessionContext {
    
    private int cashSize;
    private long sessionTimeout;
    
    public SSLSessionContextImpl() {
        cashSize = 0;
        sessionTimeout = 0;
    }
    
    public int getSessionCacheSize() {
        return cashSize;
    }
    
    public void setSessionCacheSize(int newSize) throws IllegalArgumentException {
        if(newSize < 0) throw new IllegalArgumentException();
        if (newSize < cashSize) {
            System.out.println("<--- Number of sessions will be changed");
        }
        cashSize = newSize;
    }
    
    public int getSessionTimeout()
    {
        return (int)(sessionTimeout / 1000L);
    }
    
    public void setSessionTimeout(int seconds) throws IllegalArgumentException {
        if(seconds < 0) {
            throw new IllegalArgumentException();
        } else {
            sessionTimeout = (long)seconds * 1000L;
        }
    }

    public SSLSession getSession(byte abyte0[])
    {
        return null;
    }

    public Enumeration getIds()
    {
        return null;
    }

}
