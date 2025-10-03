package com.tikam.microservices.notification_service;

import com.tikam.microservices.order_service.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static Logger log =  LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender javaMailSender;

    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @KafkaListener(topics = "order-placed")
    public void listen(OrderPlacedEvent orderPlaced) {
        log.info("Got message from OrderPlacedEvent-service topic {}", orderPlaced);
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(orderPlaced.getEmail().toString());
            messageHelper.setSubject(String.format("Your OrderPlacedEvent with OrderNumber %s is placed successfully", orderPlaced.getOrderNumber()));
            messageHelper.setText(String.format("""
                            Hi %s,%s

                            Your OrderPlacedEvent with OrderPlacedEvent number %s is now placed successfully.

                            Best Regards
                            Spring Shop
                            """,
                    orderPlaced.getFirstName().toString(),
                    orderPlaced.getLastName().toString(),
                    orderPlaced.getOrderNumber()));
        };
        try {
            javaMailSender.send(messagePreparator);
            log.info("OrderPlacedEvent Notifcation email sent!!");
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
        }
    }
}
