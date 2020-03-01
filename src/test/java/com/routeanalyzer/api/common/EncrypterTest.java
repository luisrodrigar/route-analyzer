package com.routeanalyzer.api.common;

import org.junit.Test;

import static com.routeanalyzer.api.common.Encrypter.decrypt;
import static com.routeanalyzer.api.common.Encrypter.encrypt;
import static org.assertj.core.api.Assertions.assertThat;

public class EncrypterTest {

    public static final String DECODED_STR = "rTDAasfasgASDFASFasfasa";
    public static final String ENCODED_STR = "LSE+bVLIUmXOyhadEwDQ0mhhJ2hs0IHjePuQgH9Zpn8=";

    public static final String LONG_DECODED_STR = "AIzaSyDEtc96UC9co31AFUNuNsPZ1xV4SYEMwfA";
    public static final String LONG_ENCODED_STR = "uQp1FZQnVlXmwrp7me9xmcOfrp+CvXIAPmm0c2hyGdzjNnFtxpMiGvA+fDYxJAUk";

    @Test
    public void encryptTest() {
        String result = encrypt(DECODED_STR);
        assertThat(result).isEqualTo(ENCODED_STR);
    }

    @Test
    public void decryptTest() {
        String result = decrypt(ENCODED_STR);
        assertThat(result).isEqualTo(DECODED_STR);
    }

    @Test
    public void encryptLongStrTest() {
        String result = encrypt(LONG_DECODED_STR);
        assertThat(result).isEqualTo(LONG_ENCODED_STR);
    }

    @Test
    public void decryptLongStrTest() {
        String result = decrypt(LONG_ENCODED_STR);
        assertThat(result).isEqualTo(LONG_DECODED_STR);
    }
}
