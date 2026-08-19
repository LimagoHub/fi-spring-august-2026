package de.fi.webapp;


import de.fi.webapp.persistence.entity.PersonEntity;
import de.fi.webapp.persistence.repository.PersonenRepository;
import de.fi.webapp.service.PersonenService;
import de.fi.webapp.service.exception.PersonenServiceException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class Demo {

    private final PersonenService service;

    public Demo(final PersonenService service) {
        this.service = service;
    }

    @PostConstruct
    private void init() throws Exception{
        //PersonEntity p = PersonEntity.builder().id(UUID.randomUUID()).vorname("Max").nachname("Mustermann").build();
        //personenRepository.save(p);

        var maxe = service.findeAlle() ;

        maxe.forEach(System.out::println);


    }
}
