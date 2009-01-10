package tests.targets.security;

import dalvik.annotation.TestTargetClass;

import javax.crypto.spec.IvParameterSpec;

@TestTargetClass(targets.AlgorithmParameters.AES.class)
public class AlgorithmParametersTestAES extends AlgorithmParametersTest {

    private static final byte[] parameterData = new byte[] {
        (byte) 0x04, (byte) 0x08, (byte) 0x68, (byte) 0xC8,
        (byte) 0xFF, (byte) 0x64, (byte) 0x72, (byte) 0xF5,
        (byte) 0x04, (byte) 0x08, (byte) 0x68, (byte) 0xC8,
        (byte) 0xFF, (byte) 0x64, (byte) 0x72, (byte) 0xF5 };
    
    public AlgorithmParametersTestAES() {
        super("AES", new AlgorithmParameterSymmetricHelper("AES", "CBC/PKCS5PADDING", 128), new IvParameterSpec(parameterData));
    }

}
