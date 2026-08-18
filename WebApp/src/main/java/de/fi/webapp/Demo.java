package de.fi.webapp;


import de.fi.webapp.persistence.entity.PersonEntity;
import de.fi.webapp.persistence.repository.PersonenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Demo {

    private final PersonenRepository personenRepository;

    @PostConstruct
    private void init() {
        //PersonEntity p = PersonEntity.builder().id(UUID.randomUUID()).vorname("Max").nachname("Mustermann").build();
        //personenRepository.save(p);

        var maxe = personenRepository.findAll();

        maxe.forEach(System.out::println);

        System.out.println(personenRepository.count());
    }
}
