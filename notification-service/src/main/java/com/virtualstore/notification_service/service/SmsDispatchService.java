// package com.virtualstore.notification_service.service;

// import com.twilio.Twilio;
// import com.twilio.rest.api.v2010.account.Message;
// import com.twilio.type.PhoneNumber;
// import jakarta.annotation.PostConstruct;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// import java.util.Map;

// /**
//  * SmsDispatchService
//  *
//  * Sends SMS messages via the Twilio REST API.
//  * Builds the message body from a simple text template (no Thymeleaf — SMS is plain text).
//  *
//  * Supported templates (resolved in buildBody()):
//  *   sms-otp  — "Your VirtualStore OTP is {otp}. Valid for {expiryMinutes} minutes."
//  *
//  * Add more templates by extending the switch in buildBody().
//  */
// @Slf4j
// @Service
// public class SmsDispatchService {

//     @Value("${twilio.account-sid}")
//     private String accountSid;

//     @Value("${twilio.auth-token}")
//     private String authToken;

//     @Value("${twilio.from-number}")
//     private String fromNumber;

//     @PostConstruct
//     void init() {
//         Twilio.init(accountSid, authToken);
//         log.info("[SMS] Twilio client initialised (from={})", fromNumber);
//     }

//     /**
//      * Sends an SMS to the given phone number using the named template.
//      *
//      * @param toPhone  E.164 phone number (e.g. +919876543210)
//      * @param template Template key (e.g. "sms-otp")
//      * @param payload  Variables used to build the message body
//      * @return Twilio SID on success
//      * @throws SmsDispatchException on failure
//      */
//     @Async("notificationTaskExecutor")
//     public String send(String toPhone, String template, Map<String, Object> payload) {
//         String body = buildBody(template, payload);

//         try {
//             Message message = Message.creator(
//                             new PhoneNumber(toPhone),
//                             new PhoneNumber(fromNumber),
//                             body)
//                     .create();

//             log.info("[SMS] template='{}' sent to {} — SID={}", template, toPhone, message.getSid());
//             return message.getSid();

//         } catch (Exception ex) {
//             log.error("[SMS] Failed to send template='{}' to {}: {}", template, toPhone, ex.getMessage());
//             throw new SmsDispatchException("Twilio send failed: " + ex.getMessage(), ex);
//         }
//     }

//     // ------------------------------------------------------------------ //
//     //  SMS template resolver (plain text)
//     // ------------------------------------------------------------------ //

//     private String buildBody(String template, Map<String, Object> payload) {
//         return switch (template) {
//             case "sms-otp" -> String.format(
//                     "Your VirtualStore OTP is %s. Valid for %s minutes. Do not share it with anyone.",
//                     payload.getOrDefault("otp", ""),
//                     payload.getOrDefault("expiryMinutes", 10));

//             default -> throw new SmsDispatchException(
//                     "Unknown SMS template: " + template, null);
//         };
//     }

//     // ------------------------------------------------------------------ //

//     public static class SmsDispatchException extends RuntimeException {
//         public SmsDispatchException(String message, Throwable cause) {
//             super(message, cause);
//         }
//     }
// }