package com.motete.mango.ecommerce_backend.service;

import com.motete.mango.ecommerce_backend.exception.EmailFailureException;
import com.motete.mango.ecommerce_backend.model.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${email.from}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String url;

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    private SimpleMailMessage makeMakeMessage() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromAddress);

        return simpleMailMessage;
    }

    public void sendVerificationEmail(VerificationToken verificationToken) throws EmailFailureException {
        SimpleMailMessage message = makeMakeMessage();
        message.setTo(verificationToken.getUser().getEmail());
        message.setSubject("Verify your email to active your account");
        message.setText("Please follow the link below to verify your email to active your account.\n" +
                url + "/api/auth/verify?token="+verificationToken.getToken());

        try {
            javaMailSender.send(message);
        } catch (MailException e){
            throw new EmailFailureException();
        }
    }
}
