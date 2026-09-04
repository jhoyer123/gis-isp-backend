package gis_isp.person;

import jakarta.persistence.*;
import gis_isp.user.UserEntity;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "persons",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_person_document",
                        columnNames = {"document_type", "document_number"}
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id") // indica que la entidad es unica solo por su id
@ToString // lo trasforma a string bueno para debuguear
public class PersonEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(name = "document_type", length = 30)
    private String documentType;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    //@OneToOne(mappedBy = "person", fetch = FetchType.LAZY)
    //private UserEntity user;
}
