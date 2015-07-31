package data;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class EncryptionUtils {
    public static final String DEFAULT_ENCODING="UTF-8";
    static BASE64Encoder encoder = new BASE64Encoder();
    static BASE64Decoder decoder = new BASE64Decoder();

    public static String encode(String text){
        try {
            String rez = encoder.encode( text.getBytes( DEFAULT_ENCODING ) );
            return rez;
        }
        catch ( UnsupportedEncodingException e ) {
            return null;
        }
    }

    public static String decode(String text){

        try {
            return new String(decoder.decodeBuffer( text ),DEFAULT_ENCODING);
        }
        catch ( IOException e ) {
            return null;
        }

    }
}
