package com.tutorialregister.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails via Gmail SMTP.
 *
 * <p>All methods are {@code @Async} — they run on a separate thread pool so
 * the caller's HTTP response is never delayed by email sending.
 *
 * <p>Set {@code MAIL_ENABLED=false} in your environment to skip real sending
 * (e.g. in local dev or tests) without changing any code.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ------------------------------------------------------------------ //
    //  Fee / Payment emails                                               //
    // ------------------------------------------------------------------ //

    /**
     * Sent to a student after a payment instalment is recorded.
     *
     * @param toEmail     student's email address
     * @param studentName student's full name
     * @param amount      instalment amount paid
     * @param outstanding remaining balance after this payment
     * @param reference   payment reference number (may be null)
     */
    @Async
    public void sendPaymentConfirmation(
            String toEmail,
            String studentName,
            BigDecimal amount,
            BigDecimal outstanding,
            String reference) {

        String subject = "Payment Received – Tutorial Register";
        String body = """
                Dear %s,

                We have received your payment of ₹%s.

                %s

                Remaining balance: ₹%s

                Thank you for your prompt payment.

                Best regards,
                Tutorial Register Team
                """.formatted(
                studentName,
                amount,
                reference != null ? "Payment reference: " + reference : "",
                outstanding);

        send(toEmail, subject, body);
    }

    /**
     * Sent to a student when a fee is fully settled (outstanding = 0).
     */
    @Async
    public void sendFeeClearanceNotice(String toEmail, String studentName, String courseName) {
        String subject = "Fee Cleared – " + courseName;
        String body = """
                Dear %s,

                Congratulations! Your fee for the course "%s" has been fully paid.

                No outstanding balance remains on this course.

                Best regards,
                Tutorial Register Team
                """.formatted(studentName, courseName);

        send(toEmail, subject, body);
    }

    // ------------------------------------------------------------------ //
    //  Assessment emails                                                  //
    // ------------------------------------------------------------------ //

    /**
     * Sent to a student when their assessment result is published.
     *
     * @param toEmail        student's email address
     * @param studentName    student's full name
     * @param assessmentTitle title of the assessment
     * @param marksObtained  marks scored
     * @param maxMarks       maximum possible marks
     * @param assessmentDate date the assessment was held
     */
    @Async
    public void sendAssessmentResult(
            String toEmail,
            String studentName,
            String assessmentTitle,
            BigDecimal marksObtained,
            BigDecimal maxMarks,
            LocalDate assessmentDate) {

        String subject = "Assessment Result Published – " + assessmentTitle;
        String percentage = (maxMarks != null && maxMarks.compareTo(BigDecimal.ZERO) > 0 && marksObtained != null)
                ? marksObtained.multiply(BigDecimal.valueOf(100))
                        .divide(maxMarks, 1, RoundingMode.HALF_UP) + "%"
                : "N/A";

        String body = """
                Dear %s,

                Your result for the assessment "%s" (held on %s) has been published.

                Marks obtained : %s / %s
                Percentage     : %s

                Please log in to your portal to view detailed feedback.

                Best regards,
                Tutorial Register Team
                """.formatted(
                studentName,
                assessmentTitle,
                assessmentDate != null ? assessmentDate.toString() : "N/A",
                marksObtained != null ? marksObtained : "N/A",
                maxMarks != null ? maxMarks : "N/A",
                percentage);

        send(toEmail, subject, body);
    }

    // ------------------------------------------------------------------ //
    //  Enrolment emails                                                   //
    // ------------------------------------------------------------------ //

    /**
     * Sent to a student when they are enrolled into a course.
     */
    @Async
    public void sendEnrolmentConfirmation(String toEmail, String studentName, String courseName) {
        String subject = "Enrolment Confirmed – " + courseName;
        String body = """
                Dear %s,

                You have been successfully enrolled in the course: "%s".

                Please log in to your student portal for class schedules and course materials.

                Best regards,
                Tutorial Register Team
                """.formatted(studentName, courseName);

        send(toEmail, subject, body);
    }

    // ------------------------------------------------------------------ //
    //  Internal helper                                                    //
    // ------------------------------------------------------------------ //

    private void send(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[EmailService] Mail disabled – skipping email to {} | Subject: {}", to, subject);
            return;
        }
        if (to == null || to.isBlank()) {
            log.warn("[EmailService] No email address provided, skipping email | Subject: {}", subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[EmailService] Email sent to {} | Subject: {}", to, subject);
        } catch (MailException ex) {
            // Never let email failure bubble up and break the main business transaction
            log.error("[EmailService] Failed to send email to {} | Subject: {} | Reason: {}",
                    to, subject, ex.getMessage());
        }
    }
}
