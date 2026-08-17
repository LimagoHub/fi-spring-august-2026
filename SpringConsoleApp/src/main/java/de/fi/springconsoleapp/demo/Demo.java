package de.fi.springconsoleapp.demo;

import de.fi.springconsoleapp.translator.Translator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
//@Scope("singleton") // default
@Scope("prototype") // kein default
//@Lazy(true)
public class Demo {


    private final Translator translator;


    public Demo( final Translator translator) {
        this.translator = translator;
        System.out.println(translator.translate("Ctor Demo"));
    }

    @PostConstruct
    public void init() {
        System.out.println(translator.translate("Post Construct von Demo"));
    }

    @PreDestroy
    public void peter() {
        System.out.println(translator.translate("Pre Destroy von Demo"));
    }

}
