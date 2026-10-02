package gis_isp.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class AvatarController {

    private final SupabaseAvatarService avatarService;

    @PostMapping("/avatar")
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        String url = avatarService.uploadAvatar(
                file,
                null
        );

        return ResponseEntity.ok(
                new AvatarUploadResponse(url)
        );
    }
}