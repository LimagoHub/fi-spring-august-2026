package de.fi.webapp.service.internal;

import de.fi.webapp.persistence.repository.PersonenRepository;
import de.fi.webapp.service.PersonenService;
import de.fi.webapp.service.exception.PersonenServiceException;
import de.fi.webapp.service.mapper.PersonMapper;
import de.fi.webapp.service.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = PersonenServiceException.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class PersonServiceImpl implements PersonenService {

    private final PersonenRepository repo;
    private final PersonMapper mapper;

    /*
        person ist null -> PSE
        vorname ist -> PSE
        vorname zu kurz -> PSE
        das selbe für Nachname
        vorname Attila -> PSE
        technische Exceptions aller art wandeln in PSE
        Happy Day wird person an repo uebergeben

     */
    public void bulkInsert(List<Person> personen) throws PersonenServiceException{
        for (Person person : personen) {
            speichern(person);
        }
    }


    @Override
    public void speichern(Person person) throws PersonenServiceException {
        try {
            if (person == null) throw new PersonenServiceException("Person darf nicht null sein");
            if (person.getVorname() == null ||person.getVorname().length()< 2) throw new PersonenServiceException("Vorname zu kurz");
            if (person.getNachname() == null ||person.getNachname().length()< 2) throw new PersonenServiceException("Nachname zu kurz");
            if (person.getVorname().equals("Attila")) throw new PersonenServiceException("Antipath");
            repo.save(mapper.convert(person));
        } catch (RuntimeException e) {
            throw new PersonenServiceException("Fehler beim Speichern",  e);
        }

    }

    @Override
    public void aendern(final Person person) throws PersonenServiceException {
        try {
            if (person == null) throw new PersonenServiceException("Person darf nicht null sein");
            if (person.getVorname() == null ||person.getVorname().length()< 2) throw new PersonenServiceException("Vorname zu kurz");
            if (person.getNachname() == null ||person.getNachname().length()< 2) throw new PersonenServiceException("Nachname zu kurz");
            if (person.getVorname().equals("Attila")) throw new PersonenServiceException("Antipath");
            repo.save(mapper.convert(person));
        } catch (RuntimeException e) {
            throw new PersonenServiceException("Fehler beim Aendern",  e);
        }
    }

    @Override
    public void loeschen(final UUID uuid) throws PersonenServiceException {
        try {

            repo.deleteById(uuid);
        } catch (RuntimeException e) {
            throw new PersonenServiceException("Fehler beim Loeschen",  e);
        }
    }

    @Transactional(rollbackFor = PersonenServiceException.class, isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Optional<Person> findeNachId(final UUID uuid) throws PersonenServiceException {
        try {
            return repo.findById(uuid).map(mapper::convert);

        } catch (RuntimeException e) {
            throw new PersonenServiceException("Fehler beim Suchen",  e);
        }
    }

    @Override
    public Iterable<Person> findeAlle() throws PersonenServiceException {
        try {
            return mapper.convert(repo.findAll());

        } catch (RuntimeException e) {
            throw new PersonenServiceException("Fehler beim Suchen",  e);
        }
    }
}
