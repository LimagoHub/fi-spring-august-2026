package de.fi.webapp.service.config;

import de.fi.webapp.YamlPropertySourceFactory;
import de.fi.webapp.service.MailServiceDummy;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:mail.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix="mail")
@Setter
public class MailConfig {
    private String host;
    private String port;
    private String username;
    private String password;

    @Bean
    public MailServiceDummy MailServiceDummy() {
        return new MailServiceDummy(username, password);
    }
}
