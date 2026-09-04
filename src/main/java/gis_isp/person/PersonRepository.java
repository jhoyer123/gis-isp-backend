package gis_isp.person;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<PersonEntity, UUID> {

    // Método derivado por nombre de propiedad
    Optional<PersonEntity> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
    // Verificar si existe una persona con el mismo documento
    boolean existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);

}
