package gis_isp.person;

import gis_isp.person.dto.PersonResponse;
import gis_isp.person.dto.CreatePersonRequest;

import java.util.List;
import java.util.UUID;

public interface PersonService {

    // Create person
    PersonEntity createPerson(CreatePersonRequest person);

    // Update person
    PersonEntity updatePerson(UUID id, CreatePersonRequest person);

    // Delete person
    void deletePerson(UUID id);

    // Get person by id
    PersonResponse getPersonById(UUID id);

    // Get all persons
    List<PersonResponse> getAllPersons();
}
