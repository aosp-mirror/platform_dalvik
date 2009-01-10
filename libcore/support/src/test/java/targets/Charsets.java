package targets;

import dalvik.annotation.VirtualTestTarget;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

/**
 * @hide
 */
public interface Charsets {
    
    /**
     * @hide 
     */
    static abstract class _Abstract extends Charset {

        public void functionalCoDec_REPR () {}

        _Abstract() throws IllegalCharsetNameException {
            super(null, null);
        }

//        @Override
//        public boolean contains(Charset charset) { return false; }
//
//        @Override
//        public CharsetDecoder newDecoder() { return null; }
//
//        @Override
//        public CharsetEncoder newEncoder() { return null; }

//        static abstract class _Decoder extends CharsetDecoder {
//
//            _Decoder() throws IllegalCharsetNameException {
//                super(null, 0, 0);
//            }
//
//            protected CoderResult decode_REPR(ByteBuffer in, CharBuffer out, boolean endOfInput) { return null; }
//        }
//
//        static abstract class _Encoder extends CharsetDecoder {
//
//            _Encoder() throws IllegalCharsetNameException {
//                super(null, 0, 0);
//            }
//
////            protected CoderResult encode_REPR(ByteBuffer in, CharBuffer out, boolean endOfInput) { return null; }
//        }
    }

    @VirtualTestTarget
    static abstract class US_ASCII extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class UTF_8 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class UTF_16 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class UTF_16BE extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class UTF_16LE extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_1 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_2 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_3 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_4 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_5 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_6 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_7 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_8 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_9 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_10 extends _Abstract {
        //
        // Note: NOT SUPPORTED BY RI!!!
        //
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_11 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_13 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_14 extends _Abstract {
        //
        // Note: NOT SUPPORTED BY RI!!!
        //
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_15 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_8859_16 extends _Abstract {
        //
        // Note: NOT SUPPORTED BY RI!!!
        //
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class ISO_2022_JP extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class x_windows_950 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1250 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1251 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1252 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1253 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1254 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1255 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1256 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1257 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class windows_1258 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class Big5 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class IBM864 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class x_IBM874 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class GB2312 extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class EUC_JP extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class KOI8_R extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class macintosh extends _Abstract {
        //
        // Note: NOT SUPPORTED BY RI!!!
        //
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class GBK extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class EUC_KR extends _Abstract {
        public void functionalCoDec_REPR () {}
    }

    @VirtualTestTarget
    static abstract class GSM0338 extends _Abstract {
        //
        // Note: NOT SUPPORTED BY RI!!!
        //
        public void functionalCoDec_REPR () {}
    }

}
