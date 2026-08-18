package de.fi.webapp.service.internal;


import de.fi.webapp.persistence.repository.PersonenRepository;
import de.fi.webapp.service.mapper.PersonMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {
    @InjectMocks
    private PersonServiceImpl objectUnderTest;

    @Mock
    private PersonenRepository personRepositoryMock;

    @Mock
    private PersonMapper mapperMock;
}