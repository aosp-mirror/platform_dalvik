package tests.targets.security;

import javax.crypto.spec.IvParameterSpec;

import dalvik.annotation.TestTargetClass;

@TestTargetClass(targets.AlgorithmParameters.DESede.class)
public class AlgorithmParametersTestDESede extends AlgorithmParametersTest {

    private static final byte[] parameterData = new byte[] {
        (byte) 0x04, (byte) 0x08, (byte) 0x68, (byte) 0xC8,
        (byte) 0xFF, (byte) 0x64, (byte) 0x72, (byte) 0xF5 };
    
    public AlgorithmParametersTestDESede() {
        super("DESede", new AlgorithmParameterSymmetricHelper("DESede", "CBC/PKCS5PADDING", 112), new IvParameterSpec(parameterData));
    }

}
