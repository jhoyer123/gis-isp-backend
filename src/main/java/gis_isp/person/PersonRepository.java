package gis_isp.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonRepository extends JpaRepository<PersonEntity, UUID> {

    // Already exists ci?
    boolean existsByCi(String ci);

    // Already exists ci exclude user updating
    boolean existsByCiAndIdNot(String ci, UUID id);

}
