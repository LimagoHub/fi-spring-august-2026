package de.fi.webapp;


import de.fi.webapp.persistence.entity.PersonEntity;
import de.fi.webapp.persistence.repository.PersonenRepository;
import de.fi.webapp.service.MailServiceDummy;
import de.fi.webapp.service.PersonenService;
import de.fi.webapp.service.exception.PersonenServiceException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component

public class Demo {

    private final MailServiceDummy mailServiceDummy;

    public Demo(final MailServiceDummy mailServiceDummy) {
        this.mailServiceDummy = mailServiceDummy;
        System.out.println(mailServiceDummy);
    }
}
