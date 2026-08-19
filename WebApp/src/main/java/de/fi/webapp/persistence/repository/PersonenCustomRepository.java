package de.fi.webapp.persistence.repository;


import de.fi.webapp.persistence.entity.PersonEntity;

public interface PersonenCustomRepository {

    void onlySave(PersonEntity personEntity) ;
}
