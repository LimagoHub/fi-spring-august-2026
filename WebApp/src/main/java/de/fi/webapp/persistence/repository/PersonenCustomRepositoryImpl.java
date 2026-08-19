package de.fi.webapp.persistence.repository;


import de.fi.webapp.persistence.entity.PersonEntity;
import de.fi.webapp.service.exception.AlreadyExistsException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PersonenCustomRepositoryImpl implements PersonenCustomRepository {

    @PersistenceContext
    private EntityManager em;


    @Override
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void onlySave(final PersonEntity personEntity) {
        try {
            em.persist(personEntity);
            em.flush();
        } catch (EntityExistsException | DataIntegrityViolationException e) {
            throw new AlreadyExistsException(e.getMessage());
        }
    }
}
