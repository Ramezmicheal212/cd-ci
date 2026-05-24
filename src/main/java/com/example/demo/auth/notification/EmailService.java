package com.example.demo.auth.notification;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;    
import org.springframework.stereotype.Service;
 import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import java.io.File;
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @Async
    public void sendEmail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendEmailHtmlWithImage(String to, String subject, String htmlBody, String imagePath) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart

    helper.setTo(to);
    helper.setSubject(subject);

    // HTML body مع معرف الصورة cid:hotelImage
    String bodyWithImage = htmlBody + "<br><img src='cid:hotelImage' width='300' />";

    helper.setText(bodyWithImage, true); // true = HTML

    // أضف الصورة كمرفق Inline
    FileSystemResource res = new FileSystemResource(new File(imagePath));
    helper.addInline("hotelImage", res); // معرف الصورة cid:hotelImage

    mailSender.send(message);
    }
}

