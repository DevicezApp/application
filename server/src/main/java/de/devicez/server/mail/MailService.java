package de.devicez.server.mail;

import com.google.common.base.Preconditions;
import de.devicez.server.DeviceZServerApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MailService {

    private final DeviceZServerApplication application;
    private final Mailer mailer;

    private final Map<String, MailTemplate> templateMap = new HashMap<>();

    public MailService(final DeviceZServerApplication application) {
        this.application = application;
        this.mailer = MailerBuilder
                .withSMTPServer(
                        application.getConfig().getStringOrDefault("mail-host", "localhost"),
                        application.getConfig().getIntOrDefault("mail-port", 465),
                        application.getConfig().getStringOrDefault("mail-username", "mailer"),
                        application.getConfig().getStringOrDefault("mail-password", "password"))
                .withTransportStrategy(TransportStrategy.SMTPS)
                .buildMailer();

        loadTemplates();
    }

    private void loadTemplates() {
        final File templatesFolder = new File(application.getConfig().getStringOrDefault("mail-templates", "mails/"));
        final File[] templates = templatesFolder.listFiles(File::isFile);
        if (templates == null) throw new IllegalArgumentException("invalid mail template folder");

        for (final File file : templates) {
            try {
                final String name = FilenameUtils.removeExtension(file.getName());
                final String content = Files.readString(file.toPath());
                final Document document = Jsoup.parse(content);
                templateMap.put(name, new MailTemplate(name, document.title(), document.body().html()));
            } catch (final IOException e) {
                log.error("Error while reading mail template '{}'", file.getAbsolutePath(), e);
            }
        }
    }

    public void sendMail(final String recipientName, final String recipientAddress,
                         final String templateName, final String... replacements) {
        final MailTemplate mailTemplate = templateMap.get(templateName);
        if (mailTemplate == null) {
            throw new IllegalArgumentException("Invalid template: " + templateName);
        }

        sendMail(recipientName, recipientAddress, mailTemplate, replacements);
    }

    public void sendMail(final String recipientName, final String recipientAddress,
                         final MailTemplate template, final String... replacements) {
        Preconditions.checkNotNull(recipientAddress, "recipientAddress may not be null");
        Preconditions.checkNotNull(template, "template may not be null");
        Preconditions.checkNotNull(replacements, "replacements may not be null");

        // Handling parameters
        String subject = applyReplacements(template.subject(), replacements);
        String content = template.content();
        Preconditions.checkNotNull(content, "content may not be null");

        content = applyReplacements(content, replacements);

        // Sending mail
        final Email email = EmailBuilder.startingBlank()
                .to(recipientName, recipientAddress)
                .from(application.getInformation().getOrganisationName(), mailer.getServerConfig().getUsername())
                .withSubject(subject)
                .withHTMLText(content)
                .buildEmail();
        mailer.sendMail(email, true).whenComplete((it, throwable) -> {
            if (throwable != null) {
                log.error("An error occurred while sending mail with template '{}' to '{} <{}>'",
                        template.name(), recipientName, recipientAddress, throwable);
                return;
            }

            log.info("Mail with template '{}' successfully sent to '{} <{}>'",
                    template.name(), recipientName, recipientAddress);
        });
    }

    private String applyReplacements(String initialString, final String... replacements) {
        for (int i = 0; i < replacements.length; i += 2) {
            initialString = initialString.replace(replacements[i], replacements[i + 1]);
        }
        return initialString;
    }
}
