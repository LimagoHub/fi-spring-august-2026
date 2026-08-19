package de.fi.webapp.service.config;


import de.fi.webapp.persistence.repository.PersonenRepository;
import de.fi.webapp.service.PersonenService;
import de.fi.webapp.service.internal.PersonServiceImpl;
import de.fi.webapp.service.mapper.PersonMapper;
import de.fi.webapp.service.model.Person;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Configuration
public class PersonConfig {

    @Bean
    @Qualifier("antipathen")
    public List<String> createAntipathen() {
        return List.of("Attila", "Peter","Paul", "Mary");
    }

    @Bean
    @Qualifier("fruits")
    public List<String> createFruits() {
        return List.of("Banana", "Cherry","Strawberry", "Raspberry");
    }

    /*@Bean
    public PersonenService createPersonenService(final PersonenRepository repo, final PersonMapper mapper, @Qualifier("antipathen") final List<String> antipathen) {
        return new PersonServiceImpl(repo,mapper, antipathen );
    }*/
}
