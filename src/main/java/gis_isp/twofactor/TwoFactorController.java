package gis_isp.twofactor;

import gis_isp.twofactor.dto.*;
import gis_isp.twofactor.event.TwoFactorEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// GESTIÓN DEL 2FA (todo autenticado: el userId sale del JWT, nunca del body).
// El reto del login NO está aquí, está en AuthController (/api/auth/2fa/verify).
// Este controller no es @Transactional: cada operación hace commit en TwoFactorService
// y solo después se publica el evento (auditoría + correo), así que un evento
// nunca se registra si la operación falló.
@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService service;
    private final ApplicationEventPublisher events;

    // PASO 1 PARA ACTIVAR: genera un secreto nuevo (pendiente, el 2FA aún NO está activo).
    // Devuelve {secret, otpauthUri}: el front dibuja el QR con el otpauthUri y muestra el
    // secret como texto de respaldo. Llamarlo otra vez reemplaza el secreto (el QR viejo deja
    // de servir). Si el 2FA ya está activo responde 409. El secret NUNCA se loguea.
    @PostMapping("/setup")
    public SetupResponse setup(@AuthenticationPrincipal UUID userId) {
        return service.setup(userId);
    }

    // PASO 2 PARA ACTIVAR: el usuario escanea el QR y manda el primer código de 6 dígitos.
    // Si es válido: se activa el 2FA y se devuelven los 10 códigos de respaldo.
    // ÚNICO momento en que se generan los códigos de respaldo y se ven en claro: el front debe
    // mostrarlos una sola vez (descargar/copiar) y avisar que no se pueden volver a ver.
    // Código inválido = 400 (no 401, para que el front no intente refrescar sesión).
    @PostMapping("/enable")
    public BackupCodesResponse enable(@AuthenticationPrincipal UUID userId,
                                      @Valid @RequestBody CodeRequest req,
                                      HttpServletRequest http) {
        List<String> codes = service.enable(userId, req.code());
        events.publishEvent(new TwoFactorEvent(userId, TwoFactorEvent.Type.ENABLED, http.getRemoteAddr()));
        return new BackupCodesResponse(codes);
    }

    // DESACTIVAR: exige contraseña + código (app o respaldo), para que alguien con una
    // sesión robada no pueda quitarlo. Borra el secreto y los códigos de respaldo.
    // Al volver a activarlo se genera un secreto y códigos nuevos (nada se reutiliza).
    // No limpia cookies ni revoca sesiones. Dispara auditoría + correo de aviso.
    @PostMapping("/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@AuthenticationPrincipal UUID userId,
                        @Valid @RequestBody DisableRequest req,
                        HttpServletRequest http) {
        service.disable(userId, req.password(), req.code());
        events.publishEvent(new TwoFactorEvent(userId, TwoFactorEvent.Type.DISABLED, http.getRemoteAddr()));
    }
}