package de.fi.webapp;


import de.fi.webapp.event.PersonCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MyEventListener {

    @EventListener
    public void handlePersonCreatedEvent(PersonCreatedEvent event) {
        System.out.println("PersonCreatedEvent wurde ausgloest");
        System.out.println(event.toString());
    }
}
