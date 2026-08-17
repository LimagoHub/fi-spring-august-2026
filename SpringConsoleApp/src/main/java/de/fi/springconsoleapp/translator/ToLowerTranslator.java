package de.fi.springconsoleapp.translator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
//@Qualifier("lower")
//@Primary
@Profile("test")
public class ToLowerTranslator implements Translator {
    @Override
    public String translate(final String input) {
        return input.toLowerCase();
    }
}
