package com.bernard.beaconportal.activities.mail.internet;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import android.test.AndroidTestCase;

import com.bernard.beaconportal.activities.activity.MAILActivityCommon;
import com.bernard.beaconportal.activities.mail.Address;
import com.bernard.beaconportal.activities.mail.MessagingException;
import com.bernard.beaconportal.activities.mail.Message.RecipientType;
import com.bernard.beaconportal.activities.mail.internet.MimeUtility.ViewableContainer;

public class ViewablesTest extends AndroidTestCase {

    public void testSimplePlainTextMessage() throws MessagingException {
        String bodyText = "Mail Mail rocks :>";

        // Create text/plain body
        TextBody body = new TextBody(bodyText);

        // Create message
        MimeMessage message = new MimeMessage();
        message.setBody(body);

        // Extract text
        ViewableContainer container = MimeUtility.extractTextAndAttachments(getContext(), message);

        String expectedText = bodyText;
        String expectedHtml =
                "<pre class=\"mailmail\">" +
                "Mail Mail rocks :&gt;" +
                "</pre>";

        assertEquals(expectedText, container.text);
        assertEquals(expectedHtml, container.html);
    }

    public void testSimpleHtmlMessage() throws MessagingException {
        String bodyText = "<strong>Mail Mail</strong> rocks :&gt;";

        // Create text/plain body
        TextBody body = new TextBody(bodyText);

        // Create message
        MimeMessage message = new MimeMessage();
        message.setHeader("Content-Type", "text/html");
        message.setBody(body);

        // Extract text
        ViewableContainer container = MimeUtility.extractTextAndAttachments(getContext(), message);

        String expectedText = "Mail Mail rocks :>";
        String expectedHtml =
                bodyText;

        assertEquals(expectedText, container.text);
        assertEquals(expectedHtml, container.html);
    }

    public void testMultipartPlainTextMessage() throws MessagingException {
        String bodyText1 = "text body 1";
        String bodyText2 = "text body 2";

        // Create text/plain bodies
        TextBody body1 = new TextBody(bodyText1);
        TextBody body2 = new TextBody(bodyText2);

        // Create multipart/mixed part
        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart bodyPart1 = new MimeBodyPart(body1, "text/plain");
        MimeBodyPart bodyPart2 = new MimeBodyPart(body2, "text/plain");
        multipart.addBodyPart(bodyPart1);
        multipart.addBodyPart(bodyPart2);

        // Create message
        MimeMessage message = new MimeMessage();
        message.setBody(multipart);

        // Extract text
        ViewableContainer container = MimeUtility.extractTextAndAttachments(getContext(), message);

        String expectedText =
                bodyText1 + "\r\n\r\n" +
                "------------------------------------------------------------------------\r\n\r\n" +
                bodyText2;
        String expectedHtml =
                "<pre class=\"mailmail\">" +
                bodyText1 +
                "</pre>" +
                "<p style=\"margin-top: 2.5em; margin-bottom: 1em; " +
                        "border-bottom: 1px solid #000\"></p>" +
                "<pre class=\"mailmail\">" +
                bodyText2 +
                "</pre>";


        assertEquals(expectedText, container.text);
        assertEquals(expectedHtml, container.html);
    }

    public void testTextPlusRfc822Message() throws MessagingException {
    	MAILActivityCommon.setLanguage(getContext(), "en");
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+01:00"));

        String bodyText = "Some text here";
        String innerBodyText = "Hey there. I'm inside a message/rfc822 (inline) attachment.";

        // Create text/plain body
        TextBody textBody = new TextBody(bodyText);

        // Create inner text/plain body
        TextBody innerBody = new TextBody(innerBodyText);

        // Create message/rfc822 body
        MimeMessage innerMessage = new MimeMessage();
        innerMessage.addSentDate(new Date(112, 02, 17));
        innerMessage.setRecipients(RecipientType.TO, new Address[] { new Address("to@example.com") });
        innerMessage.setSubject("Subject");
        innerMessage.setFrom(new Address("from@example.com"));
        innerMessage.setBody(innerBody);

        // Create multipart/mixed part
        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart bodyPart1 = new MimeBodyPart(textBody, "text/plain");
        MimeBodyPart bodyPart2 = new MimeBodyPart(innerMessage, "message/rfc822");
        bodyPart2.setHeader("Content-Disposition", "inline; filename=\"message.eml\"");
        multipart.addBodyPart(bodyPart1);
        multipart.addBodyPart(bodyPart2);

        // Create message
        MimeMessage message = new MimeMessage();
        message.setBody(multipart);

        // Extract text
        ViewableContainer container = MimeUtility.extractTextAndAttachments(getContext(), message);

        String expectedText =
                bodyText +
                "\r\n\r\n" +
                "----- message.eml ------------------------------------------------------" +
                "\r\n\r\n" +
                "From: from@example.com" + "\r\n" +
                "To: to@example.com" + "\r\n" +
                "Sent: Sat Mar 17 00:00:00 GMT+01:00 2012" + "\r\n" +
                "Subject: Subject" + "\r\n" +
                "\r\n" +
                innerBodyText;
        String expectedHtml =
                "<pre class=\"mailmail\">" +
                bodyText +
                "</pre>" +
                "<p style=\"margin-top: 2.5em; margin-bottom: 1em; border-bottom: " +
                        "1px solid #000\">message.eml</p>" +
                "<table style=\"border: 0\">" +
                "<tr>" +
                "<th style=\"text-align: left; vertical-align: top;\">From:</th>" +
                "<td>from@example.com</td>" +
                "</tr><tr>" +
                "<th style=\"text-align: left; vertical-align: top;\">To:</th>" +
                "<td>to@example.com</td>" +
                "</tr><tr>" +
                "<th style=\"text-align: left; vertical-align: top;\">Sent:</th>" +
                "<td>Sat Mar 17 00:00:00 GMT+01:00 2012</td>" +
                "</tr><tr>" +
                "<th style=\"text-align: left; vertical-align: top;\">Subject:</th>" +
                "<td>Subject</td>" +
                "</tr>" +
                "</table>" +
                "<pre class=\"mailmail\">" +
                innerBodyText +
                "</pre>";

        assertEquals(expectedText, container.text);
        assertEquals(expectedHtml, container.html);
    }
}
