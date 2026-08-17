package de.fi.springconsoleapp.demo;

import de.fi.springconsoleapp.translator.Translator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Component
@Scope("singleton") // default
//@Scope("prototype") // kein default
//@Lazy(true)
@RequiredArgsConstructor
public class Demo {


    private final Translator translator;

    @Value("${Demo.gruss}")
    private final String message;



    @PostConstruct
    public void init() {
        System.out.println(translator.translate("Post Construct von Demo"));
        System.out.println(message);
    }

    @PreDestroy
    public void peter() {
        System.out.println(translator.translate("Pre Destroy von Demo"));
    }

}
