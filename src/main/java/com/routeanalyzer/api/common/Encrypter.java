package com.routeanalyzer.api.common;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Optional;

import static com.routeanalyzer.api.common.Constants.INIT_VECTOR;
import static com.routeanalyzer.api.common.Constants.KEY_TO_ENCRYPT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * Encrypter with AES of 128bytes.
 * Using Cipher java class
 */
@Slf4j
@UtilityClass
public final class Encrypter {
    public static String encrypt(String value) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY_TO_ENCRYPT.getBytes(UTF_8), "AES");
        IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes());

        return initializeCipher(ENCRYPT_MODE, "AES/CBC/PKCS5PADDING", secretKeySpec, iv)
                .flatMap(cipher -> ofNullable(value)
                        .map(__ -> encodeString(value, cipher)))
                .orElse(null);
    }

    public static String decrypt(String encrypted) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY_TO_ENCRYPT.getBytes(UTF_8), "AES");
        IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes());

        return initializeCipher(DECRYPT_MODE, "AES/CBC/PKCS5PADDING", secretKeySpec, iv)
                .flatMap(cipher -> Try.of(() -> decodeString(encrypted, cipher))
                        .onFailure(error -> log.error(error.getMessage(), error))
                        .toJavaOptional())
                .map(String::new)
                .orElse(null);
    }

    private static Optional<Cipher> initializeCipher(int mode, String type, SecretKeySpec skeySpec, IvParameterSpec iv) {
        return Try.of(() -> Cipher.getInstance(type))
                .toJavaOptional()
                .map(cipher -> {
                    Try.run(() -> cipher.init(mode, skeySpec, iv))
                            .onFailure(err -> log.error("Error initializing the cipher object", err));
                    return cipher;
                });
    }

    private static byte[] decodeString(String encrypted, Cipher cipher) {
        return ofNullable(encrypted)
                .map(Base64::decodeBase64)
                .flatMap(decodeStr -> Try.of(() -> cipher.doFinal(decodeStr))
                        .onFailure(err -> log.error("Error trying to get the decoded string", err))
                        .toJavaOptional())
                .orElse(null);
    }

    private static String encodeString(String decrypted, Cipher cipher) {
        return ofNullable(decrypted)
                .map(String::getBytes)
                .flatMap(bytes -> Try.of( () -> cipher.doFinal(bytes))
                        .onFailure(err -> log.error("Error trying to get the encoded string", err))
                        .toJavaOptional())
                .map(Base64::encodeBase64String)
                .orElse(null);
    }

}
