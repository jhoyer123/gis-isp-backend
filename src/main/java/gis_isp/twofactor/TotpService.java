package gis_isp.twofactor;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.OptionalLong;

@Service
public class TotpService {
    private static final long STEP_SECONDS = 30;
    private final TimeBasedOneTimePasswordGenerator totp;
    private final TwoFactorProperties props;
    private final SecureRandom random = new SecureRandom();

    public TotpService(TwoFactorProperties props) {
        this.props = props;
        try {
            this.totp = new TimeBasedOneTimePasswordGenerator(); // 6 dígitos, 30s, HmacSHA1
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String generateSecret() {
        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);
        return new Base32().encodeToString(bytes).replace("=", "");
    }

    public String buildOtpAuthUri(String secret, String accountEmail) {
        String issuer = enc(props.issuer());
        return "otpauth://totp/" + issuer + ":" + enc(accountEmail)
                + "?secret=" + secret + "&issuer=" + issuer
                + "&algorithm=SHA1&digits=6&period=30";
    }

    // Devuelve el step que coincidió (para guardarlo como lastUsedStep) o empty.
    public OptionalLong verify(String secret, String code, Long lastUsedStep) {
        if (code == null || !code.matches("\\d{6}")) return OptionalLong.empty();
        try {
            SecretKey key = new SecretKeySpec(
                    new Base32().decode(secret),
                    totp.getAlgorithm()
            );
            Instant now = Instant.now();
            for (int i = -1; i <= 1; i++) {
                Instant t = now.plusSeconds(i * STEP_SECONDS);
                long step = t.getEpochSecond() / STEP_SECONDS;
                if (lastUsedStep != null && step <= lastUsedStep) continue;
                String expected = totp.generateOneTimePasswordString(key, t);
                if (MessageDigest.isEqual(
                        expected.getBytes(StandardCharsets.UTF_8),
                        code.getBytes(StandardCharsets.UTF_8))) {
                    return OptionalLong.of(step);
                }
            }
            return OptionalLong.empty();
        } catch (Exception e) {
            throw new IllegalStateException("Error verificando TOTP", e);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
