package gis_isp.storage;

import gis_isp.common.exception.AvatarUploadException;
import gis_isp.common.exception.InvalidAvatarException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseAvatarService {

    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1 MB

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/svg+xml", ".svg",
            "image/webp", ".webp"
    );

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.bucket-name}")
    private String bucketName;

    // upload new avatar
    public String uploadAvatar(
            MultipartFile file,
            String currentAvatarPath
    ) throws IOException {

        String extension = validate(file);

        String fileName = UUID.randomUUID() + extension;
        String newPath = "avatars/" + fileName;

        String uploadUrl = supabaseUrl
                + "/storage/v1/object/"
                + bucketName
                + "/"
                + newPath;

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(serviceRoleKey);
        headers.setContentType(
                MediaType.parseMediaType(file.getContentType())
        );

        HttpEntity<byte[]> request = new HttpEntity<>(
                file.getBytes(),
                headers
        );

        // first upload file
        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new AvatarUploadException(
                        "Respuesta no exitosa del servidor de almacenamiento."
                );
            }

        } catch (Exception e) {

            throw new AvatarUploadException(
                    "Ocurrió un error al subir la imagen de avatar",
                    e
            );
        }

        // file upload successfully, now delete file if exists
        if (currentAvatarPath != null && !currentAvatarPath.isBlank()) {
            deleteAvatar(currentAvatarPath);
        }

        return newPath;
    }

    // validate avatar file before
    private String validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidAvatarException(
                    "El avatar es obligatorio"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidAvatarException(
                    "El avatar no puede superar 1 MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_TYPES.containsKey(contentType)) {
            throw new InvalidAvatarException(
                    "El avatar debe ser SVG o WebP"
            );
        }

        return ALLOWED_TYPES.get(contentType);
    }

    // delete avatar
    private void deleteAvatar(String path) {

        if (!path.startsWith("avatars/")) {
            return;
        }

        String deleteUrl = supabaseUrl
                + "/storage/v1/object/"
                + bucketName
                + "/"
                + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(
                        "No se pudo eliminar el avatar anterior. "
                                + "Código HTTP: "
                                + response.getStatusCode()
                );
            }

        } catch (HttpClientErrorException.NotFound e) {

            //The file don't exist

        } catch (HttpClientErrorException e) {

            System.err.println(
                    "No se pudo eliminar el avatar anterior: "
                            + path
                            + ". Supabase respondió: "
                            + e.getResponseBodyAsString()
            );

        } catch (Exception e) {

            System.err.println(
                    "Error inesperado al eliminar el avatar anterior: "
                            + path
                            + ". "
                            + e.getMessage()
            );
        }
    }
}
