package de.fi.webapp.service.mapper;

import de.fi.webapp.persistence.entity.SchweinEntity;
import de.fi.webapp.service.model.Schwein;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SchweinMapper {
    Schwein convert(SchweinEntity schweinEntity);
    SchweinEntity convert(Schwein schwein);
    Iterable<Schwein> convert(Iterable<SchweinEntity> schweinEntities);
}
