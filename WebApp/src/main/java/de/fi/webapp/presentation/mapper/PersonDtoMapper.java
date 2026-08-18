package de.fi.webapp.presentation.mapper;


import de.fi.webapp.presentation.dto.PersonDto;
import de.fi.webapp.service.model.Person;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonDtoMapper {

    PersonDto convert(Person person);
    Person convert(PersonDto personDto);
    Iterable<PersonDto> convert(Iterable<Person> persons);
}
