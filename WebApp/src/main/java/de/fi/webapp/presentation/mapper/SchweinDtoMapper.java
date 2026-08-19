package de.fi.webapp.presentation.mapper;

import de.fi.webapp.presentation.dto.SchweinDto;
import de.fi.webapp.service.model.Schwein;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SchweinDtoMapper {

    SchweinDto convert(Schwein schwein);
    Schwein convert(SchweinDto schweinDto);
    Iterable<SchweinDto> convert(Iterable<Schwein> schweine);
}
