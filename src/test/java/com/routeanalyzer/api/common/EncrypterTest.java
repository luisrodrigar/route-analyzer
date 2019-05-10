package com.routeanalyzer.api.common;

import org.junit.Test;

import static com.routeanalyzer.api.common.Encrypter.decrypt;
import static com.routeanalyzer.api.common.Encrypter.encrypt;
import static org.assertj.core.api.Assertions.assertThat;

public class EncrypterTest {

    public static final String RESULT_EXPECTED = "AKIA4NWBTY2YUG7EHHEA";
    public static final String ENCODED_STR = "DlM1pGPUM6CcV2SkAI6xMCD3C926uxdOsb1W3J2vkdc=";

    @Test
    public void encryptTest() {
        String result = encrypt(RESULT_EXPECTED);
        assertThat(result).isEqualTo(ENCODED_STR);
    }

    @Test
    public void decryptTest() {
        String result = decrypt(ENCODED_STR);
        assertThat(result).isEqualTo(RESULT_EXPECTED);
    }
}