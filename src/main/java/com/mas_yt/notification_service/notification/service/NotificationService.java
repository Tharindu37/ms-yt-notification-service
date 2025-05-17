//package com.mas_yt.notification_service.notification.service;
//
//import com.mas_yt.notification_service.order.event.OrderPlacedEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.mail.MailException;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.mail.javamail.MimeMessagePreparator;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
////@RequiredArgsConstructor
//public class NotificationService {
//
//    private final JavaMailSender javaMailSender;
//
//    public NotificationService(JavaMailSender javaMailSender) {
//        this.javaMailSender = javaMailSender;
//    }
//
//    @KafkaListener(topics = "order-placed")
//    public void listen(OrderPlacedEvent orderPlacedEvent){
//        log.info("Got Message from order-placed topic {}", orderPlacedEvent);
//        // Send email to the customer
//        MimeMessagePreparator messagePreparator = mimeMessage -> {
//            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
//            messageHelper.setFrom("tharindut520@email.com");
//            messageHelper.setTo(orderPlacedEvent.getEmail());
//            messageHelper.setSubject(String.format("Your Order with OrderNumber %s is placed successfully", orderPlacedEvent.getOrderNumber()));
//            messageHelper.setText(String.format("""
//                            Hi %s,%s
//
//                            Your order with order number %s is now placed successfully.
//
//                            Best Regards
//                            Spring Shop
//                            """,
//                    orderPlacedEvent.getOrderNumber()));
//        };
//        try {
//            javaMailSender.send(messagePreparator);
//            log.info("Order Notifcation email sent!!");
//        } catch (MailException e) {
//            log.error("Exception occurred when sending mail", e);
//            throw new RuntimeException("Exception occurred when sending mail to springshop@email.com", e);
//        }
//
//    }
//}

package com.mas_yt.notification_service.notification.service;

import com.mas_yt.notification_service.order.event.OrderPlacedEventAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
//@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    public NotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @KafkaListener(topics = "order-placed")
//    public void listen(OrderPlacedEventAvro orderPlacedEvent) {
    public void listen(GenericRecord orderPlacedEvent) {
        try {
            log.info("Received order event: {}", orderPlacedEvent);

            // Validate required fields
//            if (orderPlacedEvent.getEmail() == null || orderPlacedEvent.getOrderNumber() == null) {
//                throw new IllegalArgumentException("Missing required fields in order event");
//            }

            // Extract fields using the get(String) method
            String email = orderPlacedEvent.get("email").toString();
            String orderNumber = orderPlacedEvent.get("orderNumber").toString();

            // Validate required fields
            if (email == null || orderNumber == null) {
                throw new IllegalArgumentException("Missing required fields in order event");
            }
            //sendOrderConfirmationEmail(orderPlacedEvent);
            sendOrderConfirmationEmail(orderPlacedEvent);

        } catch (Exception e) {
            log.error("Failed to process order event: {}", orderPlacedEvent, e);
            throw e;
        }
    }

//    private void sendOrderConfirmationEmail(OrderPlacedEventAvro order) {
//        String email = order.getEmail().toString();
//        String orderNumber = order.getOrderNumber().toString();
//
//        // Safely handle nullable name fields
//        String firstName = order.getFirstName() != null ? order.getFirstName().toString() : "";
//        String lastName = order.getLastName() != null ? order.getLastName().toString() : "";
//        String customerName = (firstName + " " + lastName).trim();
//
//        MimeMessagePreparator messagePreparator = mimeMessage -> {
//            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
//            helper.setFrom("tharindut520@email.com");
//            helper.setTo(email);
//            helper.setSubject("Your Order #" + orderNumber + " is confirmed");
//
//            String emailContent = buildEmailContent(customerName, orderNumber);
//            helper.setText(emailContent, true);
//        };
//
//        try {
//            javaMailSender.send(messagePreparator);
//            log.info("Successfully sent confirmation email for order {}", orderNumber);
//        } catch (MailException e) {
//            log.error("Failed to send email for order {}", orderNumber, e);
//            throw new RuntimeException("Email sending failed for order " + orderNumber, e);
//        }
//    }


    private void sendOrderConfirmationEmail(GenericRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Order record is null");
        }

        String email = record.get("email") != null ? record.get("email").toString() : "";
        String orderNumber = record.get("orderNumber") != null ? record.get("orderNumber").toString() : "";

        String firstName = record.get("firstName") != null ? record.get("firstName").toString() : "";
        String lastName = record.get("lastName") != null ? record.get("lastName").toString() : "";
        String customerName = (firstName + " " + lastName).trim();

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("tharindut520@email.com");
            helper.setTo(email);
            helper.setSubject("Your Order #" + orderNumber + " is confirmed");

            String emailContent = buildEmailContent(customerName, orderNumber);
            helper.setText(emailContent, true);
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Successfully sent confirmation email for order {}", orderNumber);
        } catch (MailException e) {
            log.error("Failed to send email for order {}", orderNumber, e);
            throw new RuntimeException("Email sending failed for order " + orderNumber, e);
        }
    }



    private String buildEmailContent(String customerName, String orderNumber) {
        return """
            <html>
                <body>
                    <p>Dear %s,</p>
                    
                    <p>Thank you for your order <strong>#%s</strong>.</p>
                    
                    <p>We've received your order and are processing it. You'll receive another email when your items ship.</p>
                    
                    <p>Best Regards,<br/>
                    Spring Shop Team</p>
                </body>
            </html>
            """.formatted(
                customerName.isEmpty() ? "Customer" : customerName,
                orderNumber
        );
    }
}