package de.fi.springconsoleapp.translator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
//@Qualifier("upper")
@Profile({"dev","production"})
public class ToUpperTranslator implements Translator {
    @Override
    public String translate(String input) {
        return input.toUpperCase();
    }
}
