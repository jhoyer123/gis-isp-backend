package gis_isp.role.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(

        @NotBlank(message = "El nombre es requerido")
        String name,

        String description

) {}
