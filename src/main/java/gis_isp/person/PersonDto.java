package gis_isp.person;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PersonDto(

        UUID id,
        String firstName,
        String lastName,
        String phone,
        String ci,
        OffsetDateTime createdAt

) {}
