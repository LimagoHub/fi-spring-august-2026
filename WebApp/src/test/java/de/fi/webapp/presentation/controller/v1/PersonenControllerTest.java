package de.fi.webapp.presentation.controller.v1;

import de.fi.webapp.presentation.dto.PersonDto;
import de.fi.webapp.service.PersonenService;
import de.fi.webapp.service.exception.PersonenServiceException;
import de.fi.webapp.service.model.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ExtendWith(SpringExtension.class)
@Sql({"/create.sql", "/insert.sql"})
class PersonenControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private PersonenService personenServiceMock;

    @Test
    void findByTest() throws PersonenServiceException {
        final var optionalPerson = Optional.of(new Person(UUID.fromString("b2e24e74-8686-43ea-baff-d9396b4202e0"),"John","Doe"));

        // Recordmode
        when(personenServiceMock.findeNachId(any())).thenReturn(optionalPerson);

        var personDto = restTemplate.getForObject("/v1/personen/b2e24e74-8686-43ea-baff-d9396b4202e0", PersonDto.class);
        assertEquals("John", personDto.getVorname());
        verify(personenServiceMock, times(1)).findeNachId(UUID.fromString("b2e24e74-8686-43ea-baff-d9396b4202e0"));
    }

    @Test
    void test2() throws PersonenServiceException {
        final var optionalPerson = Optional.of(new Person(UUID.fromString("b2e24e74-8686-43ea-baff-d9396b4202e0"), "John", "Doe"));

        // Recordmode
        when(personenServiceMock.findeNachId(any())).thenReturn(optionalPerson);

        var personDto = restTemplate.getForObject("/v1/personen/b2e24e74-8686-43ea-baff-d9396b4202e0", String.class);

        System.out.println(personDto);
    }

    @Test
    void test3() throws PersonenServiceException {
        final var optionalPerson = Optional.of(new Person(UUID.fromString("b2e24e74-8686-43ea-baff-d9396b4202e0"), "John", "Doe"));

        // Recordmode
        when(personenServiceMock.findeNachId(any())).thenReturn(optionalPerson);

        var entity = restTemplate.getForEntity("/v1/personen/b2e24e74-8686-43ea-baff-d9396b4202e0", PersonDto.class);

        var personDto = entity.getBody();
        assertEquals("John", personDto.getVorname());
        verify(personenServiceMock, times(1)).findeNachId(UUID.fromString("b2e24e74-8686-43ea-baff-d9396b4202e0"));
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    void test4() throws PersonenServiceException {

        final Optional<Person> optionalPerson = Optional.empty();

        // Recordmode
        when(personenServiceMock.findeNachId(any())).thenReturn(optionalPerson);

        // Replay

        var entity = restTemplate.getForEntity("/v1/personen/b2e24e74-8686-43ea-baff-d9396b4202e0", PersonDto.class);

        var personDto = entity.getBody();
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());

        verify(personenServiceMock, times(1)).findeNachId(UUID.fromString("b2e24e74-8686-43ea-baff-d9396b4202e0"));

    }

    @Test
    void test5() throws PersonenServiceException {

        var personZumHochladen = new PersonDto(UUID.randomUUID(),"John","Doe");
        HttpEntity<PersonDto> request = new HttpEntity<>(personZumHochladen);

        var personen = List.of(
                new Person(UUID.randomUUID(),"John","Doe"),
                new Person(UUID.randomUUID(),"Jane","Doe"));
        when(personenServiceMock.findeAlle()).thenReturn(personen);


        var entity = restTemplate.exchange("/v1/personen", HttpMethod.GET,request,new ParameterizedTypeReference<List<PersonDto>>() { }) ;

        var liste = entity.getBody();
        assertEquals(2,liste.size());
    }

}