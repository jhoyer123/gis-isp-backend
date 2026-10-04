package gis_isp.role.dto;

public record RoleListResponse(
        Long id,
        String name,
        String description,
        long usersCount,
        long permissionsCount
) {
}
