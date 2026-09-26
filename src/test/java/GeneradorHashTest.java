import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class GeneradorHashTest {

    @Test
    void generarHash() {
        String hash = new BCryptPasswordEncoder().encode("12345678");
        System.out.println("\n--------------------------------------------------");
        System.out.println("TU HASH BCrypt: " + hash);
        System.out.println("--------------------------------------------------\n");
    }
}