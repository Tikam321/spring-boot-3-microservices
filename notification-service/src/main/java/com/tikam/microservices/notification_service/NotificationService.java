package com.tikam.microservices.notification_service;

import com.tikam.microservices.order_service.event.OrderConfirmedEvent;
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

    @KafkaListener(topics = "order-confirmed")
    public void listen(OrderConfirmedEvent orderConfirmedEvent) {
        log.info("Got message from order-confirmed topic {}", orderConfirmedEvent);
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(orderConfirmedEvent.getEmail().toString());
            messageHelper.setSubject(String.format("Your Order with OrderNumber %s is confirmed", orderConfirmedEvent.getOrderNumber()));
            messageHelper.setText(String.format("""
                            Hi %s,%s

                            Your Order with order number %s is now confirmed.

                            Best Regards
                            Spring Shop
                            """,
                    orderConfirmedEvent.getFirstName().toString(),
                    orderConfirmedEvent.getLastName().toString(),
                    orderConfirmedEvent.getOrderNumber()));
        };
        try {
            javaMailSender.send(messagePreparator);
            log.info("Order Confirmation email sent!!");
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
        }
    }
}
