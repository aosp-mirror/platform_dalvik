package tests.api.java.net;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;

import junit.framework.TestCase;

import tests.support.Support_Configuration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

@TestTargetClass(URLStreamHandlerFactory.class) 
public class URLStreamHandlerFactoryTest extends TestCase {

    URLStreamHandlerFactory oldFactory = null;
    Field factoryField = null;
    
    boolean isTestable = false;
    
    boolean isOpenConnectionCalled = false;
    boolean isCreateURLStreamHandlerCalled = false;
    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "createURLStreamHandler",
        args = {java.lang.String.class}
    )
    public void test_createURLStreamHandler() throws MalformedURLException {
        
        if(isTestable) {
            
            TestURLStreamHandlerFactory shf = new TestURLStreamHandlerFactory();
            assertFalse(isCreateURLStreamHandlerCalled);
            URL.setURLStreamHandlerFactory(shf);
            URL url = new URL("http://" + 
                    Support_Configuration.SpecialInetTestAddress);

            try {
                url.openConnection();
                assertTrue(isCreateURLStreamHandlerCalled);
                assertTrue(isOpenConnectionCalled);
            } catch (Exception e) {
                fail("Exception during test : " + e.getMessage());
            
            }
            
            try {
                URL.setURLStreamHandlerFactory(shf);
                fail("java.lang.Error was not thrown.");                
            } catch(java.lang.Error e) {
                //expected
            }
            
            try {
                URL.setURLStreamHandlerFactory(null);
                fail("java.lang.Error was not thrown.");                
            } catch(java.lang.Error e) {
                //expected
            }
            
        } else { 
            TestURLStreamHandlerFactory shf = new TestURLStreamHandlerFactory();
            URLStreamHandler sh = shf.createURLStreamHandler("");
            assertNotNull(sh.toString());
        }
    }
    
    public void setUp() {
        Field [] fields = URL.class.getDeclaredFields();
        int counter = 0;
        for (Field field : fields) {
            if (URLStreamHandlerFactory.class.equals(field.getType())) {
                counter++;
                factoryField = field;
            }
        } 
        
        if(counter == 1) {
            
            isTestable = true;
    
            factoryField.setAccessible(true);
            try {
                oldFactory = (URLStreamHandlerFactory) factoryField.get(null);
            } catch (IllegalArgumentException e) {
                fail("IllegalArgumentException was thrown during setUp: " 
                        + e.getMessage());
            } catch (IllegalAccessException e) {
                fail("IllegalAccessException was thrown during setUp: "
                        + e.getMessage());
            }        
        }
    }
    
    public void tearDown() {
        if(isTestable) {
            try {
                factoryField.set(null, oldFactory);
            } catch (IllegalArgumentException e) {
                fail("IllegalArgumentException was thrown during tearDown: " 
                        + e.getMessage());
            } catch (IllegalAccessException e) {
                fail("IllegalAccessException was thrown during tearDown: "
                        + e.getMessage());
            }
        }
    }
    
    class TestURLStreamHandlerFactory implements URLStreamHandlerFactory {

        public URLStreamHandler createURLStreamHandler(String protocol) {
            isCreateURLStreamHandlerCalled = true;
            return new TestURLStreamHandler();
        }
    }
    
    class TestURLStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            isOpenConnectionCalled = true;
            return null;
        }
    }
}
