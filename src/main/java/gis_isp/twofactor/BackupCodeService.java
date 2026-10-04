package gis_isp.twofactor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class BackupCodeService {

    // Sin 0/O/1/I para evitar confusiones al dictarlos o copiarlos
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();
    private final TwoFactorProperties props;

    // Códigos en claro con formato XXXXX-XXXXX (se muestran una sola vez).
    public List<String> generate() {
        return IntStream.range(0, props.backupCodesCount())
                .mapToObj(i -> randomChars(5) + "-" + randomChars(5))
                .toList();
    }

    public List<String> hashAll(List<String> plain) {
        return plain.stream().map(this::hash).collect(Collectors.toCollection(ArrayList::new));
    }

    // Si el código existe lo elimina de la entidad y devuelve true.
    public boolean consume(UserTwoFactorAuthEntity entity, String code) {
        List<String> current = entity.getBackupCodes();
        if (current == null || current.isEmpty()) return false;
        String h = hash(code);
        if (!current.contains(h)) return false;
        List<String> updated = new ArrayList<>(current);
        updated.remove(h);
        entity.setBackupCodes(updated); // lista nueva para que Hibernate detecte el cambio
        return true;
    }

    private String hash(String code) {
        String normalized = code.replace("-", "").replace(" ", "").toUpperCase();
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String randomChars(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}
