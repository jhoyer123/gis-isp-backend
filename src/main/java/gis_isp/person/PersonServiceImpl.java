package gis_isp.person;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    // Create Person
    @Override
    public PersonDto createPerson(PersonRequestDto request) {

        if( personRepository.existsByCi(request.ci()) ) {
            throw new IllegalArgumentException("CI already exists");
        }

        if( personRepository.existsByPhone(request.phone()) ) {
            throw new IllegalArgumentException("Phone already exists");
        }

        PersonEntity personEntity = PersonEntity.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .ci(request.ci())
                .build();

        PersonEntity personSaved =  personRepository.save(personEntity);

        return new PersonDto(
                personSaved.getId(),
                personSaved.getFirstName(),
                personSaved.getLastName(),
                personSaved.getPhone(),
                personSaved.getCi(),
                personSaved.getCreatedAt()
        );
    }


}
