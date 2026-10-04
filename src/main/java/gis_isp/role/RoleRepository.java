package gis_isp.role;

import gis_isp.role.dto.RoleListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    // ya existe un rol con el mismo nombre?
    boolean existsByName(String role);

    // obtener lista de roles
    @Query(value = """
            select new gis_isp.role.dto.RoleListResponse(
                r.id,
                r.name,
                r.description,
                (select count(u) from UserEntity u where u.role = r),
                size(r.permissions)
            )
            from RoleEntity r
            where lower(r.name) like lower(concat('%', trim(:search), '%')) or lower(r.description) like lower(concat('%', trim(:search), '%'))
            """,
            countQuery = """
                    select count(r) from RoleEntity r
                    where lower(r.name) like lower(concat('%', trim(:search), '%')) or lower(r.description) like lower(concat('%', trim(:search), '%'))
                    """)
    Page<RoleListResponse> findAllWithCounts(@Param("search") String search, Pageable pageable);

}
