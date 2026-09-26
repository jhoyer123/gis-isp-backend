package gis_isp.person;

import gis_isp.common.exception.ResourceAlreadyExistsException;
import gis_isp.common.exception.ResourceNotFoundException;
import gis_isp.person.dto.PersonResponse;
import gis_isp.person.dto.CreatePersonRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    // Create Person
    @Override
    public PersonEntity createPerson(CreatePersonRequest request) {

        if( personRepository.existsByCi(request.ci()) )
            throw new ResourceAlreadyExistsException("El CI ya está registrado");

        PersonEntity person = PersonEntity.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .ci(request.ci())
                .build();

        return personRepository.save(person);
    }

    // Update Person
    @Override
    public PersonEntity updatePerson(UUID id, CreatePersonRequest request) {

        PersonEntity person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));

        if( personRepository.existsByCiAndIdNot(request.ci(), id) )
            throw new ResourceAlreadyExistsException("El CI ya está registrado");

        person.setFirstName(request.firstName());
        person.setLastName(request.lastName());
        person.setPhone(request.phone());
        person.setCi(request.ci());

        return personRepository.save(person);
    }

    // Delete Person
    @Override
    public void deletePerson(UUID id) {

        PersonEntity person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));

        personRepository.delete(person);
    }

    // Get Person by ID
    @Override
    public PersonResponse getPersonById(UUID id) {
        PersonEntity person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));

        return new PersonResponse(
                person.getId(),
                person.getFirstName(),
                person.getLastName(),
                person.getPhone(),
                person.getCi(),
                person.getCreatedAt()
        );
    }

    // Get All Persons
    @Override
    public List<PersonResponse> getAllPersons() {
        return personRepository.findAll()
                .stream()
                .map(person -> new PersonResponse(
                        person.getId(),
                        person.getFirstName(),
                        person.getLastName(),
                        person.getPhone(),
                        person.getCi(),
                        person.getCreatedAt()
                ))
                .toList();
    }

}
