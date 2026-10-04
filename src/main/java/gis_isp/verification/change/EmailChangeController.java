package gis_isp.verification.change;

import gis_isp.verification.change.dto.ChangeEmailRequest;
import gis_isp.verification.change.dto.ConfirmEmail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Endpoints for changing the logged-in user's contact email.
@RestController
@RequestMapping("/api/account/email-change")
@RequiredArgsConstructor
public class EmailChangeController {

    private final EmailChangeRequestService service;

    // Authenticated: asks for a change; a confirmation link is emailed to the new address.
    @PostMapping
    public ResponseEntity<Void> request(Authentication auth, @Valid @RequestBody ChangeEmailRequest body) {
        service.requestChange(UUID.fromString(auth.getName()), body.newEmail(), body.currentPassword());
        return ResponseEntity.accepted().build();
    }

    // Public: the token in the emailed link is the proof; applies the change.
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@Valid @RequestBody ConfirmEmail body) {
        service.confirm(body.token());
        return ResponseEntity.noContent().build();
    }

}