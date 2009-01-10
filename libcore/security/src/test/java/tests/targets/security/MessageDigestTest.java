package tests.targets.security;

import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import dalvik.annotation.TestTargets;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
@TestTargetClass(targets.MessageDigests.Internal.class)
public abstract class MessageDigestTest extends TestCase {

    private String digestAlgorithmName;
    
    protected MessageDigestTest(String digestAlgorithmName) {
        super();
        this.digestAlgorithmName = digestAlgorithmName;
    }

    private MessageDigest digest;
    private InputStream sourceData;
    private byte[] checkDigest;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.digest = getMessageDigest();
        this.sourceData = getSourceData();
        this.checkDigest = getCheckDigest();
        
    }
    
    MessageDigest getMessageDigest()
    {
        try {
            return MessageDigest.getInstance(digestAlgorithmName);
        } catch (NoSuchAlgorithmException e) {
            fail("failed to get digest instance: " + e);
            return null;
        }
    }
    
    InputStream getSourceData()
    {
        InputStream sourceStream = getClass().getResourceAsStream(digestAlgorithmName + ".data");
        assertNotNull("digest source data not found: " + digestAlgorithmName, sourceStream);
        return sourceStream;
    }
    
    byte[] getCheckDigest()
    {
        InputStream checkDigestStream = getClass().getResourceAsStream(digestAlgorithmName + ".check");
        byte[] checkDigest = new byte[digest.getDigestLength()];
        int read = 0;
        int index = 0;
        try {
            while ((read = checkDigestStream.read()) != -1)
            {
                checkDigest[index++] = (byte)read;
            }
        } catch (IOException e) {
            fail("failed to read digest golden data: " + digestAlgorithmName);
        }
        return checkDigest;
    }
    
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "update",
                args = {byte[].class,int.class,int.class}
            ),
            @TestTargetNew(
                level = TestLevel.ADDITIONAL,
                method = "digest",
                args = {}
            )
    })
    public void testMessageDigest()
    {
        byte[] buf = new byte[128];
        int read = 0;
        try {
            while ((read = sourceData.read(buf)) != -1)
            {
                digest.update(buf, 0, read);
            }
        } catch (IOException e) {
            fail("failed to read digest data");
        }
        
        byte[] computedDigest = digest.digest();
        
        assertNotNull("computed digest is is null", computedDigest);
        assertEquals("digest length mismatch", checkDigest.length, computedDigest.length);
        
        for (int i = 0; i < checkDigest.length; i++)
        {
            assertEquals("byte " + i + " of computed and check digest differ", checkDigest[i], computedDigest[i]);
        }
        
    }
}
