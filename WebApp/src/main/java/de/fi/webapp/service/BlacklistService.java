package de.fi.webapp.service;

import de.fi.webapp.service.model.Person;

public interface BlacklistService {

    boolean isBlacklisted(Person possibleBlacklistedPerson);
}
