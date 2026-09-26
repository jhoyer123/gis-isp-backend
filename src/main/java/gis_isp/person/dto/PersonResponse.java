package gis_isp.person.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PersonResponse(

        UUID id,
        String firstName,
        String lastName,
        String phone,
        String ci,
        OffsetDateTime createdAt

) {}
