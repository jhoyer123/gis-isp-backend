package gis_isp.twofactor;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecretCipher {
    private static final int IV_LEN = 12, TAG_BITS = 128;
    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    // Decodes the Base64 encryption key and ensures it is exactly 32 bytes (256 bits).
    public SecretCipher(TwoFactorProperties props) {
        byte[] raw = Base64.getDecoder().decode(props.encryptionKey());
        if (raw.length != 32) throw new IllegalStateException("TWOFACTOR_ENC_KEY debe ser de 32 bytes en base64");
        this.key = new SecretKeySpec(raw, "AES");
    }

    // Encrypts the plain text using AES-256-GCM with a random IV and returns the result as Base64.
    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(IV_LEN + ct.length).put(iv).put(ct).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error cifrando secreto 2FA", e);
        }
    }

    // Decodes the Base64 value, extracts the IV, decrypts the ciphertext, and returns the original text.
    public String decrypt(String stored) {
        try {
            byte[] all = Base64.getDecoder().decode(stored);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, all, 0, IV_LEN));
            return new String(c.doFinal(all, IV_LEN, all.length - IV_LEN), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error descifrando secreto 2FA", e);
        }
    }
}