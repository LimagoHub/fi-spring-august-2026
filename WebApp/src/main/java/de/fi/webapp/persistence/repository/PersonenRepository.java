package de.fi.webapp.persistence.repository;

import de.fi.webapp.persistence.entity.PersonEntity;
import de.fi.webapp.persistence.entity.TinyPerson;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PersonenRepository extends CrudRepository<PersonEntity, UUID>, PersonenCustomRepository {

    Iterable<PersonEntity> findByVorname(String vorname);

    @Query("select new de.fi.webapp.persistence.entity.TinyPerson(p.id, p.nachname) from PersonEntity p")
    Iterable<TinyPerson> egal();

    Iterable<TinyPerson> findAllProjectByVorname(String vorname);
}
