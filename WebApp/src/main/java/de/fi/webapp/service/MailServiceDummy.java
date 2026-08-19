package de.fi.webapp.service;


public class MailServiceDummy {

    private final String username;
    private final String password;

    public MailServiceDummy(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MailServiceDummy{");
        sb.append("username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void send(final String subject, final String text) {
        System.out.println( "MailServiceDummy: " + subject + " " + text);
        System.out.println(this);
    }
}
