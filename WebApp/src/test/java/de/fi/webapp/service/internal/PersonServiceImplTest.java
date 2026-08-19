package de.fi.webapp.service.internal;


import de.fi.webapp.persistence.repository.PersonenRepository;
import de.fi.webapp.service.exception.PersonenServiceException;
import de.fi.webapp.service.mapper.PersonMapper;
import de.fi.webapp.service.model.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {
    @InjectMocks
    private PersonServiceImpl objectUnderTest;

    @Mock
    private PersonenRepository personRepositoryMock;

    @Mock
    private PersonMapper mapperMock;

    @Mock
    private List<String> antipathenMock;

    @Test
    void speichern__person_is_null__PersoneServiceExceotionIsThrown() {
        final PersonenServiceException ex = assertThrows(PersonenServiceException.class, ()->objectUnderTest.speichern(null));
        assertEquals("Person darf nicht null sein", ex.getMessage());
    }
}