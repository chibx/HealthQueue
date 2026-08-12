package com.healthqueue.utils;

import com.healthqueue.utils.Utils.GoReturn;
import com.password4j.Password;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class Auth {

    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    public static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    public static final String HMAC256_ALGORITHM = "HmacSHA256";

    // Base64 string form — safe as text, used as the Argon2 pepper
    public static final String HS256_SECRET_STRING = Utils.getEnv(Constants.SECRET_KEY);
    // public static final byte[] HS256_SECRET_BYTE =
    // Base64.getDecoder().decode(HS256_SECRET_STRING);

    public static final String AES_SECRET_STRING = Utils.getEnv(Constants.SECRET_KEY);
    public static final byte[] AES_SECRET_BYTE = Base64.getDecoder().decode(AES_SECRET_STRING);

    public static GoReturn<String> encrypt(String text, SecretKey key) {
        return Utils.tryGo(() -> {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] output = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

            // Java appends the auth tag onto the ciphertext automatically — Node keeps them
            // separate (cipher.getAuthTag()). Split it back out to match your
            // iv:tag:ciphertext format.
            int tagLen = GCM_TAG_LENGTH_BITS / 8;
            byte[] ciphertext = Arrays.copyOfRange(output, 0, output.length - tagLen);
            byte[] authTag = Arrays.copyOfRange(output, output.length - tagLen, output.length);

            Base64.Encoder b64 = Base64.getEncoder();
            return b64.encodeToString(iv) + ":" + b64.encodeToString(authTag) + ":" + b64.encodeToString(ciphertext);
        });
    }

    public static GoReturn<@Nullable String> decrypt(String encryptedPayload, SecretKey key) {
        return Utils.tryGo(() -> {
            String[] parts = encryptedPayload.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid encrypted payload format.");
            }

            Base64.Decoder b64 = Base64.getDecoder();
            byte[] iv = b64.decode(parts[0]);
            byte[] authTag = b64.decode(parts[1]);
            byte[] ciphertext = b64.decode(parts[2]);

            // Java expects ciphertext + tag concatenated back together for GCM
            byte[] combined = new byte[ciphertext.length + authTag.length];
            System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
            System.arraycopy(authTag, 0, combined, ciphertext.length, authTag.length);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(combined), StandardCharsets.UTF_8);
        });
    }

    private static final String SAFE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static GoReturn<@Nullable String> generateRandomSafeString(int length) {
        return Utils.tryGo(() -> {
            int alphabetLength = SAFE_ALPHABET.length();
            int maxValidByte = 256 - (256 % alphabetLength);

            StringBuilder result = new StringBuilder(length);
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[length];

            while (result.length() < length) {
                random.nextBytes(bytes);
                for (byte raw : bytes) {
                    int unsigned = raw & 0xFF; // Java's byte is signed (-128..127) — mask to 0-255 like a Node Buffer
                    if (unsigned < maxValidByte) {
                        result.append(SAFE_ALPHABET.charAt(unsigned % alphabetLength));
                        if (result.length() == length)
                            return result.toString();
                    }
                }
            }
            return result.toString();
        });
    }

    public static GoReturn<String> hashText(String text) {
        return Utils.tryGo(() -> Password.hash(text)
                .addPepper(HS256_SECRET_STRING)
                .addRandomSalt()
                .withArgon2()
                .getResult());
    }

    public static GoReturn<Boolean> verifyHash(String hashedText, String password) {
        return Utils.tryGo(() -> Password.check(password, hashedText)
                .addPepper(HS256_SECRET_STRING)
                .withArgon2());
    }

    public static GoReturn<String> signJWT(Map<String, Object> payload, SecretKey secretKey) {
        return signJWT(payload, secretKey, Constants.MINUTES_30);
    }

    public static GoReturn<String> signJWT(Map<String, Object> payload, SecretKey secretKey, long expiresInMs) {
        return Utils.tryGo(() -> Jwts.builder()
                .claims(payload)
                .issuer(Constants.APP_NAME)
                .issuedAt(new Date())
                .expiration(Date.from(Utils.addToDate(expiresInMs)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact());
    }

    public static GoReturn<Claims> verifyJWT(String token, SecretKey secretKey) {
        return Utils.tryGo(() -> Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload());
    }
}