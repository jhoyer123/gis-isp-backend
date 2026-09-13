package gis_isp.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonRepository extends JpaRepository<PersonEntity, UUID> {
    // Already exists phone?
    boolean  existsByPhone(String phone);

    // Already exists ci?
    boolean existsByCi(String ci);

}
