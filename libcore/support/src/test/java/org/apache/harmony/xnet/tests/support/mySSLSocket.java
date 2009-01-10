package org.apache.harmony.xnet.tests.support;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.HandshakeCompletedListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Additional class for SSLSocket constructor verification
 */
public class mySSLSocket extends SSLSocket {
    
    private boolean init = false;
    private boolean sslFlag = true;
    private boolean sslNeed = false;
    private boolean sslWant = false;
    private int sslMode;
    private String[] supportProtocol = null;
    private String[] supportSuites = null;
    
    public mySSLSocket(int mode){
        super();
        sslMode = mode;
    }
    public mySSLSocket(String[] protocol, String[] suites){
        super();
        supportProtocol = protocol;
        supportSuites = suites;
    }
    public mySSLSocket(int mode, String[] protocol, String[] suites){
        super();
        sslMode = mode;
        supportProtocol = protocol;
        supportSuites = suites;
    }
    public mySSLSocket(){
        super();
    }
    public mySSLSocket(InetAddress address, int port) throws IOException{
        super(address, port);
    }
    public mySSLSocket(InetAddress address, int port, 
                       InetAddress clientAddress, int clientPort) throws IOException{
        super(address, port, clientAddress, clientPort);
    }
    public mySSLSocket(String host, int port) throws IOException, UnknownHostException{
        super(host, port);
    }
    public mySSLSocket(String host, int port, InetAddress clientAddress, 
                       int clientPort) throws IOException, UnknownHostException{
        super(host, port, clientAddress, clientPort);
    }
    
    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if(listener == null)  throw new IllegalArgumentException("listener is null");
    }
    public String[] getEnabledCipherSuites() {
        return supportSuites;
    }
    public String[] getEnabledProtocols() {
        return supportProtocol;
    }
    public boolean getEnableSessionCreation() {
        return sslFlag;
    }
    public boolean getNeedClientAuth() {
        if (sslMode == 1) {
            throw new IllegalStateException("Incorrect mode");
        } else return sslNeed;
    }
    public SSLSession getSession() {
        return null;
    }
    public String[] getSupportedCipherSuites() {
        if (supportSuites == null) {
            throw new NullPointerException();
        }
        if (supportSuites.length == 0) {
            return null;
        } else return supportSuites;
    }
    public String[] getSupportedProtocols() {
        if (supportProtocol == null) {
            throw new NullPointerException();
        }
        if (supportProtocol.length == 0) {
            return null;
        } else return supportProtocol;
    }
    public boolean getUseClientMode() {
        if (sslMode == 1) {
            return true;
        } else return false;
    }
    public boolean getWantClientAuth() {
        if (sslMode == 1) {
            throw new IllegalStateException("Incorrect mode");
        } else return sslWant; 
    }
    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if(listener == null)  throw new IllegalArgumentException("listener is null");
    }
    public void setEnabledCipherSuites(String[] suites) {
        if (suites == null) {
            throw new IllegalArgumentException("null parameter");
        }
        if (!suites.equals(supportSuites)) {
            throw new IllegalArgumentException("incorrect suite");
        }
    }
    public void setEnabledProtocols(String[] protocols) {
        if (protocols == null) {
            throw new IllegalArgumentException("null protocol");
        }
        if (!protocols.equals(supportProtocol)) {
            throw new IllegalArgumentException("incorrect protocol");
        }
    }
    public void setEnableSessionCreation(boolean flag) {
        sslFlag = flag;
    }
    public void setNeedClientAuth(boolean need) {
        if (sslMode == 0) {
            sslNeed = need;
        } else {
            throw new IllegalStateException("Incorrect mode");
        }
    }
    public void setUseClientMode(boolean mode) {
        if (!init) {
            if (mode && sslMode == 0) {
                sslMode = 1;
            } else if (!mode && sslMode == 1) {
                sslMode = 0;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    public void setWantClientAuth(boolean want) {
        if (sslMode == 0) {
            sslWant = want;
        } else {
            throw new IllegalStateException("Incorrect mode");
        }
    }
    public void startHandshake() throws IOException {
        for (int i = 0; i < supportProtocol.length; i++) {
            if (supportProtocol[i] == "Protocol_2") {
                throw new IOException();
            }
        }
        init = true;
    }
}   
    
