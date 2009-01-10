package org.apache.harmony.luni.tests.java.lang;

import java.io.FilePermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.PropertyPermission;

class MockSecurityManager extends SecurityManager {
    
    String deletedFile = "/";
    String readedFile  = "/";
    String writedFile  = "/";
    
    public void setInCheck(boolean inCheck) {
        super.inCheck = inCheck;
    }

    @Override
    public int classDepth(String name) {
        return super.classDepth(name);
    }

    @Override
    public int classLoaderDepth() {
        return super.classLoaderDepth();
    }

    @Override
    public void checkPermission(Permission perm) {
        if (perm.equals(new RuntimePermission("createSecurityManager")) ||
//          perm.equals(new AWTPermission("accessEventQueue")) ||
            perm.equals(new RuntimePermission("createClassLoader")) ||
            perm.equals(new FilePermission(deletedFile,"delete")) ||
            perm.equals(new FilePermission(readedFile,"read")) ||
            perm.equals(new PropertyPermission("*", "read,write")) ||
            perm.equals(new PropertyPermission("key", "read")) ||
            perm.equals(new SecurityPermission("getPolicy")) ||
//          perm.equals(new AWTPermission("accessClipboard")) ||
            perm.equals(new FilePermission(writedFile,"write"))) {
            throw
            new SecurityException("Unable to create Security Manager");
        }
    }

    @Override
    public ClassLoader currentClassLoader() {
        return super.currentClassLoader();
    }

    @Override
    public Class<?> currentLoadedClass() {
        return super.currentLoadedClass();
    }

    @Override
    public Class[] getClassContext() {
        return super.getClassContext();
    }

    @Override
    public boolean inClass(String name) {
        return super.inClass(name);
    }

    @Override
    public boolean inClassLoader() {
        return super.inClassLoader();
    }
}
