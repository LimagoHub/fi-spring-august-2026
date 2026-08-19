package de.fi.webapp.event;

import de.fi.webapp.presentation.dto.PersonDto;

import java.util.UUID;

public record PersonCreatedEvent(UUID id, String vorname, String nachname) {
}
